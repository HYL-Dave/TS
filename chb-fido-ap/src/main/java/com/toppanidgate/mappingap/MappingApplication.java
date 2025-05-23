package com.toppanidgate.mappingap;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Common.model.BaseServlet;
import com.toppanidgate.fidouaf.common.model.Log4j;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@RestController
@RefreshScope
@OpenAPIDefinition(info = @Info(title = "iDenKey", version = BaseServlet.svVerNo, description = "iDenKey Information"))
public class MappingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MappingApplication.class, args);
	}
	
	@Value("${config.test_mode:false}")
	private String TestMode;
	public static String testMode;	// 無作用
	
	@Value("${config.channel:SP}")
	private String Channel;
	public static String channel;
	
	@Value("${config.msg:default value}")
	private String msgString;

	@Value("${info.app.name:default value}")
	private String appName;
	
	@Hidden
	@GetMapping("/hello")
//	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
	public String hello() {
		return String.format("Hello %s! msg:%s channel: %s testMode: %s @%s", appName, msgString, channel, testMode, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
	}
	
	@Hidden
	@GetMapping("/hello2")
	public String sayHello() {
		return "Hello ComFidoApApp @" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
	}
	
	@Hidden
	@GetMapping(value = "/")
	public String home() {
		return "Mapping iDenKey application @" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
	}
	
	@Hidden
	@GetMapping("/channel")
	public String getChannel() {
		return String.format("channel: %s!", Channel);
	}

	@Hidden
	@GetMapping("/testmode")
	public String getTestMode() {
		return String.format("testMode: %s!", TestMode);
	}
	
	@PostConstruct
	void init() {
		channel = Channel;
		testMode = TestMode;
		Log4j.log.warn("*** init done. *** \n testMode:{} \n channel:{}", testMode, channel);
	}
}
