package com.bondhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BondHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(BondHubApplication.class, args);
	}

}
