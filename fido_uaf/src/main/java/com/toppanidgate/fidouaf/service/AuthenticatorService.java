package com.toppanidgate.fidouaf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.Log4j;
import com.toppanidgate.fidouaf.DAO.AuthenticatorRepository;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.fidouaf.common.model.SQLLogFormat;
import com.toppanidgate.fidouaf.model.Authenticator;

@Service
public class AuthenticatorService {
	
	@Autowired
	AuthenticatorRepository authRepository;
	Gson gson = new Gson();
	/**
	 * Step3
	 * @param userName
	 * @return
	 */
	public List<String> getDevkeysByName(String userName) {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		Log4j.log.trace("Exec SQL: " + "select devkey from Authenticator a where a.available = 'Y' and  userid = ? ORDER BY id ASC");
		sqlLogFormat.setSql_Statement("select devkey from Authenticator a where a.available = 'Y' and  userid = ? ORDER BY id ASC");
		long start_Time = 0, end_Time = 0, count = 0;
		start_Time = System.currentTimeMillis();
		
		List<String> returnList = null;
		try {
			returnList = authRepository.getDevkeysByName(userName);
			if(returnList !=null && returnList.size() > 0) {
				count = returnList.size(); 
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("userid", userName);
				sqlLogFormat.setMessage(gson.toJson(condition));
				logException("Cannot find devkey by userId: " + userName, sqlLogFormat);
				return returnList;
			} 
		} catch (Exception e) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("userid", userName);
			sqlLogFormat.setMessage(gson.toJson(condition));
			logException(e.getMessage(), sqlLogFormat);
			return returnList;
		}
		end_Time = System.currentTimeMillis();

		sqlLogFormat.setResult_Count(count);
		sqlLogFormat.setMessage(gson.toJson(returnList));
//		sqlLogFormat.setMessage(userName);
		sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
		sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
		Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
		
		return returnList;
	}
	
	
	/**
	 * 註銷使用者
	 * 
	 * @param username
	 */
	@Transactional
	public void deleteAuthenticatorByUser(String username) {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		try {
			Log4j.log.trace(
					"Exec SQL: " + "update Authenticator set available = 'N' where available = 'Y' and userid = ? ORDER BY id ASC");
			sqlLogFormat
					.setSql_Statement("update Authenticator set available = 'N' where available = 'Y' and userid = ? ORDER BY id ASC");
			long start_Time = 0, end_Time = 0, count = 0;
			start_Time = System.currentTimeMillis();

			count = authRepository.updateByUserID(username);

			end_Time = System.currentTimeMillis();

			sqlLogFormat.setResult_Count(count);
			Map<String, Object> condition = new HashMap<>();
			condition.put("userid", username);
			sqlLogFormat.setMessage(gson.toJson(condition));
//		sqlLogFormat.setMessage(String.valueOf(count));
			sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
			sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
			Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
		} catch (Exception e) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("userid", username);
			sqlLogFormat.setMessage(gson.toJson(condition));
			logException(e.getMessage(), sqlLogFormat);
		}
	}
	
	/**
	 * Step3
	 * @param devKey
	 * @param username
	 * @return
	 */
	public String getValue(String devKey, String username) {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		Log4j.log.trace("Exec SQL: "
				+ "select value from Authenticator a where a.available = 'Y' and userid = ? and devKey = ?");
		sqlLogFormat.setSql_Statement(
				"select value from Authenticator a where a.available = 'Y' and userid = ? and devKey = ?");
		long start_Time = 0, end_Time = 0, count = 0;
		start_Time = System.currentTimeMillis();
		
		String returnVal = null;
		try {
			returnVal = authRepository.getValueByDevKeyAndUsername(devKey, username);
			if (returnVal != null && returnVal.length() > 0) {
				count = 1; 
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("userid", username);
				condition.put("devKey", devKey);
				sqlLogFormat.setMessage(gson.toJson(condition));
				logException("Cannot find value by devKey: " + devKey + " and userId: " + username, sqlLogFormat);
				return returnVal;
			} 
		} catch (Exception e) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("userid", username);
			condition.put("devKey", devKey);
			sqlLogFormat.setMessage(gson.toJson(condition));
			logException(e.getMessage(), sqlLogFormat);
			return returnVal;
		}
		
		end_Time = System.currentTimeMillis();

		sqlLogFormat.setResult_Count(count);
//		sqlLogFormat.setMessage(gson.fromJson(returnVal, new TypeToken<HashMap<String, Object>>() {
//		}.getType()));
		sqlLogFormat.setMessage(returnVal);
		sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
		sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
		Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
		
		return returnVal;
	}
	
	/**
	 * Step4
	 * @param devKey
	 * @return
	 */
	public String getValue(String devKey) {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		Log4j.log.trace("Exec SQL: " + "select value from Authenticator a where a.available = 'Y' and devKey = ?");
		sqlLogFormat.setSql_Statement("select value from Authenticator a where a.available = 'Y' and devKey = ?");
		long start_Time = 0, end_Time = 0, count = 0;
		start_Time = System.currentTimeMillis();
		
		String returnVal = null;
		try {
			returnVal = authRepository.getValueByDevKey(devKey);
			if (returnVal != null && returnVal.length() > 0) {
				count = 1; 
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("devKey", devKey);
				sqlLogFormat.setMessage(gson.toJson(condition));
				logException("Cannot find value by devKey: " + devKey, sqlLogFormat);
				return returnVal;
			} 
		} catch (Exception e) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("devKey", devKey);
			sqlLogFormat.setMessage(gson.toJson(condition));
			logException(e.getMessage(), sqlLogFormat);
			return returnVal;
		}
		
		end_Time = System.currentTimeMillis();

		sqlLogFormat.setResult_Count(count);
