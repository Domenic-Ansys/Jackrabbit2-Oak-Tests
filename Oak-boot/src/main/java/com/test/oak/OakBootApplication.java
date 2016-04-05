package com.test.oak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OakBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(OakBootApplication.class, args);
		OakTest.executeTests();
	}
}
