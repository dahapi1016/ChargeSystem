package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.PageResult;
import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.dto.resp.ChargingBillVO;
import com.hapi.chargingsystem.service.BillingService;
import com.hapi.chargingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充电详单相关
 */
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private UserService userService;

    /**
     * 分页获取用户充电详单列表
     */
    @GetMapping("/list")
    public Result<PageResult<ChargingBillVO>> getBillList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "3") long size) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        PageResult<ChargingBillVO> bills = billingService.getUserBillsWithPage(userId, current, size);
        return Result.success(bills);
    }

    /**
     * 获取用户充电详单列表（不分页）
     */
    @GetMapping("/list/all")
    public Result<List<ChargingBillVO>> getAllBillList(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<ChargingBillVO> bills = billingService.getUserBills(userId);
        return Result.success(bills);
    }

    /**
     * 获取充电详单详情
     */
    @GetMapping("/detail/{billId}")
    public Result<ChargingBillVO> getBillDetail(@PathVariable Long billId) {
        ChargingBillVO bill = billingService.getBillDetail(billId);
        return Result.success(bill);
    }
}
