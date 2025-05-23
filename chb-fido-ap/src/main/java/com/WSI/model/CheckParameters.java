package com.WSI.model;

import java.util.HashMap;
import java.util.Map;

import com.Common.model.ReturnCode;

public class CheckParameters {

	private static final String amount = "^[0-9]{1,10}$";
	private static final String method = "^[\\w]*$";
	private static final String channel = "^[\\w]{1,30}$";
	private static final String iso8691_Datetime = "^[0-9T\\-\\.:]{23}$";
	private static final String txnID = "^[0-9]{16,24}$";
	private static final String name = "^[\\w]+$";
	private static final String reqSys = "^[\\w]+$";
	private static final String returnMsg = "^[^<>()?;%&\\r\\n]*$";
	private static final String title = "^[\\w[^x00-xff]]{0,100}$";
	private static final String deviceOS = "^[A-Za-z0-9. ]{5,15}$";
	// IDgate ID, numeric, not null
	private static final String idgateID = "^[\\d]{10,38}$";
	private static final String phoneNo = "^[\\d]{10}$";

	private static final String activate = "^[NY]{1}$";
	private static final String channelName = "^[^<>&\\r\\n]*$";
	private static final String label = "^[\\w,-]{1,100}$";
	private static final String model = "^[\\w,-]{1,100}$";
	private static final String authType = "^[0123]{1,4}$";
	private static final String otp = "^[0-9]{6,8}$";
	private static final String oNO = "^[0-9]+$";
	private static final String txnData = "^[^<>&\\r\\n]*$";
	private static final String push = "^[01]{1}$";
	private static final String verifyType = "^[012]$|^-1$";
	private static final String jndi = "^[\\w:/]{3,50}$";
	private static final String callback = "^[^<>&\\r\\n]*$";;
	// Push token, nullable
	private static final String pushToken = "^[a-z0-9]{64,128}$|^[\\w-:]{152,256}$";

	private static final String idgateIDs = "^[0-9,{}\":]{10,50}$";

	public HashMap<String, String> checkMap(Map<String, String> map) {

		HashMap<String, String> hashMap = new HashMap<>();

		for (Map.Entry<String, String> entry : map.entrySet()) {

			String param = entry.getKey();
			String[] returnCode = null;

			if ("method".equals(param)) {
				returnCode = checkParameter(method, entry.getValue(), false);
			} else if ("channel".equals(param)) {
				returnCode = checkParameter(channel, entry.getValue(), false);
			} else if ("startDate".equals(param)) {
				returnCode = checkParameter(iso8691_Datetime, entry.getValue(), false);
			} else if ("endDate".equals(param)) {
				returnCode = checkParameter(iso8691_Datetime, entry.getValue(), false);
			} else if ("txnID".equals(param)) {
				returnCode = checkParameter(txnID, entry.getValue(), false);
			} else if ("name".equals(param)) {
				returnCode = checkParameter(name, entry.getValue(), false);
			} else if ("amount".equals(param)) {
				returnCode = checkParameter(amount, entry.getValue(), false);
			} else if ("reqSys".equals(param)) {
				returnCode = checkParameter(reqSys, entry.getValue(), false);
			} else if ("returnMsg".equals(param)) {
				returnCode = checkParameter(returnMsg, entry.getValue(), false);
			} else if ("title".equals(param)) {
				returnCode = checkParameter(title, entry.getValue(), false);
			} else if ("deviceOS".equals(param)) {
				returnCode = checkParameter(deviceOS, entry.getValue(), false);
			} else if ("idgateID".equals(param)) {
				returnCode = checkParameter(idgateID, entry.getValue(), false);
			} else if ("phoneNo".equals(param)) {
				returnCode = checkParameter(phoneNo, entry.getValue(), false);
			} else if ("activate".equals(param) || "blocking".equals(param)) {
				returnCode = checkParameter(activate, entry.getValue(), false);
			} else if ("channelName".equals(param)) {
				returnCode = checkParameter(channelName, entry.getValue(), false);
			} else if ("label".equals(param)) {
				returnCode = checkParameter(label, entry.getValue(), false);
			} else if ("model".equals(param)) {
				returnCode = checkParameter(model, entry.getValue(), false);
			} else if ("type".equals(param) || "authType".equals(param)) {
				returnCode = checkParameter(authType, entry.getValue(), false);
			} else if ("otp".equals(param)) {
				returnCode = checkParameter(otp, entry.getValue(), false);
			} else if ("oNO".equals(param)) {
				returnCode = checkParameter(oNO, entry.getValue(), false);
			} else if ("push".equals(param)) {
				returnCode = checkParameter(push, entry.getValue(), false);
			} else if ("txnData".equals(param)) {
				returnCode = checkParameter(txnData, entry.getValue(), false);
			} else if ("verifyType".equals(param)) {
				returnCode = checkParameter(verifyType, entry.getValue(), false);
			} else if ("jndi".equals(param)) {
				returnCode = checkParameter(jndi, entry.getValue(), false);
			} else if ("callback".equals(param)) {
				returnCode = checkParameter(callback, entry.getValue(), true);
			} else if ("idgateIDs".equals(param)) {
				returnCode = checkParameter(idgateIDs, entry.getValue(), true);
			} else if ("pushToken".equals(param)) {
				returnCode = checkParameter(pushToken, entry.getValue(), true);
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
