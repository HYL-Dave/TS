package com.Common.Model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Log4j {

	public final static Logger log = LogManager.getLogger(Log4j.class);
	private static boolean configurated = false;
	
	public Log4j(String config_path) {
		if (!configurated) {
			Configurator.initialize(null, config_path);
			configurated = true;
		}
	};
}