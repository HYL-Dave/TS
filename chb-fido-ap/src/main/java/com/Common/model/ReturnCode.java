package com.Common.model;

public class ReturnCode {

	public static final String Success = "00000";
	public static final String MemberNotFound = "M0001";
	public static final String MemberStatusError = "M0002";
	public static final String MemberLockedError = "M0003";
	public static final String MemberDeletedError = "M0004";
	public static final String MemberNotMatch = "M0005";
	public static final String FidoSVConnectionError = "M0006";
	public static final String FidoSVError = "M0007";
	public static final String DatabaseError = "M0008";
	public static final String InternalError = "M0009";
	public static final String ParameterError = "M0010";
	public static final String JsonParseError = "M0014";
	public static final String ChannelInvalidError = "M0015";
	public static final String PushSVError = "M0016";

	public static final String ChannelNotMatchToMember = "M0051";
	public static final String APIKeyInvalid = "M0052";

	public static final String CannotAddAuthInSendRegResponse = "M0100";
	public static final String CannotReplaceAuthInSendRegResponse = "M0101";
	public static final String CannotDisableAllAuthInSendRegResponse = "M0102";
	public static final String CannotCreateNewMemberInSendRegResponse = "M0103";
	
	public static final String CannotUseQRCodeInOfflineOTPRequest = "M0150";
	
	public static final String NotMatchToDBTypeRecordInSvfGetAuthRequest = "M0200";
	public static final String VerifyTypeNotSupportInSvfGetAuthRequest = "M0201";
}