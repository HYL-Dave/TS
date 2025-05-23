package com.toppanidgate.idenkey.Channel.model;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

public class ChannelService {
	ChannelDAO_interface dao = null;
	
	public ChannelService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new ChannelDAO(jndi, sessID);
	}
	
	public void addNewChannel(String code, String name, String mode, String jndi) throws SQLException {
		dao.addChannel(code, name, mode, jndi);
	}
	
	public void updateChannelMode(String code, String mode) throws SQLException {
		dao.updateChannelMode(code, mode);
	}
	
	public List<ChannelVO> getChannelList() throws SQLException{
		return dao.getChannelList();
	}
	
	public ChannelVO getOneChannel(String code) throws SQLException {
		return dao.getChannel(code);
	}
}
