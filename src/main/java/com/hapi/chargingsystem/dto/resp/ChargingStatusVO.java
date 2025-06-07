package com.hapi.chargingsystem.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingStatusVO {

    private Long requestId;

    private Long pileId;

    private String pileCode;

    private Integer chargingMode;

    private String chargingModeDesc;

    private BigDecimal requestAmount;

    private BigDecimal currentAmount;  // 当前已充电量

    private Integer chargingDuration;  // 充电时长（分钟）

    private BigDecimal estimatedFee;  // 预计费用

    private LocalDateTime startTime;

    private LocalDateTime estimatedEndTime;  // 预计结束时间

    private Integer progress;  // 充电进度（百分比）
}
