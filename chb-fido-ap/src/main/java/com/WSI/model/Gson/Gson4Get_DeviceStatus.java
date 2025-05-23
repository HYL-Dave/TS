package com.WSI.model.Gson;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4Get_DeviceStatus extends Gson4Common {
	private HashMap<String, String> failCount;
	private HashMap<String, String> type;
	private String memberStatus;
	private String deviceLabel;
	private String deviceModel;
	private String deviceOS;
	private String deviceOSVer;
	private String verifyType;
	private String createTime;
	private String lastModifyTime;
}