package com.toppanidgate.WSM.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Cvn.Config.Cfg;
import com.Cvn.Encryptor.AESUtil;
import com.Cvn.Encryptor.Encode;
import com.Cvn.Encryptor.RSA;
import com.ECIES.model.ECCUtil;
import com.ECIES.model.ECIES;
import com.TrustServer.Func.TS_MainFunc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.WSM.Exception.DecryptFailedInSignupDeviceException;
import com.toppanidgate.WSM.Exception.EncryptFailedException;
import com.toppanidgate.WSM.Exception.JSONErrInSignupDeviceException;
import com.toppanidgate.WSM.model.CheckParameters;
import com.toppanidgate.WSM.model.gson.Gson4Common;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;
import com.toppanidgate.fidouaf.res.FidoUafResource;
import com.toppanidgate.idenkey.Blocked_Device_Auth.model.Blocked_Device_AuthService;
import com.toppanidgate.idenkey.Blocked_Device_Auth.model.Blocked_Device_AuthVO2;
import com.toppanidgate.idenkey.Channel.model.ChannelService;
import com.toppanidgate.idenkey.Channel.model.ChannelVO;
import com.toppanidgate.idenkey.Config.ConfigFIDO;
import com.toppanidgate.idenkey.Config.IDGateConfig;
import com.toppanidgate.idenkey.Device_Detail.model.Device_DetailService;
import com.toppanidgate.idenkey.Device_Detail.model.Device_DetailVO;
import com.toppanidgate.idenkey.Members.model.MembersService;
import com.toppanidgate.idenkey.Members.model.MembersVO;
import com.toppanidgate.idenkey.PubkeyStore.model.PubkeyStoreService;
import com.toppanidgate.idenkey.PubkeyStore.model.PubkeyStoreVO;
import com.toppanidgate.idenkey.SMS_Count.model.SMS_CountService;
import com.toppanidgate.idenkey.SMS_Count.model.SMS_CountVO;
import com.toppanidgate.idenkey.Verify_Detail.model.Verify_DetailService;
import com.toppanidgate.idenkey.Verify_Detail.model.Verify_DetailVO;
import com.toppanidgate.idenkey.Verify_Request.model.TxnVO;
import com.toppanidgate.idenkey.Verify_Request.model.Verify_RequestService;
import com.toppanidgate.idenkey.Verify_Request.model.Verify_RequestVO;
import com.toppanidgate.idenkey.common.model.APLogFormat;
import com.toppanidgate.idenkey.common.model.GsonToString;
import com.toppanidgate.idenkey.common.model.InboundLogFormat;
import com.toppanidgate.idenkey.common.model.MemberStatus;
import com.toppanidgate.idenkey.common.model.ReturnCode;
import com.toppanidgate.idenkey.common.model.SmsOperation;
import com.toppanidgate.idenkey.common.model.SmsStatus;
import com.toppanidgate.idenkey.common.model.TxnStatus;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/GW")
public class WSMServlet extends Const {

	//	private static final long serialVersionUID = 1L;
	private Gson gson = new Gson();

	@Autowired
	FidoUafResource fidoUafResource;

	@Autowired
	HttpServletRequest req;

	@Autowired
	HttpServletResponse res;

	@Autowired
	ConfigFIDO configFIDO;
	
	@Value("${config_file_path}")
	private String config_file_path;
	@Value("${log4j_file_path}")
	private String log4j_file_path;
	@Value("${err_msg_path}")
	private String err_msg_path;

	// TS Persofile generating salt elements
	@Value("${Component-A}")
	private String ComponentA;
	@Value("${Component-B}")
	private String ComponentB;
	@Value("${Component-C}")
	private String ComponentC;

	private String i18n_Name;
	private String JNDI_Name;

	private long OTP_Timeout;
	private long Txn_Timeout;
	private int Digital_Fail_Limit;
	private int Pattern_Fail_Limit;
	private int OTP_Fail_Limit;

	private GsonToString gts;

//	private String healthCheckString;
	// private String fakeServerData;
	private String enableStep4OTP;
	private String onlyOTP;
	private String onlyFIDO;

	private TS_MainFunc TS_Inst = null;

	private String urlPrefix;

	static private boolean gotDefault = false;

	// WSM api key
	private String WSM_API_Key;

	private String rsaKeyAlias;

	/**
	 * 測試 ECDH
	 */
	private String mobilePubKey = null;

	/**
	 * [keystore].ECCkeyAlias
	 */
	private String eccKeyAlias;

//	cache values
	private static String webpinAppKey = null;
	
	@Operation(summary = "RP API Portal")
	@PostMapping("/WSM")
	public String doPost(@RequestBody Object body) throws ServletException, IOException {
		String returnMsg = null;
		long beginTime = System.currentTimeMillis();
		InboundLogFormat inLogObj = new InboundLogFormat();
		APLogFormat apLogObj = new APLogFormat();

		// 設定輸入編碼
		req.setCharacterEncoding(UTF_8);
		// 取得 Session ID
		final String sessionID = req.getSession().getId();
		inLogObj.setTraceID(sessionID);
		inLogObj.setClazz(COM_TOPPANIDGATE_WSM_CONTROLLER_WSM_SERVLET);
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz(COM_TOPPANIDGATE_WSM_CONTROLLER_WSM_SERVLET);

//		// get api key
		final String headerApiKey = req.getHeader("apiKey");

		// TODO 這一塊可刪除，這一塊已被 init 取代，目前測試用（立即生效，不用重啟 JBoss
		Properties properties = null;
		File exCfgFile = new File(config_file_path);
		try (InputStream is = new FileInputStream(exCfgFile);) {
			properties = new Properties();
			properties.load(is);
//			i18n_Name = properties.getProperty("i18n_Name", "Message");
//			JNDI_Name = properties.getProperty("JNDI_Name", "java:comp/env/jdbc/PB_AP");
//
			WSM_API_Key = properties.getProperty("WSM_API_Key", "API_Key");
//
//			OTP_Timeout = Long.parseLong(properties.getProperty("OTP_Timeout"));
//			Txn_Timeout = Long.parseLong(properties.getProperty("Txn_Timeout"));
//			Digital_Fail_Limit = Integer.parseInt(properties.getProperty("Digital_Fail_Limit"));
//			Pattern_Fail_Limit = Integer.parseInt(properties.getProperty("Pattern_Fail_Limit"));
			OTP_Fail_Limit = Integer.parseInt(properties.getProperty("OTP_Fail_Limit", "999"));

			// TODO 正式版要寫死成 false (不可使用假資料)
//			useFakeData = "false";	// TODO 正式版要寫死成 false (不可使用假資料)
//			useFakeData = properties.getProperty("USE_FAKE", "false");
			enableStep4OTP = properties.getProperty("enableOTP", FALSE);

			// TODO 正式版 OnlyOTP 要寫死成 false (不可使用 OnlyOTP)
			onlyOTP = properties.getProperty("OnlyOTP", FALSE);
//			OnlyOTP = "false";	// TODO 正式版要寫死成 false (不可使用 OnlyOTP)

			// TODO 正式版 OnlyFIDO 要寫死成 false (不可使用 OnlyFIDO)
			onlyFIDO = properties.getProperty("OnlyFIDO", FALSE);
//			OnlyFIDO = "false";	// TODO 正式版要寫死成 false (不可使用 OnlyFIDO)
//			healthCheckString = properties.getProperty("healthCheckBody", "{\"method\": \"svfHealthCheck\"}");

//			fakeServerData = properties.getProperty("SERVER_DATA");
			rsaKeyAlias = properties.getProperty(RSA_KEY_ALIAS, "RSAkeyAlias");
			eccKeyAlias = properties.getProperty("ECCkeyAlias", "ECCkeyAlias");
//			mobilePubKey = properties.getProperty("mobilePubKey", "3059301306072a8648ce3d020106082a8648ce3d030107034200043e10e16071e724945b1ba3b37f267ed7f12ccedf1e36649ca240dcdec6abf190ba67de7bbf8a9ccf0ec6c51bd9267898941b1d544754a241065390d0e3fa595d");
			
			// 設定Trust server的設定檔路徑
			TS_Inst = new TS_MainFunc(config_file_path, log4j_file_path);

		} catch (IOException e) {
			Log4j.log.fatal("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
					+ "][doPost] Unable to read config from [" + config_file_path + "]. " + e);
//			Log4j.log.fatal("Unable to read config from [" + Config_file_path + "]. " + e);
//			Log4j.log.info("[{}] *** setThrowable@{}: {}ms \r\n", sessionID, "doPost", e.getMessage());
			logException(sessionID, e, DO_POST, "Unable to read config from [" + config_file_path + "]. ", apLogObj,
					inLogObj);

			inLogObj.setExecuteTime(System.currentTimeMillis() - beginTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest(EMPTY_JSONOBJECT);
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
			
			Gson4Common gson4 = new Gson4Common();
			gson4.setReturnCode("0009");
			gson4.setReturnMsg("config_file_path error: " + config_file_path);
			return gson.toJson(gson4);
		} finally {
			if (properties != null) {
				properties.clear();
			}
		}

		// TODO: 設定 IDGateConfig.svVerNo
		if (gotDefault == false) {
//		GsonToString gts = new GsonToString("Message", req.getLocale());
			// 多國語系設定
			gts = new GsonToString(err_msg_path, i18n_Name, req.getLocale());
			Log4j.log.debug("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][doPost]\n config_file_path: "
					+ config_file_path + "\n log4j_file_path: " + log4j_file_path + "\n err_msg_path: " + err_msg_path);
			gotDefault = true;
		}

		Log4j.log.debug("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] API key in header: " + headerApiKey);
		apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSM_API_Key.equals(headerApiKey)) {
			logException(sessionID, new Exception("Reject client request for invalid API key"), DO_POST, "Reject client request for invalid API key", apLogObj, inLogObj);

			inLogObj.setRequest(EMPTY_JSONOBJECT);
			inLogObj.setResponseTxt(gts.common(ReturnCode.APIKeyInvalid));
			inLogObj.setExecuteTime(System.currentTimeMillis() - beginTime);

//			try {
//				// TODO
////				new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.APIKeyInvalid));
//			} catch (IOException e) {
//				logException(sessionID, e, DO_POST, "OutputHandler Error", apLogObj, inLogObj);
//			}
			Log4jInbound.log.info(inLogObj.getCompleteTxt());
			Log4jAP.log.error(apLogObj.getCompleteTxt());	
			return gts.common(ReturnCode.APIKeyInvalid);
		}

		String jsonString = null;
		try {
			jsonString = new ObjectMapper().writeValueAsString(body);
		} catch (JsonProcessingException e) {
			Log4j.log.fatal("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
					+ "][doPost] Error occurred while reading body: " + e.getMessage());
			returnMsg =  gts.common(ReturnCode.JsonParseError);
		}

		// inbound
		Log4j.log.info("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][doPost]\n inbound: " + jsonString);

		JSONObject jSONObject = null;
		HashMap<String, String> map = new HashMap<>();
		// 檢查參數
		CheckParameters cp = new CheckParameters();

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);
			map.put(METHOD2, jSONObject.getString(METHOD2));
			// 111.12.6 回傳 Param: "Error", Value: "{"returnCode":"0014","returnMsg":"JSON
			// parse error"}", value didn't pass the WhiteList filter
			// 改為回傳 { "returnCode": "0014", "returnMsg": "JSON parse error" }
			map = cp.checkMap(map);
		} catch (JsonSyntaxException | JSONException e) {
			logException(sessionID, e, DO_POST, "Unable to parse client JSON msg", apLogObj, inLogObj);
			map.put(ERROR, gts.common(ReturnCode.JsonParseError));
		}

		String errMsg = null;
		String method = null;
		if (map.containsKey(ERROR)) {
			logException(sessionID, new Exception(map.get(ERROR)), DO_POST, PARAMETER_ERROR, apLogObj, inLogObj);
			errMsg = map.get(ERROR);
			if (errMsg.indexOf(gts.common(ReturnCode.JsonParseError)) > -1) {
				returnMsg = errMsg;
			} else {
				returnMsg = gts.common(ReturnCode.ParameterError);
			}
		} else {

			// 方法回傳值
			method = map.get(METHOD2);

//			Log4j.log.info("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] method: " + method + ", Header locale: "
//					+ req.getLocale().toString() + ", IP: " + get_Client_Ip(req));
//			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] method: " + method + ", Header locale: "
//					+ req.getLocale().toString() + ", IP: " + get_Client_Ip(req));
			// Log4jAP.log.trace(apLogObj.getCompleteTxt());
//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][" + method + "], Header locale: " + req.getLocale().toString() + ", IP: " + get_Client_Ip(req));
//			log4jAPforDebug(sessionID, method,
//					"Header locale: " + req.getLocale().toString() + ", IP: " + getClientIp(req), apLogObj);

			this.setServerURL(req);
			
			// switch
			try {
				if (SVF_GET_AUTH_REQUEST.equals(method)) {
					returnMsg = svfGetAuthRequest(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_SEND_AUTH_RESPONSE.equals(method)) {
//					returnMsg = gts.send_AuthResponse("0000", "", "", "", "0");
					returnMsg = svfSendAuthResponse(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_HEALTH_CHECK.equals(method)) {
					returnMsg = svfHealthCheck(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_SIGNUP_DEVICE.equals(method)) {
					returnMsg = svfSignup_Device(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_GET_REG_REQUEST.equals(method)) {
					returnMsg = svfGetRegRequest(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_SEND_REG_RESPONSE.equals(method)) {
					returnMsg = svfSendRegResponse(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_SEND_DEREG_RESPONSE.equals(method)) {
					returnMsg = svfSendDeregResponse(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_SET_AUTH_TYPE.equals(method)) {
					returnMsg = svfSetAuthType(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_CHECK_SETTING.equals(method)) {
					returnMsg = svfCheckSetting(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_CHANGE_SETTING.equals(method)) {
					returnMsg = svfChangeSetting(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_CANCEL_AUTH.equals(method)) {
					returnMsg = svfCancelAuth(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_CANCEL_AUTH.equals(method)) {
					returnMsg = svCancelAuth(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_GET_AUTH_STATUS.equals(method)) {
					returnMsg = svGetAuthStatus(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_GET_TXN_DATA.equals(method)) {
					returnMsg = svfGetTxnData(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_GET_TXN_LIST.equals(method)) {
					returnMsg = svfGetTxnList(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_GET_PUB_KEY_N_TIME.equals(method)) {
					returnMsg = svfGetPubKeyNTime(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_GET_DEVICE_STATUS.equals(method)) {
					returnMsg = svGetDeviceStatus(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_LOCK_DEVICE.equals(method)) {
					returnMsg = svLockDevice(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_UNLOCK_DEVICE.equals(method)) {
					returnMsg = svUnlockDevice(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_DE_REGISTER.equals(method)) {
					returnMsg = svDeRegister(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SV_DE_REGISTER_ST.equals(method)) {
					returnMsg = svDeRegister_St(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if ("svfSyncTime".equals(method)) {
					returnMsg = svfSyncTime(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if ("createAESKey".equals(method)) {
					returnMsg = createAESKey(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if ("svVerify_Offline_OTP".equals(method)) {
					returnMsg = svVerify_Offline_OTP(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_OFFLINE_OTP_REQUEST.equals(method)) {
					returnMsg = svfOfflineOTPRequest(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_GET_TOKEN.equals(method)) {
					returnMsg = svfGetToken(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_OFFLINE_OTP_RESPONSE.equals(method)) {
					returnMsg = svfOfflineOTPResponse(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_DECRYPT_ECDH.equals(method)) {
					returnMsg = svfDecryptECDH(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else if (SVF_ENCRYPT_ECDH.equals(method)) {
					returnMsg = svfEncryptECDH(jsonString, res, gts, sessionID, apLogObj, inLogObj);
				} else {
					errMsg = "Method: " + method + " is not recongized.";
					Log4j.log.warn("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] " + (errMsg));

//					apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] " + (errMsg));

					// Map<String, Object> apLogMap = new HashMap<>();
//					apLogMap.put("sessID", sessionID);
//					apLogMap.put("version", IDGateConfig.svVerNo);
//					apLogMap.put("method", method);
//					apLogMap.put("message", errMsg);
//					apLogObj.setMessageForMap(apLogMap);
//					Log4jAP.log.warn(apLogObj.getCompleteTxt());
					logException(sessionID, new Exception("Method: " + method + " is not recongized."), method,
							PARAMETER_ERROR, apLogObj, inLogObj);
					returnMsg = gts.common(ReturnCode.UnknownMethod);
				}
			} catch (JsonSyntaxException e) {
				logException(sessionID, e, method, "JSON Exception", apLogObj, inLogObj);
				returnMsg = gts.common(ReturnCode.ParameterError);
			} catch (JSONException e) {
				logException(sessionID, e, method, "JSON Exception", apLogObj, inLogObj);
				returnMsg = gts.common(ReturnCode.ParameterError);
			}
		}
		map.clear();
		map.put("returnMsg", returnMsg);
		map = cp.checkMap(map);

		if (!map.containsKey(ERROR)) {
//			// outbound
//			Log4j.log.info("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][" + method + "]\n outbound: "
//					+ returnMsg);
			Log4j.log.info("[{}][Version: " + IDGateConfig.svVerNo + "][{}]\n outbound:[{}] ", sessionID, method,
					returnMsg);
//			returnMsg = map.get("returnMsg");
		} else {
			errMsg = map.get(ERROR);
			logException(sessionID, new Exception(errMsg), method, "ReturnMsgError", apLogObj, inLogObj);
			returnMsg = gts.common(ReturnCode.ReturnMsgError);
		}

//		Log4j.log.info("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] ErrMsg: " + errMsg + ", ReturnMsg: " + returnMsg
//				+ "\r\n");
//		apLogObj.setMessage(
//				"[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "] ErrMsg: " + errMsg + ", ReturnMsg: " + returnMsg);
		// Log4jAP.log.info(apLogObj.getCompleteTxt());
		// 0622問題_註冊一次log量達22K_有重複性紀錄優化
//		log4jAPforDebug(sessionID, method, "ErrMsg: " + errMsg + ", ReturnMsg: " + returnMsg, apLogObj);

		long totalTime = (System.currentTimeMillis() - beginTime);
//		Log4j.log.debug("[" + sessionID + "][" + method + "] *** ReturnMsg:" + returnMsg);
//		Log4j.log.debug("[" + sessionID + "]*** totalTime@" + method + ": " + totalTime + "ms" + "\r\n");
//		Log4j.log.info("[{}] *** totalTime@{}: {}ms \r\n", sessionID, method, totalTime);
		log4jAPforDebug(sessionID, method, "*** totalTime@" + method + ": " + totalTime + "ms", apLogObj);
		
		// 112.3.6 修正 SvfOfflineRequest會有錯誤的log出現HTTP500  
		String returnMsg2 = returnMsg;
		// 112.3.13 修改 svfOfflineOTPRequest 的回傳值 (CHB_FIDO_AP 會檢查 JSON 格式
//		if (SVF_OFFLINE_OTP_REQUEST.equals(method)) {
//			HashMap<String, String> returnMap = new Gson().fromJson(returnMsg, new TypeToken<HashMap<String, String>>() {
//			}.getType());
//			// 112.2.23 去除反斜線(修正 OfflineOTP 計算基礎不同的問題
//			returnMsg = gson.toJson(returnMap).replace("\\", "");
//		} 
		// TODO
//		try {
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(returnMsg);	// pOut.println(result) 收到的FIDO回傳訊息尾部有\r\n，這目前是禁止字元之一
//		} catch (IOException e) {
//			logException(sessionID, e, DO_POST, "OutputHandler Error", apLogObj, inLogObj);
//		}
//		res.setCharacterEncoding("UTF-8");
//		res.setContentType("application/json; charset=UTF-8");
//		try (PrintWriter printWriter = res.getWriter()) {
//			printWriter.write(returnMsg);
//			printWriter.flush();
//		}
		
		try {
			// store response msg into inbound response log part
			inLogObj.setResponseTxt(returnMsg2);
			inLogObj.setExecuteTime(totalTime);

			if (inLogObj.hasException()) {
				Log4jInbound.log.warn(inLogObj.getCompleteTxt());
			} else {
				Log4jInbound.log.info(inLogObj.getCompleteTxt());
			}
		} catch (JsonSyntaxException e) {
			logException(sessionID, e, method, "Unable to parse response JSON msg", apLogObj, inLogObj);
		}
		return returnMsg;
	}
	
	private String svfOfflineOTPResponse(String input, HttpServletResponse res, GsonToString gts, String sessionID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(TXN_ID, jSONObject.getString(TXN_ID));
		map.put(OTP2, jSONObject.getString(OTP2));
		// offline不用
//		boolean hasDeviceOS = jSONObject.has(DEVICE_OS);
//		if (hasDeviceOS) {
//			map.put(DEVICE_OS, jSONObject.getString(DEVICE_OS));
//		}

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessionID, new Exception(map.get(ERROR)), SVF_OFFLINE_OTP_RESPONSE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String txnID = map.get(TXN_ID);
		String otp = map.get(OTP2);
		long idgID = Long.parseLong(idgateID);

		log4jAPforDebug(sessionID, SVF_OFFLINE_OTP_RESPONSE, "idgateID: " + idgateID + ", channel: "
				+ channel + ", txnID: " + txnID + ", otp: " + otp, apLogObj);

		ChannelService chSvc = null;
		ChannelVO chVO = null;
		MembersService memSrv = null;
		MembersVO memVO = null;
		Device_DetailService ddSvc = null;
		Device_DetailVO ddVO = new Device_DetailVO();

		try {
			chSvc = new ChannelService(JNDI_Name, sessionID);
			memSrv = new MembersService(JNDI_Name, sessionID);
			ddSvc = new Device_DetailService(JNDI_Name, sessionID);

			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}

			memVO = memSrv.getByIdgateID(idgID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] DB Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		// check user status
		if (memVO == null || ddVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} 
		
		String prevStatus = memVO.getCustomer_Status();
		if (prevStatus.equals(MemberStatus.Deleted)) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else if (prevStatus.equals(MemberStatus.Locked)
				|| prevStatus.equals(MemberStatus.LockedForTooManyAuthFails)) {
			return gts.common(ReturnCode.MemberLockedError);
		} else if (!prevStatus.equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		// fetch txn
		Verify_RequestService vrSvc = null;
		Verify_DetailService vdSvc = null;
		Verify_RequestVO vrVO = null;
		Verify_DetailVO vdVO = null;
		String idgateIdHash = null;

		try {
			vrSvc = new Verify_RequestService(JNDI_Name, sessionID);
			String chJNDI = chVO.getJNDI();
//			String lastJNDI = chJNDI.substring(chJNDI.lastIndexOf("/") + 1, chJNDI.length());
//			vdSvc = new Verify_DetailService("java:comp/env/jdbc/" + lastJNDI, sessionID);
			vdSvc = new Verify_DetailService(chJNDI, sessionID);

			String requestID = txnID.substring(8);
			String transationDate = txnID.substring(0, 8);
			vrVO = vrSvc.getOneWithTime(idgID, requestID,
					transationDate);

			if (vrVO == null) {
				return gts.common(ReturnCode.TxnNotFoundInOfflineOTPResponse);
			} else {
				String authStatus = vrVO.getStatus_Code();
				if (isNotWaitForVerify(authStatus)) {	// authStatus 必須是 01
					Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
							+ "][svfGetTxnData] Request rejected. Current Txn status: " + authStatus);
					//					    "returnCode": "0857", "returnMsg": "交易狀態錯誤"
					// 修正 svfOfflineOTPResponse 沒有阻擋已經驗證成功過的OTP 再次驗證
					return gts.common(ReturnCode.TxnStatusErrInOfflineOTPResponse);
				}
			}

			vdVO = vdSvc.getOneVerify_Detail(idgID, requestID,
					transationDate);

			if (vdVO == null) {
				return gts.common(ReturnCode.TxnNotFoundInOfflineOTPResponse);
				// TODO UNREMARK BELOW 2 lines 111.7.27 交易狀態錯誤
			}

			idgateIdHash = AESUtil.SHA256_To_HexString(idgateID);

		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] DB Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.TrustServerError);
		}

		// Trust SV OTP verification
		String returnCode;
		HashMap<String, String> dataSet = null;
		HashMap<String, String> formparams = new HashMap<String, String>();
		formparams.put(METHOD, VERIFY_DEV_ID_OTP);
		formparams.put(CHANNEL3, memVO.getChannel_Code());
		formparams.put(TIME_STAMP, String.valueOf(System.currentTimeMillis()));
		formparams.put(CHALLENGE, idgateIdHash);	// challenge = sha256(idgateID)
		//	        System.out.println("*** idgateIdHash: " + idgateIdHash);
		//	        System.out.println("*** vdVO.getTransaction_Hash(): " + vdVO.getTransaction_Hash());
		//	        System.out.println("*** ddVO.getDevice_ID(): " + ddVO.getDevice_ID());
		//	        Log4j.log.info("*** idgateIdHash: " + idgateIdHash);
		//	        Log4j.log.info("*** vdVO.getTransaction_Hash(): " + vdVO.getTransaction_Hash());
		//	        Log4j.log.info("*** ddVO.getDevice_ID(): " + ddVO.getDevice_ID());
		formparams.put(TXN_DATA, vdVO.getTransaction_Hash());	// txnData =實際交易內容sha256(64) 
		formparams.put(ESN, ddVO.getESN());	// seed = DevId 
		formparams.put(OTP3, otp);

		Log4j.log.debug("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfOfflineOTPResponse] Sending msg to TrustServer to verify otp: "
				+ new Gson().toJson(formparams));

		apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfOfflineOTPResponse] Sending msg to TrustServer to verify otp: "
				+ new Gson().toJson(formparams));
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		returnCode = TS_Inst.verifyDevIdOTP(channel, ddVO.getESN(), idgateIdHash,
				vdVO.getTransaction_Hash(), otp, String.valueOf(System.currentTimeMillis()), sessionID);

		Log4j.log.debug("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfOfflineOTPResponse] Respond msg from TrustServer: " + returnCode);

		try {
			dataSet = new Gson().fromJson(returnCode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.TrustServerError);
		}

		try {
			// OTP invalid
			if (FAIL_0020.equals(dataSet.get(RETURN_CODE))) {
				// svfOfflineOTPResponse OTP驗錯FIDO這邊記錄到pattern錯誤次數了
				int otpFailCount = memVO.getAuth_Fails() + 1;
				
				Log4j.log.debug("[{}][Version: {}][svfOfflineOTPResponse] *** otpFailCount:{} \nOTP_Fail_Limit is {} \notpFailCount >= OTP_Fail_Limit:[{}]", sessionID,
						IDGateConfig.svVerNo, otpFailCount, OTP_Fail_Limit, otpFailCount >= OTP_Fail_Limit);

				memSrv.updateAuthFailCounter(idgID, otpFailCount);
				// verification fail counter have reached limit, lock account
				// 要鎖 
				if (otpFailCount >= OTP_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(OFFLINE_OTP, ""), ddVO.getAuth_Type());
					memSrv.addMemberStatusLog(idgID, prevStatus, MemberStatus.Normal,
							"disabled for reached TOP failure limit");

					return gts.get_FailCount(ReturnCode.TooManyAuthFailsInChangeSetting,
							String.valueOf(otpFailCount), null);
				}
				// 112.1.11 照理說成功不會回覆failcount，而且成功後failcount應該歸0
				return gts.get_FailCount(ReturnCode.OTPInvalidInOfflineOTPResponse, String.valueOf(otpFailCount), null);
				//	                return gts.get_OfflineOTPResponse(ReturnCode.Success, String.valueOf(memVO.getAuth_Fails() + 1));
			}
			// Trust SV abnormal
			else if (!SUCCESS.equals(dataSet.get(RETURN_CODE))) {
				return gts.common(ReturnCode.TrustServerError);
			} else {
				// SvfOfflineOTPResponse驗證成功, 發送 svGetAuthStatus 下行authStatus依舊為01, 應為00
                // verify success
                vrSvc.updateTxnStatus(idgID, Long.parseLong(txnID.substring(8)), TxnStatus.Success, vrVO.getStatus_Code());
            }

			// Reset to 0 if otp is valid
			memSrv.updateAuthFailCounter(idgID, 0);

		} catch (SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPResponse] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfOfflineOTPResponse] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		return gts.get_OfflineOTPResponse(ReturnCode.Success, ZERO_0);
	}

	/**
	 * authStatus 是否為 01
	 * @param authStatus
	 * @return
	 */
	private boolean isNotWaitForVerify(String authStatus) {
		return !TxnStatus.WaitForVerify.equals(authStatus);
	}
	 
	private String svfGetToken(String input, HttpServletResponse res, GsonToString gts, String sessionID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessionID, new Exception(map.get(ERROR)), SVF_GET_TOKEN, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);

		log4jAPforDebug(sessionID, SVF_GET_TOKEN, "idgateID: " + idgateID + ", channel: " + channel, apLogObj);

		ChannelService chSvc = null;
		ChannelVO chVO = null;
		MembersService memSrv = null;
		MembersVO memVO = null;
		Device_DetailService ddSvc = null;
		Device_DetailVO ddVO = new Device_DetailVO();

		try {
			chSvc = new ChannelService(JNDI_Name, sessionID);
			memSrv = new MembersService(JNDI_Name, sessionID);
			ddSvc = new Device_DetailService(JNDI_Name, sessionID);

			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
			
			memVO = memSrv.getByIdgateID(idgID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfGetToken] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfGetToken] DB Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		// check user status
		if (memVO == null || ddVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		String encToken = null;
		HashMap<String, String> tokenData = new HashMap<String, String>();

		try {
			tokenData.put(TIME, String.valueOf(new Date().getTime()));
			tokenData.put(IDGATE_ID, AESUtil.SHA256_To_HexString(idgateID));

			String deviceKeyB = ddVO.getDevice_Data().substring(32, 64);
			encToken = this.getEncData(gson.toJson(tokenData), deviceKeyB, 14, 30);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchPaddingException |
				InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
				BadPaddingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfGetToken] Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfGetToken] Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.EncryptFailedInSvfGetToken);
		}

		return gts.get_Token(ReturnCode.Success, encToken);
	}

	private String getEncData(String data, String deviceKeyB, int beginIndex, int endIndex)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		//			System.out.println("*** plainTxt:" + data);
		String encToken;
		String shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
		// AES()加密後的字串，Key = deviceKeyB[14, 29]
		String shaDevKeyB16 = shaDeviceKeyB.substring(beginIndex, endIndex);
		//				byte[] keyBytes = AESUtil.SHA256_To_Bytes(shaDevKeyB16); // CF91B621D9B7CF95
		byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		//			Log4j.log.debug("*** deviceKeyB: {}", deviceKeyB);
		//			Log4j.log.debug("*** shaDevKeyB16: {}", shaDevKeyB16);	// 你截斷的部分是長這樣嗎？ CF91B621D9B7CF95

		byte[] keyBytes = shaDevKeyB16.getBytes(StandardCharsets.UTF_8);
		byte[] encryptedData = AESUtil.encrypt(ivBytes, keyBytes, data.getBytes(StandardCharsets.UTF_8));
		//			System.out.println("*** encryptedData:" + Encode.byteToHex(encryptedData));
		encToken = Base64.getUrlEncoder()
				.encodeToString(encryptedData);
		//			System.out.println("*** encData:" + encToken);
		return encToken;
	}

	private String svfOfflineOTPRequest(String input, HttpServletResponse res, GsonToString gts, String sessionID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(BANK_TXN_DATA, jSONObject.getString(BANK_TXN_DATA));
		map.put(TXN_TITLE, jSONObject.getString(TXN_TITLE));
		
		Log4j.log.debug("[{}][Version: {}][svfOfflineOTPRequest] *** TXN_TITLE:{} length is {}", sessionID,
				IDGateConfig.svVerNo, jSONObject.getString(TXN_TITLE), jSONObject.getString(TXN_TITLE).length());
		Log4j.log.debug("[{}][Version: {}][svfOfflineOTPRequest] *** BANK_TXN_DATA:{} length is {}", sessionID,
				IDGateConfig.svVerNo, jSONObject.getString(BANK_TXN_DATA), jSONObject.getString(BANK_TXN_DATA).length());

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessionID, new Exception(map.get(ERROR)), SVF_OFFLINE_OTP_REQUEST, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String txnTitle = map.get(TXN_TITLE);
		
		String bankTxnData = map.get(BANK_TXN_DATA);
		long idgID = Long.parseLong(idgateID);

		log4jAPforDebug(sessionID, SVF_OFFLINE_OTP_REQUEST, "idgateID: " + idgateID + ", channel: " + channel + ", txnTitle: " + txnTitle + ", txnData: " + bankTxnData, apLogObj);

		ChannelService chSvc = null;
		ChannelVO chVO = null;
		MembersService memSrv = null;
		MembersVO memVO = null;
		Device_DetailService ddSvc = null;
		Device_DetailVO ddVO = new Device_DetailVO();

		try {
			chSvc = new ChannelService(JNDI_Name, sessionID);
			memSrv = new MembersService(JNDI_Name, sessionID);
			ddSvc = new Device_DetailService(JNDI_Name, sessionID);

			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}

			memVO = memSrv.getByIdgateID(idgID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPRequest] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPRequest] DB Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		// check user status
		if (memVO == null || ddVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else {
			String customer_Status = memVO.getCustomer_Status();
			if (customer_Status.equals(MemberStatus.Deleted)) {
				return gts.common(ReturnCode.MemberDeletedError);
			} else if (customer_Status.equals(MemberStatus.Locked)
					|| customer_Status.equals(MemberStatus.LockedForTooManyAuthFails)) {
				return gts.common(ReturnCode.MemberLockedError);
			} else if (!customer_Status.equals(MemberStatus.Normal)) {
				return gts.common(ReturnCode.MemberStatusError);
			}
		}

		// build verify request content
		Verify_RequestVO vrVO = new Verify_RequestVO();
		vrVO.setiDGate_ID(idgID);
		vrVO.setAuth_Mode(ZERO_0);
		vrVO.setChannel_Code(channel);
		vrVO.setStatus_Code(TxnStatus.WaitForVerify);
		vrVO.setVerify_Type(ZERO_0);
		vrVO.setTransaction_Name(txnTitle);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Verify_DetailService vdSvc = null;
		Verify_RequestService vrSvc = null;

		String qrCode;
		long txnID;
		try {
			vdSvc = new Verify_DetailService(chVO.getJNDI(), sessionID);
			vrSvc = new Verify_RequestService(JNDI_Name, sessionID);

			txnID = vrSvc.addVerify_Request(vrVO);
			vrVO = vrSvc.getOne(idgID, String.valueOf(txnID));
			// add timestamp into txnData
			bankTxnData = bankTxnData.substring(0, bankTxnData.length() - 1) + ",\"Timestamp\":\""
					+ String.valueOf(vrVO.getTransaction_Date().getTime()) + "\"}";

			vdSvc.addVerify_DetailVO(idgID, String.valueOf(txnID), channel, "", txnTitle, dateFormat.format(new Date()),
					"", bankTxnData, AESUtil.SHA256_To_HexString(bankTxnData), null, vrVO.getTransaction_Date());
			// build qr code string
			qrCode = "offlineotp|" + AESUtil.SHA256_To_HexString(bankTxnData) + "|" + txnTitle;
			String qrCodeForChecksum = "offlineotp|" + AESUtil.SHA256_To_HexString(bankTxnData) + "|" + gson.toJson(txnTitle);
			
			Log4j.log.debug("[{}][Version: {}][svfOfflineOTPRequest] *** qrCode:[{}] length is {}", sessionID,
					IDGateConfig.svVerNo, qrCode, qrCode.length() );
			Log4j.log.debug("[{}][Version: {}][svfOfflineOTPRequest] *** qrCodeForChecksum:[{}] length is {}", sessionID,
					IDGateConfig.svVerNo, qrCodeForChecksum, qrCodeForChecksum.length() );
			
			String deviceKeyB = ddVO.getDevice_Data().substring(32, 64);
			String shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			String shaDevKeyB16 = shaDeviceKeyB.substring(13, 29);
			//				byte[] keyBytes = AESUtil.SHA256_To_Bytes(shaDevKeyB16); // CF91B621D9B7CF95
			byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			//	            Log4j.log.debug("*** deviceKeyB: {}", deviceKeyB);

			// Key = deviceKeyB[13, 28]
			byte[] keyBytes = shaDeviceKeyB.substring(13, 29).getBytes(StandardCharsets.UTF_8);
			Log4j.log.debug("*** shaDevKeyB16: {}", shaDevKeyB16);	// 你截斷的部分是長這樣嗎？ CF91B621D9B7CF95
			Log4j.log.debug("*** qrCode: {}", qrCode);	 
			Log4j.log.debug("*** qrCodeForChecksum: {}", qrCodeForChecksum);	 

			// 檢核碼為AES()加密後的字串取“後“16碼：
			// compute and add checksum
			String checksum = AESUtil.Bytes_To_HexString(AESUtil.encrypt(ivBytes,
					keyBytes, qrCode.getBytes(StandardCharsets.UTF_8)));
			qrCode += "|" + checksum.substring(checksum.length() - 16);
			
			// 112.2.23 Json 格式計算不同(計算時無\，前端計算到\ )
//			String test = gson.toJson(qrCode);
////			String test = qrCode.replace("\"", "\\\"");
////			String test = "offlineotp|935E8A03A8791F4393F0AB78216AA44D0A8A64DE705BC906E897824563D926E9|{\\\"k\\\":\\\"v\\\"}";
//			Log4j.log.debug("*** test: {}", test);	

		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPRequest] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPRequest] DB Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchPaddingException |
				InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
				BadPaddingException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPRequest] Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + IDGateConfig.svVerNo + "][svfOfflineOTPRequest] Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.EncryptFailedInSvfOfflineOTPRequest);
		}

		String txnIdStr = new SimpleDateFormat(YYYY_MM_DD).format(vrVO.getTransaction_Date()) + String.valueOf(txnID);
		return gts.get_OfflineOTPRequest(ReturnCode.Success, qrCode, txnIdStr);
	}

	private String svVerify_Offline_OTP(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(OTP2, jSONObject.getString(OTP2));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] "
					+ map.get(ERROR));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] "
					+ map.get(ERROR));
			Log4jAP.log.debug(apLogObj.getCompleteTxt());

			return gts.common(ReturnCode.ParameterError);
		}

		String otp = map.get(OTP2);
		String channel = map.get(CHANNEL2);
		String idgateID = map.get(IDGATE_ID);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] channel: "
				+ channel + ", idgateID: " + idgateID + ", otp: " + otp);

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] channel: "
				+ channel + ", idgateID: " + idgateID + ", otp: " + otp);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		ChannelService chSvc = null;
		ChannelVO chVO = null;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] DB Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svVerify_Offline_OTP] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		MembersService memSvc = null;
		MembersVO memVO = null;
		Device_DetailService ddSvc = null;
		Device_DetailVO ddVO = null;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
			memVO = memSvc.getByIdgateID(idgID);
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] DB Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svVerify_Offline_OTP] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null || ddVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.ChannelNotMatchToMember);
		} else {
			String customer_Status = memVO.getCustomer_Status();
			if (customer_Status.equals(MemberStatus.Locked)
					|| customer_Status.equals(MemberStatus.LockedForTooManyAuthFails)) {
				return gts.common(ReturnCode.MemberLockedError);
			} else if (customer_Status.equals(MemberStatus.Deleted)) {
				return gts.common(ReturnCode.MemberDeletedError);
			}
		}

		String returnCode;
		HashMap<String, String> dataSet = null;
		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair(METHOD, VERIFY_DEV_ID_OTP));
		formparams.add(new BasicNameValuePair(CHANNEL3, memVO.getChannel_Code()));
		formparams.add(new BasicNameValuePair(TIME_STAMP, String.valueOf(System.currentTimeMillis())));
		formparams.add(new BasicNameValuePair(CHALLENGE, ZERO_0000000000000000));
		formparams.add(new BasicNameValuePair(TXN_DATA, ZERO_0000000000000000));
		formparams.add(new BasicNameValuePair(ESN, ddVO.getESN()));
		formparams.add(new BasicNameValuePair(OTP3, otp));

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svVerify_Offline_OTP] Sending msg to TrustServer to generate sms otp: "
				+ new Gson().toJson(formparams));

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svVerify_Offline_OTP] Sending msg to TrustServer to generate sms otp: "
				+ new Gson().toJson(formparams));
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		returnCode = TS_Inst.verifyDevIdOTP(channel, ddVO.getESN(), ZERO_0000000000000000, ZERO_0000000000000000, otp,
				String.valueOf(System.currentTimeMillis()), sessID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svVerify_Offline_OTP] Respond msg from TrustServer: " + returnCode);

		try {
			dataSet = new Gson().fromJson(returnCode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svVerify_Offline_OTP] Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svVerify_Offline_OTP] Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.TrustServerError);
		}

		if (FAIL_0020.equals(dataSet.get(RETURN_CODE))) {
			return gts.common(ReturnCode.VerifyFailedInVerifyOfflineOtp);
		} else if (!SUCCESS.equals(dataSet.get(RETURN_CODE))) {
			return gts.common(ReturnCode.TrustServerError);
		}

		return gts.common(ReturnCode.Success);
	}

	private String svfSignup_Device(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(TRANSACTION_ID, jSONObject.get(TRANSACTION_ID).toString());
		map.put(DEVICE_INFO, jSONObject.getString(DEVICE_INFO));
		map.put(DATA, jSONObject.getString(ENC_REG_RES));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			Log4j.log.debug(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] " + map.get(ERROR));

			apLogObj.setMessage(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] " + map.get(ERROR));
			Log4jAP.log.debug(apLogObj.getCompleteTxt());

			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String deviceInfo = map.get(DEVICE_INFO);
		String encRegRes = map.get(DATA);
		String transactionID = map.get(TRANSACTION_ID);

		Log4j.log.info("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] idgateID: " + idgateID
				+ ", channel: " + map.get(CHANNEL2) + ", deviceInfo: " + map.get(DEVICE_INFO) + ", encRegRes: "
				+ map.get(ENC_REG_RES) + ", transactionID: " + transactionID);

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] idgateID: "
				+ idgateID + ", channel: " + map.get(CHANNEL2) + ", deviceInfo: " + map.get(DEVICE_INFO)
				+ ", encRegRes: " + map.get(ENC_REG_RES) + ", transactionID: " + transactionID);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		ChannelVO chVO = null;
		try {
			chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] DB Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}
		
		String encRegResB64Decode = null;
		try {
			encRegResB64Decode = new String(Base64.getUrlDecoder().decode(encRegRes), StandardCharsets.UTF_8);
		} catch (Exception e) {
			Log4j.log.debug("*** encRegRes:{}", encRegRes);
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to decrypt encRegRes", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInSignupDevice);
		}

		HashMap<String, String> dataSet = null;
		try {
			dataSet = new Gson().fromJson(encRegResB64Decode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JSONException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] Unable to parse JSON: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] Unable to parse JSON: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.JSONErrInSignupDevice);
		}

		// decrypt data by TS
		byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] keyBytes = null;
		String decryptedData = null;

		try {
			decryptedData = this.decryptRSA(sessID, dataSet.get(KEY), "", dataSet.get(DATA));
		} catch (Exception e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] Unable to decrypt data: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] Unable to decrypt data: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DecryptFailedInSignupDevice);
		}

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] Decrypted data: "
				+ decryptedData);

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] Decrypted data: "
				+ decryptedData);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (decryptedData.indexOf("ReturnCode\":\"0000\"") == -1) {
			return gts.common(ReturnCode.DecryptFailedInSignupDevice);
		}

		JSONObject json = null;
		try {
			json = new JSONObject(decryptedData);
		} catch (JsonSyntaxException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] Unable to parse JSON: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] Unable to parse JSON: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.JSONErrInSignupDevice);
		}

		HashMap<String, String> devInfo = new Gson().fromJson(deviceInfo, new TypeToken<HashMap<String, String>>() {
		}.getType());

		if (!devInfo.containsKey(DEVICE_LABEL) || !devInfo.containsKey(DEVICE_MODEL)
				|| !devInfo.containsKey(DEVICE_OS) || !devInfo.containsKey(DEVICE_NAME)
				|| !devInfo.containsKey(DEVICE_IP) || !devInfo.containsKey(USER_ID2)) {
			Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] deviceInfo is either missing key(s) or key name incorrect.");

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] deviceInfo is either missing key(s) or key name incorrect.");
			Log4jAP.log.warn(apLogObj.getCompleteTxt());

			return gts.common(ReturnCode.JSONErrInSignupDevice);
		}

		Device_DetailService ddSvc = null;
		MembersService memSrv = null;
		MembersVO memVO = null;
		long idgateId = -1;
		try {
			idgateId = Long.parseLong(idgateID);
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
			memSrv = new MembersService(JNDI_Name, sessID);

			memVO = memSrv.getByIdgateID(idgateId);
		} catch (SQLException | UnknownHostException | NamingException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] DB Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Register)) {
			Log4j.log.info("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] This member isn't waiting for register");

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] This member isn't waiting for register");
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			return gts.common(ReturnCode.MemberStatusIncorrectInSignupDevice);
		}

		JSONObject jsonSignupData;
		String signupDeviceData;
		String signupPinHash;
		String signupPush;
		String signupPattern;
		String signupBio;
		String signupAuthType;
		try {
			jsonSignupData = json.getJSONObject(DATA2);
			signupDeviceData = jsonSignupData.getString(DEVICE_DATA);
			signupPinHash = jsonSignupData.getString(PIN);
			signupPush = jsonSignupData.has(PUSH) ? jsonSignupData.getString(PUSH) : "-";
			signupPattern = jsonSignupData.getString(PATTERN);
			signupBio = jsonSignupData.getString(BIO);
			signupAuthType = jsonSignupData.getString(AUTH_TYPE);
		} catch (JSONException e) {
			logException(sessID, e, SVF_SIGNUP_DEVICE, "JSONErrInSignupDevice", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSignupDevice);
		}

		// 112.1.18 註冊時檢查黑名單
		String deviceBrand = devInfo.get(DEVICE_LABEL);
		String dvcModel = devInfo.get(DEVICE_MODEL);
		List<Blocked_Device_AuthVO2> channelAndModelList = new ArrayList<>();
		try {
			String upperLabel = deviceBrand.toUpperCase();
			String upperModel = dvcModel.toUpperCase();
			String convertedAuthType = this.changeAuthType(signupAuthType);
			channelAndModelList = new Blocked_Device_AuthService(JNDI_Name, sessID).getList2(upperLabel, upperModel, convertedAuthType, memVO.getChannel_Code());

			if (!channelAndModelList.isEmpty()) {
				return gts.common(ReturnCode.BlockedAuthTypeInSignupDevice);
			}
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		/*
		 * TS 創建會員 這區塊是log紀錄用
		 */
		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair(METHOD, SIGNUP_PERSO));
		formparams.add(new BasicNameValuePair(CHANNEL3, channel));
		formparams.add(new BasicNameValuePair(DEV_TYPE, "4"));
		formparams.add(new BasicNameValuePair(USER_ID, String.valueOf(System.currentTimeMillis())));
		formparams.add(new BasicNameValuePair(SEED_SECRET, ComponentA));
		formparams.add(new BasicNameValuePair(ESN_SECRET, ComponentB));
		formparams.add(new BasicNameValuePair(MASTER_SECRET, ComponentC));
		formparams.add(new BasicNameValuePair(DEV_DATA, signupDeviceData));
		formparams.add(new BasicNameValuePair(PIN_HASH, signupPinHash));

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfSignup_Device][signup_Perso] Sending to TS: " + gson.toJson(formparams));

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfSignup_Device][signup_Perso] Sending to TS: " + gson.toJson(formparams));
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		// String.valueOf(System.currentTimeMillis())
		SecureRandom random = new SecureRandom();  
		byte[] bytes = new byte[3];
		random.nextBytes(bytes);
		// user id + secure random = new user id
		String userID = String.valueOf(System.currentTimeMillis()) + Encode.byteToHex(bytes);
		String retMsg = TS_Inst.signupPerso(channel, "4", ComponentB, ComponentA, ComponentC,
				userID, signupDeviceData,
				signupPinHash, sessID);	

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfSignup_Device][signup_Perso] Reply from TS: " + retMsg);

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfSignup_Device][signup_Perso] Reply from TS: " + retMsg);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		HashMap<String, String> tsMsg = new Gson().fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
		}.getType());

		if (!SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
			return gts.common(ReturnCode.TrustServerError);
		}

		try {
			ddSvc.addDevice_Detail(idgateId, tsMsg.get(ESN), signupDeviceData,
					signupPush, BIO_1, signupPinHash,
					jsonSignupData.has(PATTERN) ? signupPattern : "-",
					jsonSignupData.has(BIO) ? signupBio : "-",
					jsonSignupData.has(AUTH_TYPE) ? signupAuthType : "01",
					devInfo.get(DEVICE_OS), NOT_AVAILABLE,	// 111.12.21 iDenKeyFIDO APP帶入的裝置IP需轉換格式並儲存DB
					devInfo.get(DEVICE_LABEL),
					devInfo.get(DEVICE_MODEL),
					devInfo.get(DEVICE_IP),
					devInfo.get(DEVICE_OS_VER),
					devInfo.get(APP_VER), transactionID);
		} catch (SQLException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] DB Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		HashMap<String, String> respSet = new HashMap<String, String>();
		respSet.put("AB", tsMsg.get("Mercury"));
		respSet.put(IDGATE_ID, String.valueOf(idgateId));
		respSet.put("perso", tsMsg.get("PersoFile"));
		respSet.put(TIME, String.valueOf(System.currentTimeMillis()));
		
		// 112.3.14 回傳值 增加authType、authHash
		boolean hasDevice = jsonSignupData.has(DEVICE);
		String device = jsonSignupData.getString(DEVICE);
