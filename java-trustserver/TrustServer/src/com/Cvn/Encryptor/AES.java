package com.Cvn.Encryptor;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.toppanidgate.fidouaf.common.model.Log4j;

// Author: Calvin @ iDGate.com
public class AES {

	public AES() {
	}

	/* --- Fixed-IV is forbidden by fortify --- */

	public static byte[] encrypt(byte[] plainTxt, byte[] kTxt, byte[] iv, String sessionId) {

		// Parameter examination
		if (kTxt == null || plainTxt == null) {
			Log4j.log.info("[" + sessionId + "][Cvn.AES.encrypt] - #Invalid empty keyTxt or plainTxt.");
			return new byte[] {};
		}

		// To check the maximum length of AES key.
		// Cipher.getMaxAllowedKeyLength("AES");

		// Generate SHA-256 key
		byte[] kStream = Hash.encode_SHA256(kTxt, sessionId);
		if (kStream.length == 0) {
			return kStream;
		}

		SecretKeySpec myKSpec = new SecretKeySpec(kStream, "AES");
		Cipher cipher;
		byte[] byteText;

		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, myKSpec, new IvParameterSpec(iv));
			byteText = cipher.doFinal(plainTxt);
			return byteText;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException e) {
			Log4j.log.error("[" + sessionId + "][Cvn.AES.encrypt] - #Initialize JCA Cipher error: " + e.getMessage());
			return new byte[] {};
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Log4j.log
					.error("[" + sessionId + "][Cvn.AES.encrypt] - #Computing AES encryption error: " + e.getMessage());
			return new byte[] {};
		}
	}

	/**
	 * Use base key to generate derived-key(32) and derived-iv(16), then compute
	 * AES-CBC-encrypt the plain stream.
	 * 
	 * @param kBase     also used as the salt in RFC2898.
	 * @param plainStream
	 * @param sessionId
	 * @return
	 * @throws GeneralSecurityException 
	 */
	public static byte[] drvRfc2898_encrypt(byte[] kBase, byte[] plainStream, String sessionId) throws GeneralSecurityException {
		Rfc2898 kGen = null;
		try {
			kGen = new Rfc2898(kBase, kBase, 1000);

		} catch (InvalidKeyException | NoSuchAlgorithmException ex) {
			Log4j.log.error(
					"[" + sessionId + "][drvRfc2898_encrypt] - #Generate Rfc2898 key failed: " + ex.getMessage());
//			return new byte[] {};
			throw ex;
		}

		byte[] drvKy = kGen.getBytes(32);
		byte[] drvIv = kGen.getBytes(16);

		try {
			SecretKeySpec kSpec = new SecretKeySpec(drvKy, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, kSpec, new IvParameterSpec(drvIv));
			byte[] encrypted = cipher.doFinal(plainStream);
			return encrypted;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			Log4j.log.error(
					"[" + sessionId + "][drvRfc2898_encrypt] - #Computing AES encryption failed: " + e.getMessage());
//			return new byte[] {};
			throw e;
		}
	}

	/**
	 * Use base key to generate derived-key(32) and derived-iv(16), then compute
	 * AES-CBC-decrypt the cipher stream.
	 * 
	 * @param kBase      also used as the salt in RFC2898.
	 * @param cipherStream
	 * @param sessionId
	 * @return
	 * @throws GeneralSecurityException 
	 */
	public static byte[] drvRfc2898_decrypt(byte[] kBase, byte[] cipherStream, String sessionId) throws GeneralSecurityException {
		Rfc2898 kGen = null;
		try {
			kGen = new Rfc2898(kBase, kBase, 1000);

		} catch (InvalidKeyException | NoSuchAlgorithmException ex) {
			Log4j.log.error(
					"[" + sessionId + "][drvRfc2898_decrypt] - #Generate Rfc2898 key failed: " + ex.getMessage());
//			return new byte[] {};
			throw ex;
		}

		byte[] drvKy = kGen.getBytes(32);
		byte[] drvIv = kGen.getBytes(16);

		try {
			SecretKeySpec kSpec = new SecretKeySpec(drvKy, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, kSpec, new IvParameterSpec(drvIv));
			byte[] decrypted = cipher.doFinal(cipherStream);
			return decrypted;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			Log4j.log.error(
					"[" + sessionId + "][drvRfc2898_decrypt] - #Computing AES decryption failed: " + e.getMessage());
//			return new byte[] {};
			throw e;
		}
	}

}
