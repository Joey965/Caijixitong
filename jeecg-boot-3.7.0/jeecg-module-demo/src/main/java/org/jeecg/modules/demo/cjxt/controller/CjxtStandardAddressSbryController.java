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
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddressSbry;
import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressSbryService;

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
 * @Description: 申报地址授权
 * @Author: jeecg-boot
 * @Date:   2024-06-14
 * @Version: V1.0
 */
@Api(tags="申报地址授权")
@RestController
@RequestMapping("/cjxt/cjxtStandardAddressSbry")
@Slf4j
public class CjxtStandardAddressSbryController extends JeecgController<CjxtStandardAddressSbry, ICjxtStandardAddressSbryService> {
	@Autowired
	private ICjxtStandardAddressSbryService cjxtStandardAddressSbryService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtStandardAddressSbry
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "申报地址授权-分页列表查询")
	@ApiOperation(value="申报地址授权-分页列表查询", notes="申报地址授权-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtStandardAddressSbry>> queryPageList(CjxtStandardAddressSbry cjxtStandardAddressSbry,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtStandardAddressSbry> queryWrapper = QueryGenerator.initQueryWrapper(cjxtStandardAddressSbry, parameterMap);
		queryWrapper.orderByDesc("address_name");
		Page<CjxtStandardAddressSbry> page = new Page<CjxtStandardAddressSbry>(pageNo, pageSize);
		IPage<CjxtStandardAddressSbry> pageList = cjxtStandardAddressSbryService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtStandardAddressSbry
	 * @return
	 */
	@AutoLog(value = "申报地址授权-添加")
	@ApiOperation(value="申报地址授权-添加", notes="申报地址授权-添加")
//	@RequiresPermissions("cjxt:cjxt_standard_address_sbry:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtStandardAddressSbry cjxtStandardAddressSbry) {
		String[] addressIds = cjxtStandardAddressSbry.getAddressId().split(",");
		String[] addressCodes = cjxtStandardAddressSbry.getAddressCode().split(",");
		String[] addressNames = cjxtStandardAddressSbry.getAddressName().split(",");

		for (int j = 0; j < addressIds.length; j++) {
			String addressId = addressIds[j];
			String addressCode = addressCodes[j];
			String addressName = addressNames[j];
			//检查是否存在
			QueryWrapper<CjxtStandardAddressSbry> queryWrapper = new QueryWrapper<CjxtStandardAddressSbry>()
					.eq("phone", cjxtStandardAddressSbry.getPhone())
					.eq("address_id", addressId);
			CjxtStandardAddressSbry sbryServiceOne = cjxtStandardAddressSbryService.getOne(queryWrapper);
			if (sbryServiceOne != null) {
				// 如果数据已经存在，则先删除
				cjxtStandardAddressSbryService.remove(queryWrapper);
			}

			CjxtStandardAddressSbry newCjxtStandardAddressPerson = new CjxtStandardAddressSbry();
			newCjxtStandardAddressPerson.setUserName(cjxtStandardAddressSbry.getUserName());
			newCjxtStandardAddressPerson.setPhone(cjxtStandardAddressSbry.getPhone());
			newCjxtStandardAddressPerson.setRyjs(cjxtStandardAddressSbry.getRyjs());
			newCjxtStandardAddressPerson.setAddressId(addressId);
			newCjxtStandardAddressPerson.setAddressCode(addressCode);
			newCjxtStandardAddressPerson.setAddressName(addressName);

			// 保存新对象
			cjxtStandardAddressSbryService.save(newCjxtStandardAddressPerson);
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtStandardAddressSbry
	 * @return
	 */
	@AutoLog(value = "申报地址授权-编辑")
	@ApiOperation(value="申报地址授权-编辑", notes="申报地址授权-编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address_sbry:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtStandardAddressSbry cjxtStandardAddressSbry) {
		cjxtStandardAddressSbryService.removeById(cjxtStandardAddressSbry.getId());
		String[] addressIds = cjxtStandardAddressSbry.getAddressId().split(",");
		String[] addressCodes = cjxtStandardAddressSbry.getAddressCode().split(",");
		String[] addressNames = cjxtStandardAddressSbry.getAddressName().split(",");

		for (int j = 0; j < addressIds.length; j++) {
			String addressId = addressIds[j];
			String addressCode = addressCodes[j];
			String addressName = addressNames[j];
			//检查是否存在
			QueryWrapper<CjxtStandardAddressSbry> queryWrapper = new QueryWrapper<CjxtStandardAddressSbry>()
					.eq("phone", cjxtStandardAddressSbry.getPhone())
					.eq("address_id", addressId);
			CjxtStandardAddressSbry sbryServiceOne = cjxtStandardAddressSbryService.getOne(queryWrapper);
			if (sbryServiceOne != null) {
				// 如果数据已经存在，则先删除
				cjxtStandardAddressSbryService.remove(queryWrapper);
			}

			CjxtStandardAddressSbry newCjxtStandardAddressPerson = new CjxtStandardAddressSbry();
			newCjxtStandardAddressPerson.setUserName(cjxtStandardAddressSbry.getUserName());
			newCjxtStandardAddressPerson.setPhone(cjxtStandardAddressSbry.getPhone());
			newCjxtStandardAddressPerson.setRyjs(cjxtStandardAddressSbry.getRyjs());
			newCjxtStandardAddressPerson.setAddressId(addressId);
			newCjxtStandardAddressPerson.setAddressCode(addressCode);
			newCjxtStandardAddressPerson.setAddressName(addressName);

			// 保存新对象
			cjxtStandardAddressSbryService.save(newCjxtStandardAddressPerson);
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "申报地址授权-通过id删除")
	@ApiOperation(value="申报地址授权-通过id删除", notes="申报地址授权-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_sbry:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtStandardAddressSbryService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "申报地址授权-批量删除")
	@ApiOperation(value="申报地址授权-批量删除", notes="申报地址授权-批量删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_sbry:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtStandardAddressSbryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "申报地址授权-通过id查询")
	@ApiOperation(value="申报地址授权-通过id查询", notes="申报地址授权-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtStandardAddressSbry> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtStandardAddressSbry cjxtStandardAddressSbry = cjxtStandardAddressSbryService.getById(id);
		if(cjxtStandardAddressSbry==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtStandardAddressSbry);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtStandardAddressSbry
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_sbry:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtStandardAddressSbry cjxtStandardAddressSbry) {
        return super.exportXls(request, cjxtStandardAddressSbry, CjxtStandardAddressSbry.class, "申报地址授权");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_sbry:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtStandardAddressSbry.class);
    }

}
