package com.toppanidgate.idenkey.SMS_Count.model;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.Config.IDGateConfig;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class SMS_CountDAO extends BaseDAO implements SMS_CountDAO_interface {
	private static final String CREATE_ONE = "INSERT INTO SMS_COUNT(IDGATE_ID, OPERATION) VALUES(?, ?)";
	private static final String FIND_RECORD = "SELECT * FROM SMS_COUNT WHERE IDGATE_ID = ? AND OPERATION = ? AND DATEDIFF(MI, LAST_MODIFIED, GETDATE())<=10 ORDER BY ID DESC";
	private static final String FIND_RECORD_MySQL = "SELECT * FROM SMS_COUNT WHERE IDGATE_ID = ? AND OPERATION = ? AND TIMESTAMPDIFF(minute, LAST_MODIFIED, GETDATE())<=10 ORDER BY ID DESC";
	private static final String FIND_ONE_SMS_RECORD = "SELECT * FROM SMS_COUNT WHERE ID = ? AND OPERATION = '05' AND DATEDIFF(MI, LAST_MODIFIED, GETDATE())<=5 ORDER BY ID DESC";
	private static final String FIND_ONE_SMS_RECORD_MySQL = "SELECT * FROM SMS_COUNT WHERE ID = ? AND OPERATION = '05' AND TIMESTAMPDIFF(minute, LAST_MODIFIED, GETDATE())<=5 ORDER BY ID DESC";
	private static final String FIND_SUCCESS_RECORD_IN_5MIN = "SELECT * FROM SMS_COUNT WHERE IDGATE_ID = ? AND STATUS = '0' AND DATEDIFF(MI, LAST_MODIFIED, GETDATE())<=5 ORDER BY ID DESC";
	private static final String FIND_SUCCESS_RECORD_IN_5MIN_MySQL = "SELECT * FROM SMS_COUNT WHERE IDGATE_ID = ? AND STATUS = '0' AND TIMESTAMPDIFF(minute, LAST_MODIFIED, GETDATE())<=5 ORDER BY ID DESC";
	private static final String UPDATE_COUNTER = "UPDATE SMS_COUNT SET FAIL_COUNT = ?, LAST_MODIFIED = GETDATE() WHERE ID = ?";
	private static final String UPDATE_STATUS = "UPDATE SMS_COUNT SET STATUS = ?, LAST_MODIFIED = GETDATE() WHERE ID = ?";

	private long start_Time = 0, end_Time = 0, count = 0;

	public SMS_CountDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.SMS_Count.model.SMS_CountDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public long create_Record(long iDGate_ID, String operation) throws SQLException {

		long generatedKey = 0;

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(CREATE_ONE, Statement.RETURN_GENERATED_KEYS);) {
			sqlLogFormat.setSql_Statement(CREATE_ONE);

			pstmt.setLong(1, iDGate_ID);
			pstmt.setString(2, operation);

			// get execution time
			start_Time = System.currentTimeMillis();
			if (pstmt.executeUpdate() == 0) {
				throw new SQLException("Creating SMS record failed, no rows affected.");
			}

			try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					generatedKey = generatedKeys.getLong(1);
					count++;
				} else {
					throw new SQLException("Creating SMS record failed, no ID obtained.");
				}
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(generatedKey));
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
		return generatedKey;
	}

	@Override
	public SMS_CountVO find_Active_Record(long iDGate_ID, String operation) throws SQLException {

		ResultSet rs = null;
		SMS_CountVO smsVO = null;
		
		String sqlStatement = FIND_RECORD;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = FIND_RECORD_MySQL;
		} 

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setLong(1, iDGate_ID);
			pstmt.setString(2, operation);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				smsVO = new SMS_CountVO();
				smsVO.setId(rs.getLong("id"));
				smsVO.setiDGate_ID(rs.getLong("iDGate_ID"));
				smsVO.setOperation(rs.getString("Operation"));
				smsVO.setStatus(rs.getString("Status"));
				smsVO.setFail_Count(rs.getInt("Fail_Count"));
				smsVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				smsVO.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
//			sqlLogFormat.setMessage(new Gson().toJson(smsVO));
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (smsVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(smsVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", iDGate_ID);
				condition.put("Operation", operation);
				sqlLogFormat.setMessageForMap(condition);
			} 
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
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
		}

		return smsVO;
	}

	@Override
	public void update_Counter(long id, int count) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_COUNTER);) {
			sqlLogFormat.setSql_Statement(UPDATE_COUNTER);

			pstmt.setInt(1, count);
			pstmt.setLong(2, id);

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
	public void update_Status(long id, String status) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_STATUS);) {
			sqlLogFormat.setSql_Statement(UPDATE_STATUS);

			pstmt.setString(1, status);
			pstmt.setLong(2, id);

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
	public SMS_CountVO find_Lastest_5MIN_Success_Record(long iDGate_ID) throws SQLException {

		ResultSet rs = null;
		SMS_CountVO smsVO = null;
		
		String sqlStatement = FIND_SUCCESS_RECORD_IN_5MIN;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = FIND_SUCCESS_RECORD_IN_5MIN_MySQL;
		} 

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setLong(1, iDGate_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				smsVO = new SMS_CountVO();
				smsVO.setId(rs.getLong("id"));
				smsVO.setiDGate_ID(rs.getLong("iDGate_ID"));
				smsVO.setOperation(rs.getString("Operation"));
				smsVO.setStatus(rs.getString("Status"));
				smsVO.setFail_Count(rs.getInt("Fail_Count"));
				smsVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				smsVO.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(smsVO));
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
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
		}

		return smsVO;
	}

	@Override
	public SMS_CountVO get_One(long id) throws SQLException {

		ResultSet rs = null;
		SMS_CountVO smsVO = null;
		
		String sqlStatement = FIND_ONE_SMS_RECORD;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = FIND_ONE_SMS_RECORD_MySQL;
		} 

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);

			pstmt.setLong(1, id);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				smsVO = new SMS_CountVO();
				smsVO.setId(rs.getLong("id"));
				smsVO.setiDGate_ID(rs.getLong("iDGate_ID"));
				smsVO.setOperation(rs.getString("Operation"));
				smsVO.setStatus(rs.getString("Status"));
				smsVO.setFail_Count(rs.getInt("Fail_Count"));
				smsVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				smsVO.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(smsVO));
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
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
		}

		return smsVO;
	}

}
