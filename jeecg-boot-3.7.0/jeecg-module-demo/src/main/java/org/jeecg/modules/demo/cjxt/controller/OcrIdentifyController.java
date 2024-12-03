package org.jeecg.modules.demo.cjxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.MinioUtil;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;
import org.jeecg.modules.demo.cjxt.utils.PoliceOcr;
import org.jeecg.modules.demo.cjxt.utils.baidu.Idcard;
import org.jeecg.modules.demo.cjxt.utils.baidu.LicensePlate;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags="Ocr识别")
@RestController
@RequestMapping("/cjxt/identify")
@Slf4j
public class OcrIdentifyController {

    //minio桶名称
    @Value(value="${jeecg.minio.bucketName}")
    private String bucketName;

    @Value(value="${jeecg.minio.minio_url}")
    private String minioUrl;
    @Autowired
    private ICjxtXtcsService cjxtXtcsService;
    @Autowired
    private ISysDictItemService sysDictItemService;

    /**
     * OCR识别
     * @param uploadT
     * @param tplx
     * @param tpFile
     * @return
     */
    @IgnoreAuth
    @ApiOperation(value="OCR识别", notes="OCR识别")
    @GetMapping(value = "/ocrsb")
    public Map<String, String> ocrsb(
                                   @RequestParam(required = true, name="uploadT") String uploadT,
                                   @RequestParam(required = true, name="tplx") String tplx,
                                   @RequestParam(required = true, name="tpFile") String tpFile){
        Map<String, String> map = new HashMap<>();
        CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"ocrFileSaveOrDel"));
        CjxtXtcs ocrXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"InterOrPoliceOCR"));
        String TPFILE = tpFile.split("&")[0];
        if("1".equals(tplx)){
            if(ocrXtcs!=null){
                if("1".equals(ocrXtcs.getCsVal())){
                    //公安网
                    map = PoliceOcr.gawOcrSfzh(TPFILE);
                    log.info("=================================================公安网人员识别=================================================");
                }else {
                    //互联网
                    map = Idcard.jxSfzUrl(TPFILE);
                    log.info("=================================================互联网人员识别=================================================");
                }
            }else {
                //互联网
                map = Idcard.jxSfzUrl(TPFILE);
                log.info("=================================================互联网人员识别=================================================");
            }
            String nation = map.get("nation");
            String sex = map.get("sex");
            if(!"".equals(nation)){
                SysDictItem mzItemValue = sysDictItemService.getOne(new LambdaQueryWrapper<SysDictItem>().like(SysDictItem::getItemText,nation).inSql(SysDictItem::getDictId," SELECT id FROM sys_dict WHERE dict_code = 'mz'"));
                if(mzItemValue!=null){
                    map.put("nationItem",mzItemValue.getItemValue());
                }
            }
            if(!"".equals(sex)){
                SysDictItem sexItemValue = sysDictItemService.getOne(new LambdaQueryWrapper<SysDictItem>().like(SysDictItem::getItemText,sex).inSql(SysDictItem::getDictId," SELECT id FROM sys_dict WHERE dict_code = 'sex'"));
                if(sexItemValue!=null){
                    map.put("sexItem",sexItemValue.getItemValue());
                }
            }
        }
        if("2".equals(tplx)){
            if(ocrXtcs!=null){
                if("1".equals(ocrXtcs.getCsVal())){
                    //公安网
                    map = PoliceOcr.gawOcrLicense(TPFILE);
                    log.info("=================================================公安网车辆识别=================================================");
                }else {
                    //互联网
                    map = LicensePlate.licenseUrl(TPFILE);
                    log.info("=================================================互联网车辆识别=================================================");
                }
            }else {
                //互联网
                map = LicensePlate.licenseUrl(TPFILE);
                log.info("=================================================互联网车辆识别=================================================");
            }
        }
        String savePath = TPFILE;
        String heardUrl = minioUrl+"/"+bucketName+"/";
        savePath = savePath.replace(heardUrl,"");

        if(!"".equals(uploadT) && uploadT!=null){
            //Ocr识别标识
            if("1".equals(uploadT)){
                if(cjxtXtcs!=null && "0".equals(cjxtXtcs.getCsVal())){
                    MinioUtil.removeObject(bucketName,savePath);
                }
            }
        }
        return map;
    }

    /**
     * 公安网
     * @param uploadT
     * @param tplx
     * @param tpFile
     * @return
     */
    @IgnoreAuth
    @ApiOperation(value="公安网-OCR识别", notes="公安网-OCR识别")
    @GetMapping(value = "/ocrsbGzw")
    public Map<String, String> ocrsbGzw(
            @RequestParam(required = true, name="uploadT") String uploadT,
            @RequestParam(required = true, name="tplx") String tplx,
            @RequestParam(required = true, name="tpFile") String tpFile){
        Map<String, String> map = new HashMap<>();
        CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"ocrFileSaveOrDel"));
        if("1".equals(tplx)){
            map = PoliceOcr.gawOcrSfzh(tpFile);
        }
        if("2".equals(tplx)){
            map = PoliceOcr.gawOcrLicense(tpFile);
        }
        String savePath = tpFile;
        String heardUrl = minioUrl+"/"+bucketName+"/";
        savePath = savePath.replace(heardUrl,"");

        if(!"".equals(uploadT) && uploadT!=null){
            //Ocr识别标识
            if("1".equals(uploadT)){
                if(cjxtXtcs!=null && "0".equals(cjxtXtcs.getCsVal())){
                    MinioUtil.removeObject(bucketName,savePath);
                }
            }
        }
        return map;
    }

    @ApiOperation(value="OCR识别车牌", notes="OCR识别车牌")
    @GetMapping(value = "/ocrsbcp")
    public Map<String, String> ocrsbcp(
                                   @RequestParam(required = true, name="uploadT") String uploadT,
                                   @RequestParam(required = true, name="tplx" ,defaultValue="1") String tplx,
                                   @RequestParam(required = true, name="tpFile") String tpFile){
        Map<String, String> map = new HashMap<>();
        CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"ocrFileSaveOrDel"));
        if("1".equals(tplx)){
            map = LicensePlate.licenseUrl(tpFile);
        }

        String savePath = tpFile;
        String heardUrl = minioUrl+"/"+bucketName+"/";
        savePath = savePath.replace(heardUrl,"");

        if(!"".equals(uploadT) && uploadT!=null){
            //Ocr识别标识
            if("1".equals(uploadT)){
                if(cjxtXtcs!=null && "0".equals(cjxtXtcs.getCsVal())){
                    MinioUtil.removeObject(bucketName,savePath);
                }
            }
        }
        return map;
    }


}