//		if (hasDevice) {
//			 Log4j.log.debug("[{}][svfSignup_Device]*** device:{}", sessID, device);
//		} else {
//			Log4j.log.debug("[{}][svfSignup_Device]*** NO_DEVICE:{}", sessID, "NO DEVICE");
//		}
		respSet.put(AUTH_TYPE, signupAuthType);
		if (signupAuthType.indexOf(BIO_1) > -1) {
			respSet.put(AUTH_HASH, signupBio);
		} else if (signupAuthType.indexOf(PATTERN_2) > -1) {
			respSet.put(AUTH_HASH, signupPattern);
		} else if (signupAuthType.indexOf(PIN_3) > -1) {
			respSet.put(AUTH_HASH, signupPinHash);
		} else {
			respSet.put(AUTH_HASH, hasDevice? device : "NO_device");
		}

		String respTxt = new Gson().toJson(respSet);

		Log4j.log.trace("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfSignup_Device] Plain text data before encryption: " + respTxt);

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
				+ "][svfSignup_Device] Plain text data before encryption: " + respTxt);
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		try {
			keyBytes = AESUtil.SHA256_To_Bytes(signupDeviceData.substring(2, 21));
			respTxt = Base64.getUrlEncoder()
					.encodeToString(AESUtil.encrypt(ivBytes, keyBytes, respTxt.getBytes(StandardCharsets.UTF_8)));

			// log member status change
			memSrv.addMemberStatusLog(idgateId, memVO.getCustomer_Status(), MemberStatus.Normal, "Registration");
			;

			// update status to normal
			memVO.setCustomer_Status(MemberStatus.Normal);
			memSrv.updateMember(memVO);

		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfVerify_Device] Unable to decrypt data: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfVerify_Device] Unable to decrypt data: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.EncryptFailedInSignupDevice);
		} catch (SQLException e) {
			Log4j.log.error(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSignup_Device] DB Error occurred: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSignup_Device] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			return gts.common(ReturnCode.DatabaseError);
		}

		return gts.signup_Device(ReturnCode.Success, respTxt, idgateID,
				jsonSignupData.has(PUSH) ? signupPush : null);
	}

	private String createAESKey(String input, HttpServletResponse res2, GsonToString gts2, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(RANDOM2, jSONObject.getString(RANDOM2));
		map.put(KEY_ALIAS, jSONObject.getString(KEY_ALIAS));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			Log4j.log
					.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][createAESKey] " + map.get(ERROR));

			apLogObj.setMessage(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][createAESKey] " + map.get(ERROR));
			Log4jAP.log.debug(apLogObj.getCompleteTxt());

			return gts.common(ReturnCode.ParameterError);
		}

		String random = map.get(RANDOM2);
		String keyAlias = map.get(KEY_ALIAS);

		Log4j.log.info("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][createAESKey] random: " + random
				+ ", keyAlias: ");

		apLogObj.setMessage("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][createAESKey] random: " + random
				+ ", keyAlias: ");
		Log4jAP.log.info(apLogObj.getCompleteTxt());
		
		return TS_Inst.createKey_AES(random, keyAlias, sessID);
	}
	
	private String svUnlockDevice(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_UNLOCK_DEVICE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		
		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);
		
		log4jAPforDebug(sessID, SV_UNLOCK_DEVICE, "idgateID: " + idgateID + ", channel: " + channel, apLogObj);
		
		try {
			if (!isChannelValid(sessID, channel)) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, "NamingException::DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (SQLException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, "SQLException::DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		ChannelService chSvc;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, "Unknown Host Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, "DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersService memSrv;
		try {
			memSrv = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		ChannelVO chVO = null;
		try {
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
			
			memVO = memSrv.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (memVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.ChannelNotMatchToMember);
		}
		
		String customer_Status = memVO.getCustomer_Status();
		if (!customer_Status.equals(MemberStatus.Locked)) {
			return gts.get_DeviceStatus(ReturnCode.MemberStatusError, customer_Status, null, null, null,
					null, null, null, null, null);
		}
		
		try {
			memSrv.addMemberStatusLog(memVO.getiDGate_ID(), customer_Status, MemberStatus.Normal,
					"Issued by " + channel);
			memVO.setCustomer_Status(MemberStatus.Normal);
			memSrv.updateMember(memVO);
		} catch (SQLException e) {
			logException(sessID, e, SV_UNLOCK_DEVICE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		return gts.get_DeviceStatus(ReturnCode.Success, MemberStatus.Normal, null, null, null, null, null, null, null, null);
	}
	
	private String svLockDevice(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_LOCK_DEVICE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);

		log4jAPforDebug(sessID, SV_LOCK_DEVICE, "idgateID: " + idgateID + ", channel: " + channel, apLogObj);
		
		ChannelService chSvc;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_LOCK_DEVICE, "Unknown Host Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_LOCK_DEVICE, "DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersService memSrv;
		try {
			memSrv = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_LOCK_DEVICE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_LOCK_DEVICE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		ChannelVO chVO = null;
		try {
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
			
			memVO = memSrv.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SV_LOCK_DEVICE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.ChannelNotMatchToMember);
		}

//		if (memVO.getCustomer_Status().equals(MemberStatus.Locked)) {
		String customer_Status = memVO.getCustomer_Status();
		if (customer_Status.equals(MemberStatus.Deleted)
				|| customer_Status.equals(MemberStatus.Locked)
				|| customer_Status.equals(MemberStatus.LockedForTooManyAuthFails)) {
			return gts.get_DeviceStatus(ReturnCode.MemberStatusError, customer_Status, null, null, null,
					null, null, null, null, null);
		}

		try {
			// 112.1.5 Fix bug (Add prevStatus
			memSrv.addMemberStatusLog(memVO.getiDGate_ID(), customer_Status, MemberStatus.Locked,
					"Issued by " + channel);

			memVO.setCustomer_Status(MemberStatus.Locked);
			memSrv.updateMember(memVO);
		} catch (SQLException e) {
			logException(sessID, e, SV_LOCK_DEVICE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		return gts.get_DeviceStatus(ReturnCode.Success, MemberStatus.Locked, null, null, null, null, null, null, null, null);
	}



	private String svGetDeviceStatus(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_GET_DEVICE_STATUS, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String channel = map.get(CHANNEL2);
		String idgateID = map.get(IDGATE_ID);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svGetDeviceStatus] idgateID: " + (idgateID)
				+ ", channel: " + (channel));

		MembersService memSrv;
		try {
			memSrv = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_GET_DEVICE_STATUS, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_GET_DEVICE_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_GET_DEVICE_STATUS, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_GET_DEVICE_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		Device_DetailVO ddVO = null;

		try {
			memVO = memSrv.getByIdgateID(idgID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SV_GET_DEVICE_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null || ddVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
			// 111.12.8 可以跨channel驗證 ( svfGetAuthRequest, svfSendAuthResponse不檢核channel)
//		} else if (!channel.equals(memVO.getChannel_Code())) {
//			return gts.common(ReturnCode.ChannelNotMatchToMember);
		}

		HashMap<String, String> failData = new HashMap<String, String>();
		failData.put("oneClick", String.valueOf(memVO.getAuth_Fails()));
		failData.put(PIN, String.valueOf(memVO.getDigital_Fails()));
		failData.put(PATTERN, String.valueOf(memVO.getPattern_Fails()));

		HashMap<String, String> typeData = new HashMap<String, String>();
		typeData.put(BIO, ddVO.getAuth_Type().indexOf(BIO_1) > -1 ? BIO_1 : ZERO_0);
		typeData.put(PATTERN, ddVO.getAuth_Type().indexOf(PATTERN_2) > -1 ? BIO_1 : ZERO_0);
		typeData.put(PIN, ddVO.getAuth_Type().indexOf(PIN_3) > -1 ? BIO_1 : ZERO_0);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return gts.get_DeviceStatus(ReturnCode.Success, memVO.getCustomer_Status(), failData, typeData,
				ddVO.getDeviceLabel(), ddVO.getDeviceModel(), ddVO.getDevice_OS(),ddVO.getDevice_OS_Ver(), dateFormat.format(ddVO.getCreate_Date()), dateFormat.format(ddVO.getModified_Date()));
	}

	private String svfGetPubKeyNTime(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		String idgateID = null;
		String encECCData = null;
		
		// 非必填，測試 ECDH (取出idgateIDECCPubKey
		if (jSONObject.has(IDGATE_ID)) {
//			map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
			idgateID = jSONObject.getString(IDGATE_ID);
		}
		// 非必填，測試 ECDH (解出  decryptedData
		if (jSONObject.has("encECCData")) {
//			map.put("encECCData", jSONObject.getString("encECCData"));
			encECCData = jSONObject.getString("encECCData");
		}

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_GET_PUB_KEY_N_TIME, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String channel = map.get(CHANNEL2);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetPubKeyNTime] channel: " + (channel));

		ChannelService chSvc;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		ChannelVO chVO = null;
		try {
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		String serverECCPubKey = null;
		//		System.out.println("*** webpin_AppKey:" + webpin_AppKey);
//		System.out.println("if true:" + webpin_AppKey == null || "".equals(webpin_AppKey));
		if (StringUtils.isBlank(webpinAppKey)) {
			// Server’s public key
			String serverPubKey = RSA.loadPublicKey(Cfg.getExternalCfgValue(RSA_KEY_ALIAS), sessID);
			if (serverPubKey.contains(ERROR2)) {
				logException(sessID, new Exception("Loading RSA public key failed:"
						+ serverPubKey), SVF_GET_PUB_KEY_N_TIME, "Loading RSA public key failed", apLogObj, inLogObj);
				return gts.common(ReturnCode.KeyGenErr);
			}
			webpinAppKey = serverPubKey;
		}
		try {
			serverECCPubKey = ECCUtil.getOwnPublicKey(sessID, eccKeyAlias);
		} catch (NoSuchAlgorithmException e) {
			logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (Exception e) {
			logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		// 測試 ECDH (取出idgateIDECCPubKey
		String decryptedData = null;
		String encryptedData = null;
		if (idgateID != null && encECCData != null) {
			long idgID = Long.parseLong(idgateID);

			// 取回 Step2 svfSendRegResponse 存的 username
			PubkeyStoreService frontKeyStoreSVC;
			try {
				frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			PubkeyStoreVO pkStore = null;
			try {
				pkStore = frontKeyStoreSVC.getByIdgateID(idgID);
			} catch (SQLException e) {
				logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			if (pkStore == null) {
				Log4j.log.error("## ERROR ## Alias IS NULL");
				Log4j.log.error("## ERROR ## Cannot find Alias In pub_Key_Store for idgateID:" + idgateID);
				return gts.common(ReturnCode.MemberNotFound);
			}
			
			String idgateIDECCPubKey = pkStore.getPub_key_ECC();
			if (StringUtils.isNoneBlank(idgateIDECCPubKey)) {
				try {
					decryptedData = ECIES.decrypt(encECCData, idgateIDECCPubKey,
							ECCUtil.getOwnPrivateKey(sessID, eccKeyAlias));
					Log4j.log.debug("*** decryptedData:{}", decryptedData);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException
						| NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException
						| IllegalBlockSizeException | UnsupportedEncodingException e) {
					Log4j.log.error(e.getMessage());
				} catch (Exception e) {
					Log4j.log.error(e.getMessage());
				}
			} else {
				logException(sessID, new Exception("idgateIDECCPubKey is NULL") , SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			// TEST
//			String mobilePubKey = "3059301306072A8648CE3D020106082A8648CE3D030107034200043FC5366056758EA844C585C2DD41E4FA55D1E9CA3C2AAB919246E48775EE597BF19FBADA8F3FEA576F34950FD4BED39D9F762619D40203ADD40DF728508B57AD";
//			String mobilePubKey = "3059301306072A8648CE3D020106082A8648CE3D030107034200043FC5366056758EA844C585C2DD41E4FA55D1E9CA3C2AAB919246E48775EE597BF19FBADA8F3FEA576F34950FD4BED39D9F762619D40203ADD40DF728508B57AD";
//			String mobilePubKey = "3059301306072a8648ce3d020106082a8648ce3d030107034200043e10e16071e724945b1ba3b37f267ed7f12ccedf1e36649ca240dcdec6abf190ba67de7bbf8a9ccf0ec6c51bd9267898941b1d544754a241065390d0e3fa595d";
			Log4j.log.debug("*** StringUtils.isNotEmpty(mobilePubKey):{}", StringUtils.isNotEmpty(mobilePubKey));
			if (StringUtils.isNotEmpty(mobilePubKey)) {
				Log4j.log.debug("*** mobilePubKey:{}", mobilePubKey);
				try {
					encryptedData = ECIES.encrypt("TEST" + idgateID, mobilePubKey,
							ECCUtil.getOwnPrivateKey(sessID, eccKeyAlias));
					Log4j.log.debug("*** encryptedData:{}", encryptedData);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException
						| NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException
						| IllegalBlockSizeException | UnsupportedEncodingException e) {
					logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				} catch (Exception e) {
					logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, "EXCEPTION", apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			}
			
//			try { 
//				encryptedData = ECIES.encrypt("TEST" + idgateID, idgateIDECCPubKey,
//						ECCUtil.getOwnPrivateKey(sessID, eccKeyAlias));
//				Log4j.log.debug("*** encryptedData:{}", encryptedData);
//			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException
//					| NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException
//					| IllegalBlockSizeException | UnsupportedEncodingException e) {
//				logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
//				return gts.common(ReturnCode.DatabaseError);
//			} catch (Exception e) {
//				logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, "EXCEPTION", apLogObj, inLogObj);
//				return gts.common(ReturnCode.DatabaseError);
//			}
		}

		return gts.get_Pubkey(ReturnCode.Success, webpinAppKey, serverECCPubKey, decryptedData, encryptedData);
	}

	// fetch encrypted txn list
	private String svfGetTxnList(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		// 112.1.12 修改 svfGetTxnList 回應參數txnList，及輸入參數增加days, number
		map.put(DAYS2, jSONObject.getString(DAYS2));	// 查詢最近幾天紀錄 
		map.put(NUMBER2, jSONObject.getString(NUMBER2));	// 查詢幾筆數量
		// 112.1.5 Teams:svfGetTxnList不用encGetTxnListData
//		map.put("data", jSONObject.getString("encGetTxnListData"));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_GET_TXN_LIST, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		int number = Integer.parseInt(map.get(NUMBER2));
		int days = Integer.parseInt(map.get(DAYS2));
//		String encGetTxnListData = map.get("data");
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetTxnList] idgateID: " + (idgateID)
				+ ", channel: " + (channel));

		ChannelService chSvc;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		ChannelVO chVO = null;
		try {
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfGetTxnList] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInTxnGetList);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfGetTxnList] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}

		List<TxnVO> txnVoList;
		try {
			txnVoList = new Verify_RequestService(JNDI_Name, sessID).getAllTxnEncrypted(idgID, number, days);
			String chJNDI = chVO.getJNDI();
			// done:佈署環境 JBoss ?? java:jboss/datasource/iDenKeyFidoTW | java:comp/env/jdbc/
//			String lastJNDI = chJNDI.substring(chJNDI.lastIndexOf("/") + 1, chJNDI.length());
//			Log4j.log.debug("[" + sessID + "][svfGetTxnList] *** lastJNDI:{}", lastJNDI);
//			txnVoList = new Verify_DetailService("java:comp/env/jdbc/" + lastJNDI, sessID).getEncTxnData(idgID, txnVoList);
			txnVoList = new Verify_DetailService(chJNDI, sessID).getEncTxnData(idgID, txnVoList);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (IllegalBlockSizeException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, "Unable to encrypt data", apLogObj, inLogObj);
			return gts.common(ReturnCode.EncryptFailedInTxnGetList);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_LIST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		String txnVoListJson = gson.toJson(txnVoList);
		
		if (StringUtils.isBlank(webpinAppKey)) {
			// Server’s public key
			String serverPubKey = RSA.loadPublicKey(rsaKeyAlias, sessID);
			if (serverPubKey.contains(ERROR2)) {
				logException(sessID, new Exception("Loading RSA public key failed:"
						+ serverPubKey), SVF_GET_TXN_LIST, "Loading RSA public key failed", apLogObj, inLogObj);
				return gts.common(ReturnCode.KeyGenErr);
			}
			webpinAppKey = serverPubKey;
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetTxnList] Txn list: " + txnDataStr);

		return gts.get_Txn_List(ReturnCode.Success, txnVoListJson ,webpinAppKey);
	}

	private JSONObject extractJSONData(String sessID, String encData, String idgateID, int beginIndex, int endIndex,
			String deviceData, String funcName, APLogFormat apLogObj, InboundLogFormat inLogObj)
			throws DecryptFailedInSignupDeviceException, UnsupportedEncodingException, JSONErrInSignupDeviceException {
		String deviceKeyB = deviceData.substring(32, 64);

//		 Log4j.log.info("[{}][Version: {}] *** encData:[{}] ", sessID, funcName, encData);
//		Log4j.log.info("*** encData@{}:{}", funcName, encData);

		String b64DecodeData;
		try {
			b64DecodeData = new String(Base64.getUrlDecoder().decode(encData), StandardCharsets.UTF_8);
		} catch (Exception e) {
			Log4j.log.debug("*** encData:{}", encData);
			logException(sessID, e, funcName, "Unable to process encData", apLogObj, inLogObj);
			throw new DecryptFailedInSignupDeviceException(e);
		}

//		Log4j.log.debug("[{}][Version: {}] *** encDataB64decoded:[{}] ", sessID, funcName, b64DecodeData);
//		Log4j.log.debug("*** encDataB64decode:{}", encData);

		HashMap<String, String> dataMap = null;
		try {
			dataMap = gson.fromJson(b64DecodeData, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			logException(sessID, e, funcName, "Unable to parse JSON", apLogObj, inLogObj);
			throw new JSONErrInSignupDeviceException(e);
		}
		
//		Log4j.log.debug("[{}][Version: {}] *** key:[{}] ", sessID, funcName, dataMap.get("key"));
//		Log4j.log.debug("[{}][Version: {}] *** data:[{}] ", sessID, funcName, dataMap.get("data"));
//		Log4j.log.debug("*** key:[{}]", dataSet.get("key"));
//		Log4j.log.debug("*** data:[{}]", dataSet.get("data"));
		String decryptedData = null;
		try {
			String shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			String shaDevKeyB16 = shaDeviceKeyB.substring(beginIndex, endIndex);

			//			Log4j.log.debug("[" + sessID + "][" + funcName + "] *** deviceData:" + deviceData);
			//			Log4j.log.debug("[" + sessID + "][" + funcName + "] *** shaDeviceKeyB:" + shaDeviceKeyB);
			//			Log4j.log.debug("[" + sessID + "][" + funcName + "] *** deviceKeyB:" + deviceKeyB);
			//			Log4j.log.debug("[" + sessID + "][" + funcName + "] *** shaDevKeyB16:" + shaDevKeyB16);

			decryptedData = this.decryptRSA(sessID, dataMap.get(KEY), shaDevKeyB16, dataMap.get(DATA));
		} catch (UnsupportedEncodingException e) {
			logException(sessID, e, funcName, "Unable to decrypt data", apLogObj, inLogObj);
			throw new DecryptFailedInSignupDeviceException(e);
		} catch (JsonSyntaxException | NoSuchAlgorithmException | IOException e) {
			logException(sessID, e, funcName, "Unable to decrypt data", apLogObj, inLogObj);
			throw new DecryptFailedInSignupDeviceException(e);
//		} catch (JsonSyntaxException e) {
//			throw e;
//		} catch (IOException e) {
//			throw e;
//		} catch (NoSuchAlgorithmException e) {
//			throw e;
		}
			
//		Log4j.log.debug("[" + sessID + "][" + funcName + "] Decrypted data: " + decryptedData);
		// {"ReturnCode":"0000","ReturnMsg":"Success.","Data":{"msgCount":"
		if (decryptedData.indexOf("ReturnCode\":\"0000\"") == -1) {
			throw new DecryptFailedInSignupDeviceException(new Exception("Decrypt Fail"));
//			return gts.common(ReturnCode.DecryptFailedInSignupDevice);
		}

		JSONObject json = null;
		try {
			json = new JSONObject(decryptedData);
		} catch (JSONException e) {
			logException(sessID, e, funcName, "Unable to parse JSON", apLogObj, inLogObj);
			throw e;
		}

		// data decrypt failed
		if ("0030".equals(json.getString(RETURN_CODE))) {
			throw new DecryptFailedInSignupDeviceException(new Exception("Decrypt Fail"));
		}

		return json;
	}
	
	/**
	 * 取得 svfGetTxnData 交易內容
	 * 
	 * @param input
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param apLogObj
	 * @param inLogObj
	 * @return
	 */
	private String svfGetTxnData(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(ENC_TXN_ID, jSONObject.getString(ENC_TXN_ID));
		
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_GET_TXN_DATA, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		
		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String encTxnID = map.get(ENC_TXN_ID);
		long idgID = Long.parseLong(idgateID);
		
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetTxnData] idgateID:" + (idgateID)
				+ ", channel: " + (channel) + ", encTxnID: " + (encTxnID));
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfGetTxnData] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInGetTxnData);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}
		
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfGetTxnData] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		
		Verify_RequestService vrSvc;
		try {
			vrSvc = new Verify_RequestService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		byte[] b64DecodeData = null;
		try {
			b64DecodeData = Base64.getUrlDecoder().decode(encTxnID);
		} catch (IllegalArgumentException e) {
			Log4j.log.debug("*** encTxnID:{}", encTxnID);
			logException(sessID, e, SVF_GET_TXN_DATA, "Unable to process encTxnID", apLogObj, inLogObj);
			throw e;
		}
//			System.out.println("*** encTxnID:" + encTxnID);
//			System.out.println("*** b64DecodeData:" + b64DecodeData);
		
		 // encTxnID	String	AES()加密後的字串，Key = deviceKeyB[12, 27])
//		encTxnID = getEncData(rtnTxnIDStr, deviceKeyB, 12, 28);
		String deviceKeyB = ddVO.getDevice_Data().substring(32, 64);
		byte[] tempByte = null;
		String txnID = null;
			try {
				String shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
				// AES()加密後的字串，Key = deviceKeyB[14, 29]
				String shaDevKeyB16 = shaDeviceKeyB.substring(12, 28);
//			byte[] keyBytes = AESUtil.SHA256_To_Bytes(shaDevKeyB16); // CF91B621D9B7CF95
				byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
//            Log4j.log.debug("*** deviceKeyB: {}", deviceKeyB);
//			Log4j.log.debug("*** shaDevKeyB16: {}", shaDevKeyB16);	// 你截斷的部分是長這樣嗎？ CF91B621D9B7CF95
				byte[] keyBytes = shaDevKeyB16.getBytes(StandardCharsets.UTF_8);
				tempByte = AESUtil.decrypt(ivBytes, keyBytes, b64DecodeData);
				txnID = new String(tempByte, StandardCharsets.UTF_8);
			} catch (InvalidKeyException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "InvalidKeyException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			} catch (NoSuchAlgorithmException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "NoSuchAlgorithmException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			} catch (UnsupportedEncodingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "UnsupportedEncodingException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			} catch (NoSuchPaddingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "NoSuchPaddingException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			} catch (InvalidAlgorithmParameterException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "InvalidAlgorithmParameterException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			} catch (IllegalBlockSizeException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "IllegalBlockSizeException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			} catch (BadPaddingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "BadPaddingException", apLogObj, inLogObj);
				return gts.common(ReturnCode.HashGenFailedInGetTxnData);
			}
			
//			try {
//		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | NoSuchPaddingException
//				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
//			logException(sessID, e, SVF_GET_TXN_DATA, "HashGenFailedInGetTxnData", apLogObj, inLogObj);
//			return gts.common(ReturnCode.HashGenFailedInGetTxnData);
//		}
		
		String requestID = txnID.substring(8);
		String transationDate = txnID.substring(0, 8);
		Verify_RequestVO vrVO = null;
		Verify_DetailVO vdVO = null;
		String encTxnData;
		String encAuthReq;
		//		Log4j.log.debug("[" + sessID + "][svfGetTxnData] *** request_ID: " + requestID);
		try {
			vrVO = vrSvc.getOneWithTime(idgID, requestID, null);
			if (vrVO == null) {
				return gts.common(ReturnCode.TxnNotFoundInGetTxnData);
				// 應該要不檢查01，直接回authStatus
//			} else if (!TxnStatus.WaitForVerify.equals(vrVO.getStatus_Code())) {
//				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
//						+ "][svfGetTxnData] Request rejected. Current Txn status: " + vrVO.getStatus_Code());
////				    "returnCode": "0857", "returnMsg": "交易狀態錯誤"
//				return gts.common(ReturnCode.TxnStatusErrInTxnCancelTxn);
			}
			
			ChannelVO chVO = null;
			try {
				chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(vrVO.getChannel_Code());
				if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
					return gts.common(ReturnCode.ChannelInvalidError);
				}
//			chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(channel);
				// Log4j.log.info("[{}] *** after getOneChannel@{}: {}ms \r\n", sessID, "svfSendAuthResponse", new SimpleDateFormat("mm:ss.SSS").format(new Date()));
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			String chJNDI = chVO.getJNDI();
			Verify_DetailService vdSvc = new Verify_DetailService(chJNDI, sessID);
			vdVO = vdSvc.getOneVerify_Detail(idgID, requestID, transationDate);
			if (vdVO == null) {
				return gts.common(ReturnCode.TxnNotFoundInGetTxnData);
				// TODO UNREMARK BELOW 2 lines 111.7.27 交易狀態錯誤
			}
			
			encTxnData = vdVO.getTransaction_Data();
			encAuthReq = vdVO.getEncAuthReq();
			
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_TXN_DATA, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (StringUtils.isBlank(webpinAppKey)) {
			// Server’s public key
			String serverPubKey = RSA.loadPublicKey(rsaKeyAlias, sessID);
			if (serverPubKey.contains(ERROR2)) {
				logException(sessID, new Exception("Loading RSA public key failed:"
						+ serverPubKey), SVF_GET_TXN_DATA, "Loading RSA public key failed", apLogObj, inLogObj);
				return gts.common(ReturnCode.KeyGenErr);
			}
			webpinAppKey = serverPubKey;
		}
		
		return gts.get_TxnData(ReturnCode.Success, webpinAppKey, encTxnData, encAuthReq, txnID, vrVO.getStatus_Code(), vrVO.getTransaction_Name());
	}

	/**
	 * 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid
	 * @param channel
	 * @param memVO
	 * @return
	 */
	private boolean isEqualChannel2Member(String channel, MembersVO memVO) {
		return !channel.equals(memVO.getChannel_Code());
	}
	
	/**
	 * 取得 authStatus	裝置狀態資訊
	 * 
	 * @param input
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param apLogObj
	 * @param inLogObj
	 * @return
	 */
	private String svGetAuthStatus(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(TXN_ID, jSONObject.getString(TXN_ID));
		
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_GET_AUTH_STATUS, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		
		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String txnID = map.get(TXN_ID);
		long idgID = Long.parseLong(idgateID);
		
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svGetAuthStatus] idgateID:" + (idgateID)
				+ ", channel: " + (channel) + ", txnID: " + (txnID));
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svGetAuthStatus] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInGetAuthStatus);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}
		
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svGetAuthStatus] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		
		Verify_RequestService vrSvc;
		try {
			vrSvc = new Verify_RequestService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Verify_RequestVO vrVO = null;
		String requestID = txnID.substring(8);
//		Log4j.log.debug("[" + sessID + "][svGetAuthStatus] *** request_ID: " + requestID);
		try {
			vrVO = vrSvc.getOneWithTime(idgID, requestID, null);
		} catch (SQLException e) {
			logException(sessID, e, SV_GET_AUTH_STATUS, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (vrVO == null) {
			return gts.common(ReturnCode.TxnNotFoundInGetAuthStatus);
			// 應該要不檢查01，直接回authStatus
//		} else if (!TxnStatus.WaitForVerify.equals(vrVO.getStatus_Code()) && !TRUE.equals(IDGateConfig.testMode)) {
//			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
//					+ "][svGetAuthStatus] Request rejected. Current Txn status: " + vrVO.getStatus_Code());
////			    "returnCode": "0857", "returnMsg": "交易狀態錯誤"
//			return gts.common(ReturnCode.TxnStatusErrInTxnCancelTxn);
		}
		
		if (getTimeInterval(vrVO) > Txn_Timeout) {
			String authStatus = vrVO.getStatus_Code();
			try {
				vrSvc.updateTxnStatusWithTime(idgID,
						Long.parseLong(txnID.substring(8)),
						txnID.substring(0, 8), TxnStatus.Timeout,
						authStatus, ReturnCode.TxnTimeoutInSendAuthResponse);
				// 112.3.21 驗證交易超過時限(5分鐘)，回應逾時 04
				return gts.get_AuthStatus(ReturnCode.Success, TxnStatus.Timeout);
			} catch (RuntimeException e) {
				Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfSendAuthResponse] DB warning: " + e);
				inLogObj.setThrowable(e.getMessage());
				
				return gts.common(ReturnCode.RequestExpired);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}
		
		return gts.get_AuthStatus(ReturnCode.Success, vrVO.getStatus_Code());
	}

	private String svfDecryptECDH(String input, HttpServletResponse res, GsonToString gts, String sessID,
								  APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(ENC_PLAIN_DATA, jSONObject.getString(ENC_PLAIN_DATA));

		// variable check
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_DECRYPT_ECDH, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		final String channel = map.get(CHANNEL2);
		final String idgateID = map.get(IDGATE_ID);
		final String encPlainData = map.get(ENC_PLAIN_DATA);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfDecryptECDH] channel: "
				+ channel + ", idgateID: " + idgateID + ", encPlainData: " + encPlainData);

		final long idgID;
		ChannelService chSvc;
		ChannelVO chVO = null;
		MembersService memSvc;
		MembersVO memVO = null;

		try {
			// convert to long type
			idgID = Long.parseLong(idgateID);

			// channel check
			chSvc = new ChannelService(JNDI_Name, sessID);
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}

			// member status check
			memSvc = new MembersService(JNDI_Name, sessID);
			memVO = memSvc.getByIdgateID(idgID);

			if (memVO == null) {
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfDecryptECDH] Didn't find the member data for this ID: " + idgateID);
				return gts.common(ReturnCode.MemberNotFound);
			} else if (isEqualChannel2Member(channel, memVO)) {
				return gts.common(ReturnCode.MemberChannelInvalidInDecryptECDH);
			} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
				return gts.common(ReturnCode.MemberStatusError);
			}

		} catch (SQLException e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		PubkeyStoreService frontKeyStoreSVC;
		PubkeyStoreVO pkStore = null;
		String decryptedData = null;
		// decrypt data
		try {
			frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
			pkStore = frontKeyStoreSVC.getByIdgateID(idgID);

			if(pkStore == null){
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfDecryptECDH] Cannot find the correspond ECDH key to this user.");
				return gts.common(ReturnCode.MemberKeyNotFoundInDecryptECDH);
			}

			decryptedData = ECIES.decrypt(encPlainData, pkStore.getPub_key_ECC(),
					ECCUtil.getOwnPrivateKey(sessID, eccKeyAlias));

		} catch (SQLException e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}  catch (Exception e) {
			logException(sessID, e, SVF_DECRYPT_ECDH, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptErrInDecryptECDH);
		}

		return gts.decrypt_ECDH(ReturnCode.Success, decryptedData);
	}

	private String svfEncryptECDH(String input, HttpServletResponse res, GsonToString gts, String sessID,
								  APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(PLAIN_DATA, jSONObject.getString(PLAIN_DATA));

		// variable check
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_ENCRYPT_ECDH, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		final String channel = map.get(CHANNEL2);
		final String idgateID = map.get(IDGATE_ID);
		final String encPlainData = map.get(PLAIN_DATA);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfEncryptECDH] channel: "
				+ channel + ", idgateID: " + idgateID + ", encPlainData: " + encPlainData);

		final long idgID;
		ChannelService chSvc;
		ChannelVO chVO = null;
		MembersService memSvc;
		MembersVO memVO = null;

		try {
			// convert to long type
			idgID = Long.parseLong(idgateID);

			// channel check
			chSvc = new ChannelService(JNDI_Name, sessID);
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}

			// member status check
			memSvc = new MembersService(JNDI_Name, sessID);
			memVO = memSvc.getByIdgateID(idgID);

			if (memVO == null) {
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfEncryptECDH] Didn't find the member data for this ID: " + idgateID);
				return gts.common(ReturnCode.MemberNotFound);
			} else if (isEqualChannel2Member(channel, memVO)) {
				return gts.common(ReturnCode.MemberChannelInvalidInEncryptECDH);
			} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
				return gts.common(ReturnCode.MemberStatusError);
			}

		} catch (SQLException e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		PubkeyStoreService frontKeyStoreSVC;
		PubkeyStoreVO pkStore = null;
		String encryptedData = null;
		// encrypt data
		try {
			frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
			pkStore = frontKeyStoreSVC.getByIdgateID(idgID);

			if(pkStore == null){
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfEncryptECDH] Cannot find the correspond ECDH key to this user.");
				return gts.common(ReturnCode.MemberKeyNotFoundInEncryptECDH);
			}

			encryptedData = ECIES.encrypt(encPlainData, pkStore.getPub_key_ECC(),
					ECCUtil.getOwnPrivateKey(sessID, eccKeyAlias));

		} catch (SQLException e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}  catch (Exception e) {
			logException(sessID, e, SVF_ENCRYPT_ECDH, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptErrInEncryptECDH);
		}

		return gts.encrypt_ECDH(ReturnCode.Success, encryptedData);
	}
	
