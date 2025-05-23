package com.Cvn.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.Cvn.Config.Cfg;
import com.TrustServer.Func.TS_MainFunc;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.common.model.ReturnCode;

// Author: Calvin @ iDGate.com
public class IDGateOracle extends BaseDAO {
	private static final Logger logger = LogManager.getLogger(IDGateOracle.class);
	
	private static final String CREATE_DEVICE = "INSERT INTO DEVICES (USERID, ESN, DEVDATA, MERCURY, STATUS, DEVID, CHKCOUNT,XPH) VALUES(?, ?, ?, ?, ?, ?, 0, ?)";
	private static final String CREATE_PROTECTOR = "INSERT INTO PROTECTORS (PROTECTOR, VALUE1, VALUE2) VALUES(?, ?, ?)";
//	private static final String CREATE_TXN = "INSERT INTO TXNS (CHANNEL, USERID, ESN, TXNID, TXNDATA, OTPH, CHKTIMESTAMP, STATUS, AUTHTYPE) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
//	private static final String DELETE_DEVICE = "DELETE FROM DEVICES WHERE ESN = ?";
//	private static final String DELETE_PROTECTOR = "DELETE FROM PROTECTORS WHERE PROTECTOR = ?";
	private static final String GET_DEVICE_ESN = "SELECT * FROM DEVICES WHERE ESN = ?";
	private static final String GET_DEVICE_USERID = "SELECT * FROM DEVICES WHERE USERID = ?";
	private static final String GET_NEW_ESN = "EXEC USP_NEXTVAL @SEQ_NAME = 'ESNTAIL';";	// MSSQL
//	private static final String GET_NEW_ESN = "SELECT NEXTVAL('ESNTAIL');";	// MySQL
	private static final String GET_PROTECTOR = "SELECT * FROM PROTECTORS WHERE (PROTECTOR = P_PROTECTOR);";
//	private static final String GET_TXN = "SELECT * FROM TXNS WHERE USERID = ? AND TXNID = ? AND CHANNEL = ?";
//	private static final String GET_TXM_LIST = "SELECT * FROM TXNS WHERE (USERID = ? AND CHANNEL = ? AND TXNDATE > SYSDATE-7)";
	private static final String SET_DEVICE = "UPDATE DEVICES SET ESN = ?, DEVDATA = ?, MERCURY = ?, STATUS = ?, DEVID = ?, CHKCOUNT = 0, XPH = ? WHERE USERID = ?";
//	private static final String SET_DEVICE_AUTH_TYPE = "UPDATE DEVICES SET AUTHTYPE = P_AUTHTYPE WHERE ESN = ?";
	private static final String SET_DEVICE_MERCURY = "UPDATE DEVICES SET MERCURY = ?, CHKCOUNT = ? WHERE ESN = ?";
	private static final String SET_DEVICE_STATUS = "UPDATE DEVICES SET STATUS = ?, MODIFIED = CURDATE() WHERE ESN = ?";
//	private static final String SET_TXN_STATUS = "UPDATE TXNS SET STATUS = P_STATUS WHERE CHANNEL = ? AND USERID =? AND TXNID = ?";
	private static final String STORE_KEY = "INSERT INTO KEYSTORE(ALIAS, PRIV_KEY, PUB_KEY) VALUES(?,?,?)";
	private static final String FETCH_KEY = "SELECT * FROM KEYSTORE WHERE ALIAS=?";
	private static final String ADD_CHANNEL = "INSERT INTO CHANNEL_TRUST(CHANNEL_ID, CHANNEL_NAME, OTP_LENGTH, OTP_RANGE, OTP_INTERVAL) VALUES(?,?,?,?,?)";
	private static final String GET_CHANNEL_ByID = "SELECT * FROM CHANNEL_TRUST WHERE CHANNEL_ID=?";
	private static final String GET_CHANNEL_ByName = "SELECT * FROM CHANNEL_TRUST WHERE CHANNEL_NAME=?";
	private static final String GET_CHANNEL_LIST = "SELECT * FROM CHANNEL_TRUST";

