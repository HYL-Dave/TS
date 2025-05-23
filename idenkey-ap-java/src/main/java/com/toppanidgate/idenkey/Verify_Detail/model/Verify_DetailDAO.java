package com.toppanidgate.idenkey.Verify_Detail.model;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.Config.IDGateConfig;
import com.toppanidgate.idenkey.Verify_Request.model.TxnVO;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class Verify_DetailDAO extends BaseDAO implements Verify_DetailDAO_interface {

	private static final String INSERT_STMT = "INSERT INTO VERIFY_DETAIL(IDGATE_ID, REQUEST_ID, CHANNEL_CODE, VERIFY_METHOD, TRANSACTION_NAME, TRANSACTION_CONTENT, CHALLENGE, CALLBACK, TRANSACTION_DATA, TRANSACTION_HASH, TRANSACTION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String GET_CHALLENGE = "SELECT CHALLENGE,TRANSACTION_DATA FROM VERIFY_DETAIL WHERE IDGATE_ID=? AND REQUEST_ID=? AND DATEDIFF(DD, TRANSACTION_DATE, ?) = 0";
	private static final String GET_CHALLENGE_MySQL = "SELECT CHALLENGE,TRANSACTION_DATA FROM VERIFY_DETAIL WHERE IDGATE_ID=? AND REQUEST_ID=? AND TIMESTAMPDIFF(DAY, TRANSACTION_DATE, ?) = 0";
	private static final String UPDATE_TXN_DETAIL = "UPDATE VERIFY_DETAIL SET TRANSACTION_DATA=? WHERE IDGATE_ID=? AND REQUEST_ID=? AND DATEDIFF(DD, TRANSACTION_DATE, ?) = 0";
	private static final String UPDATE_TXN_DETAIL2 = "UPDATE VERIFY_DETAIL SET TRANSACTION_DATA=?, SERVERTIME=?,RANDOMENCTXNDATA=?,ENCAUTHREQ=? WHERE IDGATE_ID=? AND REQUEST_ID=? AND DATEDIFF(DD, TRANSACTION_DATE, ?) = 0";
	private static final String UPDATE_TXN_DETAIL2_MySQL = "UPDATE VERIFY_DETAIL SET TRANSACTION_DATA=?, SERVERTIME=?,RANDOMENCTXNDATA=?,ENCAUTHREQ=? WHERE IDGATE_ID=? AND REQUEST_ID=? AND TIMESTAMPDIFF(DAY, TRANSACTION_DATE, ?) = 0";
	private static final String GET_ONE = "SELECT * FROM VERIFY_DETAIL WHERE IDGATE_ID=? AND REQUEST_ID=? AND DATEDIFF(DD, TRANSACTION_DATE, ?) = 0";
	private static final String GET_ONE_MySQL = "SELECT * FROM VERIFY_DETAIL WHERE IDGATE_ID=? AND REQUEST_ID=? AND TIMESTAMPDIFF(DAY, TRANSACTION_DATE, ?) = 0";
	private static final String GET_ENCTXN_DATA = "SELECT TRANSACTION_DATA, ENCAUTHREQ FROM VERIFY_DETAIL WHERE IDGATE_ID=? AND REQUEST_ID=? AND DATEDIFF(DD, TRANSACTION_DATE, ?) = 0";
	private static final String GET_ENCTXN_DATA_MySQL = "SELECT TRANSACTION_DATA, ENCAUTHREQ FROM VERIFY_DETAIL WHERE IDGATE_ID=? AND REQUEST_ID=? AND TIMESTAMPDIFF(DAY, TRANSACTION_DATE, ?) = 0";

	private long start_Time = 0, end_Time = 0, count = 0;

	public Verify_DetailDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.Verify_Detail.model.Verify_DetailDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public void insert(Verify_DetailVO verify_DetailVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(INSERT_STMT);) {
			sqlLogFormat.setSql_Statement(INSERT_STMT);

			pstmt.setLong(1, verify_DetailVO.getiDGate_ID());
			pstmt.setString(2, verify_DetailVO.getRequest_ID());
			pstmt.setString(3, verify_DetailVO.getChannel_Code());
			pstmt.setString(4, verify_DetailVO.getVerify_Method());
			pstmt.setNString(5, verify_DetailVO.getTransaction_Name());
			pstmt.setNString(6, verify_DetailVO.getTransaction_Content());
			pstmt.setString(7, verify_DetailVO.getChallenge());
			pstmt.setString(8, verify_DetailVO.getCallback());
			pstmt.setNString(9, verify_DetailVO.getTransaction_Data());
			pstmt.setString(10, verify_DetailVO.getTransaction_Hash());
			pstmt.setTimestamp(11, verify_DetailVO.getTransaction_Date());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
	
	public void upadte_TxnDeatil(long customer_ID, String request_ID, String date, String txn_Data)
			throws SQLException {
		
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_TXN_DETAIL);) {
			sqlLogFormat.setSql_Statement(UPDATE_TXN_DETAIL);
			
			pstmt.setNString(1, txn_Data);
			pstmt.setLong(2, customer_ID);
			pstmt.setString(3, request_ID);
			pstmt.setString(4, date);
			
			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			
			end_Time = System.currentTimeMillis();
			
			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	public void upadte_TxnDeatil2(long customer_ID, String request_ID, String date, String encTxnData, String serverTime, String rsaRandomKey, String encAuthReq)
			throws SQLException {
		String sqlStatement = UPDATE_TXN_DETAIL2;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = UPDATE_TXN_DETAIL2_MySQL;
		} 

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setNString(1, encTxnData);
			pstmt.setNString(2, serverTime);
			pstmt.setNString(3, rsaRandomKey);
			pstmt.setNString(4, encAuthReq);
			pstmt.setLong(5, customer_ID);
			pstmt.setString(6, request_ID);
			pstmt.setString(7, date);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public Verify_DetailVO findChallenge(long customer_ID, String request_ID, String date) throws SQLException {

		Verify_DetailVO verify_DetailVO = null;
		ResultSet rs = null;
		
		String sqlStatement = GET_CHALLENGE;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = GET_CHALLENGE_MySQL;
		} 

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setLong(1, customer_ID);
			pstmt.setString(2, request_ID);
			pstmt.setString(3, date);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				verify_DetailVO = new Verify_DetailVO();
				verify_DetailVO.setChallenge(rs.getString("Challenge"));
				verify_DetailVO.setTransaction_Data(rs.getNString("Transaction_Data"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (verify_DetailVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(verify_DetailVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				condition.put("Request_ID", request_ID);
				condition.put("Transaction_Date", date);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessage(new Gson().toJson(verify_DetailVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}
		return verify_DetailVO;
	}

	@Override
	public Verify_DetailVO getOne(long customer_ID, String request_ID, String date) throws SQLException {

		Verify_DetailVO verify_DetailVO = null;
		ResultSet rs = null;
		
		String sqlStatement = GET_ONE;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = GET_ONE_MySQL;
		} 

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setLong(1, customer_ID);
			pstmt.setString(2, request_ID);
			pstmt.setString(3, date);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				verify_DetailVO = new Verify_DetailVO();
				verify_DetailVO.setRequest_ID(rs.getString("Request_ID"));
				verify_DetailVO.setiDGate_ID(rs.getInt("iDGate_ID"));
				verify_DetailVO.setChannel_Code(rs.getString("Channel_Code"));
				verify_DetailVO.setVerify_Method(rs.getString("Verify_Method"));
				verify_DetailVO.setChallenge(rs.getString("Challenge"));
				verify_DetailVO.setCallback(rs.getString("Callback"));
				verify_DetailVO.setTransaction_Data(rs.getNString("Transaction_Data"));
				verify_DetailVO.setTransaction_Content(rs.getNString("Transaction_Content"));
				verify_DetailVO.setTransaction_Name(rs.getNString("Transaction_Name"));
				verify_DetailVO.setTransaction_Hash(rs.getString("Transaction_Hash"));
				verify_DetailVO.setTransaction_Date(rs.getTimestamp("Transaction_Date"));
				verify_DetailVO.setEncAuthReq(rs.getNString("encAuthReq"));
				verify_DetailVO.setServerTime(rs.getString("serverTime"));
				verify_DetailVO.setRandomEncTxnData(rs.getString("randomEncTxnData"));
				verify_DetailVO.setRandomEncAuthReq(rs.getString("randomEncAuthReq"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (verify_DetailVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(verify_DetailVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				condition.put("Request_ID", request_ID);
				condition.put("Transaction_Date", date);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessage(new Gson().toJson(verify_DetailVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}
		return verify_DetailVO;
	}

	@Override
	public List<TxnVO> getEncTxnData(long customer_ID, List<TxnVO> txnData) throws SQLException {

		ResultSet rs = null;
		TxnVO tmp = null;
		
		String sqlStatement = GET_ENCTXN_DATA;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = GET_ENCTXN_DATA_MySQL;
		} 

		for (int i = 0; i < txnData.size(); i++) {
			tmp = txnData.get(i);
			try (Connection con = ds.getConnection();
					PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
				sqlLogFormat.setSql_Statement(sqlStatement);

				pstmt.setLong(1, customer_ID);
				pstmt.setString(2, tmp.getTxnID().substring(8));
				pstmt.setTimestamp(3, Timestamp.valueOf(tmp.getSubTitle()));

				// get execution time
				start_Time = System.currentTimeMillis();
				rs = pstmt.executeQuery();

				if (rs.next()) {
					tmp.setEncTxnData(rs.getNString(1));
					tmp.setEncAuthReq(rs.getNString(2));
					count++;
				}

				end_Time = System.currentTimeMillis();

				sqlLogFormat.setResult_Count(count);
				sqlLogFormat.setMessage(new Gson().toJson(tmp));
				sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
				sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
				Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			} catch (SQLException se) {
				sqlLogFormat.setEexecute_Ttime(0);
				sqlLogFormat.setThrowable(se.getMessage());
				sqlLogFormat.setResult_StatusMessage("false");
				Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
				// Handle any SQL errors
				throw new SQLException("A database error occured. " + se.getMessage());
				// Clean up JDBC resources
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (SQLException se) {
					}
			}

			txnData.set(i, tmp);
			tmp = null;
		}

		return txnData;
	}
}
