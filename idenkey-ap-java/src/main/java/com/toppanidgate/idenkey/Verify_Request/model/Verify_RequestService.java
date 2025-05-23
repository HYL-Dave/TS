package com.toppanidgate.idenkey.Verify_Request.model;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.naming.NamingException;

public class Verify_RequestService {

	private Verify_RequestDAO_interface dao;

	public Verify_RequestService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new Verify_RequestDAO(jndi, sessID);
	}

	public long addVerify_Request(Verify_RequestVO verify_RequestVO) throws SQLException {
		return dao.insert(verify_RequestVO);
	}

	public void updateVerify_Request(String status_Code, String return_Data, Timestamp last_Modified, long customer_ID,
			String request_ID, String device_OS) throws SQLException {

		Verify_RequestVO verify_RequestVO = new Verify_RequestVO();

		verify_RequestVO.setStatus_Code(status_Code);
		verify_RequestVO.setReturn_Data(return_Data);
		verify_RequestVO.setiDGate_ID(customer_ID);
		verify_RequestVO.setRequest_ID(request_ID);
		verify_RequestVO.setDevice_OS(device_OS);

		dao.update(verify_RequestVO);
	}

	public Verify_RequestVO getOneVerify_Request(long customer_ID, String request_ID) throws SQLException {
		return dao.findStatusCode(customer_ID, request_ID);
	}

	public List<TxnVO> getAllTxn(long customer_ID) throws SQLException {
		return dao.getAllTxn(customer_ID);
	}

	public List<TxnVO> getAllTxnEncrypted(long customer_ID, int number, int days) throws SQLException, IllegalBlockSizeException {
		return dao.getAllTxnEncrypted(customer_ID, number, days);
	}

	public Verify_RequestVO getOne(long customer_ID, String request_ID) throws SQLException {
		return dao.getOne(customer_ID, request_ID);
	}

	public Verify_RequestVO getOneWithTime(long customer_ID, String request_ID, String date) throws SQLException {
		return dao.getOneWithTime(customer_ID, request_ID, date);
	}

	public Verify_RequestVO getLastOne(long customer_ID) throws SQLException {
		return dao.getLastOne(customer_ID);
	}

	public void updateTxnStatus(long customer_ID, long req_ID, String status_Code, String old_Status_Code)
			throws SQLException {
		dao.update_TxnStatus(customer_ID, req_ID, status_Code, old_Status_Code);
	}

	public void updateTxnStatusWithTime(long customer_ID, long req_ID, String date, String status_Code,
			String old_Status_Code, String return_Data) throws SQLException {
		dao.update_TxnStatus_With_Time(customer_ID, req_ID, date, status_Code, old_Status_Code, return_Data);
	}

}
