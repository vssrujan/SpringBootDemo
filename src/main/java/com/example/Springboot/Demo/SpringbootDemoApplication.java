package com.example.Springboot.Demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@PropertySource("classpath:data.txt")
@SpringBootApplication
public class SpringbootDemoApplication {


	public static void main(String[] args) {
		SpringbootDemoApplication springbootDemoApplication = new SpringbootDemoApplication();
		SpringApplication.run(SpringbootDemoApplication.class, args);
		System.out.println("Started main()...");
		String environment1 = getEnvironment(System.getProperty("spring.profiles.active"));
		System.out.println(environment1);
		System.out.println();

	}

	public static String getEnvironment(String activeProfiles){
		String env = "";
		if(activeProfiles.toLowerCase().contains("dev")){
			env="dev";
		}
		else env="local";
		return  env;
	}

	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("Demo-");
		executor.initialize();
		return executor;

	}

}
