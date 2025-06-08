package com.hapi.chargingsystem.service;

import com.hapi.chargingsystem.dto.resp.ChargingReportVO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /**
     * 生成日报表
     * @param date 日期
     */
    void generateDailyReport(LocalDate date);

    /**
     * 生成周报表
     * @param weekOfYear 年中的第几周
     * @param year 年份
     */
    void generateWeeklyReport(Integer weekOfYear, Integer year);

    /**
     * 生成月报表
     * @param month 月份
     * @param year 年份
     */
    void generateMonthlyReport(Integer month, Integer year);

    /**
     * 获取日报表
     * @param date 日期
     * @return 日报表列表
     */
    List<ChargingReportVO> getDailyReport(LocalDate date);

    /**
     * 获取周报表
     * @param weekOfYear 年中的第几周
     * @param year 年份
     * @return 周报表列表
     */
    List<ChargingReportVO> getWeeklyReport(Integer weekOfYear, Integer year);

    /**
     * 获取月报表
     * @param month 月份
     * @param year 年份
     * @return 月报表列表
     */
    List<ChargingReportVO> getMonthlyReport(Integer month, Integer year);

    /**
     * 获取充电桩报表
     * @param pileId 充电桩ID
     * @param reportType 报表类型
     * @return 充电桩报表列表
     */
    List<ChargingReportVO> getPileReport(Long pileId, Integer reportType);
}
