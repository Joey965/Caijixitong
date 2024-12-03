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
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtMapKey;
import org.jeecg.modules.demo.cjxt.service.ICjxtMapKeyService;

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
 * @Description: 地图应用KEY
 * @Author: jeecg-boot
 * @Date:   2024-11-05
 * @Version: V1.0
 */
@Api(tags="地图应用KEY")
@RestController
@RequestMapping("/cjxt/cjxtMapKey")
@Slf4j
public class CjxtMapKeyController extends JeecgController<CjxtMapKey, ICjxtMapKeyService> {
	@Autowired
	private ICjxtMapKeyService cjxtMapKeyService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtMapKey
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "地图应用KEY-分页列表查询")
	@ApiOperation(value="地图应用KEY-分页列表查询", notes="地图应用KEY-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtMapKey>> queryPageList(CjxtMapKey cjxtMapKey,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtMapKey> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMapKey, parameterMap);
		queryWrapper.orderByAsc("key_num");
		Page<CjxtMapKey> page = new Page<CjxtMapKey>(pageNo, pageSize);
		IPage<CjxtMapKey> pageList = cjxtMapKeyService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtMapKey
	 * @return
	 */
	@AutoLog(value = "地图应用KEY-添加")
	@ApiOperation(value="地图应用KEY-添加", notes="地图应用KEY-添加")
	//@RequiresPermissions("cjxt:cjxt_map_key:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtMapKey cjxtMapKey) {
		try{
			if(!"".equals(cjxtMapKey.getKeyName()) && cjxtMapKey.getKeyName()!=null){
				CjxtMapKey mapKey = cjxtMapKeyService.getOne(new LambdaQueryWrapper<CjxtMapKey>().eq(CjxtMapKey::getKeyName,cjxtMapKey.getKeyName()));
				if(mapKey!=null){
					return Result.error("key名称已存在!!!");
				}
			}
			cjxtMapKeyService.save(cjxtMapKey);
			return Result.OK("添加成功！");
		}catch (Exception e){
			e.printStackTrace();
		}
		return Result.ok("");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtMapKey
	 * @return
	 */
	@AutoLog(value = "地图应用KEY-编辑")
	@ApiOperation(value="地图应用KEY-编辑", notes="地图应用KEY-编辑")
	//@RequiresPermissions("cjxt:cjxt_map_key:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtMapKey cjxtMapKey) {
		try{
			if(!"".equals(cjxtMapKey.getKeyName()) && cjxtMapKey.getKeyName()!=null &&
			   !"".equals(cjxtMapKey.getKeyNameDto()) && cjxtMapKey.getKeyNameDto()!=null &&
			   !cjxtMapKey.getKeyNameDto().equals(cjxtMapKey.getKeyName())){
				CjxtMapKey mapKey = cjxtMapKeyService.getOne(new LambdaQueryWrapper<CjxtMapKey>().eq(CjxtMapKey::getKeyName,cjxtMapKey.getKeyName()));
				if(mapKey!=null){
					return Result.error("key名称已存在!!!");
				}
			}
			cjxtMapKeyService.updateById(cjxtMapKey);
			return Result.OK("编辑成功!");
		}catch (Exception e){
			e.printStackTrace();
		}
		return Result.ok("");
	}

	 @ApiOperation(value="地图应用KEY-获取key信息", notes="地图应用KEY-获取key信息")
	 @GetMapping(value = "/queryMapKey")
	 public Result<CjxtMapKey> queryB() {
		 CjxtMapKey cjxtMapKey = cjxtMapKeyService.getOne(new LambdaQueryWrapper<CjxtMapKey>().eq(CjxtMapKey::getKeyStatus,"1").eq(CjxtMapKey::getSfqy,"1").orderByAsc(CjxtMapKey::getKeyNum).last(" LIMIT 1"));
		 if(cjxtMapKey==null) {
			 return Result.error("未找到对应数据");
		 }
		 return Result.OK(cjxtMapKey);
	 }

	 @ApiOperation(value="地图应用KEY-修改key状态", notes="地图应用KEY-修改key状态")
	 @PostMapping(value = "/updateKeyStatus")
	 public Result<String> updateKeyStatus(@RequestParam(name="id",required=true) String id) {
		 CjxtMapKey cjxtMapKey = cjxtMapKeyService.getById(id);
		 cjxtMapKey.setKeyStatus("0");
		 cjxtMapKeyService.updateById(cjxtMapKey);
		 List<CjxtMapKey> mapKeyList = cjxtMapKeyService.list(new LambdaQueryWrapper<CjxtMapKey>().eq(CjxtMapKey::getKeyStatus,"1"));
		 if(mapKeyList.size()==0){
			 String updateSql = "UPDATE cjxt_map_key SET key_status = '1' ;";
			 jdbcTemplate.update(updateSql);
		 }
		 return Result.OK();
	 }
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "地图应用KEY-通过id删除")
	@ApiOperation(value="地图应用KEY-通过id删除", notes="地图应用KEY-通过id删除")
	//@RequiresPermissions("cjxt:cjxt_map_key:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtMapKeyService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "地图应用KEY-批量删除")
	@ApiOperation(value="地图应用KEY-批量删除", notes="地图应用KEY-批量删除")
	//@RequiresPermissions("cjxt:cjxt_map_key:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtMapKeyService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "地图应用KEY-通过id查询")
	@ApiOperation(value="地图应用KEY-通过id查询", notes="地图应用KEY-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtMapKey> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtMapKey cjxtMapKey = cjxtMapKeyService.getById(id);
		if(cjxtMapKey==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtMapKey);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtMapKey
    */
    //@RequiresPermissions("cjxt:cjxt_map_key:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtMapKey cjxtMapKey) {
        return super.exportXls(request, cjxtMapKey, CjxtMapKey.class, "地图应用KEY");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("cjxt:cjxt_map_key:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtMapKey.class);
    }

}
