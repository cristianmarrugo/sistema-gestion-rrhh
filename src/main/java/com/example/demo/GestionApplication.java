package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionApplication.class, args);
	}

//	# Este es el valor real que usará Docker
//	SPRING_AI_OPENAI_API_KEY=
//
//	LOCAL_DB_URL=jdbc:mysql://localhost:3307/rrhh_db?allowPublicKeyRetrieval=true&useSSL=false
//	DB_USERNAME=admin
//			DB_PASSWORD=admin123
//	DB_ROOT_PASSWORD=root123
//
//	PYTHON_EXE_PATH=/usr/bin/python3

}
