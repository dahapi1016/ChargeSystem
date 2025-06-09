package com.hapi.chargingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.common.enums.RequestStatus;
import com.hapi.chargingsystem.common.exception.BusinessException;
import com.hapi.chargingsystem.common.utils.QueueNumberGenerator;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.dto.req.ChargeReqDTO;
import com.hapi.chargingsystem.dto.resp.ChargeRespDTO;
import com.hapi.chargingsystem.dto.resp.QueueStatusRespDTO;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import com.hapi.chargingsystem.mapper.PileQueueMapper;
import com.hapi.chargingsystem.service.ChargingRequestService;
import com.hapi.chargingsystem.service.ScheduleService;
import com.hapi.chargingsystem.service.SystemParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ChargingRequestServiceImpl extends ServiceImpl<ChargingRequestMapper, ChargingRequest> implements ChargingRequestService {
    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    @Autowired
    private QueueNumberGenerator queueNumberGenerator;

    @Autowired
    private SystemParamService systemParamService;

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private PileQueueMapper pileQueueMapper;

    @Override
    @Transactional
    public ChargeRespDTO submitRequest(Long userId, ChargeReqDTO requestDTO) {
        // 检查用户是否已有未完成的充电请求
        ChargingRequest existingRequest = getActiveRequest(userId);
        if (existingRequest != null) {
            throw new BusinessException("您已有一个正在进行的充电请求，请先完成或取消");
        }

        // 检查等候区是否已满
        int waitingCount = countWaitingRequests();
        if (waitingCount >= systemParamService.getWaitingAreaSize()) {
            throw new BusinessException("等候区已满，请稍后再试");
        }

        // 创建充电请求
        ChargingRequest request = new ChargingRequest();
        request.setUserId(userId);
        request.setChargingMode(requestDTO.getChargingMode());
        request.setRequestAmount(requestDTO.getRequestAmount());
        request.setBatteryCapacity(requestDTO.getBatteryCapacity());
        request.setStatus(RequestStatus.WAITING.getCode());
        request.setQueueStartTime(LocalDateTime.now());
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());

        // 生成排队号码
        String queueNumber = queueNumberGenerator.generateQueueNumber(requestDTO.getChargingMode());
        request.setQueueNumber(queueNumber);

        // 保存请求
        chargingRequestMapper.insert(request);

        // 触发调度
        scheduleService.triggerSchedule();

        // 返回VO
        return convertToVO(request);
    }

    @Override
    @Transactional
    public ChargeRespDTO updateRequest(Long userId, ChargeReqDTO updateDTO) {
        // 获取用户当前活动的请求
        ChargingRequest request = getActiveRequest(userId);
        if (request == null) {
            throw new BusinessException("您当前没有充电请求");
        }

        // 检查请求状态
        if (!RequestStatus.WAITING.getCode().equals(request.getStatus())) {
            throw new BusinessException("只能修改等待中的充电请求");
        }

        // 检查是否修改了充电模式
        boolean modeChanged = updateDTO.getChargingMode() != null &&
                !updateDTO.getChargingMode().equals(request.getChargingMode());

        // 如果修改了充电模式，需要重新生成排队号
        if (modeChanged) {
            // 生成新的排队号码
            String newQueueNumber = queueNumberGenerator.generateQueueNumber(updateDTO.getChargingMode());
            request.setQueueNumber(newQueueNumber);
            request.setChargingMode(updateDTO.getChargingMode());
            request.setQueueStartTime(LocalDateTime.now()); // 重新开始排队
        }

        // 修改请求充电量
        if (updateDTO.getRequestAmount() != null) {
            request.setRequestAmount(updateDTO.getRequestAmount());
        }

        request.setUpdateTime(LocalDateTime.now());

        // 更新请求
        chargingRequestMapper.updateById(request);

        // 触发调度
        scheduleService.triggerSchedule();

        // 返回VO
        return convertToVO(request);
    }

    @Override
    @Transactional
    public boolean cancelRequest(Long userId) {
        // 获取用户当前活动的请求
        ChargingRequest request = getActiveRequest(userId);
        if (request == null) {
            throw new BusinessException("您当前没有充电请求");
        }

        // 检查请求状态
        if (RequestStatus.COMPLETED.getCode().equals(request.getStatus()) ||
                RequestStatus.CANCELLED.getCode().equals(request.getStatus())) {
            throw new BusinessException("该充电请求已完成或已取消");
        }

        // 更新状态为已取消
        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingRequest::getId, request.getId())
                .set(ChargingRequest::getStatus, RequestStatus.CANCELLED.getCode())
                .set(ChargingRequest::getUpdateTime, LocalDateTime.now());

        boolean result = chargingRequestMapper.update(null, updateWrapper) > 0;

        // 如果是充电中的请求，需要释放充电桩
        if (result && RequestStatus.CHARGING.getCode().equals(request.getStatus())) {
            scheduleService.releasePile(request.getPileId());
        }

        // 触发调度
        scheduleService.triggerSchedule();

        return result;
    }

    @Override
    public ChargeRespDTO getCurrentRequest(Long userId) {
        ChargingRequest request = getActiveRequest(userId);
        return request != null ? convertToVO(request) : null;
    }

    @Override
    public QueueStatusRespDTO getQueueStatus(Long userId) {
        // 获取用户当前请求
        ChargingRequest request = getActiveRequest(userId);
        if (request == null) {
            throw new BusinessException("您当前没有充电请求");
        }

        QueueStatusRespDTO statusVO = new QueueStatusRespDTO();
        statusVO.setQueueNumber(request.getQueueNumber());

        // 如果是等待中，计算前车等待数量
        if (RequestStatus.WAITING.getCode().equals(request.getStatus())) {
            int waitingAhead = 0;
            // 检查是否已经进入充电桩队列
            if (request.getPileId() != null) {
                // 已进入充电桩队列，查看在队列中的位置
                com.hapi.chargingsystem.domain.PileQueue pileQueue = pileQueueMapper.findByRequestId(request.getId());
                if (pileQueue != null) {
                    // 前车数量就是队列位置（position从0开始，所以position就是前车数量）
                    waitingAhead = pileQueue.getPosition();
                }
            } else {
                // 还在等候区，使用原来的逻辑
                Integer ahead = chargingRequestMapper.countWaitingAhead(
                        request.getChargingMode(), request.getId());
                waitingAhead = ahead != null ? ahead : 0;
            }
            statusVO.setWaitingCount(waitingAhead);

            // 简单估算：前面每辆车平均充电30分钟
            int estimatedTime = waitingAhead * 30;
            statusVO.setEstimatedWaitingTime(estimatedTime);
        } else {
            statusVO.setWaitingCount(0);
            statusVO.setEstimatedWaitingTime(0);
        }

        // 计算同类型总等待数量
        Integer totalWaiting = chargingRequestMapper.countWaitingByMode(request.getChargingMode());
        statusVO.setTotalWaitingCount(totalWaiting);

        // 估算等待时间
        double power = ChargingMode.FAST.getCode().equals(request.getChargingMode()) ?
                systemParamService.getFastChargingPower() :
                systemParamService.getTrickleChargingPower();

        // 简单估算：前面每辆车平均充电30分钟
        int estimatedTime = totalWaiting * 30;
        statusVO.setEstimatedWaitingTime(estimatedTime);

        return statusVO;
    }

    @Override
    @Transactional
    public boolean endCharging(Long userId) {
        // 获取用户当前活动的请求
        ChargingRequest request = getActiveRequest(userId);
        if (request == null) {
            throw new BusinessException("您当前没有充电请求");
        }

        // 检查请求状态
        if (!RequestStatus.CHARGING.getCode().equals(request.getStatus())) {
            throw new BusinessException("只能结束正在充电中的请求");
        }

        // 更新状态为已完成
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<ChargingRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChargingRequest::getId, request.getId())
                .set(ChargingRequest::getStatus, RequestStatus.COMPLETED.getCode())
                .set(ChargingRequest::getChargingEndTime, now)
                .set(ChargingRequest::getUpdateTime, now);

        boolean result = chargingRequestMapper.update(null, updateWrapper) > 0;

        // 释放充电桩
        if (result) {
            scheduleService.releasePile(request.getPileId());

            // 生成充电详单
            // 这里调用计费服务生成详单，具体实现在计费模块
        }

        // 触发调度
        scheduleService.triggerSchedule();

        return result;
    }

    /**
     * 获取用户当前活动的充电请求（等待中或充电中）
     */
    private ChargingRequest getActiveRequest(Long userId) {
        LambdaQueryWrapper<ChargingRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChargingRequest::getUserId, userId)
                .in(ChargingRequest::getStatus,
                        RequestStatus.WAITING.getCode(), RequestStatus.CHARGING.getCode())
                .orderByDesc(ChargingRequest::getCreateTime)
                .last("LIMIT 1");

        return chargingRequestMapper.selectOne(queryWrapper);
    }

    /**
     * 统计等待中的请求数量
     */
    private int countWaitingRequests() {
        LambdaQueryWrapper<ChargingRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChargingRequest::getStatus, RequestStatus.WAITING.getCode());

        return chargingRequestMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 将实体转换为VO
     */
    private ChargeRespDTO convertToVO(ChargingRequest request) {
        if (request == null) {
            return null;
        }

        ChargeRespDTO vo = new ChargeRespDTO();
        BeanUtils.copyProperties(request, vo);

        // 设置充电模式和状态描述
        vo.setChargingMode(request.getChargingMode());
        vo.setStatus(request.getStatus());

        // 计算已等待时间
        if (request.getQueueStartTime() != null) {
            long waitingMinutes = Duration.between(request.getQueueStartTime(), LocalDateTime.now()).toMinutes();
            vo.setWaitingTime(waitingMinutes);
        }

        // 如果是等待中，计算前车等待数量
        if (RequestStatus.WAITING.getCode().equals(request.getStatus())) {
            // 检查是否已经进入充电桩队列
            if (request.getPileId() != null) {
                // 已进入充电桩队列，查看在队列中的位置
                com.hapi.chargingsystem.domain.PileQueue pileQueue = pileQueueMapper.findByRequestId(request.getId());
                if (pileQueue != null) {
                    // 前车数量就是队列位置（position从0开始，所以position就是前车数量）
                    vo.setWaitingCount(pileQueue.getPosition());
                } else {
                    vo.setWaitingCount(0);
                }
            } else {
                // 还在等候区，使用原来的逻辑
                Integer waitingAhead = chargingRequestMapper.countWaitingAhead(
                        request.getChargingMode(), request.getId());
                vo.setWaitingCount(waitingAhead);
            }
        } else {
            vo.setWaitingCount(0);
        }

        return vo;
    }
}

