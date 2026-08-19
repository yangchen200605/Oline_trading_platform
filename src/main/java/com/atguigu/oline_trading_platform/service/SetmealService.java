package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.SetmealDTO;
import com.atguigu.oline_trading_platform.dto.SetmealPageQueryDTO;
import com.atguigu.oline_trading_platform.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    void save(SetmealDTO setmealDTO);

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    void startOrStop(Integer status, Long id);

    SetmealVO getById(Long id);

    void update(SetmealDTO setmealDTO);

    void deleteById(Long id);

    List<SetmealVO> listByCategoryId(Long categoryId);
}
