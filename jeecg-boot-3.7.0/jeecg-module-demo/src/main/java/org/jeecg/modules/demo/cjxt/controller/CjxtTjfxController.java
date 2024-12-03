package org.jeecg.modules.demo.cjxt.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbgl;
import org.jeecg.modules.demo.cjxt.entity.CjxtPjwgqx;
import org.jeecg.modules.demo.cjxt.service.ICjxtMbglService;
import org.jeecg.modules.demo.cjxt.service.ICjxtPjwgqxService;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(tags="统计分析")
@RestController
@RequestMapping("/cjxt/cjxTjfx")
@Slf4j
public class CjxtTjfxController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ICjxtMbglService cjxtMbglService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ICjxtPjwgqxService cjxtPjwgqxService;
    @Autowired
    private ISysDictService sysDictService;
    @Autowired
    private WebSocket webSocket;
    @Value(value = "${jeecg.minio.minio_url}")
    private String minioUrl;
    @Value(value = "${jeecg.minio.bucketName}")
    private String bucketName;

    @AutoLog(value = "统计分析-总数统计")
    @ApiOperation(value="统计分析-总数统计", notes="统计分析-总数统计")
    @GetMapping(value = "/zsTj")
    public Result<Map<String, Object>> zsTj(@RequestParam(name="userId",required=false) String userId) {
        Map<String, Object> result = new HashMap<>();

        StringBuilder orgCodeBuilder = new StringBuilder();
        StringBuilder sysDepartCode = new StringBuilder();
        SysUser sysUser = sysUserService.getById(userId);
        List<String> orgCodes = new ArrayList<>();
        List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
        List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
        if(sysUser!=null){
            if(sysDepartsList.size()>0){
                for(int j = 0;j<sysDepartsList.size();j++){
                    SysDepart sysDepart = sysDepartsList.get(j);
                    if (j > 0) {
                        sysDepartCode.append(",");
                    }
                    sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                }
            }
            if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
                if(sysDepartCode.length()>0){
                    StringBuilder newSysDepartCode = new StringBuilder();
                    newSysDepartCode.append("(").append(sysDepartCode).append(")");
                    orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
                    for (int i = 0; i < orgCodes.size(); i++) {
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                    }
                }
            }
            if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
                pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
                if(pjwgqxList.size()>0){
                    for(int i=0;i<pjwgqxList.size();i++){
                        CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                    }
                }
            }
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
        }
        String orgCode = "";
        if(orgCodes.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else if(pjwgqxList.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else {
            if(sysDepartsList.size()>0){
                orgCode = sysDepartCode.toString();
            }
        }
        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(!"".equals(orgCode)){
            orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
        }

        for(int i=0;i<3;i++){
            if(i==0){
                //房屋
                CjxtMbgl fwlb = null;
                StringBuilder fwSql = new StringBuilder();
                List<CjxtMbgl> fwList = cjxtMbglService.list(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getIsDb,"1").eq(CjxtMbgl::getMblx,"1"));
                if(fwList.size()>0){
                    fwSql.append("SELECT sum(cnt) AS fwCount FROM ( ");
                    int k=1;
                    for(int f=0;f<fwList.size();f++){
                        if(f==k){
                            k++;
                            fwSql.append(" UNION ALL ");
                        }
                        CjxtMbgl fw = fwList.get(f);
                        if("FW001".equals(fw.getMbbh())){
                            fwlb = fw;
                        }
                        fwSql.append(" SELECT count(*) AS cnt FROM " +fw.getBm()+ " t WHERE t.del_flag = '0' ").append(orgCodeQuery);
                    }
                    fwSql.append(" ) AS fwmb");
                }
                if(fwSql.length()>0){
                    int fw = jdbcTemplate.queryForObject(fwSql.toString(), Integer.class);
                    result.put("syfw",fw);
                }else {
                    result.put("syfw",0);
                }

                //计算房屋类别
                if(fwlb!=null){
                    String isczfSql = " SELECT count(*) FROM " + fwlb.getBm() + " t WHERE t.del_flag = '0' AND t.isczf = '1' " + orgCodeQuery;
                    int isczf = jdbcTemplate.queryForObject(isczfSql, Integer.class);
                    result.put("isczf",isczf);
                    String notIsczfSql = " SELECT count(*) FROM " + fwlb.getBm() + " t WHERE t.del_flag = '0' AND t.isczf = '0' " + orgCodeQuery;
                    int notIsczf = jdbcTemplate.queryForObject(notIsczfSql, Integer.class);
                    result.put("notIsczf",notIsczf);
                }else {
                    result.put("isczf",0);
                    result.put("notIsczf",0);
                }
            }
            if(i==1){
                //人员
                CjxtMbgl rklb = null;
                StringBuilder rySql = new StringBuilder();
                List<CjxtMbgl> ryList = cjxtMbglService.list(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getIsDb,"1").eq(CjxtMbgl::getMbbh,"RY001"));
                if(ryList.size()>0){
                    rySql.append("SELECT sum(cnt) AS fwCount FROM ( ");
                    int k=1;
                    for(int f=0;f<ryList.size();f++){
                        if(f==k){
                            k++;
                            rySql.append(" UNION ALL ");
                        }
                        CjxtMbgl ry = ryList.get(f);
                        if("RY001".equals(ry.getMbbh())){
                            rklb = ry;
                        }
                        rySql.append(" SELECT count(*) AS cnt FROM " +ry.getBm()+ " t WHERE t.del_flag = '0' ").append(orgCodeQuery);
                    }
                    rySql.append(" ) AS fwmb");
                }
                if(rySql.length()>0){
                    int ry = jdbcTemplate.queryForObject(rySql.toString(), Integer.class);
                    result.put("syry",ry);
                }else {
                    result.put("syry",0);
                }
                if(rklb!=null){
                    String sql = "SELECT " +
                            "    SUM(CASE WHEN t.rklx = 1 OR t.rklx IS NULL OR t.rklx = '' THEN 1 ELSE 0 END) AS ldrk," +
                            "    SUM(CASE WHEN t.rklx = 2 THEN 1 ELSE 0 END) AS hjrk " +
                            "FROM "+rklb.getBm()+" t " +
                            "WHERE t.del_flag = '0' " + orgCodeQuery;
                    List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                    Map<String, Object> row = rows.get(0);
                    BigDecimal ldrk = (BigDecimal) row.get("ldrk");
                    BigDecimal hjrk = (BigDecimal) row.get("hjrk");
                    BigDecimal qt = (BigDecimal) row.get("qt");
                    result.put("ldrk",ldrk.intValue());
                    result.put("hjrk",hjrk.intValue());
                }else {
                    result.put("ldrk",0);
                    result.put("hjrk",0);
                }
            }
            if(i==2){
                //单位
                StringBuilder dwSql = new StringBuilder();
                StringBuilder dwAddressSql = new StringBuilder();
                List<CjxtMbgl> fwList = cjxtMbglService.list(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getIsDb,"1").eq(CjxtMbgl::getMblx,"3"));
                if(fwList.size()>0){
                    dwSql.append("SELECT sum(cnt) AS fwCount FROM ( ");
                    int k=1;
                    for(int f=0;f<fwList.size();f++){
                        if(f==k){
                            k++;
                            dwSql.append(" UNION ALL ");
                            dwAddressSql.append(" UNION ALL ");
                        }
                        CjxtMbgl fw = fwList.get(f);
                        dwAddressSql.append("SELECT t.address_id " +
                                "FROM "+fw.getBm()+" t " +
                                "WHERE t.del_flag = '0' AND t.address_id IS NOT NULL " + orgCodeQuery +
                                "GROUP BY t.address_id ");
                        dwSql.append(" SELECT count(*) AS cnt FROM " +fw.getBm()+ " t WHERE t.del_flag = '0' ").append(orgCodeQuery);
                    }
                    dwSql.append(" ) AS fwmb");
                }
                if(dwSql.length()>0){
                    int dw = jdbcTemplate.queryForObject(dwSql.toString(), Integer.class);
                    result.put("sydw",dw);
                }else {
                    result.put("sydw",0);
                }

                if(dwAddressSql.length()>0){
                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(dwAddressSql.toString());
                    StringBuilder addressBuilder = new StringBuilder();
                    if(resultList.size()>0){
                        for(Map<String, Object> row: resultList){
                            Object addressV = row.get("address_id");
                            addressBuilder.append(addressV+"','");
                        }

                        String addressId = addressBuilder.substring(0,addressBuilder.length()-2);

                        CjxtMbgl dwcyry = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,"RY002"));
                        if(dwcyry!=null && resultList.size()>0){
                            String cyrySql = " SELECT count(*) FROM " + dwcyry.getBm() + " t WHERE t.del_flag = '0' AND t.address_id in ('" + addressId + ")";
                            int cyryNum = jdbcTemplate.queryForObject(cyrySql, Integer.class);
                            result.put("cyryNum",cyryNum);
                        }
                    }else {
                        result.put("cyryNum",0);
                    }
                }else {
                    result.put("cyryNum",0);
                }
            }
        }
        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-积分排序")
    @ApiOperation(value="统计分析-积分排序", notes="统计分析-积分排序")
    @GetMapping(value = "/scorePx")
    public Result<Map<String, Object>> scorePx(@RequestParam(name="isYear",required=false) String isYear,
                                                       @RequestParam(name="isMonth",required=false) String isMonth,
                                                       @RequestParam(name="isWeek",required=false) String isWeek,
                                                       @RequestParam(name="isDay",required=false) String isDay,
                                                       @RequestParam(name="timeS",required=false) String timeS,
                                                       @RequestParam(name="timeE",required=false) String timeE,
                                                       HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        //获取时间
        Calendar calendar = Calendar.getInstance();
        // 获取当前年
        int currentYear = calendar.get(Calendar.YEAR);
        // 获取当前月份
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        //获取当前时间
        Date currentDate = calendar.getTime();


        // 获取当前日期的周一和周日
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStartDate = calendar.getTime();
        calendar.add(Calendar.DATE, 6);
        Date weekEndDate = calendar.getTime();

        // 将日期格式化为字符串
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //当前年 yyyy格式
        String formattedCurrentYear = String.format("%04d", currentYear);
        //当前月 yyyy-MM格式
        String currentYearMonth = String.format("%04d-%02d", currentYear, currentMonth);
        //当前周
        String formattedWeekStartDate = dateFormat.format(weekStartDate);
        String formattedWeekEndDate = dateFormat.format(weekEndDate);
        //当前日 yyyy-MM-dd格式
        String formattedCurrentDate = dateFormat.format(currentDate);


        List<Map<String, Object>> resultList = new ArrayList<>();
        //条件查询
        StringBuilder searchBuilder = new StringBuilder();
        //当前年
        if(!"".equals(isYear) && isYear!=null && !isYear.isEmpty()){
            searchBuilder.append(" AND t.create_time like '"+formattedCurrentYear+"%' ");
        }
        //当前月
        if(!"".equals(isMonth) && isMonth!=null && !isMonth.isEmpty()){
            searchBuilder.append(" AND t.create_time like '%"+currentYearMonth+"%' ");
        }
        //当前周
        if(!"".equals(isWeek) && isWeek!=null && !isWeek.isEmpty()){
            searchBuilder.append(" AND t.create_time BETWEEN '"+formattedWeekStartDate+"' AND '"+formattedWeekEndDate+"' ");
        }
        //当前日
        if(!"".equals(isDay) && isDay!=null && !isDay.isEmpty()){
            searchBuilder.append(" AND t.create_time like '"+formattedCurrentDate+"%'");
        }
        //区间查询
        if(!"".equals(timeS) && timeS!=null && !timeS.isEmpty() && !"".equals(timeE) && timeE!=null && !timeE.isEmpty()){
            searchBuilder.append(" AND t.create_time BETWEEN '"+timeS+"' AND '"+timeE+"' ");
        }
        String sql = "SELECT COUNT(*) AS jfNum, t.user_id, su.realname, sd.depart_name_full FROM cjxt_score_detail t \n" +
                "INNER JOIN sys_user su ON su.id = t.user_id\n" +
                "INNER JOIN sys_depart sd ON sd.org_code = su.org_code\n" +
                "WHERE t.del_flag = '0' "+searchBuilder+" GROUP BY t.user_id ORDER BY jfNum DESC";
        resultList = jdbcTemplate.queryForList(sql);
        result.put("data",resultList);

        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-房屋类别")
    @ApiOperation(value="统计分析-房屋类别", notes="统计分析-房屋类别")
    @GetMapping(value = "/fwlb")
    public Result<Map<String, Object>> fwlb(@RequestParam(name="userId",required=false) String userId,
                                               HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder orgCodeBuilder = new StringBuilder();
        StringBuilder sysDepartCode = new StringBuilder();
        SysUser sysUser = sysUserService.getById(userId);
        List<String> orgCodes = new ArrayList<>();
        List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
        List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
        if(sysUser!=null){
            if(sysDepartsList.size()>0){
                for(int j = 0;j<sysDepartsList.size();j++){
                    SysDepart sysDepart = sysDepartsList.get(j);
                    if (j > 0) {
                        sysDepartCode.append(",");
                    }
                    sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                }
            }
            if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
                if(sysDepartCode.length()>0){
                    StringBuilder newSysDepartCode = new StringBuilder();
                    newSysDepartCode.append("(").append(sysDepartCode).append(")");
                    orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
                    for (int i = 0; i < orgCodes.size(); i++) {
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                    }
                }
            }
            if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
                pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
                if(pjwgqxList.size()>0){
                    for(int i=0;i<pjwgqxList.size();i++){
                        CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                    }
                }
            }
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
        }
        String orgCode = "";
        if(orgCodes.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else if(pjwgqxList.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else {
            if(sysDepartsList.size()>0){
                orgCode = sysDepartCode.toString();
            }
        }
        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(!"".equals(orgCode)){
            orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
        }
        String sql = "SELECT COUNT(*) AS fwNum, t.ssfwlb, (SELECT item_text FROM sys_dict_item WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'fwlb') AND item_value = t.ssfwlb) AS fwText FROM cjxt_fwcj t WHERE t.del_flag = '0' AND t.ssfwlb IS NOT NULL "+orgCodeQuery+" GROUP BY t.ssfwlb";
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
        result.put("data",resultList);
        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-人口年龄")
    @ApiOperation(value="统计分析-人口年龄", notes="统计分析-人口年龄")
    @GetMapping(value = "/rknl")
    public Result<Map<String, Object>> rknl(@RequestParam(name="userId",required=false) String userId,
                                            HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder orgCodeBuilder = new StringBuilder();
        StringBuilder sysDepartCode = new StringBuilder();
        SysUser sysUser = sysUserService.getById(userId);
        List<String> orgCodes = new ArrayList<>();
        List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
        List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
        if(sysUser!=null){
            if(sysDepartsList.size()>0){
                for(int j = 0;j<sysDepartsList.size();j++){
                    SysDepart sysDepart = sysDepartsList.get(j);
                    if (j > 0) {
                        sysDepartCode.append(",");
                    }
                    sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                }
            }
            if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
                if(sysDepartCode.length()>0){
                    StringBuilder newSysDepartCode = new StringBuilder();
                    newSysDepartCode.append("(").append(sysDepartCode).append(")");
                    orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
                    for (int i = 0; i < orgCodes.size(); i++) {
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                    }
                }
            }
            if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
                pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
                if(pjwgqxList.size()>0){
                    for(int i=0;i<pjwgqxList.size();i++){
                        CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                    }
                }
            }
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
        }
        String orgCode = "";
        if(orgCodes.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else if(pjwgqxList.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else {
            if(sysDepartsList.size()>0){
                orgCode = sysDepartCode.toString();
            }
        }
        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(!"".equals(orgCode)){
            orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
        }
        List<Map<String, Object>> dataMapList = null;
        String tableName = "cjxt_rkcj";

        String sql = "SELECT\n" +
                "    CASE\n" +
                "        WHEN (YEAR(NOW()) - YEAR(t.rycsrq) - 1) + (DATE_FORMAT(t.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 0 AND 14 THEN '0-14岁'\n" +
                "        WHEN (YEAR(NOW()) - YEAR(t.rycsrq) - 1) + (DATE_FORMAT(t.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 15 AND 29 THEN '15-29岁'\n" +
                "		 WHEN (YEAR(NOW()) - YEAR(t.rycsrq) - 1) + (DATE_FORMAT(t.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 30 AND 44 THEN '30-44岁'\n" +
                "		 WHEN (YEAR(NOW()) - YEAR(t.rycsrq) - 1) + (DATE_FORMAT(t.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 45 AND 59 THEN '45-59岁'\n" +
                "		 WHEN (YEAR(NOW()) - YEAR(t.rycsrq) - 1) + (DATE_FORMAT(t.rycsrq, '%m%d') <= DATE_FORMAT(NOW(), '%m%d')) BETWEEN 60 AND 999 THEN '60岁及以上'\n" +
                "        ELSE '其他'\n" +
                "    END AS census,\n" +
                "    COUNT(*) AS ageRange\n" +
                "FROM "+ tableName +" t \n" +
                "where t.del_flag = '0' " + orgCodeQuery +
                "GROUP BY census " +
                "ORDER BY \n" +
                "    CASE census\n" +
                "         WHEN '0-14岁' THEN 1\n" +
                "         WHEN '15-29岁' THEN 2\n" +
                "         WHEN '30-44岁' THEN 3\n" +
                "         WHEN '45-59岁' THEN 4\n" +
                "         WHEN '60岁及以上' THEN 5\n" +
                "         ELSE 6\n" +
                "    END";
        dataMapList = jdbcTemplate.queryForList(sql);

//        List<Object> resultListCensus = new ArrayList<Object>();
//        List<Object> resultListAgeRange = new ArrayList<Object>();
//        for(int i=0;i<dataMapList.size();i++){
//            Map<String, Object> dataMap = dataMapList.get(i);
//            resultListCensus.add(dataMap.get("census"));
//            resultListAgeRange.add(dataMap.get("ageRange"));
//        }
        result.put("data",dataMapList);
        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-单位性质")
    @ApiOperation(value="统计分析-单位性质", notes="单位性质")
    @GetMapping(value = "/dwNatChart")
    public Result<Map<String, Object>> dwNatChart(HttpServletRequest req,
                                                  @RequestParam(required = false, name="userId") String userId) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder orgCodeBuilder = new StringBuilder();
        StringBuilder sysDepartCode = new StringBuilder();
        SysUser sysUser = sysUserService.getById(userId);
        List<String> orgCodes = new ArrayList<>();
        List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
        List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
        if(sysUser!=null){
            if(sysDepartsList.size()>0){
                for(int j = 0;j<sysDepartsList.size();j++){
                    SysDepart sysDepart = sysDepartsList.get(j);
                    if (j > 0) {
                        sysDepartCode.append(",");
                    }
                    sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                }
            }
            if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
                if(sysDepartCode.length()>0){
                    StringBuilder newSysDepartCode = new StringBuilder();
                    newSysDepartCode.append("(").append(sysDepartCode).append(")");
                    orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
                    for (int i = 0; i < orgCodes.size(); i++) {
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                    }
                }
            }
            if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
                pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
                if(pjwgqxList.size()>0){
                    for(int i=0;i<pjwgqxList.size();i++){
                        CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                    }
                }
            }
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
        }
        String orgCode = "";
        if(orgCodes.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else if(pjwgqxList.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else {
            if(sysDepartsList.size()>0){
                orgCode = sysDepartCode.toString();
            }
        }
        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(!"".equals(orgCode)){
            orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
        }


        SysDict sysDict = sysDictService.getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode,"dwxz"));
        List<Map<String, Object>> dataMapList = null;
        String tableName = "cjxt_dwcj";

        String sql = "SELECT sd.item_text as dwxz, COUNT(*) dwxzNum \n" +
                "FROM "+tableName+" t,sys_dict_item sd, sys_dict s\n" +
                "WHERE t.del_flag = '0' AND t.dwxz = sd.item_value AND sd.dict_id = s.id AND s.dict_code = 'dwxz' \n" + orgCodeQuery +
                "GROUP BY sd.item_text";
        dataMapList = jdbcTemplate.queryForList(sql);

//        for(int i=0;i<dataMapList.size();i++){
//            Map<String, Object> dataMap = dataMapList.get(i);
//            resultListDwxz.add(dataMap.get("dwxz"));
//            resultListDwxzList.add(dataMap.get("dwxzNum"));
//        }
//        result.put("dwxz",resultListDwxz);
        result.put("data",dataMapList);
        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-实有房屋/实有人口/实有单位(部门统计)")
    @ApiOperation(value="统计分析-实有房屋/实有人口/实有单位(部门统计)", notes="统计分析-实有房屋/实有人口/实有单位(部门统计)")
    @GetMapping(value = "/syfwDepart")
    public Result<Map<String, Object>> syfwDepart(@RequestParam(name="userId",required=false) String userId) {
        Map<String, Object> result = new HashMap<>();

        StringBuilder orgCodeBuilder = new StringBuilder();
        StringBuilder sysDepartCode = new StringBuilder();
        SysUser sysUser = sysUserService.getById(userId);
        List<String> orgCodes = new ArrayList<>();
        List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
        List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
        if(sysUser!=null){
            if(sysDepartsList.size()>0){
                for(int j = 0;j<sysDepartsList.size();j++){
                    SysDepart sysDepart = sysDepartsList.get(j);
                    if (j > 0) {
                        sysDepartCode.append(",");
                    }
                    sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                }
            }
            if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
                if(sysDepartCode.length()>0){
                    StringBuilder newSysDepartCode = new StringBuilder();
                    newSysDepartCode.append("(").append(sysDepartCode).append(")");
                    orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
                    for (int i = 0; i < orgCodes.size(); i++) {
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                    }
                }
            }
            if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
                pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
                if(pjwgqxList.size()>0){
                    for(int i=0;i<pjwgqxList.size();i++){
                        CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                    }
                }
            }
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
        }
        String orgCode = "";
        if(orgCodes.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else if(pjwgqxList.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else {
            if(sysDepartsList.size()>0){
                orgCode = sysDepartCode.toString();
            }
        }
        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(!"".equals(orgCode)){
            orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
        }
        //实有房屋
        String syfwSql = "SELECT COUNT(*) AS num ,t.sys_org_code, (SELECT sd.depart_name_full FROM sys_depart sd WHERE sd.org_code = t.sys_org_code) AS departName  FROM cjxt_fwcj t  WHERE t.del_flag = '0' "+orgCodeQuery+" AND t.sys_org_code IS NOT NULL GROUP BY t.sys_org_code ORDER BY num DESC LIMIT 0,10 ;" ;
        List<Map<String, Object>> dataSyfw = jdbcTemplate.queryForList(syfwSql);
        result.put("syfw",dataSyfw);
        //实有人口
        String syrkSql = "SELECT COUNT(*) AS num ,t.sys_org_code, (SELECT sd.depart_name_full FROM sys_depart sd WHERE sd.org_code = t.sys_org_code) AS departName  FROM cjxt_rkcj t  WHERE t.del_flag = '0' "+orgCodeQuery+" AND t.sys_org_code IS NOT NULL GROUP BY t.sys_org_code ORDER BY num DESC LIMIT 0,10 ;" ;
        List<Map<String, Object>> dataSyrk = jdbcTemplate.queryForList(syrkSql);
        result.put("syrk",dataSyrk);
        //实有单位
        String sydwSql = "SELECT COUNT(*) AS num ,t.sys_org_code, (SELECT sd.depart_name_full FROM sys_depart sd WHERE sd.org_code = t.sys_org_code) AS departName  FROM cjxt_dwcj t  WHERE t.del_flag = '0' "+orgCodeQuery+" AND t.sys_org_code IS NOT NULL GROUP BY t.sys_org_code ORDER BY num DESC LIMIT 0,10 ;" ;
        List<Map<String, Object>> dataSydw = jdbcTemplate.queryForList(sydwSql);
        result.put("sydw",dataSydw);

        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-数据大屏社区信息")
    @ApiOperation(value="统计分析-数据大屏社区信息", notes="统计分析-数据大屏社区信息")
    @GetMapping(value = "/sjdpSqXx")
    public Result<Map<String, Object>> sjdpSqXx(@RequestParam(name="userId",required=false) String userId) {
        Map<String, Object> result = new HashMap<>();

        StringBuilder orgCodeBuilder = new StringBuilder();
        StringBuilder sysDepartCode = new StringBuilder();
        SysUser sysUser = sysUserService.getById(userId);
        List<String> orgCodes = new ArrayList<>();
        List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
        List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
        if(sysUser!=null){
            if(sysDepartsList.size()>0){
                for(int j = 0;j<sysDepartsList.size();j++){
                    SysDepart sysDepart = sysDepartsList.get(j);
                    if (j > 0) {
                        sysDepartCode.append(",");
                    }
                    sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                }
            }
            if("4".equals(sysUser.getUserSf())||"5".equals(sysUser.getUserSf())||"6".equals(sysUser.getUserSf())||"7".equals(sysUser.getUserSf())||"8".equals(sysUser.getUserSf())||"9".equals(sysUser.getUserSf())) {
                if(sysDepartCode.length()>0){
                    StringBuilder newSysDepartCode = new StringBuilder();
                    newSysDepartCode.append("(").append(sysDepartCode).append(")");
                    orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_code in " + newSysDepartCode.toString(), String.class);
                    for (int i = 0; i < orgCodes.size(); i++) {
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                    }
                }
            }
            if("2".equals(sysUser.getUserSf())||"3".equals(sysUser.getUserSf())){
                pjwgqxList= cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId,userId));
                if(pjwgqxList.size()>0){
                    for(int i=0;i<pjwgqxList.size();i++){
                        CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                        if (i > 0) {
                            orgCodeBuilder.append(",");
                        }
                        orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                    }
                }
            }
//				if("1".equals(sysUser.getUserSf())){
//					zzsbQuery.append(" AND create_by = '").append(sysUser.getUsername()).append("'");
//				}
        }
        String orgCode = "";
        if(orgCodes.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else if(pjwgqxList.size()>0){
            orgCode = orgCodeBuilder.toString();
        } else {
            if(sysDepartsList.size()>0){
                orgCode = sysDepartCode.toString();
            }
        }
        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(!"".equals(orgCode)){
            orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
        }
        //社区信息
        String sql = "SELECT \n" +
                "    t.sys_org_code,\n" +
                "    (SELECT sd.depart_name_full FROM sys_depart sd WHERE sd.org_code = t.sys_org_code) AS departName,\n" +
                "    SUM(CASE WHEN t.type = 'syfw' THEN t.num ELSE 0 END) AS syfw_count,\n" +
                "    SUM(CASE WHEN t.type = 'syrk' THEN t.num ELSE 0 END) AS syrk_count,\n" +
                "    SUM(CASE WHEN t.type = 'sydw' THEN t.num ELSE 0 END) AS sydw_count\n" +
                "FROM (\n" +
                "    SELECT 'syfw' AS type, COUNT(*) AS num, t.sys_org_code\n" +
                "    FROM cjxt_fwcj t\n" +
                "    WHERE t.del_flag = '0' AND t.sys_org_code IS NOT NULL\n" + orgCodeQuery +
                "    GROUP BY t.sys_org_code\n" +
                "    UNION ALL\n" +
                "    SELECT 'syrk' AS type, COUNT(*) AS num, t.sys_org_code\n" +
                "    FROM cjxt_rkcj t\n" +
                "    WHERE t.del_flag = '0' AND t.sys_org_code IS NOT NULL \n" + orgCodeQuery +
                "    GROUP BY t.sys_org_code\n" +
                "    UNION ALL\n" +
                "    SELECT 'sydw' AS type, COUNT(*) AS num, t.sys_org_code\n" +
                "    FROM cjxt_dwcj t\n" +
                "    WHERE t.del_flag = '0' AND t.sys_org_code IS NOT NULL\n" + orgCodeQuery +
                "    GROUP BY t.sys_org_code\n" +
                ") t \n" +
                "GROUP BY t.sys_org_code\n" +
                "ORDER BY t.sys_org_code ;" ;
        List<Map<String, Object>> dataSqXx = jdbcTemplate.queryForList(sql);
        result.put("data",dataSqXx);

        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-数据大屏随手拍")
    @ApiOperation(value="统计分析-数据大屏随手拍", notes="统计分析-数据大屏随手拍")
    @GetMapping(value = "/sjdpSsp")
    public Result<Map<String, Object>> sjdpSsp(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        //社区信息
        String sql = "SELECT *," +
                "(SELECT sd.item_text FROM sys_dict s,sys_dict_item sd WHERE s.id = sd.dict_id AND s.dict_code = 'sblx' AND sd.item_value = sb_type) AS sblx " +
                " FROM cjxt_snap_shot WHERE del_flag = '0' ORDER BY create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize + " ;";
        List<Map<String, Object>> dataSqXx = jdbcTemplate.queryForList(sql);
        for(Map<String, Object> row: dataSqXx){
            Object value = row.get("sb_pic");
            if(value!=null && !"".equals(value) && !"null".equals(value)){
                row.put("sb_pic", minioUrl + "/" + bucketName + "/" + value);
            }
        }
        String sqlType = "SELECT COUNT(*) AS num ,sb_type, (SELECT sd.item_text FROM sys_dict s,sys_dict_item sd WHERE s.id = sd.dict_id AND s.dict_code = 'sblx' AND sd.item_value = sb_type) AS sblx FROM cjxt_snap_shot WHERE del_flag = '0' GROUP BY sb_type ;" ;
        List<Map<String, Object>> dataSqType = jdbcTemplate.queryForList(sqlType);
        Map<String, Object> resultSsp = new HashMap<>();

        String countSql = "SELECT COUNT(*) FROM cjxt_snap_shot t WHERE t.del_flag = '0' " ;
        // 执行查询并获取总条数
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        resultSsp.put("totalPages", totalPages);
        resultSsp.put("count", totalCount);
        resultSsp.put("list",dataSqXx);
        resultSsp.put("type",dataSqType);
        result.put("data",resultSsp);
        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-数据大屏隐患上报")
    @ApiOperation(value="统计分析-数据大屏隐患上报", notes="统计分析-数据大屏隐患上报")
    @GetMapping(value = "/sjdpYhsb")
    public Result<Map<String, Object>> sjdpYhsb(@RequestParam(name="userId",required=false) String userId,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        //部门信息数据
        StringBuilder orgCodeQuery = new StringBuilder();
        if(userId!=null && !"".equals(userId)){
            SysUser user = sysUserService.getById(userId);
            if("2".equals(user.getUserSf()) || "3".equals(user.getUserSf())){
                orgCodeQuery.append(" AND t.sys_org_code IN ( SELECT wg_code FROM cjxt_pjwgqx WHERE del_flag = '0' AND pj_id = '"+user.getId()+"' )");
            }
            if("4".equals(user.getUserSf())){
                orgCodeQuery.append(" AND t.sys_org_code LIKE '"+user.getOrgCode()+"%' ");
            }
            //隐患上报---网格员
            if("1".equals(user.getUserSf())){
                orgCodeQuery.append(" AND t.create_by = '"+user.getUsername()+"'");
            }
            if(!"1".equals(user.getUserSf())){
                orgCodeQuery.append(" OR (t.create_by = '"+user.getUsername()+"' AND t.del_flag = '0') ");
            }
        }

        String sql = "SELECT *," +
                "(SELECT sd.item_text FROM sys_dict s,sys_dict_item sd WHERE s.id = sd.dict_id AND s.dict_code = 'yhlx' AND sd.item_value = t.yhlx) AS yhlxText,\n" +
                "(SELECT sd.item_text FROM sys_dict s,sys_dict_item sd WHERE s.id = sd.dict_id AND s.dict_code = 'clzt' AND sd.item_value = t.clzt) AS clztText " +
                " FROM cjxt_pitfall_report t WHERE t.del_flag = '0' "+orgCodeQuery+" ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize +" ;";
        List<Map<String, Object>> dataSqXx = jdbcTemplate.queryForList(sql);
        for(Map<String, Object> row: dataSqXx){
            Object xctp = row.get("xctp");
            if(xctp!=null && !"".equals(xctp) && !"null".equals(xctp)){
                row.put("xctp", minioUrl + "/" + bucketName + "/" + xctp);
            }
            Object xcsp = row.get("xcsp");
            if(xcsp!=null && !"".equals(xcsp) && !"null".equals(xcsp)){
                row.put("xcsp", minioUrl + "/" + bucketName + "/" + xcsp);
            }
        }

        String sqlType = "SELECT COUNT(*) AS num ,t.yhlx, (SELECT sd.item_text FROM sys_dict s,sys_dict_item sd WHERE s.id = sd.dict_id AND s.dict_code = 'yhlx' AND sd.item_value = t.yhlx) AS yhlxText FROM cjxt_pitfall_report t WHERE t.del_flag = '0' "+orgCodeQuery+" GROUP BY t.yhlx ;" ;
        List<Map<String, Object>> dataSqType = jdbcTemplate.queryForList(sqlType);

        Map<String, Object> resultSsp = new HashMap<>();
        String countSql = "SELECT COUNT(*) FROM cjxt_pitfall_report t WHERE t.del_flag = '0' " + orgCodeQuery;
        // 执行查询并获取总条数
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        // 将总页数添加到结果中
        resultSsp.put("totalPages", totalPages);
        resultSsp.put("count", totalCount);
        resultSsp.put("list",dataSqXx);
        resultSsp.put("type",dataSqType);
        result.put("data",resultSsp);
        return Result.OK(result);
    }

    @AutoLog(value = "统计分析-webSocket测试")
    @ApiOperation(value="统计分析-webSocket测试", notes="统计分析-webSocket测试")
    @GetMapping(value = "/webSocket")
    public Result<String> webSocket() {
        webSocket.sendMergedMessage();
        return Result.OK("发送成功");
    }

}
