package com.Cvn.KObjs;

public class KObjA {
	private String objA = null;

	protected void finalize() {
		objA = "";
		objA = null;
	}

	public final String setObjA(final String data) {
		if (objA != null)
			return "-1";

		objA = data;
		return "0";
	}

	public final String getObjA() {
		return objA;
	}
}
