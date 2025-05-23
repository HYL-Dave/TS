package com.Channel.model;

import java.sql.SQLException;
import java.util.List;

public class ChannelService {
	ChannelDAO_interface dao = null;

	public ChannelService(final String jndi, final String sessID) {
		dao = new ChannelDAO(jndi, sessID);
	}

	public void addNewChannel(String code, String name, String mode) throws SQLException {
		dao.addChannel(code, name, mode);
	}

	public void updateChannelMode(String code, String mode) throws SQLException {
		dao.updateChannelMode(code, mode);
	}

	public List<ChannelVO> getChannelList() throws SQLException {
		return dao.getChannelList();
	}

	public ChannelVO getOneChannel(String code) throws SQLException {
		return dao.getChannel(code);
	}
}
