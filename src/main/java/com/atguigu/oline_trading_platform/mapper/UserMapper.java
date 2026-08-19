package com.atguigu.oline_trading_platform.mapper;

import com.atguigu.oline_trading_platform.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(*) FROM user WHERE create_time >= #{begin} AND create_time < #{end}")
    Integer countNewUsers(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(*) FROM user WHERE create_time < #{end}")
    Integer countTotalUsers(@Param("end") LocalDateTime end);
}
