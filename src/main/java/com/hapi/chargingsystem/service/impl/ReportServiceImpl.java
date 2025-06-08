package
        com.hapi.chargingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hapi.chargingsystem.common.enums.ReportTimeType;
import com.hapi.chargingsystem.domain.ChargingBill;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingReport;
import com.hapi.chargingsystem.dto.resp.ChargingReportVO;
import com.hapi.chargingsystem.mapper.ChargingBillMapper;
import com.hapi.chargingsystem.mapper.ChargingPileMapper;
import com.hapi.chargingsystem.mapper.ChargingReportMapper;
import com.hapi.chargingsystem.service.ReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ChargingBillMapper billMapper;

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private ChargingReportMapper reportMapper;

    @Override
    @Transactional
    public void generateDailyReport(LocalDate date) {
        // 获取指定日期的所有充电详单
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();

        List<ChargingBill> bills = billMapper.selectList(
                new LambdaQueryWrapper<ChargingBill>()
                        .between(ChargingBill::getEndTime, startTime, endTime));

        // 按充电桩ID分组统计
        Map<Long, List<ChargingBill>> billsByPile = bills.stream()
                .collect(Collectors.groupingBy(ChargingBill::getPileId));

        // 生成每个充电桩的日报表
        for (Map.Entry<Long, List<ChargingBill>> entry : billsByPile.entrySet()) {
            Long pileId = entry.getKey();
            List<ChargingBill> pileBills = entry.getValue();

            // 获取充电桩信息
            ChargingPile pile = pileMapper.selectById(pileId);
            if (pile == null) {
                continue;
            }

            // 统计数据
            int chargingTimes = pileBills.size();
            int chargingDuration = pileBills.stream()
                    .mapToInt(ChargingBill::getChargingDuration)
                    .sum();
            BigDecimal chargingAmount = pileBills.stream()
                    .map(ChargingBill::getChargingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal chargingFee = pileBills.stream()
                    .map(ChargingBill::getChargingFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal serviceFee = pileBills.stream()
                    .map(ChargingBill::getServiceFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = pileBills.stream()
                    .map(ChargingBill::getTotalFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 检查是否已存在报表
            ChargingReport existingReport = reportMapper.selectOne(
                    new LambdaQueryWrapper<ChargingReport>()
                            .eq(ChargingReport::getReportType, ReportTimeType.DAILY.getCode())
                            .eq(ChargingReport::getReportDate, date)
                            .eq(ChargingReport::getPileId, pileId));

            if (existingReport != null) {
                // 更新现有报表
                existingReport.setChargingTimes(chargingTimes);
                existingReport.setChargingDuration(chargingDuration);
                existingReport.setChargingAmount(chargingAmount);
                existingReport.setChargingFee(chargingFee);
                existingReport.setServiceFee(serviceFee);
                existingReport.setTotalFee(totalFee);
                existingReport.setUpdateTime(LocalDateTime.now());

                reportMapper.updateById(existingReport);
            } else {
                // 创建新报表
                ChargingReport report = new ChargingReport();
                report.setReportType(ReportTimeType.DAILY.getCode());
                report.setReportDate(date);
                report.setWeekOfYear(date.get(WeekFields.ISO.weekOfWeekBasedYear()));
                report.setMonthOfYear(date.getMonthValue());
                report.setPileId(pileId);
                report.setPileCode(pile.getPileCode());
                report.setChargingTimes(chargingTimes);
                report.setChargingDuration(chargingDuration);
                report.setChargingAmount(chargingAmount);
                report.setChargingFee(chargingFee);
                report.setServiceFee(serviceFee);
                report.setTotalFee(totalFee);
                report.setCreateTime(LocalDateTime.now());
                report.setUpdateTime(LocalDateTime.now());

                reportMapper.insert(report);
            }
        }
    }

    @Override
    @Transactional
    public void generateWeeklyReport(Integer weekOfYear, Integer year) {
        // 获取指定周的所有日报表
        List<ChargingReport> dailyReports = reportMapper.selectList(
                new LambdaQueryWrapper<ChargingReport>()
                        .eq(ChargingReport::getReportType, ReportTimeType.DAILY.getCode())
                        .eq(ChargingReport::getWeekOfYear, weekOfYear));

        if (dailyReports.isEmpty()) {
            return;
        }

        // 按充电桩ID分组统计
        Map<Long, List<ChargingReport>> reportsByPile = dailyReports.stream()
                .collect(Collectors.groupingBy(ChargingReport::getPileId));

        // 获取该周的第一天
        LocalDate firstDayOfWeek = LocalDate.now()
                .with(WeekFields.ISO.weekOfWeekBasedYear(), weekOfYear)
                .with(WeekFields.ISO.dayOfWeek(), 1);

        // 生成每个充电桩的周报表
        for (Map.Entry<Long, List<ChargingReport>> entry : reportsByPile.entrySet()) {
            Long pileId = entry.getKey();
            List<ChargingReport> pileReports = entry.getValue();

            // 获取充电桩信息
            ChargingPile pile = pileMapper.selectById(pileId);
            if (pile == null) {
                continue;
            }

            // 统计数据
            int chargingTimes = pileReports.stream()
                    .mapToInt(ChargingReport::getChargingTimes)
                    .sum();
            int chargingDuration = pileReports.stream()
                    .mapToInt(ChargingReport::getChargingDuration)
                    .sum();
            BigDecimal chargingAmount = pileReports.stream()
                    .map(ChargingReport::getChargingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal chargingFee = pileReports.stream()
                    .map(ChargingReport::getChargingFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal serviceFee = pileReports.stream()
                    .map(ChargingReport::getServiceFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = pileReports.stream()
                    .map(ChargingReport::getTotalFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 检查是否已存在报表
            ChargingReport existingReport = reportMapper.selectOne(
                    new LambdaQueryWrapper<ChargingReport>()
                            .eq(ChargingReport::getReportType, ReportTimeType.WEEKLY.getCode())
                            .eq(ChargingReport::getWeekOfYear, weekOfYear)
                            .eq(ChargingReport::getPileId, pileId));

            if (existingReport != null) {
                // 更新现有报表
                existingReport.setChargingTimes(chargingTimes);
                existingReport.setChargingDuration(chargingDuration);
                existingReport.setChargingAmount(chargingAmount);
                existingReport.setChargingFee(chargingFee);
                existingReport.setServiceFee(serviceFee);
                existingReport.setTotalFee(totalFee);
                existingReport.setUpdateTime(LocalDateTime.now());

                reportMapper.updateById(existingReport);
            } else {
                // 创建新报表
                ChargingReport report = new ChargingReport();
                report.setReportType(ReportTimeType.WEEKLY.getCode());
                report.setReportDate(firstDayOfWeek);  // 使用该周的第一天作为报表日期
                report.setWeekOfYear(weekOfYear);
                report.setMonthOfYear(firstDayOfWeek.getMonthValue());
                report.setPileId(pileId);
                report.setPileCode(pile.getPileCode());
                report.setChargingTimes(chargingTimes);
                report.setChargingDuration(chargingDuration);
                report.setChargingAmount(chargingAmount);
                report.setChargingFee(chargingFee);
                report.setServiceFee(serviceFee);
                report.setTotalFee(totalFee);
                report.setCreateTime(LocalDateTime.now());
                report.setUpdateTime(LocalDateTime.now());

                reportMapper.insert(report);
            }
        }
    }

    @Override
    @Transactional
    public void generateMonthlyReport(Integer month, Integer year) {
        // 获取指定月的所有日报表
        List<ChargingReport> dailyReports = reportMapper.selectList(
                new LambdaQueryWrapper<ChargingReport>()
                        .eq(ChargingReport::getReportType, ReportTimeType.DAILY.getCode())
                        .eq(ChargingReport::getMonthOfYear, month));

        if (dailyReports.isEmpty()) {
            return;
        }

        // 按充电桩ID分组统计
        Map<Long, List<ChargingReport>> reportsByPile = dailyReports.stream()
                .collect(Collectors.groupingBy(ChargingReport::getPileId));

        // 获取该月的第一天
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);

        // 生成每个充电桩的月报表
        for (Map.Entry<Long, List<ChargingReport>> entry : reportsByPile.entrySet()) {
            Long pileId = entry.getKey();
            List<ChargingReport> pileReports = entry.getValue();

            // 获取充电桩信息
            ChargingPile pile = pileMapper.selectById(pileId);
            if (pile == null) {
                continue;
            }

            // 统计数据
            int chargingTimes = pileReports.stream()
                    .mapToInt(ChargingReport::getChargingTimes)
                    .sum();
            int chargingDuration = pileReports.stream()
                    .mapToInt(ChargingReport::getChargingDuration)
                    .sum();
            BigDecimal chargingAmount = pileReports.stream()
                    .map(ChargingReport::getChargingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal chargingFee = pileReports.stream()
                    .map(ChargingReport::getChargingFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal serviceFee = pileReports.stream()
                    .map(ChargingReport::getServiceFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = pileReports.stream()
                    .map(ChargingReport::getTotalFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 检查是否已存在报表
            ChargingReport existingReport = reportMapper.selectOne(
                    new LambdaQueryWrapper<ChargingReport>()
                            .eq(ChargingReport::getReportType, ReportTimeType.MONTHLY.getCode())
                            .eq(ChargingReport::getMonthOfYear, month)
                            .eq(ChargingReport::getPileId, pileId));

            if (existingReport != null) {
                // 更新现有报表
                existingReport.setChargingTimes(chargingTimes);
                existingReport.setChargingDuration(chargingDuration);
                existingReport.setChargingAmount(chargingAmount);
                existingReport.setChargingFee(chargingFee);
                existingReport.setServiceFee(serviceFee);
                existingReport.setTotalFee(totalFee);
                existingReport.setUpdateTime(LocalDateTime.now());

                reportMapper.updateById(existingReport);
            } else {
                // 创建新报表
                ChargingReport report = new ChargingReport();
                report.setReportType(ReportTimeType.MONTHLY.getCode());
                report.setReportDate(firstDayOfMonth);  // 使用该月的第一天作为报表日期
                report.setWeekOfYear(firstDayOfMonth.get(WeekFields.ISO.weekOfWeekBasedYear()));
                report.setMonthOfYear(month);
                report.setPileId(pileId);
                report.setPileCode(pile.getPileCode());
                report.setChargingTimes(chargingTimes);
                report.setChargingDuration(chargingDuration);
                report.setChargingAmount(chargingAmount);
                report.setChargingFee(chargingFee);
                report.setServiceFee(serviceFee);
                report.setTotalFee(totalFee);
                report.setCreateTime(LocalDateTime.now());
                report.setUpdateTime(LocalDateTime.now());

                reportMapper.insert(report);
            }
        }
    }

    @Override
    public List<ChargingReportVO> getDailyReport(LocalDate date) {
        List<ChargingReport> reports = reportMapper.findDailyReports(ReportTimeType.DAILY.getCode(), date);
        return convertToVOList(reports);
    }

    @Override
    public List<ChargingReportVO> getWeeklyReport(Integer weekOfYear, Integer year) {
        List<ChargingReport> reports = reportMapper.findWeeklyReports(ReportTimeType.WEEKLY.getCode(), weekOfYear);
        return convertToVOList(reports);
    }

    @Override
    public List<ChargingReportVO> getMonthlyReport(Integer month, Integer year) {
        List<ChargingReport> reports = reportMapper.findMonthlyReports(ReportTimeType.MONTHLY.getCode(), month);
        return convertToVOList(reports);
    }

    @Override
    public List<ChargingReportVO> getPileReport(Long pileId, Integer reportType) {
        List<ChargingReport> reports = reportMapper.findReportsByPile(reportType, pileId);
        return convertToVOList(reports);
    }

    /**
     * 将实体列表转换为VO列表
     */
    private List<ChargingReportVO> convertToVOList(List<ChargingReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return new ArrayList<>();
        }

        return reports.stream().map(report -> {
            ChargingReportVO vo = new ChargingReportVO();
            BeanUtils.copyProperties(report, vo);

            // 设置报表类型描述
            ReportTimeType reportType = ReportTimeType.getByCode(report.getReportType());
            if (reportType != null) {
                vo.setReportTypeDesc(reportType.getDescription());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 每天凌晨1点生成前一天的日报表
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledDailyReport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        generateDailyReport(yesterday);
    }

    /**
     * 每周一凌晨2点生成上周的周报表
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void scheduledWeeklyReport() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        int weekOfYear = lastWeek.get(WeekFields.ISO.weekOfWeekBasedYear());
        int year = lastWeek.getYear();
        generateWeeklyReport(weekOfYear, year);
    }

    /**
     * 每月1日凌晨3点生成上月的月报表
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void scheduledMonthlyReport() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int month = lastMonth.getMonthValue();
        int year = lastMonth.getYear();
        generateMonthlyReport(month, year);
    }
}
