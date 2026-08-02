package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.CategoryDTO;
import com.atguigu.oline_trading_platform.dto.CategoryPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Category;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.mapper.CategoryMapper;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
import com.atguigu.oline_trading_platform.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }

    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        Page<Category> page = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(categoryPageQueryDTO.getName()),
                Category::getName, categoryPageQueryDTO.getName());
        wrapper.eq(categoryPageQueryDTO.getType() != null,
                Category::getType, categoryPageQueryDTO.getType());
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);

        categoryMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.updateById(category);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteById(Long id) {
        Long count = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException("当前分类下有菜品，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(type != null, Category::getType, type)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime));
    }
}
