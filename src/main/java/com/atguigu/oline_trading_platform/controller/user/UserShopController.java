package com.atguigu.oline_trading_platform.controller.user;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.entity.Category;
import com.atguigu.oline_trading_platform.service.CategoryService;
import com.atguigu.oline_trading_platform.service.DishService;
import com.atguigu.oline_trading_platform.service.SetmealService;
import com.atguigu.oline_trading_platform.vo.DishVO;
import com.atguigu.oline_trading_platform.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserShopController {

    private final CategoryService categoryService;
    private final DishService dishService;
    private final SetmealService setmealService;

    @GetMapping("/category/list")
    public Result<List<Category>> categoryList(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.list(type));
    }

    @GetMapping("/dish/list")
    public Result<List<DishVO>> dishList(@RequestParam Long categoryId) {
        return Result.success(dishService.listByCategoryId(categoryId));
    }

    @GetMapping("/setmeal/list")
    public Result<List<SetmealVO>> setmealList(@RequestParam Long categoryId) {
        return Result.success(setmealService.listByCategoryId(categoryId));
    }
}
