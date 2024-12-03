package org.jeecg.modules.demo.cjxt.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbgl;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtMbglService;
import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;
import org.jeecg.modules.message.service.ISysMessageService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 发送消息任务
 * @author: jeecg-boot
 */

@Slf4j
public class CstxSendMsgJob implements Job {

	@Autowired
	private ISysMessageService sysMessageService;
	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Autowired
	private ICjxtXtcsService cjxtXtcsService;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		//查询系统参数（超时天数设置）
		QueryWrapper<CjxtXtcs> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("cs_key", "csts");
		CjxtXtcs cjxtXtcs = cjxtXtcsService.getOne(queryWrapper);
		String csts = cjxtXtcs.getCsVal();
		//查询审核超时天数
		queryWrapper.clear();
		queryWrapper.eq("cs_key", "sjshCsts");
		CjxtXtcs cjxtXtcs1 = cjxtXtcsService.getOne(queryWrapper);
		String sjshCsts = cjxtXtcs1.getCsVal();
		String auditDataSql = "select * from cjxt_data_audit where case when update_time is not null then update_time else create_time end < date_sub(now(),interval "+sjshCsts+" day)";;
		List<Map<String, Object>> auditResultList = jdbcTemplate.queryForList(auditDataSql);
		if(auditResultList.size() > 0){
			//手机短信发送
//					try {
//						JSONObject obj = new JSONObject();
//						obj.put("name", sysUser.getRealname());
//						obj.put("gdh", jsjbQzsq.getGdh());
//
//						boolean cl = DySmsHelper.sendSms(clrphone, obj, DySmsEnum.YYGA_PF); //处理人
//					} catch (ClientException e) {
//						e.printStackTrace();
//					}
			//发送系统消息给处理人
			SysUser sysUser = sysUserService.getById((String) auditResultList.get(0).get("audit_person_id"));
			ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
			String title = "您当前有【"+auditResultList.size() + "】条数据已超过【"+sjshCsts+"】天审核周期，请您尽快审核!";
			MessageDTO messageDTO = new MessageDTO("系统提醒", sysUser.getUsername(), "审核提醒", title);
			sysBaseApi.sendSysAnnouncement(messageDTO);
		}
		//查询所有模板
		QueryWrapper<CjxtMbgl> queryWrapperMbgl = new QueryWrapper<>();
		queryWrapperMbgl.eq("is_db","1");
		List<CjxtMbgl> cjxtMbglList = cjxtMbglService.list(queryWrapperMbgl);
		for (CjxtMbgl cjxtMbglData:cjxtMbglList ) {
			//查询过期数据
			if(cjxtMbglData.getBm() != null){
				String dataSql = "select * from "+cjxtMbglData.getBm()+" where case when update_time is not null then update_time else create_time end < date_sub(now(),interval "+csts+" day)";;
				List<Map<String, Object>> resultList = jdbcTemplate.queryForList(dataSql);
				if(resultList.size() > 0){
					//手机短信发送
//					try {
//						JSONObject obj = new JSONObject();
//						obj.put("name", sysUser.getRealname());
//						obj.put("gdh", jsjbQzsq.getGdh());
//
//						boolean cl = DySmsHelper.sendSms(clrphone, obj, DySmsEnum.YYGA_PF); //处理人
//					} catch (ClientException e) {
//						e.printStackTrace();
//					}
					//发送系统消息给处理人
					ISysBaseAPI sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
					String title = "模板【"+cjxtMbglData.getMbname()+"】下有【"+resultList.size() + "】条数据已超过数据更新时间【"+csts+"】天，请您尽快更新数据!";
					String userId = (String)resultList.get(0).get("create_by");
					if(userId != null && !"".equals(userId) && !"null".equals(userId)){
						MessageDTO messageDTO = new MessageDTO("系统提醒", userId, "数据更新提醒【"+cjxtMbglData.getMbname()+ "】", title);
						sysBaseApi.sendSysAnnouncement(messageDTO);
					}
				}
			}
		}
	}

}
