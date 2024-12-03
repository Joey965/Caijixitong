package org.jeecg.modules.demo.cjxt.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.demo.cjxt.entity.*;
import org.jeecg.modules.demo.cjxt.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.utils.log.Dg;
import org.jeecg.modules.demo.cjxt.vo.CjxtMbglPage;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * @Description: 任务派发
 * @Author: jeecg-boot
 * @Date:   2024-07-04
 * @Version: V1.0
 */
@Api(tags="任务派发")
@RestController
@RequestMapping("/cjxt/cjxtTask")
@Slf4j
public class CjxtTaskController extends JeecgController<CjxtTask, ICjxtTaskService>{
	@Autowired
	private ICjxtTaskService cjxtTaskService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ICjxtTaskDispatchService cjxtTaskDispatchService;
	@Autowired
	private ICjxtBmfzrService cjxtBmfzrService;
	@Autowired
	private ICjxtBmDataService cjxtBmDataService;
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	@Autowired
	private ICjxtStandardAddressPersonService cjxtStandardAddressPersonService;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	@Autowired
	private ICjxtTaskImportService cjxtTaskImportService;
	@Autowired
	private ICjxtMbglPzService cjxtMbglPzService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ICjxtDataReentryService cjxtDataReentryService;
	@Autowired
	private ICjxtWarningMessageService cjxtWarningMessageService;
	@Autowired
	private ICjxtJsmbpzService cjxtJsmbpzService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ICjxtScoreRuleService cjxtScoreRuleService;
	@Autowired
	private ICjxtScoreDetailService cjxtScoreDetailService;
	@Autowired
	private ISysDictService sysDictService;
	@Autowired
	private WebSocket webSocket;

	/**
	 * 手机分页列表查询
	 *
	 * @param cjxtTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "任务派发-手机分页列表查询")
	@ApiOperation(value="任务派发-手机分页列表查询", notes="任务派发-手机分页列表查询")
	@GetMapping(value = "/appList")
	public Result<IPage<CjxtTask>> appList(CjxtTask cjxtTask,
										   @RequestParam(required = false, name="isDispatcher") String isDispatcher,
										   @RequestParam(required = true, name="userId") String userId,
										   @RequestParam(required = false, name="rwzt") String rwzt,
										   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										   @RequestParam(required = false, name="search") String search,
										   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtTask> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTask, parameterMap);
		if(userId!=null){
			if(!"".equals(isDispatcher) && isDispatcher!=null && "1".equals(isDispatcher)){
				queryWrapper.eq("dispatcher_id",userId);
			}else {
				queryWrapper.eq("receiver_id",userId);
			}
		}
		if(rwzt!=null && !"".equals(rwzt)){
			//已派发 2 已完成4
			queryWrapper.eq("rwzt",rwzt);
		}
		if(search!=null && !"".equals(search)){
			queryWrapper.like("task_name",search);
		}
		queryWrapper.ne("pid","0");
		queryWrapper.orderByDesc("create_time");
		Page<CjxtTask> page = new Page<CjxtTask>(pageNo, pageSize);
		IPage<CjxtTask> pageList = cjxtTaskService.page(page, queryWrapper);
		if("1".equals(isDispatcher) && "4".equals(rwzt)){
			pageList.getRecords().forEach(taskItem ->{
				if(taskItem.getId()!=null){
					String wszt = "";
					List<CjxtTaskDispatch> taskDispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getTaskId,taskItem.getId()));
					for(CjxtTaskDispatch dispatch: taskDispatchList){
						wszt = dispatch.getWszt();
						if(dispatch.getWszt()==null){
							wszt = null;
							break;
						}
					}
					taskItem.setWsztDto(wszt);
				}
			});
		}
		return Result.OK(pageList);
	}

	@ApiOperation(value="任务派发-任务完成数量", notes="任务派发-任务完成数量")
	@GetMapping(value = "/appTaskNum")
	public Result<Integer> appTaskNum(@RequestParam(required = true, name="userId") String userId,
									  @RequestParam(required = true, name="rwzt") String rwzt) {
		Integer num = 0;
		if("2".equals(rwzt)){
			List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>()
					.eq(CjxtTask::getDispatcherId,userId)
					.eq(CjxtTask::getRwzt,rwzt)
					.ne(CjxtTask::getPid,"0")
					.orderByDesc(CjxtTask::getCreateTime));
			if(taskList.size()>0){
				num = taskList.size();
			}
		}
		if("4".equals(rwzt)){
			List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>()
					.eq(CjxtTask::getDispatcherId,userId)
					.eq(CjxtTask::getRwzt,rwzt)
					.ne(CjxtTask::getPid,"0")
					.orderByDesc(CjxtTask::getCreateTime));
			int numTask = 0;
			if(taskList.size()>0){
				for(CjxtTask cjxtTask: taskList){
					List<CjxtTaskDispatch> taskDispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getTaskId,cjxtTask.getId()).isNull(CjxtTaskDispatch::getWszt));
					if(taskDispatchList.size()>0){
						numTask++;
					}
				}
			}
			num = numTask;
		}
		return Result.OK(num);
	}

	/**
	 * 我的任务数量
	 * @param userId
	 * @param req
	 * @return
	 */
	@ApiOperation(value="任务派发表-我的任务数量", notes="任务派发表-我的任务数量")
	@GetMapping(value = "/listWdrwNum")
	public Result<Integer> listWdrwNum(@RequestParam(name="userId",required=true) String userId,
									   HttpServletRequest req) {
//		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Integer num = 0;
		if(userId!=null){
			List<CjxtTask> list = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getReceiverId,userId).eq(CjxtTask::getRwzt,"2").ne(CjxtTask::getPid,"0"));
			if(list.size()>0){
				num = list.size();
			}
		}
		return Result.OK(num);
	}

	/**
	 * 专项任务采集列表
	 */
	//@AutoLog(value = "任务派发-专项任务采集列表")
	@ApiOperation(value="任务派发-专项任务采集列表", notes="任务派发-专项任务采集列表")
	@GetMapping(value = "/zxrwcjList")
	public Result<IPage<CjxtTask>> zxrwcjList(CjxtTask cjxtTask,
											  @RequestParam(name="userId",required=true) String userId,
											  @RequestParam(name="rwzt",required = false) String rwzt,
											  @RequestParam(name="search",required = false) String search,
											  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											  HttpServletRequest req) {
//		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		QueryWrapper<CjxtTask> queryWrapper =  QueryGenerator.initQueryWrapper(cjxtTask, req.getParameterMap());
		queryWrapper.inSql("mb_id","SELECT id FROM cjxt_mbgl WHERE del_flag = '0' AND mblx = '5' ");
		queryWrapper.ne("pid","0");
		queryWrapper.eq("has_child","0");
		queryWrapper.eq("receiver_id",userId);
		if(!"".equals(rwzt) && rwzt!=null){
			queryWrapper.eq("rwzt",rwzt);
		}
		if(!"".equals(search) && search!=null){
			queryWrapper.last(" AND (task_name LIKE '"+search+"' OR dispatcher_name LIKE '"+search+"')");
		}
		Page<CjxtTask> page = new Page<CjxtTask>(pageNo,pageSize);
		IPage<CjxtTask> pageList = cjxtTaskService.page(page,queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 专项任务采集数量
	 * @param userId
	 * @param req
	 * @return
	 */
	@ApiOperation(value="任务派发表-专项任务采集数量", notes="任务派发表-专项任务采集数量")
	@GetMapping(value = "/zxrwcjNum")
	public Result<Integer> zxrwcjNum(@RequestParam(name="userId",required=true) String userId,
									 HttpServletRequest req) {
//		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Integer num = 0;
		if(userId!=null){
			List<CjxtTask> list = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>()
					.inSql(CjxtTask::getMbId,"SELECT id FROM cjxt_mbgl WHERE del_flag = '0' AND mblx = '5' ")
					.eq(CjxtTask::getReceiverId,userId)
					.eq(CjxtTask::getHasChild,"0")
					.eq(CjxtTask::getRwzt,"2")
					.ne(CjxtTask::getPid,"0"));
			if(list.size()>0){
				num = list.size();
			}
		}
		return Result.OK(num);
	}

	/**
	 * 九小场所采集列表
	 */
	//@AutoLog(value = "任务派发-九小场所采集列表")
	@ApiOperation(value="任务派发-九小场所采集列表", notes="任务派发-九小场所采集列表")
	@GetMapping(value = "/jxcsglList")
	public Result<IPage<CjxtTask>> jxcsglList(CjxtTask cjxtTask,
											  @RequestParam(name="userId",required=true) String userId,
											  @RequestParam(name="rwzt",required = false) String rwzt,
											  @RequestParam(name="search",required = false) String search,
											  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											  HttpServletRequest req) {
//		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		QueryWrapper<CjxtTask> queryWrapper =  QueryGenerator.initQueryWrapper(cjxtTask, req.getParameterMap());
		queryWrapper.inSql("mb_id","SELECT id FROM cjxt_mbgl WHERE del_flag = '0' AND mblx = '4' ");
		queryWrapper.ne("pid","0");
		queryWrapper.eq("has_child","0");
		queryWrapper.eq("receiver_id",userId);
		if(!"".equals(rwzt) && rwzt!=null){
			queryWrapper.eq("rwzt",rwzt);
		}
		if(!"".equals(search) && search!=null){
			queryWrapper.last(" AND (task_name LIKE '"+search+"' OR dispatcher_name LIKE '"+search+"')");
		}
		Page<CjxtTask> page = new Page<CjxtTask>(pageNo,pageSize);
		IPage<CjxtTask> pageList = cjxtTaskService.page(page,queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 九小场所任务采集数量
	 * @param userId
	 * @param req
	 * @return
	 */
	@ApiOperation(value="任务派发表-九小场所任务采集数量", notes="任务派发表-九小场所任务采集数量")
	@GetMapping(value = "/jxcsNum")
	public Result<Integer> jxcsNum(@RequestParam(name="userId",required=true) String userId,
								   HttpServletRequest req) {
//		 LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Integer num = 0;
		if(userId!=null){
			List<CjxtTask> list = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>()
					.inSql(CjxtTask::getMbId,"SELECT id FROM cjxt_mbgl WHERE del_flag = '0' AND mblx = '4' ")
					.eq(CjxtTask::getReceiverId,userId)
					.eq(CjxtTask::getHasChild,"0")
					.eq(CjxtTask::getRwzt,"2")
					.ne(CjxtTask::getPid,"0"));
			if(list.size()>0){
				num = list.size();
			}
		}
		return Result.OK(num);
	}

	/**
	 * 分页列表查询
	 *
	 * @param cjxtTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "任务派发-分页列表查询")
	@ApiOperation(value="任务派发-分页列表查询", notes="任务派发-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<CjxtTask>> queryPageList(CjxtTask cjxtTask,
												 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												 HttpServletRequest req) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String hasQuery = req.getParameter("hasQuery");
		if(hasQuery != null && "true".equals(hasQuery)){
			QueryWrapper<CjxtTask> queryWrapper =  QueryGenerator.initQueryWrapper(cjxtTask, req.getParameterMap());
			List<CjxtTask> list = cjxtTaskService.queryTreeListNoPage(queryWrapper);
			IPage<CjxtTask> pageList = new Page<>(1, 10, list.size());
			pageList.setRecords(list);
			return Result.OK(pageList);
		}else{
			String parentId = cjxtTask.getPid();
			if (oConvertUtils.isEmpty(parentId)) {
				parentId = "0";
			}
			cjxtTask.setPid(null);
			QueryWrapper<CjxtTask> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTask, req.getParameterMap());
			queryWrapper.eq("org_code",sysUser.getOrgCode());
			// 使用 eq 防止模糊查询
			//queryWrapper.eq("pid", parentId);
			Page<CjxtTask> page = new Page<CjxtTask>(pageNo, pageSize);
			IPage<CjxtTask> pageList = cjxtTaskService.page(page, queryWrapper);
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
			List<SelectTreeModel> ls = cjxtTaskService.queryListByPid(pid);
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
			List<SelectTreeModel> ls = cjxtTaskService.queryListByCode(pcode);
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
			List<SelectTreeModel> temp = cjxtTaskService.queryListByPid(tsm.getKey());
			if (temp != null && temp.size() > 0) {
				tsm.setChildren(temp);
				loadAllChildren(temp);
			}
		}
	}

	/**
	 * 获取子数据
	 * @param cjxtTask
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "任务派发-获取子数据")
	@ApiOperation(value="任务派发-获取子数据", notes="任务派发-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<CjxtTask>> queryPageList(CjxtTask cjxtTask,HttpServletRequest req) {
		QueryWrapper<CjxtTask> queryWrapper = QueryGenerator.initQueryWrapper(cjxtTask, req.getParameterMap());
		List<CjxtTask> list = cjxtTaskService.list(queryWrapper);
		IPage<CjxtTask> pageList = new Page<>(1, 10, list.size());
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
	//@AutoLog(value = "任务派发-批量获取子数据")
	@ApiOperation(value="任务派发-批量获取子数据", notes="任务派发-批量获取子数据")
	@GetMapping("/getChildListBatch")
	public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
		try {
			QueryWrapper<CjxtTask> queryWrapper = new QueryWrapper<>();
			List<String> parentIdList = Arrays.asList(parentIds.split(","));
			queryWrapper.in("pid", parentIdList);
			List<CjxtTask> list = cjxtTaskService.list(queryWrapper);
			IPage<CjxtTask> pageList = new Page<>(1, 10, list.size());
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
	 * @param cjxtTask
	 * @return
	 */
	@AutoLog(value = "任务派发-添加")
	@ApiOperation(value="任务派发-添加", notes="任务派发-添加")
//    @RequiresPermissions("cjxt:cjxt_task:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtTask cjxtTask) {
		if((cjxtTask.getPid()==null || "0".equals(cjxtTask.getPid()) || "".equals(cjxtTask.getPid())) && "1".equals(cjxtTask.getChzt())){
			List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTask.getTaskName()).eq(CjxtTask::getPid,"0"));
			if(taskList.size()>0){
				return Result.error("当前任务名称已存在,请修改任务名称!!!");
			}
		}
		if(cjxtTask.getTaskNameDto()!=null){
			if(!cjxtTask.getTaskNameDto().equals(cjxtTask.getTaskName())){
				List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTask.getTaskName()).eq(CjxtTask::getPid,"0"));
				if(taskList.size()>0){
					return Result.error("当前任务名称已存在,请修改任务名称!!!");
				}
			}
		}
		String recOrgId = "";
		String recOrgName = "";
		String recOrgCode = "";
		String recUserId = "";
		String recUserZh = "";
		String recUserName = "";
		String rwzt = "";
		String uuid = "";
		boolean isFirst = true;
		//当前派发用户信息
		SysUser sysUser = sysUserService.getById(cjxtTask.getDispatcherId());
		//接收人信息
		SysUser receiverUser = sysUserService.getUserByName(cjxtTask.getReceiverName());
		//网格员、采集地址等于null
		if(receiverUser==null && cjxtTask.getAddressQhId()==null && cjxtTask.getAddressId()==null){
			CjxtTask newCjxtTask =new CjxtTask();
			//当前派发部门信息
			SysDepart disDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtTask.getDispatcherOrgCode()).last("LIMIT 1"));
			//接收部门信息
			String[] receiverOrgIds = null;
			if(cjxtTask.getReceiverOrgId()!=null && !"".equals(cjxtTask.getReceiverOrgId())){
				receiverOrgIds = cjxtTask.getReceiverOrgId().split(",");
			}else {
				receiverOrgIds = cjxtTask.getReceiverOrgName().split(",");
			}
			int cjzs = 0;
			for(int i = 0;i<receiverOrgIds.length;i++){
				//接收部门详细信息
				String receiverOrgId = receiverOrgIds[i];
				SysDepart receiverName = sysDepartService.getById(receiverOrgId);
				//接收部门负责人信息 接收人
				List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));
				if(bmfzrList.size()<=0){
					return  Result.error(receiverName.getDepartNameFull()+"未配置部门负责人,请配置部门负责人或选择接收人!!!");
				}
				if(bmfzrList!=null){
					for(CjxtBmfzr cjxtBmfzr:bmfzrList){
						if(!"".equals(recUserId)){
							recUserId+=","+cjxtBmfzr.getFzryId();
							recUserZh+=","+cjxtBmfzr.getLxdh();
							recUserName+=","+cjxtBmfzr.getFzryName();
						}else {
							recUserId+=cjxtBmfzr.getFzryId();
							recUserZh+=cjxtBmfzr.getLxdh();
							recUserName+=cjxtBmfzr.getFzryName();
						}
					}
				}
				recOrgId+=receiverName.getId()+",";
				recOrgName+=receiverName.getDepartNameFull()+",";
				recOrgCode+=receiverName.getOrgCode()+",";
				//接收部门采集总数
				List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,receiverName.getId()));
				if(addressList!=null){
					cjzs += addressList.size();
				}
				if(addressList.size()==0){
					return Result.error(receiverName.getDepartNameFull()+"不存在采集地址,请配置地址或选择详细地址!");
				}
			}
			uuid = UUID.randomUUID().toString().replace("-","");
			if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
				newCjxtTask.setId(uuid);
				newCjxtTask.setTaskCode(uuid);
			}else {
				newCjxtTask.setId(uuid);
				newCjxtTask.setTaskCode(cjxtTask.getId());
			}
			//节点信息
			newCjxtTask.setPid(cjxtTask.getPid());
			newCjxtTask.setHasChild(cjxtTask.getHasChild());
			//任务信息
			newCjxtTask.setTaskName(cjxtTask.getTaskName());
			newCjxtTask.setTaskDescription(cjxtTask.getTaskDescription());
			//模板信息
			newCjxtTask.setMbId(cjxtTask.getMbId());
			newCjxtTask.setMbName(cjxtTask.getMbName());
			newCjxtTask.setMbCode(cjxtTask.getMbCode());
			newCjxtTask.setBm(cjxtTask.getBm());
			//部门数据权限==派发部门
			newCjxtTask.setOrgId(disDepart.getId());
			newCjxtTask.setOrgCode(disDepart.getOrgCode());
			newCjxtTask.setOrgName(disDepart.getDepartNameFull());
			//当前派发部门
			newCjxtTask.setDispatcherOrgId(disDepart.getId());
			newCjxtTask.setDispatcherOrgCode(disDepart.getOrgCode());
			newCjxtTask.setDispatcherOrgName(disDepart.getDepartNameFull());
			//当前派发人
			newCjxtTask.setDispatcherId(cjxtTask.getDispatcherId());
			newCjxtTask.setDispatcherName(cjxtTask.getDispatcherName());
			//接收部门、部门负责人信息
			if (recOrgId != null && recOrgId.length() > 0 && recOrgId.charAt(recOrgId.length() - 1) == ',') {
				recOrgId = recOrgId.substring(0, recOrgId.length() - 1);
			}
			if (recOrgName != null && recOrgName.length() > 0 && recOrgName.charAt(recOrgName.length() - 1) == ',') {
				recOrgName = recOrgName.substring(0, recOrgName.length() - 1);
			}
			if (recOrgCode != null && recOrgCode.length() > 0 && recOrgCode.charAt(recOrgCode.length() - 1) == ',') {
				recOrgCode = recOrgCode.substring(0, recOrgCode.length() - 1);
			}
			newCjxtTask.setReceiverOrgId(recOrgId);
			newCjxtTask.setReceiverOrgCode(recOrgCode);
			newCjxtTask.setReceiverOrgName(recOrgName);
			newCjxtTask.setReceiverBmfzrId(recUserId);
			newCjxtTask.setReceiverBmfzrZh(recUserZh);
			newCjxtTask.setReceiverBmfzrName(recUserName);
			//接收人信息：：：接收人为空存入部门负责人信息
			newCjxtTask.setReceiverId(recUserId);
			newCjxtTask.setReceiverZh(recUserZh);
			newCjxtTask.setReceiverName(recUserName);
			newCjxtTask.setDueDate(cjxtTask.getDueDate());
			//采集情况
			newCjxtTask.setCjZs(cjzs);
			newCjxtTask.setCjYwc(0);
			newCjxtTask.setCjSy(cjzs);
			newCjxtTask.setCjWcqk("0%");
			newCjxtTask.setChzt("1");
			if(cjxtTask.getPid()==null){
				newCjxtTask.setRwzt("2");
				cjxtTaskService.addCjxtTask(newCjxtTask);
			}else if(cjxtTask.getPid()!=null){
				CjxtTask taskFather = new CjxtTask();
				taskFather.setId(cjxtTask.getId());
				taskFather.setRwzt("5");
				taskFather.setPid(cjxtTask.getPid());
				cjxtTaskService.updateById(taskFather);
			}

			//创建接收部门任务
			for(int i = 0;i<receiverOrgIds.length;i++){
				uuid = UUID.randomUUID().toString().replace("-","");
				String bmfzrId= "";
				String bmfzrZh="";
				String bmfzrName= "";
				int cjzsChild = 0;
				//接收部门详细信息
				String receiverOrgId = receiverOrgIds[i];
				SysDepart receiverName = sysDepartService.getById(receiverOrgId);
				//接收部门负责人信息 接收人
				List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));
				//接收部门采集总数
				List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,receiverName.getId()));
				if(addressList!=null){
					cjzsChild = addressList.size();
				}
				//创建子接收部门数据
				CjxtTask taskChild = new CjxtTask();
				taskChild.setId(uuid);
				if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
					taskChild.setPid(newCjxtTask.getId());
					taskChild.setTaskCode(newCjxtTask.getTaskCode());
				}else {
					taskChild.setPid(cjxtTask.getId());
					taskChild.setTaskCode(cjxtTask.getTaskCode());
				}
				//节点信息
				taskChild.setHasChild(cjxtTask.getHasChild());
				//任务信息
				taskChild.setTaskName(newCjxtTask.getTaskName());
				taskChild.setTaskDescription(newCjxtTask.getTaskDescription());
				//模板信息
				taskChild.setMbId(newCjxtTask.getMbId());
				taskChild.setMbName(newCjxtTask.getMbName());
				taskChild.setMbCode(newCjxtTask.getMbCode());
				taskChild.setBm(newCjxtTask.getBm());
				//部门数据权限==派发部门
				taskChild.setOrgId(receiverName.getId());
				taskChild.setOrgCode(receiverName.getOrgCode());
				taskChild.setOrgName(receiverName.getDepartNameFull());
				//当前派发部门
				taskChild.setDispatcherOrgId(disDepart.getId());
				taskChild.setDispatcherOrgCode(disDepart.getOrgCode());
				taskChild.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				taskChild.setDispatcherId(newCjxtTask.getDispatcherId());
				taskChild.setDispatcherName(newCjxtTask.getDispatcherName());
				//接收部门、部门负责人信息
				taskChild.setReceiverOrgId(receiverName.getId());
				taskChild.setReceiverOrgCode(receiverName.getOrgCode());
				taskChild.setReceiverOrgName(receiverName.getDepartNameFull());
				if(bmfzrList!=null){
					for(CjxtBmfzr cjxtBmfzr: bmfzrList){
						bmfzrId+=cjxtBmfzr.getFzryId();
						bmfzrZh+=cjxtBmfzr.getLxdh();
						bmfzrName+=cjxtBmfzr.getFzryName();
					}
					taskChild.setReceiverBmfzrId(bmfzrId);
					taskChild.setReceiverBmfzrZh(bmfzrZh);
					taskChild.setReceiverBmfzrName(bmfzrName);
					//接收人信息
					taskChild.setReceiverId(bmfzrId);
					taskChild.setReceiverZh(bmfzrZh);
					taskChild.setReceiverName(bmfzrName);
				}
				taskChild.setRwzt("1");
				taskChild.setDueDate(newCjxtTask.getDueDate());
				//采集情况
				taskChild.setCjZs(cjzsChild);
				taskChild.setCjYwc(0);
				taskChild.setCjSy(cjzsChild);
				taskChild.setCjWcqk("0%");
				taskChild.setChzt("1");
				cjxtTaskService.addCjxtTask(taskChild);

				try {
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int j=0;j<bmfzrZhs.length;j++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[j], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[j]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[j]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && !taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int j=0;j<bmfzrZhs.length;j++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[j], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[j]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[j]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}

						String[] receiverZhs = taskChild.getReceiverZh().split(",");
						if(receiverZhs.length>0 && !"".equals(receiverZhs[0])){
							for(int j=0;j<receiverZhs.length;j++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[j], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[j]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(receiverZhs[j]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//网格员不等于null、采集地址等于null
		if(receiverUser!=null && cjxtTask.getAddressId()==null && cjxtTask.getAddressQhId()==null){
			CjxtTask perTask = new CjxtTask();
			if("1".equals(receiverUser.getUserSf())){
				//当前派发部门信息
				SysDepart disDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtTask.getDispatcherOrgCode()).last("LIMIT 1"));
				//接收部门信息
				String[] receiverOrgIds = null;
				if(cjxtTask.getReceiverOrgId()!=null && !"".equals(cjxtTask.getReceiverOrgId())){
					receiverOrgIds = cjxtTask.getReceiverOrgId().split(",");
				}else {
					receiverOrgIds = cjxtTask.getReceiverOrgName().split(",");
				}
				int cjzs = 0;
				for(int i = 0;i<receiverOrgIds.length;i++){
					//接收部门详细信息
					String receiverOrgId = receiverOrgIds[i];
					SysDepart receiverName = sysDepartService.getById(receiverOrgId);
					//接收部门负责人信息 接收人
					List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));
					if(bmfzrList!=null){
						for(CjxtBmfzr cjxtBmfzr:bmfzrList){
							if(!"".equals(recUserId)){
								recUserId+=","+cjxtBmfzr.getFzryId();
								recUserZh+=","+cjxtBmfzr.getLxdh();
								recUserName+=","+cjxtBmfzr.getFzryName();
							}else {
								recUserId+=cjxtBmfzr.getFzryId();
								recUserZh+=cjxtBmfzr.getLxdh();
								recUserName+=cjxtBmfzr.getFzryName();
							}
						}
					}
					recOrgId+=receiverName.getId()+",";
					recOrgName+=receiverName.getDepartNameFull()+",";
					recOrgCode+=receiverName.getOrgCode()+",";
					//接收部门采集总数
					List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,receiverName.getId()));
					if(addressList!=null){
						cjzs += addressList.size();
					}
				}
				uuid = UUID.randomUUID().toString().replace("-","");
				if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
					perTask.setId(uuid);
					perTask.setTaskCode(uuid);
				}else {
					perTask.setId(uuid);
					perTask.setTaskCode(cjxtTask.getId());
				}
				//节点信息
				perTask.setPid(cjxtTask.getPid());
				perTask.setHasChild(cjxtTask.getHasChild());
				//任务信息
				perTask.setTaskName(cjxtTask.getTaskName());
				perTask.setTaskDescription(cjxtTask.getTaskDescription());
				//模板信息
				perTask.setMbId(cjxtTask.getMbId());
				perTask.setMbName(cjxtTask.getMbName());
				perTask.setMbCode(cjxtTask.getMbCode());
				perTask.setBm(cjxtTask.getBm());
				//部门数据权限==派发部门
				perTask.setOrgId(disDepart.getId());
				perTask.setOrgCode(disDepart.getOrgCode());
				perTask.setOrgName(disDepart.getDepartNameFull());
				//当前派发部门
				perTask.setDispatcherOrgId(disDepart.getId());
				perTask.setDispatcherOrgCode(disDepart.getOrgCode());
				perTask.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				perTask.setDispatcherId(cjxtTask.getDispatcherId());
				perTask.setDispatcherName(cjxtTask.getDispatcherName());
				//接收部门、部门负责人信息
				if (recOrgId != null && recOrgId.length() > 0 && recOrgId.charAt(recOrgId.length() - 1) == ',') {
					recOrgId = recOrgId.substring(0, recOrgId.length() - 1);
				}
				if (recOrgName != null && recOrgName.length() > 0 && recOrgName.charAt(recOrgName.length() - 1) == ',') {
					recOrgName = recOrgName.substring(0, recOrgName.length() - 1);
				}
				if (recOrgCode != null && recOrgCode.length() > 0 && recOrgCode.charAt(recOrgCode.length() - 1) == ',') {
					recOrgCode = recOrgCode.substring(0, recOrgCode.length() - 1);
				}
				perTask.setReceiverOrgId(recOrgId);
				perTask.setReceiverOrgCode(recOrgCode);
				perTask.setReceiverOrgName(recOrgName);
				perTask.setReceiverBmfzrId(recUserId);
				perTask.setReceiverBmfzrZh(recUserZh);
				perTask.setReceiverBmfzrName(recUserName);
				//接收人信息：：：接收人为空存入部门负责人信息
				perTask.setReceiverId(receiverUser.getId());
				perTask.setReceiverZh(receiverUser.getUsername());
				perTask.setReceiverName(receiverUser.getRealname());
				perTask.setDueDate(cjxtTask.getDueDate());
				//采集情况
				perTask.setCjZs(cjzs);
				perTask.setCjYwc(0);
				perTask.setCjSy(cjzs);
				perTask.setCjWcqk("0%");
				perTask.setRwzt("2");
				perTask.setChzt("1");
				if(cjxtTask.getPid()==null){
					isFirst = true;
					cjxtTaskService.addCjxtTask(perTask);
				}else {
					isFirst = false;
					CjxtTask task = new CjxtTask();
					task.setId(cjxtTask.getId());
					task.setRwzt("5");
					task.setPid(cjxtTask.getPid());
					if("2".equals(cjxtTask.getChzt())){
						task.setReceiverOrgId(recOrgId);
						task.setReceiverOrgCode(recOrgCode);
						task.setReceiverOrgName(recOrgName);
						task.setReceiverBmfzrId(recUserId);
						task.setReceiverBmfzrZh(recUserZh);
						task.setReceiverBmfzrName(recUserName);
						//接收人信息：：：接收人为空存入部门负责人信息
						task.setReceiverId(receiverUser.getId());
						task.setReceiverZh(receiverUser.getUsername());
						task.setReceiverName(receiverUser.getRealname());
						task.setDueDate(cjxtTask.getDueDate());
						//采集情况
						task.setCjZs(cjzs);
						task.setCjYwc(0);
						task.setCjSy(cjzs);
						task.setCjWcqk("0%");
						task.setChzt("1");
					}
					cjxtTaskService.updateCjxtTask(task);
				}
				//创建接收部门任务
				uuid = UUID.randomUUID().toString().replace("-","");
				String bmfzrId= "";
				String bmfzrZh= "";
				String bmfzrName= "";
				int cjzsChild = 0;
				//接收部门详细信息
				String receiverOrgId = perTask.getReceiverOrgId();
				SysDepart receiverName = sysDepartService.getById(receiverOrgId);
				//接收部门负责人信息 接收人
				List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));
				//接收部门采集总数
