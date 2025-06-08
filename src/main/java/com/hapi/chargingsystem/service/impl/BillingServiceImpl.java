package com.hapi.chargingsystem.service.impl;

import com.hapi.chargingsystem.common.enums.PriceType;
import com.hapi.chargingsystem.common.enums.TimeSlot;
import com.hapi.chargingsystem.common.exception.BusinessException;
import com.hapi.chargingsystem.domain.ChargingBill;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.dto.resp.ChargingBillVO;
import com.hapi.chargingsystem.mapper.ChargingBillMapper;
import com.hapi.chargingsystem.mapper.ChargingPileMapper;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import com.hapi.chargingsystem.service.BillingService;
import com.hapi.chargingsystem.service.SystemParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingServiceImpl implements BillingService {

    @Autowired
    private ChargingRequestMapper requestMapper;

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private ChargingBillMapper billMapper;

    @Autowired
    private SystemParamService systemParamService;

    @Override
    @Transactional
    public ChargingBill generateBill(Long requestId, boolean isPileFault) {
        // 获取充电请求
        ChargingRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("充电请求不存在");
        }

        // 获取充电桩
        ChargingPile pile = pileMapper.selectById(request.getPileId());
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }

        // 计算充电时长（分钟）
        LocalDateTime startTime = request.getChargingStartTime();
        LocalDateTime endTime = isPileFault ? LocalDateTime.now() : request.getChargingEndTime();

        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            throw new BusinessException("充电时间异常");
        }

        int chargingDuration = (int) Duration.between(startTime, endTime).toMinutes();

        // 计算充电电量（度）
        BigDecimal power = pile.getPower();  // 度/小时
        BigDecimal hours = BigDecimal.valueOf(chargingDuration).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal chargingAmount = power.multiply(hours).setScale(2, RoundingMode.HALF_UP);

        // 如果因故障结束，或者充电时间过短，充电量可能超过请求量，需要限制
        if (chargingAmount.compareTo(request.getRequestAmount()) > 0) {
            chargingAmount = request.getRequestAmount();
        }

        // 计算不同时段的电量
        BigDecimal[] timeSlotAmounts = calculateTimeSlotAmounts(startTime, endTime, chargingAmount, power);
        BigDecimal peakAmount = timeSlotAmounts[0];
        BigDecimal flatAmount = timeSlotAmounts[1];
        BigDecimal valleyAmount = timeSlotAmounts[2];

        // 获取电价
        BigDecimal peakPrice = BigDecimal.valueOf(systemParamService.getDoubleParam("PeakPrice", 1.0));
        BigDecimal flatPrice = BigDecimal.valueOf(systemParamService.getDoubleParam("FlatPrice", 0.7));
        BigDecimal valleyPrice = BigDecimal.valueOf(systemParamService.getDoubleParam("ValleyPrice", 0.4));

        // 计算充电费用
        BigDecimal chargingFee = peakAmount.multiply(peakPrice)
                .add(flatAmount.multiply(flatPrice))
                .add(valleyAmount.multiply(valleyPrice))
                .setScale(2, RoundingMode.HALF_UP);

        // 计算服务费
        BigDecimal serviceFeeRate = BigDecimal.valueOf(systemParamService.getDoubleParam("ServiceFeeRate", 0.8));
        BigDecimal serviceFee = chargingAmount.multiply(serviceFeeRate).setScale(2, RoundingMode.HALF_UP);

        // 计算总费用
        BigDecimal totalFee = chargingFee.add(serviceFee).setScale(2, RoundingMode.HALF_UP);

        // 生成详单编号
        String billNumber = generateBillNumber();

        // 创建详单
        ChargingBill bill = new ChargingBill();
        bill.setBillNumber(billNumber);
        bill.setUserId(request.getUserId());
        bill.setRequestId(requestId);
        bill.setPileId(pile.getId());
        bill.setPileCode(pile.getPileCode());
        bill.setChargingAmount(chargingAmount);
        bill.setChargingDuration(chargingDuration);
        bill.setStartTime(startTime);
        bill.setEndTime(endTime);
        bill.setPeakAmount(peakAmount);
        bill.setFlatAmount(flatAmount);
        bill.setValleyAmount(valleyAmount);
        bill.setChargingFee(chargingFee);
        bill.setServiceFee(serviceFee);
        bill.setTotalFee(totalFee);
        bill.setCreateTime(LocalDateTime.now());
        bill.setUpdateTime(LocalDateTime.now());

        billMapper.insert(bill);

        // 更新充电桩统计数据
        updatePileStatistics(pile.getId(), chargingDuration, chargingAmount);

        return bill;
    }

    /**
     * 计算不同时段的电量
     * @return [峰时电量, 平时电量, 谷时电量]
     */
    private BigDecimal[] calculateTimeSlotAmounts(LocalDateTime startTime, LocalDateTime endTime,
                                                  BigDecimal totalAmount, BigDecimal power) {
        // 如果充电时间很短，简化计算
        if (Duration.between(startTime, endTime).toMinutes() < 10) {
            LocalTime time = startTime.toLocalTime();
            PriceType priceType = TimeSlot.getPriceType(time);

            BigDecimal[] result = new BigDecimal[3];
            result[0] = BigDecimal.ZERO;  // 峰时
            result[1] = BigDecimal.ZERO;  // 平时
            result[2] = BigDecimal.ZERO;  // 谷时

            if (priceType == PriceType.PEAK) {
                result[0] = totalAmount;
            } else if (priceType == PriceType.FLAT) {
                result[1] = totalAmount;
            } else {
                result[2] = totalAmount;
            }

            return result;
        }
        //TODO：完善充电策略
        // 对于较长时间的充电，需要按时间段分割计算
        // 这里使用简化算法，按照开始和结束时间的时段类型按比例分配
        PriceType startType = TimeSlot.getPriceType(startTime.toLocalTime());
        PriceType endType = TimeSlot.getPriceType(endTime.toLocalTime());

        BigDecimal[] result = new BigDecimal[3];

        if (startType == endType) {
            // 如果开始和结束时间在同一时段，则全部归为该时段
            result[0] = startType == PriceType.PEAK ? totalAmount : BigDecimal.ZERO;
            result[1] = startType == PriceType.FLAT ? totalAmount : BigDecimal.ZERO;
            result[2] = startType == PriceType.VALLEY ? totalAmount : BigDecimal.ZERO;
        } else {
            // 跨时段充电，精确统计各时段分钟数，按比例分配电量
            long totalMinutes = Duration.between(startTime, endTime).toMinutes();

            int peakMinutes = 0;
            int flatMinutes = 0;
            int valleyMinutes = 0;

            LocalDateTime current = startTime;
            while (current.isBefore(endTime)) {
                PriceType type = TimeSlot.getPriceType(current.toLocalTime());
                if (type == PriceType.PEAK) {
                    peakMinutes++;
                } else if (type == PriceType.FLAT) {
                    flatMinutes++;
                } else {
                    valleyMinutes++;
                }
                current = current.plusMinutes(1);
            }

            result[0] = totalMinutes == 0 ? BigDecimal.ZERO :
                    totalAmount.multiply(BigDecimal.valueOf(peakMinutes))
                            .divide(BigDecimal.valueOf(totalMinutes), 2, RoundingMode.HALF_UP);
            result[1] = totalMinutes == 0 ? BigDecimal.ZERO :
                    totalAmount.multiply(BigDecimal.valueOf(flatMinutes))
                            .divide(BigDecimal.valueOf(totalMinutes), 2, RoundingMode.HALF_UP);
            result[2] = totalMinutes == 0 ? BigDecimal.ZERO :
                    totalAmount.multiply(BigDecimal.valueOf(valleyMinutes))
                            .divide(BigDecimal.valueOf(totalMinutes), 2, RoundingMode.HALF_UP);
        }

        return result;
    }

    /**
     * 生成详单编号
     */
    private String generateBillNumber() {
        Integer maxNumber = billMapper.getMaxBillNumber();
        int nextNumber = (maxNumber == null) ? 1 : maxNumber + 1;
        return "B" + String.format("%06d", nextNumber);
    }

    /**
     * 更新充电桩统计数据
     */
    private void updatePileStatistics(Long pileId, int duration, BigDecimal amount) {
        ChargingPile pile = pileMapper.selectById(pileId);
        if (pile != null) {
            pile.setTotalChargingDuration(pile.getTotalChargingDuration() + duration);
            pile.setTotalChargingAmount(pile.getTotalChargingAmount().add(amount));
            pile.setUpdateTime(LocalDateTime.now());
            pileMapper.updateById(pile);
        }
    }

    @Override
    public List<ChargingBillVO> getUserBills(Long userId) {
        List<ChargingBill> bills = billMapper.findByUserId(userId);
        return bills.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ChargingBillVO getBillDetail(Long billId) {
        ChargingBill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("充电详单不存在");
        }
        return convertToVO(bill);
    }

    /**
     * 将实体转换为VO
     */
    private ChargingBillVO convertToVO(ChargingBill bill) {
        if (bill == null) {
            return null;
        }

        ChargingBillVO vo = new ChargingBillVO();
        BeanUtils.copyProperties(bill, vo);

        return vo;
    }
}
