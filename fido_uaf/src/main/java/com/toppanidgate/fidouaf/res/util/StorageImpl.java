/*
 * Copyright 2015 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toppanidgate.fidouaf.res.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.toppanidgate.fido.uaf.storage.AAIDnotAllowedException;
import com.toppanidgate.fido.uaf.storage.DuplicateKeyException;
import com.toppanidgate.fido.uaf.storage.RegistrationRecord;
import com.toppanidgate.fido.uaf.storage.StorageInterface;
import com.toppanidgate.fido.uaf.storage.SystemErrorException;
import com.toppanidgate.fidouaf.Log4j;
import com.toppanidgate.fidouaf.model.Authenticator;
import com.toppanidgate.fidouaf.res.config.Config;
import com.toppanidgate.fidouaf.service.AuthenticatorService;

@Service
public class StorageImpl implements StorageInterface {

	@Autowired
	AuthenticatorService authService;

	private static StorageImpl instance = new StorageImpl();

	/**
	 * get AAID + DLM + KeyID by username
	 * 
	 * @param userName (username
	 * @return
	 */
	public List<String> getAllKeysByName(String userName) {
		// Log4j.log.info("*** getKeysByValue@look up:" + userName);
		// fixed: 修正keyType[0,1,2] 配合 id 的排序
		List<String> devKeys = authService.getDevkeysByName(userName);
		// Log4j.log.info("devKeys.size: " + devKeys.size());
		return devKeys;
	}

	public static StorageImpl getInstance() {
		return instance;
	}

