package com.example.project;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx){
		return args -> {
			System.out.println("lets check the beans springboot provides");
			String[] beans = ctx.getBeanDefinitionNames();
			System.out.println(Arrays.toString(beans));
		};
	}

	@Bean
	public CommandLineRunner printSum(){
		return args -> {
			int x = 25;
			int sum = 0;
			for(int i = 1; i <= x; i++){
				sum += i;
			}
			System.out.println("Total is "+sum);
		};
	}

}
