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

import com.google.gson.Gson;
import com.toppanidgate.fido.uaf.msg.DeregisterAuthenticator;
import com.toppanidgate.fido.uaf.msg.DeregistrationRequest;
import com.toppanidgate.fido.uaf.storage.AuthenticatorRecord;

public class DeregRequestProcessor {
	private Gson gson = new Gson();

	public String process(String payload) {
		if (!payload.isEmpty()) {
			try {
				DeregistrationRequest[] deregFromJson = gson.fromJson(payload,
						DeregistrationRequest[].class);
				DeregistrationRequest deregRequest = deregFromJson[0];
//				Dash.getInstance().stats.put(Dash.LAST_DEREG_REQ, deregFromJson);	// TODO: Dash.LAST_DEREG_REQ
				AuthenticatorRecord authRecord = new AuthenticatorRecord();
				for (DeregisterAuthenticator authenticator : deregRequest.authenticators) {
					authRecord.AAID = authenticator.aaid;
					authRecord.KeyID = authenticator.keyID;
//					try {
//						String Key = authRecord.toString();
////						StorageImpl.getInstance().deleteRegistrationRecord(Key);
//					} catch (Exception e) {
//						return "Failure: Problem in deleting record from local DB";
//					}
				}
			} catch (Exception e) {
				return "Failure: Problem in processing deregistration request";
			}
			return "Success";
		}
		return "Failure: Problem in processing deregistration request";
	}
}
