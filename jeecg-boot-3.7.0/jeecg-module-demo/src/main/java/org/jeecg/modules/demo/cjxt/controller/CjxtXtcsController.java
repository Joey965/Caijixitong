package org.jeecg.modules.demo.cjxt.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.cjxt.utils.MyStartupListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

import static org.jeecg.modules.demo.cjxt.utils.RsaUtil.decryptRes;
import static org.jeecg.modules.demo.cjxt.utils.RsaUtil.encryptRes;


/**
 * @Description: 系统参数
 * @Author: jeecg-boot
 * @Date:   2024-07-03
 * @Version: V1.0
 */
@Api(tags="系统参数")
@RestController
@RequestMapping("/cjxt/cjxtXtcs")
@Slf4j
public class CjxtXtcsController extends JeecgController<CjxtXtcs, ICjxtXtcsService> {
	@Autowired
	private ICjxtXtcsService cjxtXtcsService;

	@Autowired
	private MyStartupListener myStartupListener;


	/**
      * 分页列表查询
      *
      * @param cjxtXtcs
      * @param pageNo
      * @param pageSize
      * @param req
      * @return
      */
	//@AutoLog(value = "系统参数-分页列表查询")
	@ApiOperation(value="系统参数-分页列表查询", notes="系统参数-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtXtcs>> queryPageList(CjxtXtcs cjxtXtcs,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtXtcs> queryWrapper = QueryGenerator.initQueryWrapper(cjxtXtcs, parameterMap);
		queryWrapper.orderByAsc("depart_order").orderByAsc("cs_key");
		Page<CjxtXtcs> page = new Page<CjxtXtcs>(pageNo, pageSize);
		IPage<CjxtXtcs> pageList = cjxtXtcsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param cjxtXtcs
	 * @return
	 */
	@AutoLog(value = "系统参数-添加")
	@ApiOperation(value="系统参数-添加", notes="系统参数-添加")
//	@RequiresPermissions("cjxt:cjxt_xtcs:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtXtcs cjxtXtcs) {
		CjxtXtcs xtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,cjxtXtcs.getCsKey()).last("LIMIT 1"));
		if(xtcs!=null){
			return Result.error("当前参数已存在!");
		}
		if("AESJMKEY".equals(cjxtXtcs.getCsKey())){
			String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDj9qP6motJuk2wvj5x15f0GuaKux7g2IgSqr5F\r\n/RY98789bErqfB9ndJFe8dVsYUCeaycjf+R3UUn13dY5iPaP8gIRjcCy1bRQYAkLH7qhSJcBAkTI\r\navaOqwe89fVAUvmG4vBx3MrGSWxN9JojXdd6dIN3oZxUh0ICsbLsQjJV8wIDAQAB";
			String content = cjxtXtcs.getCsVal();
			String mw = encryptRes(content,publicKey);
			cjxtXtcs.setCsVal(mw);
		}
		if("AESJMIV".equals(cjxtXtcs.getCsKey())){
			String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDj9qP6motJuk2wvj5x15f0GuaKux7g2IgSqr5F\r\n/RY98789bErqfB9ndJFe8dVsYUCeaycjf+R3UUn13dY5iPaP8gIRjcCy1bRQYAkLH7qhSJcBAkTI\r\navaOqwe89fVAUvmG4vBx3MrGSWxN9JojXdd6dIN3oZxUh0ICsbLsQjJV8wIDAQAB";
			String content = cjxtXtcs.getCsVal();
			String mw = encryptRes(content,publicKey);
			cjxtXtcs.setCsVal(mw);
		}
		cjxtXtcsService.save(cjxtXtcs);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtXtcs
	 * @return
	 */
	@AutoLog(value = "系统参数-编辑")
	@ApiOperation(value="系统参数-编辑", notes="系统参数-编辑")
