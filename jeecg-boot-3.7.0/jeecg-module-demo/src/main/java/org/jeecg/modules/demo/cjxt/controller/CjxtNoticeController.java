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
import org.jeecg.modules.demo.cjxt.entity.CjxtNotice;
import org.jeecg.modules.demo.cjxt.service.ICjxtNoticeService;

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
 * @Description: 通知公告
 * @Author: jeecg-boot
 * @Date:   2024-06-13
 * @Version: V1.0
 */
@Api(tags="通知公告")
@RestController
@RequestMapping("/cjxt/cjxtNotice")
@Slf4j
public class CjxtNoticeController extends JeecgController<CjxtNotice, ICjxtNoticeService> {
	@Autowired
	private ICjxtNoticeService cjxtNoticeService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtNotice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "通知公告-分页列表查询")
	@ApiOperation(value="通知公告-分页列表查询", notes="通知公告-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtNotice>> queryPageList(CjxtNotice cjxtNotice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper queryWrapper = new QueryWrapper(cjxtNotice);
		queryWrapper.orderByDesc("fbsj");
		Page<CjxtNotice> page = new Page<CjxtNotice>(pageNo, pageSize);
		IPage<CjxtNotice> pageList = cjxtNoticeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtNotice
	 * @return
	 */
	@AutoLog(value = "通知公告-添加")
	@ApiOperation(value="通知公告-添加", notes="通知公告-添加")
	@RequiresPermissions("cjxt:cjxt_notice:add")
	@PostMapping(value = "/add")
	public Result<CjxtNotice> add(@RequestBody CjxtNotice cjxtNotice) {
		Result<CjxtNotice> result = new Result<CjxtNotice>();
		try {
			cjxtNotice.setCreateTime(new Date());
			cjxtNotice.setDelFlag(CommonConstant.DEL_FLAG_0);
			cjxtNotice.setFbzt("0");
			cjxtNoticeService.save(cjxtNotice);
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
	 * @param cjxtNotice
	 * @return
	 */
	@AutoLog(value = "通知公告-编辑")
	@ApiOperation(value="通知公告-编辑", notes="通知公告-编辑")
	@RequiresPermissions("cjxt:cjxt_notice:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<CjxtNotice> edit(@RequestBody CjxtNotice cjxtNotice) {
		Result<CjxtNotice> result = new Result<CjxtNotice>();
		CjxtNotice cjxtnotice = cjxtNoticeService.getById(cjxtNotice.getId());
		if(cjxtnotice==null) {
			result.error500("未找到对应实体");
		}else {
			cjxtNotice.setUpdateTime(new Date());
			boolean ok = cjxtNoticeService.updateById(cjxtNotice);
			if(ok) {
				result.success("编辑成功!");
			}
		}
		return result;
	}

	 /**
	  *  发布/撤销
	  *
	  * @param cjxtNotice
	  * @return
	  */
	 @AutoLog(value = "通知公告-发布/撤销")
	 @ApiOperation(value="通知公告-发布/撤销", notes="通知公告-发布/撤销")
	 @RequestMapping(value = "/fbcx", method = {RequestMethod.PUT,RequestMethod.POST})
	 @RequiresPermissions("cjxt:cjxt_notice:fbcx")
	 public Result<CjxtNotice> fbcx(@RequestBody CjxtNotice cjxtNotice) {
		 Result<CjxtNotice> result = new Result<CjxtNotice>();
		 CjxtNotice cjxtnotice = cjxtNoticeService.getById(cjxtNotice.getId());
		 if(cjxtnotice==null) {
			 result.error500("未找到对应实体");
		 }else {
			 cjxtNotice.setUpdateTime(new Date());
			 boolean ok = cjxtNoticeService.updateById(cjxtNotice);
			 if(ok) {
				 if("1".equals(cjxtNotice.getFbzt())){
					 result.success("发布成功!");
				 }else if("2".equals(cjxtNotice.getFbzt())){
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
	@AutoLog(value = "通知公告-通过id删除")
	@ApiOperation(value="通知公告-通过id删除", notes="通知公告-通过id删除")
	@RequiresPermissions("cjxt:cjxt_notice:delete")
	@DeleteMapping(value = "/delete")
	public Result<CjxtNotice> delete(@RequestParam(name="id",required=true) String id) {
		Result<CjxtNotice> result = new Result<CjxtNotice>();
		boolean ok = cjxtNoticeService.removeById(id);
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
	@AutoLog(value = "通知公告-批量删除")
	@ApiOperation(value="通知公告-批量删除", notes="通知公告-批量删除")
	@RequiresPermissions("cjxt:cjxt_notice:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<CjxtNotice> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<CjxtNotice> result = new Result<CjxtNotice>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("参数不识别！");
		}else {
			cjxtNoticeService.removeByIds(Arrays.asList(ids.split(",")));
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
	//@AutoLog(value = "通知公告-通过id查询")
	@ApiOperation(value="通知公告-通过id查询", notes="通知公告-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtNotice> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtNotice cjxtNotice = cjxtNoticeService.getById(id);
		if(cjxtNotice==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtNotice);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtNotice
    */
    @RequiresPermissions("cjxt:cjxt_notice:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtNotice cjxtNotice) {
        return super.exportXls(request, cjxtNotice, CjxtNotice.class, "通知公告");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("cjxt:cjxt_notice:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtNotice.class);
    }
	 /**
	  * 查询全部已发布的通知公告（APP接口）
	  *
	  * @param req
	  * @return
	  */
	 //@AutoLog(value = "通知公告-列表查询")
	 @ApiOperation(value="通知公告-列表查询", notes="通知公告-列表查询")
	 @GetMapping(value = "/listNotice")
	 public Result<IPage<CjxtNotice>> listNotice(
									 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									 @RequestParam(name="search",required=false) String search,
			 						HttpServletRequest req) {
		 QueryWrapper queryWrapper = new QueryWrapper<>();
		 if(search!=null&&!"".equals(search)){
			 queryWrapper.like("title",search);
			 queryWrapper.or();
			 queryWrapper.like("fbdw",search);
		 }
		 queryWrapper.eq("fbzt",1);
		 queryWrapper.orderByDesc("fbsj");
		 Page<CjxtNotice> page = new Page<CjxtNotice>(pageNo, pageSize);
		 IPage<CjxtNotice> pageList = cjxtNoticeService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }
	 /**
	  * 查询全部已发布的最新一条通知公告（APP接口）
	  *
	  * @param req
	  * @return
	  */
	 //@AutoLog(value = "通知公告-最新一条查询")
	 @ApiOperation(value="通知公告-最新一条查询", notes="通知公告-最新一条查询")
	 @GetMapping(value = "/QueryFirstNotice")
	 public Result<CjxtNotice> QueryFirstNotice(HttpServletRequest req) {
		 QueryWrapper queryWrapper = new QueryWrapper<>();
		 queryWrapper.orderByDesc("fbsj");
		 queryWrapper.eq("fbzt",1);
		 List<CjxtNotice> list = cjxtNoticeService.list(queryWrapper);
		 CjxtNotice cjxtNotice = new CjxtNotice();
		 if(list.size()>0){
			 cjxtNotice = list.get(0);
		 }
		 return Result.OK(cjxtNotice);
	 }
}
