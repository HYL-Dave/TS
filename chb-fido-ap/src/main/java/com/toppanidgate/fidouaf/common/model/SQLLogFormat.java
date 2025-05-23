package com.toppanidgate.fidouaf.common.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.model.Authenticator;

public class SQLLogFormat {

	private String beginStr;
	private HashMap<String, Object> secLayer = null;
	private boolean noThrowable = true;

	public SQLLogFormat()  {
		try {
			beginStr = "\"hostname\":\"" + InetAddress.getLocalHost().getHostName() + "\"";
		} catch (UnknownHostException e) {
			Log4j.log.warn("UnknownHostException:" + e.getMessage());
		}
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

	public void setMessageForList(List<Authenticator> authenticators) {
		List<Map<String, Object>> listMap = new ArrayList<>();
		Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
		for(Authenticator auth: authenticators) {
			Map<String, Object> map = new HashMap<>();
			map = gson.fromJson(gson.toJson(auth), new TypeToken<HashMap<String, Object>>() {
			}.getType());
			map.put("value", gson.fromJson((String) map.get("value"), new TypeToken<HashMap<String, Object>>() {
			}.getType()));
			listMap.add(map);
		}
		secLayer.put("message", listMap);
	}
	
	public void setMessage(Object obj) {
		secLayer.put("message", obj);
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
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//		Gson gson = new GsonBuilder().serializeNulls().create();
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
