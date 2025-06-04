package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.enums.UserRole;
import com.hapi.chargingsystem.common.utils.JwtTokenUtil;
import com.hapi.chargingsystem.domain.User;
import com.hapi.chargingsystem.dto.UserDTO;
import com.hapi.chargingsystem.dto.req.LoginReqDTO;
import com.hapi.chargingsystem.dto.req.RegisterReqDTO;
import com.hapi.chargingsystem.dto.resp.LoginRespDTO;
import com.hapi.chargingsystem.service.UserService;
import com.hapi.chargingsystem.service.impl.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterReqDTO registerRequest) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.USER.name());  // 默认为普通用户
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());

        userService.save(user);

        return ResponseEntity.ok("注册成功");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginReqDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // 获取用户信息
        User user = userService.getByUsername(loginRequest.getUsername());
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        LoginRespDTO response = new LoginRespDTO();
        response.setToken(token);
        response.setUserInfo(userDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Principal principal) {
        User user = userService.getByUsername(principal.getName());
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return ResponseEntity.ok(userDTO);
    }
}
