package com.Cvn.Encryptor;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.Cvn.Config.Cfg;
import com.Cvn.Core.CvnCore;
import com.Cvn.Database.IDGateOracle;
import com.TrustServer.Func.TS_MainFunc;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.toppanidgate.idenkey.common.model.ReturnCode;



import com.Cvn.Database.KeyStore;
import com.Cvn.Database.KeyStoreService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

// Author: Calvin @ iDGate.com
@Component
public class RSA {

    @Value("${DbKeyAlias}")
    private String DbKeyAlias;

    @Value("${SoftRSA}")
    protected String SoftRSA;

    @Autowired
    KeyStoreService keyStoreSvc;

    private static Logger logger = LogManager.getLogger(RSA.class);
    //@Autowired
    //private CvnCore cvnCore;
    private static KeyPair keyPair = null;



    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html

    // --- Public APIs --- //
    public String genKeyPair(String keyAlias, String sessionId) {

         logger.info("[" + sessionId + "][RSA.genKeyPair] - #keyName:[" + keyAlias + "] - #Generate a 2048-length key pair...");

        try {
            KeyPairGenerator myKeyGen = KeyPairGenerator.getInstance("RSA");
            myKeyGen.initialize(2048, new SecureRandom());

            // Create Keys
            KeyPair myPair = myKeyGen.generateKeyPair();

            // 111.12.6 造成誤判，誤以為存 DB 後，存 cache 
//			if ("true".equals(Cfg.getExternalCfgValue("SoftRSA"))) {
//				keyPair = myPair;
//			}

            // Store public and private key
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(myPair.getPublic().getEncoded());
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(myPair.getPrivate().getEncoded());


//			System.out.println("*** private input:" + Encode.byteToHex(pkcs8EncodedKeySpec.getEncoded()));
            // encrypt priv key
            HashMap<String, String> localData = new HashMap<String, String>();
            try {
                byte[] plain_bin =Encode.byteToHex(pkcs8EncodedKeySpec.getEncoded()).getBytes("UTF-8");
                byte[] kBin = DbKeyAlias.getBytes("UTF-8");
                byte[] ci_bin = AES.drvRfc2898_encrypt(kBin, plain_bin, sessionId);
                localData.put("ReturnCode", ReturnCode.Success);
                localData.put("ReturnMsg", "Cvn-AES encrypted.");
                localData.put("EncData", Encode.byteToHex(ci_bin));
                logger.info("inside genkey, retrun code: {}, return msg: {}, encdata: {}",localData.get("ReturnCode"), localData.get("ReturnMsg"), localData.get("EncData"));
            } catch (Exception e) {
                localData.put("ReturnCode", ReturnCode.Fail);
                localData.put("ReturnMsg", e.getMessage());
                localData.put("EncData", "--");
            }


            //String encryptRsp = gson.toJson(localData).replace("\\u003d", "=");

            //HashMap<String, String> rspData = new Gson().fromJson(encryptRsp, new TypeToken<HashMap<String, String>>() {
            //}.getType());

            if (!localData.get("ReturnCode").equals(ReturnCode.Success)) {
//				logger.error("*** [{}] Exception@genKeyPair:check ReturnCode:{}", sessionId, rspData.get("ReturnMsg"));
                throw new Exception(localData.get("ReturnMsg"));
//				return "ERROR:" + rspData.get("ReturnMsg");
            }

//			KeyStore keyStoreNew = new KeyStore(keyAlias, rspData.get("EncData"), Encode.byteToHex(x509EncodedKeySpec.getEncoded()), new Timestamp(System.currentTimeMillis()));
            // postgreSQL
//			KeyStore keyStoreNew = new KeyStore(1L, keyAlias, rspData.get("EncData"), Encode.byteToHex(x509EncodedKeySpec.getEncoded()));
//			keyStoreDAO.save(keyStoreNew);
//			keyStore = keyStoreDAO.findByAlias(keyAlias);

            //dao.storeKey(keyAlias, rspData.get("EncData"), Encode.byteToHex(x509EncodedKeySpec.getEncoded()),
            //        sessionId);
            logger.info("keyalias before store key :{}",keyAlias);
            keyStoreSvc.storeKey(keyAlias, localData.get("EncData"), Encode.byteToHex(x509EncodedKeySpec.getEncoded()));

            String rspMsg = "Success. Key-pair(" + keyAlias + ") was generated and saved.";
            logger.info("[" + sessionId + "][RSA.genKeyPair] - #rspMsg:[" + rspMsg + "]");

//			// 存 DB 後，再存 cache
//			if ("true".equals(Cfg.getExternalCfgValue("SoftRSA"))) {
//				keyPair = myPair;
//			}

            return rspMsg;

        } catch (Exception e) {
//			logger.error("*** [{}] Exception@genKeyPair::Line103:{}", sessionId, e.getMessage());
            return "ERROR:" + e.getMessage();
        }
    }


