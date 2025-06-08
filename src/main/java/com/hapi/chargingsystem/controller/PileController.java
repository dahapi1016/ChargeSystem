package com.hapi.chargingsystem.controller;

import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.domain.ChargingPile;
import com.hapi.chargingsystem.domain.ChargingRequest;
import com.hapi.chargingsystem.domain.PileQueue;
import com.hapi.chargingsystem.domain.User;
import com.hapi.chargingsystem.dto.resp.ChargingPileVO;
import com.hapi.chargingsystem.dto.resp.PileQueueItemVO;
import com.hapi.chargingsystem.mapper.ChargingPileMapper;
import com.hapi.chargingsystem.mapper.ChargingRequestMapper;
import com.hapi.chargingsystem.mapper.PileQueueMapper;
import com.hapi.chargingsystem.mapper.UserMapper;
import com.hapi.chargingsystem.service.ScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 充电桩控制相关
 */
@RestController
@RequestMapping("/api/admin/pile")
@PreAuthorize("hasRole('ADMIN')")
public class PileController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ChargingPileMapper pileMapper;

    @Autowired
    private PileQueueMapper pileQueueMapper;

    @Autowired
    private ChargingRequestMapper requestMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 启动/关闭充电桩
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updatePileStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == 0) {
            // 处理故障
            scheduleService.handlePileFault(id, 1);  // 使用优先级调度策略
        } else {
            // 处理恢复
            scheduleService.handlePileRecovery(id);
        }
        return Result.success(true);
    }

    /**
     * 获取充电桩状态
     */
    @GetMapping("/{id}")
    public Result<ChargingPileVO> getPileStatus(@PathVariable Long id) {
        ChargingPile pile = scheduleService.getPileStatus(id);
        if (pile == null) {
            return Result.error(404, "充电桩不存在");
        }

        ChargingPileVO vo = new ChargingPileVO();
        BeanUtils.copyProperties(pile, vo);
        vo.setPileType(pile.getPileType());
        vo.setStatus(pile.getStatus());

        // 获取队列长度
        Integer queueLength = pileQueueMapper.countQueueByPileId(id);
        vo.setQueueLength(queueLength);

        return Result.success(vo);
    }

    /**
     * 获取充电桩队列
     */
    @GetMapping("/{id}/queue")
    public Result<List<PileQueueItemVO>> getPileQueue(@PathVariable Long id) {
        List<PileQueue> queue = pileQueueMapper.findQueueByPileId(id);
        List<PileQueueItemVO> result = new ArrayList<>();

        for (PileQueue item : queue) {
            ChargingRequest request = requestMapper.selectById(item.getRequestId());
            if (request != null) {
                User user = userMapper.selectById(request.getUserId());

                PileQueueItemVO vo = new PileQueueItemVO();
                vo.setRequestId(request.getId());
                vo.setUserId(request.getUserId());
                vo.setUsername(user != null ? user.getUsername() : "未知用户");
                vo.setBatteryCapacity(request.getBatteryCapacity());
                vo.setRequestAmount(request.getRequestAmount());
                vo.setPosition(item.getPosition());
                vo.setEnterTime(item.getEnterTime());

                // 计算排队时长
                long waitingMinutes = Duration.between(item.getEnterTime(), LocalDateTime.now()).toMinutes();
                vo.setWaitingTime(waitingMinutes);

                result.add(vo);
            }
        }

        return Result.success(result);
    }

    /**
     * 获取所有充电桩状态
     */
    @GetMapping("/status")
    public Result<List<ChargingPileVO>> getAllPiles() {
        List<ChargingPile> piles = pileMapper.selectList(null);
        List<ChargingPileVO> result = new ArrayList<>();

        for (ChargingPile pile : piles) {
            ChargingPileVO vo = new ChargingPileVO();
            BeanUtils.copyProperties(pile, vo);
            vo.setPileType(pile.getPileType());
            vo.setStatus(pile.getStatus());

            // 获取队列长度
            Integer queueLength = pileQueueMapper.countQueueByPileId(pile.getId());
            vo.setQueueLength(queueLength);

            result.add(vo);
        }

        return Result.success(result);
    }
}
