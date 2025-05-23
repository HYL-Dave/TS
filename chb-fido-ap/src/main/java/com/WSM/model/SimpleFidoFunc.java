package com.WSM.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.toppanidgate.WSM.controller.WSMServlet;

// This is the place for those frequently called FIDO functions. These functions only appear once won't be here
@Service
public class SimpleFidoFunc {
	
	@Autowired
	WSMServlet wsmServlet;
	
	public String getDeviceStatus(Locale locale, String apiKey, String idgateID, String channel, String sessionID)
			throws IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "svGetDeviceStatus");
		data.put("channel", channel);
		data.put("idgateID", idgateID);

		return wsmServlet.doPost(apiKey, locale, new Gson().toJson(data), sessionID);
//		return new Send2Remote().sendPost(fido_SV_IP, apiKey, new Gson().toJson(data));
	}

	public String lockDevice(Locale locale, String apiKey, String idgateID, String channel, String sessionID)
			throws IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "svLockDevice");
		data.put("channel", channel);
		data.put("idgateID", idgateID);

		return wsmServlet.doPost(apiKey, locale, new Gson().toJson(data), sessionID);
//		return new Send2Remote().sendPost(fido_SV_IP, apiKey, new Gson().toJson(data));
	}

	public String deregister(Locale locale, String apiKey, String idgateID, String channel, String sessionID)
			throws IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "svDeRegister");
		data.put("channel", channel);
		data.put("idgateID", idgateID);
		// DONE
		return wsmServlet.doPost(apiKey, locale, new Gson().toJson(data), sessionID);
//		return new Send2Remote().sendPost(fido_SV_IP, apiKey, new Gson().toJson(data));
	}

	// for soft token deregister process
	public String deregister_St(Locale locale, String apiKey, String idgateID, String channel, String sessionID)
			throws IOException {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("method", "svDeRegister_St");
		data.put("channel", channel);
		data.put("idgateID", idgateID);
		
		return wsmServlet.doPost(apiKey, locale, new Gson().toJson(data), sessionID);
//		return new Send2Remote().sendPost(locale, apiKey, new Gson().toJson(data));
	}
}
