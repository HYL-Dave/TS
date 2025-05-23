package com.Members.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.Common.model.BaseDAO;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.Common.model.MemberStatus;
import com.google.gson.Gson;

public class MembersDAO extends BaseDAO implements MembersDAO_interface {

	private static final String INSERT = "INSERT INTO MAP_MEMBERS ( DEVICE_ID, CUSTOMER_NAME, MOBILE_PHONE, EMAIL, CHANNEL_CODE, CUSTOMER_STATUS, PREF_LANG, IDGATE_ID, Verify_Type) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE = "UPDATE MAP_MEMBERS SET CUSTOMER_STATUS=?, CUSTOMER_NAME=?, MOBILE_PHONE=?, EMAIL=?, PREF_LANG=?, LAST_MODIFIED=GETDATE() WHERE IDGATE_ID=?";
	private static final String UPDATE_DEVICE_ID = "UPDATE MAP_MEMBERS SET DEVICE_ID = ? WHERE IDGATE_ID=?";
	private static final String GET_ONE = "SELECT * FROM MAP_MEMBERS WHERE IDGATE_ID=?";
	private static final String GET_CUSTOMERID_BY_DeviceID = "SELECT * FROM MAP_MEMBERS WHERE DEVICE_ID=? ORDER BY CREATE_DATE DESC";
	private static final String GET_BY_CUSTOMERACCOUNT = "SELECT * FROM MAP_MEMBERS WHERE DEVICE_ID=? ORDER BY CREATE_DATE DESC";
	private static final String COUNT_ACCOUNTS = "SELECT COUNT(*) FROM MAP_MEMBERS WHERE DEVICE_ID=? AND CUSTOMER_STATUS != 9";
	private static final String UPDATE_PREF_LANG = "UPDATE MAP_MEMBERS SET PREF_LANG = ? WHERE IDGATE_ID = ?";
	private static final String ADD_MEMBER_STATUS_LOG = "INSERT INTO MAP_MEMBERS_STATUS_LOG(IDGATE_ID, Previous_Status, New_Status, Reason) VALUES(?,?,?,?)";
	private static final String DISABLE_MEMBER_UNDER_DEVICE_ID = "UPDATE MAP_MEMBERS SET CUSTOMER_STATUS='"
			+ MemberStatus.Deleted + "', LAST_MODIFIED=GETDATE() WHERE DEVICE_ID=?";
	private static final String UPDATE_MEMBER_STATUS_UNDER_DEVICE_ID = "UPDATE MAP_MEMBERS SET CUSTOMER_STATUS=?, LAST_MODIFIED=GETDATE() WHERE DEVICE_ID=? AND CUSTOMER_STATUS <> '"
			+ MemberStatus.Deleted + "'";
	private static final String DISABLE_ONE_MEMBER_ID = "UPDATE MAP_MEMBERS SET CUSTOMER_STATUS='"
			+ MemberStatus.Deleted + "', LAST_MODIFIED=GETDATE() WHERE IDGATE_ID=?";
	private static final String UPDATE_TXN_FAILS = "UPDATE MAP_MEMBERS SET TXN_AUTH_FAILS = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_LOGIN_FAILS = "UPDATE MAP_MEMBERS SET LOGIN_AUTH_FAILS = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_OFFLINE_FAILS = "UPDATE MAP_MEMBERS SET OFFLINE_AUTH_FAILS = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_MSG_COUNT = "UPDATE MAP_MEMBERS SET MSG_COUNT = ? WHERE IDGATE_ID = ?";

	private long start_Time = 0, end_Time = 0, count = 0;

	private static final String FALSE = "false";
	private static final String TRUE = "true";

