package com.TrustServer.Func;

import java.io.IOException;

import com.Cvn.Core.CvnCore;
import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;

/**
 * @author SK
 * @Date 2022/12/28
 */
public class TS_MainFunc {
	private static final Gson gson = new Gson();

	public static final String SvVersion = "0.6.21";

	private static String ExCfgPath;

	private CvnCore myCore;

	public TS_MainFunc(String cfgPath, String log4jCfgPath) throws IOException {
		ExCfgPath = cfgPath;
		new Log4j(log4jCfgPath);
		myCore = new CvnCore();
	}

	public static String getExCfgPath() {
		return ExCfgPath;
	}

	/**
	 * Create a persofile with given user info
	 * 
	 * @param channel      user channel code
	 * @param devType      device type
	 * @param esnSecret    ESN secret
	 * @param seedSecret   seed secret
	 * @param masterSecret master secret
	 * @param userID       user ID
	 * @param devData      device data
	 * @param pinHash      PIN hash
	 * @param sessID       session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, ESN, Mercury, DevID, PersoFile
	 */
	public String signupPerso(String channel, String devType, String esnSecret, String seedSecret, String masterSecret,
			String userID, String devData, String pinHash, String sessID) {
		return myCore.signupPerso(channel, devType, esnSecret, seedSecret, masterSecret, userID, devData, pinHash,
				sessID);
	}
	
	public String signupPersoFIDO(String channel, String devType, String esnSecret, String seedSecret, String masterSecret,
			String userID, String devData, String pinHash, String sessID) {
		return myCore.signupPersoFIDO(channel, devType, esnSecret, seedSecret, masterSecret, userID, devData, pinHash,
				sessID);
	}

	/**
	 * Get current device status info from ESN
	 * 
	 * @param esn    device ESN
	 * @param sessID session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, UserID, DevData, Mercury, ErrCount,
	 *         ErrMax, Status, DevID. In case of error, only ReturnCode and
	 *         ReturnMsg will be returned
	 */
	public String get_DeviceInfo(String esn, String sessID) {
		return myCore.getDeviceInfo(esn, sessID);
	}

	/**
	 * Lock a specified device to use verification system
	 * 
	 * @param esn    device ESN
	 * @param remark comment to be logged
	 * @param sessID session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String lock_Device(String esn, String remark, String sessID) {
		return myCore.lockDevice(esn, remark, sessID);
	}

	/**
	 * Unlock a specified device to use verification system
	 * 
	 * @param esn    device ESN
	 * @param remark comment to be logged
	 * @param sessID session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String unlock_Device(String esn, String remark, String sessID) {
		return myCore.unlockDevice(esn, remark, sessID);
	}

	/**
	 * Update a specified device's mercury value
	 * 
	 * @param esn    device ESN
	 * @param sessID session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String update_Mercury(String esn, String sessID) {
		return myCore.updateMercury(esn, sessID);
	}

	/**
	 * Generate a OTP for a specified device with txnData and challenge constrains
	 * 
	 * @param channel   user channel code
	 * @param esn       device ESN
	 * @param challenge variant code for OTP
	 * @param txnData   variant data for OTP
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, OTP. In case of error, only
	 *         ReturnCode, ReturnMsg will be returned
	 */
	public String generate_OTP(String channel, String esn, String challenge, String txnData, String sessID) {
		return myCore.generateOTP(channel, esn, challenge, txnData, sessID);
	}

