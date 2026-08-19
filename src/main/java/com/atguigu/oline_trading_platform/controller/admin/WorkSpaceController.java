package com.atguigu.oline_trading_platform.controller.admin;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.service.WorkspaceService;
import com.atguigu.oline_trading_platform.vo.BusinessDataVO;
import com.atguigu.oline_trading_platform.vo.DishOverViewVO;
import com.atguigu.oline_trading_platform.vo.OrderOverViewVO;
import com.atguigu.oline_trading_platform.vo.SetmealOverViewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@RequiredArgsConstructor
public class WorkSpaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        LocalDate today = LocalDate.now();
        return Result.success(workspaceService.getBusinessData(
                LocalDateTime.of(today, LocalTime.MIN),
                LocalDateTime.of(today.plusDays(1), LocalTime.MIN)));
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        return Result.success(workspaceService.getOrderOverView());
    }

    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes() {
        return Result.success(workspaceService.getDishOverView());
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        return Result.success(workspaceService.getSetmealOverView());
    }
}
