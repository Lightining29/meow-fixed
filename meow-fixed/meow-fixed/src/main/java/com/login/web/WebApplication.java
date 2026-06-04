package com.login.web;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/*
╔════════════════════════════════════════════════╗
║                MAIN APPLICATION                ║
╚════════════════════════════════════════════════╝
*/

 
@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

}
