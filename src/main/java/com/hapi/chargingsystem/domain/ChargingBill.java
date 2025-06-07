package com.hapi.chargingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_bill")
public class ChargingBill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String billNumber;  // 详单编号

    private Long userId;  // 用户ID

    private Long requestId;  // 充电请求ID

    private Long pileId;  // 充电桩ID

    private String pileCode;  // 充电桩编号

    private BigDecimal chargingAmount;  // 充电电量（度）

    private Integer chargingDuration;  // 充电时长（分钟）

    private LocalDateTime startTime;  // 启动时间

    private LocalDateTime endTime;  // 停止时间

    private BigDecimal peakAmount;  // 峰时段电量

    private BigDecimal flatAmount;  // 平时段电量

    private BigDecimal valleyAmount;  // 谷时段电量

    private BigDecimal chargingFee;  // 充电费用（元）

    private BigDecimal serviceFee;  // 服务费用（元）

    private BigDecimal totalFee;  // 总费用（元）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
