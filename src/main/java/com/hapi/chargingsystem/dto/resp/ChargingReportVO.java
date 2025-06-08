package com.hapi.chargingsystem.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ChargingReportVO {

    private Long id;

    private Integer reportType;

    private String reportTypeDesc;

    private LocalDate reportDate;

    private Integer weekOfYear;

    private Integer monthOfYear;

    private Long pileId;

    private String pileCode;

    private Integer chargingTimes;

    private Integer chargingDuration;

    private BigDecimal chargingAmount;

    private BigDecimal chargingFee;

    private BigDecimal serviceFee;

    private BigDecimal totalFee;
}
