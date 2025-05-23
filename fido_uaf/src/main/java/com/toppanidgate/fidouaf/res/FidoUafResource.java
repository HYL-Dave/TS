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

package com.toppanidgate.fidouaf.res;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fido.uaf.crypto.SHA;
import com.toppanidgate.fido.uaf.msg.AuthenticationRequest;
import com.toppanidgate.fido.uaf.msg.AuthenticationResponse;
import com.toppanidgate.fido.uaf.msg.DeregistrationRequest;
import com.toppanidgate.fido.uaf.msg.MatchCriteria;
import com.toppanidgate.fido.uaf.msg.Operation;
import com.toppanidgate.fido.uaf.msg.OperationHeaderforin;
import com.toppanidgate.fido.uaf.msg.RegistrationRequest;
import com.toppanidgate.fido.uaf.msg.RegistrationResponse;
import com.toppanidgate.fido.uaf.msg.Transaction;
import com.toppanidgate.fido.uaf.msg.Version;
import com.toppanidgate.fido.uaf.storage.AAIDnotAllowedException;
import com.toppanidgate.fido.uaf.storage.AuthenticatorRecord;
import com.toppanidgate.fido.uaf.storage.DuplicateKeyException;
import com.toppanidgate.fido.uaf.storage.RegistrationRecord;
import com.toppanidgate.fido.uaf.storage.SystemErrorException;
import com.toppanidgate.fidouaf.Log4j;
import com.toppanidgate.fidouaf.RPserver.msg.GetUAFDeregRequest;
import com.toppanidgate.fidouaf.RPserver.msg.GetUAFRequest;
import com.toppanidgate.fidouaf.RPserver.msg.GetUAFResponse;
import com.toppanidgate.fidouaf.RPserver.msg.ReturnUAFAuthenticationRequest;
import com.toppanidgate.fidouaf.RPserver.msg.ReturnUAFDeregistrationRequest;
import com.toppanidgate.fidouaf.RPserver.msg.ReturnUAFRegistrationRequest;
import com.toppanidgate.fidouaf.RPserver.msg.ServerResponse;
import com.toppanidgate.fidouaf.RPserver.msg.UAFUserName;
import com.toppanidgate.fidouaf.common.model.APLogFormat;
import com.toppanidgate.fidouaf.common.model.InboundLogFormat;
import com.toppanidgate.fidouaf.common.model.Log4jAP;
import com.toppanidgate.fidouaf.common.model.Log4jInbound;
import com.toppanidgate.fidouaf.common.model.Log4jSQL;
import com.toppanidgate.fidouaf.facets.Facets;
import com.toppanidgate.fidouaf.facets.TrustedFacets;
import com.toppanidgate.fidouaf.model.Authenticator;
import com.toppanidgate.fidouaf.res.config.Config;
import com.toppanidgate.fidouaf.res.util.DeregRequestProcessor;
import com.toppanidgate.fidouaf.res.util.FetchRequest;
import com.toppanidgate.fidouaf.res.util.ProcessResponse;
import com.toppanidgate.fidouaf.res.util.StorageImpl;

@Component
@Path("/v1")
public class FidoUafResource extends Application {

	@Autowired
	StorageImpl storageImpl;

	@Autowired
	ProcessResponse processResponse;

//	@Autowired
//	public FidoUafResource(StorageImpl storageImpl) {
//		this.storageImpl = storageImpl;
//	}

	protected Gson gson = new GsonBuilder().disableHtmlEscaping().create();

//	private static final Logger Log4j.log = LogManager.getLogger(FidoUafResource.class);
//	private final Logger Log4j.log = LogManager.getLogger(this.getClass().getName());

	@Value("${log4j_file_path}")
	private String log4j_file_path;

	@Value("${appID:NOfacets}")
	private String appID;


	@GET
	@Path("/public/regRequest/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public RegistrationRequest[] getRegisReqPublic(@PathParam("username") String username,
			@PathParam("channel") String channel) {

		return regReqPublic(username, channel);
	}

	private RegistrationRequest[] regReqPublic(String username, String channel) {
		RegistrationRequest[] regReq = new RegistrationRequest[1];
		// Log4j.log.info("*** getAppId:" + getAppId());
		regReq[0] = new FetchRequest(getAppId(), storageImpl.getAllowedAaids(channel)).getRegistrationRequest(username);
//		ArrayList<RegistrationRecord> useRegRecord = storageImpl.getRegistrationRecord(username);
		/*
		 * if (useRegRecord == null) {
		 * 
		 * IdgExtension idexts1 = new IdgExtension(); //idexts.authtype = "0"; Extension
		 * ext1 = new Extension(); //ext.id = "ToppeniDGate"; //ext.fail_if_unknown =
		 * false; //ext.data = gson.toJson(idexts); Extension[] exts = new Extension[3];
		 * idexts1.authtype = "0"; //Extension ext = new Extension(); ext1.id =
		 * "ToppeniDGate"; ext1.fail_if_unknown = false; ext1.data =
		 * Base64.encodeBase64URLSafeString(gson.toJson(idexts1).getBytes()); exts[0] =
		 * ext1;
		 * 
		 * IdgExtension idexts2 = new IdgExtension(); idexts2.authtype = "1"; Extension
		 * ext2 = new Extension(); ext2.id = "ToppeniDGate"; ext2.fail_if_unknown =
		 * false; ext2.data =
		 * Base64.encodeBase64URLSafeString(gson.toJson(idexts2).getBytes()); exts[1] =
		 * ext2;
		 * 
		 * IdgExtension idexts3 = new IdgExtension(); idexts3.authtype = "2"; Extension
		 * ext3 = new Extension(); ext3.id = "ToppeniDGate"; ext3.fail_if_unknown =
		 * false; ext3.data =
		 * Base64.encodeBase64URLSafeString(gson.toJson(idexts3).getBytes()); exts[2] =
		 * ext3; regReq[0].header.exts = exts; } else { Extension[] exts = new
		 * Extension[3-useRegRecord.size()];
		 * 
		 * }
		 */
//		Dash.getInstance().stats.put(Dash.LAST_REG_REQ, regReq);
//		Dash.getInstance().history.add(regReq);
		return regReq;
	}

