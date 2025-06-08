package com.hapi.chargingsystem.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemParamVO {

    private Long id;

    private String paramKey;

    private String paramValue;

    private String description;

    private LocalDateTime updateTime;
}