	public MembersDAO(String jndi, String sessID) {
		super(jndi);

		sqlLogFormat.setClazz("com.Members.model.MembersDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public long insert(MembersVO membersVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(INSERT);) {
			sqlLogFormat.setSql_Statement(INSERT);

			pstmt.setString(1, membersVO.getDevice_ID());
			pstmt.setString(2, membersVO.getCustomer_Name());
			pstmt.setString(3, membersVO.getMobile_Phone());
			pstmt.setString(4, membersVO.getEmail());
			pstmt.setString(5, membersVO.getChannel_Code());
			pstmt.setString(6, membersVO.getCustomer_Status());
			pstmt.setString(7, membersVO.getPref_Lang());
			pstmt.setLong(8, membersVO.getIDGateID());
			pstmt.setString(9, membersVO.getVerify_Type());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();
			if (count == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}

		return membersVO.getIDGateID();
	}

	@Override
	public void update(MembersVO membersVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE);) {
			sqlLogFormat.setSql_Statement(UPDATE);

			pstmt.setString(1, membersVO.getCustomer_Status());
			pstmt.setString(2, membersVO.getCustomer_Name());
			pstmt.setString(3, membersVO.getMobile_Phone());
			pstmt.setString(4, membersVO.getEmail());
			pstmt.setString(5, membersVO.getPref_Lang());
			pstmt.setLong(6, membersVO.getIDGateID());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any driver errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public MembersVO findByCustomerID(long customer_ID) throws SQLException {

		MembersVO membersVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE);) {
			sqlLogFormat.setSql_Statement(GET_ONE);

			pstmt.setLong(1, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				membersVO = new MembersVO();
				membersVO.setIDGateID(customer_ID);
				membersVO.setDevice_ID(rs.getString("DEVICE_ID"));
				membersVO.setCustomer_Name(rs.getString("CUSTOMER_NAME"));
				membersVO.setMobile_Phone(rs.getString("MOBILE_PHONE"));
				membersVO.setEmail(rs.getString("EMAIL"));
				membersVO.setChannel_Code(rs.getString("Channel_Code"));
				membersVO.setCustomer_Status(rs.getString("CUSTOMER_STATUS"));
				membersVO.setVerify_Type(rs.getString("Verify_Type"));
				membersVO.setMsg_Count(rs.getInt("MSG_COUNT"));
				membersVO.setTxn_Auth_Fails(rs.getInt("TXN_AUTH_FAILS"));
				membersVO.setLogin_Auth_Fails(rs.getInt("LOGIN_AUTH_FAILS"));
				membersVO.setOffline_Auth_Fails(rs.getInt("OFFLINE_AUTH_FAILS"));
				membersVO.setPref_Lang(rs.getString("PREF_LANG"));
				membersVO.setCreate_Date(rs.getTimestamp("CREATE_DATE"));
				membersVO.setLast_Modified(rs.getTimestamp("LAST_MODIFIED"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(membersVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
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
		return membersVO;
	}

	@Override
	public MembersVO findCustomerIDByBankID(String bankID) throws SQLException {

		MembersVO membersVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(GET_CUSTOMERID_BY_DeviceID);) {
			sqlLogFormat.setSql_Statement(GET_CUSTOMERID_BY_DeviceID);

			pstmt.setString(1, bankID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				membersVO = new MembersVO();
				membersVO.setIDGateID(rs.getLong("IDGATE_ID"));
				membersVO.setDevice_ID(rs.getString("DEVICE_ID"));
				membersVO.setCustomer_Name(rs.getString("CUSTOMER_NAME"));
				membersVO.setMobile_Phone(rs.getString("MOBILE_PHONE"));
				membersVO.setEmail(rs.getString("EMAIL"));
				membersVO.setVerify_Type(rs.getString("Verify_Type"));
				membersVO.setChannel_Code(rs.getString("Channel_Code"));
				membersVO.setCustomer_Status(rs.getString("CUSTOMER_STATUS"));
				membersVO.setPref_Lang(rs.getString("PREF_LANG"));
				membersVO.setCreate_Date(rs.getTimestamp("CREATE_DATE"));
				membersVO.setLast_Modified(rs.getTimestamp("LAST_MODIFIED"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(membersVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
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
		return membersVO;
	}

	@Override
	public MembersVO findByCustomerAccount(String customer_ID, String signup_Type) throws SQLException {

		MembersVO membersVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(GET_BY_CUSTOMERACCOUNT);) {
			sqlLogFormat.setSql_Statement(GET_BY_CUSTOMERACCOUNT);

			pstmt.setString(1, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				membersVO = new MembersVO();
				membersVO.setIDGateID(rs.getLong("Customer_ID"));
				membersVO.setCustomer_Name(rs.getString("CUSTOMER_NAME"));
				membersVO.setMobile_Phone(rs.getString("MOBILE_PHONE"));
				membersVO.setChannel_Code(rs.getString("Channel_Code"));
				membersVO.setCustomer_Status(rs.getString("CUSTOMER_STATUS"));
				membersVO.setVerify_Type(rs.getString("Verify_Type"));
				membersVO.setCreate_Date(rs.getTimestamp("CREATE_DATE"));
				membersVO.setLast_Modified(rs.getTimestamp("LAST_MODIFIED"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(membersVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
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
		return membersVO;
	}

	public int findCustomerAccountCount(String customer_Account) throws SQLException {

		ResultSet rs = null;
		int result = 0;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(COUNT_ACCOUNTS);) {
			sqlLogFormat.setSql_Statement(COUNT_ACCOUNTS);

			pstmt.setString(1, customer_Account);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				result = rs.getInt(1);
			}

			count = result;
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
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
		return result;
	}

	@Override
	public void updateMemberLang(long idgate_ID, String lang) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_PREF_LANG);) {
			sqlLogFormat.setSql_Statement(UPDATE_PREF_LANG);

			pstmt.setString(1, lang);
			pstmt.setLong(2, idgate_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	public void addMemberStatusLog(long idgate_ID, String prevStatus, String newStatus, String reason)
			throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(ADD_MEMBER_STATUS_LOG);) {
			sqlLogFormat.setSql_Statement(ADD_MEMBER_STATUS_LOG);

			pstmt.setLong(1, idgate_ID);
			pstmt.setString(2, prevStatus);
			pstmt.setString(3, newStatus);
			pstmt.setString(4, reason);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void disableMemberUnderBankID(String DEVICE_ID) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(DISABLE_MEMBER_UNDER_DEVICE_ID);) {
			sqlLogFormat.setSql_Statement(DISABLE_MEMBER_UNDER_DEVICE_ID);

			pstmt.setString(1, DEVICE_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateTxnAuthFails(long customer_ID, int counter) throws SQLException {
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_TXN_FAILS);) {
			sqlLogFormat.setSql_Statement(UPDATE_TXN_FAILS);

			pstmt.setInt(1, counter);
			pstmt.setLong(2, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateLoginAuthFails(long customer_ID, int counter) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_LOGIN_FAILS);) {
			sqlLogFormat.setSql_Statement(UPDATE_LOGIN_FAILS);

			pstmt.setInt(1, counter);
			pstmt.setLong(2, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateMsgCounter(long customer_ID, long counter) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_MSG_COUNT);) {
			sqlLogFormat.setSql_Statement(UPDATE_MSG_COUNT);

			pstmt.setLong(1, counter);
			pstmt.setLong(2, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateOfflineAuthFails(long customer_ID, int counter) throws SQLException {
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_OFFLINE_FAILS);) {
			sqlLogFormat.setSql_Statement(UPDATE_OFFLINE_FAILS);

			pstmt.setInt(1, counter);
			pstmt.setLong(2, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateDeviceID(long customer_ID, String deviceID) throws SQLException {
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_DEVICE_ID);) {
			sqlLogFormat.setSql_Statement(UPDATE_DEVICE_ID);

			pstmt.setString(1, deviceID);
			pstmt.setLong(2, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any driver errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void disableOneMemberID(long idgate_ID) throws SQLException {
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(DISABLE_ONE_MEMBER_ID);) {
			sqlLogFormat.setSql_Statement(DISABLE_ONE_MEMBER_ID);

			pstmt.setLong(1, idgate_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any driver errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateMemberUnderBankID(String deviceID, String status) throws SQLException {
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_MEMBER_STATUS_UNDER_DEVICE_ID);) {
			sqlLogFormat.setSql_Statement(UPDATE_MEMBER_STATUS_UNDER_DEVICE_ID);

			pstmt.setString(1, status);
			pstmt.setString(2, deviceID);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			// Handle any driver errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
}