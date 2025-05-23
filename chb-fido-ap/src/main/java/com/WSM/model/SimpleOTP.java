package com.WSM.model;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.Cvn.Encryptor.AESUtil;

public class SimpleOTP {
	private final static int OtpLength = 6;

	// check this otp is valid. msg and otp are related, the revertInterval
	// indicated how many time interval of 5min that you want to back trace for its otp in
	// that time interval
	public boolean checkOTP(final String msg, final String otp, final int revertInterval)
			throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, IllegalStateException {

		String[] generatedOTP = new String[revertInterval + 1];

		// generate otp for this msg in those time interval(s)
		for (int i = 0; i <= revertInterval; i++) {
			generatedOTP[i] = HMacSha1OTP((msg + msg.substring(msg.length() / 2)).getBytes("UTF-8"),
					String.valueOf((System.currentTimeMillis() / 300000L) - i).getBytes("UTF-8"));
			
			if (generatedOTP[i].length() >= OtpLength) {
				generatedOTP[i] = generatedOTP[i].substring(generatedOTP[i].length() - OtpLength);
			}
		}
		
		// check if this otp is match any number inside the generated OTP list. return true if yes
		if (Arrays.stream(generatedOTP).anyMatch(str -> str.equals(otp))) {
			return true;
		}

		return false;
	}

	// get a unique TOTP for this msg. It is valid for 5 min
	public String getOTP(final String msg)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, IllegalStateException {

		String otpstr = HMacSha1OTP((msg + msg.substring(msg.length() / 2)).getBytes("UTF-8"),
				String.valueOf(System.currentTimeMillis() / 300000L).getBytes("UTF-8"));

		if (otpstr.length() >= OtpLength) {
			return otpstr.substring(otpstr.length() - OtpLength, otpstr.length());
		}

		return otpstr;
	}

	private String HMacSha1OTP(final byte[] msg, final byte[] keyBytes)
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, IllegalStateException {
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(signingKey);
		return ToOTPString(mac.doFinal(msg));
	}

	private String ToOTPString(final byte[] hsha1) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String tmp = AESUtil.Bytes_To_HexString(hsha1);
		String ch = tmp.substring(tmp.length() - 1);

		int va = Integer.parseInt(ch, 16);
		String otpstr = tmp.substring(va * 2, va * 2 + 8);
		Long otp = Long.valueOf(Long.parseLong(otpstr, 16) & 0x7FFFFFFF);
		otp = Long.valueOf((long) ((double) otp.longValue() % Math.pow(10D, 10D)));

		return String.format("%010d", new Object[] { otp });
	}
}
