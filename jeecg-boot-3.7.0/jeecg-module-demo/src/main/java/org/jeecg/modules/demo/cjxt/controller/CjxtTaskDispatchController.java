package org.jeecg.modules.demo.cjxt.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
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
import org.jeecg.modules.demo.cjxt.entity.*;
import org.jeecg.modules.demo.cjxt.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.utils.log.Dg;
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
 * @Description: 任务派发表
 * @Author: jeecg-boot
 * @Date:   2024-06-14
 * @Version: V1.0
 */
@Api(tags="任务派发表")
@RestController
@RequestMapping("/cjxt/cjxtTaskDispatch")
@Slf4j
public class CjxtTaskDispatchController extends JeecgController<CjxtTaskDispatch, ICjxtTaskDispatchService> {
	@Autowired
	private ICjxtTaskDispatchService cjxtTaskDispatchService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ICjxtStandardAddressSbryService cjxtStandardAddressSbryService;
	@Autowired
	private ICjxtPjwgqxService cjxtPjwgqxService;
	@Autowired
	private ICjxtTaskService cjxtTaskService;
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
	 * @param cjxtTaskDispatch
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "任务派发表-分页列表查询")
	@ApiOperation(value="任务派发表-分页列表查询", notes="任务派发表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtTaskDispatch>> queryPageList(CjxtTaskDispatch cjxtTaskDispatch,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(required = false, name="userId") String userId,
								   @RequestParam(required = false, name="isDisp") String isDisp,
								   @RequestParam(required = false, name="taskId") String taskId,
								   @RequestParam(required = false, name="rwzt") String rwzt,
								   @RequestParam(required = false, name="search") String search,
								   @RequestParam(required = false, name="searchLd") String searchLd,
								   HttpServletRequest req) throws UnsupportedEncodingException {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if(userId!=null && rwzt!=null){
			if(!"".equals(isDisp) && isDisp!=null && "1".equals(isDisp)){
				cjxtTaskDispatch.setDispatcherId(userId);
			}else {
				cjxtTaskDispatch.setReceiverId(userId);
			}
			if("2".equals(rwzt)){
				cjxtTaskDispatch.setRwzt("2");
			}
			if("4".equals(rwzt)){
				cjxtTaskDispatch.setRwzt("4");
			}
		}
		QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		if(search!=null&&!"".equals(search)&&rwzt!=null&&!"".equals(rwzt)){
			queryWrapper.eq("rwzt",rwzt);
			queryWrapper.like("address_name",search);
		}
		if(searchLd!=null && !"".equals(searchLd)){
//			searchLd = java.net.URLDecoder.decode(searchLd,"utf8");
			searchLd = searchLd.replace(",","%");
			searchLd = "%"+searchLd+"%";
			String finalSearchLd = searchLd;
			queryWrapper.and(wrapper -> wrapper.like("address_name", finalSearchLd));
		}
		if(taskId!=null && !"".equals(taskId)){
			queryWrapper.eq("task_id",taskId);
		}else {
			if(userId==null){
				if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())){
					List<String> orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysUser.getOrgId());
					if(orgCodes.size()>0){
						queryWrapper.in("sys_org_code", orgCodes);
					}
				}else {
					//queryWrapper.eq("dispatcher_id",sysUser.getId());
					queryWrapper.eq("receiver_id",sysUser.getId());
				}
			}
		}
		if("4".equals(rwzt)){
			queryWrapper.orderByDesc("update_time");
		}
		Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		for(CjxtTaskDispatch dispatch: pageList.getRecords()){
			CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(dispatch.getAddressId());
			if(cjxtStandardAddress!=null){
				String addressName = "";
				if ("1".equals(cjxtStandardAddress.getDzType())) {
					//小区名
					if (cjxtStandardAddress.getDz1Xqm() != null && !"".equals(cjxtStandardAddress.getDz1Xqm())) {
						addressName = addressName + cjxtStandardAddress.getDz1Xqm();
					}
					//楼栋
					if (cjxtStandardAddress.getDz1Ld() != null && !"".equals(cjxtStandardAddress.getDz1Ld())) {
						addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
					}
					//单元
					if (cjxtStandardAddress.getDz1Dy() != null && !"".equals(cjxtStandardAddress.getDz1Dy())) {
						addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
					}
					//室
					if (cjxtStandardAddress.getDz1S() != null && !"".equals(cjxtStandardAddress.getDz1S())) {
						addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
					}
				} else if ("2".equals(cjxtStandardAddress.getDzType())) {
					cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
					//村名
					if (cjxtStandardAddress.getDz2Cm() != null && !"".equals(cjxtStandardAddress.getDz2Cm())) {
						addressName = addressName + cjxtStandardAddress.getDz2Cm();
					}
					//组名
					if (cjxtStandardAddress.getDz2Zm() != null && !"".equals(cjxtStandardAddress.getDz2Zm())) {
						addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
					}
					//号名
					if (cjxtStandardAddress.getDz2Hm() != null && !"".equals(cjxtStandardAddress.getDz2Hm())) {
						addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
					}

				} else if ("3".equals(cjxtStandardAddress.getDzType())) {
					cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
					//大厦名
					if (cjxtStandardAddress.getDz3Dsm() != null && !"".equals(cjxtStandardAddress.getDz3Dsm())) {
						addressName = addressName + cjxtStandardAddress.getDz3Dsm();
					}
					//楼栋名
					if (cjxtStandardAddress.getDz3Ldm() != null && !"".equals(cjxtStandardAddress.getDz3Ldm())) {
						addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
					}
					//室名
					if (cjxtStandardAddress.getDz3Sm() != null && !"".equals(cjxtStandardAddress.getDz3Sm())) {
						addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
					}
				} else if ("4".equals(cjxtStandardAddress.getDzType())) {
					if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						addressName = addressName + cjxtStandardAddress.getDetailMc();
					}
				} else if ("5".equals(cjxtStandardAddress.getDzType())) {
					if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						addressName = addressName + cjxtStandardAddress.getDetailMc();
					}
					if (cjxtStandardAddress.getDz5P() != null && !"".equals(cjxtStandardAddress.getDz5P())) {
						addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
					}
					if (cjxtStandardAddress.getDz5H() != null && !"".equals(cjxtStandardAddress.getDz5H())) {
						addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
					}
					if (cjxtStandardAddress.getDz5S() != null && !"".equals(cjxtStandardAddress.getDz5S())) {
						addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
					}

				} else if ("6".equals(cjxtStandardAddress.getDzType())) {
					if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						addressName = addressName + cjxtStandardAddress.getDetailMc();
					}
					if (cjxtStandardAddress.getDz6S() != null && !"".equals(cjxtStandardAddress.getDz6S())) {
						addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
					}
				} else if ("99".equals(cjxtStandardAddress.getDzType())) {
					if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						addressName = addressName + cjxtStandardAddress.getDetailMc();
					}
				}
				dispatch.setAddressName(addressName);
			}
		}
		return Result.OK(pageList);
	}

	 @AutoLog(value = "任务派发表-民警审核列表")
	 @ApiOperation(value="任务派发表-民警审核列表", notes="任务派发表-民警审核列表")
	 @GetMapping(value = "/appList")
	 public Result<IPage<CjxtTaskDispatch>> queryPageList(CjxtTaskDispatch cjxtTaskDispatch,
														  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														  @RequestParam(required = true, name="userId") String userId,
														  @RequestParam(required = false, name="isDisp") String isDisp,
														  @RequestParam(required = false, name="taskId") String taskId,
														  @RequestParam(required = false, name="rwzt") String rwzt,
														  @RequestParam(required = false, name="wsztApp") String wsztApp,
														  @RequestParam(required = false, name="search") String search,
														  @RequestParam(required = false, name="searchLd") String searchLd,
														  HttpServletRequest req) throws UnsupportedEncodingException {
		 SysUser sysUser = null;
		 if(userId!=null && rwzt!=null){
			 sysUser = sysUserService.getById(userId);
			 if(!"".equals(isDisp) && isDisp!=null && "1".equals(isDisp)){
				 cjxtTaskDispatch.setDispatcherId(userId);
			 }else {
				 cjxtTaskDispatch.setReceiverId(userId);
			 }
			 if("2".equals(rwzt)){
				 cjxtTaskDispatch.setRwzt("2");
			 }
			 if("4".equals(rwzt)){
				 cjxtTaskDispatch.setRwzt("4");
			 }
		 }
		 QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		 if(search!=null&&!"".equals(search)&&rwzt!=null&&!"".equals(rwzt)){
			 queryWrapper.eq("rwzt",rwzt);
			 queryWrapper.like("address_name",search);
		 }
		 if(searchLd!=null && !"".equals(searchLd)){
			 searchLd = searchLd.replace(",","%");
			 searchLd = "%"+searchLd+"%";
			 String finalSearchLd = searchLd;
			 queryWrapper.and(wrapper -> wrapper.like("address_name", finalSearchLd));
		 }
		 if(!"".equals(wsztApp)){
			 queryWrapper.isNull("wszt");
		 }
		 if(taskId!=null && !"".equals(taskId)){
			 queryWrapper.eq("task_id",taskId);
		 }else {
			 if(userId==null){
				 if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())){
					 List<String> orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysUser.getOrgId());
					 if(orgCodes.size()>0){
						 queryWrapper.in("sys_org_code", orgCodes);
					 }
				 }else {
					 //queryWrapper.eq("dispatcher_id",sysUser.getId());
					 queryWrapper.eq("receiver_id",sysUser.getId());
				 }
			 }
		 }
		 if("4".equals(rwzt)){
			 queryWrapper.orderByDesc("update_time");
		 }
		 Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		 IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		 for(CjxtTaskDispatch dispatch: pageList.getRecords()){
			 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(dispatch.getAddressId());
			 if(cjxtStandardAddress!=null){
				 String addressName = "";
				 if ("1".equals(cjxtStandardAddress.getDzType())) {
					 //小区名
					 if (cjxtStandardAddress.getDz1Xqm() != null && !"".equals(cjxtStandardAddress.getDz1Xqm())) {
						 addressName = addressName + cjxtStandardAddress.getDz1Xqm();
					 }
					 //楼栋
					 if (cjxtStandardAddress.getDz1Ld() != null && !"".equals(cjxtStandardAddress.getDz1Ld())) {
						 addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
					 }
					 //单元
					 if (cjxtStandardAddress.getDz1Dy() != null && !"".equals(cjxtStandardAddress.getDz1Dy())) {
						 addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
					 }
					 //室
					 if (cjxtStandardAddress.getDz1S() != null && !"".equals(cjxtStandardAddress.getDz1S())) {
						 addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
					 }
				 } else if ("2".equals(cjxtStandardAddress.getDzType())) {
					 cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
					 //村名
					 if (cjxtStandardAddress.getDz2Cm() != null && !"".equals(cjxtStandardAddress.getDz2Cm())) {
						 addressName = addressName + cjxtStandardAddress.getDz2Cm();
					 }
					 //组名
					 if (cjxtStandardAddress.getDz2Zm() != null && !"".equals(cjxtStandardAddress.getDz2Zm())) {
						 addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
					 }
					 //号名
					 if (cjxtStandardAddress.getDz2Hm() != null && !"".equals(cjxtStandardAddress.getDz2Hm())) {
						 addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
					 }

				 } else if ("3".equals(cjxtStandardAddress.getDzType())) {
					 cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
					 //大厦名
					 if (cjxtStandardAddress.getDz3Dsm() != null && !"".equals(cjxtStandardAddress.getDz3Dsm())) {
						 addressName = addressName + cjxtStandardAddress.getDz3Dsm();
					 }
					 //楼栋名
					 if (cjxtStandardAddress.getDz3Ldm() != null && !"".equals(cjxtStandardAddress.getDz3Ldm())) {
						 addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
					 }
					 //室名
					 if (cjxtStandardAddress.getDz3Sm() != null && !"".equals(cjxtStandardAddress.getDz3Sm())) {
						 addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
					 }
				 } else if ("4".equals(cjxtStandardAddress.getDzType())) {
					 if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 }
				 } else if ("5".equals(cjxtStandardAddress.getDzType())) {
					 if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 }
					 if (cjxtStandardAddress.getDz5P() != null && !"".equals(cjxtStandardAddress.getDz5P())) {
						 addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
					 }
					 if (cjxtStandardAddress.getDz5H() != null && !"".equals(cjxtStandardAddress.getDz5H())) {
						 addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
					 }
					 if (cjxtStandardAddress.getDz5S() != null && !"".equals(cjxtStandardAddress.getDz5S())) {
						 addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
					 }

				 } else if ("6".equals(cjxtStandardAddress.getDzType())) {
					 if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 }
					 if (cjxtStandardAddress.getDz6S() != null && !"".equals(cjxtStandardAddress.getDz6S())) {
						 addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
					 }
				 } else if ("99".equals(cjxtStandardAddress.getDzType())) {
					 if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
						 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 }
				 }
				 dispatch.setAddressName(addressName);
			 }
		 }
		 return Result.OK(pageList);
	 }

	 @ApiOperation(value="任务派发表-未完成", notes="任务派发表-未完成")
	 @GetMapping(value = "/listWwc")
	 public Result<IPage<CjxtTaskDispatch>> listWwc(CjxtTaskDispatch cjxtTaskDispatch,
														  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														  HttpServletRequest req) {
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 cjxtTaskDispatch.setRwzt("2");
		 QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		 if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())){
			 List<String> orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysUser.getOrgId());
			 if(orgCodes.size()>0){
				 queryWrapper.in("sys_org_code", orgCodes);
			 }
		 }else {
//			 queryWrapper.eq("dispatcher_id",sysUser.getId());
			 queryWrapper.eq("receiver_id",sysUser.getId());
		 }
		 Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		 IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }

	 @ApiOperation(value="任务派发表-已完成", notes="任务派发表-已完成")
	 @GetMapping(value = "/listYwc")
	 public Result<IPage<CjxtTaskDispatch>> listYwc(CjxtTaskDispatch cjxtTaskDispatch,
														  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														  HttpServletRequest req) {
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 cjxtTaskDispatch.setRwzt("4");
		 QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		 if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())){
			 List<String> orgCodes = jdbcTemplate.queryForList("" +
					 "SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysUser.getOrgId());
			 if(orgCodes.size()>0){
				 queryWrapper.in("sys_org_code", orgCodes);
			 }
		 }else {
//			 queryWrapper.eq("dispatcher_id",sysUser.getId());
			 queryWrapper.eq("receiver_id",sysUser.getId());
		 }
		 Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		 IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }

	 @ApiOperation(value="任务派发表-已延迟", notes="任务派发表-已延迟")
	 @GetMapping(value = "/listYyc")
	 public Result<IPage<CjxtTaskDispatch>> listYyc(CjxtTaskDispatch cjxtTaskDispatch,
														  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														  HttpServletRequest req) {
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		 if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())){
			 List<String> orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysUser.getOrgId());
			 if(orgCodes.size()>0){
				 queryWrapper.in("sys_org_code", orgCodes);
			 }
		 }else {
//			 queryWrapper.eq("dispatcher_id",sysUser.getId());
			 queryWrapper.eq("receiver_id",sysUser.getId());
		 }
		 queryWrapper.lt("due_date",new Date());
		 queryWrapper.ne("rwzt","1");
		 queryWrapper.ne("rwzt","4");
		 queryWrapper.isNull("wcsj");
		 Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		 IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }

	 @ApiOperation(value="任务派发表-我的任务", notes="任务派发表-我的任务")
	 @GetMapping(value = "/listWdrw")
	 public Result<IPage<CjxtTaskDispatch>> listWdrw(CjxtTaskDispatch cjxtTaskDispatch,
														@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														HttpServletRequest req) {
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		 queryWrapper.eq("receiver_id",sysUser.getId());
		 queryWrapper.ne("rwzt","1");
		 Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		 IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }


	 @ApiOperation(value="任务派发表-我的任务数量", notes="任务派发表-我的任务数量")
	 @GetMapping(value = "/listWdrwNum")
	 public Result<Integer> listWdrwNum(@RequestParam(name="userId",required=true) String userId,
									HttpServletRequest req) {
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 Integer num = 0;
		 if(userId!=null){
			 List<CjxtTaskDispatch> list = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getReceiverId,userId).eq(CjxtTaskDispatch::getRwzt,"2"));
			 if(list.size()>0){
				 num = list.size();
			 }
		 }
		 return Result.OK(num);
	 }

	 @ApiOperation(value="任务派发表-我的任务(已延迟任务)", notes="任务派发表-我的任务(已延迟任务)")
	 @GetMapping(value = "/listWdrwYyc")
	 public Result<IPage<CjxtTaskDispatch>> listWdrwYyc(CjxtTaskDispatch cjxtTaskDispatch,
													@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
													@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
													HttpServletRequest req) {
		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 QueryWrapper<CjxtTaskDispatch> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTaskDispatch, req.getParameterMap());
		 queryWrapper.eq("receiver_id",sysUser.getId());
		 queryWrapper.lt("due_date",new Date());
		 queryWrapper.ne("rwzt","1");
		 queryWrapper.ne("rwzt","4");
		 queryWrapper.isNull("wcsj");
		 Page<CjxtTaskDispatch> page = new Page<CjxtTaskDispatch>(pageNo, pageSize);
		 IPage<CjxtTaskDispatch> pageList = cjxtTaskDispatchService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }

	 @ApiOperation(value="任务派发表-任务是否存在", notes="任务派发表-任务是否存在")
	 @GetMapping(value = "/listIsRw")
	 public Result<Boolean> listIsRw(CjxtTaskDispatch cjxtTaskDispatch) {
		 Boolean isRW = false;
		 String[] mbIds = cjxtTaskDispatch.getMbId().split(",");
		 String[] addressIds = cjxtTaskDispatch.getAddressId().split(",");
		 for (int i = 0; i < mbIds.length; i++) {
			 String mbId = mbIds[i];
			 for (int j = 0; j < addressIds.length; j++) {
				 String addressId = addressIds[j];
				 //检查是否存在
				 List<CjxtTaskDispatch> taskDispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,mbId).eq(CjxtTaskDispatch::getAddressId,addressId).eq(CjxtTaskDispatch::getReceiverId,cjxtTaskDispatch.getReceiverId()));;
				 if (taskDispatchList.size()>0) {
					 isRW = true ;
					 return Result.OK(isRW);
				 }
			 }
		 }
		 return Result.OK(isRW);
	 }

	 /**
	  *   创建
	  *
	  * @param cjxtTaskDispatch
	  * @return
	  */
	 @AutoLog(value = "任务派发表-创建")
	 @ApiOperation(value="任务派发表-创建", notes="任务派发表-创建")
