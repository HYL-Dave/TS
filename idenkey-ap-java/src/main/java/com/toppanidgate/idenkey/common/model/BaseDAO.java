package com.toppanidgate.idenkey.common.model;

import java.net.UnknownHostException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.toppanidgate.fidouaf.common.model.Log4j;

public class BaseDAO {
	
	protected SQLLogFormat sqlLogFormat = null;
	protected DataSource ds = null;
	protected APLogFormat apLogObj = null;

	public BaseDAO() {
		try {
			apLogObj = new APLogFormat();
			sqlLogFormat = new SQLLogFormat();
			ds = (DataSource) new InitialContext().lookup("jdbc/Common");
		} catch (NamingException | UnknownHostException e) {
			Log4j.log.error(e.getMessage());
		}  
	}

	public BaseDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
			try {
				apLogObj = new APLogFormat();
				sqlLogFormat = new SQLLogFormat();
				ds = (DataSource) new InitialContext().lookup(jndi);
			} catch (UnknownHostException e) {
				Log4j.log.error(e.getMessage());
				throw new UnknownHostException(e.getMessage());
			} catch (NamingException e) {
				Log4j.log.error(e.getMessage());
				throw new NamingException(e.getMessage());
			}
	}
}
