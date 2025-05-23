package com.toppanidgate.fidouaf.common.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.toppanidgate.fido.uaf.msg.AuthenticationResponse;
import com.toppanidgate.fido.uaf.msg.RegistrationResponse;
import com.toppanidgate.fido.uaf.storage.AuthenticatorRecord;
import com.toppanidgate.fido.uaf.storage.RegistrationRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

	public void setRequestForGet(String req) throws JsonSyntaxException {
		HashMap<String, Object> reqData = new Gson().fromJson(req, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		reqData.put("context",
				new Gson().fromJson((String) reqData.get("context"), new TypeToken<HashMap<String, Object>>() {
				}.getType()));

		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("path", inClass);
		tmp.put("source", "SDK");
		tmp.put("body", reqData);

		secLayer.put("request", tmp);
	}

	/**
	 * @param payload
	 */
	public void setRequestForGetAuth(String req) {
		HashMap<String, Object> reqData = new Gson().fromJson(req, new TypeToken<HashMap<String, Object>>() {
		}.getType());

		HashMap<String, Object> context = new Gson().fromJson((String) reqData.get("context"),
				new TypeToken<HashMap<String, Object>>() {
				}.getType());

		context.put("transaction",
				new Gson().fromJson((String) context.get("transaction"), new TypeToken<HashMap<String, Object>>() {
				}.getType()));

		reqData.put("context", context);

		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("path", inClass);
		tmp.put("source", "SDK");
		tmp.put("body", reqData);

		secLayer.put("request", tmp);
	}

	public void setRequestForResponseOpReg(String req) throws JsonSyntaxException {
		HashMap<String, Object> reqData = new Gson().fromJson(req, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		reqData.put("context",
				new Gson().fromJson((String) reqData.get("context"), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
		reqData.put("uafResponse",
				new Gson().fromJson((String) reqData.get("uafResponse"), RegistrationResponse[].class));

		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("path", inClass);
		tmp.put("source", "SDK");
		tmp.put("body", reqData);

		secLayer.put("request", tmp);
	}

	public void setRequestForResponseOpAuth(String req) throws JsonSyntaxException {
		HashMap<String, Object> reqData = new Gson().fromJson(req, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		reqData.put("context",
				new Gson().fromJson((String) reqData.get("context"), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
		reqData.put("uafResponse",
				new Gson().fromJson((String) reqData.get("uafResponse"), AuthenticationResponse[].class));

		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("path", inClass);
		tmp.put("source", "SDK");
		tmp.put("body", reqData);

		secLayer.put("request", tmp);
	}

	public void setResponseTxt(String rsp) throws JsonSyntaxException, JsonMappingException, JsonProcessingException {
//		HashMap<String, Object> rspData = new Gson().fromJson(rsp, new TypeToken<HashMap<String, Object>>() {
//		}.getType());
		
//		System.out.println("*** rsp:" + rsp);
		HashMap<String, Object> rspData = new ObjectMapper().readValue(rsp,
				new TypeReference<HashMap<String, Object>>() {
				});

		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("message", rspData);

		tmp.put("status_code", rspData.get("statusCode"));
		tmp.put("result_code", rspData.get("statusCode"));

		secLayer.put("response", tmp);
	}

	public void setResponseTxtForStep2(String rsp) throws JsonMappingException, JsonProcessingException {
//		HashMap<String, Object> rspData = new Gson().fromJson(rsp, new TypeToken<HashMap<String, Object>>() {
//		}.getType());
		HashMap<String, Object> rspData = new ObjectMapper().readValue(rsp,
				new TypeReference<HashMap<String, Object>>() {
		});
		
		rspData.put("newUAFRequest",
				new Gson().fromJson((String) rspData.get("newUAFRequest"), RegistrationRecord[].class));
		
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("message", rspData);
		
		tmp.put("status_code", rspData.get("statusCode"));
		tmp.put("result_code", rspData.get("statusCode"));
		
		secLayer.put("response", tmp);
	}
	
	public void setResponseTxtForStep4(String rsp) throws JsonMappingException, JsonProcessingException {
//		HashMap<String, Object> rspData = new Gson().fromJson(rsp, new TypeToken<HashMap<String, Object>>() {
//		}.getType());
		HashMap<String, Object> rspData = new ObjectMapper().readValue(rsp,
				new TypeReference<HashMap<String, Object>>() {
		});
		
		rspData.put("newUAFRequest",
				new Gson().fromJson((String) rspData.get("newUAFRequest"), AuthenticatorRecord[].class));
		
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("message", rspData);
		
		tmp.put("status_code", rspData.get("statusCode"));
		tmp.put("result_code", rspData.get("statusCode"));
		
		secLayer.put("response", tmp);
	}

	public String getCompleteTxt(String sessionID) {
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
