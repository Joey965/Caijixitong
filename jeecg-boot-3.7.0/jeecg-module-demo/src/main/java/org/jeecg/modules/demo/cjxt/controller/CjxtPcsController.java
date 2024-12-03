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
import org.jeecg.modules.demo.cjxt.entity.CjxtPcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtPcsService;

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
 * @Description: 派出所基本信息
 * @Author: jeecg-boot
 * @Date:   2024-08-07
 * @Version: V1.0
 */
@Api(tags="派出所基本信息")
@RestController
@RequestMapping("/cjxt/cjxtPcs")
@Slf4j
public class CjxtPcsController extends JeecgController<CjxtPcs, ICjxtPcsService> {
	@Autowired
	private ICjxtPcsService cjxtPcsService;
	//minio图片服务器
	@Value(value="${jeecg.minio.minio_url}")
	private String minioUrl;
	@Value(value="${jeecg.minio.bucketName}")
	private String bucketName;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtPcs
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "派出所基本信息-分页列表查询")
	@ApiOperation(value="派出所基本信息-分页列表查询", notes="派出所基本信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtPcs>> queryPageList(CjxtPcs cjxtPcs,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtPcs> queryWrapper = QueryGenerator.initQueryWrapper(cjxtPcs, req.getParameterMap());
		Page<CjxtPcs> page = new Page<CjxtPcs>(pageNo, pageSize);
		IPage<CjxtPcs> pageList = cjxtPcsService.page(page, queryWrapper);
		for(CjxtPcs pcs: pageList.getRecords()){
			if(pcs.getSqmjTp()!=null && !"".equals(pcs.getSqmjTp())){
				pcs.setSqmjTp(minioUrl+"/"+bucketName+"/"+pcs.getSqmjTp());
			}
		}
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtPcs
	 * @return
	 */
	@AutoLog(value = "派出所基本信息-添加")
	@ApiOperation(value="派出所基本信息-添加", notes="派出所基本信息-添加")
//	@RequiresPermissions("cjxt:cjxt_pcs:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtPcs cjxtPcs) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtPcs.getSqmjTp()!=null && !"".equals(cjxtPcs.getSqmjTp())){
			if(cjxtPcs.getSqmjTp().contains(heardUrl)){
				cjxtPcs.setSqmjTp(cjxtPcs.getSqmjTp().replace(heardUrl,""));
			}
		}
		cjxtPcsService.save(cjxtPcs);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtPcs
	 * @return
	 */
	@AutoLog(value = "派出所基本信息-编辑")
	@ApiOperation(value="派出所基本信息-编辑", notes="派出所基本信息-编辑")
//	@RequiresPermissions("cjxt:cjxt_pcs:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtPcs cjxtPcs) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtPcs.getSqmjTp()!=null && !"".equals(cjxtPcs.getSqmjTp())){
			if(cjxtPcs.getSqmjTp().contains(heardUrl)){
				cjxtPcs.setSqmjTp(cjxtPcs.getSqmjTp().replace(heardUrl,""));
			}
		}
		cjxtPcsService.updateById(cjxtPcs);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "派出所基本信息-通过id删除")
	@ApiOperation(value="派出所基本信息-通过id删除", notes="派出所基本信息-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_pcs:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtPcsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "派出所基本信息-批量删除")
	@ApiOperation(value="派出所基本信息-批量删除", notes="派出所基本信息-批量删除")
//	@RequiresPermissions("cjxt:cjxt_pcs:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtPcsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "派出所基本信息-通过id查询")
	@ApiOperation(value="派出所基本信息-通过id查询", notes="派出所基本信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtPcs> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtPcs cjxtPcs = cjxtPcsService.getById(id);
		if(cjxtPcs==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtPcs);
	}

	 /**
	  * 通过部门ID查询
	  *
	  * @param orgId
	  * @return
	  */
	 //@AutoLog(value = "派出所基本信息-通过部门ID查询")
	 @ApiOperation(value="派出所基本信息-通过部门ID查询", notes="派出所基本信息-通过部门ID查询")
	 @GetMapping(value = "/queryByOrgId")
	 public Result<CjxtPcs> queryByOrgId(@RequestParam(name="orgId",required=true) String orgId) {
		 CjxtPcs cjxtPcs = cjxtPcsService.getOne(new LambdaQueryWrapper<CjxtPcs>().eq(CjxtPcs::getOrgId,orgId).last("ORDER BY create_time ASC LIMIT 1"));
		 String heardUrl = minioUrl+"/"+bucketName+"/";
		 if(cjxtPcs.getSqmjTp()!=null && !"".equals(cjxtPcs.getSqmjTp())){
			 cjxtPcs.setSqmjTp(heardUrl+cjxtPcs.getSqmjTp());
		 }
//		 CjxtPcs cjxtPcs = cjxtPcsService.getById(id);
		 if(cjxtPcs==null) {
			 return Result.error("未找到对应数据");
		 }
		 return Result.OK(cjxtPcs);
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtPcs
    */
//    @RequiresPermissions("cjxt:cjxt_pcs:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtPcs cjxtPcs) {
        return super.exportXls(request, cjxtPcs, CjxtPcs.class, "派出所基本信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_pcs:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtPcs.class);
    }

}
