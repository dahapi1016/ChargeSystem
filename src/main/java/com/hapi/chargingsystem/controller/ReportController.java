package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.dto.resp.ChargingReportVO;
import com.hapi.chargingsystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表生成相关
 */
@RestController
@RequestMapping("/api/admin/report")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取日报表
     */
    @GetMapping("/daily")
    public Result<List<ChargingReportVO>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ChargingReportVO> reports = reportService.getDailyReport(date);
        return Result.success(reports);
    }

    /**
     * 获取周报表
     */
    @GetMapping("/weekly")
    public Result<List<ChargingReportVO>> getWeeklyReport(
            @RequestParam Integer weekOfYear,
            @RequestParam Integer year) {
        List<ChargingReportVO> reports = reportService.getWeeklyReport(weekOfYear, year);
        return Result.success(reports);
    }

    /**
     * 获取月报表
     */
    @GetMapping("/monthly")
    public Result<List<ChargingReportVO>> getMonthlyReport(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<ChargingReportVO> reports = reportService.getMonthlyReport(month, year);
        return Result.success(reports);
    }

    /**
     * 获取充电桩报表
     */
    @GetMapping("/pile/{pileId}")
    public Result<List<ChargingReportVO>> getPileReport(
            @PathVariable Long pileId,
            @RequestParam Integer reportType) {
        List<ChargingReportVO> reports = reportService.getPileReport(pileId, reportType);
        return Result.success(reports);
    }

    /**
     * 手动生成日报表
     */
    @PostMapping("/generate/daily")
    public Result<Void> generateDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        reportService.generateDailyReport(date);
        return Result.success(null);
    }

    /**
     * 手动生成周报表
     */
    @PostMapping("/generate/weekly")
    public Result<Void> generateWeeklyReport(
            @RequestParam Integer weekOfYear,
            @RequestParam Integer year) {
        reportService.generateWeeklyReport(weekOfYear, year);
        return Result.success(null);
    }

    /**
     * 手动生成月报表
     */
    @PostMapping("/generate/monthly")
    public Result<Void> generateMonthlyReport(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        reportService.generateMonthlyReport(month, year);
        return Result.success(null);
    }
}
