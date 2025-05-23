package com.WSM.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import com.Device_Detail.model.Device_DetailService;
import com.Device_Detail.model.Device_DetailVO;
import com.Members.model.MembersService;
import com.Members.model.MembersVO;
import com.WSM.model.CheckParameters;
import com.WSM.model.Send2Remote;
import com.WSM.model.Gson.Gson4Common;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.toppanidgate.WSM.controller.WSMServlet;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Servlet implementation class SvfGetAuthRequest
 */
@RestController
// @WebServlet(description = "請求驗證", urlPatterns = { "/svfGetAuthRequest" })
public class SvfGetAuthRequest extends BaseServlet {

	@Autowired
	WSMServlet wsmServlet;

	@PostMapping("/svfGetAuthRequest")
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
		inLogObj.setClazz("com.WSM.controller.SvfGetAuthRequest");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSM.controller.SvfGetAuthRequest");

//		System.out.println("Config_file_path: " + Config_file_path + ", Log4j_file_path: " + Log4j_file_path
//				+ ", Err_msg_path: " + Err_msg_path);

		String pushSvUrl = null;

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

			pushSvUrl = properties.getProperty("PushServer_URL");

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
				"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSM_API_Key.equals(headerApiKey)) {
			Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Reject client request for invalid API key");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Reject client request for invalid API key");
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
			Log4j.log.fatal("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Error occurred while reading body: " + e.getMessage());

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Error occurred while reading body: " + e.getMessage());
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
		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Msg from body: " + jsonString);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));
			map.put("idgateID", jSONObject.getString("idgateID"));
			map.put("authTypeRequest", jSONObject.getString("authType"));
			map.put("keyType", jSONObject.getString("keyType"));
			map.put("verifyType", jSONObject.getString("verifyType"));
			map.put("title", jSONObject.getString("title"));
			map.put("bankTxnData", jSONObject.getString("bankTxnData"));
			map.put("push", jSONObject.getString("push"));

			if (jSONObject.has("body"))
				map.put("body", jSONObject.getString("body"));

			if (jSONObject.has("clickAction"))
				map.put("clickAction", jSONObject.getString("clickAction"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Unable to parse client JSON msg: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()) + ", Response: " + gts.common(ReturnCode.JsonParseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Error occurred while reading body: " + e.getMessage() + ", Response: "
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
		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Locale: " + req.getLocale());
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		String channel;
		String idgateID;
		String authType;
		String keyType;
		String title;
		String bankTxnData;
		String verifyType;
		String push;
		String body;
		String clickAction;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
			idgateID = map.get("idgateID");
			authType = map.get("authTypeRequest");
			keyType = map.get("keyType");
			title = map.get("title");
			verifyType = map.get("verifyType");
			bankTxnData = map.get("bankTxnData");
			push = map.get("push");
			body = map.get("body");
			clickAction = map.get("clickAction");

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] channel: " + channel
					+ ", idgateID: " + idgateID + ", authType: " + authType + ", keyType: " + keyType + ", title: "
					+ title + ", bankTxnData: " + bankTxnData + ", verifyType: " + verifyType + ", body: " + body
					+ ", clickAction: " + clickAction + ", push: " + push);
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] channel: " + channel
					+ ", idgateID: " + idgateID + ", authType: " + authType + ", keyType: " + keyType + ", title: "
					+ title + ", bankTxnData: " + bankTxnData + ", verifyType: " + verifyType + ", body: " + body
					+ ", clickAction: " + clickAction + ", push: " + push);
			Log4jAP.log.info(apLogObj.getCompleteTxt());

		} else {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Error: " + map.get("Error")
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
		MembersVO memVO = null;
		Device_DetailService ddSvc = new Device_DetailService(JNDI_Name, sessionID);
		Device_DetailVO ddVO = null;
		long idgID = 0;
		try {
			idgID = Long.parseLong(idgateID);

			// check if this channel exist
			chVO = chSvc.getOneChannel(channel);
			memVO = memSvc.getByIdgateID(idgID);
			ddVO = ddSvc.getOneDevice_Detail(idgID);

		} catch (SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] DB Error occurred: "
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
			// channel is not usable
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.ChannelInvalidError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.ChannelInvalidError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelInvalidError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.ChannelInvalidError));
			return gts.common(ReturnCode.ChannelInvalidError);

		} else if (memVO == null || ddVO == null ) {
			// data not found
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.MemberNotFound));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.MemberNotFound));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.MemberNotFound));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.MemberNotFound));
			return gts.common(ReturnCode.MemberNotFound);

		} else if (memVO.getVerify_Type().equals(VerifyType.OFFLINE)|| memVO.getVerify_Type().equals(VerifyType.OFFLINE2)) {
			// data not found
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.VerifyTypeNotSupportInSvfGetAuthRequest));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.VerifyTypeNotSupportInSvfGetAuthRequest));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.VerifyTypeNotSupportInSvfGetAuthRequest));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.MemberNotFound));
			return gts.common(ReturnCode.VerifyTypeNotSupportInSvfGetAuthRequest);

		} else if (!memVO.getCustomer_Status().equals(MemberStatus.Normal)) {
			// not on usable state
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.MemberStatusError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.MemberStatusError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.MemberStatusError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.MemberStatusError));
			return gts.common(ReturnCode.MemberStatusError);
		} else if (!memVO.getVerify_Type().equals(verifyType)) {
			// not equal to recorded verify type
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.NotMatchToDBTypeRecordInSvfGetAuthRequest));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: "
					+ gts.common(ReturnCode.NotMatchToDBTypeRecordInSvfGetAuthRequest));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.NotMatchToDBTypeRecordInSvfGetAuthRequest));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.NotMatchToDBTypeRecordInSvfGetAuthRequest));
			return gts.common(ReturnCode.NotMatchToDBTypeRecordInSvfGetAuthRequest);
		}

		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo
				+ "][SvfGetAuthRequest] Sending to FIDO svfGetAuthRequest API");
		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
				+ "][SvfGetAuthRequest] Sending to FIDO svfGetAuthRequest API");
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		// outward data to FIDO server
		HashMap<String, String> dataPack = new HashMap<String, String>();
		dataPack.put("method", "svfGetAuthRequest");
		dataPack.put("channel", channel);
		dataPack.put("idgateID", idgateID);
		dataPack.put("authType", authType);
		dataPack.put("keyType", keyType);
		dataPack.put("title", title);
		dataPack.put("bankTxnData", bankTxnData);

		String retMsg = null;
		String txnID = null;
		JSONObject json;
		try {
			// DONE
			retMsg = wsmServlet.doPost(WSM_API_Key, req.getLocale(), gson.toJson(dataPack), sessionID);
//			retMsg = new Send2Remote().sendPost(FIDO_SV_IP, WSM_API_Key, gson.toJson(dataPack));
			json = new JSONObject(retMsg);

			if (json.has("encTxnID")) {
				txnID = json.getString("encTxnID");
			}
		} catch (IOException | JSONException e) {
			Log4j.log
					.error("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Connection to FIDO error: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Connection to FIDO error: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.FidoSVError));
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.FidoSVError));
			return gts.common(ReturnCode.FidoSVError);
		}

		Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] FIDO SV Response: " + retMsg);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] FIDO SV Response: " + retMsg);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		map.clear();
		map.put("returnMsg", retMsg);
		map = cp.checkMap(map);

		if (map.containsKey("Error")) {
			Log4j.log.debug(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Error: " + map.get("Error"));
			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		// send push when it is set to 1, and FIDO SV reply success
		if (push.equals("1") && json.getString("returnCode").equals("0000") && ddVO.getDevice_ID() != null
				&& !ddVO.getDevice_ID().equals("")) {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Sending push");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Sending push");
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("TokenID", ddVO.getDevice_ID()));
			formparams.add(new BasicNameValuePair("DeviceOS", ddVO.getDevice_OS()));
			formparams.add(new BasicNameValuePair("TxnID", txnID));
			formparams.add(new BasicNameValuePair("Title", title));
			formparams.add(new BasicNameValuePair("Content", body));
			formparams.add(new BasicNameValuePair("Channel", channel));
			formparams.add(new BasicNameValuePair("ClickAction", clickAction));
			formparams.add(new BasicNameValuePair("iDGateID", String.valueOf(ddVO.getIdgateID())));

			String pushRetMsg = new Send2Remote().sendPostWithForm(pushSvUrl, formparams);

			Log4j.log.info(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] PushSV Response: " + pushRetMsg);
			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] PushSV Response: " + pushRetMsg);
			Log4jAP.log.info(apLogObj.getCompleteTxt());

		} else if (ddVO.getDevice_ID() == null || ddVO.getDevice_ID().equals("")) {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Push skipped due to no push token record in DB");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Push skipped due to no push token record in DB");
			Log4jAP.log.info(apLogObj.getCompleteTxt());

		} else {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Push skipped");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Push skipped");
			Log4jAP.log.info(apLogObj.getCompleteTxt());
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
					"[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Unable to parse FIDO JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfGetAuthRequest] Unable to parse FIDO JSON msg: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: " + retMsg);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfGetAuthRequest] Response: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(retMsg);
		Log4jInbound.log.info(inLogObj.getCompleteTxt());

//		new OutputHandler(res, "application/json; charset=UTF-8").OutResult(retMsg);
		return retMsg;
	}
}
