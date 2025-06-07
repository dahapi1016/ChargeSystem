package com.hapi.chargingsystem.common.strategy.impl;


import com.hapi.chargingsystem.common.strategy.DispatchStrategy;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ShortestCompletionTimeStrategy implements DispatchStrategy {

    @Override
    public Long selectBestPile(ChargingRequest request, List<ChargingPile> availablePiles,
                               Map<Long, List<ChargingRequest>> pileQueueMap) {
        if (availablePiles == null || availablePiles.isEmpty()) {
            return null;
        }

        // 找出完成充电所需时长最短的充电桩
        Optional<ChargingPile> bestPile = availablePiles.stream()
                .min(Comparator.comparing(pile -> calculateCompletionTime(request, pile, pileQueueMap.get(pile.getId()))));

        return bestPile.map(ChargingPile::getId).orElse(null);
    }

    /**
     * 计算在指定充电桩完成充电所需的总时长（等待时间+充电时间）
     * @param request 充电请求
     * @param pile 充电桩
     * @param queueRequests 充电桩队列中的请求列表
     * @return 完成充电所需的总时长（分钟）
     */
    private double calculateCompletionTime(ChargingRequest request, ChargingPile pile, List<ChargingRequest> queueRequests) {
        // 计算自己的充电时间（分钟）
        double chargingTime = calculateChargingTime(request.getRequestAmount(), pile.getPower());

        // 如果队列为空，则只需要自己的充电时间
        if (queueRequests == null || queueRequests.isEmpty()) {
            return chargingTime;
        }

        // 计算等待时间（队列中所有请求的充电时间之和）
        double waitingTime = queueRequests.stream()
                .mapToDouble(queueRequest -> calculateChargingTime(queueRequest.getRequestAmount(), pile.getPower()))
                .sum();

        return waitingTime + chargingTime;
    }

    /**
     * 计算充电时间（分钟）
     * @param requestAmount 请求充电量（度）
     * @param power 充电功率（度/小时）
     * @return 充电时间（分钟）
     */
    private double calculateChargingTime(BigDecimal requestAmount, BigDecimal power) {
        // 充电时间（小时）= 充电量 / 功率
        BigDecimal hours = requestAmount.divide(power, 4, RoundingMode.CEILING);
        // 转换为分钟
        return hours.multiply(BigDecimal.valueOf(60)).doubleValue();
    }
}
