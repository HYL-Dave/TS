package com.ECIES.model;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.KeyAgreement;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.Cvn.Config.Cfg;
import com.Cvn.Core.CvnCore;
import com.Cvn.Database.IDGateOracle;
import com.Cvn.Encryptor.Encode;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.idenkey.common.model.ReturnCode;

// ECDH key generation tool
public class ECCUtil {
	private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
	private static final String SECURITY_PROVIDER = "BC";
	private static final String KEY_GEN_ALGORITHM = "EC";
	private static final String KEY_ALGORITHM = "ECDH";

	private static final int KEY_GEN_WAIT_TIME = 3;
	private static final int KEY_SIZE = 256;

	private static boolean onUpdate = false;
	private static KeyPair keyPair = null;
	
	private static String dbKeyAlias = Cfg.getExternalCfgValue("DbKeyAlias");

	private static CvnCore cvnCore;

	static {
		cvnCore = new CvnCore();
	}

	public ECCUtil(final String sessionId, String keyAlias) throws Exception {
		int retry = 0;

		if (keyPair == null) {
			Log4j.log.info("[" + sessionId + "][" + keyAlias + "] Generating new ECDH keys.");
		}

		while (keyPair == null) {
			if (onUpdate) {
				try {
					// wait while the other is doing key gen
					Thread.sleep((long)KEY_GEN_WAIT_TIME * 1000);
				} catch (InterruptedException e) {
					Log4j.log.error("*** [{}] InterruptedException@ECCUtil:{}", sessionId, e.getMessage());
					throw e;
				}
			} else {
				onUpdate = true;
				
				ECCUtil.loadKeyPair(keyAlias, sessionId);

//				try {
//					KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_GEN_ALGORITHM);
//					keyPairGenerator.initialize(KEY_SIZE);
//					keyPair = keyPairGenerator.generateKeyPair();
//				} catch (NoSuchAlgorithmException e) {
//					throw e;
//				}

				onUpdate = false;
			}

			retry++;
			Log4j.log.info("[{}] Generating new ECDH keys. retry:[{}]", sessionId, retry );
			if (retry == 60) {
				Log4j.log.warn("[" + sessionId + "] A new thread have waited " + KEY_GEN_WAIT_TIME * retry
						+ " seconds and ECDH key is still not generated.");
				break;
			}
		}
	}

	/** Get self generated public key
	 * @param sessID session ID for logging
	 * @param keyAlias 
	 * @return B64 form public key
	 * @throws Exception 
	 */
	public static String getOwnPublicKey(final String sessID, final String keyAlias) throws Exception {
		if (keyPair == null) {
			new ECCUtil(sessID, keyAlias);
		}

		try {
			if (keyPair != null) {
				return Encode.byteToHex(keyPair.getPublic().getEncoded());
			} else {
				throw new NullPointerException("The keyPair is null.");
			}
		} catch (Exception e) {
			Log4j.log.error("*** [{}] Exception@getOwnPublicKey:{}", sessID, e.getMessage());
			throw e;
		}
	}

	/** Get self generated private key
	 * @param sessID session ID for logging
	 * @param keyAlias 
	 * @return B64 form private key
	 * @throws Exception 
	 */
	public static String getOwnPrivateKey(final String sessID, final String keyAlias) throws Exception {
		if (keyPair == null) {
			new ECCUtil(sessID, keyAlias);
		}

		try {
			if (keyPair != null) {
				return Encode.byteToHex(keyPair.getPrivate().getEncoded());
			} else {
				throw new NullPointerException("The keyPair is null.");
			}
		} catch (Exception e) {
			Log4j.log.error("*** [{}] Exception@getOwnPublicKey:{}", sessID, e.getMessage());
			throw e;
		}
	}

	/** Get PublicKey object from binary
	 * @param publicKey binary form public key
	 * @return PublicKey object
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 */
	public static PublicKey getPublicKey(byte[] publicKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

		Security.addProvider(bouncyCastleProvider);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM, SECURITY_PROVIDER);

