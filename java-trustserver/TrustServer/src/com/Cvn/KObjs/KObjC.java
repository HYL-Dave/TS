package com.Cvn.KObjs;

public class KObjC {
	private String objC = null;

	protected void finalize() {
		objC = "";
		objC = null;
	}

	public final String setObjC(final String data) {
		if (objC != null)
			return "-1";

		objC = data;
		return "0";
	}

	public final String getObjC() {
		return objC;
	}
}
