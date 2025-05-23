package com.Cvn.Database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.Cvn.Config.Cfg;
import com.toppanidgate.fidouaf.common.model.Log4j;

// Author: Calvin @ iDGate.com
public class IDGateMSSQL extends BaseDAO {

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
			String db_psd = Cfg.getExternalCfgValue("DBpass");
			String connURL = "";

			connURL = db_JdbcName + ";databaseName=" + Cfg.getExternalCfgValue("DBname");

			try {
				Class.forName(Cfg.getExternalCfgValue("DBclassName"));
				Connection new_connection = DriverManager.getConnection(connURL, db_user, db_psd);
				Log4j.log.info("[" + sessionId + "][iDGateMSSQL] - #Direct-connection activated.");
				return new_connection;
			} catch (ClassNotFoundException | SQLException ex) {
				Log4j.log.error(
						"[" + sessionId + "][iDGateMSSQL] - #Direct-connection init failed.(" + ex.getMessage() + ")");
				throw ex;
			} finally {
				db_psd = "";
				db_user = "";
			}
		} else {
			return getDataSrc().getConnection();
		}
	}

	public static String getNewEsnSeq(String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				CallableStatement callStmt = conn.prepareCall("{call usp_getNewEsnSeq()}");) {

			callStmt.executeQuery();
			rs = callStmt.getResultSet();
			if (rs.next()) {
				String newEsnSeq = rs.getString("newEsnSeq");
				return newEsnSeq;
			} else {
				return "";
			}
		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-getNewEsnSeq] - #DB Error occurred: " + e.getMessage());
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
				CallableStatement callStmt = conn.prepareCall("{call usp_createDevice(?,?,?,?,?,?,?)}");) {

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
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-createDevice] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

	public static String[] getDevice_UserID(String userID, String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				CallableStatement callStmt = conn.prepareCall("{call usp_getDevice_UserID(?)}");) {

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
			Log4j.log
					.error("[" + sessionId + "][iDGateMSSQL-getDevice_UserID] - #DB Error occurred: " + e.getMessage());
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
				CallableStatement callStmt = conn.prepareCall("{call usp_getDevice_Esn(?)}");) {

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
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-getDevice_Esn] - #DB Error occurred: " + e.getMessage());
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
				CallableStatement callStmt = conn.prepareCall("{call usp_setDevice(?,?,?,?,?,?,?)}");) {

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
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-updateDevice] - #DB Error occurred: " + e.getMessage() + " esn: " + esn);
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
				CallableStatement callStmt = conn.prepareCall("{call usp_setDevice_Mercury(?,?,?)}");) {

			callStmt.setString(1, esn);
			callStmt.setString(2, mercury);
			callStmt.setInt(3, newChkCount);
			callStmt.execute();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error(
					"[" + sessionId + "][iDGateMSSQL-updateDevice_Mercury] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

	public static String updateDevice_Status(String esn, String status, String sessionId) {

		try (Connection conn = getNewConnection(sessionId);
				CallableStatement callStmt = conn.prepareCall("{call usp_setDevice_Status(?,?)}");) {

			callStmt.setString(1, esn);
			callStmt.setString(2, status);
			callStmt.execute();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error(
					"[" + sessionId + "][iDGateMSSQL-updateDevice_Status] - #DB Error occurred: " + e.getMessage());
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
//			CvnLogger.writeLog(sessionId, "Debug", "[iDGateMSSQL-createTxn] - #DB Error occurred: " + e.getMessage());
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
//			CvnLogger.writeLog(sessionId, "Debug", "[iDGateMSSQL-getTxn] - #DB Error occurred: " + e.getMessage());
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

	public static String addChannel(String channelID, String channelName, int otpLength, long timeInterval,
			int timeRange, String sessionId) {
		String sql = "INSERT INTO channel_trust(Channel_ID, Channel_Name, OTP_Length, OTP_Interval, OTP_Range) VALUES(?, ?, ?, ?, ?)";

		try (Connection conn = getNewConnection(sessionId); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, channelID);
			pstmt.setString(2, channelName);
			pstmt.setInt(3, otpLength);
			pstmt.setLong(4, timeInterval);
			pstmt.setInt(5, timeRange);
			pstmt.executeUpdate();

			return "{\"ReturnCode\":\"0000\",\"ReturnMsg\":\"Channel created.\"}";
		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-addChannel] - #DB Error occurred: " + e.getMessage());
			return "{\"ReturnCode\":\"0007\",\"ReturnMsg\":\"" + e.getMessage() + "\"}";
		}
	}

	public static HashMap<String, String> getChannelByID(String channelID, String sessionId) {
		String sql = "SELECT * FROM channel_trust WHERE Channel_ID = ?";
		ResultSet rs = null;
		HashMap<String, String> data = null;

		try (Connection conn = getNewConnection(sessionId); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, channelID);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				data = new HashMap<String, String>();
				data.put("Channel_ID", rs.getString("Channel_ID"));
				data.put("Channel_Name", rs.getString("Channel_Name"));
				data.put("OTP_Length", String.valueOf(rs.getInt("OTP_Length")));
				data.put("OTP_Interval", String.valueOf(rs.getLong("OTP_Interval")));
				data.put("OTP_Range", String.valueOf(rs.getInt("OTP_Range")));
				data.put("Create_Date", String.valueOf(rs.getTimestamp("Create_Date")));
				data.put("Last_Modified", String.valueOf(rs.getTimestamp("Last_Modified")));
			}

		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-getChannelByID] - #DB Error occurred: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}

		return data;
	}

	public static HashMap<String, String> getChannelByName(String channelName, String sessionId) {
		String sql = "SELECT * FROM channel_trust WHERE Channel_Name = ?";
		ResultSet rs = null;
		HashMap<String, String> data = null;

		try (Connection conn = getNewConnection(sessionId); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, channelName);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				data = new HashMap<String, String>();
				data.put("Channel_ID", rs.getString("Channel_ID"));
				data.put("Channel_Name", rs.getString("Channel_Name"));
				data.put("OTP_Length", String.valueOf(rs.getInt("OTP_Length")));
				data.put("OTP_Interval", String.valueOf(rs.getLong("OTP_Interval")));
				data.put("OTP_Range", String.valueOf(rs.getInt("OTP_Range")));
				data.put("Create_Date", String.valueOf(rs.getTimestamp("Create_Date")));
				data.put("Last_Modified", String.valueOf(rs.getTimestamp("Last_Modified")));
			}

		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-getChannelByID] - #DB Error occurred: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}

		return data;
	}

	public static List<HashMap<String, String>> listChannel(String sessionId) {
		String sql = "SELECT * FROM channel_trust";
		List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> tmp = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery();) {

			while (rs.next()) {
				tmp = new HashMap<String, String>();
				tmp.put("Channel_ID", rs.getString("Channel_ID"));
				tmp.put("Channel_Name", rs.getString("Channel_Name"));
				tmp.put("OTP_Length", String.valueOf(rs.getInt("OTP_Length")));
				tmp.put("OTP_Interval", String.valueOf(rs.getLong("OTP_Interval")));
				tmp.put("OTP_Range", String.valueOf(rs.getInt("OTP_Range")));
				tmp.put("Create_Date", String.valueOf(rs.getTimestamp("Create_Date")));
				tmp.put("Last_Modified", String.valueOf(rs.getTimestamp("Last_Modified")));

				data.add(tmp);
			}

		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-listChannel] - #DB Error occurred: " + e.getMessage());
		}

		return data;
	}

	public static String createProtector(String protectorName, String value1, String value2, String sessionId) {
		/**
		 * preparedStatement.setString() truncate string is its lenght > 254.
		 *
		 * It seems that JdbcOdbc bridge are convert setString() to SQL_CHAR which has a
		 * length limit of 254. According to JDBC, the preparedStatement.setString(int
		 * parameterIndex, String x) set a parameter to a Java String value. The driver
		 * should convert this to a SQL VARCHAR or LONGVARCHAR value,
		 */

		String sql = "INSERT INTO Protectors (Protector, Value1, Value2) VALUES(?,?,?)";

		try (Connection conn = getNewConnection(sessionId); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			pstmt.setString(1, protectorName);
			pstmt.setString(2, value1);
			pstmt.setString(3, value2);
			pstmt.executeUpdate();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-createProtector] - #DB Error occurred: " + e.getMessage());
			return "";
		}
	}

	public static String[] getProtector(String protectorName, String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				CallableStatement callStmt = conn.prepareCall("{call usp_getProtector(?)}");) {

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
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-getProtector] - #DB Error occurred: " + e.getMessage());
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

	// store key into DB
	public static String storeKey(String kAlias, String kPriv, String kPublic, String sessionId) {
		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pstmt = conn
						.prepareStatement("INSERT INTO Keys(Alias, PRIV_KEY, PUB_KEY) VALUES(?, ?, ?)")) {

			pstmt.setString(1, kAlias);
			pstmt.setString(2, kPriv);
			pstmt.setString(3, kPublic);
			pstmt.executeUpdate();

			return "0";

		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-storeRSAKey] - #DB Error occurred: " + e.getMessage());
			return e.getMessage();
		}
	}

	// fetch Key from DB
	public static String[] fetchKey(String kAlias, String sessionId) {
		ResultSet rs = null;

		try (Connection conn = getNewConnection(sessionId);
				PreparedStatement pstmt = conn
						.prepareCall("SELECT TOP(1) * FROM Keys WHERE Alias=? ORDER BY Create_Date DESC");) {

			pstmt.setString(1, kAlias);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				String[] rsp = { rs.getString("PRIV_KEY"), rs.getString("PUB_KEY") };
				return rsp;

			} else {
				return new String[] { "", "--" };
			}

		} catch (SQLException | ClassNotFoundException e) {
			Log4j.log.error("[" + sessionId + "][iDGateMSSQL-fetchRSAKey] - #DB Error occurred: " + e.getMessage());
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
}
