package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.dto.OrdersSubmitDTO;
import com.atguigu.oline_trading_platform.entity.AddressBook;
import com.atguigu.oline_trading_platform.entity.OrderDetail;
import com.atguigu.oline_trading_platform.entity.Orders;
import com.atguigu.oline_trading_platform.entity.ShoppingCart;
import com.atguigu.oline_trading_platform.mapper.AddressBookMapper;
import com.atguigu.oline_trading_platform.mapper.OrderDetailMapper;
import com.atguigu.oline_trading_platform.mapper.OrderMapper;
import com.atguigu.oline_trading_platform.mapper.ShoppingCartMapper;
import com.atguigu.oline_trading_platform.service.OrderService;
import com.atguigu.oline_trading_platform.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BusinessException("地址不存在");
        }

        List<ShoppingCart> cartList = shoppingCartMapper.selectList(
                new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
        if (cartList == null || cartList.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(0);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(buildAddress(addressBook));
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserName(addressBook.getConsignee());

        BigDecimal amount = BigDecimal.ZERO;
        for (ShoppingCart cart : cartList) {
            amount = amount.add(cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())));
        }
        if (ordersSubmitDTO.getPackAmount() != null) {
            amount = amount.add(BigDecimal.valueOf(ordersSubmitDTO.getPackAmount()));
        }
        orders.setAmount(amount);
        orderMapper.insert(orders);

        List<OrderDetail> details = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(cart, detail);
            detail.setId(null);
            detail.setOrderId(orders.getId());
            details.add(detail);
            orderDetailMapper.insert(detail);
        }

        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    @Override
    public void payment(Long orderId) {
        Orders orders = orderMapper.selectById(orderId);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        Orders update = Orders.builder()
                .id(orderId)
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(1)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(update);
    }

    private String buildAddress(AddressBook addressBook) {
        StringBuilder sb = new StringBuilder();
        if (addressBook.getProvinceName() != null) {
            sb.append(addressBook.getProvinceName());
        }
        if (addressBook.getCityName() != null) {
            sb.append(addressBook.getCityName());
        }
        if (addressBook.getDistrictName() != null) {
            sb.append(addressBook.getDistrictName());
        }
        if (addressBook.getDetail() != null) {
            sb.append(addressBook.getDetail());
        }
        return sb.toString();
    }
}