//	cancel txn -f 
	private String svCancelAuth(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(TXN_ID, jSONObject.getString(TXN_ID));
		
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_CANCEL_AUTH, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		
		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String txnID = map.get(TXN_ID);
		long idgID = Long.parseLong(idgateID);
		
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svCancelAuth] idgateID:" + (idgateID)
				+ ", channel: " + (channel) + ", txnID: " + (txnID));
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_CANCEL_AUTH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SV_CANCEL_AUTH, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svCancelAuth] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInTxnCancelTxn);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}
		
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_CANCEL_AUTH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SV_CANCEL_AUTH, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svCancelAuth] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		
		
		Verify_RequestService vrSvc;
		try {
			vrSvc = new Verify_RequestService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_CANCEL_AUTH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Verify_RequestVO vrVO = null;
		String requestID = txnID.substring(8);
//		Log4j.log.debug("[" + sessID + "][svCancelAuth] *** request_ID: " + requestID);
		try {
			vrVO = vrSvc.getOneWithTime(idgID, requestID, null);
		} catch (SQLException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		String authStatus;
		if (vrVO != null) {
			authStatus = vrVO.getStatus_Code();
			if (isNotWaitForVerify(authStatus)) {
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfCancelAuth] Request rejected. Current Txn status: " + authStatus);
//				    "returnCode": "0857", "returnMsg": "交易狀態錯誤"
				return gts.common(ReturnCode.TxnStatusErrInTxnCancelTxn);
			}
		} else {
			return gts.common(ReturnCode.TxnNotFoundInTxnCancelTxn);
		}
		
		try {
			if (getTimeInterval(vrVO) > Txn_Timeout) {
				vrSvc.updateTxnStatusWithTime(idgID, Long.parseLong(requestID),
						 txnID.substring(0, 8), TxnStatus.Timeout,
						authStatus, ReturnCode.TxnTimeoutInTxnCancelTxn);
//				"returnCode": "0858", "returnMsg": "交易逾時"
				return gts.common(ReturnCode.TxnTimeoutInTxnCancelTxn);
			} else {
				// 取消交易成功
				vrSvc.updateTxnStatusWithTime(idgID, Long.parseLong(requestID),
						 txnID.substring(0, 8), TxnStatus.Cancel,
						authStatus, ReturnCode.Success);
			}
		} catch (RuntimeException e) {
			Log4j.log.warn(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svCancelAuth] DB warning: " + e);
			inLogObj.setThrowable(e.getMessage());
			
			return gts.common(ReturnCode.RequestExpired);
		} catch (SQLException e) {
			logException(sessID, e, SV_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		return gts.get_AuthStatus(ReturnCode.Success, authStatus);
//		return gts.common(ReturnCode.Success);
	}

//	cancel txn
	private String svfCancelAuth(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(DATA, jSONObject.getString("encCancelAuthData"));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_CANCEL_AUTH, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String encCancelAuthData = map.get(DATA);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfCancelAuth] idgateID:" + (idgateID)
				+ ", channel: " + (channel) + ", encCancelAuthData: " + (encCancelAuthData));

		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid

		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;

		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfCancelAuth] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInTxnCancelTxn);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfCancelAuth] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}

