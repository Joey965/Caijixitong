package org.jeecg.modules.demo.cjxt.controller;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import org.jeecg.modules.demo.cjxt.service.ICjxtMbglPzService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpz;
import org.jeecg.modules.demo.cjxt.vo.CjxtJsmbpzPage;
import org.jeecg.modules.demo.cjxt.service.ICjxtJsmbpzService;
import org.jeecg.modules.demo.cjxt.service.ICjxtJsmbpzDtlService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;


 /**
 * @Description: 角色模版配置
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
@Api(tags="角色模版配置")
@RestController
@RequestMapping("/cjxt/cjxtJsmbpz")
@Slf4j
public class CjxtJsmbpzController {
	@Autowired
	private ICjxtJsmbpzService cjxtJsmbpzService;
	@Autowired
	private ICjxtJsmbpzDtlService cjxtJsmbpzDtlService;
	@Autowired
	private ICjxtMbglPzService cjxtMbglPzService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtJsmbpz
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "角色模版配置-分页列表查询")
	@ApiOperation(value="角色模版配置-分页列表查询", notes="角色模版配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtJsmbpz>> queryPageList(CjxtJsmbpz cjxtJsmbpz,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtJsmbpz> queryWrapper = QueryGenerator.initQueryWrapper(cjxtJsmbpz, req.getParameterMap());
		Page<CjxtJsmbpz> page = new Page<CjxtJsmbpz>(pageNo, pageSize);
		IPage<CjxtJsmbpz> pageList = cjxtJsmbpzService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtJsmbpzPage
	 * @return
	 */
	@AutoLog(value = "角色模版配置-添加")
	@ApiOperation(value="角色模版配置-添加", notes="角色模版配置-添加")
//    @RequiresPermissions("cjxt:cjxt_jsmbpz:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtJsmbpzPage cjxtJsmbpzPage) {
		CjxtJsmbpz cjxtJsmbpz = new CjxtJsmbpz();
		CjxtJsmbpz jsmbpz = cjxtJsmbpzService.getOne(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode,cjxtJsmbpzPage.getRoleCode()).eq(CjxtJsmbpz::getMbId,cjxtJsmbpzPage.getMbId()));
		if(jsmbpz!=null){
			return Result.error("当前角色模板配置已存在!!!");
		}
		BeanUtils.copyProperties(cjxtJsmbpzPage, cjxtJsmbpz);
		cjxtJsmbpzService.saveMain(cjxtJsmbpz, cjxtJsmbpzPage.getCjxtJsmbpzDtlList());
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtJsmbpzPage
	 * @return
	 */
	@AutoLog(value = "角色模版配置-编辑")
	@ApiOperation(value="角色模版配置-编辑", notes="角色模版配置-编辑")
//    @RequiresPermissions("cjxt:cjxt_jsmbpz:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtJsmbpzPage cjxtJsmbpzPage) {
		CjxtJsmbpz cjxtJsmbpz = new CjxtJsmbpz();
		if(!cjxtJsmbpzPage.getMbIdDto().equals(cjxtJsmbpzPage.getMbId()) || !cjxtJsmbpzPage.getRoleCodeDto().equals(cjxtJsmbpzPage.getRoleCode())){
			CjxtJsmbpz jsmbpz = cjxtJsmbpzService.getOne(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode,cjxtJsmbpzPage.getRoleCode()).eq(CjxtJsmbpz::getMbId,cjxtJsmbpzPage.getMbId()));
			if(jsmbpz!=null){
				return Result.error("当前角色模板配置已存在!!!");
			}
		}
		BeanUtils.copyProperties(cjxtJsmbpzPage, cjxtJsmbpz);
		CjxtJsmbpz cjxtJsmbpzEntity = cjxtJsmbpzService.getById(cjxtJsmbpz.getId());
		if(cjxtJsmbpzEntity==null) {
			return Result.error("未找到对应数据");
		}
		cjxtJsmbpzService.updateMain(cjxtJsmbpz, cjxtJsmbpzPage.getCjxtJsmbpzDtlList());
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "角色模版配置-通过id删除")
	@ApiOperation(value="角色模版配置-通过id删除", notes="角色模版配置-通过id删除")
//    @RequiresPermissions("cjxt:cjxt_jsmbpz:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtJsmbpzService.delMain(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "角色模版配置-批量删除")
	@ApiOperation(value="角色模版配置-批量删除", notes="角色模版配置-批量删除")
