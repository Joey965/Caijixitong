package org.jeecg.modules.demo.cjxt.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtBmData;
import org.jeecg.modules.demo.cjxt.entity.CjxtLbt;
import org.jeecg.modules.demo.cjxt.service.ICjxtBmDataService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
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
 * @Description: 部门数据权限
 * @Author: jeecg-boot
 * @Date:   2024-06-13
 * @Version: V1.0
 */
@Api(tags="部门数据权限")
@RestController
@RequestMapping("/cjxt/cjxtBmData")
@Slf4j
public class CjxtBmDataController extends JeecgController<CjxtBmData, ICjxtBmDataService> {
	@Autowired
	private ICjxtBmDataService cjxtBmDataService;
	@Autowired
	private ISysDepartService sysDepartService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtBmData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "部门数据权限-分页列表查询")
	@ApiOperation(value="部门数据权限-分页列表查询", notes="部门数据权限-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtBmData>> queryPageList(CjxtBmData cjxtBmData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtBmData> queryWrapper = QueryGenerator.initQueryWrapper(cjxtBmData, req.getParameterMap());
		Page<CjxtBmData> page = new Page<CjxtBmData>(pageNo, pageSize);
		IPage<CjxtBmData> pageList = cjxtBmDataService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtBmData
	 * @return
	 */
	@AutoLog(value = "部门数据权限-添加")
	@ApiOperation(value="部门数据权限-添加", notes="部门数据权限-添加")
	@RequiresPermissions("cjxt:cjxt_bm_data:add")
	@PostMapping(value = "/add")
	public Result<CjxtBmData> add(@RequestBody CjxtBmData cjxtBmData) {
		Result<CjxtBmData> result = new Result<CjxtBmData>();
		try {
			if(cjxtBmData.getOrgId()!=null){
				String[] dataOrgIds = cjxtBmData.getDataOrgId().split(",");
				SysDepart sysDepart = sysDepartService.getById(cjxtBmData.getOrgId());
				String departName = null ;
				String departCode = null;
				if (sysDepart!=null){
					departName = sysDepart.getDepartName();
					departCode = sysDepart.getOrgCode();
				}else {
					return result.error500("部门信息数据有误");
				}
				for(int i=0;i< dataOrgIds.length;i++){
					String dataOrgId = dataOrgIds[i];
					SysDepart sysDepartOrg = sysDepartService.getById(dataOrgId);
					String dataOrgName = null;
					String dataOrgCode = null;
					if(sysDepartOrg!=null){
						dataOrgName = sysDepartOrg.getDepartName();
						dataOrgCode = sysDepartOrg.getOrgCode();
					}
					QueryWrapper<CjxtBmData> queryWrapper = new QueryWrapper<CjxtBmData>()
							.eq("org_id", cjxtBmData.getOrgId())
							.eq("data_org_id", dataOrgId);
					CjxtBmData cjxtBmDataOne = cjxtBmDataService.getOne(queryWrapper);
					if(cjxtBmDataOne!=null){
						cjxtBmDataService.removeById(cjxtBmDataOne.getId());
					}
					CjxtBmData newBmData = new CjxtBmData();
					newBmData.setOrgId(cjxtBmData.getOrgId());
					newBmData.setOrgName(departName);
					newBmData.setOrgCode(departCode);
					newBmData.setDataOrgId(dataOrgId);
					newBmData.setDataOrgCode(dataOrgCode);
					newBmData.setDataOrgName(dataOrgName);
					cjxtBmDataService.save(newBmData);
					result.success("保存成功！");
				}
			}else {
				result.error500("部门信息数据有误");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtBmData
	 * @return
	 */
	@AutoLog(value = "部门数据权限-编辑")
	@ApiOperation(value="部门数据权限-编辑", notes="部门数据权限-编辑")
	@RequiresPermissions("cjxt:cjxt_bm_data:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<CjxtBmData> edit(@RequestBody CjxtBmData cjxtBmData) {
		Result<CjxtBmData> result = new Result<CjxtBmData>();
		CjxtBmData cjxtbmdata = cjxtBmDataService.getById(cjxtBmData.getId());
		if(cjxtbmdata==null) {
			result.error500("未找到对应实体");
		}else {
			cjxtBmDataService.removeById(cjxtBmData.getId());
			String[] dataOrgIds = cjxtBmData.getDataOrgId().split(",");
			SysDepart sysDepart = sysDepartService.getById(cjxtBmData.getOrgId());
			String departName = null ;
			if (sysDepart!=null){
				departName = sysDepart.getDepartName();
			}else {
				return result.error500("部门信息数据有误");
			}
			for(int i=0;i< dataOrgIds.length;i++){
				String dataOrgId = dataOrgIds[i];
				SysDepart sysDepartOrg = sysDepartService.getById(dataOrgId);
				String dataOrgName = null;
				if(sysDepartOrg!=null){
					dataOrgName = sysDepartOrg.getDepartName();
				}
				QueryWrapper<CjxtBmData> queryWrapper = new QueryWrapper<CjxtBmData>()
						.eq("org_id", cjxtBmData.getOrgId())
						.eq("data_org_id", dataOrgId);
				CjxtBmData cjxtBmDataOne = cjxtBmDataService.getOne(queryWrapper);
				if(cjxtBmDataOne!=null){
					cjxtBmDataService.removeById(cjxtBmDataOne.getId());
				}
				CjxtBmData newBmData = new CjxtBmData();
				newBmData.setOrgId(cjxtBmData.getOrgId());
				newBmData.setOrgName(departName);
				newBmData.setDataOrgId(dataOrgId);
				newBmData.setDataOrgName(dataOrgName);
				cjxtBmDataService.save(newBmData);
				result.success("编辑成功！");
			}
		}
		return result;
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "部门数据权限-通过id删除")
	@ApiOperation(value="部门数据权限-通过id删除", notes="部门数据权限-通过id删除")
	@RequiresPermissions("cjxt:cjxt_bm_data:delete")
	@DeleteMapping(value = "/delete")
	public Result<CjxtBmData> delete(@RequestParam(name="id",required=true) String id) {
		Result<CjxtBmData> result = new Result<CjxtBmData>();
		boolean ok = cjxtBmDataService.removeById(id);
		if(ok) {
			result.success("删除成功!");
		}else{
			result.error500("删除失败!");
		}
		return result;
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "部门数据权限-批量删除")
	@ApiOperation(value="部门数据权限-批量删除", notes="部门数据权限-批量删除")
	@RequiresPermissions("cjxt:cjxt_bm_data:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<CjxtBmData> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<CjxtBmData> result = new Result<CjxtBmData>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("参数不识别！");
		}else {
			cjxtBmDataService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "部门数据权限-通过id查询")
	@ApiOperation(value="部门数据权限-通过id查询", notes="部门数据权限-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtBmData> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtBmData cjxtBmData = cjxtBmDataService.getById(id);
		if(cjxtBmData==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtBmData);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtBmData
    */
    @RequiresPermissions("cjxt:cjxt_bm_data:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtBmData cjxtBmData) {
        return super.exportXls(request, cjxtBmData, CjxtBmData.class, "部门数据权限");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cjxt:cjxt_bm_data:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtBmData.class);
    }

}
