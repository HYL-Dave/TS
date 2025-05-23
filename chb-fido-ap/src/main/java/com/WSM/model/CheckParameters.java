package com.WSM.model;

import java.util.HashMap;
import java.util.Map;

import com.Common.model.ReturnCode;

public class CheckParameters {

	// Method name, mixed with alphabetic and dash, not null
	private static final String method = "^[_a-zA-Z]+$";
	// Channel name, alphabetic, not null
	private static final String channel = "^[\\w]{1,30}$";
	// IDgate ID, numeric, not null
	private static final String idgateID = "^[0-9,]{10,50}$";
	// Device OS name, alphabets + numerics + space + dot, not null
	private static final String deviceOS = "^[A-Za-z0-9. ]{5,20}$";
	// Device info, mixed with alphabetic and numerics, not null
	private static final String deviceInfo = "^[\\w- .\"\\{},:=]+$";
	// Device label
	private static final String deviceLabel = "^[\\w ()]{0,30}$";
	// Device model
	private static final String deviceModel = "^[\\w,+ ()]{0,30}$";
	// Hash string, not null
	private static final String hash = "^[a-z0-9]{40,64}$";
	// OTP, not null
	private static final String otp = "^[\\d]{6,8}$";
	// Push switch, not null
	private static final String push = "^[01]$";
	// Push token, nullable
	private static final String pushToken = "^[a-z0-9]{64,128}$|^[_a-zA-Z0-9-:]{152,256}$";
	// Txn id, not null
	private static final String txnID = "^[\\d]{16,20}|[\\d]{24}$";
	// Timestamp in millisecond
	private static final String timestamp = "^[\\d]{17}$";
	// APP version
	private static final String verNo = "^[0-9]{1}\\.[0-9]{1}\\.[0-9]{1}[()0-9]*$";
	// Reply msg, not null
	private static final String returnMsg = "^[^<>?;%&\\r\\n]*$";
	//
	private static final String option = "^[01]$";
	// prefer language
	private static final String lang = "^[a-zA-Z-]{1,10}$";
	// base64 encoded format
	private static final String base64 = "^[\\w-\"{},:=]+$";
	// encrypted reg info
	private static final String encRegInfo = "^[\\w{}\",:=-]+$";
	// txn title
	private static final String title = "^[\\p{sc=Han}| \\w\\,\\\"\\\\{\\}\\[\\]\\:\\-+=/]{1,300}$";
	//
	private static final String body = "^[\\p{sc=Han}| \\w\\,\\\"\\\\{\\}\\[\\]\\:\\-]{1,300}$";
	// txn auth type
	private static final String authType = "^[0123]$";

	private static final String authTypeRequest = "^[0123]{1,4}$";
	// key type
	private static final String keyType = "^[012]$";
	// bank txn data, no checking specified
	private static final String bankTxnData = "^[^<>&\\r\\n]*$";
	// txn verification type
	private static final String verifyType = "^[0-9]{1,2}$";

	private static final String encRegRes = "^[\\w-=]+$";

	private static final String transactionID = "^[a-zA-Z0-9-]{1,150}$";

	private static final String replace = "^[012]$";

	private static final String idgateIDs = "^[0-9,{}\":]{10,50}$";

	private static final String encVerifyTxnData = "^[\\w-\"{},:=]+$";

	private static final String encDeregData = "^[\\w-\"{},:=]+$";

	private static final String encChangeSettingData = "^[\\w-\"{},:=]+$";

	private static final String changeSettingSession = "^[^<>?;%&\\r\\n]*$";

	private static final String encGetTxnListData = "^[\\w-\"{},:=]+$";

	private static final String encTxnID = "^[\\w-\"{},:=]+$";

	private static final String bankTxnID = "^[a-zA-Z0-9-]{1,150}$";

	private static final String encCancelAuthData = "^[\\w-\"{},:=]+$";

	private static final String encSettingData = "^[\\w-\"{},:=]+$";
	
	private static final String days = "^[0-9]{1,2}$";
	private static final String number = "^[0-9]{1,3}$";

	private static final String clickAction = "^[\\w-]{1,100}$";
	private static final String encPlainData = "^[\\w=-]+$";
	private static final String plainData = "^[^<>&\\r\\n]*$";


