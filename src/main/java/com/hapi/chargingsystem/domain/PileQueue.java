package com.hapi.chargingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pile_queue")
public class PileQueue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pileId;  // 充电桩ID

    private Long requestId;  // 充电请求ID

    private Integer position;  // 位置（0表示正在充电，1、2表示等待位置）

    private LocalDateTime enterTime;  // 进入队列时间

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}