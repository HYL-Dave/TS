package com.toppanidgate.idenkey.common.model;

public class ReturnCode {
	// Common
	public static final String Success = "0000";
	public static final String MemberNotFound = "0001";
	public static final String MemberStatusError = "0002";
	public static final String MemberLockedError = "0003";
	public static final String MemberDeletedError = "0004";
	public static final String MemberNotMatch = "0005";	// 沒用到
	public static final String TrustServerConnectionError = "0006";	// 沒用到
	public static final String TrustServerError = "0007";
	public static final String DatabaseError = "0008";
	public static final String InternalError = "0009";
	public static final String ParameterError = "0010";
	public static final String HTTP_POSTError = "0011";	// 沒用到
	public static final String ReturnMsgError = "0012";
	public static final String UnknownMethod = "0013";
	public static final String JsonParseError = "0014";
	public static final String ChannelInvalidError = "0015";
	public static final String KeyGenErr = "0018";
	public static final String Fail = "0020";	// Trust Server 用
	public static final String DecryptFailed = "0021";	// 沒用到
	public static final String ChannelNotMatchToMember = "0051";
	public static final String APIKeyInvalid = "0052";
	public static final String RequestExpired = "0057";
	public static final String EncryptFailed = "0058";	// 沒用到
	public static final String LockedForTooMuchFail = "0060";
//	public static final String IllegalArgument = "0070";
	
	// svfSetAuthType
	public static final String MemberChannelInvalidInResetAuthType = "0600";
	public static final String DecryptFailedInResetAuthType = "0601";
	public static final String HashGenFailedInResetAuthType = "0602";
	public static final String MsgCountInvalidInResetAuthType = "0603";
	public static final String MemberHashInvalidInResetAuthType = "0604";
	public static final String JSONErrInResetAuthType = "0605";
	public static final String TimeoutInResetAuthType = "0606";
	public static final String OTPInvalidInResetAuthType = "0607";
	public static final String TooManyAuthFailsInResetAuthType = "0608";
	public static final String CannotTurnOnInResetAuthType = "0609";
	public static final String BlockedAuthTypeInResetAuthType = "0610";
	public static final String CannotTurnOffInResetAuthType = "0611";
	public static final String EncryptFailedInResetAuthType = "0612";
	
	// svfChangeSetting
	public static final String MemberChannelInvalidInChangeSetting = "0650";
	public static final String DecryptFailedInChangeSetting = "0651";
	public static final String HashGenFailedInChangeSetting = "0652";
	public static final String MsgCountInvalidInChangeSetting = "0653";
	public static final String MemberHashInvalidInChangeSetting = "0654";
	public static final String JSONErrInChangeSetting = "0655";
	public static final String NotAnActiveAuthInChangeSetting = "0656";
	public static final String UnknownAuthTypeInChangeSetting = "0657";
	public static final String RequestInvalidInChangeSetting = "0658";
	public static final String TokenInvalidInChangeSetting = "0659";
	public static final String OTPInvalidInChangeSetting = "0660";
	public static final String TooManyAuthFailsInChangeSetting = "0661";
	public static final String PatternKeyIsSameInChangeSetting = "0662";
	public static final String DigitalKeyIsSameInChangeSetting = "0663";
	public static final String EncryptFailedInChangeSetting = "0664";
	
	// svfCheckSetting
	public static final String MemberChannelInvalidInCheckSetting = "0700";
	public static final String DecryptFailedInCheckSetting = "0701";
	public static final String HashGenFailedInCheckSetting = "0702";
	public static final String MsgCountInvalidInCheckSetting = "0703";
	public static final String MemberHashInvalidInCheckSetting = "0704";
	public static final String JSONErrInCheckSetting = "0705";
	public static final String TimeoutInCheckSetting = "0706";
	public static final String OTPInvalidInCheckSetting = "0707";
	public static final String NotAnActiveAuthInCheckSetting = "0709";
	public static final String BlockedAuthTypeInCheckSetting = "0710";
	public static final String BioHashInvalidInCheckSetting = "0711";
	public static final String DiagramHashInvalidInCheckSetting = "0712";
	public static final String PinHashInvalidInCheckSetting = "0713";
	public static final String FaceInvalidInCheckSetting = "0714";
	public static final String UnknownAuthTypeInCheckSetting = "0715";
	public static final String EncryptFailedInCheckSetting = "0716";
	
	// svfGetTxnList
	public static final String MemberChannelInvalidInTxnGetList = "0750";
	public static final String DecryptFailedInTxnGetList = "0751";
	public static final String HashGenFailedInTxnGetList = "0752";
	public static final String MsgCountInvalidInTxnGetList = "0753";
	public static final String MemberHashInvalidInTxnGetList = "0754";
	public static final String JSONErrInTxnGetList = "0755";
	public static final String EncryptFailedInTxnGetList = "0758";
	
	// svfCancelAuth
	public static final String MemberChannelInvalidInTxnCancelTxn = "0850";
	public static final String JSONErrInTxnCancelTxn = "0855";
	public static final String DecryptFailedInTxnCancelTxn = "0851";
	public static final String HashGenFailedInTxnCancelTxn = "0852";
	public static final String MsgCountInvalidInTxnCancelTxn = "0853";
	public static final String MemberHashInvalidInTxnCancelTxn = "0854";
	public static final String TxnNotFoundInTxnCancelTxn = "0856";
	public static final String TxnStatusErrInTxnCancelTxn = "0857";
	public static final String TxnTimeoutInTxnCancelTxn = "0858";

