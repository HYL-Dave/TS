package com.toppanidgate.idenkey.Device_Detail.model;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.toppanidgate.WSM.controller.WSMServlet;

public class Device_DetailService {
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WSMServlet.class);

	private Device_DetailDAO_interface dao;

	public Device_DetailService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new Device_DetailDAO(jndi, sessID);
	}

	public void addDevice_Detail(long customer_ID, String eSN, String device_Data, String device_ID, String device_Type,
			String digital_Hash, String pattern_Hash, String bio_Hash, String auth_Type, String device_OS,
			String perso_Update, String deviceLabel, String deviceModel, String device_Reg_Ip, String device_OS_Ver,
			String app_Ver, String transactionID) throws SQLException {

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
		device_DetailVO.setAuth_Type(auth_Type);
		device_DetailVO.setDigital_Hash(digital_Hash);
		device_DetailVO.setPattern_Hash(pattern_Hash);
		device_DetailVO.setBio_Hash(bio_Hash);
		device_DetailVO.setDevice_Reg_IP(device_Reg_Ip);
		device_DetailVO.setDevice_OS_Ver(device_OS_Ver);
		device_DetailVO.setAPP_Ver(app_Ver);
		device_DetailVO.setTransaction_ID(transactionID);

		dao.insert(device_DetailVO);
	}

	public void updatePushID(String device_ID, String device_OS, Timestamp modified_Date, String device_OS_Ver,
			String app_Ver, long customer_ID) throws SQLException {

		Device_DetailVO device_DetailVO = new Device_DetailVO();

		device_DetailVO.setDevice_ID(device_ID);
		device_DetailVO.setDevice_OS(device_OS);
		device_DetailVO.setModified_Date(modified_Date);
		device_DetailVO.setIdgateID(customer_ID);
		device_DetailVO.setDevice_OS_Ver(device_OS_Ver);
		device_DetailVO.setAPP_Ver(app_Ver);

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

	public void update_Auth_Type(long customer_ID, String type, String oldType) throws SQLException {
		dao.updateAuthType(customer_ID, type, oldType);
	}

	public void update_Digital_Hash(long customer_ID, String hash) throws SQLException {
		dao.updateDigitalHash(customer_ID, hash);
	}

	public void update_Pattern_Hash(long customer_ID, String hash) throws SQLException {
		dao.updatePatternHash(customer_ID, hash);
	}

	public void update_Bio_Hash(long customer_ID, String hash) throws SQLException {
		dao.updateBioHash(customer_ID, hash);
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