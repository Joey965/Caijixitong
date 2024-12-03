package org.jeecg.modules.demo.cjxt.utils;

import java.util.Collections;

public class Desensitization {

    // 身份证号 1
    public static String desensitize(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return idCard;
        }
        return idCard.substring(0, 6)
                .concat(String.join("", Collections.nCopies(8, "*")))
                .concat(idCard.substring(14));
    }

    // 手机号 2
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static void main(String[] args) {
        String idCard = "610121200102025899";
        String desensitizedIdCard = desensitize(idCard);
        System.out.println(desensitizedIdCard); // 输出 123456******5678

        String phone = "18192498651";
        String desensitizedPhone = maskPhone(phone);
        System.out.println(desensitizedPhone);
    }

}
