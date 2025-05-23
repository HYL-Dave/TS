package com.WSI.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
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
import com.Device_Detail.model.Device_DetailService;
import com.Device_Detail.model.Device_DetailVO;
import com.Members.model.MembersService;
import com.Members.model.MembersVO;
import com.WSI.model.CheckParameters;
import com.WSI.model.Log4jWSI;
import com.WSM.model.Gson.Gson4Common;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Servlet implementation class svUpdatePushToken
 */
@RestController
// @WebServlet(description = "更新裝置push token", urlPatterns = { "/svUpdatePushToken" })
public class svUpdatePushToken extends BaseServlet {
	
	@PostMapping("/svUpdatePushToken")
	protected String doPost(@RequestBody Object reqBody, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		inLogObj = new InboundLogFormat();
		apLogObj = new APLogFormat();

		// 取得 Session ID
		final String sessionID = req.getSession().getId();
		// get api key
		final String headerApiKey = req.getHeader("apiKey");

		Properties properties = null;
		File exCfgFile = new File(Config_file_path);

		inLogObj.setTraceID(sessionID);
		inLogObj.setClazz("com.WSI.controller.svUpdatePushToken");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSI.controller.svUpdatePushToken");

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
				"[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSI_API_Key.equals(headerApiKey)) {
			Log4jWSI.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][svUpdatePushToken] Reject client request for invalid API key");
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svUpdatePushToken] Reject client request for invalid API key");
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
					+ "][svUpdatePushToken] Error occurred while reading body: " + e.getMessage());

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svUpdatePushToken] Error occurred while reading body: " + e.getMessage());
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
		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Msg from body: " + jsonString);

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));
			map.put("idgateID", jSONObject.getString("idgateID"));
			map.put("pushToken", jSONObject.getString("pushToken"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4jWSI.log.error(
					"[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Unable to parse client JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svUpdatePushToken] Error occurred while reading body: " + e.getMessage());
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
		Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Locale: " + req.getLocale());

		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		final String channel;
		final String idgateID;
		final String pushToken;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
			idgateID = map.get("idgateID");
			pushToken = map.get("pushToken");

			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] channel: " + channel
					+ ", idgateID: " + idgateID + ", pushToken: " + pushToken);

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] channel: " + channel
					+ ", idgateID: " + idgateID + ", pushToken: " + pushToken);
			Log4jAP.log.info(apLogObj.getCompleteTxt());
		} else {
			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Error: " + map.get("Error")
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
		Device_DetailService ddSrv = new Device_DetailService(JNDI_Name, sessionID);
		Device_DetailVO ddVO = null;
		MembersService memSrv = new MembersService(JNDI_Name, sessionID);
		MembersVO memVO = null;

		try {
			// check if this channel exist
			chVO = chSvc.getOneChannel(channel);

			if (chVO == null) {
				apLogObj.setMessage(gts.common(ReturnCode.ChannelInvalidError));
				Log4jAP.log.info(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelInvalidError));
				Log4jInbound.log.info(inLogObj.getCompleteTxt());

//				new OutputHandler(res, "application/json; charset=UTF-8")
//						.OutResult(gts.common(ReturnCode.ChannelInvalidError));
				return gts.common(ReturnCode.ChannelInvalidError);
			}
			
			// fetch member info
			memVO = memSrv.getByIdgateID(Long.parseLong(idgateID));
			ddVO = ddSrv.getOneDevice_Detail(Long.parseLong(idgateID));

			if (memVO == null) {
				Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo
						+ "][svUpdatePushToken] Member not found. Response: " + gts.common(ReturnCode.MemberNotFound));

				apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
						+ "][svUpdatePushToken] Member not found. Response: " + gts.common(ReturnCode.MemberNotFound));
				Log4jAP.log.info(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setResponseTxt(gts.common(ReturnCode.MemberNotFound));
				Log4jInbound.log.info(inLogObj.getCompleteTxt());

//				new OutputHandler(res, "application/json; charset=UTF-8")
//						.OutResult(gts.common(ReturnCode.MemberNotFound));
				return gts.common(ReturnCode.MemberNotFound);
			} else if (!channel.equals(memVO.getChannel_Code())) {
				Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo
						+ "][svUpdatePushToken] Member channel not match. Response: "
						+ gts.common(ReturnCode.ChannelNotMatchToMember));

				apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
						+ "][svUpdatePushToken] Member channel not match. Response: "
						+ gts.common(ReturnCode.ChannelNotMatchToMember));
				Log4jAP.log.info(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setResponseTxt(gts.common(ReturnCode.ChannelNotMatchToMember));
				Log4jInbound.log.info(inLogObj.getCompleteTxt());

//				new OutputHandler(res, "application/json; charset=UTF-8")
//						.OutResult(gts.common(ReturnCode.ChannelNotMatchToMember));
				return gts.common(ReturnCode.ChannelNotMatchToMember);
			}
			// Member is locked or deleted already
			if (memVO.getCustomer_Status().equals(MemberStatus.Deleted)) {

				Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Response: "
						+ gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null, null,
								null, null, null, null, null, null, null));

				apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Response: "
						+ gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null, null,
								null, null, null, null, null, null, null));
				Log4jAP.log.info(apLogObj.getCompleteTxt());

				inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
				inLogObj.setResponseTxt(gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(),
						null, null, null, null, null, null, null, null, null));
				Log4jInbound.log.info(inLogObj.getCompleteTxt());

//				new OutputHandler(res, "application/json; charset=UTF-8")
//						.OutResult(gts.get_DeviceStatus(ReturnCode.MemberStatusError, memVO.getCustomer_Status(), null,
//								null, null, null, null, null, null, null, null));
				return gts.common(ReturnCode.MemberStatusError);
			}

			Log4jWSI.log.trace(
					"[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Update account push token");

			ddVO.setDevice_ID(pushToken);
			ddVO.setModified_Date(new Timestamp(System.currentTimeMillis()));

			ddSrv.updatePushID(ddVO);

		} catch (SQLException e) {
			Log4jWSI.log.error("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] DB Error occurred: "
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

		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Response: "
				+ gts.common(ReturnCode.Success));

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svUpdatePushToken] Response: "
				+ gts.common(ReturnCode.Success));
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(gts.common(ReturnCode.Success));
		Log4jInbound.log.info(inLogObj.getCompleteTxt());

//		new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.Success));
		return gts.common(ReturnCode.Success);
	}
}