//	@RequiresPermissions("cjxt:cjxt_task_dispatch:add")
	 @PostMapping(value = "/cj")
	 public Result<String> cj(@RequestBody CjxtTaskDispatch cjxtTaskDispatch) {
		 String[] mbIds = cjxtTaskDispatch.getMbId().split(",");
		 String[] mbNames = cjxtTaskDispatch.getMbName().split(",");
		 String[] mbCodes = cjxtTaskDispatch.getMbCode().split(",");
		 String[] bms = cjxtTaskDispatch.getBm().split(",");
		 String[] addressIds = cjxtTaskDispatch.getAddressId().split(",");
		 String[] addressCodes = cjxtTaskDispatch.getAddressCode().split(",");
		 String[] addressNames = cjxtTaskDispatch.getAddressName().split(",");

		 for (int i = 0; i < mbIds.length; i++) {
			 String mbId = mbIds[i];
			 String mbName = mbNames[i];
			 String mbCode = mbCodes[i];
			 String bm = bms[i];
			 for (int j = 0; j < addressIds.length; j++) {
				 String addressId = addressIds[j];
				 String addressCode = addressCodes[j];
				 String addressName = addressNames[j];
//				 //检查是否存在
//				 QueryWrapper<CjxtTaskDispatch> queryWrapper = new QueryWrapper<CjxtTaskDispatch>()
//						 .eq("mb_id", mbId)
//						 .eq("address_id", addressId)
//						 .eq("receiver_id",cjxtTaskDispatch.getReceiverId());
//				 CjxtTaskDispatch taskDispatch = cjxtTaskDispatchService.getOne(queryWrapper);
//				 if (taskDispatch != null) {
//					 // 如果数据已经存在，则先删除
//					 cjxtTaskDispatchService.remove(queryWrapper);
//				 }

				 CjxtTaskDispatch newCjxtTaskDispatch = new CjxtTaskDispatch();
				 newCjxtTaskDispatch.setTaskName(cjxtTaskDispatch.getTaskName());
				 newCjxtTaskDispatch.setTaskDescription(cjxtTaskDispatch.getTaskDescription());
				 newCjxtTaskDispatch.setDispatcherId(cjxtTaskDispatch.getDispatcherId());
				 newCjxtTaskDispatch.setDispatcherName(cjxtTaskDispatch.getDispatcherName());
				 newCjxtTaskDispatch.setReceiverId(cjxtTaskDispatch.getReceiverId());
				 newCjxtTaskDispatch.setReceiverName(cjxtTaskDispatch.getReceiverName());
				 newCjxtTaskDispatch.setDueDate(cjxtTaskDispatch.getDueDate());
				 newCjxtTaskDispatch.setMbId(mbId);
				 newCjxtTaskDispatch.setMbName(mbName);
				 newCjxtTaskDispatch.setMbCode(mbCode);
				 newCjxtTaskDispatch.setBm(bm);
				 newCjxtTaskDispatch.setAddressId(addressId);
				 newCjxtTaskDispatch.setAddressCode(addressCode);
				 newCjxtTaskDispatch.setAddressName(addressName);
				 newCjxtTaskDispatch.setRwzt(cjxtTaskDispatch.getRwzt());

				 // 保存新对象
				 cjxtTaskDispatchService.save(newCjxtTaskDispatch);

				 if("2".equals(cjxtTaskDispatch.getRwzt())){
					 // 数据派发
					 if(!"".equals(addressId) && addressId!=null && mbId!=null && !"".equals(mbId)){
						 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(cjxtTaskDispatch.getAddressId());
						 CjxtMbgl cjxtMbgl = cjxtMbglService.getById(cjxtTaskDispatch.getMbId());
						 if(cjxtStandardAddress!=null&&cjxtMbgl!=null){
							 String sql = "";
							 List<Map<String, Object>> resultList = new ArrayList<>();
							 if(cjxtStandardAddress.getId()!=null && !"".equals(cjxtStandardAddress.getId()) && !cjxtStandardAddress.getId().isEmpty()){
								 sql = "SELECT id,create_time,update_time FROM " + cjxtMbgl.getBm() + " WHERE address_id = '" + cjxtStandardAddress.getId()+"'";
								 resultList = jdbcTemplate.queryForList(sql);
							 }
							 Date createTime = null;
							 Date updateTime = null;
							 if(resultList.size()>0){Map<String, Object> row = resultList.get(0);
								 Object id = row.get("id");
								 LocalDateTime create_time = (LocalDateTime) row.get("create_time");
								 LocalDateTime update_time = (LocalDateTime) row.get("update_time");
								 if(create_time != null){
									 createTime = java.sql.Timestamp.valueOf(create_time);
								 }
								 if(update_time != null){
									 updateTime = java.sql.Timestamp.valueOf(update_time);
								 }
								 newCjxtTaskDispatch.setDataId(String.valueOf(id));
								 newCjxtTaskDispatch.setHszt("2");
								 if(updateTime!=null){
									 newCjxtTaskDispatch.setSchssj(updateTime);
								 }else {
									 newCjxtTaskDispatch.setSchssj(createTime);
								 }
								 cjxtTaskDispatchService.updateById(newCjxtTaskDispatch);
							 }
						 }
					 }
				 }
			 }
		 }
		 return Result.OK("添加成功！");
	 }

	/**
	 *   添加
	 *
	 * @param cjxtTaskDispatch
	 * @return
	 */
	@AutoLog(value = "任务派发表-添加")
	@ApiOperation(value="任务派发表-添加", notes="任务派发表-添加")
