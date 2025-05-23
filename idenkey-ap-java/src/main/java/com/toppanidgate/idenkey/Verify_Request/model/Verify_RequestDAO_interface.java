package com.toppanidgate.idenkey.Verify_Request.model;

import java.sql.SQLException;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;

public interface Verify_RequestDAO_interface {

	public long insert(Verify_RequestVO verify_RequestVO) throws SQLException;

	public void update(Verify_RequestVO verify_RequestVO) throws SQLException;

	public Verify_RequestVO findStatusCode(long customer_ID, String request_ID) throws SQLException;

	public List<TxnVO> getAllTxn(long customer_ID) throws SQLException;
	
	public List<TxnVO> getAllTxnEncrypted(long customer_ID, int number, int days) throws SQLException, IllegalBlockSizeException;

	public Verify_RequestVO getOne(long customer_ID, String request_ID) throws SQLException;
	
	public Verify_RequestVO getOneWithTime(long customer_ID, String request_ID, String date) throws SQLException;
	
	public Verify_RequestVO getLastOne(long customer_ID) throws SQLException;

	public void update_TxnStatus(long customer_ID, long req_ID, String status_Code, String old_Status_Code)
			throws SQLException;
	
	public void update_TxnStatus_With_Time(long customer_ID, long req_ID, String date, String status_Code, String old_Status_Code, String return_Data)
			throws SQLException;
}