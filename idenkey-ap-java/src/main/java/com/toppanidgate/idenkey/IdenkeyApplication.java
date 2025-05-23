package com.toppanidgate.idenkey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.toppanidgate.idenkey.Config.IDGateConfig;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "RP API", version = IDGateConfig.svVerNo, description = "RP Information"))
public class IdenkeyApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdenkeyApplication.class, args);
	}
}
