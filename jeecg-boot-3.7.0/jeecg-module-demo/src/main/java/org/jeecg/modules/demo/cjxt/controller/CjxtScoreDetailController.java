package org.jeecg.modules.demo.cjxt.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtScoreDetail;
import org.jeecg.modules.demo.cjxt.service.ICjxtScoreDetailService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

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
 * @Description: 积分明细表
 * @Author: jeecg-boot
 * @Date:   2024-06-17
 * @Version: V1.0
 */
@Api(tags="积分明细表")
@RestController
@RequestMapping("/cjxt/cjxtScoreDetail")
@Slf4j
public class CjxtScoreDetailController extends JeecgController<CjxtScoreDetail, ICjxtScoreDetailService> {
	@Autowired
	private ICjxtScoreDetailService cjxtScoreDetailService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	 //minio图片服务器
	 @Value(value="${jeecg.minio.minio_url}")
	 private String minioUrl;
	 @Value(value="${jeecg.minio.bucketName}")
	 private String bucketName;

	/**
	 * 分页列表查询
	 *
	 * @param cjxtScoreDetail
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "积分明细表-分页列表查询")
	@ApiOperation(value="积分明细表-分页列表查询", notes="积分明细表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtScoreDetail>> queryPageList(CjxtScoreDetail cjxtScoreDetail,
								   @RequestParam(name="userId",required=false) String userId,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		if(userId!=null && !"".equals(userId)){
			cjxtScoreDetail.setUserId(userId);
		}else {
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			cjxtScoreDetail.setUserId(sysUser.getId());
		}
		QueryWrapper<CjxtScoreDetail> queryWrapper = QueryGenerator.initQueryWrapper(cjxtScoreDetail, req.getParameterMap());
		queryWrapper.orderByDesc("create_time");
		Page<CjxtScoreDetail> page = new Page<CjxtScoreDetail>(pageNo, pageSize);
		IPage<CjxtScoreDetail> pageList = cjxtScoreDetailService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 /**
	  * APP分页列表查询
	  *
	  * @param cjxtScoreDetail
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 //@AutoLog(value = "积分明细表-分页列表查询")
	 @ApiOperation(value="积分明细表-APP分页列表查询", notes="积分明细表-APP分页列表查询")
	 @GetMapping(value = "/listAPP")
	 public Result<Map<String, Object>> listAPP(CjxtScoreDetail cjxtScoreDetail,
														 @RequestParam(name="userId",required=false) String userId,
														 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														 HttpServletRequest req) {
		 Map<String, Object> result = new HashMap<>();
		 String scoreNum = "";
		 if(userId!=null && !"".equals(userId)){
			 cjxtScoreDetail.setUserId(userId);
			 String sql = "SELECT SUM(score) FROM cjxt_score_detail WHERE del_flag = '0' AND user_id = '"+userId+"' ";
			 scoreNum = jdbcTemplate.queryForObject(sql, String.class);
		 }else {
			 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			 cjxtScoreDetail.setUserId(sysUser.getId());
			 String sql = "SELECT SUM(score) FROM cjxt_score_detail WHERE del_flag = '0' AND user_id = '"+userId+"' ";
			 scoreNum = jdbcTemplate.queryForObject(sql, String.class);
		 }
		 QueryWrapper<CjxtScoreDetail> queryWrapper = QueryGenerator.initQueryWrapper(cjxtScoreDetail, req.getParameterMap());
		 queryWrapper.orderByDesc("create_time");
		 Page<CjxtScoreDetail> page = new Page<CjxtScoreDetail>(pageNo, pageSize);
		 IPage<CjxtScoreDetail> pageList = cjxtScoreDetailService.page(page, queryWrapper);
		 result.put("data",pageList);
		 result.put("scoreNum",scoreNum);
		 return Result.OK(result);
	 }


	 @ApiOperation(value="积分排序-分页", notes="积分排序-分页")
	 @GetMapping(value = "/listMx")
	 public Result<Map<String, Object>> queryPageListMx(@RequestParam(name="userId",required=false) String userId,
														@RequestParam(name="isMonth",required=false) String isMonth,
														@RequestParam(name="month",required=false) String month,
														@RequestParam(name="weekS",required=false) String weekS,
														@RequestParam(name="weekE",required=false) String weekE,
														@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														HttpServletRequest req) {
		 Map<String, Object> result = new HashMap<>();

		 // 获取当前年的月份
		 Calendar calendar = Calendar.getInstance();
		 int currentYear = calendar.get(Calendar.YEAR);
		 int currentMonth = calendar.get(Calendar.MONTH) + 1;
		 String currentYearMonth = String.format("%04d-%02d", currentYear, currentMonth);

		 // 获取当前日期的周一和周日
		 calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		 Date weekStartDate = calendar.getTime();
		 calendar.add(Calendar.DATE, 6);
		 Date weekEndDate = calendar.getTime();

		 // 将日期格式化为字符串
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		 String formattedWeekStartDate = dateFormat.format(weekStartDate);
		 String formattedWeekEndDate = dateFormat.format(weekEndDate);

		 List<Map<String, Object>> resultList = new ArrayList<>();
		 List<Map<String, Object>> resultUser = new ArrayList<>();
		 String sql = null;
		 String countSql = null;
		 //周搜索
		 StringBuilder weekBuilder = new StringBuilder();
		 //月搜索
		 StringBuilder creatTBuilder = new StringBuilder();
		 //是否月
		 if("1".equals(isMonth)){
			 if(!"".equals(month) && month!=null){
				 creatTBuilder.append( "AND cs.create_time like '").append(month).append("%' ");
			 }
			 if("".equals(month) || month==null){
				 creatTBuilder.append( "AND cs.create_time like '").append(currentYearMonth).append("%' ");
			 }
		 }
		 if("0".equals(isMonth)){
			 //开始时间结束时间为空
			 if(("".equals(weekS) || weekS==null) && ("".equals(weekE) || weekE==null)){
				 weekBuilder.append(" AND cs.create_time BETWEEN '").append(formattedWeekStartDate + " 00:00:00' ").append(" AND '").append(formattedWeekEndDate + " 23:59:59' ");
			 }
			 //开始时间结束时间都不为空
			 if((!"".equals(weekS) && weekS!=null) && (!"".equals(weekE) && weekE!=null)){
				 weekBuilder.append( " AND cs.create_time BETWEEN '").append(weekS+" 00:00:00' ").append(" AND '").append(weekE+" 23:59:59' ");
			 }
			 //开始时间为空结束时间不为空
			 if(("".equals(weekS) && weekS==null)){
				 weekBuilder.append(" AND cs.create_time <= '").append(weekE + " 23:59:59' ");
			 }
			 //开始时间不为空结束时间为空
			 if((!"".equals(weekS) && weekS!=null) && ("".equals(weekE) && weekE==null)){
				 weekBuilder.append(" AND cs.create_time >= '").append(weekS + " 00:00:00' ");
			 }
		 }
		 if(!"".equals(isMonth) && isMonth!=null){
			 //周、月排名
			 sql = "SELECT " +
					 "	(@rank := @rank + 1) AS ranking," +
					 "	userId," +
					 "	userName," +
					 "	score," +
					 "	avatar " +
					 "FROM ( " +
					 "	SELECT " +
					 "	    cs.user_id AS userId, " +
					 "	    cs.user_name AS userName, " +
					 "	    SUM(cs.score) AS score, " +
					 "	    su.avatar " +
					 "	FROM " +
					 "	    cjxt_score_detail cs " +
					 "	INNER JOIN " +
					 "	    sys_user su " +
					 "	ON " +
					 "	    cs.user_id = su.id " +
					 "	WHERE cs.del_flag = '0' " + creatTBuilder + weekBuilder +
					 "	GROUP BY " +
					 "	    cs.user_id, cs.user_name " +
					 "	ORDER BY " +
					 "	    score DESC " +
					 "	LIMIT 0, 10" +
					 ") AS subquery, (SELECT @rank := 0) AS r";
			 resultList = jdbcTemplate.queryForList(sql);
			 result.put("data",resultList);

			 //总条数
			 countSql = "SELECT COUNT(*) AS total_rows " +
					 "FROM ( " +
					 "    SELECT cs.user_id, cs.user_name " +
					 "    FROM cjxt_score_detail cs " +
					 "    WHERE cs.del_flag = '0' " + creatTBuilder + weekBuilder +
					 "    GROUP BY cs.user_id, cs.user_name " +
					 ") AS grouped_data ";
			 // 执行查询并获取总条数
			 int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
			 // 计算总页数
			 int totalPages = (int) Math.ceil((double) totalCount / pageSize);

			 for (Map<String, Object> item : resultList) {
				 String avatar = (String) item.get("avatar");
				 if (avatar != null ) {
					 item.put("avatar", minioUrl+"/"+bucketName+"/"+ avatar);
				 }
			 }

			 //用户信息
			 if(!"".equals(userId) && userId!=null){
				 for(Map<String, Object> item : resultList){
					 if(userId.equals(item.get("userId"))){
						 resultUser.add(item);
					 }
				 }
			 }

			 //返回结果
			 result.put("dataUser",resultUser);
			 result.put("totalPages", totalPages);
			 result.put("data",resultList);
		 }else {
			 result.put("message","查询失败!");
		 }
		 return Result.OK(result);
	 }




	/**
	 *   添加
	 *
	 * @param cjxtScoreDetail
	 * @return
	 */
	@AutoLog(value = "积分明细表-添加")
	@ApiOperation(value="积分明细表-添加", notes="积分明细表-添加")
//	@RequiresPermissions("cjxt:cjxt_score_detail:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtScoreDetail cjxtScoreDetail) {
		cjxtScoreDetailService.save(cjxtScoreDetail);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtScoreDetail
	 * @return
	 */
	@AutoLog(value = "积分明细表-编辑")
	@ApiOperation(value="积分明细表-编辑", notes="积分明细表-编辑")
//	@RequiresPermissions("cjxt:cjxt_score_detail:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtScoreDetail cjxtScoreDetail) {
		cjxtScoreDetailService.updateById(cjxtScoreDetail);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "积分明细表-通过id删除")
	@ApiOperation(value="积分明细表-通过id删除", notes="积分明细表-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_score_detail:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtScoreDetailService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "积分明细表-批量删除")
	@ApiOperation(value="积分明细表-批量删除", notes="积分明细表-批量删除")
//	@RequiresPermissions("cjxt:cjxt_score_detail:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtScoreDetailService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "积分明细表-通过id查询")
	@ApiOperation(value="积分明细表-通过id查询", notes="积分明细表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtScoreDetail> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtScoreDetail cjxtScoreDetail = cjxtScoreDetailService.getById(id);
		if(cjxtScoreDetail==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtScoreDetail);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtScoreDetail
    */
//    @RequiresPermissions("cjxt:cjxt_score_detail:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtScoreDetail cjxtScoreDetail) {
        return super.exportXls(request, cjxtScoreDetail, CjxtScoreDetail.class, "积分明细表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_score_detail:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtScoreDetail.class);
    }

}
