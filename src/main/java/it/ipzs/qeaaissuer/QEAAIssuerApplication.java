package it.ipzs.qeaaissuer;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = { @Server(url = "/", description = "Default Server URL") })

@SpringBootApplication
public class QEAAIssuerApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(QEAAIssuerApplication.class);
		springApplication.addListeners(new ApplicationPidFileWriter());
		Security.addProvider(new BouncyCastleProvider());
		springApplication.run(args);
	}

}
