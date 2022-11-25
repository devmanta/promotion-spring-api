package com.dndn.promotions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
//public class PromotionsApplication extends SpringBootServletInitializer {
public class PromotionsApplication {

//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(PromotionsApplication.class);
//	}

	public static void main(String[] args) {
		SpringApplication.run(PromotionsApplication.class, args);
	}

}
