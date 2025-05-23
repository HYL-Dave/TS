package com.Cvn.Core;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.Common.Model.Send2Remote;
import com.Cvn.Config.Cfg;
import com.Cvn.Database.ChannelTrustService;
import com.Cvn.Database.DevicesService;
import com.Cvn.Database.KeyStoreService;
import com.Cvn.Encryptor.AES;
import com.Cvn.Encryptor.CvnSecrets;
import com.Cvn.Encryptor.Encode;
import com.Cvn.Encryptor.Hash;
import com.Cvn.Encryptor.RSA;
import com.Cvn.Encryptor.Rndm;
import com.Cvn.Verifier.Verifier;
import com.TrustServer.Func.TS_MainFunc;
import com.google.gson.Gson;
import com.toppanidgate.idenkey.common.model.ReturnCode;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class CvnCore {
	private static final Gson gson = new Gson();

	@Value("${HSMProvider}")
	private String HSMProvider;
	@Value("${SoftRSA}")
	private String SoftRSA;

	@Value("${DbKeyAlias}")
	private String DbKeyAlias;
	
	@Value("${RsaKeyAlias}")
	private String RsaKeyAlias;

	@Value("${SetKeyAlias}")
	private String SetKeyAlias;

	@Value("${SysCode:#{null}}")
	private String SysCode;

	@Value("${TxnCode:#{null}}")
	private String TxnCode;

	@Value("${SenderCode:#{null}}")
	private String SenderCode;
	@Value("${ReceiverCode:#{null}}")
	private String ReceiverCode;
	@Value("${OperatorCode:#{null}}")
	private String OperatorCode;

	@Value("${UnitCode:#{null}}")
	private String UnitCode;
	@Value("${AuthorizerCode:#{null}}")
	private String AuthorizerCode;

	@Value("${EncryptUrl}")
	private String EncryptUrl;

	@Value("${HSM_SLOT}")
	private String HSM_SLOT;

	@Value("${HSM_PXD}")
	private String HSM_PXD;

	@Value("${DecryptUrl}")
	private String DecryptUrl;

	@Autowired
	private RSA myrsa;

	@Autowired
	KeyStoreService keyStoreSvc;

	@Autowired
	ChannelTrustService channelTrustService;

	@Autowired
	DevicesService devicesService;

	@Autowired
	Verifier Verifier;

	@Autowired
	Rndm rndm;

	private static Logger logger = LogManager.getLogger(CvnCore.class);
	public CvnCore() {
	}

	public String signupPerso(String channel, String devType, String esnSec, String seedSec, String masterSec,
			String userID, String devData, String pinHash, String sessionId) {

		logger.info("[" + sessionId + "][Version:][signupPerso] -> #Channel:[" + channel
				+ "] - #DevType:[" + devType + "] - #esnSecret:[" + esnSec + "] - #seedSecret:[" + seedSec
				+ "] - #masterSecret:[" + masterSec + "] - #userID:[" + userID + "] - #devData:[" + devData
				+ "] - #PinHash:[" + pinHash + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String[] newPersoFile;

		if (devType.length() != 1 || userID.length() > 15) {
			rspData.put("ReturnCode", ReturnCode.ParameterError);
			rspData.put("ReturnMsg", "Invalid parameter.");
			rspData.put("ESN", "");
			rspData.put("Mercury", "");
			rspData.put("DevID", "");
			rspData.put("PersoFile", "");

			return gson.toJson(rspData);
			
		}

		try {
			newPersoFile = Verifier.generatePersoData(channel, devType, esnSec, seedSec, masterSec, devData, pinHash,
					sessionId);

			if (newPersoFile[0].equals("")) {
				rspData.put("ReturnCode", "0016");
				rspData.put("ReturnMsg", "Generate perso-file failed.");
				rspData.put("ESN", "");
				rspData.put("Mercury", "");
				rspData.put("DevID", "");
				rspData.put("PersoFile", "");

				return gson.toJson(rspData);

			}

			String newPersoChk = Hash.encode_CRC32_Hex(newPersoFile[3].getBytes("StandardCharsets.UTF_8"), 8, sessionId);

			String[] checkUser = devicesService.getDevice_UserID(userID, sessionId);
			if (checkUser[0].equals("")) {
				logger.info("[" + sessionId + "][Version:][signupPerso] - #userID:["
						+ userID + "] - #Insert into DB.");
				devicesService.createDevice(userID, newPersoFile[0], devData, newPersoFile[1], "0", newPersoFile[2],
						pinHash, sessionId);
			} else {
				logger.info("[" + sessionId + "][Version:][signupPerso] - #userID:["
						+ userID + "] - #Update into DB.");
				devicesService.updateDevice(userID, newPersoFile[0], devData, newPersoFile[1], "0", newPersoFile[2],
						pinHash, sessionId);
			}

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Success.");
			rspData.put("ESN", newPersoFile[0]);
			rspData.put("Mercury", newPersoFile[1]);
			rspData.put("DevID", newPersoFile[2]);
			rspData.put("PersoFile", newPersoFile[3] + newPersoChk);

			String rsp = gson.toJson(rspData).replace("\\", "").replace(":\"{", ":{").replace("}\"", "}");
			return rsp;

		} catch (Exception ex) {

			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate PersoFile failed: " + ex.getMessage() + ", stacktrace: "
					+ ExceptionUtils.getStackTrace(ex));
			rspData.put("ESN", "");
			rspData.put("Mercury", "");
			rspData.put("DevID", "");
			rspData.put("PersoFile", "");

			String rsp = gson.toJson(rspData).replace("\\", "").replace(":\"{", ":{").replace("}\"", "}");
			return rsp;
		}
	}

	public String getDeviceInfo(String esn, String sessionId) {

		logger.info("[{}][Version:][getDeviceInfo] - #esn:[{}]", sessionId,esn);

		HashMap<String, Object> rspData = new HashMap<String, Object>();

		try {
			String[] myDeviceInfo = devicesService.getDevice_Esn(esn, sessionId);

			if (myDeviceInfo[0].equals("")) {
				rspData.put("ReturnCode", ReturnCode.MemberNotFound);
				rspData.put("ReturnMsg", "Device not found.");
			} else {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Success.");
				rspData.put("UserID", myDeviceInfo[0]);
				rspData.put("DevData", myDeviceInfo[1]);
				rspData.put("Mercury", myDeviceInfo[2]);
				rspData.put("ErrCount", myDeviceInfo[3]);
				rspData.put("ErrMax", myDeviceInfo[4]);
				rspData.put("Status", myDeviceInfo[5]);
				rspData.put("DevID", myDeviceInfo[6]);
			}

		} catch (Exception e) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Access database error: " + e.getMessage());
		}

		return gson.toJson(rspData);
	}

	public String lockDevice(String esn, String remark, String sessionId) {

		logger.info("[{}][Version:][lockDevice] - #esn:[{}] - #remark:[{}]", sessionId,esn,remark);

		String rsp = "";
		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String[] checkDevice;

		try {
			checkDevice = devicesService.getDevice_Esn(esn, sessionId);

		} catch (Exception ex) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Check device status failed: " + ex.getMessage());
			return gson.toJson(rspData);
		}

		if (checkDevice[0].equals("")) {
			rspData.put("ReturnCode", ReturnCode.MemberNotFound);
			rspData.put("ReturnMsg", "Device not found.");
			return gson.toJson(rspData);
		}

		if (checkDevice[5].equals("0")) {
			try {
				devicesService.updateDevice_Status(esn, "L", sessionId);
				logger.info("[" + sessionId + "][Version:][lockDevice] - #esn:[" + esn
						+ "] - #UserID:[" + checkDevice[0] + "] - #Remark:[" + remark + "] - #Device locked.");
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Device locked.");
				return gson.toJson(rspData);

			} catch (Exception ex) {
				logger.error("[" + sessionId + "][Version:][lockDevice] - #Lock device failed: " + ex.getMessage());
				rspData.put("ReturnCode", ReturnCode.DatabaseError);
				rspData.put("ReturnMsg", "Lock device failed: " + ex.getMessage());
				return gson.toJson(rspData);
			}

		} else if (checkDevice[5].equals("L")) {

			logger.info("[" + sessionId + "][Version:][lockDevice] #esn:[" + esn
					+ "] - #UserID:[" + checkDevice[0] + "] - #Remark:[" + remark + "] - #Device alreay locked.");
			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Device already locked.");
			return gson.toJson(rspData);
		} else {
			rspData.put("ReturnCode", ReturnCode.MemberStatusError);
			rspData.put("ReturnMsg", "Lock device failed: Invalid device status:(" + checkDevice[5] + ")");
			return gson.toJson(rspData);
		}

	}

	public String unlockDevice(String esn, String remark, String sessionId) {

		logger.info("[" + sessionId + "][Version:][unlockDevice] #esn:[" + esn
				+ "] - #remark:[" + remark + "]");

		String rsp = "";
		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String[] checkDevice;

		try {
			checkDevice = devicesService.getDevice_Esn(esn, sessionId);

		} catch (Exception ex) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Check device status failed: " + ex.getMessage());
			return gson.toJson(rspData);
		}

		if (checkDevice[0].equals("")) {
			rspData.put("ReturnCode", ReturnCode.MemberNotFound);
			rspData.put("ReturnMsg", "Device not found.");
			return gson.toJson(rspData);
		}

		if (checkDevice[5].equals("L")) {
			try {
				devicesService.updateDevice_Status(esn, "0", sessionId);

				logger.info("[" + sessionId + "][Version:][unlockDevice] #esn:[" + esn
						+ "] - #UserID:[" + checkDevice[0] + "] - #Remark:[" + remark + "] - #Device unlocked.");
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Unlock device Success.");
				return gson.toJson(rspData);

			} catch (Exception ex) {
				logger.error("[" + sessionId + "][Version:[unlockDevice] - #Unlock device failed: " + ex.getMessage());
				rspData.put("ReturnCode", ReturnCode.DatabaseError);
				rspData.put("ReturnMsg", "Unlock device failed: " + ex.getMessage());
				return gson.toJson(rspData);
			}
		} else if (checkDevice[5].equals("0")) {
			logger.info("[" + sessionId + "][Version:][unlockDevice] #esn:[" + esn
					+ "] - #UserID:[" + checkDevice[0] + "] - #Remark:[" + remark + "] - #Device already unlocked.");
			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Device already unlocked.");
			return gson.toJson(rspData);

		} else {
			rspData.put("ReturnCode", ReturnCode.MemberStatusError);
			rspData.put("ReturnMsg", "Unlock device failed: Invalid device status:(" + checkDevice[5] + ")");
			return gson.toJson(rspData);
		}

	}

	public String updateMercury(String esn, String sessionId) {

		logger.info(
				"[" + sessionId + "][Version:][updateMercury] -> #esn:[" + esn + "]");

		String rsp = "";
		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String[] myDeviceInfo = null;

		try {
			myDeviceInfo = devicesService.getDevice_Esn(esn, sessionId);

		} catch (Exception e) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Access database error: " + e.getMessage());
			return gson.toJson(rspData);
		}

		if (myDeviceInfo[0].equals("")) {
			rspData.put("ReturnCode", ReturnCode.MemberNotFound);
			rspData.put("ReturnMsg", "User not found.");
			return gson.toJson(rspData);
		}

		if (!myDeviceInfo[5].equals("0")) {
			logger.info("[" + sessionId + "][Version:][updateMercury] -> #esn:[" + esn
					+ "] - #UserID:[" + myDeviceInfo[0] + "] - #Invalid device status, update mercury failed.");
			rspData.put("ReturnCode", ReturnCode.MemberStatusError);
			rspData.put("ReturnMsg", "Invalid device status, update mercury failed.");
			return gson.toJson(rspData);
		}

		String oriMercury = myDeviceInfo[2];
		String newMercuryTail = rndm.generateRdmHexStr(10);
		String newMercury = oriMercury.substring(10) + newMercuryTail;
		logger.info("[" + sessionId + "][Version:][updateMercury] - #userID:["
				+ myDeviceInfo[0] + "] - oriMercury:[" + oriMercury + "] - newMercury:[" + newMercury
				+ "] - #Update to DB...");

		try {
			devicesService.updateDevice_Mercury(esn, newMercury, 0, sessionId);
			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Success.");
			return gson.toJson(rspData);

		} catch (Exception e) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Update mercury error: " + e.getMessage());
			return gson.toJson(rspData);
		}

	}

	public String generateOTP(String channel, String esn, String challenge, String txnData, String sessionId) {

		logger.info("[" + sessionId + "][Version:][generateOTP] -> #channel:[" + channel
				+ "] - #esn:[" + esn + "] - #challenge:[" + challenge + "] - #txnData:[" + txnData + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		HashMap<String, Object> channelDetail = null;

		if (channel.length() <= 3) {
			channelDetail = channelTrustService.getChannelById(channel, sessionId);
		} else {
			channelDetail = channelTrustService.getChannelByName(channel, sessionId);
		}

		if (channelDetail == null) {
			rspData.put("ReturnCode", ReturnCode.ChannelInvalidError);
			rspData.put("ReturnMsg", "Channel not found");
			return gson.toJson(rspData);
		}

		// read OTP settings if channel is found
		int timeInterval = Integer.parseInt(channelDetail.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(channelDetail.get("OTP_Length").toString());

		String[] myDeviceInfo;

		try {
			myDeviceInfo = devicesService.getDevice_Esn(esn, sessionId);
		} catch (Exception e) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Access database error: " + e.getMessage());
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}

		if (myDeviceInfo[0].equals("")) {
			rspData.put("ReturnCode", ReturnCode.MemberNotFound);
			rspData.put("ReturnMsg", "User not found.");
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}

		if (!myDeviceInfo[5].equals("0")) {
			logger.info("[" + sessionId + "][Version:][generateOTP] #esn:[" + esn
					+ "] - #UserID:[" + myDeviceInfo[0] + "] - #Invalid device status, generate OTP failed.");
			rspData.put("ReturnCode", ReturnCode.MemberStatusError);
			rspData.put("ReturnMsg", "Invalid device status, generate OTP failed.");
			return gson.toJson(rspData);
		}

		byte[] seed = null;
		try {
			seed = Verifier.generateSEED(esn, sessionId);

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate seed failed: " + ex.getMessage());
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}

		String genOTP = "";
		java.util.Date myDate = new java.util.Date();
		long currUnixTime = myDate.getTime();
		long timeStep = currUnixTime / timeInterval;

		logger.info("[" + sessionId + "][Version:][generateOTP] - #esn:[" + esn
				+ "] - #currUnixTime(" + currUnixTime + ") - timeInterval:(" + timeInterval + ") - timeStep:("
				+ timeStep + ").");

		try {
			genOTP = Verifier.truncator(
					Verifier.generateOTP(Hash.encode_HmacSHA1(
							Verifier.generateOCRAData(timeStep, challenge, txnData, sessionId), seed, sessionId)),
					otpLength);

			logger.info("[" + sessionId + "][Version:][generateOTP] - #Legal-OTP("
					+ timeStep + "):[" + genOTP + "] has been generated.");

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Success");
			rspData.put("OTP", genOTP);
			return gson.toJson(rspData);

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate OTP failed: " + ex.getMessage());
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}
	}

	public String verifyDevIdOTP(String channel, String esn, String challenge, String txnData, String otp,
			String timeStamp, String sessionId) {

		logger.info("[" + sessionId + "][Version:][verifyDevIdOTP] ->> #channel:["
				+ channel + "] - #esn:[" + esn + "] - #OTP:[" + otp + "] - #TimeStamp:[" + timeStamp
				+ "] - #challenge:[" + challenge + "] - #txnData:[" + txnData + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		HashMap<String, Object> channelDetail = null;

		if (channel.length() <= 3) {
			channelDetail = channelTrustService.getChannelById(channel, sessionId);
		} else {
			channelDetail = channelTrustService.getChannelByName(channel, sessionId);
		}

		if (channelDetail == null) {
			rspData.put("ReturnCode", ReturnCode.ChannelInvalidError);
			rspData.put("ReturnMsg", "Channel not found");
			return gson.toJson(rspData);
		}
		// read OTP settings if channel is found
		int timeInterval = Integer.parseInt(channelDetail.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(channelDetail.get("OTP_Length").toString());
		int otpRange = Integer.parseInt(channelDetail.get("OTP_Range").toString());

		String[] myDeviceInfo;

		try {
			myDeviceInfo = devicesService.getDevice_Esn(esn, sessionId);
		} catch (Exception e) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Access database error: " + e.getMessage());
			return gson.toJson(rspData);
		}

		if (myDeviceInfo[0].equals("")) {
			rspData.put("ReturnCode", ReturnCode.MemberNotFound);
			rspData.put("ReturnMsg", "User not found.");
			return gson.toJson(rspData);
		}

		if (!myDeviceInfo[5].equals("0") && !myDeviceInfo[5].equals("A")) {
			logger.info("[" + sessionId + "][Version:][verifyDevIdOTP] #esn:[" + esn
					+ "] - #UserID:[" + myDeviceInfo[0] + "] - #Invalid device status.");
			rspData.put("ReturnCode", ReturnCode.MemberStatusError);
			rspData.put("ReturnMsg", "Invalid device status.");
			return gson.toJson(rspData);
		}

//		byte[] seed = Verifier.generateSEED(esn, sessionId);
//		String Xesn = Encode.byteToHex(Verifier.generateXESN(esn, sessionId));
		byte[] seed3 = null;
		try {
			seed3 = Verifier.generateDID(esn, sessionId);
//			if (logger.isEnabledFor(Level.DEBUG)) {
//			}
				logger.debug("[" + sessionId + "][Version:[verifyDevIdOTP] - #Generate Seed3(DevID): " + Encode.byteToHex(seed3));

		} catch (Exception ex) {
			logger.error("[" + sessionId + "][Version:][verifyDevIdOTP] - #Generate Seed3(DevID) error: " + ex.getMessage());
		}
		
//		logger.debug("*** Encode.byteToHex(seed3):" + Encode.byteToHex(seed3));
//		logger.debug("*** challenge:" + challenge);
//		logger.debug("*** txnData:" + txnData);
//		logger.debug("*** otp:" + otp);
//		logger.debug("*** otpLength:" + otpLength);

		java.util.Date myDate = new java.util.Date();
		long currUnixTime = myDate.getTime();
		long timeStep = currUnixTime / timeInterval;
		int v_counter = 0 - (otpRange);
		String verifyResult = "X";

//		logger.debug("*** timeStep:" + timeStep);
//		logger.debug("*** v_counter:" + v_counter);
//		logger.debug("*** timeStep + v_counter:" + timeStep + v_counter);
//		logger.info("[" + sessionId + "][Version:][verifyDevIdOTP] - #esn:[" + esn
//				+ "] - #currUnixTime(" + currUnixTime + "),timeInterval:(" + timeInterval + "),timeStep:(" + timeStep
//				+ ").");

		try {
			while (v_counter <= otpRange) {
				// byte[] myOTPsrc =
				// Encryptor.encrypt_HmacSHA1(Encryptor.generateOCRAData(timeStep + v_counter),
				// seed); // 20 Bytes
				
				String genOTP = Verifier.truncator(Verifier.generateOTP(Hash.encode_HmacSHA1(
						Verifier.generateOCRAData((timeStep + v_counter), challenge, txnData, sessionId), seed3,
						sessionId)), otpLength);
//				 logger.debug("*** genOTP:" + genOTP);
				if (otp.equals(genOTP)) {
					verifyResult = "00";
					break;
				} else {
					// TODO BELOW MUST BE REMARKED
					logger.debug("[" + sessionId + "][verifyOTP] - #Legal-OTP(" + v_counter + ","
							+ (timeStep + v_counter) + "):[" + genOTP + "] - #Not matched.");
					v_counter++;
				}
			}

			if (verifyResult.equals("00")) {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Verify OTP Success.");
				return gson.toJson(rspData);
			} else {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", "Verify failed: invalid OTP.");
				return gson.toJson(rspData);
			}

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Verify failed: " + ex.getMessage());
			return gson.toJson(rspData);
		}
	}

	public String verifyEsnOTP(String channel, String esn, String challenge, String txnData, String otp,
			String timeStamp, String sessionId) {
		logger.info("[" + sessionId + "][Version:][verifyEsnOTP] ->> #channel:["
				+ channel + "] - #esn:[" + esn + "] - #OTP:[" + otp + "] - #TimeStamp:[" + timeStamp
				+ "] - #challenge:[" + challenge + "] - #txnData:[" + txnData + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		HashMap<String, Object> channelDetail = null;

		if (channel.length() <= 3) {
			channelDetail = channelTrustService.getChannelById(channel, sessionId);
		} else {
			channelDetail = channelTrustService.getChannelByName(channel, sessionId);
		}

		if (channelDetail == null) {
			rspData.put("ReturnCode", ReturnCode.ChannelInvalidError);
			rspData.put("ReturnMsg", "Channel not found");
			return gson.toJson(rspData);
		}
		// read OTP settings if channel is found
		int timeInterval = Integer.parseInt(channelDetail.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(channelDetail.get("OTP_Length").toString());
		int otpRange = Integer.parseInt(channelDetail.get("OTP_Range").toString());

		String[] myDeviceInfo;

		try {
			myDeviceInfo = devicesService.getDevice_Esn(esn, sessionId);
		} catch (Exception e) {
			rspData.put("ReturnCode", ReturnCode.DatabaseError);
			rspData.put("ReturnMsg", "Access database error: " + e.getMessage());

			return gson.toJson(rspData);
		}

		if (myDeviceInfo[0].equals("")) {
			rspData.put("ReturnCode", ReturnCode.MemberNotFound);
			rspData.put("ReturnMsg", "User not found.");
			return gson.toJson(rspData);
		}

		if (!myDeviceInfo[5].equals("0") && !myDeviceInfo[5].equals("A")) {
			logger.info("[" + sessionId + "][Version:][verifyEsnOTP] #esn:[" + esn
					+ "] - #UserID:[" + myDeviceInfo[0] + "] - #Invalid device status.");
			rspData.put("ReturnCode", ReturnCode.MemberStatusError);
			rspData.put("ReturnMsg", "Invalid device status.");
			return gson.toJson(rspData);
		}

		byte[] Seed2 = null;
		Seed2 = Verifier.generateXESN(esn, sessionId);
		logger.info("[" + sessionId + "][Version:][verifyEsnOTP] - #Generate Seed2(Xesn): " + Encode.byteToHex(Seed2));

		java.util.Date myDate = new java.util.Date();
		long currUnixTime = myDate.getTime();
		long timeStep = currUnixTime / timeInterval;
		int v_counter = 0 - (otpRange);
		String verifyResult = "X";

		logger.info("[" + sessionId + "][Version:][verifyEsnOTP] - #esn:[" + esn
				+ "] - #currUnixTime(" + currUnixTime + "),timeInterval:(" + timeInterval + "),timeStep:(" + timeStep
				+ ").");

		try {
			while (v_counter <= otpRange) {
				// byte[] myOTPsrc =
				// Encryptor.encrypt_HmacSHA1(Encryptor.generateOCRAData(timeStep + v_counter),
				// seed); // 20 Bytes
				String genOTP = Verifier.truncator(Verifier.generateOTP(Hash.encode_HmacSHA1(
						Verifier.generateOCRAData((timeStep + v_counter), challenge, txnData, sessionId), Seed2,
						sessionId)), otpLength);

				if (otp.equals(genOTP)) {
					verifyResult = "00";
					break;
				} else {
					v_counter++;
				}
			}

			if (verifyResult.equals("00")) {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Verify OTP Success.");
				return gson.toJson(rspData);
			} else {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", "Verify failed: invalid OTP.");
				return gson.toJson(rspData);
			}

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Verify failed: " + ex.getMessage());
			return gson.toJson(rspData);
		}
	}

	public String verifyOTP(String channel, String deviceData2Seed, String challenge, String txnData, String otp,
			String timeStamp, String sessionId) {

		// logger.info("[" + sessionId + "][verifyOTP] -> #esn:[" + deviceData2Seed + "]
		// - #OTP:[" + otp + "] - #TimeStamp:[" + timeStamp + "] - #challenge:[" +
		// challenge + "] - #txnData:[" + txnData + "]");

		HashMap<String, Object> data = channelTrustService.getChannelById(channel, sessionId);
		logger.info("data in verifyOTP : {}", data);
		if (!data.get("ReturnCode").equals(ReturnCode.Success)||(data.containsKey("No_Data") && data.get("No_Data").equals(true))) {
			return gson.toJson(data);

		}

		int otpRange = Integer.parseInt(data.get("OTP_Range").toString());
		long otpInterval = Long.parseLong(data.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(data.get("OTP_Length").toString());

		String rsp = "";
		HashMap<String, Object> rspData = new HashMap<String, Object>();

		byte[] seed = null;
		try {
			seed = Encode.hexToByte(deviceData2Seed);
		} catch (Exception ex) {
			logger.error("[" + sessionId + "][verifyOTP] - #Generate seed error: " + ex.getMessage());
		}

		java.util.Date myDate = new java.util.Date();
		long currUnixTime = myDate.getTime();
		long timeStep = currUnixTime / otpInterval;
		int v_counter = 0 - (otpRange);
		String verifyResult = "X";

		try {
			while (v_counter <= otpRange) {
				String genOTP = Verifier.truncator(Verifier.generateOTP(Hash.encode_HmacSHA1(
						Verifier.generateOCRAData((timeStep + v_counter), challenge, txnData, sessionId), seed,
						sessionId)), otpLength);
				// logger.info("*** genOTP:" + genOTP);
				if (otp.equals(genOTP)) {
					// logger.info("[" + sessionId + "][verifyOTP] - #Legal-OTP(" + v_counter + "," + (timeStep + v_counter) + "):[" + genOTP + "] - #Verify success.");
					verifyResult = "00";
					break;
				} else {
					// TODO BELOW MUST BE REMARKED
//					logger.debug("[" + sessionId + "][verifyOTP] - #Legal-OTP(" + v_counter + ","
	//						+ (timeStep + v_counter) + "):[" + genOTP + "] - #Not matched.");
					v_counter++;
				}
			}

			if (verifyResult.equals("00")) {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Verify OTP Success.");
			} else {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", "Verify failed: invalid OTP.");
			}

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Verify failed: " + ex.getMessage());
		}

		// logger.info("[" + sessionId + "][verifyOTP] <- #Response:[" + rsp + "]");
		return gson.toJson(rspData);

	}

	/**
	 * Generate a TOTP with fixed-esn.
	 * 
	 * @param txnData : The input hex-string.
	 * @return
	 */
	public String generateSmsOTP(String channel, String txnData, String sessionId) {
		HashMap<String, Object> rspData = new HashMap<String, Object>();
		logger.info("[" + sessionId + "][Version:][generateSmsOTP] -> #channel:["
				+ channel + "] - #txnData:[" + txnData + "]");

		if (!txnData.matches("[0-9a-fA-F]*")) {
			rspData.put("ReturnCode", 0010);
			rspData.put("ReturnMsg", "Invalid txnData, should be a hex string.");
			rspData.put("OTP", "-");
			return gson.toJson(rspData);
		}

		HashMap<String, Object> channelDetail = null;

		if (channel.length() <= 3) {
			channelDetail = channelTrustService.getChannelById(channel, sessionId);
		} else {
			channelDetail = channelTrustService.getChannelByName(channel, sessionId);
		}

		if (channelDetail == null) {
			rspData.put("ReturnCode", ReturnCode.ChannelInvalidError);
			rspData.put("ReturnMsg", "Channel not found");
			return gson.toJson(rspData);
		}

		// read OTP settings if channel is found
		int timeInterval = Integer.parseInt(channelDetail.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(channelDetail.get("OTP_Length").toString());

		String esn = "SMSREMSOCUTE2019";

		byte[] seed = null;
		try {
			seed = Verifier.generateSEED(esn, sessionId);

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate seed failed with exception: " + ex.getMessage());
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}

		if (seed == null) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate seed failed.");
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}

		String genOTP = "";
		java.util.Date myDate = new java.util.Date();
		long currUnixTime = myDate.getTime();
		long timeStep = currUnixTime / timeInterval;

		try {
			genOTP = Verifier.truncator(Verifier.generateOTP(Hash.encode_HmacSHA1(
					Verifier.generateOCRAData(timeStep, "0000000000000000", txnData, sessionId), seed, sessionId)),
					otpLength);

			logger.info("[" + sessionId + "][Version:][generateSmsOTP] - #Legal-OTP("
					+ timeStep + "):[" + genOTP + "] has been generated.");

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Success");
			rspData.put("OTP", genOTP);
			return gson.toJson(rspData);

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate OTP failed: " + ex.getMessage());
			rspData.put("OTP", "");
			return gson.toJson(rspData);
		}

	}

	public String verifySmsOTP(String channel, String txnData, String otp, String timeStamp, String sessionId) {

		logger.info("[" + sessionId + "][Version:][verifySmsOTP] -> #channel:["
				+ channel + "] - #txnData:[" + txnData + "] - #OTP:[" + otp + "] - #TimeStamp:[" + timeStamp + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		HashMap<String, Object> channelDetail = null;

		if (channel.length() <= 3) {
			channelDetail = channelTrustService.getChannelById(channel, sessionId);
		} else {
			channelDetail = channelTrustService.getChannelByName(channel, sessionId);
		}

		if (channelDetail == null) {
			rspData.put("ReturnCode", ReturnCode.ChannelInvalidError);
			rspData.put("ReturnMsg", "Channel not found");
			return gson.toJson(rspData);
		}
		// read OTP settings if channel is found
		int timeInterval = Integer.parseInt(channelDetail.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(channelDetail.get("OTP_Length").toString());
		int otpRange = Integer.parseInt(channelDetail.get("OTP_Range").toString());

		String esn = "SMSREMSOCUTE2019";

		byte[] seed = null;
		try {
			seed = Verifier.generateSEED(esn, sessionId);

		} catch (Exception ex) {
			logger.error("[" + sessionId + "][Version:][verifySmsOTP] - #Generate seed error: " + ex.getMessage());
		}

		java.util.Date myDate = new java.util.Date();
		long currUnixTime = myDate.getTime();
		long timeStep = currUnixTime / timeInterval;
		int v_counter = 0 - (otpRange);
		String verifyResult = "X";

		try {
			while (v_counter <= otpRange) {
				// byte[] myOTPsrc =
				// Encryptor.encrypt_HmacSHA1(Encryptor.generateOCRAData(timeStep + v_counter),
				// seed); // 20 Bytes
				String genOTP = Verifier.truncator(Verifier.generateOTP(Hash.encode_HmacSHA1(
						Verifier.generateOCRAData((timeStep + v_counter), "0000000000000000", txnData, sessionId), seed,
						sessionId)), otpLength);

				if (otp.equals(genOTP)) {
//					if (logger.isEnabledFor(Level.DEBUG)) {
//					}
//					logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
//							+ "][verifySmsOTP] - #Legal-OTP(" + v_counter + "):[" + genOTP + "] - #Verify success.");
					verifyResult = "00";
					break;
				} else {
//					if (logger.isEnabledFor(Level.DEBUG)) {
//					}
					// TODO BELOW MUST BE REMARKED
//					logger.debug("[" + sessionId + "[verifySmsOTP] - #Legal-OTP(" + v_counter + "):[" + genOTP
//							+ "] - #Not matched.");
					v_counter++;
				}
			}

			if (verifyResult.equals("00")) {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Verify OTP Success.");
			} else {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", "Verify failed: invalid OTP.");
			}

		} catch (Exception ex) {
			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Verify failed: " + ex.getMessage());
		}

		return gson.toJson(rspData);
	}

	public String createKey_RSA(String keyAlias, String sessionId) {

		logger.info("[" + sessionId + "][Version:][createKey_RSA] -> #Generate a Cvn-RSA key - #keyAlias:[" + keyAlias + "] - #SoftRSA:[" + SoftRSA
				+ "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		

		// use native api to generate RSA
		if ("true".equals(SoftRSA)) {
			String rspMsg = myrsa.genKeyPair(keyAlias, sessionId);
			if (rspMsg.contains("Success")) {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", rspMsg);
			} else {
				rspData.put("ReturnCode", "0042");
				rspData.put("ReturnMsg", rspMsg);
			}
		} else {
			// use HSM to generate RSA
			switch (HSMProvider) {

			case "SafeNet":
				String rspMsg = myrsa.genKeyPair(keyAlias, sessionId);
				if (rspMsg.contains("Success")) {
					rspData.put("ReturnCode", ReturnCode.Success);
					rspData.put("ReturnMsg", rspMsg);
				} else {
					rspData.put("ReturnCode", "0042");
					rspData.put("ReturnMsg", rspMsg);
				}
				break;

			case "Cvn":
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Create (" + HSMProvider + ") RSA-key(" + keyAlias
						+ ") result:[In Cvn mode, take key-alias as your key.]");
				break;

			default:
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Create (" + HSMProvider + ") RSA-key(" + keyAlias
						+ ") result:[Support RSA providers: SafeNet, Cvn.]");
				break;
			}

		}

		return gson.toJson(rspData);
	}

	public String encrypt_RSA(String plainTxt, String kAlias, String sessionId) {

		
//		logger.info("[" + sessionId + "][Version:][encrypt_RSA] -> #plainTxt:["
//				+ plainTxt + "] - #keyAlias:[" + kAlias + "].");

		HashMap<String, Object> rspData = new HashMap<String, Object>();

		if ("true".equals(SoftRSA)) {
			String encTxt = myrsa.encrypt_B64(plainTxt, kAlias, sessionId,SoftRSA);
			if (encTxt.contains("ERROR:")) {
				rspData.put("ReturnCode", ReturnCode.InternalError);
				rspData.put("ReturnMsg", "Cvn-RSA encrypted error:" + encTxt);
				rspData.put("EncData", "--");
			} else {
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Success.");
				rspData.put("EncData", encTxt);
			}
		} else {

		}

		return gson.toJson(rspData);
	}

	public String decrypt_RSA(String enTxt, String kAlias, String sessionId) {

		// Encode space to +
		enTxt = enTxt.replace(" ", "+");
		logger.info("[" + sessionId + "][Version:][decrypt_RSA] -> #cipherTxt:[" + enTxt
				+ "] - #keyAlias:[" + kAlias + "].");

		HashMap<String, Object> rspData = new HashMap<String, Object>();

		String plainTxt = myrsa.decrypt_B64(enTxt, kAlias, sessionId,SoftRSA);
		if (plainTxt.contains("ERROR:")) {
			rspData.put("ReturnCode", ReturnCode.InternalError);
			rspData.put("ReturnMsg", "Cvn-RSA decrypted error:" + plainTxt);
			rspData.put("Data", "--");
		} else {
			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Cvn-RSA decrypted.");
			rspData.put("Data", plainTxt);
		}

		return gson.toJson(rspData);

	}

	public String createKey_AES(String random, String kAlias, String sessionId) {

		

		logger.info("[" + sessionId + "][createKey_AES] -> #HSMProvider:[" + HSMProvider + "] - #keyAlias:[" + kAlias
				+ "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String result;
		// create master key
		String mk = Hash.encode_SHA256_Hex(random + CvnSecrets.masterSec_ret, sessionId);
//		mk = encrypt_AES(mk, "DbKeyAlias", sessionId);
		HashMap<String, Object> tmpData = encrypt_AES(mk, DbKeyAlias, sessionId);

		//HashMap<String, String> tmpData = gson.fromJson(mk, new TypeToken<HashMap<String, String>>() {
		//}.getType());

		if (!tmpData.get("ReturnCode").equals(ReturnCode.Success)) {
			rspData.put("ReturnCode", ReturnCode.KeyGenErr);
			rspData.put("ReturnMsg", "Key generation failed at phase 1");
		} else {
			mk = (String) tmpData.get("EncData");
			tmpData.clear();
			switch (HSMProvider) {

			case "ECSP":
			
				// encrypt master key
				tmpData = encrypt_AES(mk, "SetKeyAlias", sessionId);
				//tmpData = gson.fromJson(mk, new TypeToken<HashMap<String, String>>() {
				//}.getType());

				//result = dao.storeKey("MasterKey", tmpData.get("EncData"), " ", sessionId);
				result = keyStoreSvc.storeKey("MasterKey", (String) tmpData.get("EncData"), " ");
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Create (" + HSMProvider + ") AES-key(MasterKey) result:[" + result + "]");
				break;

			case "SafeNet":
				
//				int slotId = Integer.parseInt(Cfg.getExternalCfgValue("HSM_SLOT"));
//				String slotPwd = Cfg.getExternalCfgValue("HSM_PXD");

//				com.HSM.SafeNet.AES aes = new com.HSM.SafeNet.AES(slotId, slotPwd, sessionId);

				tmpData = encrypt_AES(mk, SetKeyAlias, sessionId);
				//tmpData = gson.fromJson(mk, new TypeToken<HashMap<String, String>>() {
				//}.getType());
				
				//result = dao.storeKey("MasterKey", tmpData.get("EncData"), " ", sessionId);
				result = keyStoreSvc.storeKey("MasterKey", (String) tmpData.get("EncData"), " ");
//				result = com.HSM.SafeNet.AES.genKey(slotId, slotPwd, kAlias, sessionId);

				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg",
						"Create (" + HSMProvider + ") AES-key(" + kAlias + ") result:[" + result + "]");
				break;

			case "Cvn":
				result = "In Cvn mode, take key-alias as your key.";
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg",
						"Create (" + HSMProvider + ") AES-key(" + kAlias + ") result:[" + result + "]");
				break;

			default:
				result = "Support AES providers: PayShield, nCipher, SafeNet, Cvn.";
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg",
						"Create (" + HSMProvider + ") AES-key(" + kAlias + ") result:[" + result + "]");
				break;
			}
		}

		return gson.toJson(rspData);

	}

	public HashMap<String, Object> encrypt_AES(String plainTxt, String kAlias, String sessionId) {

			logger.debug("[" + sessionId + "][Version:][Version:][encrypt_AES] -> #plainTxt:[" + plainTxt + "] - #keyAlias:[" + kAlias
					+ "] - #HSMProvider:[" + HSMProvider + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		int dummyNeeded;

		if (HSMProvider.equals("Cvn")) {
			try {
				String pBin = Encode.byteToHex(plainTxt.getBytes(StandardCharsets.UTF_8));
				dummyNeeded = pBin.length() % 32;

				if (dummyNeeded > 0) {
					for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
						pBin += "00";
					}
				}

				byte[] plain_bin = pBin.getBytes(StandardCharsets.UTF_8);
				byte[] kBin = kAlias.getBytes(StandardCharsets.UTF_8);
				byte[] ci_bin = AES.drvRfc2898_encrypt(kBin, plain_bin, sessionId);
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Cvn-AES encrypted.");
				rspData.put("EncData", Encode.byteToHex(ci_bin));

			} catch (Exception e) {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", e.getMessage());
				rspData.put("EncData", "--");
			}

		} else {
			rspData.put("ReturnCode", ReturnCode.ParameterError);
			rspData.put("ReturnMsg", "Invalid provider.");
			rspData.put("EncData", "--");
		}

		return rspData;
//		return gson.toJson(rspData);


	}

	public String decrypt_AES(String enTxt, String kAlias, String sessionId) {

		logger.info("inside decrypt_AES");

//		if (logger.isEnabledFor(Level.DEBUG)) {
//		}
			logger.info("[" + sessionId + "][Version:][decrypt_AES] -> #cipherTxt:["
					+ enTxt + "] - #keyAlias:[" + kAlias + "] - #HSMProvider:[" + HSMProvider + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();

		if (HSMProvider.equals("Cvn")) {
			try {
				byte[] ci_bin = Encode.hexToByte(enTxt);
				byte[] k_bin = kAlias.getBytes(StandardCharsets.UTF_8);
				byte[] plain_bin = AES.drvRfc2898_decrypt(k_bin, ci_bin, sessionId);

				String retData = new String(plain_bin, StandardCharsets.UTF_8);
				int trimIndex = retData.indexOf("00");
				if (trimIndex > -1) {
					retData = retData.substring(0, trimIndex);
				}

				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Cvn-AES decrypted.");
				rspData.put("Data", retData);

			} catch (Exception e) {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", e.getMessage());
				rspData.put("Data", "--");
			}
		} else {
			rspData.put("ReturnCode", ReturnCode.ParameterError);
			rspData.put("ReturnMsg", "Invalid provider.");
			rspData.put("Data", "--");
		}

		return gson.toJson(rspData);

	}

	
	public String decrypt_Package(String aesSecret, String xdata, String devData, String sessionId) {

		aesSecret = aesSecret.replace("_", "/").replace("-", "+");
		xdata = xdata.replace("_", "/").replace("-", "+");
		HashMap<String, Object> retData = new HashMap<String, Object>();

		if (devData.equals("-"))
			devData = "";

		logger.info("[" + sessionId + "][decrypt_Package] -> #aesSecret:[" + aesSecret + "] - #xdata:[" + xdata
				+ "] - #devData:[" + devData + "]");

		if (!aesSecret.matches("[0-9a-zA-Z_+/=-]*")) {
			retData.put("ReturnCode", ReturnCode.ParameterError);
			retData.put("ReturnMsg", "Invalid AesSecret(" + aesSecret + ")");

			 logger.info("[" + sessionId + "][decrypt_Package] <- #Response:[" + gson.toJson(retData) + "]");
			 return gson.toJson(retData);
		}

		if (!xdata.matches("[0-9a-zA-Z_+/=-]*")) {
			retData.put("ReturnCode", ReturnCode.ParameterError);
			retData.put("ReturnMsg", "Invalid Xdata(" + xdata + ")");

			 logger.info("[" + sessionId + "][decrypt_Package] <- #Response:[" + gson.toJson(retData) + "]");
			 return gson.toJson(retData);
		}

		if (!devData.matches("[0-9a-fA-F-]*")) {
			retData.put("ReturnCode", ReturnCode.ParameterError);
			retData.put("ReturnMsg", "Invalid DevData(" + devData + ")");

			 logger.info("[" + sessionId + "][decrypt_Package] <- #Response:[" + gson.toJson(retData) + "]");
			 return gson.toJson(retData);
		}

		String plain_aesSecret = "ERROR: NULL";

//		plain_aesSecret = RSA.decrypt_B64(aesSecret, RsaKeyAlias,
//					Cfg.getExternalCfgValue("KeyStorePath"), sessionId);
		plain_aesSecret = myrsa.decrypt_B64(aesSecret, RsaKeyAlias, sessionId,SoftRSA);

		
		if (plain_aesSecret.contains("ERROR:")) {
			retData.put("ReturnCode", "0030");
			retData.put("ReturnMsg", "Computing RSA decryption failed:" + plain_aesSecret);
			retData.put("Data", "-");

			 logger.info("[{}][decrypt_Package] <- #Response:[{}]",sessionId,retData);
			 return gson.toJson(retData);
		}

//		logger.trace("[" + sessionId + "][decrypt_Package] - #Computing Rfc2898 AES-decrypt - #baseKey:["
//				+ plain_aesSecret + devData + "] - #cipherTxt:[" + xdata + "]");

		byte[] plain_data_stream;
		Map<String, Object> decryptedData = null;
		logger.info("*** plain_aesSecret + devData:" + plain_aesSecret + devData);
		try {
			plain_data_stream = AES.drvRfc2898_decrypt(
					Hash.encode_SHA256((plain_aesSecret + devData).getBytes(StandardCharsets.UTF_8), sessionId),
					Encode.hexToByte(xdata), sessionId);
//			plain_data_stream = com.Cvn.Encryptor.AES.drvRfc2898_decrypt(
//					com.Cvn.Encryptor.Hash.get_SHA256((plain_aesSecret + devData).getBytes(StandardCharsets.UTF_8), sessionId),
//					Base64.getDecoder().decode(xdata), sessionId);
			if (plain_data_stream.length == 0) {
				retData.put("ReturnCode", ReturnCode.InternalError);
				retData.put("ReturnMsg", "Computing Rfc2898 AES-decrypt failed");
				retData.put("Data", "-");

				logger.error("[" + sessionId + "][decrypt_Package] <- #Response:[" + gson.toJson(retData) + "]");
				return gson.toJson(retData);
			} else {
				decryptedData = new JSONObject(new String(plain_data_stream, StandardCharsets.UTF_8)).toMap();
				//decryptedData = gson.fromJson(new String(plain_data_stream, StandardCharsets.UTF_8),
				//		new TypeToken<HashMap<String, String>>() {
				//		}.getType());

				retData.put("ReturnCode", ReturnCode.Success);
				retData.put("ReturnMsg", "Success.");
				retData.put("Data", decryptedData);

//				logger.trace("[" + sessionId + "][decrypt_Package] <- #Response:[" + gson.toJson(retData) + "]");
				return gson.toJson(retData);
			}

		} catch (Exception e) {
			retData.put("ReturnCode", ReturnCode.InternalError);
			retData.put("ReturnMsg", "Encoding plain data failed: " + e.getMessage());
			retData.put("Data", "-");
			return gson.toJson(retData);
		}

	}

	public String get_RsaPublic(String sessionId) {

		logger.info("[" + sessionId + "][Version:][get_RsaPublic] -> #Loading RSA public key.");

		String rsaSec = myrsa.loadPublicKey(RsaKeyAlias, sessionId,SoftRSA);

		if (rsaSec.contains("ERROR:")) {
			String rspMsg = "{\"ReturnCode\":\"0011\",\"ReturnMsg\":\"" + "Loading RSA public key failed:" + rsaSec
					+ "\",\"RsaSecret\":\"" + "-" + "\"}";
			return rspMsg;
		} else {
			String rspMsg = "{\"ReturnCode\":\"0000\",\"ReturnMsg\":\""
					+ "Loading RSA public key loaded.\",\"RsaSecret\":\"" + rsaSec + "\"}";
			return rspMsg;
		}

	}

	public String add_Channel(String channelID, String channelName, int otpLength, long timeInterval, int timeRange,
											   String sessionId) {
		logger.info("[" + sessionId + "][Version:][add_Channel] -> #channelID:["
				+ channelID + "] - #channelName:[" + channelName + "] - #otpLength:[" + otpLength
				+ "] - #timeInterval:[" + timeInterval + "] - #timeRange:[" + timeRange + "]");

		//return dao.addChannel(channelID, channelName, otpLength, timeRange, (int)timeInterval, sessionId);
		return gson.toJson(channelTrustService.addChannel(channelID, channelName, otpLength, timeRange, (int)timeInterval, sessionId));
	}

	public String get_Channel_ByID(String channelID, String sessionId) {
		logger.info("[" + sessionId + "][Version:][get_Channel_ByID] -> #channelID:["
				+ channelID + "]");

		//return gson.toJson(dao.getChannelById(channelID, sessionId));
		return gson.toJson(channelTrustService.getChannelById(channelID, sessionId));
	}

	public String get_Channel_ByName(String channelName, String sessionId) {
		logger.info("[" + sessionId + "][Version:][get_Channel_ByName] -> #channelName:[" + channelName + "]");

		//return gson.toJson(dao.getChannelByName(channelName, sessionId));
		return gson.toJson(channelTrustService.getChannelByName(channelName, sessionId));
	}

	public String list_Channel(String sessionId) {
		//return gson.toJson(dao.getChannelList(sessionId));
		return gson.toJson(channelTrustService.getChannelList(sessionId));
	}

	public String monitor(String sessionId) {

		String dbCode = "";
		String dbMsg = "";
		String hsmCode = "";
		String hsmMsg = "";

		// Check database.
		String[] myDeviceInfo = devicesService.getDevice_Esn("CHK001102", sessionId);
		if (myDeviceInfo[0].equals("0")) {
			dbCode = "0";
			dbMsg = "Active";
		} else {
			dbCode = "X";
			dbMsg = myDeviceInfo[0];
		}

		// Check HSM.
//		
//		String HSMKeyAlias = Cfg.getExternalCfgValue("PersoKeyAlias");
//		if (HSMProvider.equals("PayShield")) {
//			AES aes = new AES(sessionId);
//			String[] loadKey = dao.getProtector(HSMKeyAlias, sessionId);
//			aes.import_Key(loadKey[0], sessionId);
//			hsmMsg = aes.encrypt_Msg("CH00110211021102", sessionId);
//			if (hsmMsg.length() == 64) {
//				hsmCode = "0";
//				hsmMsg = "Active";
//			} else {
//				hsmCode = "X";
//			}
//		}

		return "{\"dbCode\":\"" + dbCode + "\", " + "\"dbMsg\":\"" + dbMsg + "\", " + "\"hsmCode\":\"" + hsmCode
				+ "\", " + "\"hsmMsg\":\"" + hsmMsg + "\"} ";
	}

	public void redirect(HttpServletResponse response) throws IOException {
		// request.getRequestDispatcher("/WEB-INF/animation.html").forward(request,response);
		response.sendRedirect("animation.html");
	}

	public String signupPersoFIDO(String channel, String devType, String esnSecret, String seedSecret, String masterSecret,
			String userID, String devData, String pinHash, String sessionId) {

		// logger.info("[" + sessionId + "][signupPerso] -> #Channel:[" + channel + "] - #DevType:[" + devType + "] - #esnSecret:[" + esnSecret + "] - #seedSecret:[" + seedSecret + "] - #masterSecret:[" + masterSecret + "] - #userID:[" + userID + "] - #devData:[" + devData + "] - #PinHash:[" + pinHash + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
//		String[] newPersoFile;
		// 允許50字英數+底線組合
		if (channel.length() > 50 || devType.length() != 1 || userID.length() > 15) {
			rspData.put("ReturnCode", "0010");
			rspData.put("ReturnMsg", "Invalid parameter.");
			rspData.put("ESN", "");
			rspData.put("Mercury", "");
			rspData.put("DevID", "");
//			rspData.put("PersoFile", "");
			return gson.toJson(rspData);
		}

		try {
			// FIDO 不用 generatePersoData
//			newPersoFile = Verifier.generatePersoData(channel, devType, esnSecret, seedSecret, masterSecret, devData,
//					pinHash, sessionId);
			// + Buildup PersoData-ESPack-ESN
			java.util.Date myDate = new java.util.Date();
			long unixTime = myDate.getTime();
			String esn = Verifier.generateESN(channel, devType, unixTime, sessionId);
			String MercuryAB = rndm.generateRdmHexStr(20);
			String perso_DevID = Encode.byteToHex(Verifier.generateDID(esn, sessionId));
			// return new String[] { esn, MercuryAB, perso_DevID, EncPersoFile };
			if (esn.equals("")) { // esn
				rspData.put("ReturnCode", "0016");
				rspData.put("ReturnMsg", "Generate perso-file failed.");
				rspData.put("ESN", "");
				rspData.put("Mercury", "");
				rspData.put("DevID", "");
//				rspData.put("PersoFile", "");

				return gson.toJson(rspData);
			}

//			String newPersoChk = Hash.get_CRC32_Hex(newPersoFile[3].getBytes("StandardCharsets.UTF_8"), 8, sessionId);
			logger.info("userID:{}",userID);
			String[] checkUser = devicesService.getDevice_UserID(userID, sessionId);
			if (checkUser[0].equals("")) {
				// logger.info("[" + sessionId + "][signupPerso] - #userID:[" + userID + "] - #Insert into DB.");
				devicesService.createDevice(userID, esn, devData, MercuryAB, "0", perso_DevID, pinHash, sessionId);
			} else {
				// logger.info("[" + sessionId + "][signupPerso] - #userID:[" + userID + "] - #Update into DB.");
				devicesService.updateDevice(userID, esn, devData, MercuryAB, "0", perso_DevID, pinHash, sessionId);
			}

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Success.");
			rspData.put("ESN", esn); // newPersoFile[0]
			rspData.put("Mercury", MercuryAB); // newPersoFile[1]
			rspData.put("DevID", perso_DevID); // newPersoFile[2]
//			rspData.put("PersoFile", newPersoFile[3] + newPersoChk);

			return gson.toJson(rspData);

		} catch (Exception ex) {

			rspData.put("ReturnCode", "0007");
			rspData.put("ReturnMsg", "Generate PersoFile failed: " + ex.getMessage() + ", stacktrace: "
					+ExceptionUtils.getStackTrace(ex) );
			rspData.put("ESN", "");
			rspData.put("Mercury", "");
			rspData.put("DevID", "");
//			rspData.put("PersoFile", "");

			return gson.toJson(rspData);
		}
	}
	
	public String encrypt_AES_always_HSM(String plainTxt, String kAlias, String sessionId) {

	

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String encTxt = null;
		int dummyNeeded;

		switch (HSMProvider) {

		case "ECSP":
			try {
				plainTxt = Encode.byteToHex(plainTxt.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("[" + sessionId + "[encrypt_AES_always_HSM] - # Data convert error:" + e.getMessage());
				return null;
			}

			dummyNeeded = plainTxt.length() % 32;

			if (dummyNeeded != 0) {
				for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
					plainTxt += "00";
				}
			}

			Timestamp ts = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			HashMap<String, Object> data = new HashMap<String, Object>();
			HashMap<String, Object> secLayer = new HashMap<String, Object>();
			HashMap<String, String> trdLayer = new HashMap<String, String>();

			data.put("msgNo", Cfg.getExternalCfgValue("SysCode") + "_" + String.valueOf(ts.getTime()) + "_"
					+ sessionId.substring(0, 4));
			data.put("txnTime", sdf.format(ts));
			data.put("txnCode", Cfg.getExternalCfgValue("TxnCode"));
			data.put("senderCode", Cfg.getExternalCfgValue("SenderCode"));
			data.put("receiverCode", Cfg.getExternalCfgValue("ReceiverCode"));
			data.put("operatorCode", Cfg.getExternalCfgValue("OperatorCode"));
			data.put("unitCode", Cfg.getExternalCfgValue("UnitCode"));
			data.put("authorizerCode", Cfg.getExternalCfgValue("AuthorizerCode"));

			trdLayer.put("data", plainTxt);
			secLayer.put("keyIndex", kAlias);
			secLayer.put("secDatasets", trdLayer);
			data.put("requestBody", secLayer);

			logger.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
					+ "][encrypt_AES_always_HSM] - # Generate xEsnSrc (AES with key index: " + kAlias + ")");

			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
					+ "][encrypt_AES_always_HSM] - # Sending msg: " + gson.toJson(data));

			String enTxt = null;
			try {
				String remoteRsp = new Send2Remote().post(EncryptUrl, gson.toJson(data));

				logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
						+ "][encrypt_AES_always_HSM] - #ECSP response:[" + remoteRsp + "]");

				if (remoteRsp.equals("")) {
					logger.error("[" + sessionId
							+ "[encrypt_AES_always_HSM] - # Encryption (ECSP-AES) failed with empty result.");
					return null;
				}

				JSONObject json = new JSONObject(remoteRsp);

				if (!(Cfg.getExternalCfgValue("ReceiverCode") + "_0000").equals(json.getString("resultCode"))) {
					logger.info(
							"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][encrypt_AES_always_HSM] - # Encryption (ECSP-AES) failed with error: ["
									+ json.getString("resultCode") + "]");
					return null;
				}

				enTxt = json.getJSONObject("resultBody").getJSONObject("cipherDatasets").getString("data");

			} catch (JSONException e) {
				logger.error("[" + sessionId + "[encrypt_AES_always_HSM] - # Unable to parse ECSP response:"
						+ e.getMessage());
				return null;
			} catch (IOException e) {
				logger.error(
						"[" + sessionId + "[encrypt_AES_always_HSM] - # Connecting to ECSP error:" + e.getMessage());
				return null;
			}

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "ECSP-AES encrypted.");
			rspData.put("EncData", enTxt);

			break;

//		case "SafeNet":
//			int slotId = Integer.parseInt(Cfg.getExternalCfgValue("HSM_SLOT"));
//			String slotPwd = Cfg.getExternalCfgValue("HSM_PXD");
//
//			logger.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][encrypt_AES_always_HSM] - # Calling HSM for encryption");
//
//			com.HSM.SafeNet.AES myAES = com.HSM.SafeNet.AES.initSafeNetAES(slotId, slotPwd, sessionId);
//			encTxt = myAES.encrypt_Hex(CvnSecrets.commonSec_ret, kAlias, sessionId);
//
//			if (encTxt.contains("ERROR:")) {
//				rspData.put("ReturnCode", ReturnCode.Fail);
//				rspData.put("ReturnMsg", encTxt);
//				rspData.put("EncData", "--");
//				break;
//			}
//
//			try {
//				encTxt = Encode.byteToHex(
//						AES.drvRfc2898_encrypt(encTxt.getBytes("UTF-8"), plainTxt.getBytes("UTF-8"), sessionId));
//			} catch (UnsupportedEncodingException | GeneralSecurityException e) {
//			}
//
//			if (encTxt.contains("ERROR:")) {
//				rspData.put("ReturnCode", ReturnCode.Fail);
//				rspData.put("ReturnMsg", encTxt);
//				rspData.put("EncData", "--");
//			} else {
//				rspData.put("ReturnCode", ReturnCode.Success);
//				rspData.put("ReturnMsg", "SafeNet-AES encrypted.");
//				rspData.put("EncData", encTxt);
//			}
//
//			break;

		case "Cvn":
			logger.debug("[" + sessionId + "][encrypt_AES_always_HSM] Using Cvn f0r encryption.");
			try {
				String pBin = Encode.byteToHex(plainTxt.getBytes("UTF-8"));
				dummyNeeded = pBin.length() % 32;

				if (dummyNeeded > 0) {
					for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
						pBin += "00";
					}
				}

				byte[] plain_bin = pBin.getBytes("UTF-8");
				byte[] kBin = kAlias.getBytes("UTF-8");
				byte[] ci_bin = com.Cvn.Encryptor.AES.drvRfc2898_encrypt(kBin, plain_bin, sessionId);
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Cvn-AES encrypted.");
				rspData.put("EncData", Encode.byteToHex(ci_bin));

			} catch (Exception e) {
				logger.error("[encrypt_AES] Cvn Exception::" + e.getMessage());
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", e.getMessage());
				rspData.put("EncData", "--");
			}
			break;

		default:
			rspData.put("ReturnCode", ReturnCode.ParameterError);
			rspData.put("ReturnMsg", "Invalid provider.");
			rspData.put("EncData", "--");
			break;
		}

		String rsp = gson.toJson(rspData).replace("\\u003d", "=");
		return rsp;

	}
	
	public String decrypt_AES_always_HSM(String enTxt, String kAlias, String sessionId) {


		logger.debug(
				"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][decrypt_AES_always_HSM] -> #cipherTxt:["
						+ enTxt + "] - #keyAlias:[" + kAlias + "] - #HSMProvider:[" + HSMProvider + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String plainTxt = null;

		switch (HSMProvider) {
		case "ECSP":
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			HashMap<String, Object> data = new HashMap<String, Object>();
			HashMap<String, Object> secLayer = new HashMap<String, Object>();
			HashMap<String, String> trdLayer = new HashMap<String, String>();

			data.put("msgNo", Cfg.getExternalCfgValue("SysCode") + "_" + String.valueOf(ts.getTime()) + "_"
					+ sessionId.substring(0, 4));
			data.put("txnTime", sdf.format(ts));
			data.put("txnCode", Cfg.getExternalCfgValue("TxnCode"));
			data.put("senderCode", Cfg.getExternalCfgValue("SenderCode"));
			data.put("receiverCode", Cfg.getExternalCfgValue("ReceiverCode"));
			data.put("operatorCode", Cfg.getExternalCfgValue("OperatorCode"));
			data.put("unitCode", Cfg.getExternalCfgValue("UnitCode"));
			data.put("authorizerCode", Cfg.getExternalCfgValue("AuthorizerCode"));

			trdLayer.put("data", enTxt);
			secLayer.put("keyIndex", kAlias);
			secLayer.put("cipherDatasets", trdLayer);
			data.put("requestBody", secLayer);

			logger.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
					+ "][decrypt_AES_always_HSM] - # Decrypting msg (AES with key index: " + kAlias + ")");

			try {
				String remoteRsp = new Send2Remote().post(Cfg.getExternalCfgValue("DecryptUrl"), gson.toJson(data));

				if (remoteRsp.equals("")) {
					logger.error("[" + sessionId
							+ "[decrypt_AES_always_HSM] - # Decryption (ECSP-AES) failed with empty result.");
					return null;
				}

				JSONObject json = new JSONObject(remoteRsp);

				if (!(Cfg.getExternalCfgValue("ReceiverCode") + "_0000").equals(json.getString("resultCode"))) {
					logger.info(
							"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][decrypt_AES_always_HSM] - # Decryption (ECSP-AES) failed with error: ["
									+ json.getString("resultCode") + "]");
					return null;
				}

				plainTxt = json.getJSONObject("resultBody").getJSONObject("secDatasets").getString("data");

				int trimDummy = plainTxt.indexOf("00");
				if (trimDummy > -1) {
					// found dummy, trim it
					logger.debug("[" + sessionId + "[decrypt_AES_always_HSM] Spotted dummy in text: " + plainTxt);

					plainTxt = plainTxt.substring(0, trimDummy);

					logger.debug("[" + sessionId + "[decrypt_AES_always_HSM] Trimmed text: " + plainTxt);
				}

				plainTxt = new String(Encode.hexToByte(plainTxt), "UTF-8");

			} catch (JSONException e) {
				logger.error("[" + sessionId + "[decrypt_AES_always_HSM] - # Unable to parse ECSP response:"
						+ e.getMessage());
				return null;
			} catch (IOException e) {
				logger.error(
						"[" + sessionId + "[decrypt_AES_always_HSM] - # Connecting to ECSP error:" + e.getMessage());
				return null;
			}

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "ECSP-AES decrypted.");
			rspData.put("Data", plainTxt);

			break;

//		case "SafeNet":
//			try {
//				int slotId = Integer.parseInt(Cfg.getExternalCfgValue("HSM_SLOT"));
//				String slotPwd = Cfg.getExternalCfgValue("HSM_PXD");
//
//				logger.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][decrypt_AES_always_HSM] - # Calling HSM for decryption");
//
//				com.HSM.SafeNet.AES myAES = com.HSM.SafeNet.AES.initSafeNetAES(slotId, slotPwd, sessionId);
//				String encTxt = myAES.encrypt_Hex(CvnSecrets.commonSec_ret, kAlias, sessionId);
//
//				if (encTxt.contains("ERROR:")) {
//					rspData.put("ReturnCode", ReturnCode.Fail);
//					rspData.put("ReturnMsg", encTxt);
//					rspData.put("EncData", "--");
//					break;
//				}
//
//				try {
//					plainTxt = new String(
//							AES.drvRfc2898_decrypt(encTxt.getBytes("UTF-8"), Encode.hexToByte(enTxt), sessionId),
//							"UTF-8");
//				} catch (UnsupportedEncodingException e) {
//				}
//
//				rspData.put("ReturnCode", ReturnCode.Success);
//				rspData.put("ReturnMsg", "SafeNet-AES decrypted.");
//				rspData.put("Data", plainTxt);
//			} catch (Exception e) {
//				rspData.put("ReturnCode", ReturnCode.Fail);
//				rspData.put("ReturnMsg", e.getMessage());
//				rspData.put("Data", "--");
//			}
//			break;

		case "Cvn":
			logger.debug("[" + sessionId + "][decrypt_AES_always_HSM] Using Cvn f0r decryption.");
			try {
				byte[] cipher_bin = Encode.hexToByte(enTxt);
				byte[] key_bin = kAlias.getBytes("UTF-8");
				byte[] plain_bin = com.Cvn.Encryptor.AES.drvRfc2898_decrypt(key_bin, cipher_bin, sessionId);
				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "Cvn-AES decrypted.");
				rspData.put("Data", new String(plain_bin, "UTF-8"));

			} catch (Exception e) {
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", e.getMessage());
				rspData.put("Data", "--");
			}
			break;

		default:
			rspData.put("ReturnCode", ReturnCode.ParameterError);
			rspData.put("ReturnMsg", "Invalid provider.");
			rspData.put("Data", "--");
			break;
		}

		String rsp = gson.toJson(rspData).replace("\\u003d", "=");
		return rsp;

	}
}

