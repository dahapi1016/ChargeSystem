package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.service.ChargingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/charging")
@PreAuthorize("hasRole('ADMIN')")
public class AdminChargingController {

    @Autowired
    private ChargingService chargingService;

    /**
     * 处理充电桩故障
     */
    @PostMapping("/pile/{pileId}/fault")
    public Result<Boolean> handlePileFault(@PathVariable Long pileId) {
        boolean success = chargingService.handlePileFault(pileId);
        return Result.success(success);
    }
}
