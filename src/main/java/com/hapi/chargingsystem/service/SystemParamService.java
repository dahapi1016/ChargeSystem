package com.hapi.chargingsystem.service;


import com.hapi.chargingsystem.domain.SystemParam;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取所有系统参数
     * @return 系统参数列表
     */
    List<SystemParam> getAllParams();

    /**
     * 根据ID获取系统参数
     * @param id 参数ID
     * @return 系统参数
     */
    SystemParam getParamById(Long id);

    /**
     * 根据Key获取系统参数
     * @param key 参数Key
     * @return 系统参数
     */
    SystemParam getParamByKey(String key);

    /**
     * 更新系统参数
     * @param key 参数Key
     * @param value 参数值
     * @return 更新后的系统参数
     */
    SystemParam updateParam(String key, String value);

    /**
     * 批量更新系统参数
     * @param params 参数键值对
     * @return 更新的参数数量
     */
    int batchUpdateParams(Map<String, String> params);

    /**
     * 获取字符串参数
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    String getStringParam(String key, String defaultValue);

    /**
     * 获取整数参数
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    int getIntParam(String key, int defaultValue);

    /**
     * 获取浮点数参数
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    double getDoubleParam(String key, double defaultValue);
}
