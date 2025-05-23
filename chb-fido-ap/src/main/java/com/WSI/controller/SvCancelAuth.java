package com.WSI.controller;

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
import com.Members.model.MembersService;
import com.Members.model.MembersVO;
import com.WSI.model.CheckParameters;
import com.WSI.model.Log4jWSI;
import com.WSM.model.Gson.Gson4Common;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.toppanidgate.WSM.controller.WSMServlet;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Servlet implementation class svCancelAuth
 */
@RestController
// @WebServlet(description = "取消驗證", urlPatterns = { "/svCancelAuth" })
public class SvCancelAuth extends BaseServlet {
	@Autowired
	WSMServlet wsmServlet;

	@PostMapping("/svCancelAuth")
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
		inLogObj.setClazz("com.WSI.controller.svCancelAuth");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSI.controller.svCancelAuth");

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

			WSI_API_Key = properties.getProperty("WSI_API_Key");

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

		Log4jWSI.log.debug(
				"[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSI_API_Key.equals(headerApiKey)) {
			Log4jWSI.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Reject client request for invalid API key");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Reject client request for invalid API key");
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
			Log4jWSI.log.fatal("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Error occurred while reading body: " + e.getMessage());

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Error occurred while reading body: " + e.getMessage());
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
		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Msg from body: " + jsonString);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));
			map.put("idgateID", jSONObject.getString("idgateID"));
			map.put("txnID", jSONObject.getString("txnID"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4jWSI.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Unable to parse client JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Unable to parse client JSON msg: " + e.getMessage());
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
		Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Locale: " + req.getLocale());

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		String channel;
		String idgateID;
		String txnID;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
			idgateID = map.get("idgateID");
			txnID = map.get("txnID");

			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] channel: " + channel
					+ ", idgateID: " + idgateID + ", txnID: " + txnID);

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] channel: " + channel
					+ ", idgateID: " + idgateID + ", txnID: " + txnID);
			Log4jAP.log.info(apLogObj.getCompleteTxt());
		} else {
			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Error: " + map.get("Error")
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
		MembersService memSrv = new MembersService(JNDI_Name, sessionID);
		MembersVO memVO = null;
		try {
			// check if this channel exist
			chVO = chSvc.getOneChannel(channel);
			// fetch member info
			memVO = memSrv.getByIdgateID(Long.parseLong(idgateID));

		} catch (NumberFormatException e) {
			Log4jWSI.log.fatal("Unable to read idgateID: " + e.getMessage());

			apLogObj.setMessage("Unable to read idgateID: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.fatal(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setRequest("{}");
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());

			Gson4Common gson4 = new Gson4Common();
			gson4.setReturnCode("M0001");
			gson4.setReturnMsg("Unable to read idgateID: " + idgateID);
			return gson.toJson(gson4);
		} catch (SQLException e) {
			Log4jWSI.log.error("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] DB Error occurred: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.DatabaseError));
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.DatabaseError));
			return gts.common(ReturnCode.DatabaseError);
		}

		if (chVO == null) {
			apLogObj.setMessage(gts.common(ReturnCode.ChannelInvalidError));
			Log4jAP.log.info(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelInvalidError));
			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.common(ReturnCode.ChannelInvalidError));
			return gts.common(ReturnCode.ChannelInvalidError);
		}

		if (memVO == null) {
			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Member not found. Response: "
					+ gts.common(ReturnCode.MemberNotFound));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Member not found. Response: " + gts.common(ReturnCode.MemberNotFound));
			Log4jAP.log.info(apLogObj.getCompleteTxt());
			
			try {
				// store response msg into inbound response log part
				inLogObj.setResponseTxt(gts.common(ReturnCode.MemberNotFound));
				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);

				if (inLogObj.hasException()) {
					Log4jInbound.log.warn(inLogObj.getCompleteTxt());
				} else {
					Log4jInbound.log.info(inLogObj.getCompleteTxt());
				}
			} catch (JsonSyntaxException e) {
				Log4jWSI.log.error("[Version: " + svVerNo + "][svCancelAuth] JsonSyntaxException: "
						+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
			}

//			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
//			inLogObj.setResponseTxt(gts.common(ReturnCode.MemberNotFound));
//			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.MemberNotFound));
			return gts.common(ReturnCode.MemberNotFound);
		}

		// Member is locked or deleted already
		if (memVO.getCustomer_Status().equals(MemberStatus.Deleted)) {

			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Response: "
					+ gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null, null, null,
							null, null, null, null, null, null));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Response: "
					+ gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null, null, null,
							null, null, null, null, null, null));
			Log4jAP.log.info(apLogObj.getCompleteTxt());
			
			try {
				// store response msg into inbound response log part
				inLogObj.setResponseTxt(gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null,
						null, null, null, null, null, null, null, null));
				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);

				if (inLogObj.hasException()) {
					Log4jInbound.log.warn(inLogObj.getCompleteTxt());
				} else {
					Log4jInbound.log.info(inLogObj.getCompleteTxt());
				}
			} catch (JsonSyntaxException e) {
				Log4jWSI.log.error("[Version: " + svVerNo + "][svCancelAuth] JsonSyntaxException: "
						+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
			}

//			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
//			inLogObj.setResponseTxt(gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null,
//					null, null, null, null, null, null, null, null));
//			Log4jInbound.log.info(inLogObj.getCompleteTxt());

//			new OutputHandler(res, "application/json; charset=UTF-8")
//					.OutResult(gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null,
//							null, null, null, null, null, null, null, null));
			return gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null,
					null, null, null, null, null, null, null, null);
		}

		Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Sending to FIDO svCancelAuth API");
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Sending to FIDO svCancelAuth API");
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		// outward data to FIDO server
		HashMap<String, String> formparams = new HashMap<String, String>();
		formparams.put("method", "svCancelAuth");
		formparams.put("idgateID", idgateID);
		formparams.put("channel", channel);
		formparams.put("txnID", txnID);

		String retMsg = null;
		try {
			// DONE
			retMsg = wsmServlet.doPost(WSI_API_Key, req.getLocale(), gson.toJson(formparams), sessionID);
//			retMsg = new Send2Remote().sendPost(FIDO_SV_IP, WSI_API_Key, gson.toJson(formparams));
		} catch (IOException e) {
			Log4jWSI.log.error("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Connection to FIDO error: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Connection to FIDO error: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());
			
			try {
				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setThrowable(e.getMessage());
				inLogObj.setResponseTxt(gts.common(ReturnCode.FidoSVError));
				Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
			} catch (JsonSyntaxException e1) {
				Log4jWSI.log.error("[Version: " + svVerNo + "][svCancelAuth] JsonSyntaxException: "
						+ e1.getMessage() + ", stacktrace: " + new Gson().toJson(e1.getStackTrace()));
			}

//			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
//			inLogObj.setThrowable(e.getMessage());
//			inLogObj.setResponseTxt(gts.common(ReturnCode.FidoSVError));
//			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
			
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.FidoSVError));
			return gts.common(ReturnCode.FidoSVError);
		}

		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] FIDO SV Response: " + retMsg);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] FIDO SVResponse: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		map.clear();
		map.put("returnMsg", retMsg);
		map = cp.checkMap(map);

		if (map.containsKey("Error")) {
			Log4jWSI.log.debug("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Error: " + map.get("Error"));
			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		try {
			jSONObject = new JSONObject(retMsg);
			if (jSONObject.getString("returnCode").equals("0000")) {
				jSONObject.put("returnCode", "0" + jSONObject.getString("returnCode"));
			} else {
				jSONObject.put("returnCode", "F" + jSONObject.getString("returnCode"));
			}
			
			retMsg = jSONObject.toString();
		} catch (JsonSyntaxException | JSONException e) {
			Log4jWSI.log
					.error("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Unable to parse FIDO JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svCancelAuth] Unable to parse FIDO JSON msg: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svCancelAuth] Response: " + retMsg);

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(retMsg);
		Log4jInbound.log.info(inLogObj.getCompleteTxt());

//		new OutputHandler(res, "application/json; charset=UTF-8").OutResult(retMsg);
		return retMsg;
	}
}
