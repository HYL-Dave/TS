package com.WSI.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.Members.model.MembersService;
import com.Members.model.MembersVO;
import com.WSI.model.CheckParameters;
import com.WSI.model.Log4jWSI;
import com.WSM.model.SimpleFidoFunc;
import com.WSM.model.Gson.Gson4Common;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Servlet implementation class svDeRegister
 */
@RestController
// @WebServlet(description = "註銷裝置", urlPatterns = { "/svDeRegister" })
public class svDeRegister extends BaseServlet {
	@Autowired
	SimpleFidoFunc simpleFidoFunc;

	@PostMapping("/svDeRegister")
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
		inLogObj.setClazz("com.WSI.controller.svDeRegister");
		apLogObj.setTraceID(sessionID);
		apLogObj.setClazz("com.WSI.controller.svDeRegister");

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
				"[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] API key in header: " + headerApiKey);
		apLogObj.setMessage(
				"[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] API key in header: " + headerApiKey);
		Log4jAP.log.debug(apLogObj.getCompleteTxt());

		if (headerApiKey == null || !WSI_API_Key.equals(headerApiKey)) {
			Log4jWSI.log.debug("[" + sessionID + "][Version: " + svVerNo
					+ "][svDeRegister] Reject client request for invalid API key. Response: "
					+ gts.common(ReturnCode.APIKeyInvalid));
			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svDeRegister] Reject client request for invalid API key. Response: "
					+ gts.common(ReturnCode.APIKeyInvalid));
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
			Log4jWSI.log.fatal(
					"[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Error occurred while reading body: "
							+ e.getMessage() + ". Response: " + gts.common(ReturnCode.InternalError));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Error occurred while reading body: "
							+ e.getMessage() + ". Response: " + gts.common(ReturnCode.InternalError));
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
		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Msg from body: " + jsonString);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Msg from body: " + jsonString);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		try {
			// store request msg into inbound request log part
			inLogObj.setRequest(jsonString);
			jSONObject = new JSONObject(jsonString);

			map.put("channel", jSONObject.getString("channel"));
			map.put("idgateIDs", jSONObject.getString("idgateIDs"));
			map.put("verifyType", jSONObject.getString("verifyType"));

		} catch (JsonSyntaxException | JSONException e) {
			Log4jWSI.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][svDeRegister] Unable to parse client JSON msg: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()) + ". Response: " + gts.common(ReturnCode.DatabaseError));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Unable to parse client JSON msg: "
							+ e.getMessage() + ". Response: " + gts.common(ReturnCode.DatabaseError));
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
		Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Locale: " + req.getLocale());

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Locale: " + req.getLocale());
		Log4jAP.log.trace(apLogObj.getCompleteTxt());

		String channel;
		String idgateIDs;
		String verifyType;
		if (!map.containsKey("Error")) {
			channel = map.get("channel");
			idgateIDs = map.get("idgateIDs");
			verifyType = map.get("verifyType");

			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] channel: " + channel
					+ ", idgateIDs: " + idgateIDs + ", verifyType: " + verifyType);

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] channel: " + channel
					+ ", idgateIDs: " + idgateIDs + ", verifyType: " + verifyType);
			Log4jAP.log.info(apLogObj.getCompleteTxt());
		} else {
			Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Error: " + map.get("Error")
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
		HashMap<String, String> idList = null;
		String retMsg = null;

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

			idList = gson.fromJson(idgateIDs, new TypeToken<HashMap<String, String>>() {
			}.getType());
			long idgID;
			if (verifyType.equals("-1")) {
				// Deregister them all
				for (Map.Entry<String, String> set : idList.entrySet()) {
					idgID = Long.parseLong(set.getValue());
					// check member
					memVO = memSrv.getByIdgateID(idgID);

					if (memVO == null) {
						Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Member not found. "
								+ idgID + " Skipped");

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Member not found. " + idgID + " Skipped");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						if (retMsg == null)
							retMsg = gts.common(ReturnCode.MemberNotFound);

						continue;

					} else if (!channel.equals(memVO.getChannel_Code())) {
						Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Member channel not match. " + idgID + " Skipped");

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Member channel not match. " + idgID + " Skipped");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						if (retMsg == null)
							retMsg = gts.common(ReturnCode.ChannelNotMatchToMember);

						continue;
					}
					// Member is deleted already
					else if (memVO.getCustomer_Status().equals(MemberStatus.Deleted)) {

						Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] " + idgID
								+ " has been deregisted already");

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] " + idgID
								+ " has been deregisted already");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						if (retMsg == null)
							retMsg = gts.common(ReturnCode.MemberDeletedError);

					} else if (memVO != null) {
						Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] account " + idgID
								+ " in mapping DB deregistered");
						// disable account
						memSrv.disableOneMemberID(idgID);
						memSrv.addMemberStatusLog(memVO.getIDGateID(), memVO.getCustomer_Status(), MemberStatus.Deleted,
								"Issued by " + channel);

						Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Sending to FIDO deregister API");
						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Sending to FIDO deregister API");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						// outward data to FIDO server
						if (set.getKey().equals(VerifyType.OFFLINE)) {
							retMsg = simpleFidoFunc.deregister_St(req.getLocale(), WSI_API_Key, set.getValue(), channel, sessionID);
						} else {
							retMsg = simpleFidoFunc.deregister(req.getLocale(), WSI_API_Key, set.getValue(), channel, sessionID);
						}
					}
				}
			} else {
				// only the idgateID that is using specified verifyType will be deregister
				if (idList.containsKey(verifyType)) {
					idgID = Long.parseLong(idList.get(verifyType));
					// check member
					memVO = memSrv.getByIdgateID(idgID);

					if (memVO == null) {
						Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Member not found. "
								+ idgID + " Skipped");

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Member not found. " + idgID + " Skipped");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						if (retMsg == null)
							retMsg = gts.common(ReturnCode.MemberNotFound);

					} else if (!channel.equals(memVO.getChannel_Code())) {
						Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Member channel not match. " + idgID + " Skipped");

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Member channel not match. " + idgID + " Skipped");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						if (retMsg == null)
							retMsg = gts.common(ReturnCode.ChannelNotMatchToMember);
					}
					// Member is deleted already
					else if (memVO.getCustomer_Status().equals(MemberStatus.Deleted)) {

						Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] " + idgID
								+ " has been deregisted already");

						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] " + idgID
								+ " has been deregisted already");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						if (retMsg == null)
							retMsg = gts.common(ReturnCode.MemberDeletedError);

					} else if (memVO != null) {
						Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] account " + idgID
								+ " in mapping DB deregistered");
						// disable account
						memSrv.disableOneMemberID(idgID);
						memSrv.addMemberStatusLog(memVO.getIDGateID(), memVO.getCustomer_Status(), MemberStatus.Deleted,
								"Issued by " + channel);

						Log4jWSI.log.trace("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Sending to FIDO deregister API");
						apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
								+ "][svDeRegister] Sending to FIDO deregister API");
						Log4jAP.log.info(apLogObj.getCompleteTxt());

						// outward data to FIDO server
						if (verifyType.equals(VerifyType.OFFLINE)||verifyType.equals(VerifyType.OFFLINE2)) {
							retMsg = simpleFidoFunc.deregister_St(req.getLocale(), WSI_API_Key, idList.get(verifyType),
									channel, sessionID);
						} else {
							retMsg = simpleFidoFunc.deregister(req.getLocale(), WSI_API_Key, idList.get(verifyType),
									channel, sessionID);
						}

						map.clear();
						map.put("returnMsg", retMsg);
						map = cp.checkMap(map);

						if (map.containsKey("Error")) {
							Log4jWSI.log.warn("[" + sessionID + "][Version: " + svVerNo
									+ "][SvfSetAuthType] FIDO SV respond error: " + retMsg);
							apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
									+ "][SvfSetAuthType] FIDO SV respond error: " + retMsg);
							Log4jAP.log.warn(apLogObj.getCompleteTxt());
						}

					}

				} else {
					retMsg = gts.common(ReturnCode.MemberNotFound);
				}
			}
		} catch (SQLException e) {
			Log4jWSI.log.error("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] DB Error occurred: "
					+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()) + ". Response: "
					+ gts.common(ReturnCode.DatabaseError));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] DB Error occurred: "
					+ e.getMessage() + ". Response: " + gts.common(ReturnCode.DatabaseError));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.DatabaseError));
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.DatabaseError));
			return gts.common(ReturnCode.DatabaseError);
		} catch (JsonSyntaxException e) {
			Log4jWSI.log.error("[" + sessionID + "][Version: " + svVerNo
					+ "][svDeRegister] Unable to parse idgateIDs JSON: " + e.getMessage() + ", stacktrace: "
					+ new Gson().toJson(e.getStackTrace()) + ". Response: " + gts.common(ReturnCode.JsonParseError));

			apLogObj.setMessage(
					"[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Unable to parse idgateIDs JSON: "
							+ e.getMessage() + ". Response: " + gts.common(ReturnCode.JsonParseError));
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
			inLogObj.setThrowable(e.getMessage());
			inLogObj.setResponseTxt(gts.common(ReturnCode.JsonParseError));
			Log4jInbound.log.fatal(inLogObj.getCompleteTxt());
//			new OutputHandler(res, "application/json; charset=UTF-8").OutResult(gts.common(ReturnCode.JsonParseError));
			return gts.common(ReturnCode.APIKeyInvalid);
		}
		
		try {
			jSONObject = new JSONObject(retMsg);
			if (jSONObject.getString("returnCode").equals("0000")) {
				jSONObject.put("returnCode", "0" + jSONObject.getString("returnCode"));
			} else if(jSONObject.getString("returnCode").length() == 4) {
				jSONObject.put("returnCode", "F" + jSONObject.getString("returnCode"));
			}
			
			retMsg = jSONObject.toString();
		} catch (JsonSyntaxException | JSONException e) {
			Log4jWSI.log
					.error("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Unable to parse FIDO JSON msg: "
							+ e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));

			apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo
					+ "][svDeRegister] Unable to parse FIDO JSON msg: " + e.getMessage());
			apLogObj.setThrowable(e.getMessage());
			Log4jAP.log.error(apLogObj.getCompleteTxt());

			retMsg = gts.common(ReturnCode.FidoSVError);
		}

		Log4jWSI.log.info("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Response: " + retMsg);

		apLogObj.setMessage("[" + sessionID + "][Version: " + svVerNo + "][svDeRegister] Response: " + retMsg);
		Log4jAP.log.info(apLogObj.getCompleteTxt());

		inLogObj.setExecuteTime(System.currentTimeMillis() - startTime);
		inLogObj.setResponseTxt(retMsg);
		Log4jInbound.log.info(inLogObj.getCompleteTxt());

//		new OutputHandler(res, "application/json; charset=UTF-8").OutResult(retMsg);
		return retMsg;
	}
}
