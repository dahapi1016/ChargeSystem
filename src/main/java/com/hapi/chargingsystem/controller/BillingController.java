package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.dto.resp.ChargingBillVO;
import com.hapi.chargingsystem.service.BillingService;
import com.hapi.chargingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 获取用户充电详单列表
     */
    @GetMapping("/list")
    public Result<List<ChargingBillVO>> getBillList(@AuthenticationPrincipal UserDetails userDetails) {
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
