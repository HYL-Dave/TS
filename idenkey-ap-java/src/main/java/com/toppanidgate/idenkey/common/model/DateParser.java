package com.toppanidgate.idenkey.common.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser {
	public String getDateName(String lang, String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date d = sdf.parse(date);
		sdf.applyPattern("u");		
		String result = null;

		switch (sdf.format(d)) {
		case "1":
			if (lang.indexOf("zh") > -1) {
				result = "(星期一)";
			} else {
				result = "(Monday)";
			}
			break;

		case "2":
			if (lang.indexOf("zh") > -1) {
				result = "(星期二)";
			} else {
				result = "(Tuesday)";
			}
			break;

		case "3":
			if (lang.indexOf("zh") > -1) {
				result = "(星期三)";
			} else {
				result = "(Wednesday)";
			}
			break;

		case "4":
			if (lang.indexOf("zh") > -1) {
				result = "(星期四)";
			} else {
				result = "(Thursday)";
			}
			break;

		case "5":
			if (lang.indexOf("zh") > -1) {
				result = "(星期五)";
			} else {
				result = "(Friday)";
			}
			break;

		case "6":
			if (lang.indexOf("zh") > -1) {
				result = "(星期六)";
			} else {
				result = "(Saturday)";
			}
			break;

		case "7":
			if (lang.indexOf("zh") > -1) {
				result = "(星期日)";
			} else {
				result = "(Sunday)";
			}
			break;
		}

		return result;
	}

	public String getBritishDateFormat(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
		Date d = sdf.parse(date);
		sdf.applyPattern("dd-MMM-yyyy");

		return sdf.format(d);
	}

	public String getChineseDateFormat(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date d = sdf.parse(date);
		sdf.applyPattern("yyyy年MM月dd");

		return sdf.format(d);
	}
}