//				List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,receiverName.getId()));
//				if(addressList!=null){
//					cjzsChild = addressList.size();
//				}
				//创建子接收部门数据
				CjxtTask taskChild = new CjxtTask();
				taskChild.setId(uuid);

				//节点信息
				if(cjxtTask.getId()!=null){
					taskChild.setPid(cjxtTask.getId());
					taskChild.setTaskCode(cjxtTask.getTaskCode());
				}else {
					taskChild.setPid(perTask.getId());
					taskChild.setTaskCode(perTask.getTaskCode());
				}
				taskChild.setHasChild(cjxtTask.getHasChild());
				//任务信息
				taskChild.setTaskName(cjxtTask.getTaskName());
				taskChild.setTaskDescription(cjxtTask.getTaskDescription());
				//模板信息
				taskChild.setMbId(cjxtTask.getMbId());
				taskChild.setMbName(cjxtTask.getMbName());
				taskChild.setMbCode(perTask.getMbCode());
				taskChild.setBm(cjxtTask.getBm());
				//部门数据权限==派发部门
				taskChild.setOrgId(receiverName.getId());
				taskChild.setOrgCode(receiverName.getOrgCode());
				taskChild.setOrgName(receiverName.getDepartNameFull());
				//当前派发部门
				taskChild.setDispatcherOrgId(disDepart.getId());
				taskChild.setDispatcherOrgCode(disDepart.getOrgCode());
				taskChild.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				taskChild.setDispatcherId(cjxtTask.getDispatcherId());
				taskChild.setDispatcherName(cjxtTask.getDispatcherName());
				//接收部门、部门负责人信息
				taskChild.setReceiverOrgId(receiverName.getId());
				taskChild.setReceiverOrgCode(receiverName.getOrgCode());
				taskChild.setReceiverOrgName(receiverName.getDepartNameFull());
				if(bmfzrList!=null){
					for(CjxtBmfzr cjxtBmfzr: bmfzrList){
						bmfzrId+=cjxtBmfzr.getFzryId();
						bmfzrZh+=cjxtBmfzr.getLxdh();
						bmfzrName+=cjxtBmfzr.getFzryName();
					}
					taskChild.setReceiverBmfzrId(bmfzrId);
					taskChild.setReceiverBmfzrZh(bmfzrZh);
					taskChild.setReceiverBmfzrName(bmfzrName);
				}
				//接收人信息
				taskChild.setReceiverId(receiverUser.getId());
				taskChild.setReceiverZh(receiverUser.getUsername());
				taskChild.setReceiverName(receiverUser.getRealname());
				taskChild.setRwzt("2");
				taskChild.setDueDate(cjxtTask.getDueDate());
				//采集情况
//				taskChild.setCjZs(cjzsChild);
				taskChild.setCjYwc(0);
//				taskChild.setCjSy(cjzsChild);
				taskChild.setCjWcqk("0%");
				taskChild.setChzt("1");
				cjxtTaskService.addCjxtTask(taskChild);

				SysUser user = sysUserService.getById(receiverUser.getId());
				SysDepart sysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,user.getOrgCode()));

				//创建子表任务
				List<CjxtStandardAddress> personList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressCodeMz,sysDepart.getOrgCode()));
				int j =0 ;
				if(personList!=null){
					if(personList.size()>0){
						for(CjxtStandardAddress cjxtStandardAddress: personList){
							j++;
							CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
							if(!"".equals(cjxtTask.getBlId()) && cjxtTask.getBlId()!=null){
								cjxtTaskDispatch.setBlId(cjxtTask.getBlId());
							}
							cjxtTaskDispatch.setTaskId(taskChild.getId());
							cjxtTaskDispatch.setTaskCode(taskChild.getTaskCode());
							cjxtTaskDispatch.setTaskName(cjxtTask.getTaskName());
							cjxtTaskDispatch.setTaskDescription(cjxtTask.getTaskDescription());
							cjxtTaskDispatch.setMbId(cjxtTask.getMbId());
							cjxtTaskDispatch.setMbCode(cjxtTask.getMbCode());
							cjxtTaskDispatch.setMbName(cjxtTask.getMbName());
							cjxtTaskDispatch.setBm(cjxtTask.getBm());
							//采集地址
							cjxtTaskDispatch.setAddressId(cjxtStandardAddress.getId());
							cjxtTaskDispatch.setAddressCode(cjxtStandardAddress.getAddressCodeMz());
							cjxtTaskDispatch.setAddressName(cjxtStandardAddress.getAddressNameMz());
							cjxtTaskDispatch.setDispatcherId(cjxtTask.getDispatcherId());
							cjxtTaskDispatch.setDispatcherName(cjxtTask.getDispatcherName());
							cjxtTaskDispatch.setReceiverId(receiverUser.getId());
							cjxtTaskDispatch.setReceiverName(receiverUser.getRealname());
							cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
							cjxtTaskDispatch.setRwzt("2");
							String sql = "SELECT id,create_time,update_time FROM " + cjxtTask.getBm() + " WHERE del_flag = '0' AND address_id = '" + cjxtStandardAddress.getId()+"' ORDER BY create_time ASC LIMIT 1";
							List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
							Date createTime = null;
							Date updateTime = null;
							if(resultList.size()>0){
								Map<String, Object> row = resultList.get(0);
								Object id = row.get("id");
								LocalDateTime create_time = (LocalDateTime) row.get("create_time");
								LocalDateTime update_time = (LocalDateTime) row.get("update_time");
								if(create_time != null){
									createTime = java.sql.Timestamp.valueOf(create_time);
								}
								if(update_time != null){
									updateTime = java.sql.Timestamp.valueOf(update_time);
								}
								cjxtTaskDispatch.setDataId(String.valueOf(id));
								if(updateTime!=null){
									cjxtTaskDispatch.setSchssj(updateTime);
								}else {
									cjxtTaskDispatch.setSchssj(createTime);
								}
								cjxtTaskDispatch.setHszt("2");
							}
							cjxtTaskDispatchService.save(cjxtTaskDispatch);
						}
					} else {
						CjxtTask taskPer = new CjxtTask();
						taskPer.setId(perTask.getId());
						if(isFirst==true){
							cjxtTaskService.removeById(taskPer);
						}else {
							taskPer.setRwzt("1");
							cjxtTaskService.updateById(taskPer);
						}
						CjxtTask taskdelete = new CjxtTask();
						taskdelete.setId(taskChild.getId());
						cjxtTaskService.removeById(taskdelete);
						return Result.error("当前网格员部门不存在地址,请重新派发!");
					}
					CjxtTask taskChild1 = new CjxtTask();
					taskChild1.setId(taskChild.getId());
					taskChild1.setCjZs(j);
					taskChild1.setCjYwc(0);
					taskChild1.setCjSy(j);
					taskChild1.setCjWcqk("0%");
					cjxtTaskService.updateById(taskChild1);

					CjxtTask taskFather = new CjxtTask();
					if(isFirst == false){
						taskFather.setId(cjxtTask.getId());
					}else {
						taskFather.setId(perTask.getId());
					}
					taskFather.setCjZs(j);
					taskFather.setCjYwc(0);
					taskFather.setCjSy(j);
					taskFather.setCjWcqk("0%");
					cjxtTaskService.updateById(taskFather);
				}
				try {
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && !taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}

						String[] receiverZhs = taskChild.getReceiverZh().split(",");
						if(receiverZhs.length>0 && !"".equals(receiverZhs[0])){
							for(int k=0;k<receiverZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[j]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(receiverZhs[j]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				return Result.error("接收人员不是网格员,请选择采集地址!");
			}
		}
		//网格员、采集地址不等于null
		if(receiverUser!=null && (cjxtTask.getAddressId()!=null || cjxtTask.getAddressQhId()!=null)){
			CjxtTask addresstask = new CjxtTask();
			//区划地址不为空 详细地址为空
			if(cjxtTask.getAddressQhId()!=null && cjxtTask.getAddressId()==null){
				//当前派发部门信息
				SysDepart disDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtTask.getDispatcherOrgCode()).last("LIMIT 1"));
				//接收部门信息
				String[] receiverOrgIds = null;
				if(cjxtTask.getReceiverOrgId()!=null && !"".equals(cjxtTask.getReceiverOrgId())){
					receiverOrgIds = cjxtTask.getReceiverOrgId().split(",");
				}else {
					receiverOrgIds = cjxtTask.getReceiverOrgName().split(",");
				}
				int cjzs = 0;
				for(int i = 0;i<receiverOrgIds.length;i++){
					//接收部门详细信息
					String receiverOrgId = receiverOrgIds[i];
					SysDepart receiverName = sysDepartService.getById(receiverOrgId);
					//接收部门负责人信息 接收人
					List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));
					if(bmfzrList!=null){
						for(CjxtBmfzr cjxtBmfzr:bmfzrList){
							if(!"".equals(recUserId)){
								recUserId+=","+cjxtBmfzr.getFzryId();
								recUserZh+=","+cjxtBmfzr.getLxdh();
								recUserName+=","+cjxtBmfzr.getFzryName();
							}else {
								recUserId+=cjxtBmfzr.getFzryId();
								recUserZh+=cjxtBmfzr.getLxdh();
								recUserName+=cjxtBmfzr.getFzryName();
							}
						}
					}
					recOrgId+=receiverName.getId()+",";
					recOrgName+=receiverName.getDepartNameFull()+",";
					recOrgCode+=receiverName.getOrgCode()+",";
					//接收部门采集总数
					List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,receiverName.getId()));
					if(addressList!=null){
						cjzs += addressList.size();
					}
				}
				uuid = UUID.randomUUID().toString().replace("-","");
				if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
					addresstask.setId(uuid);
					addresstask.setTaskCode(uuid);
				}else {
					addresstask.setId(uuid);
					addresstask.setTaskCode(cjxtTask.getId());
				}
				//节点信息
				addresstask.setPid(cjxtTask.getPid());
				addresstask.setHasChild(cjxtTask.getHasChild());
				//任务信息
				addresstask.setTaskName(cjxtTask.getTaskName());
				addresstask.setTaskDescription(cjxtTask.getTaskDescription());
				//模板信息
				addresstask.setMbId(cjxtTask.getMbId());
				addresstask.setMbName(cjxtTask.getMbName());
				addresstask.setMbCode(cjxtTask.getMbCode());
				addresstask.setBm(cjxtTask.getBm());
				//部门数据权限==派发部门
				addresstask.setOrgId(disDepart.getId());
				addresstask.setOrgCode(disDepart.getOrgCode());
				addresstask.setOrgName(disDepart.getDepartNameFull());
				//当前派发部门
				addresstask.setDispatcherOrgId(disDepart.getId());
				addresstask.setDispatcherOrgCode(disDepart.getOrgCode());
				addresstask.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				addresstask.setDispatcherId(cjxtTask.getDispatcherId());
				addresstask.setDispatcherName(cjxtTask.getDispatcherName());
				//接收部门、部门负责人信息
				if (recOrgId != null && recOrgId.length() > 0 && recOrgId.charAt(recOrgId.length() - 1) == ',') {
					recOrgId = recOrgId.substring(0, recOrgId.length() - 1);
				}
				if (recOrgName != null && recOrgName.length() > 0 && recOrgName.charAt(recOrgName.length() - 1) == ',') {
					recOrgName = recOrgName.substring(0, recOrgName.length() - 1);
				}
				if (recOrgCode != null && recOrgCode.length() > 0 && recOrgCode.charAt(recOrgCode.length() - 1) == ',') {
					recOrgCode = recOrgCode.substring(0, recOrgCode.length() - 1);
				}
				addresstask.setReceiverOrgId(recOrgId);
				addresstask.setReceiverOrgCode(recOrgCode);
				addresstask.setReceiverOrgName(recOrgName);
				addresstask.setReceiverBmfzrId(recUserId);
				addresstask.setReceiverBmfzrZh(recUserZh);
				addresstask.setReceiverBmfzrName(recUserName);
				//接收人信息：：：接收人为空存入部门负责人信息
				addresstask.setReceiverId(receiverUser.getId());
				addresstask.setReceiverZh(receiverUser.getUsername());
				addresstask.setReceiverName(receiverUser.getRealname());
				addresstask.setDueDate(cjxtTask.getDueDate());
				//采集情况
				addresstask.setCjZs(cjzs);
				addresstask.setCjYwc(0);
				addresstask.setCjSy(cjzs);
				addresstask.setCjWcqk("0%");
				addresstask.setRwzt("2");
				addresstask.setChzt("1");
				if(cjxtTask.getPid()==null){
					isFirst = true;
					cjxtTaskService.addCjxtTask(addresstask);
				}else {
					isFirst = false;
					CjxtTask task = new CjxtTask();
					task.setId(cjxtTask.getId());
					task.setRwzt("5");
					task.setPid(cjxtTask.getPid());
					if("2".equals(cjxtTask.getChzt())){
						task.setReceiverOrgId(recOrgId);
						task.setReceiverOrgCode(recOrgCode);
						task.setReceiverOrgName(recOrgName);
						task.setReceiverBmfzrId(recUserId);
						task.setReceiverBmfzrZh(recUserZh);
						task.setReceiverBmfzrName(recUserName);
						//接收人信息：：：接收人为空存入部门负责人信息
						task.setReceiverId(receiverUser.getId());
						task.setReceiverZh(receiverUser.getUsername());
						task.setReceiverName(receiverUser.getRealname());
						task.setDueDate(cjxtTask.getDueDate());
						//采集情况
						task.setCjZs(cjzs);
						task.setCjYwc(0);
						task.setCjSy(cjzs);
						task.setCjWcqk("0%");
						task.setChzt("1");
					}
					cjxtTaskService.updateCjxtTask(task);
				}

				//创建接收部门任务
				uuid = UUID.randomUUID().toString().replace("-","");
				String bmfzrId= "";
				String bmfzrZh= "";
				String bmfzrName= "";
				int cjzsChild = 0;
				//接收部门详细信息
				String receiverOrgId = addresstask.getReceiverOrgId();
				SysDepart receiverName = sysDepartService.getById(receiverOrgId);
				//接收部门负责人信息 接收人
				List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));

				//创建子接收部门数据
				CjxtTask taskChild = new CjxtTask();
				taskChild.setId(uuid);
				//节点信息
				if(cjxtTask.getId()!=null){
					taskChild.setPid(cjxtTask.getId());
					taskChild.setTaskCode(cjxtTask.getTaskCode());
				}else {
					taskChild.setPid(addresstask.getId());
					taskChild.setTaskCode(addresstask.getTaskCode());
				}

				taskChild.setHasChild(cjxtTask.getHasChild());
				//任务信息
				taskChild.setTaskName(cjxtTask.getTaskName());
				taskChild.setTaskDescription(cjxtTask.getTaskDescription());
				//模板信息
				taskChild.setMbId(cjxtTask.getMbId());
				taskChild.setMbName(cjxtTask.getMbName());
				taskChild.setMbCode(cjxtTask.getMbCode());
				taskChild.setBm(cjxtTask.getBm());
				//部门数据权限==派发部门
				taskChild.setOrgId(receiverName.getId());
				taskChild.setOrgCode(receiverName.getOrgCode());
				taskChild.setOrgName(receiverName.getDepartNameFull());
				//当前派发部门
				taskChild.setDispatcherOrgId(disDepart.getId());
				taskChild.setDispatcherOrgCode(disDepart.getOrgCode());
				taskChild.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				taskChild.setDispatcherId(cjxtTask.getDispatcherId());
				taskChild.setDispatcherName(cjxtTask.getDispatcherName());
				//接收部门、部门负责人信息
				taskChild.setReceiverOrgId(receiverName.getId());
				taskChild.setReceiverOrgCode(receiverName.getOrgCode());
				taskChild.setReceiverOrgName(receiverName.getDepartNameFull());
				if(bmfzrList!=null){
					for(CjxtBmfzr cjxtBmfzr: bmfzrList){
						bmfzrId+=cjxtBmfzr.getFzryId();
						bmfzrZh+=cjxtBmfzr.getLxdh();
						bmfzrName+=cjxtBmfzr.getFzryName();
					}
					taskChild.setReceiverBmfzrId(bmfzrId);
					taskChild.setReceiverBmfzrZh(bmfzrZh);
					taskChild.setReceiverBmfzrName(bmfzrName);
				}
				//接收人信息
				taskChild.setReceiverId(receiverUser.getId());
				taskChild.setReceiverZh(receiverUser.getUsername());
				taskChild.setReceiverName(receiverUser.getRealname());
				taskChild.setRwzt("2");
				taskChild.setDueDate(cjxtTask.getDueDate());
				//采集情况
//				taskChild.setCjZs(cjzsChild);
				taskChild.setCjYwc(0);
