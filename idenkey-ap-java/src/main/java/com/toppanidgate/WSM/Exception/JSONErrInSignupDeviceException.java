package com.toppanidgate.WSM.Exception;

import org.json.JSONException;

import com.google.gson.JsonSyntaxException;

public class JSONErrInSignupDeviceException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JSONErrInSignupDeviceException(JSONException e) {
		super(e);
	}

	public JSONErrInSignupDeviceException(JsonSyntaxException e) {
		super(e);
	}

}
