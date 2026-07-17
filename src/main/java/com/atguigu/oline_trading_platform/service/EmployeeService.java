package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.dto.EmployeeLoginDTO;
import com.atguigu.oline_trading_platform.vo.EmployeeLoginVO;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);
}
