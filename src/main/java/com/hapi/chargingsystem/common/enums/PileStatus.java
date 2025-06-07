package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

@Getter
public enum PileStatus {

    FAULT(0, "故障"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String description;

    PileStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PileStatus getByCode(Integer code) {
        for (PileStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
