package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.SetmealDTO;
import com.atguigu.oline_trading_platform.dto.SetmealPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.entity.Setmeal;
import com.atguigu.oline_trading_platform.entity.SetmealDish;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
import com.atguigu.oline_trading_platform.mapper.SetmealDishMapper;
import com.atguigu.oline_trading_platform.mapper.SetmealMapper;
import com.atguigu.oline_trading_platform.service.SetmealService;
import com.atguigu.oline_trading_platform.vo.SetmealVO;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {

    private static final String SETMEAL_CACHE_PREFIX = "setmeal:category:";

    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final DishMapper dishMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(0);
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.insert(setmeal);
        saveSetmealDishes(setmeal.getId(), setmealDTO.getSetmealDishes());
        cleanCache(setmeal.getCategoryId());
    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        Page<Setmeal> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(setmealPageQueryDTO.getName()),
                Setmeal::getName, setmealPageQueryDTO.getName());
        wrapper.eq(setmealPageQueryDTO.getCategoryId() != null,
                Setmeal::getCategoryId, setmealPageQueryDTO.getCategoryId());
        wrapper.eq(setmealPageQueryDTO.getStatus() != null,
                Setmeal::getStatus, setmealPageQueryDTO.getStatus());
        wrapper.orderByDesc(Setmeal::getCreateTime);
        setmealMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal old = setmealMapper.selectById(id);
        if (old == null) {
            throw new BusinessException("套餐不存在");
        }
        if (status != null && status == 1) {
            List<SetmealDish> dishes = setmealDishMapper.selectList(
                    new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
            if (dishes == null || dishes.isEmpty()) {
                throw new BusinessException("套餐内没有菜品，不能起售");
            }
            for (SetmealDish setmealDish : dishes) {
                Dish dish = dishMapper.selectById(setmealDish.getDishId());
                if (dish == null || dish.getStatus() == null || dish.getStatus() == 0) {
                    throw new BusinessException("套餐内含有停售菜品，不能起售");
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        setmealMapper.updateById(setmeal);
        cleanCache(old.getCategoryId());
    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new BusinessException("套餐不存在");
        }
        return toVO(setmeal);
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal old = setmealMapper.selectById(setmealDTO.getId());
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.updateById(setmeal);

        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, setmealDTO.getId()));
        saveSetmealDishes(setmealDTO.getId(), setmealDTO.getSetmealDishes());

        if (old != null) {
            cleanCache(old.getCategoryId());
        }
        cleanCache(setmealDTO.getCategoryId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new BusinessException("套餐不存在");
        }
        if (setmeal.getStatus() != null && setmeal.getStatus() == 1) {
            throw new BusinessException("起售中的套餐不能删除");
        }
        setmealMapper.deleteById(id);
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        cleanCache(setmeal.getCategoryId());
    }

    @Override
    public List<SetmealVO> listByCategoryId(Long categoryId) {
        String key = SETMEAL_CACHE_PREFIX + categoryId;
        try {
            String cache = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(cache)) {
                return objectMapper.readValue(cache, new TypeReference<List<SetmealVO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取套餐缓存失败，回源数据库: {}", e.getMessage());
        }

        List<Setmeal> setmeals = setmealMapper.selectList(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, 1)
                .orderByDesc(Setmeal::getCreateTime));
        List<SetmealVO> result = new ArrayList<>();
        if (setmeals != null) {
            for (Setmeal setmeal : setmeals) {
                result.add(toVO(setmeal));
            }
        }

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入套餐缓存失败: {}", e.getMessage());
        }
        return result;
    }

    private void saveSetmealDishes(Long setmealId, List<SetmealDish> setmealDishes) {
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            throw new BusinessException("套餐至少包含一道菜品");
        }
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setId(null);
            setmealDish.setSetmealId(setmealId);
            if (!StringUtils.hasText(setmealDish.getName()) && setmealDish.getDishId() != null) {
                Dish dish = dishMapper.selectById(setmealDish.getDishId());
                if (dish != null) {
                    setmealDish.setName(dish.getName());
                    if (setmealDish.getPrice() == null) {
                        setmealDish.setPrice(dish.getPrice());
                    }
                }
            }
            setmealDishMapper.insert(setmealDish);
        }
    }

    private SetmealVO toVO(Setmeal setmeal) {
        SetmealVO vo = new SetmealVO();
        BeanUtils.copyProperties(setmeal, vo);
        vo.setSetmealDishes(setmealDishMapper.selectList(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, setmeal.getId())));
        return vo;
    }

    private void cleanCache(Long categoryId) {
        try {
            if (categoryId != null) {
                stringRedisTemplate.delete(SETMEAL_CACHE_PREFIX + categoryId);
            } else {
                Set<String> keys = stringRedisTemplate.keys(SETMEAL_CACHE_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                }
            }
        } catch (Exception e) {
            log.warn("清理套餐缓存失败: {}", e.getMessage());
        }
    }
}
