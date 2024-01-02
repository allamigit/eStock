package com.estock;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class eStockApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(eStockApplication.class, args);
		
        System.out.println("\n ... eStockApplication API started ...\n");
	}

	@Override
	public void run(String... args) {
		
	}

}