//		Log4j.log.debug("[" + sessID + "][svfCancelAuth] *** encCancelAuthData:{}", encCancelAuthData);

		// decrypt data
		JSONObject json = null;
		String deviceData = ddVO.getDevice_Data();
		try {
			json = extractJSONData(sessID, encCancelAuthData, idgateID, 8, 24, deviceData, SVF_CANCEL_AUTH, apLogObj, inLogObj);
		} catch (UnsupportedEncodingException e) {
			return gts.common(ReturnCode.DecryptFailedInTxnCancelTxn);
		} catch (DecryptFailedInSignupDeviceException e) {
			return gts.common(ReturnCode.DecryptFailedInTxnCancelTxn);
		} catch (JSONErrInSignupDeviceException e) {
			return gts.common(ReturnCode.JSONErrInTxnCancelTxn);
		}

		String idgateIDHash = null;
		try {
			idgateIDHash = AESUtil.SHA256_To_HexString(idgateID);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInTxnCancelTxn);
		}

		JSONObject jsonCancelAuthData;
		String msgCount;
		Object iDGateId;
		String txnId;
		try {
			jsonCancelAuthData = json.getJSONObject(DATA2);
			msgCount = jsonCancelAuthData.getString(MSG_COUNT);
			iDGateId = jsonCancelAuthData.getString(IDGATE_ID);
			txnId = jsonCancelAuthData.getString(TXN_ID);
		} catch (JSONException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, "JSONErrInTxnCancelTxn", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInTxnCancelTxn);
		}
		
		if (!idgateIDHash.equals(iDGateId)) {
//			"returnCode": "0854", "returnMsg": "會員雜湊無效"
			return gts.common(ReturnCode.MemberHashInvalidInTxnCancelTxn);
			// TODO UNREMARK BELOW
		} else if (memVO.getMsg_Count() >= Long.parseLong(msgCount)) {
//			"returnCode": "0853", "returnMsg": "訊息無效"
			return gts.common(ReturnCode.MsgCountInvalidInTxnCancelTxn);
		}

		try {
			memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));
		} catch (SQLException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		Verify_RequestService vrSvc;
		try {
			vrSvc = new Verify_RequestService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Verify_RequestVO vrVO = null;
		String requestID = txnId.substring(8);
//		Log4j.log.debug("[" + sessID + "][svfCancelAuth] *** request_ID: " + requestID);
		try {
			vrVO = vrSvc.getOneWithTime(idgID, requestID, null);
//			vrVO = vrSvc.getOneWithTime(idgID, json.getJSONObject("Data").getString("txnID").substring(8),
//					json.getJSONObject("Data").getString("txnID").substring(0, 8));
		} catch (SQLException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		String authStatus;
		if (vrVO != null) {
			authStatus = vrVO.getStatus_Code();
			if (isNotWaitForVerify(authStatus)) {
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfCancelAuth] Request rejected. Current Txn status: " + authStatus);
//				    "returnCode": "0857", "returnMsg": "交易狀態錯誤"
				return gts.common(ReturnCode.TxnStatusErrInTxnCancelTxn);
			}
		} else {
			return gts.common(ReturnCode.TxnNotFoundInTxnCancelTxn);
		}

		try {
			if (getTimeInterval(vrVO) > Txn_Timeout) {
				vrSvc.updateTxnStatusWithTime(idgID, Long.parseLong(requestID),
						txnId.substring(0, 8), TxnStatus.Timeout,
						authStatus, ReturnCode.TxnTimeoutInTxnCancelTxn);
//				"returnCode": "0858", "returnMsg": "交易逾時"
				return gts.common(ReturnCode.TxnTimeoutInTxnCancelTxn);
			} else {
				// 取消交易成功
				vrSvc.updateTxnStatusWithTime(idgID, Long.parseLong(requestID),
						txnId.substring(0, 8), TxnStatus.Cancel,
						authStatus, ReturnCode.Success);
			}
		} catch (RuntimeException e) {
			Log4j.log.warn(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfCancelAuth] DB warning: " + e);
			inLogObj.setThrowable(e.getMessage());
			
			return gts.common(ReturnCode.RequestExpired);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CANCEL_AUTH, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		return gts.get_AuthStatus(ReturnCode.Success, authStatus);
//		return gts.common(ReturnCode.Success);
	}

	private long getTimeInterval(Verify_RequestVO vrVO) {
		return vrVO.getDB_Time().getTime() - vrVO.getTransaction_Date().getTime();
	}

	/**
	 * 是否 Request內的 Channel 有在 Table Channel
	 * 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid
	 * @param gts
	 * @param sessID
	 * @param apLogObj
	 * @param inLogObj
	 * @param channel
	 * @return
	 * @throws UnknownHostException
	 * @throws NamingException
	 * @throws SQLException
	 */
	private boolean isChannelValid(String sessID, String channel)
			throws UnknownHostException, NamingException, SQLException {
//		Log4j.log.debug("*** ChannelService.JNDI_Name:" + JNDI_Name);
		ChannelService chSvc = new ChannelService(JNDI_Name, sessID);
		ChannelVO chVO = chSvc.getOneChannel(channel);
		if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
			return false;
		}
		
		return true;
	}

//	return server time
	private String svfSyncTime(String input, HttpServletResponse res, GsonToString gts, String sessID, APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSyncTime] " + map.get(ERROR));
			return gts.common(ReturnCode.ParameterError);
		}

		String channel = map.get(CHANNEL2);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSyncTime] channel: " + (channel));

		ChannelService chSvc;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, "svfSyncTime", UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, "svfSyncTime", DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		ChannelVO chVO = null;

		try {
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (SQLException e) {
			logException(sessID, e, "svfSyncTime", DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		return gts.sync_Time(ReturnCode.Success);
	}

//	change settings with access token
	private String svfChangeSetting(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(DATA, jSONObject.getString("encChangeSettingData"));
		map.put(SESSION, jSONObject.getString("changeSettingSession"));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_CHANGE_SETTING, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String encChangeSettingData = map.get(DATA);
		String changeSettingSession = map.get(SESSION);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting] idgateID:" + (idgateID)
				+ ", channel: " + (channel) + ", encChangeSettingData: " + (encChangeSettingData)
				+ ", changeSettingSession: " + changeSettingSession);

		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid

		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;

		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		String prevStatus = null;
		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfChangeSetting] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} 
		prevStatus = memVO.getCustomer_Status();
		if (prevStatus.equals(MemberStatus.Locked)
				|| prevStatus.equals(MemberStatus.LockedForTooManyAuthFails)) {
			return gts.common(ReturnCode.MemberLockedError);
		} else if (prevStatus.equals(MemberStatus.Deleted)) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else if (!prevStatus.equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInChangeSetting);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;

		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfChangeSetting] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}

//		Log4j.log.debug("[" + sessID + "][svfChangeSetting] *** encChangeSettingData:{}", encChangeSettingData);

		// decrypt data
		JSONObject json = null;
		String deviceData = ddVO.getDevice_Data();
		String deviceKeyB = deviceData.substring(32, 64);
		String shaDeviceKeyB = null;
		try {
			shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			json = extractJSONData(sessID, encChangeSettingData, idgateID, 7, 23, deviceData, SVF_CHANGE_SETTING, apLogObj, inLogObj);
		} catch (UnsupportedEncodingException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, "Unsupported Encoding Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInChangeSetting);
		} catch (DecryptFailedInSignupDeviceException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, "Decrypt Failed InSignupDevice Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInChangeSetting);
		} catch (JSONErrInSignupDeviceException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, "JSONErr InSignupDevice Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInChangeSetting);
		} catch (NoSuchAlgorithmException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, "HashGenFailedInChangeSetting", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInChangeSetting);
		}

		String idgateIDHash = null;
		try {
			idgateIDHash = AESUtil.SHA256_To_HexString(idgateID);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInChangeSetting);
		}

		JSONObject jsonChangeSettingData;
		String iDGateId;
		String msgCount;
		String authType;
		String token;
		String authHash;
		String otp;
		String time;
		try {
			jsonChangeSettingData = json.getJSONObject(DATA2);
			iDGateId = jsonChangeSettingData.getString(IDGATE_ID);
			msgCount = jsonChangeSettingData.getString(MSG_COUNT);
			authType = jsonChangeSettingData.getString(AUTH_TYPE);
			authHash = jsonChangeSettingData.getString(AUTH_HASH);
			token = jsonChangeSettingData.getString("token"); // check setting成功時的token
			otp = jsonChangeSettingData.getString(OTP2);
			time = jsonChangeSettingData.getString(TIME);
		} catch (JSONException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, "JSONErrInChangeSetting", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInChangeSetting);
		}
		
		if (!idgateIDHash.equals(iDGateId)) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting] ID hash: " + idgateIDHash
					+ " not match to the client's packed hash ID: " + iDGateId);

			return gts.common(ReturnCode.MemberHashInvalidInChangeSetting);
		} else if (memVO.getMsg_Count() >= Long.parseLong(msgCount)) {
			return gts.common(ReturnCode.MsgCountInvalidInChangeSetting);
		}

		try {
			memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		// check if this auth type is not part of active auth type
		if (ddVO.getAuth_Type().indexOf(authType) == -1) {
			return gts.common(ReturnCode.NotAnActiveAuthInChangeSetting);
		}

		// search active record
		SMS_CountService smsSvc;
		try {
			smsSvc = new SMS_CountService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		SMS_CountVO smsVO = null;
		try {
			if (BIO_1.equals(authType)) {
				smsVO = smsSvc.find_Active_Record(idgID, SmsOperation.BioTypeChangeSetting);
			} else if (PATTERN_2.equals(authType)) {
				smsVO = smsSvc.find_Active_Record(idgID, SmsOperation.DiagramTypeChangeSetting);
			} else if (PIN_3.equals(authType)) {
				smsVO = smsSvc.find_Active_Record(idgID, SmsOperation.PinTypeChangeSetting);
			} else {
				return gts.common(ReturnCode.UnknownAuthTypeInChangeSetting);
			}

			if (smsVO == null) {
				return gts.common(ReturnCode.RequestInvalidInChangeSetting);
			} else if (smsVO.getId() != Integer.parseInt(token) || !smsVO.getStatus().equals(SmsStatus.Enable)) {
				return gts.common(ReturnCode.TokenInvalidInChangeSetting);
			}

			// Will update this record to finish state
			smsSvc.update_Status(smsVO.getId(), SmsStatus.Success);

		} catch (SQLException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		// otp verification
		int failCount = memVO.getPattern_Fails() > memVO.getDigital_Fails() ? memVO.getPattern_Fails()
				: memVO.getDigital_Fails();
		String returnCode = null;
		HashMap<String, String> tsMsg = null;
		Map<String, String> paramMap = new HashMap<>();
//		List<NameValuePair> formparams = new ArrayList<>();
		try {
//			if (type.equals("3")) {
//				paramMap.put("Method", "verify_OTP");
//			} else {
//				paramMap.put("Method", "verifyEsnOTP");
//			}
			paramMap.put(METHOD, VERIFY_OTP);

			paramMap.put(CHANNEL3, memVO.getChannel_Code());
//			paramMap.put("ESN", ddVO.getESN());
			// seed = deviceKeyB[24, 63]
			paramMap.put(ESN, shaDeviceKeyB.substring(24, 64));	
			paramMap.put(OTP3, otp);
			paramMap.put(TIME_STAMP, time);
			// sha256(msgCoung + changeSettingSession) // msgCoung由SDK傳上來
			paramMap.put(CHALLENGE, AESUtil.SHA256_To_HexString(msgCount + changeSettingSession));	
			paramMap.put(TXN_DATA, AESUtil.SHA256_To_HexString(idgateID + changeSettingSession));

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting][verify_OTP] Sending following msg to TS: " + gson.toJson(paramMap));

//			returnCode = cvnVerifier.doSelect(paramMap.get("Method"), paramMap, sessID);
			returnCode = TS_Inst.verifyOTP(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(ESN)),
					CvnLib.Common.Filter.filter((String) paramMap.get(CHALLENGE)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TXN_DATA)),
					CvnLib.Common.Filter.filter((String) paramMap.get(OTP3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TIME_STAMP)), sessID);

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting][verify_OTP] Reply from TS: " + returnCode);

			tsMsg = gson.fromJson(returnCode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException | IOException | NoSuchAlgorithmException | JSONException e) {
			logException(sessID, e, SVF_CHANGE_SETTING, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.TrustServerError);
		}

		if (SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
			// success
			try {
				if (PIN_3.equals(authType)) {
					memSvc.updateDigitalFailCounter(idgID, 0);
				} else if (PATTERN_2.equals(authType)) {
					memSvc.updatePatternFailCounter(idgID, 0);
				}
			} catch (SQLException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} else if (FAIL_0020.equals(tsMsg.get(RETURN_CODE))) {
			// otp invalid
			try {
				// verification fail counter have reached limit, lock account
				if (PATTERN_2.equals(authType) && memVO.getPattern_Fails() + 1 >= Pattern_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PATTERN_2, ""), ddVO.getAuth_Type());
					memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.Normal,
							"Auth type 2 disabled for reached failure limit");

					return gts.get_FailCount(ReturnCode.TooManyAuthFailsInChangeSetting,
							String.valueOf(memVO.getPattern_Fails() + 1), null);

				} else if (PIN_3.equals(authType) && memVO.getDigital_Fails() + 1 >= Digital_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PIN_3, ""), ddVO.getAuth_Type());
					memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.LockedForTooManyAuthFails,
							"Devuce locked for reached failure limit of digital");
					memVO.setCustomer_Status(MemberStatus.LockedForTooManyAuthFails);
					memSvc.updateMember(memVO);

					return gts.get_FailCount(ReturnCode.LockedForTooMuchFail,
							String.valueOf(memVO.getDigital_Fails() + 1), null);
				}

			} catch (RuntimeException e) {
				Log4j.log.warn(
						"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting] DB warning: " + e);
				inLogObj.setThrowable(e.getMessage());

				return gts.common(ReturnCode.RequestExpired);
			} catch (SQLException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}

			return gts.get_FailCount(ReturnCode.OTPInvalidInChangeSetting, String.valueOf(failCount + 1), null);
		} else if (FAIL_0001.equals(tsMsg.get(RETURN_CODE)) || FAIL_0002.equals(tsMsg.get(RETURN_CODE))) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else {
			return gts.common(ReturnCode.TrustServerError);
		}

		if (BIO_1.equals(authType)) {
			try {
				ddSvc.update_Bio_Hash(idgID, authHash);
			} catch (SQLException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} else if (PATTERN_2.equals(authType)) {
			// 檢查圖形鎖與密碼鎖與上次相同
			if (ddVO.getPattern_Hash().equals(authHash)) {
				return gts.common(ReturnCode.PatternKeyIsSameInChangeSetting);
			}

			try {
				ddSvc.update_Pattern_Hash(idgID, authHash);
			} catch (SQLException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} else if (PIN_3.equals(authType)) {
			// 檢查圖形鎖與密碼鎖與上次相同
			if (ddVO.getDigital_Hash().equals(authHash)) {
				return gts.common(ReturnCode.DigitalKeyIsSameInChangeSetting);
			}

			/* TS 創建新Persofile */
			paramMap.clear();
			paramMap.put(METHOD, SIGNUP_PERSO);
			paramMap.put(CHANNEL3, channel);
			paramMap.put(DEV_TYPE, "4");
			paramMap.put(USER_ID, String.valueOf(System.currentTimeMillis()));
			paramMap.put(SEED_SECRET, "");
			paramMap.put(ESN_SECRET, "");
			paramMap.put(MASTER_SECRET, "");
			paramMap.put(DEV_DATA, ddVO.getDevice_Data());
			paramMap.put(PIN_HASH, authHash);

			String retMsg;
			//				retMsg = cvnVerifier.doSelect(paramMap.get("Method"), paramMap, sessID);
			retMsg = TS_Inst.signupPersoFIDO(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(DEV_TYPE)),(String) paramMap.get(ESN_SECRET),
					(String) paramMap.get(SEED_SECRET), (String) paramMap.get(MASTER_SECRET),
					CvnLib.Common.Filter.filter((String) paramMap.get(USER_ID)),
					CvnLib.Common.Filter.filter((String) paramMap.get(DEV_DATA)),
					CvnLib.Common.Filter.filter((String) paramMap.get(PIN_HASH)), sessID);

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting][signup_Perso] Reply from TS: " + retMsg);

			tsMsg = gson.fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
			}.getType());

			if (!SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
				return gts.common(ReturnCode.TrustServerError);
			}

			try {
				ddSvc.prestore_New_Esn(idgID, tsMsg.get(ESN), authHash);
			} catch (SQLException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}

			// pass (svfConfirm_Perso)
			try {
				// update digital fail counter to 0 and refresh ESN
				memSvc.updateDigitalFailCounter(idgID, 0);
				ddSvc.update_To_New_ESN(idgID);

				// turn on/off auth type

				// reset AB counter to 0
				ddSvc.update_AB_Count(idgID, 0);

				memSvc.addMemberStatusLog(idgID, prevStatus, prevStatus,
						"Persofile update successful");

			} catch (RuntimeException e) {
				Log4j.log.warn(
						"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting] DB warning: " + e);
				inLogObj.setThrowable(e.getMessage());

				return gts.common(ReturnCode.RequestExpired);
			} catch (SQLException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}

			HashMap<String, String> persoData = new HashMap<String, String>();
//			persoData.put("AB", tsMsg.get("Mercury"));
//			persoData.put("perso", tsMsg.get("PersoFile"));
			persoData.put(TIME, String.valueOf(System.currentTimeMillis()));

			retMsg = gson.toJson(persoData);

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfChangeSetting] Raw perso data before encryption: " + retMsg);

			byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] keyBytes = null;
			try {
				keyBytes = AESUtil.SHA256_To_Bytes(ddVO.getDevice_Data().substring(11, 27));
				retMsg = Base64.getUrlEncoder()
						.encodeToString(AESUtil.encrypt(ivBytes, keyBytes, retMsg.getBytes(StandardCharsets.UTF_8)));
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e) {
				logException(sessID, e, SVF_CHANGE_SETTING, "Unable to encrypt data", apLogObj, inLogObj);
				return gts.common(ReturnCode.EncryptFailedInChangeSetting);
			}

			return gts.change_Setting(ReturnCode.Success, retMsg);
		}

		return gts.common(ReturnCode.Success);
	}

//	check old settings before allow to change it, and return a access token to
//	use change setting
	private String svfCheckSetting(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(DATA, jSONObject.getString(ENC_SETTING_DATA));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_CHECK_SETTING, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String encSettingData = map.get(DATA);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfCheckSetting] idgateID:" + (idgateID)
				+ ", channel: " + (channel) + ", encSettingData: " + (encSettingData));

		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid

		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHECK_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_CHECK_SETTING, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfCheckSetting] Didn't find the member data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		} 
		String prevStatus = memVO.getCustomer_Status();
		if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInCheckSetting);
		} else if (prevStatus.equals(MemberStatus.Locked)
				|| prevStatus.equals(MemberStatus.LockedForTooManyAuthFails)) {
			return gts.common(ReturnCode.MemberLockedError);
		} else if (prevStatus.equals(MemberStatus.Deleted)) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else if (!prevStatus.equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHECK_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_CHECK_SETTING, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfCheckSetting] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}

//		Log4j.log.debug("[" + sessID + "][svfCheckSetting] *** encSettingData:{}", encSettingData);

		// decrypt data
		JSONObject json = null;
		String deviceData = ddVO.getDevice_Data();
		String deviceKeyB = deviceData.substring(32, 64);
		String shaDeviceKeyB = null;
		try {
			shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			json = extractJSONData(sessID, encSettingData, idgateID, 5, 21, deviceData, SVF_CHECK_SETTING, apLogObj, inLogObj);
		} catch (NoSuchAlgorithmException | JSONErrInSignupDeviceException | UnsupportedEncodingException | DecryptFailedInSignupDeviceException e) {
			logException(sessID, e, SVF_CHECK_SETTING, "HashGenFailedInCheckSetting", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInCheckSetting);
		}

		String idgateIDHash = null;
		try {
			idgateIDHash = AESUtil.SHA256_To_HexString(idgateID);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_CHECK_SETTING, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInCheckSetting);
		}

		JSONObject jsonSettingData;
		String msgCount;
		String iDGateId;
		String settingAuthType;
		String settingTime;
		String settingAuthHash;
		String settingOTP;
		try {
			jsonSettingData = json.getJSONObject(DATA2);
			msgCount = jsonSettingData.getString(MSG_COUNT);
			iDGateId = jsonSettingData.getString(IDGATE_ID);
			settingAuthType = jsonSettingData.getString(AUTH_TYPE);
			settingTime = jsonSettingData.getString(TIME);
			settingAuthHash = jsonSettingData.getString(AUTH_HASH);
			settingOTP = jsonSettingData.getString(OTP2);
		} catch (JSONException e) {
			logException(sessID, e, SVF_CHECK_SETTING, "JSONErrInCheckSetting", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInCheckSetting);
		}
		
		if (!idgateIDHash.equals(iDGateId)) {
			return gts.common(ReturnCode.MemberHashInvalidInCheckSetting);
		} else if (memVO.getMsg_Count() >= Long.parseLong(msgCount) && !TRUE.equals(IDGateConfig.testMode)) {
			return gts.common(ReturnCode.MsgCountInvalidInCheckSetting);
		}

		try {
			memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		long time = System.currentTimeMillis();
		long recvTime = Long.parseLong(settingTime);
		// check time is still valid
		if ((time - recvTime) > OTP_Timeout && !TRUE.equals(IDGateConfig.testMode)) {
			return gts.common(ReturnCode.TimeoutInCheckSetting);
		}
		
		Log4j.log.debug("[{}][Version: {}][svfCheckSetting] *** authType:{} NOT contain {} is {}", sessID,
				IDGateConfig.svVerNo, ddVO.getAuth_Type(), settingAuthType,
				ddVO.getAuth_Type().indexOf(settingAuthType) == -1);

		// check if this auth type is not part of active auth type
		if (ddVO.getAuth_Type().indexOf(settingAuthType) == -1) {
			return gts.common(ReturnCode.NotAnActiveAuthInCheckSetting);
		}
		
		List<Blocked_Device_AuthVO2> channelAndModelList = new ArrayList<>();
		try {
			String upperLabel = ddVO.getDeviceLabel().toUpperCase();
			String upperModel = ddVO.getDeviceModel().toUpperCase();
			channelAndModelList = new Blocked_Device_AuthService(JNDI_Name, sessID).getList2(upperLabel, upperModel, settingAuthType, memVO.getChannel_Code());

			if (!channelAndModelList.isEmpty()) {
				return gts.common(ReturnCode.BlockedAuthTypeInCheckSetting);
			}

			// TODO
//		failCount	String	當前驗證錯誤次數：”1”
			// 111.7.14 先不檢查
			// check authType hash is the same
//			if ("1".equals(type) && !ddVO.getBio_Hash().equals(json.getJSONObject("Data").getString("authHash"))) {
//				return gts.common(ReturnCode.BioHashInvalidInCheckSetting);
//			} else 
			if (PATTERN_2.equals(settingAuthType) && !ddVO.getPattern_Hash().equals(settingAuthHash)) {
				memSvc.updatePatternFailCounter(idgID, memVO.getPattern_Fails() + 1);

				if (memVO.getPattern_Fails() + 1 >= Pattern_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PATTERN_2, ""), ddVO.getAuth_Type());
					memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.Normal,
							"Auth type 2 disabled for reached failure limit");

					return gts.get_FailCount(ReturnCode.TooManyAuthFailsInResetAuthType,
							String.valueOf(memVO.getPattern_Fails() + 1), null);
				}

				return gts.get_FailCount(ReturnCode.DiagramHashInvalidInCheckSetting,
						String.valueOf(memVO.getPattern_Fails() + 1), null);
			} else if (PIN_3.equals(settingAuthType) && !ddVO.getDigital_Hash().equals(settingAuthHash)) {
				memSvc.updateDigitalFailCounter(idgID, memVO.getDigital_Fails() + 1);

				if (memVO.getDigital_Fails() + 1 >= Digital_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PIN_3, ""), ddVO.getAuth_Type());
					memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.LockedForTooManyAuthFails,
							"Locked for digital lock fails have reached limit");
					memVO.setCustomer_Status(MemberStatus.LockedForTooManyAuthFails);
					memSvc.updateMember(memVO);

					return gts.get_FailCount(ReturnCode.LockedForTooMuchFail,
							String.valueOf(memVO.getDigital_Fails() + 1), null);
				}

				return gts.get_FailCount(ReturnCode.PinHashInvalidInCheckSetting,
						String.valueOf(memVO.getDigital_Fails() + 1), null);
			} else if ("4".equals(settingAuthType)) {
				return gts.common(ReturnCode.FaceInvalidInCheckSetting);
			} else if (settingAuthType.matches("^[^1234]+$")) {
				return gts.common(ReturnCode.UnknownAuthTypeInCheckSetting);
			}

			if (PATTERN_2.equals(settingAuthType)) {
				memSvc.updatePatternFailCounter(idgID, 0);
			} else if (PIN_3.equals(settingAuthType)) {
				memSvc.updateDigitalFailCounter(idgID, 0);
			}

		} catch (RuntimeException e) {
			Log4j.log.warn(
					"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfCheckSetting] DB warning: " + e);
			inLogObj.setThrowable(e.getMessage());

			return gts.common(ReturnCode.RequestExpired);
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHECK_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		// otp verification
		String returnCode = null;
		Map<String, String> paramMap = new HashMap<>();
//		List<NameValuePair> formparams = new ArrayList<>();
//		if ("3".equals(json.getJSONObject("Data").getString("authType")))
//			paramMap.put("Method", "verify_OTP");
//		else
//			paramMap.put("Method", "verifyEsnOTP");
		paramMap.put(METHOD, VERIFY_OTP);
//		paramMap.put("ESN", ddVO.getESN());
		paramMap.put(ESN, shaDeviceKeyB.substring(24, 64));	// seed = deviceKeyB[24, 63] 
		paramMap.put(CHANNEL3, memVO.getChannel_Code());
		paramMap.put(OTP3, settingOTP);
		paramMap.put(TIME_STAMP, settingTime);
		paramMap.put(CHALLENGE, idgateIDHash);	// sha256(idgateID)
		paramMap.put(TXN_DATA, idgateIDHash);

		HashMap<String, String> tsMsg = null;
		try {
//			returnCode = cvnVerifier.doSelect(paramMap.get("Method"), paramMap, sessID);
			returnCode = TS_Inst.verifyOTP(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(ESN)),
					CvnLib.Common.Filter.filter((String) paramMap.get(CHALLENGE)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TXN_DATA)),
					CvnLib.Common.Filter.filter((String) paramMap.get(OTP3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TIME_STAMP)), sessID);

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfCheckSetting][verify_OTP] Reply from TS: " + returnCode);

			tsMsg = gson.fromJson(returnCode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_CHECK_SETTING, "Unable to parse JSON", apLogObj, inLogObj);
			return gts.common(ReturnCode.TrustServerError);
		}

		if (SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
			// success
		} else if (FAIL_0020.equals(tsMsg.get(RETURN_CODE))) {
			// otp invalid
			return gts.common(ReturnCode.OTPInvalidInCheckSetting);
		} else if (FAIL_0001.equals(tsMsg.get(RETURN_CODE)) || FAIL_0002.equals(tsMsg.get(RETURN_CODE))) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else {
			return gts.common(ReturnCode.TrustServerError);
		}

		// generate token for change setting
		SMS_CountService smsSvc;
		try {
			smsSvc = new SMS_CountService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_CHECK_SETTING, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		long tokenID = -1;

		try {
			if (BIO_1.equals(settingAuthType)) {
				tokenID = smsSvc.create_Record(idgID, SmsOperation.BioTypeChangeSetting);
			} else if (PATTERN_2.equals(settingAuthType)) {
				tokenID = smsSvc.create_Record(idgID, SmsOperation.DiagramTypeChangeSetting);
			} else if (PIN_3.equals(settingAuthType)) {
				tokenID = smsSvc.create_Record(idgID, SmsOperation.PinTypeChangeSetting);
			}
		} catch (SQLException e) {
			logException(sessID, e, SVF_CHECK_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfCheckSetting] Raw token ID: " + tokenID);

		String encCheckingData;

		byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] keyBytes = null;
		// encrypt token id
		try {
			// Key = deviceKeyB[6, 21]
			keyBytes = shaDeviceKeyB.substring(6, 22).getBytes(StandardCharsets.UTF_8);
//			Log4j.log.info("*** tokenID:" + tokenID);
//			Log4j.log.info("*** keyBytes:" + gson.toJson(keyBytes));
//			Log4j.log.info("*** AESUtil.encrypt:" + gson.toJson(AESUtil.encrypt(ivBytes, keyBytes, String.valueOf(tokenID).getBytes("UTF-8"))));
			encCheckingData = Base64.getUrlEncoder()
					.encodeToString(AESUtil.encrypt(ivBytes, keyBytes, String.valueOf(tokenID).getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			logException(sessID, e, SVF_CHECK_SETTING, "Unable to encrypt data", apLogObj, inLogObj);
			return gts.common(ReturnCode.EncryptFailedInCheckSetting);
		}

		return gts.check_Setting(ReturnCode.Success, encCheckingData);
	}

	private String changeAuthType(String authType) {

		if (authType.contains("1")) {
			return "1";
		} else if (authType.contains("2")) {
			return "2";
		} else if (authType.contains("3")) {
			return "3";
		} else if (authType.contains("0")) {
			return "ALL";
		}
		return authType;
	}

	private String changeAuthTypeForTxn(String authType) {
		if(authType.length() > 1) {
			StringBuffer stringBuffer = new StringBuffer();
		 char[] charArr	= authType.toCharArray();
		 int count = 1;
			for (char signalChar: charArr) {
				if (authType.length() == count) {
					stringBuffer.append(signalChar);
				} else {
					stringBuffer.append(signalChar + ",");
				}
				count++;
			}
			String resultString = stringBuffer.toString();
			return resultString;
		} else {
			return authType;
		}
	}

	//	turn on or off specified authenticate mode
	private String svfSetAuthType(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(DATA, jSONObject.getString(ENC_SETTING_DATA));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_SET_AUTH_TYPE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String channel = map.get(CHANNEL2);
		String encSettingData = map.get(DATA);
		long idgID = Long.parseLong(idgateID);
		
		log4jAPforDebug(sessID, SVF_SET_AUTH_TYPE, "idgateID: " + (idgateID)
			+ ", channel: " + (channel) + ", encSettingData: " + (encSettingData), apLogObj);
		
		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid

		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			logException(sessID, new Exception("Didn't find the member data for this ID"), SVF_SET_AUTH_TYPE, "MemberNotFound", apLogObj, inLogObj);
			return gts.common(ReturnCode.MemberNotFound);
		} 
		String prevStatus = memVO.getCustomer_Status();
		if (prevStatus.equals(MemberStatus.Locked)
				|| prevStatus.equals(MemberStatus.LockedForTooManyAuthFails)) {
			return gts.common(ReturnCode.MemberLockedError);
		} else if (prevStatus.equals(MemberStatus.Deleted)) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else if (!prevStatus.equals(MemberStatus.Normal)) {
			return gts.common(ReturnCode.MemberStatusError);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.MemberChannelInvalidInResetAuthType);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (ddVO == null) {
			logException(sessID, new Exception("Didn't find the member device data for this ID"), SVF_SET_AUTH_TYPE, "MemberNotFound", apLogObj, inLogObj);
			return gts.common(ReturnCode.MemberNotFound);
		}

//		Log4j.log.debug("[" + sessID + "][svfSetAuthType] *** encSettingData:{}", encSettingData);

		// decrypt data
		JSONObject json = null;
		String deviceData = ddVO.getDevice_Data();
		String deviceKeyB = deviceData.substring(32, 64);
		String shaDeviceKeyB = null;
		try {
			shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			json = extractJSONData(sessID, encSettingData, idgateID, 4, 20, deviceData, SVF_SET_AUTH_TYPE, apLogObj, inLogObj);
		} catch (UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, "Unsupported Encoding Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInResetAuthType);
		} catch (DecryptFailedInSignupDeviceException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, "Decrypt Failed InSignupDevice Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInResetAuthType);
		} catch (JSONErrInSignupDeviceException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, "JSONErr InSignupDevice Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInResetAuthType);
		} catch (NoSuchAlgorithmException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, "HashGenFailedInResetAuthType", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInResetAuthType);
		}

		String idgateIDHash = null;
		try {
			idgateIDHash = AESUtil.SHA256_To_HexString(idgateID);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInResetAuthType);
		}

		JSONObject jsonSettingData = null;
		String msgCount;
		String iDGateId;
		String settingTime;
		String settingOTP;
		String settingEnable;
		String settingAuthType;
		String authHash;
		try {
			jsonSettingData = json.getJSONObject(DATA2);
			msgCount = jsonSettingData.getString(MSG_COUNT);
			iDGateId = jsonSettingData.getString(IDGATE_ID);
			settingTime = jsonSettingData.getString(TIME);
			settingOTP = jsonSettingData.getString(OTP2);
			settingEnable = jsonSettingData.getString(ENABLE2);
			settingAuthType = jsonSettingData.getString(AUTH_TYPE);
			authHash = jsonSettingData.getString(AUTH_HASH); // svfSetAuthType 沒有 key 了
		} catch (JSONException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, "JSONErrInResetAuthType", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInResetAuthType);
		}
		
		if (!idgateIDHash.equals(iDGateId)) {
			return gts.common(ReturnCode.MemberHashInvalidInResetAuthType);
			// TODO:UNREMARK BELOW
		} else if (memVO.getMsg_Count() >= Long.parseLong(msgCount)) {
			return gts.common(ReturnCode.MsgCountInvalidInResetAuthType); // 0603
		} else if (ddVO.getPerso_Update().equals(YES)) {
			return gts.common(ReturnCode.PersoUpdate); // 9998
		}

		try {
			memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));
		} catch (SQLException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		long time = System.currentTimeMillis();
		long recvTime = Long.parseLong(settingTime);
		// check time is still valid
		// TODO:UNREMARK BELOW
		if ((time - recvTime) > OTP_Timeout) {
			return gts.common(ReturnCode.TimeoutInResetAuthType); // 0606
		}

		String returnCode = null;
		Map<String, String> paramMap = new HashMap<>();
//		List<NameValuePair> formparams = new ArrayList<>();

//		paramMap.put("Method", "verifyEsnOTP");
		paramMap.put(METHOD, VERIFY_OTP);

		paramMap.put(ESN, shaDeviceKeyB.substring(24, 64));	// seed = deviceKeyB[24, 63] 
		paramMap.put(CHANNEL3, memVO.getChannel_Code());
		paramMap.put(OTP3, settingOTP);
		paramMap.put(TIME_STAMP, settingTime);
		try {
			// 111.9.8 Jack 測試
			paramMap.put(CHALLENGE, AESUtil.SHA256_To_HexString(idgateID));	// sha256(idgateID)
			paramMap.put(TXN_DATA, AESUtil.SHA256_To_HexString(idgateID));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInResetAuthType);
		}

