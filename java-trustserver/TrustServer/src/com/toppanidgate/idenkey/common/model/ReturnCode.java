package com.toppanidgate.idenkey.common.model;

/**
 *  0000 = Success<br>
 *  0001 = iDGateID not found<br>
 *  0002 = iDGateID status error<br>
 *  0003 = iDGateID locked<br>
 *  0004 = iDGateID deleted<br>
 *  0005 = iDGateID not match<br>
 *  0006 = Trust Server connection error<br>
 *  0007 = Trust Server error<br>
 *  0008 = Database error<br>
 *  0009 = Internal error<br>
 *  0010 = Input parameter error<br>
 *  0011 = POST to remote service failed<br>
 *  0012 = Return message error<br>
 *  0013 = Unknown method<br>
 *  0014 = JSON parse error<br>
 *  0015 = Channel invalid<br>
 *  0018 = Failed to generate key<br>
 *  0020 = OTP invalid<br>
 *  0021 = Data decrypt failed<br>
 *  0051 = Channel name doesn't match to the member<br>
 *  0052 = API key invalid<br>
 *  0057 = This request is timed out. Please refresh and try again.<br>
 *  0058 = Data encryption failure<br>
 *  0060 = Locked for too many fail authentication<br>
 *  0070 = Illegal Argument<br>
 *  0600 = Member channel invalid<br>
 *  0601 = Data decrypt failed<br>
 *  0602 = Failed to generate hash<br>
 *  0603 = Message invalid<br>
 *  0604 = Member hash invalid<br>
 *  0605 = JSON parse error<br>
 *  0606 = Request timeout<br>
 *  0607 = OTP invalid<br>
 *  0608 = Locked for too many fail authentication<br>
 *  0609 = Cannot turn on an authentication type which is on already<br>
 *  0610 = Blocked authentication type<br>
 *  0611 = Cannot turn off an authentication type which is off already<br>
 *  0612 = Data encryption failure<br>
 *  0650 = Member channel invalid<br>
 *  0651 = Data decrypt failed<br>
 *  0652 = Failed to generate hash<br>
 *  0653 = Message invalid<br>
 *  0654 = Member hash invalid<br>
 *  0655 = JSON parse error<br>
 *  0656 = Not an active authentication type<br>
 *  0657 = Unknown authentication type<br>
 *  0658 = Request invalid<br>
 *  0659 = Token invalid<br>
 *  0660 = OTP invalid<br>
 *  0661 = Locked for too many fail authentication<br>
 *  0662 = Pattern key is the same as set currently<br>
 *  0663 = Digital key is the same as set currently<br>
 *  0664 = Data encryption failure<br>
 *  0700 = Member channel invalid<br>
 *  0701 = Data decrypt failed<br>
 *  0702 = Failed to generate hash<br>
 *  0703 = Message invalid<br>
 *  0704 = Member hash invalid<br>
 *  0705 = JSON parse error<br>
 *  0706 = Request timeout<br>
 *  0707 = OTP invalid<br>
 *  0709 = Not an active authentication type<br>
 *  0710 = Authentication type blocked<br>
 *  0711 = Biological identify invalid<br>
 *  0712 = Pattern identify invalid<br>
 *  0713 = Digital identify invalid<br>
 *  0714 = Face identify invalid<br>
 *  0715 = Unknown authentication type<br>
 *  0716 = Data encryption failure<br>
 *  0750 = Member channel invalid<br>
 *  0751 = Data decrypt failed<br>
 *  0752 = Failed to generate hash<br>
 *  0753 = Message invalid<br>
 *  0754 = Member hash invalid<br>
 *  0755 = JSON parse error<br>
 *  0758 = Data encryption failure<br>
 *  0850 = Member channel invalid<br>
 *  0851 = Data decrypt failed<br>
 *  0852 = Failed to generate hash<br>
 *  0853 = Message invalid<br>
 *  0854 = Member hash invalid<br>
 *  0855 = JSON parse error<br>
 *  0856 = Transaction not found<br>
 *  0857 = Transaction status error<br>
 *  0858 = Transaction timeout<br>
 *  0950 = Member channel invalid<br>
 *  0951 = Data decrypt failed<br>
 *  0952 = Failed to generate hash<br>
 *  0953 = JSON parse error<br>
 *  0954 = Channel invalid<br>
 *  0955 = iDGateID status error<br>
 *  0956 = Timeout<br>
 *  0958 = Data encrypt failed<br>
 *  0960 = Blocked authentication type<br>
 *  1000 = Member channel invalid<br>
 *  1010 = Data encryption failure<br>
 *  9998 = Need to update security component<br>
 *  1290 = Verification failure<br>
 *  2501 = Data decrypt failed<br>
 *  2502 = Failed to generate hash<br>
 *  2505 = JSON parse error<br>
 *  2504 = Member hash invalid<br>
 *  2503 = Message invalid<br>
 *  2506 = OTP invalid<br>
 *  2507 = Request timeout<br>
 *  2517 = Device hash error<br>
 *  2558 = Data encryption failure<br>
 *  2410 = Blocked authentication type<br>
 *  2456 = Transaction not found<br>
 *  2408 = Transaction status error<br>
 *  2458 = Authentication type not set<br>
 *  2409 = Transaction timeout<br>
 *  2411 = Biological identify invalid<br>
 *  2412 = Pattern identify invalid<br>
 *  2416 = Pattern authentication fails reached limit<br>
 *  2413 = Digital identify invalid<br>
 *  2414 = Transaction data doesn't match<br>
 *  2407 = OTP invalid<br>
 *  2402 = Failed to generate hash<br>
 *  2417 = Device hash error<br>
 *  2454 = Channel invalid<br>
 *  2400 = Member channel invalid<br>
 *  2451 = Data decrypt failed<br>
 *  2453 = JSON parse error<br>
 *  2404 = Member hash invalid<br>
 *  2403 = Message invalid<br>
 *  2320 = Invalid authentication type<br>
 *  2302 = Failed to generate hash<br>
 *  2358 = Data encryption failure<br>
 *  2210 = Blocked authentication type<br>
 *  2254 = Channel invalid<br>
 *  2253 = JSON parse error<br>
 *  2251 = Data decrypt failed<br>
 *  2258 = Data encryption failure<br>
 *  2203 = Message invalid<br>
 *  2109 = KeyType not match<br>
 *  2154 = Channel invalid<br>
 *  3050 = Member channel invalid<br>
 *  3052 = Failed to generate hash<br>
 *  3056 = Transaction not found<br>
 *  3054 = Channel invalid<br>
 *  3150 = Member channel invalid<br>
 *  3156 = Transaction not found<br>
 *  3256 = Transaction not found<br>
 *  3257 = Transaction status error<br>
 *  3260 = OTP invalid
 */
