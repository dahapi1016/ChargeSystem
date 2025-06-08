package com.hapi.chargingsystem.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String username;

    private String nickname;

    private String role;

    private String roleDesc;
}