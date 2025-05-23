package com.Common.model;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.idenkey.Config.ConfigFIDO;

public abstract class BaseServlet {
	protected final Gson gson = new Gson();
	public static final String svVerNo = "2.2.13";

	// Common resource variables
	@Value("${config_file_path}")
	protected String Config_file_path;
	
	@Value("${log4j_file_path}")
	protected String Log4j_file_path;
	
	@Value("${err_msg_path}")
	protected String Err_msg_path;

	// Swagger GUI activation flag
	@Value("${use_swagger}")
	protected boolean USE_SWAGGER;

	// main DB JNDI name
	protected static String JNDI_Name;

	// i18n 檔案名稱
	protected static String i18n_Name;

	// Verification related timeout variables
	protected static Long OTP_Timeout;
	protected static Long Txn_Timeout;
	protected static Long LoginTxn_Timeout;

	// FIDO server related info
	protected static String FIDO_SV_IP;

	// WSM api key
	protected static String WSM_API_Key;
	// WSI api key
	protected static String WSI_API_Key;
	// push SV url
	protected static String PushServer_URL;

	// cache values
	protected static String server_PubKey = null;
	protected InboundLogFormat inLogObj = null;
	protected APLogFormat apLogObj = null;
	
	@Autowired
	ConfigFIDO configFIDO;
	
	private String urlPrefix;
	
	protected void setServerURL(HttpServletRequest request) {
		if (StringUtils.isBlank(urlPrefix)) {
			String schema = request.getScheme();
			Log4j.log.info("schema: " + schema);
			String serverName = request.getServerName();
			Log4j.log.info("serverName: " + serverName);
			String port = Integer.toString(request.getServerPort());
			Log4j.log.info("port: " + port);
			Log4j.log.info("contextPath: [{}]", request.getContextPath());
			// schema + "://" + serverName + ":" + port + request.getContextPath()
			urlPrefix = schema + "://" + serverName + ":" + port + request.getContextPath();
			configFIDO.setUrlOfRP(urlPrefix + "/");
		}
	}
}
