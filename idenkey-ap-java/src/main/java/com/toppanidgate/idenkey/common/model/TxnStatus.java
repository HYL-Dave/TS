package com.toppanidgate.idenkey.common.model;

/**
 *
 * 交易狀態 <br>
 * 00 = 驗證成功 <br>
 * 01 = 等待驗證 <br>
 * 02 = 驗證失敗 <br>
 * 03 = 驗證取消 <br>
 * 04 = 逾時 <br>
 * 05 = 登入驗證成功 <br>
 * 06 = 登入驗證失敗 <br>
 * 07 = 裝置驗證成功 <br>
 * 08 = 裝置驗證失敗
 */
public class TxnStatus {
	static final public String Success = "00";
	static final public String WaitForVerify = "01";
	static final public String Fail = "02";
	static final public String Cancel = "03";
	static final public String Timeout = "04";
	static final public String LoginVerifySuccess = "05";
	static final public String LoginVerifyFailed = "06";
	static final public String VerifyDeviceSuccess = "07";
	static final public String VerifyDeviceFailed = "08";
}