package com.Channel.model;

import java.sql.SQLException;
import java.util.List;

public interface ChannelDAO_interface {
	public void addChannel(String code, String name, String mode) throws SQLException;
	
	public void updateChannelMode(String code, String mode) throws SQLException;
	
	public List<ChannelVO> getChannelList() throws SQLException;
	
	public ChannelVO getChannel(String code) throws SQLException;
}
