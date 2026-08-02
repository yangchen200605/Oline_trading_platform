package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.JwtUtil;
import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.JwtProperties;
import com.atguigu.oline_trading_platform.common.properties.PageResult;
import com.atguigu.oline_trading_platform.dto.EmployeeDTO;
import com.atguigu.oline_trading_platform.dto.EmployeeLoginDTO;
import com.atguigu.oline_trading_platform.dto.EmployeePageQueryDTO;
import com.atguigu.oline_trading_platform.entity.Employee;
import com.atguigu.oline_trading_platform.mapper.EmployeeMapper;
import com.atguigu.oline_trading_platform.service.EmployeeService;
import com.atguigu.oline_trading_platform.vo.EmployeeLoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final JwtProperties jwtProperties;

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        Employee employee = employeeMapper.selectOne(
                new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, username));

        if (employee == null) {
            throw new BusinessException("账号不存在");
        }

        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(employee.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (employee.getStatus() != null && employee.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("empId", employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        return EmployeeLoginVO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Long count = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, employeeDTO.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("账号已存在");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        employee.setStatus(1);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        Page<Employee> page = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(employeePageQueryDTO.getName()),
                Employee::getName, employeePageQueryDTO.getName());
        wrapper.orderByDesc(Employee::getCreateTime);

        employeeMapper.selectPage(page, wrapper);
        page.getRecords().forEach(employee -> employee.setPassword(null));

        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.updateById(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        employee.setPassword(null);
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateById(employee);
    }
}
