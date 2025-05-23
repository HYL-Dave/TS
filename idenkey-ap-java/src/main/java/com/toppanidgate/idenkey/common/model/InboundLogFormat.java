package com.toppanidgate.idenkey.common.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public void setRequest(String req) throws JsonSyntaxException {
		HashMap<String, Object> reqData = new Gson().fromJson(req, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		
		if (reqData.get("txnData") != null) {
			reqData.put("txnData",
					new Gson().fromJson((String) reqData.get("txnData"), new TypeToken<HashMap<String, Object>>() {
					}.getType()));
		}
		if (reqData.get("deviceInfo") != null) {
			reqData.put("deviceInfo", reqData.get("deviceInfo"));
//			reqData.put("deviceInfo",
//					new Gson().fromJson((String) reqData.get("deviceInfo"), new TypeToken<HashMap<String, Object>>() {
//					}.getType()));
		}
		
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("path", inClass);
		tmp.put("source", "SDK");
		tmp.put("body", reqData);

		secLayer.put("request", tmp);
	}

	public void setResponseTxt(String rsp) throws JsonSyntaxException {
		HashMap<String, Object> rspData = new Gson().fromJson(rsp, new TypeToken<HashMap<String, Object>>() {
		}.getType());

		if (rspData.get("regReq") != null) {
			rspData.put("regReq",
					new Gson().fromJson((String) rspData.get("regReq"), new TypeToken<HashMap<String, Object>>() {
					}.getType()));
//			rspData.put("regReq",
//					new Gson().fromJson((String) rspData.get("regReq"), new TypeToken<List<Map<String, Object>>>() {
//					}.getType()));
		}
		
		if (rspData.get("authResult") != null) {
			HashMap<String, Object> authResult = new Gson().fromJson((String) rspData.get("authResult"), new TypeToken<HashMap<String, Object>>() {
			}.getType());
			authResult.put("fidoRes",
					new Gson().fromJson((String) authResult.get("fidoRes"), new TypeToken<List<Map<String, Object>>>() {
					}.getType()));
			rspData.put("authResult", authResult);
		}
		
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("message", rspData);
		tmp.put("status_code", rspData.get("returnCode"));
		tmp.put("result_code", rspData.get("returnCode"));

		secLayer.put("response", tmp);
	}

	public String getCompleteTxt() {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//		Gson gson = new GsonBuilder().serializeNulls().create();
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
