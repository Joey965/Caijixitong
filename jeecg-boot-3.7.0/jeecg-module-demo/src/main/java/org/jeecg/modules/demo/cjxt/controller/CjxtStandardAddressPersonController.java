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
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddressPerson;
import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressPersonService;

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
 * @Description: 人员地址授权
 * @Author: jeecg-boot
 * @Date:   2024-06-14
 * @Version: V1.0
 */
@Api(tags="人员地址授权")
@RestController
@RequestMapping("/cjxt/cjxtStandardAddressPerson")
@Slf4j
public class CjxtStandardAddressPersonController extends JeecgController<CjxtStandardAddressPerson, ICjxtStandardAddressPersonService> {
	@Autowired
	private ICjxtStandardAddressPersonService cjxtStandardAddressPersonService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtStandardAddressPerson
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "人员地址授权-分页列表查询")
	@ApiOperation(value="人员地址授权-分页列表查询", notes="人员地址授权-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtStandardAddressPerson>> queryPageList(CjxtStandardAddressPerson cjxtStandardAddressPerson,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtStandardAddressPerson> queryWrapper = QueryGenerator.initQueryWrapper(cjxtStandardAddressPerson, parameterMap);
		queryWrapper.orderByDesc("address_name");
		Page<CjxtStandardAddressPerson> page = new Page<CjxtStandardAddressPerson>(pageNo, pageSize);
		IPage<CjxtStandardAddressPerson> pageList = cjxtStandardAddressPersonService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtStandardAddressPerson
	 * @return
	 */
	@AutoLog(value = "人员地址授权-添加")
	@ApiOperation(value="人员地址授权-添加", notes="人员地址授权-添加")
//	@RequiresPermissions("cjxt:cjxt_standard_address_person:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtStandardAddressPerson cjxtStandardAddressPerson) {
		String[] addressIds = cjxtStandardAddressPerson.getAddressId().split(",");
		String[] addressCodes = cjxtStandardAddressPerson.getAddressCode().split(",");
		String[] addressNames = cjxtStandardAddressPerson.getAddressName().split(",");

		for (int j = 0; j < addressIds.length; j++) {
			String addressId = addressIds[j];
			String addressCode = addressCodes[j];
			String addressName = addressNames[j];
			//检查是否存在
			QueryWrapper<CjxtStandardAddressPerson> queryWrapper = new QueryWrapper<CjxtStandardAddressPerson>()
					.eq("user_id", cjxtStandardAddressPerson.getUserId())
					.eq("address_id", addressId);
			CjxtStandardAddressPerson existingJsjbBmfgld = cjxtStandardAddressPersonService.getOne(queryWrapper);
			if (existingJsjbBmfgld != null) {
				// 如果数据已经存在，则先删除
				cjxtStandardAddressPersonService.remove(queryWrapper);
			}

			CjxtStandardAddressPerson newCjxtStandardAddressPerson = new CjxtStandardAddressPerson();
			newCjxtStandardAddressPerson.setUserId(cjxtStandardAddressPerson.getUserId());
			newCjxtStandardAddressPerson.setUserName(cjxtStandardAddressPerson.getUserName());
			newCjxtStandardAddressPerson.setPhone(cjxtStandardAddressPerson.getPhone());
			newCjxtStandardAddressPerson.setRyjs(cjxtStandardAddressPerson.getRyjs());
			newCjxtStandardAddressPerson.setAddressId(addressId);
			newCjxtStandardAddressPerson.setAddressCode(addressCode);
			newCjxtStandardAddressPerson.setAddressName(addressName);

			// 保存新对象
			cjxtStandardAddressPersonService.save(newCjxtStandardAddressPerson);
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtStandardAddressPerson
	 * @return
	 */
	@AutoLog(value = "人员地址授权-编辑")
	@ApiOperation(value="人员地址授权-编辑", notes="人员地址授权-编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address_person:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtStandardAddressPerson cjxtStandardAddressPerson) {
		//先删除数据
		cjxtStandardAddressPersonService.removeById(cjxtStandardAddressPerson.getId());

		String[] addressIds = cjxtStandardAddressPerson.getAddressId().split(",");
		String[] addressCodes = cjxtStandardAddressPerson.getAddressCode().split(",");
		String[] addressNames = cjxtStandardAddressPerson.getAddressName().split(",");
		for (int j = 0; j < addressIds.length; j++) {
			String addressId = addressIds[j];
			String addressCode = addressCodes[j];
			String addressName = addressNames[j];
			//检查是否存在
			QueryWrapper<CjxtStandardAddressPerson> queryWrapper = new QueryWrapper<CjxtStandardAddressPerson>()
					.eq("user_id", cjxtStandardAddressPerson.getUserId())
					.eq("address_id", addressId);
			CjxtStandardAddressPerson existingJsjbBmfgld = cjxtStandardAddressPersonService.getOne(queryWrapper);
			if (existingJsjbBmfgld != null) {
				// 如果数据已经存在，则先删除
				cjxtStandardAddressPersonService.remove(queryWrapper);
			}

			CjxtStandardAddressPerson newCjxtStandardAddressPerson = new CjxtStandardAddressPerson();
			newCjxtStandardAddressPerson.setUserId(cjxtStandardAddressPerson.getUserId());
			newCjxtStandardAddressPerson.setUserName(cjxtStandardAddressPerson.getUserName());
			newCjxtStandardAddressPerson.setPhone(cjxtStandardAddressPerson.getPhone());
			newCjxtStandardAddressPerson.setRyjs(cjxtStandardAddressPerson.getRyjs());
			newCjxtStandardAddressPerson.setAddressId(addressId);
			newCjxtStandardAddressPerson.setAddressCode(addressCode);
			newCjxtStandardAddressPerson.setAddressName(addressName);

			// 保存新对象
			cjxtStandardAddressPersonService.save(newCjxtStandardAddressPerson);
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "人员地址授权-通过id删除")
	@ApiOperation(value="人员地址授权-通过id删除", notes="人员地址授权-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_person:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtStandardAddressPersonService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "人员地址授权-批量删除")
	@ApiOperation(value="人员地址授权-批量删除", notes="人员地址授权-批量删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_person:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtStandardAddressPersonService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "人员地址授权-通过id查询")
	@ApiOperation(value="人员地址授权-通过id查询", notes="人员地址授权-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtStandardAddressPerson> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtStandardAddressPerson cjxtStandardAddressPerson = cjxtStandardAddressPersonService.getById(id);
		if(cjxtStandardAddressPerson==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtStandardAddressPerson);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtStandardAddressPerson
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_person:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtStandardAddressPerson cjxtStandardAddressPerson) {
        return super.exportXls(request, cjxtStandardAddressPerson, CjxtStandardAddressPerson.class, "人员地址授权");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_person:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtStandardAddressPerson.class);
    }

}
