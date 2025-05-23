package com.Cvn.Encryptor;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

	private static final Map<String, Integer> HEX_MAP;
	static {
		HEX_MAP = new HashMap<>();
		HEX_MAP.put("0", 0);
		HEX_MAP.put("1", 1);
		HEX_MAP.put("2", 2);
		HEX_MAP.put("3", 3);
		HEX_MAP.put("4", 4);
		HEX_MAP.put("5", 5);
		HEX_MAP.put("6", 6);
		HEX_MAP.put("7", 7);
		HEX_MAP.put("8", 8);
		HEX_MAP.put("9", 9);
		HEX_MAP.put("a", 10);
		HEX_MAP.put("b", 11);
		HEX_MAP.put("c", 12);
		HEX_MAP.put("d", 13);
		HEX_MAP.put("e", 14);
		HEX_MAP.put("f", 15);
	}

	public static byte[] SHA256_To_Bytes(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(text.getBytes("UTF-8"));
		byte[] digest = md.digest();
		return digest;
	}

	public static byte[] decryptRFC2898(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
			throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		Rfc2898DeriveBytes keyGenerator = new Rfc2898DeriveBytes(keyBytes, keyBytes, 1000);

		byte[] bKey = keyGenerator.getBytes(32);
		byte[] bIv = keyGenerator.getBytes(16);

		IvParameterSpec ivSpec = new IvParameterSpec(bIv);
		SecretKeySpec newKey = new SecretKeySpec(bKey, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
		return cipher.doFinal(textBytes);
	}

	public static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
			throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		Rfc2898DeriveBytes keyGenerator = new Rfc2898DeriveBytes(keyBytes, keyBytes, 1000);

		byte[] bKey = keyGenerator.getBytes(32);
		byte[] bIv = keyGenerator.getBytes(16);
//		System.out.println("*** encrypt.textBytes:" + Encode.byteToHex(textBytes));
//		System.out.println("*** encrypt.bKey(byteToHex:" + Encode.byteToHex(bKey));
//		System.out.println("*** encrypt.bIv(byteToHex:" + Encode.byteToHex(bIv));

		IvParameterSpec ivSpec = new IvParameterSpec(bIv);
		SecretKeySpec newKey = new SecretKeySpec(bKey, "AES");
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
		return cipher.doFinal(textBytes);
	}

	public static byte[] encryptForFrontEnd(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
			throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		Rfc2898DeriveBytes keyGenerator = new Rfc2898DeriveBytes(keyBytes, keyBytes, 1000);

		byte[] bKey = keyGenerator.getBytes(32);
		byte[] bIv = keyGenerator.getBytes(16);

		IvParameterSpec ivSpec = new IvParameterSpec(bIv);
		SecretKeySpec newKey = new SecretKeySpec(bKey, "AES");
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
		return cipher.doFinal(textBytes);
	}

	public static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
			throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		Rfc2898DeriveBytes keyGenerator = new Rfc2898DeriveBytes(keyBytes, keyBytes, 1000);

		byte[] bKey = keyGenerator.getBytes(32);
		byte[] bIv = keyGenerator.getBytes(16);
//		System.out.println("*** decrypt.textBytes:" + Encode.byteToHex(textBytes));
//		System.out.println("*** decrypt.bKey(byteToHex:" + Encode.byteToHex(bKey));
//		System.out.println("*** decrypt.bIv(byteToHex:" + Encode.byteToHex(bIv));

		IvParameterSpec ivSpec = new IvParameterSpec(bIv);
		SecretKeySpec newKey = new SecretKeySpec(bKey, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
		return cipher.doFinal(textBytes);
	}

	public static String SHA256_To_HexString(String text)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(text.getBytes("UTF-8"));
		byte[] digest = md.digest();

		StringBuffer result = new StringBuffer();
		StringBuffer testResult = new StringBuffer();
		for (byte b : digest) {
			result.append(String.format("%02X", b)); // convert to hex
			testResult.append(String.format("%02x", b)); // convert to hex
		}
//		System.out.println("*** shaDevKeyB16:" + testResult);
		return result.toString();
	}

	public static byte[] HexString_To_Bytes(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		if (0 != text.length() % 2) {
			throw new UnsupportedEncodingException();
		}
		int size = text.length() / 2;
		byte[] result = new byte[size];

		Pattern pattern = Pattern.compile("[a-f0-9]{2}");
		Matcher matcher = pattern.matcher(text);
		int position = 0;

		while (matcher.find()) {
			String temp = matcher.group();
			result[position] = (byte) ((HEX_MAP.get(temp.substring(0, 1)) * 16) + HEX_MAP.get(temp.substring(1, 2)));
			position += 1;
		}

		return result;
	}

	public static String Bytes_To_HexString(byte[] bytes)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		StringBuffer result = new StringBuffer();
		for (byte b : bytes) {
			result.append(String.format("%02X", b)); // convert to hex
		}
		return result.toString();
	}
}
