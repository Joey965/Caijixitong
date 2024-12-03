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

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.sf.json.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbgl;
import org.jeecg.modules.demo.cjxt.entity.CjxtRyhccj;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtMbglService;
import org.jeecg.modules.demo.cjxt.service.ICjxtRyhccjService;

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
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @Description: 人员核查
 * @Author: jeecg-boot
 * @Date:   2024-08-20
 * @Version: V1.0
 */
@Api(tags="人员核查")
@RestController
@RequestMapping("/cjxt/cjxtRyhccj")
@Slf4j
public class CjxtRyhccjController extends JeecgController<CjxtRyhccj, ICjxtRyhccjService> {
	@Autowired
	private ICjxtRyhccjService cjxtRyhccjService;
	@Autowired
	private ICjxtXtcsService cjxtXtcsService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	 //minio图片服务器
	 @Value(value="${jeecg.minio.minio_url}")
	 private String minioUrl;
	 @Value(value="${jeecg.minio.bucketName}")
	 private String bucketName;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtRyhccj
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "人员核查-分页列表查询")
	@ApiOperation(value="人员核查-分页列表查询", notes="人员核查-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtRyhccj>> queryPageList(CjxtRyhccj cjxtRyhccj,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtRyhccj> queryWrapper = QueryGenerator.initQueryWrapper(cjxtRyhccj, req.getParameterMap());
		Page<CjxtRyhccj> page = new Page<CjxtRyhccj>(pageNo, pageSize);
		IPage<CjxtRyhccj> pageList = cjxtRyhccjService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtRyhccj
	 * @return
	 */
	@AutoLog(value = "人员核查-添加")
	@ApiOperation(value="人员核查-添加", notes="人员核查-添加")
//	@RequiresPermissions("cjxt:cjxt_ryhccj:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtRyhccj cjxtRyhccj) {
		cjxtRyhccjService.save(cjxtRyhccj);
		//根据身份证更新（暂时弃用）
//		if(!"".equals(cjxtRyhccj.getGmsfzh())){
//			CjxtRyhccj ryhccj = cjxtRyhccjService.getOne(new LambdaQueryWrapper<CjxtRyhccj>().eq(CjxtRyhccj::getGmsfzh,cjxtRyhccj.getGmsfzh()).last("LIMIT 1"));
//			if(ryhccj!=null){
//				if(cjxtRyhccj.getSfzdry() != null || !"".equals(cjxtRyhccj.getSfzdry())){
//					ryhccj.setSfzdry(cjxtRyhccj.getSfzdry());
//				}
//				if(cjxtRyhccj.getCjdz() != null || !"".equals(cjxtRyhccj.getCjdz())){
//					ryhccj.setCjdz(cjxtRyhccj.getCjdz());
//				}
//				if(cjxtRyhccj.getCjsj() != null || !"".equals(cjxtRyhccj.getCjsj())){
//					ryhccj.setCjsj(cjxtRyhccj.getCjsj());
//				}
//				if(cjxtRyhccj.getLongitude() != null || !"".equals(cjxtRyhccj.getLongitude())){
//					ryhccj.setLongitude(cjxtRyhccj.getLongitude());
//				}
//				if(cjxtRyhccj.getLatitude() != null || !"".equals(cjxtRyhccj.getLatitude())){
//					ryhccj.setLatitude(cjxtRyhccj.getLatitude());
//				}
//				cjxtRyhccjService.updateById(ryhccj);
//			}else {
//				cjxtRyhccjService.save(cjxtRyhccj);
//			}
//		}

		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtRyhccj
	 * @return
	 */
	@AutoLog(value = "人员核查-编辑")
	@ApiOperation(value="人员核查-编辑", notes="人员核查-编辑")
//	@RequiresPermissions("cjxt:cjxt_ryhccj:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtRyhccj cjxtRyhccj) {
		cjxtRyhccjService.updateById(cjxtRyhccj);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "人员核查-通过id删除")
	@ApiOperation(value="人员核查-通过id删除", notes="人员核查-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_ryhccj:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtRyhccjService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "人员核查-批量删除")
	@ApiOperation(value="人员核查-批量删除", notes="人员核查-批量删除")
//	@RequiresPermissions("cjxt:cjxt_ryhccj:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtRyhccjService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "人员核查-通过id查询")
	@ApiOperation(value="人员核查-通过id查询", notes="人员核查-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtRyhccj> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtRyhccj cjxtRyhccj = cjxtRyhccjService.getById(id);
		if(cjxtRyhccj==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtRyhccj);
	}

	 @AutoLog(value = "人员核查-公安网大数据核查")
	 @ApiOperation(value="人员核查-公安网大数据核查", notes="人员核查-公安网大数据核查")
	 @GetMapping(value = "/queryByR")
	 public Result<Map<String, Object>> queryByR(@RequestParam(name="sfzh",required=false) String sfzh) {
		 Map<String, Object> result = new HashMap<>();
		 CjxtXtcs requestHeader = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_rk_requestHeader"));
		 CjxtXtcs accessId = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_accessId"));
		 CjxtXtcs accessKey = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_accessKey"));
		 CjxtXtcs reqId = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_rk_reqId"));
		 CjxtXtcs resId = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"dsj_rk_resId"));
		 try {
			 if(requestHeader!=null && accessId!=null && accessKey!=null && reqId!=null && resId!=null){
				 String url = requestHeader.getCsVal()+"accessId="+accessId.getCsVal()+"&accessKey="+accessKey.getCsVal()+"&resId="+resId.getCsVal()+"&reqId="+reqId.getCsVal()+"&sql=(%60gmsfhm%60%3D%3F)&queryValues=%5B%7B%22type%22%3A%22string%22%2C%22value%22%3A%22"+sfzh+"%22%7D%5D";
				 String url1 = requestHeader.getCsVal()+"accessId="+accessId.getCsVal()+"&accessKey="+accessKey.getCsVal()+"&resId="+resId.getCsVal()+"&reqId="+reqId.getCsVal()+"&sql=(`SFZH`=?)&queryValues=[{\"type\":\"string\",\"value\":\""+sfzh+"\"}]";
				 String jsonResponse = getCustomerInfo(url);
//				 String jsonResponse = "{\n" +
//						 "\t\"msg\": \"success\",\n" +
//						 "\t\"total\": \"1\",\n" +
//						 "\t\"code\": 0,\n" +
//						 "\t\"data\": [\n" +
//						 "\t\t{\n" +
//						 "\t\t\t\"hjdz_ssxqmc\": \"陕西省西安市长安区\",\n" +
//						 "\t\t\t\"hh\": \"610800005740\",\n" +
//						 "\t\t\t\"csrq\": \"20010202\",\n" +
//						 "\t\t\t\"hyzkmc\": \"未婚\",\n" +
//						 "\t\t\t\"gmsfhm\": \"610121200102025899\",\n" +
//						 "\t\t\t\"rylb\": \"\",\n" +
//						 "\t\t\t\"ryzp\": \"http://92.187.7.166:9988/pki/person/pic?idCard=610121200102025899\",\n" +
//						 "\t\t\t\"res_rownum\": \"3047982\",\n" +
//						 "\t\t\t\"mzmc\": \"汉\",\n" +
//						 "\t\t\t\"xlmc\": \"大专\",\n" +
//						 "\t\t\t\"xm\": \"郭红星\",\n" +
//						 "\t\t\t\"yddh\": \"18192498651\",\n" +
//						 "\t\t\t\"xbmc\": \"男\",\n" +
//						 "\t\t\t\"rowkey\": \"53782084#61270119XXXXXXX19#XXX#6108XXXXXX5740\",\n" +
//						 "\t\t\t\"hjdz_qhnxxdz\": \"陕西省西安市长安区五台街道大瓢村2组28号\"\n" +
//						 "\t\t}\n" +
//						 "\t]\n" +
//						 "}";
				 // 将 JSON 响应转换为 Map
				 JSONObject customerInfo = JSONObject.fromObject(jsonResponse);
				 System.out.println("ssssssssssssssssssssssssssssssssssssssssss sout输出返回结果customerInfo ======================"+customerInfo);
				 log.info("llllllllllllllllllllllllllllll log输出返回结果customerInfo =================="+customerInfo);
				 getCustomerInfo1(url1);
				 getCustomerInfoTest(sfzh);
				 getCustomerInfoTextXt(sfzh);
				 if ("success".equals(customerInfo.getString("msg"))) {
					 JSONObject data = customerInfo.getJSONArray("data").getJSONObject(0);
					 result.put("name",data.getString("xm"));
					 result.put("sex",data.getString("xbmc"));
					 result.put("mz",data.getString("mzmc"));
					 result.put("address",data.getString("hjdz_ssxqmc"));
					 result.put("ryzp",data.getString("ryzp"));
					 String csrq = data.getString("csrq");
					 SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
					 SimpleDateFormat newFormat = new SimpleDateFormat("yyyy年MM月dd日");
					 // 处理日期
					 Date date = originalFormat.parse(csrq);
					 String formattedDate = newFormat.format(date);
					 result.put("birthday", formattedDate);
					 result.put("id_crad",data.getString("gmsfhm"));
					 result.put("sfzdry",data.getString("rylb"));
				 }
			 }
		 } catch (ParseException e) {
			 e.printStackTrace();
		 }
		 return Result.OK(result);
	 }


	 @ApiOperation(value="人员核查-互联网核查", notes="人员核查-互联网核查")
	 @GetMapping(value = "/queryByInter")
	 public Result<Map<String, Object>> queryByInter(@RequestParam(name="sfzh",required=false) String sfzh) {
		 Map<String, Object> result = new HashMap<>();
		 CjxtRyhccj cjxtRyhccj = cjxtRyhccjService.getOne(new LambdaQueryWrapper<CjxtRyhccj>().eq(CjxtRyhccj::getGmsfzh,sfzh).orderByDesc(CjxtRyhccj::getCreateTime).last(" LIMIT 1"));
		 CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getIsDb,"1").eq(CjxtMbgl::getMbbh,"RY001"));
		 if(cjxtMbgl!=null){
			 String sql = "SELECT * FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' AND t.rysfzh = '"+sfzh+"'" ;
			 List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
			 if(rows.size()>0) {
				 Map<String, Object> row = rows.get(0);

				 result.put("name", row.get("ryxm"));
				 result.put("sex", row.get("ryxb"));
				 result.put("mz", row.get("rymz"));
				 result.put("address", row.get("ryhjdxz"));
				 result.put("birthday", row.get("rycsrq"));
				 result.put("id_crad", row.get("rysfzh"));

//				 result.put("name", cjxtRyhccj.getXm());
//				 result.put("sex", cjxtRyhccj.getSex());
//				 result.put("mz", cjxtRyhccj.getMz());
//				 result.put("address", cjxtRyhccj.getZz());
//
//				 String csrq = "";
//				 if(cjxtRyhccj.getCsrq()!=null && !"".equals(cjxtRyhccj.getCsrq())){
//					 csrq = cjxtRyhccj.getCsrq().toString();
//				 }
//				 SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
//				 SimpleDateFormat newFormat = new SimpleDateFormat("yyyy年MM月dd日");
//				 try {
//					 if(!"".equals(csrq)){
//						 Date date = originalFormat.parse(csrq);
//						 String formattedDate = newFormat.format(date);
//						 result.put("birthday", formattedDate);
//					 }
//				 } catch (ParseException e) {
//					 e.printStackTrace();
//				 }
//				 result.put("id_crad", cjxtRyhccj.getGmsfzh());
//				 result.put("sfzdry", cjxtRyhccj.getSfzdry());
			 }
		 }
		 return Result.OK(result);
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtRyhccj
    */
