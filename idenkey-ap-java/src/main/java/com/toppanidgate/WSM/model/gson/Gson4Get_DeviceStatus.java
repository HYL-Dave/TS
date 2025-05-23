package com.toppanidgate.WSM.model.gson;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4Get_DeviceStatus extends Gson4Common {
	private HashMap<String, String> failCount;
	private String memberStatus;
	private HashMap<String, String> type;
	private String deviceLabel;
	private String deviceModel;
	private String deviceOS;
	private String deviceOSVer;
	private String creatTime;
	private String lastModifyTime;
}
