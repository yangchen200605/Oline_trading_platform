package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.dto.UserLoginDTO;
import com.atguigu.oline_trading_platform.vo.UserLoginVO;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);
}
