package org.jeecg.modules.demo.cjxt.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtPitfallReport;
import org.jeecg.modules.demo.cjxt.entity.CjxtTask;
import org.jeecg.modules.demo.cjxt.entity.CjxtTaskDispatch;
import org.jeecg.modules.demo.cjxt.service.ICjxtPitfallReportService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
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
 * @Description: 隐患上报
 * @Author: jeecg-boot
 * @Date:   2024-08-01
 * @Version: V1.0
 */
@Api(tags="隐患上报")
@RestController
@RequestMapping("/cjxt/cjxtPitfallReport")
@Slf4j
public class CjxtPitfallReportController extends JeecgController<CjxtPitfallReport, ICjxtPitfallReportService> {
	@Autowired
	private ICjxtPitfallReportService cjxtPitfallReportService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDepartService sysDepartService;
	 @Autowired
	 private WebSocket webSocket;
	 //minio图片服务器
	 @Value(value="${jeecg.minio.minio_url}")
	 private String minioUrl;
	 @Value(value="${jeecg.minio.bucketName}")
	 private String bucketName;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtPitfallReport
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "隐患上报-分页列表查询")
	@ApiOperation(value="隐患上报-分页列表查询", notes="隐患上报-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtPitfallReport>> queryPageList(CjxtPitfallReport cjxtPitfallReport,
								   @RequestParam(name="search", required = false) String search,
								   @RequestParam(name="userId", required = false) String userId,
								   @RequestParam(name="clzt", required = false) String clzt,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtPitfallReport> queryWrapper = QueryGenerator.initQueryWrapper(cjxtPitfallReport, parameterMap);
		if(userId!=null && !"".equals(userId)){
			SysUser user = sysUserService.getById(userId);
			if("2".equals(user.getUserSf()) || "3".equals(user.getUserSf())){
				queryWrapper.inSql("sys_org_code","SELECT wg_code FROM cjxt_pjwgqx WHERE del_flag = '0' AND pj_id = '"+user.getId()+"'");
			}
			if("4".equals(user.getUserSf())){
				queryWrapper.likeRight("sys_org_code",user.getOrgCode());
			}
			//隐患上报---网格员
			if("1".equals(user.getUserSf())){
				queryWrapper.eq("create_by",user.getUsername());
			}
		}else {
			if("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())){
				queryWrapper.inSql("sys_org_code","SELECT wg_code FROM cjxt_pjwgqx WHERE del_flag = '0' AND pj_id = '"+sysUser.getId()+"'");
			}
			if("4".equals(sysUser.getUserSf())){
				queryWrapper.likeRight("sys_org_code",sysUser.getOrgCode());
			}
			if("1".equals(sysUser.getUserSf())){
				queryWrapper.eq("create_by",sysUser.getUsername());
			}
		}
		if(search!=null && !"".equals(search) && !search.isEmpty()){
			String yhlx = "";
			switch (search) {
				case "重大隐患":
					yhlx =  "1";
				case "紧急隐患":
					yhlx =  "2";
				case "一般隐患":
					yhlx =  "3";
			}
			queryWrapper.like("yhdd",search);
			if(!"".equals(yhlx)){
				queryWrapper.eq("yhlx",yhlx);
			}
		}
		if(!"".equals(clzt) && clzt!=null){
			queryWrapper.eq("clzt",clzt);
		}else {
			queryWrapper.eq("clzt","0");
		}
//		queryWrapper.eq("clzt","0");
		if(!"1".equals(sysUser.getUserSf())){
			queryWrapper.last(" OR (create_by = '"+sysUser.getUsername()+"' AND del_flag = '0')");
		}else {
			queryWrapper.orderByDesc("create_time");
		}
		Page<CjxtPitfallReport> page = new Page<CjxtPitfallReport>(pageNo, pageSize);
		IPage<CjxtPitfallReport> pageList = cjxtPitfallReportService.page(page, queryWrapper);

		pageList.getRecords().forEach(cjxtPitfallReportItem ->{
			if(cjxtPitfallReportItem.getXctp()!=null && !cjxtPitfallReportItem.getXctp().isEmpty()){
				cjxtPitfallReportItem.setXctp(minioUrl+"/"+bucketName+"/"+cjxtPitfallReportItem.getXctp());
			}
			if(cjxtPitfallReportItem.getXcsp()!=null && !cjxtPitfallReportItem.getXcsp().isEmpty()){
				cjxtPitfallReportItem.setXcsp(minioUrl+"/"+bucketName+"/"+cjxtPitfallReportItem.getXcsp());
			}
		});
		return Result.OK(pageList);
	}

	 @ApiOperation(value="隐患上报-未处理数量", notes="隐患上报-未处理数量")
	 @GetMapping(value = "/pitfallWclNum")
	 public Result<Integer> pitfallWclNum(@RequestParam(required = true, name="userId") String userId) {
		 Integer num = 0;
		 List<CjxtPitfallReport> pitfallReportList = new ArrayList<>();
		 SysUser user = sysUserService.getById(userId);
		 //民警/片警
		 if("2".equals(user.getUserSf()) || "3".equals(user.getUserSf())){
			 pitfallReportList = cjxtPitfallReportService.list(new LambdaQueryWrapper<CjxtPitfallReport>().eq(CjxtPitfallReport::getClzt,"0").inSql(CjxtPitfallReport::getSysOrgCode,"SELECT wg_code FROM cjxt_pjwgqx WHERE del_flag = '0' AND pj_id = '"+user.getId()+"'"));
		 }
		 if("4".equals(user.getUserSf())){
			 pitfallReportList = cjxtPitfallReportService.list(new LambdaQueryWrapper<CjxtPitfallReport>().eq(CjxtPitfallReport::getClzt,"0").likeRight(CjxtPitfallReport::getSysOrgCode,user.getOrgCode()));
		 }
		 //隐患上报---网格员
		 if("1".equals(user.getUserSf())){
			 pitfallReportList = cjxtPitfallReportService.list(new LambdaQueryWrapper<CjxtPitfallReport>().eq(CjxtPitfallReport::getClzt,"0").eq(CjxtPitfallReport::getCreateBy,user.getUsername()));
		 }
		 if(pitfallReportList.size()>0){
			 num = pitfallReportList.size();
		 }
		 //WebSocket消息推送
		 if(userId!=null && !"".equals(userId)){
			 JSONObject json = new JSONObject();
			 json.put("msgType", "waMsg");
			 String msgNew = json.toString();
			 webSocket.sendOneMessage(userId, msgNew);
		 }
		 return Result.OK(num);
	 }
	
	/**
	 *   添加
	 *
	 * @param cjxtPitfallReport
	 * @return
	 */
	@AutoLog(value = "隐患上报-添加")
	@ApiOperation(value="隐患上报-添加", notes="隐患上报-添加")
