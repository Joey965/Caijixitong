package org.jeecg.modules.demo.cjxt.controller;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.sf.json.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtLicensePlate;
import org.jeecg.modules.demo.cjxt.entity.CjxtTaskDispatch;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtLicensePlateService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

 /**
 * @Description: 车辆核查
 * @Author: jeecg-boot
 * @Date:   2024-08-20
 * @Version: V1.0
 */
@Api(tags="车辆核查")
@RestController
@RequestMapping("/cjxt/cjxtLicensePlate")
@Slf4j
public class CjxtLicensePlateController extends JeecgController<CjxtLicensePlate, ICjxtLicensePlateService> {
	@Autowired
	private ICjxtLicensePlateService cjxtLicensePlateService;
	@Autowired
	private ICjxtXtcsService cjxtXtcsService;
	//minio图片服务器
	@Value(value="${jeecg.minio.minio_url}")
	private String minioUrl;
	@Value(value="${jeecg.minio.bucketName}")
	private String bucketName;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtLicensePlate
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "车辆核查-分页列表查询")
	@ApiOperation(value="车辆核查-分页列表查询", notes="车辆核查-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtLicensePlate>> queryPageList(CjxtLicensePlate cjxtLicensePlate,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		QueryWrapper<CjxtLicensePlate> queryWrapper = QueryGenerator.initQueryWrapper(cjxtLicensePlate, req.getParameterMap());
		Page<CjxtLicensePlate> page = new Page<CjxtLicensePlate>(pageNo, pageSize);
		IPage<CjxtLicensePlate> pageList = cjxtLicensePlateService.page(page, queryWrapper);
		for(CjxtLicensePlate licensePlate: pageList.getRecords()){
			if(licensePlate.getClzp()!=null && !"".equals(cjxtLicensePlate.getClzp())){
				licensePlate.setClzp(heardUrl+"/"+licensePlate.getClzp());
			}
		}
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtLicensePlate
	 * @return
	 */
	@AutoLog(value = "车辆核查-添加")
	@ApiOperation(value="车辆核查-添加", notes="车辆核查-添加")
//	@RequiresPermissions("cjxt:cjxt_license_plate:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtLicensePlate cjxtLicensePlate) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtLicensePlate.getClzp()!=null && !"".equals(cjxtLicensePlate.getClzp())){
			if(cjxtLicensePlate.getClzp().contains(heardUrl)){
				cjxtLicensePlate.setClzp(cjxtLicensePlate.getClzp().replace(heardUrl,""));
			}
		}
		cjxtLicensePlateService.save(cjxtLicensePlate);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtLicensePlate
	 * @return
	 */
	@AutoLog(value = "车辆核查-编辑")
	@ApiOperation(value="车辆核查-编辑", notes="车辆核查-编辑")
