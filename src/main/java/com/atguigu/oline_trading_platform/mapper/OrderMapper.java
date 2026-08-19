package com.atguigu.oline_trading_platform.mapper;

import com.atguigu.oline_trading_platform.dto.GoodsSalesDTO;
import com.atguigu.oline_trading_platform.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper extends BaseMapper<Orders> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders " +
            "WHERE status = 5 AND order_time >= #{begin} AND order_time < #{end}")
    BigDecimal sumTurnover(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(*) FROM orders WHERE order_time >= #{begin} AND order_time < #{end}")
    Integer countByTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(*) FROM orders WHERE status = 5 AND order_time >= #{begin} AND order_time < #{end}")
    Integer countValidByTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("SELECT od.name AS name, SUM(od.number) AS number " +
            "FROM order_detail od INNER JOIN orders o ON od.order_id = o.id " +
            "WHERE o.status = 5 AND o.order_time >= #{begin} AND o.order_time < #{end} " +
            "GROUP BY od.name ORDER BY number DESC LIMIT 10")
    List<GoodsSalesDTO> top10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
