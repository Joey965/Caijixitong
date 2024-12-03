package org.jeecg.modules.demo.cjxt.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.demo.cjxt.entity.CjxtMenuPz;
import org.jeecg.modules.demo.cjxt.entity.CjxtRoleMenu;
import org.jeecg.modules.demo.cjxt.service.ICjxtMenuPzService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.service.ICjxtRoleMenuService;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @Description: APP功能菜单
 * @Author: jeecg-boot
 * @Date:   2024-10-09
 * @Version: V1.0
 */
@Api(tags="APP功能菜单")
@RestController
@RequestMapping("/cjxt/cjxtMenuPz")
@Slf4j
public class CjxtMenuPzController extends JeecgController<CjxtMenuPz, ICjxtMenuPzService>{
	@Autowired
	private ICjxtMenuPzService cjxtMenuPzService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ICjxtRoleMenuService cjxtRoleMenuService;
	@Autowired
	private JdbcTemplate jdbcTemplate;


	 @AutoLog(value = "APP功能菜单-微信小程序菜单列表")
	 @ApiOperation(value="APP功能菜单-微信小程序菜单列表", notes="APP功能菜单-微信小程序菜单列表")
	 @GetMapping(value = "/wxList")
	 public Result<Map<String, Object>> wxList(@RequestParam(name="userId", required = false) String userId) {
		 Map<String, Object> result = new HashMap<>();

		 //民警辅警---任务采集
		 List<Map<String, Object>> mjfjList = new ArrayList<>();
		 //民警辅警---基础采集
		 List<Map<String, Object>> mjJcxxList = new ArrayList<>();
		 //民警辅警---核查采集
		 List<Map<String, Object>> mjHccjList = new ArrayList<>();
		 //民警辅警---统计分析
		 List<Map<String, Object>> mjTjfxList = new ArrayList<>();

		 //网格员---基础采集
		 List<Map<String, Object>> wgyJcxxList = new ArrayList<>();
		 //网格员---上报管理
		 List<Map<String, Object>> wgysbglList = new ArrayList<>();
		 //网格员---自主上报审核
		 List<Map<String, Object>> wgyzjsbList = new ArrayList<>();

		 //积分管理---积分管理
		 List<Map<String, Object>> mjJfglList = new ArrayList<>();

		 String sql = "select cmp.* from sys_user_role sur \n" +
				 "inner join sys_role sr on sur.role_id = sr.id \n" +
				 "inner join cjxt_role_menu crm on crm.role_id = sr.id\t\n" +
				 "inner join cjxt_menu_pz cmp on crm.menu_id = cmp.id\n" +
				 "WHERE cmp.del_flag = '0' and cmp.pid != '0' and crm.del_flag = '0' and sur.user_id = '"+userId+"' ORDER BY cmp.pid,cmp.order_num";

		 List<Map<String, Object>> addressList = jdbcTemplate.queryForList(sql);
		 for (Map<String, Object> row : addressList) {
			 String addressid = (String) row.get("address_id");
			 Map<String, Object> item = new HashMap<>();
			 item.put("title", (String) row.get("app_name"));
			 item.put("icon", (String) row.get("tb"));
			 item.put("key", (String) row.get("key_name"));
			 item.put("useCount", (String) row.get("use_count"));
			 item.put("page", (String) row.get("wx_page"));
			 if (!"".equals((String) row.get("is_remind")) && "true".equals((String) row.get("is_remind"))) {
				 item.put("isRemind", (String) row.get("is_remind"));
			 }
			 if ("任务采集".equals((String) row.get("parent_name"))) {
				 mjfjList.add(item);
			 } else if ("基础采集".equals((String) row.get("parent_name"))) {
				 mjJcxxList.add(item);
				 wgyJcxxList.add(item);
			 } else if ("功能应用".equals((String) row.get("parent_name")) || "核查采集".equals((String) row.get("parent_name"))) {
				 mjHccjList.add(item);
			 }  else if ("统计分析".equals((String) row.get("parent_name"))) {
				 mjTjfxList.add(item);
			 } else if ("上报管理".equals((String) row.get("parent_name"))) {
				 wgysbglList.add(item);
			 } else if ("自主上报审核".equals((String) row.get("parent_name"))) {
				 wgyzjsbList.add(item);
			 } else if ("积分管理".equals((String) row.get("parent_name"))) {
				 mjJfglList.add(item);
			 }
		 }
		 //民警/片警
		 result.put("mjfj", mjfjList);
		 result.put("mjJcxx", mjJcxxList);
		 result.put("mjHccj", mjHccjList);
		 result.put("mjTjfx", mjTjfxList);

		 //网格员
		 result.put("wgyJcxx", wgyJcxxList);
		 result.put("wgysbgl", wgysbglList);
		 result.put("wgyzjsb", wgyzjsbList);

		 //积分管理
		 result.put("mjJfgl", mjJfglList);

		 return Result.ok(result);
	 }

