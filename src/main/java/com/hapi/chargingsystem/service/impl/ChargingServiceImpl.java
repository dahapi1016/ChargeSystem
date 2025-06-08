package com.hapi.chargingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.common.enums.PileStatus;
import com.hapi.chargingsystem.common.enums.RequestStatus;
import com.hapi.chargingsystem.common.exception.BusinessException;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.domain.PileQueue;
import com.hapi.chargingsystem.dto.resp.ChargingStatusVO;
import com.hapi.chargingsystem.mapper.ChargingPileMapper;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import com.hapi.chargingsystem.mapper.PileQueueMapper;
import com.hapi.chargingsystem.service.BillingService;
import com.hapi.chargingsystem.service.ChargingService;
import com.hapi.chargingsystem.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ChargingServiceImpl implements ChargingService {

    @Autowired
    private ChargingRequestMapper requestMapper;

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private PileQueueMapper pileQueueMapper;

    @Autowired
    private BillingService billingService;

    @Autowired
    private ScheduleService scheduleService;

    @Override
    @Transactional
    public boolean startCharging(Long requestId, Long pileId) {
        // 获取充电请求
        ChargingRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("充电请求不存在");
        }

        // 检查请求状态
        if (!RequestStatus.WAITING.getCode().equals(request.getStatus())) {
            throw new BusinessException("充电请求状态异常，无法开始充电");
        }

        // 检查充电桩状态
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }

        if (!PileStatus.NORMAL.getCode().equals(pile.getStatus())) {
            throw new BusinessException("充电桩状态异常，无法开始充电");
        }

        // 检查充电模式是否匹配
        if ((ChargingMode.FAST.getCode().equals(request.getChargingMode()) && !pile.getPileType().equals(1)) ||
                (ChargingMode.TRICKLE.getCode().equals(request.getChargingMode()) && !pile.getPileType().equals(2))) {
            throw new BusinessException("充电模式与充电桩类型不匹配");
        }

        // 检查是否在队列首位
        PileQueue queueItem = pileQueueMapper.findByRequestId(requestId);
        if (queueItem == null || queueItem.getPosition() != 0) {
            throw new BusinessException("充电请求不在队列首位，无法开始充电");
        }

        // 更新请求状态
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingRequest::getId, requestId)
                .set(ChargingRequest::getStatus, RequestStatus.CHARGING.getCode())
                .set(ChargingRequest::getChargingStartTime, now)
                .set(ChargingRequest::getUpdateTime, now);

        boolean result = requestMapper.update(null, updateWrapper) > 0;

        if (result) {
            // 更新充电桩统计数据
            updatePileStatistics(pileId);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean endCharging(Long requestId, Long userId) {
        // 获取充电请求
        ChargingRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("充电请求不存在");
        }

        // 检查用户权限
        if (!request.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此充电请求");
        }

        // 检查请求状态
        if (!RequestStatus.CHARGING.getCode().equals(request.getStatus())) {
            throw new BusinessException("充电请求不在充电中状态，无法结束充电");
        }

        // 更新请求状态
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingRequest::getId, requestId)
                .set(ChargingRequest::getStatus, RequestStatus.COMPLETED.getCode())
                .set(ChargingRequest::getChargingEndTime, now)
                .set(ChargingRequest::getUpdateTime, now);

        boolean result = requestMapper.update(null, updateWrapper) > 0;

        if (result) {
            // 生成充电详单
            billingService.generateBill(requestId, false);

            // 释放充电桩
            scheduleService.releasePile(request.getPileId());
        }

        return result;
    }

    @Override
    public ChargingStatusVO getChargingStatus(Long requestId) {
        // 获取充电请求
        ChargingRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("充电请求不存在");
        }

        // 检查请求状态
        if (!RequestStatus.CHARGING.getCode().equals(request.getStatus())) {
            throw new BusinessException("充电请求不在充电中状态");
        }

        // 获取充电桩
        ChargingPile pile = pileMapper.selectById(request.getPileId());
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }

        // 计算当前充电状态
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = request.getChargingStartTime();

        // 充电时长（分钟）
        int chargingDuration = (int) Duration.between(startTime, now).toMinutes();

        // 计算当前已充电量
        BigDecimal power = pile.getPower();  // 度/小时
        BigDecimal hours = BigDecimal.valueOf(chargingDuration).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal currentAmount = power.multiply(hours).setScale(2, RoundingMode.HALF_UP);

        // 如果已超过请求量，则限制为请求量
        if (currentAmount.compareTo(request.getRequestAmount()) > 0) {
            currentAmount = request.getRequestAmount();
        }

        // 计算充电进度
        int progress = currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(request.getRequestAmount(), 0, RoundingMode.HALF_UP)
                .intValue();

        // 计算预计结束时间
        BigDecimal remainingAmount = request.getRequestAmount().subtract(currentAmount);
        BigDecimal remainingHours = remainingAmount.divide(power, 4, RoundingMode.CEILING);
        int remainingMinutes = remainingHours.multiply(BigDecimal.valueOf(60)).intValue();
        LocalDateTime estimatedEndTime = now.plusMinutes(remainingMinutes);

        // 计算预计费用（简化计算，实际应考虑分时电价）
        BigDecimal averagePrice = BigDecimal.valueOf(0.7);  // 假设平均电价0.7元/度
        BigDecimal serviceFeeRate = BigDecimal.valueOf(0.8);  // 假设服务费率0.8元/度
        BigDecimal estimatedFee = currentAmount.multiply(averagePrice.add(serviceFeeRate)).setScale(2, RoundingMode.HALF_UP);

        // 构建返回对象
        ChargingStatusVO vo = new ChargingStatusVO();
        vo.setRequestId(requestId);
        vo.setPileId(pile.getId());
        vo.setPileCode(pile.getPileCode());
        vo.setChargingMode(request.getChargingMode());
        vo.setChargingModeDesc(Objects.requireNonNull(ChargingMode.getByCode(request.getChargingMode())).getDescription());
        vo.setRequestAmount(request.getRequestAmount());
        vo.setCurrentAmount(currentAmount);
        vo.setChargingDuration(chargingDuration);
        vo.setEstimatedFee(estimatedFee);
        vo.setStartTime(startTime);
        vo.setEstimatedEndTime(estimatedEndTime);
        vo.setProgress(progress);

        return vo;
    }

    @Override
    @Scheduled(fixedRate = 5000)  // 每5秒检查一次
    public void checkChargingCompletion() {
        // 获取所有正在充电的请求
        List<ChargingRequest> chargingRequests = requestMapper.selectList(
                new LambdaQueryWrapper<ChargingRequest>()
                        .eq(ChargingRequest::getStatus, RequestStatus.CHARGING.getCode()));

        for (ChargingRequest request : chargingRequests) {
            try {
                // 获取充电桩
                ChargingPile pile = pileMapper.selectById(request.getPileId());
                if (pile == null) {
                    continue;
                }

                // 计算当前已充电量
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startTime = request.getChargingStartTime();

                int chargingDuration = (int) Duration.between(startTime, now).toMinutes();
                BigDecimal power = pile.getPower();
                BigDecimal hours = BigDecimal.valueOf(chargingDuration).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
                BigDecimal currentAmount = power.multiply(hours).setScale(2, RoundingMode.HALF_UP);

                // 如果已达到或超过请求量，则结束充电
                if (currentAmount.compareTo(request.getRequestAmount()) >= 0) {
                    // 更新请求状态
                    LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ChargingRequest::getId, request.getId())
                            .set(ChargingRequest::getStatus, RequestStatus.COMPLETED.getCode())
                            .set(ChargingRequest::getChargingEndTime, now)
                            .set(ChargingRequest::getUpdateTime, now);

                    boolean result = requestMapper.update(null, updateWrapper) > 0;

                    if (result) {
                        // 生成充电详单
                        billingService.generateBill(request.getId(), false);

                        // 释放充电桩
                        scheduleService.releasePile(request.getPileId());
                    }
                }
            } catch (Exception e) {
                // 记录异常但不中断循环
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional
    public boolean handlePileFault(Long pileId) {
        // 获取充电桩
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }

        // 更新充电桩状态
        LambdaUpdateWrapper<ChargingPile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingPile::getId, pileId)
                .set(ChargingPile::getStatus, PileStatus.FAULT.getCode())
                .set(ChargingPile::getUpdateTime, LocalDateTime.now());

        boolean result = pileMapper.update(null, updateWrapper) > 0;

        if (result) {
            // 获取正在充电的请求
            List<ChargingRequest> chargingRequests = requestMapper.selectList(
                    new LambdaQueryWrapper<ChargingRequest>()
                            .eq(ChargingRequest::getPileId, pileId)
                            .eq(ChargingRequest::getStatus, RequestStatus.CHARGING.getCode()));

            for (ChargingRequest request : chargingRequests) {
                // 生成充电详单（标记为故障结束）
                billingService.generateBill(request.getId(), true);

                // 更新请求状态为等待中
                LambdaUpdateWrapper<ChargingRequest> reqUpdateWrapper = new LambdaUpdateWrapper<>();
                reqUpdateWrapper.eq(ChargingRequest::getId, request.getId())
                        .set(ChargingRequest::getStatus, RequestStatus.WAITING.getCode())
                        .set(ChargingRequest::getPileId, null)
                        .set(ChargingRequest::getQueuePosition, null)
                        .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

                requestMapper.update(null, reqUpdateWrapper);
            }

            // 调用调度服务处理故障
            scheduleService.handlePileFault(pileId, 1);  // 使用优先级调度策略
        }

        return result;
    }

    /**
     * 更新充电桩统计数据
     */
    private void updatePileStatistics(Long pileId) {
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile != null) {
            pile.setTotalChargingTimes(pile.getTotalChargingTimes() + 1);
            pile.setUpdateTime(LocalDateTime.now());
            pileMapper.updateById(pile);
        }
    }
}
