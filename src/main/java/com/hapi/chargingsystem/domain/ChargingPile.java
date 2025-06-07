package com.hapi.chargingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_pile")
public class ChargingPile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pileCode;

    private Integer pileType;  // 1-快充，2-慢充

    private Integer status;  // 1-正常，0-故障

    private BigDecimal power;  // 功率（度/小时）

    private Integer totalChargingTimes;  // 累计充电次数

    private Integer totalChargingDuration;  // 累计充电时长（分钟）

    private BigDecimal totalChargingAmount;  // 累计充电电量（度）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
