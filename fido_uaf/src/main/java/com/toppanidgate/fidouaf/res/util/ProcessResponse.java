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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.toppanidgate.fido.uaf.msg.AuthenticationResponse;
import com.toppanidgate.fido.uaf.msg.RegistrationResponse;
import com.toppanidgate.fido.uaf.ops.AuthenticationResponseProcessing;
import com.toppanidgate.fido.uaf.ops.RegistrationResponseProcessing;
import com.toppanidgate.fido.uaf.storage.AuthenticatorRecord;
import com.toppanidgate.fido.uaf.storage.RegistrationRecord;
import com.toppanidgate.fidouaf.res.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessResponse {
	
	@Autowired
	StorageImpl storageImpl;

//	@Autowired
//	public ProcessResponse(StorageImpl storageImpl) {
//		this.storageImpl = storageImpl;
//	}
	
	private static final Logger logger = LogManager.getLogger(ProcessResponse.class);

	private static final int SERVER_DATA_EXPIRY_IN_MS = 5 * 60 * 1000;

	// Gson gson = new Gson ();

	public AuthenticatorRecord[] processAuthResponse(AuthenticationResponse resp) {
		AuthenticatorRecord[] result = null;
		try {
//			System.out.println("*** testMode:" + Config.testMode);

			result = new AuthenticationResponseProcessing(
					SERVER_DATA_EXPIRY_IN_MS, NotaryImpl.getInstance()).verify(
					resp, storageImpl, Config.testMode);
		} catch (Exception e) {
			logger.error("!!!!!!!!!!!!!!!!!!!..............................."
							+ e.getMessage());
			result = new AuthenticatorRecord[1];
			result[0] = new AuthenticatorRecord();
			result[0].status = "processAuthResponse Error";
		}
		return result;
	}

	public RegistrationRecord[] processRegResponse(RegistrationResponse resp, String username) {
		RegistrationRecord[] result = null;
		try {
			result = new RegistrationResponseProcessing(
					SERVER_DATA_EXPIRY_IN_MS, NotaryImpl.getInstance())
					.processResponse(resp, Config.testMode);
		} catch (Exception e) {
			logger.error("!!!!!!!!!!!!!!!!!!!..............................."
					+ e.getMessage());
			result = new RegistrationRecord[1];
			result[0] = new RegistrationRecord();
			result[0].status = e.getMessage();
		}
		return result;
	}
	
	public RegistrationRecord[] processRegResponse(RegistrationResponse resp) {
		RegistrationRecord[] result = null;
		try {
//			System.out.println("*** testMode:" + Config.testMode);
			
			result = new RegistrationResponseProcessing(
					SERVER_DATA_EXPIRY_IN_MS, NotaryImpl.getInstance())
					.processResponse(resp, Config.testMode);
		} catch (Exception e) {
			logger.error("!!!!!!!!!!!!!!!!!!!..............................."
					+ e.getMessage());
			result = new RegistrationRecord[1];
			result[0] = new RegistrationRecord();
			result[0].status = e.getMessage();
		}
		return result;
	}
}