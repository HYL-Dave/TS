package com.Device_Detail.model;

import java.sql.SQLException;
import java.sql.Timestamp;

public class Device_DetailService {

	private Device_DetailDAO_interface dao;

	public Device_DetailService(final String jndi, final String sessID) {
		dao = new Device_DetailDAO(jndi, sessID);
	}

	public void addDevice_Detail(long customer_ID, String eSN, String device_Data, String device_ID, String device_Type,
			String device_OS, String perso_Update, String deviceLabel, String deviceModel, String device_Reg_Ip,
			String device_OS_Ver, String app_Ver) throws SQLException {

		Device_DetailVO device_DetailVO = new Device_DetailVO();

		device_DetailVO.setIdgateID(customer_ID);
		device_DetailVO.setESN(eSN);
		device_DetailVO.setDevice_Data(device_Data);
		device_DetailVO.setDevice_ID(device_ID);
		device_DetailVO.setDevice_Type(device_Type);
		device_DetailVO.setDevice_OS(device_OS);
		device_DetailVO.setPerso_Update(perso_Update);
		device_DetailVO.setDeviceLabel(deviceLabel);
		device_DetailVO.setDeviceModel(deviceModel);
		device_DetailVO.setDevice_Reg_IP(device_Reg_Ip);
		device_DetailVO.setDevice_OS_Ver(device_OS_Ver);
		device_DetailVO.setAPP_Ver(app_Ver);

		dao.insert(device_DetailVO);
	}

	public void updatePushID(Device_DetailVO device_DetailVO) throws SQLException {
		dao.update(device_DetailVO);
	}

	public Device_DetailVO updateDeviceData(String eSN, String device_Data, String pushID, String deviceType, String os,
			String deviceLabel, String deviceModel, Timestamp modified_Date, String device_OS_Ver, String app_Ver,
			long customer_ID) throws SQLException {

		Device_DetailVO device_DetailVO = new Device_DetailVO();

		device_DetailVO.setESN(eSN);
		device_DetailVO.setDevice_Data(device_Data);
		device_DetailVO.setDevice_Type(deviceType);
		device_DetailVO.setDevice_OS(os);
		device_DetailVO.setDeviceLabel(deviceLabel);
		device_DetailVO.setDeviceModel(deviceModel);
		device_DetailVO.setDevice_ID(pushID);
		device_DetailVO.setModified_Date(modified_Date);
		device_DetailVO.setIdgateID(customer_ID);
		device_DetailVO.setDevice_OS_Ver(device_OS_Ver);
		device_DetailVO.setAPP_Ver(app_Ver);

		dao.updateDeviceData(device_DetailVO);

		return device_DetailVO;
	}

	public void updatePerso(String perso_Update, Timestamp modified_Date, long customer_ID) throws SQLException {

		Device_DetailVO device_DetailVO = new Device_DetailVO();

		device_DetailVO.setPerso_Update(perso_Update);
		device_DetailVO.setModified_Date(modified_Date);
		device_DetailVO.setIdgateID(customer_ID);

		dao.updatePerso(device_DetailVO);
	}

	public Device_DetailVO getOneDevice_Detail(long idgateID) throws SQLException {
		return dao.findByPrimaryKey(idgateID);
	}

	public void update_AB_Count(long customer_ID, Integer count) throws SQLException {
		dao.updateABCount(customer_ID, count);
	}

	public void prestore_New_Esn(long customer_ID, String esn, String pin) throws SQLException {
		dao.prestoreNewEsn(customer_ID, esn, pin);
	}

	public void update_To_New_ESN(long customer_ID) throws SQLException {
		dao.updateToNewEsn(customer_ID);
	}

	public void force_ALL_Perso_Update() throws SQLException {
		dao.forceAllPersoUpdate();
	}
}