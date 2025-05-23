package com.toppanidgate.idenkey.common.model;

/**
 *
 * 會員狀態 <br>
 * 0 = 正常 <br>
 * 1 = 暫禁 <br>
 * 2 = 驗證錯誤過多鎖定 <br>
 * 3 = 註冊中 <br>
 * 9 = 已註銷 <br>
 */
public class MemberStatus {
	static final public String Normal = "0";
	static final public String Locked = "1";
	static final public String LockedForTooManyAuthFails = "2";
	static final public String Register = "3";

	static final public String Deleted = "9";
}