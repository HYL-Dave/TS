package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4SvfGet_RegRequest extends Gson4Common {
	private String regReq;
	private String serverPubKey;
	private String serverData;
//	private String uafRequest;
}
