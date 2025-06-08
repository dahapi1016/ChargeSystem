package com.hapi.chargingsystem.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SystemParamUpdateDTO {

    @NotNull(message = "参数不能为空")
    @NotEmpty(message = "参数不能为空")
    private Map<String, String> params;
}
