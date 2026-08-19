package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.vo.BusinessDataVO;
import com.atguigu.oline_trading_platform.vo.DishOverViewVO;
import com.atguigu.oline_trading_platform.vo.OrderOverViewVO;
import com.atguigu.oline_trading_platform.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {

    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    OrderOverViewVO getOrderOverView();

    DishOverViewVO getDishOverView();

    SetmealOverViewVO getSetmealOverView();
}