//				taskChild.setCjSy(cjzsChild);
				taskChild.setCjWcqk("0%");
				taskChild.setChzt("1");
				cjxtTaskService.addCjxtTask(taskChild);

				//创建子表任务
				List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,cjxtTask.getAddressQhId()));
				int j =0 ;
				if(addressList!=null){
					java.util.Collection<CjxtTaskDispatch> entityList = new ArrayList<>();
					for(CjxtStandardAddress cjxtStandardAddress: addressList){
						j++;
						CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
						if(!"".equals(cjxtTask.getBlId()) && cjxtTask.getBlId()!=null){
							cjxtTaskDispatch.setBlId(cjxtTask.getBlId());
						}
						cjxtTaskDispatch.setTaskId(taskChild.getId());
						cjxtTaskDispatch.setTaskCode(taskChild.getTaskCode());
						cjxtTaskDispatch.setTaskName(cjxtTask.getTaskName());
						cjxtTaskDispatch.setTaskDescription(cjxtTask.getTaskDescription());
						cjxtTaskDispatch.setMbId(cjxtTask.getMbId());
						cjxtTaskDispatch.setMbCode(cjxtTask.getMbCode());
						cjxtTaskDispatch.setMbName(cjxtTask.getMbName());
						cjxtTaskDispatch.setBm(cjxtTask.getBm());
						//采集地址
						cjxtTaskDispatch.setAddressId(cjxtStandardAddress.getId());
						cjxtTaskDispatch.setAddressCode(cjxtStandardAddress.getAddressCodeMz());
						cjxtTaskDispatch.setAddressName(cjxtStandardAddress.getAddressNameMz());
						cjxtTaskDispatch.setDispatcherId(cjxtTask.getDispatcherId());
						cjxtTaskDispatch.setDispatcherName(cjxtTask.getDispatcherName());
						cjxtTaskDispatch.setReceiverId(receiverUser.getId());
						cjxtTaskDispatch.setReceiverName(receiverUser.getRealname());
						cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
						cjxtTaskDispatch.setRwzt("2");
						String sql = "SELECT id,create_time,update_time FROM " + cjxtTask.getBm() + " WHERE del_flag = '0' AND address_id = '" + cjxtStandardAddress.getId()+"' ORDER BY create_time ASC LIMIT 1";
						List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
						Date createTime = null;
						Date updateTime = null;
						if(resultList.size()>0){
							Map<String, Object> row = resultList.get(0);
							Object id = row.get("id");
							LocalDateTime create_time = (LocalDateTime) row.get("create_time");
							LocalDateTime update_time = (LocalDateTime) row.get("update_time");
							if(create_time != null){
								createTime = java.sql.Timestamp.valueOf(create_time);
							}
							if(update_time != null){
								updateTime = java.sql.Timestamp.valueOf(update_time);
							}
							cjxtTaskDispatch.setDataId(String.valueOf(id));
							if(updateTime!=null){
								cjxtTaskDispatch.setSchssj(updateTime);
							}else {
								cjxtTaskDispatch.setSchssj(createTime);
							}
							cjxtTaskDispatch.setHszt("2");
						}
						entityList.add(cjxtTaskDispatch);
					}
					cjxtTaskDispatchService.saveOrUpdateBatch(entityList);
					CjxtTask taskFather = new CjxtTask();
					taskFather.setId(taskChild.getId());
					taskFather.setCjZs(j);
					taskFather.setCjYwc(0);
					taskFather.setCjSy(j);
					taskFather.setCjWcqk("0%");
					cjxtTaskService.updateById(taskFather);

					CjxtTask taskFathers = new CjxtTask();
					if(isFirst == false){
						taskFathers.setId(cjxtTask.getId());
					}else {
						taskFathers.setId(addresstask.getId());
					}
					taskFathers.setCjZs(j);
					taskFathers.setCjYwc(0);
					taskFathers.setCjSy(j);
					taskFathers.setCjWcqk("0%");
					cjxtTaskService.updateById(taskFathers);
				}
				if(addressList.size()<=0){
					//父级上级任务
					CjxtTask taskS = new CjxtTask();
					taskS.setId(taskChild.getPid());
					if(isFirst==true){
						cjxtTaskService.removeById(taskS);
					}else {
						taskS.setRwzt("1");
						taskS.setHasChild("0");
						cjxtTaskService.updateById(taskS);
					}
					//父级任务
					CjxtTask task = new CjxtTask();
					task.setId(taskChild.getId());
					cjxtTaskService.removeById(task);
					return Result.error("当前地址区划,不存在采集地址,请选择详细采集地址!");
				}
				try {
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && !taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}

						String[] receiverZhs = taskChild.getReceiverZh().split(",");
						if(receiverZhs.length>0 && !"".equals(receiverZhs[0])){
							for(int k=0;k<receiverZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(receiverZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if((cjxtTask.getAddressQhId()!=null || cjxtTask.getAddressQhId()==null) && cjxtTask.getAddressId()!=null){
				String[] addressIdSize = cjxtTask.getAddressId().split(",");

				//当前派发部门信息
				SysDepart disDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtTask.getDispatcherOrgCode()).last("LIMIT 1"));
				//接收部门信息
				String[] receiverOrgIds = null;
				if(cjxtTask.getReceiverOrgId()!=null && !"".equals(cjxtTask.getReceiverOrgId())){
					receiverOrgIds = cjxtTask.getReceiverOrgId().split(",");
				}else {
					receiverOrgIds = cjxtTask.getReceiverOrgName().split(",");
				}
				int cjzs = 0;
				for(int i = 0;i<receiverOrgIds.length;i++){
					//接收部门详细信息
					String receiverOrgId = receiverOrgIds[i];
					SysDepart receiverName = sysDepartService.getById(receiverOrgId);
					//接收部门负责人信息 接收人
					List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));
					if(bmfzrList!=null){
						for(CjxtBmfzr cjxtBmfzr:bmfzrList){
							if(!"".equals(recUserId)){
								recUserId+=","+cjxtBmfzr.getFzryId();
								recUserZh+=","+cjxtBmfzr.getLxdh();
								recUserName+=","+cjxtBmfzr.getFzryName();
							}else {
								recUserId+=cjxtBmfzr.getFzryId();
								recUserZh+=cjxtBmfzr.getLxdh();
								recUserName+=cjxtBmfzr.getFzryName();
							}
						}
					}
					recOrgId+=receiverName.getId()+",";
					recOrgName+=receiverName.getDepartNameFull()+",";
					recOrgCode+=receiverName.getOrgCode()+",";
					//接收部门采集总数
//					List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,receiverName.getId()));
//					if(addressList!=null){
//						cjzs += addressList.size();
//					}
				}
				uuid = UUID.randomUUID().toString().replace("-","");
				if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
					addresstask.setId(uuid);
					addresstask.setTaskCode(uuid);
				}else {
					addresstask.setId(uuid);
					addresstask.setTaskCode(cjxtTask.getId());
				}
				//节点信息
				addresstask.setPid(cjxtTask.getPid());
				addresstask.setHasChild(cjxtTask.getHasChild());
				//任务信息
				addresstask.setTaskName(cjxtTask.getTaskName());
				addresstask.setTaskDescription(cjxtTask.getTaskDescription());
				//模板信息
				addresstask.setMbId(cjxtTask.getMbId());
				addresstask.setMbName(cjxtTask.getMbName());
				addresstask.setMbCode(cjxtTask.getMbCode());
				addresstask.setBm(cjxtTask.getBm());
				//部门数据权限==派发部门
				addresstask.setOrgId(disDepart.getId());
				addresstask.setOrgCode(disDepart.getOrgCode());
				addresstask.setOrgName(disDepart.getDepartNameFull());
				//当前派发部门
				addresstask.setDispatcherOrgId(disDepart.getId());
				addresstask.setDispatcherOrgCode(disDepart.getOrgCode());
				addresstask.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				addresstask.setDispatcherId(cjxtTask.getDispatcherId());
				addresstask.setDispatcherName(cjxtTask.getDispatcherName());
				//接收部门、部门负责人信息
				if (recOrgId != null && recOrgId.length() > 0 && recOrgId.charAt(recOrgId.length() - 1) == ',') {
					recOrgId = recOrgId.substring(0, recOrgId.length() - 1);
				}
				if (recOrgName != null && recOrgName.length() > 0 && recOrgName.charAt(recOrgName.length() - 1) == ',') {
					recOrgName = recOrgName.substring(0, recOrgName.length() - 1);
				}
				if (recOrgCode != null && recOrgCode.length() > 0 && recOrgCode.charAt(recOrgCode.length() - 1) == ',') {
					recOrgCode = recOrgCode.substring(0, recOrgCode.length() - 1);
				}
				addresstask.setReceiverOrgId(recOrgId);
				addresstask.setReceiverOrgCode(recOrgCode);
				addresstask.setReceiverOrgName(recOrgName);
				addresstask.setReceiverBmfzrId(recUserId);
				addresstask.setReceiverBmfzrZh(recUserZh);
				addresstask.setReceiverBmfzrName(recUserName);
				//接收人信息：：：接收人为空存入部门负责人信息
				addresstask.setReceiverId(receiverUser.getId());
				addresstask.setReceiverZh(receiverUser.getUsername());
				addresstask.setReceiverName(receiverUser.getRealname());
				addresstask.setDueDate(cjxtTask.getDueDate());
				//采集情况
				addresstask.setCjZs(addressIdSize.length);
				addresstask.setCjYwc(0);
				addresstask.setCjSy(addressIdSize.length);
				addresstask.setCjWcqk("0%");
				addresstask.setRwzt("2");
				addresstask.setChzt("1");
				if(addresstask.getPid()==null){
					cjxtTaskService.addCjxtTask(addresstask);
				}else {
					CjxtTask task = new CjxtTask();
					task.setId(cjxtTask.getId());
					task.setPid(cjxtTask.getPid());
					task.setRwzt("5");
					task.setTaskName(cjxtTask.getTaskName());
					task.setTaskDescription(cjxtTask.getTaskDescription());

					if("2".equals(cjxtTask.getChzt())){
						task.setReceiverOrgId(recOrgId);
						task.setReceiverOrgCode(recOrgCode);
						task.setReceiverOrgName(recOrgName);
						task.setReceiverBmfzrId(recUserId);
						task.setReceiverBmfzrZh(recUserZh);
						task.setReceiverBmfzrName(recUserName);
						//接收人信息：：：接收人为空存入部门负责人信息
						task.setReceiverId(receiverUser.getId());
						task.setReceiverZh(receiverUser.getUsername());
						task.setReceiverName(receiverUser.getRealname());
						task.setDueDate(cjxtTask.getDueDate());
						//采集情况
						task.setCjZs(addressIdSize.length);
						task.setCjYwc(0);
						task.setCjSy(addressIdSize.length);
						task.setCjWcqk("0%");
						task.setChzt("1");
					}
					cjxtTaskService.updateCjxtTask(task);
				}

				//创建接收部门任务
				uuid = UUID.randomUUID().toString().replace("-","");
				String bmfzrId= "";
				String bmfzrZh= "";
				String bmfzrName= "";
				int cjzsChild = 0;
				//接收部门详细信息
				String receiverOrgId = addresstask.getReceiverOrgId();
				SysDepart receiverName = sysDepartService.getById(receiverOrgId);
				//接收部门负责人信息 接收人
				List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));

				//创建子接收部门数据
				CjxtTask taskChild = new CjxtTask();
				taskChild.setId(uuid);
				//节点信息
				if(cjxtTask.getId()!=null){
					taskChild.setPid(cjxtTask.getId());
					taskChild.setTaskCode(cjxtTask.getTaskCode());
				}else {
					taskChild.setPid(addresstask.getId());
					taskChild.setTaskCode(addresstask.getTaskCode());
				}
				taskChild.setHasChild(addresstask.getHasChild());
				//任务信息
				taskChild.setTaskName(addresstask.getTaskName());
				taskChild.setTaskDescription(addresstask.getTaskDescription());
				//模板信息
				taskChild.setMbId(addresstask.getMbId());
				taskChild.setMbName(addresstask.getMbName());
				taskChild.setMbCode(addresstask.getMbCode());
				taskChild.setBm(addresstask.getBm());
				//部门数据权限==派发部门
				taskChild.setOrgId(receiverName.getId());
				taskChild.setOrgCode(receiverName.getOrgCode());
				taskChild.setOrgName(receiverName.getDepartNameFull());
				//当前派发部门
				taskChild.setDispatcherOrgId(disDepart.getId());
				taskChild.setDispatcherOrgCode(disDepart.getOrgCode());
				taskChild.setDispatcherOrgName(disDepart.getDepartNameFull());
				//当前派发人
				taskChild.setDispatcherId(addresstask.getDispatcherId());
				taskChild.setDispatcherName(addresstask.getDispatcherName());
				//接收部门、部门负责人信息
				taskChild.setReceiverOrgId(receiverName.getId());
				taskChild.setReceiverOrgCode(receiverName.getOrgCode());
				taskChild.setReceiverOrgName(receiverName.getDepartNameFull());
				if(bmfzrList!=null){
					for(CjxtBmfzr cjxtBmfzr: bmfzrList){
						bmfzrId+=cjxtBmfzr.getFzryId();
						bmfzrZh+=cjxtBmfzr.getLxdh();
						bmfzrName+=cjxtBmfzr.getFzryName();
					}
					taskChild.setReceiverBmfzrId(bmfzrId);
					taskChild.setReceiverBmfzrZh(bmfzrZh);
					taskChild.setReceiverBmfzrName(bmfzrName);
				}
				//接收人信息
				taskChild.setReceiverId(receiverUser.getId());
				taskChild.setReceiverZh(receiverUser.getUsername());
				taskChild.setReceiverName(receiverUser.getRealname());
				taskChild.setRwzt("2");
				taskChild.setDueDate(addresstask.getDueDate());
				//采集情况
				taskChild.setCjZs(addressIdSize.length);
				taskChild.setCjYwc(0);
				taskChild.setCjSy(addressIdSize.length);
				taskChild.setCjWcqk("0%");
				taskChild.setChzt("1");
				cjxtTaskService.addCjxtTask(taskChild);
				//数据补录
				if(addresstask.getPid()!=null){
					CjxtTask task = new CjxtTask();
					task.setId(cjxtTask.getId());
					task.setReceiverId(taskChild.getReceiverId());
					task.setReceiverName(taskChild.getReceiverName());
					task.setReceiverZh(taskChild.getReceiverZh());
					task.setReceiverBmfzrId(taskChild.getReceiverBmfzrId());
					task.setReceiverBmfzrZh(taskChild.getReceiverBmfzrZh());
					task.setReceiverBmfzrName(taskChild.getReceiverBmfzrName());
					task.setReceiverOrgId(taskChild.getReceiverOrgId());
					task.setReceiverOrgName(taskChild.getReceiverOrgName());
					task.setReceiverOrgCode(taskChild.getReceiverOrgCode());
					cjxtTaskService.updateById(task);
				}

				//创建子表任务
				String[] addressIds = cjxtTask.getAddressId().split(",");
				String[] addressCodes = cjxtTask.getAddressCode().split(",");
				String[] addressNames = cjxtTask.getAddressName().split(",");
				int zs = 0;
				java.util.Collection<CjxtTaskDispatch> entityList = new ArrayList<>();
				for (int j = 0; j < addressIds.length; j++) {
					zs++;
					String addressId = addressIds[j];
					String addressCode = addressCodes[j];
					String addressName = addressNames[j];
					//查询任务是否已派发
					List<CjxtTaskDispatch> dispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbCode,cjxtTask.getMbCode()).eq(CjxtTaskDispatch::getMbId,cjxtTask.getMbId()).eq(CjxtTaskDispatch::getAddressId,addressId).last(" ORDER BY create_time LIMIT 1 "));
					CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
					if(!"".equals(cjxtTask.getBlId()) && cjxtTask.getBlId()!=null){
						cjxtTaskDispatch.setBlId(cjxtTask.getBlId());
					}
					cjxtTaskDispatch.setTaskId(taskChild.getId());
					cjxtTaskDispatch.setTaskCode(taskChild.getTaskCode());
					cjxtTaskDispatch.setTaskName(addresstask.getTaskName());
					cjxtTaskDispatch.setTaskDescription(addresstask.getTaskDescription());
					cjxtTaskDispatch.setMbId(addresstask.getMbId());
					cjxtTaskDispatch.setMbCode(addresstask.getMbCode());
					cjxtTaskDispatch.setMbName(taskChild.getMbName());
					cjxtTaskDispatch.setBm(taskChild.getBm());
					cjxtTaskDispatch.setAddressId(addressId);
					cjxtTaskDispatch.setAddressCode(addressCode);
					cjxtTaskDispatch.setAddressName(addressName);
					cjxtTaskDispatch.setDispatcherId(taskChild.getDispatcherId());
					cjxtTaskDispatch.setDispatcherName(taskChild.getDispatcherName());
					cjxtTaskDispatch.setReceiverId(receiverUser.getId());
					cjxtTaskDispatch.setReceiverName(receiverUser.getRealname());
					cjxtTaskDispatch.setDueDate(taskChild.getDueDate());
					String sql = "SELECT id,create_time,update_time FROM " + cjxtTask.getBm() + " WHERE del_flag = '0' AND address_id = '" + addressId +"' ORDER BY create_time ASC LIMIT 1";
					List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
					Date createTime = null;
					Date updateTime = null;
					if(resultList.size()>0){
						Map<String, Object> row = resultList.get(0);
						Object id = row.get("id");
						LocalDateTime create_time = (LocalDateTime) row.get("create_time");
						LocalDateTime update_time = (LocalDateTime) row.get("update_time");
						if(create_time != null){
							createTime = java.sql.Timestamp.valueOf(create_time);
						}
						if(update_time != null){
							updateTime = java.sql.Timestamp.valueOf(update_time);
						}
						cjxtTaskDispatch.setDataId(String.valueOf(id));
						if(updateTime!=null){
							cjxtTaskDispatch.setSchssj(updateTime);
						}else {
							cjxtTaskDispatch.setSchssj(createTime);
						}
						cjxtTaskDispatch.setHszt("2");
					}
					cjxtTaskDispatch.setRwzt("2");
					entityList.add(cjxtTaskDispatch);
				}
				cjxtTaskDispatchService.saveOrUpdateBatch(entityList);

				CjxtTask taskFathers = new CjxtTask();
				taskFathers.setId(cjxtTask.getId());
				taskFathers.setCjZs(addressIdSize.length);
				taskFathers.setCjYwc(0);
				taskFathers.setCjSy(addressIdSize.length);
				taskFathers.setCjWcqk("0%");
				cjxtTaskService.updateById(taskFathers);
				try {
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0 && !"".equals(bmfzrZhs[0])){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
					if(taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && !taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						System.out.println(bmfzrZhs.length);
						if(bmfzrZhs.length > 0 && !"".equals(bmfzrZhs[0])){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}

						String[] receiverZhs = taskChild.getReceiverZh().split(",");
						if(receiverZhs.length>0 && !"".equals(receiverZhs[0])){
							for(int k=0;k<receiverZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(receiverZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+disDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(taskChild.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return Result.OK("添加成功！");
	}

	/**
	 *   首次派发
	 *
	 * @param cjxtTask
	 * @return
	 */
	@AutoLog(value = "任务派发-首次派发")
	@ApiOperation(value="任务派发-首次派发", notes="任务派发-首次派发")
	@PostMapping(value = "/rwpfCjxtTask")
	public Result<String> rwpfCjxtTask(@RequestBody CjxtTask cjxtTask) {
		cjxtTask.setHasChild("1");
		cjxtTask.setRwzt("2");
		cjxtTaskService.updateById(cjxtTask);

		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//新派发部门信息
		SysDepart sysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtTask.getDispatcherOrgCode()).last("LIMIT 1"));
		//接收部门信息 receiverOrgName接收的值为部门ID
		String[] receiverOrgIds = cjxtTask.getReceiverOrgId().split(",");
		for(int i = 0;i<receiverOrgIds.length;i++){
			//接收部门信息
			String receiverOrgId = receiverOrgIds[i];
			SysDepart receiverName = sysDepartService.getById(receiverOrgId);

			CjxtTask task = new CjxtTask();
			//节点信息
			task.setId(null);
			task.setPid(cjxtTask.getId());
			task.setHasChild(cjxtTask.getId());
			task.setTaskCode(cjxtTask.getId());
			//任务信息
			task.setTaskName(cjxtTask.getTaskName());
			task.setTaskDescription(cjxtTask.getTaskDescription());
			//模板信息
			task.setMbId(cjxtTask.getMbId());
			task.setMbName(cjxtTask.getMbName());
			task.setMbCode(cjxtTask.getMbCode());
			task.setBm(cjxtTask.getBm());
			//部门数据权限ID
			task.setOrgId(receiverOrgId);
			task.setOrgCode(receiverName.getOrgCode());
			task.setOrgName(receiverName.getDepartNameFull());
			//派发部门人员信息
			task.setDispatcherId(cjxtTask.getDispatcherId());
			task.setDispatcherName(cjxtTask.getDispatcherName());
			task.setDispatcherOrgId(cjxtTask.getDispatcherOrgId());
			task.setDispatcherOrgName(cjxtTask.getDispatcherOrgName());
			task.setDispatcherOrgCode(cjxtTask.getDispatcherOrgCode());
//			 task.setDispatcherOrgId(null);
//			 task.setDispatcherOrgName(null);
//			 task.setDispatcherOrgCode(null);
			//接收部门
			task.setReceiverOrgId(receiverOrgId);
			task.setReceiverOrgName(receiverName.getDepartNameFull());
			task.setReceiverOrgCode(receiverName.getOrgCode());
			task.setDueDate(cjxtTask.getDueDate());
			//接收人不为空
			if(cjxtTask.getReceiverId()!=null && cjxtTask.getAddressId()==null){
				int j =0;
				//接收人信息
				//采集地址：：：：接收部门地址查询数据权限 查询详细地址
				task.setReceiverId(cjxtTask.getReceiverId());
				task.setReceiverName(cjxtTask.getReceiverName());
				cjxtTaskService.addCjxtTask(task);
				//userSf===1 接收人为网格员
				if("1".equals(cjxtTask.getUserSf())){
					List<CjxtStandardAddressPerson> cjxtStandardAddressPersonList = cjxtStandardAddressPersonService.list(new LambdaQueryWrapper<CjxtStandardAddressPerson>().eq(CjxtStandardAddressPerson::getUserId,cjxtTask.getReceiverId()));
					if(cjxtStandardAddressPersonList!=null){
						for(CjxtStandardAddressPerson person: cjxtStandardAddressPersonList){
							j++;
							CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
							cjxtTaskDispatch.setTaskId(cjxtTask.getId());
							cjxtTaskDispatch.setTaskName(cjxtTask.getTaskName());
							cjxtTaskDispatch.setTaskDescription(cjxtTask.getTaskDescription());
							cjxtTaskDispatch.setMbId(cjxtTask.getMbId());
							cjxtTaskDispatch.setMbCode(cjxtTask.getMbCode());
							cjxtTaskDispatch.setMbName(cjxtTask.getMbName());
							cjxtTaskDispatch.setBm(cjxtTask.getBm());
							//采集地址
							cjxtTaskDispatch.setAddressId(person.getId());
							cjxtTaskDispatch.setAddressCode(person.getAddressCode());
							cjxtTaskDispatch.setAddressName(person.getAddressName());
							cjxtTaskDispatch.setDispatcherId(cjxtTask.getDispatcherId());
							cjxtTaskDispatch.setDispatcherName(cjxtTask.getDispatcherName());
							cjxtTaskDispatch.setReceiverId(cjxtTask.getReceiverId());
							cjxtTaskDispatch.setReceiverName(cjxtTask.getReceiverName());
							cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
							cjxtTaskDispatch.setRwzt("2");
							cjxtTaskDispatchService.save(cjxtTaskDispatch);
						}
						if(j==0){
							return Result.error("当前接收部门,不存在详细采集地址!");
						}
					}
				}else {
					List<CjxtBmData> bmData = cjxtBmDataService.list(new LambdaQueryWrapper<CjxtBmData>().eq(CjxtBmData::getOrgCode,receiverName.getOrgCode()));
					if(bmData.size()>0){
						for(CjxtBmData cjxtBmData :bmData){
							//数据权限地址
							SysDepart taskdepart = sysDepartService.getById(cjxtBmData.getDataOrgId());
							if(taskdepart!=null){
								List<CjxtStandardAddress> cjxtStandardAddressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressCodeMz,taskdepart.getOrgCode()));
								if(cjxtStandardAddressList != null){
									for(CjxtStandardAddress cjxtStandardAddress :cjxtStandardAddressList){
										j++;
										CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
										cjxtTaskDispatch.setTaskId(cjxtTask.getId());
										cjxtTaskDispatch.setTaskName(cjxtTask.getTaskName());
										cjxtTaskDispatch.setTaskDescription(cjxtTask.getTaskDescription());
										cjxtTaskDispatch.setMbId(cjxtTask.getMbId());
										cjxtTaskDispatch.setMbCode(cjxtTask.getMbCode());
										cjxtTaskDispatch.setMbName(cjxtTask.getMbName());
										cjxtTaskDispatch.setBm(cjxtTask.getBm());
										//采集地址
										cjxtTaskDispatch.setAddressId(cjxtStandardAddress.getId());
										cjxtTaskDispatch.setAddressCode(cjxtStandardAddress.getAddressCode());
										cjxtTaskDispatch.setAddressName(cjxtStandardAddress.getAddressNameMz());
										cjxtTaskDispatch.setDispatcherId(cjxtTask.getDispatcherId());
										cjxtTaskDispatch.setDispatcherName(cjxtTask.getDispatcherName());
										cjxtTaskDispatch.setReceiverId(cjxtTask.getReceiverId());
										cjxtTaskDispatch.setReceiverName(cjxtTask.getReceiverName());
										cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
										cjxtTaskDispatch.setRwzt("2");
										cjxtTaskDispatchService.save(cjxtTaskDispatch);
									}
								}
							}
							if(j==0){
								return Result.error("当前接收部门,不存在详细采集地址!");
							}
						}
					}else {
						return Result.error("当前接收部门,不存在详细采集地址!");
					}
				}
				UpdateWrapper<CjxtTask> updateWrapper = new UpdateWrapper<>();
				updateWrapper.eq("task_code", task.getTaskCode());
				updateWrapper.set("cj_zs", j);
				updateWrapper.set("cj_sy", j);
				updateWrapper.set("cj_ywc", "0");
				updateWrapper.set("cj_wcqk", "0%");
				cjxtTaskService.update(updateWrapper);
			}else if(cjxtTask.getReceiverId()!=null && cjxtTask.getAddressId()!=null){
				cjxtTaskService.addCjxtTask(task);
				//接收人员和接收地址不为空
				String[] addressIds = cjxtTask.getAddressId().split(",");
				String[] addressCodes = cjxtTask.getAddressCode().split(",");
				String[] addressNames = cjxtTask.getAddressName().split(",");
				int zs = 0;
				for (int j = 0; j < addressIds.length; j++) {
					zs++;
					String addressId = addressIds[j];
					String addressCode = addressCodes[j];
					String addressName = addressNames[j];

					CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
					cjxtTaskDispatch.setTaskId(cjxtTask.getId());
					cjxtTaskDispatch.setTaskName(cjxtTask.getTaskName());
					cjxtTaskDispatch.setTaskDescription(cjxtTask.getTaskDescription());
					cjxtTaskDispatch.setMbId(cjxtTask.getMbId());
					cjxtTaskDispatch.setMbCode(cjxtTask.getMbCode());
					cjxtTaskDispatch.setMbName(cjxtTask.getMbName());
					cjxtTaskDispatch.setBm(cjxtTask.getBm());
					cjxtTaskDispatch.setAddressId(addressId);
					cjxtTaskDispatch.setAddressCode(addressCode);
					cjxtTaskDispatch.setAddressName(addressName);
					cjxtTaskDispatch.setDispatcherId(cjxtTask.getDispatcherId());
					cjxtTaskDispatch.setDispatcherName(cjxtTask.getDispatcherName());
					cjxtTaskDispatch.setReceiverId(cjxtTask.getReceiverId());
					cjxtTaskDispatch.setReceiverName(cjxtTask.getReceiverName());
					cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
					cjxtTaskDispatch.setRwzt("2");
					cjxtTaskDispatchService.save(cjxtTaskDispatch);
				}
				UpdateWrapper<CjxtTask> updateWrapper = new UpdateWrapper<>();
				updateWrapper.eq("task_code", task.getTaskCode());
				updateWrapper.set("cj_zs", zs);
				updateWrapper.set("cj_sy", zs);
				updateWrapper.set("cj_ywc", "0");
				updateWrapper.set("cj_wcqk", "0%");
				cjxtTaskService.update(updateWrapper);
			}else if(cjxtTask.getReceiverId()==null && cjxtTask.getAddressId()==null){
				//接收人员和详细采集地址都为空 接收人信息部门负责人员
				CjxtBmfzr cjxtBmfzr = cjxtBmfzrService.getOne(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverOrgId).last("LIMIT 1"));
				if(cjxtBmfzr!=null){
					task.setReceiverBmfzrId(cjxtBmfzr.getFzryId());
					task.setReceiverBmfzrName(cjxtBmfzr.getFzryName());
				}
				//-----------------------------------发送系统消息-----------------------------------//
				cjxtTaskService.addCjxtTask(task);
				return Result.OK("派发成功！");
			}
		}
		return Result.OK("派发成功！");
	}

	public String truncateToLength(String input, int maxLength) {
		if (input.length() > maxLength) {
			return input.substring(0, maxLength);
		} else {
			return input;
		}
	}

	/**
	 *  一键派发
	 *
	 * @param cjxtTask
	 * @return
	 */
	@AutoLog(value = "任务派发-一键派发")
	@ApiOperation(value="任务派发-一键派发", notes="任务派发-一键派发")
	@PostMapping(value = "/oneClack")
	public Result<String> oneClack(@RequestBody CjxtTask cjxtTask) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//获取部门信息
		SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
		String recOrgId = "";
		String recOrgName = "";
		String recOrgCode = "";
		String recRyId = "";
		String recRyZh = "";
		String recRyName = "";
		//			 网格员	1
		//			 民警	2
		//			 辅警	3
		//			 社区工作人员	4
		//			 街办工作人员	5
		//			 委办局工作人员	6
		//			 派出所工作人员	7
		//			 分局工作人员	8
		//			 市局工作人员	9
		String dataSql = "";
		List<Map<String, Object>> resultList = new ArrayList<>();
		String taskId = "";
		if("4".equals(sysUser.getUserSf())){
			taskId =  cjxtTask.getId();
			//任务id为空，即为新增
			if(taskId==null || "".equals(taskId)){
				cjxtTask.setRwzt("1");
				cjxtTask.setDispatcherOrgId(sysDepart.getId());
				cjxtTask.setDispatcherOrgName(sysDepart.getDepartNameFull());
				cjxtTask.setDispatcherOrgCode(sysDepart.getOrgCode());
				//部门数据权限ID
				cjxtTask.setOrgId(sysDepart.getId());
				cjxtTask.setOrgName(sysDepart.getDepartNameFull());
				cjxtTask.setOrgCode(sysDepart.getOrgCode());
				cjxtTaskService.addCjxtTask(cjxtTask);
			}

			dataSql = "select s.depart_name,d.dep_id,s.org_code,u.realname,u.username,d.user_id,(select count(1) from cjxt_standard_address p where p.address_code_mz=s.org_code and  p.del_flag='0' ) cjsl\n" +
					"from sys_depart s,sys_user_depart d,sys_user u \n" +
					"where s.id=d.dep_id and d.user_id=u.id and s.org_code like '" + sysDepart.getOrgCode() + "%' and s.org_code !='" + sysDepart.getOrgCode() + "'\n" +
					"order by s.org_code ";
			resultList = jdbcTemplate.queryForList(dataSql);
			if(resultList != null && resultList.size() > 0){
				java.util.Collection<CjxtTask> entityList = new ArrayList<>();
				Map<String, Object> map = new HashMap<String, Object>();
				int cjzsl = 0;
				int cjsl = 0;
				for(int i=0;i<resultList.size();i++){
					map = resultList.get(i);
					//采集数量为0，说明没有配置采集地址，不派发任务
					cjsl = Integer.parseInt(map.get("CJSL").toString());
					if(cjsl == 0) {
						continue;
					}

					CjxtTask task = new CjxtTask();
					//节点信息
					task.setPid(cjxtTask.getId());
					//任务信息
					task.setTaskName(cjxtTask.getTaskName());
					task.setTaskDescription(cjxtTask.getTaskDescription());
					//模板信息
					task.setMbId(cjxtTask.getMbId());
					task.setMbName(cjxtTask.getMbName());
					task.setMbCode(cjxtTask.getMbCode());
					task.setBm(cjxtTask.getBm());
					//部门数据权限ID
					task.setOrgId(map.get("DEP_ID").toString());
					task.setOrgName(map.get("DEPART_NAME").toString());
					task.setOrgCode(map.get("ORG_CODE").toString());
					//派发部门人员信息
					if(taskId==null || "".equals(taskId)){
						task.setDispatcherId(cjxtTask.getDispatcherId());
						task.setDispatcherName(cjxtTask.getDispatcherName());
						task.setDispatcherOrgId(cjxtTask.getDispatcherOrgId());
						task.setDispatcherOrgName(cjxtTask.getDispatcherOrgName());
						task.setDispatcherOrgCode(cjxtTask.getDispatcherOrgCode());
						task.setTaskCode(cjxtTask.getId());
					}else{
						task.setDispatcherId(sysUser.getId());
						task.setDispatcherName(sysUser.getRealname());
						task.setDispatcherOrgId(sysDepart.getId());
						task.setDispatcherOrgName(sysDepart.getDepartNameFull());
						task.setDispatcherOrgCode(sysDepart.getOrgCode());
						task.setTaskCode(cjxtTask.getTaskCode());
					}
					//接收部门
					task.setReceiverOrgId(map.get("DEP_ID").toString());
					task.setReceiverOrgName(map.get("DEPART_NAME").toString());
					task.setReceiverOrgCode(map.get("ORG_CODE").toString());
					//接收人
					task.setReceiverId(map.get("user_id").toString());
					task.setReceiverZh(map.get("username").toString());
					task.setReceiverName(map.get("realname").toString());
					//截止日期
					task.setDueDate(cjxtTask.getDueDate());
					//部门负责人，因为是网格员，所以插入成部门负责人，以便发送消息
					task.setReceiverBmfzrId(map.get("user_id").toString());
					task.setReceiverBmfzrZh(map.get("username").toString());
					task.setReceiverBmfzrName(map.get("realname").toString());
					//采集量设置
//					 总采集	cj_zs	总采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 已完成采集	cj_ywc	已完成采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 剩余采集	cj_sy	剩余采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 采集完成情况	cj_wcqk	采集完成情况(例如：10.33%)	0%	varchar(32)	32		FALSE	FALSE	FALSE
					cjzsl = cjzsl + cjsl;
					task.setCjZs(cjsl);
					task.setCjYwc(0);
					task.setCjSy(cjsl);
					task.setCjWcqk("0%");
					task.setRwzt("2");
					task.setHasChild("0");

					//主表接收部门汇总
					recOrgId += ("".equals(recOrgId)?"":",") + task.getReceiverOrgId();
					recOrgName += ("".equals(recOrgName)?"":",") + task.getReceiverOrgName();
					recOrgCode += ("".equals(recOrgCode)?"":",") + task.getReceiverOrgCode();
					recRyId += ("".equals(recRyId)?"":",") +  task.getReceiverId();
					recRyZh += ("".equals(recRyZh)?"":",") +  task.getReceiverZh();
					recRyName += ("".equals(recRyName)?"":",") +  task.getReceiverName();

					//cjxtTaskService.addCjxtTask(task);
					entityList.add(task);

					try {
						//发送系统消息给 网格员
						ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
						String title = sysDepart.getDepartNameFull() + sysUser.getRealname()+"派发了采集任务【" + task.getTaskName() + "】给您，请您尽快处理!";
						MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), task.getReceiverBmfzrId(), "任务派发【"+task.getTaskName()+ "】", title);
						sysBaseApi.sendSysAnnouncement(messageDTO);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(taskId==null || "".equals(taskId)){
					cjxtTask.setTaskCode(cjxtTask.getId());
					cjxtTask.setCjZs(cjzsl);
					cjxtTask.setCjYwc(0);
					cjxtTask.setCjSy(cjzsl);
					cjxtTask.setCjWcqk("0%");
					cjxtTask.setRwzt("2");
					cjxtTask.setHasChild("1");

					cjxtTask.setReceiverOrgId(truncateToLength(recOrgId,1000));
					cjxtTask.setReceiverOrgName(truncateToLength(recOrgName,1000));
					cjxtTask.setReceiverOrgCode(truncateToLength(recOrgCode,1000));
					cjxtTask.setReceiverId(truncateToLength(recRyId,1000));
					cjxtTask.setReceiverZh(truncateToLength(recRyZh,1000));
					cjxtTask.setReceiverName(truncateToLength(recRyName,1000));
					cjxtTask.setReceiverBmfzrId(truncateToLength(recRyId,1000));
					cjxtTask.setReceiverBmfzrZh(truncateToLength(recRyZh,1000));
					cjxtTask.setReceiverBmfzrName(truncateToLength(recRyName,1000));

					entityList.add(cjxtTask);
					//cjxtTaskService.updateCjxtTask(cjxtTask);
					cjxtTaskService.saveOrUpdateBatch(entityList);
				}else{
					cjxtTask.setRwzt("2");
					cjxtTask.setHasChild("1");
					entityList.add(cjxtTask);
					//cjxtTaskService.updateCjxtTask(cjxtTask);
					cjxtTaskService.saveOrUpdateBatch(entityList);
				}
				java.util.Collection<CjxtTaskDispatch> dataList = new ArrayList<>();
				int cnt = 0;
				for (CjxtTask ctask : entityList) {
					// 在这里处理每个CjxtTask对象
					dataSql = "select p.id address_id,p.address_code,p.address_name from cjxt_standard_address p where p.address_code_mz='"+ctask.getReceiverOrgCode()+"' and p.del_flag='0'";
					resultList = jdbcTemplate.queryForList(dataSql);
					if(resultList != null){
						for(int i=0;i<resultList.size();i++){
							map = resultList.get(i);
							//派发采集明细
							CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
							cjxtTaskDispatch.setTaskId(ctask.getId());
							cjxtTaskDispatch.setTaskCode(ctask.getTaskCode());
							cjxtTaskDispatch.setTaskName(ctask.getTaskName());
							cjxtTaskDispatch.setTaskDescription(ctask.getTaskDescription());
							cjxtTaskDispatch.setMbId(ctask.getMbId());
							cjxtTaskDispatch.setMbCode(ctask.getMbCode());
							cjxtTaskDispatch.setMbName(ctask.getMbName());
							cjxtTaskDispatch.setBm(ctask.getBm());
							//采集地址
							cjxtTaskDispatch.setAddressId(map.get("address_id").toString());
							cjxtTaskDispatch.setAddressCode(map.get("address_code").toString());
							cjxtTaskDispatch.setAddressName(map.get("address_name").toString());
							cjxtTaskDispatch.setDispatcherId(ctask.getDispatcherId());
							cjxtTaskDispatch.setDispatcherName(ctask.getDispatcherName());
							cjxtTaskDispatch.setReceiverId(ctask.getReceiverId());
							cjxtTaskDispatch.setReceiverName(ctask.getReceiverName());
							cjxtTaskDispatch.setDueDate(ctask.getDueDate());
							cjxtTaskDispatch.setRwzt("2");
							dataList.add(cjxtTaskDispatch);
							//cjxtTaskDispatchService.save(cjxtTaskDispatch);
						}
					}
					cnt = cnt + 1;
					if(cnt == entityList.size()-1){
						break;
					}
				}
				if(dataList.size() > 0){
					cjxtTaskDispatchService.saveOrUpdateBatch(dataList);
				}
			}else{
				return Result.OK("派发失败，请先配置采集人员！");
			}
		}else if("5".equals(sysUser.getUserSf())){//街道办工作人员

			taskId =  cjxtTask.getId();
			//任务id为空，即为新增
			if(taskId==null || "".equals(taskId)){
				cjxtTask.setRwzt("1");
				cjxtTask.setDispatcherOrgId(sysDepart.getId());
				cjxtTask.setDispatcherOrgName(sysDepart.getDepartNameFull());
				cjxtTask.setDispatcherOrgCode(sysDepart.getOrgCode());
				//部门数据权限ID
				cjxtTask.setOrgId(sysDepart.getId());
				cjxtTask.setOrgName(sysDepart.getDepartNameFull());
				cjxtTask.setOrgCode(sysDepart.getOrgCode());
				cjxtTaskService.addCjxtTask(cjxtTask);
			}

			//		{ value:'1'，1abel:'市级”}
			//		{ value:'2',1abe1:"委办局"}
			//		{ value:'3'，1abel:"公安局"}
			//		{value: '4'"，1abel:"分局"}
			//		{value: '5'，1abel:"派出所"}
			//					{ value:'6','，1abe1:'区/县级'子，
			//					{value:'7'，1abe1:"街道办"}
			//					value:"8'，1abel:·社区'},
			//					value:"9'1abel:'网格'}
			dataSql = "select d.id,d.depart_name,d.org_code,\n" +
					"(select group_concat(b.fzry_id) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_id,\n" +
					"(select group_concat(b.fzry_name) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_name,\n" +
					"(select group_concat(b.fzry_zh) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_zh,\n" +
					"(select count(1) from cjxt_standard_address a where a.address_code_mz like concat(d.org_code,'%') and a.del_flag='0') cjsl\n" +
					"from sys_depart d \n" +
					"where d.org_category='8' and d.parent_id='"+ sysDepart.getId() +"' " +
					"and exists(select 1 from cjxt_bmfzr cb where cb.bmid=d.id and cb.del_flag='0' ) " +
					"order by d.org_code ";
			resultList = jdbcTemplate.queryForList(dataSql);

			if(resultList != null && resultList.size() > 0){
				java.util.Collection<CjxtTask> entityList = new ArrayList<>();
				Map<String, Object> map = new HashMap<String, Object>();
				int cjzsl = 0;
				int cjsl = 0;
				for (int i = 0; i < resultList.size(); i++) {
					map = resultList.get(i);
					//采集数量为0，说明没有配置采集地址，不派发任务
					cjsl = Integer.parseInt(map.get("CJSL").toString());

					if (cjsl == 0) {
						continue;
					}

					CjxtTask task = new CjxtTask();
					//节点信息
					task.setPid(cjxtTask.getId());
					task.setHasChild("0");
					//任务信息
					task.setTaskName(cjxtTask.getTaskName());
					task.setTaskDescription(cjxtTask.getTaskDescription());
					//模板信息
					task.setMbId(cjxtTask.getMbId());
					task.setMbName(cjxtTask.getMbName());
					task.setMbCode(cjxtTask.getMbCode());
					task.setBm(cjxtTask.getBm());
					//部门数据权限ID
					task.setOrgId(map.get("id").toString());
					task.setOrgName(map.get("depart_name").toString());
					task.setOrgCode(map.get("org_code").toString());
					//派发部门人员信息
					if(taskId==null || "".equals(taskId)){
						task.setDispatcherId(cjxtTask.getDispatcherId());
						task.setDispatcherName(cjxtTask.getDispatcherName());
						task.setDispatcherOrgId(cjxtTask.getDispatcherOrgId());
						task.setDispatcherOrgName(cjxtTask.getDispatcherOrgName());
						task.setDispatcherOrgCode(cjxtTask.getDispatcherOrgCode());
						task.setTaskCode(cjxtTask.getId());
					}else{
						task.setDispatcherId(sysUser.getId());
						task.setDispatcherName(sysUser.getRealname());
						task.setDispatcherOrgId(sysDepart.getId());
						task.setDispatcherOrgName(sysDepart.getDepartNameFull());
						task.setDispatcherOrgCode(sysDepart.getOrgCode());
						task.setTaskCode(cjxtTask.getTaskCode());
					}
					//接收部门
					task.setReceiverOrgId(map.get("id").toString());
					task.setReceiverOrgName(map.get("depart_name").toString());
					task.setReceiverOrgCode(map.get("org_code").toString());
					//接收人
					task.setReceiverId(map.get("bmfzr_id").toString());
					task.setReceiverZh(map.get("bmfzr_zh").toString());
					task.setReceiverName(map.get("bmfzr_name").toString());
					//截止日期
					task.setDueDate(cjxtTask.getDueDate());
					//部门负责人，因为是网格员，所以插入成部门负责人，以便发送消息
					task.setReceiverBmfzrId(map.get("bmfzr_id").toString());
					task.setReceiverBmfzrZh(map.get("bmfzr_zh").toString());
					task.setReceiverBmfzrName(map.get("bmfzr_name").toString());
					//采集量设置
//					 总采集	cj_zs	总采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 已完成采集	cj_ywc	已完成采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 剩余采集	cj_sy	剩余采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 采集完成情况	cj_wcqk	采集完成情况(例如：10.33%)	0%	varchar(32)	32		FALSE	FALSE	FALSE
					cjzsl = cjzsl + cjsl;
					task.setCjZs(cjsl);
					task.setCjYwc(0);
					task.setCjSy(cjsl);
					task.setCjWcqk("0%");
					task.setRwzt("1");

					//主表接收部门汇总
					recOrgId += ("".equals(recOrgId) ? "" : ",") + task.getReceiverOrgId();
					recOrgName += ("".equals(recOrgName) ? "" : ",") + task.getReceiverOrgName();
					recOrgCode += ("".equals(recOrgCode) ? "" : ",") + task.getReceiverOrgCode();
					recRyId += ("".equals(recRyId) ? "" : ",") + task.getReceiverId();
					recRyZh += ("".equals(recRyZh)?"":",") +  task.getReceiverZh();
					recRyName += ("".equals(recRyName) ? "" : ",") + task.getReceiverName();

					//cjxtTaskService.addCjxtTask(task);
					entityList.add(task);

					try {
						if(task.getReceiverBmfzrZh()!=null){
							String[] bmfzrZh = task.getReceiverBmfzrZh().split(",");
							for(int j=0;j<bmfzrZh.length;j++){
								//发送系统消息给 网格员
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + task.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZh[j], "任务派发【" + task.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZh[j]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZh[j]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage(sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + task.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(task.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}


				//任务id为空，即为新增
				if(taskId==null || "".equals(taskId)){
					cjxtTask.setTaskCode(cjxtTask.getId());
					cjxtTask.setCjZs(cjzsl);
					cjxtTask.setCjYwc(0);
					cjxtTask.setCjSy(cjzsl);
					cjxtTask.setCjWcqk("0%");
					cjxtTask.setRwzt("2");
					cjxtTask.setHasChild("1");

					cjxtTask.setReceiverOrgId(truncateToLength(recOrgId, 1000));
					cjxtTask.setReceiverOrgName(truncateToLength(recOrgName, 1000));
					cjxtTask.setReceiverOrgCode(truncateToLength(recOrgCode, 1000));
					cjxtTask.setReceiverId(truncateToLength(recRyId, 1000));
					cjxtTask.setReceiverZh(truncateToLength(recRyZh, 1000));
					cjxtTask.setReceiverName(truncateToLength(recRyName, 1000));
					cjxtTask.setReceiverBmfzrId(truncateToLength(recRyId,1000));
					cjxtTask.setReceiverBmfzrZh(truncateToLength(recRyZh,1000));
					cjxtTask.setReceiverBmfzrName(truncateToLength(recRyName,1000));

					entityList.add(cjxtTask);
					//cjxtTaskService.updateCjxtTask(cjxtTask);
					cjxtTaskService.saveOrUpdateBatch(entityList);
				}else{
					cjxtTask = cjxtTaskService.getById(cjxtTask.getId());
					cjxtTask.setRwzt("2");
					cjxtTask.setHasChild("1");
					entityList.add(cjxtTask);
					//cjxtTaskService.updateCjxtTask(cjxtTask);
					cjxtTaskService.saveOrUpdateBatch(entityList);
				}
			}else{
				return Result.OK("派发失败，请先配置采集人员或部门负责人！");
			}
//
		}else if("6".equals(sysUser.getUserSf())){//委办局工作人员

			taskId =  cjxtTask.getId();
			//任务id为空，即为新增
			if(taskId==null || "".equals(taskId)){
				cjxtTask.setRwzt("1");
				cjxtTask.setDispatcherOrgId(sysDepart.getId());
				cjxtTask.setDispatcherOrgName(sysDepart.getDepartNameFull());
				cjxtTask.setDispatcherOrgCode(sysDepart.getOrgCode());
				//部门数据权限ID
				cjxtTask.setOrgId(sysDepart.getId());
				cjxtTask.setOrgName(sysDepart.getDepartNameFull());
				cjxtTask.setOrgCode(sysDepart.getOrgCode());
				cjxtTaskService.addCjxtTask(cjxtTask);
			}

			//		{ value:'1'，1abel:'市级”}
			//		{ value:'2',1abe1:"委办局"}
			//		{ value:'3'，1abel:"公安局"}
			//		{value: '4'"，1abel:"分局"}
			//		{value: '5'，1abel:"派出所"}
			//					{ value:'6','，1abe1:'区/县级'子，
			//					{value:'7'，1abe1:"街道办"}
			//					value:"8'，1abel:·社区'},
			//					value:"9'1abel:'网格'}
			dataSql = "select d.id,d.depart_name,d.org_code,\n" +
					"(select group_concat(b.fzry_id) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_id,\n" +
					"(select group_concat(b.fzry_name) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_name,\n" +
					"(select group_concat(b.fzry_zh) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_zh,\n" +
					"(select count(1) from cjxt_standard_address a where a.address_code_mz like concat(d.org_code,'%') and a.del_flag='0') cjsl\n" +
					"from sys_depart d \n" +
					"where d.org_category='7' " +
					"and exists(select 1 from cjxt_bmfzr cb where cb.bmid=d.id and cb.del_flag='0' ) " +
					"order by d.org_code ";
			resultList = jdbcTemplate.queryForList(dataSql);

			if(resultList != null && resultList.size() > 0){
				java.util.Collection<CjxtTask> entityList = new ArrayList<>();
				Map<String, Object> map = new HashMap<String, Object>();
				int cjzsl = 0;
				int cjsl = 0;
				for (int i = 0; i < resultList.size(); i++) {
					map = resultList.get(i);
					//采集数量为0，说明没有配置采集地址，不派发任务
					cjsl = Integer.parseInt(map.get("CJSL").toString());
					if (cjsl == 0) {
						continue;
					}

					CjxtTask task = new CjxtTask();
					//节点信息
					task.setPid(cjxtTask.getId());
					task.setHasChild("0");
					//任务信息
					task.setTaskName(cjxtTask.getTaskName());
					task.setTaskDescription(cjxtTask.getTaskDescription());
					//模板信息
					task.setMbId(cjxtTask.getMbId());
					task.setMbName(cjxtTask.getMbName());
					task.setMbCode(cjxtTask.getMbCode());
					task.setBm(cjxtTask.getBm());
					//部门数据权限ID
					task.setOrgId(map.get("id").toString());
					task.setOrgName(map.get("depart_name").toString());
					task.setOrgCode(map.get("org_code").toString());
					//派发部门人员信息
					if(taskId==null || "".equals(taskId)){
						task.setDispatcherId(cjxtTask.getDispatcherId());
						task.setDispatcherName(cjxtTask.getDispatcherName());
						task.setDispatcherOrgId(cjxtTask.getDispatcherOrgId());
						task.setDispatcherOrgName(cjxtTask.getDispatcherOrgName());
						task.setDispatcherOrgCode(cjxtTask.getDispatcherOrgCode());
						task.setTaskCode(cjxtTask.getId());
					}else{
						task.setDispatcherId(sysUser.getId());
						task.setDispatcherName(sysUser.getRealname());
						task.setDispatcherOrgId(sysDepart.getId());
						task.setDispatcherOrgName(sysDepart.getDepartNameFull());
						task.setDispatcherOrgCode(sysDepart.getOrgCode());
						task.setTaskCode(cjxtTask.getTaskCode());
					}
					//接收部门
					task.setReceiverOrgId(map.get("id").toString());
					task.setReceiverOrgName(map.get("depart_name").toString());
					task.setReceiverOrgCode(map.get("org_code").toString());
					//接收人
					task.setReceiverId(map.get("bmfzr_id").toString());
					task.setReceiverZh(map.get("bmfzr_zh").toString());
					task.setReceiverName(map.get("bmfzr_name").toString());
					//截止日期
					task.setDueDate(cjxtTask.getDueDate());
					//部门负责人，因为是网格员，所以插入成部门负责人，以便发送消息
					task.setReceiverBmfzrId(map.get("bmfzr_id").toString());
					task.setReceiverBmfzrZh(map.get("bmfzr_zh").toString());
					task.setReceiverBmfzrName(map.get("bmfzr_name").toString());
					//采集量设置
//					 总采集	cj_zs	总采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 已完成采集	cj_ywc	已完成采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 剩余采集	cj_sy	剩余采集	0	int(11)	11		FALSE	FALSE	FALSE
//					 采集完成情况	cj_wcqk	采集完成情况(例如：10.33%)	0%	varchar(32)	32		FALSE	FALSE	FALSE
					cjzsl = cjzsl + cjsl;
					task.setCjZs(cjsl);
					task.setCjYwc(0);
					task.setCjSy(cjsl);
					task.setCjWcqk("0%");
					task.setRwzt("1");

					//主表接收部门汇总
					recOrgId += ("".equals(recOrgId) ? "" : ",") + task.getReceiverOrgId();
					recOrgName += ("".equals(recOrgName) ? "" : ",") + task.getReceiverOrgName();
					recOrgCode += ("".equals(recOrgCode) ? "" : ",") + task.getReceiverOrgCode();
					recRyId += ("".equals(recRyId) ? "" : ",") + task.getReceiverId();
					recRyZh += ("".equals(recRyZh)?"":",") +  task.getReceiverZh();
					recRyName += ("".equals(recRyName) ? "" : ",") + task.getReceiverName();

					//cjxtTaskService.addCjxtTask(task);
					entityList.add(task);

					try {
						if(task.getReceiverBmfzrZh()!=null){
							String[] bmfzrZh = task.getReceiverBmfzrZh().split(",");
							for(int j=0;j<bmfzrZh.length;j++){
								//发送系统消息给 网格员
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + task.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZh[j], "任务派发【" + task.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZh[j]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZh[j]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage(sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + task.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(task.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}


				//任务id为空，即为新增
				if(taskId==null || "".equals(taskId)){
					cjxtTask.setTaskCode(cjxtTask.getId());
					cjxtTask.setCjZs(cjzsl);
					cjxtTask.setCjYwc(0);
					cjxtTask.setCjSy(cjzsl);
					cjxtTask.setCjWcqk("0%");
					cjxtTask.setRwzt("2");
					cjxtTask.setHasChild("1");

					cjxtTask.setReceiverOrgId(truncateToLength(recOrgId, 1000));
					cjxtTask.setReceiverOrgName(truncateToLength(recOrgName, 1000));
					cjxtTask.setReceiverOrgCode(truncateToLength(recOrgCode, 1000));
					cjxtTask.setReceiverId(truncateToLength(recRyId, 1000));
					cjxtTask.setReceiverZh(truncateToLength(recRyZh, 1000));
					cjxtTask.setReceiverName(truncateToLength(recRyName, 1000));
					cjxtTask.setReceiverBmfzrId(truncateToLength(recRyId,1000));
					cjxtTask.setReceiverBmfzrZh(truncateToLength(recRyZh,1000));
					cjxtTask.setReceiverBmfzrName(truncateToLength(recRyName,1000));

					entityList.add(cjxtTask);
					//cjxtTaskService.updateCjxtTask(cjxtTask);
					cjxtTaskService.saveOrUpdateBatch(entityList);
				}else{
					cjxtTask = cjxtTaskService.getById(cjxtTask.getId());
					cjxtTask.setRwzt("2");
					cjxtTask.setHasChild("1");
					entityList.add(cjxtTask);
					//cjxtTaskService.updateCjxtTask(cjxtTask);
					cjxtTaskService.saveOrUpdateBatch(entityList);
				}
			}else{
				return Result.OK("派发失败，请先配置采集人员或部门负责人！");
			}

		}else if("7".equals(sysUser.getUserSf())){//派出所工作人员

		}else if("8".equals(sysUser.getUserSf())){//分局工作人员

		}else if("9".equals(sysUser.getUserSf())){//市局工作人员

		}
		return Result.OK("派发成功！");
	}

	/**
	 * 任务是否存在
	 */
	@AutoLog(value = "任务派发-任务是否存在")
	@ApiOperation(value="任务派发-任务是否存在", notes="任务派发-任务是否存在")
	@GetMapping(value = "/doesTask")
	public Result<Boolean> doesTask(@RequestBody CjxtTask cjxtTask) {
		Boolean isDoesTask = false;
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//接收部门信息
		String[] receiverOrgIds = cjxtTask.getReceiverOrgName().split(",");
		if(cjxtTask.getReceiverId()!=null && !"".equals(cjxtTask.getReceiverId()) && cjxtTask.getReceiverOrgName()!=null && cjxtTask.getAddressId()==null){
			for(int i = 0;i<receiverOrgIds.length;i++) {
				//接收部门信息
				String receiverOrgId = receiverOrgIds[i];
				SysDepart receiverName = sysDepartService.getById(receiverOrgId);
				List<SysDepart> departList = sysDepartService.list(new QueryWrapper<SysDepart>().eq("parent_id", receiverName.getId()));
				if(departList!=null){
					for(SysDepart sysDepart: departList){
						List<SysUserDepart> sysUserDepartList = sysUserDepartService.list(new LambdaQueryWrapper<SysUserDepart>().eq(SysUserDepart::getDepId,sysDepart.getId()));
						if(sysUserDepartList!=null){
							for(SysUserDepart sysUerDepart: sysUserDepartList){
								SysUser user = sysUserService.getById(sysUerDepart.getUserId());
								if(user!=null){
									List<CjxtStandardAddressPerson> personList = cjxtStandardAddressPersonService.list(new LambdaQueryWrapper<CjxtStandardAddressPerson>().eq(CjxtStandardAddressPerson::getUserId,user.getId()));
									if(personList!=null){
										for(CjxtStandardAddressPerson cjxtStandardAddressPerson: personList){
											CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,cjxtTask.getMbId())
													.eq(CjxtTaskDispatch::getAddressId,cjxtStandardAddressPerson.getAddressId())
													.eq(CjxtTaskDispatch::getReceiverId,user.getId()).last("LIMIT 1"));
											if(dispatch!=null){
												isDoesTask = true;
												break;
											}
										}
									}else {
										List<CjxtStandardAddress> cjxtStandardAddressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressIdMz,sysDepart.getId()));
										if(cjxtStandardAddressList!=null){
											for(CjxtStandardAddress address: cjxtStandardAddressList){
												CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,cjxtTask.getMbId())
														.eq(CjxtTaskDispatch::getAddressId,address.getAddressIdMz())
														.eq(CjxtTaskDispatch::getReceiverId,user.getId()).last("LIMIT 1"));
												if(dispatch!=null){
													isDoesTask = true;
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if((cjxtTask.getReceiverId()==null || "".equals(cjxtTask.getReceiverId())) && ((cjxtTask.getAddressId()==null || "".equals(cjxtTask.getAddressId())) && cjxtTask.getReceiverOrgName()!=null)){
			//派发部门信息
			SysDepart dispatDepart = sysDepartService.getById(cjxtTask.getDispatcherId());
			if(dispatDepart!=null && "8".equals(dispatDepart.getOrgCategory())){
				for(int i = 0;i<receiverOrgIds.length;i++) {
					//接收部门信息
					String receiverOrgId = receiverOrgIds[i];
					SysDepart receiverName = sysDepartService.getById(receiverOrgId);
					List<SysDepart> departList = sysDepartService.list(new QueryWrapper<SysDepart>().eq("parent_id", receiverName.getId()));
					if(departList!=null){
						for(SysDepart sysDepart: departList){
							List<SysUserDepart> sysUserDepartList = sysUserDepartService.list(new LambdaQueryWrapper<SysUserDepart>().eq(SysUserDepart::getDepId,sysDepart.getId()));
							if(sysUserDepartList!=null){
								for(SysUserDepart sysUerDepart: sysUserDepartList){
									SysUser user = sysUserService.getById(sysUerDepart.getUserId());
									if(user!=null){
										List<CjxtStandardAddressPerson> personList = cjxtStandardAddressPersonService.list(new LambdaQueryWrapper<CjxtStandardAddressPerson>().eq(CjxtStandardAddressPerson::getUserId,user.getId()));
										if(personList!=null){
											for(CjxtStandardAddressPerson cjxtStandardAddressPerson: personList){
												CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,cjxtTask.getMbId())
														.eq(CjxtTaskDispatch::getAddressId,cjxtStandardAddressPerson.getAddressId())
														.eq(CjxtTaskDispatch::getReceiverId,user.getId()).last("LIMIT 1"));
												if(dispatch!=null){
													isDoesTask = true;
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if(cjxtTask.getReceiverId()!=null && !"".equals(cjxtTask.getReceiverId()) && cjxtTask.getAddressId()!=null && !"".equals(cjxtTask.getAddressId())){
			//接收人员和接收地址不为空
			String[] addressIds = cjxtTask.getAddressId().split(",");
			for (int j = 0; j < addressIds.length; j++) {
				String addressId = addressIds[j];
				CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbId,cjxtTask.getMbId())
						.eq(CjxtTaskDispatch::getAddressId,addressId)
						.eq(CjxtTaskDispatch::getReceiverId,cjxtTask.getReceiverId()).last("LIMIT 1"));
				if(dispatch!=null){
					isDoesTask = true;
					break;
				}
			}
		}
		return Result.OK(isDoesTask);
	}

	/**
	 * 一键派发列表数据
	 * @return
	 */
	@ApiOperation(value="任务派发-一键派发列表数据", notes="任务派发-一键派发列表数据")
	@GetMapping(value = "/oneClickData")
	public Result<Map<String, Object>> oneClickData(
			@RequestParam Map<String, String> params,
			@RequestParam(required = false, name="orgCode") String orgCode,
			@RequestParam(required = false, name="isCommunity") String isCommunity,
			@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
			@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
			HttpServletRequest req) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> result = new HashMap<>();
		if(orgCode != null){
			String dataSql = null;
			List<Map<String, Object>> resultList = new ArrayList<>();
			//获取部门信息
			SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
//			 网格员	1
//			 民警	2
//			 辅警	3
//			 社区工作人员	4
//			 街办工作人员	5
//			 委办局工作人员	6
//			 派出所工作人员	7
//			 分局工作人员	8
//			 市局工作人员	9
			//社区工作人员	4
			if("4".equals(sysUser.getUserSf())){
				dataSql = "select concat('"+ sysDepart.getDepartNameFull() +"_',s.depart_name) depart_name,d.dep_id,u.realname,d.user_id,(select count(1) from cjxt_standard_address p where p.address_code_mz=s.org_code and  p.del_flag='0' ) cjsl\n" +
						"from sys_depart s,sys_user_depart d,sys_user u \n" +
						"where s.id=d.dep_id and d.user_id=u.id and s.org_code like '" + orgCode + "%' and s.org_code !='" + orgCode + "'\n" +
						"order by s.org_code ";
				resultList = jdbcTemplate.queryForList(dataSql);
				result.put("records", resultList);
			}else if("5".equals(sysUser.getUserSf())){//街道办工作人员
				//		{ value:'1'，1abel:'市级”}
				//		{ value:'2',1abe1:"委办局"}
				//		{ value:'3'，1abel:"公安局"}
				//		{value: '4'"，1abel:"分局"}
				//		{value: '5'，1abel:"派出所"}
				//					{ value:'6','，1abe1:'区/县级'子，
				//					{value:'7'，1abe1:"街道办"}
				//					value:"8'，1abel:·社区'},
				//					value:"9'1abel:'网格'}
				dataSql = "select d.id,d.depart_name,\n" +
						"(select group_concat(b.fzry_name) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr,\n" +
						"(select group_concat(b.fzry_zh) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_lxdh,\n" +
						"(select count(1) from cjxt_standard_address a where a.address_code_mz like concat(d.org_code,'%') and a.del_flag='0') cjsl\n" +
						"from sys_depart d \n" +
						"where d.org_category='8' and d.parent_id='"+ sysDepart.getId() +"' order by d.org_code ";
				resultList = jdbcTemplate.queryForList(dataSql);
				result.put("records", resultList);
			}else if("6".equals(sysUser.getUserSf())){//委办局工作人员
				//		{ value:'1'，1abel:'市级”}
				//		{ value:'2',1abe1:"委办局"}
				//		{ value:'3'，1abel:"公安局"}
				//		{value: '4'"，1abel:"分局"}
				//		{value: '5'，1abel:"派出所"}
				//					{ value:'6','，1abe1:'区/县级'子，
				//					{value:'7'，1abe1:"街道办"}
				//					value:"8'，1abel:·社区'},
				//					value:"9'1abel:'网格'}
				dataSql = "select d.id,d.depart_name,\n" +
						"(select group_concat(b.fzry_name) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr,\n" +
						"(select group_concat(b.fzry_zh) from cjxt_bmfzr b where b.bmid=d.id and b.del_flag='0') bmfzr_lxdh,\n" +
						"(select count(1) from cjxt_standard_address a where a.address_code_mz like concat(d.org_code,'%') and a.del_flag='0') cjsl\n" +
						"from sys_depart d \n" +
						"where d.org_category='7' order by d.org_code ";
				resultList = jdbcTemplate.queryForList(dataSql);
				result.put("records", resultList);
			}else if("7".equals(sysUser.getUserSf())){//派出所工作人员
				result.put("records", resultList);
			}else if("8".equals(sysUser.getUserSf())){//分局工作人员
				result.put("records", resultList);
			}else if("9".equals(sysUser.getUserSf())){//市局工作人员
				result.put("records", resultList);
			}
		}
		return Result.OK(result);
	}

	/**
	 *  撤回
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务派发-撤回")
	@ApiOperation(value="任务派发-撤回", notes="任务派发-撤回")
	@PostMapping(value = "/withdraw")
	public Result<String> withdraw(@RequestParam(name="id",required=true) String id) {
		CjxtTask cjxtTask = new CjxtTask();
		cjxtTask.setId(id);
		cjxtTask.setRwzt("1");
		cjxtTask.setHasChild("0");
		cjxtTask.setReceiverOrgId("");
		cjxtTask.setReceiverOrgName("");
		cjxtTask.setReceiverOrgCode("");
		cjxtTask.setReceiverId("");
		cjxtTask.setReceiverZh("");
		cjxtTask.setReceiverName("");
		cjxtTask.setReceiverBmfzrId("");
		cjxtTask.setReceiverBmfzrZh("");
		cjxtTask.setReceiverBmfzrName("");
		cjxtTask.setCjZs(0);
		cjxtTask.setCjYwc(0);
		cjxtTask.setCjSy(0);
		cjxtTask.setCjWcqk("");
		cjxtTask.setChzt("2");
		cjxtTaskService.updateById(cjxtTask);
		updateDelFlag(id);
		return Result.OK("撤回成功!");
	}

	public void updateDelFlag(String id) {
		List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getPid, id));
		for (CjxtTask task : taskList) {
			CjxtWarningMessage cjxtWarningMessage = cjxtWarningMessageService.getOne(new LambdaQueryWrapper<CjxtWarningMessage>().eq(CjxtWarningMessage::getBm,task.getBm())
					.eq(CjxtWarningMessage::getCreateBy,task.getCreateBy())
					.eq(CjxtWarningMessage::getStatus,"1")
					.eq(CjxtWarningMessage::getMsgType,"1")
					.orderByDesc(CjxtWarningMessage::getCreateTime)
					.last("LIMIT 1"));
			if(cjxtWarningMessage!=null){
				if(cjxtWarningMessage.getUserId()!=null){
					JSONObject json = new JSONObject();
					json.put("msgType", "waMsg");
					String msg = json.toString();
					webSocket.sendOneMessage(cjxtWarningMessage.getUserId(), msg);
				}
				cjxtWarningMessageService.removeById(cjxtWarningMessage.getId());
			}
			cjxtTaskService.removeById(task.getId());
			//cjxtTaskService.removeById(task.getId()).deleteCjxtTask(task.getId());
			jdbcTemplate.update("update cjxt_task_dispatch set del_flag='1' where task_code='" + task.getTaskCode() + "'");
			updateDelFlag(task.getId()); // 递归调用
		}
	}

	/**
	 *  催单
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务派发-催单")
	@ApiOperation(value="任务派发-催单", notes="任务派发-催单")
//    @RequiresPermissions("cjxt:cjxt_task:edit")
	@GetMapping(value = "/reminder")
	public Result<String> reminder(@RequestParam(name="id",required=true) String id) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		CjxtTask cjxtTask = cjxtTaskService.getById(id);
		SysDepart disDepart = sysDepartService.getById(cjxtTask.getDispatcherOrgId());
		//接收人消息发送状态
		boolean receiverXx = false;
//		 SysUser sysUser = null;
//		 if(cjxtTask.getReceiverBmfzrId()!=null && cjxtTask.getReceiverId()!=null && cjxtTask.getReceiverBmfzrId().equals(cjxtTask.getReceiverId())){
//			 sysUser = sysUserService.getById(cjxtTask.getReceiverBmfzrId());
//		 }
//		 if(cjxtTask.getReceiverBmfzrId()!=null && cjxtTask.getReceiverId()!=null && !cjxtTask.getReceiverBmfzrId().equals(cjxtTask.getReceiverId())) {
//			 SysUser bmfzr = sysUserService.getById(cjxtTask.getReceiverBmfzrId());
//			 sysUser = sysUserService.getById(cjxtTask.getReceiverId());
//		 }
		try {
			if(cjxtTask.getReceiverBmfzrZh()!=null && !"".equals(cjxtTask.getReceiverBmfzrZh()) && cjxtTask.getReceiverZh()!=null && !"".equals(cjxtTask.getReceiverZh()) && cjxtTask.getReceiverBmfzrZh().equals(cjxtTask.getReceiverZh())){
				String[] bmfzrZhs = cjxtTask.getReceiverBmfzrZh().split(",");
				for(int k=0;k<bmfzrZhs.length;k++){
					//发送系统消息给 部门负责人
					ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
					String title = disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您注意【" + cjxtTask.getTaskName() + "】任务处理进度，请您提醒网格员尽快处理!";
					MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务催单【" + cjxtTask.getTaskName() + "】", title);
					sysBaseApi.sendSysAnnouncement(messageDTO);

					//发送提醒消息
					CjxtWarningMessage messageSec = new CjxtWarningMessage();
					SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
					if(sysUserSec!=null){
						messageSec.setUserId(sysUserSec.getId());
						messageSec.setUsername(bmfzrZhs[k]);
						messageSec.setRealname(sysUserSec.getRealname());
						messageSec.setMessage(disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您注意【" + cjxtTask.getTaskName() + "】任务处理进度，请您提醒网格员尽快处理!");
						messageSec.setStatus("1");
						//messageSec.setDataId(""); //消息提醒不发数据ID
						messageSec.setBm(cjxtTask.getBm());
						messageSec.setMsgType("1");//提醒消息
						cjxtWarningMessageService.save(messageSec);

						//WebSocket消息推送
						if(sysUserSec.getId()!=null){
							JSONObject json = new JSONObject();
							json.put("msgType", "waMsg");
							String msgNew = json.toString();
							webSocket.sendOneMessage(sysUserSec.getId(), msgNew);
						}
						receiverXx = true;
					}
				}
			}
			if(cjxtTask.getReceiverBmfzrZh()!=null && !"".equals(cjxtTask.getReceiverBmfzrZh()) && cjxtTask.getReceiverZh()!=null && !"".equals(cjxtTask.getReceiverZh()) && !cjxtTask.getReceiverBmfzrZh().equals(cjxtTask.getReceiverZh())){
				String[] bmfzrZhs = cjxtTask.getReceiverBmfzrZh().split(",");
				for(int k=0;k<bmfzrZhs.length;k++){
					//发送系统消息给 部门负责人
					ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
					String title = disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您注意【" + cjxtTask.getTaskName() + "】任务处理进度，请您提醒网格员尽快处理!";
					MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务催单【" + cjxtTask.getTaskName() + "】", title);
					sysBaseApi.sendSysAnnouncement(messageDTO);

					//发送提醒消息
					CjxtWarningMessage messageSec = new CjxtWarningMessage();
					SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
					if(sysUserSec!=null){
						messageSec.setUserId(sysUserSec.getId());
						messageSec.setUsername(bmfzrZhs[k]);
						messageSec.setRealname(sysUserSec.getRealname());
						messageSec.setMessage(disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您注意【" + cjxtTask.getTaskName() + "】任务处理进度，请您提醒网格员尽快处理!");
						messageSec.setStatus("1");
						//messageSec.setDataId(""); //消息提醒不发数据ID
						messageSec.setBm(cjxtTask.getBm());
						messageSec.setMsgType("1");//提醒消息
						cjxtWarningMessageService.save(messageSec);

						//WebSocket消息推送
						if(sysUserSec.getId()!=null){
							JSONObject json = new JSONObject();
							json.put("msgType", "waMsg");
							String msgNew = json.toString();
							webSocket.sendOneMessage(sysUserSec.getId(), msgNew);
						}
					}
				}

				String[] receiverZhs = cjxtTask.getReceiverZh().split(",");
				for(int k=0;k<receiverZhs.length;k++){
					//发送系统消息给 接收人
					ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
					String title = disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您采集任务【" + cjxtTask.getTaskName() + "】处理进度，请您尽快处理!";
					MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务催单【" + cjxtTask.getTaskName() + "】", title);
					sysBaseApi.sendSysAnnouncement(messageDTO);

					//发送提醒消息
					CjxtWarningMessage messageSec = new CjxtWarningMessage();
					SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[k]);
					if(sysUserSec!=null){
						messageSec.setUserId(sysUserSec.getId());
						messageSec.setUsername(receiverZhs[k]);
						messageSec.setRealname(sysUserSec.getRealname());
						messageSec.setMessage(disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您采集任务【" + cjxtTask.getTaskName() + "】处理进度，请您尽快处理!");
						messageSec.setStatus("1");
						//messageSec.setDataId(""); //消息提醒不发数据ID
						messageSec.setBm(cjxtTask.getBm());
						messageSec.setMsgType("1");//提醒消息
						cjxtWarningMessageService.save(messageSec);

						//WebSocket消息推送
						if(sysUserSec.getId()!=null){
							JSONObject json = new JSONObject();
							json.put("msgType", "waMsg");
							String msgNew = json.toString();
							webSocket.sendOneMessage(sysUserSec.getId(), msgNew);
						}
						receiverXx = true;
					}
				}
			}
			if(cjxtTask.getReceiverZh()!=null && !"".equals(cjxtTask.getReceiverZh()) && receiverXx==false){
				String[] receiverZhs = cjxtTask.getReceiverZh().split(",");
				for(int k=0;k<receiverZhs.length;k++){
					//发送系统消息给 接收人
					ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
					String title = disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您采集任务【" + cjxtTask.getTaskName() + "】处理进度，请您尽快处理!";
					MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务催单【" + cjxtTask.getTaskName() + "】", title);
					sysBaseApi.sendSysAnnouncement(messageDTO);

					//发送提醒消息
					CjxtWarningMessage messageSec = new CjxtWarningMessage();
					SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[k]);
					if(sysUserSec!=null){
						messageSec.setUserId(sysUserSec.getId());
						messageSec.setUsername(receiverZhs[k]);
						messageSec.setRealname(sysUserSec.getRealname());
						messageSec.setMessage(disDepart.getDepartNameFull() + sysUser.getRealname() + "提醒您采集任务【" + cjxtTask.getTaskName() + "】处理进度，请您尽快处理!");
						messageSec.setStatus("1");
						//messageSec.setDataId(""); //消息提醒不发数据ID
						messageSec.setBm(cjxtTask.getBm());
						messageSec.setMsgType("1");//提醒消息
						cjxtWarningMessageService.save(messageSec);

						//WebSocket消息推送
						if(sysUserSec.getId()!=null){
							JSONObject json = new JSONObject();
							json.put("msgType", "waMsg");
							String msgNew = json.toString();
							webSocket.sendOneMessage(sysUserSec.getId(), msgNew);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result.OK("催单成功!");
	}

	/**
	 *   APP任务派发
	 *
	 * @param cjxtTask
	 * @return
	 */
	@AutoLog(value = "任务派发-APP任务派发")
	@ApiOperation(value="任务派发-APP任务派发", notes="任务派发-APP任务派发")
//    @RequiresPermissions("cjxt:cjxt_task:add")
	@PostMapping(value = "/addApp")
	public Result<String> addApp(@RequestBody CjxtTask cjxtTask) {
//		if((cjxtTask.getPid()==null || "0".equals(cjxtTask.getPid()) || "".equals(cjxtTask.getPid())) && "1".equals(cjxtTask.getChzt())){
//			List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTask.getTaskName()).eq(CjxtTask::getPid,"0"));
//			if(taskList.size()>0){
//				return Result.error("当前任务名称已存在,请修改任务名称!!!");
//			}
//		}
//		if(cjxtTask.getTaskNameDto()!=null){
//			if(!cjxtTask.getTaskNameDto().equals(cjxtTask.getTaskName())){
//				List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTask.getTaskName()).eq(CjxtTask::getPid,"0"));
//				if(taskList.size()>0){
//					return Result.error("当前任务名称已存在,请修改任务名称!!!");
//				}
//			}
//		}
		int addressIdSize = 0;
		List<CjxtStandardAddress> addressList = new ArrayList<>();
		//地址区划网格员
		SysUser addressQhUser = null;
		if(cjxtTask.getAddressQhId() != null && !"".equals(cjxtTask.getAddressQhId())){
			addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressIdMz,cjxtTask.getAddressQhId()));
			if(addressList.size()>0){
				addressIdSize = addressList.size();
			}else{
				return Result.error("当前区划没有具体地址,任务无法派发!!!");
			}
			List<SysUser> list = sysUserService.list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserSf,"1").inSql(SysUser::getOrgCode,"SELECT org_code FROM sys_depart WHERE id = '"+cjxtTask.getAddressQhId()+"'"));
//			List<SysUserDepart> list = sysUserDepartService.list(new LambdaQueryWrapper<SysUserDepart>().eq(SysUserDepart::getDepId,cjxtTask.getAddressQhId()));
			if(list.size()>0){
				addressQhUser = list.get(0);
//				if(addressQhUser==null){
//					return Result.error("当前区划地址没有网格员,任务无法派发!!!");
//				}
			}else {
				return Result.error("当前区划地址没有网格员,任务无法派发!!!");
			}
		}
		if((cjxtTask.getAddressQhId() == null || "".equals(cjxtTask.getAddressQhId())) && (cjxtTask.getReceiverId()==null || "".equals(cjxtTask.getReceiverId()))){
			return Result.error("当前任务没有选择接收人员,无法派发!!!");
		}
		String recOrgId = "";
		String recOrgName = "";
		String recOrgCode = "";
		String recUserId = "";
		String recUserZh = "";
		String recUserName = "";
		String rwzt = "";
		String uuid = "";
		boolean isFirst = true;
		//当前派发用户信息
		SysUser sysUser = sysUserService.getById(cjxtTask.getDispatcherId());
		//接收人信息
		SysUser receiverUser = null;
		if(cjxtTask.getAddressId()!=null && !"".equals(cjxtTask.getAddressId())  && cjxtTask.getReceiverId()!=null){
			receiverUser = sysUserService.getUserByName(cjxtTask.getReceiverZh());
		}
		if(cjxtTask.getAddressQhId()!=null && !"".equals(cjxtTask.getAddressQhId())){
			if(cjxtTask.getReceiverId() == null || "".equals(cjxtTask.getReceiverId())){
				receiverUser = addressQhUser;
			}else {
				receiverUser = sysUserService.getUserByName(cjxtTask.getReceiverZh());
			}
		}

		CjxtTask addresstask = new CjxtTask();

		if(cjxtTask.getAddressId()!=null && !"".equals(cjxtTask.getAddressId())){
			String[] addressId = cjxtTask.getAddressId().split(",");
			addressIdSize = addressId.length;
		}

		//当前接收部门信息
		SysDepart receiverDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtTask.getReceiverOrgCode()).last("LIMIT 1"));
		//部门Code
		String orgCode = "";

		int cjzs = 0;

		//接收部门负责人信息 接收人
		List<CjxtBmfzr> bmfzrList = new ArrayList<>();
		bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverDepart.getId()));
		if(bmfzrList.size()>0){
			for(CjxtBmfzr cjxtBmfzr:bmfzrList){
				if(!"".equals(recUserId)){
					recUserId+=cjxtBmfzr.getFzryId()+",";
					recUserZh+=cjxtBmfzr.getLxdh()+",";
					recUserName+=cjxtBmfzr.getFzryName()+",";
				}
			}
		}else {
			int orgCategOry = Integer.valueOf(receiverDepart.getOrgCategory())-1;
			orgCode = receiverDepart.getOrgCode();
			if(orgCategOry==9){
				int lastIndex = orgCode.lastIndexOf('A');
				if (lastIndex != -1) {
					orgCode = orgCode.substring(0, lastIndex);
				}
				SysDepart recDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,orgCode).last("LIMIT 1"));
				bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,recDepart.getId()));
				if(bmfzrList.size()>0){
					for(CjxtBmfzr cjxtBmfzr:bmfzrList){
						if(!"".equals(recUserId)){
							recUserId+=cjxtBmfzr.getFzryId()+",";
							recUserZh+=cjxtBmfzr.getLxdh()+",";
							recUserName+=cjxtBmfzr.getFzryName()+",";
						}
					}
				}
			}
			if(orgCategOry==8){
				int lastIndex = orgCode.lastIndexOf('A');
				if (lastIndex != -1) {
					orgCode = orgCode.substring(0, lastIndex);
				}
				SysDepart recDep = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,orgCode).last("LIMIT 1"));
				bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,recDep.getId()));
				if(bmfzrList.size()>0){
					for(CjxtBmfzr cjxtBmfzr:bmfzrList){
						if(!"".equals(recUserId)){
							recUserId+=cjxtBmfzr.getFzryId()+",";
							recUserZh+=cjxtBmfzr.getLxdh()+",";
							recUserName+=cjxtBmfzr.getFzryName()+",";
						}
					}
				}
			}
		}

		uuid = UUID.randomUUID().toString().replace("-","");
		if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
			addresstask.setId(uuid);
			addresstask.setTaskCode(uuid);
		}else {
			addresstask.setId(uuid);
			addresstask.setTaskCode(cjxtTask.getId());
		}
		//节点信息
		addresstask.setPid(cjxtTask.getPid());
		addresstask.setHasChild(cjxtTask.getHasChild());
		//任务信息
		addresstask.setTaskName(cjxtTask.getTaskName());
		addresstask.setTaskDescription(cjxtTask.getTaskDescription());
		//模板信息
		addresstask.setMbId(cjxtTask.getMbId());
		addresstask.setMbName(cjxtTask.getMbName());
		addresstask.setMbCode(cjxtTask.getMbCode());
		addresstask.setBm(cjxtTask.getBm());
		//部门数据权限==派发部门
		addresstask.setOrgId(cjxtTask.getOrgId());
		addresstask.setOrgCode(cjxtTask.getOrgCode());
		addresstask.setOrgName(cjxtTask.getOrgName());
		//当前派发部门
		addresstask.setDispatcherOrgId(cjxtTask.getOrgId());
		addresstask.setDispatcherOrgCode(cjxtTask.getOrgCode());
		addresstask.setDispatcherOrgName(cjxtTask.getDispatcherOrgName());
		//当前派发人
		addresstask.setDispatcherId(cjxtTask.getDispatcherId());
		addresstask.setDispatcherName(cjxtTask.getDispatcherName());
		//接收部门、部门负责人信息
		if (recOrgId != null && recOrgId.length() > 0 && recOrgId.charAt(recOrgId.length() - 1) == ',') {
			recOrgId = recOrgId.substring(0, recOrgId.length() - 1);
		}
		if (recOrgName != null && recOrgName.length() > 0 && recOrgName.charAt(recOrgName.length() - 1) == ',') {
			recOrgName = recOrgName.substring(0, recOrgName.length() - 1);
		}
		if (recOrgCode != null && recOrgCode.length() > 0 && recOrgCode.charAt(recOrgCode.length() - 1) == ',') {
			recOrgCode = recOrgCode.substring(0, recOrgCode.length() - 1);
		}
		addresstask.setReceiverOrgId(receiverDepart.getId());
		addresstask.setReceiverOrgCode(receiverDepart.getOrgCode());
		addresstask.setReceiverOrgName(receiverDepart.getDepartNameFull());

		if (recUserId != null && recUserId.length() > 0 && recUserId.charAt(recUserId.length() - 1) == ',') {
			recUserId = recUserId.substring(0, recUserId.length() - 1);
		}
		if (recUserZh != null && recUserZh.length() > 0 && recUserZh.charAt(recUserZh.length() - 1) == ',') {
			recUserZh = recUserZh.substring(0, recUserZh.length() - 1);
		}
		if (recUserName != null && recUserName.length() > 0 && recUserName.charAt(recUserName.length() - 1) == ',') {
			recUserName = recUserName.substring(0, recUserName.length() - 1);
		}
		addresstask.setReceiverBmfzrId(recUserId);
		addresstask.setReceiverBmfzrZh(recUserZh);
		addresstask.setReceiverBmfzrName(recUserName);
		//接收人信息：：：接收人为空存入部门负责人信息
		if(cjxtTask.getAddressId()!=null && !"".equals(cjxtTask.getAddressId()) && cjxtTask.getReceiverId()!=null){
			addresstask.setReceiverId(receiverUser.getId());
			addresstask.setReceiverZh(receiverUser.getUsername());
			addresstask.setReceiverName(receiverUser.getRealname());
		}
		if(cjxtTask.getAddressQhId()!=null && !"".equals(cjxtTask.getAddressQhId())){
			if(cjxtTask.getReceiverId() == null || "".equals(cjxtTask.getReceiverId())){
				if(addressQhUser!=null){
					addresstask.setReceiverId(addressQhUser.getId());
					addresstask.setReceiverZh(addressQhUser.getUsername());
					addresstask.setReceiverName(addressQhUser.getRealname());
				}else {
					return Result.error("当前区划地址没有网格员,任务无法派发!!!");
				}
			}else {
				addresstask.setReceiverId(receiverUser.getId());
				addresstask.setReceiverZh(receiverUser.getUsername());
				addresstask.setReceiverName(receiverUser.getRealname());
			}
		}
		addresstask.setDueDate(cjxtTask.getDueDate());
		//采集情况
		addresstask.setCjZs(addressIdSize);
		addresstask.setCjYwc(0);
		addresstask.setCjSy(addressIdSize);
		addresstask.setCjWcqk("0%");
		addresstask.setRwzt("2");
		addresstask.setChzt("1");
		if(addresstask.getPid()==null){
			cjxtTaskService.addCjxtTask(addresstask);
		}

		//创建接收部门任务
		uuid = UUID.randomUUID().toString().replace("-","");
		String bmfzrId= "";
		String bmfzrZh= "";
		String bmfzrName= "";
		int cjzsChild = 0;
		//接收部门详细信息
		String receiverOrgId = addresstask.getReceiverOrgId();
		SysDepart receiverName = sysDepartService.getById(receiverOrgId);
		//接收部门负责人信息 接收人
//		List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverName.getId()));

		//创建子接收部门数据
		CjxtTask taskChild = new CjxtTask();
		taskChild.setId(uuid);
		//节点信息
		if(cjxtTask.getId()!=null){
			taskChild.setPid(cjxtTask.getId());
			taskChild.setTaskCode(cjxtTask.getTaskCode());
		}else {
			taskChild.setPid(addresstask.getId());
			taskChild.setTaskCode(addresstask.getTaskCode());
		}
		taskChild.setHasChild(addresstask.getHasChild());
		//任务信息
		taskChild.setTaskName(addresstask.getTaskName());
		taskChild.setTaskDescription(addresstask.getTaskDescription());
		//模板信息
		taskChild.setMbId(addresstask.getMbId());
		taskChild.setMbName(addresstask.getMbName());
		taskChild.setMbCode(addresstask.getMbCode());
		taskChild.setBm(addresstask.getBm());
		//部门数据权限==派发部门
		taskChild.setOrgId(addresstask.getOrgId());
		taskChild.setOrgCode(addresstask.getOrgCode());
		taskChild.setOrgName(addresstask.getOrgName());
		//当前派发部门
		taskChild.setDispatcherOrgId(addresstask.getDispatcherOrgId());
		taskChild.setDispatcherOrgCode(addresstask.getDispatcherOrgCode());
		taskChild.setDispatcherOrgName(addresstask.getDispatcherOrgName());
		//当前派发人
		taskChild.setDispatcherId(addresstask.getDispatcherId());
		taskChild.setDispatcherName(addresstask.getDispatcherName());
		//接收部门、部门负责人信息
		taskChild.setReceiverOrgId(addresstask.getReceiverOrgId());
		taskChild.setReceiverOrgCode(addresstask.getReceiverOrgCode());
		taskChild.setReceiverOrgName(addresstask.getReceiverOrgName());
		taskChild.setReceiverBmfzrId(addresstask.getReceiverBmfzrId());
		taskChild.setReceiverBmfzrZh(addresstask.getReceiverBmfzrZh());
		taskChild.setReceiverBmfzrName(addresstask.getReceiverBmfzrName());
		//接收人信息
		taskChild.setReceiverId(addresstask.getReceiverId());
		taskChild.setReceiverZh(addresstask.getReceiverZh());
		taskChild.setReceiverName(addresstask.getReceiverName());
		taskChild.setRwzt("2");
		taskChild.setDueDate(addresstask.getDueDate());
		//采集情况
		taskChild.setCjZs(addressIdSize);
		taskChild.setCjYwc(0);
		taskChild.setCjSy(addressIdSize);
		taskChild.setCjWcqk("0%");
		taskChild.setChzt("1");
		cjxtTaskService.addCjxtTask(taskChild);

		if(cjxtTask.getAddressQhId()!=null && !"".equals(cjxtTask.getAddressQhId()) && (cjxtTask.getAddressId()==null || "".equals(cjxtTask.getAddressId()))){
			int j =0 ;
			if(addressList!=null && addressList.size()>0){
				java.util.Collection<CjxtTaskDispatch> entityList = new ArrayList<>();
				for(CjxtStandardAddress cjxtStandardAddress: addressList){
					j++;
					CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
					if(!"".equals(cjxtTask.getBlId()) && cjxtTask.getBlId()!=null){
						cjxtTaskDispatch.setBlId(cjxtTask.getBlId());
					}
					cjxtTaskDispatch.setTaskId(taskChild.getId());
					cjxtTaskDispatch.setTaskCode(taskChild.getTaskCode());
					cjxtTaskDispatch.setTaskName(addresstask.getTaskName());
					cjxtTaskDispatch.setTaskDescription(addresstask.getTaskDescription());
					cjxtTaskDispatch.setMbId(addresstask.getMbId());
					cjxtTaskDispatch.setMbCode(addresstask.getMbCode());
					cjxtTaskDispatch.setMbName(taskChild.getMbName());
					cjxtTaskDispatch.setBm(taskChild.getBm());
					//采集地址
					cjxtTaskDispatch.setAddressId(cjxtStandardAddress.getId());
					cjxtTaskDispatch.setAddressCode(cjxtStandardAddress.getAddressCodeMz());
					cjxtTaskDispatch.setAddressName(cjxtStandardAddress.getAddressNameMz());
					cjxtTaskDispatch.setDispatcherId(taskChild.getDispatcherId());
					cjxtTaskDispatch.setDispatcherName(taskChild.getDispatcherName());
					cjxtTaskDispatch.setReceiverId(receiverUser.getId());
					cjxtTaskDispatch.setReceiverName(receiverUser.getRealname());
					cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
					cjxtTaskDispatch.setRwzt("2");
					String sql = "SELECT id,create_time,update_time FROM " + cjxtTask.getBm() + " WHERE del_flag = '0' AND address_id = '" + cjxtStandardAddress.getId()+"' ORDER BY create_time ASC LIMIT 1";
					List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
					Date createTime = null;
					Date updateTime = null;
					if(resultList.size()>0){
						Map<String, Object> row = resultList.get(0);
						Object id = row.get("id");
						LocalDateTime create_time = (LocalDateTime) row.get("create_time");
						LocalDateTime update_time = (LocalDateTime) row.get("update_time");
						if(create_time != null){
							createTime = java.sql.Timestamp.valueOf(create_time);
						}
						if(update_time != null){
							updateTime = java.sql.Timestamp.valueOf(update_time);
						}
						cjxtTaskDispatch.setDataId(String.valueOf(id));
						if(updateTime!=null){
							cjxtTaskDispatch.setSchssj(updateTime);
						}else {
							cjxtTaskDispatch.setSchssj(createTime);
						}
						cjxtTaskDispatch.setHszt("2");
					}
					entityList.add(cjxtTaskDispatch);
				}
				cjxtTaskDispatchService.saveOrUpdateBatch(entityList);
				CjxtTask taskFather = new CjxtTask();
				taskFather.setId(taskChild.getId());
				taskFather.setCjZs(j);
				taskFather.setCjYwc(0);
				taskFather.setCjSy(j);
				taskFather.setCjWcqk("0%");
				cjxtTaskService.updateById(taskFather);

				CjxtTask taskFathers = new CjxtTask();
				if(isFirst == false){
					taskFathers.setId(cjxtTask.getId());
				}else {
					taskFathers.setId(addresstask.getId());
				}
				taskFathers.setCjZs(j);
				taskFathers.setCjYwc(0);
				taskFathers.setCjSy(j);
				taskFathers.setCjWcqk("0%");
				cjxtTaskService.updateById(taskFathers);
			}
		}

		if(cjxtTask.getAddressId()!=null && !"".equals(cjxtTask.getAddressId())){
			//创建子表任务
			String[] addressIds = cjxtTask.getAddressId().split(",");
			String[] addressCodes = cjxtTask.getAddressCode().split(",");
			String[] addressNames = cjxtTask.getAddressName().split(",");
			int zs = 0;
			java.util.Collection<CjxtTaskDispatch> entityList = new ArrayList<>();
			for (int j = 0; j < addressIds.length; j++) {
				zs++;
				String addressId = addressIds[j];
				String addressCode = addressCodes[j];
				String addressName = addressNames[j];
//			查询任务是否已派发
//			List<CjxtTaskDispatch> dispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getMbCode,cjxtTask.getMbCode()).eq(CjxtTaskDispatch::getMbId,cjxtTask.getMbId()).eq(CjxtTaskDispatch::getAddressId,addressId).last(" ORDER BY create_time LIMIT 1 "));
				CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
				if(!"".equals(cjxtTask.getBlId()) && cjxtTask.getBlId()!=null){
					cjxtTaskDispatch.setBlId(cjxtTask.getBlId());
				}
				cjxtTaskDispatch.setTaskId(taskChild.getId());
				cjxtTaskDispatch.setTaskCode(taskChild.getTaskCode());
				cjxtTaskDispatch.setTaskName(addresstask.getTaskName());
				cjxtTaskDispatch.setTaskDescription(addresstask.getTaskDescription());
				cjxtTaskDispatch.setMbId(addresstask.getMbId());
				cjxtTaskDispatch.setMbCode(addresstask.getMbCode());
				cjxtTaskDispatch.setMbName(taskChild.getMbName());
				cjxtTaskDispatch.setBm(taskChild.getBm());
				cjxtTaskDispatch.setAddressId(addressId);
				cjxtTaskDispatch.setAddressCode(addressCode);
				cjxtTaskDispatch.setAddressName(addressName);
				cjxtTaskDispatch.setDispatcherId(taskChild.getDispatcherId());
				cjxtTaskDispatch.setDispatcherName(taskChild.getDispatcherName());
				cjxtTaskDispatch.setReceiverId(receiverUser.getId());
				cjxtTaskDispatch.setReceiverName(receiverUser.getRealname());
				cjxtTaskDispatch.setDueDate(taskChild.getDueDate());
//			if(dispatchList!=null && dispatchList.size()>0){}
				List<Map<String, Object>> resultList = new ArrayList<>();
				String sql = "SELECT id,create_time,update_time FROM " + taskChild.getBm() + " WHERE del_flag = '0' AND address_id  = '" + addressId +"' ORDER BY create_time ASC LIMIT 1";
				resultList = jdbcTemplate.queryForList(sql);
				Date createTime = null;
				Date updateTime = null;

				if(resultList.size()>0){
					Map<String, Object> row = resultList.get(0);
					Object id = row.get("id");
					LocalDateTime create_time = (LocalDateTime) row.get("create_time");
					LocalDateTime update_time = (LocalDateTime) row.get("update_time");
					if(create_time != null){
						createTime = java.sql.Timestamp.valueOf(create_time);
					}
					if(update_time != null){
						updateTime = java.sql.Timestamp.valueOf(update_time);
					}
					cjxtTaskDispatch.setDataId(String.valueOf(id));
					if(updateTime!=null){
						cjxtTaskDispatch.setSchssj(updateTime);
					}else {
						cjxtTaskDispatch.setSchssj(createTime);
					}
					cjxtTaskDispatch.setHszt("2");
				}
				cjxtTaskDispatch.setRwzt("2");
				entityList.add(cjxtTaskDispatch);
			}
			cjxtTaskDispatchService.saveOrUpdateBatch(entityList);
		}


		CjxtTask taskFathers = new CjxtTask();
		taskFathers.setId(cjxtTask.getId());
		taskFathers.setCjZs(addressIdSize);
		taskFathers.setCjYwc(0);
		taskFathers.setCjSy(addressIdSize);
		taskFathers.setCjWcqk("0%");
		cjxtTaskService.updateById(taskFathers);
		try {
			if(!"".equals(taskChild.getReceiverBmfzrZh()) && !"".equals(taskChild.getReceiverZh()) && taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
				String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
				if(bmfzrZhs.length>0){
					for(int k=0;k<bmfzrZhs.length;k++){
						//发送系统消息给 部门负责人
						ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
						String title = "采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
						MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
						sysBaseApi.sendSysAnnouncement(messageDTO);

						//发送提醒消息
						CjxtWarningMessage messageSec = new CjxtWarningMessage();
						SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
						messageSec.setUserId(sysUserSec.getId());
						messageSec.setUsername(bmfzrZhs[k]);
						messageSec.setRealname(sysUserSec.getRealname());
						messageSec.setMessage("采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
						messageSec.setStatus("1");
						//messageSec.setDataId(""); //消息提醒不发数据ID
						messageSec.setBm(cjxtTask.getBm());
						messageSec.setMsgType("1");//提醒消息
						cjxtWarningMessageService.save(messageSec);

						//WebSocket消息推送
						if(sysUserSec.getId()!=null){
							JSONObject json = new JSONObject();
							json.put("msgType", "waMsg");
							String msg = json.toString();
							webSocket.sendOneMessage(sysUserSec.getId(), msg);
						}
					}
				}
			}
			if((!"".equals(taskChild.getReceiverBmfzrZh()) || !"".equals(taskChild.getReceiverZh()) || taskChild.getReceiverBmfzrZh()!=null || taskChild.getReceiverZh()!=null) && !taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
				if(!"".equals(taskChild.getReceiverBmfzrZh()) && taskChild.getReceiverBmfzrZh()!=null){
					String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
					if(bmfzrZhs.length>0){
						for(int k=0;k<bmfzrZhs.length;k++){
							//发送系统消息给 部门负责人
							ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
							String title = "采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
							MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
							sysBaseApi.sendSysAnnouncement(messageDTO);

							//发送提醒消息
							CjxtWarningMessage messageSec = new CjxtWarningMessage();
							SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
							messageSec.setUserId(sysUserSec.getId());
							messageSec.setUsername(bmfzrZhs[k]);
							messageSec.setRealname(sysUserSec.getRealname());
							messageSec.setMessage("采集任务提醒！,"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
							messageSec.setStatus("1");
							//messageSec.setDataId(""); //消息提醒不发数据ID
							messageSec.setBm(cjxtTask.getBm());
							messageSec.setMsgType("1");//提醒消息
							cjxtWarningMessageService.save(messageSec);

							//WebSocket消息推送
							if(sysUserSec.getId()!=null){
								JSONObject json = new JSONObject();
								json.put("msgType", "waMsg");
								String msg = json.toString();
								webSocket.sendOneMessage(sysUserSec.getId(), msg);
							}
						}
					}
				}

				if(!"".equals(taskChild.getReceiverZh()) && taskChild.getReceiverZh()!=null){
					String[] receiverZhs = taskChild.getReceiverZh().split(",");
					if(receiverZhs.length>0){
						for(int k=0;k<receiverZhs.length;k++){
							//发送系统消息给接收人
							ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
							String title = "采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
							MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
							sysBaseApi.sendSysAnnouncement(messageDTO);

							//发送提醒消息
							CjxtWarningMessage messageSec = new CjxtWarningMessage();
							SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[k]);
							messageSec.setUserId(sysUserSec.getId());
							messageSec.setUsername(receiverZhs[k]);
							messageSec.setRealname(sysUserSec.getRealname());
							messageSec.setMessage("采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
							messageSec.setStatus("1");
							//messageSec.setDataId(""); //消息提醒不发数据ID
							messageSec.setBm(cjxtTask.getBm());
							messageSec.setMsgType("1");//提醒消息
							cjxtWarningMessageService.save(messageSec);


							System.out.println("==============================发送网格员消息提醒==============================");
							//WebSocket消息推送
							if(sysUserSec.getId()!=null){
								JSONObject json = new JSONObject();
								json.put("msgType", "waMsg");
								String msg = json.toString();
								webSocket.sendOneMessage(sysUserSec.getId(), msg);
								System.out.println("==============================消息提醒发送成功==============================");
								log.info("接收人员ID：：：：：：：：：：：：：：：：：：：：：：："+sysUserSec.getId()+ "; 消息内容：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：" + msg);
							}
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result.OK("派发成功！");
	}

	/**
	 *   APP任务派发
	 *
	 * @param cjxtTask
	 * @return
	 */
	@AutoLog(value = "任务派发-APP区划任务派发")
	@ApiOperation(value="任务派发-APP区划任务派发", notes="任务派发-APP区划任务派发")
//    @RequiresPermissions("cjxt:cjxt_task:add")
	@PostMapping(value = "/addQhApp")
	public Result<String> addQhApp(@RequestBody CjxtTask cjxtTask) {
//		if((cjxtTask.getPid()==null || "0".equals(cjxtTask.getPid()) || "".equals(cjxtTask.getPid())) && "1".equals(cjxtTask.getChzt())){
//			List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTask.getTaskName()).eq(CjxtTask::getPid,"0"));
//			if(taskList.size()>0){
//				return Result.error("当前任务名称已存在,请修改任务名称!!!");
//			}
//		}
//		if(cjxtTask.getTaskNameDto()!=null){
//			if(!cjxtTask.getTaskNameDto().equals(cjxtTask.getTaskName())){
//				List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTask.getTaskName()).eq(CjxtTask::getPid,"0"));
//				if(taskList.size()>0){
//					return Result.error("当前任务名称已存在,请修改任务名称!!!");
//				}
//			}
//		}
		int addressIdSize = 0;
		List<CjxtStandardAddress> addressList = new ArrayList<>();
		//地址区划网格员
		SysUser addressQhUser = null;
		//拼接网格员ID
		String userIds = "";
		if(cjxtTask.getAddressQhId() != null && !"".equals(cjxtTask.getAddressQhId()) && cjxtTask.getAddressQhCode() != null && !"".equals(cjxtTask.getAddressQhCode())){

			String userDepartSql = "SELECT su.* FROM sys_user su \n" +
					"INNER JOIN sys_user_depart sud ON sud.user_id = su.id\n" +
					"INNER JOIN sys_depart sd ON sd.id = sud.dep_id\n" +
					"WHERE su.user_sf = '1' AND sd.org_code like '"+cjxtTask.getAddressQhCode()+"%' ";

			List<Map<String,Object>> userDepartList = jdbcTemplate.queryForList(userDepartSql);
			if(userDepartList.size()>0){
				boolean hasAddress = false;
				for(Map<String,Object> row: userDepartList){
					userIds += (String) row.get("id")+",";
					String orgCode = (String) row.get("org_code");
					//部门CODE查询地址信息
					addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressCodeMz,orgCode));
					if(addressList.size()>0){
						hasAddress = true;
					}
				}
				if(hasAddress==false){
					return Result.error("当前区划没有具体地址,任务无法派发!!!");
				}
			}else {
				return Result.error("当前地址区划不存在网格员,任务无法派发!!!");
			}
		}
		String recUserId = "";
		String recUserZh = "";
		String recUserName = "";
		String uuid = "";
		//当前派发用户信息
		SysUser sysUser = sysUserService.getById(cjxtTask.getDispatcherId());
		//接收人信息
		SysUser receiverUser = null;
		String[] USERIDS = userIds.substring(0,userIds.length()-1).split(",");
		for(int uId=0;uId< USERIDS.length;uId++){
			String userId = USERIDS[uId];
			receiverUser = sysUserService.getById(userId);
			//任务信息
			CjxtTask addresstask = new CjxtTask();
			//地址信息数量
			addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressCodeMz,receiverUser.getOrgCode()));
			addressIdSize = addressList.size();
			//当前接收部门信息
			SysDepart receiverDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,receiverUser.getOrgCode()).last("LIMIT 1"));
			//部门Code
			String orgCode = "";
			//查询部门负责人信息 接收人
			List<CjxtBmfzr> bmfzrList = new ArrayList<>();
			bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,receiverDepart.getId()));
			if(bmfzrList.size()>0){
				for(CjxtBmfzr cjxtBmfzr:bmfzrList){
					if(!"".equals(recUserId)){
						recUserId+=cjxtBmfzr.getFzryId()+",";
						recUserZh+=cjxtBmfzr.getLxdh()+",";
						recUserName+=cjxtBmfzr.getFzryName()+",";
					}
				}
			}else {
				int orgCategOry = Integer.valueOf(receiverDepart.getOrgCategory())-1;
				orgCode = receiverDepart.getOrgCode();
				if(orgCategOry==9){
					int lastIndex = orgCode.lastIndexOf('A');
					if (lastIndex != -1) {
						orgCode = orgCode.substring(0, lastIndex);
					}
					SysDepart recDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,orgCode).last("LIMIT 1"));
					bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,recDepart.getId()));
					if(bmfzrList.size()>0){
						for(CjxtBmfzr cjxtBmfzr:bmfzrList){
							if(!"".equals(recUserId)){
								recUserId+=cjxtBmfzr.getFzryId()+",";
								recUserZh+=cjxtBmfzr.getLxdh()+",";
								recUserName+=cjxtBmfzr.getFzryName()+",";
							}
						}
					}
				}
				if(orgCategOry==8){
					int lastIndex = orgCode.lastIndexOf('A');
					if (lastIndex != -1) {
						orgCode = orgCode.substring(0, lastIndex);
					}
					SysDepart recDep = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,orgCode).last("LIMIT 1"));
					bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,recDep.getId()));
					if(bmfzrList.size()>0){
						for(CjxtBmfzr cjxtBmfzr:bmfzrList){
							if(!"".equals(recUserId)){
								recUserId+=cjxtBmfzr.getFzryId()+",";
								recUserZh+=cjxtBmfzr.getLxdh()+",";
								recUserName+=cjxtBmfzr.getFzryName()+",";
							}
						}
					}
				}
			}

			uuid = UUID.randomUUID().toString().replace("-","");
			if(cjxtTask.getId()==null && !"".equals(cjxtTask.getId())){
				addresstask.setId(uuid);
				addresstask.setTaskCode(uuid);
			}else {
				addresstask.setId(uuid);
				addresstask.setTaskCode(cjxtTask.getId());
			}
			//节点信息
			addresstask.setPid(cjxtTask.getPid());
			addresstask.setHasChild(cjxtTask.getHasChild());
			//任务信息
			addresstask.setTaskName(cjxtTask.getTaskName());
			addresstask.setTaskDescription(cjxtTask.getTaskDescription());
			//模板信息
			addresstask.setMbId(cjxtTask.getMbId());
			addresstask.setMbName(cjxtTask.getMbName());
			addresstask.setMbCode(cjxtTask.getMbCode());
			addresstask.setBm(cjxtTask.getBm());
			//部门数据权限==派发部门
			addresstask.setOrgId(cjxtTask.getOrgId());
			addresstask.setOrgCode(cjxtTask.getOrgCode());
			addresstask.setOrgName(cjxtTask.getOrgName());
			//当前派发部门
			addresstask.setDispatcherOrgId(cjxtTask.getOrgId());
			addresstask.setDispatcherOrgCode(cjxtTask.getOrgCode());
			addresstask.setDispatcherOrgName(cjxtTask.getDispatcherOrgName());
			//当前派发人
			addresstask.setDispatcherId(cjxtTask.getDispatcherId());
			addresstask.setDispatcherName(cjxtTask.getDispatcherName());
			//接收部门
			addresstask.setReceiverOrgId(receiverDepart.getId());
			addresstask.setReceiverOrgCode(receiverDepart.getOrgCode());
			addresstask.setReceiverOrgName(receiverDepart.getDepartNameFull());
			//部门负责人信息
			if (recUserId != null && recUserId.length() > 0 && recUserId.charAt(recUserId.length() - 1) == ',') {
				recUserId = recUserId.substring(0, recUserId.length() - 1);
			}
			if (recUserZh != null && recUserZh.length() > 0 && recUserZh.charAt(recUserZh.length() - 1) == ',') {
				recUserZh = recUserZh.substring(0, recUserZh.length() - 1);
			}
			if (recUserName != null && recUserName.length() > 0 && recUserName.charAt(recUserName.length() - 1) == ',') {
				recUserName = recUserName.substring(0, recUserName.length() - 1);
			}
			addresstask.setReceiverBmfzrId(recUserId);
			addresstask.setReceiverBmfzrZh(recUserZh);
			addresstask.setReceiverBmfzrName(recUserName);
			//接收人信息
			addresstask.setReceiverId(receiverUser.getId());
			addresstask.setReceiverZh(receiverUser.getUsername());
			addresstask.setReceiverName(receiverUser.getRealname());

			addresstask.setDueDate(cjxtTask.getDueDate());
			//采集情况
			addresstask.setCjZs(addressIdSize);
			addresstask.setCjYwc(0);
			addresstask.setCjSy(addressIdSize);
			addresstask.setCjWcqk("0%");
			addresstask.setRwzt("2");
			addresstask.setChzt("1");
			if(addresstask.getPid()==null){
				cjxtTaskService.addCjxtTask(addresstask);
			}

			//创建接收部门任务
			uuid = UUID.randomUUID().toString().replace("-","");
			//接收部门详细信息
			String receiverOrgId = addresstask.getReceiverOrgId();
			SysDepart receiverName = sysDepartService.getById(receiverOrgId);

			//创建子接收部门数据
			CjxtTask taskChild = new CjxtTask();
			taskChild.setId(uuid);
			//节点信息
			if(cjxtTask.getId()!=null){
				taskChild.setPid(cjxtTask.getId());
				taskChild.setTaskCode(cjxtTask.getTaskCode());
			}else {
				taskChild.setPid(addresstask.getId());
				taskChild.setTaskCode(addresstask.getTaskCode());
			}
			taskChild.setHasChild(addresstask.getHasChild());
			//任务信息
			taskChild.setTaskName(addresstask.getTaskName());
			taskChild.setTaskDescription(addresstask.getTaskDescription());
			//模板信息
			taskChild.setMbId(addresstask.getMbId());
			taskChild.setMbName(addresstask.getMbName());
			taskChild.setMbCode(addresstask.getMbCode());
			taskChild.setBm(addresstask.getBm());
			//部门数据权限==派发部门
			taskChild.setOrgId(addresstask.getOrgId());
			taskChild.setOrgCode(addresstask.getOrgCode());
			taskChild.setOrgName(addresstask.getOrgName());
			//当前派发部门
			taskChild.setDispatcherOrgId(addresstask.getDispatcherOrgId());
			taskChild.setDispatcherOrgCode(addresstask.getDispatcherOrgCode());
			taskChild.setDispatcherOrgName(addresstask.getDispatcherOrgName());
			//当前派发人
			taskChild.setDispatcherId(addresstask.getDispatcherId());
			taskChild.setDispatcherName(addresstask.getDispatcherName());
			//接收部门、部门负责人信息
			taskChild.setReceiverOrgId(addresstask.getReceiverOrgId());
			taskChild.setReceiverOrgCode(addresstask.getReceiverOrgCode());
			taskChild.setReceiverOrgName(addresstask.getReceiverOrgName());
			taskChild.setReceiverBmfzrId(addresstask.getReceiverBmfzrId());
			taskChild.setReceiverBmfzrZh(addresstask.getReceiverBmfzrZh());
			taskChild.setReceiverBmfzrName(addresstask.getReceiverBmfzrName());
			//接收人信息
			taskChild.setReceiverId(addresstask.getReceiverId());
			taskChild.setReceiverZh(addresstask.getReceiverZh());
			taskChild.setReceiverName(addresstask.getReceiverName());
			taskChild.setRwzt("2");
			taskChild.setDueDate(addresstask.getDueDate());
			//采集情况
			taskChild.setCjZs(addressIdSize);
			taskChild.setCjYwc(0);
			taskChild.setCjSy(addressIdSize);
			taskChild.setCjWcqk("0%");
			taskChild.setChzt("1");
			cjxtTaskService.addCjxtTask(taskChild);

			if(cjxtTask.getAddressQhId()!=null && !"".equals(cjxtTask.getAddressQhId()) && (cjxtTask.getAddressId()==null || "".equals(cjxtTask.getAddressId()))){
				int j =0 ;
				if(addressList!=null && addressList.size()>0){
					java.util.Collection<CjxtTaskDispatch> entityList = new ArrayList<>();
					for(CjxtStandardAddress cjxtStandardAddress: addressList){
						j++;
						CjxtTaskDispatch cjxtTaskDispatch = new CjxtTaskDispatch();
						if(!"".equals(cjxtTask.getBlId()) && cjxtTask.getBlId()!=null){
							cjxtTaskDispatch.setBlId(cjxtTask.getBlId());
						}
						cjxtTaskDispatch.setTaskId(taskChild.getId());
						cjxtTaskDispatch.setTaskCode(taskChild.getTaskCode());
						cjxtTaskDispatch.setTaskName(addresstask.getTaskName());
						cjxtTaskDispatch.setTaskDescription(addresstask.getTaskDescription());
						cjxtTaskDispatch.setMbId(addresstask.getMbId());
						cjxtTaskDispatch.setMbCode(addresstask.getMbCode());
						cjxtTaskDispatch.setMbName(taskChild.getMbName());
						cjxtTaskDispatch.setBm(taskChild.getBm());
						//采集地址
						cjxtTaskDispatch.setAddressId(cjxtStandardAddress.getId());
						cjxtTaskDispatch.setAddressCode(cjxtStandardAddress.getAddressCodeMz());
						cjxtTaskDispatch.setAddressName(cjxtStandardAddress.getAddressNameMz());
						cjxtTaskDispatch.setDispatcherId(taskChild.getDispatcherId());
						cjxtTaskDispatch.setDispatcherName(taskChild.getDispatcherName());
						cjxtTaskDispatch.setReceiverId(receiverUser.getId());
						cjxtTaskDispatch.setReceiverName(receiverUser.getRealname());
						cjxtTaskDispatch.setDueDate(cjxtTask.getDueDate());
						cjxtTaskDispatch.setRwzt("2");
						String sql = "SELECT id,create_time,update_time FROM " + cjxtTask.getBm() + " WHERE del_flag = '0' AND address_id = '" + cjxtStandardAddress.getId()+"' ORDER BY create_time ASC LIMIT 1";
						List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
						Date createTime = null;
						Date updateTime = null;
						if(resultList.size()>0){
							Map<String, Object> row = resultList.get(0);
							Object id = row.get("id");
							LocalDateTime create_time = (LocalDateTime) row.get("create_time");
							LocalDateTime update_time = (LocalDateTime) row.get("update_time");
							if(create_time != null){
								createTime = java.sql.Timestamp.valueOf(create_time);
							}
							if(update_time != null){
								updateTime = java.sql.Timestamp.valueOf(update_time);
							}
							cjxtTaskDispatch.setDataId(String.valueOf(id));
							if(updateTime!=null){
								cjxtTaskDispatch.setSchssj(updateTime);
							}else {
								cjxtTaskDispatch.setSchssj(createTime);
							}
							cjxtTaskDispatch.setHszt("2");
						}
						entityList.add(cjxtTaskDispatch);
					}
					cjxtTaskDispatchService.saveOrUpdateBatch(entityList);
					CjxtTask taskFather = new CjxtTask();
					taskFather.setId(taskChild.getId());
					taskFather.setCjZs(j);
					taskFather.setCjYwc(0);
					taskFather.setCjSy(j);
					taskFather.setCjWcqk("0%");
					cjxtTaskService.updateById(taskFather);

					CjxtTask taskFathers = new CjxtTask();
					taskFathers.setId(addresstask.getId());
					taskFathers.setCjZs(j);
					taskFathers.setCjYwc(0);
					taskFathers.setCjSy(j);
					taskFathers.setCjWcqk("0%");
					cjxtTaskService.updateById(taskFathers);
				}
			}

			try {
				if(!"".equals(taskChild.getReceiverBmfzrZh()) && !"".equals(taskChild.getReceiverZh()) && taskChild.getReceiverBmfzrZh()!=null && taskChild.getReceiverZh()!=null && taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
					String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
					if(bmfzrZhs.length>0){
						for(int k=0;k<bmfzrZhs.length;k++){
							//发送系统消息给 部门负责人
							ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
							String title = "采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
							MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
							sysBaseApi.sendSysAnnouncement(messageDTO);

							//发送提醒消息
							CjxtWarningMessage messageSec = new CjxtWarningMessage();
							SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
							messageSec.setUserId(sysUserSec.getId());
							messageSec.setUsername(bmfzrZhs[k]);
							messageSec.setRealname(sysUserSec.getRealname());
							messageSec.setMessage("采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
							messageSec.setStatus("1");
							//messageSec.setDataId(""); //消息提醒不发数据ID
							messageSec.setBm(cjxtTask.getBm());
							messageSec.setMsgType("1");//提醒消息
							cjxtWarningMessageService.save(messageSec);

							//WebSocket消息推送
							if(sysUserSec.getId()!=null){
								JSONObject json = new JSONObject();
								json.put("msgType", "waMsg");
								String msg = json.toString();
								webSocket.sendOneMessage(sysUserSec.getId(), msg);
							}
						}
					}
				}
				if((!"".equals(taskChild.getReceiverBmfzrZh()) || !"".equals(taskChild.getReceiverZh()) || taskChild.getReceiverBmfzrZh()!=null || taskChild.getReceiverZh()!=null) && !taskChild.getReceiverBmfzrZh().equals(taskChild.getReceiverZh())){
					if(!"".equals(taskChild.getReceiverBmfzrZh()) && taskChild.getReceiverBmfzrZh()!=null){
						String[] bmfzrZhs = taskChild.getReceiverBmfzrZh().split(",");
						if(bmfzrZhs.length>0){
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(bmfzrZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(bmfzrZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！,"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(cjxtTask.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);

								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
								}
							}
						}
					}

					if(!"".equals(taskChild.getReceiverZh()) && taskChild.getReceiverZh()!=null){
						String[] receiverZhs = taskChild.getReceiverZh().split(",");
						if(receiverZhs.length>0){
							for(int k=0;k<receiverZhs.length;k++){
								//发送系统消息给接收人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = "采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务派发【" + taskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);

								//发送提醒消息
								CjxtWarningMessage messageSec = new CjxtWarningMessage();
								SysUser sysUserSec = sysUserService.getUserByName(receiverZhs[k]);
								messageSec.setUserId(sysUserSec.getId());
								messageSec.setUsername(receiverZhs[k]);
								messageSec.setRealname(sysUserSec.getRealname());
								messageSec.setMessage("采集任务提醒！"+cjxtTask.getDispatcherOrgName() + sysUser.getRealname() + "派发了采集任务【" + taskChild.getTaskName() + "】给您，请您尽快处理!");
								messageSec.setStatus("1");
								//messageSec.setDataId(""); //消息提醒不发数据ID
								messageSec.setBm(cjxtTask.getBm());
								messageSec.setMsgType("1");//提醒消息
								cjxtWarningMessageService.save(messageSec);


								System.out.println("==============================发送网格员消息提醒==============================");
								//WebSocket消息推送
								if(sysUserSec.getId()!=null){
									JSONObject json = new JSONObject();
									json.put("msgType", "waMsg");
									String msg = json.toString();
									webSocket.sendOneMessage(sysUserSec.getId(), msg);
									System.out.println("==============================消息提醒发送成功==============================");
									log.info("接收人员ID：：：：：：：：：：：：：：：：：：：：：：："+sysUserSec.getId()+ "; 消息内容：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：：" + msg);
								}
							}
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Result.OK("派发成功！");
	}

	/**
	 *   APP任务派发权限信息
	 ** @return
	 */
	@AutoLog(value = "任务派发-APP任务派发权限信息")
	@ApiOperation(value="任务派发-APP任务派发权限信息", notes="任务派发-APP任务派发权限信息")
	@GetMapping(value = "/addAppUser")
	public Result<Map<String, Object>> addAppUser(@RequestParam(name = "userId", required = true)String userId,
												  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		Map<String, Object> result = new HashMap<>();
		// 获取当前日期
		LocalDate today = LocalDate.now();
		// 当前日期加三天
		LocalDate futureDate = today.plusDays(3);
		// 定义日期格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		// 处理后日期
		String formattedDate = futureDate.format(formatter);


		//片警网格权限
		boolean isFirstCondition = true;
		StringBuilder mbId = new StringBuilder();
		List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(new LambdaQueryWrapper<CjxtJsmbpz>().inSql(CjxtJsmbpz::getRoleCode, "SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role u ON r.id = u.role_id WHERE u.user_id = '" +userId+ "'"));
		for (CjxtJsmbpz cjxtJsmbpz : cjxtJsmbpzList) {
			if (isFirstCondition) {
				isFirstCondition = false;
			} else {
				mbId.append(",");
			}
			mbId.append(cjxtJsmbpz.getMbId());
		}
		QueryWrapper<CjxtMbgl> queryWrapper = new QueryWrapper<CjxtMbgl>();
		queryWrapper.eq("sfsb", "1");
		if (!"".equals(mbId)) {
			List<String> idList = Arrays.asList(mbId.toString().split(","));
			queryWrapper.in("id", idList);
		}
		queryWrapper.orderByAsc("mblx").orderByAsc("mbsort");
		Page<CjxtMbgl> page = new Page<CjxtMbgl>(pageNo, pageSize);
		IPage<CjxtMbgl> pageList = cjxtMbglService.page(page, queryWrapper);
		pageList.getRecords().forEach(cjxtMbglItem -> {
			if (cjxtMbglItem.getMbname() != null && !cjxtMbglItem.getMbname().isEmpty()) {
				cjxtMbglItem.setTaskNameDto(cjxtMbglItem.getMbname()+"任务");
			}
		});

		//网格员
		List<SysUser> wgyUserList = sysUserService.list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserSf,"1").inSql(SysUser::getOrgCode,"SELECT wg_code FROM cjxt_pjwgqx WHERE pj_id = '"+userId+"'"));
		for(SysUser sysUser: wgyUserList){
			SysDepart sysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,sysUser.getOrgCode()));
			if(sysDepart!=null){
				sysUser.setRealnameDto(sysDepart.getDepartNameFull()+" | "+sysUser.getRealname());
			}
		}

		//采集地址信息
		List<CjxtStandardAddress> standardAddressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>()
				.inSql(CjxtStandardAddress::getAddressCodeMz,"SELECT address_code_mz FROM cjxt_standard_address WHERE address_code_mz IN ( SELECT wg_code FROM cjxt_pjwgqx WHERE pj_id = '"+userId+"')")
				.orderByAsc(CjxtStandardAddress::getAddressCodeMz)
				.orderByAsc(CjxtStandardAddress::getDetailLm)
				.orderByAsc(CjxtStandardAddress::getDetailLhm)
				.orderByAsc(CjxtStandardAddress::getDz1Ld)
				.orderByAsc(CjxtStandardAddress::getDz1Dy)
				.orderByAsc(CjxtStandardAddress::getDz1S)
				.orderByAsc(CjxtStandardAddress::getDz2Zm)
				.orderByAsc(CjxtStandardAddress::getDz2Hm)
				.orderByAsc(CjxtStandardAddress::getDz3Ldm)
				.orderByAsc(CjxtStandardAddress::getDz3Sm)
				.orderByAsc(CjxtStandardAddress::getDz5P)
				.orderByAsc(CjxtStandardAddress::getDz5H)
				.orderByAsc(CjxtStandardAddress::getDz5S)
				.orderByAsc(CjxtStandardAddress::getDz6S)
				.orderByAsc(CjxtStandardAddress::getDetailAddress)
		);

		for(CjxtStandardAddress cjxtStandardAddress: standardAddressList){
			String addressName = "";
			if ("1".equals(cjxtStandardAddress.getDzType())) {
				//小区名
				if (cjxtStandardAddress.getDz1Xqm() != null && !"".equals(cjxtStandardAddress.getDz1Xqm())) {
					addressName = addressName + cjxtStandardAddress.getDz1Xqm();
				}
				//楼栋
				if (cjxtStandardAddress.getDz1Ld() != null && !"".equals(cjxtStandardAddress.getDz1Ld())) {
					addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
				}
				//单元
				if (cjxtStandardAddress.getDz1Dy() != null && !"".equals(cjxtStandardAddress.getDz1Dy())) {
					addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
				}
				//室
				if (cjxtStandardAddress.getDz1S() != null && !"".equals(cjxtStandardAddress.getDz1S())) {
					addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
				}
			} else if ("2".equals(cjxtStandardAddress.getDzType())) {
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
				//村名
				if (cjxtStandardAddress.getDz2Cm() != null && !"".equals(cjxtStandardAddress.getDz2Cm())) {
					addressName = addressName + cjxtStandardAddress.getDz2Cm();
				}
				//组名
				if (cjxtStandardAddress.getDz2Zm() != null && !"".equals(cjxtStandardAddress.getDz2Zm())) {
					addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
				}
				//号名
				if (cjxtStandardAddress.getDz2Hm() != null && !"".equals(cjxtStandardAddress.getDz2Hm())) {
					addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
				}

			} else if ("3".equals(cjxtStandardAddress.getDzType())) {
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
				//大厦名
				if (cjxtStandardAddress.getDz3Dsm() != null && !"".equals(cjxtStandardAddress.getDz3Dsm())) {
					addressName = addressName + cjxtStandardAddress.getDz3Dsm();
				}
				//楼栋名
				if (cjxtStandardAddress.getDz3Ldm() != null && !"".equals(cjxtStandardAddress.getDz3Ldm())) {
					addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
				}
				//室名
				if (cjxtStandardAddress.getDz3Sm() != null && !"".equals(cjxtStandardAddress.getDz3Sm())) {
					addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
				}
			} else if ("4".equals(cjxtStandardAddress.getDzType())) {
				if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
					addressName = addressName + cjxtStandardAddress.getDetailMc();
				}
			} else if ("5".equals(cjxtStandardAddress.getDzType())) {
				if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
					addressName = addressName + cjxtStandardAddress.getDetailMc();
				}
				if (cjxtStandardAddress.getDz5P() != null && !"".equals(cjxtStandardAddress.getDz5P())) {
					addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
				}
				if (cjxtStandardAddress.getDz5H() != null && !"".equals(cjxtStandardAddress.getDz5H())) {
					addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
				}
				if (cjxtStandardAddress.getDz5S() != null && !"".equals(cjxtStandardAddress.getDz5S())) {
					addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
				}

			} else if ("6".equals(cjxtStandardAddress.getDzType())) {
				if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
					addressName = addressName + cjxtStandardAddress.getDetailMc();
				}
				if (cjxtStandardAddress.getDz6S() != null && !"".equals(cjxtStandardAddress.getDz6S())) {
					addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
				}
			} else if ("99".equals(cjxtStandardAddress.getDzType())) {
				if (cjxtStandardAddress.getDetailMc() != null && !"".equals(cjxtStandardAddress.getDetailMc())) {
					addressName = addressName + cjxtStandardAddress.getDetailMc();
				}
			}
			cjxtStandardAddress.setAddressName(addressName);
		}

		result.put("mbList", pageList);
		result.put("wgyUser", wgyUserList);
		result.put("addressList", standardAddressList);
		result.put("dueDate", formattedDate);
		return Result.OK(result);
	}

	/**
	 *  归档
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务派发-归档")
	@ApiOperation(value="任务派发-归档", notes="任务派发-归档")
	@PostMapping(value = "/fileOk")
	public Result<String> fileOk(@RequestParam(name="id",required=true) String id) {
		CjxtTask task = cjxtTaskService.getById(id);
		UpdateWrapper<CjxtTask> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("task_code", task.getTaskCode());
		updateWrapper.set("rwzt", 6);
		updateWrapper.set("gdsj", new Date());
		cjxtTaskService.update(updateWrapper);
		return Result.OK("归档成功!");
	}

	/**
	 *  派发完成
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务派发-派发完成")
	@ApiOperation(value="任务派发-派发完成", notes="任务派发-派发完成")
	@PostMapping(value = "/disOk")
	public Result<String> disOk(@RequestParam(name="id",required=true) String id) {
		CjxtTask task = cjxtTaskService.getById(id);
		task.setRwzt("4");
		cjxtTaskService.updateById(task);
		return Result.OK("派发完成!");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtTask
	 * @return
	 */
	@AutoLog(value = "任务派发-编辑")
	@ApiOperation(value="任务派发-编辑", notes="任务派发-编辑")
