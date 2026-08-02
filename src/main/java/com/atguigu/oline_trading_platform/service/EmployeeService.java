package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.EmployeeDTO;
import com.atguigu.oline_trading_platform.dto.EmployeeLoginDTO;
import com.atguigu.oline_trading_platform.dto.EmployeePageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Employee;
import com.atguigu.oline_trading_platform.vo.EmployeeLoginVO;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);
}
