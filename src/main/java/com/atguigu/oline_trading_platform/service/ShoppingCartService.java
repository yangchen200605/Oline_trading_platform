package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.dto.ShoppingCartDTO;
import com.atguigu.oline_trading_platform.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> list();

    void sub(ShoppingCartDTO shoppingCartDTO);

    void clean();
}
