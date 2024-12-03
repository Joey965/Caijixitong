package org.jeecg.modules.demo.cjxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.util.MinioUtil;
import org.jeecg.common.util.oss.OssBootUtil;
import org.jeecg.modules.demo.cjxt.entity.CjxtPitfallReport;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddress;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtPitfallReportService;
import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressService;
import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Api(tags="二维码上报")
@RestController
@RequestMapping("/cjxt/generate")
@Slf4j
public class GenerateQRCodeController extends JeecgController<CjxtPitfallReport, ICjxtPitfallReportService>{

    //minio图片服务器
    @Value(value="${jeecg.minio.minio_url}")
    private String minioUrl;
    @Value(value="${jeecg.minio.bucketName}")
    private String bucketName;
    @Value(value="${jeecg.uploadType}")
    private String uploadType;
    @Autowired
    private ICjxtStandardAddressService standardAddressService;
    @Autowired
    private ICjxtXtcsService cjxtXtcsService;
    @Autowired
    private ISysUserService sysUserService;



    /**
     * 从业人员上报
     * @param mbCode
     * @param addressId
     * @param baseUrl
     * @param userId
     * @return
     */
    @GetMapping("/qrcode")
    @ApiOperation(value="二维码上报-从业人员生成二维码", notes="二维码上报-从业人员生成二维码")
    public Result<Map<String, Object>> generateQRCode(
            @RequestParam(name = "uploadT",required = false)String uploadT,
            @RequestParam(name = "mbCode",required = true) String mbCode,
            @RequestParam(name = "addressId",required = true) String addressId,
            @RequestParam(name = "baseUrl",required = true) String baseUrl,
            @RequestParam(name = "userId",required = false) String userId) {
        try {
            Map<String, Object> result = new HashMap<>();
            String imageUrl = "";
            String imageUrlHeader = minioUrl + "/" + bucketName;
            CjxtStandardAddress standardAddress = standardAddressService.getById(addressId);
            SysUser sysUser = null ;
            if(standardAddress!=null && standardAddress.getAddressCodeMz()!=null && !"".equals(standardAddress.getAddressCodeMz())){
                sysUser =  sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserSf,"1").eq(SysUser::getOrgCode,standardAddress.getAddressCodeMz()).orderByDesc(SysUser::getCreateTime).last("LIMIT 1"));
            }
            if(standardAddress!=null && !"".equals(standardAddress.getCyryQrcode()) && standardAddress.getCyryQrcode()!=null){
                if("RY002".equals(mbCode)){
                    imageUrl = standardAddress.getCyryQrcode();
                }
            }else {
                CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"qrCodeUrl"));
                String BASE_URL = "";
                if(cjxtXtcs!=null && cjxtXtcs.getCsVal()!=null && !"".equals(cjxtXtcs.getCsVal())){
                    BASE_URL = cjxtXtcs.getCsVal()+baseUrl;
                }else {
                    BASE_URL = "http://111.20.214.43:20144/app/"+baseUrl;
                }
                // 生成二维码图片并转换为 MultipartFile
                String text = "";
                if(sysUser!=null){
                    userId = sysUser.getId();
                    text += BASE_URL+"?mbCode="+mbCode+"&addressId="+addressId+"&userId="+userId;
                }else {
                    text += BASE_URL+"?mbCode="+mbCode+"&addressId="+addressId;
                }
                MultipartFile multipartFile = generateQRCodeImage(text, 300, 300,addressId+"_sb");
                String bizPath = "";
                if(CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)){
                    //未指定目录，则用阿里云默认目录 upload
                    bizPath = "upload";
                }else{
                    bizPath = "";
                }
                // 上传文件原方法
