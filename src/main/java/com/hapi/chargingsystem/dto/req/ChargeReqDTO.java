package com.hapi.chargingsystem.dto.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeReqDTO {

    @NotNull(message = "充电模式不能为空")
    @Min(value = 1, message = "充电模式必须为1(快充)或2(慢充)")
    @Max(value = 2, message = "充电模式必须为1(快充)或2(慢充)")
    private Integer chargingMode;

    @NotNull(message = "请求充电量不能为空")
    @DecimalMin(value = "0.01", message = "请求充电量必须大于0")
    private BigDecimal requestAmount;

//    @NotNull(message = "电池总容量不能为空")
    @DecimalMin(value = "0.01", message = "电池总容量必须大于0")
    private BigDecimal batteryCapacity;
}