package com.king.eschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EschoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(EschoolApplication.class, args);
		//@Auditable(action = "CREATE_STUDENT", targetEntity = "Student")
	}

}
