package com.toppanidgate.idenkey.Config;

import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.Cvn.Encryptor.Encode;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.idenkey.common.model.APLogFormat;

@Configuration
@EntityScan("com.toppanidgate.fidouaf.model")
@EnableJpaRepositories("com.toppanidgate.fidouaf.DAO")
@ComponentScan(basePackages = { "com.Cvn"
		, "com.toppanidgate.fidouaf"
//		, "com.toppanidgate.fidouaf.DAO"
//		, "com.toppanidgate.fidouaf.service"
		, "com.toppanidgate.fido.uaf"
//		, "com.toppanidgate.fido.uaf.storage"
		, "com.HSM.Esuncorp"
		, "com.toppanidgate.WSM.controller" })
public class IDGateConfig {
	public static final String svVerNo = "2.2.2";
	// RandomStringUtils.random
	static String sessionId;
	
	static {
		SecureRandom random = new SecureRandom();  
		byte[] bytes = new byte[8];
		random.nextBytes(bytes);
		IDGateConfig.sessionId = Encode.byteToHex(bytes);
	}

	@Value("${test_mode:false}")
	private String TestMode;
	public static String testMode;
	
	@Value("${test_mode_hint:}")
	private String TestModeTitle;
	
	@Value("${log4j_file_path}")
	private String log4j_file_path;
	
	@Value("${database_type:MSSQL}")
	private String DataBaseType;
	public static String dataBaseType;
//	
//	@Value("${OnlyFIDO:false}")
//	private String OnlyFIDO;
//	public static String onlyFIDO;
	
	@PostConstruct
	void init() {
		testMode = TestMode;
		dataBaseType = DataBaseType;
//		onlyOTP = OnlyOTP;
//		onlyFIDO = OnlyFIDO;
//			new Log4j(log4j_file_path);
//			new Log4jAP(log4j_file_path);
//			new Log4jInbound(log4j_file_path);
//			new Log4jSQL(log4j_file_path);
//		Log4j.log.info("========================== Config iDGate ==========================");
//		try {
//		} catch (IOException e) {
//			Log4j.log.error(e.getMessage());
//		}
		try {
			logAPwarn(sessionId, IDGateConfig.dataBaseType, "init", "DataBaseType");
		} catch (UnknownHostException e) {
			Log4j.log.error("[init] UnknownHostException:{}", e.getMessage());
		}
		if ("true".equals(testMode) && StringUtils.isNotBlank(TestModeTitle)) {
//			Log4j.log.info(TestModeTitle + IDGateConfig.testMode);
//			System.out.println(TestModeTitle + IDGateConfig.testMode);
			try {
				logAPwarn(sessionId, IDGateConfig.testMode, "init", TestModeTitle);
			} catch (UnknownHostException e) {
				Log4j.log.error("[init] UnknownHostException:{}", e.getMessage());
			}
		}
	}
	
	
	public void logAPwarn(String sessID, String value, String method, String key) throws UnknownHostException {
		Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][" + method + "] " + key + ": " + value
				);
		APLogFormat apLogObj = new APLogFormat();
		Map<String, Object> apLogMap = new HashMap<>();
		apLogMap.put("sessID", sessID);
		apLogMap.put("version", svVerNo);
		apLogMap.put("method", method);
		apLogMap.put("message", key + ": " + value);
//		apLogMap.put("stacktrace", e.getStackTrace());
		apLogObj.setMessageForMap(apLogMap);
		Log4jAP.log.warn(apLogObj.getCompleteTxt());
	}

