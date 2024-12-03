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
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtZxcjtb;
import org.jeecg.modules.demo.cjxt.service.ICjxtZxcjtbService;

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
 * @Description: 专项采集图标
 * @Author: jeecg-boot
 * @Date:   2024-09-23
 * @Version: V1.0
 */
@Api(tags="专项采集图标")
@RestController
@RequestMapping("/cjxt/cjxtZxcjtb")
@Slf4j
public class CjxtZxcjtbController extends JeecgController<CjxtZxcjtb, ICjxtZxcjtbService> {
	@Autowired
	private ICjxtZxcjtbService cjxtZxcjtbService;
	 //minio图片服务器
	 @Value(value="${jeecg.minio.minio_url}")
	 private String minioUrl;
	 @Value(value="${jeecg.minio.bucketName}")
	 private String bucketName;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtZxcjtb
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "专项采集图标-分页列表查询")
	@ApiOperation(value="专项采集图标-分页列表查询", notes="专项采集图标-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtZxcjtb>> queryPageList(CjxtZxcjtb cjxtZxcjtb,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtZxcjtb> queryWrapper = QueryGenerator.initQueryWrapper(cjxtZxcjtb, req.getParameterMap());
		Page<CjxtZxcjtb> page = new Page<CjxtZxcjtb>(pageNo, pageSize);
		IPage<CjxtZxcjtb> pageList = cjxtZxcjtbService.page(page, queryWrapper);
		pageList.getRecords().forEach(zxcjtbItem -> {
			if (zxcjtbItem.getTbImg() != null && !zxcjtbItem.getTbImg().isEmpty()) {
				zxcjtbItem.setTbImg(minioUrl+"/"+bucketName+"/"+zxcjtbItem.getTbImg());
			}
		});
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtZxcjtb
	 * @return
	 */
	@AutoLog(value = "专项采集图标-添加")
	@ApiOperation(value="专项采集图标-添加", notes="专项采集图标-添加")
	//@RequiresPermissions("cjxt:cjxt_zxcjtb:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtZxcjtb cjxtZxcjtb) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtZxcjtb.getTbImg()!=null && !"".equals(cjxtZxcjtb.getTbImg())){
			if(cjxtZxcjtb.getTbImg().contains(heardUrl)){
				cjxtZxcjtb.setTbImg(cjxtZxcjtb.getTbImg().replace(heardUrl,""));
			}
		}
		cjxtZxcjtbService.save(cjxtZxcjtb);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtZxcjtb
	 * @return
	 */
	@AutoLog(value = "专项采集图标-编辑")
	@ApiOperation(value="专项采集图标-编辑", notes="专项采集图标-编辑")
	//@RequiresPermissions("cjxt:cjxt_zxcjtb:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtZxcjtb cjxtZxcjtb) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtZxcjtb.getTbImg()!=null && !"".equals(cjxtZxcjtb.getTbImg())){
			if(cjxtZxcjtb.getTbImg().contains(heardUrl)){
				cjxtZxcjtb.setTbImg(cjxtZxcjtb.getTbImg().replace(heardUrl,""));
			}
		}
		cjxtZxcjtbService.updateById(cjxtZxcjtb);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "专项采集图标-通过id删除")
	@ApiOperation(value="专项采集图标-通过id删除", notes="专项采集图标-通过id删除")
	//@RequiresPermissions("cjxt:cjxt_zxcjtb:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtZxcjtbService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "专项采集图标-批量删除")
	@ApiOperation(value="专项采集图标-批量删除", notes="专项采集图标-批量删除")
	//@RequiresPermissions("cjxt:cjxt_zxcjtb:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtZxcjtbService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "专项采集图标-通过id查询")
	@ApiOperation(value="专项采集图标-通过id查询", notes="专项采集图标-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtZxcjtb> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtZxcjtb cjxtZxcjtb = cjxtZxcjtbService.getById(id);
		if(cjxtZxcjtb==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtZxcjtb);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtZxcjtb
    */
    //@RequiresPermissions("cjxt:cjxt_zxcjtb:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtZxcjtb cjxtZxcjtb) {
        return super.exportXls(request, cjxtZxcjtb, CjxtZxcjtb.class, "专项采集图标");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("cjxt:cjxt_zxcjtb:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtZxcjtb.class);
    }

}
