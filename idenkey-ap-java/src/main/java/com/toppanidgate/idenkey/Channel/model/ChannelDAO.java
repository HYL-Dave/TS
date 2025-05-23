package com.toppanidgate.idenkey.Channel.model;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class ChannelDAO extends BaseDAO implements ChannelDAO_interface {
	private static final String ADD_NEW_CHANNEL = "INSERT INTO Channel(Channel_Code, Channel_Name, Activate, JNDI) VALUES(?,?,?,?)";
	private static final String UPDATE_CHANNEL_STATUS = "UPDATE Channel SET Activate=?, Last_Modified=getdate() WHERE Channel_Code=?";
	private static final String GET_CHANNEL_LIST = "SELECT * FROM Channel ORDER BY Create_Date ASC";
	private static final String GET_ONE_CHANNEL = "SELECT * FROM Channel WHERE Channel_Code = ?";

	private long start_Time = 0, end_Time = 0, count = 0;

	public ChannelDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.Channel.model.ChannelDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public void addChannel(String code, String name, String mode, String jndi) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(ADD_NEW_CHANNEL);) {

			sqlLogFormat.setSql_Statement(ADD_NEW_CHANNEL);

			pstmt.setString(1, code);
			pstmt.setString(2, name);
			pstmt.setString(3, mode);
			pstmt.setString(4, jndi);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateChannelMode(String code, String mode) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_CHANNEL_STATUS);) {

			sqlLogFormat.setSql_Statement(UPDATE_CHANNEL_STATUS);

			pstmt.setString(1, mode);
			pstmt.setString(2, code);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public List<ChannelVO> getChannelList() throws SQLException {
		List<ChannelVO> data = new ArrayList<ChannelVO>();
		ChannelVO tmp;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_CHANNEL_LIST);) {
			sqlLogFormat.setSql_Statement(GET_CHANNEL_LIST);

			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				tmp = new ChannelVO();
				tmp.setChannel(rs.getString("Channel_Code"));
				tmp.setChannelName(rs.getString("Channel_Name"));
				tmp.setActivate(rs.getString("Activate"));
				tmp.setJNDI(rs.getString("JNDI"));
				tmp.setCreate_Date(rs.getTimestamp("Create_Date"));
				tmp.setLast_Modified(rs.getTimestamp("Last_Modified"));

				data.add(tmp);
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(data));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}

		return data;
	}

	@Override
	public ChannelVO getChannel(String code) throws SQLException {
		ChannelVO data = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE_CHANNEL);) {
			sqlLogFormat.setSql_Statement(GET_ONE_CHANNEL);
			
			pstmt.setString(1, code);

			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				data = new ChannelVO();
				data.setChannel(rs.getString("Channel_Code"));
				data.setChannelName(rs.getString("Channel_Name"));
				data.setActivate(rs.getString("Activate"));
				data.setJNDI(rs.getString("JNDI"));
				data.setCreate_Date(rs.getTimestamp("Create_Date"));
				data.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			if (data != null) {
				sqlLogFormat.setMessageForMap(new Gson().fromJson(new Gson().toJson(data), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("Channel_Code", code);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessageForMap(new Gson().toJson(data));
//			sqlLogFormat.setMessage(new Gson().toJson(data));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}

		return data;
	}
}
