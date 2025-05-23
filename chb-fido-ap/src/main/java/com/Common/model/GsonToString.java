package com.Common.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.Channel.model.ChannelVO;
import com.WSI.model.Gson.Gson4Create_VerifyTxn;
import com.WSI.model.Gson.Gson4Get_Challenge;
import com.WSI.model.Gson.Gson4Get_ChannelList;
import com.WSI.model.Gson.Gson4Get_DeviceStatus;
import com.WSI.model.Gson.Gson4Get_NewsID;
import com.WSI.model.Gson.Gson4Get_TxnStatus;
import com.WSI.model.Gson.Gson4Send_SMS;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.WSM.model.Gson.Gson4Change_Setting;
import com.WSM.model.Gson.Gson4Check_Setting;
import com.WSM.model.Gson.Gson4Common;
import com.WSM.model.Gson.Gson4Common_w_Data;
import com.WSM.model.Gson.Gson4Common_w_Failcount;
import com.WSM.model.Gson.Gson4Get_Fails;
import com.WSM.model.Gson.Gson4Get_Info;
import com.WSM.model.Gson.Gson4Get_RSAPublicKey;
import com.WSM.model.Gson.Gson4Get_Sync_Data;
import com.WSM.model.Gson.Gson4Get_Txn_List;
import com.WSM.model.Gson.Gson4Signup_Device;
import com.WSM.model.Gson.Gson4SvfCreate_VerifyTxn;
import com.WSM.model.Gson.Gson4Sync_Time;
import com.WSM.model.Gson.Gson4Verify_Device;
import com.google.gson.Gson;

public class GsonToString {

	private ResourceBundle rb;
	private static final Gson gson = new Gson();

	public GsonToString(String message, Locale locale) {
		rb = ResourceBundle.getBundle(message, locale);
	}

	public ResourceBundle getResourceBundle() {
		return rb;
	}

	public GsonToString(String path, String message, Locale locale) throws MalformedURLException {
		// load message files from specified path
		File file = new File(path);
		URL[] urls = { file.toURI().toURL() };
		ClassLoader loader = new URLClassLoader(urls);
		rb = ResourceBundle.getBundle(message, locale, loader);
	}

