package com.toppanidgate.fidouaf.common.model;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Log4jSQL {
	public static final Logger log = LogManager.getLogger(Log4jSQL.class);
	private static boolean configurated = false;

	public Log4jSQL() {
	};

	public Log4jSQL(String config_path) throws IOException {
		if (configurated == false) {
			Configurator.initialize(null, config_path);
			configurated = true;
		}
	};
}