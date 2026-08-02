package com.atguigu.oline_trading_platform.controller.admin;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.EmployeeDTO;
import com.atguigu.oline_trading_platform.dto.EmployeeLoginDTO;
import com.atguigu.oline_trading_platform.dto.EmployeePageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Employee;
import com.atguigu.oline_trading_platform.service.EmployeeService;
import com.atguigu.oline_trading_platform.vo.EmployeeLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        return Result.success(employeeService.login(employeeLoginDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @PostMapping
    public Result<Void> save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        return Result.success(employeeService.page(employeePageQueryDTO));
    }

    @PostMapping("/status/{status}")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    public Result<Void> update(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
