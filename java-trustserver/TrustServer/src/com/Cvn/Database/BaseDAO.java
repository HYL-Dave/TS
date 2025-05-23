package com.Cvn.Database;

import java.net.UnknownHostException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.Cvn.Config.Cfg;
import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.idenkey.common.model.APLogFormat;
import com.toppanidgate.idenkey.common.model.SQLLogFormat;

public abstract class BaseDAO {
	protected static SQLLogFormat sqlLogFormat = null;
	protected static APLogFormat apLogObj = null;
	private static DataSource ds = null;
	protected static Gson gson = new Gson();
	static {
		try {
			apLogObj = new APLogFormat();
			sqlLogFormat = new SQLLogFormat();
			ds = (DataSource) new InitialContext().lookup(Cfg.getExternalCfgValue("JNDI_Name"));
//			Context envTxt = (Context) new InitialContext().lookup("java:comp/env");
//			ds = (DataSource) envTxt.lookup(Cfg.getExternalCfgValue("DBdataSrc"));
		} catch (NamingException | UnknownHostException e) {
			Log4j.log.error(e.getMessage());
		}
	}

	protected static DataSource getDataSrc() {
//		if (ds == null) { // for 2nd JNDI
			try {
				apLogObj = new APLogFormat();
				sqlLogFormat = new SQLLogFormat();
				ds = (DataSource) new InitialContext().lookup(Cfg.getExternalCfgValue("JNDI_Name"));
//				Context envTxt = (Context) new InitialContext().lookup("java:comp/env");
//				ds = (DataSource) envTxt.lookup(Cfg.getExternalCfgValue("DBdataSrc"));
			} catch (UnknownHostException e) {
				Log4j.log.error(e.getMessage());
//				throw new UnknownHostException(e.getMessage());
			} catch (NamingException e) {
				Log4j.log.error(e.getMessage());
//				throw new NamingException(e.getMessage());
			}
//		}

		return ds;
	}

}
