package com.WSM.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.Channel.model.ChannelService;
import com.Channel.model.ChannelVO;
import com.Common.model.APLogFormat;
import com.Common.model.BaseServlet;
import com.Common.model.GsonToString;
import com.Common.model.InboundLogFormat;
import com.Common.model.ReturnCode;
import com.WSM.model.CheckParameters;
import com.WSM.model.Gson.Gson4Common;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
//import com.toppanidgate.WSM.controller.WSMServlet;
import com.toppanidgate.WSM.controller.WSMServlet;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;
import com.toppanidgate.idenkey.Config.IDGateConfig;

/**
 * Servlet implementation class SvfGetPubKeyNTime
 */
//@RequestMapping("/MP")
// @WebServlet(description = "取得RSA公鑰與伺服器時間", urlPatterns = { "/svfGetPubKeyNTime" })
@RestController
public class SvfGetPubKeyNTime extends BaseServlet {
	
	@Autowired
	WSMServlet wsmServlet;
	
	@Autowired
	HttpServletRequest req;

	@Autowired
	HttpServletResponse res;
	
	@PostMapping("/svfGetPubKeyNTime")
	protected String doPost(@RequestBody Object body) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		inLogObj = new InboundLogFormat();
		apLogObj = new APLogFormat();

		// 設定輸入編碼
		req.setCharacterEncoding("UTF-8");
		// 取得 Session ID
		final String sessionID = req.getSession().getId();
		// get api key
		final String headerApiKey = req.getHeader("apiKey");
		super.setServerURL(req);
		
		Log4j.log.debug(
				"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] *** getLocale: " + req.getLocale());

		Properties properties = null;
		File exCfgFile = new File(Config_file_path);

		inLogObj.setTraceID(sessionID);
		inLogObj.setClazz("com.WSM.controller.svfGetPubKeyNTime");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSM.controller.svfGetPubKeyNTime");

//		System.out.println("Config_file_path: " + Config_file_path + ", Log4j_file_path: " + Log4j_file_path
//				+ ", Err_msg_path: " + Err_msg_path);

		try (InputStream is = new FileInputStream(exCfgFile);) {

			// setup general config
			properties = new Properties();
			properties.load(is);

			i18n_Name = properties.getProperty("i18n_Name");
			JNDI_Name = properties.getProperty("JNDI_Name");
			
			String otpTimeout = properties.getProperty("OTP_Timeout");
			String txnTimeout = properties.getProperty("Txn_Timeout");
			String loginTxnTimeout = properties.getProperty("LoginTxn_Timeout");
			
			WSM_API_Key = properties.getProperty("WSM_API_Key");
			
//			validateKey(i18n_Name, "i18n_Name");
//			validateKey(JNDI_Name, "JNDI_Name");
//			validateKey(otpTimeout, "OTP_Timeout");
//			validateKey(txnTimeout, "Txn_Timeout");
//			validateKey(loginTxnTimeout, "LoginTxn_Timeout");
//			validateKey(WSM_API_Key, "WSM_API_Key");
			
			OTP_Timeout = Long.parseLong(otpTimeout);
			Txn_Timeout = Long.parseLong(txnTimeout);
			LoginTxn_Timeout = Long.parseLong(loginTxnTimeout);

		} catch (NumberFormatException e) {
			Log4j.log.fatal("Unable to read [{}] from {}. ", e.getMessage(), Config_file_path);

			apLogObj.setMessage(String.format("Unable to read [%s] from %s. ", e.getMessage(), Config_file_path));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.fatal(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest("{}");
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());

			Gson4Common gson4 = new Gson4Common();
			gson4.setReturnCode("M0009");
			gson4.setReturnMsg(String.format("Unable to read [%s] from %s. ", e.getMessage(), Config_file_path));
			return gson.toJson(gson4);
		} catch (IOException e) {
			Log4j.log.fatal("Unable to read config from [" + Config_file_path + "]. " + e.getMessage());

			apLogObj.setMessage("Unable to read config from [" + Config_file_path + "]. " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.fatal(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest("{}");
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());

			Gson4Common gson4 = new Gson4Common();
			gson4.setReturnCode("M0009");
			gson4.setReturnMsg("config_file_path error: " + Config_file_path);
			return gson.toJson(gson4);
		} finally {
			if (properties != null) {
				properties.clear();
			}
		}
		
		// 多國語系設定
		GsonToString gts = new GsonToString(Err_msg_path, i18n_Name, req.getLocale());

		Log4j.log.debug(
				"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSM_API_Key.equals(headerApiKey)) {
			Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][svfGetPubKeyNTime] Reject client request for invalid API key");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svfGetPubKeyNTime] Reject client request for invalid API key");
			Log4jAP.log.debug(apLogObj.getCompleteTxt());

			inLogObj.setRequest("{}");
			inLogObj.setResponseTxt(gts.common(ReturnCode.APIKeyInvalid));
			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.APIKeyInvalid));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());
			return gts.common(ReturnCode.APIKeyInvalid);
		}