//		Log4j.log.debug("[" + sessID + "][svfSetAuthType] *** paramMap.toJson:" + gson.toJson(paramMap));

		HashMap<String, String> tsMsg = null;
		try {
			returnCode = TS_Inst.verifyOTP(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(ESN)),
					CvnLib.Common.Filter.filter((String) paramMap.get(CHALLENGE)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TXN_DATA)),
					CvnLib.Common.Filter.filter((String) paramMap.get(OTP3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TIME_STAMP)), sessID);

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType][verify_OTP] Reply from TS: " + returnCode);

			tsMsg = gson.fromJson(returnCode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, "Unable to parse JSON", apLogObj, inLogObj);
			return gts.common(ReturnCode.TrustServerError);
		}
		
		if (SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
			// pass
			try {
				if (PIN_3.equals(settingAuthType)) {
					memSvc.updateDigitalFailCounter(idgID, 0);
				} else if (PATTERN_2.equals(settingAuthType)) {
					memSvc.updatePatternFailCounter(idgID, 0);
				}
			} catch (SQLException e) {
				logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}

		} else if (FAIL_0020.equals(tsMsg.get(RETURN_CODE))) {
			// otp invalid
			try {
				// verification fail counter have reached limit, lock account
				if (PATTERN_2.equals(settingAuthType) && memVO.getPattern_Fails() + 1 >= Pattern_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PATTERN_2, ""), ddVO.getAuth_Type());
					memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.Normal,
							"Auth type 2 disabled for reached failure limit");

					return gts.get_FailCount(ReturnCode.TooManyAuthFailsInResetAuthType,
							String.valueOf(memVO.getPattern_Fails() + 1), null);

				} else if (PIN_3.equals(settingAuthType) && memVO.getDigital_Fails() + 1 >= Digital_Fail_Limit) {
					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PIN_3, ""), ddVO.getAuth_Type());
					memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.LockedForTooManyAuthFails,
							"Device locked for reached failure limit of digital");
					memVO.setCustomer_Status(MemberStatus.LockedForTooManyAuthFails);
					memSvc.updateMember(memVO);

					return gts.get_FailCount(ReturnCode.LockedForTooMuchFail,
							String.valueOf(memVO.getDigital_Fails() + 1), null);
				}

			} catch (RuntimeException e) {
				Log4j.log.warn(
						"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] DB warning: " + e);
				inLogObj.setThrowable(e.getMessage());
				
				return gts.common(ReturnCode.RequestExpired);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			// TODO:UNREMARK BELOW
			return gts.common(ReturnCode.OTPInvalidInResetAuthType); // 0607 111.9.8 Jack 測試
		} else if (FAIL_0001.equals(tsMsg.get(RETURN_CODE)) || FAIL_0002.equals(tsMsg.get(RETURN_CODE))) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else {
			return gts.common(ReturnCode.TrustServerError);
		}

		tsMsg.clear();
		paramMap.clear();
		// 開的時候
		if (YES.equals(settingEnable)) {
			// 112.1.12 svfSetAuthType可以後蓋前
			// TODO:UNREMARK BELOW
//			if (ddVO.getAuth_Type().indexOf(settingAuthType) > -1) {
			// not allow to turn on an active verification type
//				return gts.common(ReturnCode.CannotTurnOnInResetAuthType); // 0609
			// 112.3.1 資料行 'Auth_Type' 中的字串或二進位資料將會截斷。截斷的值: '0111111111'
//				try {
//					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type(), ddVO.getAuth_Type());
//				} catch (RuntimeException e) {
//					Log4j.log.warn(
//							"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] DB warning: " + e);
//					inLogObj.setThrowable(e.getMessage());
//
//					return gts.common(ReturnCode.RequestExpired);
//				} catch (SQLException e) {
//					logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
//					return gts.common(ReturnCode.DatabaseError);
//				}
//				return gts.common(ReturnCode.Success);
			if (BIO_1.equals(settingAuthType)) {
				try {
					ddSvc.update_Bio_Hash(idgID, authHash);
				} catch (SQLException e) {
					logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			} else if (PATTERN_2.equals(settingAuthType)) {
				// 檢查圖形鎖與密碼鎖與上次相同
				if (ddVO.getPattern_Hash().equals(authHash)) {
					return gts.common(ReturnCode.PatternKeyIsSameInChangeSetting);
				}

				try {
					ddSvc.update_Pattern_Hash(idgID, authHash);
				} catch (SQLException e) {
					logException(sessID, e, SVF_CHANGE_SETTING, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			} else if (PIN_3.equals(settingAuthType)) {
				// 檢查圖形鎖與密碼鎖與上次相同
				if (ddVO.getDigital_Hash().equals(authHash)) {
					return gts.common(ReturnCode.DigitalKeyIsSameInChangeSetting);
				}

			} // END OF CHANGE_SETTING
//		}

			List<Blocked_Device_AuthVO2> channelAndModelList = new ArrayList<>();
			try {
				String upperLabel = ddVO.getDeviceLabel().toUpperCase();
				String upperModel = ddVO.getDeviceModel().toUpperCase();
				channelAndModelList = new Blocked_Device_AuthService(JNDI_Name, sessID).getList2(upperLabel, upperModel, settingAuthType, memVO.getChannel_Code());

				if (!channelAndModelList.isEmpty()) {
					return gts.common(ReturnCode.BlockedAuthTypeInResetAuthType);
				}
			} catch (SQLException e) {
				logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SET_AUTH_TYPE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}

			if (BIO_1.equals(settingAuthType)) {
				// bio hash
				try {
//					ddSvc.update_Bio_Hash(idgID, key); 
//					ddSvc.update_Bio_Hash(idgID, authHash);
					// [問題]Idenkey 2.1.8_重複設定人臉/圖形鎖/動態密碼最後會出現資料庫錯誤
//					ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(settingAuthType, ""), ddVO.getAuth_Type());
					this.updateAuthType(sessID, idgID, ddSvc, ddVO, settingAuthType);
				} catch (RuntimeException e) {
					Log4j.log.warn(
							"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] DB warning: " + e.getMessage());
					inLogObj.setThrowable(e.getMessage());

					return gts.common(ReturnCode.RequestExpired);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			} else if (PATTERN_2.equals(settingAuthType)) {
				try {
					ddSvc.update_Pattern_Hash(idgID, authHash);
					this.updateAuthType(sessID, idgID, ddSvc, ddVO, settingAuthType);
				} catch (RuntimeException e) {
					Log4j.log.warn(
							"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] DB warning: " + e.getMessage());
					inLogObj.setThrowable(e.getMessage());

					return gts.common(ReturnCode.RequestExpired);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			} else if (PIN_3.equals(settingAuthType)) {
				/* TS 創建新Persofile */
				paramMap.put(METHOD, SIGNUP_PERSO);
				paramMap.put(CHANNEL3, channel);
				paramMap.put(DEV_TYPE, "4");
				paramMap.put(USER_ID, String.valueOf(System.currentTimeMillis()));
				paramMap.put(SEED_SECRET, "");
				paramMap.put(ESN_SECRET, "");
				paramMap.put(MASTER_SECRET, "");
				paramMap.put(DEV_DATA, ddVO.getDevice_Data());
//				paramMap.put("PinHash", key);

				String retMsg = null;
				retMsg = TS_Inst.signupPersoFIDO(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
						CvnLib.Common.Filter.filter((String) paramMap.get(DEV_TYPE)), (String) paramMap.get(ESN_SECRET),
						(String) paramMap.get(SEED_SECRET), (String) paramMap.get(MASTER_SECRET),
						CvnLib.Common.Filter.filter((String) paramMap.get(USER_ID)),
						CvnLib.Common.Filter.filter((String) paramMap.get(DEV_DATA)),
						CvnLib.Common.Filter.filter((String) paramMap.get(PIN_HASH)), sessID);

//				Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType][signup_Perso] Reply from TS: " + retMsg);

				tsMsg = gson.fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
				}.getType());

				if (!SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
					return gts.common(ReturnCode.TrustServerError);
				}

				try {
					ddSvc.prestore_New_Esn(idgID, tsMsg.get(ESN), authHash); // 移 Perso_Update = 'Y'
				} catch (SQLException e) {
					logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}

				HashMap<String, String> persoData = new HashMap<String, String>();
//				persoData.put("AB", tsMsg.get("Mercury"));
//				persoData.put("perso", tsMsg.get("PersoFile"));
				persoData.put(TIME, String.valueOf(System.currentTimeMillis()));

				String respTxt = gson.toJson(persoData);

//				Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] Raw perso data before encryption: " + respTxt);

				byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
				byte[] keyBytes = null;
				try {
					keyBytes = AESUtil.SHA256_To_Bytes(ddVO.getDevice_Data().substring(11, 27));
					respTxt = Base64.getUrlEncoder()
							.encodeToString(AESUtil.encrypt(ivBytes, keyBytes, respTxt.getBytes(StandardCharsets.UTF_8)));
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException
						| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
						| BadPaddingException e) {
					logException(sessID, e, SVF_SET_AUTH_TYPE, "Unable to encrypt data", apLogObj, inLogObj);
					return gts.common(ReturnCode.EncryptFailedInResetAuthType);
				}

//				return gts.change_Setting(ReturnCode.Success, respTxt);	// encChangedData

				// pass (svfConfirm_Perso)
				try {
					// update digital fail counter to 0 and refresh ESN
					memSvc.updateDigitalFailCounter(idgID, 0);
					ddSvc.update_To_New_ESN(idgID);
					
					this.updateAuthType(sessID, idgID, ddSvc, ddVO, settingAuthType);					
					String logMsg = "Auth type " + settingAuthType;
					memSvc.addMemberStatusLog(idgID, prevStatus, prevStatus, logMsg);
					
//					// turn on/off auth type
//					if (jsonSettingData.has(ENABLE2)) {
//						String logMsg = "Auth type " + settingAuthType;
//
//						if (YES.equals(settingEnable)) {
//							logMsg += " is on";
//							
//							ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type() + settingAuthType, ddVO.getAuth_Type());
//						} else if (NOT_AVAILABLE.equals(settingEnable)) {
//							logMsg += " is off";
//							ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(settingAuthType, ""),
//									ddVO.getAuth_Type());
//						}
//
//						memSvc.addMemberStatusLog(idgID, prevStatus, prevStatus, logMsg);
//					}

					// reset AB counter to 0
					ddSvc.update_AB_Count(idgID, 0);

					memSvc.addMemberStatusLog(idgID, prevStatus, prevStatus, "Persofile update successful");

				} catch (RuntimeException e) {
					Log4j.log.warn(
							"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] DB warning: " + e.getMessage());
					inLogObj.setThrowable(e.getMessage());

					return gts.common(ReturnCode.RequestExpired);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			}
			// 關的時候
		} else {
			Log4j.log.debug("[" + sessID + "][svfSetAuthType] *** settingEnable:" + settingEnable);
			Log4j.log.debug("[" + sessID + "][svfSetAuthType] *** ddVO.getAuth_Type:" + ddVO.getAuth_Type());
			Log4j.log.debug("[" + sessID + "][svfSetAuthType] *** settingAuthType:" + settingAuthType);
			// turn off an active verification type
			// TODO:UNREMARK BELOW
			if (ddVO.getAuth_Type().indexOf(settingAuthType) == -1) {
				// not allow to turn off an inactive verification type
				return gts.common(ReturnCode.CannotTurnOffInResetAuthType); // 0611
			}

			try {
				ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(settingAuthType, ""), ddVO.getAuth_Type());
			} catch (RuntimeException e) {
				Log4j.log.warn(
						"[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] DB warning: " + e);
				inLogObj.setThrowable(e.getMessage());

				return gts.common(ReturnCode.RequestExpired);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}

		return gts.common(ReturnCode.Success);
	}

	/**
	 * 修改 [問題]Idenkey 2.1.8_重複設定人臉/圖形鎖/動態密碼最後會出現資料庫錯誤
	 * 
	 * @param sessID
	 * @param idgID
	 * @param ddSvc
	 * @param ddVO
	 * @param settingAuthType
	 * @throws SQLException
	 */
	private void updateAuthType(String sessID, long idgID, Device_DetailService ddSvc, Device_DetailVO ddVO,
			String settingAuthType) throws SQLException {
		if (ddVO.getAuth_Type().contains(settingAuthType)) {
			Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSetAuthType] AuthType ["
					+ ddVO.getAuth_Type() + "] already set [" + settingAuthType + "]");
		} else {
			ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type() + settingAuthType, ddVO.getAuth_Type());
		}
	}

	/**
	 * Log WSM, AP and In + Outbound
	 * 
	 * @param sessID
	 * @param e
	 * @param method
	 * @param errMsg
	 * @param apLogObj
	 * @param inLogObj
	 */
	private void logException(String sessID, Exception e, String method, String errMsg, APLogFormat apLogObj, InboundLogFormat inLogObj) {
		StackTraceElement[] stackTrace = e.getStackTrace();
		String execpMessage = e.getMessage();
		Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][" + method + "] " + errMsg + " : " + execpMessage
				+ ", stacktrace: " + gson.toJson(stackTrace));
		Map<String, Object> apLogMap = new HashMap<>();
		apLogMap.put(SESS_ID, sessID);
		apLogMap.put(VERSION, IDGateConfig.svVerNo);
		apLogMap.put(METHOD2, method);
		apLogMap.put(MESSAGE, errMsg +" : " + execpMessage);
		apLogMap.put(STACKTRACE2, gson.toJson(stackTrace));
		apLogObj.setMessageForMap(apLogMap);
		apLogObj.setThrowable(execpMessage);
		Log4jAP.log.error(apLogObj.getCompleteTxt());

		inLogObj.setThrowable(execpMessage);
	}
	
	/**
	 * Log WSM and AP
	 * @param sessID
	 * @param method
	 * @param message
	 * @param apLogObj
	 */
	private void log4jAPforDebug(String sessID, String method, String message, APLogFormat apLogObj) {
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][" + method + "] " + message);
		Map<String, Object> apLogMap = new HashMap<>();
		apLogMap.put(SESS_ID, sessID);
		apLogMap.put(VERSION, IDGateConfig.svVerNo);
		apLogMap.put(METHOD2, method);
		apLogMap.put(MESSAGE, message);
//		apLogMap.put("stacktrace", gson.toJson(e.getStackTrace())); // TODO
		apLogObj.setMessageForMap(apLogMap);
//		apLogObj.setThrowable(e.getMessage());
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

//		inLogObj.setThrowable(e.getMessage());
	}

	/**
	 * Step5
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param apLogObj 
	 * @param inLogObj 
	 * @return
	 */
	private String svfSendDeregResponse(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.get(IDGATE_ID).toString());
		map.put(DATA, jSONObject.get("encDeregData").toString());
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_SEND_DEREG_RESPONSE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
		String encDeregData = map.get(DATA);
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendDeregResponse] idgateID: " + idgateID
				+ ", channel: " + channel + ", encDeregData:" + encDeregData);

		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SET_AUTH_TYPE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.ChannelNotMatchToMember);
		}
		// TODO UNREMARK BELOW
		String customer_Status = memVO.getCustomer_Status();
		if (customer_Status.equals(MemberStatus.Deleted) 
				&& !TRUE.equals(IDGateConfig.testMode)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSendDeregResponse] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}

//		 Log4j.log.debug("[" + sessID + "][svfSendDeregResponse] *** encDeregData:{}", encDeregData);

		// decrypt data
		JSONObject json = null;
		String deviceData = ddVO.getDevice_Data();
		String deviceKeyB = deviceData.substring(32, 64);
		String shaDeviceKeyB = null;
		try {
			shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			json = extractJSONData(sessID, encDeregData, idgateID, 10, 26, deviceData, SVF_SEND_DEREG_RESPONSE, apLogObj, inLogObj);
		} catch (UnsupportedEncodingException e) {
			return gts.common(ReturnCode.DecryptFailedInSendDeregResponse);
		} catch (DecryptFailedInSignupDeviceException e) {
			return gts.common(ReturnCode.DecryptFailedInSendDeregResponse);
		} catch (JSONErrInSignupDeviceException e) {
			return gts.common(ReturnCode.JSONErrInSendDeregResponse);
		} catch (NoSuchAlgorithmException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, "HashGenFailedInSendDeregResponse", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInSendDeregResponse);
		}

		JSONObject jsonDeregData;
		String msgCount;
		String deregDeviceData;
		String iDGateId;
		String deregTime;
		String deregOTP;
		try {
			jsonDeregData = json.getJSONObject(DATA2);
			msgCount = jsonDeregData.getString(MSG_COUNT);
			deregDeviceData = jsonDeregData.getString(DEVICE_DATA);
			iDGateId = jsonDeregData.getString(IDGATE_ID);
			deregTime = jsonDeregData.getString(TIME);
			deregOTP = jsonDeregData.getString(OTP2);
		} catch (JSONException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, "JSONErrInSendDeregResponse", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSendDeregResponse);
		}

		// encData.Data
//		String deviceDataHash = (String) jsonDeregData.get("deviceData"); // sha256雜湊字串
		// KEEP
//		String fidoStr = (String) jsonDeregData.get("fidoRes");

		// Log4j.log.debug("[{}] *** fidoStr@svfSendDeregResponse:", sessID, fidoStr);

		String deviceHash;
		try {
			deviceHash = AESUtil.SHA256_To_HexString(ddVO.getDevice_Data());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, "Unable to encrypt data", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInSendDeregResponse);
		}

		if (!deregDeviceData.equals(deviceHash)) {
//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo
//					+ "][svfSendDeregResponse] Client device data hash doesn't match DB device data hash: "
//					+ deviceHash);
			return gts.common(ReturnCode.DeviceHashErrInSendDeregResponse);
		}

		String idgateIDHash = null;
		try {
			idgateIDHash = AESUtil.SHA256_To_HexString(idgateID);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInSendDeregResponse);
		}

		if (!idgateIDHash.equals(iDGateId)) {
			return gts.common(ReturnCode.MemberHashInvalidInSendDeregResponse);
		} else if (memVO.getMsg_Count() >= Long.parseLong(msgCount) 
				&& !TRUE.equals(IDGateConfig.testMode)) {
			// TODO UNREMARK BELOW
			return gts.common(ReturnCode.MsgCountInvalidInSendDeregResponse); // 2503
		}

		try {
			memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		long time = System.currentTimeMillis();
		long recvTime = Long.parseLong(deregTime);
		// check time is still valid
		if ((time - recvTime) > OTP_Timeout && !TRUE.equals(IDGateConfig.testMode)) {
			// TODO UNREMARK BELOW
			return gts.common(ReturnCode.TimeoutInSendDeregResponse); // 2507
		}

		// otp verification
		String returnCode = null;
		Map<String, String> paramMap = new HashMap<>();

		paramMap.put(METHOD, VERIFY_OTP);
		paramMap.put(ESN, shaDeviceKeyB.substring(24, 64));	// seed
//		paramMap.put("ESN", ddVO.getESN());
		paramMap.put(CHANNEL3, memVO.getChannel_Code());
		paramMap.put(OTP3, deregOTP);
		paramMap.put(TIME_STAMP, deregTime);
		paramMap.put(CHALLENGE, idgateIDHash);
		paramMap.put(TXN_DATA, idgateIDHash);

//		Log4j.log.debug("[" + sessID + "][svfSendDeregResponse] *** paramMap.toJson:" + gson.toJson(paramMap));

		HashMap<String, String> tsMsg = null;
		try {
			returnCode = TS_Inst.verifyOTP(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(ESN)),
					CvnLib.Common.Filter.filter((String) paramMap.get(CHALLENGE)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TXN_DATA)),
					CvnLib.Common.Filter.filter((String) paramMap.get(OTP3)),
					CvnLib.Common.Filter.filter((String) paramMap.get(TIME_STAMP)), sessID);

