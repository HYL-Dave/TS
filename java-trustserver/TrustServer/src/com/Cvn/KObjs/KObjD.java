package com.Cvn.KObjs;

public class KObjD {
	private String objD = null;

	protected void finalize() {
		objD = "";
		objD = null;
	}

	public final String setObjD(final String data) {
		if (objD != null)
			return "-1";

		objD = data;
		return "0";
	}

	public final String getObjD() {
		return objD;
	}
}
