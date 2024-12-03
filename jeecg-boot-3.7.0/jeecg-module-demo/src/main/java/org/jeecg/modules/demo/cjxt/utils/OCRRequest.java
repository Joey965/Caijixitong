package org.jeecg.modules.demo.cjxt.utils;

import cn.hutool.json.JSONObject;
import org.apache.commons.compress.utils.IOUtils;
import org.jeecg.modules.demo.cjxt.utils.baidu.Base64Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRRequest {

    public static String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] imageData = new byte[(int) file.length()];
        fis.read(imageData);
        fis.close();
        return Base64.getEncoder().encodeToString(imageData);
    }
 
   public static void main(String[] args) {
       String url = "http://123.249.17.105:1224/api/ocr";
       String base64Image = "";
       String img = "G:\\Desktop\\gr\\dg_z.jpg"; // 替换为你的图片路径
       img = "F:\\z.png"; // 替换为你的图片路径
       try {
//           String sfzImgUrl = "http://192.168.0.156:19000/cjxtdata/a.png" ;
//           URL urlR = new URL(sfzImgUrl);
//           URLConnection connection = urlR.openConnection();
//           connection.setDoInput(true);
//           connection.connect();
//           InputStream in = connection.getInputStream();
//           byte[] imgData = IOUtils.toByteArray(in);
//           base64Image = Base64Util.encode(imgData);

           base64Image = encodeImageToBase64(img);
//           System.out.println("Base64编码后的字符串： " + base64Image);
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

                   System.out.println(jsonObject);
                   System.out.println(data);
                   parseData(data);
//                 System.out.println(asciiToStr(data));
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
   }

    public static void parseData(String data) {
//        Pattern pattern = Pattern.compile("姓名[:\\s]*([\\S]+)\\s*性别[:\\s]*([\\S]+)\\s*民族[:\\s]*([\\S]+)\\s*出生[:\\s]*([\\S]+)\\s*住址[:\\s]*([\\S\\s]+)\\s*公民身份证号码[:\\s]*([\\S]+)");
        Pattern pattern = Pattern.compile("姓名[:\\s]*([\\S]+)\\s*性别[:\\s]*([\\S]+)\\s*民族[:\\s]*([\\S]+)\\s*出生[:\\s]*([\\S\\s]+?)\\s*住址[:\\s]*([\\S\\s]+?)\\s*公民身份号码[:\\s]*([\\S]+)");
//        Pattern pattern = Pattern.compile("姓名[:\\s]*([\\S]+)\\s*性别[:\\s]*([\\S]+)\\s*民族[:\\s]*([\\S]+)\\s*出生[:\\s]*([\\S\\s]+?)\\s*住址[:\\s]*([\\S\\s]+?)\\s*公民身份号码[:\\s]*([\\S]+)");
//        Pattern pattern = Pattern.compile("姓名[:\\s]*([\\S]+?)\\s*性别[:\\s]*([\\S]+?)\\s*民族[:\\s]*([\\S]+?)\\s*出生[:\\s]*([\\S\\s]+?)\\s*住址[:\\s]*([\\S\\s]+?)\\s*公民身份号码[:\\s]*([\\S]+)");
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            String name = matcher.group(1);
            String gender = matcher.group(2);
            String ethnicity = matcher.group(3);
            String birthDate = matcher.group(4);
            String address = matcher.group(5);
            String idNumber = matcher.group(6);

            System.out.println("姓名: " + name);
            System.out.println("性别: " + gender);
            System.out.println("民族: " + ethnicity);
            System.out.println("出生: " + birthDate);
            System.out.println("住址: " + address);
            System.out.println("公民身份证号码: " + idNumber);
        } else {
            System.out.println("无法解析数据");
        }
    }

    /**
     * 将Ascii码转为字符串
     * @param asciiStr ASCII码字符串
     * @return 对应的字符串
     */
//    public static String asciiToStr(String asciiStr) {
//        StringBuilder result = new StringBuilder();
//        for (int i = 0; i < asciiStr.length(); i++) {
//            char c = asciiStr.charAt(i);
//            if (c == '\\') {
//                if (i + 5 < asciiStr.length() && asciiStr.substring(i, i + 6).equals("\\u")) {
//                    String hex = asciiStr.substring(i + 2, i + 6);
//                    result.append((char) Integer.parseInt(hex, 16));
//                    i += 5;
//                } else {
//                    result.append(c);
//                }
//            } else {
//                result.append(c);
//            }
//        }
//        return result.toString();
//    }
}