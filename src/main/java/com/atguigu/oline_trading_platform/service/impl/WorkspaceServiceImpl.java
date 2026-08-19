package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.entity.Dish;
import com.atguigu.oline_trading_platform.entity.Orders;
import com.atguigu.oline_trading_platform.entity.Setmeal;
import com.atguigu.oline_trading_platform.mapper.DishMapper;
import com.atguigu.oline_trading_platform.mapper.OrderMapper;
import com.atguigu.oline_trading_platform.mapper.SetmealMapper;
import com.atguigu.oline_trading_platform.mapper.UserMapper;
import com.atguigu.oline_trading_platform.service.WorkspaceService;
import com.atguigu.oline_trading_platform.vo.BusinessDataVO;
import com.atguigu.oline_trading_platform.vo.DishOverViewVO;
import com.atguigu.oline_trading_platform.vo.OrderOverViewVO;
import com.atguigu.oline_trading_platform.vo.SetmealOverViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Integer totalOrders = defaultInt(orderMapper.countByTime(begin, end));
        Integer validOrders = defaultInt(orderMapper.countValidByTime(begin, end));
        BigDecimal turnover = orderMapper.sumTurnover(begin, end);
        if (turnover == null) {
            turnover = BigDecimal.ZERO;
        }
        double rate = totalOrders == 0 ? 0D : validOrders * 1.0 / totalOrders;
        BigDecimal unitPrice = validOrders == 0
                ? BigDecimal.ZERO
                : turnover.divide(BigDecimal.valueOf(validOrders), 2, RoundingMode.HALF_UP);
        Integer newUsers = defaultInt(userMapper.countNewUsers(begin, end));
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrders)
                .orderCompletionRate(rate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        return OrderOverViewVO.builder()
                .waitingOrders(countStatus(Orders.TO_BE_CONFIRMED))
                .deliveredOrders(countStatus(Orders.DELIVERY_IN_PROGRESS))
                .completedOrders(countStatus(Orders.COMPLETED))
                .cancelledOrders(countStatus(Orders.CANCELLED))
                .allOrders(countStatus(null))
                .build();
    }

    @Override
    public DishOverViewVO getDishOverView() {
        return DishOverViewVO.builder()
                .sold(countDish(1))
                .discontinued(countDish(0))
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        return SetmealOverViewVO.builder()
                .sold(countSetmeal(1))
                .discontinued(countSetmeal(0))
                .build();
    }

    private Integer countStatus(Integer status) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(status != null, Orders::getStatus, status));
        return count == null ? 0 : count.intValue();
    }

    private Integer countDish(Integer status) {
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, status));
        return count == null ? 0 : count.intValue();
    }

    private Integer countSetmeal(Integer status) {
        Long count = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, status));
        return count == null ? 0 : count.intValue();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