//    @RequiresPermissions("cjxt:cjxt_ryhccj:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtRyhccj cjxtRyhccj) {
        return super.exportXls(request, cjxtRyhccj, CjxtRyhccj.class, "人员核查");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_ryhccj:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtRyhccj.class);
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
			 log.error("getCustomerInfo 异常输出getCustomerInfo: " + e.getMessage());
			 e.printStackTrace();
		 }
		 return jsonObject != null ? jsonObject.toString() : "{\"msg\":\"error\"}";
	 }

	 public String getCustomerInfo1(String Url) {
		 log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++getCustomerInfo1 开始输出 getCustomerInfo1+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
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
			 System.out.println("输出jsonObject" + jsonObject.toString());
			 log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++getCustomerInfo1 结束输出 getCustomerInfo1+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		 } catch (SocketTimeoutException e) {
			 log.error("请求超时: " + e.getMessage());
			 return "{\"msg\":\"timeout\"}";
		 } catch (Exception e) {
			 log.error("getCustomerInfo1 异常输出: " + e.getMessage());
			 e.printStackTrace();
		 }
		 return jsonObject != null ? jsonObject.toString() : "{\"msg\":\"error\"}";
	 }

	 public String getCustomerInfoTest(String sfzh) {
		 log.info("***********************************************************getCustomerInfoTest 开始输出 getCustomerInfoTest***********************************************************");
		 JSONObject jsonObject = null;
		 OutputStreamWriter out = null;
		 StringBuffer buffer = new StringBuffer();
		 try {
			 if("".equals(sfzh)){
				 sfzh="612725199503100014";
			 }
			 // 1.连接部分
			 URL url = new URL("http://92.187.7.139/datac/service/support/esSql/query?accessId=2f9057f4&accessKey=fd96babad2104a8f9e5301dfd1e6aa80&reqId=9&resId=109&sql=(%60gmsfhm%60%3D%3F)&queryValues=%5B%7B%22type%22%3A%22string%22%2C%22value%22%3A%22"+sfzh+"%22%7D%5D");
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
			 System.out.println("输出getCustomerInfo*******************************************************" + jsonObject.toString());
			 log.info("***********************************************************getCustomerInfoTest 结束输出 getCustomerInfoTest**************************************************************");
		 } catch (SocketTimeoutException e) {
			 log.error("请求超时: " + e.getMessage());
			 return "{\"msg\":\"timeout\"}";
		 } catch (Exception e) {
			 log.error("getCustomerInfoTest 异常输出: " + e.getMessage());
			 e.printStackTrace();
		 }
		 return jsonObject != null ? jsonObject.toString() : "{\"msg\":\"error\"}";
	 }

	 public String getCustomerInfoTextXt(String sfzh) {
		 log.info("---------------------------------------------------------getCustomerInfoTextXt 开始输出 getCustomerInfoTextXt---------------------------------------------------------");
		 JSONObject jsonObject = null;
		 StringBuffer buffer = new StringBuffer();
		 try {
			 if("".equals(sfzh)){
				 sfzh = "612725199503100014";
			 }
			 // 1.连接部分
			 URL url = new URL("http://92.187.7.139/datac/service/support/esSql/query?accessId=2f9057f4&accessKey=fd96babad2104a8f9e5301dfd1e6aa80&reqId=9&resId=109&sql=(`SFZH`=?)&queryValues=[{\"type\":\"string\",\"value\":\""+sfzh+"\"}]");
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

			 // 2.获取数据
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
			 httpUrlConn.disconnect();
			 jsonObject = JSONObject.fromObject(buffer.toString());
			 System.out.println("输出getCustomerInfoTextXt---------------------------------------------------------" + jsonObject.toString());
			 log.info("---------------------------------------------------------getCustomerInfoTextXt 结束输出 getCustomerInfoTextXt---------------------------------------------------------");
		 } catch (SocketTimeoutException e) {
			 log.error("请求超时: " + e.getMessage());
			 return "{\"msg\":\"timeout\"}";
		 } catch (Exception e) {
			 log.error("getCustomerInfoTextXt 异常输出: " + e.getMessage());
			 e.printStackTrace();
		 }
		 return jsonObject != null ? jsonObject.toString() : "{\"msg\":\"error\"}";
	 }

 }
