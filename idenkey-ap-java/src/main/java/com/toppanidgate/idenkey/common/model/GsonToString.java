package com.toppanidgate.idenkey.common.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.toppanidgate.WSM.model.gson.*;
import com.toppanidgate.fidouaf.common.model.Log4j;

public class GsonToString {

	private ResourceBundle rb;
	private static final Gson gson = new Gson();

	public GsonToString(final String message, final Locale locale) {
		rb = ResourceBundle.getBundle(message, locale);
	}

	public ResourceBundle getResourceBundle() {
		return rb;
	}

	public GsonToString(final String path, final String message, final Locale locale) throws MalformedURLException {
		// load message files from specified path
		File file = new File(path);
		URL[] urls = { file.toURI().toURL() };
		ClassLoader loader = new URLClassLoader(urls);
		rb = ResourceBundle.getBundle(message, locale, loader);
	}

	public String common(final String returnCode) {
		Gson4Common gson4 = new Gson4Common();

		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);

	}

	//	public String signup_Device(String returnCode, String data) {
	//		Gson4svfSignup_Device gson4 = new Gson4svfSignup_Device();
	//
	//		gson4.setEncPersoData(data);
	//		gson4.setReturnCode(returnCode);
	//		try {
	//			gson4.setReturnMsg(rb.getString(returnCode));
	//		} catch (MissingResourceException e) {
	//			gson4.setReturnMsg(rb.getString("0009"));
	//		}
	//
	//		return gson.toJson(gson4);
	//	}

	public String signup_Device(String returnCode, String data, String idgateID, String pushID) {
		Gson4svfSignup_Device gson4 = new Gson4svfSignup_Device();

		gson4.setEncRegResult(data);
		gson4.setIdgateID(idgateID);
		gson4.setPushID(pushID);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String create_Txn(final String returnCode, final String txnId, final String encTxnData) {
		Gson4SvfCreate_Txn gson4 = new Gson4SvfCreate_Txn();

		gson4.setTxnID(txnId);
		gson4.setEncTxnData(encTxnData);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_RegRequest(final String returnCode, final String uafReq, final String rsaSecret) {
		Gson4SvfGet_RegRequest gson4 = new Gson4SvfGet_RegRequest();

		gson4.setRegReq(uafReq);
		gson4.setReturnCode(returnCode);
		gson4.setServerPubKey(rsaSecret);
		//		gson4.setServerData(serverData);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
//			if (description != null) {
//				gson4.setReturnMsg(description);
//			}
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_TxnData(final String returnCode, final String pubKey, final String encTxnData, final String encAuthReq, String txnID, String authStatus, String title) {
		Gson4SvfGet_TxnData gson4 = new Gson4SvfGet_TxnData();

		gson4.setReturnCode(returnCode);
		gson4.setServerPubKey(pubKey);
		gson4.setEncTxnData(encTxnData);
		gson4.setEncAuthReq(encAuthReq);
		gson4.setTxnID(txnID);
		gson4.setAuthStatus(authStatus);
		gson4.setTitle(title);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_AuthStatus(final String returnCode, final String authStatus) {
		Gson4SvfGet_AuthStatus gson4 = new Gson4SvfGet_AuthStatus();

		gson4.setReturnCode(returnCode);
		gson4.setAuthStatus(authStatus);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_AuthRequest(final String returnCode, final String encAuthReq, final String rsaSecret,
			final String txnID, String encTxnData, String encTxnID) {
		Gson4SvfGet_AuthRequest gson4 = new Gson4SvfGet_AuthRequest();

		gson4.setReturnCode(returnCode);
		gson4.setEncAuthReq(encAuthReq);
		gson4.setEncTxnData(encTxnData);
		gson4.setServerPubKey(rsaSecret);
		gson4.setTxnID(txnID);
		gson4.setEncTxnID(encTxnID);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String send_AuthResponse(final String returnCode, final String authResult, final String description,
			final String txnID, final String failCount) {
		Gson4Send_AuthResponse gson4 = new Gson4Send_AuthResponse();

		gson4.setTxnID(txnID);
		gson4.setFailCount(failCount);
		gson4.setReturnCode(returnCode);
		gson4.setAuthResult(authResult);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
			if (description != null) {
				gson4.setReturnMsg(description);
			}
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_Token(final String returnCode, final String encToken) {
		Gson4Get_Token gson4 = new Gson4Get_Token();

		gson4.setReturnCode(returnCode);
		gson4.setEncToken(encToken);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_OfflineOTPRequest(final String returnCode, final String qrCode, final String txnID) {
		Gson4svfOfflineOTPRequest gson4 = new Gson4svfOfflineOTPRequest();

		gson4.setReturnCode(returnCode);
		gson4.setQrCode(qrCode);
		Log4j.log.debug("*** [get_OfflineOTPRequest] qrCode: {}", qrCode);	
		gson4.setTxnID(txnID);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}
		return gson.toJson(gson4);
	}


	public String get_OfflineOTPResponse(final String returnCode, final String failCount){
		Gson4OfflineOTPResponse gson4 = new Gson4OfflineOTPResponse();

		gson4.setReturnCode(returnCode);
		//	        gson4.setFailCount(failCount);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_DeregResponse(final String returnCode, String memberStatus) {
		Gson4svfGet_DeregResponse gson4 = new Gson4svfGet_DeregResponse();

		gson4.setReturnCode(returnCode);
		gson4.setMemberStatus(memberStatus);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String get_DeregRequest(final String returnCode, final String uafReq, String encIdgateID) {
		Gson4svfGet_DeregRequest gson4 = new Gson4svfGet_DeregRequest();

		//		gson4.setRegReq(uafReq);
		gson4.setReturnCode(returnCode);
		gson4.setEncIdgateID(encIdgateID);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String send_RegResponse(final String returnCode, final String encRegResult, final String description, String idgateID) {
		Gson4SvfSendRegResponse gson4 = new Gson4SvfSendRegResponse();

		gson4.setEncRegResult(encRegResult);
		gson4.setReturnCode(returnCode);
		gson4.setIdgateID(idgateID);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
			//			if (description != null) {
			//				gson4.setReturnMsg(description);
			//			}
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString(ReturnCode.InternalError));
		}

		return gson.toJson(gson4);
	}

	public String change_Setting(String returnCode, String encData) {
		Gson4Change_Setting gson4 = new Gson4Change_Setting();

		gson4.setEncChangedData(encData);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String get_FailCount(String returnCode, String count, String data) {
		Gson4Get_Fails gson4 = new Gson4Get_Fails();

		gson4.setFailCount(count);
		gson4.setData(data);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String check_Setting(String returnCode, String encData) {
		Gson4Check_Setting gson4 = new Gson4Check_Setting();

		gson4.setEncCheckingData(encData);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String get_Pubkey(String returnCode, String appKey, String serverECCPubKey, String decryptedData, String encryptedData) {
		Gson4Get_RSAPublicKey gson4 = new Gson4Get_RSAPublicKey();

		gson4.setReturnCode(returnCode);
		gson4.setServerPubKey(appKey);
		gson4.setServerECCPubKey(serverECCPubKey);
		gson4.setDecryptedData(decryptedData);
		gson4.setEncryptedData(encryptedData);
		gson4.setServerTime(String.valueOf(System.currentTimeMillis()));
		//		gson4.setAppKey(appKey);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String decrypt_ECDH(String returnCode, String plainData){
		Gson4SvfDecrypt_ECDH gson4 = new Gson4SvfDecrypt_ECDH();

		gson4.setReturnCode(returnCode);
		gson4.setPlainData(plainData);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String encrypt_ECDH(String returnCode, String encPlainData){
		Gson4SvfEncrypt_ECDH gson4 = new Gson4SvfEncrypt_ECDH();

		gson4.setReturnCode(returnCode);
		gson4.setEncPlainData(encPlainData);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String sync_Time(String returnCode) {
		Gson4Sync_Time gson4 = new Gson4Sync_Time();

		gson4.setReturnCode(returnCode);
		gson4.setServerTime(String.valueOf(System.currentTimeMillis()));
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String get_Txn_List(String returnCode, String listData, String serverPubKey) {
		Gson4Get_Txn_List gson4 = new Gson4Get_Txn_List();

		gson4.setReturnCode(returnCode);
		gson4.setServerPubKey(serverPubKey);
		gson4.setTxnList(listData);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String get_DeviceStatus(String returnCode, String status, HashMap<String, String> failCount,
			HashMap<String, String> type, String label, String model, String os, String deviceOSVer, String creatTime, String lastModifyTime) {
		Gson4Get_DeviceStatus gson4 = new Gson4Get_DeviceStatus();

		gson4.setReturnCode(returnCode);
		gson4.setMemberStatus(status);
		gson4.setFailCount(failCount);
		gson4.setType(type);
		gson4.setDeviceLabel(label);
		gson4.setDeviceModel(model);
		gson4.setDeviceOS(os);
		gson4.setDeviceOSVer(deviceOSVer);
		gson4.setCreatTime(creatTime);
		gson4.setLastModifyTime(lastModifyTime);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		} catch (Exception e) {
			Log4j.log.error("Unknown error code: {}, Exception:{}", returnCode, e.getMessage());
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);

	}
}