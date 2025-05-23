package com.toppanidgate.idenkey.Members.model;

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
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class MembersDAO extends BaseDAO implements MembersDAO_interface {

	private static final String INSERT = "INSERT INTO MEMBERS ( BANK_ID, CUSTOMER_NAME, MOBILE_NAME, ACCOUNT, PCODE, CHANNEL_CODE, CUSTOMER_STATUS, PREF_LANG) VALUES (?,?,?,?,?,?,?,?)";
	private static final String UPDATE = "UPDATE MEMBERS SET CUSTOMER_STATUS=?, CUSTOMER_NAME=?, MOBILE_NAME=?, ACCOUNT=?, PCODE=?, PREF_LANG=?, LAST_MODIFIED=GETDATE(), BANK_ID=? WHERE IDGATE_ID=?";
//	private static final String GET_ONE_LOG = "SELECT * FROM Members_STATUS_LOG WHERE iDGate_ID=?";
	private static final String GET_ONE = "SELECT * FROM MEMBERS WHERE IDGATE_ID=?";
	private static final String GET_CUSTOMERID_BY_BankID = "SELECT * FROM MEMBERS WHERE BANK_ID=? ORDER BY CREATE_DATE DESC";
	private static final String GET_BY_CUSTOMERACCOUNT = "SELECT * FROM MEMBERS WHERE PCODE=? AND CHANNEL_CODE=? ORDER BY CREATE_DATE DESC";
	private static final String COUNT_ACCOUNTS = "SELECT COUNT(*) FROM MEMBERS WHERE PCODE=? AND CUSTOMER_STATUS != 9";
	private static final String UPDATE_PREF_LANG = "UPDATE MEMBERS SET PREF_LANG = ? WHERE IDGATE_ID = ?";
	private static final String ADD_MEMBER_STATUS_LOG = "INSERT INTO MEMBERS_STATUS_LOG(IDGATE_ID, PREVIOUS_STATUS, NEW_STATUS, REASON) VALUES(?,?,?,?)";
	private static final String DISABLE_MEMBER_UNDER_BANKID = "UPDATE MEMBERS SET CUSTOMER_STATUS='9', LAST_MODIFIED=GETDATE() WHERE BANK_ID=?";
	private static final String UPDATE_DIGITAL_FAILS = "UPDATE MEMBERS SET DIGITAL_FAILS = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_PATTERN_FAILS = "UPDATE MEMBERS SET PATTERN_FAILS = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_AUTH_FAILS = "UPDATE MEMBERS SET AUTH_FAILS = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_MSG_COUNT = "UPDATE MEMBERS SET MSG_COUNT = ? WHERE IDGATE_ID = ?";

	private long start_Time = 0, end_Time = 0, count = 0;

	public MembersDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.Members.model.MembersDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public long insert(MembersVO membersVO) throws SQLException {

		long generatedKey = 0;

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);) {
			sqlLogFormat.setSql_Statement(INSERT);

			pstmt.setString(1, membersVO.get_BankID());
			pstmt.setString(2, membersVO.getCustomer_Name());
			pstmt.setString(3, membersVO.getMobile_Name());
			pstmt.setString(4, membersVO.getAccount());
			pstmt.setString(5, membersVO.getPcode());
			pstmt.setString(6, membersVO.getChannel_Code());
			pstmt.setString(7, membersVO.getCustomer_Status());
			pstmt.setString(8, membersVO.getPref_Lang());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();
			if (count == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					generatedKey = generatedKeys.getLong(1);
				} else {
					throw new SQLException("Creating user failed, no ID obtained.");
				}
			}

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
	public void update(MembersVO membersVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE);) {
			sqlLogFormat.setSql_Statement(UPDATE);

			pstmt.setString(1, membersVO.getCustomer_Status());
			pstmt.setString(2, membersVO.getCustomer_Name());
			pstmt.setString(3, membersVO.getMobile_Name());
			pstmt.setString(4, membersVO.getAccount());
			pstmt.setString(5, membersVO.getPcode());
			pstmt.setString(6, membersVO.getPref_Lang());
			pstmt.setString(7, membersVO.get_BankID());
			pstmt.setLong(8, membersVO.getiDGate_ID());

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
			// Handle any driver errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
	
	@Override
	public MembersLogVO findPrvStatusByCustomerID(long customer_ID) throws SQLException {
		
		MembersLogVO MembersLogVO = null;
		ResultSet rs = null;
		
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE);) {
			sqlLogFormat.setSql_Statement(GET_ONE);
			
			pstmt.setLong(1, customer_ID);
			
			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				MembersLogVO = new MembersLogVO();
				MembersLogVO.setiDGateID(customer_ID);
				MembersLogVO.setPrevious_Status(rs.getString("Previous_Status"));
				count++;
			}
			
			end_Time = System.currentTimeMillis();
			
			sqlLogFormat.setResult_Count(count);
			
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (MembersLogVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(MembersLogVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessage(new Gson().toJson(MembersLogVO));
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
		return MembersLogVO;
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
				membersVO.setiDGate_ID(customer_ID);
				membersVO.setBank_ID(rs.getString("Bank_ID"));
				membersVO.setCustomer_Name(rs.getString("Customer_Name"));
				membersVO.setMobile_Name(rs.getString("Mobile_Name"));
				membersVO.setAccount(rs.getString("Account"));
				membersVO.setPcode(rs.getString("Pcode"));
				membersVO.setChannel_Code(rs.getString("Channel_Code"));
				membersVO.setCustomer_Status(rs.getString("Customer_Status"));
				membersVO.setMsg_Count(rs.getInt("Msg_Count"));
				membersVO.setPattern_Fails(rs.getInt("Pattern_Fails"));
				membersVO.setDigital_Fails(rs.getInt("Digital_Fails"));
				membersVO.setAuth_Fails(rs.getInt("Auth_Fails"));
				membersVO.setPref_Lang(rs.getString("Pref_Lang"));
				membersVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				membersVO.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (membersVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(membersVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessage(new Gson().toJson(membersVO));
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
		return membersVO;
	}

	@Override
	public MembersVO findCustomerIDByBankID(String bankID) throws SQLException {

		MembersVO membersVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(GET_CUSTOMERID_BY_BankID);) {
			sqlLogFormat.setSql_Statement(GET_CUSTOMERID_BY_BankID);
			
			
			pstmt.setString(1, bankID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				membersVO = new MembersVO();
				membersVO.setiDGate_ID(rs.getLong("iDGate_ID"));
				membersVO.setBank_ID(rs.getString("Bank_ID"));
				membersVO.setCustomer_Name(rs.getString("Customer_Name"));
				membersVO.setMobile_Name(rs.getString("Mobile_Name"));
				membersVO.setAccount(rs.getString("Account"));
				membersVO.setPcode(rs.getString("Pcode"));
				membersVO.setChannel_Code(rs.getString("Channel_Code"));
				membersVO.setCustomer_Status(rs.getString("Customer_Status"));
				membersVO.setPref_Lang(rs.getString("Pref_Lang"));
				membersVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				membersVO.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(membersVO));
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
			pstmt.setString(2, signup_Type);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				membersVO = new MembersVO();
				membersVO.setiDGate_ID(rs.getLong("Customer_ID"));
				membersVO.setCustomer_Name(rs.getString("Customer_Name"));
				membersVO.setMobile_Name(rs.getString("Mobile_Name"));
				membersVO.setPcode(rs.getString("Pcode"));
				membersVO.setChannel_Code(rs.getString("Channel_Code"));
				membersVO.setCustomer_Status(rs.getString("Customer_Status"));
				membersVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				membersVO.setLast_Modified(rs.getTimestamp("Last_Modified"));
				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(membersVO));
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
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
	public void disableMemberUnderBankID(String bank_ID) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(DISABLE_MEMBER_UNDER_BANKID);) {
			sqlLogFormat.setSql_Statement(DISABLE_MEMBER_UNDER_BANKID);
			
			pstmt.setString(1, bank_ID);

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
	public void updateDigitalFailCounter(long customer_ID, int counter) throws SQLException {
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_DIGITAL_FAILS);) {
			sqlLogFormat.setSql_Statement(UPDATE_DIGITAL_FAILS);

			pstmt.setInt(1, counter);
			pstmt.setLong(2, customer_ID);

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
	public void updateAuthFailCounter(long customer_ID, int counter) throws SQLException {
		
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_AUTH_FAILS);) {
			sqlLogFormat.setSql_Statement(UPDATE_AUTH_FAILS);
			
			
			pstmt.setInt(1, counter);
			pstmt.setLong(2, customer_ID);
			
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
	public void updatePatternFailCounter(long customer_ID, int counter) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_PATTERN_FAILS);) {
			sqlLogFormat.setSql_Statement(UPDATE_PATTERN_FAILS);
			
			
			pstmt.setInt(1, counter);
			pstmt.setLong(2, customer_ID);

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
}