package com.ECIES.model;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.Cvn.Encryptor.AESUtil;
import com.Cvn.Encryptor.Encode;
import com.toppanidgate.fidouaf.common.model.Log4j;

public class ECIES {

	/**
	 * Decrypt a ECDH encrypted msg
	 * 
	 * @param encryptB64Text Msg that need to be decrypted
	 * @param publicKey      Element to calculate the secret key, B64 format
	 * @param privateKey     Element to calculate the secret key, B64 format
	 * @return decrypted plain text in String type
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws UnsupportedEncodingException
	 */
	public static String decrypt(String encryptB64Text, String publicKey, String privateKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException,
			UnsupportedEncodingException {

		PublicKey pubKey = ECCUtil.getPublicKey(Encode.hexToByte(publicKey));
		PrivateKey privKey = ECCUtil.getPrivateKey(Encode.hexToByte(privateKey));
		byte[] encryptTxt = Encode.hexToByte(encryptB64Text);

		return decrypt(encryptTxt, pubKey, privKey);
	}

	private static String decrypt(byte[] encryptData, PublicKey publicKey, PrivateKey privateKey)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException,
			UnsupportedEncodingException {

		// Get share secret key
		byte[] shareSecretKey = ECCUtil.getSecretKey(publicKey, privateKey);
		// TODO 
		Log4j.log.debug("***[decrypt] shareSecretKey:[{}]", Encode.byteToHex(shareSecretKey));

		// Encrypt data decrypt with AES
//		byte[] decryptData = AESUtil.decrypt(encryptData, shareSecretKey);
		byte[] decryptData = AESUtil.decrypt(null, shareSecretKey, encryptData);

		return new String(decryptData, "UTF-8");
	}

	/**
	 * Encrypt a msg using ECDH key
	 * 
	 * @param plainText  the original msg
	 * @param publicKey  Element to calculate the secret key, B64 format
	 * @param privateKey Element to calculate the secret key, B64 format
	 * @return encrypted msg, B64 format
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws UnsupportedEncodingException
	 */
	public static String encrypt(String plainText, String publicKey, String privateKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException,
			UnsupportedEncodingException {

		PublicKey pubKey = ECCUtil.getPublicKey(Encode.hexToByte(publicKey));
		PrivateKey privKey = ECCUtil.getPrivateKey(Encode.hexToByte(privateKey));

		return encrypt(plainText, pubKey, privKey);
	}

	private static String encrypt(String plainText, PublicKey publicKey, PrivateKey privateKey)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException,
			UnsupportedEncodingException {

		// Get share secret key
		byte[] shareSecretKey = ECCUtil.getSecretKey(publicKey, privateKey);
		// TODO 
		Log4j.log.debug("***[encrypt] shareSecretKey:[{}]", Encode.byteToHex(shareSecretKey));

		// PlainText encrypt with AES
//		byte[] encryptPlainTextWithAES = AESUtil.encrypt(plainText.getBytes("UTF-8"), shareSecretKey);
		byte[] encryptPlainTextWithAES = AESUtil.encrypt(null, shareSecretKey, plainText.getBytes("UTF-8"));

		return Encode.byteToHex(encryptPlainTextWithAES);
	}
}
