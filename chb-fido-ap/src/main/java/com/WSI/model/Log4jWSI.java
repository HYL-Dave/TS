package com.WSI.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Log4jWSI {
//	public final static Logger log = Logger.getLogger(Log4j.class);
	public final static Logger log = LogManager.getLogger(Log4jWSI.class);

	private static boolean configurated = false;

	public Log4jWSI() {
	};

	public Log4jWSI(String config_path) {
		if (configurated == false) {
			Configurator.initialize(null, config_path);
			configurated = true;
		}
	};
}