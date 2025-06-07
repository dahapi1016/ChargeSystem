package com.hapi.chargingsystem.service;

public interface SystemParamService {

    /**
     * 获取等候区最大容量
     * @return 等候区最大容量
     */
    int getWaitingAreaSize();

    /**
     * 获取快充电桩数量
     * @return 快充电桩数量
     */
    int getFastChargingPileNum();

    /**
     * 获取慢充电桩数量
     * @return 慢充电桩数量
     */
    int getTrickleChargingPileNum();

    /**
     * 获取充电桩队列长度
     * @return 充电桩队列长度
     */
    int getChargingQueueLen();

    /**
     * 获取快充功率（度/小时）
     * @return 快充功率
     */
    double getFastChargingPower();

    /**
     * 获取慢充功率（度/小时）
     * @return 慢充功率
     */
    double getTrickleChargingPower();
}