	/**
	 * List of trusted Application Facet ID. An (application) facet is how an
	 * application is implemented on various platforms. For example, the application
	 * MyBank may have an Android app, an iOS app, and a Web app. These are all
	 * facets of the MyBank application.
	 *
	 * A platform-specific identifier (URI) for an application facet. For Web
	 * applications, the facet id is the RFC6454 origin [RFC6454]. For Android
	 * applications, the facet id is the URI
	 * android:apk-key-hash:<hash-of-apk-signing-cert> For iOS, the facet id is the
	 * URI ios:bundle-id:<ios-bundle-id-of-app>.
	 *
	 * @return List of trusted Application Facet ID.
	 */
	@GET
	@Path("/public/uaf/facets")
	@Produces("application/fido.trusted-apps+json")
	public Facets facets() {
//		String timestamp = new Date().toString();
//		Dash.getInstance().stats.put(Dash.LAST_REG_REQ, timestamp);
		String[] trustedIds = { "ios:bundle-id:com.idgate.fido", "android:apk-key-hash:hoG2zCMzhpGCNnLFceBuVpPfDqM" };
		List<String> trustedIdsList = new ArrayList<String>(Arrays.asList(trustedIds));
//		trustedIdsList.addAll(Dash.getInstance().facetIds);
		trustedIdsList.add(readFacet());
		Facets facets = new Facets();
		facets.trustedFacets = new TrustedFacets[1];
		TrustedFacets trusted = new TrustedFacets();
		trusted.version = new Version(1, 0);
		trusted.ids = trustedIdsList.toArray(new String[0]);
		facets.trustedFacets[0] = trusted;
		return facets;
	}

	private String readFacet() {
		InputStream in = getClass().getResourceAsStream("config.properties");
		String facetVal = "";
		try {
			Properties props = new Properties();
			props.load(in);
			facetVal = props.getProperty("facetId");
		} catch (IOException e) {
			Log4j.log.error(e);
			
			// e.printStackTrace();
		}
		return facetVal.toString();
	}

	/**
	 * The AppID is an identifier for a set of different Facets of a relying party's
	 * application. The AppID is a URL pointing to the TrustedFacets, i.e. list of
	 * FacetIDs related to this AppID.
	 * 
	 * @return a URL pointing to the TrustedFacets
	 */
	@Context
	UriInfo uriInfo;

	private String getAppId() {
		// You can get it dynamically.
		// It only works if your server is not behind a reverse proxy
		return uriInfo.getBaseUri() + appID;
		// Or you can define it statically
//		return "https://www.head2toes.org/fidouaf/v1/public/uaf/facets";
	}

	// for mock test
//    public ProcessResponse processResponse;
//	public ProcessResponse getProcessResponse() {
//		return processResponse;
//	}
//	public void setProcessResponse(ProcessResponse processResponse) {
//		this.processResponse = processResponse;
//	}

	@POST
	@Path("/public/regResponse")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public RegistrationRecord[] processRegResponse(String payload , @PathParam("channel") String channel , @PathParam("sessionID") String sessionID) {
		InboundLogFormat inLogObj = null;
		APLogFormat apLogObj = null;
		try {
			inLogObj = new InboundLogFormat();
			apLogObj = new APLogFormat();
		} catch (UnknownHostException e) {
			if (sessionID != null) {
		        logException(sessionID, e, "UAFResponse", "Unknown Host Exception", null, null);
		        Log4j.log.warn("[UAFResponse][{}] UnknownHostException:{}", sessionID, e.getMessage());
		    } else {
		    	Log4j.log.error(e);
		    }
		}
		RegistrationRecord[] result = null;
		if (!payload.isEmpty()) {
			RegistrationResponse[] fromJson = (new Gson()).fromJson(payload, RegistrationResponse[].class);
//			Dash.getInstance().stats.put(Dash.LAST_REG_RES, fromJson);
//			Dash.getInstance().history.add(fromJson);

			RegistrationResponse registrationResponse = fromJson[0];
			// for mock test
//			result = this.processResponse.processRegResponse(registrationResponse,username);
			result = this.processResponse.processRegResponse(registrationResponse);
			String username = getUserName(payload);	// TODO
			if (result[0].status.equals("SUCCESS")) {
				try {
					// validate
//					if (username == null || username.trim().equals("")) {
//						Log4j.log.error("*** username:{} is required.", username);
//						result = new RegistrationRecord[1];
//						result[0] = new RegistrationRecord();						
//						result[0].status = "Username cannot be empty.";
//						return result;
//					}

//					doDereg(username); // username 前端檢核 TODO 重覆註冊
					// Log4j.log.info("*** store(result):" + gson.toJson(result));
					storageImpl.store(result, username, channel); // "Duplicate Key"
				} catch (DuplicateKeyException e) {
					logException(sessionID, e, "processRegResponse", "DuplicateKey Exception", apLogObj, inLogObj);
					Log4j.log.error(e);
					result = new RegistrationRecord[1];
					result[0] = new RegistrationRecord();
					result[0].status = "Duplicate Key";
				} catch (SystemErrorException e) {
					logException(sessionID, e, "processRegResponse", "SystemError Exception", apLogObj, inLogObj);
					Log4j.log.error(e);
					result = new RegistrationRecord[1];
					result[0] = new RegistrationRecord();
					result[0].status = "Data cannot be stored in DB";
				} catch (AAIDnotAllowedException e) {
					logException(sessionID, e, "processRegResponse", "AAIDnotAllowed Exception", apLogObj, inLogObj);
					Log4j.log.error(e);
					result = new RegistrationRecord[1];
					result[0] = new RegistrationRecord();
					result[0].status = "AAID is not Allowed";
				} catch (SQLException e) {
					logException(sessionID, e, "processRegResponse", "SQL Exception", apLogObj, inLogObj);
					Log4j.log.error(e);
					result = new RegistrationRecord[1];
					result[0] = new RegistrationRecord();
					result[0].status = "NO AAID in Table";
				}
			}
		} else {
			// TODO Could be interesting refactor this method (and its callers) and modify
			// return type to javax.ws.rs.core.Response and send
			// Response.Status.PRECONDITION_FAILED error code.
			result = new RegistrationRecord[1];
			result[0] = new RegistrationRecord();
			result[0].status = "payload could not be empty";
		}
		return result;
	}

