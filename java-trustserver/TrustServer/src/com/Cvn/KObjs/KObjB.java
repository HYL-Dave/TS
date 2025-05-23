package com.Cvn.KObjs;

public class KObjB {
	private String objB = null;

	protected void finalize() {
		objB = "";
		objB = null;
	}

	public final String setObjB(final String data) {
		if (objB != null)
			return "-1";

		objB = data;
		return "0";
	}

	public final String getObjB() {
		return objB;
	}
}
