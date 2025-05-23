package com.WSM.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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
import com.Cvn.Encryptor.AESUtil;
import com.Device_Detail.model.Device_DetailService;
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

@RestController
// @WebServlet(description = "確認註冊裝置綁定成功與否", urlPatterns = { "/svfSendRegResponse" })
public class SvfSendRegResponse extends BaseServlet {
	@Autowired
	WSMServlet wsmServlet;
	
	@Autowired
	SimpleFidoFunc simpleFidoFunc;

	@PostMapping("/svfSendRegResponse")
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
		inLogObj.setClazz("com.WSM.controller.SvfSendRegResponse");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSM.controller.SvfSendRegResponse");

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
				"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSM_API_Key.equals(headerApiKey)) {
			Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Reject client request for invalid API key");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Reject client request for invalid API key");
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
					+ "][SvfSendRegResponse] Error occurred while reading body: " + e.getMessage() + ", Response: "
					+ gts.common(ReturnCode.JsonParseError));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.InternalError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Error occurred while reading body: " + e.getMessage() + ", Response: "
					+ gts.common(ReturnCode.JsonParseError));
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
		Log4j.log
				.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Msg from body: " + jsonString);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));
			map.put("deviceInfo", jSONObject.getString("deviceInfo"));
			map.put("encRegRes", jSONObject.getString("encRegRes"));
			map.put("transactionID", jSONObject.getString("transactionID"));
			map.put("idgateID", jSONObject.getString("idgateID"));
			map.put("verifyType", jSONObject.getString("verifyType"));
			map.put("replace", jSONObject.getString("replace"));

			if (jSONObject.has("idgateIDs"))
				map.put("idgateIDs", jSONObject.getString("idgateIDs"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to parse client JSON msg: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.JsonParseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Error occurred while reading body: " + e.getMessage());
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

		Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Locale: " + req.getLocale());

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		String channel;
		String deviceInfo;
		String encRegRes;
		String idgateID;
		String idgateIDs;
		String transactionID;
		String verifyType;
		String replace;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
			deviceInfo = map.get("deviceInfo");
			encRegRes = map.get("encRegRes");
			idgateID = map.get("idgateID");
			transactionID = map.get("transactionID");
			idgateIDs = map.get("idgateIDs");
			verifyType = map.get("verifyType");
			replace = map.get("replace");

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] channel: " + channel
					+ ", deviceInfo: " + deviceInfo + ", encRegRes: " + encRegRes + ", idgateID: " + idgateID
					+ ", idgateIDs: " + idgateIDs + ", transactionID: " + transactionID + ", verifyType: " + verifyType
					+ ", replace: " + replace);

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] channel: " + channel
					+ ", deviceInfo: " + deviceInfo + ", encRegRes: " + encRegRes + ", idgateID: " + idgateID
					+ ", idgateIDs: " + idgateIDs + ", transactionID: " + transactionID + ", verifyType: " + verifyType
					+ ", replace: " + replace);
			Log4jAP.log.info(apLogObj.getCompleteTxt());

		} else {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Error: "
					+ map.get("Error") + ", Response: " + gts.common(ReturnCode.ParameterError));

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
		} catch (SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.DatabaseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
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
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.ChannelInvalidError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.ChannelInvalidError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelInvalidError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.ChannelInvalidError));
			return gts.common(ReturnCode.ChannelInvalidError);
		}

		// compare idgateIDs list with idgateID because it must not exist in there
		if (idgateIDs != null && idgateIDs.length() > 10) {
			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Checking idgateIDs if it contains idgateID");

			if (idgateIDs.indexOf(idgateID) >= 0) {
				Log4j.log.warn("[" + sessionID + "][Version: " + svVerNo
						+ "][SvfSendRegResponse] Found same idgateID that is sent from client in idgateIDs list.");

				Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
						+ gts.common(ReturnCode.ParameterError));

				apLogObj.setMessage("Found same idgateID that is sent from client in idgateIDs list.");
				Log4jAP.log.warn(apLogObj.getCompleteTxt());

				apLogObj.setMessage(gts.common(ReturnCode.ParameterError));
				Log4jAP.log.info(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setResponseTxt(gts.common(ReturnCode.ParameterError));
				Log4jInbound.log.info(inLogObj.getCompleteTxt());

//				new OutputHandler(res, "application/json; charset=UTF-8")
//						.OutResult(gts.common(ReturnCode.ParameterError));
				return gts.common(ReturnCode.ParameterError);
			}
		}

		HashMap<String, String> idList = null;
		HashMap<String, String> devInfo = null;
		try {
			idList = gson.fromJson(idgateIDs, new TypeToken<HashMap<String, String>>() {
			}.getType());

			devInfo = gson.fromJson(deviceInfo, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			Log4j.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Unable to parse idgateIDs JSON: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.JsonParseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to parse idgateIDs JSON: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
			Log4jInbound.log.error(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.JsonParseError));
			return gts.common(ReturnCode.JsonParseError);
		}

		// 112.5.16 M0100, M0101, M0102 從 log 看來沒用到
		// check if there is add auth violation
		if (replace.equals("0") && idList != null && idList.containsKey(verifyType)) {
			Log4j.log.warn("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] This verify type "
					+ verifyType + " already existed. Cannot add one more.");

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Reponse: "
					+ gts.common(ReturnCode.CannotAddAuthInSendRegResponse));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] This verify type "
					+ verifyType + " already existed. Cannot add one more.");
			Log4jAP.log.warn(apLogObj.getCompleteTxt());

			inLogObj.setResponseTxt(gts.common(ReturnCode.CannotAddAuthInSendRegResponse));
			Log4jInbound.log.error(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.CannotAddAuthInSendRegResponse));
			return gts.common(ReturnCode.CannotAddAuthInSendRegResponse);
		}
		// check if there is replace auth violation
		else if (replace.equals("1") && (idList == null || !idList.containsKey(verifyType))) {
			Log4j.log.warn("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] This verify type "
					+ verifyType + " doesn't exist. Cannot replace it.");

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Reponse: "
					+ gts.common(ReturnCode.CannotReplaceAuthInSendRegResponse));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] This verify type "
					+ verifyType + " doesn't exist. Cannot replace it.");
			Log4jAP.log.warn(apLogObj.getCompleteTxt());

			inLogObj.setResponseTxt(gts.common(ReturnCode.CannotReplaceAuthInSendRegResponse));
			Log4jInbound.log.error(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.CannotReplaceAuthInSendRegResponse));
			return gts.common(ReturnCode.CannotReplaceAuthInSendRegResponse);
		}
		// check if it attempt to disable authentications without idgateIDs
		else if (replace.equals("2") && (idList == null || idList.isEmpty())) {
			Log4j.log.warn("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] No idgateIDs provided, cannot disable accounts.");

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Reponse: "
					+ gts.common(ReturnCode.CannotDisableAllAuthInSendRegResponse));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Reponse: "
					+ gts.common(ReturnCode.CannotDisableAllAuthInSendRegResponse));
			Log4jAP.log.warn(apLogObj.getCompleteTxt());

			inLogObj.setResponseTxt(gts.common(ReturnCode.CannotDisableAllAuthInSendRegResponse));
			Log4jInbound.log.error(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.CannotDisableAllAuthInSendRegResponse));
			return gts.common(ReturnCode.CannotDisableAllAuthInSendRegResponse);
		}

		MembersService memSrv = new MembersService(JNDI_Name, sessionID);
		MembersVO memVO = null;
		HashMap<String, String> formparams = new HashMap<String, String>();

		String deviceID = null;
		try {

			if (idList != null && idList.size() > 0) {
				String id = idList.entrySet().iterator().next().getValue();
				memVO = memSrv.getByIdgateID(Long.parseLong(id));

				if (memVO == null || (memVO != null && memVO.getCustomer_Status().equals(MemberStatus.Deleted))) {
					// cannot find a valid member
					Log4j.log.info("[" + sessionID + "][Version: " + svVerNo
							+ "][SvfSendRegResponse] Unable to find a valid member record from idgateID in idgateIDs: "
							+ id + ". Response: " + gts.common(ReturnCode.MemberNotFound));

					apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
							+ "][SvfSendRegResponse] Unable to find a valid member record from idgateID in idgateIDs: "
							+ id + ". Response: " + gts.common(ReturnCode.MemberNotFound));
					Log4jAP.log.info(apLogObj.getCompleteTxt());

					inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
					inLogObj.setResponseTxt(gts.common(ReturnCode.MemberNotFound));
					Log4jInbound.log.info(inLogObj.getCompleteTxt());
//					new OutputHandler(res, "application/json; charset=UTF-8")
//							.OutResult(gts.common(ReturnCode.MemberNotFound));
					return gts.common(ReturnCode.MemberNotFound);

				} else if (replace.equals("2")) {
					// replace all and create new device id
					deviceID = AESUtil
							.SHA256_To_HexString(devInfo.get("userID") + String.valueOf(System.currentTimeMillis()));
				} else {
					// fetch existing id
					deviceID = memVO.getDevice_ID();
				}

			} else {
				// compute new device id
				deviceID = AESUtil
						.SHA256_To_HexString(devInfo.get("userID") + String.valueOf(System.currentTimeMillis()));
			}
		} catch (NumberFormatException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Log4j.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Unable to Compute SHA256: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to Compute SHA256: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.InternalError));
			Log4jInbound.log.error(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.InternalError));
			return gts.common(ReturnCode.InternalError);
		} catch (SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.DatabaseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
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

		String retMsg = null;
		// register with soft token way
		if (verifyType.equals(VerifyType.OFFLINE)|| verifyType.equals(VerifyType.OFFLINE2)) {
			Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Sending to FIDO svfSignup_Device API");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Sending to FIDO svfSignup_Device API");
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			// outward data to FIDO server
			formparams.put("method", "svfSignup_Device");
			formparams.put("channel", channel);
			formparams.put("idgateID", idgateID);
			formparams.put("deviceInfo", deviceInfo);
			formparams.put("encRegRes", encRegRes);
			formparams.put("transactionID", transactionID);

		} else {
			// register with FIDO way
			Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Sending to FIDO SvfSendRegResponse API");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Sending to FIDO SvfSendRegResponse API");
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			// outward data to FIDO server
			formparams.put("method", "svfSendRegResponse");
			formparams.put("channel", channel);
			formparams.put("deviceInfo", deviceInfo);
			formparams.put("encRegRes", encRegRes);
			formparams.put("transactionID", transactionID);
		}

		try {
			// DONE
			retMsg = wsmServlet.doPost(WSM_API_Key, req.getLocale(), gson.toJson(formparams), sessionID);
//			retMsg = new Send2Remote().sendPost(FIDO_SV_IP, WSM_API_Key, gson.toJson(formparams));
		} catch (IOException e) {
			Log4j.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Connection to FIDO error: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			Log4j.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Connection to FIDO error: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.FidoSVError));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Connection to FIDO error: "
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

		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] FIDO SV Response: " + retMsg);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] FIDO SV Response: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		// parsing response data from FIDO
		HashMap<String, String> respData = null;
		try {
			respData = new Gson().fromJson(retMsg, new TypeToken<HashMap<String, String>>() {
			}.getType());
		} catch (JsonSyntaxException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to parse FIDO SV response: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to parse FIDO SV response: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setThrowable(e.getMessage());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.FidoSVError));
			return gts.common(ReturnCode.FidoSVError);
		}

		if (!respData.containsKey("idgateID")) {
			// no idgate id generated
			Log4j.log.warn("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: " + retMsg);

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: " + retMsg);
			Log4jAP.log.warn(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(retMsg);
			Log4jInbound.log.warn(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(retMsg);
			return retMsg;
		}

		// add member and device info
		String pushID = null;
		try {
			if (respData.containsKey("pushID"))
				pushID = respData.get("pushID");

			// check if this member info exist already
			memVO = memSrv.getByIdgateID(Long.parseLong(idgateID));

			if (memVO == null) {
				memSrv.addMembers(Long.parseLong(idgateID), deviceID, null, null, null, verifyType, channel,
						MemberStatus.Normal, null);
				new Device_DetailService(JNDI_Name, sessionID).addDevice_Detail(Long.parseLong(idgateID), null, null,
						pushID, "1", devInfo.get("deviceOS"), "N", devInfo.get("deviceLabel"),
						devInfo.get("deviceModel"), devInfo.get("deviceIP"), devInfo.get("deviceOSVer"),
						devInfo.get("appVer"));

				// reply additional info
				respData.put("verifyType", verifyType);
				respData.put("deviceID", deviceID);
				retMsg = gson.toJson(respData);

			} else {
				retMsg = gts.common(ReturnCode.CannotCreateNewMemberInSendRegResponse);
			}

		} catch (NumberFormatException | SQLException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
					+ gts.common(ReturnCode.DatabaseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
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
				
				// 成功加新帳號後再註銷
				// disable old account and replace it with new one later
				if (replace.equals("1")) {
					String disableID = idList.get(verifyType);

					Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Disabling old id "
							+ disableID + " before creating a new one");
					apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Disabling old id "
							+ disableID + " before creating a new one");
					Log4jAP.log.trace(apLogObj.getCompleteTxt());

					try {
						// fetch and disable specified idgate id
						memVO = memSrv.getByIdgateID(Long.parseLong(disableID));

						if (memVO != null && memVO.getDevice_ID().equals(deviceID)) {

							memSrv.addMemberStatusLog(memVO.getIDGateID(), memVO.getCustomer_Status(), MemberStatus.Deleted,
									sessionID);
							memVO.setCustomer_Status(MemberStatus.Deleted);
							memSrv.updateMember(memVO);
							
							// DONE
//							retMsg = wsmServlet.doPost(WSM_API_Key, req.getLocale(), gson.toJson(formparams), sessionID);
							String fidoResp = simpleFidoFunc.deregister(req.getLocale(), WSM_API_Key, disableID, channel, sessionID);
							Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] FIDO deregister response: "
									+ fidoResp);
							apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] FIDO deregister response: "
									+ fidoResp);
							Log4jAP.log.debug(apLogObj.getCompleteTxt());
						} else {

							Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Old id "
									+ disableID + ",s device ID doesn't match. Skipped");
							apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Old id "
									+ disableID + ",s device ID doesn't match. Skipped");
							Log4jAP.log.trace(apLogObj.getCompleteTxt());
						}

					} catch (NumberFormatException | SQLException e) {
						Log4j.log.error("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
								+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

						Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
								+ gts.common(ReturnCode.DatabaseError));

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][SvfSendRegResponse] DB Error occurred: " + e.getMessage());
						apLogObj.setThrowable(e.getMessage());
						Log4jAP.log.error(apLogObj.getCompleteTxt());

						inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
						inLogObj.setThrowable(e.getMessage());
						inLogObj.setResponseTxt(gts.common(ReturnCode.DatabaseError));
						Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//						new OutputHandler(res, "application/json; charset=UTF-8")