//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendDeregResponse][verify_OTP] Reply from TS: " + returnCode);

			tsMsg = gson.fromJson(returnCode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.TrustServerError);
		}

		if (SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
			// success
		} else if (FAIL_0020.equals(tsMsg.get(RETURN_CODE)) && !TRUE.equals(IDGateConfig.testMode)) {
			// otp invalid
			return gts.common(ReturnCode.OTPInvalidInSendDeregResponse); // 2506
		} else if (FAIL_0001.equals(tsMsg.get(RETURN_CODE)) || FAIL_0002.equals(tsMsg.get(RETURN_CODE))) {
			return gts.common(ReturnCode.MemberDeletedError);
		} else {
			// TODO UNREMARK BELOW
			if(!TRUE.equals(IDGateConfig.testMode)) {
				logException(sessID, new Exception("OTP Invalid"), SVF_SEND_DEREG_RESPONSE, "OTPInvalidInSendDeregResponse", apLogObj, inLogObj);
				return gts.common(ReturnCode.OTPInvalidInSendDeregResponse);
			}
		}

		// 取回 Step2 svfSendRegResponse 存的 username
		PubkeyStoreService frontKeyStoreSVC;
		try {
			frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		PubkeyStoreVO pkStore = null;
		try {
			pkStore = frontKeyStoreSVC.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		if (pkStore == null) {
			Log4j.log.error("## ERROR ## Alias IS NULL");
			Log4j.log.error("## ERROR ## Cannot find Alias In pub_Key_Store for idgateID:" + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		String idgateIDPubKey = pkStore.getPub_key();
		String username = pkStore.getAlias();
//		String username = getUserName(idgateID, gts);
//		String username = pkStore.getAlias();
		HashMap<String, String> uafRequest = new HashMap<String, String>();
		uafRequest.put(OP, DEREG);
		uafRequest.put(CONTEXT2, "{\"username\":\"" + username + "\"}");

		String rsp = fidoUafResource.GetUAFRequest(gson.toJson(uafRequest), sessID);
		Map<String, Object> rspDataMap = null;
		try {
			rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonMappingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonProcessingException e) {
			logException(sessID, e, SVF_SEND_DEREG_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendDeregResponse] rspData: " + rspDataMap);

		if (rspDataMap == null) {
			Log4j.log.error("## ERROR ## rspData IS NULL");
			Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
			return gts.common(ReturnCode.TrustServerError);
		}
		String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
		// KEEP
//		String description = (String) rspDataMap.get("Description");
		String encDeregisterResult = null;
		String encIdgateID = null;
//		Log4j.log.debug("[{}][svfSendDeregResponse] *** rtnCode:{}", sessID, rtnCode);
//		Log4j.log.debug("[{}][svfSendDeregResponse] *** description:{}", sessID, description);
		// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
		if (SUCCESS_1200.equals(rtnCode)) {
			rtnCode = ReturnCode.Success;

			try {
				memVO.setCustomer_Status(MemberStatus.Deleted);
				memSvc.updateMember(memVO);
				memSvc.addMemberStatusLog(memVO.getiDGate_ID(), customer_Status, MemberStatus.Deleted,
						"Issued by " + channel);
				// remove [Pub_Key_Store] where [iDGate_ID] =
				// TODO UNREMARK BELOW
				if(!TRUE.equals(IDGateConfig.testMode)) {
					frontKeyStoreSVC.removeOneRow(idgateID);
				}
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}

			HashMap<String, String> encIdgateIDMap = new HashMap<String, String>();
			encIdgateIDMap.put(IDGATE_ID, idgateID);

			String jsonDeRegResult = gson.toJson(encIdgateIDMap);

			try {
				encIdgateID = this.getEncData(sessID, jsonDeRegResult, idgateIDPubKey, deviceData, 11, 27,
						SVF_SEND_DEREG_RESPONSE, apLogObj, inLogObj);
			} catch (EncryptFailedException e) {
				return gts.common(ReturnCode.EncryptFailedInSendDeregResponse);
			}
			
			// Reset to 0 if otp is valid
			try {
				memSvc.updateAuthFailCounter(idgID, 0);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} else {
			// FIDO簽章驗證錯誤也要記錄錯誤次數+1在Members.Auth_Fails 
			try {
				memSvc.updateAuthFailCounter(idgID, memVO.getAuth_Fails() + 1);
				return gts.get_FailCount(rtnCode, String.valueOf(memVO.getAuth_Fails() + 1), null);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_DEREG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}

		String returnVal = gts.get_DeregRequest(rtnCode, encDeregisterResult, encIdgateID);
//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendDeregResponse] Response: "
//				+ returnVal);

		return returnVal;
	}
	
	/**
	 * Step5.2 For verifyType = 0
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @return
	 */
	private String svDeRegister_St(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.get(IDGATE_ID).toString());
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		
		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_DE_REGISTER_ST, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		
		String idgateID = map.get(IDGATE_ID);
//		String encDeregData = map.get("encDeregData");
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);
		
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svDeRegister_St] channel: " + channel + ", idgateID: "
				+ idgateID);
		
		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (memVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.ChannelNotMatchToMember);
		}
		// TODO UNREMARK BELOW
		String customer_Status = memVO.getCustomer_Status();
		if (customer_Status.equals(MemberStatus.Deleted)) {
			return gts.common(ReturnCode.MemberStatusError);
		}
		
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SV_DE_REGISTER_ST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svDeRegister_St] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		
			String rtnCode = ReturnCode.Success;
			
			try {
				memVO.setCustomer_Status(MemberStatus.Deleted);
				memSvc.updateMember(memVO);
				memSvc.addMemberStatusLog(memVO.getiDGate_ID(), customer_Status, MemberStatus.Deleted,
						"Issued by " + channel);
			} catch (SQLException e) {
				logException(sessID, e, SV_DE_REGISTER_ST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		
		String returnVal = gts.get_DeregResponse(rtnCode, MemberStatus.Deleted);
//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svDeRegister_St] Response: "
//				+ returnVal);
		
		return returnVal;
	}

	/**
	 * Step5.1
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @return
	 */
	private String svDeRegister(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(IDGATE_ID, jSONObject.get(IDGATE_ID).toString());
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SV_DE_REGISTER, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String idgateID = map.get(IDGATE_ID);
//		String encDeregData = map.get("encDeregData");
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svDeRegister] channel: " + channel + ", idgateID: "
				+ idgateID);

		// 若有 !channel.equals(memVO.getChannel_Code()) ，就不用再檢核 isChannelValid
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_DE_REGISTER, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null) {
			return gts.common(ReturnCode.MemberNotFound);
		} else if (isEqualChannel2Member(channel, memVO)) {
			return gts.common(ReturnCode.ChannelNotMatchToMember);
		}
		// TODO UNREMARK BELOW
		String customer_Status = memVO.getCustomer_Status();
		if (customer_Status.equals(MemberStatus.Deleted)) {
			return gts.common(ReturnCode.MemberStatusError);
		}

		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_DE_REGISTER, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SV_DE_REGISTER, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svDeRegister] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}

		// 取回 Step2 svfSendRegResponse 存的 username
		PubkeyStoreService frontKeyStoreSVC;
		try {
			frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SV_DE_REGISTER, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		PubkeyStoreVO pkStore = null;
		try {
//			Log4j.log.debug("[" + sessID + "][svDeRegister] *** idgID:{}",idgID);
			pkStore = frontKeyStoreSVC.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		if (pkStore == null) {
			Log4j.log.error("## ERROR ## Alias IS NULL");
			Log4j.log.error("## ERROR ## Cannot find Alias In pub_Key_Store for idgateID:" + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		String username = pkStore.getAlias();
		
		HashMap<String, String> uafRequest = new HashMap<String, String>();
		uafRequest.put(OP, DEREG);
		uafRequest.put(CONTEXT2, "{\"username\":\"" + username + "\"}");

		String rsp = fidoUafResource.GetUAFRequest(gson.toJson(uafRequest), sessID);
		Map<String, Object> rspDataMap = null;
			// parse json
//			try {
//				rspDataMap = gson.fromJson(rsp, new TypeToken<Map<String, Object>>() {
//				}.getType());
//			} catch (JsonSyntaxException e) {
//				logException(sessID, e, SV_DE_REGISTER, "Unable to parse UAF json string", apLogObj, inLogObj);
//				return gts.common(ReturnCode.JsonParseError);
//			}
			try {
				rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
				});
			} catch (JsonMappingException e) {
				logException(sessID, e, SV_DE_REGISTER, "Unable to parse UAF json string", apLogObj, inLogObj);
				return gts.common(ReturnCode.JsonParseError);
			} catch (JsonProcessingException e) {
				logException(sessID, e, SV_DE_REGISTER, "Unable to parse UAF json string", apLogObj, inLogObj);
				return gts.common(ReturnCode.JsonParseError);
			}
	

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svDeRegister] rspData: " + rspDataMap);

		if (rspDataMap == null) {
			Log4j.log.error("## ERROR ## rspData IS NULL");
			Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
			return gts.common(ReturnCode.TrustServerError);
		}
		String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
		//KEEP
//		String description = (String) rspDataMap.get("Description");
		//		Log4j.log.debug("[{}][svDeRegister] *** rtnCode:{}", sessID, rtnCode);
//		Log4j.log.debug("[{}][svDeRegister] *** description:{}", sessID, description);
		// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
		if (SUCCESS_1200.equals(rtnCode)) {
			rtnCode = ReturnCode.Success;
			try {
				memVO.setCustomer_Status(MemberStatus.Deleted);
				memSvc.updateMember(memVO);
				memSvc.addMemberStatusLog(memVO.getiDGate_ID(), customer_Status, MemberStatus.Deleted,
						"Issued by " + channel);
				// remove [Pub_Key_Store] where [iDGate_ID] =
				// TODO UNREMARK BELOW
				frontKeyStoreSVC.removeOneRow(idgateID);
			} catch (SQLException e) {
				logException(sessID, e, SV_DE_REGISTER, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svDeRegister] Response: "
//				+ returnVal);

		return gts.get_DeregResponse(rtnCode, MemberStatus.Deleted);
	}

	/**
	 * Health Check Ref Step4 
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param inLogObj 
	 * @return
	 */
	public String svfHealthCheck(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_HEALTH_CHECK, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String channel = map.get(CHANNEL2);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfHealthCheck] channel: " + (channel));

		try {
			if (!isChannelValid(sessID, channel)) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_HEALTH_CHECK, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_HEALTH_CHECK, "NamingException::DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_HEALTH_CHECK, "SQLException::DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		HashMap<String, String> uafRequest = new HashMap<String, String>();
		uafRequest.put(OP, "Hcheck");
//		uafRequest.put("op", "1Hcheck"); // 反向測試
		String username = HEALTH_CHECK;
		uafRequest.put(CONTEXT2, "{\"username\":\"" + username  + "\"}");

		String rsp = fidoUafResource.GetUAFRequest(gson.toJson(uafRequest), sessID);
		Map<String, Object> rspDataMap = null;
		// parse json
//			try {
//				rspDataMap = gson.fromJson(rsp, new TypeToken<Map<String, Object>>() {
//				}.getType());
//			} catch (JsonSyntaxException e) {
//				logException(sessID, e, SVF_HEALTH_CHECK, "Unable to parse UAF json string", apLogObj, inLogObj);
//				return gts.common(ReturnCode.JsonParseError);
//			}
		try {
			rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonMappingException e) {
			logException(sessID, e, SVF_HEALTH_CHECK, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonProcessingException e) {
			logException(sessID, e, SVF_HEALTH_CHECK, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfHealthCheck] rspData: " + rspDataMap);

		if (rspDataMap == null) {
			Log4j.log.error("## ERROR ## rspData IS NULL");
			Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
			return gts.common(ReturnCode.TrustServerError);
		}
		String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
		// KEEP
//		String description = (String) rspDataMap.get("Description");

		String serverECCPubKey = null;
		//		Log4j.log.debug("[{}][svfHealthCheck] *** rtnCode:{}", sessID, rtnCode);
//		Log4j.log.debug("[{}][svfHealthCheck] *** description:{}", sessID, description);
		// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
		if (SUCCESS_1200.equals(rtnCode)) {
			rtnCode = ReturnCode.Success;
			
			String rsaSecret = null;
//			 Server’s public key
			// 每次都去DB取出加密的RSA Key
			rsaSecret = RSA.loadPublicKeyFromDB(rsaKeyAlias, sessID);
			if (rsaSecret.contains(ERROR2)) {
				logException(sessID, new Exception("Loading RSA public key failed:"
						+ rsaSecret), SVF_HEALTH_CHECK, "Loading RSA public key failed", apLogObj, inLogObj);
				return gts.common(ReturnCode.KeyGenErr);
			}
			webpinAppKey = rsaSecret;
//			if (webpin_AppKey == null || "".equals(webpin_AppKey)) {
//			} else {
//				rsaSecret = webpin_AppKey;
//			}
			
			try {
//				String testServerECCPubKey = ECCUtil.loadPublicKeyFromDB(eccKeyAlias, sessID);
				
				serverECCPubKey = ECCUtil.loadPublicKeyFromDB(eccKeyAlias, sessID);
//				serverECCPubKey = ECCUtil.getOwnPublicKey(sessID, eccKeyAlias);	// Cache 
//				Log4j.log.debug("[svfHealthCheck] *** testServerECCPubKey:{}", testServerECCPubKey);
				Log4j.log.debug("[svfHealthCheck] *** serverECCPubKey:{}", serverECCPubKey);
			} catch (Exception e) {
				logException(sessID, e, SVF_GET_PUB_KEY_N_TIME, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}

		return gts.get_Pubkey(rtnCode, webpinAppKey, serverECCPubKey, null, null);
	}
	
	/**
	 * Step4
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param inLogObj 
	 * @return
	 */
	private String svfSendAuthResponse(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		// System.out.println("[" + sessID + "] begin svfSendAuthResponse: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(BANK_TXN_ID, jSONObject.get(BANK_TXN_ID).toString());
		map.put(IDGATE_ID, jSONObject.get(IDGATE_ID).toString());
		map.put(DATA, jSONObject.get("encVerifyTxnData").toString());
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		boolean hasDeviceOS = jSONObject.has(DEVICE_OS);
		if (hasDeviceOS) {
			map.put(DEVICE_OS, jSONObject.getString(DEVICE_OS));
		}
		
		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_SEND_AUTH_RESPONSE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		
		String bankTxnID = map.get(BANK_TXN_ID);
		String idgateID = map.get(IDGATE_ID);
		String encVerifyTxnData = map.get(DATA);
		String channel = map.get(CHANNEL2);
		long idgID = Long.parseLong(idgateID);
		String deviceOS = null;
		if (hasDeviceOS) {
			deviceOS = map.get(DEVICE_OS);
		}
		if (StringUtils.isNoneEmpty(deviceOS)) {
			Log4j.log.debug("[{}][Version: {}][svfSendAuthResponse] *** deviceOS:[{}]", sessID,
					IDGateConfig.svVerNo, deviceOS);
		}
		
//		String serverData = map.get("serverData");
		
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendAuthResponse] idgateID: " + idgateID
				+ ", channel: " + channel + ", bankTxnID: " + bankTxnID + ", encVerifyTxnData:" + encVerifyTxnData);
		
		MembersService memSvc = null;
		MembersVO memVO = null;
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
		String prevStatus = null;
		if (!TRUE.equals(onlyOTP)) {
			// // Log4j.log.info("[{}] *** begin svfSendAuthResponse@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
			try {
				memSvc = new MembersService(JNDI_Name, sessID);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			try {
				memVO = memSvc.getByIdgateID(idgID);
			} catch (NumberFormatException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNEXPECTED_VALUE, apLogObj, inLogObj);
				return gts.common(ReturnCode.ParameterError);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			// // Log4j.log.info("[{}] *** before check_memVO@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
			
			if (memVO == null) {
				Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
						+ "][svfSendAuthResponse] Didn't find the member data for this ID: " + idgateID);
				return gts.common(ReturnCode.MemberNotFound);
				// 111.12.8 可以跨channel驗證 ( svfGetAuthRequest, svfSendAuthResponse不檢核channel)
//			} else if (!channel.equals(memVO.getChannel_Code())) {
//				return gts.common(ReturnCode.MemberChannelInvalidInSendAuthResponse);
			} 
			prevStatus = memVO.getCustomer_Status();
			if (!prevStatus.equals(MemberStatus.Normal)) {
				Log4j.log.error("*** memVO.getCustomer_Status():" + prevStatus);
				return gts.common(ReturnCode.MemberStatusError);
			}
			
			// Fix bug:111.12.8 可以跨channel驗證後，未檢核 Channel
			try {
				if (!isChannelValid(sessID, channel)) {
					return gts.common(ReturnCode.ChannelInvalidError);
				}
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "NamingException::DB Error occurred", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "SQLException::DB Error occurred", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			// // Log4j.log.info("[{}] *** after getByIdgateID@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
		}
		
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = null;
		try {
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		if (ddVO == null) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSendAuthResponse] Didn't find the member device data for this ID: " + idgateID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		// System.out.println("[" + sessID + "] after getOneDevice_Detail: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
		// // Log4j.log.info("[{}] *** after getOneDevice_Detail@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
//		// // Log4j.log.info("[" + sessID + "][svfSendAuthResponse] *** encVerifyTxnData:{}", encVerifyTxnData);
		
		// decrypt data
		JSONObject json = null;
		String deviceData = ddVO.getDevice_Data();
		String deviceKeyB = deviceData.substring(32, 64);
		String shaDeviceKeyB = null;
		try {
			shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			json = extractJSONData(sessID, encVerifyTxnData, idgateID, 3, 19, deviceData, SVF_SEND_AUTH_RESPONSE, apLogObj, inLogObj);
		} catch (UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Unsupported Encoding Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInSendAuthResponse);
		} catch (DecryptFailedInSignupDeviceException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Decrypt Failed InSignupDevice Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInSendAuthResponse);
		} catch (JSONErrInSignupDeviceException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "JSONErr InSignupDevice Exception", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSendAuthResponse);
		} catch (NoSuchAlgorithmException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "HashGenFailedInSendAuthResponse", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInSendAuthResponse);
		}
		
		// System.out.println("[" + sessID + "] after shaDeviceKeyB: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
//		// // Log4j.log.info("[{}] *** after shaDeviceKeyB@{}: {}ms \r\n", sessID, "svfSendAuthResponse", new SimpleDateFormat("mm:ss.SSS").format(new Date()));
		
		JSONObject jsonVerifyTxnData = null;
		String msgCount = null;
		String time = null;
		String otp = null;
		String txnID = null;
		String idgateIDHash = null;
		String banktxnDataHash = null;
		String deviceDataHash = null;
		String authType = null;
		String authHash = null;
		String keyType = null;
		String fidoStr = null;
		try {
			jsonVerifyTxnData = json.getJSONObject(DATA2);
			msgCount = (String) jsonVerifyTxnData.get(MSG_COUNT);
			time = (String) jsonVerifyTxnData.get(TIME);
			otp = (String) jsonVerifyTxnData.get(OTP2);
			txnID = jsonVerifyTxnData.getString(TXN_ID);
			idgateIDHash = (String) jsonVerifyTxnData.get(IDGATE_ID);
			banktxnDataHash = (String) jsonVerifyTxnData.get(BANK_TXN_DATA);	// 112.1.13 “bankTxnData”:”xxxxx”, //sha256雜湊字串 
			deviceDataHash = (String) jsonVerifyTxnData.get(DEVICE_DATA);
			authType = (String) jsonVerifyTxnData.get(AUTH_TYPE);
			authHash = (String) jsonVerifyTxnData.get(AUTH_HASH);
//			authHash = (String) dataObj.get("test");	//	TEST ONLY
			keyType = (String) jsonVerifyTxnData.get(KEY_TYPE); // 簽章金鑰類型
			fidoStr = (String) jsonVerifyTxnData.get(FIDO_RES);
		} catch (JSONException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "JSONErrInSendAuthResponse", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSendAuthResponse);
		}
		
		Log4j.log.debug("[{}][Version: {}][svfSendAuthResponse] *** authType:[{}]", sessID,
				IDGateConfig.svVerNo, authType);
		
//		Log4j.log.debug("[{}] *** msgCount@svfSendAuthResponse:", sessID, msgCount);
//		Log4j.log.debug("[{}] *** txnID@svfSendAuthResponse:", sessID, txnID);
//		 Log4j.log.debug("[{}] *** fidoStr@svfSendAuthResponse:", sessID, fidoStr);
		
		String idgateIDHashIn = null;
		try {
			idgateIDHashIn = AESUtil.SHA256_To_HexString(idgateID);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInSendAuthResponse);
		}
		
		if (!TRUE.equals(onlyOTP)) {
			// check idgateID
			if (!idgateIDHashIn.equals(idgateIDHash)) {
				return gts.common(ReturnCode.MemberHashInvalidInSendAuthResponse);
				// 禁止重送
				// TODO: RELEASE BELOW 2 lines
			} else if (memVO.getMsg_Count() >= Long.parseLong(msgCount) && !TRUE.equals(IDGateConfig.testMode)) {
				return gts.common(ReturnCode.MsgCountInvalidInSendAuthResponse); // 2403
			}
		}
		
		// System.out.println("[" + sessID + "] after idgateIDHashIn: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
//		// Log4j.log.info("[{}] *** after shaDeviceKeyB@{}: {}ms \r\n", sessID, "svfSendAuthResponse", new SimpleDateFormat("mm:ss.SSS").format(new Date()));
		
		String deviceHash;
		try {
			deviceHash = AESUtil.SHA256_To_HexString(ddVO.getDevice_Data());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Unable to encrypt data", apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInSendAuthResponse);
		}
		
		if (!deviceDataHash.equals(deviceHash)) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSendAuthResponse] Client device data hash doesn't match DB device data hash: "
					+ deviceHash);
			return gts.common(ReturnCode.DeviceHashErrInSendAuthResponse);
		}
		
		// System.out.println("[" + sessID + "] after deviceHash: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
//		// Log4j.log.info("[{}] *** after deviceHash@{}: {}ms \r\n", sessID, "svfSendAuthResponse", new SimpleDateFormat("mm:ss.SSS").format(new Date()));
		
		Verify_RequestService vrSvc = null;
		if (!TRUE.equals(onlyOTP)) {
			try {
				memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			// fetch txn
			try {
				vrSvc = new Verify_RequestService(JNDI_Name, sessID);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			Verify_RequestVO vrVO = null;
			String requestID = null;
			String transationDate = null;
			List<Blocked_Device_AuthVO2> channelAndModelList = new ArrayList<>();
			// 支援跨區驗證(黑名單
			String vrChannel;
			try {
				String upperLabel = ddVO.getDeviceLabel().toUpperCase();
				String upperModel = ddVO.getDeviceModel().toUpperCase();
				requestID = txnID.substring(8);
				transationDate = txnID.substring(0, 8);
				vrVO = vrSvc.getOneWithTime(idgID, requestID, transationDate);
				vrChannel = vrVO.getChannel_Code();
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** ddVO.getDeviceLabel():" + upperLabel);
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** ddVO.getDeviceModel():" + upperModel);
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** memVO.getChannel_Code():" + vrVO.getChannel_Code());
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** authType:" + authType);

				channelAndModelList = new Blocked_Device_AuthService(JNDI_Name, sessID).getList2(upperLabel, upperModel,
						authType, vrChannel);

				if (!channelAndModelList.isEmpty()) {
					return gts.common(ReturnCode.BlockedAuthTypeInSendAuthResponse);
				}
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			Verify_DetailVO vdVO = null;
			try {
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** iDGate_ID:" + idgID);
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** Request_ID:" + requestID);
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** Transaction_Date:" + transationDate);
				
				ChannelVO chVO = null;
				try {
					chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(vrChannel);
					if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
						return gts.common(ReturnCode.ChannelInvalidError);
					}
//				chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(channel);
					// Log4j.log.info("[{}] *** after getOneChannel@{}: {}ms \r\n", sessID, "svfSendAuthResponse", new SimpleDateFormat("mm:ss.SSS").format(new Date()));
				} catch (SQLException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				} catch (UnknownHostException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				} catch (NamingException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
				String chJNDI = chVO.getJNDI();
//				String lastJNDI = chJNDI.substring(chJNDI.lastIndexOf("/") + 1, chJNDI.length());
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** lastJNDI:{}", lastJNDI);
				Verify_DetailService vdSvc = new Verify_DetailService(chJNDI, sessID);
//			vdSvc = new Verify_DetailService(chVO.getJNDI(), sessID);
				vdVO = vdSvc.getOneVerify_Detail(idgID, requestID, transationDate);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (UnknownHostException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NamingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			
			// System.out.println("[" + sessID + "] after getOneVerify_Detail: " + new
			// SimpleDateFormat("mm:ss.SSS").format(new Date()));
			// Log4j.log.info("[{}] *** after getOneVerify_Detail@{}: {}ms \r\n", sessID, "svfSendAuthResponse", new SimpleDateFormat("mm:ss.SSS").format(new Date()));
			
			String returnNum = ReturnCode.Success;
			String txnStatus = TxnStatus.WaitForVerify;
			
			if (vrVO == null || vdVO == null) {
				return gts.common(ReturnCode.TxnNotFoundInSendAuthResponse);
				// TODO UNREMARK BELOW 2 lines 111.7.27 交易狀態錯誤
			} else {
				String authStatus = vrVO.getStatus_Code();
				if (isNotWaitForVerify(authStatus)
						&& !TRUE.equals(IDGateConfig.testMode)) {
					return gts.common(ReturnCode.TxnStatusErrInSendAuthResponse); // 2408
				} else if (vrVO.getAuth_Mode().indexOf(authType) == -1 || ddVO.getAuth_Type().indexOf(authType) == -1) {
					return gts.common(ReturnCode.AuthTypeNotSetInSendAuthResponse);
					// TODO UNREMARK BELOW {} 111.7.27 check time is still valid
				} else if (getTimeInterval(vrVO) > Txn_Timeout
						&& !TRUE.equals(IDGateConfig.testMode)) {
					try {
						vrSvc.updateTxnStatusWithTime(idgID,
								Long.parseLong(txnID.substring(8)),
								txnID.substring(0, 8), TxnStatus.Timeout,
								authStatus, ReturnCode.TxnTimeoutInSendAuthResponse);
					} catch (RuntimeException e) {
						Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo
								+ "][svfSendAuthResponse] DB warning: " + e);
						inLogObj.setThrowable(e.getMessage());
						
						return gts.common(ReturnCode.RequestExpired);
					} catch (SQLException e) {
						logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
						return gts.common(ReturnCode.DatabaseError);
					}
					// TODO UNREMARK BELOW 1 lines
					if (!TRUE.equals(IDGateConfig.testMode)) {
						return gts.common(ReturnCode.TxnTimeoutInSendAuthResponse); // 2409 重打時會出錯
					}
					// 111.7.14 先不檢查
//		} else if ("1".equals(authType)
//				&& !ddVO.getBio_Hash().equals(authHash)) {
//			returnCode = ReturnCode.BioInvalidInSendAuthResponse;
				} else if (PATTERN_2.equals(authType) && !ddVO.getPattern_Hash().equals(authHash)) {
					returnNum = ReturnCode.PatternInvalidInSendAuthResponse;
					
					try {
						memSvc.updatePatternFailCounter(idgID, (memVO.getPattern_Fails() + 1));
						if ((memVO.getPattern_Fails() + 1) >= Pattern_Fail_Limit) {
							// disable member auth for reached failure limit
							ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PATTERN_2, ""), ddVO.getAuth_Type());
							memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.Normal,
									"Auth type 2 disabled for reached failure limit");
							returnNum = ReturnCode.PatternAuthDisabledInSendAuthResponse;
						}
					} catch (RuntimeException e) {
						Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo
								+ "][svfSendAuthResponse] DB warning: " + e);
						inLogObj.setThrowable(e.getMessage());
						
						return gts.common(ReturnCode.RequestExpired);
					} catch (SQLException e) {
						logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
						return gts.common(ReturnCode.DatabaseError);
					}
				} else if (PIN_3.equals(authType) && !ddVO.getDigital_Hash().equals(authHash)) {
					returnNum = ReturnCode.DigitalInvalidInSendAuthResponse;
					
					try {
						memSvc.updateDigitalFailCounter(idgID, (memVO.getDigital_Fails() + 1));
						if ((memVO.getDigital_Fails() + 1) >= Digital_Fail_Limit) {
							// txn status to fail when it reached limit
							returnNum = ReturnCode.LockedForTooMuchFail;
							txnStatus = TxnStatus.Fail;
							vrSvc.updateVerify_Request(TxnStatus.Fail, returnNum, new Timestamp(System.currentTimeMillis()),
									idgID, txnID, deviceOS);
							
							// disable member auth for reached failure limit and lock it
							ddSvc.update_Auth_Type(idgID, ddVO.getAuth_Type().replace(PIN_3, ""), ddVO.getAuth_Type());
							memSvc.addMemberStatusLog(idgID, prevStatus,
									MemberStatus.LockedForTooManyAuthFails,
									"Device locked for reached failure limit of digital.");
							memVO.setCustomer_Status(MemberStatus.LockedForTooManyAuthFails);
							memSvc.updateMember(memVO);
						}
					} catch (RuntimeException e) {
						Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo
								+ "][svfSendAuthResponse] DB warning: " + e);
						inLogObj.setThrowable(e.getMessage());
						
						return gts.common(ReturnCode.RequestExpired);
					} catch (SQLException e) {
						logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
						return gts.common(ReturnCode.DatabaseError);
					}
				}
			}
			
			if (returnNum.equals(ReturnCode.Success)) {
				try {
					if (txnStatus.equals(TxnStatus.WaitForVerify) && PATTERN_2.equals(authType)) {
						memSvc.updatePatternFailCounter(idgID, 0);
					} else if (txnStatus.equals(TxnStatus.WaitForVerify) && PIN_3.equals(authType)) {
						memSvc.updateDigitalFailCounter(idgID, 0);
					}
				} catch (SQLException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
				
				// validate
				// vdVO.getTransaction_Hash() = Step3: AESUtil.SHA256_To_HexString(txnData)
				if (!banktxnDataHash.equals(vdVO.getTransaction_Hash())) { 
					return gts.common(ReturnCode.TxnDataNotMatchInSendAuthResponse); // 2414
				}
				
				// check keyType
				if (!keyType.equals(vrVO.getVerify_Type())) { // Step3: AESUtil.SHA256_To_HexString(txnData)
					return gts.common(ReturnCode.KeyTypeNotMatchInSendAuthResponse);
				}
				// <BEGIN 暫時註解 >
				if (enableStep4OTP.equals(TRUE)) {
// 這一塊沒作用
//					List<NameValuePair> formparams = new ArrayList<>();
//					if ("01".equals(vrVO.getVerify_Type())) {
//						if (ZERO_0.equals(authType)) {
//							formparams.add(new BasicNameValuePair(METHOD, VERIFY_DEV_ID_OTP));
//						} else if (!PIN_3.equals(authType)) {
//							formparams.add(new BasicNameValuePair(METHOD, "verifyEsnOTP"));
//						} else {
//							formparams.add(new BasicNameValuePair(METHOD, VERIFY_OTP));
//						}
//					} else {
//						formparams.add(new BasicNameValuePair(METHOD, VERIFY_DEV_ID_OTP));
//					}

					// verify txn OTP
//			List<NameValuePair> formparams = new ArrayList<>();

					Map<String, String> paramMap = new HashMap<>();
					paramMap.put(METHOD, VERIFY_OTP);
					String seed = shaDeviceKeyB.substring(24, 64);
					paramMap.put(ESN, seed);
					paramMap.put(CHANNEL3, memVO.getChannel_Code());
					paramMap.put(OTP3, otp);
					paramMap.put(TIME_STAMP, time);

					String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vrVO.getTransaction_Date());
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** deviceData.substring(24, 64):" + deviceData.substring(24, 64));
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** seed:" + seed);
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** createTime:" + createTime);
//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** bankTxnID:" + bankTxnID);
					try {
						paramMap.put(TXN_DATA, AESUtil.SHA256_To_HexString(bankTxnID)); // txnData = sha256(bankTxnID)
						paramMap.put(CHALLENGE, AESUtil.SHA256_To_HexString(idgateID + createTime)); // challenge =
						// sha256(idgateID+createTime)
					} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
						logException(sessID, e, SVF_SEND_AUTH_RESPONSE, ERROR_OCCURRED, apLogObj, inLogObj);
						return gts.common(ReturnCode.HashGenFailedInSendAuthResponse);
					}

//			Log4j.log.debug("[" + sessID + "][svfSendAuthResponse] *** paramMap.toJson:" + gson.toJson(paramMap));

					HashMap<String, String> tsMsg = null;
					try {
						String retMsg = TS_Inst.verifyOTP(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
								CvnLib.Common.Filter.filter((String) paramMap.get(ESN)),
								CvnLib.Common.Filter.filter((String) paramMap.get(CHALLENGE)),
								CvnLib.Common.Filter.filter((String) paramMap.get(TXN_DATA)),
								CvnLib.Common.Filter.filter((String) paramMap.get(OTP3)),
								CvnLib.Common.Filter.filter((String) paramMap.get(TIME_STAMP)), sessID);

//				Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendAuthResponse][verifyEsnOTP] Reply from TS: " + retMsg);

						tsMsg = gson.fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
						}.getType());
					} catch (JsonSyntaxException e) {
						logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Unable to parse JSON", apLogObj, inLogObj);
						return gts.common(ReturnCode.TrustServerError);
					}

					if (SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
						// otp valid
						txnStatus = TxnStatus.Success;
						try {
							//					returnNum = ReturnCode.Success;
							vrSvc.updateVerify_Request(txnStatus, returnNum, new Timestamp(System.currentTimeMillis()),
									idgID, txnID, deviceOS);
						} catch (SQLException e) {
							logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
							return gts.common(ReturnCode.DatabaseError);
						}

					} else if (FAIL_0020.equals(tsMsg.get(RETURN_CODE))) {
						// otp invalid
						txnStatus = TxnStatus.Fail;
						try {
							returnNum = ReturnCode.OTPInvalidInSendAuthResponse;
							vrSvc.updateVerify_Request(txnStatus, returnNum, new Timestamp(System.currentTimeMillis()),
									idgID, txnID, deviceOS);
						} catch (SQLException e) {
							logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
							return gts.common(ReturnCode.DatabaseError);
						}

					} else if ("0007".equals(tsMsg.get(RETURN_CODE))) {
						return gts.common(ReturnCode.TrustServerError);
					} else {
						Log4j.log.warn("[" + sessID + "][Version: " + IDGateConfig.svVerNo
								+ "][svfSendAuthResponse] idgateID: " + (idgateID) + " account status abnormal.");
						return gts.common(ReturnCode.MemberStatusError);
					}
				}
				// <END 暫時註解 >
			}
			
			// OTP invalid 0020
			if (!returnNum.equals(ReturnCode.Success) && PATTERN_2.equals(authType)
					&& !TRUE.equals(IDGateConfig.testMode)) {
				return gts.get_FailCount(returnNum, String.valueOf(memVO.getPattern_Fails() + 1), null);
			} else if (!returnNum.equals(ReturnCode.Success) && PIN_3.equals(authType)
					&& !TRUE.equals(IDGateConfig.testMode)) {
				return gts.get_FailCount(returnNum, String.valueOf(memVO.getDigital_Fails() + 1), null);
			}
			
			// TODO UNREMARK BELOW {} 111.7.27
			if (txnStatus.equals(TxnStatus.Fail) && !TRUE.equals(IDGateConfig.testMode)) {
				return gts.get_FailCount(returnNum, String.valueOf(memVO.getMsg_Count() + 1), null); // Review
				// memVO.getMsg_Count()
			} // 2407
			// System.out.println("[" + sessID + "] after verify_OTP: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
			// Log4j.log.info("[{}] *** after verify_OTP@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
		} // End of if
		
		HashMap<String, String> uafRequest = new HashMap<String, String>();
		uafRequest.put("uafResponse", fidoStr);
		
		if (!TRUE.equals(onlyFIDO)) {
			// System.out.println("[" + sessID + "] before hit FIDO Server: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
			// Log4j.log.info("[{}] *** before hit FIDO Server@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
			
			String rsp = null;
			rsp = fidoUafResource.UAFResponse(gson.toJson(uafRequest), sessID);

			Map<String, Object> rspDataMap = null;
			try {
				rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
				});
			} catch (JsonSyntaxException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
				return gts.common(ReturnCode.JsonParseError);
			} catch (JsonMappingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
				return gts.common(ReturnCode.JsonParseError);
			} catch (JsonProcessingException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
				return gts.common(ReturnCode.JsonParseError);
			}
			
