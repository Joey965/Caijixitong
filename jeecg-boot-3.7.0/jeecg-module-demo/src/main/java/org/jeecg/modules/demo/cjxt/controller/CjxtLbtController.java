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
import org.jeecg.modules.demo.cjxt.entity.CjxtLbt;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.demo.cjxt.service.ICjxtLbtService;

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
 * @Description: 轮播图
 * @Author: jeecg-boot
 * @Date:   2024-06-12
 * @Version: V1.0
 */
@Api(tags="轮播图")
@RestController
@RequestMapping("/cjxt/cjxtLbt")
@Slf4j
public class CjxtLbtController extends JeecgController<CjxtLbt, ICjxtLbtService> {
	@Autowired
	private ICjxtLbtService cjxtLbtService;
	//minio图片服务器
	@Value(value="${jeecg.minio.minio_url}")
	private String minioUrl;
	@Value(value="${jeecg.minio.bucketName}")
	private String bucketName;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtLbt
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "轮播图-分页列表查询")
	@ApiOperation(value="轮播图-分页列表查询", notes="轮播图-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtLbt>> queryPageList(CjxtLbt cjxtLbt,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper queryWrapper = new QueryWrapper<>(cjxtLbt);
		queryWrapper.orderByAsc("display_order");
		Page<CjxtLbt> page = new Page<CjxtLbt>(pageNo, pageSize);
		IPage<CjxtLbt> pageList = cjxtLbtService.page(page, queryWrapper);
		pageList.getRecords().forEach(cjxtLbtItem -> {
			if (cjxtLbtItem.getImagePath() != null && !cjxtLbtItem.getImagePath().isEmpty()) {
				cjxtLbtItem.setImagePath(minioUrl+"/"+bucketName+"/"+cjxtLbtItem.getImagePath());
			}
		});
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtLbt
	 * @return
	 */
	@AutoLog(value = "轮播图-添加")
	@ApiOperation(value="轮播图-添加", notes="轮播图-添加")
	@RequiresPermissions("cjxt:cjxt_lbt:add")
	@PostMapping(value = "/add")
	public Result<CjxtLbt> add(@RequestBody CjxtLbt cjxtLbt) {
		Result<CjxtLbt> result = new Result<CjxtLbt>();
		try {
			String heardUrl = minioUrl+"/"+bucketName+"/";
			if(cjxtLbt.getImagePath()!=null && !"".equals(cjxtLbt.getImagePath())){
				if(cjxtLbt.getImagePath().contains(heardUrl)){
					cjxtLbt.setImagePath(cjxtLbt.getImagePath().replace(heardUrl,""));
				}
			}
			cjxtLbt.setCreateTime(new Date());
			cjxtLbt.setDelFlag(CommonConstant.DEL_FLAG_0);
			cjxtLbt.setFbzt("0");
			cjxtLbtService.save(cjxtLbt);
			result.success("保存成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtLbt
	 * @return
	 */
	@AutoLog(value = "轮播图-编辑")
	@ApiOperation(value="轮播图-编辑", notes="轮播图-编辑")
	@RequiresPermissions("cjxt:cjxt_lbt:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<CjxtLbt> edit(@RequestBody CjxtLbt cjxtLbt) {
		Result<CjxtLbt> result = new Result<CjxtLbt>();
		CjxtLbt cjxtlbt = cjxtLbtService.getById(cjxtLbt.getId());
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtLbt.getImagePath()!=null && !"".equals(cjxtLbt.getImagePath())){
			if(cjxtLbt.getImagePath().contains(heardUrl)){
				cjxtLbt.setImagePath(cjxtLbt.getImagePath().replace(heardUrl,""));
			}
		}
		if(cjxtlbt==null) {
			result.error500("未找到对应实体");
		}else {
			cjxtLbt.setUpdateTime(new Date());
			boolean ok = cjxtLbtService.updateById(cjxtLbt);
			if(ok) {
				result.success("编辑成功!");
			}
		}
		return result;
	}

	 /**
	  *  发布/撤销
	  *
	  * @param cjxtLbt
	  * @return
	  */
	 @AutoLog(value = "轮播图-发布/撤销")
	 @ApiOperation(value="轮播图-发布/撤销", notes="轮播图-发布/撤销")
	 @RequestMapping(value = "/fbcx", method = {RequestMethod.PUT,RequestMethod.POST})
	 @RequiresPermissions("cjxt:cjxt_lbt:fbcx")
	 public Result<CjxtLbt> fbcx(@RequestBody CjxtLbt cjxtLbt) {
		 Result<CjxtLbt> result = new Result<CjxtLbt>();
		 CjxtLbt cjxtlbt = cjxtLbtService.getById(cjxtLbt.getId());
		 if(cjxtlbt==null) {
			 result.error500("未找到对应实体");
		 }else {
			 cjxtLbt.setUpdateTime(new Date());
			 boolean ok = cjxtLbtService.updateById(cjxtLbt);
			 if(ok) {
				 if("1".equals(cjxtLbt.getFbzt())){
					 result.success("发布成功!");
				 }else if("2".equals(cjxtLbt.getFbzt())){
					 result.success("撤销成功!");
				 }
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
	@AutoLog(value = "轮播图-通过id删除")
	@ApiOperation(value="轮播图-通过id删除", notes="轮播图-通过id删除")
	@RequiresPermissions("cjxt:cjxt_lbt:delete")
	@DeleteMapping(value = "/delete")
	public Result<CjxtLbt> delete(@RequestParam(name="id",required=true) String id) {
		Result<CjxtLbt> result = new Result<CjxtLbt>();
		boolean ok = cjxtLbtService.removeById(id);
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
	@AutoLog(value = "轮播图-批量删除")
	@ApiOperation(value="轮播图-批量删除", notes="轮播图-批量删除")
	@RequiresPermissions("cjxt:cjxt_lbt:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<CjxtLbt> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<CjxtLbt> result = new Result<CjxtLbt>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("参数不识别！");
		}else {
			cjxtLbtService.removeByIds(Arrays.asList(ids.split(",")));
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
	//@AutoLog(value = "轮播图-通过id查询")
	@ApiOperation(value="轮播图-通过id查询", notes="轮播图-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtLbt> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtLbt cjxtLbt = cjxtLbtService.getById(id);
		cjxtLbt.setImagePath(minioUrl+"/"+bucketName+"/"+cjxtLbt.getImagePath());
		if(cjxtLbt==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtLbt);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtLbt
    */
    @RequiresPermissions("cjxt:cjxt_lbt:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtLbt cjxtLbt) {
        return super.exportXls(request, cjxtLbt, CjxtLbt.class, "轮播图");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cjxt:cjxt_lbt:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtLbt.class);
    }
	/**
	* 查询全部已发布的轮播图（APP接口）
	*
	* @param req
	* @return
	*/
	//@AutoLog(value = "轮播图-列表查询")
	@ApiOperation(value="轮播图-列表查询", notes="轮播图-列表查询")
	@GetMapping(value = "/listYfb")
	public Result<List<CjxtLbt>> listYfb(HttpServletRequest req) {
	 	QueryWrapper queryWrapper = new QueryWrapper<>();
		queryWrapper.orderByAsc("display_order");
	 	queryWrapper.eq("fbzt",1);
	 	List<CjxtLbt> list = cjxtLbtService.list(queryWrapper);
		for(CjxtLbt cjxtLbt: list){
			cjxtLbt.setImagePath(minioUrl+"/"+bucketName+"/"+cjxtLbt.getImagePath());
		}
	 	return Result.OK(list);
	}
}
