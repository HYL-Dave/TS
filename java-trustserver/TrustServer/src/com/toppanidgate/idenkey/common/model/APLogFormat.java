package com.toppanidgate.idenkey.common.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class APLogFormat {

	private String beginStr;
	private HashMap<String, Object> secLayer = null;
	private boolean noThrowable = true;

	public APLogFormat() throws UnknownHostException {
		beginStr = "\"hostname\":\"" + InetAddress.getLocalHost().getHostName() + "\"";
		secLayer = new HashMap<String, Object>();
	}

	public void setTraceID(String id) {
		beginStr += ",\"trace_id\":\"" + id + "\"";
	}

	public void setClazz(String txt) {
		beginStr += ",\"clazz\":\"" + txt + "\"";
	}

	public void setThrowable(String txt) {
		if (noThrowable) {
			beginStr += ",\"throwable\":\"" + txt + "\"";
			noThrowable = false;
		}
	}

	public void setMessage(String rsp) {
		secLayer.put("message", rsp);
	}
	
	public void setMessageForMap(Map<String, Object> map) {
		secLayer.put("message", map);
	}

	public String getCompleteTxt() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		String respMsg = "";

		if (noThrowable) {
			respMsg = beginStr + ",\"throwable\":null" + ",\"ap_log_info\":" + gson.toJson(secLayer) + "}";
			secLayer.clear();
			return respMsg;
		}
		
		respMsg = beginStr + ",\"ap_log_info\":" + gson.toJson(secLayer) + "}";
		secLayer.clear();
		return respMsg;
	}

	public boolean hasException() {
		return !noThrowable;
	}
}
