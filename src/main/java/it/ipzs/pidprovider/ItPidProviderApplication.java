package it.ipzs.pidprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class ItPidProviderApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(ItPidProviderApplication.class);
		springApplication.addListeners(new ApplicationPidFileWriter());
		springApplication.run(args);
	}

}
