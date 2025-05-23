package com.Cvn.Encryptor;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.toppanidgate.fidouaf.common.model.Log4j;

import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

// Author: Calvin @ iDGate.com
public class Hash {

	public Hash() {

	}

	public static String encode_SHA256_Hex(String plainTxt, String sessionId) {

		MessageDigest myDigest = null;
		try {
			myDigest = MessageDigest.getInstance("SHA-256");
			myDigest.update(plainTxt.getBytes("UTF-8"));
			return Encode.byteToHex(myDigest.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Log4j.log.error("[" + sessionId + "][Hash] - #Computing encrypt-SHA256-Hex error:[" + e.getMessage() + "]");
			return "";
		}
	}

	public static byte[] encode_SHA256(byte[] plainTxt, String sessionId) {

		MessageDigest myDigest = null;
		try {
			myDigest = MessageDigest.getInstance("SHA-256");
			myDigest.update(plainTxt);
			return myDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			Log4j.log.error("[" + sessionId + "][Hash] - #Computing encrypt-SHA256 error:[" + e.getMessage() + "]");
			return new byte[] {};
		}
	}

	public static String encode_SHA1_Hex(String plainTxt, String sessionId) {

		MessageDigest myDigest = null;
		try {
			myDigest = MessageDigest.getInstance("SHA-1");
			myDigest.update(plainTxt.getBytes("UTF-8"));
			return Encode.byteToHex(myDigest.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Log4j.log.error("[" + sessionId + "][Hash] - #Computing encrypt-SHA1-Hex error:[" + e.getMessage() + "]");
			return "";
		}
	}

	public static byte[] encode_SHA1(byte[] plainTxt, String sessionId) {

		MessageDigest myDigest = null;
		try {
			myDigest = MessageDigest.getInstance("SHA-1");
			myDigest.update(plainTxt);
			return myDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			Log4j.log.error("[" + sessionId + "][Hash] - #Computing encrypt-SHA1 error:[" + e.getMessage() + "]");
			return new byte[] {};
		}
	}

	public static byte[] encode_HmacSHA1(byte[] plainTxt, byte[] k, String sessionId) throws Exception {

		SecretKeySpec kSpec = new SecretKeySpec(k, "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(kSpec);
		byte[] encHmacSha1 = mac.doFinal(plainTxt);

//		Log4j.log
//				.debug("[" + sessionId + "][encrypt_HmacSHA1] - #encHmacSha1:(" + Encode.byteToHex(encHmacSha1) + ").");
		return encHmacSha1;
	}

	public static String encode_CRC32_Hex(byte[] plainStream, int length, String sessionId) throws Exception {

		Checksum myCalculator = new CRC32();
		myCalculator.update(plainStream, 0, plainStream.length);

		// get the current checksum value
		long checksum = myCalculator.getValue();
		String hex = Long.toHexString(checksum).toUpperCase(Locale.getDefault());
		while (hex.length() < length) {
			hex = "0" + hex;
		}

//		Log4j.log.debug("[" + sessionId + "][encrypt_CRC32_Hex] - #newPersoChkLong:[" + checksum
//				+ "] - #newPersoChkHex:[" + hex + "]");
		return hex;
	}

	public static long encode_CRC32_Long(byte[] plainStream) throws Exception {

		Checksum myCalculator = new CRC32();
		myCalculator.update(plainStream, 0, plainStream.length);

		// get the current checksum value
		long checksum = myCalculator.getValue();

		return checksum;
	}

}
