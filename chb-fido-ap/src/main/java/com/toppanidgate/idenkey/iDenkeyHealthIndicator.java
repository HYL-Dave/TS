package com.toppanidgate.idenkey;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Cvn.Config.Cfg;
import com.Cvn.Encryptor.Encode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.WSM.controller.WSMServlet;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.mappingap.MappingApplication;

import io.swagger.v3.oas.annotations.Hidden;

@RefreshScope
@RestController
public class iDenkeyHealthIndicator implements HealthIndicator {

	@Autowired
	WSMServlet wsmServlet;
	
//	@Value("${channel: SP}")
//	private String channel;
	
	private final String message_key = "HealthCheck";
	String retMsg = null;
	
	@Hidden
	@Override
	@PostMapping("/testHealthCheck")
	public Health health() {
		if (!isRunningHealthCheck()) {
			return Health.down()
					.withDetail(message_key, "Not Available")
					.withDetail("DB", "Not Available")
					.withDetail("HSM", "Not Available")
					.build();
		}

//		Map<String, String> mapResponse = new HashMap<>();
//		mapResponse.put("serverPubKey",
//				"30820122300D06092A864886F70D01010105000382010F003082010A0282010100B7081FDF43D316D8FACFC1CB0A8492DE4EAF933C10972C5C4BF00FB4BC294185A81336BC23ECED399CE533FE4A30CD068A73AF6DBFCC65B1F4FA0BC1E9E9A0FE14C1EF51F314AE472A80FFD718D011C56EDE2A147F5E25D248803A7E934871EF0E29747214BD1DB03D4256991B856371B22761A95E84D0CD2349BB9FD653B8297F3720D55CAA3F81C91C01B8D3B9AECE93E7B758CD918162C2A2479BC382278315F6DD8DD87873714D0DEC2781D984C5FF07B3E56BBD0A9584E537214DCE2B2B4C1AE8E322E3B02CC024A00B434D9697A907BB737E5D3A2654DC827E2C24FDA5F2342A9DC7A62E95C429101E05D526FD5FC773B7A90DC89C6013335DC45B2B9B0203010001");
//		mapResponse.put("serverECCPubKey", "1682322184355");
//		mapResponse.put("serverTime", "1682322184355");
//		mapResponse.put("returnCode", "0000");
//		mapResponse.put("returnMsg", "Success");
		
		Map<String, String> dataSet = new Gson().fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
		}.getType());

		Map<String, Object> details = new HashMap<>();
		details.put("response", dataSet);
		details.put("channel", MappingApplication.channel);
//		details.put("strategy", "thread-local");

		return Health.up()
				.withDetail(message_key, "Available")
				.withDetail("DB", "Available")
				.withDetail("HSM", "Available")
				.withDetails(details)
				.build();
	}

	private Boolean isRunningHealthCheck() {
		Boolean isRunning = true;

		// Check with FIDO server to see if it is ok
		HashMap<String, String> formparams = new HashMap<String, String>();
		formparams.put("method", "svfHealthCheck");
		formparams.put("channel", MappingApplication.channel);
//		Locale local = new Locale("en-US");
		Locale local = new Locale("zh-TW");
		
		SecureRandom random = new SecureRandom(); 
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		
		try {
			retMsg = wsmServlet.doPost(Cfg.getExternalCfgValue("WSI_API_Key"), local, new Gson().toJson(formparams), Encode.byteToHex(bytes));
		} catch (IOException e) {
			isRunning = false;
		}
		// showHealthCheckHeartBeat=true
		Log4j.log.debug("*** Health Check isRunning:{}", isRunning);
		return isRunning;
	}
}