//	@RequiresPermissions("cjxt:cjxt_license_plate:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtLicensePlate cjxtLicensePlate) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtLicensePlate.getClzp()!=null && !"".equals(cjxtLicensePlate.getClzp())){
			if(cjxtLicensePlate.getClzp().contains(heardUrl)){
				cjxtLicensePlate.setClzp(cjxtLicensePlate.getClzp().replace(heardUrl,""));
			}
		}
		cjxtLicensePlateService.updateById(cjxtLicensePlate);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "车辆核查-通过id删除")
	@ApiOperation(value="车辆核查-通过id删除", notes="车辆核查-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_license_plate:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtLicensePlateService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "车辆核查-批量删除")
	@ApiOperation(value="车辆核查-批量删除", notes="车辆核查-批量删除")
//	@RequiresPermissions("cjxt:cjxt_license_plate:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtLicensePlateService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "车辆核查-通过id查询")
	@ApiOperation(value="车辆核查-通过id查询", notes="车辆核查-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtLicensePlate> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtLicensePlate cjxtLicensePlate = cjxtLicensePlateService.getById(id);
		if(cjxtLicensePlate==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtLicensePlate);
	}

	 @ApiOperation(value="车辆核查-大数据平台核查", notes="车辆核查-大数据平台核查")
	 @GetMapping(value = "/queryByP")
	 public Result<Map<String, Object>> queryByP(@RequestParam(name="cp",required=false) String cp,
												 @RequestParam(name="cllx",required=false) String cllx) {
		 Map<String, Object> result = new HashMap<>();
		 CjxtXtcs requestHeader = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_cl_requestHeader"));
		 CjxtXtcs accessId = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_accessId"));
		 CjxtXtcs accessKey = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_accessKey"));
		 CjxtXtcs reqId = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_cl_reqId"));
		 CjxtXtcs resId = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_cl_resId"));
		 //公安网互联网核查配置 0-互联网 1-公安网
		 CjxtXtcs clcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_cl_cs"));

		 try {
			 if(requestHeader!=null && accessId!=null && accessKey!=null && reqId!=null && resId!=null){
				 String firstChar = "";
				 firstChar = String.valueOf(cp.charAt(0));
				 if (Character.isLetter(cp.charAt(0))) {
					 // 去除第一个字符
					 cp = cp.substring(1);
				 }
				 String url = requestHeader.getCsVal()+"accessId="+accessId.getCsVal()+"&accessKey="+accessKey.getCsVal()+"&resId="+resId.getCsVal()+"&reqId="+reqId.getCsVal()+"&sql=(%60jdchphm%60%3D%3F)&queryValues=%5B%7B%22type%22%3A%22string%22%2C%22value%22"+cp+"%22%7D%5D";
				 String jsonResponse = "";
				 if(clcs!=null){
					 //公安网
					 if("1".equals(clcs.getCsVal())){
						 jsonResponse = getCustomerInfo(url);
					 }
					 //互联网
					 if("0".equals(clcs.getCsVal())){
						 jsonResponse = "{\n" +
								 "\t\"msg\": \"success\",\n" +
								 "\t\"total\": \"1\",\n" +
								 "\t\"code\": 0,\n" +
								 "\t\"data\": [\n" +
								 "\t\t{\n" +
								 "\t\t\t\"jdcsyrmc\": \"张三\",\n" +
								 "\t\t\t\"clxh\": \"CC6470CF01CPHEV\",\n" +
								 "\t\t\t\"rowkey\": \"52#XXXXX#XXXXXXXXXXX#610100000400#610108267845\",\n" +
								 "\t\t\t\"jdchphm\": \"" + cp + "\",\n" +
								 "\t\t\t\"jdccsysdm_dic\": \"X\",\n" +
								 "\t\t\t\"zjhm\": \"610121200102025899\",\n" +
								 "\t\t\t\"jdcfdjddjh\": \"23455505135\",\n" +
								 "\t\t\t\"res_rownum\": \"54980939\",\n" +
								 "\t\t\t\"jdccsysdm\": \"B\",\n" +
								 "\t\t\t\"cl_zwppmc\": \"哈弗牌\"\n" +
								 "\t\t}\n" +
								 "\t]\n" +
								 "}\n";
					 }
				 }else {
					 //查不到默认公安网
					 jsonResponse = getCustomerInfo(url);
				 }
				 // 将 JSON 响应转换为 Map
				 JSONObject customerInfo = JSONObject.fromObject(jsonResponse);
				 System.out.println("ssssssssssssssssssssssssssssssssssssssssss sout输出返回结果customerInfo ======================"+customerInfo);
				 log.info("llllllllllllllllllllllllllllll log输出返回结果customerInfo =================="+customerInfo);

				 if ("success".equals(customerInfo.getString("msg"))) {
					 JSONObject data = customerInfo.getJSONArray("data").getJSONObject(0);
					 result.put("cphm",firstChar+data.getString("jdchphm"));
					 result.put("cplx",data.getString("clxh"));
					 result.put("clys",data.getString("jdccsysdm_dic"));
					 result.put("fdjh",data.getString("jdcfdjddjh"));
					 result.put("clpp",data.getString("cl_zwppmc"));
					 result.put("syrsfz",data.getString("zjhm"));
					 result.put("syr",data.getString("jdcsyrmc"));
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return Result.OK(result);
	 }

	 public String getCustomerInfo(String Url) {
		 log.info("===========================================================getCustomerInfo 开始输出 getCustomerInfo===========================================================");
		 JSONObject jsonObject = null;
		 OutputStreamWriter out = null;
		 StringBuffer buffer = new StringBuffer();
		 try {
			 // 1.连接部分
			 URL url = new URL(Url);
			 // http协议传输
			 HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

			 httpUrlConn.setDoOutput(true);
			 httpUrlConn.setDoInput(true);
			 httpUrlConn.setUseCaches(false);
			 // 设置请求方式（GET/POST）
			 httpUrlConn.setRequestMethod("GET");
			 httpUrlConn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
			 // 设置超时时间
			 httpUrlConn.setConnectTimeout(6000); // 连接超时6秒
			 httpUrlConn.setReadTimeout(6000); // 读取超时6秒

			 // 2.传入参数部分
			 // 得到请求的输出流对象
			 out = new OutputStreamWriter(httpUrlConn.getOutputStream(), "UTF-8");
			 out.flush();
			 out.close();

			 // 3.获取数据
			 // 将返回的输入流转换成字符串
			 InputStream inputStream = httpUrlConn.getInputStream();
			 InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			 BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			 String str = null;
			 while ((str = bufferedReader.readLine()) != null) {
				 buffer.append(str);
			 }
			 bufferedReader.close();
			 inputStreamReader.close();
			 // 释放资源
			 inputStream.close();
			 inputStream = null;
			 httpUrlConn.disconnect();
			 jsonObject = JSONObject.fromObject(buffer.toString());
			 System.out.println("输出getCustomerInfo===========================================================" + jsonObject.toString());
			 log.info("===========================================================getCustomerInfo 结束输出 getCustomerInfo===========================================================");
		 } catch (SocketTimeoutException e) {
			 log.error("请求超时: " + e.getMessage());
			 return "{\"msg\":\"timeout\"}";
		 } catch (Exception e) {
			 log.info("getCustomerInfo 异常输出getCustomerInfo==========================================================="+e);
			 e.printStackTrace();
		 }
		 return jsonObject != null ? jsonObject.toString() : "{\"msg\":\"error\"}";
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtLicensePlate
    */
//    @RequiresPermissions("cjxt:cjxt_license_plate:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtLicensePlate cjxtLicensePlate) {
        return super.exportXls(request, cjxtLicensePlate, CjxtLicensePlate.class, "车辆核查");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_license_plate:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtLicensePlate.class);
    }

}
