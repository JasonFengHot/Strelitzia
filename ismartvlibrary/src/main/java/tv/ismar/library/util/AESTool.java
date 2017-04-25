package tv.ismar.library.util;

import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESTool {

    public static String decrypt(String url, String device_token) {
        return AESTool.decrypt(device_token.substring(0, 16), Base64.decode(url, Base64.URL_SAFE));
    }

    /**
     * 解密
     */
    public static String decrypt(String keyWord, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key = new SecretKeySpec(keyWord.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(getPosByte(content, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plain = cipher.doFinal(getPosByte(content, 16, content.length));
            // 去除填充字符
            byte[] plainTemp = getPosByte(plain, plain.length - 1, plain.length);
            byte[] plainTemp2 = getPosByte(plain, 0, plain.length - (int) plainTemp[0]);
            return new String(plainTemp2);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] getPosByte(byte[] content, int start, int end) {
        byte[] newContent = new byte[end - start];

        int k = 0;
        for (int i = start; i < end; i++) {
            newContent[k] = content[i];
            k++;
        }
        return newContent;
    }
}
