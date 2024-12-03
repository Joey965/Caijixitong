package org.jeecg.modules.demo.cjxt.controller;

import java.util.Arrays;
import java.util.HashMap;
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
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtGtzd;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import org.jeecg.modules.demo.cjxt.service.ICjxtGtzdService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.service.ICjxtMbglPzService;
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
 * @Description: 共通字段配置
 * @Author: jeecg-boot
 * @Date:   2024-06-06
 * @Version: V1.0
 */
@Api(tags="共通字段配置")
@RestController
@RequestMapping("/cjxt/cjxtGtzd")
@Slf4j
public class CjxtGtzdController extends JeecgController<CjxtGtzd, ICjxtGtzdService> {
	@Autowired
	private ICjxtGtzdService cjxtGtzdService;
	@Autowired
	private ICjxtMbglPzService cjxtMbglPzService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtGtzd
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "共通字段配置-分页列表查询")
	@ApiOperation(value="共通字段配置-分页列表查询", notes="共通字段配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtGtzd>> queryPageList(CjxtGtzd cjxtGtzd,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtGtzd> queryWrapper = QueryGenerator.initQueryWrapper(cjxtGtzd, parameterMap);
		queryWrapper.orderByAsc("zdnum");
		Page<CjxtGtzd> page = new Page<CjxtGtzd>(pageNo, pageSize);
		IPage<CjxtGtzd> pageList = cjxtGtzdService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtGtzd
	 * @return
	 */
	@AutoLog(value = "共通字段配置-添加")
	@ApiOperation(value="共通字段配置-添加", notes="共通字段配置-添加")
//	@RequiresPermissions("cjxt:cjxt_gtzd:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtGtzd cjxtGtzd) {
		CjxtGtzd gtzd = cjxtGtzdService.getOne(new LambdaQueryWrapper<CjxtGtzd>().eq(CjxtGtzd::getZdname,cjxtGtzd.getZdname()));
		if(gtzd!=null){
			return Result.error("当前字段已存在！");
		}
		cjxtGtzdService.save(cjxtGtzd);

		if("2".equals(cjxtGtzd.getYmsx())){
			CjxtMbglPz cjxtMbglPz = new CjxtMbglPz();
			cjxtMbglPz.setId(cjxtGtzd.getId());
			cjxtMbglPz.setMbglId("1");
			cjxtMbglPz.setDbFieldName(cjxtGtzd.getZdname());
			cjxtMbglPz.setDbFieldTxt(cjxtGtzd.getZdbz());
			cjxtMbglPz.setDbLength(cjxtGtzd.getZdcd());
			cjxtMbglPz.setDbDefaultVal(cjxtGtzd.getMrz());
			cjxtMbglPz.setDbType(cjxtGtzd.getZdlx());
			cjxtMbglPz.setFieldShowType(cjxtGtzd.getKjlx());
			cjxtMbglPz.setDbIsKey(cjxtGtzd.getIsKey());
			cjxtMbglPz.setYmsx(cjxtGtzd.getYmsx());
			cjxtMbglPz.setIsQuery("1");
			cjxtMbglPz.setIsfather("0");
			cjxtMbglPz.setMbglMbbh("1");
			cjxtMbglPzService.save(cjxtMbglPz);
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtGtzd
	 * @return
	 */
	@AutoLog(value = "共通字段配置-编辑")
	@ApiOperation(value="共通字段配置-编辑", notes="共通字段配置-编辑")
//	@RequiresPermissions("cjxt:cjxt_gtzd:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtGtzd cjxtGtzd) {
		if(!cjxtGtzd.getZdnameDto().equals(cjxtGtzd.getZdname())){
			CjxtGtzd gtzd = cjxtGtzdService.getOne(new LambdaQueryWrapper<CjxtGtzd>().eq(CjxtGtzd::getZdname,cjxtGtzd.getZdname()));
			if(gtzd!=null){
				return Result.error("当前字段已存在！");
			}
		}
		if("2".equals(cjxtGtzd.getYmsx())){
			CjxtMbglPz cjxtMbglPz = cjxtMbglPzService.getOne(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getDbFieldName,cjxtGtzd.getZdname()).eq(CjxtMbglPz::getMbglId,"1").last("LIMIT 1"));
			if(cjxtMbglPz!=null){
				cjxtMbglPz.setDbFieldName(cjxtGtzd.getZdname());
				cjxtMbglPz.setDbFieldTxt(cjxtGtzd.getZdbz());
				cjxtMbglPz.setDbLength(cjxtGtzd.getZdcd());
				cjxtMbglPz.setDbDefaultVal(cjxtGtzd.getMrz());
				cjxtMbglPz.setDbType(cjxtGtzd.getZdlx());
				cjxtMbglPz.setFieldShowType(cjxtGtzd.getKjlx());
				cjxtMbglPz.setDbIsKey(cjxtGtzd.getIsKey());
				cjxtMbglPz.setYmsx(cjxtGtzd.getYmsx());
				cjxtMbglPzService.updateById(cjxtMbglPz);
			} else {
				CjxtMbglPz mbglPz = new CjxtMbglPz();
				mbglPz.setId(cjxtGtzd.getId());
				mbglPz.setMbglId("1");
				mbglPz.setDbFieldName(cjxtGtzd.getZdname());
				mbglPz.setDbFieldTxt(cjxtGtzd.getZdbz());
				mbglPz.setDbLength(cjxtGtzd.getZdcd());
				mbglPz.setDbDefaultVal(cjxtGtzd.getMrz());
				mbglPz.setDbType(cjxtGtzd.getZdlx());
				mbglPz.setFieldShowType(cjxtGtzd.getKjlx());
				mbglPz.setDbIsKey(cjxtGtzd.getIsKey());
				mbglPz.setYmsx(cjxtGtzd.getYmsx());
				mbglPz.setIsQuery("1");
				mbglPz.setIsfather("0");
				mbglPz.setMbglMbbh("1");
				cjxtMbglPzService.save(mbglPz);
			}
		}else {
			CjxtMbglPz cjxtMbglPz = cjxtMbglPzService.getOne(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getDbFieldName,cjxtGtzd.getZdname()).eq(CjxtMbglPz::getMbglId,"1").last("LIMIT 1"));
			if(cjxtMbglPz!=null){
				cjxtMbglPzService.removeById(cjxtMbglPz.getId());
			}
		}
		cjxtGtzdService.updateById(cjxtGtzd);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "共通字段配置-通过id删除")
	@ApiOperation(value="共通字段配置-通过id删除", notes="共通字段配置-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_gtzd:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		CjxtGtzd cjxtGtzd = cjxtGtzdService.getById(id);
		if(cjxtGtzd!=null&&"2".equals(cjxtGtzd.getYmsx())){
			cjxtMbglPzService.removeById(id);
		}
		cjxtGtzdService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "共通字段配置-批量删除")
	@ApiOperation(value="共通字段配置-批量删除", notes="共通字段配置-批量删除")
//	@RequiresPermissions("cjxt:cjxt_gtzd:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtGtzdService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "共通字段配置-通过id查询")
	@ApiOperation(value="共通字段配置-通过id查询", notes="共通字段配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtGtzd> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtGtzd cjxtGtzd = cjxtGtzdService.getById(id);
		if(cjxtGtzd==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtGtzd);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtGtzd
    */
//    @RequiresPermissions("cjxt:cjxt_gtzd:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtGtzd cjxtGtzd) {
        return super.exportXls(request, cjxtGtzd, CjxtGtzd.class, "共通字段配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cjxt:cjxt_gtzd:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtGtzd.class);
    }

}