	/**
	 * Verify a OTP that is generated from Device ID. L1 OTP
	 * 
	 * @param channel   user channel code
	 * @param esn       device ESN
	 * @param challenge variant code for OTP
	 * @param txnData   variant data for OTP
	 * @param otp       otp code that need to be verified
	 * @param timeStamp reference timestamp, only for logging
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String verifyDevIdOTP(String channel, String esn, String challenge, String txnData, String otp,
			String timeStamp, String sessID) {
		return myCore.verifyDevIdOTP(channel, esn, challenge, txnData, otp, timeStamp, sessID);
	}

	/**
	 * Verify a OTP that is generated from ESN. L2 OTP
	 * 
	 * @param channel   user channel code
	 * @param esn       device ESN
	 * @param challenge variant code for OTP
	 * @param txnData   variant data for OTP
	 * @param otp       otp code that need to be verified
	 * @param timeStamp reference timestamp, only for logging
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String verifyEsnOTP(String channel, String esn, String challenge, String txnData, String otp,
			String timeStamp, String sessID) {
		return myCore.verifyEsnOTP(channel, esn, challenge, txnData, otp, timeStamp, sessID);
	}

	/**
	 * Verify a OTP that is generated from Seed. L3 OTP
	 * 
	 * @param channel   user channel code
	 * @param esn       device ESN
	 * @param challenge variant code for OTP
	 * @param txnData   variant data for OTP
	 * @param otp       otp code that need to be verified
	 * @param timeStamp reference timestamp, only for logging
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String verify_OTP(String channel, String esn, String challenge, String txnData, String otp, String timeStamp,
			String sessID) {
		return myCore.verifyOTP(channel, esn, challenge, txnData, otp, timeStamp, sessID);
	}
	
	public String verifyOTP(String channel, String esn, String challenge, String txnData, String otp, String timeStamp,
			String sessID) {
		return myCore.verifyOTP(channel, esn, challenge, txnData, otp, timeStamp, sessID);
	}

	/**
	 * Generate a simple OTP for sending via SMS
	 * 
	 * @param channel user channel code
	 * @param txnData variant data for OTP
	 * @param sessID  session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, OTP. In case of error, only
	 *         ReturnCode, ReturnMsg will be returned
	 */
	public String generate_SmsOTP(String channel, String txnData, String sessID) {
		return myCore.generateSmsOTP(channel, txnData, sessID);
	}

	/**
	 * Verify a OTP that is generated from generate_SmsOTP
	 * 
	 * @param channel   user channel code
	 * @param txnData   variant data for OTP
	 * @param otp       otp code that need to be verified
	 * @param timeStamp reference timestamp, only for logging
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String verify_SmsOTP(String channel, String txnData, String otp, String timeStamp, String sessID) {
		return myCore.verifySmsOTP(channel, txnData, otp, timeStamp, sessID);
	}

	/**
	 * Create a new RSA key
	 * 
	 * @param keyAlias encryption key alias
	 * @param sessID   session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String createKey_RSA(String keyAlias, String sessID) {
		return myCore.createKey_RSA(keyAlias, sessID);
	}

	/**
	 * Encrypt a string of text with specified RSA key
	 * 
	 * @param plainTxt text that you want to encrypt
	 * @param keyAlias encrytion key name that you want to use
	 * @param sessID   session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, EncData
	 */
	public String encrypt_RSA(String plainTxt, String keyAlias, String sessID) {
		return myCore.encrypt_RSA(plainTxt, keyAlias, sessID);
	}

	/**
	 * Decrypt a string of text with specified RSA key
	 * 
	 * @param cipherTxt text that you want to decrypt
	 * @param keyAlias  encrytion key name that you want to use
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, Data
	 */
	public String decrypt_RSA(String cipherTxt, String keyAlias, String sessID) {
		return myCore.decrypt_RSA(cipherTxt, keyAlias, sessID);
	}

	/**
	 * Get RSA public key
	 * 
	 * @param sessID session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, RsaSecret
	 */
	public String get_RsaPublic(String sessID) {
		return myCore.get_RsaPublic(sessID);
	}

