package com.toppanidgate.fidouaf.res.config;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.toppanidgate.fidouaf.Log4j;
import com.toppanidgate.fidouaf.common.model.APLogFormat;
import com.toppanidgate.fidouaf.common.model.Log4jAP;

@Configuration
public class Config {
	
	public static final String svVerNo = "1.1.5";
	
	@Value("${AllowedAaids:5431#3280}")
	private String AllowedAaids;
	public static String allowedAaids;
	
	@Value("${TestMode:false}")
	private String TestMode;
	public static String testMode;
	
	@Value("${TestModeHint:}")
	private String TestModeTitle;
	
	@PostConstruct
	void init() {
		allowedAaids = AllowedAaids;
		testMode = TestMode;
		if ("true".equals(testMode) && StringUtils.isNotBlank(TestModeTitle)) {
			System.out.println(TestModeTitle + Config.testMode);
			try {
				logAPwarn("session", Config.testMode, "init", TestModeTitle);
			} catch (UnknownHostException e) {
				Log4j.log.error("[init] UnknownHostException:{}", e.getMessage());
			}
		}
	}
	
	public void logAPwarn(String sessID, String value, String method, String key) throws UnknownHostException {
		Log4j.log.warn("[" + sessID + "][Version: " + Config.svVerNo + "][" + method + "] " + key + ": " + value
				);
		APLogFormat apLogObj = new APLogFormat();
		Map<String, Object> apLogMap = new HashMap<>();
		apLogMap.put("sessID", sessID);
		apLogMap.put("version", svVerNo);
		apLogMap.put("method", method);
		apLogMap.put("message", key + ": " + value);
//		apLogMap.put("stacktrace", e.getStackTrace());
		apLogObj.setMessage(new Gson().toJson(apLogMap));
		Log4jAP.log.warn(apLogObj.getCompleteTxt());
	}
}
