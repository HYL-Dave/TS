package com.toppanidgate.idenkey.Blocked_Device_Auth.model;

import java.sql.SQLException;
import java.util.List;

public interface Blocked_Device_Auth_interface {
	public void addBlockedDeviceAuth(String label, String model, String type, String blocking, String channel) throws SQLException;

	public void updateBlockedDeviceAuth(String label, String model, String type, String blocking, String channel) throws SQLException;

	public void removeBlockedDeviceAuth(String label, String model, String type, String channel) throws SQLException;

	public List<Blocked_Device_AuthVO> getList(String channel) throws SQLException;

	public Blocked_Device_AuthVO getOne(String label, String model, String type, String channel) throws SQLException;

	public List<String> getChannelList(String label, String model, String type) throws SQLException;

	public List<Blocked_Device_AuthVO2> getList2(String label, String model, String type, String channel) throws SQLException;
}