public class ReturnCode {
	// Common
	static final public String Success = "0000";
	static final public String MemberNotFound = "0001";
	static final public String MemberStatusError = "0002";
	static final public String MemberLockedError = "0003";
	static final public String MemberDeletedError = "0004";
	static final public String MemberNotMatch = "0005";
	static final public String TrustServerConnectionError = "0006";
	static final public String TrustServerError = "0007";
	static final public String DatabaseError = "0008";
	static final public String InternalError = "0009";
	static final public String ParameterError = "0010";
	static final public String HTTP_POSTError = "0011";
	static final public String ReturnMsgError = "0012";
	static final public String UnknownMethod = "0013";
	static final public String JsonParseError = "0014";
	static final public String ChannelInvalidError = "0015";
	public static final String KeyGenErr = "0018";
	public static final String Fail = "0020";
	public static final String DecryptFailed = "0021";
	static final public String ChannelNotMatchToMember = "0051";
	static final public String APIKeyInvalid = "0052";
	static final public String RequestExpired = "0057";
	public static final String EncryptFailed = "0058";
	static final public String LockedForTooMuchFail = "0060";
	static final public String IllegalArgument = "0070";
	
	// svfSetAuthType
	static final public String MemberChannelInvalidInResetAuthType = "0600";
	static final public String DecryptFailedInResetAuthType = "0601";
	static final public String HashGenFailedInResetAuthType = "0602";
	static final public String MsgCountInvalidInResetAuthType = "0603";
	static final public String MemberHashInvalidInResetAuthType = "0604";
	static final public String JSONErrInResetAuthType = "0605";
	static final public String TimeoutInResetAuthType = "0606";
	static final public String OTPInvalidInResetAuthType = "0607";
	static final public String TooManyAuthFailsInResetAuthType = "0608";
	static final public String CannotTurnOnInResetAuthType = "0609";
	static final public String BlockedAuthTypeInResetAuthType = "0610";
	static final public String CannotTurnOffInResetAuthType = "0611";
	static final public String EncryptFailedInResetAuthType = "0612";
	
