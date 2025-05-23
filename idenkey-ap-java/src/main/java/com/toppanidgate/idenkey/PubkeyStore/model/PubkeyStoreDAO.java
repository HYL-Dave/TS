package com.toppanidgate.idenkey.PubkeyStore.model;

import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import com.Cvn.Encryptor.Encode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.idenkey.Config.IDGateConfig;
import com.toppanidgate.idenkey.common.model.BaseDAO;

public class PubkeyStoreDAO extends BaseDAO implements PubkeyStoreDAO_interface {

	private static final String INSERT = "INSERT INTO PUB_KEY_STORE ( IDGATE_ID, ALIAS, DEVICE_DATA, PUB_KEY, PUB_KEY_ECC) VALUES (?,?,?,?,?)";
	private static final String UPDATE = "UPDATE PUB_KEY_STORE SET IDGATE_ID=?, ALIAS=?, DEVICE_DATA=?, PUB_KEY=? WHERE IDGATE_ID=?";
	private static final String GET_ONE = "SELECT * FROM PUB_KEY_STORE WHERE IDGATE_ID=?";

	private static final String REMOVE_PUB_KEY_STORE = "DELETE PUB_KEY_STORE WHERE IDGATE_ID=?";
	private static final String REMOVE_PUB_KEY_STORE_MySQL = "DELETE FROM PUB_KEY_STORE WHERE IDGATE_ID=?";

	private long start_Time = 0, end_Time = 0, count = 0;

	// RandomStringUtils.random
	private static String sessID;
	
	static {
		SecureRandom random = new SecureRandom();  
		byte[] bytes = new byte[8];
		random.nextBytes(bytes);
		PubkeyStoreDAO.sessID = Encode.byteToHex(bytes);
	}

	public PubkeyStoreDAO(String jndi, String sessID) throws UnknownHostException, NamingException {
		super(jndi, sessID);

		sqlLogFormat.setClazz("com.toppanidgate.idenkey.PubkeyStore.model.PubkeyStoreDAO");
		sqlLogFormat.setTraceID(sessID);
	}

	@Override
	public void insert(PubkeyStoreVO PubkeyStoreVO) throws SQLException {

		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);) {
			sqlLogFormat.setSql_Statement(INSERT);
			pstmt.setLong(1, PubkeyStoreVO.getiDGateID());
			pstmt.setString(2, PubkeyStoreVO.getAlias());
			pstmt.setString(3, PubkeyStoreVO.getDevice_data());
			pstmt.setString(4, PubkeyStoreVO.getPub_key());
			pstmt.setString(5, PubkeyStoreVO.getPub_key_ECC());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();
			if (count == 0) {
				throw new SQLException("Creating Pub_Key_Store failed, no rows affected.");
			}

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

		} catch (SQLException se) {
			// Handle any SQL errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public void update(PubkeyStoreVO PubkeyStoreVO) throws SQLException {

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(UPDATE);) {
			sqlLogFormat.setSql_Statement(UPDATE);
			Log4j.log.trace("[" + sessID + "] Exec SQL: " + UPDATE);

			pstmt.setLong(1, PubkeyStoreVO.getiDGateID());
			pstmt.setString(2, PubkeyStoreVO.getAlias());
			pstmt.setString(3, PubkeyStoreVO.getDevice_data());
			pstmt.setString(4, PubkeyStoreVO.getPub_key());

			// get execution time
			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			Log4j.log.trace("[" + sessID + "] Affected row: " + pstmt.executeUpdate());

		} catch (SQLException se) {
			// Handle any driver errors
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}

	@Override
	public PubkeyStoreVO findByIDgateID(long iDGateID) throws SQLException {

		PubkeyStoreVO PubkeyStoreVO = null;
		ResultSet rs = null;

		try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(GET_ONE);) {
			sqlLogFormat.setSql_Statement(GET_ONE);
			Log4j.log.trace("[" + sessID + "] Exec SQL: " + GET_ONE);

			pstmt.setLong(1, iDGateID);
			// get execution time
			start_Time = System.currentTimeMillis();
			rs = pstmt.executeQuery();

			if (rs.next()) {
				PubkeyStoreVO = new PubkeyStoreVO();
				PubkeyStoreVO.setiDGateID(iDGateID);
				PubkeyStoreVO.setCreate_Date(rs.getTimestamp("Create_Date"));
				PubkeyStoreVO.setId(rs.getLong("id"));
				PubkeyStoreVO.setAlias(rs.getString("Alias"));
				PubkeyStoreVO.setDevice_data(rs.getString("Device_Data"));
				PubkeyStoreVO.setPub_key(rs.getString("Pub_Key"));
				PubkeyStoreVO.setPub_key_ECC(rs.getString("Pub_Key_ECC"));
				count++;
			}

			end_Time = System.currentTimeMillis();
			sqlLogFormat.setResult_Count(count);
			
			Gson gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
			if (PubkeyStoreVO != null) {
				sqlLogFormat.setMessageForMap(gson.fromJson(gson.toJson(PubkeyStoreVO), new TypeToken<HashMap<String, Object>>() {
				}.getType()));
				
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("iDGate_ID", iDGateID);
				sqlLogFormat.setMessageForMap(condition);
			} 
			
//			sqlLogFormat.setMessageForMap(new Gson().fromJson(new Gson().toJson(PubkeyStoreVO), new TypeToken<HashMap<String, Object>>() {
//			}.getType()));
//			sqlLogFormat.setMessage(new Gson().toJson(PubkeyStoreVO));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			// Handle any SQL errors
		} catch (SQLException se) {
			throw new SQLException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException se) {
				}
		}
		return PubkeyStoreVO;
	}

	@Override
	public void removeOneRowByIDgateID(String iDGateID) throws SQLException {
		
		String sqlStatement = REMOVE_PUB_KEY_STORE;
		if("MySQL".equals(IDGateConfig.dataBaseType)) {
			sqlStatement = REMOVE_PUB_KEY_STORE_MySQL;
		} 
		
		try (Connection con = ds.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sqlStatement);) {
			sqlLogFormat.setSql_Statement(sqlStatement);
			Log4j.log.trace("[" + sessID + "] Exec SQL: " + REMOVE_PUB_KEY_STORE);
			
			// get execution time
			start_Time = System.currentTimeMillis();
			
			pstmt.setString(1, iDGateID);

			start_Time = System.currentTimeMillis();
			count = pstmt.executeUpdate();
			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");

			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());

			Log4j.log.trace("[" + sessID + "] Affected row: " + pstmt.executeUpdate());

			// Handle any driver errors
		} catch (SQLException se) {
			throw new SQLException("A database error occured. " + se.getMessage());
		}
	}
}