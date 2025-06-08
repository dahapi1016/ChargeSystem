package com.hapi.chargingsystem.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemParamItemUpdateDTO {

    @NotBlank(message = "参数值不能为空")
    private String paramValue;
}