	 @AutoLog(value = "APP功能菜单-APP/H5菜单列表")
	 @ApiOperation(value="APP功能菜单-APP/H5菜单列表", notes="APP功能菜单-APP/H5菜单列表")
	 @GetMapping(value = "/appList")
	 public Result<Map<String, Object>> appList(@RequestParam(name="userId", required = false) String userId) {
		 Map<String, Object> result = new HashMap<>();

		 //民警辅警---任务采集
		 List<Map<String, Object>> mjfjList = new ArrayList<>();
		 //民警辅警---基础采集
		 List<Map<String, Object>> mjJcxxList = new ArrayList<>();
		 //民警辅警---核查采集
		 List<Map<String, Object>> mjHccjList = new ArrayList<>();
		 //民警辅警---统计分析
		 List<Map<String, Object>> mjTjfxList = new ArrayList<>();

		 //网格员---基础采集
		 List<Map<String, Object>> wgyJcxxList = new ArrayList<>();
		 //网格员---上报管理
		 List<Map<String, Object>> wgysbglList = new ArrayList<>();
		 //网格员---自主上报审核
		 List<Map<String, Object>> wgyzjsbList = new ArrayList<>();

		 //积分管理---积分管理
		 List<Map<String, Object>> mjJfglList = new ArrayList<>();

		 String sql = "select cmp.* from sys_user_role sur \n" +
				 "inner join sys_role sr on sur.role_id = sr.id \n" +
				 "inner join cjxt_role_menu crm on crm.role_id = sr.id\t\n" +
				 "inner join cjxt_menu_pz cmp on crm.menu_id = cmp.id\n" +
				 "WHERE cmp.del_flag = '0' and cmp.pid != '0' and crm.del_flag = '0' and sur.user_id = '"+userId+"' ORDER BY cmp.pid,cmp.order_num";

		 List<Map<String, Object>> addressList = jdbcTemplate.queryForList(sql);
		 for (Map<String, Object> row : addressList) {
			 String addressid = (String) row.get("address_id");
			 Map<String, Object> item = new HashMap<>();
			 item.put("title", (String) row.get("app_name"));
			 item.put("icon", (String) row.get("tb"));
			 item.put("key", (String) row.get("key_name"));
			 item.put("useCount", (String) row.get("use_count"));
			 item.put("page", (String) row.get("app_page"));
			 if (!"".equals((String) row.get("is_remind")) && "true".equals((String) row.get("is_remind"))) {
				 item.put("isRemind", (String) row.get("is_remind"));
			 }
			 if ("任务采集".equals((String) row.get("parent_name"))) {
				 mjfjList.add(item);
			 } else if ("基础采集".equals((String) row.get("parent_name"))) {
				 mjJcxxList.add(item);
				 wgyJcxxList.add(item);
			 } else if ("功能应用".equals((String) row.get("parent_name")) || "核查采集".equals((String) row.get("parent_name"))) {
				 mjHccjList.add(item);
			 }  else if ("统计分析".equals((String) row.get("parent_name"))) {
				 mjTjfxList.add(item);
			 } else if ("上报管理".equals((String) row.get("parent_name"))) {
				 wgysbglList.add(item);
			 } else if ("自主上报审核".equals((String) row.get("parent_name"))) {
				 wgyzjsbList.add(item);
			 } else if ("积分管理".equals((String) row.get("parent_name"))) {
				 mjJfglList.add(item);
			 }
		 }
		 //民警/片警
		 result.put("mjfj", mjfjList);
		 result.put("mjJcxx", mjJcxxList);
		 result.put("mjHccj", mjHccjList);
		 result.put("mjTjfx", mjTjfxList);

		 //网格员
		 result.put("wgyJcxx", wgyJcxxList);
		 result.put("wgysbgl", wgysbglList);
		 result.put("wgyzjsb", wgyzjsbList);

		 //积分管理
		 result.put("mjJfgl", mjJfglList);

		 return Result.ok(result);
	 }