	private String getUserName(String fidoStr) {
		String serverData;
		List<Map<String, Object>> fidoList = gson.fromJson(fidoStr, new TypeToken<List<Map<String, Object>>>() {
		}.getType());
		Map<String, Object> fidoMap = (Map<String, Object>) fidoList.get(0);
		// Log4j.log.info("*** fidoMap:" + fidoMap); // ["AT4kAQM-2AALLgkAMDA4QSMw
		@SuppressWarnings("unchecked")
		Map<String, Object> header = (Map<String, Object>) fidoMap.get("header");
		serverData = (String) header.get("serverData");
		// Log4j.log.info("*** serverData@processRegResponse:" + serverData);
		String serverDataB64Decode = new String(Base64.decodeBase64(serverData));
		// Log4j.log.info(" decodeBase64.serverDataB64:" + serverDataB64Decode);
		String[] tokens = serverDataB64Decode.split("\\.");
		String username = tokens[2];
		username = new String(Base64.decodeBase64(username));
		// Log4j.log.info("*** username@processRegResponse:{}", username);
		return username;
	}

	@POST
	@Path("/public/deregRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String deregRequestPublic(String payload) {

		return new DeregRequestProcessor().process(payload);
	}


	private void setTransaction(String trxContent, AuthenticationRequest[] authReqObj) {
		authReqObj[0].transaction = new Transaction[1];
		Transaction t = new Transaction();
		t.content = trxContent;
		t.contentType = MediaType.TEXT_PLAIN;
		authReqObj[0].transaction[0] = t;
	}

//	@SuppressWarnings("unused")
//	private void setTransactionwithimage(String trxContent, AuthenticationRequest[] authReqObj) {
//		authReqObj[0].transaction = new Transaction[1];
//		Transaction t = new Transaction();
//		t.content = trxContent;
//		t.contentType = "image/png";
//		authReqObj[0].transaction[0] = t;
//	}

	public AuthenticationRequest[] getAuthReqObj(String channel) {
		AuthenticationRequest[] ret = new AuthenticationRequest[1];
		ret[0] = new FetchRequest(getAppId(), storageImpl.getAllowedAaids(channel)).getAuthenticationRequest();
//		Dash.getInstance().stats.put(Dash.LAST_AUTH_REQ, ret);
//		Dash.getInstance().history.add(ret);
		return ret;
	}

	@POST
	@Path("/public/authResponse")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AuthenticatorRecord[] processAuthResponse(String payload) {
		if (!payload.isEmpty()) {
//			Dash.getInstance().stats.put(Dash.LAST_AUTH_RES, payload);
			Gson gson = new Gson();
			AuthenticationResponse[] authResp = gson.fromJson(payload, AuthenticationResponse[].class);
//			Dash.getInstance().stats.put(Dash.LAST_AUTH_RES, authResp); // TODO: Fix
//			Dash.getInstance().history.add(authResp);
			AuthenticatorRecord[] result = this.processResponse.processAuthResponse(authResp[0]);
			return result;
		}
		return new AuthenticatorRecord[0];
	}


