package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4SvfGet_TxnData extends Gson4Common{
	private String serverPubKey;
	private String encAuthReq;
	private String encTxnData;
	private String txnID;
	private String authStatus;
	private String title;	// nvarchar(100)
}
