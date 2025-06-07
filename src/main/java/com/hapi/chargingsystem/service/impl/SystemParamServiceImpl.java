package com.hapi.chargingsystem.service.impl;

import com.hapi.chargingsystem.domain.SystemParam;
import com.hapi.chargingsystem.mapper.SystemParamMapper;
import com.hapi.chargingsystem.service.SystemParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SystemParamServiceImpl implements SystemParamService {

    @Autowired
    private SystemParamMapper systemParamMapper;

    @Override
    @Cacheable(value = "systemParam", key = "'WaitingAreaSize'")
    public int getWaitingAreaSize() {
        return getIntParam("WaitingAreaSize", 6);
    }

    @Override
    @Cacheable(value = "systemParam", key = "'FastChargingPileNum'")
    public int getFastChargingPileNum() {
        return getIntParam("FastChargingPileNum", 2);
    }

    @Override
    @Cacheable(value = "systemParam", key = "'TrickleChargingPileNum'")
    public int getTrickleChargingPileNum() {
        return getIntParam("TrickleChargingPileNum", 3);
    }

    @Override
    @Cacheable(value = "systemParam", key = "'ChargingQueueLen'")
    public int getChargingQueueLen() {
        return getIntParam("ChargingQueueLen", 2);
    }

    @Override
    @Cacheable(value = "systemParam", key = "'FastChargingPower'")
    public double getFastChargingPower() {
        return getDoubleParam("FastChargingPower", 30.0);
    }

    @Override
    @Cacheable(value = "systemParam", key = "'TrickleChargingPower'")
    public double getTrickleChargingPower() {
        return getDoubleParam("TrickleChargingPower", 7.0);
    }

    private int getIntParam(String key, int defaultValue) {
        SystemParam param = systemParamMapper.selectByKey(key);
        if (param == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param.getParamValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double getDoubleParam(String key, double defaultValue) {
        SystemParam param = systemParamMapper.selectByKey(key);
        if (param == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(param.getParamValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