//                imageUrl = upload(multipartFile, bizPath, uploadType);
                if(!"".equals(uploadT) && uploadT!=null){
                    if("1".equals(uploadT)){
                        bizPath = "ocr";
                    }else if("2".equals(uploadT)){
                        bizPath = "dzewm";
                    }else if("3".equals(uploadT)){
                        bizPath = "yhsb";
                    }
                }
                imageUrl = MinioUtil.uploadUrl(multipartFile, bizPath, null);
                if(standardAddress!=null){
                    if("RY002".equals(mbCode)){
                        standardAddress.setCyryQrcode(imageUrl);
                    }
                    standardAddressService.updateById(standardAddress);
                }
            }
            result.put("url",imageUrlHeader+"/"+imageUrl);
            return Result.ok(result);
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 地址门牌信息
     * @param addressId
     * @param addressMsg
     * @return
     */
    @GetMapping("/addressMpQrcode")
    @ApiOperation(value="二维码上报-地址门牌信息", notes="二维码上报-地址门牌信息")
    public Result<Map<String, Object>> addressMpQrcode(
            @RequestParam(name = "uploadT",required = false)String uploadT,
            @RequestParam(name = "addressId",required = true) String addressId,
            @RequestParam(name = "addressMsg",required = true) String addressMsg) {
        try {
            Map<String, Object> result = new HashMap<>();
            String imageUrl = "";
            String imageUrlHeader = minioUrl + "/" + bucketName;
            CjxtStandardAddress standardAddress = standardAddressService.getById(addressId);
            if(standardAddress!=null && !"".equals(standardAddress.getAddressMpQrcode()) && standardAddress.getAddressMpQrcode()!=null){
                imageUrl = standardAddress.getAddressMpQrcode();
            }else {
                // 生成二维码图片并转换为 MultipartFile
                String text = "";
                text += addressMsg;
                MultipartFile multipartFile = generateQRCodeImage(text, 300, 300,addressId+"_mp");
                String bizPath = "";
                if(CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)){
                    //未指定目录，则用阿里云默认目录 upload
                    bizPath = "upload";
                }else{
                    bizPath = "";
                }
                // 上传文件原方法
//                imageUrl = upload(multipartFile, bizPath, uploadType);
                if(!"".equals(uploadT) && uploadT!=null){
                    if("1".equals(uploadT)){
                        bizPath = "ocr";
                    }else if("2".equals(uploadT)){
                        bizPath = "dzewm";
                    }else if("3".equals(uploadT)){
                        bizPath = "yhsb";
                    }
                }
                imageUrl = MinioUtil.uploadUrl(multipartFile, bizPath, null);
                if(standardAddress!=null){
                    standardAddress.setAddressMpQrcode(imageUrl);
                    standardAddressService.updateById(standardAddress);
                }
            }
            result.put("url",imageUrlHeader+"/"+imageUrl);
            return Result.ok(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 任务地址二维码
     * @param addressId
     * @param addressMsg
     * @return
     */
    @GetMapping("/taskAddressQrcode")
    @ApiOperation(value="二维码上报-任务地址二维码", notes="二维码上报-地址门牌信息")
    public Result<Map<String, Object>> taskAddressQrcode(
            @RequestParam(name = "uploadT",required = false)String uploadT,
            @RequestParam(name = "addressId",required = true) String addressId,
            @RequestParam(name = "addressMsg",required = true) String addressMsg) {
        try {
            Map<String, Object> result = new HashMap<>();
            String imageUrl = "";
            String imageUrlHeader = minioUrl + "/" + bucketName;
            CjxtStandardAddress standardAddress = standardAddressService.getById(addressId);
            if(standardAddress!=null && !"".equals(standardAddress.getTaskAddressQrcode()) && standardAddress.getTaskAddressQrcode()!=null){
                imageUrl = standardAddress.getTaskAddressQrcode();
            }else {
                // 生成二维码图片并转换为 MultipartFile
                String text = "";
                text += addressMsg;
                MultipartFile multipartFile = generateQRCodeImage(text, 300, 300,addressId+"_dzrw");
                String bizPath = "";
                if(CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)){
                    //未指定目录，则用阿里云默认目录 upload
                    bizPath = "upload";
                }else{
                    bizPath = "";
                }
                // 上传文件原方法
//                imageUrl = upload(multipartFile, bizPath, uploadType);
                if(!"".equals(uploadT) && uploadT!=null){
                    if("1".equals(uploadT)){
                        bizPath = "ocr";
                    }else if("2".equals(uploadT)){
                        bizPath = "dzewm";
                    }else if("3".equals(uploadT)){
                        bizPath = "yhsb";
                    }
                }
                imageUrl = MinioUtil.uploadUrl(multipartFile, bizPath, null);
                if(standardAddress!=null){
                    standardAddress.setTaskAddressQrcode(imageUrl);
                    standardAddressService.updateById(standardAddress);
                }
            }
            result.put("url",imageUrlHeader+"/"+imageUrl);
            return Result.ok(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static MultipartFile generateQRCodeImage(String text, int width, int height, String addressId) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        String fileName = addressId + ".png";
        MultipartFile multipartFile = new MockMultipartFile("file", fileName, "image/png", pngData);

        return multipartFile;
    }


    /**
     * 统一全局上传
     * @Return: java.lang.String
     */
    public static String upload(MultipartFile file, String bizPath, String uploadType) {
        String url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)) {
                url = MinioUtil.upload(file, bizPath);
            } else {
                url = OssBootUtil.upload(file, bizPath);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JeecgBootException(e.getMessage());
        }
        return url;
    }
}
