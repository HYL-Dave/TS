package com.toppanidgate.mappingap.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.Common.model.BaseServlet;
import com.Common.model.InboundLogFormat;
import com.WSI.model.Log4jWSI;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.common.model.APLogFormat;

import io.swagger.v3.oas.models.parameters.Parameter;

//@ImportResource("web.xml")
@Configuration
@EntityScan("com.toppanidgate.fidouaf.model")
@EnableJpaRepositories("com.toppanidgate.fidouaf.DAO")
@ComponentScan(basePackages = { "com"
		, "com.toppanidgate.fidouaf"
//		, "com.toppanidgate.fidouaf.DAO"
//		, "com.toppanidgate.fidouaf.service"
		, "com.toppanidgate.fido.uaf"
//		, "com.toppanidgate.fido.uaf.storage"
		, "com.WSM.controller"
		, "com.WSI.controller" })
public class MappingConfig {
	public static String sessionId = RandomStringUtils.random(16, false, true);
	
	// Common resource variables
	@Value("${config_file_path}")
	private String Config_file_path;
	
	@Value("${log4j_file_path}")
	protected String Log4j_file_path;

	private boolean gotDefault;
	
	private InboundLogFormat inLogObj = null;
	private APLogFormat apLogObj = null;
	
	@Value("${database_type:MSSQL}")
	private String DataBaseType;
	
