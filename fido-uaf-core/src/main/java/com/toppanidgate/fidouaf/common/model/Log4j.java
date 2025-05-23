package com.toppanidgate.fidouaf.common.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4j {
	public final static Logger log = LogManager.getLogger(Log4j.class);
//	private static boolean configurated = false;

	public Log4j() {
	};

//	public Log4j(String config_path) throws IOException {
//		if (configurated == false) {
//			Configurator.initialize(null, config_path);
//			configurated = true;
//		}
//	};
}