//	@Bean
//	public TomcatServletWebServerFactory tomcatFactory(@Value("${config_file_path}") String configFilePath,
//			@Value("${resource1.name}") String rs1Name,
//			@Value("${resource1.url}") String rs1URL,
//			@Value("${resource1.driver_classname}") String rs1Driver,
//			@Value("${resource1.username}") String rs1Username,
//			@Value("${resource1.password}") String rs1PWD,
//			@Value("${resource1.max_active}") String rs1MaxActive,
//			@Value("${resource1.max_idle}") String rs1MaxIdle,
//			@Value("${resource1.max_wait}") String rs1MaxWait,
//			@Value("${resource1.initial_size}") String rs1InitialSize,
//			@Value("${resource2.name}") String rs2Name,
//			@Value("${resource2.url}") String rs2URL,
//			@Value("${resource2.driver_classname}") String rs2Driver,
//			@Value("${resource2.username}") String rs2Username,
//			@Value("${resource2.password}") String rs2PWD,
//			@Value("${resource2.max_active}") String rs2MaxActive,
//			@Value("${resource2.max_idle}") String rs2MaxIdle,
//			@Value("${resource2.max_wait}") String rs2MaxWait,
//			@Value("${resource2.initial_size}") String rs2InitialSize
////			, @Value("${resource3.name}") String rs3Name,
////			@Value("${resource3.url}") String rs3URL,
////			@Value("${resource3.driver_classname}") String rs3Driver,
////			@Value("${resource3.username}") String rs3Username,
////			@Value("${resource3.password}") String rs3PWD,
////			@Value("${resource3.max_active}") String rs3MaxActive,
////			@Value("${resource3.max_idle}") String rs3MaxIdle,
////			@Value("${resource3.initial_size}") String rs3InitialSize,
////			@Value("${resource3.max_wait}") String rs3MaxWait 
//			) {
//		return new TomcatServletWebServerFactory() {
//			@Override
//			protected TomcatWebServer getTomcatWebServer(org.apache.catalina.startup.Tomcat tomcat) {
//				tomcat.enableNaming();
//				return super.getTomcatWebServer(tomcat);
//			}
//
//			@Override
//			protected void postProcessContext(Context context) {
//
//				// context
//				ContextResource resource1 = new ContextResource();
//				resource1.setName(rs1Name);
//				resource1.setType(DataSource.class.getName());
//				resource1.setProperty("driverClassName", rs1Driver);
//				resource1.setProperty("url", rs1URL);
//				try {
//					resource1.setProperty("username", EncryptHelper.Decrypt(rs1Username));
//					resource1.setProperty("password", EncryptHelper.Decrypt(rs1PWD));
//				} catch (Exception e) {
//					Log4j.log.error(e.getMessage());
//				}
//				resource1.setProperty("maxTotal", rs1MaxActive);
//				resource1.setProperty("maxIdle", rs1MaxIdle);
//				resource1.setProperty("maxWaitMillis", rs1MaxWait);
//				resource1.setProperty("initialSize", rs1InitialSize);
//				context.getNamingResources().addResource(resource1);
//				// context
//				ContextResource resource2 = new ContextResource();
//				resource2.setName(rs2Name);
//				resource2.setType(DataSource.class.getName());
//				resource2.setProperty("driverClassName", rs2Driver);
//				resource2.setProperty("url", rs2URL);
//				try {
//					resource2.setProperty("username", EncryptHelper.Decrypt(rs2Username));
//					resource2.setProperty("password", EncryptHelper.Decrypt(rs2PWD));
//				} catch (Exception e) {
//					Log4j.log.error(e.getMessage());
//				}
//				resource2.setProperty("maxTotal", rs2MaxActive);
//				resource2.setProperty("maxIdle", rs2MaxIdle);
//				resource2.setProperty("maxWaitMillis", rs2MaxWait);
//				resource2.setProperty("initialSize", rs2InitialSize);
//				context.getNamingResources().addResource(resource2);
//				// context
////				ContextResource resource3 = new ContextResource();
////				resource3.setName(rs3Name);
////				resource3.setType(DataSource.class.getName());
////				resource3.setProperty("driverClassName", rs3Driver);
////				resource3.setProperty("url", rs3URL);
//////				resource3.setProperty("driverClassName", "com.mysql.jdbc.Driver");
//////				resource3.setProperty("url", "jdbc:mysql://localhost:3306/trustdb");
////				// EXECUTE || SELECT || UPDATE || INSERT
////				resource3.setProperty("username", rs3Username);
////				resource3.setProperty("password", rs3PWD);
////				resource3.setProperty("maxTotal", rs3MaxActive);
////				resource3.setProperty("maxIdle", rs3MaxIdle);
////				resource3.setProperty("maxWaitMillis", rs3MaxWait);
//////				resource3.setProperty("initialSize", rs3InitialSize);
////				context.getNamingResources().addResource(resource3);
//
//				ContextEnvironment ce = new ContextEnvironment();
//				ce.setName("ExCfgPath");
//				ce.setType(String.class.getName());
//				ce.setValue(configFilePath);
//				context.getNamingResources().addEnvironment(ce);
//				
//			}
//		};
//	}
}
