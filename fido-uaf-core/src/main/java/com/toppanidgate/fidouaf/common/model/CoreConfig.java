package com.toppanidgate.fidouaf.common.model;

import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {

	public static final String svVerNo = "1.1.5";
	public static String sessionId = RandomStringUtils.random(16, false, true);

	@Value("${Allowed-Aaids:5431#3280}")
	private String AllowedAaids;
	public static String allowedAaids;

	@Value("${test_mode:false}")
	private String TestMode;
	public static String testMode;

	@Value("${test_mode_hint:### Test Mode}")
	private String TestModeTitle;

	@PostConstruct
	void init() {
		allowedAaids = AllowedAaids;
		testMode = TestMode;
//		try {
//			Log4j.log.info("#--------------- FIDO-UAF-CORE Initialization -----------------#");
////			Log4j.log.info("AllowedAaids:" + allowedAaids);
////			Log4j.log.info("serverDataTimeout:" + serverDataTimeoutStr);
//			logAPwarn(sessionId, allowedAaids, "init", "AllowedAaids");
//		} catch (UnknownHostException e) {
//			Log4j.log.error("[init] UnknownHostException:{}", e.getMessage());
//		}
//		if ("true".equals(testMode) && StringUtils.isNotBlank(TestModeTitle)) {
////			System.out.println(TestModeTitle + Config.testMode);
//			try {
//				logAPwarn(sessionId, CoreConfig.testMode, "init", TestModeTitle);
//			} catch (UnknownHostException e) {
//				Log4j.log.error("[init] UnknownHostException:{}", e.getMessage());
//			}
//		}
//		Log4j.log.info("#------------------------------------------------#");
	}

	public void logAPwarn(String sessID, String value, String method, String key) throws UnknownHostException {
		Log4j.log.warn("[" + sessID + "][Version: " + CoreConfig.svVerNo + "][" + method + "] " + key + ": " + value);
	}
}
