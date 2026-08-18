package com.king.eschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 🟢 Active l'exécution des tâches planifiées (CRON)
@EnableAsync // 🟢 ACTIVER L'EXECUTION DES METHODES @ASYNC
public class EschoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(EschoolApplication.class, args);
		//@Auditable(action = "CREATE_STUDENT", targetEntity = "Student")
	}

}
