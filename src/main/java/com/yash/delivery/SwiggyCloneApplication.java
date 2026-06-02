package com.yash.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SwiggyCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwiggyCloneApplication.class, args);
	}

}
