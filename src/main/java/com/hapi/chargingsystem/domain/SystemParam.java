package com.hapi.chargingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_param")
public class SystemParam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paramKey;

    private String paramValue;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}