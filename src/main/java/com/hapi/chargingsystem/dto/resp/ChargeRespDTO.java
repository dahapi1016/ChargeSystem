package com.hapi.chargingsystem.dto.resp;

import com.hapi.chargingsystem.common.enums.ChargingMode;
import com.hapi.chargingsystem.common.enums.RequestStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargeRespDTO {
    private Long id;

    private String queueNumber;

    private Integer chargingMode;

    private String chargingModeDesc;

    private BigDecimal requestAmount;

    private BigDecimal batteryCapacity;

    private Integer status;

    private String statusDesc;

    private Long pileId;

    private String pileCode;

    private Integer queuePosition;

    private LocalDateTime queueStartTime;

    private LocalDateTime chargingStartTime;

    private LocalDateTime chargingEndTime;

    private Long waitingTime; // 已等待时间（分钟）

    private Integer waitingCount; // 前车等待数量

    public void setChargingMode(Integer chargingMode) {
        this.chargingMode = chargingMode;
        ChargingMode mode = ChargingMode.getByCode(chargingMode);
        if (mode != null) {
            this.chargingModeDesc = mode.getDescription();
        }
    }

    public void setStatus(Integer status) {
        this.status = status;
        RequestStatus requestStatus = RequestStatus.getByCode(status);
        if (requestStatus != null) {
            this.statusDesc = requestStatus.getDescription();
        }
    }
}
