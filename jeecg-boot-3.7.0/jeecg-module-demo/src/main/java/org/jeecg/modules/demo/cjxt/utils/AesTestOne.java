package org.jeecg.modules.demo.cjxt.utils;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.soap.SAAJResult;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AesTestOne {
    private static final String ALG_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private String aesKey = "12e476sxby1a4g20";  // 指定好的秘钥，非Base64和16进制
    private String aesIv = "2e119esxby26bc64";   // 偏移量
    private SecretKeySpec skeySpec;
    private IvParameterSpec iv;

    /**
     * 解密方法
     * @param cipherStr Base64编码的加密字符串
     * @return 解密后的字符串(UTF8编码)
     * @throws Exception 异常
     */
    public String decrypt(String cipherStr) throws Exception {
        // step 1 获得一个密码器
        Cipher cipher = Cipher.getInstance(ALG_AES_CBC_PKCS5);
        // step 2 初始化密码器，指定是加密还是解密(Cipher.DECRYPT_MODE 解密; Cipher.ENCRYPT_MODE 加密)
        skeySpec = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
        iv = new IvParameterSpec(aesIv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        // 解密前先确保 Base64 编码正确
        byte[] encrypted1 = Base64.getDecoder().decode(cipherStr);  // 使用标准的 Base64 解码
        // 解密后的报文数组
        byte[] original = cipher.doFinal(encrypted1);
        // 输出utf8编码的字符串，输出字符串需要指定编码格式
        return new String(original, UTF8);
    }

    /**
     * 传参解密
     * @param cipherStr
     * @param key
     * @param Iv
     * @return
     * @throws Exception
     */
    public String decryptZdy(String cipherStr, String key, String Iv) throws Exception {
        // step 1 获得一个密码器
        Cipher cipher = Cipher.getInstance(ALG_AES_CBC_PKCS5);
        // step 2 初始化密码器，指定是加密还是解密(Cipher.DECRYPT_MODE 解密; Cipher.ENCRYPT_MODE 加密)
        skeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        iv = new IvParameterSpec(Iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        // 解密前先确保 Base64 编码正确
        byte[] encrypted1 = Base64.getDecoder().decode(cipherStr);  // 使用标准的 Base64 解码
        // 解密后的报文数组
        byte[] original = cipher.doFinal(encrypted1);
        // 输出utf8编码的字符串，输出字符串需要指定编码格式
        return new String(original, UTF8);
    }

    public String decryptZdyCf(String cipherStr, String key, String Iv) throws Exception {
        // Step 1: 获取密码器
        Cipher cipher = Cipher.getInstance(ALG_AES_CBC_PKCS5);

        // Step 2: 初始化密码器，指定加密模式
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(Iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        // Step 3: 拆解密文字符串（假设加密的字符串用 "_sxby" 作为分隔符）
        String[] JmValue = cipherStr.split("_sxby");
        StringBuilder jm = new StringBuilder();

        String encry = JmValue[0];
        // 解密前先确保 Base64 编码正确
        byte[] encrypted1 = Base64.getDecoder().decode(encry);  // 使用标准的 Base64 解码
        // 解密后的报文数组
        byte[] original = cipher.doFinal(encrypted1);
        jm.append(new String(original, UTF8)); // 拼接解密后的内容

        // 返回拼接后的解密字符串
        return jm.toString();
    }

    /**
     * 加密
     * @param plainText 明文
     * @return Base64编码的密文
     * @throws Exception  加密异常
     */
    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALG_AES_CBC_PKCS5);
        skeySpec = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
        iv = new IvParameterSpec(aesIv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        // 这里的编码格式需要与解密编码一致
        byte[] encryptText = cipher.doFinal(plainText.getBytes(UTF8));
        return Base64.getEncoder().encodeToString(encryptText);  // 使用标准的 Base64 编码
    }

    /**
     * 传参加密
     * @param plainText
     * @param key
     * @param Iv
     * @return
     * @throws Exception
     */
    public String encryptZdy(String plainText, String key, String Iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALG_AES_CBC_PKCS5);
        skeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        iv = new IvParameterSpec(Iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        // 这里的编码格式需要与解密编码一致
        byte[] encryptText = cipher.doFinal(plainText.getBytes(UTF8));
        return Base64.getEncoder().encodeToString(encryptText);  // 使用标准的 Base64 编码
    }

    /**
     * 传参加密
     * @param plainText
     * @param key
     * @param Iv
     * @return
     * @throws Exception
     */
    public String encryptZdyCf(String plainText, String key, String Iv) throws Exception {
        // 创建Cipher实例
        Cipher cipher = Cipher.getInstance(ALG_AES_CBC_PKCS5);

        // 创建密钥和初始化向量
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(Iv.getBytes());

        // 初始化加密器
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        // 1. 整体加密
        byte[] encryptText = cipher.doFinal(plainText.getBytes(UTF8));
        String encryptedText = Base64.getEncoder().encodeToString(encryptText);
        // 拼接 _sxby
        String result = encryptedText + "_sxby";
        // 插接赋值
        StringBuilder sb = new StringBuilder();
        for (char c : plainText.toCharArray()) {
            byte[] charEncrypted = cipher.doFinal(String.valueOf(c).getBytes(UTF8));
            sb.append(Base64.getEncoder().encodeToString(charEncrypted));
        }
        return result + sb.toString();
    }

    public static void main(String[] args) {
        AesTestOne testOne = new AesTestOne();
        String plainText = "小郭网吧";
        String cipherStr;
        try {
            System.out.println("被加解密的报文:[ " + plainText + " ]");
            cipherStr = testOne.encrypt(plainText);

            String aa = "jd1QjTYokSR8oRuoFSklhQ==";
            System.out.println("AES 加密后的Base64报文:[ " + cipherStr + " ]");
            System.out.println("对加密后的报文解密后的明文为:[ " + testOne.decrypt(cipherStr) + " ]");
            System.out.println("对加密后的报文解密后的明文为:[ " + testOne.decrypt(aa) + " ]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}