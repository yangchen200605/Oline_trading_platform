package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.DishDTO;
import com.atguigu.oline_trading_platform.dto.DishPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Dish;

import java.util.List;

public interface DishService {

    void save(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void startOrStop(Integer status, Long id);

    Dish getById(Long id);

    void update(DishDTO dishDTO);

    void deleteById(Long id);

    /** 按分类查询起售菜品（带 Redis 缓存） */
    List<Dish> listByCategoryId(Long categoryId);
}
