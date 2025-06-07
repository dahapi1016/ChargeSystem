package com.hapi.chargingsystem.common.enums;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public enum TimeSlot {

    PEAK_1(LocalTime.of(10, 0), LocalTime.of(15, 0), "峰时段1"),
    PEAK_2(LocalTime.of(18, 0), LocalTime.of(21, 0), "峰时段2"),
    FLAT_1(LocalTime.of(7, 0), LocalTime.of(10, 0), "平时段1"),
    FLAT_2(LocalTime.of(15, 0), LocalTime.of(18, 0), "平时段2"),
    FLAT_3(LocalTime.of(21, 0), LocalTime.of(23, 0), "平时段3"),
    VALLEY(LocalTime.of(23, 0), LocalTime.of(7, 0), "谷时段");

    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String description;

    TimeSlot(LocalTime startTime, LocalTime endTime, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    /**
     * 判断给定时间是否在此时间段内
     */
    public boolean contains(LocalTime time) {
        if (endTime.isAfter(startTime)) {
            // 正常时间段，如10:00-15:00
            return !time.isBefore(startTime) && time.isBefore(endTime);
        } else {
            // 跨天时间段，如23:00-7:00
            return !time.isBefore(startTime) || time.isBefore(endTime);
        }
    }

    /**
     * 获取时间所属的时间段类型
     */
    public static PriceType getPriceType(LocalTime time) {
        if (PEAK_1.contains(time) || PEAK_2.contains(time)) {
            return PriceType.PEAK;
        } else if (FLAT_1.contains(time) || FLAT_2.contains(time) || FLAT_3.contains(time)) {
            return PriceType.FLAT;
        } else {
            return PriceType.VALLEY;
        }
    }
}