	/**
	 * Create a new AES encryption key
	 * 
	 * @param random   salt string that will be involved to create AES key
	 * @param keyAlias encryption key alias
	 * @param sessID   session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String createKey_AES(String random, String keyAlias, String sessID) {
		return myCore.createKey_AES(random, keyAlias, sessID);
	}

	/**
	 * Encrypt a string of text with specified AES key
	 * 
	 * @param plainTxt text that you want to encrypt
	 * @param keyAlias encryption key alias
	 * @param sessID   session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, EncData
	 */
	public String encrypt_AES(String plainTxt, String keyAlias, String sessID) {
		return gson.toJson(myCore.encrypt_AES(plainTxt, keyAlias, sessID));
	}
	
	/**
	 * Encrypt a string of text with specified AES key. But this function would always call HSM
	 * 
	 * @param plainTxt text that you want to encrypt
	 * @param keyAlias encryption key alias
	 * @param sessID   session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, EncData
	 */
	public String encrypt_AES_always_HSM(String plainTxt, String keyAlias, String sessID) {
		return gson.toJson(myCore.encrypt_AES_always_HSM(plainTxt, keyAlias, sessID));
	}

	/**
	 * Decrypt a string of text with specified AES key
	 * 
	 * @param cipherTxt text that you want to decrypt
	 * @param keyAlias  encrytion key name that you want to use
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, Data
	 */
	public String decrypt_AES(String cipherTxt, String keyAlias, String sessID) {
		return myCore.decrypt_AES(cipherTxt, keyAlias, sessID);
	}
	
	/**
	 * Decrypt a string of text with specified AES key. But this function would always call HSM
	 * 
	 * @param cipherTxt text that you want to decrypt
	 * @param keyAlias  encrytion key name that you want to use
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, Data
	 */
	public String decrypt_AES_always_HSM(String cipherTxt, String keyAlias, String sessID) {
		return gson.toJson(myCore.decrypt_AES_always_HSM(cipherTxt, keyAlias, sessID));
	}

	/**
	 * Decrypt a composite webpin package
	 * 
	 * @param aesSecret key part in the package
	 * @param encData   data part in the package
	 * @param devData   device data part for decryption
	 * @param sessID    session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg, Data. In case of error, only
	 *         ReturnCode, ReturnMsg will be returned
	 */
	public String decrypt_Package(String aesSecret, String encData, String devData, String sessID) {
		return myCore.decrypt_Package(aesSecret, encData, devData, sessID);
	}

	/**
	 * Create a new channel with its own OTP settings
	 * 
	 * @param channelID    channel ID, or channel code. This is a unique ID that
	 *                     will be used for the other functions
	 * @param channelName  channel name, this is serve as a comment part for human
	 *                     identification
	 * @param otpLength    OTP string length
	 * @param timeInterval OTP valid time interval
	 * @param timeRange    OTP will be valid in those range. For example, if you set
	 *                     it as 2, then it will be valid between -2 to +2 range(0
	 *                     included)
	 * @param sessID       session ID for logging
	 * @return JSON with ReturnCode, ReturnMsg
	 */
	public String add_Channel(String channelID, String channelName, int otpLength, long timeInterval, int timeRange,
			String sessID) {
		return myCore.add_Channel(channelID, channelName, otpLength, timeInterval, timeRange, sessID);
	}

	/**
	 * List all channel info that is in DB
	 * 
	 * @param sessID session ID for logging
	 * @return JSON array of channel info that contains Channel_ID, Channel_Name,
	 *         OTP_Length, OTP_Interval, OTP_Range, Create_Date, Last_Modified
	 */
	public String list_Channel(String sessID) {
		return myCore.list_Channel(sessID);
	}

	/**
	 * Get a channel info by channel id
	 * 
	 * @param channelID Channel ID, or Channel code
	 * @param sessID    session ID for logging
	 * @return JSON with Channel_ID, Channel_Name, OTP_Length, OTP_Interval,
	 *         OTP_Range, Create_Date, Last_Modified
	 */
	public String get_Channel_ByID(String channelID, String sessID) {
		return myCore.get_Channel_ByID(channelID, sessID);
	}
}
