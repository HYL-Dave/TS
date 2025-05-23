package com.Common.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;


public class Log4jAP {
//	public final static Logger log = Logger.getLogger(Log4jAP.class);
	public static final Logger log = LogManager.getLogger(Log4jAP.class);

	private static boolean configurated = false;

	public Log4jAP() {
	};

	public Log4jAP(String config_path) {
		if (configurated == false) {
			Configurator.initialize(null, config_path);
			configurated = true;
		}
	};
}