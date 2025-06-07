package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

@Getter
public enum ChargingMode {

    FAST(1, "快充", "F"),
    TRICKLE(2, "慢充", "T");

    private final Integer code;
    private final String description;
    private final String prefix;

    ChargingMode(Integer code, String description, String prefix) {
        this.code = code;
        this.description = description;
        this.prefix = prefix;
    }

    public static ChargingMode getByCode(Integer code) {
        for (ChargingMode mode : values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
