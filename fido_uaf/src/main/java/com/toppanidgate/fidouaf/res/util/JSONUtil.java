package com.toppanidgate.fidouaf.res.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONUtil {

	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	/**
	 * fromJson
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static Object objFromJson(String json, Class<Object> clazz) {
		 return gson.fromJson(json, clazz);
	}
	
	/**
	 * toJson
	 * @param pojo
	 * @return
	 */
	public static String pojoToJson(Object pojo) {
		 return gson.toJson(pojo);
	}
	
	
	
}
