package com.hapi.chargingsystem.common.strategy;

import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingRequest;

import java.util.List;
import java.util.Map;

public interface DispatchStrategy {

    /**
     * 为请求选择最佳充电桩
     * @param request 充电请求
     * @param availablePiles 可用充电桩列表
     * @param pileQueueMap 充电桩队列映射（key: pileId, value: 队列中的请求列表）
     * @return 选择的充电桩ID，如果无法分配则返回null
     */
    Long selectBestPile(ChargingRequest request, List<ChargingPile> availablePiles,
                        Map<Long, List<ChargingRequest>> pileQueueMap);
}
