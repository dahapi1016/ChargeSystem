package com.hapi.chargingsystem.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hapi.chargingsystem.domain.ChargingRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChargingRequestMapper extends BaseMapper<ChargingRequest> {

    @Select("SELECT MAX(CAST(SUBSTRING(queue_number, 2) AS UNSIGNED)) AS max_number FROM charging_request " +
            "WHERE charging_mode = #{chargingMode} AND queue_number LIKE #{prefix}")
    Integer getMaxQueueNumber(@Param("chargingMode") Integer chargingMode, @Param("prefix") String prefix);

    @Select("SELECT COUNT(*) FROM charging_request " +
            "WHERE charging_mode = #{chargingMode} AND status = 1")
    Integer countWaitingByMode(@Param("chargingMode") Integer chargingMode);

    @Select("SELECT COUNT(*) FROM charging_request " +
            "WHERE charging_mode = #{chargingMode} AND status = 1 AND " +
            "queue_start_time < (SELECT queue_start_time FROM charging_request WHERE id = #{requestId})")
    Integer countWaitingAhead(@Param("chargingMode") Integer chargingMode, @Param("requestId") Long requestId);

    @Select("SELECT * FROM charging_request " +
            "WHERE status = 1 AND charging_mode = #{chargingMode} " +
            "ORDER BY queue_start_time ASC")
    List<ChargingRequest> getWaitingRequestsByMode(@Param("chargingMode") Integer chargingMode);

    @Select("SELECT MAX(queue_number) FROM charging_request " +
            "WHERE charging_mode = #{chargingMode}")
    Integer getMaxQueueNumberByMode(@Param("chargingMode") Integer chargingMode);
}
