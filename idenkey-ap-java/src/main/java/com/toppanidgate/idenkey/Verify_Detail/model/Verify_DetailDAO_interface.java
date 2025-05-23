package com.toppanidgate.idenkey.Verify_Detail.model;

import java.sql.SQLException;
import java.util.List;

import com.toppanidgate.idenkey.Verify_Request.model.TxnVO;


public interface Verify_DetailDAO_interface {
	public void insert(Verify_DetailVO verify_DetailVO) throws SQLException;

	public Verify_DetailVO findChallenge(long customer_ID, String request_ID, String date) throws SQLException;
	
	public void upadte_TxnDeatil(long customer_ID, String request_ID, String date, String txn_Data) throws SQLException;
	public void upadte_TxnDeatil2(long customer_ID, String request_ID, String date, String encTxnData, String serverTime, String rsaRandomKey, String encAuthReq) throws SQLException;
	
	public Verify_DetailVO getOne(long customer_ID, String request_ID, String date) throws SQLException;
	
	public List<TxnVO> getEncTxnData(long customer_ID, List<TxnVO> txnData) throws SQLException;
}
