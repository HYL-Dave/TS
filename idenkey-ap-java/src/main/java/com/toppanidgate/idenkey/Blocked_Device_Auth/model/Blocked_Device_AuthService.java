package com.toppanidgate.idenkey.Blocked_Device_Auth.model;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

public class Blocked_Device_AuthService {
	private Blocked_Device_Auth_interface dao;
	
	public Blocked_Device_AuthService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new Blocked_Device_AuthDAO(jndi, sessID);
	}

	public void addBlockedDeviceAuth(String label, String model, String type, String blocking, String channel) throws SQLException {
		dao.addBlockedDeviceAuth(label, model, type, blocking, channel);
	}

	public void updateBlockedDeviceAuth(String label, String model, String type, String blocking, String channel) throws SQLException{
		dao.updateBlockedDeviceAuth(label, model, type, blocking, channel);
	}
	
	public void removeBlockedDeviceAuth(String label, String model, String type, String channel) throws SQLException{
		dao.removeBlockedDeviceAuth(label, model, type, channel);
	}
	
	public List<Blocked_Device_AuthVO> getList(String channel) throws SQLException{
		return dao.getList(channel);
	}
	
	public List<Blocked_Device_AuthVO2> getList2(String label, String model, String type, String channel) throws SQLException{
		return dao.getList2(label, model, type, channel);
	}
	
	public Blocked_Device_AuthVO getOne(String label, String model, String type, String channel) throws SQLException{
		return dao.getOne(label, model, type, channel);
	}
	
	public List<String> getChannelList(String label, String model, String type) throws SQLException{
		return dao.getChannelList(label, model, type);

	}

}