//	@RequiresPermissions("cjxt:cjxt_task_dispatch:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtTaskDispatch cjxtTaskDispatch) {
		String[] mbIds = cjxtTaskDispatch.getMbId().split(",");
		String[] mbNames = cjxtTaskDispatch.getMbName().split(",");
		String[] mbCodes = cjxtTaskDispatch.getMbCode().split(",");
		String[] bms = cjxtTaskDispatch.getBm().split(",");
		String[] addressIds = cjxtTaskDispatch.getAddressId().split(",");
		String[] addressCodes = cjxtTaskDispatch.getAddressCode().split(",");
		String[] addressNames = cjxtTaskDispatch.getAddressName().split(",");

		for (int i = 0; i < mbIds.length; i++) {
			String mbId = mbIds[i];
			String mbName = mbNames[i];
			String mbCode = mbCodes[i];
			String bm = bms[i];
			for (int j = 0; j < addressIds.length; j++) {
				String addressId = addressIds[j];
				String addressCode = addressCodes[j];
				String addressName = addressNames[j];
				//检查是否存在
				CjxtTaskDispatch taskDispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,mbId)
						.eq(CjxtTaskDispatch::getAddressId,addressId).eq(CjxtTaskDispatch::getReceiverId,cjxtTaskDispatch.getReceiverId()).last("ORDER BY create_time LIMIT 1"));
				if (taskDispatch != null) {
					// 如果数据已经存在，则先删除
					cjxtTaskDispatchService.removeById(taskDispatch.getId());
				}

				CjxtTaskDispatch newCjxtTaskDispatch = new CjxtTaskDispatch();
				newCjxtTaskDispatch.setTaskName(cjxtTaskDispatch.getTaskName());
				newCjxtTaskDispatch.setTaskDescription(cjxtTaskDispatch.getTaskDescription());
				newCjxtTaskDispatch.setDispatcherId(cjxtTaskDispatch.getDispatcherId());
				newCjxtTaskDispatch.setDispatcherName(cjxtTaskDispatch.getDispatcherName());
				newCjxtTaskDispatch.setReceiverId(cjxtTaskDispatch.getReceiverId());
				newCjxtTaskDispatch.setReceiverName(cjxtTaskDispatch.getReceiverName());
				newCjxtTaskDispatch.setDueDate(cjxtTaskDispatch.getDueDate());
				newCjxtTaskDispatch.setMbId(mbId);
				newCjxtTaskDispatch.setMbName(mbName);
				newCjxtTaskDispatch.setMbCode(mbCode);
				newCjxtTaskDispatch.setBm(bm);
				newCjxtTaskDispatch.setAddressId(addressId);
				newCjxtTaskDispatch.setAddressCode(addressCode);
				newCjxtTaskDispatch.setAddressName(addressName);
				newCjxtTaskDispatch.setRwzt(cjxtTaskDispatch.getRwzt());

				// 保存新对象
				cjxtTaskDispatchService.save(newCjxtTaskDispatch);

				if("2".equals(cjxtTaskDispatch.getRwzt())){
					// 数据派发
					if(!"".equals(addressId) && addressId!=null && mbId!=null && !"".equals(mbId)){
						CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(cjxtTaskDispatch.getAddressId());
						CjxtMbgl cjxtMbgl = cjxtMbglService.getById(cjxtTaskDispatch.getMbId());
						if(cjxtStandardAddress!=null&&cjxtMbgl!=null){
							String sql = "";
							List<Map<String, Object>> resultList = new ArrayList<>();
							if(cjxtStandardAddress.getId()!=null && !"".equals(cjxtStandardAddress.getId()) && !cjxtStandardAddress.getId().isEmpty()){
								sql = "SELECT id,create_time,update_time FROM " + cjxtMbgl.getBm() + " WHERE address_id = '" + cjxtStandardAddress.getId()+"'";
								resultList = jdbcTemplate.queryForList(sql);
							}
							Date createTime = null;
							Date updateTime = null;
							if(resultList.size()>0){
								Map<String, Object> row = resultList.get(0);
								Object id = row.get("id");
								LocalDateTime create_time = (LocalDateTime) row.get("create_time");
								LocalDateTime update_time = (LocalDateTime) row.get("update_time");
								if(create_time != null){
									createTime = java.sql.Timestamp.valueOf(create_time);
								}
								if(update_time != null){
									updateTime = java.sql.Timestamp.valueOf(update_time);
								}
								newCjxtTaskDispatch.setDataId(String.valueOf(id));
								newCjxtTaskDispatch.setHszt("2");
								if(updateTime!=null){
									newCjxtTaskDispatch.setSchssj(updateTime);
								}else {
									newCjxtTaskDispatch.setSchssj(createTime);
								}
								cjxtTaskDispatchService.updateById(newCjxtTaskDispatch);
							}
						}
					}
				}
			}
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtTaskDispatch
	 * @return
	 */
	@AutoLog(value = "任务派发表-编辑")
	@ApiOperation(value="任务派发表-编辑", notes="任务派发表-编辑")
