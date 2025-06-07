package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {

    WAITING(1, "等待中"),
    CHARGING(2, "充电中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String description;

    RequestStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RequestStatus getByCode(Integer code) {
        for (RequestStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}