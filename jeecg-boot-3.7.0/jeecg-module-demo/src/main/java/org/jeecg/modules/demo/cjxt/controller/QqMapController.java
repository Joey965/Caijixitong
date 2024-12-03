package org.jeecg.modules.demo.cjxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Api(tags="腾讯地图接口")
@RestController
@RequestMapping("/cjxt/qqMap")
@Slf4j
public class QqMapController {

    @Autowired
    private ICjxtXtcsService cjxtXtcsService;

    @ApiOperation(value = "腾讯地图接口-系统参数Key", notes = "腾讯地图接口-系统参数Key")
    @GetMapping(value = "/qqMapKey")
    public Result<Map<String, Object>> qqMapKey() {
        Map<String,Object> result = new HashMap<>();
        CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"qqMapKey"));
        if(cjxtXtcs!=null){
            result.put("qqMapKey",cjxtXtcs.getCsVal());
        }
        return Result.ok(result);
    }

    @ApiOperation(value = "腾讯地图接口-关键字搜索", notes = "腾讯地图接口-关键字搜索")
    @GetMapping(value = "/suggestion")
    public Result<Map<String, Object>> suggestion(@RequestParam(name = "keyword", required = false) String keyword,
                                                  @RequestParam(name = "key", required = true) String key) throws UnsupportedEncodingException {
        String region = "北京";
        String url = "https://apis.map.qq.com/ws/place/v1/suggestion?keyword=" +
                URLEncoder.encode(keyword, String.valueOf(StandardCharsets.UTF_8)) +
                "&region=" + URLEncoder.encode(region, String.valueOf(StandardCharsets.UTF_8)) +
                "&key="+key;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Result.ok(response.getBody());
            } else {
                return Result.error("获取酒店数据失败，HTTP状态码: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Result.error("获取酒店数据失败: " + e.getMessage());
        }
    }

    @ApiOperation(value = "腾讯地图接口-逆地址转换", notes = "腾讯地图接口-逆地址转换")
    @GetMapping(value = "/geocoder")
    public Result<Map<String, Object>> geocoder(@RequestParam(name = "location", required = false) String location,
                                                @RequestParam(name = "key", required = true) String key) throws UnsupportedEncodingException {
        String url = "https://apis.map.qq.com/ws/geocoder/v1?get_poi=1" +
                "&location=" + URLEncoder.encode(location, String.valueOf(StandardCharsets.UTF_8)) +
                "&key="+key;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Result.ok(response.getBody());
            } else {
                return Result.error("获取酒店数据失败，HTTP状态码: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Result.error("获取酒店数据失败: " + e.getMessage());
        }
    }
}

