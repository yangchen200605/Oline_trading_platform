package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.JwtUtil;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.common.properties.JwtProperties;
import com.atguigu.oline_trading_platform.dto.UserLoginDTO;
import com.atguigu.oline_trading_platform.entity.User;
import com.atguigu.oline_trading_platform.mapper.UserMapper;
import com.atguigu.oline_trading_platform.service.UserService;
import com.atguigu.oline_trading_platform.vo.UserLoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || !StringUtils.hasText(userLoginDTO.getOpenid())) {
            throw new BusinessException("openid 不能为空");
        }
        String openid = userLoginDTO.getOpenid();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .name("用户" + openid.substring(Math.max(0, openid.length() - 4)))
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
    }
}
