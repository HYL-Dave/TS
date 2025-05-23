package com.toppanidgate.idenkey.Blocked_Device_Auth.model;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.Config.IDGateConfig;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class Blocked_Device_AuthDAO extends BaseDAO implements Blocked_Device_Auth_interface {
	private static final String ADD_BLOCKED_AUTH = "INSERT INTO BLOCKED_DEVICE_AUTH(DEVICE_LABEL, DEVICE_MODEL, AUTH_TYPE, BLOCKED, CHANNEL) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE ";
	private static final String REMOVE_BLOCKED_AUTH = "DELETE FROM BLOCKED_DEVICE_AUTH WHERE DEVICE_LABEL=? AND DEVICE_MODEL=? AND AUTH_TYPE=? AND CHANNEL=?";
	private static final String UPDATE_BLOCK_MODE = "UPDATE BLOCKED_DEVICE_AUTH SET BLOCKED=? WHERE DEVICE_LABEL=? AND DEVICE_MODEL=? AND AUTH_TYPE=? AND CHANNEL=?";
	private static final String LIST_ALL = "SELECT * FROM BLOCKED_DEVICE_AUTH WHERE CHANNEL=?";
	private static final String LIST_CHANNELnModel = "SELECT CHANNEL,DEVICE_MODEL FROM BLOCKED_DEVICE_AUTH WHERE UPPER(DEVICE_LABEL)=? AND CHANNEL in ('ALL', ?) "
			+ "	AND DEVICE_MODEL in ( 'ALL', ?) AND BLOCKED= 'Y' AND AUTH_TYPE in (";
	private static final String GET_ONE = "SELECT * FROM BLOCKED_DEVICE_AUTH WHERE DEVICE_LABEL=? AND DEVICE_MODEL=? AND AUTH_TYPE=? AND CHANNEL=?";
	private static final String GET_CHANNEL_LIST = "SELECT CHANNEL FROM BLOCKED_DEVICE_AUTH WHERE UPPER(DEVICE_LABEL)=? AND UPPER(DEVICE_MODEL)=? AND AUTH_TYPE=? AND BLOCKED= 'Y'";

	private long start_Time = 0, end_Time = 0, count = 0;

	public Blocked_Device_AuthDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.Blocked_Device_Auth.model.Blocked_Device_AuthDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public void addBlockedDeviceAuth(String label, String model, String type, String blocking, String channel)
			throws SQLException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection();) {
			pstmt = con.prepareStatement(GET_ONE);
			pstmt.setString(1, label);
			pstmt.setString(2, model);
			pstmt.setString(3, type);
			pstmt.setString(4, channel);

			// get execution start time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				// Update current define
				pstmt.close();
				pstmt = con.prepareStatement(UPDATE_BLOCK_MODE);

				sqlLogFormat.setSql_Statement(UPDATE_BLOCK_MODE);

				pstmt.setString(1, blocking);
				pstmt.setString(2, label);
				pstmt.setString(3, model);
				pstmt.setString(4, type);
				pstmt.setString(5, channel);

				count = pstmt.executeUpdate();

				sqlLogFormat.setResult_Count(count);
				sqlLogFormat.setMessage(String.valueOf(count));
				sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			} else {
				// Insert new row
				pstmt.close();
				pstmt = con.prepareStatement(ADD_BLOCKED_AUTH);

				sqlLogFormat.setSql_Statement(ADD_BLOCKED_AUTH);

				pstmt.setString(1, label);
				pstmt.setString(2, model);
				pstmt.setString(3, type);
				pstmt.setString(4, blocking);
				pstmt.setString(5, channel);

				count = pstmt.executeUpdate();

				sqlLogFormat.setResult_Count(count);
				sqlLogFormat.setMessage(String.valueOf(count));
				sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			}

			end_Time = System.currentTimeMillis();
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());

			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}

			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException se) {
				}
		}

	}

	@Override
	public void removeBlockedDeviceAuth(String label, String model, String type, String channel) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(REMOVE_BLOCKED_AUTH);) {
			sqlLogFormat.setSql_Statement(REMOVE_BLOCKED_AUTH);

			pstmt.setString(1, label);
			pstmt.setString(2, model);
			pstmt.setString(3, type);
			pstmt.setString(4, channel);

			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
	
	 private static String createQuery(int length) {
	        StringBuilder queryBuilder = new StringBuilder(LIST_CHANNELnModel);
	        for (int i = 0; i < length; i++) {
	            queryBuilder.append(" ?");
	            if (i != length - 1)
	                queryBuilder.append(",");
	        }
	        queryBuilder.append(")");
	        return queryBuilder.toString();
	    }

	@Override
	public List<Blocked_Device_AuthVO2> getList2(String label, String model, String type, String channel) throws SQLException {
		List<Blocked_Device_AuthVO2> result = new ArrayList<Blocked_Device_AuthVO2>();
		Blocked_Device_AuthVO2 bdaVO = null;
		ResultSet rs = null;
		
		String[] strArr = type.split(",");
		List<String> typeList = new ArrayList<>();
		typeList.add("ALL");
		typeList.addAll(Arrays.asList(strArr));
		
		String query = createQuery(typeList.size());
		
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query);) {
			sqlLogFormat.setSql_Statement(LIST_CHANNELnModel);

			pstmt.setString(1, label);
			pstmt.setString(2, channel);
			pstmt.setString(3, model);
//			pstmt.setString(2, type);
//			pstmt.setArray(2, typeArr);
//			pstmt.setArray(2, con.createArrayOf("VARCHAR", new Object[]{"ALL", "2","3"})); // 不支援此作業

			int parameterIndex = 4;
			for (String authType : typeList) {
				pstmt.setString(parameterIndex++, authType);
			}
			
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();
			end_Time = System.currentTimeMillis();

			while (rs.next()) {
				bdaVO = new Blocked_Device_AuthVO2();
				bdaVO.setChannel(rs.getString("Channel"));
				bdaVO.setDevice_Model(rs.getString("Device_Model"));

				result.add(bdaVO);
				count++;
			}

			Map<String, Object> condition = new HashMap<>();
			condition.put("Device_Label", label);
			condition.put("Auth_Type", type);
			Log4j.log.debug("[Version: {}][getList2] *** condition:{} ", IDGateConfig.svVerNo, condition);

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(result));
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}
		
		return result;
	}

	@Override
	public List<Blocked_Device_AuthVO> getList(String channel) throws SQLException {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<Blocked_Device_AuthVO> result = new ArrayList<Blocked_Device_AuthVO>();
		Blocked_Device_AuthVO bdaVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(LIST_ALL);) {

			sqlLogFormat.setSql_Statement(LIST_ALL);

			pstmt.setString(1, channel);

			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();
			end_Time = System.currentTimeMillis();

			while (rs.next()) {
				bdaVO = new Blocked_Device_AuthVO();
				bdaVO.setAuth_Type(rs.getString("Auth_Type"));
				bdaVO.setBlocked(rs.getString("Blocked"));
				bdaVO.setChannel(rs.getString("Channel"));
				bdaVO.setCreate_Date(df.format(rs.getTimestamp("Create_Date")));
				bdaVO.setDevice_Label(rs.getString("Device_Label"));
				bdaVO.setDevice_Model(rs.getString("Device_Model"));
				bdaVO.setLast_Modified(df.format(rs.getTimestamp("Last_Modified")));

				result.add(bdaVO);
				count++;
			}

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(result));
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}

		return result;
	}

	@Override
	public void updateBlockedDeviceAuth(String label, String model, String type, String blocking, String channel)
			throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_BLOCK_MODE);) {

			sqlLogFormat.setSql_Statement(UPDATE_BLOCK_MODE);

			pstmt.setString(1, blocking);
			pstmt.setString(2, label);
			pstmt.setString(3, model);
			pstmt.setString(4, type);
			pstmt.setString(5, channel);

			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
	
	@Override
	public Blocked_Device_AuthVO getOne(String label, String model, String type, String channel) throws SQLException {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Blocked_Device_AuthVO bdaVO = null;
		ResultSet rs = null;
		
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE);) {
			
			sqlLogFormat.setSql_Statement(GET_ONE);
			
			pstmt.setString(1, label);
			pstmt.setString(2, model);
			pstmt.setString(3, type);
			pstmt.setString(4, channel);
			
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();
			end_Time = System.currentTimeMillis();
			
			if (rs.next()) {
				bdaVO = new Blocked_Device_AuthVO();
				bdaVO.setAuth_Type(rs.getString("Auth_Type"));
				bdaVO.setBlocked(rs.getString("Blocked"));
				bdaVO.setChannel(rs.getString("Channel"));
				bdaVO.setCreate_Date(df.format(rs.getTimestamp("Create_Date")));
				bdaVO.setDevice_Label(rs.getString("Device_Label"));
				bdaVO.setDevice_Model(rs.getString("Device_Model"));
				bdaVO.setLast_Modified(df.format(rs.getTimestamp("Last_Modified")));
				count++;
			}
			
			sqlLogFormat.setResult_Count(count);
			if (bdaVO != null) {
				sqlLogFormat.setMessage(new Gson().toJson(bdaVO));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("Device_Label", label);
				condition.put("Device_Model", model);
				condition.put("Auth_Type", type);
				condition.put("Channel", channel);
				sqlLogFormat.setMessageForMap(condition);
			}
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			
			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}
		
		return bdaVO;
	}

	@Override
	public List<String> getChannelList(String label, String model, String type) throws SQLException {
		List<String> channeList = new ArrayList<>();
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_CHANNEL_LIST);) {

			sqlLogFormat.setSql_Statement(GET_CHANNEL_LIST);

			pstmt.setString(1, label);
			pstmt.setString(2, model);
			pstmt.setString(3, type);

			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();
			end_Time = System.currentTimeMillis();

			if (rs.next()) {
				channeList.add(rs.getString("Channel"));
				count++;
			}
			
			Map<String, Object> condition = new HashMap<>();
			condition.put("Device_Label", label);
			condition.put("Device_Model", model);
			condition.put("Auth_Type", type);
			Log4j.log.debug(
					"[Version: {}][getChannelList] *** condition:{} ",
					 IDGateConfig.svVerNo, condition);
			
			sqlLogFormat.setResult_Count(count);
			if (!channeList.isEmpty()) {
				sqlLogFormat.setMessage(new Gson().toJson(channeList));
			} else {
				sqlLogFormat.setMessageForMap(condition);
			}
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}

		return channeList;
	}

}
