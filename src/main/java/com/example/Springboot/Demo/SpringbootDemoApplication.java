package com.example.Springboot.Demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SpringbootDemoApplication {

	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		SpringbootDemoApplication springbootDemoApplication = new SpringbootDemoApplication();
		SpringApplication.run(SpringbootDemoApplication.class, args);

		System.out.println("Started main()...");

	}




}
