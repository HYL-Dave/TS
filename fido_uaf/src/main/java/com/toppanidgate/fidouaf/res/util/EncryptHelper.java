package com.toppanidgate.fidouaf.res.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptHelper {

    /**
     * encrpyt to MD5 for passing the phoneNo to AP SERVER
     * @param msg
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5(String msg) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] messageDigest = md.digest(msg.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return number.toString(16);
    }
	

}
