package com.hapi.chargingsystem.controller;


import com.hapi.chargingsystem.common.http.Result;
import com.hapi.chargingsystem.domain.SystemParam;
import com.hapi.chargingsystem.dto.req.SystemParamItemUpdateDTO;
import com.hapi.chargingsystem.dto.req.SystemParamUpdateDTO;
import com.hapi.chargingsystem.dto.resp.SystemParamVO;
import com.hapi.chargingsystem.service.SystemParamService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统参数控制相关
 */
@RestController
@RequestMapping("/api/admin/param")
@PreAuthorize("hasRole('ADMIN')")
public class SystemParamController {

    @Autowired
    private SystemParamService systemParamService;

    /**
     * 获取所有系统参数
     */
    @GetMapping("/list")
    public Result<List<SystemParamVO>> getAllParams() {
        List<SystemParam> params = systemParamService.getAllParams();
        List<SystemParamVO> voList = params.stream().map(param -> {
            SystemParamVO vo = new SystemParamVO();
            BeanUtils.copyProperties(param, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 获取系统参数
     */
    @GetMapping("/{key}")
    public Result<SystemParamVO> getParam(@PathVariable String key) {
        SystemParam param = systemParamService.getParamByKey(key);
        if (param == null) {
            return Result.error(404, "系统参数不存在");
        }

        SystemParamVO vo = new SystemParamVO();
        BeanUtils.copyProperties(param, vo);

        return Result.success(vo);
    }

    /**
     * 更新单个系统参数
     */
    @PutMapping("/{key}")
    public Result<SystemParamVO> updateParam(
            @PathVariable String key,
            @RequestBody @Valid SystemParamItemUpdateDTO updateDTO) {
        SystemParam param = systemParamService.updateParam(key, updateDTO.getParamValue());

        SystemParamVO vo = new SystemParamVO();
        BeanUtils.copyProperties(param, vo);

        return Result.success(vo);
    }

    /**
     * 批量更新系统参数
     */
    @PutMapping("/batch")
    public Result<Integer> batchUpdateParams(@RequestBody @Valid SystemParamUpdateDTO updateDTO) {
        int updatedCount = systemParamService.batchUpdateParams(updateDTO.getParams());
        return Result.success(updatedCount);
    }
}
