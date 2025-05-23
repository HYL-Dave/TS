package com.Device_Detail.model;

import java.sql.SQLException;

public interface Device_DetailDAO_interface {

	public void insert(Device_DetailVO device_DetailVO) throws SQLException;

	public void update(Device_DetailVO device_DetailVO) throws SQLException;

	public void updateDeviceData(Device_DetailVO device_DetailVO) throws SQLException;

	public void updatePerso(Device_DetailVO device_DetailVO) throws SQLException;

	public Device_DetailVO findByPrimaryKey(long customer_ID) throws SQLException;

	public void updateABCount(long customer_ID, Integer count) throws SQLException;

	public void prestoreNewEsn(long customer_ID, String esn, String pin) throws SQLException;

	public void updateToNewEsn(long customer_ID) throws SQLException;

	public void forceAllPersoUpdate() throws SQLException;
}