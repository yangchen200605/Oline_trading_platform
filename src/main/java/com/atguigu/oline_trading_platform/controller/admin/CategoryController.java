package com.atguigu.oline_trading_platform.controller.admin;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.CategoryDTO;
import com.atguigu.oline_trading_platform.dto.CategoryPageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Category;
import com.atguigu.oline_trading_platform.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<Void> save(@RequestBody CategoryDTO categoryDTO) {
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        return Result.success(categoryService.page(categoryPageQueryDTO));
    }

    @PostMapping("/status/{status}")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam Long id) {
        categoryService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.list(type));
    }
}