	//svfSignupDevice
	public static final String MemberChannelInvalidInSignupDevice = "0950";
	public static final String DecryptFailedInSignupDevice = "0951";
	public static final String HashGenFailedInSignupDevice = "0952";
	public static final String JSONErrInSignupDevice = "0953";
	public static final String ChannelInvalidInSignupDevice = "0954";
	public static final String MemberStatusIncorrectInSignupDevice = "0955";
	public static final String TimeoutInSignupDevice = "0956";
	public static final String BlockedAuthTypeInSignupDevice = "0960";
	public static final String EncryptFailedInSignupDevice = "0958";
	
	// SvfOfflineOTPRequest
	public static final String EncryptFailedInSvfOfflineOTPRequest = "1000";
	
	// SvfGetToken
	public static final String EncryptFailedInSvfGetToken = "1010";
	
	public static final String PersoUpdate = "9998";
	
	// VerifyOfflineOtp
	public static final String VerifyFailedInVerifyOfflineOtp = "1290";
	
	// svfSendDeregResponse
	public static final String DecryptFailedInSendDeregResponse = "2501";
	public static final String HashGenFailedInSendDeregResponse = "2502";
	public static final String JSONErrInSendDeregResponse = "2505";
	public static final String MemberHashInvalidInSendDeregResponse = "2504";
	public static final String MsgCountInvalidInSendDeregResponse = "2503";
	public static final String OTPInvalidInSendDeregResponse = "2506";
	public static final String TimeoutInSendDeregResponse = "2507";
	public static final String DeviceHashErrInSendDeregResponse = "2517";
	public static final String EncryptFailedInSendDeregResponse = "2558";
				
	// svfSendAuthResponse
	public static final String BlockedAuthTypeInSendAuthResponse = "2410";
	public static final String TxnNotFoundInSendAuthResponse = "2456";
	public static final String TxnStatusErrInSendAuthResponse = "2408";
	public static final String AuthTypeNotSetInSendAuthResponse = "2458";
	public static final String TxnTimeoutInSendAuthResponse = "2409";
	public static final String BioInvalidInSendAuthResponse = "2411";
	public static final String PatternInvalidInSendAuthResponse = "2412";
	public static final String PatternAuthDisabledInSendAuthResponse = "2416";
	public static final String DigitalInvalidInSendAuthResponse = "2413";
	public static final String TxnDataNotMatchInSendAuthResponse = "2414";
	public static final String OTPInvalidInSendAuthResponse = "2407";
	public static final String HashGenFailedInSendAuthResponse = "2402";
	public static final String DeviceHashErrInSendAuthResponse = "2417";
	public static final String ChannelInvalidInSendAuthResponse = "2454";
	public static final String MemberChannelInvalidInSendAuthResponse = "2400";
	public static final String DecryptFailedInSendAuthResponse = "2451";
	public static final String JSONErrInSendAuthResponse = "2453";
	public static final String MemberHashInvalidInSendAuthResponse = "2404";
	public static final String MsgCountInvalidInSendAuthResponse = "2403";
	
	// svfGetAuthRequest
	public static final String BlockedAuthTypeInGetAuthRequest = "2310";
	public static final String InvalidAuthTypeInGetAuthRequest = "2320";
	public static final String HashGenFailedInGetAuthRequest = "2302";
	public static final String EncryptFailedInGetAuthRequest = "2358";
	
	// svfSendRegResponse
	public static final String BlockedAuthTypeInSendRegResponse = "2210";
	public static final String ChannelInvalidInSendRegResponse = "2254";
	public static final String JSONErrInSendRegResponse = "2253";
	public static final String DecryptFailedInSendRegResponse = "2251";
	public static final String EncryptFailedInSendRegResponse = "2258";
	public static final String MsgCountInvalidInSendRegResponse = "2203";
	
	// svfGetRegRequest
	public static final String KeyTypeNotMatchInSendAuthResponse = "2109";
	public static final String ChannelInvalidInGetRegRequest = "2154";
	
	// svfGetTxnData
	public static final String MemberChannelInvalidInGetTxnData = "3050";
	public static final String HashGenFailedInGetTxnData = "3052";
	public static final String TxnNotFoundInGetTxnData = "3056";
	public static final String ChannelInvalidInGetTxnData = "3054";
	
	// svGetAuthStatus
	public static final String MemberChannelInvalidInGetAuthStatus = "3150";
	public static final String TxnNotFoundInGetAuthStatus = "3156";

	// svfOfflineOTPResponse
	public static final String TxnNotFoundInOfflineOTPResponse = "3256";
	public static final String TxnStatusErrInOfflineOTPResponse = "3257";
	public static final String OTPInvalidInOfflineOTPResponse = "3260";

	// svfDecryptECDH
	public static final String MemberChannelInvalidInDecryptECDH = "3300";
	public static final String MemberKeyNotFoundInDecryptECDH = "3301";
	public static final String DecryptErrInDecryptECDH = "3302";

	// svfEncryptECDH
	public static final String MemberChannelInvalidInEncryptECDH = "3350";
	public static final String MemberKeyNotFoundInEncryptECDH = "3351";
	public static final String DecryptErrInEncryptECDH = "3352";
}
	