	public HashMap<String, String> checkMap(Map<String, String> map) {

		HashMap<String, String> hashMap = new HashMap<>();

		for (Map.Entry<String, String> entry : map.entrySet()) {

			String param = entry.getKey();
			String[] returnCode = null;

			if ("method".equals(param)) {
				returnCode = checkParameter(method, entry.getValue(), false);
			} else if ("idgateID".equals(param)) {
				returnCode = checkParameter(idgateID, entry.getValue(), false);
			} else if ("channel".equals(param)) {
				returnCode = checkParameter(channel, entry.getValue(), false);
			} else if ("deviceOS".equals(param)) {
				returnCode = checkParameter(deviceOS, entry.getValue(), false);
			} else if ("deviceInfo".equals(param)) {
				returnCode = checkParameter(deviceInfo, entry.getValue(), false);
			} else if ("deviceLabel".equals(param)) {
				returnCode = checkParameter(deviceLabel, entry.getValue(), true);
			} else if ("deviceModel".equals(param)) {
				returnCode = checkParameter(deviceModel, entry.getValue(), true);
			} else if ("hash".equals(param)) {
				returnCode = checkParameter(hash, entry.getValue(), false);
			} else if ("option".equals(param)) {
				returnCode = checkParameter(option, entry.getValue(), false);
			} else if ("otp".equals(param)) {
				returnCode = checkParameter(otp, entry.getValue(), false);
			} else if ("authType".equals(param)) {
				returnCode = checkParameter(authType, entry.getValue(), false);
			} else if ("title".equals(param) || "txnTitle".equals(param)) {
				returnCode = checkParameter(title, entry.getValue(), false);
			} else if ("bankTxnData".equals(param) || "txnData".equals(param)) {
				returnCode = checkParameter(bankTxnData, entry.getValue(), false);
			} else if ("verifyType".equals(param)) {
				returnCode = checkParameter(verifyType, entry.getValue(), false);
			} else if ("push".equals(param)) {
				returnCode = checkParameter(push, entry.getValue(), false);
			} else if ("pushToken".equals(param)) {
				returnCode = checkParameter(pushToken, entry.getValue(), true);
			} else if ("body".equals(param)) {
				returnCode = checkParameter(body, entry.getValue(), true);
			} else if ("txnID".equals(param)) {
				returnCode = checkParameter(txnID, entry.getValue(), false);
			} else if ("timestamp".equals(param)) {
				returnCode = checkParameter(timestamp, entry.getValue(), false);
			} else if ("verNo".equals(param)) {
				returnCode = checkParameter(verNo, entry.getValue(), false);
			} else if ("lang".equals(param)) {
				returnCode = checkParameter(lang, entry.getValue(), false);
			} else if ("returnMsg".equals(param)) {
				returnCode = checkParameter(returnMsg, entry.getValue(), false);
			} else if ("enIdgateID".equals(param) || "data".equals(param)) {
				returnCode = checkParameter(base64, entry.getValue(), false);
			} else if ("encRegInfo".equals(param)) {
				returnCode = checkParameter(encRegInfo, entry.getValue(), false);
			} else if ("encABCount".equals(param)) {
				returnCode = checkParameter(base64, entry.getValue(), false);
			} else if ("authKey".equals(param)) {
				returnCode = checkParameter(base64, entry.getValue(), false);
			} else if ("authTypeRequest".equals(param)) {
				returnCode = checkParameter(authTypeRequest, entry.getValue(), false);
			} else if ("encRegRes".equals(param)) {
				returnCode = checkParameter(encRegRes, entry.getValue(), false);
			} else if ("transactionID".equals(param)) {
				returnCode = checkParameter(transactionID, entry.getValue(), false);
			} else if ("replace".equals(param)) {
				returnCode = checkParameter(replace, entry.getValue(), false);
			} else if ("idgateIDs".equals(param)) {
				returnCode = checkParameter(idgateIDs, entry.getValue(), true);
			} else if ("encVerifyTxnData".equals(param)) {
				returnCode = checkParameter(encVerifyTxnData, entry.getValue(), false);
			} else if ("encDeregData".equals(param)) {
				returnCode = checkParameter(encDeregData, entry.getValue(), false);
			} else if ("keyType".equals(param)) {
				returnCode = checkParameter(keyType, entry.getValue(), false);
			} else if ("encChangeSettingData".equals(param)) {
				returnCode = checkParameter(encChangeSettingData, entry.getValue(), false);
			} else if ("changeSettingSession".equals(param)) {
				returnCode = checkParameter(changeSettingSession, entry.getValue(), false);
			} else if ("encGetTxnListData".equals(param)) {
				returnCode = checkParameter(encGetTxnListData, entry.getValue(), false);
			} else if ("encTxnID".equals(param)) {
				returnCode = checkParameter(encTxnID, entry.getValue(), false);
			} else if ("encCancelAuthData".equals(param)) {
				returnCode = checkParameter(encCancelAuthData, entry.getValue(), false);
			} else if ("bankTxnID".equals(param)) {
				returnCode = checkParameter(bankTxnID, entry.getValue(), false);
			} else if ("encSettingData".equals(param)) {
				returnCode = checkParameter(encSettingData, entry.getValue(), false);
			} else if ("number".equals(param)) {
				returnCode = checkParameter(number, entry.getValue(), false);
			} else if ("days".equals(param)) {
				returnCode = checkParameter(days, entry.getValue(), false);
			} else if ("clickAction".equals(param)) {
				returnCode = checkParameter(clickAction, entry.getValue(), true);
			} else if ("encPlainData".equals(param)) {
				returnCode = checkParameter(encPlainData, entry.getValue(), false);
			} else if ("plainData".equals(param)) {
				returnCode = checkParameter(plainData, entry.getValue(), false);
			}

			if (returnCode != null && ReturnCode.Success.equals(returnCode[0])) {
				hashMap.put(param, returnCode[1]);
			} else {
				hashMap.clear();
				hashMap.put("Error", "Param: \"" + param + "\", Value: \"" + entry.getValue()
						+ "\", value didn't pass the WhiteList filter");
				break;
			}
		}

		return hashMap;
	}

	/* �ˬd�ŭ� */
	private Object[] translate(String value, boolean isNull) {

		Object[] obj = new Object[2];

		if (value == null || value.trim().length() == 0) {
			if (isNull) {
				obj[0] = true;
				obj[1] = "";
			} else {
				obj[0] = false;
			}
		} else {
			obj[0] = true;
			obj[1] = value.trim();
		}

		return obj;
	}

	private String[] checkParameter(String regular, String value, boolean isNull) {

		String[] str = new String[2];
		Object[] obj = translate(value, isNull);

		if ((Boolean) obj[0]) {
			if (isNull || value.matches(regular)) {
				str[0] = ReturnCode.Success;
				str[1] = (String) obj[1];
			} else {
				str[0] = ReturnCode.ParameterError;
			}
		} else {
			str[0] = ReturnCode.ParameterError;
		}

		return str;
	}
}
