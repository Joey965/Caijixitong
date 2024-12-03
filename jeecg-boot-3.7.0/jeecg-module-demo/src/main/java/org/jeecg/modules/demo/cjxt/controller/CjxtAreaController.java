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
import org.jeecg.modules.demo.cjxt.entity.CjxtArea;
import org.jeecg.modules.demo.cjxt.service.ICjxtAreaService;

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
 * @Description: cjxt_area
 * @Author: jeecg-boot
 * @Date:   2024-06-13
 * @Version: V1.0
 */
@Api(tags="cjxt_area")
@RestController
@RequestMapping("/cjxt/cjxtArea")
@Slf4j
public class CjxtAreaController extends JeecgController<CjxtArea, ICjxtAreaService> {
	@Autowired
	private ICjxtAreaService cjxtAreaService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtArea
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "cjxt_area-分页列表查询")
	@ApiOperation(value="cjxt_area-分页列表查询", notes="cjxt_area-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtArea>> queryPageList(CjxtArea cjxtArea,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtArea> queryWrapper = QueryGenerator.initQueryWrapper(cjxtArea, req.getParameterMap());
		Page<CjxtArea> page = new Page<CjxtArea>(pageNo, pageSize);
		IPage<CjxtArea> pageList = cjxtAreaService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtArea
	 * @return
	 */
	@AutoLog(value = "cjxt_area-添加")
	@ApiOperation(value="cjxt_area-添加", notes="cjxt_area-添加")
	@RequiresPermissions("cjxt:cjxt_area:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtArea cjxtArea) {
		CjxtArea areaCode = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaCode,cjxtArea.getAreaCode()));
		CjxtArea areaName = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtArea.getAreaName()));
		if(areaCode!=null){
			return Result.error("编码已存在！");
		}
		if(areaName!=null){
			return Result.error("名称已存在！");
		}
		cjxtAreaService.save(cjxtArea);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtArea
	 * @return
	 */
	@AutoLog(value = "cjxt_area-编辑")
	@ApiOperation(value="cjxt_area-编辑", notes="cjxt_area-编辑")
	@RequiresPermissions("cjxt:cjxt_area:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtArea cjxtArea) {
		cjxtAreaService.updateById(cjxtArea);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "cjxt_area-通过id删除")
	@ApiOperation(value="cjxt_area-通过id删除", notes="cjxt_area-通过id删除")
	@RequiresPermissions("cjxt:cjxt_area:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtAreaService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "cjxt_area-批量删除")
	@ApiOperation(value="cjxt_area-批量删除", notes="cjxt_area-批量删除")
	@RequiresPermissions("cjxt:cjxt_area:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtAreaService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "cjxt_area-通过id查询")
	@ApiOperation(value="cjxt_area-通过id查询", notes="cjxt_area-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtArea> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtArea cjxtArea = cjxtAreaService.getById(id);
		if(cjxtArea==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtArea);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtArea
    */
    @RequiresPermissions("cjxt:cjxt_area:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtArea cjxtArea) {
        return super.exportXls(request, cjxtArea, CjxtArea.class, "cjxt_area");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cjxt:cjxt_area:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtArea.class);
    }

}
