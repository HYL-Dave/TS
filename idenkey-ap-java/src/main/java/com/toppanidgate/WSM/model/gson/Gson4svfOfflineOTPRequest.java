package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4svfOfflineOTPRequest extends Gson4Common {
	private String qrCode;
	private String txnID;
}
