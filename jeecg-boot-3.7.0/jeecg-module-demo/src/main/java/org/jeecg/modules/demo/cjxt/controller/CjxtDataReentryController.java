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
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtDataReentry;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpz;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddress;
import org.jeecg.modules.demo.cjxt.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
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
 * @Description: 数据补录表
 * @Author: jeecg-boot
 * @Date:   2024-08-19
 * @Version: V1.0
 */
@Api(tags="数据补录表")
@RestController
@RequestMapping("/cjxt/cjxtDataReentry")
@Slf4j
public class CjxtDataReentryController extends JeecgController<CjxtDataReentry, ICjxtDataReentryService> {
	@Autowired
	private ICjxtDataReentryService cjxtDataReentryService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	@Autowired
	private ICjxtJsmbpzService cjxtJsmbpzService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 分页列表查询
	 *
	 * @param cjxtDataReentry
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "数据补录表-分页列表查询")
	@ApiOperation(value="数据补录表-分页列表查询", notes="数据补录表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtDataReentry>> queryPageList(CjxtDataReentry cjxtDataReentry,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtDataReentry> queryWrapper = QueryGenerator.initQueryWrapper(cjxtDataReentry, req.getParameterMap());
		Page<CjxtDataReentry> page = new Page<CjxtDataReentry>(pageNo, pageSize);
		IPage<CjxtDataReentry> pageList = cjxtDataReentryService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 @ApiOperation(value="数据补录表-分页列表查询", notes="数据补录表-分页列表查询")
	 @GetMapping(value = "/list2")
	 public Result<IPage<CjxtDataReentry>> queryPageList2(CjxtDataReentry cjxtDataReentry,
														 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
														 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
														 HttpServletRequest req) {
		 QueryWrapper<CjxtDataReentry> queryWrapper = QueryGenerator.initQueryWrapper(cjxtDataReentry, req.getParameterMap());
		 queryWrapper.ne("blzt","1");
		 Page<CjxtDataReentry> page = new Page<CjxtDataReentry>(pageNo, pageSize);
		 IPage<CjxtDataReentry> pageList = cjxtDataReentryService.page(page, queryWrapper);
		 return Result.OK(pageList);
	 }

	 @ApiOperation(value="数据补录表-APP模板列表", notes="数据补录表-APP模板列表")
	 @GetMapping(value = "/appMbList")
	 public Result<Map<String, Object>> appMbList(@RequestParam(name="userId", required = false) String userId,
												  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
		 Map<String, Object> result = new HashMap<>();
		 List<Map<String, Object>> records = new ArrayList<>();
		 if (userId != null) {
			 String sql = "SELECT \n" +
					 "cm.*\n" +
					 "FROM sys_user_role su \n" +
					 "INNER JOIN sys_role s ON su.role_id = s.id\n" +
					 "INNER JOIN cjxt_jsmbpz cj ON cj.role_code = s.role_code\n" +
					 "INNER JOIN cjxt_mbgl cm ON cj.mb_id = cm.id \n" +
					 "WHERE cj.del_flag = '0' AND su.user_id = '" + userId + "' " + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

			 List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
			 String wclNumSql = "SELECT mb_code, COUNT(*) AS wclNum FROM cjxt_data_reentry WHERE del_flag = '0' AND blzt = '1' AND receiver_id = '"+userId+"' GROUP BY mb_code ";
			 List<Map<String, Object>> wclList = jdbcTemplate.queryForList(wclNumSql);

			 for (Map<String, Object> row : resultList) {
				 String mbbh = (String) row.get("mbbh");
				 int wclNum = 0;
				 boolean matchFound = false;

				 for (Map<String, Object> wclRow : wclList) {
					 String mb_code = (String) wclRow.get("mb_code");
					 if (mbbh.equals(mb_code)) {
						 wclNum = ((Long) wclRow.get("wclNum")).intValue();
						 matchFound = true;
						 break;
					 }
				 }

				 if (matchFound) {
					 Map<String, Object> dataMap = new HashMap<>(row);
					 dataMap.put("wclNum", wclNum);
					 records.add(dataMap);
				 }
			 }

			 String countSql = "SELECT \n" +
					 "COUNT(*)\n" +
					 "FROM sys_user_role su \n" +
					 "INNER JOIN sys_role s ON su.role_id = s.id\n" +
					 "INNER JOIN cjxt_jsmbpz cj ON cj.role_code = s.role_code\n" +
					 "INNER JOIN cjxt_mbgl cm ON cj.mb_id = cm.id \n" +
					 "WHERE cj.del_flag = '0' AND su.user_id = '" + userId + "'";

			 // 执行查询并获取总条数
			 int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
			 // 计算总页数
			 int totalPages = (int) Math.ceil((double) totalCount / pageSize);
			 // 将总页数添加到结果中
			 result.put("current", pageNo);
			 result.put("size", pageSize);
			 result.put("total", totalCount);
			 result.put("pages", totalPages);
			 result.put("records", records);
		 }
		 return Result.OK(result);
	 }

	 @ApiOperation(value="数据补录表-未处理数量", notes="数据补录表-未处理数量")
	 @GetMapping(value = "/sjblWclNum")
	 public Result<Map<String, Object>> sjblWclNum(@RequestParam(name="userId", required = false) String userId) {
		 Map<String, Object> result = new HashMap<>();
		 if (userId != null) {
			 int sjblNum = 0;
			 List<CjxtDataReentry> reentryList = cjxtDataReentryService.list(new LambdaQueryWrapper<CjxtDataReentry>().eq(CjxtDataReentry::getReceiverId,userId).eq(CjxtDataReentry::getBlzt,"1"));
			 if(reentryList.size()>0){
				 sjblNum = reentryList.size();
			 }
			 result.put("sjblNum", sjblNum);
			 result.put("status", "success");
		 }
		 return Result.OK(result);
	 }

	 @ApiOperation(value="数据补录表-APP/TAB列表", notes="数据补录表-APP/TAB列表")
	 @GetMapping(value = "/applist")
	 public Result<IPage<CjxtDataReentry>> applist(CjxtDataReentry cjxtDataReentry,
												   @RequestParam(name="blzt", required = false) String blzt,
												   @RequestParam(name="userId", required = false) String userId,
												   @RequestParam(name="mbCode", required = false) String mbCode,
												   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												   HttpServletRequest req) {
		 QueryWrapper<CjxtDataReentry> queryWrapper = QueryGenerator.initQueryWrapper(cjxtDataReentry, req.getParameterMap());
		 if(blzt!=null && !"".equals(blzt) && userId!=null && !"".equals(userId) && !"".equals(mbCode) && mbCode!=null){
			 queryWrapper.eq("receiver_id",userId);
			 queryWrapper.eq("blzt",blzt);
			 queryWrapper.eq("mb_code",mbCode);
		 }
		 Page<CjxtDataReentry> page = new Page<CjxtDataReentry>(pageNo, pageSize);
		 IPage<CjxtDataReentry> pageList = cjxtDataReentryService.page(page, queryWrapper);
		 for (CjxtDataReentry dataReentry : pageList.getRecords()) {
			 if(dataReentry.getAddressId()!=null && !"".equals(dataReentry.getAddressId())){
				 CjxtStandardAddress address = cjxtStandardAddressService.getById(dataReentry.getAddressId());
				 String addressName = "";
				 if("1".equals(address.getDzType())){
					 //小区名
					 if(address.getDz1Xqm()!=null && !"".equals(address.getDz1Xqm())){
						 addressName = addressName + address.getDz1Xqm();
					 }
					 //楼栋
					 if(address.getDz1Ld()!=null && !"".equals(address.getDz1Ld())){
						 addressName = addressName + address.getDz1Ld() + "号楼";
					 }
					 //单元
					 if(address.getDz1Dy()!=null && !"".equals(address.getDz1Dy())){
						 addressName = addressName + address.getDz1Dy() + "单元";
					 }
					 //室
					 if(address.getDz1S()!=null && !"".equals(address.getDz1S())){
						 addressName = addressName + address.getDz1S() + "室";
					 }
				 }else if("2".equals(address.getDzType())){
					 address.setDetailMc(address.getDz2Cm());
					 //村名
					 if(address.getDz2Cm()!=null && !"".equals(address.getDz2Cm())){
						 addressName = addressName + address.getDz2Cm();
					 }
					 //组名
					 if(address.getDz2Zm()!=null && !"".equals(address.getDz2Zm())){
						 addressName = addressName + address.getDz2Zm() + "组";
					 }
					 //号名
					 if(address.getDz2Hm()!=null && !"".equals(address.getDz2Hm())){
						 addressName = addressName + address.getDz2Hm() + "号";
					 }

				 }else if("3".equals(address.getDzType())){
					 address.setDetailMc(address.getDz3Dsm());
					 //大厦名
					 if(address.getDz3Dsm()!=null && !"".equals(address.getDz3Dsm())){
						 addressName = addressName + address.getDz3Dsm();
					 }
					 //楼栋名
					 if(address.getDz3Ldm()!=null && !"".equals(address.getDz3Ldm())){
						 addressName = addressName + address.getDz3Ldm() + "栋";
					 }
					 //室名
					 if(address.getDz3Sm()!=null && !"".equals(address.getDz3Sm())){
						 addressName = addressName + address.getDz3Sm() + "室";
					 }
				 }else if("4".equals(address.getDzType())){
					 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
						 addressName = addressName + address.getDetailMc();
					 }
				 }else if("5".equals(address.getDzType())){
					 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
						 addressName = addressName + address.getDetailMc();
					 }
					 if(address.getDz5P()!=null && !"".equals(address.getDz5P())){
						 addressName = addressName + address.getDz5P() + "排";
					 }
					 if(address.getDz5H()!=null && !"".equals(address.getDz5H())){
						 addressName = addressName + address.getDz5H() + "号";
					 }
					 if(address.getDz5S()!=null && !"".equals(address.getDz5S())){
						 addressName = addressName + address.getDz5S() + "室";
					 }

				 }else if("6".equals(address.getDzType())){
					 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
						 addressName = addressName + address.getDetailMc();
					 }
					 if(address.getDz6S()!=null && !"".equals(address.getDz6S())){
						 addressName = addressName + address.getDz6S() + "室";
					 }
				 }else if("99".equals(address.getDzType())){
					 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
						 addressName = addressName + address.getDetailMc();
					 }
				 }
				 dataReentry.setAddressName(addressName);
			 }
		 }
		 return Result.OK(pageList);
	 }

	/**
	 *   添加
	 *
	 * @param cjxtDataReentry
	 * @return
	 */
	@AutoLog(value = "数据补录表-添加")
	@ApiOperation(value="数据补录表-添加", notes="数据补录表-添加")
	//RequiresPermissions("cjxt:cjxt_data_reentry:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtDataReentry cjxtDataReentry) {
