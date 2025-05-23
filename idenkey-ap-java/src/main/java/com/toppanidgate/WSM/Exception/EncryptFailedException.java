package com.toppanidgate.WSM.Exception;

public class EncryptFailedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EncryptFailedException(Exception e) {
		super(e);
	}
}
