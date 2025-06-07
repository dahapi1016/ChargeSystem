package com.hapi.chargingsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.dto.req.ChargeReqDTO;
import com.hapi.chargingsystem.dto.resp.ChargeRespDTO;
import com.hapi.chargingsystem.dto.resp.QueueStatusRespDTO;

public interface ChargingRequestService extends IService<ChargingRequest> {
    /**
     * 提交充电请求
     * @param userId 用户ID
     * @param requestDTO 充电请求DTO
     * @return 充电请求VO
     */
    ChargeRespDTO submitRequest(Long userId, ChargeReqDTO requestDTO);

    /**
     * 修改充电请求
     * @param userId 用户ID
     * @param requestId 请求ID
     * @param updateDTO 修改DTO
     * @return 充电请求VO
     */
    ChargeRespDTO updateRequest(Long userId, Long requestId, ChargeReqDTO updateDTO);

    /**
     * 取消充电请求
     * @param userId 用户ID
     * @param requestId 请求ID
     * @return 是否成功
     */
    boolean cancelRequest(Long userId, Long requestId);

    /**
     * 获取用户当前充电请求
     * @param userId 用户ID
     * @return 充电请求VO，如果没有则返回null
     */
    ChargeRespDTO getCurrentRequest(Long userId);

    /**
     * 获取排队状态
     * @param userId 用户ID
     * @return 队列状态VO
     */
    QueueStatusRespDTO getQueueStatus(Long userId);

    /**
     * 结束充电
     * @param userId 用户ID
     * @param requestId 请求ID
     * @return 是否成功
     */
    boolean endCharging(Long userId, Long requestId);
}
