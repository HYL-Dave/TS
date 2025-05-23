package com.toppanidgate.idenkey.Device_Detail.model;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class Device_DetailDAO extends BaseDAO implements Device_DetailDAO_interface {

	private static final String INSERT_STMT = "INSERT INTO DEVICE_DETAIL(IDGATE_ID, ESN, DEVICE_DATA, DEVICE_ID, DEVICE_TYPE, DEVICE_OS, DEVICE_LABEL, DEVICE_MODEL, AUTH_TYPE, DIGITAL_HASH, PATTERN_HASH, BIO_HASH, DEVICE_REG_IP, DEVICE_OS_VER, APP_VER, TRANSACTION_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE = "UPDATE DEVICE_DETAIL SET DEVICE_ID=?, DEVICE_OS=?, LAST_MODIFIED=?, DEVICE_OS_VER=?, APP_VER=? WHERE IDGATE_ID=?";
	private static final String UPDATE_DEVICEDATA = "UPDATE DEVICE_DETAIL SET ESN=?, DEVICE_DATA=?, DEVICE_ID=?, DEVICE_TYPE=?, DEVICE_OS=?, DEVICE_LABEL=?, DEVICE_MODEL=?, LAST_MODIFIED=?, DEVICE_OS_VER=?, APP_VER=? WHERE IDGATE_ID=?";
	private static final String UPDATE_PERSO = "UPDATE DEVICE_DETAIL SET PERSO_UPDATE=?, LAST_MODIFIED=? WHERE IDGATE_ID=?";
	private static final String PRESTORE_NEW_ESN = "UPDATE DEVICE_DETAIL SET NEW_ESN = ?, NEW_DIGITAL_HASH = ? WHERE IDGATE_ID=?";
//	private static final String PRESTORE_NEW_ESN = "UPDATE DEVICE_DETAIL SET NEW_ESN = ?, NEW_DIGITAL_HASH = ?, PERSO_UPDATE = 'Y' WHERE IDGATE_ID=?";
	private static final String UPDATE_TO_NEW_ESN = "UPDATE DEVICE_DETAIL SET ESN = NEW_ESN, DIGITAL_HASH = NEW_DIGITAL_HASH, PERSO_UPDATE = 'N', PERSOFILE_DATE = GETDATE() WHERE IDGATE_ID=?";
	private static final String GET_ONE = "SELECT * FROM DEVICE_DETAIL WHERE IDGATE_ID = ?";
	private static final String UPDATE_AB_COUNT = "UPDATE DEVICE_DETAIL SET AB_COUNT = ? WHERE IDGATE_ID=?";
	private static final String UPDATE_AUTH_TYPE = "UPDATE DEVICE_DETAIL SET AUTH_TYPE = ?, LAST_MODIFIED = GETDATE() WHERE IDGATE_ID = ? AND AUTH_TYPE = ?";
	private static final String UPDATE_DIGITAL_HASH = "UPDATE DEVICE_DETAIL SET DIGITAL_HASH = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_PATTERN_HASH = "UPDATE DEVICE_DETAIL SET PATTERN_HASH = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_BIO_HASH = "UPDATE DEVICE_DETAIL SET BIO_HASH = ? WHERE IDGATE_ID = ?";
	private static final String UPDATE_ALL_PERSO = "UPDATE DEVICE_DETAIL SET PERSO_UPDATE = 'Y'";

	private long start_Time = 0, end_Time = 0, count = 0;

	public Device_DetailDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.Device_Detail.model.Device_DetailDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public void insert(Device_DetailVO device_DetailVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(INSERT_STMT);) {
			sqlLogFormat.setSql_Statement(INSERT_STMT);
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			sqlLogFormat.setMessageForMap(
					gson.fromJson(gson.toJson(device_DetailVO), new TypeToken<HashMap<String, Object>>() {
					}.getType()));

			pstmt.setLong(1, device_DetailVO.getIdgateID());
			pstmt.setString(2, device_DetailVO.getESN());
			pstmt.setString(3, device_DetailVO.getDevice_Data());
			pstmt.setString(4, device_DetailVO.getDevice_ID());
			pstmt.setString(5, device_DetailVO.getDevice_Type());
			pstmt.setString(6, device_DetailVO.getDevice_OS());
			pstmt.setString(7, device_DetailVO.getDeviceLabel());
			pstmt.setString(8, device_DetailVO.getDeviceModel());
			pstmt.setString(9, device_DetailVO.getAuth_Type());
			pstmt.setString(10, device_DetailVO.getDigital_Hash());
			pstmt.setString(11, device_DetailVO.getPattern_Hash());
			pstmt.setString(12, device_DetailVO.getBio_Hash());
			pstmt.setString(13, device_DetailVO.getDevice_Reg_IP());
			pstmt.setString(14, device_DetailVO.getDevice_OS_Ver());
			pstmt.setString(15, device_DetailVO.getAPP_Ver());
			pstmt.setString(16, device_DetailVO.getTransaction_ID());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage("false");
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void update(Device_DetailVO device_DetailVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE);) {

			sqlLogFormat.setSql_Statement(UPDATE);

			pstmt.setString(1, device_DetailVO.getDevice_ID());
			pstmt.setString(2, device_DetailVO.getDevice_OS());
			pstmt.setTimestamp(3, device_DetailVO.getModified_Date());
			pstmt.setLong(4, device_DetailVO.getIdgateID());
			pstmt.setString(5, device_DetailVO.getDevice_OS_Ver());
			pstmt.setString(6, device_DetailVO.getAPP_Ver());

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		}
	}

	@Override
	public void updateDeviceData(Device_DetailVO device_DetailVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_DEVICEDATA);) {

			sqlLogFormat.setSql_Statement(UPDATE_DEVICEDATA);

			pstmt.setString(1, device_DetailVO.getESN());
			pstmt.setString(2, device_DetailVO.getDevice_Data());
			pstmt.setString(3, device_DetailVO.getDevice_ID());
			pstmt.setString(4, device_DetailVO.getDevice_Type());
			pstmt.setString(5, device_DetailVO.getDevice_OS());
			pstmt.setString(6, device_DetailVO.getDeviceLabel());
			pstmt.setString(7, device_DetailVO.getDeviceModel());
			pstmt.setTimestamp(8, device_DetailVO.getModified_Date());
			pstmt.setLong(9, device_DetailVO.getIdgateID());
			pstmt.setString(10, device_DetailVO.getDevice_OS_Ver());
			pstmt.setString(11, device_DetailVO.getAPP_Ver());

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updatePerso(Device_DetailVO device_DetailVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_PERSO);) {

			sqlLogFormat.setSql_Statement(UPDATE_PERSO);

			pstmt.setString(1, device_DetailVO.getPerso_Update());
			pstmt.setTimestamp(2, device_DetailVO.getModified_Date());
			pstmt.setLong(3, device_DetailVO.getIdgateID());

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public Device_DetailVO findByPrimaryKey(long customer_ID) throws SQLException {

		Device_DetailVO device_DetailVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE);) {
			sqlLogFormat.setSql_Statement(GET_ONE);

			pstmt.setLong(1, customer_ID);

			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			while (rs.next()) {
				// deptVO �]�٬� Domain objects
				device_DetailVO = new Device_DetailVO();
				device_DetailVO.setIdgateID(rs.getLong("iDGate_ID"));
				device_DetailVO.setESN(rs.getString("ESN"));
				device_DetailVO.setNew_ESN(rs.getString("New_ESN"));
				device_DetailVO.setDevice_Data(rs.getString("Device_Data"));
				device_DetailVO.setDevice_ID(rs.getString("Device_ID"));
				device_DetailVO.setDevice_Type(rs.getString("Device_Type"));
				device_DetailVO.setDevice_OS(rs.getString("Device_OS"));
				device_DetailVO.setDevice_OS_Ver(rs.getString("Device_OS_Ver"));
				device_DetailVO.setPerso_Update(rs.getString("Perso_Update"));
				device_DetailVO.setDeviceLabel(rs.getString("Device_Label"));
				device_DetailVO.setDeviceModel(rs.getString("Device_Model"));
				device_DetailVO.setModified_Date(rs.getTimestamp("Last_Modified"));
				device_DetailVO.setAB_Count(rs.getInt("AB_Count"));
				device_DetailVO.setAuth_Type(rs.getString("Auth_Type"));
				device_DetailVO.setPattern_Hash(rs.getString("Pattern_Hash"));
				device_DetailVO.setDigital_Hash(rs.getString("Digital_Hash"));
				device_DetailVO.setBio_Hash(rs.getString("Bio_Hash"));
				device_DetailVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				device_DetailVO.setPersofile_Date(rs.getTimestamp("Persofile_Date"));

				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING)
					.setNumberToNumberStrategy(null).create();
			if (device_DetailVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(device_DetailVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", customer_ID);
				sqlLogFormat.setMessageForMap(condition);
			}
//			sqlLogFormat.setMessageForMap(new Gson().fromJson(new Gson().toJson(device_DetailVO), new TypeToken<HashMap<String, Object>>() {
//			}.getType()));
//			sqlLogFormat.setMessage(new Gson().toJson(device_DetailVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
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
				} catch (SQLException e) {
					Log4j.log.error(e.getMessage());
				}
		}
		return device_DetailVO;
	}

	@Override
	public void updateABCount(long customer_ID, Integer count) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_AB_COUNT);) {
			sqlLogFormat.setSql_Statement(UPDATE_AB_COUNT);

			pstmt.setInt(1, count);
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
	public void updateAuthType(long customer_ID, String type, String oldType) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_AUTH_TYPE);) {

			sqlLogFormat.setSql_Statement(UPDATE_AUTH_TYPE);

			pstmt.setString(1, type);
			pstmt.setLong(2, customer_ID);
			pstmt.setString(3, oldType);

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			if (count == 0) {
				throw new SQLException("Auth type has been updated by other. This request is cancelled.");
			}

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void prestoreNewEsn(long customer_ID, String esn, String pin) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(PRESTORE_NEW_ESN);) {

			sqlLogFormat.setSql_Statement(PRESTORE_NEW_ESN);

			pstmt.setString(1, esn);
			pstmt.setString(2, pin);
			pstmt.setLong(3, customer_ID);

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateToNewEsn(long customer_ID) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_TO_NEW_ESN);) {
			sqlLogFormat.setSql_Statement(UPDATE_TO_NEW_ESN);

			pstmt.setLong(1, customer_ID);

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void updateDigitalHash(long customer_ID, String hash) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_DIGITAL_HASH);) {
			sqlLogFormat.setSql_Statement(UPDATE_DIGITAL_HASH);

			pstmt.setString(1, hash);
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
	public void updatePatternHash(long customer_ID, String hash) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(UPDATE_PATTERN_HASH);) {
			sqlLogFormat.setSql_Statement(UPDATE_PATTERN_HASH);

			pstmt.setString(1, hash);
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
	public void updateBioHash(long customer_ID, String hash) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_BIO_HASH);) {

			sqlLogFormat.setSql_Statement(UPDATE_BIO_HASH);

			pstmt.setString(1, hash);
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
	public void forceAllPersoUpdate() throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE_ALL_PERSO);) {

			sqlLogFormat.setSql_Statement(UPDATE_ALL_PERSO);

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
			Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
}
