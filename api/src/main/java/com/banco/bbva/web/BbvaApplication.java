package com.banco.bbva.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EntityScan("com.banco")
@EnableJpaRepositories(basePackages = {"com.banco.repository"})
@ComponentScan(basePackages = {"com.banco"})
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled=true)
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class BbvaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(BbvaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}
}
