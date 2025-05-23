package com.toppanidgate.idenkey.Verify_Request.model;

public class TxnVO {

	private String txnID;
	private String authStatus;
	private String title;
	private String createTime;
	private String encTxnData;
	private String encAuthReq;

	public String getTxnID() {
		return txnID;
	}

	public void setTxnID(String txnID) {
		this.txnID = txnID;
	}

	public String getAuthStatus() {
		return authStatus;
	}

	public void setAuthStatus(String txnStatus) {
		authStatus = txnStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return createTime;
	}

	public void setSubTitle(String subTitle) {
		createTime = subTitle;
	}

	public String getEncTxnData() {
		return encTxnData;
	}

	public void setEncTxnData(String data) {
		encTxnData = data;
	}

	public String getEncAuthReq() {
		return encAuthReq;
	}

	public void setEncAuthReq(String encAuthReq) {
		this.encAuthReq = encAuthReq;
	}
	
	
}