//	public void syncDB(String key, RegistrationRecord regRec, String username) {
//
//		Authenticator authenticator = new Authenticator();
//		authenticator.setAvailable("Y");
//		authenticator.setDate(new Date());
//		authenticator.setDevkey(key);
//		authenticator.setUserID(username);
//
//		String json = JSONUtil.pojoToJson(regRec);
//		authenticator.setValue(json);
//
//		authenticatorDao.saveAuthenticator(authenticator);
//	}

	public void store(RegistrationRecord[] records, String username, String channel)
			throws DuplicateKeyException, SystemErrorException, AAIDnotAllowedException, SQLException {
		if (username == null || username.trim().equals("")) {
			Log4j.log.error("*** username:{} is required.", username);
		}
		String[] allowedAaids = this.getAllowedAaids(channel);
		if (records != null && records.length > 0) {
			for (int i = 0; i < records.length; i++) {
				// validate
				String aaid = records[i].authenticator.AAID;
//				Log4j.log.info("*** check AAID:" + aaid);
				List<String> allowList = new ArrayList<String>(Arrays.asList(allowedAaids));
//				allowList.addAll(Dash.getInstance().uuids);
				if (!allowList.contains(aaid)) {
					// Log4j.log.info("*** check key:" + aaid);
					Log4j.log.error("*** check records:: FAIL AT INDEX[" + i + "] ; value(" + aaid + ")");
					Log4j.log.error("*** allowList: " + allowList);
					records[0].status = "FAIL AT INDEX[" + i + "]";
					throw new AAIDnotAllowedException();
				}
				// TODO 111.7.27 check DuplicateKey
				// String dbKey = records[i].authenticator.toString();
				// Log4j.log.info("*** check key:" + dbKey);
				// if (db.containsKey(dbKey)) { // AAID + DLM + KeyID
				// throw new DuplicateKeyException();
				// }
			}
			// 111.7.27 一個使用者一組(3把)[0,1,2] 生物、圖形、PIN驗證碼
			// FIX: 111.11.10 註冊流程改變，idgateId 一直產生新的，不會有重複資料
//			if (username != null) {
////				new FidoUafResource().doDereg(username); // username 前端檢核
//				this.disable(username);
//			}
			// batch insert
			batchInsert(records, username);
		}
	}

	/**
	 * List of allowed AAID - Authenticator Attestation ID. Authenticator
	 * Attestation ID / AAID. A unique identifier assigned to a model, class or
	 * batch of FIDO Authenticators that all share the same characteristics, and
	 * which a Relying Party can use to look up an Attestation Public Key and
	 * Authenticator Metadata for the device. The first 4 characters of the AAID are
	 * the vendorID.
	 * 
	 * @param channel
	 *
	 * @return list of allowed AAID - Authenticator Attestation ID.
	 */
	public String[] getAllowedAaids(String channel) {
		String[] ret;
//		System.out.println("*** AllowedAaids:" + Config.allowedAaids);
		ret = Config.allowedAaids.replace(" ", "").split(",");
//			ret = channelService.getValue(channel).replace(" ", "").split(","); 
		// 測試用 , "0057#0001",
//		for (@SuppressWarnings("unused") String str: ret) {
//			// Log4j.log.info("*** TEST str:[{}]",str);
//		}																				// "008A#0002",
		// "ABCD#ABCD"
		List<String> retList = new ArrayList<String>(Arrays.asList(ret));
//		retList.addAll(Dash.getInstance().uuids);
		return retList.toArray(new String[0]);
	}

	private void batchInsert(RegistrationRecord[] records, String username) {
		List<Authenticator> authenticators = new ArrayList<>();
		for (RegistrationRecord record : records) {
			Authenticator authenticator = new Authenticator();
			authenticator.setAvailable("Y");
			authenticator.setDate(new Date());
			authenticator.setDevkey(record.authenticator.toString());
//			authenticator.setDevkey(record.authenticator.toString() + "="); // padding 若不想改 fido-uaf-core
			authenticator.setUserID(username);
			authenticator.setValue(JSONUtil.pojoToJson(record));

			authenticators.add(authenticator);
		}
		authService.saveAuthenticator(authenticators);
	}

	public List<RegistrationRecord> readRegistrationRecords(String username) {
		// Step1 測試 HealthCheck
//		Authenticator healthCheck = authService.healthCheck();
//		System.out.println("healthCheck:"+ new Gson().toJson(healthCheck));
		List<String> result = authService.getValues(username);
		List<RegistrationRecord> regList = new ArrayList<>();
		for (String str : result) {
			regList.add(new Gson().fromJson(str, RegistrationRecord.class));
		}
		return regList;
	}
	
	public Authenticator healthCheck() {
		Authenticator authenticator = authService.healthCheck();
		Gson gson = new Gson();
		System.out.println("*** healthCheck:"+ gson.toJson(authenticator));
		Log4j.log.debug("*** healthCheck:{}", gson.toJson(authenticator));

		return authenticator;
	}

	public RegistrationRecord readRegistrationRecord(String key, String username) {
		String result = authService.getValue(key, username);

		return new Gson().fromJson(result, RegistrationRecord.class);
	}

	public void disable(String username) {
		// Log4j.log.info("!!!!!!!!!!!!!!!!!!!....................deleting object
		// associated with username = " + username);
		authService.deleteAuthenticatorByUser(username);
	}

	/**
	 *
	 */
	// fido-uaf-core用到 registrationRecord = getRegistration(authRecord, storage);
	@Override
	public RegistrationRecord readRegistrationRecord(String key) {
		Log4j.log.debug("*** devkey:{}", key);
		String result = authService.getValue(key);
//		String result = authenticatorDao.getValue(key + "="); // padding 若不想改 fido-uaf-core
		return new Gson().fromJson(result, RegistrationRecord.class);
//		return db.get(key);
	}

	@Override
	public void store(RegistrationRecord[] records) throws DuplicateKeyException, SystemErrorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void storeServerDataString(String username, String serverDataString) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUsername(String serverDataString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(RegistrationRecord[] records) {
		// TODO Auto-generated method stub

	}

	public Map<String, RegistrationRecord> dbDump() {
		// TODO Auto-generated method stub
		return null;
	}

//	public void deleteRegistrationRecord(String key) {
//		// Log4j.log.info("!!!!!!!!!!!!!!!!!!!....................deleting object associated with key = " + key);
//		authenticatorDao.deleteAuthenticatorByKey(key);
//	}

}
