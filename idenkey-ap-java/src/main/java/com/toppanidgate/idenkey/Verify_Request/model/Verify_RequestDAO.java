package com.toppanidgate.idenkey.Verify_Request.model;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.IllegalBlockSizeException;
import javax.naming.NamingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.Config.IDGateConfig;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class Verify_RequestDAO extends BaseDAO implements Verify_RequestDAO_interface {

	private static final String GET_ALL_TXN = "SELECT TOP(?) REQUEST_ID, STATUS_CODE, TRANSACTION_NAME, TRANSACTION_DATE FROM VERIFY_REQUEST WHERE IDGATE_ID=? AND STATUS_CODE <>'07' AND STATUS_CODE <>'08' AND DATEDIFF(DD,TRANSACTION_DATE,GETDATE())<=? ORDER BY TRANSACTION_DATE DESC";
	private static final String GET_ALL_TXN_MySQL = "SELECT TOP(?) REQUEST_ID, STATUS_CODE, TRANSACTION_NAME, TRANSACTION_DATE FROM VERIFY_REQUEST WHERE IDGATE_ID=? AND STATUS_CODE <>'07' AND STATUS_CODE <>'08' AND TIMESTAMPDIFF(DAY,TRANSACTION_DATE,GETDATE())<=? ORDER BY TRANSACTION_DATE DESC";

	private static final String UPDATE = "UPDATE VERIFY_REQUEST SET STATUS_CODE = ?, RETURN_DATA = ?, LAST_MODIFIED= GETDATE(), Device_OS = ? WHERE IDGATE_ID = ? AND REQUEST_ID = ?";

	private static final String GET_ONE_STMT = "SELECT REQUEST_ID, AUTH_MODE, STATUS_CODE, TRANSACTION_DATE, VERIFY_TYPE, RETURN_DATA, CHANNEL_CODE, GETDATE() AS DBTIME FROM VERIFY_REQUEST WHERE IDGATE_ID=? AND REQUEST_ID=? ORDER BY TRANSACTION_DATE DESC";
	private static final String GET_ONE_WITH_TIME_STMT = "SELECT REQUEST_ID, AUTH_MODE, STATUS_CODE, TRANSACTION_DATE, VERIFY_TYPE, RETURN_DATA, CHANNEL_CODE, Transaction_Name, GETDATE() AS DBTIME FROM VERIFY_REQUEST WHERE IDGATE_ID=? AND REQUEST_ID=?";

	private static final String GET_LAST_ONE_STMT = "SELECT TOP(1) REQUEST_ID, AUTH_MODE, STATUS_CODE, VERIFY_TYPE, TRANSACTION_DATE, RETURN_DATA, CHANNEL_CODE, GETDATE() AS DBTIME FROM VERIFY_REQUEST WHERE IDGATE_ID=? ORDER BY TRANSACTION_DATE DESC";

	private static final String INSERT_STMT = "INSERT INTO VERIFY_REQUEST(IDGATE_ID, STATUS_CODE, AUTH_MODE, RETURN_DATA, VERIFY_TYPE, CHANNEL_CODE, TRANSACTION_NAME) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String GET_STATUSCODE = "SELECT STATUS_CODE FROM VERIFY_REQUEST WHERE IDGATE_ID=? AND REQUEST_ID=?";

	private static final String UPDATE_TXN_STATUS = "UPDATE VERIFY_REQUEST SET STATUS_CODE = ?, LAST_MODIFIED = GETDATE() WHERE IDGATE_ID = ? AND REQUEST_ID = ? AND STATUS_CODE = ?";
	private static final String UPDATE_TXN_STATUS_WITH_TIME = "UPDATE VERIFY_REQUEST SET STATUS_CODE = ?, RETURN_DATA = ?, LAST_MODIFIED = GETDATE() WHERE IDGATE_ID = ? AND REQUEST_ID = ? AND STATUS_CODE = ?";

	private long start_Time = 0, end_Time = 0, count = 0;

	public Verify_RequestDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.Verify_Request.model.Verify_RequestDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public long insert(Verify_RequestVO verify_RequestVO) throws SQLException {

		long generatedKey = 0;
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(INSERT_STMT, Statement.RETURN_GENERATED_KEYS);) {
			sqlLogFormat.setSql_Statement(INSERT_STMT);

			pstmt.setLong(1, verify_RequestVO.getiDGate_ID());
			pstmt.setString(2, verify_RequestVO.getStatus_Code());
			pstmt.setString(3, verify_RequestVO.getAuth_Mode());
			pstmt.setString(4, verify_RequestVO.getReturn_Data());
			pstmt.setString(5, verify_RequestVO.getVerify_Type());
			pstmt.setString(6, verify_RequestVO.getChannel_Code());
			pstmt.setString(7, verify_RequestVO.getTransaction_Name());

			// get execution time
			start_Time = System.currentTimeMillis();
			pstmt.executeUpdate();
			try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					generatedKey = generatedKeys.getLong(1);

					count++;
					end_Time = System.currentTimeMillis();

					sqlLogFormat.setResult_Count(count);
					sqlLogFormat.setMessage(String.valueOf(count));
					sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
					sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
					Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
				} else {
					sqlLogFormat.setEexecute_Ttime(0);
					sqlLogFormat.setThrowable("Creating Txn failed, no ID obtained.");
					sqlLogFormat.setResult_StatusMessage("false");
					Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
					throw new SQLException("Creating Txn failed, no ID obtained.");
				}
			} catch (JsonSyntaxException e) {
				sqlLogFormat.setEexecute_Ttime(0);
				sqlLogFormat.setThrowable(e.getMessage());
				sqlLogFormat.setResult_StatusMessage("false");
				Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
				throw new SQLException("A database error occured. " + e.getMessage());
			}

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
		Log4j.log.debug(" [Version: {}][insert] *** generatedKey:[{}]",
				IDGateConfig.svVerNo, generatedKey);
		return generatedKey;
	}

	@Override
	public void update(Verify_RequestVO verify_RequestVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE);) {
			sqlLogFormat.setSql_Statement(UPDATE);

			pstmt.setString(1, verify_RequestVO.getStatus_Code());
			pstmt.setString(2, verify_RequestVO.getReturn_Data());
			pstmt.setString(3, verify_RequestVO.getDevice_OS());
			pstmt.setLong(4, verify_RequestVO.getiDGate_ID());
			pstmt.setString(5, verify_RequestVO.getRequest_ID().substring(8));

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
	public Verify_RequestVO findStatusCode(long customer_ID, String request_ID) throws SQLException {

		Verify_RequestVO verify_RequestVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_STATUSCODE);) {
			sqlLogFormat.setSql_Statement(GET_STATUSCODE);

			pstmt.setLong(1, customer_ID);
			pstmt.setString(2, request_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				// deptVO �]�٬� Domain objects
				verify_RequestVO = new Verify_RequestVO();
				verify_RequestVO.setStatus_Code(rs.getString("Status_Code"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(verify_RequestVO));
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
		return verify_RequestVO;
	}

	@Override
	public List<TxnVO> getAllTxn(long customer_ID) throws SQLException {
		List<TxnVO> list = new ArrayList<>();
		TxnVO txnVO = null;
		ResultSet rs = null;
		
		String sqlStatement = GET_ALL_TXN;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = GET_ALL_TXN_MySQL;
		} 

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setLong(1, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				txnVO = new TxnVO();
				txnVO.setTxnID(rs.getString("Request_ID"));
				txnVO.setAuthStatus(rs.getString("Status_Code"));
				txnVO.setTitle(rs.getString("Transaction_Name"));
				txnVO.setSubTitle(
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("Transaction_Date")));
				list.add(txnVO); // Store the row in the list
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(list));
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
		return list;
	}

	public List<TxnVO> getAllTxnEncrypted(long customer_ID, int number, int days) throws SQLException, IllegalBlockSizeException {
		List<TxnVO> list = new ArrayList<>();
		TxnVO txnVO = null;
		ResultSet rs = null;
		String txnID = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ALL_TXN);) {
			sqlLogFormat.setSql_Statement(GET_ALL_TXN);

			pstmt.setInt(1, number);
			pstmt.setLong(2, customer_ID);
			pstmt.setInt(3, days);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				txnVO = new TxnVO();
				txnID = new SimpleDateFormat("yyyyMMdd").format(rs.getTimestamp("Transaction_Date"))
						+ rs.getString("Request_ID");
				txnVO.setTxnID(txnID);
				txnVO.setAuthStatus(rs.getString("Status_Code"));
				txnVO.setTitle(rs.getString("Transaction_Name"));
				txnVO.setSubTitle(
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("Transaction_Date")));
				list.add(txnVO); // Store the row in the list
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(list));
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
		} finally {
			// Clean up JDBC resources
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}
		return list;
	}

	@Override
	public Verify_RequestVO getOne(long customer_ID, String request_ID) throws SQLException {
		Verify_RequestVO vrVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE_STMT);) {
			sqlLogFormat.setSql_Statement(GET_ONE_STMT);

			pstmt.setLong(1, customer_ID);
			pstmt.setString(2, request_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				vrVO = new Verify_RequestVO();
				vrVO.setiDGate_ID(customer_ID);
				vrVO.setRequest_ID(request_ID);
				vrVO.setVerify_Type(rs.getString("Verify_Type"));
				vrVO.setStatus_Code(rs.getString("Status_Code"));
				vrVO.setTransaction_Date(rs.getTimestamp("Transaction_Date"));
				vrVO.setChannel_Code(rs.getString("Channel_Code"));
				vrVO.setReturn_Data(rs.getString("Return_Data"));
				vrVO.setAuth_Mode(rs.getString("Auth_Mode"));
				vrVO.setDB_Time(rs.getTimestamp("DBTime"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (vrVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(vrVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				condition.put("Request_ID", request_ID);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessageForMap(new Gson().fromJson(new Gson().toJson(vrVO), new TypeToken<HashMap<String, Object>>() {
//			}.getType()));
//			sqlLogFormat.setMessage(new Gson().toJson(vrVO));
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

		return vrVO;
	}

	@Override
	public void update_TxnStatus(long customer_ID, long req_ID, String status_Code, String old_Status_Code)
			throws SQLException {
		int rowCount = 0;
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_TXN_STATUS);) {
			sqlLogFormat.setSql_Statement(UPDATE_TXN_STATUS);

			pstmt.setString(1, status_Code);
			pstmt.setLong(2, customer_ID);
			pstmt.setLong(3, req_ID);
			pstmt.setString(4, old_Status_Code);

			// get execution time
			start_Time = System.currentTimeMillis();
			rowCount = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(rowCount);
			sqlLogFormat.setMessage(String.valueOf(rowCount));
			sqlLogFormat.setResult_StatusMessage(rowCount >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			if (rowCount == 0) {
				throw new SQLException("Txn status has been updated by other. This request is cancelled.");
			}

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
	public Verify_RequestVO getLastOne(long customer_ID) throws SQLException {
		Verify_RequestVO vrVO = null;

		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_LAST_ONE_STMT);) {
			sqlLogFormat.setSql_Statement(GET_LAST_ONE_STMT);

			pstmt.setLong(1, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				vrVO = new Verify_RequestVO();
				vrVO.setiDGate_ID(customer_ID);
				vrVO.setVerify_Type(rs.getString("Verify_Type"));
				vrVO.setStatus_Code(rs.getString("Status_Code"));
				vrVO.setRequest_ID(String.valueOf(rs.getLong("Request_ID")));
				vrVO.setTransaction_Date(rs.getTimestamp("Transaction_Date"));
				vrVO.setChannel_Code(rs.getString("Channel_Code"));
				vrVO.setReturn_Data(rs.getString("Return_Data"));
				vrVO.setAuth_Mode(rs.getString("Auth_Mode"));
				vrVO.setDB_Time(rs.getTimestamp("DBTime"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(vrVO));
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

		return vrVO;
	}

	@Override
	public Verify_RequestVO getOneWithTime(long customer_ID, String request_ID, String date) throws SQLException {
		Verify_RequestVO vrVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(GET_ONE_WITH_TIME_STMT);) {

			sqlLogFormat.setSql_Statement(GET_ONE_WITH_TIME_STMT);

			pstmt.setLong(1, customer_ID);
			pstmt.setString(2, request_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				vrVO = new Verify_RequestVO();
				vrVO.setiDGate_ID(customer_ID);
				vrVO.setRequest_ID(request_ID);
				vrVO.setVerify_Type(rs.getString("Verify_Type"));
				vrVO.setStatus_Code(rs.getString("Status_Code"));
				vrVO.setTransaction_Date(rs.getTimestamp("Transaction_Date"));
				vrVO.setChannel_Code(rs.getString("Channel_Code"));
				vrVO.setReturn_Data(rs.getString("Return_Data"));
				vrVO.setAuth_Mode(rs.getString("Auth_Mode"));
				vrVO.setDB_Time(rs.getTimestamp("DBTime"));
				vrVO.setTransaction_Name(rs.getString("Transaction_Name"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (vrVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(vrVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				condition.put("Request_ID", request_ID);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessageForMap(new Gson().fromJson(new Gson().toJson(vrVO), new TypeToken<HashMap<String, Object>>() {
//			}.getType()));
//			sqlLogFormat.setMessage(new Gson().toJson(vrVO));
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

		} finally {
			// Clean up JDBC resources
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}

		return vrVO;
	}

	@Override
	public void update_TxnStatus_With_Time(long customer_ID, long req_ID, String date, String status_Code,
			String old_Status_Code, String return_Data) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_TXN_STATUS_WITH_TIME);) {

			sqlLogFormat.setSql_Statement(UPDATE_TXN_STATUS_WITH_TIME);

			pstmt.setString(1, status_Code);
			pstmt.setString(2, return_Data);
			pstmt.setLong(3, customer_ID);
			pstmt.setLong(4, req_ID);
			pstmt.setString(5, old_Status_Code);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			if (count == 0) {
				throw new SQLException("Txn status has been updated by other. This request is cancelled.");
			}

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
}
