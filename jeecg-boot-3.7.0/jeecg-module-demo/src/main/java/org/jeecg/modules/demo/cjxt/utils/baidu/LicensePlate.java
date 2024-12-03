package org.jeecg.modules.demo.cjxt.utils.baidu;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.compress.utils.IOUtils;
import org.jeecg.modules.demo.cjxt.utils.log.Dg;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class LicensePlate {

    public static String license() {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate";
        try {
            // 本地文件路径
            String filePath = "F:\\temp\\tp\\rzbd\\Front.bmp";
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "id_card_side=" + "front" + "&image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = AuthService.getAuth();

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> licenseUrl(String licenseImgUrl) {
        Map<String, String> map = new HashMap<>();
        // 身份证识别url
        String idcardIdentificate = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate";
        try {
            URL url = new URL(licenseImgUrl);
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream in = connection.getInputStream();
            byte[] imgData = IOUtils.toByteArray(in);
            String imgStr = Base64Util.encode(imgData);
            long startTime = System.currentTimeMillis();
            long sqlCostTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            // 识别身份证正面id_card_side=front;识别身份证背面id_card_side=back;
            String params = "multi_scale=false&multi_detect=false&" + URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(imgStr, "UTF-8");
            /**
             * 线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
             */
            String accessToken = AuthService.getAuth();//#####调用鉴权接口获取的token#####
            String value = HttpUtil.post(idcardIdentificate, accessToken, params);
            JSONObject jsonObject = JSONObject.parseObject(value);
            JSONObject words_result = jsonObject.getJSONObject("words_result");
            if (words_result != null) {
                if (words_result != null) {
                    String number = words_result.getString("number");
                    map.put("licensePlate", number);
                    System.out.println("车牌号: " + number);
                }
                endTime = System.currentTimeMillis();
                sqlCostTime = endTime - startTime;
                Dg.writeContent("baidu_suc", "开始时间：" + startTime + ",结束时间：" + endTime + ",共消耗：" + sqlCostTime + "开始百度ocr识别，百度成功了| "+map.toString());
            }
        } catch (Exception e) {
            System.err.println("JSON 解析错误: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }


    public static Map<String, String> licensePath(String licenImgPath) {
        Map<String, String> map = new HashMap<>();
        // 身份证识别url
        String idcardIdentificate = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate";
        // 本地图片路径
        String filePath = licenImgPath;//#####本地文件路径#####
        try {
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            long startTime = System.currentTimeMillis();
            long sqlCostTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            // 车牌识别
            String params = "multi_scale=false&multi_detect=false&" + URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(imgStr, "UTF-8");
            /**
             * 线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
             */
            String accessToken = AuthService.getAuth();//#####调用鉴权接口获取的token#####
            String value = HttpUtil.post(idcardIdentificate, accessToken, params);
            JSONObject jsonObject = JSONObject.parseObject(value);
            JSONObject words_result = jsonObject.getJSONObject("words_result");
            if (words_result != null) {
                if (words_result != null) {
                    String number = words_result.getString("number");
                    map.put("licensePlate", number);
                    System.out.println("车牌号: " + number);
                }
                endTime = System.currentTimeMillis();
                sqlCostTime = endTime - startTime;
                Dg.writeContent("baidu_suc", "开始时间：" + startTime + ",结束时间：" + endTime + ",共消耗：" + sqlCostTime + "开始百度ocr识别，百度成功了| "+map.toString());
            }
        } catch (Exception e) {
            System.err.println("JSON 解析错误: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    public static void main(String[] args) {
        LicensePlate.licensePath("F:\\temp\\tp\\rzbd\\cp.jpg");
    }
}
