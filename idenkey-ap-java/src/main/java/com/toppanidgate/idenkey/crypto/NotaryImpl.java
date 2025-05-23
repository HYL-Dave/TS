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

package com.toppanidgate.idenkey.crypto;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

import com.toppanidgate.fidouaf.common.model.Log4j;

/**
 * This is just en example implementation. You should implement this class based on your operational environment.
 */
public class NotaryImpl implements Notary {

//	private Logger logger = Log4j.log.getLogger(this.getClass().getName());
	private String hmacSecret = "HMAC-is-just-one-way";
	private static Notary instance = new NotaryImpl();

	private NotaryImpl() {
		// Init
	}

	public static Notary getInstance() {
		return instance;
	}

	public String sign(String signData) {
		try {
			return Base64.encodeBase64URLSafeString(HMAC.sign(signData, hmacSecret));
		} catch (Exception e) {
			 Log4j.log.info(e.getMessage());
		}
		return null;
	}

	public boolean verify(String signData, String signature) {
		try {
			return MessageDigest.isEqual(Base64.decodeBase64(signature), HMAC.sign(signData, hmacSecret));
		} catch (Exception e) {
			 Log4j.log.info(e.getMessage());
		}
		return false;
	}

}
