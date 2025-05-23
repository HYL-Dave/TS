package com.Common.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SQLLogFormat {

	private String beginStr;
	private HashMap<String, Object> secLayer = null;
	private boolean noThrowable = true;

	public SQLLogFormat() throws UnknownHostException {
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

	public void setResult_StatusMessage(String stat) {
		secLayer.put("result_status", stat);
	}

	public void setEexecute_Ttime(long time) {
		secLayer.put("execute_time", time);
	}

	public void setSql_Statement(String sql) {
		secLayer.put("sql_statement", sql);
	}

	public void setResult_Count(long count) {
		secLayer.put("result_count", count);
	}

	public String getCompleteTxt() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		String respMsg = "";

		if (noThrowable) {
			respMsg = beginStr + ",\"throwable\":null" + ",\"sql_log_info\":" + gson.toJson(secLayer) + "}";
			secLayer.clear();
			return respMsg;
		}

		respMsg = beginStr + ",\"sql_log_info\":" + gson.toJson(secLayer) + "}";
		secLayer.clear();
		return respMsg;
	}

	public boolean hasException() {
		return !noThrowable;
	}
}
