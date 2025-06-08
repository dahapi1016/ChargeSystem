package com.hapi.chargingsystem.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PileStatusUpdateDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