//		sqlLogFormat.setMessage(gson.fromJson(returnVal, new TypeToken<HashMap<String, Object>>() {
//		}.getType()));
		sqlLogFormat.setMessage(returnVal);
		sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
		sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
		Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
		return returnVal;
	}
	
	/**
	 * healthCheck
	 * @return
	 */
	public Authenticator healthCheck() {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		Log4j.log.trace("Exec SQL: " + "select top(1) * from AUTHENTICATOR");
		sqlLogFormat.setSql_Statement("select top(1) * from AUTHENTICATOR");
		long start_Time = 0, end_Time = 0, count = 0;
		start_Time = System.currentTimeMillis();
		
		Authenticator authenticator = new Authenticator();
		
		String returnVal = null;
		try {
			authenticator = authRepository.healthCheck();
			if (authenticator != null) {
				count = 1; 
			} else {
				Map<String, Object> condition = new HashMap<>();
				sqlLogFormat.setMessage(gson.toJson(condition));
				logException("Cannot find authenticator: ", sqlLogFormat);
				return authenticator;
			} 
		} catch (Exception e) {
			sqlLogFormat.setMessage(gson.toJson(authenticator));
			logException(e.getMessage(), sqlLogFormat);
			return null;
		}
		
		end_Time = System.currentTimeMillis();
		
		sqlLogFormat.setResult_Count(count);
//		sqlLogFormat.setMessage(gson.fromJson(returnVal, new TypeToken<HashMap<String, Object>>() {
		sqlLogFormat.setMessage(returnVal);
		sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
		sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
		Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
		return authenticator;
	}
	
	/**
	 * Step1
	 * @param username
	 * @return
	 */
	public List<String> getValues(String username) {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		Log4j.log.trace("Exec SQL: " + "select value from Authenticator a where a.available = 'Y' and userid = ? ORDER BY id ASC");
		sqlLogFormat.setSql_Statement("select value from Authenticator a where a.available = 'Y' and userid = ? ORDER BY id ASC");
		long start_Time = 0, end_Time = 0, count = 0;
		start_Time = System.currentTimeMillis();
		
		List<String> returnList = null;
		try {
			returnList = authRepository.getValuesByUsername(username);
			count = 1;
			if (returnList != null && returnList.size() > 0) {
				count = returnList.size(); 
			} else {
				Map<String, Object> condition = new HashMap<>();
				condition.put("userid", username);
				sqlLogFormat.setMessage(gson.toJson(condition));
				logException("Cannot find value by userId: " + username, sqlLogFormat);
				return returnList;
			} 
		} catch (Exception e) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("userid", username);
			sqlLogFormat.setMessage(gson.toJson(condition));
			logException(e.getMessage(), sqlLogFormat);
			return returnList;
		}
		
		end_Time = System.currentTimeMillis();

		sqlLogFormat.setResult_Count(count);
		List<Map<String, Object>> listMap = new ArrayList<>();
		for (String str: returnList) {
			listMap.add(gson.fromJson(str, new TypeToken<HashMap<String, Object>>() {
			}.getType()));
		}
		sqlLogFormat.setMessage(gson.toJson(listMap));
//		sqlLogFormat.setMessage(gson.toJson(returnList));
		sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
		sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
		Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
		
		return returnList;
	}
	
	/**
	 * Step2
	 * @param authenticators
	 */
	public void saveAuthenticator(List<Authenticator> authenticators) {
		SQLLogFormat sqlLogFormat = new SQLLogFormat();
		Log4j.log.trace("Exec SQL: "
				+ "insert into AUTHENTICATOR (available, date, devkey, userID, value, id) values (?, ?, ?, ?, ?, ?)");
		sqlLogFormat.setSql_Statement(
				"insert into AUTHENTICATOR (available, date, devkey, userID, value, id) values (?, ?, ?, ?, ?, ?)");
		long start_Time = 0, end_Time = 0, count = 0;
		start_Time = System.currentTimeMillis();
		
		try {
//			authRepository.saveAuthenticator(authenticators);
			 
			authRepository.saveAll(authenticators);
			count = authenticators.size();
		} catch (Exception e) {
//			sqlLogFormat.setMessageForList(authenticators);
			sqlLogFormat.setMessage(new GsonBuilder().disableHtmlEscaping().create().toJson(authenticators));
			logException(e.getMessage(), sqlLogFormat);
			return;
		}
		
		end_Time = System.currentTimeMillis();
		sqlLogFormat.setResult_Count(count);
//		sqlLogFormat.setMessageForList(authenticators);
		sqlLogFormat.setMessage(new GsonBuilder().disableHtmlEscaping().create().toJson(authenticators));
		sqlLogFormat.setResult_StatusMessage(count >= 1 ? "true" : "false");
		sqlLogFormat.setEexecute_Ttime(end_Time - start_Time);
		Log4jSQL.log.debug(sqlLogFormat.getCompleteTxt());
	}

	private void logException(String errMsg, SQLLogFormat sqlLogFormat) {
		sqlLogFormat.setEexecute_Ttime(0);
		sqlLogFormat.setThrowable(errMsg);
		sqlLogFormat.setResult_StatusMessage("false");
		Log4jSQL.log.error(sqlLogFormat.getCompleteTxt());
		Log4j.log.error("*** Exception: " + errMsg);
	}
}