//		StringBuffer jb = new StringBuffer();
//		String line = null;
//		BufferedReader reader = null;
//		try {
//			reader = req.getReader();
//			while ((line = reader.readLine()) != null)
//				jb.append(line);
//		} catch (Exception e) {
//			Log4j.log.fatal("[" + sessionID + "][Version: " + svVerNo
//					+ "][svfGetPubKeyNTime] Error occurred while reading body: " + e.getMessage());
//
//			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
//					+ "][svfGetPubKeyNTime] Error occurred while reading body: " + e.getMessage());
//			apLogObj.setThrowable(e.getMessage());
//			Log4jAP.log.fatal(apLogObj.getCompleteTxt());
//
//			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
//			inLogObj.setThrowable(e.getMessage());
//			inLogObj.setRequest("{}");
//			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//
////			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.InternalError));
//			return gts.common(ReturnCode.InternalError);
//		}
//		String jsonString = jb.toString();
		
		String jsonString = null;
		try {
			jsonString = new ObjectMapper().writeValueAsString(body);
		} catch (JsonProcessingException e) {
			Log4j.log.fatal("[" + sessionID + "][Version: " + IDGateConfig.svVerNo
					+ "][doPost] Error occurred while reading body: " + e.getMessage());
			return gts.common(ReturnCode.JsonParseError);
		}

		JSONObject jSONObject = null;
		HashMap<String, String> map = new HashMap<>();
		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Msg from body: " + jsonString);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][svfGetPubKeyNTime] Unable to parse client JSON msg: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()) + ", Response: " + gts.common(ReturnCode.JsonParseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svfGetPubKeyNTime] Error occurred while reading body: " + e.getMessage() + ", Response: "
					+ gts.common(ReturnCode.JsonParseError));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.fatal(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.JsonParseError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.JsonParseError));
			return gts.common(ReturnCode.JsonParseError);
		}

		// 檢查參數
		CheckParameters cp = new CheckParameters();
		map = cp.checkMap(map);

		// log locale
		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Locale: " + req.getLocale());

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		String channel;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
		} else {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Error: " + map.get("Error")
					+ ", Response: " + gts.common(ReturnCode.ParameterError));

			apLogObj.setMessage("Error: " + map.get("Error") + ", Response: " + gts.common(ReturnCode.ParameterError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.ParameterError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.ParameterError));
			return gts.common(ReturnCode.ParameterError);
		}
		
		
		ChannelService chSvc = new ChannelService(JNDI_Name, sessionID);
		ChannelVO chVO = null;

		try {
			// check if this channel exist
			chVO = chSvc.getOneChannel(channel);
		} catch (Exception e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] DB Error occurred: "
					+ e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.DatabaseError));
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.DatabaseError));
			return gts.common(ReturnCode.DatabaseError);
		}

		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Checking channel");

		if (chVO == null || "N".equals(chVO.getACTIVATE())) {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Rssponse: "
					+ gts.common(ReturnCode.ChannelInvalidError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Rssponse: "
					+ gts.common(ReturnCode.ChannelInvalidError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelInvalidError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.ChannelInvalidError));
			return gts.common(ReturnCode.ChannelInvalidError);
		}

		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo
				+ "][svfGetPubKeyNTime] Sending to FIDO svfGetPubKeyNTime API");
		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
				+ "][svfGetPubKeyNTime] Sending to FIDO svfGetPubKeyNTime API");
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		// outward data to FIDO server
		HashMap<String, String> formparams = new HashMap<String, String>();
		formparams.put("method", "svfGetPubKeyNTime");
		formparams.put("channel", channel);

		String retMsg = null;
		try {
			// DONE
			retMsg = wsmServlet.doPost(WSM_API_Key, req.getLocale(), gson.toJson(formparams), sessionID);
//			retMsg = new Send2Remote().sendPost(FIDO_SV_IP, WSM_API_Key, gson.toJson(formparams));
		} catch (IOException e) {
			Log4j.log
					.error("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Connection to FIDO error: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
		}

		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] FIDO SV Response: " + retMsg);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] FIDO SV Response: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		map.clear();
		map.put("returnMsg", retMsg);
		map = cp.checkMap(map);

		if (map.containsKey("Error")) {
			Log4j.log.debug(
					"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Error: " + map.get("Error"));
			retMsg = gts.common(ReturnCode.FidoSVError);
		}
		
		try {
			jSONObject = new JSONObject(retMsg);
			if (jSONObject.getString("returnCode").equals("0000")) {
				jSONObject.put("returnCode", "0" + jSONObject.getString("returnCode"));
			} else if (jSONObject.getString("returnCode").length() == 4) {
				jSONObject.put("returnCode", "F" + jSONObject.getString("returnCode"));
			}

			retMsg = jSONObject.toString();
		} catch (JsonSyntaxException | JSONException e) {
			Log4j.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Unable to parse FIDO JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svfGetPubKeyNTime] Unable to parse FIDO JSON msg: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][svfGetPubKeyNTime] Response: " + retMsg);

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(retMsg);	// new JSONObject(retMsg); 檢核過了

		Log4jInbound.log.info(inLogObj.getCompleteTxt());

		return retMsg;
	}
	
	@SuppressWarnings("unused")
	private void validateKey(String keyInConfig, String keyName) throws NumberFormatException{
		if (keyInConfig == null ) {
			throw new NumberFormatException(keyName);
		}
	}
}