    public KeyPair loadKeyPair(String keyAlias, String sessionId, String SoftRSA) throws Exception {

        KeyPair newKeyPair = null;
        try {
            if ("true".equals(SoftRSA) && keyPair != null) {
                logger.info("## [{}][RSA.loadKeyPair] - from catch Public key", sessionId);
                logger.info("*** [{}] KEYS.toJson@@loadKeyPair:{}", sessionId, keyPair.getPublic());
//			logger.info("*** KEYS.toJson:" + new Gson().toJson(keyPairs));

                return keyPair;
            }

            // Read Public/Private Key.
            //String[] keys = dao.fetchKey(keyAlias, sessionId);
            KeyStore keyStore = keyStoreSvc.findOne(keyAlias);
            //logger.info("*** keys(before if)@@loadKeyPair:" + new Gson().toJson(keys));
//			logger.info("*** keys == null@@loadKeyPair:{}", keys == null);
            if (keyStore == null || keyStore.isEmpty()) {
                logger.error("Couldn't find " + keyAlias);
                logger.info("## Re-generate NEW KeyPair:" + keyAlias);
                // Re-generate NEW KeyPair
                genKeyPair("RSAkeyAlias", sessionId);    // 重產成功後，加密存 DB，解密成功後，再存至 cache
                keyStore = keyStoreSvc.findOne(keyAlias);
//				logger.info("*** keys(inner if)@@loadKeyPair:" + new Gson().toJson(keys));
            }


            logger.info("## [{}][RSA.loadKeyPair] - #Public key:[{}]", sessionId, keyStore.getPubkey());

            // decrypt priv key
            logger.info("## [{}][RSA.loadKeyPair] - #key before AES.drvRfc2898_decrypt:[{}]", sessionId, keyStore.getPrikey());
            byte[] ci_bin = Encode.hexToByte(keyStore.getPrikey());
            logger.info(DbKeyAlias);
            byte[] k_bin = DbKeyAlias.getBytes("UTF-8");
            byte[] plain_bin = AES.drvRfc2898_decrypt(k_bin, ci_bin, sessionId);

            HashMap<String, String> localData = new HashMap<String, String>();
            localData.put("ReturnCode", ReturnCode.Success);
            localData.put("ReturnMsg", "Cvn-AES decrypted.");
            localData.put("Data", new String(plain_bin, "UTF-8"));
            //String decryptRsp = gson.toJson(localData).replace("\\u003d", "=");
            //logger.info("decrypted" + decryptRsp);
            // TODO REMARK BELOW
//			logger.debug("*** decryptRsp@@loadKeyPair:" + decryptRsp);
            //rspData = new Gson().fromJson(decryptRsp, new TypeToken<HashMap<String, String>>() {
            //}.getType());
            logger.info("rspData" + localData);
            if (!localData.get("ReturnCode").equals(ReturnCode.Success)) {
                logger.error("[" + sessionId + "][RSA.loadKeyPair] - #Unable to decrypt RSA Key");
                logger.debug("*** rspData@loadKeyPair::" + localData);
                logger.debug("*** rspData.get(\"Data\")@loadKeyPair:" + localData.get("Data"));

                throw new Exception(localData.get("ReturnMsg")); // null Exception

//				return null;
            }

            // Re-generate the origin KeyPair.
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Encode.hexToByte(keyStore.getPubkey()));
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            logger.info("public key" + publicKey.toString());
            logger.info("*** rspData.get(\"Data\")@@loadKeyPair: {}",Arrays.toString(Encode.hexToByte(localData.get("Data"))));
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Encode.hexToByte(localData.get("Data")));
            logger.info(privateKeySpec.toString());
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            logger.info("pri key=" + privateKey.toString());
            newKeyPair = new KeyPair(publicKey, privateKey);
            if ("true".equals(SoftRSA)) {
                keyPair = newKeyPair;    // 重產成功後，加密存 DB，解密成功後，再存至 cache

                logger.info("## KeyPair is cached: {}", keyPair != null);
//				logger.debug("*** KEYS.toJson@loadKeyPair:" + new Gson().toJson(keyPair));
            }
        } catch (Exception e) {
            logger.error("*** [{}] Exception@loadKeyPair:", sessionId, e);
            throw e;
        }

        return newKeyPair;
    }

    /**
     * 配合 healthCheck( keyPair 不 cache
     * @param keyAlias
     * @param sessionId
     * @return
     * @throws Exception
     */
    /*
    public static KeyPair loadKeyPairFromDB(String keyAlias, String sessionId) throws Exception {

//		if ("true".equals(Cfg.getExternalCfgValue("SoftRSA")) && keyPair != null) {
//			logger.debug("[{}][RSA.loadKeyPairFromDB] - from catch Public key", sessionId);
//			logger.debug("*** [{}] KEYS.toJson:{}", sessionId, new Gson().toJson(keyPair));
////			logger.info("*** KEYS.toJson:" + new Gson().toJson(keyPairs));
//			
//			return keyPair;
//		}

        // Read Public/Private Key.
        String[] keys = IDGateOracle.fetchKey(keyAlias, sessionId);
        if (keys == null || keys[0].indexOf("Error") > -1) {
            logger.error("Couldn't find " + keyAlias);
            logger.debug("Re-generate NEW KeyPair:"+ keyAlias);
            com.Cvn.Encryptor.RSA.genKeyPair("RSAkeyAlias", sessionId);
            keys = IDGateOracle.fetchKey(keyAlias, sessionId);
//			return null;	// Re-generate NEW KeyPair
        }
        // postgreSQL
//		if (keyStore == null) {
//			keyStore = keyStoreDAO.findByAlias(keyAlias);
//		}
//		if (!keyStore.isPresent()) {
//			logger.error("[{}][RSA.loadKeyPairFromDB] - Couldn't found keyAlias[{}]", sessionId, keyAlias);
//			return null;
//		}
//		String[] keys = new String[2];
//		keys[0] = keyStore.get().getPriv_key();
//		keys[1] = keyStore.get().getPub_key();

        logger.info("[{}][RSA.loadKeyPairFromDB] - #Public key:[{}]", sessionId, keys[1]);

        // decrypt priv key
        String decryptRsp = cvnCore.decrypt_AES(keys[0], Cfg.getExternalCfgValue("DbKeyAlias"), sessionId);

        logger.debug("*** decryptRsp@loadKeyPairFromDB::" + decryptRsp);

        HashMap<String, String> rspData = new Gson().fromJson(decryptRsp, new TypeToken<HashMap<String, String>>() {
        }.getType());

        if (!rspData.get("ReturnCode").equals(ReturnCode.Success)) {
            logger.debug("*** rspData@loadKeyPairFromDB::" + rspData);
            logger.debug("*** rspData.get(\"Data\")@loadKeyPairFromDB:" + rspData.get("Data"));
            logger.error("[" + sessionId + "][RSA.loadKeyPairFromDB] - #Unable to decrypt RSA Key");

            throw new Exception(rspData.get("ReturnMsg"));

//			return null;
        }

        // Re-generate the origin KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Encode.hexToByte(keys[1]));
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        logger.debug("*** rspData.get(\"Data\")@loadKeyPairFromDB:" + rspData.get("Data"));
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Encode.hexToByte(rspData.get("Data")));
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        KeyPair newKeyPair = new KeyPair(publicKey, privateKey);
//		if ("true".equals(Cfg.getExternalCfgValue("SoftRSA"))) {
////			keyPair = newKeyPair;	// 111.12.7 Jack 提醒
//		}
//		logger.debug("*** KEYS.toJson@loadKeyPairFromDB:{}", new Gson().toJson(keyPair));
        logger.debug("*** IFkeyPairISnull@loadKeyPairFromDB:\"{}\"", keyPair == null ? "IS NULL" : "Not null");

        return newKeyPair;
    }
*/
    /**
     * Loading the specific public key as hex string.
     */
    public String loadPublicKey(String keyAlias, String sessionId, String SoftRSA) {

        logger.info("[" + sessionId + "][RSA.loadPublicKey] - #keyAlias:[" + keyAlias + "]");

        try {
            KeyPair myPair = loadKeyPair(keyAlias, sessionId, SoftRSA);
            String myPublicHex = Encode.byteToHex(myPair.getPublic().getEncoded());
            logger.info("[" + sessionId + "][RSA.loadPublicKey] - #myPublicHex:[" + myPublicHex + "]");
            return myPublicHex;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.loadPublicKey] - #Loading RSA public-key error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[" + sessionId + "][RSA.loadPublicKey] - #Loading RSA public-key error::[" + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Loading the specific public key as hex string.
     */
    /*
    public static String loadPublicKeyFromDB(String keyAlias, String sessionId) {

        logger.info("[" + sessionId + "][RSA.loadPublicKeyFromDB] - #keyAlias:[" + keyAlias + "]");

        try {
            KeyPair myPair = com.Cvn.Encryptor.RSA.loadKeyPairFromDB(keyAlias, sessionId);
            String myPublicHex = Encode.byteToHex(myPair.getPublic().getEncoded());
            logger.debug("[" + sessionId + "][RSA.loadPublicKeyFromDB] - #myPublicHex:[" + myPublicHex + "]");
            return myPublicHex;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.loadPublicKeyFromDB] - #Loading RSA public-key error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[" + sessionId + "][RSA.loadPublicKeyFromDB] - #Loading RSA public-key error::[" + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }
*/
    /**
     * Encrypt a UTF-8 text to a base64-cipher text.
     */
    public String encrypt_B64(String plainTxt, String kAlias, String sessionId, String SoftRSA) {

//		logger.info(
//				"[" + sessionId + "][RSA.encrypt_Hex] - #plainTxt:[" + plainTxt + "] - #keyAlias:[" + kAlias + "]");

        try {
            Cipher rsaCip = Cipher.getInstance("RSA");
            KeyPair myPair = loadKeyPair(kAlias, sessionId,SoftRSA);

            if(myPair == null) {
                return "ERROR: load key failed";
            }

            rsaCip.init(Cipher.ENCRYPT_MODE, myPair.getPublic());

            String enTxt = Base64.getEncoder().encodeToString(rsaCip.doFinal(plainTxt.getBytes("UTF-8")));
            logger.info("[" + sessionId + "][RSA.encrypt_Hex] - #cipherTxt:[" + enTxt + "]");
            return enTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Encrypt a UTF-8 text to a base64-cipher text.
     */
    public String encrypt_B64(String plainTxt, String keyAlias, String keyStore_Location, String sessionId, String SoftRSA) {

        // logger.info( "[" + sessionId + "][RSA.encrypt_Hex] - #plainTxt:[" + plainTxt + "] - #keyAlias:[" + keyAlias + "]");

        try {
            Cipher myRSA_Cipher = Cipher.getInstance("RSA");
            KeyPair myPair = loadKeyPair(keyAlias, sessionId,SoftRSA);
            myRSA_Cipher.init(Cipher.ENCRYPT_MODE, myPair.getPublic());

            String cipherTxt = Encode.byteToHex(myRSA_Cipher.doFinal(plainTxt.getBytes("UTF-8")));
//			String cipherTxt = Base64.getEncoder().encodeToString(myRSA_Cipher.doFinal(plainTxt.getBytes("UTF-8")));
            // logger.info("[" + sessionId + "][RSA.encrypt_Hex] - #cipherTxt:[" + cipherTxt + "]");
            return cipherTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Encrypt a UTF-8 text to a base64-cipher text.
     */
    public static String encryptWithFrontEndPubKey(String plainTxt, String keyAlias, String keyStore_Location,
                                                   String sessionId, String pubKey) {

        // logger.info( "[" + sessionId + "][RSA.encrypt_Hex] - #plainTxt:[" +
        // plainTxt + "] - #keyAlias:[" + keyAlias + "]");

        try {
            Cipher myRSA_Cipher = Cipher.getInstance("RSA");
//			KeyPair myPair = this.loadKeyPair(keyAlias, sessionId);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Encode.hexToByte(pubKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            myRSA_Cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//			myRSA_Cipher.init(Cipher.ENCRYPT_MODE, myPair.getPublic());

            String cipherTxt = Encode.byteToHex(myRSA_Cipher.doFinal(plainTxt.getBytes("UTF-8")));
//			String cipherTxt = Base64.getEncoder().encodeToString(myRSA_Cipher.doFinal(plainTxt.getBytes("UTF-8")));
            // logger.info("[" + sessionId + "][RSA.encrypt_Hex] - #cipherTxt:[" + cipherTxt + "]");
            return cipherTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        }
    }

    /**
     * Encrypt a UTF-8 text to a hexadecimal-cipher text.
     */
    public String encrypt_Hex(String plainTxt, String keyAlias, String keyStore_Location, String sessionId) {

        // logger.info( "[" + sessionId + "][RSA.encrypt_Hex] - #plainTxt:[" + plainTxt + "] - #keyAlias:[" + keyAlias + "]");

        try {
            Cipher myRSA_Cipher = Cipher.getInstance("RSA");
            KeyPair myPair = loadKeyPair(keyAlias, sessionId, SoftRSA);
            myRSA_Cipher.init(Cipher.ENCRYPT_MODE, myPair.getPublic());

            String cipherTxt = Encode.byteToHex(myRSA_Cipher.doFinal(plainTxt.getBytes("UTF-8")));
            // logger.info("[" + sessionId + "][RSA.encrypt_Hex] - #cipherTxt:[" + cipherTxt + "]");
            return cipherTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[" + sessionId + "][RSA.encrypt_Hex] - #Computing RSA encryption error:[" + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Decrypt a base64-cipher text to a UTF-8 plain text.
     */
    public String decrypt_B64(String enTxt, String kAlias, String sessionId, String SoftRSA) {

        logger.info(
                "[" + sessionId + "][RSA.decrypt_B64] - #cipherTxt:[" + enTxt + "] - #keyAlias:[" + kAlias + "]");

        try {
            Cipher rsaCip = Cipher.getInstance("RSA");
            KeyPair myPair = loadKeyPair(kAlias, sessionId,SoftRSA);

            if(myPair == null) {
                return "ERROR: load key failed";
            }
            logger.info("Private: {}", myPair.getPrivate());
            rsaCip.init(Cipher.DECRYPT_MODE, myPair.getPrivate());
            String plainTxt = new String(rsaCip.doFinal(Encode.hexToByte(enTxt)), "UTF-8");
//			String plainTxt = new String(rsaCip.doFinal(Base64.getDecoder().decode(enTxt)), "UTF-8");
			logger.info("[{}][RSA.decrypt_B64] - #plainTxt:[{}]", sessionId,plainTxt);
            return plainTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error(
                    "[{} [RSA.decrypt_B64] - #Computing RSA decryption error:[{}]",sessionId, ex);
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[{}][RSA.encrypt_Hex] - #Computing RSA encryption error:[{}]",sessionId, e);
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Decrypt a base64-cipher text to a UTF-8 plain text.
     */
    public String decrypt_B64(String cipherTxt, String keyAlias, String keyStore_Location, String sessionId, String SoftRSA) {

        // logger.info( "[" + sessionId + "][RSA.decrypt_B64] - #cipherTxt:[" + cipherTxt + "] - #keyAlias:[" + keyAlias + "]");

        try {
            Cipher myRSA_Cipher = Cipher.getInstance("RSA");
            KeyPair myPair = loadKeyPair(keyAlias, sessionId,SoftRSA);
            myRSA_Cipher.init(Cipher.DECRYPT_MODE, myPair.getPrivate());

            String plainTxt = new String(myRSA_Cipher.doFinal(Encode.hexToByte(cipherTxt)), "UTF-8");
//			String plainTxt = new String(myRSA_Cipher.doFinal(Base64.getDecoder().decode(cipherTxt)), "UTF-8");
            // logger.info("[" + sessionId + "][RSA.decrypt_B64] - #plainTxt:[" + plainTxt + "]");
            return plainTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error(
                    "[" + sessionId + "][RSA.decrypt_B64] - #Computing RSA decryption error:[" + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error(
                    "[" + sessionId + "][RSA.decrypt_B64] - #Computing RSA decryption error:[" + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }

    /**
     * Decrypt a hexadecimal-cipher text to a UTF-8 plain text.
     */
    public String decrypt_Hex(String cipherTxt, String keyAlias, String keyStore_Location, String sessionId) {

        // logger.info("[" + sessionId + "][RSA.decrypt_UTF8] - #cipherTxt:[" + cipherTxt + "] - #keyAlias:[" + keyAlias + "]");

        try {
            Cipher myRSA_Cipher = Cipher.getInstance("RSA");
            KeyPair myPair = loadKeyPair(keyAlias, sessionId, SoftRSA);
            myRSA_Cipher.init(Cipher.DECRYPT_MODE, myPair.getPrivate());
            String plainTxt = new String(myRSA_Cipher.doFinal(Encode.hexToByte(cipherTxt)), "UTF-8");
            // logger.info("[" + sessionId + "][RSA.decrypt_UTF8] - #plainTxt:[" + plainTxt + "]");
            return plainTxt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException
                 | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error("[" + sessionId + "][RSA.decrypt_UTF8] - #Computing RSA decryption error:["
                    + ex.getMessage() + "]");
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            logger.error("[" + sessionId + "][RSA.decrypt_UTF8] - #Computing RSA decryption error:["
                    + e.getMessage() + "]");
            return "ERROR:" + e.getMessage();
        }
    }

    public static byte[] sign(PrivateKey privateKey, byte[] signedData) throws SignatureException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signedData);
        return signature.sign();
    }
}