	 @ApiOperation(value="APP功能菜单-网格员菜单列表", notes="APP功能菜单-网格员菜单列表")
	 @GetMapping(value = "/wgyAppList")
	 public Result<Map<String, Object>> wgyAppList(@RequestParam(name="userId", required = false) String userId) {
		 Map<String, Object> result = new HashMap<>();
		 List<CjxtMenuPz> pzList = cjxtMenuPzService.list(new LambdaQueryWrapper<CjxtMenuPz>().eq(CjxtMenuPz::getPid,"0").like(CjxtMenuPz::getPcName,"网格员"));
		 List<CjxtMenuPz> jfglList = cjxtMenuPzService.list(new LambdaQueryWrapper<CjxtMenuPz>().eq(CjxtMenuPz::getPid,"0").like(CjxtMenuPz::getPcName,"积分管理"));

		 //网格员---基础采集
		 List<Map<String, Object>> pqmjList = new ArrayList<>();
		 //网格员---上报管理
		 List<Map<String, Object>> wgysbglList = new ArrayList<>();
		 //网格员---自主上报审核
		 List<Map<String, Object>> wgyzjsbList = new ArrayList<>();
		 //网格员---积分管理
		 List<Map<String, Object>> mjJfglList = new ArrayList<>();

		 for(CjxtMenuPz cjxtMenuPz: pzList){
			 List<CjxtMenuPz> menuPzList = cjxtMenuPzService.list(new LambdaQueryWrapper<CjxtMenuPz>().eq(CjxtMenuPz::getPid,cjxtMenuPz.getId()).orderByAsc(CjxtMenuPz::getOrderNum));
			 for(CjxtMenuPz menuPz: menuPzList){
				 Map<String, Object> item = new HashMap<>();
				 item.put("title", menuPz.getAppName());
				 item.put("icon", menuPz.getTb());
				 item.put("key", menuPz.getKeyName());
				 item.put("useCount", menuPz.getUseCount());
				 item.put("page", menuPz.getAppPage());
				 if (!"".equals(menuPz.getIsRemind()) && "true".equals(menuPz.getIsRemind())) {
					 item.put("isRemind", menuPz.getIsRemind());
				 }
				 if ("基础采集".equals(cjxtMenuPz.getAppName())) {
					 pqmjList.add(item);
				 } else if ("上报管理".equals(cjxtMenuPz.getAppName())) {
					 wgysbglList.add(item);
				 } else if ("自主上报审核".equals(cjxtMenuPz.getAppName())) {
					 wgyzjsbList.add(item);
				 }
			 }
		 }

		 for(CjxtMenuPz cjxtMenuPz: jfglList){
			 List<CjxtMenuPz> menuPzList = cjxtMenuPzService.list(new LambdaQueryWrapper<CjxtMenuPz>().eq(CjxtMenuPz::getPid,cjxtMenuPz.getId()).orderByAsc(CjxtMenuPz::getOrderNum));
			 for(CjxtMenuPz menuPz: menuPzList){
				 Map<String, Object> item = new HashMap<>();
				 item.put("title", menuPz.getAppName());
				 item.put("icon", menuPz.getTb());
				 item.put("key", menuPz.getKeyName());
				 item.put("useCount", menuPz.getUseCount());
				 item.put("page", menuPz.getAppPage());
				 if (!"".equals(menuPz.getIsRemind()) && "true".equals(menuPz.getIsRemind())) {
					 item.put("isRemind", menuPz.getIsRemind());
				 }
				 if ("积分管理".equals(cjxtMenuPz.getAppName())) {
					 mjJfglList.add(item);
				 }
			 }
		 }
		 result.put("pqmj", pqmjList);
		 result.put("wgysbgl", wgysbglList);
		 result.put("wgyzjsb", wgyzjsbList);
		 result.put("mjJfgl", mjJfglList);
		 return Result.ok(result);
	 }

