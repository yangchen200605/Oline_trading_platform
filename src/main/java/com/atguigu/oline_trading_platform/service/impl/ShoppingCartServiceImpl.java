package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.dto.ShoppingCartDTO;
import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.entity.ShoppingCart;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
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

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId());
        if (shoppingCartDTO.getDishFlavor() != null) {
            wrapper.eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
        } else {
            wrapper.isNull(ShoppingCart::getDishFlavor);
        }

        ShoppingCart cart = shoppingCartMapper.selectOne(wrapper);
        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateById(cart);
            return;
        }

        Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
        if (dish == null || dish.getStatus() == null || dish.getStatus() == 0) {
            throw new BusinessException("菜品不存在或已停售");
        }

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .name(dish.getName())
                .image(dish.getImage())
                .userId(userId)
                .dishId(dish.getId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .number(1)
                .amount(dish.getPrice())
                .createTime(LocalDateTime.now())
                .build();
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
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId());
        if (shoppingCartDTO.getDishFlavor() != null) {
            wrapper.eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
        } else {
            wrapper.isNull(ShoppingCart::getDishFlavor);
        }

        ShoppingCart cart = shoppingCartMapper.selectOne(wrapper);
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
}
