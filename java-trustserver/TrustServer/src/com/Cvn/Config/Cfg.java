package com.Cvn.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.TrustServer.Func.TS_MainFunc;
import com.google.gson.Gson;

public class Cfg {
	public static String getExternalCfgValue(String keyName) {
		Properties properties = null;
		File exCfgFile = new File(TS_MainFunc.getExCfgPath());
		InputStream is = null;

		try {
			is = new FileInputStream(exCfgFile);
			properties = new Properties();
			properties.load(is);
			return properties.getProperty(keyName);

		} catch (IOException e) {
			System.out.println(e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
			return "";
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}
}
