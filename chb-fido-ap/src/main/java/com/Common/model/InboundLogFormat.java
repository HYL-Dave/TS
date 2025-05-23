package com.Common.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class InboundLogFormat {

	private String beginStr;
	private HashMap<String, Object> secLayer = null;
	private boolean noThrowable = true;
	private String inClass = null;

	public InboundLogFormat() throws UnknownHostException {
		beginStr = "\"hostname\":\"" + InetAddress.getLocalHost().getHostName() + "\"";
		secLayer = new HashMap<String, Object>();
	}

	public void setTraceID(String id) {
		beginStr += ",\"trace_id\":\"" + id + "\"";
	}

	public void setClazz(String txt) {
		beginStr += ",\"clazz\":\"" + txt + "\"";
		inClass = txt;
	}

	public void setThrowable(String txt) {
		if (noThrowable) {
			beginStr += ",\"throwable\":\"" + txt + "\"";
			noThrowable = false;
		}
	}

	public void setInServlet(String name) {
		inClass = name;
	}

	public void setExecuteTime(long time) {
		secLayer.put("execute_time", time);
	}

	public void setRequest(String req) {
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("path", inClass);
		tmp.put("source", "SDK");
		tmp.put("body", req);

		secLayer.put("request", tmp);
	}

	public void setResponseTxt(String rsp) throws JsonSyntaxException {
		HashMap<String, Object> rspData = new Gson().fromJson(rsp, new TypeToken<HashMap<String, Object>>() {
		}.getType());

		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("message", rsp);
		tmp.put("status_code", "200");
		tmp.put("result_code", rspData.get("returnCode"));

		secLayer.put("response", tmp);
	}

	public String getCompleteTxt() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		String respMsg = "";

		if (noThrowable) {
			respMsg = beginStr + ",\"throwable\":null" + ",\"inbound_log_info\":" + gson.toJson(secLayer) + "}";
			secLayer.clear();
			return respMsg;
		}

		respMsg = beginStr + ",\"inbound_log_info\":" + gson.toJson(secLayer) + "}";
		secLayer.clear();
		return respMsg;
	}

	public boolean hasException() {
		return !noThrowable;
	}
}
