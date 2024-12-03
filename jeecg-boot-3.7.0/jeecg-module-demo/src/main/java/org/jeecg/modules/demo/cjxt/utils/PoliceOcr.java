package org.jeecg.modules.demo.cjxt.utils;

import cn.hutool.json.JSONObject;
import org.apache.commons.compress.utils.IOUtils;
import org.jeecg.modules.demo.cjxt.utils.baidu.Base64Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PoliceOcr {

    /**
     * 身份证号
     * @param imgUrl
     * @return
     */
    public static Map<String, String> gawOcrLicense(String imgUrl) {
        System.out.println("公安网识别");
        Map<String, String> map = new HashMap<>();
        String url = "http://123.249.17.105:1224/api/ocr";
        String base64Image = "";
        String img = "G:\\Desktop\\gr\\dg_z.jpg"; // 替换为你的图片路径
        img = "F:\\z.png"; // 替换为你的图片路径
        try {
//           String sfzImgUrl = "http://192.168.0.156:19000/cjxtdata/a.png" ;
            URL urlR = new URL(imgUrl);
            URLConnection connection = urlR.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream in = connection.getInputStream();
            byte[] imgData = IOUtils.toByteArray(in);
            base64Image = Base64Util.encode(imgData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonInputString = String.format("{\"base64\": \"%s\", \"options\": {\"data.format\": \"text\"}}", base64Image);
        try {
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;");
            connection.setRequestProperty("Accept", "application/json");
            // 设置允许输出
            connection.setDoOutput(true);
            // 设置允许输入
            connection.setDoInput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes();
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response and process it
                try (java.io.InputStream is = connection.getInputStream();
                     java.io.InputStreamReader isr = new java.io.InputStreamReader(is, StandardCharsets.UTF_8);
                     java.io.BufferedReader br = new java.io.BufferedReader(isr)) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonObject = new JSONObject(response.toString());
                    int code = jsonObject.getInt("code");
                    String data = jsonObject.getStr("data");
                    map.put("licensePlate",data);
                    double score = jsonObject.getDouble("score");
                    double time = jsonObject.getDouble("time");
                    double timestamp = jsonObject.getDouble("timestamp");
                }
            } else {
                System.out.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 身份证号
     * @param imgUrl
     * @return
     */
    public static Map<String, String> gawOcrSfzh(String imgUrl) {
        System.out.println("公安网识别");
        Map<String, String> map = new HashMap<>();
        String url = "http://123.249.17.105:1224/api/ocr";
        String base64Image = "";
        String img = "G:\\Desktop\\gr\\dg_z.jpg"; // 替换为你的图片路径
        img = "F:\\z.png"; // 替换为你的图片路径
        try {
//           String sfzImgUrl = "http://192.168.0.156:19000/cjxtdata/a.png" ;
           URL urlR = new URL(imgUrl);
           URLConnection connection = urlR.openConnection();
           connection.setDoInput(true);
           connection.connect();
           InputStream in = connection.getInputStream();
           byte[] imgData = IOUtils.toByteArray(in);
           base64Image = Base64Util.encode(imgData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonInputString = String.format("{\"base64\": \"%s\", \"options\": {\"data.format\": \"text\"}}", base64Image);
        try {
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;");
            connection.setRequestProperty("Accept", "application/json");
            // 设置允许输出
            connection.setDoOutput(true);
            // 设置允许输入
            connection.setDoInput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes();
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response and process it
                try (java.io.InputStream is = connection.getInputStream();
                     java.io.InputStreamReader isr = new java.io.InputStreamReader(is, StandardCharsets.UTF_8);
                     java.io.BufferedReader br = new java.io.BufferedReader(isr)) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonObject = new JSONObject(response.toString());
                    int code = jsonObject.getInt("code");
                    String data = jsonObject.getStr("data");
                    map = parseData(data);
                    double score = jsonObject.getDouble("score");
                    double time = jsonObject.getDouble("time");
                    double timestamp = jsonObject.getDouble("timestamp");
                }
            } else {
                System.out.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> parseData(String data) {
        Map<String, String> resultMap = new HashMap<>();
        Pattern pattern = Pattern.compile("姓名[:\\s]*([\\S]+)\\s*性别[:\\s]*([\\S]+)\\s*民族[:\\s]*([\\S]+)\\s*出生[:\\s]*([\\S\\s]+?)\\s*住址[:\\s]*([\\S\\s]+?)\\s*公民身份号码[:\\s]*([\\S]+)");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {

//            String name = matcher.group(1);
//            String gender = matcher.group(2);
//            String ethnicity = matcher.group(3);
//            String birthDate = matcher.group(4);
//            String address = matcher.group(5);
//            String idNumber = matcher.group(6);
//
//            System.out.println("姓名: " + name);
//            System.out.println("性别: " + gender);
//            System.out.println("民族: " + ethnicity);
//            System.out.println("出生: " + birthDate);
//            System.out.println("住址: " + address);
//            System.out.println("公民身份证号码: " + idNumber);

            resultMap.put("xm", matcher.group(1));
            resultMap.put("sex", matcher.group(2));
            resultMap.put("nation", matcher.group(3));
            resultMap.put("birthday", matcher.group(4).trim());
            resultMap.put("address", matcher.group(5).trim());
            resultMap.put("sfzh", matcher.group(6));
        } else {
            System.out.println("无法解析数据");
        }
        return resultMap;
    }

    public static String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] imageData = new byte[(int) file.length()];
        fis.read(imageData);
        fis.close();
        return Base64.getEncoder().encodeToString(imageData);
    }
}
