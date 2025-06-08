package com.hapi.chargingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hapi.chargingsystem.common.exception.BusinessException;
import com.hapi.chargingsystem.domain.SystemParam;
import com.hapi.chargingsystem.mapper.SystemParamMapper;
import com.hapi.chargingsystem.service.SystemParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统参数设置相关
 */
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

    @Override
    public List<SystemParam> getAllParams() {
        return systemParamMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public SystemParam getParamById(Long id) {
        return systemParamMapper.selectById(id);
    }

    @Override
    public SystemParam getParamByKey(String key) {
        return systemParamMapper.selectOne(
                new LambdaQueryWrapper<SystemParam>()
                        .eq(SystemParam::getParamKey, key));
    }

    @Override
    @CacheEvict(value = "systemParam", key = "#key")
    public SystemParam updateParam(String key, String value) {
        SystemParam param = getParamByKey(key);
        if (param == null) {
            throw new BusinessException("系统参数不存在: " + key);
        }

        param.setParamValue(value);
        param.setUpdateTime(LocalDateTime.now());
        systemParamMapper.updateById(param);

        return param;
    }

    @Override
    @Transactional
    @CacheEvict(value = "systemParam", allEntries = true)
    public int batchUpdateParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return 0;
        }

        int updatedCount = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            SystemParam param = getParamByKey(key);
            if (param != null) {
                param.setParamValue(value);
                param.setUpdateTime(LocalDateTime.now());
                systemParamMapper.updateById(param);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    @Override
    @Cacheable(value = "systemParam", key = "#key")
    public String getStringParam(String key, String defaultValue) {
        SystemParam param = systemParamMapper.selectOne(
                new LambdaQueryWrapper<SystemParam>()
                        .eq(SystemParam::getParamKey, key));
        return param != null ? param.getParamValue() : defaultValue;
    }

    @Override
    @Cacheable(value = "systemParam", key = "#key")
    public int getIntParam(String key, int defaultValue) {
        SystemParam param = systemParamMapper.selectOne(
                new LambdaQueryWrapper<SystemParam>()
                        .eq(SystemParam::getParamKey, key));
        if (param == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param.getParamValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    @Cacheable(value = "systemParam", key = "#key")
    public double getDoubleParam(String key, double defaultValue) {
        SystemParam param = systemParamMapper.selectOne(
                new LambdaQueryWrapper<SystemParam>()
                        .eq(SystemParam::getParamKey, key));
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
