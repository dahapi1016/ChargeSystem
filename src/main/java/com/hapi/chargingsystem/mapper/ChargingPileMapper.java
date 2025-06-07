package com.hapi.chargingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hapi.chargingsystem.domain.ChargingPile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChargingPileMapper extends BaseMapper<ChargingPile> {

    @Select("SELECT * FROM charging_pile WHERE pile_type = #{pileType} AND status = 1")
    List<ChargingPile> findActivePilesByType(@Param("pileType") Integer pileType);
}