	@SuppressWarnings("unused")
	@PostConstruct
	public
	void init() throws UnknownHostException {
		try {
			new Log4j(Log4j_file_path);
			new Log4jWSI(Log4j_file_path);
			new Log4jAP(Log4j_file_path);
			new Log4jInbound(Log4j_file_path);
			new Log4jSQL(Log4j_file_path);
		} catch (IOException e) {
			Log4j.log.error(e);
		}
		Log4j.log.info("========================== Mapping Setting ==========================");
//		Log4j.log.info("*** Config_file_path: {}", Config_file_path);
//		Log4j.log.info("*** Log4j_file_path: {}", Log4j_file_path);
		try {
			logAPwarn(sessionId, Config_file_path, "init", "Config_file_path");
			logAPwarn(sessionId, Log4j_file_path, "init", "Log4j_file_path");
			logAPwarn(sessionId, DataBaseType, "init", "DataBaseType");
		} catch (UnknownHostException e) {
			Log4j.log.error("[init] UnknownHostException:{}", e.getMessage());

		}
		
		inLogObj = new InboundLogFormat();
		apLogObj = new APLogFormat();
		inLogObj.setTraceID(sessionId);
		inLogObj.setClazz("com.toppanidgate.mappingap.config.MappingConfig");
		apLogObj.setTraceID(sessionId);
		apLogObj.setClazz("com.toppanidgate.mappingap.config.MappingConfig");
		
		long startTime = System.currentTimeMillis();
		File exCfgFile = new File(Config_file_path);
		Properties properties = null;
		if (gotDefault == false) {
			try (InputStream is = new FileInputStream(exCfgFile);) {

				// setup general config
				properties = new Properties();
				properties.load(is);

				String otpTimeout = properties.getProperty("OTP_Timeout");
				String txnTimeout = properties.getProperty("Txn_Timeout");
				String loginTxnTimeout = properties.getProperty("LoginTxn_Timeout");
				String digital_fail_limit = properties.getProperty("Digital_Fail_Limit");
				String pattern_fail_limit = properties.getProperty("Pattern_Fail_Limit");
				String otp_fail_limit = properties.getProperty("OTP_Fail_Limit");
				
//				String FIDO_SV_IP = properties.getProperty("FIDO_SV_IP");
				
				validateKey(properties.getProperty("PushServer_URL"), "PushServer_URL");
				validateKey(properties.getProperty("i18n_Name"), "i18n_Name");
				validateKey(properties.getProperty("RsaKeyAlias"), "RsaKeyAlias");
				validateKey(properties.getProperty("ECCkeyAlias"), "ECCkeyAlias");
				validateKey(properties.getProperty("SoftRSA"), "SoftRSA");
				validateKey(properties.getProperty("SoftECC"), "SoftECC");
				validateKey(properties.getProperty("JNDI_Name"), "JNDI_Name");
				validateKey(otpTimeout, "OTP_Timeout");
				validateKey(txnTimeout, "Txn_Timeout");
				validateKey(loginTxnTimeout, "LoginTxn_Timeout");
				validateKey(properties.getProperty("WSM_API_Key"), "WSM_API_Key");
				validateKey(properties.getProperty("WSI_API_Key"), "WSI_API_Key");
				validateKey(digital_fail_limit, "Digital_Fail_Limit");
				validateKey(pattern_fail_limit, "Pattern_Fail_Limit");
				validateKey(otp_fail_limit, "OTP_Fail_Limit");
				validateKey(properties.getProperty("DBdirect"), "DBdirect");
				validateKey(properties.getProperty("HSMPlusMode"), "HSMPlusMode");
				validateKey(properties.getProperty("HSMProvider"), "HSMProvider");
				validateKey(properties.getProperty("HSM_SLOT"), "HSM_SLOT");
				validateKey(properties.getProperty("HSM_PXD"), "HSM_PXD");
				validateKey(properties.getProperty("PersoKeyAlias"), "PersoKeyAlias");
				validateKey(properties.getProperty("DbKeyAlias"), "DbKeyAlias");
				validateKey(properties.getProperty("SetKeyAlias"), "SetKeyAlias");
				validateKey(properties.getProperty("MultiPersoKey"), "MultiPersoKey");
//				validateKey(FIDO_SV_IP, "FIDO_SV_IP");
				
				// TODO HSMProvider = Cvn 時 KeyAlias必須長度超過8個字
				// TODO 檢核內容 如 HSMPlusMode = Y
				long OTP_Timeout = Long.parseLong(otpTimeout);
				long Txn_Timeout = Long.parseLong(txnTimeout);
				long LoginTxn_Timeout = Long.parseLong(loginTxnTimeout);
				
				long Digital_Fail_Limit = Long.parseLong(digital_fail_limit);
				long Pattern_Fail_Limit = Long.parseLong(pattern_fail_limit);
				long OTP_Fail_Limit = Long.parseLong(otp_fail_limit);

			} catch (NumberFormatException e) {
				Log4j.log.fatal("Unable to read [{}] from {}. ", e.getMessage(), Config_file_path);

				apLogObj.setMessage(String.format("Unable to read [%s] from %s. ", e.getMessage(), Config_file_path));
				apLogObj.setThrowable(e.getMessage());
				Log4jAP.log.fatal(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setThrowable(e.getMessage());
				inLogObj.setRequest("{}");
				Log4jInbound.log.fatal(inLogObj.getCompleteTxt());

			} catch (IOException e) {
				Log4j.log.fatal("Unable to read config from [" + Config_file_path + "]. " + e.getMessage());

				apLogObj.setMessage("Unable to read config from [" + Config_file_path + "]. " + e.getMessage());
				apLogObj.setThrowable(e.getMessage());
				Log4jAP.log.fatal(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setThrowable(e.getMessage());
				inLogObj.setRequest("{}");
				Log4jInbound.log.fatal(inLogObj.getCompleteTxt());

			} finally {
				if (properties != null) {
					properties.clear();
				}
			}
			
			gotDefault = true;
		}
		
//    	Log4j.log.debug("*** jndiName:" + mapDatabaseProperties().getJndiName());
//    	Log4j.log.debug("*** username:" + mapDatabaseProperties().getUsername());
//    	Log4j.log.debug("*** password:" + mapDatabaseProperties().getPassword());
//    	Log4j.log.debug("*** driverClassName:" + mapDatabaseProperties().getDriverClassName());
//    	Log4j.log.debug("*** url:" + mapDatabaseProperties().getUrl());
	}
	
	@Bean
	public JpaConfig mapDatabaseProperties() {
		return new JpaConfig();
	}
	
	@Bean
	public TomcatServletWebServerFactory tomcatFactory() {
		return new TomcatServletWebServerFactory() {
			@Override
			protected TomcatWebServer getTomcatWebServer(org.apache.catalina.startup.Tomcat tomcat) {
				tomcat.enableNaming();
				return super.getTomcatWebServer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context) {
				// context
				ContextResource contextResource = new ContextResource();
				contextResource.setName(mapDatabaseProperties().getJndiName());
				contextResource.setType(DataSource.class.getName());
				contextResource.setProperty("driverClassName", mapDatabaseProperties().getDriverClassName());
				contextResource.setProperty("url", mapDatabaseProperties().getUrl());
				try {
//					contextResource.setProperty("username", EncryptHelper.Decrypt(mapDatabaseProperties().getUsername()));
//					contextResource.setProperty("password", EncryptHelper.Decrypt(mapDatabaseProperties().getPassword()));
					contextResource.setProperty("username", mapDatabaseProperties().getUsername());
					contextResource.setProperty("password", mapDatabaseProperties().getPassword());
				} catch (Exception e) {
					Log4j.log.error(e.getMessage());
				}
				contextResource.setProperty("maxTotal", mapDatabaseProperties().getMaximum_pool_size());
				contextResource.setProperty("maxIdle", mapDatabaseProperties().getConnection_timeout());
				contextResource.setProperty("maxWaitMillis", mapDatabaseProperties().getMax_wait());
				contextResource.setProperty("initialSize", mapDatabaseProperties().getInitial_size());
				context.getNamingResources().addResource(contextResource);
			}
		};
	}
	
	@Bean
	public OperationCustomizer customize() {
	    return (operation, handlerMethod) -> operation.addParametersItem(
	            new Parameter()
	                    .in("header")
	                    .required(true)
	                    .description("API Key")
	                    .name("apiKey"));
	}
	
	public void logAPwarn(String sessID, String value, String method, String key) throws UnknownHostException {
		Log4j.log.warn("[" + sessID + "][Version: " + BaseServlet.svVerNo + "][" + method + "] " + key + ": " + value
				);
		APLogFormat apLogObj = new APLogFormat();
		Map<String, Object> apLogMap = new HashMap<>();
		apLogMap.put("sessID", sessID);
		apLogMap.put("version", BaseServlet.svVerNo);
		apLogMap.put("method", method);
		apLogMap.put("message", key + ": " + value);
//		apLogMap.put("stacktrace", e.getStackTrace());
		apLogObj.setMessageForMap(apLogMap);
		Log4jAP.log.warn(apLogObj.getCompleteTxt());
	}
	
	private void validateKey(String keyInConfig, String keyName) throws NumberFormatException{
		if (keyInConfig == null ) {
			throw new NumberFormatException(keyName);
		}
	}
}
