package com.hapi.chargingsystem.dto.resp;

import lombok.Data;

@Data
public class QueueStatusRespDTO {

    private String queueNumber;

    private Integer waitingCount; // 前车等待数量

    private Integer totalWaitingCount; // 同类型总等待数量

    private Integer estimatedWaitingTime; // 预计等待时间（分钟）
}