//	@RequiresPermissions("cjxt:cjxt_task_dispatch:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtTaskDispatch cjxtTaskDispatch) {
		cjxtTaskDispatchService.removeById(cjxtTaskDispatch.getId());
		String[] mbIds = cjxtTaskDispatch.getMbId().split(",");
		String[] mbNames = cjxtTaskDispatch.getMbName().split(",");
		String[] mbCodes = cjxtTaskDispatch.getMbCode().split(",");
		String[] bms = cjxtTaskDispatch.getBm().split(",");
		String[] addressIds = cjxtTaskDispatch.getAddressId().split(",");
		String[] addressCodes = cjxtTaskDispatch.getAddressCode().split(",");
		String[] addressNames = cjxtTaskDispatch.getAddressName().split(",");

		for (int i = 0; i < mbIds.length; i++) {
			String mbId = mbIds[i];
			String mbName = mbNames[i];
			String mbCode = mbCodes[i];
			String bm = bms[i];
			for (int j = 0; j < addressIds.length; j++) {
				String addressId = addressIds[j];
				String addressCode = addressCodes[j];
				String addressName = addressNames[j];
				//检查是否存在
				CjxtTaskDispatch taskDispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,mbId)
						.eq(CjxtTaskDispatch::getAddressId,addressId).eq(CjxtTaskDispatch::getReceiverId,cjxtTaskDispatch.getReceiverId()).last("ORDER BY create_time LIMIT 1"));
				if (taskDispatch != null) {
					// 如果数据已经存在，则先删除
					cjxtTaskDispatchService.removeById(taskDispatch.getId());
				}

				CjxtTaskDispatch newCjxtTaskDispatch = new CjxtTaskDispatch();
				newCjxtTaskDispatch.setTaskName(cjxtTaskDispatch.getTaskName());
				newCjxtTaskDispatch.setTaskDescription(cjxtTaskDispatch.getTaskDescription());
				newCjxtTaskDispatch.setDispatcherId(cjxtTaskDispatch.getDispatcherId());
				newCjxtTaskDispatch.setDispatcherName(cjxtTaskDispatch.getDispatcherName());
				newCjxtTaskDispatch.setReceiverId(cjxtTaskDispatch.getReceiverId());
				newCjxtTaskDispatch.setReceiverName(cjxtTaskDispatch.getReceiverName());
				newCjxtTaskDispatch.setDueDate(cjxtTaskDispatch.getDueDate());
				newCjxtTaskDispatch.setMbId(mbId);
				newCjxtTaskDispatch.setMbName(mbName);
				newCjxtTaskDispatch.setMbCode(mbCode);
				newCjxtTaskDispatch.setBm(bm);
				newCjxtTaskDispatch.setAddressId(addressId);
				newCjxtTaskDispatch.setAddressCode(addressCode);
				newCjxtTaskDispatch.setAddressName(addressName);
				newCjxtTaskDispatch.setRwzt(cjxtTaskDispatch.getRwzt());
				// 保存新对象
				cjxtTaskDispatchService.save(newCjxtTaskDispatch);

				if("2".equals(cjxtTaskDispatch.getRwzt())){
					// 数据派发
					if(!"".equals(addressId) && addressId!=null && mbId!=null && !"".equals(mbId)){
						CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(cjxtTaskDispatch.getAddressId());
						CjxtMbgl cjxtMbgl = cjxtMbglService.getById(cjxtTaskDispatch.getMbId());
						if(cjxtStandardAddress!=null&&cjxtMbgl!=null){
							String sql = "";
							List<Map<String, Object>> resultList = new ArrayList<>();
							if(cjxtStandardAddress.getId()!=null && !"".equals(cjxtStandardAddress.getId()) && !cjxtStandardAddress.getId().isEmpty()){
								sql = "SELECT id,create_time,update_time FROM " + cjxtMbgl.getBm() + " WHERE address_id = '" + cjxtStandardAddress.getId()+"'";
								resultList = jdbcTemplate.queryForList(sql);
							}
							Date createTime = null;
							Date updateTime = null;
							if(resultList.size()>0){
								Map<String, Object> row = resultList.get(0);
								Object id = row.get("id");
								LocalDateTime create_time = (LocalDateTime) row.get("create_time");
								LocalDateTime update_time = (LocalDateTime) row.get("update_time");
								if(create_time != null){
									createTime = java.sql.Timestamp.valueOf(create_time);
								}
								if(update_time != null){
									updateTime = java.sql.Timestamp.valueOf(update_time);
								}
								newCjxtTaskDispatch.setDataId(String.valueOf(id));
								newCjxtTaskDispatch.setHszt("2");
								if(updateTime!=null){
									newCjxtTaskDispatch.setSchssj(updateTime);
								}else {
									newCjxtTaskDispatch.setSchssj(createTime);
								}
								cjxtTaskDispatchService.updateById(newCjxtTaskDispatch);
							}
						}
					}
				}
			}
		}
		return Result.OK("编辑成功!");
	}

	@AutoLog("APP首页-采集统计")
	@ApiOperation(value="任务派发表-采集统计", notes="任务派发表-采集统计")
	@GetMapping(value = "/listCjtj")
	public Result<List<Map<String, Object>>> listCjtj(@RequestParam(name="userId",required=true) String userId,
													  @RequestParam(name="userSf",required=false) String userSf,
                                                      @RequestParam(name="cjSb",required=false) String cjSb,
										HttpServletRequest req) {
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> sqlist = new ArrayList<>();
		Integer fwNum = 0;
		Integer rsNum = 0;
		Integer dwNum = 0;
		Integer cjNum = 0;

		Integer dwryNum = 0;
		Integer gdNum = 0;
		Integer gdryNum = 0;
		Integer spNum = 0;
		Integer spryNum = 0;
		if(userId!=null){
			List<CjxtMbgl> cjxtMbgls = cjxtMbglService.list(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getIsDb,"1"));
			String bm = "";

			//自主上报或网格员角色
			StringBuilder zzsbQuery = new StringBuilder();
			StringBuilder orgCodeBuilder = new StringBuilder();
			StringBuilder sysDepartCode = new StringBuilder();
			StringBuilder zzsbAddressId = new StringBuilder();
			//网格员
			SysUser sysUser = null;
			List<SysDepart> sysDepartsList = new ArrayList<>();
			List<String> orgCodes = new ArrayList<>();
			List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
			//自主上报
			CjxtStandardAddressSbry addressSbry = null;
			if("1".equals(cjSb)){
				sysUser = sysUserService.getById(userId);
				sysDepartsList = sysDepartService.queryUserDeparts(userId);
			}
			if("3".equals(cjSb)){
				//自主上报
				addressSbry = cjxtStandardAddressSbryService.getById(userId);
			}
			String countSql = null ;
			if(sysUser!=null){
				sysDepartsList = sysDepartService.queryUserDeparts(userId);
				if(sysDepartsList.size()>0){
					for(int j = 0;j<sysDepartsList.size();j++){
						SysDepart sysDepart = sysDepartsList.get(j);
						if (j > 0) {
							sysDepartCode.append(",");
						}
						sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
					}
				}
				if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
					StringBuilder newSysDepartCode = new StringBuilder();
					newSysDepartCode.append("(").append(sysDepartCode).append(")");
					orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
					for (int i = 0; i < orgCodes.size(); i++) {
						if (i > 0) {
							orgCodeBuilder.append(",");
						}
						orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
					}
				}
				if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
					pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
					for(int i=0;i<pjwgqxList.size();i++){
						CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
						if (i > 0) {
							orgCodeBuilder.append(",");
						}
						orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
					}
				}
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
			}

			//自主上报
			if(addressSbry!=null && addressSbry.getPhone()!=null && !"".equals(addressSbry.getPhone())){
				zzsbQuery.append(" AND create_by = '").append(addressSbry.getPhone()).append("'");
				if(addressSbry.getAddressId()!=null && !"".equals(addressSbry.getAddressId())){
					zzsbAddressId.append(" AND address_id = '"+addressSbry.getAddressId()+"'");
				}
			}

			String orgCode = "";
			if(orgCodes.size()>0){
				orgCode = orgCodeBuilder.toString();
			} else if(pjwgqxList.size()>0){
				orgCode = orgCodeBuilder.toString();
			} else {
				if(sysDepartsList.size()>0){
					orgCode = sysDepartCode.toString();
				}
			}
			//部门信息数据
			StringBuilder orgCodeQuery = new StringBuilder();
			if(!"".equals(orgCode) && !"1".equals(sysUser.getUserSf()) && "1".equals(cjSb)){
				orgCodeQuery.append(" AND sys_org_code in (").append(orgCode).append(")");
			}
			if(!"".equals(orgCode) && "1".equals(sysUser.getUserSf()) && "1".equals(cjSb)){
				orgCodeQuery.append(" AND sys_org_code in (").append(orgCode).append(")");
			}
			for(CjxtMbgl cjxtMbgl: cjxtMbgls){
				bm = cjxtMbgl.getBm();
				if("1".equals(cjSb)){
					bm = cjxtMbgl.getBm();
				} else if("3".equals(cjSb)){
					bm = cjxtMbgl.getBm()+"_sb";
				}
				//房屋
				if("FW001".equals(cjxtMbgl.getMbbh())){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> fw = jdbcTemplate.queryForList(countSql, Integer.class);
					fwNum = fw.size()<=0 ? 0 : fw.get(0);
				}
				//人员  房屋人员
				if("RY001".equals(cjxtMbgl.getMbbh())){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> rs = jdbcTemplate.queryForList(countSql, Integer.class);
					rsNum = rs.size()<=0 ? 0 : rs.get(0);
				}
				//单位
				if("DW001".equals(cjxtMbgl.getMbbh())){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> dw = jdbcTemplate.queryForList(countSql, Integer.class);
					dwNum = dw.size()<=0 ? 0 : dw.get(0);
				}
				//单位人员
				if("RY002".equals(cjxtMbgl.getMbbh()) && "6".equals(userSf)){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> dw = jdbcTemplate.queryForList(countSql, Integer.class);
					dwryNum = dw.size()<=0 ? 0 : dw.get(0);
				}
				//工地
				if("QT001".equals(cjxtMbgl.getMbbh()) && "8".equals(userSf)){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> dw = jdbcTemplate.queryForList(countSql, Integer.class);
					gdNum = dw.size()<=0 ? 0 : dw.get(0);
				}
				//工地人员
				if("RY003".equals(cjxtMbgl.getMbbh()) && "8".equals(userSf)){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> dw = jdbcTemplate.queryForList(countSql, Integer.class);
					gdryNum = dw.size()<=0 ? 0 : dw.get(0);
				}
				//商铺
				if("QT002".equals(cjxtMbgl.getMbbh()) && "7".equals(userSf)){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> dw = jdbcTemplate.queryForList(countSql, Integer.class);
					spNum = dw.size()<=0 ? 0 : dw.get(0);
				}
				//商铺人员
				if("RY004".equals(cjxtMbgl.getMbbh()) && "7".equals(userSf)){
					countSql = "SELECT count(*) FROM " + bm + " WHERE del_flag = '0' " + zzsbQuery + zzsbAddressId + orgCodeQuery ;
					List<Integer> dw = jdbcTemplate.queryForList(countSql, Integer.class);
					spryNum = dw.size()<=0 ? 0 : dw.get(0);
				}
			}
			countSql = "SELECT count(*) FROM cjxt_score_detail WHERE del_flag = '0' " + zzsbQuery + orgCodeQuery ;
			List<Integer> cj = jdbcTemplate.queryForList(countSql, Integer.class);
			if(sysUser!=null){
				cjNum = cj.size()<=0 ? 0 : cj.get(0);

				//民警、片警自定义数字
				if("13347486611".equals(sysUser.getUsername()) || "18700231606".equals(sysUser.getUsername())){
					CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"polCustomD"));
					if(cjxtXtcs!=null){
						String[] csVal = cjxtXtcs.getCsVal().split(",");
						if(csVal.length > 0){
							fwNum = csVal.length > 0 && csVal[0] != null ? Integer.valueOf(csVal[0]) : 0;
							rsNum = csVal.length > 1 && csVal[1] != null ? Integer.valueOf(csVal[1]) : 0;
							dwNum = csVal.length > 2 && csVal[2] != null ? Integer.valueOf(csVal[2]) : 0;
							cjNum = csVal.length > 3 && csVal[3] != null ? Integer.valueOf(csVal[3]) : 0;
						}
					}
				}
				Map<String, Object> fwMap = new HashMap<>();
				fwMap.put("mbCOde", "FW001");
				fwMap.put("name", "户数");
				fwMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/hs.svg");
				fwMap.put("value", fwNum);
				sqlist.add(fwMap);

				Map<String, Object> rsMap = new HashMap<>();
				rsMap.put("mbCOde", "RY001");
				rsMap.put("name", "人数");
				rsMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/rs.svg");
				rsMap.put("value", rsNum);
				sqlist.add(rsMap);

				Map<String, Object> dwMap = new HashMap<>();
				dwMap.put("mbCOde", "DW001");
				dwMap.put("name", "单位");
				dwMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/dw.svg");
				dwMap.put("value", dwNum);
				sqlist.add(dwMap);

