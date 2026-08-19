package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    public static final String SHOP_STATUS_KEY = "shop:status";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void setStatus(Integer status) {
        stringRedisTemplate.opsForValue().set(SHOP_STATUS_KEY, String.valueOf(status));
    }

    @Override
    public Integer getStatus() {
        String value = stringRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        if (!StringUtils.hasText(value)) {
            return 1;
        }
        return Integer.parseInt(value);
    }
}
