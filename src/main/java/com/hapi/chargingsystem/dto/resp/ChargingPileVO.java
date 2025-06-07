package com.hapi.chargingsystem.dto.resp;

import com.hapi.chargingsystem.common.enums.PileStatus;
import com.hapi.chargingsystem.common.enums.PileType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingPileVO {

    private Long id;

    private String pileCode;

    private Integer pileType;

    private String pileTypeDesc;

    private Integer status;

    private String statusDesc;

    private BigDecimal power;

    private Integer totalChargingTimes;

    private Integer totalChargingDuration;

    private BigDecimal totalChargingAmount;

    private Integer queueLength;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public void setPileType(Integer pileType) {
        this.pileType = pileType;
        PileType type = PileType.getByCode(pileType);
        if (type != null) {
            this.pileTypeDesc = type.getDescription();
        }
    }

    public void setStatus(Integer status) {
        this.status = status;
        PileStatus pileStatus = PileStatus.getByCode(status);
        if (pileStatus != null) {
            this.statusDesc = pileStatus.getDescription();
        }
    }
}
