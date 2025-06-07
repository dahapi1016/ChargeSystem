package com.hapi.chargingsystem.service;

import com.hapi.chargingsystem.dto.resp.ChargingStatusVO;

public interface ChargingService {

    /**
     * 开始充电
     * @param requestId 充电请求ID
     * @param pileId 充电桩ID
     * @return 是否成功
     */
    boolean startCharging(Long requestId, Long pileId);

    /**
     * 结束充电
     * @param requestId 充电请求ID
     * @param userId 用户ID（用于验证权限）
     * @return 是否成功
     */
    boolean endCharging(Long requestId, Long userId);

    /**
     * 获取充电状态
     * @param requestId 充电请求ID
     * @return 充电状态
     */
    ChargingStatusVO getChargingStatus(Long requestId);

    /**
     * 检查充电是否完成
     * 定时任务调用，检查是否达到请求充电量
     */
    void checkChargingCompletion();

    /**
     * 处理充电桩故障
     * @param pileId 充电桩ID
     * @return 是否成功
     */
    boolean handlePileFault(Long pileId);
}
