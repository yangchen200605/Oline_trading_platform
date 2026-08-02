package com.atguigu.oline_trading_platform.controller.user;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.dto.OrdersSubmitDTO;
import com.atguigu.oline_trading_platform.service.OrderService;
import com.atguigu.oline_trading_platform.vo.OrderSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        return Result.success(orderService.submit(ordersSubmitDTO));
    }

    @PutMapping("/payment/{id}")
    public Result<Void> payment(@PathVariable Long id) {
        orderService.payment(id);
        return Result.success();
    }
}
