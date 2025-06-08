package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.dto.req.ChargeReqDTO;
import com.hapi.chargingsystem.dto.resp.ChargeRespDTO;
import com.hapi.chargingsystem.dto.resp.ChargingStatusVO;
import com.hapi.chargingsystem.dto.resp.QueueStatusRespDTO;
import com.hapi.chargingsystem.service.ChargingRequestService;
import com.hapi.chargingsystem.service.ChargingService;
import com.hapi.chargingsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 充电请求相关
 */
@RestController
@RequestMapping("/api/charging")
public class ChargingController {

    @Autowired
    private ChargingService chargingService;

    @Autowired
    private ChargingRequestService chargingRequestService;

    @Autowired
    private UserService userService;

    /**
     * 提交充电请求
     */
    @PostMapping("/request")
    public Result<ChargeRespDTO> submitRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChargeReqDTO requestDTO) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        ChargeRespDTO response = chargingRequestService.submitRequest(userId, requestDTO);
        return Result.success(response);
    }

    /**
     * 修改充电请求
     */
    @PostMapping("/request/change")
    public Result<ChargeRespDTO> updateRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChargeReqDTO updateDTO) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        ChargeRespDTO response = chargingRequestService.updateRequest(userId, updateDTO);
        return Result.success(response);
    }

    /**
     * 取消充电请求
     */
    @PostMapping("/request/cancel")
    public Result<Boolean> cancelRequest(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        boolean success = chargingRequestService.cancelRequest(userId);
        return Result.success(success);
    }

    /**
     * 查看当前充电请求（包含排队号码）
     */
    @GetMapping("/request/current")
    public Result<ChargeRespDTO> getCurrentRequest(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        ChargeRespDTO response = chargingRequestService.getCurrentRequest(userId);
        return Result.success(response);
    }

    /**
     * 查看排队状态（前车等待数量等）
     */
    @GetMapping("/queue/status")
    public Result<QueueStatusRespDTO> getQueueStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        QueueStatusRespDTO response = chargingRequestService.getQueueStatus(userId);
        return Result.success(response);
    }

    /**
     * 结束充电
     */
    @PostMapping("/end")
    public Result<Boolean> endCharging(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        boolean success = chargingRequestService.endCharging(userId);
        return Result.success(success);
    }

    /**
     * 获取充电状态
     */
    @GetMapping("/status/{requestId}")
    public Result<ChargingStatusVO> getChargingStatus(@PathVariable Long requestId) {
        ChargingStatusVO status = chargingService.getChargingStatus(requestId);
        return Result.success(status);
    }
}