//    @RequiresPermissions("cjxt:cjxt_task:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtTask cjxtTask) {
		cjxtTaskService.updateCjxtTask(cjxtTask);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "任务派发-通过id删除")
	@ApiOperation(value="任务派发-通过id删除", notes="任务派发-通过id删除")
//    @RequiresPermissions("cjxt:cjxt_task:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtTaskService.deleteCjxtTask(id);
		return Result.OK("删除成功!");
	}

	@AutoLog(value = "任务派发-APP删除")
	@ApiOperation(value="任务派发-APP删除", notes="任务派发-APP删除")
//    @RequiresPermissions("cjxt:cjxt_task:delete")
	@GetMapping(value = "/appDelete")
	public Result<String> appDelete(@RequestParam(name="id",required=true) String id) {
		CjxtTask cjxtTask = cjxtTaskService.getById(id);
		if(cjxtTask!=null){
			CjxtWarningMessage cjxtWarningMessage = cjxtWarningMessageService.getOne(new LambdaQueryWrapper<CjxtWarningMessage>().eq(CjxtWarningMessage::getBm,cjxtTask.getBm())
					.eq(CjxtWarningMessage::getCreateBy,cjxtTask.getCreateBy())
					.eq(CjxtWarningMessage::getStatus,"1")
					.eq(CjxtWarningMessage::getMsgType,"1")
					.orderByDesc(CjxtWarningMessage::getCreateTime)
					.last("LIMIT 1"));
			if(cjxtWarningMessage!=null){
				if(cjxtWarningMessage.getUserId()!=null){
					JSONObject json = new JSONObject();
					json.put("msgType", "waMsg");
					String msg = json.toString();
					webSocket.sendOneMessage(cjxtWarningMessage.getUserId(), msg);
				}
				cjxtWarningMessageService.removeById(cjxtWarningMessage.getId());
			}
			List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskCode,cjxtTask.getTaskCode()));
			List<CjxtTaskDispatch> dispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getTaskCode,cjxtTask.getTaskCode()));
			for(CjxtTask task: taskList){
				if(task!=null){
					cjxtTaskService.removeById(task.getId());
				}
			}
			for(CjxtTaskDispatch cjxtTaskDispatch : dispatchList){
				if(cjxtTaskDispatch!=null){
					cjxtTaskDispatchService.removeById(cjxtTaskDispatch.getId());
				}
			}
		}
		return Result.OK("成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "任务派发-批量删除")
	@ApiOperation(value="任务派发-批量删除", notes="任务派发-批量删除")
//    @RequiresPermissions("cjxt:cjxt_task:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtTaskService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "任务派发-通过id查询")
	@ApiOperation(value="任务派发-通过id查询", notes="任务派发-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtTask> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtTask cjxtTask = cjxtTaskService.getById(id);
		if(cjxtTask==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtTask);
	}

	@ApiOperation(value="任务派发-树结构部门信息查询", notes="任务派发-树结构部门信息查询")
	@RequestMapping(value = "/queryDepartTreeSync", method = RequestMethod.GET)
	public Result<Map<String, Object>> queryIdTree(@RequestParam(name = "userId",required = false)String userId) {
//		Result<List<SysDepartTreeModel>> result = new Result<>();
		Map<String, Object> result = new HashMap<>();
		try {
			//获取当前用户
			SysUser sysUser = sysUserService.getById(userId);
			//获取部门信息
			SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
			String parentId = sysDepart.getId();
			String sql = "select d.data_org_id from cjxt_bm_data d where d.org_id='"+ sysDepart.getId() +"' and d.del_flag='0'";
//				Dg.writeContent(jsjbQzsq.getGdh(),"查询主要领导sql: " + sql);
			List<String> fgld = jdbcTemplate.queryForList(sql, String.class);

			sql = "select d.wg_id from cjxt_pjwgqx d where d.pj_id='"+ sysUser.getId() +"' and d.del_flag='0' ORDER BY d.wg_code";
			List<String> pjQx = jdbcTemplate.queryForList(sql, String.class);

			if(fgld != null && fgld.size() > 0){
				if(StringUtils.isEmpty(parentId)){
					parentId = String.join(",", fgld);
				}else{
					parentId += "," + String.join(",", fgld);
				}
			}
			if(pjQx != null && pjQx.size() > 0){
				parentId = String.join(",", pjQx);
			}

			System.out.println("输出parentId"+parentId);
			String[] PARENTID = parentId.split(",");

			List<Map<String, Object>> dataList = new ArrayList<>();
			for(int i = 0; i < PARENTID.length; i++){
				String pId = PARENTID[i];
				List<SysDepartTreeModel> list = sysDepartService.queryMyDeptTreeList(pId);
				Map<String, Object> map = new HashMap<>();
				map.put("list"+i, list);
				dataList.add(map);
			}
			result.put("data", dataList);
			result.put("success", true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return Result.ok(result);
	}

	@AutoLog(value = "任务派发-网格员任务批量处理")
	@ApiOperation(value="任务派发-网格员任务批量处理", notes="任务派发-网格员任务批量处理")
	@PostMapping(value = "/batchReview")
	public Result<String> batchReview(@RequestParam(name="mbCode",required=true) String mbCode,
									  @RequestParam(name="addressId",required=true) String addressId,
									  @RequestParam(name="taskId",required=true) String taskId) {
		CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
		List<Map<String,Object>> resultList = null;
		if(cjxtMbgl!=null){
			String[] addressID = addressId.split(",");
			String dataId = "";
			for(int i=0;i<addressID.length;i++){
				String aId = addressID[i];
				resultList = jdbcTemplate.queryForList("SELECT * FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' AND t.address_id = '"+aId+"' ORDER BY t.create_time,t.update_time DESC LIMIT 1");
				if(resultList.size()>0){
					Map<String,Object> row = resultList.get(0);
					dataId = (String) row.get("id");
					String updateSql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = null WHERE del_flag = '0' AND address_id = '"+aId+"' ;";
					jdbcTemplate.update(updateSql);
				}else {
					return Result.error("无法批量完成,选中任务存在未采集数据!!!");
				}
			}

			String taskID[] = taskId.split(",");
			for(int i=0;i<taskID.length;i++){
				String tId = taskID[i];
				CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getById(tId);
				if (dispatch != null && "2".equals(dispatch.getRwzt())) {
					CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getId, tId));
					cjxtTaskDispatch.setId(tId);
					cjxtTaskDispatch.setHszt("2");
					cjxtTaskDispatch.setSchssj(new Date());
					cjxtTaskDispatch.setWcsj(new Date());
					if(!"".equals(dataId)){
						cjxtTaskDispatch.setDataId(dataId);
					}
					cjxtTaskDispatch.setRwzt("4");
					cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
					String zsql = "";
					String ywc = "";
					String[] usql = new String[3];

					CjxtTask cjxtTask = cjxtTaskService.getById(cjxtTaskDispatch.getTaskId());
					if(cjxtTask !=null && cjxtTask.getCjYwc() != null){
						ywc = "" + (cjxtTask.getCjYwc().intValue() + 1);
					}else{
						ywc = "1";
					}
					//根据主键id，更新采集进度字段,和任务状态，若任务完成，则任务状态为已完成
					usql[0] = "update cjxt_task t set t.cj_ywc='" + ywc + "',t.cj_sy=t.cj_sy-1,t.cj_wcqk=CONCAT(cast(ROUND(if(t.cj_ywc is null,1," + ywc + ")/t.cj_zs, 2)*100 AS SIGNED) ,'%'),t.rwzt=if(" + ywc + "=t.cj_zs,'4','2')," +
							"t.wcsj=if(" + ywc + "=t.cj_zs,now(),null)," +
							"t.zys=if(t.cj_ywc+1=t.cj_zs,CONCAT(\n" +
							"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())/86400),'天',\n" +
							"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())%86400/3600),'小时',\n" +
							"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)/60),'分钟',\n" +
							"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)%60),'秒'\n" +
							") ,null) " +
							"where t.id='" + cjxtTaskDispatch.getTaskId() + "';";
					Dg.writeContent("aa",usql[0]);
					log.info("输出任务sql===============================1======"+usql[0]);
					jdbcTemplate.update(usql[0]);
					cjxtTask = cjxtTaskService.getById(cjxtTaskDispatch.getTaskId());

					if (cjxtTask.getPid() != null && !"".equals(cjxtTask.getPid()) && !"0".equals(cjxtTask.getPid())) {
						CjxtTask cjxtTask_1 = cjxtTaskService.getById(cjxtTask.getPid());
						if (cjxtTask_1 != null && !"".equals(cjxtTask_1.getId())) {
							zsql = "select sum(t1.cj_ywc) from cjxt_task t1 where t1.pid = '" + cjxtTask_1.getId() + "'";
							List<String> fgld = jdbcTemplate.queryForList(zsql, String.class);
							if (fgld != null && fgld.size() > 0) {
								ywc = fgld.get(0);
							}
							usql[0] = "update cjxt_task t set t.cj_ywc='" + ywc + "'," +
									"t.cj_sy=t.cj_zs-'" + ywc + "'," +
									"t.cj_wcqk=CONCAT(cast(ROUND('" + ywc + "'/t.cj_zs, 2)*100 AS SIGNED) ,'%')," +
									"t.rwzt=if('" + ywc + "'=t.cj_zs,'4','2')," +
									"t.wcsj=if('" + ywc + "'=t.cj_zs,now(),null)," +
									"t.zys=if('" + ywc + "'=t.cj_zs,CONCAT(\n" +
									"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())/86400),'天',\n" +
									"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())%86400/3600),'小时',\n" +
									"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)/60),'分钟',\n" +
									"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)%60),'秒'\n" +
									") ,null) " +
									"where t.id='" + cjxtTask_1.getId() + "';";
							log.info("输出任务sql===============================2======"+usql[0]);
							Dg.writeContent("aa",usql[0]);
							jdbcTemplate.update(usql[0]);
							if (cjxtTask_1.getPid() != null && !"".equals(cjxtTask_1.getPid()) && !"0".equals(cjxtTask_1.getPid())) {
								CjxtTask cjxtTask_2 = cjxtTaskService.getById(cjxtTask_1.getPid());
								if (cjxtTask_2 != null && !"".equals(cjxtTask_2.getId())) {
									zsql = "select sum(t1.cj_ywc) from cjxt_task t1 where t1.pid = '" + cjxtTask_2.getId() + "'";
									fgld = jdbcTemplate.queryForList(zsql, String.class);
									if (fgld != null && fgld.size() > 0) {
										ywc = fgld.get(0);
									}
									usql[0] = "update cjxt_task t set t.cj_ywc='" + ywc + "'," +
											"t.cj_sy=t.cj_zs-'" + ywc + "'," +
											"t.cj_wcqk=CONCAT(cast(ROUND('" + ywc + "'/t.cj_zs, 2)*100 AS SIGNED) ,'%')," +
											"t.rwzt=if('" + ywc + "'=t.cj_zs,'4','2')," +
											"t.wcsj=if('" + ywc + "'=t.cj_zs,now(),null)," +
											"t.zys=if('" + ywc + "'=t.cj_zs,CONCAT(\n" +
											"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())/86400),'天',\n" +
											"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())%86400/3600),'小时',\n" +
											"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)/60),'分钟',\n" +
											"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)%60),'秒'\n" +
											") ,null) " +
											"where t.id='" + cjxtTask_2.getId() + "';";
									log.info("输出任务sql===============================3======"+usql[0]);
									jdbcTemplate.update(usql[0]);
									if (cjxtTask_2.getPid() != null && !"".equals(cjxtTask_2.getPid()) && !"0".equals(cjxtTask_2.getPid())) {
										CjxtTask cjxtTask_3 = cjxtTaskService.getById(cjxtTask_2.getPid());
										if (cjxtTask_3 != null && !"".equals(cjxtTask_3.getId())) {
											zsql = "select sum(t1.cj_ywc) from cjxt_task t1 where t1.pid = '" + cjxtTask_3.getId() + "'";
											fgld = jdbcTemplate.queryForList(zsql, String.class);
											if (fgld != null && fgld.size() > 0) {
												ywc = fgld.get(0);
											}
											usql[0] = "update cjxt_task t set t.cj_ywc='" + ywc + "'," +
													"t.cj_sy=t.cj_zs-'" + ywc + "'," +
													"t.cj_wcqk=CONCAT(cast(ROUND('" + ywc + "'/t.cj_zs, 2)*100 AS SIGNED) ,'%')," +
													"t.rwzt=if('" + ywc + "'=t.cj_zs,'4','2')," +
													"t.wcsj=if('" + ywc + "'=t.cj_zs,now(),null)," +
													"t.zys=if('" + ywc + "'=t.cj_zs,CONCAT(\n" +
													"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())/86400),'天',\n" +
													"FLOOR(TIMESTAMPDIFF(SECOND,create_time,now())%86400/3600),'小时',\n" +
													"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)/60),'分钟',\n" +
													"FLOOR((TIMESTAMPDIFF(SECOND,create_time,now())%3600)%60),'秒'\n" +
													") ,null) " +
													"where t.id='" + cjxtTask_3.getId() + "';";
											log.info("输出任务sql===============================4======"+usql[0]);
											jdbcTemplate.update(usql[0]);
										}
									}
								}
							}
						}
					}

					//采集完成 消息推送派发人
					CjxtTask taskRwzt = cjxtTaskService.getById(cjxtTaskDispatch.getTaskId());
					if(taskRwzt!=null && "4".equals(taskRwzt.getRwzt())){
						log.info("================================================demo 开始推送");
						//WebSocket消息推送
						JSONObject json = new JSONObject();
						json.put("msgType", "waMsg");
						String msg = json.toString();
						webSocket.sendOneMessage(taskRwzt.getDispatcherId(), msg);
						log.info("================================================demo 开始推送内容：：：：：："+msg);
//                        System.out.println("任务完成发送派发人消息"+);
					}

					//积分规则
					CjxtScoreRule cjxtScoreRule = cjxtScoreRuleService.getOne(new LambdaQueryWrapper<CjxtScoreRule>().eq(CjxtScoreRule::getMbCode, mbCode));
					//积分明细
					if (cjxtScoreRule != null) {
						CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
						cjxtScoreDetail.setUserId(dispatch.getReceiverId());
						cjxtScoreDetail.setUserName(dispatch.getReceiverName());
						cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
						cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
						cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
						cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
						cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
						cjxtScoreDetailService.save(cjxtScoreDetail);
					}
				}
//				if(resultList.size()<=0){
//
//				}

				//发送消息处理地址
				String dispatchName = "";
				CjxtStandardAddress dispatchA = cjxtStandardAddressService.getById(dispatch.getAddressId());
				if(dispatchA!=null){
					if ("1".equals(dispatchA.getDzType())) {
						//小区名
						if (dispatchA.getDz1Xqm() != null && !"".equals(dispatchA.getDz1Xqm())) {
							dispatchName = dispatchName + dispatchA.getDz1Xqm();
						}
						//楼栋
						if (dispatchA.getDz1Ld() != null && !"".equals(dispatchA.getDz1Ld())) {
							dispatchName = dispatchName + dispatchA.getDz1Ld() + "号楼";
						}
						//单元
						if (dispatchA.getDz1Dy() != null && !"".equals(dispatchA.getDz1Dy())) {
							dispatchName = dispatchName + dispatchA.getDz1Dy() + "单元";
						}
						//室
						if (dispatchA.getDz1S() != null && !"".equals(dispatchA.getDz1S())) {
							dispatchName = dispatchName + dispatchA.getDz1S() + "室";
						}
					} else if ("2".equals(dispatchA.getDzType())) {
						dispatchA.setDetailMc(dispatchA.getDz2Cm());
						//村名
						if (dispatchA.getDz2Cm() != null && !"".equals(dispatchA.getDz2Cm())) {
							dispatchName = dispatchName + dispatchA.getDz2Cm();
						}
						//组名
						if (dispatchA.getDz2Zm() != null && !"".equals(dispatchA.getDz2Zm())) {
							dispatchName = dispatchName + dispatchA.getDz2Zm() + "组";
						}
						//号名
						if (dispatchA.getDz2Hm() != null && !"".equals(dispatchA.getDz2Hm())) {
							dispatchName = dispatchName + dispatchA.getDz2Hm() + "号";
						}

					} else if ("3".equals(dispatchA.getDzType())) {
						dispatchA.setDetailMc(dispatchA.getDz3Dsm());
						//大厦名
						if (dispatchA.getDz3Dsm() != null && !"".equals(dispatchA.getDz3Dsm())) {
							dispatchName = dispatchName + dispatchA.getDz3Dsm();
						}
						//楼栋名
						if (dispatchA.getDz3Ldm() != null && !"".equals(dispatchA.getDz3Ldm())) {
							dispatchName = dispatchName + dispatchA.getDz3Ldm() + "栋";
						}
						//室名
						if (dispatchA.getDz3Sm() != null && !"".equals(dispatchA.getDz3Sm())) {
							dispatchName = dispatchName + dispatchA.getDz3Sm() + "室";
						}
					} else if ("4".equals(dispatchA.getDzType())) {
						if (dispatchA.getDetailMc() != null && !"".equals(dispatchA.getDetailMc())) {
							dispatchName = dispatchName + dispatchA.getDetailMc();
						}
					} else if ("5".equals(dispatchA.getDzType())) {
						if (dispatchA.getDetailMc() != null && !"".equals(dispatchA.getDetailMc())) {
							dispatchName = dispatchName + dispatchA.getDetailMc();
						}
						if (dispatchA.getDz5P() != null && !"".equals(dispatchA.getDz5P())) {
							dispatchName = dispatchName + dispatchA.getDz5P() + "排";
						}
						if (dispatchA.getDz5H() != null && !"".equals(dispatchA.getDz5H())) {
							dispatchName = dispatchName + dispatchA.getDz5H() + "号";
						}
						if (dispatchA.getDz5S() != null && !"".equals(dispatchA.getDz5S())) {
							dispatchName = dispatchName + dispatchA.getDz5S() + "室";
						}
					} else if ("6".equals(dispatchA.getDzType())) {
						if (dispatchA.getDetailMc() != null && !"".equals(dispatchA.getDetailMc())) {
							dispatchName = dispatchName + dispatchA.getDetailMc();
						}
						if (dispatchA.getDz6S() != null && !"".equals(dispatchA.getDz6S())) {
							dispatchName = dispatchName + dispatchA.getDz6S() + "室";
						}
					} else if ("99".equals(dispatchA.getDzType())) {
						if (dispatchA.getDetailMc() != null && !"".equals(dispatchA.getDetailMc())) {
							dispatchName = dispatchName + dispatchA.getDetailMc();
						}
					}
				}
				if("".equals(dispatchName)){
					dispatchName =  dispatch.getAddressName();
				}
				//发送提醒消息
				CjxtWarningMessage messageSec = new CjxtWarningMessage();
				//派发人信息
				SysUser dispatchUser = sysUserService.getById(dispatch.getDispatcherId());
				messageSec.setUserId(dispatchUser.getId());
				messageSec.setUsername(dispatchUser.getUsername());
				messageSec.setRealname(dispatch.getReceiverName());
				messageSec.setMessage("任务完成提醒！"+"任务名称【"+dispatch.getTaskName()+"】,执行人【"+dispatch.getReceiverName() + "】,采集地址【"+dispatchName+"】,请知晓!");
				messageSec.setStatus("1");
				//messageSec.setDataId(""); //消息提醒不发数据ID
				messageSec.setBm(dispatch.getBm());
				messageSec.setMsgType("1");//提醒消息
				cjxtWarningMessageService.save(messageSec);

				//WebSocket消息推送
				if(dispatchUser.getId()!=null){
					JSONObject json = new JSONObject();
					json.put("msgType", "waMsg");
					String msg = json.toString();
					webSocket.sendOneMessage(dispatchUser.getId(), msg);
				}
			}
		}
		return Result.OK("操作成功!");
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param cjxtTask
	 */
//    @RequiresPermissions("cjxt:cjxt_task:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, CjxtTask cjxtTask) {
		return super.exportXls(request, cjxtTask, CjxtTask.class, "任务派发");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
//    @RequiresPermissions("cjxt:cjxt_task:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		String msg = "";
		String uuid = "";
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// 获取上传文件对象
			MultipartFile file = entity.getValue();

			try {
				byte[] bytes = file.getBytes();

				CjxtMbgl po = new CjxtMbgl();;

				boolean sfcj = false;//是否创建
				String sfcjValue = "";
				//模板管理
				ImportParams params1 = new ImportParams();
				params1.setTitleRows(2);
				params1.setHeadRows(1);
				params1.setNeedSave(true);
				params1.setStartSheetIndex(0);
				params1.setSheetNum(1);
				List<CjxtMbglPage> list2 = ExcelImportUtil.importExcel(new ByteArrayInputStream(bytes), CjxtMbglPage.class, params1);
				for (CjxtMbglPage page : list2) {
					sfcjValue = page.getSfcj();
					CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm,page.getBm()));
					if(cjxtMbgl!=null && "1".equals(page.getSfcj())){
						msg = "当前模板表名已存在!!!如果创建模版,请修改模版表名!";
						return Result.error(msg);
					}else if(cjxtMbgl==null && !"".equals(page.getSfcj())){
						sfcj = true;
					}else if(cjxtMbgl!=null && "0".equals(page.getSfcj())){
						po = cjxtMbgl;
					}
					if(sfcj==true){
						BeanUtils.copyProperties(page, po);
						cjxtMbglService.saveMain(po, page.getCjxtMbglPzList());
					}
				}
				if(sfcj==true){
					CjxtMbgl cjxtMbgl = cjxtMbglService.getById(po.getId());
					if(cjxtMbgl!=null){
						List list = new ArrayList<>();
						String tablename = cjxtMbgl.getBm();
						list.add(tablename);
						if("1".equals(cjxtMbgl.getSfls())){
							list.add(tablename + "_ls"); //历史表
						}
						if("1".equals(cjxtMbgl.getSfsb())){
							list.add(tablename + "_sb"); //上报表
						}
						for(Object table : list){
							//查询数据库中是否存在表
							String checkTableExistsSql = "SHOW TABLES LIKE '" + table + "'";
							List<String> tables = jdbcTemplate.queryForList(checkTableExistsSql, String.class);
							if (tables.size() > 0) {
								// 存在表删除表
								String dropTableSql = "DROP TABLE " + table;
								jdbcTemplate.execute(dropTableSql);
							}

							List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId,cjxtMbgl.getId()).eq(CjxtMbglPz::getDelFlag,"0").eq(CjxtMbglPz::getIsTitle,"0").orderByAsc(CjxtMbglPz::getOrderNum));
							if(cjxtMbglPzList.size()>0 && cjxtMbgl!=null){
								StringBuilder sql = new StringBuilder("CREATE TABLE " + table + " (");
								String primaryKey = "";
								for(CjxtMbglPz cjxtMbglPz : cjxtMbglPzList){
									String dbType = mapDbType(cjxtMbglPz.getDbType());
									sql.append(cjxtMbglPz.getDbFieldName()).append(" ").append(dbType);
									if(cjxtMbglPz.getDbLength() != null&&!"0".equals(cjxtMbglPz.getDbLength())&&cjxtMbglPz.getDbLength()!="0") {
										if("Date".equals(cjxtMbglPz.getDbType()) || "Datetime".equals(cjxtMbglPz.getDbType()) || "date".equals(cjxtMbglPz.getFieldShowType()) || "datetime".equals(cjxtMbglPz.getFieldShowType())){
											sql.append("(").append("0").append(")");
										} else if ("image".equals(cjxtMbglPz.getFieldShowType()) || "textarea".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
											sql.append("(").append("1000").append(")");
										} else {
											sql.append("(").append(cjxtMbglPz.getDbLength()).append(")");
										}
									}
									if(cjxtMbglPz.getDbDefaultVal() != null && !"".equals(cjxtMbglPz.getDbDefaultVal())) {
										sql.append(" DEFAULT '").append(cjxtMbglPz.getDbDefaultVal()).append("'");
									}
									if("1".equals(cjxtMbglPz.getDbIsKey())) {
										primaryKey = cjxtMbglPz.getDbFieldName();
									}
									sql.append(" COMMENT '").append(cjxtMbglPz.getDbFieldTxt()).append("', ");
								}
								if(primaryKey.isEmpty()) {
									primaryKey = "id"; // 如果没有字段被设置为主键，就将名为"id"的字段设置为主键
								}
								sql.append("PRIMARY KEY (").append(primaryKey).append(")");
								sql.append(")");
								jdbcTemplate.execute(sql.toString());
								//如果表名包含_ls 怎给表中新增字段dataid
								if (String.valueOf(table).contains("_ls")) {
									StringBuilder sql1 = new StringBuilder("ALTER TABLE " + table + " ADD COLUMN data_id_ls VARCHAR(64) DEFAULT NULL COMMENT '主表ID'");
									jdbcTemplate.execute(sql1.toString());
								}
								cjxtMbgl.setIsDb("1");
								cjxtMbglService.updateById(cjxtMbgl);
							}
						}
					}
				}
				//任务派发
				ImportParams params2 = new ImportParams();
				params2.setTitleRows(2);
				params2.setHeadRows(1);
				params2.setNeedSave(true);
				params2.setStartSheetIndex(1);
				params2.setSheetNum(1);
				List<CjxtTaskImport> list = ExcelImportUtil.importExcel(new ByteArrayInputStream(bytes), CjxtTaskImport.class, params2);
				String receiverOrgNames = list.stream().map(CjxtTaskImport::getReceiverOrgName).collect(Collectors.joining(","));
				SysDepart sysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,sysUser.getOrgCode()));
				uuid = UUID.randomUUID().toString().replace("-","");

				String recBmfzrId = "";
				String recBmfzrZh = "";
				String recBmfzrName = "";
				String receiverOrgId = "";
				String receiverOrgCode = "";
				String receiverOrgName = "";
				int cjzs = 0;
				if (!list.isEmpty()) {
					CjxtTaskImport cjxtTaskImport = list.get(0);
					if(cjxtTaskImport.getTaskName()!=null || !"".equals(cjxtTaskImport.getTaskName())){
						List<CjxtTask> taskList = cjxtTaskService.list(new LambdaQueryWrapper<CjxtTask>().eq(CjxtTask::getTaskName,cjxtTaskImport.getTaskName()).eq(CjxtTask::getPid,"0"));
						if(taskList.size()>0){
							if("1".equals(sfcjValue)){
								cjxtMbglService.removeById(po.getId());
								cjxtMbglPzService.remove(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId,po.getId()));
							}
							return Result.error("当前任务名称已存在,请修改任务名称,重新导入!!!");
						}
					}
					cjxtTaskImport.setId(uuid);
					cjxtTaskImport.setPid("0");
					cjxtTaskImport.setHasChild("0");
					cjxtTaskImport.setTaskCode(uuid);
					cjxtTaskImport.setTaskName(cjxtTaskImport.getTaskName());
					cjxtTaskImport.setTaskDescription(cjxtTaskImport.getTaskDescription());
					cjxtTaskImport.setMbId(po.getId());
					cjxtTaskImport.setMbCode(po.getMbbh());
					cjxtTaskImport.setMbName(po.getMbname());
					cjxtTaskImport.setBm(po.getBm());
					cjxtTaskImport.setOrgId(sysDepart.getId());
					cjxtTaskImport.setOrgCode(sysDepart.getOrgCode());
					cjxtTaskImport.setOrgName(sysDepart.getDepartNameFull());
					cjxtTaskImport.setDispatcherOrgId(sysDepart.getId());
					cjxtTaskImport.setDispatcherOrgCode(sysDepart.getOrgCode());
					cjxtTaskImport.setDispatcherOrgName(sysDepart.getDepartNameFull());
					cjxtTaskImport.setDispatcherId(sysUser.getId());
					cjxtTaskImport.setDispatcherName(sysUser.getRealname());
					String[] receiverOrgNameS = receiverOrgNames.split(",");
					for(int i=0;i<receiverOrgNameS.length;i++){
						String orgName = receiverOrgNameS[i];
						List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmmc,orgName));
						for(CjxtBmfzr cjxtBmfzr: bmfzrList){
							recBmfzrId += cjxtBmfzr.getFzryId()+",";
							recBmfzrZh += cjxtBmfzr.getLxdh()+",";
							recBmfzrName += cjxtBmfzr.getFzryName()+",";
						}
						SysDepart orgDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getDepartNameFull,orgName));
						//接收部门采集数量
						List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,orgDepart.getId()));
						cjzs += addressList.size();
						if(orgDepart!=null){
							receiverOrgId +=  orgDepart.getId() + ",";
							receiverOrgCode += orgDepart.getOrgCode() + ",";
							receiverOrgName += orgDepart.getDepartNameFull() + ",";
						}else{
							if("1".equals(sfcjValue)){
								cjxtMbglService.removeById(po.getId());
								cjxtMbglPzService.remove(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId,po.getId()));
							}
							msg = "【" + orgName + "】不存在, 请修改或者删除后重新导入!";
							return Result.error(msg);
						}
					}
					if(receiverOrgId != null && receiverOrgId.length()>0){
						cjxtTaskImport.setReceiverOrgId(receiverOrgId.substring(0,receiverOrgId.length()-1));
						cjxtTaskImport.setReceiverOrgCode(receiverOrgCode.substring(0,receiverOrgCode.length()-1));
						cjxtTaskImport.setReceiverOrgName(receiverOrgName.substring(0,receiverOrgName.length()-1));
					}
					if(recBmfzrId != null && recBmfzrId.length()>0){
						cjxtTaskImport.setReceiverBmfzrId(recBmfzrId.substring(0,recBmfzrId.length()-1));
						cjxtTaskImport.setReceiverBmfzrZh(recBmfzrZh.substring(0,recBmfzrZh.length()-1));
						cjxtTaskImport.setReceiverBmfzrName(recBmfzrName.substring(0,recBmfzrName.length()-1));
						cjxtTaskImport.setReceiverId(recBmfzrId.substring(0,recBmfzrId.length()-1));
						cjxtTaskImport.setReceiverZh(recBmfzrZh.substring(0,recBmfzrZh.length()-1));
						cjxtTaskImport.setReceiverName(recBmfzrName.substring(0,recBmfzrName.length()-1));
					}
					cjxtTaskImport.setCjZs(cjzs);
					cjxtTaskImport.setCjYwc(0);
					cjxtTaskImport.setCjSy(cjzs);
					cjxtTaskImport.setCjWcqk("0%");
					cjxtTaskImport.setRwzt("2");
					cjxtTaskImport.setDueDate(cjxtTaskImport.getDueDate());
					cjxtTaskImportService.addCjxtTask(cjxtTaskImport);

					//创建接受部门派发任务
					for(int i=0;i<receiverOrgNameS.length;i++){
						String recOrgBmfzrId= "";
						String recOrgBmfzrZh= "";
						String recOrgBmfzrName= "";
						int recOrgCjzs = 0;
						String receiverorgname = receiverOrgNameS[i];
						//接收部门部门负责人
						List<CjxtBmfzr> bmfzrList = cjxtBmfzrService.list(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmmc,receiverorgname));
						for(CjxtBmfzr cjxtBmfzr: bmfzrList){
							recOrgBmfzrId += cjxtBmfzr.getFzryId()+",";
							recOrgBmfzrZh += cjxtBmfzr.getLxdh()+",";
							recOrgBmfzrName += cjxtBmfzr.getFzryName()+",";
						}
						//接收部门信息
						SysDepart orgDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getDepartNameFull,receiverorgname));
						//接收部门采集数量
						List<CjxtStandardAddress> addressList = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().likeRight(CjxtStandardAddress::getAddressIdMz,orgDepart.getId()));
						recOrgCjzs = addressList.size();

						uuid = UUID.randomUUID().toString().replace("-","");//创建新uuid
						CjxtTaskImport cjxtTaskChild = new CjxtTaskImport();
						cjxtTaskChild.setId(uuid);
						cjxtTaskChild.setPid(cjxtTaskImport.getId());
						cjxtTaskChild.setHasChild("0");
						cjxtTaskChild.setTaskCode(cjxtTaskImport.getId());
						cjxtTaskChild.setTaskName(cjxtTaskImport.getTaskName());
						cjxtTaskChild.setTaskDescription(cjxtTaskImport.getTaskDescription());
						cjxtTaskChild.setMbId(po.getId());
						cjxtTaskChild.setMbCode(po.getMbbh());
						cjxtTaskChild.setMbName(po.getMbname());
						cjxtTaskChild.setBm(po.getBm());
						cjxtTaskChild.setOrgId(orgDepart.getId());
						cjxtTaskChild.setOrgCode(orgDepart.getOrgCode());
						cjxtTaskChild.setOrgName(orgDepart.getDepartNameFull());
						cjxtTaskChild.setDispatcherOrgId(sysDepart.getId());
						cjxtTaskChild.setDispatcherOrgCode(sysDepart.getOrgCode());
						cjxtTaskChild.setDispatcherOrgName(sysDepart.getDepartNameFull());
						cjxtTaskChild.setDispatcherId(sysUser.getId());
						cjxtTaskChild.setDispatcherName(sysUser.getRealname());
						cjxtTaskChild.setReceiverOrgId(orgDepart.getId());
						cjxtTaskChild.setReceiverOrgCode(orgDepart.getOrgCode());
						cjxtTaskChild.setReceiverOrgName(orgDepart.getDepartNameFull());
						if(recOrgBmfzrId != null && recOrgBmfzrId.length()>0){
							cjxtTaskChild.setReceiverBmfzrId(recOrgBmfzrId.substring(0,recOrgBmfzrId.length()-1));
							cjxtTaskChild.setReceiverBmfzrZh(recOrgBmfzrZh.substring(0,recOrgBmfzrZh.length()-1));
							cjxtTaskChild.setReceiverBmfzrName(recOrgBmfzrName.substring(0,recOrgBmfzrName.length()-1));
							cjxtTaskChild.setReceiverId(recOrgBmfzrId.substring(0,recOrgBmfzrId.length()-1));
							cjxtTaskChild.setReceiverZh(recOrgBmfzrZh.substring(0,recOrgBmfzrZh.length()-1));
							cjxtTaskChild.setReceiverName(recOrgBmfzrName.substring(0,recOrgBmfzrName.length()-1));
						}
						cjxtTaskChild.setRwzt("1");
						cjxtTaskChild.setCjZs(recOrgCjzs);
						cjxtTaskChild.setCjYwc(0);
						cjxtTaskChild.setCjSy(recOrgCjzs);
						cjxtTaskChild.setCjWcqk("0%");
						cjxtTaskChild.setRwzt("1");
						cjxtTaskChild.setDueDate(cjxtTaskImport.getDueDate());
						cjxtTaskImportService.addCjxtTask(cjxtTaskChild);

						if(cjxtTaskChild.getReceiverBmfzrZh()!=null && cjxtTaskChild.getReceiverZh()!=null && cjxtTaskChild.getReceiverBmfzrZh().equals(cjxtTaskChild.getReceiverZh())){
							String[] bmfzrZhs = cjxtTaskChild.getReceiverBmfzrZh().split(",");
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + cjxtTaskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + cjxtTaskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);
							}
						}
						if(cjxtTaskChild.getReceiverBmfzrZh()!=null && cjxtTaskChild.getReceiverZh()!=null && !cjxtTaskChild.getReceiverBmfzrZh().equals(cjxtTaskChild.getReceiverZh())){
							String[] bmfzrZhs = cjxtTaskChild.getReceiverBmfzrZh().split(",");
							for(int k=0;k<bmfzrZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + cjxtTaskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), bmfzrZhs[k], "任务派发【" + cjxtTaskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);
							}

							String[] receiverZhs = cjxtTaskChild.getReceiverZh().split(",");
							for(int k=0;k<receiverZhs.length;k++){
								//发送系统消息给 部门负责人
								ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
								String title = sysDepart.getDepartNameFull() + sysUser.getRealname() + "派发了采集任务【" + cjxtTaskChild.getTaskName() + "】给您，请您尽快处理!";
								MessageDTO messageDTO = new MessageDTO(sysUser.getRealname(), receiverZhs[k], "任务派发【" + cjxtTaskChild.getTaskName() + "】", title);
								sysBaseApi.sendSysAnnouncement(messageDTO);
							}
						}
					}
				}
				return Result.ok("文件导入成功！");
			} catch (Exception e) {
				msg = e.getMessage();
				log.error(msg, e);
				if(msg!=null && msg.indexOf("Duplicate entry")>=0){
					return Result.error("文件导入失败:有重复数据！");
				}else{
					return Result.error("文件导入失败:" + e.getMessage());
				}
			}
		}
		return Result.error("文件导入失败！");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