//	@RequiresPermissions("cjxt:cjxt_pitfall_report:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtPitfallReport cjxtPitfallReport) {
		//上报部门
		SysDepart sysDepart = sysDepartService.getById(cjxtPitfallReport.getSbbm());
		if(sysDepart!=null){
			cjxtPitfallReport.setSbbmid(sysDepart.getId());
			cjxtPitfallReport.setSbbm(sysDepart.getDepartNameFull());
		}
		cjxtPitfallReport.setClzt("0");
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtPitfallReport.getXcsp()!=null && !"".equals(cjxtPitfallReport.getXcsp())){
			if(cjxtPitfallReport.getXcsp().contains(heardUrl)){
				cjxtPitfallReport.setXcsp(cjxtPitfallReport.getXcsp().replace(heardUrl,""));
			}
		}
		if(cjxtPitfallReport.getXctp()!=null && !"".equals(cjxtPitfallReport.getXctp())){
			if(cjxtPitfallReport.getXctp().contains(heardUrl)){
				cjxtPitfallReport.setXctp(cjxtPitfallReport.getXctp().replace(heardUrl,""));
			}
		}
		cjxtPitfallReportService.save(cjxtPitfallReport);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtPitfallReport
	 * @return
	 */
	@AutoLog(value = "隐患上报-编辑")
	@ApiOperation(value="隐患上报-编辑", notes="隐患上报-编辑")
