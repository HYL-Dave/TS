package com.WSM.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Channel.model.ChannelService;
import com.Channel.model.ChannelVO;
import com.Common.model.APLogFormat;
import com.Common.model.BaseServlet;
import com.Common.model.GsonToString;
import com.Common.model.InboundLogFormat;
import com.Common.model.MemberStatus;
import com.Common.model.ReturnCode;
import com.Common.model.VerifyType;
import com.Members.model.MembersService;
import com.Members.model.MembersVO;
import com.WSM.model.CheckParameters;
import com.WSM.model.SimpleFidoFunc;
import com.WSM.model.Gson.Gson4Common;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.WSM.controller.WSMServlet;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Servlet implementation class SvfCheckSetting
 */
@RestController
// @WebServlet(description = "確認舊密碼是否正確（圖形鎖、簡易密碼）", urlPatterns = { "/svfCheckSetting" })
public class SvfCheckSetting extends BaseServlet {

	@Autowired
	WSMServlet wsmServlet;
	
	@Autowired
	SimpleFidoFunc simpleFidoFunc;
 
	@PostMapping("/svfCheckSetting")
	protected String doPost(@RequestBody Object reqBody, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		inLogObj = new InboundLogFormat();
		apLogObj = new APLogFormat();

		// 取得 Session ID
		final String sessionID = req.getSession().getId();
		// get api key
		final String headerApiKey = req.getHeader("apiKey");
		super.setServerURL(req);

		Properties properties = null;
		File exCfgFile = new File(Config_file_path);

		inLogObj.setTraceID(sessionID);
		inLogObj.setClazz("com.WSM.controller.SvfCheckSetting");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSM.controller.SvfCheckSetting");

//		System.out.println("Config_file_path: " + Config_file_path + ", Log4j_file_path: " + Log4j_file_path
//				+ ", Err_msg_path: " + Err_msg_path);

		try (InputStream is = new FileInputStream(exCfgFile);) {

			// 設定輸入編碼
			req.setCharacterEncoding("UTF-8");
			// setup general config
			properties = new Properties();
			properties.load(is);

			i18n_Name = properties.getProperty("i18n_Name");
			JNDI_Name = properties.getProperty("JNDI_Name");
			OTP_Timeout = Long.parseLong(properties.getProperty("OTP_Timeout"));
			Txn_Timeout = Long.parseLong(properties.getProperty("Txn_Timeout"));
			LoginTxn_Timeout = Long.parseLong(properties.getProperty("LoginTxn_Timeout"));

			WSM_API_Key = properties.getProperty("WSM_API_Key");

			FIDO_SV_IP = properties.getProperty("FIDO_SV_IP");

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
				"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSM_API_Key.equals(headerApiKey)) {
			Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfCheckSetting] Reject client request for invalid API key");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfCheckSetting] Reject client request for invalid API key");
			Log4jAP.log.debug(apLogObj.getCompleteTxt());