//    @RequiresPermissions("cjxt:cjxt_jsmbpz:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtJsmbpzService.delBatchMain(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "角色模版配置-通过id查询")
	@ApiOperation(value="角色模版配置-通过id查询", notes="角色模版配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtJsmbpz> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtJsmbpz cjxtJsmbpz = cjxtJsmbpzService.getById(id);
		if(cjxtJsmbpz==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtJsmbpz);

	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "角色模版配置子表通过主表ID查询")
	@ApiOperation(value="角色模版配置子表主表ID查询", notes="角色模版配置子表-通主表ID查询")
	@GetMapping(value = "/queryCjxtJsmbpzDtlByMainId")
	public Result<List<CjxtJsmbpzDtl>> queryCjxtJsmbpzDtlListByMainId(@RequestParam(name="id",required=true) String id,
																	  @RequestParam(name="selectCanel",required=false) String selectCanel) {
		List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList = new ArrayList<>();
		if(!"".equals(id) && id != null){
			//定义返回值
			List<CjxtMbglPz> cjxtMbglPzList =  new ArrayList<>();
			List<CjxtJsmbpzDtl> cjxtJsmbpzDtlListS = cjxtJsmbpzDtlService.selectByMainId(id);
			if(cjxtJsmbpzDtlListS!=null && cjxtJsmbpzDtlListS.size()>0){
				CjxtJsmbpzDtl jsmbpzDtl = cjxtJsmbpzDtlListS.get(0);
				cjxtMbglPzList = cjxtMbglPzService.selectByMainIdCommm(jsmbpzDtl.getMbId());
				for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
					CjxtJsmbpzDtl newCjxtJsmbpzDtl = new CjxtJsmbpzDtl();
					for(CjxtJsmbpzDtl cjxtJsmbpzDtl :cjxtJsmbpzDtlListS){
						if(cjxtJsmbpzDtl.getDbFieldName().equals(cjxtMbglPz.getDbFieldName())) {
							newCjxtJsmbpzDtl.setDbShow("true");
						}
					}
					newCjxtJsmbpzDtl.setDbFieldName(cjxtMbglPz.getDbFieldName());
					newCjxtJsmbpzDtl.setDbFieldTxt(cjxtMbglPz.getDbFieldTxt());
					cjxtJsmbpzDtlList.add(newCjxtJsmbpzDtl);
				}
			}else {
				cjxtMbglPzList = cjxtMbglPzService.selectByMainIdCommm(id);
				for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
					CjxtJsmbpzDtl cjxtJsmbpzDtl = new CjxtJsmbpzDtl();
					cjxtJsmbpzDtl.setDbFieldName(cjxtMbglPz.getDbFieldName());
					cjxtJsmbpzDtl.setDbFieldTxt(cjxtMbglPz.getDbFieldTxt());
					if(!"".equals(selectCanel) && selectCanel!= null){
						if("true".equals(selectCanel)){
							cjxtJsmbpzDtl.setDbShow("false");
						}else {
							cjxtJsmbpzDtl.setDbShow("true");
						}
					}else {
						cjxtJsmbpzDtl.setDbShow("true");
					}
//					cjxtJsmbpzDtl.setDbShow("true");
					cjxtJsmbpzDtlList.add(cjxtJsmbpzDtl);
				}
			}
		}
		return Result.OK(cjxtJsmbpzDtlList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtJsmbpz
    */
//    @RequiresPermissions("cjxt:cjxt_jsmbpz:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtJsmbpz cjxtJsmbpz) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<CjxtJsmbpz> queryWrapper = QueryGenerator.initQueryWrapper(cjxtJsmbpz, request.getParameterMap());
      LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

      //配置选中数据查询条件
      String selections = request.getParameter("selections");
      if(oConvertUtils.isNotEmpty(selections)) {
         List<String> selectionList = Arrays.asList(selections.split(","));
         queryWrapper.in("id",selectionList);
      }
      //Step.2 获取导出数据
      List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(queryWrapper);

      // Step.3 组装pageList
      List<CjxtJsmbpzPage> pageList = new ArrayList<CjxtJsmbpzPage>();
      for (CjxtJsmbpz main : cjxtJsmbpzList) {
          CjxtJsmbpzPage vo = new CjxtJsmbpzPage();
          BeanUtils.copyProperties(main, vo);
          List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList = cjxtJsmbpzDtlService.selectByMainId(main.getId());
          vo.setCjxtJsmbpzDtlList(cjxtJsmbpzDtlList);
          pageList.add(vo);
      }

      // Step.4 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      mv.addObject(NormalExcelConstants.FILE_NAME, "角色模版配置列表");
      mv.addObject(NormalExcelConstants.CLASS, CjxtJsmbpzPage.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("角色模版配置数据", "导出人:"+sysUser.getRealname(), "角色模版配置"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
    }

    /**
    * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_jsmbpz:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
      for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          // 获取上传文件对象
          MultipartFile file = entity.getValue();
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<CjxtJsmbpzPage> list = ExcelImportUtil.importExcel(file.getInputStream(), CjxtJsmbpzPage.class, params);
              for (CjxtJsmbpzPage page : list) {
                  CjxtJsmbpz po = new CjxtJsmbpz();
                  BeanUtils.copyProperties(page, po);
                  cjxtJsmbpzService.saveMain(po, page.getCjxtJsmbpzDtlList());
              }
              return Result.OK("文件导入成功！数据行数:" + list.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("文件导入失败:"+e.getMessage());
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.OK("文件导入失败！");
    }

}