//	@RequiresPermissions("cjxt:cjxt_pitfall_report:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtPitfallReport cjxtPitfallReport) {
		SysDepart sysDepart = sysDepartService.getById(cjxtPitfallReport.getSbbm());
		if(sysDepart!=null){
			cjxtPitfallReport.setSbbmid(sysDepart.getId());
			cjxtPitfallReport.setSbbm(sysDepart.getDepartNameFull());
		}
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(!"".equals(cjxtPitfallReport.getXcsp()) && cjxtPitfallReport.getXcsp()!=null){
			if(cjxtPitfallReport.getXcsp().contains(heardUrl)){
				cjxtPitfallReport.setXcsp(cjxtPitfallReport.getXcsp().replace(heardUrl,""));
			}
		}
		if(!"".equals(cjxtPitfallReport.getXctp()) && cjxtPitfallReport.getXctp()!=null){
			if(cjxtPitfallReport.getXctp().contains(heardUrl)){
				cjxtPitfallReport.setXctp(cjxtPitfallReport.getXctp().replace(heardUrl,""));
			}
		}
		cjxtPitfallReportService.updateById(cjxtPitfallReport);
		return Result.OK("编辑成功!");
	}

	 /**
	  *  处理
	  *
	  * @param cjxtPitfallReport
	  * @return
	  */
	 @AutoLog(value = "隐患上报-处理")
	 @ApiOperation(value="隐患上报-处理", notes="隐患上报-处理")
//	@RequiresPermissions("cjxt:cjxt_pitfall_report:edit")
	 @RequestMapping(value = "/editCl", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> editCl(@RequestBody CjxtPitfallReport cjxtPitfallReport) {
 		 cjxtPitfallReport.setClzt("1");
		 String heardUrl = minioUrl+"/"+bucketName+"/";
		 if(!"".equals(cjxtPitfallReport.getXcsp()) && cjxtPitfallReport.getXcsp()!=null){
			 if(cjxtPitfallReport.getXcsp().contains(heardUrl)){
				 cjxtPitfallReport.setXcsp(cjxtPitfallReport.getXcsp().replace(heardUrl,""));
			 }
		 }
		 if(!"".equals(cjxtPitfallReport.getXctp()) && cjxtPitfallReport.getXctp()!=null){
			 if(cjxtPitfallReport.getXctp().contains(heardUrl)){
				 cjxtPitfallReport.setXctp(cjxtPitfallReport.getXctp().replace(heardUrl,""));
			 }
		 }
		 cjxtPitfallReportService.updateById(cjxtPitfallReport);
		 return Result.OK("编辑成功!");
	 }
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "隐患上报-通过id删除")
	@ApiOperation(value="隐患上报-通过id删除", notes="隐患上报-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_pitfall_report:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtPitfallReportService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "隐患上报-批量删除")
	@ApiOperation(value="隐患上报-批量删除", notes="隐患上报-批量删除")
//	@RequiresPermissions("cjxt:cjxt_pitfall_report:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtPitfallReportService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "隐患上报-通过id查询")
	@ApiOperation(value="隐患上报-通过id查询", notes="隐患上报-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtPitfallReport> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtPitfallReport cjxtPitfallReport = cjxtPitfallReportService.getById(id);
		if(cjxtPitfallReport==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtPitfallReport);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtPitfallReport
    */
//    @RequiresPermissions("cjxt:cjxt_pitfall_report:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtPitfallReport cjxtPitfallReport) {
        return super.exportXls(request, cjxtPitfallReport, CjxtPitfallReport.class, "隐患上报");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_pitfall_report:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtPitfallReport.class);
    }

}