			inLogObj.setRequest("{}");
			inLogObj.setResponseTxt(gts.common(ReturnCode.APIKeyInvalid));
			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.APIKeyInvalid));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());
			return gts.common(ReturnCode.APIKeyInvalid);
		}

		StringBuffer jb = new StringBuffer();
		String line = null;
		BufferedReader reader = null;
		try {
			reader = req.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			Log4j.log.fatal(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Error occurred while reading body: "
							+ e.getMessage() + ", Response: " + gts.common(ReturnCode.JsonParseError));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Error occurred while reading body: "
							+ e.getMessage() + ", Response: " + gts.common(ReturnCode.JsonParseError));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.fatal(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest("{}");
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.InternalError));
			return gts.common(ReturnCode.InternalError);
		}

		String jsonString = jb.toString();
		JSONObject jSONObject = null;
		HashMap<String, String> map = new HashMap<>();
		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Msg from body: " + jsonString);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));
			map.put("idgateID", jSONObject.getString("idgateID"));
			map.put("encSettingData", jSONObject.getString("encSettingData"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4j.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Unable to parse client JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfCheckSetting] Error occurred while reading body: " + e.getMessage());
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
		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Locale: " + req.getLocale());

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		String channel;
		String idgateID;
		String encSettingData;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
			idgateID = map.get("idgateID");
			encSettingData = map.get("encSettingData");

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] channel: " + channel
					+ ", idgateID: " + idgateID + ", encSettingData: " + encSettingData);

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] channel: " + channel
					+ ", idgateID: " + idgateID + ", encSettingData: " + encSettingData);
			Log4jAP.log.info(apLogObj.getCompleteTxt());

		} else {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Error: " + map.get("Error")
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
		MembersService memSvc = new MembersService(JNDI_Name, sessionID);
		MembersVO memVO;

		try {
			// check if this channel exist
			chVO = chSvc.getOneChannel(channel);
			memVO = memSvc.getByIdgateID(Long.parseLong(idgateID));
		} catch (SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] DB Error occurred: "
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

		if (chVO == null || "N".equals(chVO.getACTIVATE())) {
			// channel invalid
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.ChannelInvalidError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.ChannelInvalidError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelInvalidError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.ChannelInvalidError));
			return gts.common(ReturnCode.ChannelInvalidError);

		} else if (memVO == null) {
			// member doesn't exist
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.MemberNotFound));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.MemberNotFound));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.MemberNotFound));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.MemberNotFound));
			return gts.common(ReturnCode.MemberNotFound);

		} else if (memVO.getCustomer_Status().equals(MemberStatus.Locked)
				|| memVO.getCustomer_Status().equals(MemberStatus.LockedForTooManyAuthFails)) {
			// member account is in locked state
			String failCount = "";
			if (memVO.getVerify_Type().equals(VerifyType.OFFLINE)) {
				failCount = String.valueOf(memVO.getOffline_Auth_Fails());
			} else if (memVO.getVerify_Type().equals(VerifyType.QUICK_LOGIN)) {
				failCount = String.valueOf(memVO.getLogin_Auth_Fails());
			} else {
				failCount = String.valueOf(memVO.getTxn_Auth_Fails());
			}

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common_w_Failcount(ReturnCode.MemberLockedError, failCount));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common_w_Failcount(ReturnCode.MemberLockedError, failCount));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common_w_Failcount(ReturnCode.MemberLockedError, failCount));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common_w_Failcount(ReturnCode.MemberLockedError, failCount));
			return gts.common_w_Failcount(ReturnCode.MemberLockedError, failCount);
		} else if (memVO.getCustomer_Status().equals(MemberStatus.Deleted)) {
			// member account is in deleted state
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.MemberDeletedError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.MemberDeletedError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.MemberDeletedError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.MemberDeletedError));
			return gts.common(ReturnCode.MemberDeletedError);
		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			// member account is not in active state
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.MemberStatusError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: "
					+ gts.common(ReturnCode.MemberStatusError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.MemberStatusError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.MemberStatusError));
			return gts.common(ReturnCode.MemberStatusError);
		}

		Log4j.log.trace(
				"[" + sessionID + "][Version: " + svVerNo + "][svfCheckSetting] Sending to FIDO svfCheckSetting API");
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svfCheckSetting] Sending to FIDO svfCheckSetting API");
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		// outward data to FIDO server
		HashMap<String, String> formparams = new HashMap<String, String>();
		formparams.put("method", "svfCheckSetting");
		formparams.put("channel", channel);
		formparams.put("idgateID", idgateID);
		formparams.put("encSettingData", encSettingData);

		String retMsg = null;
		HashMap<String, String> dataSet = null;
		int failCount = 0;
		try {
			// receive and process response of svfCheckSetting
			// DONE
			retMsg = wsmServlet.doPost(WSM_API_Key, req.getLocale(), gson.toJson(formparams), sessionID);
//			retMsg = new Send2Remote().sendPost(FIDO_SV_IP, WSM_API_Key, gson.toJson(formparams));
			dataSet = new Gson().fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
			}.getType());

			formparams.clear();

			if (dataSet.containsKey("failCount") && !dataSet.get("failCount").equals("0")) {
				failCount = Integer.parseInt(dataSet.get("failCount"));

				// fetch member auth type to determine that is by pin or patter
				String devStatus = simpleFidoFunc.getDeviceStatus(req.getLocale(), WSM_API_Key, idgateID, channel, sessionID);
				Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo
						+ "][svfCheckSetting] FIDO device status response: " + devStatus);
				JSONObject devJson = new JSONObject(devStatus);

				if (memVO.getVerify_Type().equals(VerifyType.OFFLINE)) {
					// offline verification failed
					memSvc.updateOfflineAuthFails(Long.parseLong(idgateID), failCount);

					if (failCount >= chVO.getOFFLINE_OTP_lIMIT()) {
						simpleFidoFunc.lockDevice(req.getLocale(), WSM_API_Key, idgateID, channel, sessionID);
						memSvc.updateMemberStatusUnderBankID(memVO.getDevice_ID(),
								MemberStatus.LockedForTooManyAuthFails);
						retMsg = gts.common_w_Failcount(ReturnCode.MemberLockedError, dataSet.get("failCount"));

					}

				} else if (memVO.getVerify_Type().equals(VerifyType.QUICK_LOGIN)) {
					// login verification failed
					memSvc.updateLoginAuthFails(Long.parseLong(idgateID), failCount);

					if (devJson.getJSONObject("type").getString("pattern").equals("1")
							&& failCount >= chVO.getQUICK_LOGIN_PATTERN_LIMIT()) {
						simpleFidoFunc.lockDevice(req.getLocale(), WSM_API_Key, idgateID, channel, sessionID);
						memVO.setCustomer_Status(MemberStatus.LockedForTooManyAuthFails);
						memSvc.updateMember(memVO);
						retMsg = gts.common_w_Failcount(ReturnCode.MemberLockedError, dataSet.get("failCount"));

					} else if (devJson.getJSONObject("type").getString("pin").equals("1")
							&& failCount >= chVO.getQUICK_LOGIN_PIN_LIMIT()) {
						simpleFidoFunc.lockDevice(req.getLocale(), WSM_API_Key, idgateID, channel, sessionID);
						memVO.setCustomer_Status(MemberStatus.LockedForTooManyAuthFails);
						memSvc.updateMember(memVO);
						retMsg = gts.common_w_Failcount(ReturnCode.MemberLockedError, dataSet.get("failCount"));
					}

				} else {
					// txn verification failed
					memSvc.updateTxnAuthFails(Long.parseLong(idgateID), failCount);

					if (devJson.getJSONObject("type").getString("pattern").equals("1")
							&& failCount >= chVO.getTXN_PATTERN_LIMIT()) {
						simpleFidoFunc.lockDevice(req.getLocale(), WSM_API_Key, idgateID, channel, sessionID);
						memSvc.updateMemberStatusUnderBankID(memVO.getDevice_ID(),
								MemberStatus.LockedForTooManyAuthFails);
						retMsg = gts.common_w_Failcount(ReturnCode.MemberLockedError, dataSet.get("failCount"));

					} else if (devJson.getJSONObject("type").getString("pin").equals("1")
							&& failCount >= chVO.getTXN_PIN_LIMIT()) {
						simpleFidoFunc.lockDevice(req.getLocale(), WSM_API_Key, idgateID, channel, sessionID);
						memSvc.updateMemberStatusUnderBankID(memVO.getDevice_ID(),
								MemberStatus.LockedForTooManyAuthFails);
						retMsg = gts.common_w_Failcount(ReturnCode.MemberLockedError, dataSet.get("failCount"));
					}
				}
			} else {
				// reset fail counter if its succeed
				if (memVO.getVerify_Type().equals(VerifyType.OFFLINE)) {
					memSvc.updateOfflineAuthFails(Long.parseLong(idgateID), 0);
				} else if (memVO.getVerify_Type().equals(VerifyType.QUICK_LOGIN)) {
					memSvc.updateLoginAuthFails(Long.parseLong(idgateID), 0);
				} else {
					memSvc.updateTxnAuthFails(Long.parseLong(idgateID), 0);
				}
			}
		} catch (IOException | NumberFormatException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Connection to FIDO error: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Connection to FIDO error: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.FidoSVError));
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.FidoSVError));
			return gts.common(ReturnCode.FidoSVError);
		} catch (SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] DB Error occurred: "
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
					"[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Unable to parse FIDO JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfCheckSetting] Unable to parse FIDO JSON msg: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: " + retMsg);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfCheckSetting] Response: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(retMsg);
		Log4jInbound.log.info(inLogObj.getCompleteTxt());

//		new OutputHandler(res, "application/json; charset=UTF-8").OutResult(retMsg);
		return retMsg;
	}

}
