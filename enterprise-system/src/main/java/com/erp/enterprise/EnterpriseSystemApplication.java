package com.erp.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  // this annotation to enable automatic timestamp management
public class EnterpriseSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnterpriseSystemApplication.class, args);
	}

}