//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendAuthResponse] rspData: " + rspDataMap);
			
			if (rspDataMap == null) {
				Log4j.log.error("## ERROR ## rspData IS NULL");
				Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
				return gts.common(ReturnCode.TrustServerError);
			}
			String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
			String description = (String) rspDataMap.get(DESCRIPTION2);
			
			HashMap<String, String> authResult = new HashMap<String, String>();
//		Log4j.log.debug("[{}][svfSendAuthResponse] *** rtnCode:{}", sessID, rtnCode);
//		Log4j.log.debug("[{}][svfSendAuthResponse] *** description:{}", sessID, description);
			// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
			if (SUCCESS_1200.equals(rtnCode)) {
//				if(!enableStep4OTP.equals(TRUE)) {
				// FIDO PASS
				String txnStatus = TxnStatus.Success;
				try {
					String returnNum = ReturnCode.Success;
					vrSvc.updateVerify_Request(txnStatus, returnNum, new Timestamp(System.currentTimeMillis()), idgID,
							txnID, deviceOS);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
				rtnCode = ReturnCode.Success;
				String authenticatorRecord = (String) rspDataMap.get("newUAFRequest");

//			Log4j.log.debug("*** newUAFRequest:" + authenticatorRecord + "\n*** fidoRes:" + authenticatorRecord);

				authResult.put(IDGATE_ID, idgateID);
				authResult.put(FIDO_RES, authenticatorRecord);
				// Reset to 0 if otp is valid
				try {
					memSvc.updateAuthFailCounter(idgID, 0);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			} else {
				// FIDO FAIL
				String txnStatus = TxnStatus.Fail;
				try {
					//						String returnNum = ReturnCode.OTPInvalidInSendAuthResponse;
					vrSvc.updateVerify_Request(txnStatus, rtnCode, new Timestamp(System.currentTimeMillis()),
							idgID, txnID, deviceOS);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
				// FIDO簽章驗證錯誤也要記錄錯誤次數+1在Members.Auth_Fails 
				try {
					memSvc.updateAuthFailCounter(idgID, memVO.getAuth_Fails() + 1);
					return gts.get_FailCount(rtnCode, String.valueOf(memVO.getAuth_Fails() + 1), null);
				} catch (SQLException e) {
					logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
					return gts.common(ReturnCode.DatabaseError);
				}
			}
			
			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendAuthResponse] Response: "
					+ gts.send_AuthResponse(rtnCode, gson.toJson(authResult), description, txnID, ZERO_0));
			// System.out.println("[" + sessID + "] before return: " + new SimpleDateFormat("mm:ss.SSS").format(new Date()));
			// Log4j.log.info("[{}] *** before return@{}: {}ms \r\n", sessID, "svfSendAuthResponse", simpleDateFormat.format(new Date()));
//		logException(sessID, new Exception(new SimpleDateFormat("mm:ss.SSS").format(new Date())), "svfSendAuthResponse", "before return: ", apLogObj, inLogObj);
			return gts.send_AuthResponse(rtnCode, gson.toJson(authResult), description, txnID, ZERO_0);
		} else {
			// 壓測
			HashMap<String, String> authResult = new HashMap<String, String>();
			authResult.put(IDGATE_ID, idgateID);
			//		authResult.put("fidoRes", authenticatorRecord);
			//			if(!enableStep4OTP.equals(TRUE)) {
			// otp valid
			String txnStatus = TxnStatus.Success;
			try {
				String returnNum = ReturnCode.Success;
				vrSvc.updateVerify_Request(txnStatus, returnNum, new Timestamp(System.currentTimeMillis()),
						idgID, txnID, deviceOS);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_AUTH_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			return gts.send_AuthResponse(SUCCESS, gson.toJson(authResult), "", txnID, ZERO_0);
		}
	}
	
	/**
	 * Step3
	 * 
	 * @param input
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param inLogObj 
	 * @param apLogObj 
	 * @return
	 */
	private String svfGetAuthRequest(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(TITLE2, jSONObject.getString(TITLE2));
		map.put(IDGATE_ID, jSONObject.getString(IDGATE_ID));
		map.put(AUTH_TYPE, jSONObject.getString(AUTH_TYPE));
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));
		map.put(BANK_TXN_DATA, jSONObject.getString(BANK_TXN_DATA));
		map.put(KEY_TYPE, jSONObject.getString(KEY_TYPE));

		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_GET_AUTH_REQUEST, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		// Review 目前沒有authType=0的驗證
		String title = map.get(TITLE2);
		String idgateID = map.get(IDGATE_ID);
		String authType = map.get(AUTH_TYPE);
		String channel = map.get(CHANNEL2);
		String bankTxnData = map.get(BANK_TXN_DATA);
		String keyType = map.get(KEY_TYPE);
		long idgID = Long.parseLong(idgateID);

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetAuthRequest] idgateID: " + (idgateID)
				+ ", authType: " + (authType) + ", channel: " + (channel) + ", title: " + (title) + ", bankTxnData: "
				+ (bankTxnData) + ", keyType: " + (keyType));

		ChannelService chSvc;
		try {
			chSvc = new ChannelService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		Device_DetailVO ddVO = new Device_DetailVO();
		MembersVO memVO = null;
		ChannelVO chVO = null;
		try {
			chVO = chSvc.getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
			
			memVO = memSvc.getByIdgateID(idgID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		if (memVO == null || ddVO == null) {
			logException(sessID, new Exception("Member Not Found in Device_Detail"), SVF_GET_AUTH_REQUEST, "Member Not Found in Device_Detail", apLogObj, inLogObj);
			return gts.common(ReturnCode.MemberNotFound);
		} else {
			String memberStatus = memVO.getCustomer_Status();
			if (memberStatus.equals(MemberStatus.Deleted)) {
				return gts.common(ReturnCode.MemberDeletedError);
			} else if (memberStatus.equals(MemberStatus.Locked)
					|| memberStatus.equals(MemberStatus.LockedForTooManyAuthFails)) {
				return gts.common(ReturnCode.MemberLockedError);
			} else if (!memberStatus.equals(MemberStatus.Normal)) {
				return gts.common(ReturnCode.MemberStatusError);
				// 111.12.8 可以跨channel驗證 ( svfGetAuthRequest, svfSendAuthResponse不檢核channel)
//		} else if (!channel.equals(memVO.getChannel_Code())) {
//			return gts.common(ReturnCode.ChannelNotMatchToMember);
			}
		}
		
		// Fix bug:111.12.8 可以跨channel驗證後，未檢核 Channel
		try {
			if (!isChannelValid(sessID, channel)) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "NamingException::DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_AUTH_RESPONSE, "SQLException::DB Error occurred", apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

//		Log4j.log.info("*** ddVO.getAuth_Type():" + ddVO.getAuth_Type());

		// check if multiple auth type is given
		if (authType.length() > 1) {
			for (int i = 0; i < authType.length(); i++) {
//				Log4j.log.info("*** authType:" + authType.substring(i, i + 1));
				if (ddVO.getAuth_Type().indexOf(authType.substring(i, i + 1)) == -1) { // 指定驗證模式須在註冊(Device_Detail)的範圍內
					return gts.common(ReturnCode.InvalidAuthTypeInGetAuthRequest);
				}
			}
			// check if this auth type is not part of active auth type
		} else if (ddVO.getAuth_Type().indexOf(authType) == -1) {
			return gts.common(ReturnCode.InvalidAuthTypeInGetAuthRequest);
		}
		
		// Fixed 待驗證交易不會擋，驗證時才會擋
		List<Blocked_Device_AuthVO2> channelAndModelList = new ArrayList<>();
		try {
			String upperLabel = ddVO.getDeviceLabel().toUpperCase();
			String upperModel = ddVO.getDeviceModel().toUpperCase();
			
			String convertedAuthType = this.changeAuthTypeForTxn(authType);
			channelAndModelList = new Blocked_Device_AuthService(JNDI_Name, sessID).getList2(upperLabel, upperModel, convertedAuthType, memVO.getChannel_Code());
			
			if (!channelAndModelList.isEmpty()) {
				return gts.common(ReturnCode.BlockedAuthTypeInGetAuthRequest);
			}
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		Verify_RequestVO vrVO = new Verify_RequestVO();
		vrVO.setiDGate_ID(idgID);
		vrVO.setChannel_Code(channel);
		vrVO.setTransaction_Name(title);
		vrVO.setStatus_Code(TxnStatus.WaitForVerify);
		vrVO.setAuth_Mode(authType);
		vrVO.setVerify_Type(keyType);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Log4j.log.debug("[{}][Version: {}][svfGetAuthRequest] *** Verify_Request:[{}]", sessID,
				IDGateConfig.svVerNo, gson.toJson(vrVO));
		
//		Log4j.log.info("*** lastJNDI:{}", lastJNDI);
		Verify_DetailService vdSvc;
		try {
			String chJNDI = chVO.getJNDI();
			vdSvc = new Verify_DetailService(chJNDI, sessID);
//		String lastJNDI = chJNDI.substring(chJNDI.lastIndexOf("/") + 1, chJNDI.length());
//			vdSvc = new Verify_DetailService("java:comp/env/jdbc/" + lastJNDI, sessID);	// JAR
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		Verify_RequestService vrSvc;
		try {
			vrSvc = new Verify_RequestService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		long requestId = -1;
		String requestIdStr = null;
		try {
			// a table that has an AUTO_INCREMENT column
			requestId = vrSvc.addVerify_Request(vrVO); // txnID:0
			requestIdStr = String.valueOf(requestId);
			vrVO = vrSvc.getOne(idgID, requestIdStr);
			vdSvc.addVerify_DetailVO(idgID, requestIdStr, channel, "", title, dateFormat.format(new Date()), "", bankTxnData,
					AESUtil.SHA256_To_HexString(bankTxnData), null, vrVO.getTransaction_Date());
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.HashGenFailedInGetAuthRequest);
		}
		// 取回 Step2 svfSendRegResponse 存的 username
		PubkeyStoreService frontKeyStoreSVC;
		try {
			frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		PubkeyStoreVO pkStore = null;
		try {
			pkStore = frontKeyStoreSVC.getByIdgateID(idgID);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		if (pkStore == null) {
			Log4j.log.error("## ERROR ## pkStore IS NULL");
			Log4j.log.error("## ERROR ## Cannot find deviceData In pub_Key_Store for idgateID:" + idgID);
			return gts.common(ReturnCode.MemberNotFound);
		}
		String username = pkStore.getAlias();

		Map<String, Object> encTxnDataMap = new HashMap<>();
		encTxnDataMap.put(STATUS, TxnStatus.WaitForVerify);
		encTxnDataMap.put(AUTH_TYPE, authType);
		encTxnDataMap.put(KEY_TYPE, keyType);
		encTxnDataMap.put(BANK_TXN_DATA, bankTxnData);
		String now = String.valueOf(System.currentTimeMillis());
		encTxnDataMap.put(TIME, now);	// 存到 DB serverTime
		String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vrVO.getTransaction_Date());
		encTxnDataMap.put(CREATE_TIME, createTime);
		String txnId = new SimpleDateFormat(YYYY_MM_DD).format(vrVO.getTransaction_Date()) + requestIdStr;
		encTxnDataMap.put(TXN_ID, txnId);	// txnId = yyyyMMdd + requestId
		
//		Map<String, Object> bankTxnDataMap = new HashMap<>();
//		try {
//			bankTxnDataMap = new ObjectMapper().readValue(bankTxnData, new TypeReference<Map<String, Object>>() {
//			});
//		} catch (JsonMappingException e) {
//			logException(sessID, e, "svfGetAuthRequest", "Unable to parse JSON", apLogObj, inLogObj);
//			return gts.common(ReturnCode.JsonParseError);
//		} catch (JsonProcessingException e) {
//			logException(sessID, e, "svfGetAuthRequest", "Unable to parse JSON", apLogObj, inLogObj);
//			return gts.common(ReturnCode.JsonParseError);
//		}

		String idgateIDPubKey = pkStore.getPub_key();
		String deviceData = pkStore.getDevice_data();
		if (deviceData.contains("ERROR")) {
			logException(sessID, new Exception("deviceData HAS ERROR"), SVF_GET_AUTH_REQUEST, "deviceData HAS ERROR", apLogObj, inLogObj);
			return gts.common(ReturnCode.MemberNotFound);
		}
		String jsonEncTxnDataMap = gson.toJson(encTxnDataMap);

//		Log4j.log.info("*** txnDataMap:" + txnDataMap);
//		Log4j.log.info("*** jsonTxnDataMap:" + jsonTxnDataMap);

		// For getTxnList 建交易時的encTxnData
		String encTxnData = null;
	
		HashMap<String, String> uafRequest = new HashMap<String, String>();
		uafRequest.put(OP, "Auth");
		Map<String, String> context = new HashMap<String, String>();
		context.put(USERNAME2, username);
		context.put("transaction", bankTxnData);
		context.put(CHANNEL2, channel);
		context.put(KEY_TYPE, keyType);
		uafRequest.put(CONTEXT2, gson.toJson(context));

		Map<String, Object> rspDataMap = null;
		String rsp = fidoUafResource.GetUAFRequest(gson.toJson(uafRequest), sessID);
		try {
			// parse json
			rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonMappingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonProcessingException e) {
			logException(sessID, e, SVF_GET_AUTH_REQUEST, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetAuthRequest] rspData: " + rspDataMap);

		if (rspDataMap == null) {
			Log4j.log.error("## ERROR ## rspData IS NULL");
			Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
			return gts.common(ReturnCode.TrustServerError);
		}
		String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
		// KEEP
//		String description = (String) rspDataMap.get("Description");

//		Log4j.log.info("*** uafRequest@AuthRequest:" + rspDataMap.get("uafRequest"));
		// Log4j.log.debug("[{}]*** rtnCode:{}", sessID, rtnCode);
		// Log4j.log.debug("[{}]*** description:{}", sessID, description);

		String encAuthReq = null;
		String serverPubKey = null;
		String encTxnID = null;
		// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
		if (SUCCESS_1200.equals(rtnCode)) {
			rtnCode = ReturnCode.Success;
			String fidoReq = gson.toJson(rspDataMap.get(UAF_REQUEST));

			// Log4j.log.debug("*** fidoReq:" + fidoReq);

			HashMap<String, String> AuthReqMap = new HashMap<String, String>();
			AuthReqMap.put(TITLE2, title);
			AuthReqMap.put(STATUS, TxnStatus.WaitForVerify);
			AuthReqMap.put(AUTH_TYPE, authType);
			AuthReqMap.put(KEY_TYPE, keyType);
			AuthReqMap.put(BANK_TXN_DATA, bankTxnData);
			AuthReqMap.put(TIME, now);
			AuthReqMap.put(CREATE_TIME, createTime);
//			rtnTxnIDStr = new SimpleDateFormat("yyyyMMdd").format(vrVO.getTransaction_Date()) + requestIdStr;
//			AuthReqMap.put("txnID", rtnTxnIDStr);
			AuthReqMap.put(TXN_ID, txnId);
//			AuthReqMap.put("verifyType", verifyType);
			AuthReqMap.put("fidoReq", fidoReq);

			if (StringUtils.isBlank(webpinAppKey)) {
				// Server’s public key
				serverPubKey = RSA.loadPublicKey(rsaKeyAlias, sessID);
				if (serverPubKey.contains(ERROR2)) {
					logException(sessID, new Exception("Loading RSA public key failed:"
							+ serverPubKey), SVF_GET_AUTH_REQUEST, "Loading RSA public key failed", apLogObj, inLogObj);
					return gts.common(ReturnCode.KeyGenErr);
				}
				webpinAppKey = serverPubKey;
			} else {
				serverPubKey = webpinAppKey;
			}
			
			// RandomStringUtils.random
			SecureRandom random = new SecureRandom();  
			byte[] bytes = new byte[8];
			random.nextBytes(bytes);
			String capRandomKey4Enc = Encode.byteToHex(bytes);
			//以idgateIDPubKey加密、長度12以上隨機字串產生
			String rsaRandomKey = RSA.encryptWithFrontEndPubKey(capRandomKey4Enc, RS_AKEY_ALIAS, PATH_LOCATION, sessID,
					idgateIDPubKey);
			try {
				encTxnData = this.genEncData0fStep3(sessID, jsonEncTxnDataMap, rsaRandomKey, deviceData, 8, 24, SVF_GET_AUTH_REQUEST, apLogObj, inLogObj, capRandomKey4Enc);
			} catch (EncryptFailedException e) {
				return gts.common(ReturnCode.EncryptFailedInGetAuthRequest);
			}

//			// store encrypted txn data
//			try {
//				vdSvc.upadte_Txn_Deatil(idgID, requestIdStr,
//						new SimpleDateFormat("yyyyMMdd").format(vrVO.getTransaction_Date()), encTxnData);
//			} catch (SQLException e) {
//				logException(sessID, e, "svfGetAuthRequest", "DB Error occurred", apLogObj, inLogObj);
//				return gts.common(ReturnCode.DatabaseError);
//			}
		
			// encAuthReq
			String jsonAuthReqMap = gson.toJson(AuthReqMap);

			 String deviceKeyB = deviceData.substring(32, 64);
			 try {
				 // encTxnID	String	AES()加密後的字串，Key = deviceKeyB[12, 27])
				encTxnID = this.getEncData(txnId, deviceKeyB, 12, 28);
			} catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException
					| NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
					| BadPaddingException e) {
				logException(sessID, e, SVF_GET_AUTH_REQUEST, "Error occurred", apLogObj, inLogObj);
				return gts.common(ReturnCode.EncryptFailedInGetAuthRequest);
			}
			try {
				encAuthReq = this.genEncData0fStep3(sessID, jsonAuthReqMap, rsaRandomKey, deviceData, 2, 18, SVF_GET_AUTH_REQUEST, apLogObj, inLogObj, capRandomKey4Enc);
			} catch (EncryptFailedException e) {
				return gts.common(ReturnCode.EncryptFailedInGetAuthRequest);
			}
			
//			Log4j.log.debug("[{}][Version: {}][svfGetAuthRequest] *** encAuthReq:[{}]", sessID,
//					IDGateConfig.svVerNo, encAuthReq);
			// store encrypted txn data
			try {
				vdSvc.upadte_Txn_Deatil2(idgID, requestIdStr,
						new SimpleDateFormat(YYYY_MM_DD).format(vrVO.getTransaction_Date()), encTxnData, now, rsaRandomKey, encAuthReq);
			} catch (SQLException e) {
				logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} else {
			// FIDO簽章驗證錯誤也要記錄錯誤次數+1在Members.Auth_Fails 
			int auth_Fails = memVO.getAuth_Fails();
//			System.out.println("*** memVO.getAuth_Fails(): " + auth_Fails);
			try {
				memSvc.updateAuthFailCounter(idgID, auth_Fails + 1);
				return gts.get_FailCount(rtnCode, String.valueOf(auth_Fails + 1), null);
			} catch (SQLException e) {
				logException(sessID, e, SVF_GET_AUTH_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}

		String returnVal = gts.get_AuthRequest(rtnCode, encAuthReq, serverPubKey, txnId, encTxnData, encTxnID);
//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetAuthRequest] response: "
//				+ returnVal);

		return returnVal;
	}
	
	private String genEncData0fStep3(String sessID, String jsonRegResult, String rsaRandomKey, String deviceData,
			int beginIndex, int endIndex, String funcName, APLogFormat apLogObj, InboundLogFormat inLogObj,
			String capRandomKey4Enc) throws EncryptFailedException {
		String encResult = null;
		try {
			// deviceKeyB = sha256(deviceData[32,63])
			String deviceKeyB = deviceData.substring(32, 64);
			String shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			// AESKey = sha256(randomKey + deviceKeyB[2, 17])
			String shaDevKeyB16 = shaDeviceKeyB.substring(beginIndex, endIndex);
			String randomKeyPlusDeviceKeyB16 = capRandomKey4Enc + shaDevKeyB16;
			byte[] keyBytes = AESUtil.SHA256_To_Bytes(randomKeyPlusDeviceKeyB16);
			// unused
//			byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			
			//以隨機字串的hash為(AES(256))加密後的字串
			String encData4Result = Encode
					.byteToHex(AESUtil.encrypt(null, keyBytes, jsonRegResult.getBytes(StandardCharsets.UTF_8)));
			
//			Log4j.log.debug("*** rsaRandomKey:" + rsaRandomKey);
//			System.out.println("*** rsaRandomKey:" + rsaRandomKey);
//			Log4j.log.debug("*** deviceData:" + deviceData);
//			Log4j.log.debug("*** deviceKeyB:[{}]", deviceKeyB);
//			Log4j.log.debug("*** shaDeviceKeyB:[{}]", shaDeviceKeyB);
//			Log4j.log.debug("*** randomKey4Enc:[{}]", capRandomKey4Enc);
//			Log4j.log.debug("*** shaDevKeyB16:[{}]", shaDevKeyB16);
//			Log4j.log.debug("*** randomKeyPlusDeviceKeyB16:[{}]", randomKeyPlusDeviceKeyB16);
//			Log4j.log.debug("*** keyBytes(byteToHex:[{}]", Encode.byteToHex(keyBytes));
//			Log4j.log.debug("*** encData4Result:[{}]", encData4Result);
			
			Map<String, String> webPinMap = new HashMap<>();
			webPinMap.put(KEY, rsaRandomKey);	
			webPinMap.put(DATA, encData4Result); 
			
			String jsonWebPin = gson.toJson(webPinMap);
			encResult = Base64.getUrlEncoder().encodeToString(jsonWebPin.getBytes(StandardCharsets.UTF_8));
			// XXX TEST ONLY
//			throw new EncryptFailedException();
			return encResult;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			logException(sessID, e, funcName, ERROR_OCCURRED, apLogObj, inLogObj);
			throw new EncryptFailedException(e);
		}
	}

	private String getEncData(String sessID, String jsonRegResult, String idgateIDPubKey, String deviceData,
			int beginIndex, int endIndex, String funcName, APLogFormat apLogObj, InboundLogFormat inLogObj)
			throws EncryptFailedException {
		String encResult = null;
		try {
			// RandomStringUtils.random
			SecureRandom random = new SecureRandom();  
			byte[] bytes = new byte[8];
			random.nextBytes(bytes);
			String capRandomKey4Enc = Encode.byteToHex(bytes);
			//以idgateIDPubKey加密、長度12以上隨機字串產生
			String rsaRandomKey = RSA.encryptWithFrontEndPubKey(capRandomKey4Enc, RS_AKEY_ALIAS, PATH_LOCATION, sessID,
					idgateIDPubKey);

			// deviceKeyB = sha256(deviceData[32,63])
			String deviceKeyB = deviceData.substring(32, 64);
			String shaDeviceKeyB = AESUtil.SHA256_To_HexString(deviceKeyB);
			// AESKey = sha256(randomKey + deviceKeyB[2, 17])
			String shaDevKeyB16 = shaDeviceKeyB.substring(beginIndex, endIndex);
			String randomKeyPlusDeviceKeyB16 = capRandomKey4Enc + shaDevKeyB16;
			byte[] keyBytes = AESUtil.SHA256_To_Bytes(randomKeyPlusDeviceKeyB16);
			// unused
//			byte[] ivBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

			//以隨機字串的hash為(AES(256))加密後的字串
			String encData4Result = Encode
					.byteToHex(AESUtil.encrypt(null, keyBytes, jsonRegResult.getBytes(StandardCharsets.UTF_8)));

//			Log4j.log.debug("*** rsaRandomKey:" + rsaRandomKey);
//			System.out.println("*** rsaRandomKey:" + rsaRandomKey);
//			Log4j.log.debug("*** deviceData:" + deviceData);
//			Log4j.log.debug("*** deviceKeyB:[{}]", deviceKeyB);
//			Log4j.log.debug("*** shaDeviceKeyB:[{}]", shaDeviceKeyB);
//			Log4j.log.debug("*** randomKey4Enc:[{}]", capRandomKey4Enc);
//			Log4j.log.debug("*** shaDevKeyB16:[{}]", shaDevKeyB16);
//			Log4j.log.debug("*** randomKeyPlusDeviceKeyB16:[{}]", randomKeyPlusDeviceKeyB16);
//			Log4j.log.debug("*** keyBytes(byteToHex:[{}]", Encode.byteToHex(keyBytes));
//			Log4j.log.debug("*** encData4Result:[{}]", encData4Result);

			Map<String, String> webPinMap = new HashMap<>();
			webPinMap.put(KEY, rsaRandomKey);	
			webPinMap.put(DATA, encData4Result); 

			String jsonWebPin = gson.toJson(webPinMap);
			encResult = Base64.getUrlEncoder().encodeToString(jsonWebPin.getBytes(StandardCharsets.UTF_8));
			// XXX TEST ONLY
//			throw new EncryptFailedException();
			return encResult;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			logException(sessID, e, funcName, ERROR_OCCURRED, apLogObj, inLogObj);
			throw new EncryptFailedException(e);
		}
	}

	/**
	 * Step2
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param inLogObj 
	 * @param apLogObj 
	 * @return
	 */
	private String svfSendRegResponse(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
		map.put(DEVICE_INFO, jSONObject.get(DEVICE_INFO).toString());
		map.put(TRANSACTION_ID, jSONObject.get(TRANSACTION_ID).toString());
		map.put(DATA, jSONObject.get(ENC_REG_RES).toString());
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_SEND_REG_RESPONSE, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String deviceInfo = map.get(DEVICE_INFO);
		String transactionID = map.get(TRANSACTION_ID); // fixed: 存起來就好，供查詢及寫Log用
		String encRegRes = map.get(DATA); // Base64.encode(Webpin構造產生的加密字串)
		String channel = map.get(CHANNEL2); // 管道
		// KEEP
//		String serverData = null; // USE FAKE

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendRegResponse] transactionID: " + transactionID
				+ ", deviceInfo:" + deviceInfo + ", encRegRes:" + encRegRes + ", channel: " + channel);

		ChannelVO chVO = null;
		try {
			chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
//		if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
//			return gts.common(ReturnCode.ChannelInvalidInSendRegResponse);
//		}
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		// deviceInfo
		HashMap<String, String> devInfo = null;
		try {
			devInfo = gson.fromJson(deviceInfo, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to parse JSON", apLogObj, inLogObj);
			return gts.common(ReturnCode.TrustServerError);
		}
		if (!devInfo.containsKey(DEVICE_LABEL) || !devInfo.containsKey(DEVICE_MODEL)
				|| !devInfo.containsKey(DEVICE_OS) || !devInfo.containsKey(DEVICE_NAME)
				|| !devInfo.containsKey(DEVICE_IP) || !devInfo.containsKey(USER_ID2)) {
			Log4j.log.error("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfSendRegResponse] deviceInfo is either missing key(s) or key name incorrect.");

			return gts.common(ReturnCode.JSONErrInSendRegResponse);
		}
		
//		userID去識別化
		String bankId = devInfo.get(USER_ID2);
//		Log4j.log.info("*** devInfo.get(\"userID\"):{}", bankId);
		
		String encRegResB64Decode = null;
		try {
			encRegResB64Decode = new String(Base64.getUrlDecoder().decode(encRegRes), StandardCharsets.UTF_8);
		} catch (Exception e) {
			Log4j.log.debug("*** encRegRes:{}", encRegRes);
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to process encRegRes", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInSendRegResponse);
		}

//		Log4j.log.info("*** encRegResB64Decode:{}", encRegResB64Decode);

		HashMap<String, String> dataMap = null;
		try {
			dataMap = gson.fromJson(encRegResB64Decode, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JSONException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to parse JSON", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSendRegResponse);
		}

//		Log4j.log.info("*** key:[{}]", dataMap.get("key"));
//		Log4j.log.info("*** data:[{}]", dataMap.get("data"));

		String decryptedData = null;
		try {
			decryptedData = this.decryptRSA(sessID, dataMap.get(KEY), "", dataMap.get(DATA));
		} catch (Exception e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to decrypt data", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInSendRegResponse);
		}

//		Log4j.log.info("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendRegResponse] Decrypted data: " + decryptedData);

		if (decryptedData.indexOf("ReturnCode\":\"0000\"") == -1) {
			return gts.common(ReturnCode.DecryptFailedInSendRegResponse);
		}

		JSONObject json = null;
		try {
			json = new JSONObject(decryptedData);
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to parse JSON", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSendRegResponse);
		}
		
		JSONObject jsonRegResData = null;
		String idgateID = null;
		String idgateIDPubKey = null;
		String idgateIDECCPubKey = null;
		String deviceData = null;
		String pin;
		String fidoStr;
		String msgCount;
		String authType;
		String push;
		String pattern;
		String bio;
		try {
			jsonRegResData = json.getJSONObject(DATA2);
			idgateIDPubKey = jsonRegResData.getString("idgateIDPubKey");
			// encRegRes增加 idgateIDECCPubKey
			if (jsonRegResData.has("idgateIDECCPubKey")) {
				idgateIDECCPubKey = jsonRegResData.getString("idgateIDECCPubKey");
				Log4j.log.debug("[{}][Version: {}][svfSendRegResponse] *** idgateIDECCPubKey:[{}]", sessID,
						IDGateConfig.svVerNo, idgateIDECCPubKey);
				mobilePubKey = idgateIDECCPubKey;
			} 
			deviceData = jsonRegResData.getString(DEVICE_DATA);
			idgateID = jsonRegResData.getString(IDGATE_ID);
			pin = jsonRegResData.getString(PIN);
			fidoStr = jsonRegResData.getString("fido");
			msgCount = jsonRegResData.getString(MSG_COUNT);
			authType = jsonRegResData.getString(AUTH_TYPE);
			push = jsonRegResData.has(PUSH) ? jsonRegResData.getString(PUSH) : "-";
			pattern = jsonRegResData.getString(PATTERN);
			bio = jsonRegResData.getString(BIO);
		} catch (JSONException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "JSONErrInSendRegResponse", apLogObj, inLogObj);
			return gts.common(ReturnCode.JSONErrInSendRegResponse);
		}

		Log4j.log.debug("[{}][Version: {}][svfSendRegResponse] *** authType:[{}]", sessID,
				IDGateConfig.svVerNo, authType);
		
		// XXX 111.11.25 試重現 "IdgateId": null,
//		String idgateID = null;
		long idgID;
		try {
			idgID = Long.parseLong(idgateID);
		} catch (NumberFormatException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNEXPECTED_VALUE, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}
		// msgCount是SDK註冊後，每次產一筆驗證資料都會網上+1，Server要存msgCount來檢核，SDK丟的msgCount都要>Server存的msgCount（等於也不行）
//		String msgCount = dataObj.getString("msgCount"); // ??

		// 取出前端傳來的 serverData 內的 username
		String username;
		try {
			username = this.getUserName(fidoStr, sessID, "svfSendRegResponse", apLogObj, inLogObj);
		} catch (Exception e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to process UserName", apLogObj, inLogObj);
			return gts.common(ReturnCode.DecryptFailedInSendRegResponse);
		}

		// Device_Detail
		Device_DetailService ddSvc;
		try {
			ddSvc = new Device_DetailService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgID);	// step1 產 idgID
			
			if (memVO == null) {
				return gts.common(ReturnCode.MemberNotFound);
			} else {
				String customer_Status = memVO.getCustomer_Status();
				if (customer_Status.equals(MemberStatus.Deleted)) {
					return gts.common(ReturnCode.MemberDeletedError);
				} else if (customer_Status.equals(MemberStatus.Locked)
						|| customer_Status.equals(MemberStatus.LockedForTooManyAuthFails)) {
					return gts.common(ReturnCode.MemberLockedError);
				} else if (!customer_Status.equals(MemberStatus.Register) 
						&& !TRUE.equals(IDGateConfig.testMode)) { // step1 後的狀態是 3
					return gts.common(ReturnCode.MemberStatusError);
				}
			}

			//			Log4j.log.info("*** msgCount@svfSendRegResponse:" + msgCount);
//			Log4j.log.info("*** memVO.getMsg_Count()@svfSendRegResponse:" + memVO.getMsg_Count());
			// TODO: 改為 >=
			boolean isMsgCountInvalid = memVO.getMsg_Count() > Long.parseLong(msgCount);
			if (isMsgCountInvalid && !TRUE.equals(IDGateConfig.testMode)) {
				return gts.common(ReturnCode.MsgCountInvalidInSendRegResponse);
			}
			memSvc.updateMsgCounter(idgID, Long.parseLong(msgCount));

			// TODO REMARK BELOW
//			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo +
//					"][svfSendRegResponse][UPDATE_MSGCOUNT] \n*** msgCount@svfSendRegResponse: "
//					+ msgCount + "\n *** memVO.getMsg_Count()@svfSendRegResponse:" +
//					memVO.getMsg_Count() + "\n DB msgCount > front msgCount(FALSE):" +
//					isMsgCountInvalid);

		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		// 112.1.18 註冊時檢查黑名單
		String deviceBrand = devInfo.get(DEVICE_LABEL);
		String dvcModel = devInfo.get(DEVICE_MODEL);
		List<Blocked_Device_AuthVO2> channelAndModelList = new ArrayList<>();
		try {
			String upperLabel = deviceBrand.toUpperCase();
			String upperModel = dvcModel.toUpperCase();
			// 註冊 AUTH_TYPE 包含 0 => ALL 	包含 1 => 1 	包含 2 => 2 	包含 3 => 3
			String convertedAuthType = this.changeAuthType(authType);
			channelAndModelList = new Blocked_Device_AuthService(JNDI_Name, sessID).getList2(upperLabel, upperModel, convertedAuthType, memVO.getChannel_Code());

			if (!channelAndModelList.isEmpty()) {
				return gts.common(ReturnCode.BlockedAuthTypeInSendRegResponse);
			}
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		PubkeyStoreService frontKeyStoreSVC;
		try {
			frontKeyStoreSVC = new PubkeyStoreService(JNDI_Name, sessID);
			// 112.3.14 Table PUB_KEY_STORE  再加個欄位，存 idgateIDECCPubKey
			frontKeyStoreSVC.addPubkeyStore(idgID, username, deviceData, idgateIDPubKey, idgateIDECCPubKey);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (SQLException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}

		// TODO REMARK BELOW
		// Log4j.log.debug( "[" + sessID + "][Version: " + IDGateConfig.svVerNo +
		// "][svfSendRegResponse][SAVE_USERNAME] username: " + username);

		// 111.8.2 ddSvc.update_Pattern_Hash
		// CREATE_DEVICE
//			/* TS 創建會員 */
//			List<NameValuePair> formparams = new ArrayList<>();
		Map<String, String> paramMap = new HashMap<>();
//		paramMap.put(METHOD, SIGNUP_PERSO);
		paramMap.put(CHANNEL3, channel);
		paramMap.put(DEV_TYPE, "4");
		paramMap.put(USER_ID, String.valueOf(System.currentTimeMillis()));
		paramMap.put(SEED_SECRET, "");
		paramMap.put(ESN_SECRET, "");
		paramMap.put(MASTER_SECRET, "");
		paramMap.put(DEV_DATA, deviceData);
		paramMap.put(PIN_HASH, pin);

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendRegResponse][signup_Perso] Sending to TS: " + gson.toJson(paramMap));
		HashMap<String, String> tsMsg = null;
		String retMsg;
		retMsg = TS_Inst.signupPersoFIDO(CvnLib.Common.Filter.filter((String) paramMap.get(CHANNEL3)),
				CvnLib.Common.Filter.filter((String) paramMap.get(DEV_TYPE)),(String) paramMap.get(ESN_SECRET),
				(String) paramMap.get(SEED_SECRET), (String) paramMap.get(MASTER_SECRET),
				CvnLib.Common.Filter.filter((String) paramMap.get(USER_ID)),
				CvnLib.Common.Filter.filter((String) paramMap.get(DEV_DATA)),
				CvnLib.Common.Filter.filter((String) paramMap.get(PIN_HASH)), sessID);

//			if (channel.length() > 5 || devType.length() != 1 || userID.length() > 15) {
//			rspData.put("ReturnCode", "0010");
//			rspData.put("ReturnMsg", "Invalid parameter.");

		tsMsg = gson.fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
		}.getType());

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendRegResponse][CREATE_DEVICE] Reply from TS: " + retMsg);

		if (!SUCCESS.equals(tsMsg.get(RETURN_CODE))) {
			return gts.common(ReturnCode.TrustServerError);
		}
		
		// 驗證類型(預設01)
//		Log4j.log.debug("*** sessID:" + sessID);
//		Log4j.log.debug("*** esn:" + esn);
		// Fix bankId 的值未更新到 Members
//		System.out.println("*** devInfo.get(\"userID\"): " +bankId);
//		System.out.println("*** IDGateConfig.testMode: " +IDGateConfig.testMode);
//		System.out.println(" !\"true\".equals(IDGateConfig.testMode) : " + !"true".equals(IDGateConfig.testMode));
		
		// XXX 111.11.24 "throwable": "資料表 'FID_RP.fido.Device_Detail'，資料行 'Device_Reg_IP' 中的字串或二進位資料將會截斷。
//		if (true) {
		
		if (!TRUE.equals(IDGateConfig.testMode)) {
			try {
				// push暫留，server收到先存起來即可
				// authtype = 0 只要 otp驗過就算，而且基本上註冊不會只有0
				// digital -> pin
				// type -> authType
				// esn -> transactionID
				ddSvc.addDevice_Detail(idgID, tsMsg.get(ESN), deviceData,
						push, BIO_1, pin,
						jsonRegResData.has(PATTERN) ? pattern : "-",
						jsonRegResData.has(BIO) ? bio : "-",
						jsonRegResData.has(AUTH_TYPE) ? authType : "01",
						devInfo.get(DEVICE_OS), NOT_AVAILABLE,	// 111.12.21 iDenKeyFIDO APP帶入的裝置IP需轉換格式並儲存DB
						deviceBrand,
						dvcModel,
						devInfo.get(DEVICE_IP),
						devInfo.get(DEVICE_OS_VER),
						devInfo.get(APP_VER), transactionID);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}
		
		HashMap<String, String> uafRequest = new HashMap<String, String>();
		// fixed
//		uafRequest.put("op", "Reg");
		uafRequest.put("uafResponse", fidoStr); // 用前端的 header
		uafRequest.put(CONTEXT2, "{\"username\":\"" + username + "\"}"); // 正式用
		uafRequest.put(CHANNEL2, channel); // svfGetRegRequest回覆的regReq要從DB取得

		String rsp = fidoUafResource.UAFResponse(gson.toJson(uafRequest), sessID);
		Map<String, Object> rspDataMap = null;
		try {
			// parse json
//					rspDataMap = gson.fromJson(rsp, new TypeToken<Map<String, Object>>() {
//					}.getType());
			rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonMappingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonProcessingException e) {
			logException(sessID, e, SVF_SEND_REG_RESPONSE, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendRegResponse] rspData: " + rspDataMap);

		if (rspDataMap == null) {
			Log4j.log.error("## ERROR ## rspData IS NULL");
			Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
			return gts.common(ReturnCode.TrustServerError);
		}
		String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
		String description = (String) rspDataMap.get(DESCRIPTION2);
		String encRegResult = null;

		// Log4j.log.debug("[{}]*** rtnCode:{}", sessID, rtnCode);
		// Log4j.log.debug("[{}]*** description:{}", sessID, description);
		// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
		if (SUCCESS_1200.equals(rtnCode)) {
			rtnCode = ReturnCode.Success;
			String registrationRecords = (String) rspDataMap.get("newUAFRequest");

//			Log4j.log.info("[{}] *** newUAFRequest:[{}]\n*** fidoRes:[{}]",sessID,registrationRecords,registrationRecords);

			HashMap<String, String> regResult = new HashMap<String, String>();
			regResult.put(IDGATE_ID, idgateID);
			regResult.put(FIDO_RES, registrationRecords);
			// 112.3.14 encRegResult 增加authType、authHash
			boolean hasDevice = jsonRegResData.has(DEVICE);
			String device = jsonRegResData.getString(DEVICE);
			if (hasDevice) {
				 Log4j.log.debug("[{}][svfSendRegResponse]*** device:{}", sessID, device);
			} else {
				Log4j.log.debug("[{}][svfSendRegResponse]*** NO_DEVICE:{}", sessID, "NO DEVICE");
			}
			regResult.put(AUTH_TYPE, authType);
			if (authType.indexOf(BIO_1) > -1) {
				regResult.put(AUTH_HASH, bio);
			} else if (authType.indexOf(PATTERN_2) > -1) {
				regResult.put(AUTH_HASH, pattern);
			} else if (authType.indexOf(PIN_3) > -1) {
				regResult.put(AUTH_HASH, pin);
			} else {
				regResult.put(AUTH_HASH, hasDevice? device : "NO_device");
			}
			// AESUtil.SHA256_To_HexString(idgateID);
			String jsonRegResult = gson.toJson(regResult);
			try {
				encRegResult = this.getEncData(sessID, jsonRegResult, idgateIDPubKey, deviceData, 0, 16,
						SVF_SEND_REG_RESPONSE, apLogObj, inLogObj);
			} catch (EncryptFailedException e1) {
				return gts.common(ReturnCode.EncryptFailedInSendRegResponse);
			}

			// svfConfirm_Signup
			// FIDO 1200 成功註冊
			// update member status to normal
			try {
				String prevStatus = memVO.getCustomer_Status();
				memVO.setCustomer_Status(MemberStatus.Normal);
				
//				String userIdSigned = bankId);
				PrivateKey privateKey;
				privateKey = RSA.loadKeyPair(rsaKeyAlias, sessID).getPrivate();
				String bankIdSigned = Encode.byteToHex(RSA.sign(privateKey, bankId.getBytes(StandardCharsets.UTF_8)));
//				String bankIdSigned = new String(rsa.sign(privateKey, bankId.getBytes(StandardCharsets.StandardCharsets.UTF_8)) , StandardCharsets.StandardCharsets.UTF_8);
//				Log4j.log.debug("[{}]*** bankIdSigned:{}", sessID, bankIdSigned);
				memVO.setBank_ID(bankIdSigned);
				memVO.setCustomer_Name(devInfo.get(DEVICE_NAME));
				memSvc.updateMember(memVO);
				memSvc.addMemberStatusLog(idgID, prevStatus, MemberStatus.Normal, "Registration finished");
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NoSuchAlgorithmException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "NoSuchAlgorithmException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (InvalidKeySpecException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "InvalidKeySpecException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (IOException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "IOException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (InvalidKeyException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "InvalidKeyException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (SignatureException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "SignatureException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (NoSuchProviderException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "NoSuchProviderException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (InvalidAlgorithmParameterException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "InvalidAlgorithmParameterException", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			} catch (Exception e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, "Exception", apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
			// Reset to 0 if otp is valid
			try {
				memSvc.updateAuthFailCounter(idgID, 0);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} else {
			// FIDO簽章驗證錯誤也要記錄錯誤次數+1在Members.Auth_Fails 
			try {
				memSvc.updateAuthFailCounter(idgID, memVO.getAuth_Fails() + 1);
				return gts.get_FailCount(rtnCode, String.valueOf(memVO.getAuth_Fails() + 1), null);
			} catch (SQLException e) {
				logException(sessID, e, SVF_SEND_REG_RESPONSE, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		}
		// XXX 111.11.25 試重現 "IdgateId": null,
//		idgateID = null;
//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfSendRegResponse] Response: "
//				+ returnVal);

		return gts.send_RegResponse(rtnCode, encRegResult, description, String.valueOf(idgateID));
	}

	/**
	 * 取出前端傳來的 serverData 內的 username
	 * 
	 * @param fidoStr
	 * @return
	 */
	private String getUserName(String fidoStr, String sessID, String funcName, APLogFormat apLogObj, InboundLogFormat inLogObj) {
		String serverData;
		List<Map<String, Object>> fidoList = gson.fromJson(fidoStr, new TypeToken<List<Map<String, Object>>>() {
		}.getType());
		Map<String, Object> fidoMap = (Map<String, Object>) fidoList.get(0);
//		Log4j.log.info("*** fidoMap:" + fidoMap); // ["AT4kAQM-2AALLgkAMDA4QSMw
		@SuppressWarnings("unchecked")
		Map<String, Object> header = (Map<String, Object>) fidoMap.get("header");
		serverData = (String) header.get("serverData");
//		Log4j.log.info("*** serverData@svfSendRegResponse:{}", serverData);
		String serverDataB64Decode;
		try {
			serverDataB64Decode = new String(Base64.getUrlDecoder().decode(serverData));
		} catch (Exception e) {
			Log4j.log.debug("*** serverData:{}", serverData);
			logException(sessID, e, funcName, "Unable to process serverData", apLogObj, inLogObj);
			throw e;
		}
//		Log4j.log.info(" decodeBase64.serverDataB64:" + serverDataB64Decode);
		String[] tokens = serverDataB64Decode.split("\\.");
		String username = tokens[2];
		try {
			username = new String(Base64.getUrlDecoder().decode(username));
		} catch (Exception e) {
			Log4j.log.debug("*** username:{}", username);
			logException(sessID, e, funcName, "Unable to process username", apLogObj, inLogObj);
			throw e;
		}
//		Log4j.log.info("*** username@svfSendRegResponse:{}", username);

		return username;
	}

	/**
	 * Step1
	 * 
	 * @param jsonString
	 * @param res
	 * @param gts
	 * @param sessID
	 * @param apLogObj 
	 * @param inLogObj 
	 * @return
	 */
	private String svfGetRegRequest(String input, HttpServletResponse res, GsonToString gts, String sessID,
			APLogFormat apLogObj, InboundLogFormat inLogObj) {
		JSONObject jSONObject = new JSONObject(input);
		Map<String, String> map = new HashMap<>();
//		map.put("userName", jSONObject.get("userName").toString().trim());	//111.11.9 修改註冊流程
		map.put(CHANNEL2, jSONObject.getString(CHANNEL2));

		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);
		if (map.containsKey(ERROR)) {
			logException(sessID, new Exception(map.get(ERROR)), SVF_GET_REG_REQUEST, PARAMETER_ERROR, apLogObj, inLogObj);
			return gts.common(ReturnCode.ParameterError);
		}

		String channel = map.get(CHANNEL2);
//		String userName = map.get("userName");

		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetRegRequest] channel: " + channel);

		ChannelVO chVO = null;
		try {
			chVO = new ChannelService(JNDI_Name, sessID).getOneChannel(channel);
			if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
				return gts.common(ReturnCode.ChannelInvalidError);
			}
//		if (chVO == null || NOT_AVAILABLE.equals(chVO.getActivate())) {
//			return gts.common(ReturnCode.ChannelInvalidInGetRegRequest);
//		}
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersService memSvc;
		try {
			memSvc = new MembersService(JNDI_Name, sessID);
		} catch (UnknownHostException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, UNKNOWN_HOST_EXCEPTION, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		} catch (NamingException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		long idgateID = -1;
		// addMembers
		try {
			// TODO: use userID ??
//			idgateID = memSvc.addMembers(StringEscapeUtils.escapeHtml4(username),
			idgateID = memSvc.addMembers("", "", "", "", "", channel, MemberStatus.Register, "");
//			Log4j.log.info("*** idgateID@svfGetRegRequest:{}", idgateID);

			// TODO REMARK BELOW
			Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo
					+ "][svfGetRegRequest][CREATE_MEMBER] idgateID: " + idgateID);
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		MembersVO memVO = null;
		try {
			memVO = memSvc.getByIdgateID(idgateID);	// step1 產 idgID
			
			if (memVO == null) {
				return gts.common(ReturnCode.MemberNotFound);
			}  
		} catch (SQLException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
			return gts.common(ReturnCode.DatabaseError);
		}
		
		// 111.12.22 regReqInner為空，verifyType0 不用打FIDO server

		HashMap<String, String> uafRequest = new HashMap<String, String>();
		uafRequest.put(OP, "Reg");
//		uafRequest.put("context", "{\"username\":\"" + userName + "\"}");
		Map<String, String> context = new HashMap<String, String>();
		String idgateIDstr = String.valueOf(idgateID);
		context.put(USERNAME2, idgateIDstr);
		context.put(CHANNEL2, channel);
		uafRequest.put(CONTEXT2, gson.toJson(context));

		String rsp = fidoUafResource.GetUAFRequest(gson.toJson(uafRequest), sessID);
		Map<String, Object> rspDataMap = new HashMap<>();
		try {
			rspDataMap = new ObjectMapper().readValue(rsp, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonSyntaxException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonMappingException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		} catch (JsonProcessingException e) {
			logException(sessID, e, SVF_GET_REG_REQUEST, "Unable to parse UAF json string", apLogObj, inLogObj);
			return gts.common(ReturnCode.JsonParseError);
		}

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetRegRequest] rspData: " + rspDataMap);

		if (rspDataMap == null) {
			Log4j.log.error("## ERROR ## rspData IS NULL");
			Log4j.log.error("## ERROR ## FIDO SERVER RESPONSE ERROR:");
			return gts.common(ReturnCode.TrustServerError);
		}
		
		String rtnCode = String.valueOf(rspDataMap.get(STATUS_CODE));
		// KEEP
//		String description = (String) rspDataMap.get("Description");

//		Log4j.log.info("*** uafRequest@AuthRequest:" + rspDataMap.get("uafRequest"));
		// Log4j.log.debug("[{}]*** rtnCode:{}", sessID, rtnCode);
		// Log4j.log.debug("[{}]*** description:{}", sessID, description);

		String regReq = null;
		// "statusCode": 1200,
//		    "Description": "OK. Operation completed",
		if (SUCCESS_1200.equals(rtnCode)) {
			rtnCode = ReturnCode.Success;
			Map<String, Object> innerMap = new HashMap<>();
			innerMap.put("regReqInner", gson.toJson(rspDataMap.get(UAF_REQUEST)));
			innerMap.put(IDGATE_ID, idgateIDstr);
			regReq = gson.toJson(innerMap);

			//		Log4j.log.debug("*** uafRequest@RegRequest:" + regReq);
			
			if (StringUtils.isBlank(webpinAppKey)) {
				// Server’s public key
				String serverPubKey = RSA.loadPublicKey(rsaKeyAlias, sessID);
				if (serverPubKey.contains(ERROR2)) {
					logException(sessID, new Exception("Loading RSA public key failed:"
							+ serverPubKey), SVF_GET_REG_REQUEST, "Loading RSA public key failed", apLogObj, inLogObj);
					return gts.common(ReturnCode.KeyGenErr);
				}
				webpinAppKey = serverPubKey;
			}  
			// Reset to 0 if otp is valid
			try {
				memSvc.updateAuthFailCounter(idgateID, 0);
			} catch (SQLException e) {
				logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
				return gts.common(ReturnCode.DatabaseError);
			}
		} 
		// 112.3.24 不會產生 2018
//		else {
//			// FIDO簽章驗證錯誤也要記錄錯誤次數+1在Members.Auth_Fails 
//			try {
//				memSvc.updateAuthFailCounter(idgateID, memVO.getAuth_Fails() + 1);
//				return gts.get_FailCount(rtnCode, String.valueOf(memVO.getAuth_Fails() + 1), null);
//			} catch (SQLException e) {
//				logException(sessID, e, SVF_GET_REG_REQUEST, DB_ERROR_OCCURRED, apLogObj, inLogObj);
//				return gts.common(ReturnCode.DatabaseError);
//			}
//		}
		
		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][svfGetRegRequest] response: "
				+ gts.get_RegRequest(rtnCode, regReq, webpinAppKey));

		return gts.get_RegRequest(rtnCode, regReq, webpinAppKey);
	}

	private String decryptRSA(String sessID, String key, String deviceKey, String data)
			throws IOException, JsonSyntaxException {
		String returnCode = null;
		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair(METHOD, "decrypt_Package"));
		formparams.add(new BasicNameValuePair("AesSecret", key));
		formparams.add(new BasicNameValuePair(DEV_DATA, deviceKey));
		formparams.add(new BasicNameValuePair("Xdata", data));

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][decrypt_RSA] Sending msg to TrustServer to decrypt: " + gson.toJson(formparams));

		returnCode = TS_Inst.decrypt_Package(key, data, deviceKey, sessID);

//		Log4j.log.debug("[" + sessID + "][Version: " + IDGateConfig.svVerNo + "][decrypt_RSA] Reply from TS: " + (returnCode));

		return returnCode;
	}
	
	private void setServerURL(HttpServletRequest request) {
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

	@PostConstruct
	public void init() {
//		try {
//			new Log4j(log4j_file_path);
//			new Log4jAP(log4j_file_path);
//			new Log4jInbound(log4j_file_path);
//			new Log4jSQL(log4j_file_path);
//		} catch (IOException e) {
//			Log4j.log.error(e);
//		}
		
		InboundLogFormat inLogObj = null;
		APLogFormat apLogObj = null;
		try {
			inLogObj = new InboundLogFormat();
			apLogObj = new APLogFormat();
		} catch (UnknownHostException e) {
			Log4j.log.error(e.getMessage());
		}

		long startTime = System.currentTimeMillis();
		Properties properties = null;
		File exCfgFile = new File(config_file_path);
		try (InputStream is = new FileInputStream(exCfgFile);) {
			properties = new Properties();
			properties.load(is);
			
			i18n_Name = properties.getProperty("i18n_Name", "Message");
			JNDI_Name = properties.getProperty("JNDI_Name", "java:comp/env/jdbc/PB_AP");
//			Log4j.log.info("[{}][Version: {}][{}] JNDI_Name: {}", IDGateConfig.sessionId, IDGateConfig.svVerNo, "init", JNDI_Name);
//			healthCheckString = properties.getProperty("healthCheckBody", "{\"method\": \"svfHealthCheck\"}");

			WSM_API_Key = properties.getProperty("WSM_API_Key");

			OTP_Timeout = Long.parseLong(properties.getProperty("OTP_Timeout", "300000"));
			Txn_Timeout = Long.parseLong(properties.getProperty("Txn_Timeout", "300000"));
			Digital_Fail_Limit = Integer.parseInt(properties.getProperty("Digital_Fail_Limit", "999"));
			Pattern_Fail_Limit = Integer.parseInt(properties.getProperty("Pattern_Fail_Limit", "999"));
			OTP_Fail_Limit = Integer.parseInt(properties.getProperty("OTP_Fail_Limit", "999"));
			
			// TODO 正式版要寫死成 false (不可使用假資料)
//			useFakeData = "false";	// TODO 正式版要寫死成 false (不可使用假資料)
//			useFakeData = properties.getProperty("USE_FAKE", "false");
			enableStep4OTP = properties.getProperty("enableOTP", TRUE);	// 彰銀 FIDO 預設啟用
			                                                            // 玉山不啟用
			// TODO 正式版 OnlyOTP 要寫死成 false (不可使用 OnlyOTP)
			onlyOTP = properties.getProperty("OnlyOTP", FALSE);
//			OnlyOTP = "false";	// TODO 正式版要寫死成 false (不可使用 OnlyOTP)
			
			// TODO 正式版 OnlyFIDO 要寫死成 false (不可使用 OnlyFIDO)
			onlyFIDO = properties.getProperty("OnlyFIDO", FALSE);
//			OnlyFIDO = "false";	// TODO 正式版要寫死成 false (不可使用 OnlyFIDO)
			
//			fakeServerData = properties.getProperty("SERVER_DATA");
			rsaKeyAlias = properties.getProperty(RSA_KEY_ALIAS, "RSAkeyAlias");
			// 設定Trust server的設定檔路徑
			TS_Inst = new TS_MainFunc(config_file_path, log4j_file_path);

//			Log4j.log.info("========================== Using the normal Log4j.log ==========================");
		} catch (NumberFormatException e) {
			Log4j.log.fatal("Unable to read [{}] from {}. ", e.getMessage(), config_file_path);

			apLogObj.setMessage(String.format("Unable to read [%s] from %s. ", e.getMessage(), config_file_path));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.fatal(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest("{}");
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
			return;
		} catch (IOException e) {
			Log4j.log.fatal("[WSMServlet][Version: " + IDGateConfig.svVerNo + "][init] Unable to read config from ["
					+ config_file_path + "]. " + e);
//			Log4j.log.fatal("Unable to read config from [" + Config_file_path + "]. " + e);

			logException("WSMServlet", e, "init", "Unable to read config from ["
					+ config_file_path + "]. ", apLogObj, inLogObj);

			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest(EMPTY_JSONOBJECT);
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
			return;
		} finally {
			if (properties != null) {
				properties.clear();
			}
		}
	}
}
