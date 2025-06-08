package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.enums.UserRole;
import com.hapi.chargingsystem.common.http.Result;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户鉴权相关
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    /**
     * 用户注册
     * @param registerRequest 注册信息表单
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterReqDTO registerRequest) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return Result.error(400, "用户名已存在");
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

        return Result.success("注册成功");
    }

    /**
     * 管理员账号注册
     * @param registerRequest 注册信息表单
     * @param userDetails 当前登录的用户信息
     * @return 注册结果
     */
    @PostMapping("/register/admin")
    public Result<String> registerAdmin(@RequestBody @Valid RegisterReqDTO registerRequest,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        // 检查当前用户是否为管理员
        User currentUser = userService.getByUsername(userDetails.getUsername());
        if (currentUser == null || !UserRole.ADMIN.name().equals(currentUser.getRole())) {
            return Result.error(403, "权限不足，只有管理员才能创建管理员账号");
        }

        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return Result.error(400, "用户名已存在");
        }

        // 创建新管理员用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.ADMIN.name());  // 设置为管理员角色
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());

        userService.save(user);

        return Result.success("管理员账号创建成功");
    }

    /**
     * 初始化管理员账号注册（无需权限验证）
     * @param registerRequest 注册信息表单
     * @return 注册结果
     */
    @PostMapping("/init-admin")
    public Result<String> initAdmin(@RequestBody @Valid RegisterReqDTO registerRequest) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return Result.error(400, "用户名已存在");
        }

        // 创建管理员用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.ADMIN.name());  // 设置为管理员角色
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());

        userService.save(user);

        return Result.success("管理员账号初始化成功");
    }

    /**
     * 用户登录
     * @param loginRequest 登录请求表单
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginRespDTO> login(@RequestBody @Valid LoginReqDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return Result.error(401, "用户名或密码错误");
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

        return Result.success(response);
    }

    /**
     * 获取用户信息
     * @param userDetails 当前用户
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return Result.success(userDTO);
    }
}
