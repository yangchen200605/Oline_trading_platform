package com.atguigu.oline_trading_platform.controller.user;

import com.atguigu.oline_trading_platform.common.Result;
import com.atguigu.oline_trading_platform.dto.UserLoginDTO;
import com.atguigu.oline_trading_platform.service.UserService;
import com.atguigu.oline_trading_platform.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(userService.login(userLoginDTO));
    }
}
