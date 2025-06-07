package com.hapi.chargingsystem.components;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hapi.chargingsystem.domain.SystemParam;
import com.hapi.chargingsystem.mapper.SystemParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)  // 确保先于其他初始化器执行
public class SystemParamInitializer implements CommandLineRunner {

    @Autowired
    private SystemParamMapper systemParamMapper;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在系统参数
        long count = systemParamMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            return;
        }

        // 定义默认参数
        Map<String, String> defaultParams = new HashMap<>();
        defaultParams.put("FastChargingPileNum", "2");
        defaultParams.put("TrickleChargingPileNum", "3");
        defaultParams.put("WaitingAreaSize", "6");
        defaultParams.put("ChargingQueueLen", "2");
        defaultParams.put("FastChargingPower", "30.0");
        defaultParams.put("TrickleChargingPower", "7.0");
        defaultParams.put("PeakPrice", "1.0");
        defaultParams.put("FlatPrice", "0.7");
        defaultParams.put("ValleyPrice", "0.4");
        defaultParams.put("ServiceFeeRate", "0.8");

        // 参数描述
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("FastChargingPileNum", "快充电桩数量");
        descriptions.put("TrickleChargingPileNum", "慢充电桩数量");
        descriptions.put("WaitingAreaSize", "等候区车位容量");
        descriptions.put("ChargingQueueLen", "充电桩排队队列长度");
        descriptions.put("FastChargingPower", "快充功率(度/小时)");
        descriptions.put("TrickleChargingPower", "慢充功率(度/小时)");
        descriptions.put("PeakPrice", "峰时电价(元/度)");
        descriptions.put("FlatPrice", "平时电价(元/度)");
        descriptions.put("ValleyPrice", "谷时电价(元/度)");
        descriptions.put("ServiceFeeRate", "服务费率(元/度)");

        // 批量插入
        for (Map.Entry<String, String> entry : defaultParams.entrySet()) {
            SystemParam param = new SystemParam();
            param.setParamKey(entry.getKey());
            param.setParamValue(entry.getValue());
            param.setDescription(descriptions.getOrDefault(entry.getKey(), ""));
            param.setCreateTime(LocalDateTime.now());
            param.setUpdateTime(LocalDateTime.now());

            systemParamMapper.insert(param);
        }

        System.out.println("已初始化系统参数");
    }
}
