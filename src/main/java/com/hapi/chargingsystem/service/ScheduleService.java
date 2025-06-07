package com.hapi.chargingsystem.service;

import com.hapi.chargingsystem.domain.ChargingPile;

public interface ScheduleService {

    /**
     * 触发调度
     */
    void triggerSchedule();

    /**
     * 释放充电桩
     * @param pileId 充电桩ID
     */
    void releasePile(Long pileId);

    /**
     * 处理充电桩故障
     * @param pileId 故障充电桩ID
     * @param strategyType 故障调度策略类型（1-优先级调度，2-时间顺序调度）
     */
    void handlePileFault(Long pileId, Integer strategyType);

    /**
     * 处理充电桩恢复
     * @param pileId 恢复的充电桩ID
     */
    void handlePileRecovery(Long pileId);

    /**
     * 获取充电桩状态
     * @param pileId 充电桩ID
     * @return 充电桩对象
     */
    ChargingPile getPileStatus(Long pileId);
}
