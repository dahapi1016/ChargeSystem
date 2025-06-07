package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

@Getter
public enum PriceType {

    PEAK("峰时", "PeakPrice"),
    FLAT("平时", "FlatPrice"),
    VALLEY("谷时", "ValleyPrice");

    private final String description;
    private final String paramKey;

    PriceType(String description, String paramKey) {
        this.description = description;
        this.paramKey = paramKey;
    }
}