	/**
	 * 
	 * @param payload
	 * @return
	 * @throws UnknownHostException
	 */
//	@Path("/public/uafRequest")
	@SuppressWarnings({ "unused" })
	@POST
	@Path("/public/get")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String GetUAFRequest(String payload, @Context HttpServletRequest request) {
		String uafReq = null;
		String sessionID = request.getSession(true).getId();
		InboundLogFormat inLogObj = null;
		APLogFormat apLogObj = null;
		try {
		long beginTime = System.currentTimeMillis();
		Log4j.log.info("[UAFRequest][{}] inbound:{}", sessionID, payload);
		//		Log4jAP.log.info("[UAFRequest] inbound:" + payload);	// TEST
//		Log4jSQL.log.info("[UAFRequest] inbound:" + payload);	// TEST
		try {
			inLogObj = new InboundLogFormat();
			apLogObj = new APLogFormat();
			
			inLogObj.setTraceID(sessionID);
			inLogObj.setClazz("com.toppanidgate.fidouaf.res.FidoUafResource");
			apLogObj.setTraceID(sessionID);
			apLogObj.setClazz("com.toppanidgate.fidouaf.res.FidoUafResource");
		} catch (UnknownHostException e) {
			logException(sessionID, e, "GetUAFRequest", "Unknown Host Exception", apLogObj, inLogObj);
			Log4j.log.warn("[UAFResponse][{}] UnknownHostException:{}", sessionID, e.getMessage());
		}


		if (!payload.isEmpty()) {

			Gson gson = new Gson();
			GetUAFRequest req = gson.fromJson(payload, GetUAFRequest.class);

			UAFUserName reqUserName = gson.fromJson(req.context, UAFUserName.class);
			Log4j.log.info(" req.op.name : " + req.op.name());
			// Log4j.log.info("*** UAFUserName.username: " + reqUserName.username);
			// Log4j.log.info("*** UAFUserName.transaction: " + reqUserName.transaction);
			// Log4j.log.info("*** UAFUserName.keyType: " + reqUserName.keyType);

			if (req.op.name().equals("Reg")) { // Step1
				// store request msg into inbound request log part
				inLogObj.setRequestForGet(payload);
				// result = getRegisReqPublic(reqUserName.username);
				ReturnUAFRegistrationRequest uafRegReq = new ReturnUAFRegistrationRequest();
				// RegistrationRequest[] result = getRegisReqPublic("iafuser01");
				MatchCriteria[] disalloweds = null;
//				System.out.println("*** reqUserName.channel:" + reqUserName.channel);
				String[] allowedAaids = storageImpl.getAllowedAaids(reqUserName.channel);

				RegistrationRequest[] result = getRegisReqPublic(reqUserName.username, reqUserName.channel);

//				List<RegistrationRecord> valueList = storageImpl.getRegistrationRecords(reqUserName.username);
				List<RegistrationRecord> valueList = storageImpl.readRegistrationRecords(reqUserName.username);
//				List<String> valueList = storageImpl.getAllKeysByName(reqUserName.username);
				// Log4j.log.info("*** KeySet.size@RegReq_Step1:" + valueList.size());
				if (!valueList.isEmpty()) {
//					disalloweds = new MatchCriteria[KeySet.size()];
					Map<String, List<String>> disAllowedMap = new HashMap<>();
					int i = 0;
					for (RegistrationRecord regRecord : valueList) {
//						RegistrationRecord regRecord = storageImpl.readRegistrationRecord(regRecord,
//								reqUserName.username);
						String[] aaid = new String[1];
						String[] keyID = new String[1];
						aaid[0] = regRecord.authenticator.AAID;
						// TODO TEST ONLY
//						keyID[0] = Base64.encodeBase64String("abcde?B<>1234".getBytes())
//								.replace("/", "_").replace("+", "-");
						keyID[0] = regRecord.authenticator.KeyID;
						if (disAllowedMap.get(aaid[0]) == null) {
							List<String> keyIDsList = new ArrayList<>();
							keyIDsList.add(keyID[0]);
							disAllowedMap.put(aaid[0], keyIDsList);
						} else {
							disAllowedMap.get(aaid[0]).add(keyID[0]);
						}
//						 [
//		                    {
//		                        "aaid": [
//		                            "5431#3280"
//		                        ]
//		                    }
//		                ],
//		                [
//		                    {
//		                        "aaid": [
//		                            "FFFF#FC01"
//		                        ]
//		                    }
//		                ],
//						MatchCriteria disallowed = new MatchCriteria();
//						disallowed = new MatchCriteria();
//						disallowed.aaid = new String[1];
//						disallowed.keyIDs = new String[1];
//						disallowed.aaid = aaid;
//						disallowed.keyIDs = keyID;
//						disalloweds[i] = disallowed;

						i++;
					}
					// 依aaid歸類
//					[
//					    {
//					        "aaid": [
//					            "5431#3280"
//					        ],
//					        "keyIDs": [
//					            "WDNExynbMBAsG4VpK26U3LfgPQaCh6ieWqYCoD5WxGw=",
//					            "LNeQl5STvY0AbfM86Bdz_R3QGClly14lN3RM79Hpb0Y=",
//					            "4wlxGvrYF0aZIxFOLS6xkTHJkNqjeR7XfPlT9v5gS3A="
//					        ]
//					    }
//					]
					if (disAllowedMap.entrySet().size() > 0) {
						int entrySetSize = disAllowedMap.entrySet().size();
						disalloweds = new MatchCriteria[entrySetSize];
						int index = 0;
						for (Entry<String, List<String>> entry : disAllowedMap.entrySet()) {
							MatchCriteria disallowed = new MatchCriteria();
							disallowed.aaid = new String[1];
//							disallowed.keyIDs = new String[entry.getValue().size()];	// ArrayList.size
							disallowed.aaid[0] = entry.getKey();
							disallowed.keyIDs = entry.getValue().toArray(new String[0]);
//							disallowed.keyIDs = entry.getValue().stream().toArray(String[]::new); // Failed to start service jboss.deployment.unit."fidouaf.war".undertow-deployment:
							disalloweds[index] = disallowed;
							index++;
						}
					}
//					Log4j.log.info("*** disalloweds.toJson:" + gson.toJson(disalloweds));
					// Log4j.log.info("*** disalloweds.toJson:" + new
					// GsonBuilder().disableHtmlEscaping().create().toJson(disalloweds));
					result[0].policy.disallowed = disalloweds;
				}

				// MatchCriteria[] disallowed = new

				if (result != null) {
					uafRegReq.statusCode = 1200; // Step1
					uafRegReq.uafRequest = result;
//					uafRegReq.uafRequest = gson.toJson(result);
					// uafRegReq.uafRequest = "[{\\\"header\\\":{\\\"upv\\\":{\\\"major\\\":
					// 1,\\\"minor\\\":
					// 0},\\\"op\\\":\\\"Reg\\\",\\\"appID\\\":\\\"http://localhost:8080/fidouaf/v1/public/uaf/facets\\\",\\\"serverData\\\":
					// \\\"c3hWVTRVZjZIc1I0RGVlbWxzRVJwYVV1ODRzTm9PRmRMd2RQeDBSY0FmVS5NVFl6TWpjeE56Y3pNekV5T1EuWVd4cFkyVS5Ta1JLYUVwRVJYZEtSWEIxWkVaQ2JsRnJWWGxVUnpGTldUQm9SRnBzY0ZsU2JYQkdUMU0w\\\",\\\"exts\\\":
					// [{\\\"id\\\": \\\"ToppeniDGate\\\",\\\"data\\\":
					// \\\"eyJhdXRodHlwZSI6IjAifQ\\\",\\\"fail_if_unknown\\\":false},{\\\"id\\\":\\\"ToppeniDGate\\\",\\\"data\\\":
					// \\\"eyJhdXRodHlwZSI6IjEifQ\\\",\\\"fail_if_unknown\\\":false}]},\\\"challenge\\\":
					// \\\"JDJhJDEwJEpudFBnQkUyTG1MY0hDZlpYRmpFOS4\\\",\\\"username\\\":\\\"alice\\\",\\\"policy\\\":{\\\"accepted\\\":[[{\\\"aaid\\\":
					// [\\\"5431#3280\\\"]}]]}}]\";
					uafRegReq.op = Operation.Reg;
					uafRegReq.lifetimeMillis = 5L * 60 * 1000;
				} else {
					uafRegReq.statusCode = 1500;
				}
//				uafReq = gson.toJson(uafRegReq);	// = -> \u003d
				uafReq = new GsonBuilder().disableHtmlEscaping().create().toJson(uafRegReq); // = is remained

			} else if (req.op.name().equals("Auth")) { // Step3
				// store request msg into inbound request log part
				inLogObj.setRequestForGetAuth(payload);
				ReturnUAFAuthenticationRequest uafAuthReq = new ReturnUAFAuthenticationRequest();
				String[] allowedAaids = storageImpl.getAllowedAaids(reqUserName.channel);
				if (allowedAaids != null) {
					AuthenticationRequest[] result = getAuthReqObj(reqUserName.channel);
					// Log4j.log.info("*** reqUserName.channel:[{}]", reqUserName.channel);
					List<String> KeySet = storageImpl.getAllKeysByName(reqUserName.username);
					// Log4j.log.info("*** KeySet.size@AuthReq_Step3:" + KeySet.size());
					if (!KeySet.isEmpty()) {
						MatchCriteria[][] accepted = new MatchCriteria[1][1];
//					MatchCriteria[][] accepted = new MatchCriteria[KeySet.size()][1];
						MatchCriteria[] disalloweds = new MatchCriteria[KeySet.size()];
						// MatchCriteria[] disallowed = new MatchCriteria[KeySet.size()];
						// MatchCriteria matchCriteria = new MatchCriteria();
						// MatchCriteriaNoAAID matchCriteriaNoAAID = new MatchCriteriaNoAAID();
						boolean isAllowed = false;
						int index = 0;
						for (String entry : KeySet) {
							if (Integer.parseInt(reqUserName.keyType) == index) {
								MatchCriteria[] accepts = new MatchCriteria[1];
								String Key = entry;
								// Log4j.log.info(" *** Key: " + Key);
								MatchCriteria matchCriteria = new MatchCriteria();
								MatchCriteria disallowed = new MatchCriteria();

								RegistrationRecord regRecord = storageImpl.readRegistrationRecord(Key,
										reqUserName.username);

								// Log4j.log.info("*** AAID@AuthRequest2:" + regRecord.authenticator.AAID);
								// Log4j.log.info("*** KeyID:" + regRecord.authenticator.KeyID);
//							try {
//								// Log4j.log.info("*** KeyID@encode:" + URLEncoder.encode(regRecord.authenticator.KeyID, "UTF-8"));
//							} catch (UnsupportedEncodingException e1) {
//								logException(sessionID, e1, "GetUAFRequest", "Unknown Host Exception");
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}

								if (Arrays.asList(allowedAaids).contains(regRecord.authenticator.AAID)) {
									String[] aaid = new String[1];
									String[] keyID = new String[1];
									aaid[0] = regRecord.authenticator.AAID;
									keyID[0] = regRecord.authenticator.KeyID;
//								keyID[0] = regRecord.authenticator.KeyID; 
//									keyID[0] = URLEncoder.encode(regRecord.authenticator.KeyID, "UTF-8");	// = -> %3D
									matchCriteria.aaid = new String[1];
									matchCriteria.keyIDs = new String[1];
									matchCriteria.aaid = aaid;
									matchCriteria.keyIDs = keyID;
									disallowed.aaid = new String[1];
									disallowed.keyIDs = new String[1];
									disallowed.aaid = aaid;
									disallowed.keyIDs = keyID;
									accepts[0] = matchCriteria;
									accepted[0] = accepts;
									isAllowed = true;
								}

								String toppaniDGateID = "5431#3280";
								// Log4j.log.info("*** is AAID.equals(toppaniDGateID):" +
								// (regRecord.authenticator.AAID.equals(toppaniDGateID) &&
								// reqUserName.transaction != null));
								if (regRecord.authenticator.AAID.equals(toppaniDGateID)
										&& reqUserName.transaction != null) {
									// 只接受 text/plain
//								setTransaction(Base64.encodeBase64URLSafeString("test".getBytes()), // OK
//										result);

									// OK 資料不能太多
////								setTransaction(Base64.encodeBase64URLSafeString(reqUserName.transaction.getBytes()), 
//										result);

									// 先 sha256 再 Base64 才 OK 111.6.15
//								setTransaction(Base64.encodeBase64URLSafeString(	// OK => AssertionError
//										SHA.sha256(reqUserName.transaction).getBytes()), result);

									// OK 111.6.17
									setTransaction( // *** length3: 206
											Base64.encodeBase64String(SHA.sha256(reqUserName.transaction).getBytes())
													.replace("/", "_").replace("+", "-"),
											result);

//								accepted[0] = accepts;
//								isAllowed = true;
//								accepted[i] = accepts;
								} else if (regRecord.authenticator.AAID.equals(toppaniDGateID)) {
//								accepted[0] = accepts;
//								isAllowed = true;
//								accepted[i] = accepts;
								} else if (reqUserName.transaction != null) {
									// setTransaction(Base64.encodeBase64URLSafeString(reqUserName.transaction.getBytes()),
//											result);
									// 先 sha256 再 Base64 才 OK 111.6.15
//								setTransaction(Base64.encodeBase64URLSafeString(
//										SHA.sha256(reqUserName.transaction).getBytes()), result);

									// OK 111.6.17
									setTransaction( // *** length3: 206
											Base64.encodeBase64String(SHA.sha256(reqUserName.transaction).getBytes())
													.replace("/", "_").replace("+", "-"),
											result);
								}
							}

							index++;
						}

						if (isAllowed) {
							result[0].policy.accepted = accepted;
						}
						// result[0].policy.accepted = accepted;
						// result[0].policy.disallowed = disalloweds;
						/*
						 * for (String entry : KeySet) { String Key = entry; RegistrationRecord
						 * regRecord = storageImpl.readRegistrationRecord(Key); if
						 * (regRecord.authenticator.AAID.equals("FFFF#FC02")) {
						 * 
						 * disallowed = new MatchCriteria[KeySet.size()]; int i = 0; for (String entry1
						 * : KeySet) { String Key1 = entry; MatchCriteria disallowed = new
						 * MatchCriteria(); RegistrationRecord regRecord1 =
						 * storageImpl.readRegistrationRecord(Key); String[] aaid = new String[1];
						 * String[] keyID = new String[1]; aaid[0] = regRecord.authenticator.AAID;
						 * keyID[0] = regRecord.authenticator.KeyID; disallowed.aaid = new String[1];
						 * disallowed.keyIDs = new String[1]; disallowed.aaid = aaid; disallowed.keyIDs
						 * = keyID;
						 * 
						 * disalloweds[i] = disallowed; i++; } result[0].policy.disallowed =
						 * disalloweds; if (reqUserName.transaction != null) {
						 * setTransactionwithimage(Base64.encodeBase64URLSafeString(reqUserName.
						 * transaction.getBytes()), result); } } else { if (reqUserName.transaction !=
						 * null)
						 * setTransaction(Base64.encodeBase64URLSafeString(reqUserName.transaction.
						 * getBytes()), result); } }
						 */
					}
					if (result != null) {
						// FIXME: CHECK statusCode
//					uafAuthReq.statusCode = 1200;
						if (!KeySet.isEmpty()) {
							uafAuthReq.statusCode = 1200; // Step3
						} else {
							uafAuthReq.statusCode = 1500;
							Log4j.log.error("Cannot find devkey for " + reqUserName.username);
						}
						/*
						 * Set<String> KeySet = storageImpl.getKeysByValue(reqUserName.username); for
						 * (String entry : KeySet) { if (entry == "") { uafAuthReq.statusCode = 1401; }
						 * else { uafAuthReq.statusCode = 1200; break; } }
						 */

						uafAuthReq.uafRequest = result;
//					uafAuthReq.uafRequest = gson.toJson(result);
						uafAuthReq.op = Operation.Auth;
						uafAuthReq.lifetimeMillis = 5L * 60 * 1000;
					} else {
						uafAuthReq.statusCode = 1500;
					}
				} else {
					uafAuthReq.statusCode = 1600;
				}
//				uafReq = gson.toJson(uafRegReq); // = -> \u003d
				uafReq = new GsonBuilder().disableHtmlEscaping().create().toJson(uafAuthReq); // = is remained

			} else if (req.op.name().equals("Dereg")) { // Step5
				// store request msg into inbound request log part
				inLogObj.setRequestForGet(payload);
				GetUAFDeregRequest DeregRequest = gson.fromJson(req.context, GetUAFDeregRequest.class);
				DeregistrationRequest[] deregReq = new DeregistrationRequest[1];
				String username = DeregRequest.username;
				// Log4j.log.info(" DeregRequest.username: " + username);
				String result = "Failure: Problem in deleting record from local DB";
				if (DeregRequest != null) {
					result = doDereg(username, sessionID, apLogObj, inLogObj);
				}
//				else { 
//					result = deregRequestPublic(payload);
//				}
				ReturnUAFDeregistrationRequest uafDeregReq = new ReturnUAFDeregistrationRequest();
				if (result.equalsIgnoreCase("Success")) {
					uafDeregReq.statusCode = 1200;
				} else if (result.equalsIgnoreCase("Failure: Problem in deleting record from local DB")) {
					uafDeregReq.statusCode = 1404;
				} else if (result.equalsIgnoreCase("Failure: Problem in processing deregistration request")) {
					uafDeregReq.statusCode = 1492;
				} else {
					uafDeregReq.statusCode = 1500;
				}
//				uafDeregReq.uafRequest = gson.toJson(deregReq);
				// uafDeregReq.op = null;
				// uafDeregReq.lifetimeMillis = null;
				uafReq = new GsonBuilder().disableHtmlEscaping().create().toJson(uafDeregReq); // = is remained
//				uafReq = gson.toJson(uafDeregReq);	// = -> \u003d
			} else if (req.op.name().equals("Hcheck")) {
				Authenticator authenticator = storageImpl.healthCheck();
				ReturnUAFDeregistrationRequest uafDeregReq = new ReturnUAFDeregistrationRequest();
				if (authenticator != null) {
					uafDeregReq.statusCode = 1200;
				} else {
					uafDeregReq.statusCode = 1500;
				}
				uafReq = new GsonBuilder().disableHtmlEscaping().create().toJson(uafDeregReq);
			}
		}
//		Log4j.log.info("[" + sessionID + "] ReturnMsg: " + uafReq + "\r\n");
//		apLogObj.setMessage("[" + sessionID + "] ReturnMsg: " + uafReq + "\r\n");
//		Log4jAP.log.info(apLogObj.getCompleteTxt());

		long totalTime = (System.currentTimeMillis() - beginTime);

		// store response msg into inbound response log part
		try {
//			System.out.println("*** sessionID" + sessionID + "\nuafReq:" + uafReq);
			Log4j.log.info("[UAFRequest][{}] outbound:{} \r\n", sessionID, uafReq);
			inLogObj.setResponseTxt(uafReq);
		} catch (JsonSyntaxException | JsonProcessingException e) {
			logException(sessionID, e, "GetUAFRequest", "Json Exception", apLogObj, inLogObj);
			Log4jInbound.log.warn(e);
		}
		Log4j.log.info("[UAFRequest][{}] outbound:{} \ntotalTime:{}ms", sessionID, uafReq, totalTime);
		
//			System.out.println("*** sessionID:" + sessionID 
//					+ "\ntotalTime: " + totalTime
//					+ "\nuafReq: " + uafReq
//					);
		inLogObj.setExecuteTime(totalTime);
		if (inLogObj.hasException()) {
			Log4jInbound.log.warn(inLogObj.getCompleteTxt(sessionID));
		} else {
			Log4jInbound.log.info(inLogObj.getCompleteTxt(sessionID));
		}

		return uafReq;
		} catch (Exception e) {
			logException(sessionID, e, "GetUAFRequest", "Exception", apLogObj, inLogObj);
		}
		ReturnUAFDeregistrationRequest uafDeregReq = new ReturnUAFDeregistrationRequest();
		uafDeregReq.statusCode = 1500;
		uafReq = new GsonBuilder().disableHtmlEscaping().create().toJson(uafDeregReq); // = is remained
		return uafReq;
	}

	public String doDereg(String username, String sessionID, APLogFormat apLogObj, InboundLogFormat inLogObj) {
		String result = null;
		// Log4j.log.info("*** Dereg username:{}", username);
		try {
			storageImpl.disable(username);
			result = "Success";
		} catch (Exception e) {
			logException(sessionID, e, "doDereg", "Exception", apLogObj, inLogObj);
			Log4j.log.error("*** Exception: Problem in deleting record from local DB \n" + e);
			result = "Failure: Problem in deleting record from local DB";
			// e.printStackTrace();
		}
		return result;
	}