//								.OutResult(gts.common(ReturnCode.DatabaseError));
						return gts.common(ReturnCode.DatabaseError);
					}

				} else if (replace.equals("2")) {
					// disable all user listed in idgateIDs
					Log4j.log.info("[" + sessionID + "][Version: " + svVerNo
							+ "][SvfSendRegResponse] Disable all id in idgateIDs requested");
					apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
							+ "][SvfSendRegResponse] Disable all id in idgateIDs requested");
					Log4jAP.log.info(apLogObj.getCompleteTxt());

					for (Map.Entry<String, String> set : idList.entrySet()) {

						Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Disabling old id "
								+ set.getValue());
						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][SvfSendRegResponse] Disabling old id " + set.getValue());
						Log4jAP.log.trace(apLogObj.getCompleteTxt());

						try {
							// fetch and disable specified idgate id
							memVO = memSrv.getByIdgateID(Long.parseLong(set.getValue()));

							if (memVO != null && memVO.getDevice_ID().equals(deviceID)) {
								memVO = memSrv.getByIdgateID(Long.parseLong(set.getValue()));

								memSrv.addMemberStatusLog(memVO.getIDGateID(), memVO.getCustomer_Status(), MemberStatus.Deleted,
										sessionID);
								memVO.setCustomer_Status(MemberStatus.Deleted);
								memSrv.updateMember(memVO);
								
								String fidoResp = simpleFidoFunc.deregister(req.getLocale(), WSM_API_Key, set.getValue(), channel, sessionID);
								Log4j.log.debug("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] FIDO deregister response: "
										+ fidoResp);
								apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] FIDO deregister response: "
										+ fidoResp);
								Log4jAP.log.debug(apLogObj.getCompleteTxt());
							} else {

								Log4j.log.trace("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Old id "
										+ set.getValue() + ",s device ID doesn't match. Skipped");
								apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Old id "
										+ set.getValue() + ",s device ID doesn't match. Skipped");
								Log4jAP.log.trace(apLogObj.getCompleteTxt());
							}

						} catch (NumberFormatException | SQLException e) {
							Log4j.log.error(
									"[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] DB Error occurred: "
											+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

							Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: "
									+ gts.common(ReturnCode.DatabaseError));

							apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
									+ "][SvfSendRegResponse] DB Error occurred: " + e.getMessage());
							apLogObj.setThrowable(e.getMessage());
							Log4jAP.log.error(apLogObj.getCompleteTxt());

							inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
							inLogObj.setThrowable(e.getMessage());
							inLogObj.setResponseTxt(gts.common(ReturnCode.DatabaseError));
							Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//							new OutputHandler(res, "application/json; charset=UTF-8")
//									.OutResult(gts.common(ReturnCode.DatabaseError));
							return gts.common(ReturnCode.DatabaseError);
						}
					}
				}
			} else if (jSONObject.getString("returnCode").length() == 4) {
				jSONObject.put("returnCode", "F" + jSONObject.getString("returnCode"));
			}

			retMsg = jSONObject.toString();
		} catch (JsonSyntaxException | JSONException e) {
			Log4j.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to parse FIDO JSON msg: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][SvfSendRegResponse] Unable to parse FIDO JSON msg: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		Log4j.log.info("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: " + retMsg);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][SvfSendRegResponse] Response: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(retMsg);
		Log4jInbound.log.info(inLogObj.getCompleteTxt());

//		new OutputHandler(res, "application/json; charset=UTF-8").OutResult(retMsg);
		return retMsg;
	}
}
