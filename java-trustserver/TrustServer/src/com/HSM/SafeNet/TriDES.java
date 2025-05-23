package com.HSM.SafeNet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
//import au.com.safenet.crypto.provider.SAFENETProvider;

import com.Common.Model.Log4j;
import com.Cvn.Encryptor.Rfc2898;

public class TriDES {
//	private static TriDES myTDES;
//	private static Provider myProvider;
//	private static KeyStore ks;
//
//	private SecretKey ky;
//	// private static WrappingKeyStore safeKS;
//	private static final String kStore_Pw = "123456"; // OCS:123456
//
//	public TriDES(String sessionId) {
//		Log4j.log.info("[" + sessionId + "][SafeNet.TriDES] - #Initialize TriDES...");
//		if (myProvider == null) {
//			initSafeNetProvider(sessionId);
//		}
//
//		if (ks == null) {
//			initSafeNetKeyStore(sessionId);
//		}
//	}
//
//	public static synchronized TriDES initSafeNetTDES(String sessionId) {
//		if (myTDES == null) {
//			myTDES = new TriDES(sessionId);
//		}
//		return myTDES;
//	}
//
//	synchronized private void initSafeNetProvider(String sessionId) {
//		SAFENETProvider mySafe = new au.com.safenet.crypto.provider.SAFENETProvider();
//		myProvider = mySafe;
//		Security.addProvider(myProvider);
//		Log4j.log.info("[" + sessionId + "][SafeNet.TriDES] - # SafeNet provider initialized.");
//	}
//
//	synchronized private void initSafeNetKeyStore(String sessionId) {
//
//		// WrappingKeyStore safeKS = WrappingKeyStore.getInstance("CRYPTOKI",
//		// "SAFENET");
//		// safeKS.load(null, null);
//
//		try {
//			ks = KeyStore.getInstance("CRYPTOKI", "SAFENET");
//			ks.load(null, null);
//			Log4j.log.info("[" + sessionId + "][SafeNet.TriDES] - # SafeNet Keystore initialized.");
//
//		} catch (Exception ex) {
//			Log4j.log.info(
//					"[" + sessionId + "][SafeNet.TriDES] - # SafeNet Keystore InitError occured: " + ex.getMessage());
//			return;
//		}
//
//	}
//
//	public static String genKey(String kyAlias, String kyStore_Location, String sessionId) {
//
//		Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.genKey] - #keyAlias:[" + kyAlias
//				+ "] - #Generate a new 3DES key...");
//		String rspMsg;
//
//		// Prepare the Keystore location.
//		File myFile = new File(kyStore_Location + "/SafeNet/" + kyAlias);
//		if (!myFile.exists()) {
//			Boolean doneMkDir = myFile.getParentFile().mkdirs();
//			if (!doneMkDir) {
//				Log4j.log.error("[" + sessionId + "][SafeNet.TriDES.genKey] - #Making directory failed.");
//				return "";
//			}
//		}
//
//		// Initialize Provider and KeyStore.
//		initSafeNetTDES(sessionId);
//
//		try {
//			KeyGenerator tdesKeyGen = KeyGenerator.getInstance("DESede", "SAFENET");
//			//// Old: Key tdesKey = tdesKeyGen.generateKey();
//
//			//// New:
//			tdesKeyGen.init(168);
//			SecretKey tdesKey = tdesKeyGen.generateKey();
//
//			//// TRY ENC
//			KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(tdesKey);
//
//			//// ks.setKeyEntry(keyAlias, tdesKey, null, null);
//			ks.setEntry(kyAlias, skEntry, new KeyStore.PasswordProtection(kStore_Pw.toCharArray()));
//
//			// Store the KeyStore.(Not required.)
//			FileOutputStream outStream = new FileOutputStream(myFile);
//			ks.store(outStream, kStore_Pw.toCharArray());
//			outStream.close();
//			rspMsg = "success";
//
//		} catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException | CertificateException
//				| IOException e) {
//			rspMsg = e.getMessage();
//		}
//
//		Log4j.log.info(
//				"[" + sessionId + "][SafeNet.TriDES.genKey] - #keyAlias:[" + kyAlias + "] - #Result:[" + rspMsg + "]");
//		return rspMsg;
//	}
//
//	public boolean loadKey(String kyAlias, String sessionId) {
//		try {
//			//// key = (SecretKey) ks.getKey(keyAlias, null);
//			ky = (SecretKey) ks.getKey(kyAlias, kStore_Pw.toCharArray());
//
//			Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.loadKey] - # Existing 3DES-key(" + kyAlias
//					+ ") is loaded from KeyStore.");
//			return true;
//
//		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException ex) {
//			Log4j.log.error("[" + sessionId + "][SafeNet.TriDES.loadKey] - # Loading 3DES-Key from KeyStore error: "
//					+ ex.getMessage());
//			return false;
//		}
//	}
//
//	public String encrypt_B64(String plainTxt, String kAlias, String sessionId) {
//
////		Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.encrypt_B64] - #plainTxt:[" + plainTxt + "] - #keyAlias:["
////				+ kAlias + "]");
//
//		String enTXT;
//		Cipher cipher;
//
//		// Initialize Provider and KeyStore.
//		initSafeNetTDES(sessionId);
//
//		// Loading the TriDES key from nCipher HSM.
//		boolean loadKy = myTDES.loadKey(kAlias, sessionId);
//		if (!loadKy) {
//			enTXT = "KeyStore not found.";
////			Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.encrypt_B64] - #plainTxt:[" + plainTxt
////					+ "] - #keyAlias:[" + kAlias + "] - #Error: KeyStore not found.");
//			return enTXT;
//		}
//
//		Rfc2898 kGen = null;
//		try {
//			kGen = new Rfc2898("nCipherBase".getBytes("UTF-8"), "nCipherBase".getBytes("UTF-8"), 1000);
//
//		} catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
//			Log4j.log.error(
//					"[" + sessionId + "][SafeNet.TriDES.encrypt] - #Generate Rfc2898 iv failed: " + ex.getMessage());
//			return "ERROR:" + ex.getMessage();
//		}
//
//		byte[] drvIv = kGen.getBytes(8);
//
//		try {
//			cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding", "SAFENET");
//			cipher.init(Cipher.ENCRYPT_MODE, ky, new IvParameterSpec(drvIv));
//
//			byte[] cipStream = cipher.doFinal(plainTxt.getBytes("UTF-8"));
//			enTXT = Base64.getEncoder().encodeToString(cipStream);
////			Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.encrypt_B64] - #plainTxt:[" + plainTxt
////					+ "] - #cipherTxt:[" + enTXT + "]");
//
//		} catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
//				| InvalidAlgorithmParameterException | IllegalStateException | IllegalBlockSizeException
//				| BadPaddingException | UnsupportedEncodingException e) {
//
//			enTXT = "ERROR:" + e.getMessage();
////			Log4j.log.error("[" + sessionId + "][SafeNet.TriDES.encrypt_B64] - #plainTxt:[" + plainTxt + "] - #Error:["
////					+ enTXT + "]");
//		}
//
//		return enTXT;
//	}
//
//	public String decrypt_UTF8(String enTXT, String kyAlias, String sessionId) {
//
//		Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.decrypt_UTF8] - #cipherTxt:[" + enTXT + "] - #keyAlias:[" + kyAlias + "]");
//		
//		String plainTxt;
//		Cipher decipher;
//
//		// Initialize Provider and KeyStore.
//		initSafeNetTDES(sessionId);
//
//		// Loading the TriDES key from SafeNet HSM.
//		boolean loadKy = myTDES.loadKey(kyAlias, sessionId);
//		if (!loadKy) {
//			plainTxt = "ERROR: KeyStore not found.";
//			Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.decrypt_UTF8] - #cipherTxt:[" + enTXT
//					+ "] - #keyAlias:[" + kyAlias + "] - #Error: KeyStore not found.");
//			return plainTxt;
//		}
//
//		Rfc2898 keyGenerator = null;
//		try {
//			keyGenerator = new Rfc2898("nCipherBase".getBytes("UTF-8"), "nCipherBase".getBytes("UTF-8"), 1000);
//
//		} catch (InvalidKeyException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
//			Log4j.log.error("[" + sessionId + "][SafeNet.TriDES.decrypt] - #Generate Rfc2898 iv failed: " + ex.getMessage());
//			return "ERROR:" + ex.getMessage();
//		}
//
//		byte[] drvIv = keyGenerator.getBytes(8);
//
//		try {
//			byte[] cipStream = Base64.getDecoder().decode(enTXT);
//			decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding", "SAFENET");
//			decipher.init(Cipher.DECRYPT_MODE, ky, new IvParameterSpec(drvIv));
//
//			byte[] plainStream = decipher.doFinal(cipStream);
//			plainTxt = new String(plainStream, "UTF-8");
////			Log4j.log.info("[" + sessionId + "][SafeNet.TriDES.decrypt_UTF8] - #cipherTxt:[" + enTXT
////					+ "] - #plainTxt:[" + plainTxt + "]");
//
//		} catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
//				| InvalidAlgorithmParameterException | IllegalStateException | IllegalBlockSizeException
//				| BadPaddingException | UnsupportedEncodingException e) {
//
//			plainTxt = "ERROR:" + e.getMessage();
//			Log4j.log.error("[" + sessionId + "][SafeNet.TriDES.decrypt_UTF8] - #cipherTxt:[" + enTXT + "] - #Error:[" + plainTxt + "]");
//		}
//
//		return plainTxt;
//	}

}