//	@Path("/public/uafResponse")
	@POST
	@Path("/public/respond")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServerResponse UAFResponse(String payload, @Context HttpServletRequest request) {
		long beginTime = System.currentTimeMillis();
		String sessionID = request.getSession(true).getId();
		Log4j.log.info("[UAFResponse][{}] inbound:{}", sessionID, payload);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
		Log4j.log.info("[*** begin UAFResponse][{}] mm:ss.SSS:{}", sessionID, simpleDateFormat.format(new Date()));
		InboundLogFormat inLogObj = null;
		APLogFormat apLogObj = null;
//		Log4jAP.log.info("[UAFResponse] inbound:" + payload);	// TEST
//		Log4jSQL.log.info("[UAFResponse] inbound:" + payload);	// TEST
		try {
			apLogObj = new APLogFormat();
			apLogObj.setTraceID(sessionID);
			apLogObj.setClazz("com.toppanidgate.fidouaf.res.FidoUafResource");
			inLogObj = new InboundLogFormat();
			inLogObj.setTraceID(sessionID);
			inLogObj.setClazz("com.toppanidgate.fidouaf.res.FidoUafResource");
		} catch (UnknownHostException e) {
			logException(sessionID, e, "UAFResponse", "Unknown Host Exception", apLogObj, inLogObj);
			Log4j.log.warn("[UAFResponse][{}] UnknownHostException:{}", sessionID, e.getMessage());
		}

		ServerResponse servResp = new ServerResponse();
		if (!payload.isEmpty()) {
			String findOp = payload;
			/*
			 * findOp = findOp.substring(findOp.indexOf("op") + 7, findOp.indexOf(",",
			 * findOp.indexOf("op")) - 2); Log4j.log.info("findOp=" + findOp);
			 */
			GetUAFResponse req = new GetUAFResponse();
			// UAFUserName reqUserName = new UAFUserName();
			try {
				req = gson.fromJson(payload, GetUAFResponse.class);
				RegistrationResponse[] regResponse = (new Gson()).fromJson(req.uafResponse,
						RegistrationResponse[].class);
				// Log4j.log.info("RegistrationResponse count=" + fromJson.length);
//				Log4j.log.info("RegistrationResponse count=" + fromJson.length);
				// findOp = fromJson[0].header.op.eguals("");
				// findOp = findOp.replace(fromJson[0].header.appID, "");

				findOp = (new Gson()).toJson(regResponse[0].header);
				OperationHeaderforin Headerforin = (new Gson()).fromJson(findOp, OperationHeaderforin.class);
				findOp = Headerforin.op;

				/*
				 * findOp = findOp.replace(fromJson[0].header.appID, ""); findOp =
				 * findOp.substring(findOp.indexOf("op") + 7, findOp.indexOf(",",
				 * findOp.indexOf("op")) - 2);
				 */
				// findOp =;
				// Log4j.log.info("findOp=" + findOp);
				Log4j.log.info("[{}] res.op.name:{}", sessionID, findOp);
				// reqUserName = gson.fromJson(req.context, UAFUserName.class);
				if (findOp == null) {
					findOp = "";
				}
			} catch (Exception e) {
				logException(sessionID, e, "UAFResponse", "Exception", apLogObj, inLogObj);
				Log4j.log.error("*** Exception@UAFResponse:" + e);
				servResp.statusCode = 1500;
				servResp.Description = e.getMessage();
				return servResp;
				// throw new Exception("Server data check failed");
			}
			Log4j.log.info("[*** before if UAFResponse][{}] mm:ss.SSS:{}", sessionID, simpleDateFormat.format(new Date()));
			if (findOp.equals("Reg")) { // Step2
				// store request msg into inbound request log part
				inLogObj.setRequestForResponseOpReg(payload);

				// RegistrationRecord[] result = processRegResponse(payload);
				RegistrationRecord[] result = null;
//				UAFUserName reqUserName = gson.fromJson(req.context, UAFUserName.class);
				try {
					result = processRegResponse(req.uafResponse, req.channel, sessionID);
				} catch (Exception e) {
					logException(sessionID, e, "UAFResponse", "Exception", apLogObj, inLogObj);
					Log4j.log.error(" *** Exception@processRegResponse :" + e);
					servResp.statusCode = 1500;
					servResp.Description = e.getMessage();
					return servResp;
					// throw new Exception("Server data check failed");
					// servResp.Description = result[0].status;
				}

				if (result[0].status.equals("SUCCESS")) {
					servResp.statusCode = 1200;
					servResp.Description = "OK. Operation completed";
//					servResp.newUAFRequest = new Gson().toJson(result);
					servResp.newUAFRequest = new GsonBuilder().disableHtmlEscaping().create().toJson(result);
				} else if (result[0].status.equals("ASSERTIONS_CHECK_FAILED")) {
					logException(sessionID, new Exception("1496"), "UAFResponse", "ASSERTIONS CHECK FAILED", apLogObj, inLogObj);
					servResp.statusCode = 1496;
					servResp.Description = result[0].status;
				} else if (result[0].status.equals("Duplicate Key")) {
					logException(sessionID, new Exception("1405"), "UAFResponse", "Duplicate Key", apLogObj, inLogObj);
					servResp.statusCode = 1405;
					servResp.Description = result[0].status;
				} else if (result[0].status.equals("Data cannot be stored in DB")) {
					logException(sessionID, new Exception("1406"), "UAFResponse", "Data cannot be stored in DB", apLogObj, inLogObj);
					servResp.statusCode = 1406;
					servResp.Description = result[0].status;
				} else if (result[0].status.equals("AAID is not Allowed")) {
					logException(sessionID, new Exception("1407"), "UAFResponse", "AAID is not Allowed", apLogObj, inLogObj);
					servResp.statusCode = 1407;
					servResp.Description = result[0].status;
				} else if (result[0].status.equals("NO AAID in Table")) {
					logException(sessionID, new Exception("1600"), "UAFResponse", "NO AAID in Table", apLogObj, inLogObj);
					servResp.statusCode = 1600;
					servResp.Description = result[0].status;
				} else if (result[0].status.equals("INVALID_SERVER_DATA_EXPIRED")
						|| result[0].status.equals("INVALID_SERVER_DATA_SIGNATURE_NO_MATCH")
						|| result[0].status.equals("INVALID_SERVER_DATA_CHECK_FAILED")) {
					logException(sessionID, new Exception("1491"), "UAFResponse", "INVALID SERVER DATA", apLogObj, inLogObj);
					servResp.statusCode = 1491;
					servResp.Description = result[0].status;
				} else {
					logException(sessionID, new Exception("1500"), "UAFResponse", "Exception", apLogObj, inLogObj);
					servResp.statusCode = 1500;
					servResp.Description = result[0].status;
				}

				// store response msg into inbound response log part
				try {
					inLogObj.setResponseTxtForStep2(new GsonBuilder().disableHtmlEscaping().create().toJson(servResp));
				} catch (JsonSyntaxException | JsonProcessingException e) {
					logException(sessionID, e, "UAFResponse", "Json Exception", apLogObj, inLogObj);
					Log4jInbound.log.warn(e);
				}
			} else if (findOp.equals("Auth")) { // Step4
				// store request msg into inbound request log part
				inLogObj.setRequestForResponseOpAuth(payload);

				// AuthenticatorRecord[] result = processAuthResponse(payload);
				AuthenticatorRecord[] result = null;
				try {
					result = processAuthResponse(req.uafResponse);
					Log4j.log.info("[*** after processAuthResponse UAFResponse][{}] mm:ss.SSS:{}", sessionID, simpleDateFormat.format(new Date()));
					if (result[0].status.equals("SUCCESS")) {
						servResp.statusCode = 1200;
						servResp.Description = "OK. Operation completed";
//						servResp.newUAFRequest = new Gson().toJson(result); // 若不需要，就註解掉這一行
						servResp.newUAFRequest = new GsonBuilder().disableHtmlEscaping().create().toJson(result); // =
																													// is
																													// remained;
					} else if (result[0].status.equals("FAILED_SIGNATURE_NOT_VALID")
							|| result[0].status.equals("FAILED_SIGNATURE_VERIFICATION") // 出現 1496
																						// FAILED_SIGNATURE_VERIFICATION
																						// 重啟 FIDO Server
							|| result[0].status.equals("FAILED_ASSERTION_VERIFICATION")) {
						logException(sessionID, new Exception("1496"), "UAFResponse", "FAILED SIGNATURE", apLogObj, inLogObj);
						servResp.statusCode = 1496;
//						servResp.statusCode = 1200;
						servResp.Description = result[0].status;
					} else if (result[0].status.equals("INVALID_SERVER_DATA_EXPIRED")
							|| result[0].status.equals("INVALID_SERVER_DATA_SIGNATURE_NO_MATCH")
							|| result[0].status.equals("INVALID_SERVER_DATA_CHECK_FAILED")) {
						logException(sessionID, new Exception("1491"), "UAFResponse", "INVALID SERVER DATA", apLogObj, inLogObj);
						servResp.statusCode = 1491;
						servResp.Description = result[0].status;
					} else {
						logException(sessionID, new Exception("1600"), "UAFResponse", "Exception", apLogObj, inLogObj);
						servResp.statusCode = 1600;
						servResp.Description = result[0].status;
					}
				} catch (Exception e) {
					logException(sessionID, e, "UAFResponse", "Exception", apLogObj, inLogObj);
					Log4j.log.error(e);
					servResp.statusCode = 1500;
					servResp.Description = result==null?"Server data check failed":result[0].status;
					return servResp;
					// throw new Exception("Server data check failed");
				}

				// store response msg into inbound response log part
				try {
					inLogObj.setResponseTxtForStep4(new GsonBuilder().disableHtmlEscaping().create().toJson(servResp));
				} catch (JsonSyntaxException | JsonProcessingException e) {
					logException(sessionID, e, "UAFResponse", "Json Exception", apLogObj, inLogObj);
					Log4jInbound.log.warn(e);
				}

			} else {
				servResp.statusCode = 1500;
				servResp.Description = "Not Found OP";
			}
		} else {
			servResp.statusCode = 1500;
			servResp.Description = "payload is empty";
		}
//		Log4j.log.info("[" + sessionID + "] ReturnMsg: " + new GsonBuilder().disableHtmlEscaping().create().toJson(servResp) + "\r\n");
//		apLogObj.setMessage("[" + sessionID + "] ReturnMsg: "
//				+ new GsonBuilder().disableHtmlEscaping().create().toJson(servResp) + "\r\n");
//		Log4jAP.log.info(apLogObj.getCompleteTxt());

		long totalTime = (System.currentTimeMillis() - beginTime);
		Log4j.log.info("[UAFResponse][{}] outbound:{} \ntotalTime:{}ms", sessionID,
				new GsonBuilder().disableHtmlEscaping().create().toJson(servResp), totalTime);
		inLogObj.setExecuteTime(totalTime);
		if (inLogObj.hasException()) {
			Log4jInbound.log.warn(inLogObj.getCompleteTxt(sessionID));
		} else {
			Log4jInbound.log.info(inLogObj.getCompleteTxt(sessionID));
		}

		Log4j.log.debug("*** servResp:" + new Gson().toJson(servResp));
		Log4j.log.info("[*** before return UAFResponse][{}] mm:ss.SSS:{} totalTime:{}ms", sessionID, simpleDateFormat.format(new Date()), totalTime);

		return servResp;
	}
	
	private void logException(String sessID, Exception e, String method, String errMsg, APLogFormat apLogObj, InboundLogFormat inLogObj) {
		Log4j.log.error("[" + sessID + "][Version: " + Config.svVerNo + "][" + method + "] " + errMsg + ": " + e.getMessage()
				+ ", stacktrace: " + new Gson().toJson(e));
		if (apLogObj != null) {
	        Map<String, Object> apLogMap = new HashMap<>();
	        apLogMap.put("sessID", sessID);
	        apLogMap.put("version", Config.svVerNo);
	        apLogMap.put("method", method);
	        apLogMap.put("message", errMsg);
	        apLogMap.put("stacktrace", e.getStackTrace());
	        apLogObj.setMessage(new Gson().toJson(apLogMap));
	        apLogObj.setThrowable(e.getMessage());
	        Log4jAP.log.error(apLogObj.getCompleteTxt());
	    }

	    // Check if inLogObj is not null before using it
	    if (inLogObj != null) {
	        inLogObj.setThrowable(e.getMessage());
	    }
	}

	@PostConstruct
	public void init() {
		Log4j.log.info("========================== Using the normal Log4j.log ==========================");
		try {
			new Log4j(log4j_file_path);
			new Log4jInbound(log4j_file_path);
			new Log4jAP(log4j_file_path);
			new Log4jSQL(log4j_file_path);
		} catch (IOException e) {
			Log4j.log.warn(e);
		}
	}
	
}
