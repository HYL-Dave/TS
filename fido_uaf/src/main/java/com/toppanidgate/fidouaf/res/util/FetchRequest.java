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

import com.toppanidgate.fido.uaf.msg.AuthenticationRequest;
import com.toppanidgate.fido.uaf.msg.DeregistrationRequest;
import com.toppanidgate.fido.uaf.msg.RegistrationRequest;
import com.toppanidgate.fido.uaf.ops.AuthenticationRequestGeneration;
import com.toppanidgate.fido.uaf.ops.DeregRequestGeneration;
import com.toppanidgate.fido.uaf.ops.RegistrationRequestGeneration;

public class FetchRequest {

	private String appId;
	private String[] aaids;

	public FetchRequest() {
		this.appId = "";
		this.aaids = null;
	}

	public FetchRequest(String appId, String[] aaids) {
		this.appId = appId;
		this.aaids = aaids;
	}

	public RegistrationRequest getRegistrationRequest(String username) {
		RegistrationRequest request = new RegistrationRequestGeneration(appId,
				aaids).createRegistrationRequest(username,
				NotaryImpl.getInstance());
		return request;
	}

	public AuthenticationRequest getAuthenticationRequest() {
		AuthenticationRequest authReq = new AuthenticationRequestGeneration(
				appId, aaids).createAuthenticationRequest(NotaryImpl
				.getInstance());
		return authReq;
	}
	
	public DeregistrationRequest getcreateDeregRequest(String aaid,
			String keyID) {
		DeregistrationRequest authReq = new DeregRequestGeneration(
				appId, aaids).createDeregRequest(aaid,keyID);
		return authReq;
	}
}
