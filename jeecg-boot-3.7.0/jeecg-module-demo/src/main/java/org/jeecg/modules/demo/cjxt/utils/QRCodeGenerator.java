package org.jeecg.modules.demo.cjxt.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static String generateQRCodeImage(String text, int width, int height, String tempDir) throws WriterException, IOException {
        long startTime = System.currentTimeMillis(); // 开始计时

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path tempPath = Files.createTempFile(Paths.get(tempDir), "qrcode_", ".png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", tempPath);

        long endTime = System.currentTimeMillis(); // 结束计时
        long duration = endTime - startTime; // 计算生成二维码所需时间

        // 在控制台输出生成时间
        System.out.println("生成二维码所需时间: " + duration + " 毫秒");

        return tempPath.toAbsolutePath().toString();
    }

//    public static void main(String[] args) {
//        try {
//            String text = "https://www.example.com";
//            String tempDir = "D:/"; // 替换为你的临时文件夹路径
//            int width = 300;
//            int height = 300;
//
//            String imagePath = generateQRCodeImage(text, width, height, tempDir);
//            System.out.println("二维码图片路径: " + imagePath);
//        } catch (WriterException | IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) {
        StringBuilder baseV = new StringBuilder();
        for (int i = 1; i <= 117; i++) {
            String base = "base64" + i;
            baseV.append(base).append("+");
        }
        // 移除最后一个多余的加号
        if (baseV.length() > 0) {
            baseV.setLength(baseV.length() - 1);
        }
        System.out.println("Concatenated base64 string: " + baseV.toString());
    }
}