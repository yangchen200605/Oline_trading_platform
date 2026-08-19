package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.dto.ShoppingCartDTO;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.entity.Setmeal;
import com.atguigu.oline_trading_platform.entity.ShoppingCart;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
import com.atguigu.oline_trading_platform.mapper.SetmealMapper;
import com.atguigu.oline_trading_platform.mapper.ShoppingCartMapper;
import com.atguigu.oline_trading_platform.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = buildQuery(userId, shoppingCartDTO);
        ShoppingCart cart = shoppingCartMapper.selectOne(wrapper);
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateById(cart);
            return;
        }

        ShoppingCart shoppingCart;
        if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
            if (setmeal == null || setmeal.getStatus() == null || setmeal.getStatus() == 0) {
                throw new BusinessException("套餐不存在或已停售");
            }
            shoppingCart = ShoppingCart.builder()
                    .name(setmeal.getName())
                    .image(setmeal.getImage())
                    .userId(userId)
                    .setmealId(setmeal.getId())
                    .number(1)
                    .amount(setmeal.getPrice())
                    .createTime(LocalDateTime.now())
                    .build();
        } else {
            if (shoppingCartDTO.getDishId() == null) {
                throw new BusinessException("请选择菜品或套餐");
            }
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            if (dish == null || dish.getStatus() == null || dish.getStatus() == 0) {
                throw new BusinessException("菜品不存在或已停售");
            }
            shoppingCart = ShoppingCart.builder()
                    .name(dish.getName())
                    .image(dish.getImage())
                    .userId(userId)
                    .dishId(dish.getId())
                    .dishFlavor(shoppingCartDTO.getDishFlavor())
                    .number(1)
                    .amount(dish.getPrice())
                    .createTime(LocalDateTime.now())
                    .build();
        }
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list() {
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .orderByDesc(ShoppingCart::getCreateTime));
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = shoppingCartMapper.selectOne(buildQuery(userId, shoppingCartDTO));
        if (cart == null) {
            throw new BusinessException("购物车中无该商品");
        }
        if (cart.getNumber() > 1) {
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateById(cart);
        } else {
            shoppingCartMapper.deleteById(cart.getId());
        }
    }

    @Override
    public void clean() {
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId()));
    }

    private LambdaQueryWrapper<ShoppingCart> buildQuery(Long userId, ShoppingCartDTO dto) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId);
        if (dto.getSetmealId() != null) {
            wrapper.eq(ShoppingCart::getSetmealId, dto.getSetmealId());
        } else {
            wrapper.eq(ShoppingCart::getDishId, dto.getDishId());
            if (dto.getDishFlavor() != null) {
                wrapper.eq(ShoppingCart::getDishFlavor, dto.getDishFlavor());
            } else {
                wrapper.isNull(ShoppingCart::getDishFlavor);
            }
        }
        return wrapper;
    }
}
