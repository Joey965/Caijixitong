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
import org.jeecg.modules.demo.cjxt.entity.CjxtPjwgqx;
import org.jeecg.modules.demo.cjxt.service.ICjxtPjwgqxService;

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
 * @Description: 片警网格权限
 * @Author: jeecg-boot
 * @Date:   2024-07-22
 * @Version: V1.0
 */
@Api(tags="片警网格权限")
@RestController
@RequestMapping("/cjxt/cjxtPjwgqx")
@Slf4j
public class CjxtPjwgqxController extends JeecgController<CjxtPjwgqx, ICjxtPjwgqxService> {
	@Autowired
	private ICjxtPjwgqxService cjxtPjwgqxService;
	@Autowired
	private ISysDepartService sysDepartService;

	/**
	 * 分页列表查询
	 *
	 * @param cjxtPjwgqx
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "片警网格权限-分页列表查询")
	@ApiOperation(value="片警网格权限-分页列表查询", notes="片警网格权限-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtPjwgqx>> queryPageList(CjxtPjwgqx cjxtPjwgqx,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtPjwgqx> queryWrapper = QueryGenerator.initQueryWrapper(cjxtPjwgqx, req.getParameterMap());
		Page<CjxtPjwgqx> page = new Page<CjxtPjwgqx>(pageNo, pageSize);
		IPage<CjxtPjwgqx> pageList = cjxtPjwgqxService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param cjxtPjwgqx
	 * @return
	 */
	@AutoLog(value = "片警网格权限-添加")
	@ApiOperation(value="片警网格权限-添加", notes="片警网格权限-添加")
//	@RequiresPermissions("cjxt:cjxt_pjwgqx:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtPjwgqx cjxtPjwgqx) {
		String[] wgIds = cjxtPjwgqx.getWgId().split(",");
		for(int i = 0;i< wgIds.length;i++){
			String wgId = wgIds[i];
			SysDepart sysDepart = sysDepartService.getById(wgId);
			CjxtPjwgqx pjwgqx = cjxtPjwgqxService.getOne(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,cjxtPjwgqx.getPjId()).eq(CjxtPjwgqx::getWgId,wgId));
			if(pjwgqx!=null){
				continue;
			}
			CjxtPjwgqx newCjxtPjwgqx =new CjxtPjwgqx();
			newCjxtPjwgqx.setWgId(wgId);
//			if("9".equals(sysDepart.getOrgCategory())){
//				//如果选中网格拼接社区信息
////				SysDepart departSj = sysDepartService.getById(sysDepart.getParentId());
//				newCjxtPjwgqx.setWgName(sysDepart.getDepartNameFull());
//			}else {
//				newCjxtPjwgqx.setWgName(sysDepart.getDepartName());
//			}
			newCjxtPjwgqx.setWgName(sysDepart.getDepartNameFull());
			newCjxtPjwgqx.setWgCode(sysDepart.getOrgCode());
			newCjxtPjwgqx.setPjId(cjxtPjwgqx.getPjId());
			newCjxtPjwgqx.setPjName(cjxtPjwgqx.getPjName());
			newCjxtPjwgqx.setPjZh(cjxtPjwgqx.getPjZh());
			newCjxtPjwgqx.setPjDepart(cjxtPjwgqx.getPjDepart());
			newCjxtPjwgqx.setPjLxdh(cjxtPjwgqx.getPjLxdh());
			cjxtPjwgqxService.save(newCjxtPjwgqx);
		}
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtPjwgqx
	 * @return
	 */
	@AutoLog(value = "片警网格权限-编辑")
	@ApiOperation(value="片警网格权限-编辑", notes="片警网格权限-编辑")
//	@RequiresPermissions("cjxt:cjxt_pjwgqx:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtPjwgqx cjxtPjwgqx) {
		String[] wgIds = cjxtPjwgqx.getWgId().split(",");
		if(wgIds.length>1){
			return Result.error("编辑信息部门不可多选!!!");
		}
		for(int i = 0;i< wgIds.length;i++){
			String wgId = wgIds[i];
			SysDepart sysDepart = sysDepartService.getById(wgId);
			CjxtPjwgqx pjwgqx = cjxtPjwgqxService.getOne(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,cjxtPjwgqx.getPjId()).eq(CjxtPjwgqx::getWgId,wgId));
			if(pjwgqx!=null){
				continue;
			}
			cjxtPjwgqx.setWgId(wgId);
//			if("9".equals(sysDepart.getOrgCategory())){
//				//如果选中网格拼接社区信息
//				SysDepart departSj = sysDepartService.getById(sysDepart.getParentId());
//				cjxtPjwgqx.setWgName(departSj.getDepartName()+"-"+sysDepart.getDepartName());
//			}else {
//				cjxtPjwgqx.setWgName(sysDepart.getDepartName());
//			}
			cjxtPjwgqx.setWgName(sysDepart.getDepartNameFull());
			cjxtPjwgqx.setWgCode(sysDepart.getOrgCode());
		}
		cjxtPjwgqxService.updateById(cjxtPjwgqx);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "片警网格权限-通过id删除")
	@ApiOperation(value="片警网格权限-通过id删除", notes="片警网格权限-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_pjwgqx:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtPjwgqxService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "片警网格权限-批量删除")
	@ApiOperation(value="片警网格权限-批量删除", notes="片警网格权限-批量删除")
//	@RequiresPermissions("cjxt:cjxt_pjwgqx:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtPjwgqxService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "片警网格权限-通过id查询")
	@ApiOperation(value="片警网格权限-通过id查询", notes="片警网格权限-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtPjwgqx> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtPjwgqx cjxtPjwgqx = cjxtPjwgqxService.getById(id);
		if(cjxtPjwgqx==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtPjwgqx);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtPjwgqx
    */
//    @RequiresPermissions("cjxt:cjxt_pjwgqx:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtPjwgqx cjxtPjwgqx) {
        return super.exportXls(request, cjxtPjwgqx, CjxtPjwgqx.class, "片警网格权限");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_pjwgqx:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtPjwgqx.class);
    }

}
