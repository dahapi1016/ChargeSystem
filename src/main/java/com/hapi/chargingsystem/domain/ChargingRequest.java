package com.hapi.chargingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_request")
public class ChargingRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String queueNumber;

    private Integer chargingMode; // 1-快充，2-慢充

    private BigDecimal requestAmount;

    private BigDecimal batteryCapacity;

    private Integer status; // 1-等待中，2-充电中，3-已完成，4-已取消

    private Long pileId;

    private Integer queuePosition;

    private LocalDateTime queueStartTime;

    private LocalDateTime chargingStartTime;

    private LocalDateTime chargingEndTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

