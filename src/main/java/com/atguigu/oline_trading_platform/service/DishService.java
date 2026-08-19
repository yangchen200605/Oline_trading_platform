package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.DishDTO;
import com.atguigu.oline_trading_platform.dto.DishPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.vo.DishVO;

import java.util.List;

public interface DishService {

    void save(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void startOrStop(Integer status, Long id);

    DishVO getById(Long id);

    void update(DishDTO dishDTO);

    void deleteById(Long id);

    /** 管理端：按分类查询菜品（套餐勾选用） */
    List<Dish> list(Long categoryId);

    /** 用户端：按分类查询起售菜品及口味（带 Redis 缓存） */
    List<DishVO> listByCategoryId(Long categoryId);
}
