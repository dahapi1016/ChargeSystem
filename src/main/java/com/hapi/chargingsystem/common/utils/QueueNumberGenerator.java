package com.hapi.chargingsystem.common.utils;

import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueueNumberGenerator {

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    /**
     * 生成排队号码
     * @param chargingMode 充电模式
     * @return 排队号码（F1、T1等）
     */
    public String generateQueueNumber(Integer chargingMode) {
        ChargingMode mode = ChargingMode.getByCode(chargingMode);
        if (mode == null) {
            throw new IllegalArgumentException("无效的充电模式");
        }

        String prefix = mode.getPrefix();
        Integer maxNumber = chargingRequestMapper.getMaxQueueNumber(chargingMode, prefix + "%");

        int nextNumber = (maxNumber == null) ? 1 : maxNumber + 1;
        return prefix + nextNumber;
    }
}