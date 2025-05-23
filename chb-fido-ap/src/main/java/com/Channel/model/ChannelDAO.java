package com.Channel.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.Common.model.BaseDAO;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.google.gson.Gson;

public class ChannelDAO extends BaseDAO implements ChannelDAO_interface {
	private static final String ADD_NEW_CHANNEL = "INSERT INTO MAP_CHANNEL(CHANNEL_CODE, CHANNEL_NAME, ACTIVATE, "
			+ "QUICK_LOGIN_PATTERN_LIMIT, QUICK_LOGIN_PIN_LIMIT, TXN_PATTERN_LIMIT, TXN_PIN_LIMIT, "
			+ "OFFLINE_OTP_lIMIT) VALUES(?,?,?,?,?,?,?,?)";
	private static final String UPDATE_CHANNEL_STATUS = "UPDATE MAP_CHANNEL SET ACTIVATE=?, LAST_MODIFIED=getdate() WHERE CHANNEL_CODE=?";
	private static final String GET_CHANNEL_LIST = "SELECT * FROM MAP_CHANNEL ORDER BY CREATE_DATE ASC";
	private static final String GET_ONE_CHANNEL = "SELECT * FROM MAP_CHANNEL WHERE CHANNEL_CODE = ?";

	private long start_Time = 0, end_Time = 0, count = 0;
	
	private static final String FALSE = "false";
	private static final String TRUE = "true";

	public ChannelDAO(String jndi, String sessID) {
		super(jndi);

		sqlLogFormat.setClazz("com.Channel.model.ChannelDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	@Deprecated
	public void addChannel(String code, String name, String mode) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(ADD_NEW_CHANNEL);) {

			sqlLogFormat.setSql_Statement(ADD_NEW_CHANNEL);

			pstmt.setString(1, code);
			pstmt.setString(2, name);
			pstmt.setString(3, mode);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("An database error occured. " + se.getMessage());
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("An database error occured. " + se.getMessage());
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
				tmp.setCHANNEL_CODE(rs.getString("CHANNEL_CODE"));
				tmp.setCHANNEL_NAME(rs.getString("CHANNEL_NAME"));
				tmp.setACTIVATE(rs.getString("ACTIVATE"));
				tmp.setQUICK_LOGIN_PATTERN_LIMIT(rs.getInt("QUICK_LOGIN_PATTERN_LIMIT"));
				tmp.setQUICK_LOGIN_PIN_LIMIT(rs.getInt("QUICK_LOGIN_PIN_LIMIT"));
				tmp.setTXN_PATTERN_LIMIT(rs.getInt("TXN_PATTERN_LIMIT"));
				tmp.setTXN_PIN_LIMIT(rs.getInt("TXN_PIN_LIMIT"));
				tmp.setOFFLINE_OTP_lIMIT(rs.getInt("OFFLINE_OTP_lIMIT"));
				tmp.setCREATE_DATE(rs.getTimestamp("CREATE_DATE"));
				tmp.setLAST_MODIFIED(rs.getTimestamp("LAST_MODIFIED"));

				data.add(tmp);
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(data));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("An database error occured. " + se.getMessage());
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
				data.setCHANNEL_CODE(rs.getString("CHANNEL_CODE"));
				data.setCHANNEL_NAME(rs.getString("CHANNEL_NAME"));
				data.setACTIVATE(rs.getString("ACTIVATE"));
				data.setQUICK_LOGIN_PATTERN_LIMIT(rs.getInt("QUICK_LOGIN_PATTERN_LIMIT"));
				data.setQUICK_LOGIN_PIN_LIMIT(rs.getInt("QUICK_LOGIN_PIN_LIMIT"));
				data.setTXN_PATTERN_LIMIT(rs.getInt("TXN_PATTERN_LIMIT"));
				data.setTXN_PIN_LIMIT(rs.getInt("TXN_PIN_LIMIT"));
				data.setOFFLINE_OTP_lIMIT(rs.getInt("OFFLINE_OTP_lIMIT"));
				data.setCREATE_DATE(rs.getTimestamp("CREATE_DATE"));
				data.setLAST_MODIFIED(rs.getTimestamp("LAST_MODIFIED"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(data));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("An database error occured. " + se.getMessage());
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