	/**
	 * Connecting with jdbc connection string.
	 * 
	 * @param sessionId Session ID info.
	 * @return valid connection object or exception.
	 */
	public static Connection getNewConnection(String sessionId) throws ClassNotFoundException, SQLException {

		String db_direct = Cfg.getExternalCfgValue("DBdirect");
		if (db_direct.equals("Y")) {

			String db_JdbcName = Cfg.getExternalCfgValue("JdbcName");
			String db_user = Cfg.getExternalCfgValue("DBuser");
			String db_pwd = Cfg.getExternalCfgValue("DBpass");
			String connURL = "";

			connURL = db_JdbcName + "/" + Cfg.getExternalCfgValue("DBname");

			try {
				Class.forName(Cfg.getExternalCfgValue("DBclassName"));
				Connection new_connection = DriverManager.getConnection(connURL, db_user, db_pwd);
				// logger.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGateMySQL] - #Direct-connection activated.");
				return new_connection;
			} catch (ClassNotFoundException | SQLException ex) {
				logger.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGateMySQL] - #Direct-connection init failed.(" + ex.getMessage() + ")");
				throw ex;
			}
		} else {
			return getDataSrc().getConnection();
		}
	}

	public static String getNewEsnSeq(String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(GET_NEW_ESN);) {

			rs = pStmt.executeQuery();

			if (rs.next()) {
				String newEsnSeq = String.valueOf(rs.getLong(1));
				// logger.info("*** newEsnSeq:" + newEsnSeq);
				return newEsnSeq;
			} else {
				return "";
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getNewEsnSeq] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static String createDevice(String userID, String esn, String devData, String mercury, String status,
			String devID, String xPH, String sessionId) {

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(CREATE_DEVICE);) {

			pStmt.setString(1, userID);
			pStmt.setString(2, esn);
			pStmt.setString(3, devData);
			pStmt.setString(4, mercury);
			pStmt.setString(5, status);
			pStmt.setString(6, devID);
			pStmt.setString(7, xPH);
			pStmt.execute();

			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-createDevice] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

	public static String[] getDevice_UserID(String userID, String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement callStmt = conn.prepareStatement(GET_DEVICE_USERID);) {

			callStmt.setString(1, userID);
			callStmt.execute();
			rs = callStmt.getResultSet();

			if (rs.next()) {
				String[] rsp = { rs.getString("ESN"), rs.getString("DevData"), rs.getString("Mercury"),
						rs.getString("ErrCount"), rs.getString("ErrMax"), rs.getString("Status"),
						rs.getString("DevID") };
				return rsp;

			} else {
				return new String[] { "" };
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getDevice_UserID] - #DB Error occurred: " + e.getMessage());
			return new String[] { e.getMessage() };
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static String[] getDevice_Esn(String esn, String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement callStmt = conn.prepareStatement(GET_DEVICE_ESN);) {

			callStmt.setString(1, esn);
			callStmt.execute();
			rs = callStmt.getResultSet();

			if (rs.next()) {
				String[] rsp = { rs.getString("UserID"), rs.getString("DevData"), rs.getString("Mercury"),
						rs.getString("ErrCount"), rs.getString("ErrMax"), rs.getString("Status"),
						rs.getString("DevID") };
				return rsp;

			} else {
				return new String[] { "" };
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getDevice_Esn] - #DB Error occurred: " + e.getMessage());
			return new String[] { e.getMessage() };
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static String updateDevice(String userID, String esn, String devData, String mercury, String status,
			String devID, String xPH, String sessionId) {

		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement callStmt = conn.prepareStatement(SET_DEVICE);) {

			callStmt.setString(1, userID);
			callStmt.setString(2, esn);
			callStmt.setString(3, devData);
			callStmt.setString(4, mercury);
			callStmt.setString(5, status);
			callStmt.setString(6, devID);
			callStmt.setString(7, xPH);
			callStmt.execute();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-updateDevice] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static String updateDevice_Mercury(String esn, String mercury, int newChkCount, String sessionId) {

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement callStmt = conn.prepareStatement(SET_DEVICE_MERCURY);) {

			callStmt.setString(1, mercury);
			callStmt.setInt(2, newChkCount);
			callStmt.setString(3, esn);
			callStmt.execute();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-updateDevice_Mercury] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

	public static String updateDevice_Status(String esn, String status, String sessionId) {

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement callStmt = conn.prepareStatement(SET_DEVICE_STATUS);) {

			callStmt.setString(1, esn);
			callStmt.setString(2, status);
			callStmt.execute();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-updateDevice_Status] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

//	public static String createTxn(String esn, String txnID, String otpH, String chkTimeStamp, String status,
//			String sessionId) {
//
//		try(Connection conn = getNewConnection(sessionId); 
//			CallableStatement callStmt = conn.prepareCall("{call usp_createTxn(?,?,?,?,?)}");) {
//			
//			callStmt.setString(1, esn);
//			callStmt.setString(2, txnID);
//			callStmt.setString(3, otpH);
//			callStmt.setString(4, chkTimeStamp);
//			callStmt.setString(5, status);
//			callStmt.execute();
//			return "0";
//		} catch (SQLException | ClassNotFoundException e) {
//			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "] "[iDGate-createTxn] - #DB Error occurred: " + e.getMessage());
//			return e.getMessage();
//		}
//	}
//
//	public static ResultSet getTxn(String esn, String txnID, String sessionId) {
//		ResultSet rs = null;
//
//		try (Connection conn = getNewConnection(sessionId);
//			 CallableStatement callStmt = conn.prepareCall("{call usp_getTxn(?,?)}");) {
//
//			callStmt.setString(1, esn);
//			callStmt.setString(2, txnID);
//			callStmt.execute();
//			rs = callStmt.getResultSet();
//			return rs;
//		} catch (SQLException | ClassNotFoundException e) {
//			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "] "[iDGate-getTxn] - #DB Error occurred: " + e.getMessage());
//			return null;
//		} finally {
//			if (rs != null) {
//				try {
//					rs.close();
//				} catch (SQLException e) {
//				}
//			}
//		}
//	}

	public static String createProtector(String protectorName, String value1, String value2, String sessionId) {
		/**
		 * preparedStatement.setString() truncate string is its lenght > 254.
		 *
		 * It seems that JdbcOdbc bridge are convert setString() to SQL_CHAR which has a
		 * length limit of 254. According to JDBC, the preparedStatement.setString(int
		 * parameterIndex, String x) set a parameter to a Java String value. The driver
		 * should convert this to a SQL VARCHAR or LONGVARCHAR value,
		 */

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pstmt = conn.prepareStatement(CREATE_PROTECTOR);) {
			pstmt.setString(1, protectorName);
			pstmt.setString(2, value1);
			pstmt.setString(3, value2);
			pstmt.executeUpdate();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-createProtector] - #DB Error occurred: " + e.getMessage());
			return "";
		}
	}

	public static String[] getProtector(String protectorName, String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement callStmt = conn.prepareStatement(GET_PROTECTOR)) {

			callStmt.setString(1, protectorName);
			callStmt.execute();
			rs = callStmt.getResultSet();

			if (rs.next()) {
				String[] rsp = { rs.getString("Value1"), rs.getString("Value2") };
				return rsp;

			} else {
				return new String[] { "", "--" };
			}

		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getProtector] - #DB Error occurred: " + e.getMessage());
			return new String[] { "", e.getMessage() };
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * 公私鑰存 DB
	 * @param alias
	 * @param privKey
	 * @param pubKey
	 * @param sessionId
	 * @return
	 */
	public static String storeKey(String alias, String privKey, String pubKey, String sessionId) {
		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(STORE_KEY);) {

			pStmt.setString(1, alias);
			pStmt.setString(2, privKey);
			pStmt.setString(3, pubKey);
			pStmt.execute();

			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-storeKey] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

	public static String[] fetchKey(String alias, String sessionId) {
		ResultSet rs = null;
		String[] key = null;
		
		long start_Time = 0, end_Time = 0, count = 0;
		sqlLogFormat.setSql_Statement(FETCH_KEY);
		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(FETCH_KEY);) {
			// get execution time
			start_Time = System.currentTimeMillis();
			pStmt.setString(1, alias);
			rs = pStmt.executeQuery();

			if (rs.next()) {
				key = new String[2];
				key[0] = rs.getString("Priv_Key");
				key[1] = rs.getString("Pub_Key");
				count++;
			}
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(alias);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			return key;
		} catch (SQLException | ClassNotFoundException e) {
//			Log4jSQL.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-fetchKey] - #DB Error occurred: " + e.getMessage());
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(e.getMessage());
			sqlLogFormat.setMessage(alias);
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			return new String[] { "Error", e.getMessage() };
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static HashMap<String, Object> addChannel(String id, String name, int otpLength, int otpRange,
			int otpInterval, String sessionId) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(ADD_CHANNEL);) {

			pStmt.setString(1, id);
			pStmt.setString(2, name);
			pStmt.setInt(3, otpLength);
			pStmt.setInt(4, otpRange);
			pStmt.setInt(5, otpInterval);
			pStmt.execute();

			result.put("ReturnCode", ReturnCode.Success);
			result.put("ReturnMsg", "Success");
			return result;
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-addChannel] - #DB Error occurred: " + e.getMessage());
			result.put("ReturnCode", "0008");
			result.put("ReturnMsg", e.getMessage());
			return result;
		}
	}

	public static HashMap<String, Object> getChannelById(String id, String sessionId) {
		ResultSet rs = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(GET_CHANNEL_ByID);) {

			pStmt.setString(1, id);
			rs = pStmt.executeQuery();

			if (rs.next()) {
				result.put("OTP_Length", rs.getInt("OTP_Length"));
				result.put("OTP_Range", rs.getInt("OTP_Range"));
				result.put("OTP_Interval", rs.getInt("OTP_Interval"));
			} else {
				result.put("No_Data", true);
			}

			result.put("ReturnCode", ReturnCode.Success);
			result.put("ReturnMsg", "Success");
			return result;
		} catch (SQLException | ClassNotFoundException e) {
			logger.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getChannelById] - #DB Error occurred: " + e.getMessage());
			result = new HashMap<String, Object>();
			result.put("ReturnCode", "0008");
			result.put("ReturnMsg", e.getMessage());
			return result;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static HashMap<String, Object> getChannelByName(String name, String sessionId) {
		ResultSet rs = null;
		HashMap<String, Object> result = new HashMap<String, Object>();

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(GET_CHANNEL_ByName);) {

			pStmt.setString(1, name);
			rs = pStmt.executeQuery();

			if (rs.next()) {
				result.put("OTP_Length", rs.getInt("OTP_Length"));
				result.put("OTP_Range", rs.getInt("OTP_Range"));
				result.put("OTP_Interval", rs.getInt("OTP_Interval"));
			} else {
				result.put("No_Data", true);
			}

			result.put("ReturnCode", ReturnCode.Success);
			result.put("ReturnMsg", "Success");
			return result;
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getChannelByName] - #DB Error occurred: " + e.getMessage());
			result = new HashMap<String, Object>();
			result.put("ReturnCode", "0008");
			result.put("ReturnMsg", e.getMessage());
			return result;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static HashMap<String, Object> getChannelList(String sessionId) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> tmp = null, result = new HashMap<String, Object>();

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pStmt = conn.prepareStatement(GET_CHANNEL_LIST);
				ResultSet rs = pStmt.executeQuery();) {

			while (rs.next()) {
				tmp = new HashMap<String, Object>();
				tmp.put("Channel_ID", rs.getString("Channel_ID"));
				tmp.put("Channel_Name", rs.getString("Channel_Name"));
				tmp.put("OTP_Length", rs.getInt("OTP_Length"));
				tmp.put("OTP_Range", rs.getInt("OTP_Range"));
				tmp.put("OTP_Interval", rs.getInt("OTP_Interval"));
				tmp.put("Create_Date", rs.getTimestamp("CREATE_DATE"));
				tmp.put("Last_Modified", rs.getTimestamp("LAST_MODIFIED"));
				list.add(tmp);
			}

			result.put("Data", list);
			result.put("ReturnCode", ReturnCode.Success);
			result.put("ReturnMsg", "Success");
			return result;
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][iDGate-getChannelList] - #DB Error occurred: " + e.getMessage());
			tmp = new HashMap<String, Object>();
			result.put("ReturnCode", "0008");
			result.put("ReturnMsg", e.getMessage());
			return result;
		}
	}
}
