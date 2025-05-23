package com.toppanidgate.idenkey.SMS_Count.model;

import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.naming.NamingException;

public class SMS_CountService {
	private SMS_CountDAO_interface dao;

	public SMS_CountService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new SMS_CountDAO(jndi, sessID);
	}

	public long create_Record(long idgateID, String operation) throws SQLException {
		return dao.create_Record(idgateID, operation);
	}

	public SMS_CountVO find_Active_Record(long idgateID, String operation) throws SQLException {
		return dao.find_Active_Record(idgateID, operation);
	}

	public SMS_CountVO find_Lastest_5MIN_Success_Record(long idgateID) throws SQLException {
		return dao.find_Lastest_5MIN_Success_Record(idgateID);
	}

	public SMS_CountVO get_One(long id) throws SQLException {
		return dao.get_One(id);
	}

	public void update_Counter(long id, int count) throws SQLException {
		dao.update_Counter(id, count);
	}

	public void update_Status(long id, String status) throws SQLException {
		dao.update_Status(id, status);
	}
}
