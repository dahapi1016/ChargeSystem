package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

@Getter
public enum ReportTimeType {

    DAILY(1, "日报表"),
    WEEKLY(2, "周报表"),
    MONTHLY(3, "月报表");

    private final Integer code;
    private final String description;

    ReportTimeType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReportTimeType getByCode(Integer code) {
        for (ReportTimeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
