package com.tripnest.tripnest_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TripnestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripnestBackendApplication.class, args);
		System.out.println("====================================");
        System.out.println("TripNest Backend Started Successfully!");
        System.out.println("Database Connected Successfully!");
        System.out.println("Server Running at: http://localhost:8080");
        System.out.println("====================================");


		
	}

}
