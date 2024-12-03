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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddress;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddressOrg;
import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressOrgService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressService;
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
 * @Description: 部门地址授权
 * @Author: jeecg-boot
 * @Date:   2024-06-13
 * @Version: V1.0
 */
@Api(tags="部门地址授权")
@RestController
@RequestMapping("/cjxt/cjxtStandardAddressOrg")
@Slf4j
public class CjxtStandardAddressOrgController extends JeecgController<CjxtStandardAddressOrg, ICjxtStandardAddressOrgService> {
	@Autowired
	private ICjxtStandardAddressOrgService cjxtStandardAddressOrgService;
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtStandardAddressOrg
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "部门地址授权-分页列表查询")
	@ApiOperation(value="部门地址授权-分页列表查询", notes="部门地址授权-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtStandardAddressOrg>> queryPageList(CjxtStandardAddressOrg cjxtStandardAddressOrg,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtStandardAddressOrg> queryWrapper = QueryGenerator.initQueryWrapper(cjxtStandardAddressOrg, parameterMap);
		queryWrapper.orderByDesc("address_name");
		Page<CjxtStandardAddressOrg> page = new Page<CjxtStandardAddressOrg>(pageNo, pageSize);
		IPage<CjxtStandardAddressOrg> pageList = cjxtStandardAddressOrgService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 /**
	 *   添加
	 *
	 * @param cjxtStandardAddressOrg
	 * @return
	 */
	@AutoLog(value = "部门地址授权-添加")
	@ApiOperation(value="部门地址授权-添加", notes="部门地址授权-添加")
//	@RequiresPermissions("cjxt:cjxt_standard_address_org:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtStandardAddressOrg cjxtStandardAddressOrg) {
		String[] sysDepartIds = cjxtStandardAddressOrg.getOrgId().split(",");
		String[] sysDepartNames = cjxtStandardAddressOrg.getOrgName().split(",");
		String[] addressIds = cjxtStandardAddressOrg.getAddressId().split(",");
		String[] addressCodes = cjxtStandardAddressOrg.getAddressCode().split(",");
		String[] addressNames = cjxtStandardAddressOrg.getAddressName().split(",");

		for (int i = 0; i < sysDepartIds.length; i++) {
			String sysDepartId = sysDepartIds[i];
			String sysDepartName = sysDepartNames[i];
			for (int j = 0; j < addressIds.length; j++) {
				String addressId = addressIds[j];
				String addressCode = addressCodes[j];
				String addressName = addressNames[j];
				//检查是否存在
				QueryWrapper<CjxtStandardAddressOrg> queryWrapper = new QueryWrapper<CjxtStandardAddressOrg>()
						.eq("org_id", sysDepartId)
						.eq("address_id", addressId);
				CjxtStandardAddressOrg existingJsjbBmfgld = cjxtStandardAddressOrgService.getOne(queryWrapper);
				if (existingJsjbBmfgld != null) {
					// 如果数据已经存在，则先删除
					cjxtStandardAddressOrgService.remove(queryWrapper);
				}

				CjxtStandardAddressOrg newCjxtStandardAddressOrg = new CjxtStandardAddressOrg();
				newCjxtStandardAddressOrg.setOrgId(sysDepartId);
				newCjxtStandardAddressOrg.setOrgName(sysDepartName);
				newCjxtStandardAddressOrg.setAddressId(addressId);
				newCjxtStandardAddressOrg.setAddressCode(addressCode);
				newCjxtStandardAddressOrg.setAddressName(addressName);

				// 保存新对象
				cjxtStandardAddressOrgService.save(newCjxtStandardAddressOrg);
			}
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtStandardAddressOrg
	 * @return
	 */
	@AutoLog(value = "部门地址授权-编辑")
	@ApiOperation(value="部门地址授权-编辑", notes="部门地址授权-编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address_org:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtStandardAddressOrg cjxtStandardAddressOrg) {
		cjxtStandardAddressOrgService.removeById(cjxtStandardAddressOrg.getId());

		String[] sysDepartIds = cjxtStandardAddressOrg.getOrgId().split(",");
		String[] sysDepartNames = cjxtStandardAddressOrg.getOrgName().split(",");
		String[] addressIds = cjxtStandardAddressOrg.getAddressId().split(",");
		String[] addressCodes = cjxtStandardAddressOrg.getAddressCode().split(",");
		String[] addressNames = cjxtStandardAddressOrg.getAddressName().split(",");

		for (int i = 0; i < sysDepartIds.length; i++) {
			String sysDepartId = sysDepartIds[i];
			String sysDepartName = sysDepartNames[i];
			for (int j = 0; j < addressIds.length; j++) {
				String addressId = addressIds[j];
				String addressCode = addressCodes[j];
				String addressName = addressNames[j];
				//检查是否存在
				QueryWrapper<CjxtStandardAddressOrg> queryWrapper = new QueryWrapper<CjxtStandardAddressOrg>()
						.eq("org_id", sysDepartId)
						.eq("address_id", addressId);
				CjxtStandardAddressOrg existingJsjbBmfgld = cjxtStandardAddressOrgService.getOne(queryWrapper);
				if (existingJsjbBmfgld != null) {
					// 如果数据已经存在，则先删除
					cjxtStandardAddressOrgService.remove(queryWrapper);
				}

				CjxtStandardAddressOrg newCjxtStandardAddressOrg = new CjxtStandardAddressOrg();
				newCjxtStandardAddressOrg.setOrgId(sysDepartId);
				newCjxtStandardAddressOrg.setOrgName(sysDepartName);
				newCjxtStandardAddressOrg.setAddressId(addressId);
				newCjxtStandardAddressOrg.setAddressCode(addressCode);
				newCjxtStandardAddressOrg.setAddressName(addressName);

				// 保存新对象
				cjxtStandardAddressOrgService.save(newCjxtStandardAddressOrg);
			}
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "部门地址授权-通过id删除")
	@ApiOperation(value="部门地址授权-通过id删除", notes="部门地址授权-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_org:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtStandardAddressOrgService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "部门地址授权-批量删除")
	@ApiOperation(value="部门地址授权-批量删除", notes="部门地址授权-批量删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_org:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtStandardAddressOrgService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "部门地址授权-通过id查询")
	@ApiOperation(value="部门地址授权-通过id查询", notes="部门地址授权-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtStandardAddressOrg> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtStandardAddressOrg cjxtStandardAddressOrg = cjxtStandardAddressOrgService.getById(id);
		if(cjxtStandardAddressOrg==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtStandardAddressOrg);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtStandardAddressOrg
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_org:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtStandardAddressOrg cjxtStandardAddressOrg) {
        return super.exportXls(request, cjxtStandardAddressOrg, CjxtStandardAddressOrg.class, "部门地址授权");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_org:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtStandardAddressOrg.class);
    }

}
