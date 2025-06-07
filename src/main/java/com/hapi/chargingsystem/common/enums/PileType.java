package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

@Getter
public enum PileType {

    FAST(1, "快充"),
    TRICKLE(2, "慢充");

    private final Integer code;
    private final String description;

    PileType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PileType getByCode(Integer code) {
        for (PileType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
