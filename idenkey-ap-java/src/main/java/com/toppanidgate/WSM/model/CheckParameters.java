package com.toppanidgate.WSM.model;

import java.util.HashMap;
import java.util.Map;

import com.toppanidgate.idenkey.common.model.ReturnCode;

public class CheckParameters {

	// TODO 外部設定 REGX ??
	// Method name, mixed with alphabetic and dash, not null
	private static final String method = "^[_a-zA-Z]+$";
	// base64 encoded format
	private static final String base64 = "^[a-zA-Z0-9-_\"{},:=]+$";
	// Channel name, alphabetic, not null
	private static final String channel = "^[a-zA-Z0-9_]{2,30}$";
	// userName, alphabetic, not null
	private static final String userName = "^[a-zA-Z0-9_]{6,100}$";
	// transactionID, numeric, not null
	private static final String transactionID = "^[a-zA-Z0-9-]{1,64}$";
	// bankTxnID, sha, not null
	// 111.12.6 玉山 SIT 問題  "BankTxnID": "Hp7QkfZ4Y2YK20BPwVf4igZ9HXWsM2kOidQkzCIKAc0=",
	// Postman 範例 "bankTxnID": "1CD97D60-CC9C-4774-A1C4-D8D6CA3C24A8",
	private static final String bankTxnID = "^[a-zA-Z0-9-]{10,150}$"; // 不改(不加=
	// Reply msg, not null
	private static final String returnMsg = "^[^<>?;%&\\r\\n]*$";
	// title String (100) 要顯示的交易驗證的標題，例如：”快速登入驗證” 應用時機：取得交易列表時，每筆驗證之標題 Client端產生 
	private static final String title = "^[_0-9a-zA-Z[^x00-xff]]{0,100}$";
	// txnTitle title
	// 300個字除了不能用分隔符號 | 其他客戶可以自己決定格式內容~~
//	private static final String txnTitle = "^[^|]{1,300}$"; // 這規則會被XSS判fail喔
	// JSON 或 Base64
	private static final String txnTitle = "^[_0-9a-zA-Z\\[\\]:\\\\[^|x00-xff]]{1,300}$|^[a-zA-Z0-9-_\"{},:=+]{1,300}$";
	// txn auth type
	private static final String authType = "^[0123]{1,4}$";
	private static final String txnID = "^[0-9]{24}$";
	private static final String otp = "^[0-9]{6,}$";
	private static final String days = "^[0-9]{1,2}$";
	private static final String number = "^[0-9]{1,3}$";
	// txn data
	private static final String txnData = "^[^<>&\\r\\n]*$";
	// bankTxnData data
	private static final String bankTxnData = "^[^<>&\\r\\n]{1,1000}$";	// ^[^<>&\\r\\n]*$
	// txn verification type
	private static final String keyType = "^[0,1,2,3]$";
	// IDgate ID, numeric, not null
	private static final String idgateID = "^[\\d]{10,38}$";
	private static final String deviceOS = "^[\\w]{2,20}$";
	private static final String encPlainData = "^[\\w\\-+\\/]+$";

	public HashMap<String, String> checkMap(Map<String, String> map) {

		HashMap<String, String> hashMap = new HashMap<>();

		for (Map.Entry<String, String> entry : map.entrySet()) {

			String param = entry.getKey();
			String[] returnCode = null;

			if ("method".equals(param)) {
				returnCode = checkParameter(method, entry.getValue(), false);
			} else if ("idgateID".equals(param)) {
				returnCode = checkParameter(idgateID, entry.getValue(), false);
			} else if ("channel".equals(param) || "keyAlias".equals(param)) {
				returnCode = checkParameter(channel, entry.getValue(), false);
			} else if ("otp".equals(param)) {
				returnCode = checkParameter(otp, entry.getValue(), false);
			} else if ("keyType".equals(param)) {
				returnCode = checkParameter(keyType, entry.getValue(), false);
			} else if ("authType".equals(param)) {
				returnCode = checkParameter(authType, entry.getValue(), false);
			} else if ("txnID".equals(param)) {
				returnCode = checkParameter(txnID, entry.getValue(), false);
			} else if ("encTxnID".equals(param)) {
				returnCode = new String[] { ReturnCode.Success, entry.getValue() };
			} else if ("title".equals(param)) {
				returnCode = checkParameter(title, entry.getValue(), false);
			} else if ("txnTitle".equals(param)) {
				returnCode = checkParameter(txnTitle, entry.getValue(), false);
			} else if ("bankTxnData".equals(param)) {
				returnCode = checkParameter(bankTxnData, entry.getValue(), false);
			} else if ("data".equals(param)) {
				returnCode = checkParameter(base64, entry.getValue(), false);
			} else if ("transactionID".equals(param)) {
				returnCode = checkParameter(transactionID, entry.getValue(), false);
			} else if ("deviceInfo".equals(param)) {
				returnCode = new String[] { ReturnCode.Success, entry.getValue() };
			} else if ("txnData".equals(param)) {
				returnCode = checkParameter(txnData, entry.getValue(), false);
			} else if ("random".equals(param)) {
				returnCode = new String[] { ReturnCode.Success, entry.getValue() };
			} else if ("returnMsg".equals(param)) {
				returnCode = checkParameter(returnMsg, entry.getValue(), false);
			} else if ("userName".equals(param)) {
				returnCode = checkParameter(userName, entry.getValue(), false);
			} else if ("number".equals(param)) {
				returnCode = checkParameter(number, entry.getValue(), false);
			} else if ("days".equals(param)) {
				returnCode = checkParameter(days, entry.getValue(), false);
			} else if("session".equals(param)) {
				returnCode = checkParameter(returnMsg, entry.getValue(), false);
			} else if ("bankTxnID".equals(param)) {
				returnCode = checkParameter(bankTxnID, entry.getValue(), false);
			} else if ("deviceOS".equals(param)) {
				returnCode = checkParameter(deviceOS, entry.getValue(), false);
			} else if ("serverData".equals(param)) {
				returnCode = new String[] { ReturnCode.Success, entry.getValue() };
			} else if ("encPlainData".equals(param)) {
				returnCode = checkParameter(encPlainData, entry.getValue(), false);
			} else if ("plainData".equals(param)) {
				returnCode = checkParameter(txnData, entry.getValue(), false);
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