//				Map<String, Object> cjMap = new HashMap<>();
//				cjMap.put("name", "采集数量");
//				cjMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/cjsl.svg");
//				cjMap.put("value", cjNum);
//				sqlist.add(cjMap);
			}
			//房主
			if("4".equals(userSf)){
				Map<String, Object> sbfwMap = new HashMap<>();
				sbfwMap.put("mbCOde", "FW001");
				sbfwMap.put("name", "户数");
				sbfwMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/hs.svg");
				sbfwMap.put("value", fwNum);
				sqlist.add(sbfwMap);

				Map<String, Object> sbfwrsMap = new HashMap<>();
				sbfwrsMap.put("mbCOde", "RY001");
				sbfwrsMap.put("name", "人数");
				sbfwrsMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/rs.svg");
				sbfwrsMap.put("value", rsNum);
				sqlist.add(sbfwrsMap);
			}
			//单位
			if("6".equals(userSf)){
				Map<String, Object> sbdwMap = new HashMap<>();
				sbdwMap.put("mbCOde", "DW001");
				sbdwMap.put("name", "单位");
				sbdwMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/dw.svg");
				sbdwMap.put("value", dwNum);
				sqlist.add(sbdwMap);
				Map<String, Object> sbrsMap = new HashMap<>();
				sbrsMap.put("mbCOde", "RY002");
				sbrsMap.put("name", "人数");
				sbrsMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/rs.svg");
				sbrsMap.put("value", dwryNum);
				sqlist.add(sbrsMap);
			}
			//商铺
			if("7".equals(userSf)){
				Map<String, Object> spMap = new HashMap<>();
				spMap.put("mbCOde", "QT002");
				spMap.put("mbCOde", "SP");
				spMap.put("name", "商铺");
				spMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/sp.svg");
				spMap.put("value", spNum);
				sqlist.add(spMap);
				Map<String, Object> sprsMap = new HashMap<>();
				sprsMap.put("mbCOde", "RY004");
				sprsMap.put("name", "人数");
				sprsMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/rs.svg");
				sprsMap.put("value", spryNum);
				sqlist.add(sprsMap);
			}
			//工地
			if("8".equals(userSf)){
				Map<String, Object> gdMap = new HashMap<>();
				gdMap.put("mbCOde", "QT001");
				gdMap.put("name", "工地");
				gdMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/zjgd.svg");
				gdMap.put("value", gdNum);
				sqlist.add(gdMap);
				Map<String, Object> gdryMap = new HashMap<>();
				gdryMap.put("mbCOde", "RY003");
				gdryMap.put("name", "人数");
				gdryMap.put("icon",minioUrl+"/"+bucketName+"/static/icon/rs.svg");
				gdryMap.put("value", gdryNum);
				sqlist.add(gdryMap);
			}
		}
		return Result.OK(sqlist);
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务派发表-通过id删除")
	@ApiOperation(value="任务派发表-通过id删除", notes="任务派发表-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_task_dispatch:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtTaskDispatchService.removeById(id);
		return Result.OK("删除成功!");
	}

	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "任务派发表-批量删除")
	@ApiOperation(value="任务派发表-批量删除", notes="任务派发表-批量删除")
