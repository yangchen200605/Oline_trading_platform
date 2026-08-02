package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.CategoryDTO;
import com.atguigu.oline_trading_platform.dto.CategoryPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Category;

import java.util.List;

public interface CategoryService {

    void save(CategoryDTO categoryDTO);

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void update(CategoryDTO categoryDTO);

    void deleteById(Long id);

    List<Category> list(Integer type);
}
