package com.hapi.chargingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hapi.chargingsystem.domain.ChargingBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChargingBillMapper extends BaseMapper<ChargingBill> {

    @Select("SELECT * FROM charging_bill WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ChargingBill> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM charging_bill WHERE request_id = #{requestId}")
    ChargingBill findByRequestId(@Param("requestId") Long requestId);

    @Select("SELECT MAX(SUBSTRING(bill_number, 2)) FROM charging_bill")
    Integer getMaxBillNumber();
}
