package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4Send_AuthResponse extends Gson4Common {
	private String idgateid;
	private String authResult;
	private String txnID;
	private String failCount;
}
