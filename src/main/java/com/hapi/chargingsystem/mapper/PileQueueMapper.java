package com.hapi.chargingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hapi.chargingsystem.domain.PileQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PileQueueMapper extends BaseMapper<PileQueue> {

    @Select("SELECT * FROM pile_queue WHERE pile_id = #{pileId} ORDER BY position ASC")
    List<PileQueue> findQueueByPileId(@Param("pileId") Long pileId);

    @Select("SELECT COUNT(*) FROM pile_queue WHERE pile_id = #{pileId}")
    Integer countQueueByPileId(@Param("pileId") Long pileId);

    @Select("SELECT * FROM pile_queue WHERE request_id = #{requestId}")
    PileQueue findByRequestId(@Param("requestId") Long requestId);
}