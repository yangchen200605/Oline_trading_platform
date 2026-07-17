package com.atguigu.oline_trading_platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.atguigu.oline_trading_platform.mapper")
public class OlineTradingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(OlineTradingPlatformApplication.class, args);
	}

}
