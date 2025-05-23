package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4Get_RSAPublicKey extends Gson4Common {
	
	private String app_Key;
	private String bank_Key;
	private String serverPubKey;
	private String serverECCPubKey;
	private String decryptedData;
	private String encryptedData;
	private String serverTime;
}
