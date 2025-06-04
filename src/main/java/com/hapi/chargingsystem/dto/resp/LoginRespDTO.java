package com.hapi.chargingsystem.dto.resp;

import com.hapi.chargingsystem.dto.UserDTO;
import lombok.Data;

@Data
public class LoginRespDTO {

    String token;

    UserDTO userInfo;
}
