package org.jeecg.modules.demo.cjxt.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtBmfzr;
import org.jeecg.modules.demo.cjxt.service.ICjxtBmfzrService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

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
 * @Description: 部门负责人
 * @Author: jeecg-boot
 * @Date:   2024-07-03
 * @Version: V1.0
 */
@Api(tags="部门负责人")
@RestController
@RequestMapping("/cjxt/cjxtBmfzr")
@Slf4j
public class CjxtBmfzrController extends JeecgController<CjxtBmfzr, ICjxtBmfzrService> {
	@Autowired
	private ICjxtBmfzrService cjxtBmfzrService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDepartService sysDepartService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtBmfzr
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "部门负责人-分页列表查询")
	@ApiOperation(value="部门负责人-分页列表查询", notes="部门负责人-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtBmfzr>> queryPageList(CjxtBmfzr cjxtBmfzr,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtBmfzr> queryWrapper = QueryGenerator.initQueryWrapper(cjxtBmfzr, req.getParameterMap());
		Page<CjxtBmfzr> page = new Page<CjxtBmfzr>(pageNo, pageSize);
		IPage<CjxtBmfzr> pageList = cjxtBmfzrService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtBmfzr
	 * @return
	 */
	@AutoLog(value = "部门负责人-添加")
	@ApiOperation(value="部门负责人-添加", notes="部门负责人-添加")
//	@RequiresPermissions("cjxt:cjxt_bmfzr:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtBmfzr cjxtBmfzr) {
//		CjxtBmfzr bmfzr = cjxtBmfzrService.getOne(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getFzryId,cjxtBmfzr.getFzryId()));
//		if(bmfzr!=null){
//			return Result.error("当前人员已存在!");
//		}
		SysDepart sysDepart = sysDepartService.getById(cjxtBmfzr.getBmid());
		if(sysDepart==null){
			return Result.error("当前选择部门不存在");
		}
		cjxtBmfzr.setBmid(sysDepart.getId());
		cjxtBmfzr.setBmmc(sysDepart.getDepartName());
		cjxtBmfzrService.save(cjxtBmfzr);
		List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getFzryId,cjxtBmfzr.getFzryId()));
		SysUser sysUser = sysUserService.getById(cjxtBmfzr.getFzryId());
		if(sysUser!=null && bmfzrList!=null){
			String departIds = "";
			for(CjxtBmfzr bmfzrData: bmfzrList){
				departIds += bmfzrData.getBmid() + ",";
			}
			if(departIds != null && departIds.length()>0){
				sysUser.setDepartIds(departIds.substring(0,departIds.length()-1));
			}
			sysUserService.updateById(sysUser);
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtBmfzr
	 * @return
	 */
	@AutoLog(value = "部门负责人-编辑")
	@ApiOperation(value="部门负责人-编辑", notes="部门负责人-编辑")
//	@RequiresPermissions("cjxt:cjxt_bmfzr:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtBmfzr cjxtBmfzr) {
//		if(!cjxtBmfzr.getFzryIdDto().equals(cjxtBmfzr.getFzryId())){
//			CjxtBmfzr bmfzr = cjxtBmfzrService.getOne(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getFzryId,cjxtBmfzr.getFzryId()));
//			if(bmfzr!=null){
//				return Result.error("当前人员已存在!");
//			}
//		}
		SysDepart sysDepart = sysDepartService.getById(cjxtBmfzr.getBmid());
		if(sysDepart==null){
			return Result.error("当前选择部门不存在");
		}
		cjxtBmfzr.setBmid(sysDepart.getId());
		cjxtBmfzr.setBmmc(sysDepart.getDepartName());
		cjxtBmfzrService.updateById(cjxtBmfzr);
		List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getFzryId,cjxtBmfzr.getFzryId()));
		SysUser sysUser = sysUserService.getById(cjxtBmfzr.getFzryId());
		if(sysUser!=null && bmfzrList!=null){
			String departIds = "";
			for(CjxtBmfzr bmfzrData: bmfzrList){
				departIds += bmfzrData.getBmid() + ",";
			}
			if(departIds != null && departIds.length()>0){
				sysUser.setDepartIds(departIds.substring(0,departIds.length()-1));
			}
			sysUserService.updateById(sysUser);
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "部门负责人-通过id删除")
	@ApiOperation(value="部门负责人-通过id删除", notes="部门负责人-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_bmfzr:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		CjxtBmfzr cjxtBmfzr = cjxtBmfzrService.getById(id);
		boolean isD = false;
		if (cjxtBmfzr != null) {
			SysDepart sysDepart = sysDepartService.getById(cjxtBmfzr.getBmid());
			SysUser sysUser = sysUserService.getById(cjxtBmfzr.getFzryId());
			String departIds = sysUser.getDepartIds();
			String depIds = sysDepart.getId()+",";
			String depID = sysDepart.getId();
			if(departIds.contains(depIds)){
				isD = true;
				departIds = departIds.replaceAll(depIds,"");
			}
			if(departIds.contains(depID) && isD==false){
				departIds = departIds.replaceAll(depID,"");
			}
			sysUser.setDepartIds(departIds);
			sysUserService.updateById(sysUser);
		}
		cjxtBmfzrService.removeById(id);
		return Result.OK("删除成功!");
	}

	 public String getNextChar(String str, String ch) {
		 int index = str.indexOf(ch);
		 if (index != -1 && index < str.length() - 1) {
			 return String.valueOf(str.charAt(index + 1));
		 }
		 return null; // 如果指定字符是最后一个字符或不存在，返回null
	 }
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "部门负责人-批量删除")
	@ApiOperation(value="部门负责人-批量删除", notes="部门负责人-批量删除")
//	@RequiresPermissions("cjxt:cjxt_bmfzr:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtBmfzrService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "部门负责人-通过id查询")
	@ApiOperation(value="部门负责人-通过id查询", notes="部门负责人-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtBmfzr> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtBmfzr cjxtBmfzr = cjxtBmfzrService.getById(id);
		if(cjxtBmfzr==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtBmfzr);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtBmfzr
    */
//    @RequiresPermissions("cjxt:cjxt_bmfzr:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtBmfzr cjxtBmfzr) {
        return super.exportXls(request, cjxtBmfzr, CjxtBmfzr.class, "部门负责人");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_bmfzr:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtBmfzr.class);
    }

}
