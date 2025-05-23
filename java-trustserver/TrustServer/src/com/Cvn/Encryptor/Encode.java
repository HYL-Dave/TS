package com.Cvn.Encryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import com.toppanidgate.fidouaf.common.model.Log4j;

// Author: Calvin @ iDGate.com
public class Encode {

	public Encode() {

	}

	public static String byteToHex(byte[] stream) {
		StringBuffer hexStrBuffer = new StringBuffer();
		String tmpStr = "";
		for (int n = 0; n < stream.length; n++) {
			tmpStr = (java.lang.Integer.toHexString(stream[n] & 0XFF));

			if (tmpStr.length() == 1) {
				hexStrBuffer.append("0" + tmpStr);
				// hexStr = hexStr + "0" + tmpStr;
			} else {
				hexStrBuffer.append(tmpStr);
				// hexStr = hexStr + tmpStr;
			}
		}
		return hexStrBuffer.toString().toUpperCase(Locale.getDefault());
	}

	public static byte[] hexToByte(String hexString) {
		byte[] bytes = new byte[hexString.length() / 2]; // 2 Hex -> 8 bit -> 1 Byte
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
		// Each 2 HexStr -> 1 HexInt -> 1 Byte.
		return bytes;
	}

	public byte[] fileToBytes(File loadFile) {
		try (FileInputStream fis = new FileInputStream(loadFile);) {
			byte[] fbytes = new byte[(int) loadFile.length()];
			// int result = fis.read(fbytes);
			fis.close();
			return fbytes;
		} catch (IOException e) {
			Log4j.log.error(e.getMessage());
			return new byte[] {};
		}
	}
	
}
