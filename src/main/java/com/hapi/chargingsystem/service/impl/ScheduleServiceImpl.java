package com.hapi.chargingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.common.enums.PileStatus;
import com.hapi.chargingsystem.common.enums.PileType;
import com.hapi.chargingsystem.common.enums.RequestStatus;
import com.hapi.chargingsystem.common.exception.BusinessException;
import com.hapi.chargingsystem.common.strategy.DispatchStrategy;
import com.hapi.chargingsystem.common.utils.QueueNumberGenerator;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.domain.PileQueue;
import com.hapi.chargingsystem.mapper.ChargingPileMapper;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import com.hapi.chargingsystem.mapper.PileQueueMapper;
import com.hapi.chargingsystem.service.BillingService;
import com.hapi.chargingsystem.service.ScheduleService;
import com.hapi.chargingsystem.service.SystemParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    private ChargingRequestMapper requestMapper;

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private PileQueueMapper pileQueueMapper;

    @Autowired
    private SystemParamService systemParamService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private DispatchStrategy dispatchStrategy;

    @Autowired
    private QueueNumberGenerator queueNumberGenerator;

    // 调度锁，防止并发调度
    private final Object scheduleLock = new Object();

    // 是否正在调度中
    private volatile boolean scheduling = false;

    // 等候区叫号服务是否暂停
    private volatile boolean waitingAreaPaused = false;

    @Override
    @Transactional
    public void triggerSchedule() {
        // 使用双重检查锁定模式，避免频繁获取锁
        if (!scheduling) {
            synchronized (scheduleLock) {
                if (!scheduling) {
                    try {
                        scheduling = true;
                        doSchedule();
                    } finally {
                        scheduling = false;
                    }
                }
            }
        }
    }

    /**
     * 执行调度
     */
    private void doSchedule() {
        if (waitingAreaPaused) {
            logger.info("等候区叫号服务已暂停，跳过调度");
            return;
        }

        // 1. 获取所有正常工作的充电桩
        List<ChargingPile> activePiles = pileMapper.selectList(
                new LambdaQueryWrapper<ChargingPile>()
                        .eq(ChargingPile::getStatus, PileStatus.NORMAL.getCode()));

        if (activePiles.isEmpty()) {
            logger.info("没有正常工作的充电桩，跳过调度");
            return;
        }

        // 2. 获取所有充电桩队列
        int queueLen = systemParamService.getChargingQueueLen();
        Map<Long, List<PileQueue>> pileQueues = new HashMap<>();
        for (ChargingPile pile : activePiles) {
            pileQueues.put(pile.getId(), pileQueueMapper.findQueueByPileId(pile.getId()));
        }

        // 3. 获取所有等候区请求（按排队号码排序）
        List<ChargingRequest> waitingRequests = new ArrayList<>();
        waitingRequests.addAll(requestMapper.getWaitingRequestsByMode(ChargingMode.FAST.getCode()));
        waitingRequests.addAll(requestMapper.getWaitingRequestsByMode(ChargingMode.TRICKLE.getCode()));
        waitingRequests.sort(Comparator.comparing(ChargingRequest::getQueueNumber));

        // 4. 依次分配请求到有空位的充电桩
        for (ChargingRequest request : waitingRequests) {
            // 找出同类型且有空位的充电桩
            List<ChargingPile> availablePiles = activePiles.stream()
                    .filter(p -> p.getPileType().equals(
                            request.getChargingMode().equals(ChargingMode.FAST.getCode()) ?
                                    PileType.FAST.getCode() : PileType.TRICKLE.getCode()))
                    .filter(p -> pileQueues.get(p.getId()).size() < queueLen)
                    .collect(Collectors.toList());

            if (availablePiles.isEmpty()) {
                continue;
            }

            // 构建充电桩队列映射
            Map<Long, List<ChargingRequest>> pileQueueMap = new HashMap<>();
            for (ChargingPile availablePile : availablePiles) {
                List<ChargingRequest> queueRequests = pileQueues.get(availablePile.getId()).stream()
                        .map(q -> requestMapper.selectById(q.getRequestId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                pileQueueMap.put(availablePile.getId(), queueRequests);
            }

            // 使用调度策略选择最佳充电桩
            Long bestPileId = dispatchStrategy.selectBestPile(request, availablePiles, pileQueueMap);

            if (bestPileId != null) {
                assignRequestToPile(request, bestPileId);
                // 更新本地队列缓存
                pileQueues.get(bestPileId).add(new PileQueue()); // 只需保证size+1即可
            }
        }
    }

    /**
     * 将请求分配到充电桩队列
     */
    private void assignRequestToPile(ChargingRequest request, Long pileId) {
        // 获取当前充电桩队列
        List<PileQueue> pileQueue = pileQueueMapper.findQueueByPileId(pileId);

        // 确定新请求在队列中的位置
        int position = pileQueue.isEmpty() ? 0 : pileQueue.size();

        // 创建队列记录
        PileQueue queueItem = new PileQueue();
        queueItem.setPileId(pileId);
        queueItem.setRequestId(request.getId());
        queueItem.setPosition(position);
        queueItem.setEnterTime(LocalDateTime.now());
        queueItem.setCreateTime(LocalDateTime.now());
        queueItem.setUpdateTime(LocalDateTime.now());

        pileQueueMapper.insert(queueItem);

        // 更新请求状态
        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingRequest::getId, request.getId())
                .set(ChargingRequest::getPileId, pileId)
                .set(ChargingRequest::getQueuePosition, position);

        // 如果是队列首位，则开始充电
        if (position == 0) {
            updateWrapper.set(ChargingRequest::getStatus, RequestStatus.CHARGING.getCode())
                    .set(ChargingRequest::getChargingStartTime, LocalDateTime.now());

            // 更新充电桩统计数据
            updatePileStatistics(pileId);
        }

        updateWrapper.set(ChargingRequest::getUpdateTime, LocalDateTime.now());

        requestMapper.update(null, updateWrapper);

        logger.info("请求 {} 已分配到充电桩 {}, 位置 {}", request.getId(), pileId, position);
    }

    /**
     * 更新充电桩统计数据
     */
    private void updatePileStatistics(Long pileId) {
        LambdaUpdateWrapper<ChargingPile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingPile::getId, pileId)
                .setSql("total_charging_times = total_charging_times + 1")
                .set(ChargingPile::getUpdateTime, LocalDateTime.now());

        pileMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void releasePile(Long pileId) {
        // 获取充电桩队列
        List<PileQueue> pileQueue = pileQueueMapper.findQueueByPileId(pileId);

        if (pileQueue.isEmpty()) {
            return;
        }

        // 找到正在充电的请求（位置0）
        Optional<PileQueue> chargingItem = pileQueue.stream()
                .filter(item -> item.getPosition() == 0)
                .findFirst();

        if (chargingItem.isEmpty()) {
            return;
        }

        // 删除队列中的该项
        pileQueueMapper.deleteById(chargingItem.get().getId());

        // 更新队列中其他项的位置
        for (PileQueue item : pileQueue) {
            if (item.getPosition() > 0) {
                LambdaUpdateWrapper<PileQueue> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(PileQueue::getId, item.getId())
                        .set(PileQueue::getPosition, item.getPosition() - 1)
                        .set(PileQueue::getUpdateTime, LocalDateTime.now());

                pileQueueMapper.update(null, updateWrapper);

                // 如果更新后位置为0，则开始充电
                if (item.getPosition() - 1 == 0) {
                    startCharging(item.getRequestId(), pileId);
                }
            }
        }

        // 触发调度，尝试从等候区调入新的车辆
        triggerSchedule();
    }

    /**
     * 开始充电
     */
    private void startCharging(Long requestId, Long pileId) {
        // 更新请求状态为充电中
        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingRequest::getId, requestId)
                .set(ChargingRequest::getStatus, RequestStatus.CHARGING.getCode())
                .set(ChargingRequest::getChargingStartTime, LocalDateTime.now())
                .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

        requestMapper.update(null, updateWrapper);

        // 更新充电桩统计数据
        updatePileStatistics(pileId);

        logger.info("请求 {} 开始在充电桩 {} 充电", requestId, pileId);
    }

    @Override
    @Transactional
    public void handlePileFault(Long pileId, Integer strategyType) {
        // 获取故障充电桩
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }

        // 更新充电桩状态为故障
        LambdaUpdateWrapper<ChargingPile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingPile::getId, pileId)
                .set(ChargingPile::getStatus, PileStatus.FAULT.getCode())
                .set(ChargingPile::getUpdateTime, LocalDateTime.now());

        pileMapper.update(null, updateWrapper);

        // 获取充电桩队列
        List<PileQueue> pileQueue = pileQueueMapper.findQueueByPileId(pileId);

        if (pileQueue.isEmpty()) {
            return;
        }

        // 找到正在充电的请求（位置0）
        Optional<PileQueue> chargingItem = pileQueue.stream()
                .filter(item -> item.getPosition() == 0)
                .findFirst();

        // 如果有正在充电的请求，生成详单并创建新请求
        if (chargingItem.isPresent()) {
            ChargingRequest request = requestMapper.selectById(chargingItem.get().getRequestId());
            if (request != null && RequestStatus.CHARGING.getCode().equals(request.getStatus())) {
                // 生成详单
                billingService.generateBill(request.getId(), true);

                // 获取已充电量（通过详单服务获取）
                Double chargedAmount = billingService.getChargedAmount(request.getId());
                if (chargedAmount == null) {
                    chargedAmount = 0.0;
                }

                // 计算剩余需要充电的电量
                Double remainingAmount = request.getRequestAmount().doubleValue() - chargedAmount;

                // 更新原请求状态为已完成（部分充电）
                LambdaUpdateWrapper<ChargingRequest> requestUpdateWrapper = new LambdaUpdateWrapper<>();
                requestUpdateWrapper.eq(ChargingRequest::getId, request.getId())
                        .set(ChargingRequest::getStatus, RequestStatus.COMPLETED.getCode())
                        .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

                requestMapper.update(null, requestUpdateWrapper);

                // 如果还有剩余电量需要充电，创建新的充电请求
                if (remainingAmount > 0) {
                    ChargingRequest newRequest = new ChargingRequest();
                    newRequest.setUserId(request.getUserId());
                    newRequest.setChargingMode(request.getChargingMode());
                    newRequest.setRequestAmount(BigDecimal.valueOf(remainingAmount));
                    newRequest.setStatus(RequestStatus.WAITING.getCode());
                    newRequest.setQueueStartTime(LocalDateTime.now());
                    newRequest.setCreateTime(LocalDateTime.now());
                    newRequest.setUpdateTime(LocalDateTime.now());
                    newRequest.setBatteryCapacity(request.getBatteryCapacity()); // 新增：设置电池容量

                    // 生成新的排队号码
                    String queueNumber = queueNumberGenerator.generateQueueNumber(request.getChargingMode());
                    newRequest.setQueueNumber(queueNumber);

                    requestMapper.insert(newRequest);

                    logger.info("为故障充电桩上的请求 {} 创建新请求 {}，剩余电量 {}",
                            request.getId(), newRequest.getId(), remainingAmount);

                    // 立即为新请求尝试分配充电桩
                    tryAssignNewRequestToPile(newRequest);
                }
            }

            // 删除队列中的该项
            pileQueueMapper.deleteById(chargingItem.get().getId());
        }

        // 暂停等候区叫号服务
        waitingAreaPaused = true;

        // 获取故障队列中的其他请求
        List<PileQueue> waitingItems = pileQueue.stream()
                .filter(item -> item.getPosition() > 0)
                .toList();

        // 获取故障队列中的请求ID列表
        List<Long> faultQueueRequestIds = waitingItems.stream()
                .map(PileQueue::getRequestId)
                .collect(Collectors.toList());

        // 删除故障队列中的所有项
        for (PileQueue item : waitingItems) {
            pileQueueMapper.deleteById(item.getId());
        }

        // 更新这些请求的状态为等待中
        for (Long requestId : faultQueueRequestIds) {
            LambdaUpdateWrapper<ChargingRequest> requestUpdateWrapper = new LambdaUpdateWrapper<>();
            requestUpdateWrapper.eq(ChargingRequest::getId, requestId)
                    .set(ChargingRequest::getStatus, RequestStatus.WAITING.getCode())
                    .set(ChargingRequest::getPileId, null)
                    .set(ChargingRequest::getQueuePosition, null)
                    .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

            requestMapper.update(null, requestUpdateWrapper);
        }

        // 根据策略类型进行调度
        if (strategyType == 1) {
            // 优先级调度
            handlePriorityDispatch(pile.getPileType(), faultQueueRequestIds);
        } else {
            // 时间顺序调度
            handleTimeOrderDispatch(pile.getPileType());
        }

        // 恢复等候区叫号服务
        waitingAreaPaused = false;
    }

    /**
     * 优先级调度策略
     */
    private void handlePriorityDispatch(Integer pileType, List<Long> faultQueueRequestIds) {
        // 获取同类型的正常充电桩
        List<ChargingPile> samePiles = pileMapper.findActivePilesByType(pileType);

        if (samePiles.isEmpty()) {
            logger.warn("没有可用的同类型充电桩进行故障调度");
            return;
        }

        // 获取故障队列中的请求
        List<ChargingRequest> faultRequests = new ArrayList<>();
        for (Long requestId : faultQueueRequestIds) {
            ChargingRequest request = requestMapper.selectById(requestId);
            if (request != null) {
                faultRequests.add(request);
            }
        }

        // 按照排队号码排序
        faultRequests.sort(Comparator.comparing(ChargingRequest::getQueueNumber));

        // 为每个故障请求重新分配充电桩
        for (ChargingRequest request : faultRequests) {
            // 构建充电桩队列映射
            Map<Long, List<ChargingRequest>> pileQueueMap = new HashMap<>();
            for (ChargingPile availablePile : samePiles) {
                List<PileQueue> queue = pileQueueMapper.findQueueByPileId(availablePile.getId());
                List<ChargingRequest> queueRequests = queue.stream()
                        .map(q -> requestMapper.selectById(q.getRequestId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                pileQueueMap.put(availablePile.getId(), queueRequests);
            }

            // 使用调度策略选择最佳充电桩
            Long bestPileId = dispatchStrategy.selectBestPile(request, samePiles, pileQueueMap);

            if (bestPileId != null) {
                // 将请求分配到选定的充电桩队列
                assignRequestToPile(request, bestPileId);
            } else {
                logger.warn("无法为故障请求 {} 找到合适的充电桩", request.getId());
            }
        }
    }

    /**
     * 时间顺序调度策略
     */
    private void handleTimeOrderDispatch(Integer pileType) {
        // 获取同类型的正常充电桩
        List<ChargingPile> samePiles = pileMapper.findActivePilesByType(pileType);

        if (samePiles.isEmpty()) {
            logger.warn("没有可用的同类型充电桩进行故障调度");
            return;
        }

        // 获取所有同类型充电桩中尚未充电的车辆
        List<ChargingRequest> allWaitingRequests = new ArrayList<>();

        // 收集所有同类型充电桩中的等待请求
        for (ChargingPile pile : samePiles) {
            List<PileQueue> queue = pileQueueMapper.findQueueByPileId(pile.getId());
            for (PileQueue item : queue) {
                if (item.getPosition() > 0) {  // 只考虑尚未充电的车辆
                    ChargingRequest request = requestMapper.selectById(item.getRequestId());
                    if (request != null) {
                        allWaitingRequests.add(request);

                        // 从队列中移除
                        pileQueueMapper.deleteById(item.getId());

                        // 更新请求状态
                        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(ChargingRequest::getId, request.getId())
                                .set(ChargingRequest::getStatus, RequestStatus.WAITING.getCode())
                                .set(ChargingRequest::getPileId, null)
                                .set(ChargingRequest::getQueuePosition, null)
                                .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

                        requestMapper.update(null, updateWrapper);
                    }
                }
            }
        }

        // 按照排队号码排序
        allWaitingRequests.sort(Comparator.comparing(ChargingRequest::getQueueNumber));

        // 为每个请求重新分配充电桩
        for (ChargingRequest request : allWaitingRequests) {
            // 构建充电桩队列映射
            Map<Long, List<ChargingRequest>> pileQueueMap = new HashMap<>();
            for (ChargingPile availablePile : samePiles) {
                List<PileQueue> queue = pileQueueMapper.findQueueByPileId(availablePile.getId());
                List<ChargingRequest> queueRequests = queue.stream()
                        .map(q -> requestMapper.selectById(q.getRequestId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                pileQueueMap.put(availablePile.getId(), queueRequests);
            }

            // 使用调度策略选择最佳充电桩
            Long bestPileId = dispatchStrategy.selectBestPile(request, samePiles, pileQueueMap);

            if (bestPileId != null) {
                // 将请求分配到选定的充电桩队列
                assignRequestToPile(request, bestPileId);
            } else {
                logger.warn("无法为请求 {} 找到合适的充电桩", request.getId());
            }
        }
    }

    @Override
    @Transactional
    public void handlePileRecovery(Long pileId) {
        // 获取充电桩
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }

        // 更新充电桩状态为正常
        LambdaUpdateWrapper<ChargingPile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingPile::getId, pileId)
                .set(ChargingPile::getStatus, PileStatus.NORMAL.getCode())
                .set(ChargingPile::getUpdateTime, LocalDateTime.now());

        pileMapper.update(null, updateWrapper);

        // 获取同类型的充电桩
        List<ChargingPile> samePiles = pileMapper.findActivePilesByType(pile.getPileType());

        // 检查是否有其他同类型充电桩中有车辆排队
        boolean hasWaitingCars = false;
        for (ChargingPile samePile : samePiles) {
            if (!samePile.getId().equals(pileId)) {
                List<PileQueue> queue = pileQueueMapper.findQueueByPileId(samePile.getId());
                if (queue.stream().anyMatch(item -> item.getPosition() > 0)) {
                    hasWaitingCars = true;
                    break;
                }
            }
        }

        // 如果有车辆排队，进行重新调度
        if (hasWaitingCars) {
            // 暂停等候区叫号服务
            waitingAreaPaused = true;

            // 使用时间顺序调度策略
            handleTimeOrderDispatch(pile.getPileType());

            // 恢复等候区叫号服务
            waitingAreaPaused = false;
        } else {
            // 触发正常调度
            triggerSchedule();
        }
    }

    @Override
    public ChargingPile getPileStatus(Long pileId) {
        return pileMapper.selectById(pileId);
    }

    private void tryAssignNewRequestToPile(ChargingRequest request) {
        // 获取所有正常工作的充电桩
        List<ChargingPile> activePiles = pileMapper.selectList(
                new LambdaQueryWrapper<ChargingPile>()
                        .eq(ChargingPile::getStatus, PileStatus.NORMAL.getCode()));

        if (activePiles.isEmpty()) {
            logger.info("没有正常工作的充电桩，跳过调度");
            return;
        }

        // 2. 获取所有充电桩队列
        int queueLen = systemParamService.getChargingQueueLen();
        Map<Long, List<PileQueue>> pileQueues = new HashMap<>();
        for (ChargingPile pile : activePiles) {
            pileQueues.put(pile.getId(), pileQueueMapper.findQueueByPileId(pile.getId()));
        }

        // 找出同类型且有空位的充电桩
        List<ChargingPile> availablePiles = activePiles.stream()
                .filter(p -> p.getPileType().equals(
                        request.getChargingMode().equals(ChargingMode.FAST.getCode()) ?
                                PileType.FAST.getCode() : PileType.TRICKLE.getCode()))
                .filter(p -> pileQueues.get(p.getId()).size() < queueLen)
                .collect(Collectors.toList());

        if (availablePiles.isEmpty()) {
            return;
        }

        // 构建充电桩队列映射
        Map<Long, List<ChargingRequest>> pileQueueMap = new HashMap<>();
        for (ChargingPile availablePile : availablePiles) {
            List<ChargingRequest> queueRequests = pileQueues.get(availablePile.getId()).stream()
                    .map(q -> requestMapper.selectById(q.getRequestId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            pileQueueMap.put(availablePile.getId(), queueRequests);
        }

        // 使用调度策略选择最佳充电桩
        Long bestPileId = dispatchStrategy.selectBestPile(request, availablePiles, pileQueueMap);

        if (bestPileId != null) {
            assignRequestToPile(request, bestPileId);
            // 更新本地队列缓存
            pileQueues.get(bestPileId).add(new PileQueue()); // 只需保证size+1即可
        }
    }
}