	 @ApiOperation(value="APP功能菜单-树节点数据", notes="APP功能菜单-树节点数据")
	 @RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	 public Result<Map<String,Object>> queryTreeList(HttpServletRequest request) {
		 Result<Map<String,Object>> result = new Result<>();
		 //全部权限ids
		 List<String> ids = new ArrayList<>();
		 try {
			 LambdaQueryWrapper<CjxtMenuPz> query = new LambdaQueryWrapper<CjxtMenuPz>();
			 query.eq(CjxtMenuPz::getDelFlag, CommonConstant.DEL_FLAG_0);
			 query.orderByAsc(CjxtMenuPz::getOrderNum);
			 query.orderByAsc(CjxtMenuPz::getCreateTime);
			 List<CjxtMenuPz> list = cjxtMenuPzService.list(query);
			 for(CjxtMenuPz menuPz : list) {
				 ids.add(menuPz.getId());
			 }
			 List<TreeModel> treeList = new ArrayList<>();
			 getTreeModelList(treeList, list, null);
			 Map<String,Object> resMap = new HashMap(5);
			 //全部树节点数据
			 resMap.put("treeList", treeList);
			 //全部树ids
			 resMap.put("ids", ids);
			 result.setResult(resMap);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 private void getTreeModelList(List<TreeModel> treeList,List<CjxtMenuPz> menuPzList,TreeModel temp) {
		 for (CjxtMenuPz permission : menuPzList) {
			 String tempPid = !"0".equals(permission.getPid()) ? permission.getPid() : "";
//			 boolean hasChild = true;
			 TreeModel tree = new TreeModel(permission.getId(), tempPid, permission.getPcName(),0, "0".equals(permission.getHasChild()) ? true : false);
			 if(temp==null && oConvertUtils.isEmpty(tempPid)) {
				 treeList.add(tree);
				 if(!tree.getIsLeaf()) {
					 getTreeModelList(treeList, menuPzList, tree);
				 }
			 }else if(temp!=null && tempPid!=null && tempPid.equals(temp.getKey())){
				 temp.getChildren().add(tree);
				 if(!tree.getIsLeaf()) {
					 getTreeModelList(treeList, menuPzList, tree);
				 }
			 }

		 }
	 }

	 /**
	 * 分页列表查询
	 *
	 * @param cjxtMenuPz
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "APP功能菜单-分页列表查询")
	@ApiOperation(value="APP功能菜单-分页列表查询", notes="APP功能菜单-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<CjxtMenuPz>> queryPageList(CjxtMenuPz cjxtMenuPz,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		String hasQuery = req.getParameter("hasQuery");
        if(hasQuery != null && "true".equals(hasQuery)){
            QueryWrapper<CjxtMenuPz> queryWrapper =  QueryGenerator.initQueryWrapper(cjxtMenuPz, req.getParameterMap());
            List<CjxtMenuPz> list = cjxtMenuPzService.queryTreeListNoPage(queryWrapper);
            IPage<CjxtMenuPz> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        }else{
            String parentId = cjxtMenuPz.getPid();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            cjxtMenuPz.setPid(null);
            QueryWrapper<CjxtMenuPz> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMenuPz, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("pid", parentId);
            Page<CjxtMenuPz> page = new Page<CjxtMenuPz>(pageNo, pageSize);
            IPage<CjxtMenuPz> pageList = cjxtMenuPzService.page(page, queryWrapper);
            return Result.OK(pageList);
        }
	}

	 /**
	  * 【vue3专用】加载节点的子数据
	  *
	  * @param pid
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") String pid) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = cjxtMenuPzService.queryListByPid(pid);
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }

	 /**
	  * 【vue3专用】加载一级节点/如果是同步 则所有数据
	  *
	  * @param async
	  * @param pcode
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "pcode") String pcode) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = cjxtMenuPzService.queryListByCode(pcode);
			 if (!async) {
				 loadAllChildren(ls);
			 }
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }

	 /**
	  * 【vue3专用】递归求子节点 同步加载用到
	  *
	  * @param ls
	  */
	 private void loadAllChildren(List<SelectTreeModel> ls) {
		 for (SelectTreeModel tsm : ls) {
			 List<SelectTreeModel> temp = cjxtMenuPzService.queryListByPid(tsm.getKey());
			 if (temp != null && temp.size() > 0) {
				 tsm.setChildren(temp);
				 loadAllChildren(temp);
			 }
		 }
	 }

	 /**
      * 获取子数据
      * @param cjxtMenuPz
      * @param req
      * @return
      */
	//@AutoLog(value = "APP功能菜单-获取子数据")
	@ApiOperation(value="APP功能菜单-获取子数据", notes="APP功能菜单-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<CjxtMenuPz>> queryPageList(CjxtMenuPz cjxtMenuPz,HttpServletRequest req) {
		QueryWrapper<CjxtMenuPz> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMenuPz, req.getParameterMap());
		List<CjxtMenuPz> list = cjxtMenuPzService.list(queryWrapper);
		IPage<CjxtMenuPz> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
		return Result.OK(pageList);
	}

    /**
      * 批量查询子节点
      * @param parentIds 父ID（多个采用半角逗号分割）
      * @return 返回 IPage
      * @param parentIds
      * @return
      */
	//@AutoLog(value = "APP功能菜单-批量获取子数据")
    @ApiOperation(value="APP功能菜单-批量获取子数据", notes="APP功能菜单-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<CjxtMenuPz> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("pid", parentIdList);
            List<CjxtMenuPz> list = cjxtMenuPzService.list(queryWrapper);
            IPage<CjxtMenuPz> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }
	
	/**
	 *   添加
	 *
	 * @param cjxtMenuPz
	 * @return
	 */
	@AutoLog(value = "APP功能菜单-添加")
	@ApiOperation(value="APP功能菜单-添加", notes="APP功能菜单-添加")
//    @RequiresPermissions("cjxt:cjxt_menu_pz:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtMenuPz cjxtMenuPz) {
		cjxtMenuPzService.addCjxtMenuPz(cjxtMenuPz);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtMenuPz
	 * @return
	 */
	@AutoLog(value = "APP功能菜单-编辑")
	@ApiOperation(value="APP功能菜单-编辑", notes="APP功能菜单-编辑")
//    @RequiresPermissions("cjxt:cjxt_menu_pz:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtMenuPz cjxtMenuPz) {
		cjxtMenuPzService.updateCjxtMenuPz(cjxtMenuPz);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "APP功能菜单-通过id删除")
	@ApiOperation(value="APP功能菜单-通过id删除", notes="APP功能菜单-通过id删除")
//    @RequiresPermissions("cjxt:cjxt_menu_pz:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtMenuPzService.deleteCjxtMenuPz(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "APP功能菜单-批量删除")
	@ApiOperation(value="APP功能菜单-批量删除", notes="APP功能菜单-批量删除")
//    @RequiresPermissions("cjxt:cjxt_menu_pz:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtMenuPzService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "APP功能菜单-通过id查询")
	@ApiOperation(value="APP功能菜单-通过id查询", notes="APP功能菜单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtMenuPz> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtMenuPz cjxtMenuPz = cjxtMenuPzService.getById(id);
		if(cjxtMenuPz==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtMenuPz);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtMenuPz
    */
//    @RequiresPermissions("cjxt:cjxt_menu_pz:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtMenuPz cjxtMenuPz) {
		return super.exportXls(request, cjxtMenuPz, CjxtMenuPz.class, "APP功能菜单");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_menu_pz:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, CjxtMenuPz.class);
    }

}
