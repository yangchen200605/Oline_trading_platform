package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.DishDTO;
import com.atguigu.oline_trading_platform.dto.DishPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.entity.DishFlavor;
import com.atguigu.oline_trading_platform.entity.Setmeal;
import com.atguigu.oline_trading_platform.entity.SetmealDish;
import com.atguigu.oline_trading_platform.mapper.DishFlavorMapper;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
import com.atguigu.oline_trading_platform.mapper.SetmealDishMapper;
import com.atguigu.oline_trading_platform.mapper.SetmealMapper;
import com.atguigu.oline_trading_platform.service.DishService;
import com.atguigu.oline_trading_platform.vo.DishVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private static final String DISH_CACHE_PREFIX = "dish:category:";

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final SetmealMapper setmealMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(1);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.insert(dish);
        saveFlavors(dish.getId(), dishDTO.getFlavors());
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
        if (old == null) {
            throw new BusinessException("菜品不存在");
        }
        if (status != null && status == 0) {
            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(
                    new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getDishId, id));
            if (setmealDishes != null && !setmealDishes.isEmpty()) {
                List<Long> setmealIds = setmealDishes.stream().map(SetmealDish::getSetmealId).toList();
                Long onSale = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                        .in(Setmeal::getId, setmealIds)
                        .eq(Setmeal::getStatus, 1));
                if (onSale != null && onSale > 0) {
                    throw new BusinessException("该菜品关联了在售套餐，不能停售");
                }
            }
        }
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        dishMapper.updateById(dish);
        cleanCache(old.getCategoryId());
    }

    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        return toDishVO(dish);
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish old = dishMapper.selectById(dishDTO.getId());
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.updateById(dish);

        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dishDTO.getId()));
        saveFlavors(dishDTO.getId(), dishDTO.getFlavors());

        if (old != null) {
            cleanCache(old.getCategoryId());
        }
        cleanCache(dishDTO.getCategoryId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        if (dish.getStatus() != null && dish.getStatus() == 1) {
            throw new BusinessException("起售中的菜品不能删除");
        }
        Long related = setmealDishMapper.selectCount(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getDishId, id));
        if (related != null && related > 0) {
            throw new BusinessException("该菜品已关联套餐，不能删除");
        }
        dishMapper.deleteById(id);
        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, id));
        cleanCache(dish.getCategoryId());
    }

    @Override
    public List<Dish> list(Long categoryId) {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getCreateTime));
    }

    @Override
    public List<DishVO> listByCategoryId(Long categoryId) {
        String key = DISH_CACHE_PREFIX + categoryId;
        try {
            String cache = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(cache)) {
                return objectMapper.readValue(cache, new TypeReference<List<DishVO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取菜品缓存失败，回源数据库: {}", e.getMessage());
        }

        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getCreateTime));
        List<DishVO> result = toDishVOList(dishes);

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入菜品缓存失败: {}", e.getMessage());
        }
        return result;
    }

    private void saveFlavors(Long dishId, List<DishFlavor> flavors) {
        if (flavors == null || flavors.isEmpty()) {
            return;
        }
        for (DishFlavor flavor : flavors) {
            flavor.setId(null);
            flavor.setDishId(dishId);
            dishFlavorMapper.insert(flavor);
        }
    }

    private DishVO toDishVO(Dish dish) {
        DishVO vo = new DishVO();
        BeanUtils.copyProperties(dish, vo);
        vo.setFlavors(dishFlavorMapper.selectList(
                new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dish.getId())));
        return vo;
    }

    private List<DishVO> toDishVOList(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = dishes.stream().map(Dish::getId).toList();
        List<DishFlavor> flavors = dishFlavorMapper.selectList(
                new LambdaQueryWrapper<DishFlavor>().in(DishFlavor::getDishId, ids));
        Map<Long, List<DishFlavor>> flavorMap = flavors == null
                ? Map.of()
                : flavors.stream().collect(Collectors.groupingBy(DishFlavor::getDishId));
        List<DishVO> result = new ArrayList<>();
        for (Dish dish : dishes) {
            DishVO vo = new DishVO();
            BeanUtils.copyProperties(dish, vo);
            vo.setFlavors(flavorMap.getOrDefault(dish.getId(), Collections.emptyList()));
            result.add(vo);
        }
        return result;
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
