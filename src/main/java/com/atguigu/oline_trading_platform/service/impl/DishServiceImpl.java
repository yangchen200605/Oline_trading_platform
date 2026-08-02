package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.DishDTO;
import com.atguigu.oline_trading_platform.dto.DishPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
import com.atguigu.oline_trading_platform.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private static final String DISH_CACHE_PREFIX = "dish:category:";

    private final DishMapper dishMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(1);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.insert(dish);
        cleanCache(dishDTO.getCategoryId());
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        Page<Dish> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dishPageQueryDTO.getName()),
                Dish::getName, dishPageQueryDTO.getName());
        wrapper.eq(dishPageQueryDTO.getCategoryId() != null,
                Dish::getCategoryId, dishPageQueryDTO.getCategoryId());
        wrapper.eq(dishPageQueryDTO.getStatus() != null,
                Dish::getStatus, dishPageQueryDTO.getStatus());
        wrapper.orderByDesc(Dish::getCreateTime);

        dishMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish old = dishMapper.selectById(id);
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        dishMapper.updateById(dish);
        if (old != null) {
            cleanCache(old.getCategoryId());
        }
    }

    @Override
    public Dish getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        return dish;
    }

    @Override
    public void update(DishDTO dishDTO) {
        Dish old = dishMapper.selectById(dishDTO.getId());
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.updateById(dish);
        if (old != null) {
            cleanCache(old.getCategoryId());
        }
        cleanCache(dishDTO.getCategoryId());
    }

    @Override
    public void deleteById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        if (dish.getStatus() != null && dish.getStatus() == 1) {
            throw new BusinessException("起售中的菜品不能删除");
        }
        dishMapper.deleteById(id);
        cleanCache(dish.getCategoryId());
    }

    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        String key = DISH_CACHE_PREFIX + categoryId;
        try {
            String cache = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(cache)) {
                return objectMapper.readValue(cache, new TypeReference<List<Dish>>() {});
            }
        } catch (Exception e) {
            log.warn("读取菜品缓存失败，回源数据库: {}", e.getMessage());
        }

        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getCreateTime));
        if (dishes == null) {
            dishes = Collections.emptyList();
        }

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dishes), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入菜品缓存失败: {}", e.getMessage());
        }
        return dishes;
    }

    private void cleanCache(Long categoryId) {
        try {
            if (categoryId != null) {
                stringRedisTemplate.delete(DISH_CACHE_PREFIX + categoryId);
            } else {
                Set<String> keys = stringRedisTemplate.keys(DISH_CACHE_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                }
            }
        } catch (Exception e) {
            log.warn("清理菜品缓存失败: {}", e.getMessage());
        }
    }
}
