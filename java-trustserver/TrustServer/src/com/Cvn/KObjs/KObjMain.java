package com.Cvn.KObjs;

import com.Common.Model.Log4j;

// Key object handler: split key into 3 pieces and store them onto RAM
public class KObjMain {
	private static KObjA objA = null;
	private static KObjB objB = null;
	private static KObjC objC = null;
	private static KObjD objD = null;

	protected void finalize() {
		objA = null;
		objB = null;
		objC = null;
		objD = null;
	}

	public KObjMain(String str) {
		if (objA == null && objB == null && objC == null && objD == null) {
			int offset = str.length() / 4;

			objA = new KObjA();
			objB = new KObjB();
			objC = new KObjC();
			objD = new KObjD();

			if ("-1".equals(objA.setObjA(str.substring(0, offset)))) {
				Log4j.log.error("Unknown error: KObjA cannot be set");
			} else if ("-1".equals(objB.setObjB(str.substring(offset, offset * 2)))) {
				Log4j.log.error("Unknown error: KObjB cannot be set");
			} else if ("-1".equals(objC.setObjC(str.substring(offset * 2, offset * 3)))) {
				Log4j.log.error("Unknown error: KObjC cannot be set");
			} else if ("-1".equals(objD.setObjD(str.substring(offset * 3)))) {
				Log4j.log.error("Unknown error: KObjD cannot be set");
			}
		}
	}

	public final String get_ObjA() {
		if (objA == null)
			return "NULL";

		return objA.getObjA();
	}

	public final String get_ObjB() {
		if (objB == null)
			return "NULL";

		return objB.getObjB();
	}

	public final String get_ObjC() {
		if (objC == null)
			return "NULL";

		return objC.getObjC();
	}

	public final String get_ObjD() {
		if (objD == null)
			return "NULL";

		return objD.getObjD();
	}
}
