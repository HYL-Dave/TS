package com.Common.model;

import java.net.UnknownHostException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;

public class BaseDAO {

	protected SQLLogFormat sqlLogFormat = null;
	protected DataSource ds = null;

	public BaseDAO() {
		try {
			ds = (DataSource) new InitialContext().lookup("jdbc/Common");
//			ds = (DataSource) new InitialContext().lookup("jdbc/IDGFID2");
			sqlLogFormat = new SQLLogFormat();
		} catch (NamingException | UnknownHostException e) {
			Log4j.log.fatal(
					"Datasource error: " + e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
		}
	}

	public BaseDAO(String jndi) {
		try {
			ds = (DataSource) new InitialContext().lookup(jndi);
			sqlLogFormat = new SQLLogFormat();
		} catch (NamingException | UnknownHostException e) {
			Log4j.log.fatal(
					"Datasource error: " + e.getMessage() + ", stacktrace: " + new Gson().toJson(e.getStackTrace()));
		}
	}

}