		return factory.generatePublic(new X509EncodedKeySpec(publicKey));
	}

	/** Get PrivateKey object from binary
	 * @param privateKey binary form private key
	 * @return PrivateKey object
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 */
	public static PrivateKey getPrivateKey(byte[] privateKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

		Security.addProvider(bouncyCastleProvider);
		KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM, SECURITY_PROVIDER);

		return factory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
	}

	/** Generate key pair for external uses
	 * @param keySize key size
	 * @return KeyPair object
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair genKeyPair(int keySize) throws NoSuchAlgorithmException {

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_GEN_ALGORITHM);
		keyPairGenerator.initialize(keySize);

		return keyPairGenerator.generateKeyPair();
	}
	
	// --- Public APIs --- //
	public static String genKeyPair(String keyAlias, String sessionId) {

		 Log4j.log.info("[" + sessionId + "][ECC.genKeyPair] - #keyName:[" + keyAlias + "] - #Generate a key pair...");

		try {

			// Create Keys
			KeyPair myPair = ECCUtil.genKeyPair(KEY_SIZE);

			// 111.12.6 造成誤判，誤以為存 DB 後，存 cache 
//			if ("true".equals(Cfg.getExternalCfgValue("SoftRSA"))) {
//				keyPair = myPair;
//			}

			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(myPair.getPublic().getEncoded());
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(myPair.getPrivate().getEncoded());

//			System.out.println("*** private input:" + Encode.byteToHex(pkcs8EncodedKeySpec.getEncoded()));
			// encrypt priv key			
			String encryptRsp = cvnCore.encrypt_AES(Encode.byteToHex(pkcs8EncodedKeySpec.getEncoded()),
					dbKeyAlias, sessionId);

			HashMap<String, String> rspData = new Gson().fromJson(encryptRsp, new TypeToken<HashMap<String, String>>() {
			}.getType());

			if (!rspData.get("ReturnCode").equals(ReturnCode.Success)) {
//				Log4j.log.error("*** [{}] Exception@genKeyPair:check ReturnCode:{}", sessionId, rspData.get("ReturnMsg"));
				throw new Exception(rspData.get("ReturnMsg"));
//				return "ERROR:" + rspData.get("ReturnMsg");
			}

			// Store public and private key
			IDGateOracle.storeKey(keyAlias, rspData.get("EncData"), Encode.byteToHex(x509EncodedKeySpec.getEncoded()),
					sessionId);

			String rspMsg = "Success. Key-pair(" + keyAlias + ") was generated and saved.";
			// TODO REMARK BELOW
			Log4j.log.debug("[" + sessionId + "][ECC.genKeyPair] - #rspMsg:[" + rspMsg + "]");
			
			return rspMsg;

		} catch (Exception e) {
//			Log4j.log.error("*** [{}] Exception@genKeyPair::Line103:{}", sessionId, e.getMessage());
			return "ERROR:" + e.getMessage();
		}
	}

	/** Compute shared secret key for ECIES
	 * @param publicKey public key 
	 * @param privateKey private key
	 * @return Shared ECDH key in binary form
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static byte[] getSecretKey(PublicKey publicKey, PrivateKey privateKey)
			throws NoSuchAlgorithmException, InvalidKeyException {

		KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_ALGORITHM);
		keyAgreement.init(privateKey);
		keyAgreement.doPhase(publicKey, true);

		return keyAgreement.generateSecret();
	}
	
	public static KeyPair loadKeyPair(String keyAlias, String sessionId) throws Exception {
		Log4j.log.info("[" + sessionId + "][ECC.loadKeyPair] - #keyAlias:[" + keyAlias + "]");

		KeyPair newKeyPair = null;
		try {
//			if ("true".equals(Cfg.getExternalCfgValue("SoftRSA")) && keyPair != null) {
			if (keyPair != null) {
				Log4j.log.debug("## [{}][ECC.loadKeyPair] - from catch Public key", sessionId);
//				Log4j.log.debug("*** [{}] KEYS.toJson@@loadKeyPair:{}", sessionId, new Gson().toJson(keyPair));
//			Log4j.log.info("*** KEYS.toJson:" + new Gson().toJson(keyPairs));

				return keyPair;
			}

			Log4j.log.info("[" + sessionId + "][ECC.loadKeyPair] - #keyAlias:[" + keyAlias + "] Read Public/Private Key.");
			// Read Public/Private Key.
			String[] keys = IDGateOracle.fetchKey(keyAlias, sessionId);
			Log4j.log.info("*** keys(before if)@@loadKeyPair:" + new Gson().toJson(keys));
			Log4j.log.info("*** keys == null@@loadKeyPair:{}", keys == null);
			if (keys == null || keys[0].indexOf("Error") > -1) {
				Log4j.log.error("[{}] Couldn't find {}", sessionId, keyAlias);
				Log4j.log.debug("[{}] ## Re-generate NEW KeyPair: {}", sessionId, keyAlias);
				// Re-generate NEW KeyPair
				ECCUtil.genKeyPair(keyAlias, sessionId);	// 重產成功後，加密存 DB，解密成功後，再存至 cache
				keys = IDGateOracle.fetchKey(keyAlias, sessionId);
//				Log4j.log.info("*** keys(inner if)@@loadKeyPair:" + new Gson().toJson(keys));
//			return null;	
			}
			Log4j.log.debug("## [{}][ECC.loadKeyPair] - *** NEW Public key:[{}]", sessionId, keys[1]);

			HashMap<String, String> rspData = null;

			// decrypt priv key
			
			String decryptRsp = cvnCore.decrypt_AES(keys[0], dbKeyAlias, sessionId);
			// TODO REMARK BELOW
//			Log4j.log.debug("*** decryptRsp@@loadKeyPair:" + decryptRsp);
			rspData = new Gson().fromJson(decryptRsp, new TypeToken<HashMap<String, String>>() {
			}.getType());

			if (!rspData.get("ReturnCode").equals(ReturnCode.Success)) {
				Log4j.log.error("[{}][ECC] - #Unable to decrypt RSA Key", sessionId);
				Log4j.log.debug("[{}] *** rspData@ECC:: {}",sessionId, rspData);
				Log4j.log.debug("[{}] *** rspData.get(\"Data\")@ECC: {}", sessionId, rspData.get("Data"));
				
				throw new Exception(decryptRsp); // null Exception
				
//				return null;
			}

			// Re-generate the origin KeyPair.
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_GEN_ALGORITHM);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Encode.hexToByte(keys[1]));
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//			Log4j.log.info("*** rspData.get(\"Data\")@@ECC:" + rspData.get("Data"));
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Encode.hexToByte(rspData.get("Data")));
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

			newKeyPair = new KeyPair(publicKey, privateKey);
//			if ("true".equals(Cfg.getExternalCfgValue("SoftRSA"))) {
//			}
			keyPair = newKeyPair;	// 重產成功後，加密存 DB，解密成功後，再存至 cache 

			Log4j.log.debug("## KeyPair is cached: {}", keyPair != null);
			//				Log4j.log.debug("*** KEYS.toJson@ECC:" + new Gson().toJson(keyPair));
		} catch (JsonSyntaxException e) {
			Log4j.log.error("*** [{}] JsonSyntaxException@loadKeyPair:{}", sessionId, e.getMessage());
		} catch (Exception e) {
			Log4j.log.error("*** [{}] Exception@loadKeyPair:{}", sessionId, e.getMessage());
			throw e;
		}

		return newKeyPair;
	}
	
	/**
	 * Loading the specific public key as hex string.
	 */
	public static String loadPublicKey(String keyAlias, String sessionId) {

		Log4j.log.info("[" + sessionId + "][ECC.loadPublicKey] - #keyAlias:[" + keyAlias + "]");

		try {
			KeyPair myPair = ECCUtil.loadKeyPair(keyAlias, sessionId);
			if (myPair != null) {
				String myPublicHex = Encode.byteToHex(myPair.getPublic().getEncoded());
				Log4j.log.debug("[" + sessionId + "][ECC.loadPublicKey] - #myPublicHex:[" + myPublicHex + "]");
				return myPublicHex;
			} else {
				throw new NullPointerException("The KeyPair is null.");
			}

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
			Log4j.log.error("[{}][ECC.loadPublicKey] - #Loading RSA public-key error:[{}]", sessionId, ex.getMessage());
			return "ERROR:" + ex.getMessage();
		} catch (Exception e) {
			Log4j.log.error("[{}][ECC.loadPublicKey] - #Loading RSA public-key error::[{}]", sessionId, e.getMessage());
			return "ERROR:" + e.getMessage();
		}
	}
	
	/**
	 * Loading the specific public key as hex string.
	 */
	public static String loadPublicKeyFromDB(String keyAlias, String sessionId) {
		
		Log4j.log.info("[" + sessionId + "][ECC.loadPublicKeyFromDB] - #keyAlias:[" + keyAlias + "]");
		
		try {
			KeyPair myPair = ECCUtil.loadKeyPairFromDB(keyAlias, sessionId);
			String myPublicHex = Encode.byteToHex(myPair.getPublic().getEncoded());
			Log4j.log.debug("[" + sessionId + "][ECC.loadPublicKeyFromDB] - #myPublicHex:[" + myPublicHex + "]");
			return myPublicHex;
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
			Log4j.log.error(
					"[{}][ECC.loadPublicKeyFromDB] - #Loading RSA public-key error:[{}]", sessionId, ex.getMessage());
			return "ERROR:" + ex.getMessage();
		} catch (Exception e) {
			Log4j.log.error(
					"[{}][ECC.loadPublicKeyFromDB] - #Loading RSA public-key error::[{}]",sessionId,e.getMessage()  );
			return "ERROR:" + e.getMessage();
		}
	}
	
	/**
	 * 配合 healthCheck( keyPair 不 cache
	 * @param keyAlias
	 * @param sessionId
	 * @return
	 * @throws Exception
	 */
	public static KeyPair loadKeyPairFromDB(String keyAlias, String sessionId) throws Exception {
		Log4j.log.info("[" + sessionId + "][ECC.loadKeyPairFromDB] - #keyAlias:[" + keyAlias + "]");

		// Read Public/Private Key.
		String[] keys = IDGateOracle.fetchKey(keyAlias, sessionId);
		if (keys == null || keys[0].indexOf("Error") > -1) {
			Log4j.log.error("[{}] Couldn't find {}", sessionId, keyAlias);
			Log4j.log.debug("[{}] Re-generate NEW KeyPair:{}", sessionId, keyAlias);
			ECCUtil.genKeyPair(keyAlias, sessionId);
			keys = IDGateOracle.fetchKey(keyAlias, sessionId);
//			return null;	// Re-generate NEW KeyPair
		}
		
		Log4j.log.info("[{}][ECC.loadKeyPairFromDB] - #Public key:[{}]", sessionId, keys[1]);
		
		// decrypt priv key
		String decryptRsp = cvnCore.decrypt_AES(keys[0], dbKeyAlias, sessionId);
		
		Log4j.log.debug("*** decryptRsp@loadKeyPairFromDB::" + decryptRsp);
		
		HashMap<String, String> rspData = new Gson().fromJson(decryptRsp, new TypeToken<HashMap<String, String>>() {
		}.getType());
		
		if (!rspData.get("ReturnCode").equals(ReturnCode.Success)) {
			Log4j.log.debug("*** rspData@loadKeyPairFromDB::" + rspData);
			Log4j.log.debug("*** rspData.get(\"Data\")@loadKeyPairFromDB:" + rspData.get("Data"));
			Log4j.log.error("[{}][ECC.loadKeyPairFromDB] - #Unable to decrypt RSA Key", sessionId);
			
			throw new Exception(rspData.get("ReturnMsg"));
			
//			return null;
		}
		
		// Re-generate the origin KeyPair.
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_GEN_ALGORITHM);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Encode.hexToByte(keys[1]));
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		Log4j.log.debug("*** rspData.get(\"Data\")@loadKeyPairFromDB:" + rspData.get("Data"));
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Encode.hexToByte(rspData.get("Data")));
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		
		KeyPair newKeyPair = new KeyPair(publicKey, privateKey);
//		if ("true".equals(Cfg.getExternalCfgValue("SoftRSA"))) {
////			keyPair = newKeyPair;	// 111.12.7 Jack 提醒
//		}
//		Log4j.log.debug("*** KEYS.toJson@loadKeyPairFromDB:{}", new Gson().toJson(keyPair));
		Log4j.log.debug("*** IFkeyPairISnull@loadKeyPairFromDB:\"{}\"", keyPair == null ? "IS NULL" : "Not null");
		
		return newKeyPair;
	}
}