//		SysUser byId = sysUserService.getOne(new QueryWrapper<SysUser>().eq("username",cjxtDataReentry.getReceiverName()));
//		CjxtStandardAddress byId1 = cjxtStandardAddressService.getById(cjxtDataReentry.getAddressName());
//		cjxtDataReentry.setAddressId(byId1.getId());
//		cjxtDataReentry.setAddressName(byId1.getAddressName());
//		cjxtDataReentry.setReceiverId(byId.getId());
//		cjxtDataReentry.setReceiverName(byId.getRealname());
		cjxtDataReentryService.save(cjxtDataReentry);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtDataReentry
	 * @return
	 */
	@AutoLog(value = "数据补录表-编辑")
	@ApiOperation(value="数据补录表-编辑", notes="数据补录表-编辑")
	//RequiresPermissions("cjxt:cjxt_data_reentry:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtDataReentry cjxtDataReentry) {
		CjxtDataReentry byId2 = cjxtDataReentryService.getById(cjxtDataReentry.getId());

		if(!byId2.getAddressName().equals(cjxtDataReentry.getAddressName())){
			CjxtStandardAddress byId1 = cjxtStandardAddressService.getById(cjxtDataReentry.getAddressName());
			cjxtDataReentry.setAddressId(byId1.getId());
			cjxtDataReentry.setAddressName(byId1.getAddressName());
		}
		if(!byId2.getReceiverName().equals(cjxtDataReentry.getReceiverName())){
			SysUser byId = sysUserService.getOne(new QueryWrapper<SysUser>().eq("username",cjxtDataReentry.getReceiverName()));
			cjxtDataReentry.setReceiverId(byId.getId());
			cjxtDataReentry.setReceiverName(byId.getRealname());
		}
		cjxtDataReentryService.updateById(cjxtDataReentry);
		if("3".equals(cjxtDataReentry.getBlzt())){
			String updateSql = "UPDATE "+cjxtDataReentry.getBm()+" SET blzt = '0' WHERE del_flag = '0' AND id = '" + cjxtDataReentry.getDataId() +"' ;";
			jdbcTemplate.update(updateSql);
		}
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "数据补录表-通过id删除")
	@ApiOperation(value="数据补录表-通过id删除", notes="数据补录表-通过id删除")
	//RequiresPermissions("cjxt:cjxt_data_reentry:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtDataReentryService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "数据补录表-批量删除")
	@ApiOperation(value="数据补录表-批量删除", notes="数据补录表-批量删除")
	//RequiresPermissions("cjxt:cjxt_data_reentry:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtDataReentryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "数据补录表-通过id查询")
	@ApiOperation(value="数据补录表-通过id查询", notes="数据补录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtDataReentry> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtDataReentry cjxtDataReentry = cjxtDataReentryService.getById(id);
		if(cjxtDataReentry==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtDataReentry);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtDataReentry
    */
    //RequiresPermissions("cjxt:cjxt_data_reentry:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtDataReentry cjxtDataReentry) {
        return super.exportXls(request, cjxtDataReentry, CjxtDataReentry.class, "数据补录表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //RequiresPermissions("cjxt:cjxt_data_reentry:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtDataReentry.class);
    }

}