package org.jeecg.modules.demo.cjxt.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RsaUtil {

    /**
     * 类型
     */
    public static final String ENCRYPT_TYPE = "RSA";

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 公钥加密
     *
     * @param content:
     * @param publicKey:
     * @author: 405
     * @date: 2021/6/28
     * @return: java.lang.String
     */
    public static String encryptRes(String content, PublicKey publicKey) {
        try {
            RSA rsa = new RSA(null, publicKey);
            return rsa.encryptBase64(content, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("公钥加密异常 msg:{}",e.getMessage());
        }
        return null;
    }

    /**
     * 公钥加密
     *
     * @param content:
     * @param publicKey:
     * @author: 405
     * @date: 2021/6/28
     * @return: java.lang.String
     */
    public static String encryptRes(String content, String publicKey) {
        try {
            RSA rsa = new RSA(null, publicKey);
            return rsa.encryptBase64(content, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("公钥加密异常 msg:{}",e.getMessage());
        }
        return null;
    }


    /**
     * 私钥解密
     *
     * @param content:
     * @param privateKey:
     * @author: 405
     * @date: 2021/6/28
     * @return: java.lang.String
     */
    public static String decryptRes(String content, PrivateKey privateKey) {
        try {
            RSA rsa = new RSA(privateKey, null);
            return rsa.decryptStr(content, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("私钥解密异常 msg:{}",e.getMessage());
        }
        return null;
    }

    /**
     * 私钥解密
     *
     * @param content:
     * @param privateKey:
     * @author: 405
     * @date: 2021/6/28
     * @return: java.lang.String
     */
    public static String decryptRes(String content, String privateKey) {
        try {
            RSA rsa = new RSA(privateKey, null);
            return rsa.decryptStr(content, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("私钥解密异常 msg:{}",e.getMessage());
        }
        return null;
    }

    /**
     * 获取公私钥-请获取一次后保存公私钥使用
     * @return
     */
    public static Map<String,String> generateKeyPair() {
        try {
            KeyPair pair = SecureUtil.generateKeyPair(ENCRYPT_TYPE);
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();
            // 获取 公钥和私钥 的 编码格式（通过该 编码格式 可以反过来 生成公钥和私钥对象）
            byte[] pubEncBytes = publicKey.getEncoded();
            byte[] priEncBytes = privateKey.getEncoded();

            // 把 公钥和私钥 的 编码格式 转换为 Base64文本 方便保存
            String pubEncBase64 = new BASE64Encoder().encode(pubEncBytes);
            String priEncBase64 = new BASE64Encoder().encode(priEncBytes);

            Map<String, String> map = new HashMap<String, String>(2);
            map.put(PUBLIC_KEY,pubEncBase64);
            map.put(PRIVATE_KEY,priEncBase64);

            return map;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        // System.out.println(JSONObject.toJSONString(generateKeyPair()));
//        String ftpUrl = "ftp://wasu:234@125.2321321he12321321";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDj9qP6motJuk2wvj5x15f0GuaKux7g2IgSqr5F\r\n/RY98789bErqfB9ndJFe8dVsYUCeaycjf+R3UUn13dY5iPaP8gIRjcCy1bRQYAkLH7qhSJcBAkTI\r\navaOqwe89fVAUvmG4vBx3MrGSWxN9JojXdd6dIN3oZxUh0ICsbLsQjJV8wIDAQAB";
        String content = "19992852168";
        String mw = encryptRes(content,publicKey);
        System.out.println(content + "加密后：" + mw);

        String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOP2o/qai0m6TbC+PnHXl/Qa5oq7\r\nHuDYiBKqvkX9Fj3zvz1sSup8H2d0kV7x1WxhQJ5rJyN/5HdRSfXd1jmI9o/yAhGNwLLVtFBgCQsf\r\nuqFIlwECRMhq9o6rB7z19UBS+Ybi8HHcysZJbE30miNd13p0g3ehnFSHQgKxsuxCMlXzAgMBAAEC\r\ngYBkgTMeo9BROyBRki65xlGqP8nw1Dry6sRjZn/pH4IU4kgobnxBEWRPGmeVMo3j8KSqs5dX4WT6\r\n7g3rplhymV4Kl1Sm2EeMN2w43R1bDGZX2XuAEBQlF36Lqq2l6rTmwieFC21zeNNVtqifzcYj9NU5\r\nWNjiGyS/bJExhFkOEUVo4QJBAP8p3kJrqcdEDRObXeA1xqcsCJFvfS/QZLZS9QAOtmXjXIjY2kyv\r\ngcDwZIDj2NXDe0KQ7Q5yYMbxnQwQj3IGhukCQQDktfI36FrFrTMAiC8wzJB96bCXEitSFPcbRVXJ\r\nrpoGMXqdZ+QOtVZ7xp13IeypD+tvzkKYobvi5d0wlsUgqOR7AkBTRcqvSouh6GeMGoxMe8BVFl3F\r\neOkWDaCQkAo89pA4ODcewgacODrUSJ/EIJfHS4CSfNGggVtsEbX7Ffx6bf7hAkBYLFUPfMLSBu3s\r\nvJQE6jiicl+kPlr5MB/8IYTrVicHUDgHSzA/A2YuHepDLY8BtqN73TcLhP7cgOX/f4DMEJDVAkEA\r\n0XA3Fh4DKen2N/iKiKbtI7f9gXGlb8/jjIqw4s4NNRodvOHxsE55nc7BQnjJ9sJQOn1NdlIL6sq6\r\n9qnisQX5Tg==";
        String mw2 = decryptRes(mw,privateKey);
        System.out.println("解密后：" + mw2);
    }
}