//    @RequiresPermissions("cjxt:cjxt_score_detail:importExcel")
	@RequestMapping(value = "/importExcel2", method = RequestMethod.POST)
	public Result<CjxtTask> importExcel2(HttpServletRequest request, HttpServletResponse response) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// 获取上传文件对象
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<CjxtDataReentry> list = ExcelImportUtil.importExcel(file.getInputStream(), CjxtDataReentry.class, params);
				ArrayList<CjxtTask> cjxtTasks = new ArrayList<>();
				if(list.size() !=0 ){
					list.forEach(item ->{
						CjxtDataReentry byId = cjxtDataReentryService.getById(item.getId());
						if(byId == null){
							cjxtDataReentryService.save(item);
						}
						String updateSql = "UPDATE "+byId.getBm()+" SET blzt = '1' WHERE del_flag = '0' AND id = '" + byId.getDataId() +"' ;";
						jdbcTemplate.update(updateSql);
						CjxtTask cjxtTask = new CjxtTask();
						item.setReceiverId(null);
						item.setReceiverName(null);
						BeanUtils.copyProperties(item,cjxtTask);
						SysDepart dl = sysDepartService.getById(sysUser.getOrgId());
						cjxtTask.setOrgCode(dl.getOrgCode());
						cjxtTask.setOrgName(dl.getDepartName());
						cjxtTask.setOrgId(dl.getId());

						cjxtTask.setDispatcherOrgCode(dl.getOrgCode());
						cjxtTask.setDispatcherOrgName(dl.getDepartName());
						cjxtTask.setDispatcherOrgId(dl.getId());

						CjxtStandardAddress addressOne = cjxtStandardAddressService.getById(cjxtTask.getAddressId());
						if(addressOne!=null){
							cjxtTask.setAddressCode(addressOne.getAddressCodeMz());
						}
//						 cjxtTask.setPid("0");
//						 cjxtTask.setHasChild("0");
						cjxtTask.setRwzt("1");
						cjxtTask.setCjZs(1);
						cjxtTask.setCjYwc(0);
						cjxtTask.setCjSy(1);
						cjxtTask.setCjWcqk("0%");
						cjxtTask.setChzt("1");
						cjxtTask.setBlId(item.getId());
						cjxtTasks.add(cjxtTask);
					});
				}
				long start = System.currentTimeMillis();
				log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
				return Result.ok(cjxtTasks.get(0));
			} catch (Exception e) {
				String msg = e.getMessage();
				log.error(msg, e);
				if(msg!=null && msg.indexOf("Duplicate entry")>=0){
					return Result.error("文件导入失败:有重复数据！");
				}else{
					return Result.error("文件导入失败:" + e.getMessage());
				}
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("文件导入失败！");
	}

	@RequestMapping(value = "/exportXlsTemplate")
	public ResponseEntity<InputStreamResource> exportTemplateXls() throws IOException {
		// 1 指定文件路径
		ClassPathResource file = new ClassPathResource("static/bigscreen/template1/任务派发导入模板.xls");

		// 2 设置响应类型
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8.toString())));
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		// 3 返回包含文件的ResponseEntity
		return ResponseEntity
				.ok()
				.headers(headers)
				.contentLength(file.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(new InputStreamResource(file.getInputStream()));
	}

	private String mapDbType(String type) {
		switch (type) {
			case "string":
				return "VARCHAR";
			case "int":
				return "INT";
			case "double":
				return "DOUBLE";
			case "Date":
				return "DATE";
			case "Datetime":
				return "DATETIME";
			case "BigDeicmal":
				return "DECIMAL(10,2)";
			case "Text":
				return "TEXT";
			case "Blob":
				return "BLOB";
			default:
				return "VARCHAR";
		}
	}

	/**
	 *
	 * @return
	 */
	@ApiOperation(value="统计分析-人口统计", notes="统计分析-人口统计")
	@GetMapping(value = "/appChart")
	public Result<Map<String, Object>> appChart(HttpServletRequest req,
			@RequestParam(required = true, name="userId") String userId,
			@RequestParam(required = true, name="flag") String flag
														) {
		Map<String, Object> result = new HashMap<>();
		//获取当前用户
		SysUser sysUser = sysUserService.getById(userId);
		//获取部门信息
		SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));

		//			 网格员	1
		//			 民警	2
		//			 辅警	3
		//			 社区工作人员	4
		//			 街办工作人员	5
		//			 委办局工作人员	6
		//			 派出所工作人员	7
		//			 分局工作人员	8
		//			 市局工作人员	9
		String userSf = sysUser.getUserSf();

		String sql = "";
		List<Map<String, Object>> dataMapList = null;
		String tableName = "";

		if("1".equals(flag)){//管辖区域--房屋
			tableName = "cjxt_fwcj";
		}else if("2".equals(flag)){//管辖区域--人员
			tableName = "cjxt_rkcj";
		}else if("3".equals(flag)){//管辖区域--单位
			tableName = "cjxt_dwcj";
		}else{
			return Result.OK(null);
		}

		if("1".equals(userSf)){//网格员
			sql = "select d.depart_name_full zname,count(*) znum from "+ tableName +" f,sys_depart d \n" +
					"where f.del_flag = '0' AND d.org_code = f.sys_org_code and d.id in (select dt.dep_id from sys_user_depart dt where dt.user_id='"+ userId +"')\n" +
					"group by d.depart_name_full";
		}else if("2".equals(userSf) || "3".equals(userSf)){//民警、辅警
			sql = "select d.depart_name_full zname,count(*) znum from cjxt_pjwgqx p, "+ tableName +" f,sys_depart d \n" +
					"where f.del_flag = '0' AND p.wg_code = f.sys_org_code and d.org_code = f.sys_org_code  and p.pj_id='"+ userId +"'\n" +
					"group by d.depart_name_full";
		}else if("4".equals(userSf) || "5".equals(userSf)){//社区工作人员、街办工作人员
			sql = "select d.depart_name_full zname,count(*) znum from "+ tableName +" f,sys_depart d \n" +
					"where f.del_flag = '0' AND d.org_code = f.sys_org_code and f.sys_org_code like '"+sysUser.getOrgCode()+"%'\n" +
					"group by d.depart_name_full";
		}else if("6".equals(userSf)){//委办局工作人员
			sql = "";
		}else if("7".equals(userSf) || "8".equals(userSf) || "9".equals(userSf)){//派出所工作人员、分局工作人员、市局工作人员
			sql = "";
		}
		dataMapList = jdbcTemplate.queryForList(sql);

		List<Object> resultListName = new ArrayList<Object>();
		List<Object> resultListVal = new ArrayList<Object>();
		for(int i=0;i<dataMapList.size();i++){
			Map<String, Object> dataMap = dataMapList.get(i);
			resultListName.add(dataMap.get("zname"));
			resultListVal.add(dataMap.get("znum"));
		}
		result.put("zname",resultListName);
		result.put("znum",resultListVal);
		return Result.OK(result);
	}


	@ApiOperation(value="统计分析-人口年龄分部", notes="人口年龄分部")
	@GetMapping(value = "/appRkAgeChart")
	public Result<Map<String, Object>> appRkAgeChart(HttpServletRequest req,
												@RequestParam(required = true, name="userId") String userId) {
		Map<String, Object> result = new HashMap<>();
		//获取当前用户
		SysUser sysUser = sysUserService.getById(userId);
		//获取部门信息
		SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));

		//			 网格员	1
		//			 民警	2
		//			 辅警	3
		//			 社区工作人员	4
		//			 街办工作人员	5
		//			 委办局工作人员	6
		//			 派出所工作人员	7
		//			 分局工作人员	8
		//			 市局工作人员	9
		String userSf = sysUser.getUserSf();

		String sql = "";
		List<Map<String, Object>> dataMapList = null;
		String tableName = "cjxt_rkcj";

		if("1".equals(userSf)){//网格员
			sql = "SELECT\n" +
					"    CASE\n" +
					"        WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 0 AND 14 THEN '0-14岁'\n" +
					"        WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 15 AND 29 THEN '15-29岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 30 AND 44 THEN '30-44岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 45 AND 59 THEN '45-59岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 60 AND 999 THEN '60岁及以上'\n" +
					"        ELSE '其他'\n" +
					"    END AS census,\n" +
					"    COUNT(*) AS ageRange\n" +
					"FROM "+ tableName +" f,sys_depart d \n" +
					"where f.del_flag = '0' and d.org_code = f.sys_org_code and d.id in (select dt.dep_id from sys_user_depart dt where dt.user_id='"+ userId +"')\n" +
					"group by census";
		}else if("2".equals(userSf) || "3".equals(userSf)){//民警、辅警
			sql = "SELECT\n" +
					"    CASE\n" +
					"        WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 0 AND 14 THEN '0-14岁'\n" +
					"        WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 15 AND 29 THEN '15-29岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 30 AND 44 THEN '30-44岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 45 AND 59 THEN '45-59岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 60 AND 999 THEN '60岁及以上'\n" +
					"        ELSE '其他'\n" +
					"    END AS census,\n" +
					"    COUNT(*) AS ageRange\n" +
					"FROM cjxt_pjwgqx p, "+ tableName +" f,sys_depart d \n" +
					"where f.del_flag = '0' and p.wg_code = f.sys_org_code and d.org_code = f.sys_org_code  and p.pj_id='"+ userId +"'\n" +
					"group by census";
		}else if("4".equals(userSf) || "5".equals(userSf)){//社区工作人员、街办工作人员
			sql = "SELECT\n" +
					"    CASE\n" +
					"        WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 0 AND 14 THEN '0-14岁'\n" +
					"        WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 15 AND 29 THEN '15-29岁'\n" +
					" 		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 30 AND 44 THEN '30-44岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 45 AND 59 THEN '45-59岁'\n" +
					"		 WHEN (YEAR(NOW()) - YEAR(f.rycsrq) - 1) + (DATE_FORMAT(f.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 60 AND 999 THEN '60岁及以上'\n" +
					"        ELSE '其他'\n" +
					"    END AS census,\n" +
					"    COUNT(*) AS ageRange\n" +
					"FROM "+ tableName +" f,sys_depart d \n" +
					"where f.del_flag = '0' and d.org_code = f.sys_org_code and f.sys_org_code like '"+sysUser.getOrgCode()+"%'\n" +
					"group by census";
		}else if("6".equals(userSf)){//委办局工作人员
			sql = "";
		}else if("7".equals(userSf) || "8".equals(userSf) || "9".equals(userSf)){//派出所工作人员、分局工作人员、市局工作人员
			sql = "";
		}
		dataMapList = jdbcTemplate.queryForList(sql);

		List<Object> resultListCensus = new ArrayList<Object>();
		List<Object> resultListAgeRange = new ArrayList<Object>();
		for(int i=0;i<dataMapList.size();i++){
			Map<String, Object> dataMap = dataMapList.get(i);
			resultListCensus.add(dataMap.get("census"));
			resultListAgeRange.add(dataMap.get("ageRange"));
		}
		result.put("census",resultListCensus);
		result.put("ageRange",resultListAgeRange);
		return Result.OK(result);
	}

	@ApiOperation(value="统计分析-房屋类别", notes="房屋类别")
	@GetMapping(value = "/fwlbChart")
	public Result<Map<String, Object>> fwlbChart(HttpServletRequest req,
													 @RequestParam(required = true, name="userId") String userId) {
		Map<String, Object> result = new HashMap<>();
		//获取当前用户
		SysUser sysUser = sysUserService.getById(userId);
		//获取部门信息
		SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));

		//返回结果
		List<Object> resultListFwlb = new ArrayList<Object>();
		List<Object> resultListFwlbList = new ArrayList<Object>();

		SysDict sysDict = sysDictService.getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode,"fwlb"));
		if(sysDict==null){
			result.put("fwlb",resultListFwlb);
			result.put("fwlbNum",resultListFwlbList);
			return Result.OK(result);
		}

		//			 网格员	1
		//			 民警	2
		//			 辅警	3
		//			 社区工作人员	4
		//			 街办工作人员	5
		//			 委办局工作人员	6
		//			 派出所工作人员	7
		//			 分局工作人员	8
		//			 市局工作人员	9
		String userSf = sysUser.getUserSf();

		String sql = "";
		List<Map<String, Object>> dataMapList = null;
		String tableName = "cjxt_fwcj";

		if("1".equals(userSf)){//网格员
			sql = "SELECT sd.item_text as fwlb,COUNT(*) fwlbNum " +
					"FROM "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' AND f.ssfwlb = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and d.org_code = f.sys_org_code and d.id in (select dt.dep_id from sys_user_depart dt where dt.user_id='"+ userId +"')\n" +
					"group by fwlb";
		}else if("2".equals(userSf) || "3".equals(userSf)){//民警、辅警
			sql = "SELECT sd.item_text as fwlb,COUNT(*) fwlbNum " +
					"FROM cjxt_pjwgqx p, "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' AND f.ssfwlb = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and p.wg_code = f.sys_org_code and d.org_code = f.sys_org_code  and p.pj_id='"+ userId +"'\n" +
					"group by fwlb";
		}else if("4".equals(userSf) || "5".equals(userSf)){//社区工作人员、街办工作人员
			sql = "SELECT sd.item_text as fwlb,COUNT(*) fwlbNum " +
					"FROM "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' AND f.ssfwlb = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and d.org_code = f.sys_org_code and f.sys_org_code like '"+sysUser.getOrgCode()+"%'\n" +
					"group by fwlb";
		}else if("6".equals(userSf)){//委办局工作人员
			sql = "";
		}else if("7".equals(userSf) || "8".equals(userSf) || "9".equals(userSf)){//派出所工作人员、分局工作人员、市局工作人员
			sql = "";
		}
		dataMapList = jdbcTemplate.queryForList(sql);

		for(int i=0;i<dataMapList.size();i++){
			Map<String, Object> dataMap = dataMapList.get(i);
			resultListFwlb.add(dataMap.get("fwlb"));
			resultListFwlbList.add(dataMap.get("fwlbNum"));
		}
		result.put("fwlb",resultListFwlb);
		result.put("fwlbNum",resultListFwlbList);
		return Result.OK(result);
	}

	@ApiOperation(value="统计分析-是否出租房", notes="是否出租房")
	@GetMapping(value = "/sfczChart")
	public Result<Map<String, Object>> sfczChart(HttpServletRequest req,
												   @RequestParam(required = true, name="userId") String userId) {
		Map<String, Object> result = new HashMap<>();
		//获取当前用户
		SysUser sysUser = sysUserService.getById(userId);
		//获取部门信息
		SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
		//返回结果
		List<Object> resultListSfcz = new ArrayList<Object>();
		List<Object> resultListSfczList = new ArrayList<Object>();

		SysDict sysDict = sysDictService.getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode,"yn"));
		if(sysDict==null){
			result.put("sfcz",resultListSfcz);
			result.put("sfczNum",resultListSfczList);
			return Result.OK(result);
		}

		//			 网格员	1
		//			 民警	2
		//			 辅警	3
		//			 社区工作人员	4
		//			 街办工作人员	5
		//			 委办局工作人员	6
		//			 派出所工作人员	7
		//			 分局工作人员	8
		//			 市局工作人员	9
		String userSf = sysUser.getUserSf();

		String sql = "";
		List<Map<String, Object>> dataMapList = null;
		String tableName = "cjxt_fwcj";

		if("1".equals(userSf)){//网格员
			sql = "SELECT sd.item_text as sfcz,COUNT(*) sfczNum " +
					"FROM "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' AND f.isczf = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and d.org_code = f.sys_org_code and d.id in (select dt.dep_id from sys_user_depart dt where dt.user_id='"+ userId +"')\n" +
					"group by sfcz";
		}else if("2".equals(userSf) || "3".equals(userSf)){//民警、辅警
			sql = "SELECT sd.item_text as sfcz,COUNT(*) sfczNum " +
					"FROM cjxt_pjwgqx p, "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' AND f.isczf = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and p.wg_code = f.sys_org_code and d.org_code = f.sys_org_code  and p.pj_id='"+ userId +"'\n" +
					"group by sfcz";
		}else if("4".equals(userSf) || "5".equals(userSf)){//社区工作人员、街办工作人员
			sql = "SELECT sd.item_text as sfcz,COUNT(*) sfczNum " +
					"FROM "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' AND f.isczf = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and d.org_code = f.sys_org_code and f.sys_org_code like '"+sysUser.getOrgCode()+"%'\n" +
					"group by sfcz";
		}else if("6".equals(userSf)){//委办局工作人员
			sql = "";
		}else if("7".equals(userSf) || "8".equals(userSf) || "9".equals(userSf)){//派出所工作人员、分局工作人员、市局工作人员
			sql = "";
		}
		dataMapList = jdbcTemplate.queryForList(sql);


		for(int i=0;i<dataMapList.size();i++){
			Map<String, Object> dataMap = dataMapList.get(i);
			resultListSfcz.add(dataMap.get("sfcz"));
			resultListSfczList.add(dataMap.get("sfczNum"));
		}
		result.put("sfcz",resultListSfcz);
		result.put("sfczNum",resultListSfczList);
		return Result.OK(result);
	}

	@ApiOperation(value="统计分析-单位性质", notes="单位性质")
	@GetMapping(value = "/dwNatChart")
	public Result<Map<String, Object>> dwNatChart(HttpServletRequest req,
												   @RequestParam(required = true, name="userId") String userId) {
		Map<String, Object> result = new HashMap<>();
		//获取当前用户
		SysUser sysUser = sysUserService.getById(userId);
		//获取部门信息
		SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));

		//返回结果
		List<Object> resultListDwxz = new ArrayList<Object>();
		List<Object> resultListDwxzList = new ArrayList<Object>();

		SysDict sysDict = sysDictService.getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode,"dwxz"));
		if(sysDict==null){
			result.put("dwxz",resultListDwxz);
			result.put("dwxzNum",resultListDwxzList);
			return Result.OK(result);
		}

		//			 网格员	1
		//			 民警	2
		//			 辅警	3
		//			 社区工作人员	4
		//			 街办工作人员	5
		//			 委办局工作人员	6
		//			 派出所工作人员	7
		//			 分局工作人员	8
		//			 市局工作人员	9
		String userSf = sysUser.getUserSf();

		String sql = "";
		List<Map<String, Object>> dataMapList = null;
		String tableName = "cjxt_dwcj";

		if("1".equals(userSf)){//网格员
			sql = "SELECT sd.item_text as dwxz,COUNT(*) dwxzNum " +
					"FROM "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' and f.dwxz = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and d.org_code = f.sys_org_code and d.id in (select dt.dep_id from sys_user_depart dt where dt.user_id='"+ userId +"')\n" +
					"group by sd.item_text";
		}else if("2".equals(userSf) || "3".equals(userSf)){//民警、辅警
			sql = "SELECT sd.item_text as dwxz,COUNT(*) dwxzNum " +
					"FROM cjxt_pjwgqx p, "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' and f.dwxz = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and p.wg_code = f.sys_org_code and d.org_code = f.sys_org_code  and p.pj_id='"+ userId +"'\n" +
					"group by sd.item_text";
		}else if("4".equals(userSf) || "5".equals(userSf)){//社区工作人员、街办工作人员
			sql = "SELECT sd.item_text as dwxz,COUNT(*) dwxzNum " +
					"FROM "+ tableName +" f,sys_depart d,sys_dict_item sd \n" +
					"where f.del_flag = '0' and f.dwxz = sd.item_value AND sd.dict_id = '"+sysDict.getId()+"' and d.org_code = f.sys_org_code and f.sys_org_code like '"+sysUser.getOrgCode()+"%'\n" +
					"group by sd.item_text";
		}else if("6".equals(userSf)){//委办局工作人员
			sql = "";
		}else if("7".equals(userSf) || "8".equals(userSf) || "9".equals(userSf)){//派出所工作人员、分局工作人员、市局工作人员
			sql = "";
		}
		dataMapList = jdbcTemplate.queryForList(sql);

		for(int i=0;i<dataMapList.size();i++){
			Map<String, Object> dataMap = dataMapList.get(i);
			resultListDwxz.add(dataMap.get("dwxz"));
			resultListDwxzList.add(dataMap.get("dwxzNum"));
		}
		result.put("dwxz",resultListDwxz);
		result.put("dwxzNum",resultListDwxzList);
		return Result.OK(result);
	}

}
