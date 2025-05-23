package com.HSM.SafeNet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.Cvn.Config.Cfg;
import com.Cvn.Encryptor.CvnSecrets;
import com.Cvn.Encryptor.Encode;
import com.TrustServer.Func.TS_MainFunc;
import com.toppanidgate.fidouaf.common.model.Log4j;

public class AES {
//	public static AES myAES;
//	public static Provider myProvider;
//	public static KeyStore myKeystore;
//	private static String slotPxd;
//	private static int slotId;
//	public static String providerName;
//
//	synchronized public static AES initSafeNetAES(final int slotID, final String slotPxd, final String sessionId) {
//		
//		if (myAES == null) {
//			String hsmIPaddress = Cfg.getExternalCfgValue("HSMIPAddress");
//			try {
//				InetAddress.getByName(hsmIPaddress).isReachable(1000);	// IOException
//				myAES = new AES(slotID, slotPxd, sessionId);
//			} catch (UnknownHostException e) {
//				Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetAES] - # Unknown Host error: "
//						+ e.getMessage());
//			} catch (IOException e) {
//				Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetAES] - # IOException:Cannot connect to "
//						+ hsmIPaddress);
//			} 
//		}
//		return myAES;
//	}
//
//
//	public AES(final int slotID, final String slotPxd, final String sessionId) {
//		Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES] - #Initialize Safetnet provider and keyStore.");
//		AES.slotPxd = slotPxd;
//		AES.slotId = slotID;
//
//		if (myProvider == null) {
//			initSafeNetProvider(slotID, sessionId);
//		}
//		if (myProvider!= null && myKeystore == null) {
//			initSafeNetKeyStore(sessionId);
//		}
//	}
//
//	synchronized private void initSafeNetProvider(final int slotID, final String sessionId) {
//		String hsmIPaddress = Cfg.getExternalCfgValue("HSMIPAddress");
//		try {
//			InetAddress.getByName(hsmIPaddress).isReachable(1000);	// IOException
//			myProvider = new au.com.safenet.crypto.provider.SAFENETProvider(slotID);
//			Security.addProvider(myProvider);
//			providerName = myProvider.getName();
//			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
//					+ "][SafeNet.AES.initSafeNetProvider] - #SafeNet provider initialized. \nproviderName:"
//					+ providerName);
//		} catch (UnknownHostException e) {
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetProvider] - # Unknown Host error: "
//					+ e.getMessage());
//		} catch (IOException e) {
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetProvider] - # IOException:Cannot connect to "
//					+ hsmIPaddress);
//		} catch (Exception ex) {
//			myProvider = null;
//			Log4j.log.error(
//					"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetProvider] - # SafeNet provider initialization error:["
//							+ ex.getMessage() + "]");
//			return;
//		}
//	}
//
//	synchronized private void initSafeNetKeyStore(String sessionId) {
//		try {
//			myKeystore = KeyStore.getInstance("CRYPTOKI", myProvider);
//			myKeystore.load(null, slotPxd.toCharArray());
//			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetKeyStore] - #SafeNet Keystore initialized.");
//		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException
//				| IOException e) {
//			myKeystore = null;
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.initSafeNetKeyStore] - #SafeNet Keystore InitError: "
//					+ e.getMessage());
//			return;
//		}
//	}
//
//	public static String genKey(int slotIId, String slotPxd, String kyAlias, String sessionId) {
//
//		Log4j.log.info(
//				"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.genKey] - #keyAlias:[" + kyAlias + "] - #Generate a new AES key...");
//		String rspMsg;
//
//
//		// Initialize Provider and KeyStore.
//		Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.genKey] - #Initialize Safetnet provider and keyStore.");
//		initSafeNetAES(slotIId, slotPxd, sessionId);
//
//		try {
//			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.genKey] - #Building up KeyGenerator(AES-SAFENET)...");
//			KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES", myProvider);
//
//			aesKeyGen.init(256); // 3-DES: tdesKeyGen.init(168);
//			SecretKey aesKy = aesKeyGen.generateKey();
//
//			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.genKey] - #Saving KeyEntry to SafeNet KeyStore...");
//			KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(aesKy);
//			myKeystore.setEntry(kyAlias, skEntry,
//					new KeyStore.PasswordProtection(CvnSecrets.safenetSec_ret.toCharArray()));
//
//			rspMsg = "success";
//
//		} catch (NoSuchAlgorithmException | KeyStoreException e) {
//			rspMsg = e.getMessage();
//		}
//
//		Log4j.log.info(
//				"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.genKey] - #keyAlias:[" + kyAlias + "] - #Result:[" + rspMsg + "]");
//		return rspMsg;
//	}
//
//	private SecretKey loadKey(String kyAlias, String sessionId) {
//		try {
//			SecretKey myKey = (SecretKey) myKeystore.getKey(kyAlias, CvnSecrets.safenetSec_ret.toCharArray());
//
////			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "]][SafeNet.AES.loadKey] - # Existing AES-key(" + kyAlias
////					+ ") is loaded from SAFENET.CRYPTOKI key store.");
//			return myKey;
//
//		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException ex) {
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.loadKey] - # Loading AES-Key from KeyStore error: "
//					+ ex.getMessage());
//			return null;
//		}
//	}
//
//	public String encrypt_Hex(String plainTxt, String kyAlias, String sessionId) {
//
////		Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - #plainTxt:[" + plainTxt + "] - #keyAlias:["
////				+ kyAlias + "]");
//
//		String enTXT;
//		Cipher cipher;
//
//		// Initialize Provider and KeyStore.
//		initSafeNetAES(slotId, slotPxd, sessionId);
//
//		// Loading the TriDES key from nCipher HSM.
//		SecretKey loadKey = null;
//		String hsmIPaddress = Cfg.getExternalCfgValue("HSMIPAddress");
//		try {
//		InetAddress.getByName(hsmIPaddress).isReachable(1000);	// IOException
//		loadKey = myAES.loadKey(kyAlias, sessionId);
//		} catch (UnknownHostException e) {
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - # Unknown Host error: "
//					+ e.getMessage());
//		} catch (IOException e) {
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - # IOException:Cannot connect to "
//					+ hsmIPaddress);
//			enTXT = "ERROR: Cannot connect to " + hsmIPaddress;
//			return enTXT;
//		} catch (Exception e) { // myAES.loadKey(kyAlias, sessionId)
//			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - # Exception: "
//					+ e.getMessage());
//		}
//
//		if (loadKey == null) {
//			enTXT = "ERROR: Loading AES-key from SAFENET-CRYPTOKI key store failed.";
////			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - #plainTxt:[" + plainTxt + "] - #keyAlias:["
////					+ kyAlias + "] - #Loading AES-key from SAFENET-CRYPTOKI key store failed.");
//			return enTXT;
//		}
//
//		try {
//			Log4j.log.info("[" + sessionId
//					+ "][SafeNet.AES.encrypt_Hex] - #Initializing SAFENET-Cipher(AES/CBC/PKCS5Padding)...");
//
//			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", myProvider);
//
//			Log4j.log.debug(
//					"[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - #Init Cipher with loaded-secretKey and iv...");
//
//			IvParameterSpec myIv = new IvParameterSpec(CvnSecrets.safenetIvSrc.getBytes("UTF-8"));
//			cipher.init(Cipher.ENCRYPT_MODE, loadKey, myIv);
//
//			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - #SAFENET-Cipher executing...");
//			byte[] bStream = cipher.doFinal(plainTxt.getBytes("UTF-8"));
//			enTXT = Encode.byteToHex(bStream);
////			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - #plainTxt:[" + plainTxt
////					+ "] - #cipherTxt(Hex):[" + enTXT + "]");
//
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
//				| IllegalStateException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException
//				| InvalidAlgorithmParameterException e) {
//
//			enTXT = "ERROR: " + e.getMessage();
////			Log4j.log.error("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.encrypt_Hex] - #plainTxt:[" + plainTxt + "] - #Error:["
////					+ enTXT + "] - #Stacktrace[" + new Gson().toJson(e.getStackTrace()) + "]");
//		}
//
//		return enTXT;
//	}
//
//	public String decrypt_UTF8(String enTXT, String keyAlias, String sessionId) throws Exception {
//
//		Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.decrypt_UTF8] - #cipherTxt:[" + enTXT + "] - #keyAlias:["
//				+ keyAlias + "]");
//
//		String plainTxt;
//		Cipher decipher;
//
//		// Initialize Provider and KeyStore.
//		initSafeNetAES(slotId, slotPxd, sessionId);
//
//		// Loading the TriDES key from SafeNet HSM.
//		SecretKey sec = myAES.loadKey(keyAlias, sessionId);
//		if (sec == null) {
//			enTXT = "KeyStore not found.";
//			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.decrypt_UTF8] - #cipherTxt:[" + enTXT + "] - #keyAlias:["
//					+ keyAlias + "] - #Error: KeyStore not found.");
//			return enTXT;
//		}
//
//		try {
//			byte[] cStream = Encode.hexToByte(enTXT);
//			decipher = Cipher.getInstance("AES/CBC/PKCS5Padding", myProvider);
//
//			IvParameterSpec myIv = new IvParameterSpec(CvnSecrets.safenetIvSrc.getBytes("UTF-8"));
//			decipher.init(Cipher.DECRYPT_MODE, sec, myIv);
//
//			byte[] plainStream = decipher.doFinal(cStream);
//			plainTxt = new String(plainStream, "UTF-8");
////			Log4j.log.info("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion + "][SafeNet.AES.decrypt_UTF8] - #cipherTxt:[" + enTXT
////					+ "] is decrypted to plainTxt:[" + plainTxt + "]");
//
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
//				| InvalidAlgorithmParameterException | IllegalStateException | IllegalBlockSizeException
//				| BadPaddingException | UnsupportedEncodingException e) {
//
//			plainTxt = e.getMessage();
//			Log4j.log.error("[" + sessionId + "[SafeNet.AES.decrypt_UTF8] - #cipherTxt:[" + enTXT + "] - #Error:["
//					+ plainTxt + "]");
//			throw e;
//		}
//
//		return plainTxt;
//	}

}
