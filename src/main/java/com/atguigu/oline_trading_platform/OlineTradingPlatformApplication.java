package com.atguigu.oline_trading_platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.atguigu.oline_trading_platform.mapper")
@EnableTransactionManagement
@EnableScheduling
@EnableRabbit
public class OlineTradingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(OlineTradingPlatformApplication.class, args);
	}

}
