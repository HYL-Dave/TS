package com.Device_Detail.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.Common.model.BaseDAO;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.google.gson.Gson;

public class Device_DetailDAO extends BaseDAO implements Device_DetailDAO_interface {

	private static final String INSERT_STMT = "INSERT INTO MAP_DEVICE_DETAIL(IDGATE_ID, ESN, DEVICE_DATA, DEVICE_ID, DEVICE_TYPE, DEVICE_OS, DEVICE_LABEL, DEVICE_MODEL, DEVICE_REG_IP, DEVICE_OS_VER, APP_VER) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE = "UPDATE MAP_DEVICE_DETAIL SET DEVICE_ID=?, DEVICE_OS=?, LAST_MODIFIED=?, DEVICE_OS_VER=?, APP_VER=? WHERE IDGATE_ID=?";
	private static final String UPDATE_DEVICEDATA = "UPDATE MAP_DEVICE_DETAIL SET ESN=?, DEVICE_DATA=?, DEVICE_ID=?, DEVICE_TYPE=?, DEVICE_OS=?, DEVICE_LABEL=?, DEVICE_MODEL=?, LAST_MODIFIED=?, DEVICE_OS_VER=?, APP_VER=? WHERE IDGATE_ID=?";
	private static final String UPDATE_PERSO = "UPDATE MAP_DEVICE_DETAIL SET PERSO_UPDATE=?, LAST_MODIFIED=? WHERE IDGATE_ID=?";
	private static final String PRESTORE_NEW_ESN = "UPDATE MAP_DEVICE_DETAIL SET New_ESN = ?, NEW_DIGITAL_HASH = ?, PERSO_UPDATE = 'Y' WHERE IDGATE_ID=?";
	private static final String UPDATE_TO_NEW_ESN = "UPDATE MAP_DEVICE_DETAIL SET ESN = NEW_ESN, DIGITAL_HASH = NEW_DIGITAL_HASH, PERSO_UPDATE = 'N', PERSOFILE_DATE = GETDATE() WHERE IDGATE_ID=?";
	private static final String GET_ONE = "SELECT * FROM MAP_DEVICE_DETAIL WHERE IDGATE_ID = ?";
	private static final String UPDATE_AB_COUNT = "UPDATE MAP_DEVICE_DETAIL SET AB_COUNT = ? WHERE IDGATE_ID=?";
	private static final String UPDATE_ALL_PERSO = "UPDATE MAP_DEVICE_DETAIL SET PERSO_UPDATE = 'Y'";

	private long start_Time = 0, end_Time = 0, count = 0;
	
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	public Device_DetailDAO(String jndi, String sessID) {
		super(jndi);

		sqlLogFormat.setClazz("com.Device_Detail.model.Device_DetailDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public void insert(Device_DetailVO device_DetailVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(INSERT_STMT);) {
			sqlLogFormat.setSql_Statement(INSERT_STMT);

			pstmt.setLong(1, device_DetailVO.getIdgateID());
			pstmt.setString(2, device_DetailVO.getESN());
			pstmt.setString(3, device_DetailVO.getDevice_Data());
			pstmt.setString(4, device_DetailVO.getDevice_ID());
			pstmt.setString(5, device_DetailVO.getDevice_Type());
			pstmt.setString(6, device_DetailVO.getDevice_OS());
			pstmt.setString(7, device_DetailVO.getDeviceLabel());
			pstmt.setString(8, device_DetailVO.getDeviceModel());
			pstmt.setString(9, device_DetailVO.getDevice_Reg_IP());
			pstmt.setString(10, device_DetailVO.getDevice_OS_Ver());
			pstmt.setString(11, device_DetailVO.getAPP_Ver());

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
	public void update(Device_DetailVO device_DetailVO) throws SQLException {
		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE);) {

			sqlLogFormat.setSql_Statement(UPDATE);

			pstmt.setString(1, device_DetailVO.getDevice_ID());
			pstmt.setString(2, device_DetailVO.getDevice_OS());
			pstmt.setTimestamp(3, device_DetailVO.getModified_Date());
			pstmt.setString(4, device_DetailVO.getDevice_OS_Ver());
			pstmt.setString(5, device_DetailVO.getAPP_Ver());
			pstmt.setLong(6, device_DetailVO.getIdgateID());

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
			throw new SQLException("A database error occured. " + se.getMessage());
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
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
				device_DetailVO.setIdgateID(rs.getLong("IDGATE_ID"));
				device_DetailVO.setESN(rs.getString("ESN"));
				device_DetailVO.setNew_ESN(rs.getString("NEW_ESN"));
				device_DetailVO.setDevice_Data(rs.getString("DEVICE_DATA"));
				device_DetailVO.setDevice_ID(rs.getString("DEVICE_ID"));
				device_DetailVO.setDevice_Type(rs.getString("DEVICE_TYPE"));
				device_DetailVO.setDevice_OS(rs.getString("DEVICE_OS"));
				device_DetailVO.setPerso_Update(rs.getString("PERSO_UPDATE"));
				device_DetailVO.setDeviceLabel(rs.getString("DEVICE_LABEL"));
				device_DetailVO.setDeviceModel(rs.getString("DEVICE_MODEL"));
				device_DetailVO.setModified_Date(rs.getTimestamp("LAST_MODIFIED"));
				device_DetailVO.setAB_Count(rs.getInt("AB_COUNT"));
				device_DetailVO.setCreate_Date(rs.getTimestamp("CREATE_DATE"));
				device_DetailVO.setPersofile_Date(rs.getTimestamp("PERSOFILE_DATE"));
				device_DetailVO.setAPP_Ver(rs.getString("APP_VER"));
				device_DetailVO.setDevice_OS_Ver(rs.getString("DEVICE_OS_VER"));

				count++;
			}

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(new Gson().toJson(device_DetailVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
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
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? TRUE : FALSE);
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any driver errors
		} catch (SQLException se) {
			sqlLogFormat.setEexecute_Ttime(0);
			sqlLogFormat.setThrowable(se.getMessage());
			sqlLogFormat.setResult_StatusMessage(FALSE);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
}