	// svfChangeSetting
	static final public String MemberChannelInvalidInChangeSetting = "0650";
	static final public String DecryptFailedInChangeSetting = "0651";
	static final public String HashGenFailedInChangeSetting = "0652";
	static final public String MsgCountInvalidInChangeSetting = "0653";
	static final public String MemberHashInvalidInChangeSetting = "0654";
	static final public String JSONErrInChangeSetting = "0655";
	static final public String NotAnActiveAuthInChangeSetting = "0656";
	static final public String UnknownAuthTypeInChangeSetting = "0657";
	static final public String RequestInvalidInChangeSetting = "0658";
	static final public String TokenInvalidInChangeSetting = "0659";
	static final public String OTPInvalidInChangeSetting = "0660";
	static final public String TooManyAuthFailsInChangeSetting = "0661";
	static final public String PatternKeyIsSameInChangeSetting = "0662";
	static final public String DigitalKeyIsSameInChangeSetting = "0663";
	static final public String EncryptFailedInChangeSetting = "0664";
	
	// svfCheckSetting
	static final public String MemberChannelInvalidInCheckSetting = "0700";
	static final public String DecryptFailedInCheckSetting = "0701";
	static final public String HashGenFailedInCheckSetting = "0702";
	static final public String MsgCountInvalidInCheckSetting = "0703";
	static final public String MemberHashInvalidInCheckSetting = "0704";
	static final public String JSONErrInCheckSetting = "0705";
	static final public String TimeoutInCheckSetting = "0706";
	static final public String OTPInvalidInCheckSetting = "0707";
	static final public String NotAnActiveAuthInCheckSetting = "0709";
	static final public String BlockedAuthTypeInCheckSetting = "0710";
	static final public String BioHashInvalidInCheckSetting = "0711";
	static final public String DiagramHashInvalidInCheckSetting = "0712";
	static final public String PinHashInvalidInCheckSetting = "0713";
	static final public String FaceInvalidInCheckSetting = "0714";
	static final public String UnknownAuthTypeInCheckSetting = "0715";
	static final public String EncryptFailedInCheckSetting = "0716";
	
	// svfGetTxnList
	static final public String MemberChannelInvalidInTxnGetList = "0750";
	static final public String DecryptFailedInTxnGetList = "0751";
	static final public String HashGenFailedInTxnGetList = "0752";
	static final public String MsgCountInvalidInTxnGetList = "0753";
	static final public String MemberHashInvalidInTxnGetList = "0754";
	static final public String JSONErrInTxnGetList = "0755";
	static final public String EncryptFailedInTxnGetList = "0758";
	
	// svfCancelAuth
	static final public String MemberChannelInvalidInTxnCancelTxn = "0850";
	static final public String JSONErrInTxnCancelTxn = "0855";
	static final public String DecryptFailedInTxnCancelTxn = "0851";
	static final public String HashGenFailedInTxnCancelTxn = "0852";
	static final public String MsgCountInvalidInTxnCancelTxn = "0853";
	static final public String MemberHashInvalidInTxnCancelTxn = "0854";
	static final public String TxnNotFoundInTxnCancelTxn = "0856";
	static final public String TxnStatusErrInTxnCancelTxn = "0857";
	static final public String TxnTimeoutInTxnCancelTxn = "0858";

	//svfSignupDevice
	static final public String MemberChannelInvalidInSignupDevice = "0950";
	static final public String DecryptFailedInSignupDevice = "0951";
	static final public String HashGenFailedInSignupDevice = "0952";
	static final public String JSONErrInSignupDevice = "0953";
	static final public String ChannelInvalidInSignupDevice = "0954";
	public static final String MemberStatusIncorrectInSignupDevice = "0955";
	static final public String TimeoutInSignupDevice = "0956";
	static final public String BlockedAuthTypeInSignupDevice = "0960";
	static final public String EncryptFailedInSignupDevice = "0958";

	// SvfOfflineOTPRequest
	public static final String EncryptFailedInSvfOfflineOTPRequest = "1000";

	// SvfGetToken
	public static final String EncryptFailedInSvfGetToken = "1010";

	static final public String PersoUpdate = "9998";

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
	static final public String OTPInvalidInOfflineOTPResponse = "3260";
	
	// svfDecryptECDH
	public static final String MemberChannelInvalidInDecryptECDH = "3300";
	public static final String MemberKeyNotFoundInDecryptECDH = "3301";
	public static final String DecryptErrInDecryptECDH = "3302";

	// svfEncryptECDH
	public static final String MemberChannelInvalidInEncryptECDH = "3350";
	public static final String MemberKeyNotFoundInEncryptECDH = "3351";
	public static final String DecryptErrInEncryptECDH = "3352";
}
	