//	@RequiresPermissions("cjxt:cjxt_xtcs:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtXtcs cjxtXtcs) {
		if(!cjxtXtcs.getCsKeyDto().equals(cjxtXtcs.getCsKey())){
			CjxtXtcs xtcs = cjxtXtcsService.getOne(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,cjxtXtcs.getCsKey()).last("LIMIT 1"));
			if(xtcs!=null){
				return Result.error("当前参数已存在!");
			}
		}
		if("AESJMKEY".equals(cjxtXtcs.getCsKey())){
			String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDj9qP6motJuk2wvj5x15f0GuaKux7g2IgSqr5F\r\n/RY98789bErqfB9ndJFe8dVsYUCeaycjf+R3UUn13dY5iPaP8gIRjcCy1bRQYAkLH7qhSJcBAkTI\r\navaOqwe89fVAUvmG4vBx3MrGSWxN9JojXdd6dIN3oZxUh0ICsbLsQjJV8wIDAQAB";
			String content = cjxtXtcs.getCsVal();
			String mw = encryptRes(content,publicKey);
			cjxtXtcs.setCsVal(mw);
		}
		if("AESJMIV".equals(cjxtXtcs.getCsKey())){
			String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDj9qP6motJuk2wvj5x15f0GuaKux7g2IgSqr5F\r\n/RY98789bErqfB9ndJFe8dVsYUCeaycjf+R3UUn13dY5iPaP8gIRjcCy1bRQYAkLH7qhSJcBAkTI\r\navaOqwe89fVAUvmG4vBx3MrGSWxN9JojXdd6dIN3oZxUh0ICsbLsQjJV8wIDAQAB";
			String content = cjxtXtcs.getCsVal();
			String mw = encryptRes(content,publicKey);
			cjxtXtcs.setCsVal(mw);
		}
		cjxtXtcsService.updateById(cjxtXtcs);
		if(cjxtXtcs.getCsKey().equals("fileUrl")){
			if(cjxtXtcs.getSfqy().equals("1")){
				myStartupListener.startMonitoring();
			}else{
				myStartupListener.stopMonitoring();
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
	@AutoLog(value = "系统参数-通过id删除")
	@ApiOperation(value="系统参数-通过id删除", notes="系统参数-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_xtcs:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtXtcsService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "系统参数-批量删除")
	@ApiOperation(value="系统参数-批量删除", notes="系统参数-批量删除")
//	@RequiresPermissions("cjxt:cjxt_xtcs:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtXtcsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "系统参数-通过id查询")
	@ApiOperation(value="系统参数-通过id查询", notes="系统参数-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtXtcs> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtXtcs cjxtXtcs = cjxtXtcsService.getById(id);
		if(cjxtXtcs==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtXtcs);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtXtcs
    */
//    @RequiresPermissions("cjxt:cjxt_xtcs:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtXtcs cjxtXtcs) {
        return super.exportXls(request, cjxtXtcs, CjxtXtcs.class, "系统参数");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_xtcs:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtXtcs.class);
    }
	 /**
	  * 通过csKey查询
	  *
	  * @param csKey
	  * @return
	  */
	 //@AutoLog(value = "系统参数-通过csKey查询")
	 @ApiOperation(value="系统参数-通过csKey查询", notes="系统参数-通过csKey查询")
	 @GetMapping(value = "/queryByCsKey")
	 public Result<CjxtXtcs> queryByCsKey(@RequestParam(name="csKey",required=true) String csKey) {
		 QueryWrapper<CjxtXtcs> queryWrapper = new QueryWrapper<>();
		 queryWrapper.eq("cs_key", csKey);
		 CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(queryWrapper);
		 if(cjxtXtcs==null) {
			 return Result.error("未找到对应数据");
		 }
		 return Result.OK(cjxtXtcs);
	 }

	/**
	 * 查询加密值
	 * @return
	 */
	@ApiOperation(value="系统参数-查询加密值", notes="系统参数-查询加密值")
	@GetMapping(value = "/queryByJm")
	public Result<List<CjxtXtcs>> queryByJm() {
		List<CjxtXtcs> cjxtList = cjxtXtcsService.list(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"AESJMIV").or().eq(CjxtXtcs::getCsKey,"AESJMKEY"));
//		for(CjxtXtcs xtcs: cjxtList){
//			String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOP2o/qai0m6TbC+PnHXl/Qa5oq7\r\nHuDYiBKqvkX9Fj3zvz1sSup8H2d0kV7x1WxhQJ5rJyN/5HdRSfXd1jmI9o/yAhGNwLLVtFBgCQsf\r\nuqFIlwECRMhq9o6rB7z19UBS+Ybi8HHcysZJbE30miNd13p0g3ehnFSHQgKxsuxCMlXzAgMBAAEC\r\ngYBkgTMeo9BROyBRki65xlGqP8nw1Dry6sRjZn/pH4IU4kgobnxBEWRPGmeVMo3j8KSqs5dX4WT6\r\n7g3rplhymV4Kl1Sm2EeMN2w43R1bDGZX2XuAEBQlF36Lqq2l6rTmwieFC21zeNNVtqifzcYj9NU5\r\nWNjiGyS/bJExhFkOEUVo4QJBAP8p3kJrqcdEDRObXeA1xqcsCJFvfS/QZLZS9QAOtmXjXIjY2kyv\r\ngcDwZIDj2NXDe0KQ7Q5yYMbxnQwQj3IGhukCQQDktfI36FrFrTMAiC8wzJB96bCXEitSFPcbRVXJ\r\nrpoGMXqdZ+QOtVZ7xp13IeypD+tvzkKYobvi5d0wlsUgqOR7AkBTRcqvSouh6GeMGoxMe8BVFl3F\r\neOkWDaCQkAo89pA4ODcewgacODrUSJ/EIJfHS4CSfNGggVtsEbX7Ffx6bf7hAkBYLFUPfMLSBu3s\r\nvJQE6jiicl+kPlr5MB/8IYTrVicHUDgHSzA/A2YuHepDLY8BtqN73TcLhP7cgOX/f4DMEJDVAkEA\r\n0XA3Fh4DKen2N/iKiKbtI7f9gXGlb8/jjIqw4s4NNRodvOHxsE55nc7BQnjJ9sJQOn1NdlIL6sq6\r\n9qnisQX5Tg==";
//			String mw = decryptRes(xtcs.getCsVal(),privateKey);
//			xtcs.setCsVal(mw);
//		}
		return Result.OK(cjxtList);
	}
}
