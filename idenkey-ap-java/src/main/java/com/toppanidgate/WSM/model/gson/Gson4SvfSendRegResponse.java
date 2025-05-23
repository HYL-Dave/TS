package com.toppanidgate.WSM.model.gson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4SvfSendRegResponse extends Gson4Send_AuthResponse {
	private String encRegResult;
	private String idgateID;
}
