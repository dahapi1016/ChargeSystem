package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.dto.resp.ChargingBillVO;
import com.hapi.chargingsystem.dto.resp.ChargingStatusVO;
import com.hapi.chargingsystem.service.BillingService;
import com.hapi.chargingsystem.service.ChargingService;
import com.hapi.chargingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charging")
public class ChargingController {

    @Autowired
    private ChargingService chargingService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private UserService userService;

    /**
     * 获取充电状态
     */
    @GetMapping("/status/{requestId}")
    public Result<ChargingStatusVO> getChargingStatus(@PathVariable Long requestId) {
        ChargingStatusVO status = chargingService.getChargingStatus(requestId);
        return Result.success(status);
    }

    /**
     * 结束充电
     */
    @PostMapping("/end/{requestId}")
    public Result<Boolean> endCharging(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        boolean success = chargingService.endCharging(requestId, userId);
        return Result.success(success);
    }

    /**
     * 获取充电详单列表
     */
    @GetMapping("/bill/list")
    public Result<List<ChargingBillVO>> getBillList(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<ChargingBillVO> bills = billingService.getUserBills(userId);
        return Result.success(bills);
    }

    /**
     * 获取充电详单详情
     */
    @GetMapping("/bill/{billId}")
    public Result<ChargingBillVO> getBillDetail(@PathVariable Long billId) {
        ChargingBillVO bill = billingService.getBillDetail(billId);
        return Result.success(bill);
    }
}
