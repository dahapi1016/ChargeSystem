package com.hapi.chargingsystem.components;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hapi.chargingsystem.common.enums.PileStatus;
import com.hapi.chargingsystem.common.enums.PileType;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.mapper.ChargingPileMapper;
import com.hapi.chargingsystem.service.SystemParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChargingPileInitializer implements CommandLineRunner {

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private SystemParamService systemParamService;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在充电桩
        long count = pileMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            return;
        }

        // 获取系统参数
        int fastPileNum = systemParamService.getFastChargingPileNum();
        int tricklePileNum = systemParamService.getTrickleChargingPileNum();
        double fastPower = systemParamService.getFastChargingPower();
        double tricklePower = systemParamService.getTrickleChargingPower();

        List<ChargingPile> piles = new ArrayList<>();

        // 创建快充电桩
        for (int i = 0; i < fastPileNum; i++) {
            ChargingPile pile = new ChargingPile();
            pile.setPileCode(String.valueOf((char)('A' + i)));
            pile.setPileType(PileType.FAST.getCode());
            pile.setStatus(PileStatus.NORMAL.getCode());
            pile.setPower(BigDecimal.valueOf(fastPower));
            pile.setTotalChargingTimes(0);
            pile.setTotalChargingDuration(0);
            pile.setTotalChargingAmount(BigDecimal.ZERO);
            pile.setCreateTime(LocalDateTime.now());
            pile.setUpdateTime(LocalDateTime.now());

            piles.add(pile);
        }

        // 创建慢充电桩
        for (int i = 0; i < tricklePileNum; i++) {
            ChargingPile pile = new ChargingPile();
            pile.setPileCode(String.valueOf((char)('A' + fastPileNum + i)));
            pile.setPileType(PileType.TRICKLE.getCode());
            pile.setStatus(PileStatus.NORMAL.getCode());
            pile.setPower(BigDecimal.valueOf(tricklePower));
            pile.setTotalChargingTimes(0);
            pile.setTotalChargingDuration(0);
            pile.setTotalChargingAmount(BigDecimal.ZERO);
            pile.setCreateTime(LocalDateTime.now());
            pile.setUpdateTime(LocalDateTime.now());

            piles.add(pile);
        }

        // 批量插入
        for (ChargingPile pile : piles) {
            pileMapper.insert(pile);
        }

        System.out.println("已初始化充电桩: " + fastPileNum + "个快充, " + tricklePileNum + "个慢充");
    }
}