	public String common(String returnCode) {
		Gson4Common gson4 = new Gson4Common();

		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);

	}

	public String common(String returnCode, String returnMsg) {
		Gson4Common gson4 = new Gson4Common();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(returnMsg);

		return gson.toJson(gson4);
	}

	public String common_w_maintenance(String returnCode, String maintenance) {
		Gson4Common gson4 = new Gson4Common();

		gson4.setMaintenance(maintenance);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}
	
	public String common_w_Failcount(String returnCode, String failCount) {
		Gson4Common_w_Failcount gson4 = new Gson4Common_w_Failcount();

		gson4.setFailCount(failCount);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String signup_Device(String returnCode, String data) {
		Gson4Signup_Device gson4 = new Gson4Signup_Device();

		gson4.setEncPersoData(data);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
		}

		return gson.toJson(gson4);
	}

	public String check_Setting(String returnCode, String encData) {
		Gson4Check_Setting gson4 = new Gson4Check_Setting();

		gson4.setEncCheckingData(encData);
		gson4.setReturnCode(returnCode);
		try {
			gson4.setReturnMsg(rb.getString(returnCode));
		} catch (MissingResourceException e) {
			Log4j.log.info("Unknown error code: " + returnCode);
			gson4.setReturnMsg(rb.getString("0009"));
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

	public String iBank_common(String returnCode, String returnMsg) {
		HashMap<String, String> map = new HashMap<String, String>();

		map.put("returnCode", /* "B00" + */returnCode);
		map.put("returnMsg", returnMsg);

		return gson.toJson(map);
	}

	public String get_Txn_List(String returnCode, String listData) {
		Gson4Get_Txn_List gson4 = new Gson4Get_Txn_List();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setTxnList(listData);

		return gson.toJson(gson4);
	}

	public String get_Sync_Data(String returnCode, String data) {
		Gson4Get_Sync_Data gson4 = new Gson4Get_Sync_Data();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setEncSyncData(data);

		return gson.toJson(gson4);
	}

	public String sync_Time(String returnCode) {

		Gson4Sync_Time gson4 = new Gson4Sync_Time();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setServerTime(String.valueOf(System.currentTimeMillis()));

		return gson.toJson(gson4);
	}

	public String get_Info(String returnCode, List<HashMap<String, Object>> news, ArrayList<ArrayList<String>> bankList,
			String maintenance) {

		Gson4Get_Info gson4 = new Gson4Get_Info();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setNews(news);
		gson4.setBankList(bankList);
		gson4.setMaintenance(maintenance);

		return gson.toJson(gson4);
	}

	public String get_TxnStatus(String returnCode, String txnID, String txnStatus, String type) {

		Gson4Get_TxnStatus gson4 = new Gson4Get_TxnStatus();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setTxnStatus(txnStatus);
		gson4.setTxnID(txnID);
		gson4.setType(type);

		return gson.toJson(gson4);
	}

	public String get_DeviceStatus(String returnCode, String status, HashMap<String, String> failCount,
			HashMap<String, String> type, String label, String model, String os, String osVer, String verifyType,
			String createTime, String lastModifyTime) {

		Gson4Get_DeviceStatus gson4 = new Gson4Get_DeviceStatus();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setMemberStatus(status);
		gson4.setFailCount(failCount);
		gson4.setType(type);
		gson4.setDeviceLabel(label);
		gson4.setDeviceModel(model);
		gson4.setDeviceOS(os);
		gson4.setDeviceOSVer(osVer);
		gson4.setVerifyType(verifyType);
		gson4.setCreateTime(createTime);
		gson4.setLastModifyTime(lastModifyTime);

		return gson.toJson(gson4);

	}

	public String get_Challenge(String returnCode, String challenge) {

		Gson4Get_Challenge gson4 = new Gson4Get_Challenge();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setChallenge(challenge);

		return gson.toJson(gson4);

	}

	public String common_w_Data(String returnCode, String data) {

		Gson4Common_w_Data gson4 = new Gson4Common_w_Data();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setData(data);

		return gson.toJson(gson4);

	}

	public String common_w_Data(String returnCode, String returnMsg, String data) {

		Gson4Common_w_Data gson4 = new Gson4Common_w_Data();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(returnMsg);
		gson4.setData(data);

		return gson.toJson(gson4);

	}

	public String verify_Device(String returnCode, String id) {

		Gson4Verify_Device gson4 = new Gson4Verify_Device();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setTxnID(id);

		return gson.toJson(gson4);

	}

	public String create_VerifyTxn(String returnCode, String txnID, String enTxnID, String txnStatus) {

		Gson4Create_VerifyTxn gson4 = new Gson4Create_VerifyTxn();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setTxnStatus(txnStatus);
		gson4.setEnTxnID(txnID);
		gson4.setTxnID(enTxnID);

		return gson.toJson(gson4);
	}

	public String svfCreate_VerifyTxn(String returnCode, String txnID, String data, String txnStatus) {

		Gson4SvfCreate_VerifyTxn gson4 = new Gson4SvfCreate_VerifyTxn();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setTxnStatus(txnStatus);
		gson4.setTxnID(txnID);
		gson4.setEncTxnData(data);

		return gson.toJson(gson4);
	}

	public String get_Pubkey(String returnCode, String appKey) {
		Gson4Get_RSAPublicKey gson4 = new Gson4Get_RSAPublicKey();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setApp_Key(appKey);

		return gson.toJson(gson4);
	}

	public String get_SMS(String returnCode, String otp, String id) {
		Gson4Send_SMS gson4 = new Gson4Send_SMS();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setOtp(otp);
		gson4.setONO(id);

		return gson.toJson(gson4);
	}

	public String get_NewsID(String returnCode, String id) {
		Gson4Get_NewsID gson4 = new Gson4Get_NewsID();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setNewsID(id);

		return gson.toJson(gson4);
	}

	public String get_FailCount(String returnCode, String count, String data) {
		Gson4Get_Fails gson4 = new Gson4Get_Fails();

		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));
		gson4.setFailCount(count);
		gson4.setData(data);

		return gson.toJson(gson4);
	}

	public String get_ChannelList(String returnCode, List<ChannelVO> list) {
		Gson4Get_ChannelList gson4 = new Gson4Get_ChannelList();

		gson4.setChannel(list);
		gson4.setReturnCode(returnCode);
		gson4.setReturnMsg(rb.getString(returnCode));

		return gson.toJson(gson4);
	}
}