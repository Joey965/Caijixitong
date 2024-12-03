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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.demo.cjxt.entity.CjxtWarningMessage;
import org.jeecg.modules.demo.cjxt.service.ICjxtWarningMessageService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.message.websocket.WebSocket;
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
 * @Description: 预警提示
 * @Author: jeecg-boot
 * @Date:   2024-07-31
 * @Version: V1.0
 */
@Api(tags="预警提示")
@RestController
@RequestMapping("/cjxt/cjxtWarningMessage")
@Slf4j
public class CjxtWarningMessageController extends JeecgController<CjxtWarningMessage, ICjxtWarningMessageService> {
	@Autowired
	private ICjxtWarningMessageService cjxtWarningMessageService;
	 @Autowired
	 private JdbcTemplate jdbcTemplate;
	 @Autowired
	 private WebSocket webSocket;
	
	/**
	 * 分页列表查询
	 *
	 * @param cjxtWarningMessage
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "预警提示-分页列表查询")
	@ApiOperation(value="预警提示-分页列表查询", notes="预警提示-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtWarningMessage>> queryPageList(CjxtWarningMessage cjxtWarningMessage,
								   @RequestParam(name = "userId",required = false) String userId,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
//		parameterMap.remove("order");
		QueryWrapper<CjxtWarningMessage> queryWrapper = QueryGenerator.initQueryWrapper(cjxtWarningMessage, req.getParameterMap());
//		if(sysUser!=null){
//			queryWrapper.eq("username",sysUser.getUsername());
//			queryWrapper.eq("status","1");
//		}
		if(userId!=null && !"".equals(userId)){
			queryWrapper.eq("user_id",userId);
		}
		queryWrapper.orderByDesc("create_time");
		Page<CjxtWarningMessage> page = new Page<CjxtWarningMessage>(pageNo, pageSize);
		IPage<CjxtWarningMessage> pageList = cjxtWarningMessageService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtWarningMessage
	 * @return
	 */
	@AutoLog(value = "预警提示-添加")
	@ApiOperation(value="预警提示-添加", notes="预警提示-添加")
//	@RequiresPermissions("cjxt:cjxt_warning_message:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtWarningMessage cjxtWarningMessage) {
		cjxtWarningMessageService.save(cjxtWarningMessage);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtWarningMessage
	 * @return
	 */
	@AutoLog(value = "预警提示-编辑")
	@ApiOperation(value="预警提示-编辑", notes="预警提示-编辑")
//	@RequiresPermissions("cjxt:cjxt_warning_message:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtWarningMessage cjxtWarningMessage) {
		cjxtWarningMessageService.updateById(cjxtWarningMessage);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "预警提示-通过id删除")
	@ApiOperation(value="预警提示-通过id删除", notes="预警提示-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_warning_message:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtWarningMessageService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "预警提示-批量删除")
	@ApiOperation(value="预警提示-批量删除", notes="预警提示-批量删除")
//	@RequiresPermissions("cjxt:cjxt_warning_message:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtWarningMessageService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "预警提示-通过id查询")
	@ApiOperation(value="预警提示-通过id查询", notes="预警提示-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtWarningMessage> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtWarningMessage cjxtWarningMessage = cjxtWarningMessageService.getById(id);
		if(cjxtWarningMessage==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtWarningMessage);
	}

	 /**
	  *
	  * @param id
	  * @return
	  */
	 @ApiOperation(value="预警提示-动态数据删除(数据迁移)", notes="预警提示-动态数据删除(数据迁移)")
	 @GetMapping(value = "/dataValueDelete")
	 public Result<String> dataValueDelete(
			 @RequestParam(name="userId",required=false) String userId,
			 @RequestParam(name="id",required=true) String id) {
		 if(id != null && id !=null){
			 CjxtWarningMessage warningMessage = cjxtWarningMessageService.getById(id);
			 if(warningMessage!=null){
				 String bm = warningMessage.getBm();
				 String dataId = warningMessage.getDataId();
				 if(!"".equals(bm) && !"".equals(dataId)){
					 String renameColumnSql = "UPDATE " + bm + " SET del_flag = '1' WHERE id = '"+dataId+"'";
					 jdbcTemplate.execute(renameColumnSql);

					 UpdateWrapper<CjxtWarningMessage> updateWrapper = new UpdateWrapper<>();
					 updateWrapper.eq("data_id", dataId);
					 updateWrapper.set("status", "2");
					 updateWrapper.set("qy_status", "1");
					 cjxtWarningMessageService.update(updateWrapper);

					 //WebSocket消息推送
					 if(userId!=null){
						 JSONObject json = new JSONObject();
						 json.put("msgType", "waMsg");
						 String msg = json.toString();
						 webSocket.sendOneMessage(userId, msg);
					 }
				 }
			 }
		 }
		 return Result.OK("迁移成功");
	 }

	 /**
	  *
	  * @param id
	  * @return
	  */
	 @ApiOperation(value="预警提示-状态已读", notes="预警提示-状态已读")
	 @GetMapping(value = "/dataValueYd")
	 public Result<String> dataValueYd(@RequestParam(name="userId",required=false) String userId,
									   @RequestParam(name="id",required=true) String id) {
		 if(id != null && id !=null){
			 CjxtWarningMessage warningMessage = cjxtWarningMessageService.getById(id);
			 if(warningMessage!=null){
				 warningMessage.setStatus("2");
				 cjxtWarningMessageService.updateById(warningMessage);

				 //WebSocket消息推送
				 if(userId!=null && !"".equals(userId)){
					 JSONObject json = new JSONObject();
					 json.put("msgType", "waMsg");
					 String msg = json.toString();
					 webSocket.sendOneMessage(userId, msg);
				 }
			 }
		 }
		 return Result.OK("状态修改成功");
	 }

	 /**
	  * 数据角标
	  * @param userId
	  * @return
	  */
	 @ApiOperation(value="预警提示-数据角标", notes="预警提示-数据角标")
	 @GetMapping(value = "/dataValueNum")
	 public Result<Integer> dataValueNum(@RequestParam(name="userId",required=true) String userId) {
		 List<CjxtWarningMessage> list = cjxtWarningMessageService.list(new LambdaQueryWrapper<CjxtWarningMessage>().eq(CjxtWarningMessage::getUserId,userId).eq(CjxtWarningMessage::getStatus,"1"));
		 int num = 0;
		 if(list.size()>0){
			 num = list.size();
		 }
		 return Result.OK(num);
	 }

	 @ApiOperation(value="预警提示-webSocket", notes="预警提示-webSocket")
	 @GetMapping(value = "/webSocket")
	 public Result<String> webSocket() {
		 JSONObject json = new JSONObject();
		 json.put("msgType", "waMsg");
		 String msg = json.toString();
		 webSocket.sendOneMessage("1815286269878472706", msg);
//		 webSocket.sendOneMessage("1815286269878472706","消息推送测试");
		 return Result.OK("成功");
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtWarningMessage
    */
//    @RequiresPermissions("cjxt:cjxt_warning_message:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtWarningMessage cjxtWarningMessage) {
        return super.exportXls(request, cjxtWarningMessage, CjxtWarningMessage.class, "预警提示");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_warning_message:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtWarningMessage.class);
    }

}
