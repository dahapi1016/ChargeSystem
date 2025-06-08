package com.hapi.chargingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("charging_report")
public class ChargingReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer reportType;  // 报表类型：1-日报表，2-周报表，3-月报表

    private LocalDate reportDate;  // 报表日期

    private Integer weekOfYear;  // 年中的第几周（周报表使用）

    private Integer monthOfYear;  // 年中的第几月（月报表使用）

    private Long pileId;  // 充电桩ID

    private String pileCode;  // 充电桩编号

    private Integer chargingTimes;  // 充电次数

    private Integer chargingDuration;  // 充电时长（分钟）

    private BigDecimal chargingAmount;  // 充电电量（度）

    private BigDecimal chargingFee;  // 充电费用（元）

    private BigDecimal serviceFee;  // 服务费用（元）

    private BigDecimal totalFee;  // 总费用（元）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