//	@RequiresPermissions("cjxt:cjxt_task_dispatch:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtTaskDispatchService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	 /**
	  *   通过id派发
	  *
	  * @param id
	  * @return
	  */
	 @AutoLog(value = "任务派发表-派发")
	 @ApiOperation(value="任务派发表-派发", notes="任务派发表-派发")
	 @PostMapping(value = "/pf")
	 public Result<String> pf(@RequestParam(name="id",required=true) String id) {
		 CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getById(id);
		 if(cjxtTaskDispatch.getAddressId()!=null && cjxtTaskDispatch.getMbId()!=null){
			 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(cjxtTaskDispatch.getAddressId());
			 CjxtMbgl cjxtMbgl = cjxtMbglService.getById(cjxtTaskDispatch.getMbId());
			 if(cjxtStandardAddress!=null&&cjxtMbgl!=null){
				 String sql = "";
				 List<Map<String, Object>> resultList = new ArrayList<>();
				 if(cjxtStandardAddress.getId()!=null && !"".equals(cjxtStandardAddress.getId()) && !cjxtStandardAddress.getId().isEmpty()){
					 sql = "SELECT create_time,update_time FROM " + cjxtMbgl.getBm() + " WHERE address_id = '" + cjxtStandardAddress.getId()+"'";
					 resultList = jdbcTemplate.queryForList(sql);
				 }
				 Date createTime = null;
				 Date updateTime = null;
				 if(resultList.size()>0){
					 Map<String, Object> row = resultList.get(0);
					 LocalDateTime create_time = (LocalDateTime) row.get("create_time");
					 LocalDateTime update_time = (LocalDateTime) row.get("update_time");
					 if(create_time != null){
						 createTime = java.sql.Timestamp.valueOf(create_time);
					 }
					 if(update_time != null){
						 updateTime = java.sql.Timestamp.valueOf(update_time);
					 }
					 cjxtTaskDispatch.setHszt("2");
					 if(updateTime!=null){
						 cjxtTaskDispatch.setSchssj(updateTime);
					 }else {
						 cjxtTaskDispatch.setSchssj(createTime);
					 }
					 cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
				 }
			 }
		 }
		 if(cjxtTaskDispatch!=null){
			 cjxtTaskDispatch.setRwzt("2");
			 cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
			 return Result.OK("派发成功!");
		 }
		 return Result.error("派发失败,数据有误!");
	 }

	 /**
	  *  批量派发
	  *
	  * @param ids
	  * @return
	  */
	 @AutoLog(value = "任务派发表-批量派发")
	 @ApiOperation(value="任务派发表-批量派发", notes="任务派发表-批量派发")
	 @PostMapping(value = "/pfBatch")
	 public Result<String> pfBatch(@RequestParam(name="ids",required=true) String ids) {
		 String[] id = ids.split(",");
		 boolean pfzt = true;
		 for(int i = 0;i<id.length;i++){
			 String ID = id[i];
			 CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getById(ID);
			 if(cjxtTaskDispatch.getAddressId()!=null && cjxtTaskDispatch.getMbId()!=null){
				 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(cjxtTaskDispatch.getAddressId());
				 CjxtMbgl cjxtMbgl = cjxtMbglService.getById(cjxtTaskDispatch.getMbId());
				 if(cjxtStandardAddress!=null&&cjxtMbgl!=null){
					 String sql = "";
					 List<Map<String, Object>> resultList = new ArrayList<>();
					 if(cjxtStandardAddress.getId()!=null && !"".equals(cjxtStandardAddress.getId()) && !cjxtStandardAddress.getId().isEmpty()){
						 sql = "SELECT create_time,update_time FROM " + cjxtMbgl.getBm() + " WHERE address_id = '" + cjxtStandardAddress.getId()+"'";
						 resultList = jdbcTemplate.queryForList(sql);
					 }
					 Date createTime = null;
					 Date updateTime = null;
					 if(resultList.size()>0){
						 Map<String, Object> row = resultList.get(0);
						 LocalDateTime create_time = (LocalDateTime) row.get("create_time");
						 LocalDateTime update_time = (LocalDateTime) row.get("update_time");
						 if(create_time != null){
							 createTime = java.sql.Timestamp.valueOf(create_time);
						 }
						 if(update_time != null){
							 updateTime = java.sql.Timestamp.valueOf(update_time);
						 }
						 cjxtTaskDispatch.setHszt("2");
						 if(updateTime!=null){
							 cjxtTaskDispatch.setSchssj(updateTime);
						 }else {
							 cjxtTaskDispatch.setSchssj(createTime);
						 }
						 cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
					 }
				 }
			 }
			 if(cjxtTaskDispatch!=null){
				 cjxtTaskDispatch.setRwzt("2");
				 cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
				 pfzt = true;
			 }else {
				 pfzt = false;
			 }
		 }
		 if(pfzt==true){
			 return Result.OK("批量派发成功!");
		 }else {
			 return Result.error("部分派发成功，派发状态未改变数据有误!");
		 }
	 }
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "任务派发表-通过id查询")
	@ApiOperation(value="任务派发表-通过id查询", notes="任务派发表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtTaskDispatch> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getById(id);
		if(cjxtTaskDispatch==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtTaskDispatch);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtTaskDispatch
    */
//    @RequiresPermissions("cjxt:cjxt_task_dispatch:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtTaskDispatch cjxtTaskDispatch) {
        return super.exportXls(request, cjxtTaskDispatch, CjxtTaskDispatch.class, "任务派发表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_task_dispatch:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtTaskDispatch.class);
    }

}

