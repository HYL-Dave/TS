package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4SvfGet_AuthRequest extends Gson4SvfGet_RegRequest{
	private String encAuthReq;
	private String encTxnData;
	private String txnID;
	private String encTxnID;
}
