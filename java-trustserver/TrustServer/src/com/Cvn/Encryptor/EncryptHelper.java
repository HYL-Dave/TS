package com.Cvn.Encryptor;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(msg.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return number.toString(16);
    }


    @SuppressWarnings("unused")
	public static String Decrypt(String sSrc) throws Exception {
        String sKey="P@ssW0rdForS0@P=";
        String sIv= "P@SSwOrdfoRiS0!P";
        try {

            if (sKey == null) {
//                System.out.print("Key is null");
                return null;
            }
            // KEY OF AES MUST BE 16 LENGTH
            if (sKey.length() != 16) {
//                System.out.print("length of Key not enough");
                return null;
            }
            byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(sIv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decodeBase64(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
//                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
//            System.out.println(ex.toString());
            return null;
        }
    }

}
