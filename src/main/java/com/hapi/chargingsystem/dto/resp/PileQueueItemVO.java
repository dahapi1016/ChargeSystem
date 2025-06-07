package com.hapi.chargingsystem.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PileQueueItemVO {

    private Long requestId;

    private Long userId;

    private String username;

    private BigDecimal batteryCapacity;

    private BigDecimal requestAmount;

    private Integer position;

    private LocalDateTime enterTime;

    private Long waitingTime;  // 排队时长（分钟）
}
