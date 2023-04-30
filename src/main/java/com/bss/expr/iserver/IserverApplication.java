package com.bss.expr.iserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@PropertySource("classpath:application.properties")
@EnableAsync
@Slf4j
public class IserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(IserverApplication.class, args);
	}

}