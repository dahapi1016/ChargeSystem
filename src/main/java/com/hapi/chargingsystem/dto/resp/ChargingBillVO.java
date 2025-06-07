package com.hapi.chargingsystem.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingBillVO {

    private Long id;

    private String billNumber;

    private Long requestId;

    private Long pileId;

    private String pileCode;

    private BigDecimal chargingAmount;

    private Integer chargingDuration;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal peakAmount;

    private BigDecimal flatAmount;

    private BigDecimal valleyAmount;

    private BigDecimal chargingFee;

    private BigDecimal serviceFee;

    private BigDecimal totalFee;

    private LocalDateTime createTime;
}
