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

import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtRoleMenu;
import org.jeecg.modules.demo.cjxt.service.ICjxtRoleMenuService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysRolePermission;
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
 * @Description: APP角色菜单配置
 * @Author: jeecg-boot
 * @Date:   2024-10-09
 * @Version: V1.0
 */
@Api(tags="APP角色菜单配置")
@RestController
@RequestMapping("/cjxt/cjxtRoleMenu")
@Slf4j
public class CjxtRoleMenuController extends JeecgController<CjxtRoleMenu, ICjxtRoleMenuService> {
	@Autowired
	private ICjxtRoleMenuService cjxtRoleMenuService;

	 @ApiOperation(value="APP角色菜单配置-角色菜单新增", notes="APP角色菜单配置-角色菜单新增")
	 @RequestMapping(value = "/saveRoleMenu", method = RequestMethod.POST)
//	 @RequiresPermissions("system:permission:saveRole")
	 public Result<String> saveRolePermission(@RequestBody JSONObject json) {
		 long start = System.currentTimeMillis();
		 Result<String> result = new Result<>();
		 try {
			 String roleId = json.getString("roleId");
			 String permissionIds = json.getString("permissionIds");
			 String lastPermissionIds = json.getString("lastpermissionIds");
			 this.cjxtRoleMenuService.saveRolePermission(roleId, permissionIds, lastPermissionIds);
		 } catch (Exception e) {
			 result.error500("授权失败！");
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 @ApiOperation(value="APP角色菜单配置-初始化角色菜单数据", notes="APP角色菜单配置-初始化角色菜单数据")
	 @RequestMapping(value = "/queryRoleMenu", method = RequestMethod.GET)
	 public Result<List<String>> queryRoleMenu(@RequestParam(name = "roleId", required = true) String roleId) {
		 Result<List<String>> result = new Result<>();
		 try {
			 List<CjxtRoleMenu> list = cjxtRoleMenuService.list(new QueryWrapper<CjxtRoleMenu>().lambda().eq(CjxtRoleMenu::getRoleId, roleId));
			 result.setResult(list.stream().map(cjxtRoleMenu -> String.valueOf(cjxtRoleMenu.getMenuId())).collect(Collectors.toList()));
			 result.setSuccess(true);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtRoleMenu
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "APP角色菜单配置-分页列表查询")
	@ApiOperation(value="APP角色菜单配置-分页列表查询", notes="APP角色菜单配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtRoleMenu>> queryPageList(CjxtRoleMenu cjxtRoleMenu,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtRoleMenu> queryWrapper = QueryGenerator.initQueryWrapper(cjxtRoleMenu, req.getParameterMap());
		Page<CjxtRoleMenu> page = new Page<CjxtRoleMenu>(pageNo, pageSize);
		IPage<CjxtRoleMenu> pageList = cjxtRoleMenuService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtRoleMenu
	 * @return
	 */
	@AutoLog(value = "APP角色菜单配置-添加")
	@ApiOperation(value="APP角色菜单配置-添加", notes="APP角色菜单配置-添加")
	//@RequiresPermissions("cjxt:cjxt_role_menu:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtRoleMenu cjxtRoleMenu) {
		cjxtRoleMenuService.save(cjxtRoleMenu);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtRoleMenu
	 * @return
	 */
	@AutoLog(value = "APP角色菜单配置-编辑")
	@ApiOperation(value="APP角色菜单配置-编辑", notes="APP角色菜单配置-编辑")
	//@RequiresPermissions("cjxt:cjxt_role_menu:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtRoleMenu cjxtRoleMenu) {
		cjxtRoleMenuService.updateById(cjxtRoleMenu);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "APP角色菜单配置-通过id删除")
	@ApiOperation(value="APP角色菜单配置-通过id删除", notes="APP角色菜单配置-通过id删除")
	//@RequiresPermissions("cjxt:cjxt_role_menu:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtRoleMenuService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "APP角色菜单配置-批量删除")
	@ApiOperation(value="APP角色菜单配置-批量删除", notes="APP角色菜单配置-批量删除")
	//@RequiresPermissions("cjxt:cjxt_role_menu:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtRoleMenuService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "APP角色菜单配置-通过id查询")
	@ApiOperation(value="APP角色菜单配置-通过id查询", notes="APP角色菜单配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtRoleMenu> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtRoleMenu cjxtRoleMenu = cjxtRoleMenuService.getById(id);
		if(cjxtRoleMenu==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtRoleMenu);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtRoleMenu
    */
    //@RequiresPermissions("cjxt:cjxt_role_menu:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtRoleMenu cjxtRoleMenu) {
        return super.exportXls(request, cjxtRoleMenu, CjxtRoleMenu.class, "APP角色菜单配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("cjxt:cjxt_role_menu:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtRoleMenu.class);
    }

}
