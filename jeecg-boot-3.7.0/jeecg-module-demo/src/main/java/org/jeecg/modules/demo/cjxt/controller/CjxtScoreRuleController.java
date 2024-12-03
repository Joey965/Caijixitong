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
import org.jeecg.modules.demo.cjxt.entity.CjxtScoreRule;
import org.jeecg.modules.demo.cjxt.service.ICjxtScoreRuleService;

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
 * @Description: 积分规则
 * @Author: jeecg-boot
 * @Date:   2024-06-17
 * @Version: V1.0
 */
@Api(tags="积分规则")
@RestController
@RequestMapping("/cjxt/cjxtScoreRule")
@Slf4j
public class CjxtScoreRuleController extends JeecgController<CjxtScoreRule, ICjxtScoreRuleService> {
	@Autowired
	private ICjxtScoreRuleService cjxtScoreRuleService;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtScoreRule
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "积分规则-分页列表查询")
	@ApiOperation(value="积分规则-分页列表查询", notes="积分规则-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtScoreRule>> queryPageList(CjxtScoreRule cjxtScoreRule,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtScoreRule> queryWrapper = QueryGenerator.initQueryWrapper(cjxtScoreRule, req.getParameterMap());
		Page<CjxtScoreRule> page = new Page<CjxtScoreRule>(pageNo, pageSize);
		IPage<CjxtScoreRule> pageList = cjxtScoreRuleService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtScoreRule
	 * @return
	 */
	@AutoLog(value = "积分规则-添加")
	@ApiOperation(value="积分规则-添加", notes="积分规则-添加")
//	@RequiresPermissions("cjxt:cjxt_score_rule:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtScoreRule cjxtScoreRule) {
		String[] mbIds = cjxtScoreRule.getMbId().split(",");
		String[] mbNames = cjxtScoreRule.getMbName().split(",");
		String[] mbCodes = cjxtScoreRule.getMbCode().split(",");

		for (int i = 0; i < mbIds.length; i++) {
			String mbId = mbIds[i];
			String mbName = mbNames[i];
			String mbCode = mbCodes[i];
			//检查是否存在
			QueryWrapper<CjxtScoreRule> queryWrapper = new QueryWrapper<CjxtScoreRule>()
					.eq("mb_id", cjxtScoreRule.getMbId());
			CjxtScoreRule cjxtScoreRuleOne = cjxtScoreRuleService.getOne(queryWrapper);
			if (cjxtScoreRuleOne != null) {
				// 如果数据已经存在，则先删除
				cjxtScoreRuleService.remove(queryWrapper);
			}

			CjxtScoreRule newCjxtScoreRule = new CjxtScoreRule();
			newCjxtScoreRule.setRuleName(cjxtScoreRule.getRuleName());
			newCjxtScoreRule.setDescription(cjxtScoreRule.getDescription());
			newCjxtScoreRule.setScoreType(cjxtScoreRule.getScoreType());
			newCjxtScoreRule.setScoreValue(cjxtScoreRule.getScoreValue());
			newCjxtScoreRule.setMbId(mbId);
			newCjxtScoreRule.setMbCode(mbCode);
			newCjxtScoreRule.setMbName(mbName);
			// 保存新对象
			cjxtScoreRuleService.save(newCjxtScoreRule);
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtScoreRule
	 * @return
	 */
	@AutoLog(value = "积分规则-编辑")
	@ApiOperation(value="积分规则-编辑", notes="积分规则-编辑")
//	@RequiresPermissions("cjxt:cjxt_score_rule:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtScoreRule cjxtScoreRule) {
		cjxtScoreRuleService.removeById(cjxtScoreRule.getId());
		String[] mbIds = cjxtScoreRule.getMbId().split(",");
		String[] mbNames = cjxtScoreRule.getMbName().split(",");
		String[] mbCodes = cjxtScoreRule.getMbCode().split(",");

		for (int i = 0; i < mbIds.length; i++) {
			String mbId = mbIds[i];
			String mbName = mbNames[i];
			String mbCode = mbCodes[i];
			//检查是否存在
			QueryWrapper<CjxtScoreRule> queryWrapper = new QueryWrapper<CjxtScoreRule>()
					.eq("mb_id", cjxtScoreRule.getMbId());
			CjxtScoreRule cjxtScoreRuleOne = cjxtScoreRuleService.getOne(queryWrapper);
			if (cjxtScoreRuleOne != null) {
				// 如果数据已经存在，则先删除
				cjxtScoreRuleService.remove(queryWrapper);
			}

			CjxtScoreRule newCjxtScoreRule = new CjxtScoreRule();
			newCjxtScoreRule.setRuleName(cjxtScoreRule.getRuleName());
			newCjxtScoreRule.setDescription(cjxtScoreRule.getDescription());
			newCjxtScoreRule.setScoreType(cjxtScoreRule.getScoreType());
			newCjxtScoreRule.setScoreValue(cjxtScoreRule.getScoreValue());
			newCjxtScoreRule.setMbId(mbId);
			newCjxtScoreRule.setMbCode(mbCode);
			newCjxtScoreRule.setMbName(mbName);
			// 保存新对象
			cjxtScoreRuleService.save(newCjxtScoreRule);
		}
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "积分规则-通过id删除")
	@ApiOperation(value="积分规则-通过id删除", notes="积分规则-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_score_rule:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtScoreRuleService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "积分规则-批量删除")
	@ApiOperation(value="积分规则-批量删除", notes="积分规则-批量删除")
//	@RequiresPermissions("cjxt:cjxt_score_rule:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtScoreRuleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "积分规则-通过id查询")
	@ApiOperation(value="积分规则-通过id查询", notes="积分规则-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtScoreRule> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtScoreRule cjxtScoreRule = cjxtScoreRuleService.getById(id);
		if(cjxtScoreRule==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtScoreRule);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtScoreRule
    */
//    @RequiresPermissions("cjxt:cjxt_score_rule:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtScoreRule cjxtScoreRule) {
        return super.exportXls(request, cjxtScoreRule, CjxtScoreRule.class, "积分规则");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_score_rule:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtScoreRule.class);
    }

}
