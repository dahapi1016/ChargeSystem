package com.hapi.chargingsystem.service;

import com.hapi.chargingsystem.common.http.PageResult;
import com.hapi.chargingsystem.domain.ChargingBill;
import com.hapi.chargingsystem.dto.resp.ChargingBillVO;

import java.util.List;

public interface BillingService {

    /**
     * 生成充电详单
     * @param requestId 充电请求ID
     * @param isPileFault 是否因充电桩故障而结束充电
     * @return 充电详单
     */
    ChargingBill generateBill(Long requestId, boolean isPileFault);

    /**
     * 获取用户的充电详单列表
     * @param userId 用户ID
     * @return 详单列表
     */
    List<ChargingBillVO> getUserBills(Long userId);

    /**
     * 分页获取用户的充电详单列表
     * @param userId 用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页详单列表
     */
    PageResult<ChargingBillVO> getUserBillsWithPage(Long userId, long current, long size);

    /**
     * 获取详单详情
     * @param billId 详单ID
     * @return 详单详情
     */
    ChargingBillVO getBillDetail(Long billId);

    /**
     * 获取已充电量
     * @param requestId 充电请求ID
     * @return 已充电量
     */
    Double getChargedAmount(Long requestId);
}
