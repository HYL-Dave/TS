package com.toppanidgate.idenkey.Verify_Detail.model;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.naming.NamingException;

import com.toppanidgate.idenkey.Verify_Request.model.TxnVO;


public class Verify_DetailService {

	private Verify_DetailDAO_interface dao;

	public Verify_DetailService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new Verify_DetailDAO(jndi, sessID);
	}
	
	public void upadte_Txn_Deatil(long customer_ID, String request_ID, String date, String txn_Data)
			throws SQLException {
		dao.upadte_TxnDeatil(customer_ID, request_ID, date, txn_Data);
	}

	// , txn_Data, serverTime, rsaRandomKey
	public void upadte_Txn_Deatil2(long customer_ID, String request_ID, String date, String encTxnData, String serverTime, String rsaRandomKey, String encAuthReq)
			throws SQLException {
		dao.upadte_TxnDeatil2(customer_ID, request_ID, date, encTxnData, serverTime, rsaRandomKey, encAuthReq);
	}

	public Verify_DetailVO addVerify_DetailVO(long customer_ID, String request_ID, String channel, String verify_Method,
			String transaction_Name, String transaction_Content, String challenge, String transaction_Data, String hash,
			String callback, Timestamp date) throws SQLException {

		Verify_DetailVO verify_DetailVO = new Verify_DetailVO();

		verify_DetailVO.setiDGate_ID(customer_ID);
		verify_DetailVO.setRequest_ID(request_ID);
		verify_DetailVO.setChannel_Code(channel);
		verify_DetailVO.setVerify_Method(verify_Method);
		verify_DetailVO.setTransaction_Name(transaction_Name);
		verify_DetailVO.setTransaction_Content(transaction_Content);
		verify_DetailVO.setChallenge(challenge);
		verify_DetailVO.setTransaction_Data(transaction_Data);
		verify_DetailVO.setTransaction_Hash(hash);
		verify_DetailVO.setCallback(callback);
		verify_DetailVO.setTransaction_Date(date);

		dao.insert(verify_DetailVO);

		return verify_DetailVO;
	}

	public Verify_DetailVO getOneVerify_Detail(long idgateID, String request_ID, String date) throws SQLException {
		return dao.getOne(idgateID, request_ID, date);
	}

	public List<TxnVO> getEncTxnData(long idgID, List<TxnVO> txnData) throws SQLException {
		return dao.getEncTxnData(idgID, txnData);
	}

}
