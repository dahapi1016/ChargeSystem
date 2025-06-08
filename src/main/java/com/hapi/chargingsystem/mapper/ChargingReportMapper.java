package com.hapi.chargingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hapi.chargingsystem.domain.ChargingReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChargingReportMapper extends BaseMapper<ChargingReport> {

    @Select("SELECT * FROM charging_report WHERE report_type = #{reportType} AND report_date = #{reportDate}")
    List<ChargingReport> findDailyReports(@Param("reportType") Integer reportType, @Param("reportDate") LocalDate reportDate);

    @Select("SELECT * FROM charging_report WHERE report_type = #{reportType} AND week_of_year = #{weekOfYear}")
    List<ChargingReport> findWeeklyReports(@Param("reportType") Integer reportType, @Param("weekOfYear") Integer weekOfYear);

    @Select("SELECT * FROM charging_report WHERE report_type = #{reportType} AND month_of_year = #{monthOfYear}")
    List<ChargingReport> findMonthlyReports(@Param("reportType") Integer reportType, @Param("monthOfYear") Integer monthOfYear);

    @Select("SELECT * FROM charging_report WHERE report_type = #{reportType} AND pile_id = #{pileId}")
    List<ChargingReport> findReportsByPile(@Param("reportType") Integer reportType, @Param("pileId") Long pileId);
}
