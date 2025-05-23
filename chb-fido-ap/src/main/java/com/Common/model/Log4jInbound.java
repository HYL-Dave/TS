package com.Common.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Log4jInbound {
//	public final static Logger log = Logger.getLogger(Log4jInbound.class);
	public static final Logger log = LogManager.getLogger(Log4jInbound.class);

	private static boolean configurated = false;

	public Log4jInbound() {
	};

	public Log4jInbound(String config_path) {
		if (configurated == false) {
			Configurator.initialize(null, config_path);
			configurated = true;
		}
	};
}