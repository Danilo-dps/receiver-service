package br.com.danilodps.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReceiverServiceApplication {

	private ReceiverServiceApplication(){}

	static void main(String[] args) {
		SpringApplication.run(ReceiverServiceApplication.class, args);
	}

}
