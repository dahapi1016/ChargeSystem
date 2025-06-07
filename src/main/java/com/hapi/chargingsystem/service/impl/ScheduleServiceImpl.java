package com.hapi.chargingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.common.enums.PileStatus;
import com.hapi.chargingsystem.common.enums.PileType;
import com.hapi.chargingsystem.common.enums.RequestStatus;
import com.hapi.chargingsystem.common.exception.BusinessException;
import com.hapi.chargingsystem.common.strategy.DispatchStrategy;
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
        // 如果等候区叫号服务暂停，则不进行调度
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

        // 2. 检查每个充电桩的队列，如果有空位则尝试调度
        for (ChargingPile pile : activePiles) {
            checkAndSchedulePile(pile, activePiles);
        }
    }

    /**
     * 检查并调度单个充电桩
     */
    private void checkAndSchedulePile(ChargingPile pile, List<ChargingPile> activePiles) {
        // 获取充电桩队列长度
        int queueLen = systemParamService.getChargingQueueLen();

        // 获取当前充电桩队列
        List<PileQueue> pileQueue = pileQueueMapper.findQueueByPileId(pile.getId());

        // 如果队列已满，则跳过
        if (pileQueue.size() >= queueLen) {
            return;
        }

        // 确定需要调度的充电模式
        Integer chargingMode = (pile.getPileType().equals(PileType.FAST.getCode())) ?
                ChargingMode.FAST.getCode() : ChargingMode.TRICKLE.getCode();

        // 获取等候区中对应模式的请求，按排队号码排序
        List<ChargingRequest> waitingRequests = requestMapper.getWaitingRequestsByMode(chargingMode);

        if (waitingRequests.isEmpty()) {
            return;
        }

        // 获取第一个等待的请求
        ChargingRequest nextRequest = waitingRequests.get(0);

        // 获取所有可用的同类型充电桩
        List<ChargingPile> availablePiles = activePiles.stream()
                .filter(p -> p.getPileType().equals(pile.getPileType()))
                .filter(p -> {
                    List<PileQueue> queue = pileQueueMapper.findQueueByPileId(p.getId());
                    return queue.size() < queueLen;
                })
                .collect(Collectors.toList());

        // 构建充电桩队列映射
        Map<Long, List<ChargingRequest>> pileQueueMap = new HashMap<>();
        for (ChargingPile availablePile : availablePiles) {
            List<PileQueue> queue = pileQueueMapper.findQueueByPileId(availablePile.getId());
            List<ChargingRequest> queueRequests = queue.stream()
                    .map(q -> requestMapper.selectById(q.getRequestId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            pileQueueMap.put(availablePile.getId(), queueRequests);
        }

        // 使用调度策略选择最佳充电桩
        Long bestPileId = dispatchStrategy.selectBestPile(nextRequest, availablePiles, pileQueueMap);

        if (bestPileId == null) {
            logger.warn("无法为请求 {} 找到合适的充电桩", nextRequest.getId());
            return;
        }

        // 将请求分配到选定的充电桩队列
        assignRequestToPile(nextRequest, bestPileId);
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

        if (!chargingItem.isPresent()) {
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

        // 如果有正在充电的请求，生成详单并停止充电
        if (chargingItem.isPresent()) {
            ChargingRequest request = requestMapper.selectById(chargingItem.get().getRequestId());
            if (request != null && RequestStatus.CHARGING.getCode().equals(request.getStatus())) {
                // 生成详单
                billingService.generateBill(request.getId(), true);

                // 更新请求状态为等待中
                LambdaUpdateWrapper<ChargingRequest> requestUpdateWrapper = new LambdaUpdateWrapper<>();
                requestUpdateWrapper.eq(ChargingRequest::getId, request.getId())
                        .set(ChargingRequest::getStatus, RequestStatus.WAITING.getCode())
                        .set(ChargingRequest::getPileId, null)
                        .set(ChargingRequest::getQueuePosition, null)
                        .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

                requestMapper.update(null, requestUpdateWrapper);
            }

            // 删除队列中的该项
            pileQueueMapper.deleteById(chargingItem.get().getId());
        }

        // 暂停等候区叫号服务
        waitingAreaPaused = true;

        // 获取故障队列中的其他请求
        List<PileQueue> waitingItems = pileQueue.stream()
                .filter(item -> item.getPosition() > 0)
                .collect(Collectors.toList());

        // 删除故障队列中的所有项
        for (PileQueue item : waitingItems) {
            pileQueueMapper.deleteById(item.getId());
        }

        // 获取故障队列中的请求ID列表
        List<Long> faultQueueRequestIds = waitingItems.stream()
                .map(PileQueue::getRequestId)
                .collect(Collectors.toList());

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
}
