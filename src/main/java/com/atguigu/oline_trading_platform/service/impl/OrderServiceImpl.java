package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.OrdersCancelDTO;
import com.atguigu.oline_trading_platform.dto.OrdersPageQueryDTO;
import com.atguigu.oline_trading_platform.dto.OrdersRejectionDTO;
import com.atguigu.oline_trading_platform.dto.OrdersSubmitDTO;
import com.atguigu.oline_trading_platform.entity.AddressBook;
import com.atguigu.oline_trading_platform.entity.OrderDetail;
import com.atguigu.oline_trading_platform.entity.Orders;
import com.atguigu.oline_trading_platform.entity.ShoppingCart;
import com.atguigu.oline_trading_platform.mapper.AddressBookMapper;
import com.atguigu.oline_trading_platform.mapper.OrderDetailMapper;
import com.atguigu.oline_trading_platform.mapper.OrderMapper;
import com.atguigu.oline_trading_platform.mapper.ShoppingCartMapper;
import com.atguigu.oline_trading_platform.mq.OrderTimeoutProducer;
import com.atguigu.oline_trading_platform.service.OrderService;
import com.atguigu.oline_trading_platform.service.ShopService;
import com.atguigu.oline_trading_platform.vo.OrderStatisticsVO;
import com.atguigu.oline_trading_platform.vo.OrderSubmitVO;
import com.atguigu.oline_trading_platform.vo.OrderVO;
import com.atguigu.oline_trading_platform.websocket.WebSocketServer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int PAY_STATUS_UNPAID = 0;
    private static final int PAY_STATUS_PAID = 1;
    private static final int PAY_STATUS_REFUND = 2;

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final WebSocketServer webSocketServer;
    private final ShopService shopService;
    private final OrderTimeoutProducer orderTimeoutProducer;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        if (Integer.valueOf(0).equals(shopService.getStatus())) {
            throw new BusinessException("店铺已打烊，暂无法下单");
        }

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
        orders.setPayStatus(PAY_STATUS_UNPAID);
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

        for (ShoppingCart cart : cartList) {
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(cart, detail);
            detail.setId(null);
            detail.setOrderId(orders.getId());
            orderDetailMapper.insert(detail);
        }

        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
        orderTimeoutProducer.sendDelayCancel(orders.getId());

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    @Override
    public void payment(Long orderId) {
        Orders orders = getOwnedOrder(orderId);
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        Orders update = Orders.builder()
                .id(orderId)
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(PAY_STATUS_PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(update);
        webSocketServer.sendToAll("{\"type\":1,\"typeName\":\"NEW_ORDER\",\"orderId\":"
                + orderId + ",\"content\":\"订单号:" + orders.getNumber() + "\"}");
    }

    @Override
    public void reminder(Long orderId) {
        Orders orders = getOwnedOrder(orderId);
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())
                && !Orders.CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("当前订单状态不能催单");
        }
        webSocketServer.sendToAll("{\"type\":2,\"typeName\":\"REMINDER\",\"orderId\":"
                + orderId + ",\"content\":\"订单号:" + orders.getNumber() + "\"}");
    }

    @Override
    public PageResult page(OrdersPageQueryDTO queryDTO) {
        Page<Orders> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getUserId() != null, Orders::getUserId, queryDTO.getUserId());
        wrapper.like(StringUtils.hasText(queryDTO.getNumber()), Orders::getNumber, queryDTO.getNumber());
        wrapper.like(StringUtils.hasText(queryDTO.getPhone()), Orders::getPhone, queryDTO.getPhone());
        wrapper.eq(queryDTO.getStatus() != null, Orders::getStatus, queryDTO.getStatus());
        wrapper.ge(queryDTO.getBeginTime() != null, Orders::getOrderTime, queryDTO.getBeginTime());
        wrapper.le(queryDTO.getEndTime() != null, Orders::getOrderTime, queryDTO.getEndTime());
        wrapper.orderByDesc(Orders::getOrderTime);
        orderMapper.selectPage(page, wrapper);

        List<OrderVO> records = page.getRecords().stream().map(this::toOrderVO).toList();
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public OrderVO details(Long id) {
        return toOrderVO(getOrder(id));
    }

    @Override
    public OrderVO userDetails(Long id) {
        return toOrderVO(getOwnedOrder(id));
    }

    @Override
    @Transactional
    public void userCancel(Long id) {
        Orders orders = getOwnedOrder(id);
        if (Orders.COMPLETED.equals(orders.getStatus()) || Orders.CANCELLED.equals(orders.getStatus())) {
            throw new BusinessException("当前订单状态不能取消");
        }
        if (Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new BusinessException("派送中的订单请联系商家取消");
        }
        cancelOrder(orders, "用户取消", true);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Orders orders = getOwnedOrder(id);
        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orders.getId()));
        if (details == null || details.isEmpty()) {
            throw new BusinessException("订单明细为空，无法再来一单");
        }
        Long userId = BaseContext.getCurrentId();
        for (OrderDetail detail : details) {
            ShoppingCart cart = ShoppingCart.builder()
                    .name(detail.getName())
                    .image(detail.getImage())
                    .userId(userId)
                    .dishId(detail.getDishId())
                    .setmealId(detail.getSetmealId())
                    .dishFlavor(detail.getDishFlavor())
                    .number(detail.getNumber())
                    .amount(detail.getAmount())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(cart);
        }
    }

    @Override
    public OrderStatisticsVO statistics() {
        return OrderStatisticsVO.builder()
                .toBeConfirmed(countByStatus(Orders.TO_BE_CONFIRMED))
                .confirmed(countByStatus(Orders.CONFIRMED))
                .deliveryInProgress(countByStatus(Orders.DELIVERY_IN_PROGRESS))
                .build();
    }

    @Override
    public void confirm(Long id) {
        Orders orders = getOrder(id);
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("只有待接单订单可以接单");
        }
        orderMapper.updateById(Orders.builder().id(id).status(Orders.CONFIRMED).build());
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO dto) {
        Orders orders = getOrder(dto.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("只有待接单订单可以拒单");
        }
        cancelOrder(orders, StringUtils.hasText(dto.getRejectionReason()) ? dto.getRejectionReason() : "商家拒单", true);
        orderMapper.updateById(Orders.builder()
                .id(orders.getId())
                .rejectionReason(dto.getRejectionReason())
                .build());
    }

    @Override
    @Transactional
    public void adminCancel(OrdersCancelDTO dto) {
        Orders orders = getOrder(dto.getId());
        if (Orders.COMPLETED.equals(orders.getStatus()) || Orders.CANCELLED.equals(orders.getStatus())) {
            throw new BusinessException("当前订单状态不能取消");
        }
        cancelOrder(orders, StringUtils.hasText(dto.getCancelReason()) ? dto.getCancelReason() : "商家取消", true);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = getOrder(id);
        if (!Orders.CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("只有已接单订单可以派送");
        }
        orderMapper.updateById(Orders.builder().id(id).status(Orders.DELIVERY_IN_PROGRESS).build());
    }

    @Override
    public void complete(Long id) {
        Orders orders = getOrder(id);
        if (!Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new BusinessException("只有派送中订单可以完成");
        }
        orderMapper.updateById(Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void cancelIfUnpaid(Long orderId) {
        if (orderId == null) {
            return;
        }
        Orders orders = orderMapper.selectById(orderId);
        if (orders == null || !Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            return;
        }
        cancelOrder(orders, "支付超时，自动取消", false);
    }

    @Override
    public void processTimeoutOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> timeoutOrders = orderMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, expireTime));
        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return;
        }
        for (Orders orders : timeoutOrders) {
            try {
                cancelIfUnpaid(orders.getId());
            } catch (Exception e) {
                log.warn("自动取消超时订单失败, orderId={}: {}", orders.getId(), e.getMessage());
            }
        }
    }

    private void cancelOrder(Orders orders, String reason, boolean refundIfPaid) {
        Orders update = Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .cancelReason(reason)
                .cancelTime(LocalDateTime.now())
                .build();
        if (refundIfPaid && Integer.valueOf(PAY_STATUS_PAID).equals(orders.getPayStatus())) {
            update.setPayStatus(PAY_STATUS_REFUND);
        }
        orderMapper.updateById(update);
    }

    private OrderVO toOrderVO(Orders orders) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders, vo);
        vo.setOrderDetailList(orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orders.getId())));
        return vo;
    }

    private Integer countByStatus(Integer status) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, status));
        return count == null ? 0 : count.intValue();
    }

    private Orders getOwnedOrder(Long orderId) {
        Orders orders = orderMapper.selectById(orderId);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        return orders;
    }

    private Orders getOrder(Long orderId) {
        Orders orders = orderMapper.selectById(orderId);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        return orders;
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
