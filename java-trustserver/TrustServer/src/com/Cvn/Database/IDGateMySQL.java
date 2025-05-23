package com.Cvn.Database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.Cvn.Config.Cfg;

// Author: Calvin @ iDGate.com
public class IDGateMySQL extends BaseDAO {
    private static final Logger logger = LogManager.getLogger(IDGateMySQL.class);


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

			connURL = db_JdbcName + "/" + Cfg.getExternalCfgValue("DBname") + "?useSSL=false&serverTimezone=UTC";


			try {
				Class.forName(Cfg.getExternalCfgValue("DBclassName"));
				Connection new_connection = DriverManager.getConnection(connURL, db_user, db_psd);
				logger.info("[" + sessionId + "][iDGateMySQL] - #Direct-connection activated.");
				return new_connection;
			} catch (ClassNotFoundException | SQLException ex) {
				logger.error("[" + sessionId + "][iDGateMySQL] - #Direct-connection init failed.(" + ex.getMessage() + ")");

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

			callStmt.execute();
			rs = callStmt.getResultSet();
			if (rs.next()) {
				String newEsnSeq = rs.getString("newEsnSeq");
				return newEsnSeq;

			} else {
				return "";
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][IDGateMySQL-getNewEsnSeq] - #DB Error occurred: " + e.getMessage());

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
			logger.debug("[" + sessionId + "][IDGateMySQL-createDevice] - #DB Error occurred: " + e.getMessage());

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
			logger.debug("[" + sessionId + "][IDGateMySQL-getDevice_UserID] - #DB Error occurred: " + e.getMessage());

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
				return new String[] { "0" };
			}
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][IDGateMySQL-getDevice_Esn] - #DB Error occurred: " + e.getMessage());

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
			logger.debug("[" + sessionId + "][IDGateMySQL-updateDevice] - #DB Error occurred: " + e.getMessage());

			return e.getMessage();
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
			logger.debug("[" + sessionId + "][IDGateMySQL-updateDevice_Mercury] - #DB Error occurred: " + e.getMessage());

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
			logger.debug("[" + sessionId + "][IDGateMySQL-updateDevice_Status] - #DB Error occurred: " + e.getMessage());

			return e.getMessage();
		}
	}

//	public static String createTxn(String esn, String txnID, String otpH, String chkTimeStamp, String status,
//			String sessionId) {
//
//		try (Connection conn = getNewConnection(sessionId);
//				CallableStatement callStmt = conn.prepareCall("{call usp_createTxn(?,?,?,?,?)}");) {
//
//			callStmt.setString(1, esn);
//			callStmt.setString(2, txnID);
//			callStmt.setString(3, otpH);
//			callStmt.setString(4, chkTimeStamp);
//			callStmt.setString(5, status);
//			callStmt.execute();
//			return "0";
//		} catch (SQLException | ClassNotFoundException e) {
//			CvnLogger.writeLog(sessionId, "Debug", "[IDGateMySQL-createTxn] - #DB Error occurred: " + e.getMessage());
//			return e.getMessage();
//		}
//	}
//
//	public static ResultSet getTxn(String esn, String txnID, String sessionId) {
//		ResultSet rs = null;
//
//		try (Connection conn = getNewConnection(sessionId);
//				CallableStatement callStmt = conn.prepareCall("{call usp_getTxn(?,?)}");) {
//
//			callStmt.setString(1, esn);
//			callStmt.setString(2, txnID);
//			callStmt.execute();
//			rs = callStmt.getResultSet();
//			return rs;
//		} catch (SQLException | ClassNotFoundException e) {
//			CvnLogger.writeLog(sessionId, "Debug",
//					"[IDGateMySQL-getTxn] - #DB Error occurred: " + e.getMessage());
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

		String sql = "INSERT INTO Protectors (Protector, Value1, Value2) VALUES(?,?,?)";

		try (Connection conn = getNewConnection(sessionId); PreparedStatement callStmt = conn.prepareStatement(sql);) {

			callStmt.setString(1, protectorName);
			callStmt.setString(2, value1);
			callStmt.setString(3, value2);
			callStmt.executeUpdate();
			return "0";
		} catch (SQLException | ClassNotFoundException e) {
			logger.debug("[" + sessionId + "][IDGateMySQL-createProtector] - #DB Error occurred: " + e.getMessage());

			return e.getMessage();
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
			logger.debug("[" + sessionId + "][IDGateMySQL-getProtector] - #DB Error occurred: " + e.getMessage());

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
