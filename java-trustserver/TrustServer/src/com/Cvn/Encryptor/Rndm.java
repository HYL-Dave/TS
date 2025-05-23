package com.Cvn.Encryptor;
import java.security.SecureRandom;
import java.util.Random;

// Author: Calvin @ iDGate.com
public class Rndm {

	public Rndm() {

	}

	public static String generateRdmStr(int length) {
		// int myUUID = UUID.randomUUID().hashCode();

		String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder myGenerator = new StringBuilder();
		SecureRandom rnd = new SecureRandom();
		while (myGenerator.length() < length) {
			int index = (int) (rnd.nextFloat() * CHARS.length());
			myGenerator.append(CHARS.charAt(index));
		}

		return myGenerator.toString();
	}

	public static String generateRdmHexStr(int length) {

		// int myUUID = UUID.randomUUID().hashCode();
		String CHARS = "ABCDEF1234567890";
		StringBuilder myGenerator = new StringBuilder();
		SecureRandom rnd = new SecureRandom();
		while (myGenerator.length() < length) {
			int index = (int) (rnd.nextFloat() * CHARS.length());
			myGenerator.append(CHARS.charAt(index));
		}

		return myGenerator.toString();
	}
 
}
