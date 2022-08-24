package com.example.fcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
		scanBasePackages = {
				"com.example.fcm",
				"com.example.rabbitmq"
		}
)
@EnableEurekaClient
@EnableFeignClients(
		basePackages = "com.example.clients")
public class FcmApplication {

	public static void main(String[] args) {
		SpringApplication.run(FcmApplication.class, args);
	}

}
