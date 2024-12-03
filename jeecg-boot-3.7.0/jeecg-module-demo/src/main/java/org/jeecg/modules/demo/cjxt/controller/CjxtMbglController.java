package org.jeecg.modules.demo.cjxt.controller;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.mchange.v2.log.LogUtils;
import com.unfbx.chatgpt.entity.assistant.run.Run;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.demo.cjxt.entity.*;
import org.jeecg.modules.demo.cjxt.service.*;
import org.jeecg.modules.demo.cjxt.utils.AesTestOne;
import org.jeecg.modules.demo.cjxt.utils.baidu.Idcard;
import org.jeecg.modules.demo.cjxt.utils.log.Dg;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.MapExcelConstants;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.params.ExcelExportEntity;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.vo.CjxtMbglPage;
import org.jeecgframework.poi.excel.view.JeecgMapExcelView;
import org.openxmlformats.schemas.presentationml.x2006.main.STTLTriggerRuntimeNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

import java.io.IOException;

import static org.jeecg.modules.demo.cjxt.utils.Desensitization.desensitize;
import static org.jeecg.modules.demo.cjxt.utils.Desensitization.maskPhone;
import static org.jeecg.modules.demo.cjxt.utils.RsaUtil.decryptRes;


/**
 * @Description: 模板管理
 * @Author: jeecg-boot
 * @Date: 2024-06-03
 * @Version: V1.0
 */
@Api(tags = "模板管理")
@RestController
@RequestMapping("/cjxt/cjxtMbgl")
@Slf4j
public class CjxtMbglController {
    @Autowired
    private ICjxtMbglService cjxtMbglService;
    @Autowired
    private ICjxtMbglPzService cjxtMbglPzService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private IOnlCgformHeadsService onlCgformHeadService;
    @Autowired
    private IOnlCgformFieldsService onlCgformFieldsService;
    @Autowired
    private ISysDictItemService sysDictItemService;
    @Autowired
    private ISysDictService sysDictService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ICjxtScoreRuleService cjxtScoreRuleService;
    @Autowired
    private ICjxtScoreDetailService cjxtScoreDetailService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ICjxtStandardAddressService cjxtStandardAddressService;
    @Autowired
    private ICjxtTaskDispatchService cjxtTaskDispatchService;
    @Autowired
    private ICjxtAreaService cjxtAreaService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ICjxtTaskService cjxtTaskService;
    @Autowired
    private ICjxtJsmbpzService cjxtJsmbpzService;
    @Autowired
    private ICjxtJsmbpzDtlService cjxtJsmbpzDtlService;
    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private ISysRoleService sysRoleService;
    @Autowired
    private ICjxtWarningMessageService cjxtWarningMessageService;
    @Autowired
    private ICjxtStandardAddressSbryService cjxtStandardAddressSbryService;
    @Autowired
    private ICjxtPjwgqxService cjxtPjwgqxService;
    @Autowired
    private ICjxtDataReentryService cjxtDataReentryService;
    @Autowired
    private ICjxtBmfzrService cjxtBmfzrService;
    @Autowired
    private WebSocket webSocket;
    @Autowired
    private ICjxtXtcsService cjxtXtcsService;
    //minio图片服务器
    @Value(value = "${jeecg.minio.minio_url}")
    private String minioUrl;
    @Value(value = "${jeecg.minio.bucketName}")
    private String bucketName;


    /**
     * 专项任务采集模板查询
     */
    //@AutoLog(value = "模板管理-专项任务采集模板查询")
    @ApiOperation(value = "模板管理-专项任务采集模板查询", notes = "模板管理-专项任务采集模板查询")
    @GetMapping(value = "/zxrwcjList")
    public Result<IPage<CjxtMbgl>> zxrwcjList(CjxtMbgl cjxtMbgl,
                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              HttpServletRequest req) {
//		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		String[] sysUserRoleS = sysUser.getRoleCode().split(",");
//
//		StringBuilder mbId = new StringBuilder();
//		if (!Arrays.asList(sysUserRoleS).contains("admin")) {
//			boolean isFirstCondition = true;
//			for (String userRoleCode : sysUserRoleS) {
//				List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode, userRoleCode));
//				for (CjxtJsmbpz cjxtJsmbpz : cjxtJsmbpzList) {
//					if (isFirstCondition) {
//						isFirstCondition = false;
//					} else {
//						mbId.append(",");
//					}
//					mbId.append(cjxtJsmbpz.getMbId());
//				}
//			}
//		}

        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        parameterMap.remove("order");
        QueryWrapper<CjxtMbgl> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMbgl, parameterMap);
//		if(cjxtMbgl.getMblx()!=null){
//			queryWrapper.eq("mblx",cjxtMbgl.getMblx());
//		}
        queryWrapper.eq("mblx", "5");
//		if(!"".equals(mbId.toString())){
//			List<String> idList = Arrays.asList(mbId.toString().split(","));
//			queryWrapper.in("id",idList);
//		}
        queryWrapper.orderByAsc("mblx").orderByAsc("mbsort");
        Page<CjxtMbgl> page = new Page<CjxtMbgl>(pageNo, pageSize);
        IPage<CjxtMbgl> pageList = cjxtMbglService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 九小场所查询
     */
    //@AutoLog(value = "模板管理-九小场所查询")
    @ApiOperation(value = "模板管理-九小场所查询", notes = "模板管理-九小场所查询")
    @GetMapping(value = "/jxcsList")
    public Result<IPage<CjxtMbgl>> jxcsList(CjxtMbgl cjxtMbgl,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
//		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		String[] sysUserRoleS = sysUser.getRoleCode().split(",");
//
//		StringBuilder mbId = new StringBuilder();
//		if (!Arrays.asList(sysUserRoleS).contains("admin")) {
//			boolean isFirstCondition = true;
//			for (String userRoleCode : sysUserRoleS) {
//				List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode, userRoleCode));
//				for (CjxtJsmbpz cjxtJsmbpz : cjxtJsmbpzList) {
//					if (isFirstCondition) {
//						isFirstCondition = false;
//					} else {
//						mbId.append(",");
//					}
//					mbId.append(cjxtJsmbpz.getMbId());
//				}
//			}
//		}

        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        parameterMap.remove("order");
        QueryWrapper<CjxtMbgl> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMbgl, parameterMap);
//		if(cjxtMbgl.getMblx()!=null){
//			queryWrapper.eq("mblx",cjxtMbgl.getMblx());
//		}
        queryWrapper.eq("mblx", "4");
//		if(!"".equals(mbId.toString())){
//			List<String> idList = Arrays.asList(mbId.toString().split(","));
//			queryWrapper.in("id",idList);
//		}
        queryWrapper.orderByAsc("mblx").orderByAsc("mbsort");
        Page<CjxtMbgl> page = new Page<CjxtMbgl>(pageNo, pageSize);
        IPage<CjxtMbgl> pageList = cjxtMbglService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param cjxtMbgl
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "模板管理-分页列表查询")
    @ApiOperation(value = "模板管理-分页列表查询", notes = "模板管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<CjxtMbgl>> queryPageList(CjxtMbgl cjxtMbgl,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String[] sysUserRoleS = sysUser.getRoleCode().split(",");

        StringBuilder mbId = new StringBuilder();
        if (!Arrays.asList(sysUserRoleS).contains("admin")) {
            boolean isFirstCondition = true;
            for (String userRoleCode : sysUserRoleS) {
                List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode, userRoleCode));
                for (CjxtJsmbpz cjxtJsmbpz : cjxtJsmbpzList) {
                    if (isFirstCondition) {
                        isFirstCondition = false;
                    } else {
                        mbId.append(",");
                    }
                    mbId.append(cjxtJsmbpz.getMbId());
                }
            }
        }

        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        parameterMap.remove("order");
        QueryWrapper<CjxtMbgl> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMbgl, parameterMap);
        if (cjxtMbgl.getMblx() != null) {
            queryWrapper.eq("mblx", cjxtMbgl.getMblx());
        }
        if (!"".equals(mbId.toString())) {
            List<String> idList = Arrays.asList(mbId.toString().split(","));
            queryWrapper.in("id", idList);
        }
        queryWrapper.orderByAsc("mblx").orderByAsc("mbsort");
        Page<CjxtMbgl> page = new Page<CjxtMbgl>(pageNo, pageSize);
        IPage<CjxtMbgl> pageList = cjxtMbglService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "模板管理-上报分页列表查询", notes = "模板管理-上报分页列表查询")
    @GetMapping(value = "/listSb")
    public Result<IPage<CjxtMbgl>> listSb(CjxtMbgl cjxtMbgl,
                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                             @RequestParam(name = "shws", required = false) String shws,
                                             @RequestParam(name = "shzt", required = false) String shzt,
                                             HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String[] sysUserRoleS = sysUser.getRoleCode().split(",");
        StringBuilder mbId = new StringBuilder();
        boolean isFirstCondition = true;
        for (int i = 0; i < sysUserRoleS.length; i++) {
            String userRoleCode = sysUserRoleS[i];
            List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode, userRoleCode));
            for (CjxtJsmbpz cjxtJsmbpz : cjxtJsmbpzList) {
                if (isFirstCondition) {
                    isFirstCondition = false;
                } else {
                    mbId.append(",");
                }
                mbId.append(cjxtJsmbpz.getMbId());
            }
        }
        Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
        parameterMap.remove("order");
        QueryWrapper<CjxtMbgl> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMbgl, parameterMap);
        queryWrapper.eq("sfsb", "1");
        if (cjxtMbgl.getMblx() != null) {
            queryWrapper.eq("mblx", cjxtMbgl.getMblx());
        }
        if (!"".equals(mbId)) {
            List<String> idList = Arrays.asList(mbId.toString().split(","));
            queryWrapper.in("id", idList);
        }
        queryWrapper.orderByAsc("mblx").orderByAsc("mbsort");
        Page<CjxtMbgl> page = new Page<CjxtMbgl>(pageNo, pageSize);
        IPage<CjxtMbgl> pageList = cjxtMbglService.page(page, queryWrapper);

        // 新建一个列表来存储符合条件的记录
        List<CjxtMbgl> filteredRecords = new ArrayList<>();

        for(CjxtMbgl mbgl: pageList.getRecords()){
            String tableName = mbgl.getBm();
            StringBuilder shwsSqlAdd = new StringBuilder();
            String sjshZt = ""; // 数据审核状态
            String sjwsZt = ""; // 数据完善状态
            //数据审核 查询自主上报数据
            if (!"".equals(shws) && shws != null && "sb".equals(shws)) {
                tableName = tableName + "_sb";
                if (!"".equals(shzt) && shzt != null) {
                    if ("true".equals(shzt)) {
                        sjshZt = "1";
                        shwsSqlAdd.append(" AND t.shzt = '1' ");
                    } else if ("false".equals(shzt)) {
                        sjshZt = "0";
                        shwsSqlAdd.append(" AND t.shzt = '0' ");
                    }
                }
            } else {
                if (!"".equals(shzt) && shzt != null) {
                    if ("true".equals(shzt)) {
                        sjshZt = "1";
                        shwsSqlAdd.append(" AND t.wszt = '1' ");
                    } else if ("false".equals(shzt)) {
                        sjshZt = "0";
                        shwsSqlAdd.append(" AND t.wszt = '0' ");
                    }
                }
            }
            String dataSql = "";
            if ("2".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd +
                        "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                        + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
            } else if ("3".equals(sysUser.getUserSf())
                    || "6".equals(sysUser.getUserSf())
                    || "7".equals(sysUser.getUserSf())
                    || "8".equals(sysUser.getUserSf())
                    || "9".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd +
                        "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                        + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
            } else if ("4".equals(sysUser.getUserSf())
                    || "5".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd +
                        "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                        + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
            } else if ("1".equals(sysUser.getUserSf())) {
                //获取部门信息
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd +
                        "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                        + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
            } else {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd +
                        " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
            }
            List<Map<String, Object>> resultList  = jdbcTemplate.queryForList(dataSql);
            if (!resultList.isEmpty()) {
                filteredRecords.add(mbgl);
            }
        }

        // 创建一个新的分页对象来存储过滤后的记录
        IPage<CjxtMbgl> filteredPageList = new Page<>(pageNo, pageSize);
        filteredPageList.setRecords(filteredRecords);
        filteredPageList.setTotal(filteredRecords.size());

        return Result.OK(filteredPageList);
    }


    @ApiOperation(value = "模板管理-模版是否存在", notes = "模板管理-模版是否存在")
    @GetMapping(value = "/mbIsDb")
    public Result<Map<String, Object>> mbIsDb(
            @RequestParam(required = false, name = "mbId") String mbId, // 任务表模版ID
            @RequestParam(required = false, name = "mbCode") String mbCode, // 任务表模版ID
            HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        if (mbId != null && !"".equals(mbId) && !mbId.isEmpty()) {
            //模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbId);
            if (!"1".equals(cjxtMbgl.getIsDb())) {
                result.put("isDb", false);
                result.put("msg", "当前任务模版未同步数据库,请联系派发人!!!");
            } else {
                result.put("isDb", true);
            }
        }
        if (mbCode != null && !"".equals(mbCode) && !mbCode.isEmpty()) {
            //模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode));
            if (!"1".equals(cjxtMbgl.getIsDb())) {
                result.put("isDb", false);
                result.put("msg", "当前任务模版未同步数据库,请联系派发人!!!");
            } else {
                result.put("isDb", true);
            }
        }
        return Result.OK(result);
    }

    @IgnoreAuth
    @ApiOperation(value = "模板管理-动态配置字段数据", notes = "模板管理-动态配置字段数据")
    @GetMapping(value = "/listPz")
    public Result<Map<String, Object>> queryPageListPz(
            @RequestParam(required = false, name = "taskId") String taskId, //任务表ID
            @RequestParam(required = false, name = "mbId") String mbId, // 任务表模版ID
            @RequestParam(required = false, name = "mbCode") String mbCode, // 任务表模版ID
            @RequestParam(required = false, name = "cjSb") String cjSb, // 采集上报
            @RequestParam(required = true, name = "addressId") String addressId, // 任务表地址ID
            @RequestParam(required = true, name = "userId") String userId, // 用户ID
            @RequestParam(required = false, name = "dataId") String dataId, //数据表ID
            @RequestParam(required = false, name = "isTj") String isTj, //统计查询
            HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        if (addressId != null && !"".equals(addressId) && !addressId.isEmpty() &&
                userId != null && !"".equals(userId) && !userId.isEmpty()) {
            CjxtMbgl cjxtMbgl = new CjxtMbgl();
            if (mbId != null && !"".equals(mbId) && !mbId.isEmpty()) {
                //模版
                cjxtMbgl = cjxtMbglService.getById(mbId);
            } else if (mbCode != null && !"".equals(mbCode) && !mbCode.isEmpty()) {
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode));
            }
            boolean isRole = false;
            CjxtJsmbpz jsmbpz = null;
            if (cjxtMbgl != null) {
                List<CjxtMbglPz> list = new ArrayList<>();
                SysRole role = null;
                List<SysUserRole> userRoleList = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
                if (userRoleList != null && userRoleList.size() > 0) {
                    for (SysUserRole sysUserRole : userRoleList) {
                        role = sysRoleService.getById(sysUserRole.getRoleId());
                        CjxtJsmbpz cjxtJsmbpz = cjxtJsmbpzService.getOne(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getMbId, cjxtMbgl.getId()).eq(CjxtJsmbpz::getRoleCode, role.getRoleCode()));
                        if (cjxtJsmbpz != null) {
                            jsmbpz = cjxtJsmbpz;
                            isRole = true;
                            break;
                        }
                    }
                }

                if (isRole && jsmbpz != null) {
                    String sql = "select r.db_field_name from cjxt_jsmbpz_dtl r where r.mb_id = '" + cjxtMbgl.getId() + "' AND role_code = '" + role.getRoleCode() + "' union all select m.zdname from cjxt_gtzd m";
                    list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().inSql(CjxtMbglPz::getDbFieldName, sql).eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
                } else {
                    list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
                }

                //统计查询 返回配置APP查询排序字段
                if(!"".equals(isTj) && isTj!=null && "1".equals(isTj)){
                    list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).eq(CjxtMbglPz::getAppIsQuery,"1").orderByAsc(CjxtMbglPz::getAppQueryOrder));
                }

                if (!"".equals(cjxtMbgl.getBm()) && "1".equals(cjxtMbgl.getIsDb())) {
                    String bm = cjxtMbgl.getBm();
                    //任务采集1 自主上报3
                    if ("1".equals(cjSb)) {
                        bm = cjxtMbgl.getBm();
                    } else if ("3".equals(cjSb)) {
                        bm = cjxtMbgl.getBm() + "_sb";
                    }
                    String sql = "";
                    List<Map<String, Object>> resultList = new ArrayList<>();
                    if (dataId != null && !"".equals(dataId) && !dataId.isEmpty()) {
                        sql = "SELECT * FROM " + bm + " WHERE id = '" + dataId + "' ;";
                        resultList = jdbcTemplate.queryForList(sql);
                    }
                    if (resultList.size() > 0) {
                        CjxtStandardAddress cjxtStandardAddress = null;
                        for (Map<String, Object> row : resultList) {
                            for (CjxtMbglPz cjxtMbglPz : list) {
                                String dbFieldName = cjxtMbglPz.getDbFieldName();

                                if("1".equals(cjxtMbglPz.getSfjm())){
                                    Object value = row.get(dbFieldName);
                                    if(!"".equals(value) && value!=null){
                                        if(((String) value).contains("_sxby")){
                                            String dataV = sjjm(value.toString());
                                            row.put(dbFieldName, dataV);
                                        }else {
                                            row.put(dbFieldName, value);
                                        }
                                    }else {
                                        row.put(dbFieldName, "");
                                    }
                                }

                                if (row.containsKey(dbFieldName)) {
                                    Object value = row.get(dbFieldName);
                                    if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                                        String tmValue = sjtm(cjxtMbglPz.getDbJylx(),cjxtMbglPz.getSfjm(),String.valueOf(value));
                                        cjxtMbglPz.setDataTmValue(tmValue);
                                    }
                                    cjxtMbglPz.setDataValue(String.valueOf(value)); // 将值存入dataValue字段
                                    //图片或者附件
                                    if ("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                                        if(!"".equals(value) && value!=null){
                                            if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                                                String tmValue = sjtm(cjxtMbglPz.getDbJylx(),cjxtMbglPz.getSfjm(),String.valueOf(value));
                                                cjxtMbglPz.setDataTmValue(minioUrl + "/" + bucketName + "/" + tmValue);
                                            }
                                            cjxtMbglPz.setDataValue(minioUrl + "/" + bucketName + "/" + value);
                                        }
                                    }
                                    if ("address_id".equals(dbFieldName)) {
                                        cjxtStandardAddress = cjxtStandardAddressService.getById(value.toString());
                                    }
                                    if ("address".equals(dbFieldName)) {
                                        if (cjxtStandardAddress != null) {
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
                                            if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                                                String tmValue = sjtm(cjxtMbglPz.getDbJylx(),cjxtMbglPz.getSfjm(),addressName);
                                                cjxtMbglPz.setDataTmValue(tmValue);
                                            }
                                            cjxtMbglPz.setDataValue(addressName);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        //用户
                        SysUser sysUser = sysUserService.getById(userId);
                        //地址
                        CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressId);
                        //自主上报
                        CjxtStandardAddressSbry cjxtStandardAddressSbry = null;
                        if ("3".equals(cjSb)) {
                            cjxtStandardAddressSbry = cjxtStandardAddressSbryService.getById(userId);
                        }
                        for (CjxtMbglPz cjxtMbglPz : list) {
                            String dbFieldName = cjxtMbglPz.getDbFieldName();
                            if ("id".equals(dbFieldName)) {
                                cjxtMbglPz.setDataValue(dataId);
                            }
                            if ("create_by".equals(dbFieldName)) {
                                if (sysUser != null) {
                                    cjxtMbglPz.setDataValue(sysUser.getUsername());
                                }
                                if ("3".equals(cjSb) && cjxtStandardAddressSbry != null) {
                                    cjxtMbglPz.setDataValue(cjxtStandardAddressSbry.getPhone());
                                }
                            }
                            if ("update_by".equals(dbFieldName)) {
                                if (sysUser != null) {
                                    cjxtMbglPz.setDataValue(sysUser.getUsername());
                                }
                                if ("3".equals(cjSb) && cjxtStandardAddressSbry != null) {
                                    cjxtMbglPz.setDataValue(cjxtStandardAddressSbry.getPhone());
                                }
                            }
                            if ("sys_org_code".equals(dbFieldName)) {
                                if (cjxtStandardAddress != null) {
                                    cjxtMbglPz.setDataValue(cjxtStandardAddress.getAddressCodeMz());
                                }
                            }
                            if ("mb_id".equals(dbFieldName)) {
                                cjxtMbglPz.setDataValue(cjxtMbgl.getId());
                            }
                            if ("mb_name".equals(dbFieldName)) {
                                cjxtMbglPz.setDataValue(cjxtMbgl.getMbname());
                            }
                            if ("table_name".equals(dbFieldName)) {
                                cjxtMbglPz.setDataValue(cjxtMbgl.getBm());
                            }
                            if ("address_id".equals(dbFieldName)) {
                                if (cjxtStandardAddress != null) {
                                    cjxtMbglPz.setDataValue(cjxtStandardAddress.getId());
                                }
                            }
                            if ("address".equals(dbFieldName)) {
                                if (cjxtStandardAddress != null) {
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
                                    cjxtMbglPz.setDataValue(addressName);
                                }
                            }
                            if ("del_flag".equals(dbFieldName)) {
                                cjxtMbglPz.setDataValue("0");
                            }
                            if ("data_id".equals(dbFieldName)) {
                                cjxtMbglPz.setDataValue(taskId);
                            }
                        }
                    }
                }
                result.put("pzList", list);
            }
        }
        return Result.OK(result);
    }

    @ApiOperation(value = "模板管理配置-PC动态回显数据", notes = "模板管理配置-PC动态回显数据")
    @GetMapping(value = "/listPcDataValues")
    public Result<Map<String, Object>> listPcDataValues(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "tableName") String tableName,
            @RequestParam(required = false, name = "taskId") String taskId,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (tableName != null && taskId != null) {
            String countSql = null;
            String dataSql = null;
            Integer total = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();

            //查询字段配置
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, tableName).last("LIMIT 1"));
            List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));

            // 移除不需要的键
            params.remove("tableName");
            params.remove("pageNo");
            params.remove("pageSize");
            params.remove("column");
            params.remove("taskId");
            params.remove("order");
            params.remove("_t");
            StringBuilder additionalQuery = new StringBuilder();
            if (!params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalQuery.append(" AND ").append("t."+entry.getKey()).append(" LIKE '%").append(entry.getValue()).append("%'");
                }
            }
            if ("2".equals(sysUser.getUserSf())
                    || "3".equals(sysUser.getUserSf())
                    || "6".equals(sysUser.getUserSf())
                    || "7".equals(sysUser.getUserSf())
                    || "8".equals(sysUser.getUserSf())
                    || "9".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = '" + taskId + "') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                countSql = "SELECT COUNT(*) FROM "+tableName+" t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = 'fc92153e54254254b09a60479bcad09b') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery;
            } else if ("4".equals(sysUser.getUserSf())
                    || "5".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = '" + taskId + "') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM "+tableName+" t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = 'fc92153e54254254b09a60479bcad09b') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery;
            } else if ("1".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = '" + taskId + "') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM "+tableName+" t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = 'fc92153e54254254b09a60479bcad09b') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery;
            } else {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = '" + taskId + "') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM "+tableName+" t WHERE t.del_flag = '0' AND t.id IN (SELECT ctd.data_id FROM cjxt_task_dispatch ctd  WHERE ctd.task_id IN (SELECT id FROM cjxt_task WHERE task_code = 'fc92153e54254254b09a60479bcad09b') AND ctd.rwzt = '4' AND ctd.del_flag = '0')"
                        + additionalQuery;
            }
            resultList = jdbcTemplate.queryForList(dataSql);

            // 执行查询并获取总条数
            int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            // 计算总页数
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            // 将总页数添加到结果中
            result.put("totalPages", totalPages);

            HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");

            System.out.println("输出dictText" + dictText);
            if (resultList.size() > 0) {
                for (Map<String, Object> row : resultList) {
                    for (CjxtMbglPz cjxtMbglPz : list) {
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getValue().equals(value)) {
                                            row.put(dbFieldName, dictModel.getText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //图片或者附件
                        if ("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                if (value != null && !"".equals(value)) {
                                    row.put(dbFieldName, minioUrl + "/" + bucketName + "/" + value);
                                }
                            }
                        }
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            Object value = row.get(dbFieldName);
                            if(!"".equals(value) && value!=null){
                                if(((String) value).contains("_sxby")){
                                    String dataV = sjjm(value.toString());
                                    row.put(dbFieldName, dataV);
                                }else {
                                    row.put(dbFieldName, value);
                                }
                            }else {
                                row.put(dbFieldName, "");
                            }
                        }
                    }
                }
            }

            result.put("current", pageNo);
            result.put("size", pageSize);
            result.put("total", totalCount);
            result.put("pages", totalPages);
            result.put("records", resultList);
        }
        return Result.OK(result);
    }

    @ApiOperation(value = "模板管理配置-PC回显补录数据", notes = "模板管理配置-PC回显补录数据")
    @GetMapping(value = "/listPcDataBl")
    public Result<Map<String, Object>> listPcDataBl(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "tableName") String tableName,
            @RequestParam(required = false, name = "taskId") String taskId,
            @RequestParam(required = false, name = "id") String id,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (tableName != null && id != null) {
            String countSql = null;
            String dataSql = null;
            Integer total = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();

            //查询字段配置
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, tableName).last("LIMIT 1"));
            List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));

            // 移除不需要的键
            params.remove("tableName");
            params.remove("pageNo");
            params.remove("pageSize");
            params.remove("column");
            params.remove("taskId");
            params.remove("order");
            params.remove("_t");
            StringBuilder additionalQuery = new StringBuilder();
            if (!params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalQuery.append(" AND ").append(entry.getKey()).append(" LIKE '%").append(entry.getValue()).append("%'");
                }
            }
//            if ("2".equals(sysUser.getUserSf())
//                    || "3".equals(sysUser.getUserSf())
//                    || "6".equals(sysUser.getUserSf())
//                    || "7".equals(sysUser.getUserSf())
//                    || "8".equals(sysUser.getUserSf())
//                    || "9".equals(sysUser.getUserSf())) {
//                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
//
//                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery;
//            } else if ("4".equals(sysUser.getUserSf())
//                    || "5".equals(sysUser.getUserSf())) {
//                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
//                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery;
//            } else if ("1".equals(sysUser.getUserSf())) {
//                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
//                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery;
//            } else {
//                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery + " ORDER BY t.create_time DESC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
//                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' "
//                        + "AND t.id IN (SELECT data_id FROM cjxt_task_dispatch m  WHERE m.task_id in "
//                        + "(WITH RECURSIVE task AS (SELECT id, pid FROM cjxt_task WHERE id = '" + taskId + "' UNION ALL  SELECT c.id, c.pid FROM cjxt_task c INNER JOIN task AS f on f.id = c.pid )\t SELECT id FROM task ) "
//                        + "AND m.rwzt = '4'  AND m.del_flag = '0'  AND m.address_id = t.address_id )	"
//                        + additionalQuery;
//            }
            dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' "
                    + "AND t.id = '" + id + "'" + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

            countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " + "AND t.id = '" + id + "'" ;
            resultList = jdbcTemplate.queryForList(dataSql);

            // 执行查询并获取总条数
            int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            // 计算总页数
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            // 将总页数添加到结果中
            result.put("totalPages", totalPages);

            HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");

            System.out.println("输出dictText" + dictText);
            if (resultList.size() > 0) {
                for (Map<String, Object> row : resultList) {
                    for (CjxtMbglPz cjxtMbglPz : list) {
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getValue().equals(value)) {
                                            row.put(dbFieldName, dictModel.getText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //图片或者附件
                        if ("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                if (value != null && !"".equals(value)) {
                                    row.put(dbFieldName, minioUrl + "/" + bucketName + "/" + value);
                                }
                            }
                        }
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            Object value = row.get(dbFieldName);
                            if(!"".equals(value) && value!=null){
                                if(((String) value).contains("_sxby")){
                                    String dataV = sjjm(value.toString());
                                    row.put(dbFieldName, dataV);
                                }else {
                                    row.put(dbFieldName, value);
                                }
                            }else {
                                row.put(dbFieldName, "");
                            }
                        }
                    }
                }
            }

            result.put("current", pageNo);
            result.put("size", pageSize);
            result.put("total", totalCount);
            result.put("pages", totalPages);
            result.put("records", resultList);
        }
        return Result.OK(result);
    }

    /**
     * PC模版管理 数据新增修改
     *
     * @param map
     * @return
     */
    @ApiOperation(value = "模板管理配置-PC数据新增修改", notes = "模板管理配置-PC数据新增修改")
    @PostMapping(value = "/addPcValue")
    public Result<String> addPcValue(@RequestBody Map<String, Object> map) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            //取出模版ID
            String mbId = (String) map.get("mb_id");
            String idDataV = (String) map.get("id");

            String mbCode = "";
//            Map<String, Object> map = new LinkedHashMap<String, Object>();
            String idValue = null;
            boolean isAdd = false;
            //定义UUID
            String uuid = UUID.randomUUID().toString().replace("-", "");

            String dataVId= "";//获取数据ID
            CjxtMbgl mbgl = null;
            String dbOnlyName = "";//唯一字段名
            String dbOnlyNameValue = "";//唯一字段值
            String onlyTableName = "";//唯一表名称
            String addressIdValue = "";
            if (mbId != null && !"".equals(mbId)) {
                CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbId);
                if (cjxtMbgl != null) {
                    mbgl = cjxtMbgl;
                    onlyTableName = cjxtMbgl.getBm();
                    dbOnlyName = cjxtMbgl.getDbOnly();
                }
            }

            boolean addOrUpt = true;  // 唯一字段值是否存在数据
            boolean isAddressDis = true;  // 地址是否存在数据
            //数据已存在ID
            StringBuilder idStringBuilder = new StringBuilder();
            //人员模版身份证信息
            StringBuilder rysfzhBuilder = new StringBuilder();
            StringBuilder sfzhBuilder = new StringBuilder();
            String addressIdNotNull = "";//数据已存在地址ID数据
            List<CjxtMbglPz> pzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglId,mbId).eq(CjxtMbglPz::getDelFlag,"0"));
            for (CjxtMbglPz cjxtMbglPz : pzList) {
                //唯一字段不等于空进入
                if (!"".equals(dbOnlyName) && dbOnlyName != null && !dbOnlyName.isEmpty()) {
                    if (dbOnlyName.equals(cjxtMbglPz.getDbFieldName())) {
//                        dbOnlyNameValue = cjxtMbglPz.getDataValue();
                        dbOnlyNameValue = (String) map.get(dbOnlyName);
                        String sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "'";
                        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                        if (resultList.size() > 0) {
                            Map<String, Object> row = resultList.get(0);
                            dataVId = (String) row.get("id");
                            addOrUpt = false;
                            //return Result.error(cjxtMbglPz.getDbFieldTxt()+"数据已存在,请重新输入!!!");
                        }
                    }
                }
                //任务采集地址是否已存在数据
                if ("address_id".equals(cjxtMbglPz.getDbFieldName())) {
//                    addressIdValue = cjxtMbglPz.getDataValue();
                    addressIdValue = (String) map.get("address_id");
                    CjxtStandardAddress standardAddress = cjxtStandardAddressService.getById(addressIdValue);
                    map.put("address", standardAddress.getAddressName());
                    String sqlOnly = "";
                    if ("2".equals(mbgl.getMblx()) && mbgl != null) {
                        if ("RY001".equals(mbgl.getMbbh())) {
                            for (CjxtMbglPz cjxtMbglPzSfzh : pzList) {
                                if ("rysfzh".equals(cjxtMbglPzSfzh.getDbFieldName())) {
                                    sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + addressIdValue + "' AND rysfzh = '" + (String) map.get("rysfzh") + "'";
                                }
                            }
                        }
                        if ("RY002".equals(mbgl.getMbbh())) {
                            for (CjxtMbglPz cjxtMbglPzSfzh : pzList) {
                                if ("sfzh".equals(cjxtMbglPzSfzh.getDbFieldName())) {
                                    sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + addressIdValue + "' AND sfzh = '" + (String) map.get("sfzh") + "'";
                                }
                            }
                        }
                    } else {
                        sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + addressIdValue + "'";
                    }
                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                    if (resultList.size() > 0) {
                        for (Map<String, Object> result : resultList) {
                            dataVId = (String) result.get("id");
                            if ("2".equals(mbgl.getMblx()) && mbgl != null) {
                                if ("RY001".equals(mbgl.getMbbh())) {
                                    rysfzhBuilder.append(result.get("rysfzh")).append(",");
                                }
                                if ("RY002".equals(mbgl.getMbbh())) {
                                    sfzhBuilder.append(result.get("sfzh")).append(",");
                                }
                            }
                            idStringBuilder.append(result.get("id")).append(",");
                        }
                        addressIdNotNull += addressIdValue;
                        isAddressDis = false;
                    }
                }

                if ("1".equals(cjxtMbglPz.getIsTitle())) {
                    continue;
                }
                if ("id".equals(cjxtMbglPz.getDbFieldName())) {
                    mbCode = cjxtMbglPz.getMbglMbbh();
                    idValue = (String) map.get(cjxtMbglPz.getDbFieldName());
                    if (idValue == null || "".equals(idValue)) {
                        dataVId = uuid;
                        isAdd = true;
                        map.put(cjxtMbglPz.getDbFieldName(), uuid);
                        map.put("err_msg", "数据新增");
                    } else {
                        dataVId = idValue;
                        map.put(cjxtMbglPz.getDbFieldName(), idValue);
                        map.put("err_msg", "数据修改");
                    }
                } else {
//                    if ("null".equals(cjxtMbglPz.getDataValue()) || cjxtMbglPz.getDataValue() == null || "".equals(cjxtMbglPz.getDataValue())) {
//                        map.put(cjxtMbglPz.getDbFieldName(), null);
//                    } else {
//                        map.put(cjxtMbglPz.getDbFieldName(), cjxtMbglPz.getDataValue());
//                    }
                    if("del_flag".equals(cjxtMbglPz.getDbFieldName())){
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), "0");
                        }
                    }
                    if ("create_by".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), sysUser.getUsername());
                        }
                    }
                    if ("create_time".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), formattedDateTime);
                        }
                    }
                    if ("update_by".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == false || isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), sysUser.getUsername());
                        }
                    }
                    if ("update_time".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == false || isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), formattedDateTime);
                        }
                    }
                    if ("wszt".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true || "0".equals((String) map.get("wszt"))) {
                            map.put(cjxtMbglPz.getDbFieldName(), null);
                        }
                    }
                    if ("blzt".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), "0");
                        }
                    }
                    if("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())){
                        String tpDbName = (String) map.get(cjxtMbglPz.getDbFieldName());
                        String heardUrl = minioUrl+"/"+bucketName+"/";
                        if(!"".equals(tpDbName) && tpDbName!=null){
                            if(tpDbName.contains(heardUrl)){
                                map.put(cjxtMbglPz.getDbFieldName(), tpDbName.replace(heardUrl,""));
                            }
                        }
                    }
                    if("list".equals(cjxtMbglPz.getFieldShowType())){
                        String itemText = (String) map.get(cjxtMbglPz.getDbFieldName());
                        String dictCode = cjxtMbglPz.getDictField();
                        SysDict sysDict = sysDictService.getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode,dictCode));
                        if(sysDict!=null){
                            SysDictItem sysDictItem = sysDictItemService.getOne(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId,sysDict.getId()).eq(SysDictItem::getItemText,itemText));
                            if(sysDictItem!=null){
                                map.put(cjxtMbglPz.getDbFieldName(), sysDictItem.getItemValue());
                            }
                        }
                    }
                }

                //判断当前模版为从业人员模版
                if ("RY002".equals(mbCode)) {
                    String bmValue = "";
                    if ("bm".equals(cjxtMbglPz.getDbFieldName())) {
                        bmValue = (String) map.get(cjxtMbglPz.getDbFieldName());
                    }
                    if ("sfzh".equals(cjxtMbglPz.getDbFieldName()) && !"".equals(bmValue)) {
                        String updateSql = "UPDATE cjxt_rkcj SET ryfwcsgzdw = '" + bmValue + "' WHERE del_flag = '0' AND rysfzh = '" + (String) map.get(cjxtMbglPz.getDbFieldName()) + "' ;";
                        jdbcTemplate.update(updateSql);
                    }
                }
                //处理脱敏字段 身份证脱敏字段
                if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                    //身份证
                    if("1".equals(cjxtMbglPz.getDbJylx())){
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            String sfzh = (String) map.get(cjxtMbglPz.getDbFieldName());
                            if(!"".equals(sfzh) && sfzh!=null){
                                if(sfzh.contains("_sxby")){
                                    String dataV = sjjm(sfzh);
                                    String sfzhTm = desensitize(dataV);
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                }else {
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", sfzh);
                                }
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }else {
                            String sfzh = desensitize(cjxtMbglPz.getDataValue());
                            if(!"".equals(sfzh) && sfzh!=null){
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", sfzh);
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }
                    }
                    //手机号
                    if("2".equals(cjxtMbglPz.getDbJylx())){
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            String phone = (String) map.get(cjxtMbglPz.getDbFieldName());
                            if(!"".equals(phone) && phone!=null){
                                if(phone.contains("_sxby")){
                                    String dataV = sjjm(phone);
                                    String phoneTm = maskPhone(dataV);
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                }else {
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", phone);
                                }
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }else {
                            String phone = maskPhone(cjxtMbglPz.getDataValue());
                            if(!"".equals(phone) && phone!=null){
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", phone);
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }
                    }
                }
            }

            //处理模板数据
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode));
            StringBuilder sql = new StringBuilder();
            if ((idValue == null || "".equals(idValue)) && addOrUpt == true && isAddressDis == true) {
                StringBuilder sqlAdd = new StringBuilder();
                sqlAdd.append("INSERT INTO ");
                sqlAdd.append(cjxtMbgl.getBm());
                sqlAdd.append(" (");
                for (String key : map.keySet()) {
                    sqlAdd.append(key).append(",");
                }
                sqlAdd.setLength(sqlAdd.length() - 1);
                sqlAdd.append(") VALUES (");
                for (int i = 0; i < map.size(); i++) {
                    sqlAdd.append("?,");
                }
                sqlAdd.setLength(sqlAdd.length() - 1);
                sqlAdd.append(")");
                jdbcTemplate.update(sqlAdd.toString(), map.values().toArray());
                //人口采集判断当前用户信息是否已存在 存在添加预警数据
                if ("RY001".equals(cjxtMbgl.getMbbh())) {
                    String addressId = (String) map.get("address_id");
                    String address = (String) map.get("address");
                    String rysfzh = (String) map.get("rysfzh");
                    String ryxmN = (String) map.get("ryxm");
                    String sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' ORDER BY create_time DESC ";
                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                    if (resultList.size() > 1) {
                        Map<String, Object> secondResult = resultList.get(1);
                        String id = (String) secondResult.get("id");
                        String createBy = (String) secondResult.get("create_by");
                        String dataId = (String) secondResult.get("data_id");
                        String ryxm = (String) secondResult.get("ryxm");
                        CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getById(dataId);
                        if (dispatch != null) {
                            CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressId);
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

                            CjxtStandardAddress disAddress = cjxtStandardAddressService.getById(dispatch.getAddressId());
                            String disAddressName = "";
                            if ("1".equals(disAddress.getDzType())) {
                                //小区名
                                if (disAddress.getDz1Xqm() != null && !"".equals(disAddress.getDz1Xqm())) {
                                    disAddressName = disAddressName + disAddress.getDz1Xqm();
                                }
                                //楼栋
                                if (disAddress.getDz1Ld() != null && !"".equals(disAddress.getDz1Ld())) {
                                    disAddressName = disAddressName + disAddress.getDz1Ld() + "号楼";
                                }
                                //单元
                                if (disAddress.getDz1Dy() != null && !"".equals(disAddress.getDz1Dy())) {
                                    disAddressName = disAddressName + disAddress.getDz1Dy() + "单元";
                                }
                                //室
                                if (disAddress.getDz1S() != null && !"".equals(disAddress.getDz1S())) {
                                    disAddressName = disAddressName + disAddress.getDz1S() + "室";
                                }
                            } else if ("2".equals(disAddress.getDzType())) {
                                disAddress.setDetailMc(disAddress.getDz2Cm());
                                //村名
                                if (disAddress.getDz2Cm() != null && !"".equals(disAddress.getDz2Cm())) {
                                    disAddressName = disAddressName + disAddress.getDz2Cm();
                                }
                                //组名
                                if (disAddress.getDz2Zm() != null && !"".equals(disAddress.getDz2Zm())) {
                                    disAddressName = disAddressName + disAddress.getDz2Zm() + "组";
                                }
                                //号名
                                if (disAddress.getDz2Hm() != null && !"".equals(disAddress.getDz2Hm())) {
                                    disAddressName = disAddressName + disAddress.getDz2Hm() + "号";
                                }

                            } else if ("3".equals(disAddress.getDzType())) {
                                disAddress.setDetailMc(disAddress.getDz3Dsm());
                                //大厦名
                                if (disAddress.getDz3Dsm() != null && !"".equals(disAddress.getDz3Dsm())) {
                                    disAddressName = disAddressName + disAddress.getDz3Dsm();
                                }
                                //楼栋名
                                if (disAddress.getDz3Ldm() != null && !"".equals(disAddress.getDz3Ldm())) {
                                    disAddressName = disAddressName + disAddress.getDz3Ldm() + "栋";
                                }
                                //室名
                                if (disAddress.getDz3Sm() != null && !"".equals(disAddress.getDz3Sm())) {
                                    disAddressName = disAddressName + disAddress.getDz3Sm() + "室";
                                }
                            } else if ("4".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                            } else if ("5".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                                if (disAddress.getDz5P() != null && !"".equals(disAddress.getDz5P())) {
                                    disAddressName = disAddressName + disAddress.getDz5P() + "排";
                                }
                                if (disAddress.getDz5H() != null && !"".equals(disAddress.getDz5H())) {
                                    disAddressName = disAddressName + disAddress.getDz5H() + "号";
                                }
                                if (disAddress.getDz5S() != null && !"".equals(disAddress.getDz5S())) {
                                    disAddressName = disAddressName + disAddress.getDz5S() + "室";
                                }

                            } else if ("6".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                                if (disAddress.getDz6S() != null && !"".equals(disAddress.getDz6S())) {
                                    disAddressName = disAddressName + disAddress.getDz6S() + "室";
                                }
                            } else if ("99".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                            }

                            //迁出网格员接受信息
                            CjxtWarningMessage messageSec = new CjxtWarningMessage();
                            SysUser sysUserSec = sysUserService.getUserByName(createBy);
                            messageSec.setUserId(sysUserSec.getId());
                            messageSec.setUsername(createBy);
                            messageSec.setRealname(sysUserSec.getRealname());
                            messageSec.setMessage(ryxm + "已从(" + disAddressName + ")地址迁出");
                            messageSec.setStatus("1");
                            messageSec.setDataId(id);
                            messageSec.setBm(cjxtMbgl.getBm());
                            messageSec.setMsgType("0"); //预警消息
                            cjxtWarningMessageService.save(messageSec);

                            //WebSocket消息推送
                            if(sysUserSec.getId()!=null){
                                JSONObject json = new JSONObject();
                                json.put("msgType", "waMsg");
                                String msg = json.toString();
                                webSocket.sendOneMessage(sysUserSec.getId(), msg);
                            }

                            //迁入网格员接受信息
                            CjxtWarningMessage messageNex = new CjxtWarningMessage();
                            messageNex.setUserId(sysUser.getId());
                            messageNex.setUsername(sysUser.getUsername());
                            messageNex.setRealname(sysUser.getRealname());
                            messageNex.setMessage(ryxmN + "已从原地址(" + disAddressName + ")迁入新地址(" + addressName + ")");
                            messageNex.setStatus("1");
                            messageNex.setDataId(id);
                            messageNex.setBm(cjxtMbgl.getBm());
                            messageNex.setMsgType("0"); //预警消息
                            cjxtWarningMessageService.save(messageNex);

                            //WebSocket消息推送
                            if(sysUser.getId()!=null){
                                JSONObject json = new JSONObject();
                                json.put("msgType", "waMsg");
                                String msg = json.toString();
                                webSocket.sendOneMessage(sysUser.getId(), msg);
                            }
                        }
                    }
                }
            }
            //定义补录表ID
            String blId = "";
            if (idValue!=null && !"".equals(idValue) && !idValue.isEmpty()) {
                if ("1".equals(cjxtMbgl.getSfls())) {
                    List<Map<String, Object>> lsData = jdbcTemplate.queryForList("SELECT * FROM " + cjxtMbgl.getBm() + "_ls" + " WHERE data_id_ls = '" + map.get("id") + "'");
                    if (lsData.size() == 0) {
                        // 查询cjxtMbgl.getBm()中的所有数据
                        List<Map<String, Object>> datalsInsert = jdbcTemplate.queryForList("SELECT * FROM " + cjxtMbgl.getBm() + " WHERE id = '" + map.get("id") + "'");

                        // 如果datalsInsert不为空，进行插入操作
                        if (!datalsInsert.isEmpty()) {
                            for (Map<String, Object> data : datalsInsert) {
                                // 构造插入语句
                                StringBuilder sqlInsert = new StringBuilder("INSERT INTO " + cjxtMbgl.getBm() + "_ls (id, data_id_ls");

                                // 动态拼接字段
                                StringBuilder values = new StringBuilder(" VALUES ('" + UUID.randomUUID().toString().replace("-", "") + "', '" + map.get("id") + "'");
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    String column = entry.getKey();
                                    Object value = entry.getValue();

                                    if (!"id".equals(column)) { // 避免插入原有的id
                                        sqlInsert.append(", ").append(column);
                                        if("".equals(value) || "null".equals(value) || value == null){
                                            values.append(", null ");
                                        }else {
                                            values.append(", '").append(value).append("'");
                                        }
                                    }
                                }
                                sqlInsert.append(")").append(values).append(")");

                                // 执行插入操作
                                jdbcTemplate.update(sqlInsert.toString());
                            }
                        }
                    }
                }
                Map<String, Object> uptMap = new HashMap<>();
                uptMap.putAll(map);
                uptMap.remove("mb_id");
                uptMap.remove("mb_name");
                uptMap.remove("table_name");
                uptMap.remove("address_id");
                uptMap.remove("address");
                uptMap.remove("longitude");
                uptMap.remove("latitude");
                if (uptMap.containsKey("blzt")) {
                    Object blztValue = uptMap.get("blzt");
                    if (!"1".equals(blztValue)) {
                        uptMap.remove("blzt");
                    }
                }
//                uptMap.remove("wszt");
                uptMap.remove("data_id");
                sql.append("UPDATE ");
                sql.append(cjxtMbgl.getBm());
                sql.append(" SET ");
                Map<String, Object> newMap = new HashMap<>();
                newMap.put("wszt", null);
                for (String key : uptMap.keySet()) {
                    if ("update_by".equals(key)) {
                        newMap.put("update_by", sysUser.getUsername());
                        uptMap.put("update_by", sysUser.getUsername());
                    } else {
                        newMap.put("update_by", sysUser.getUsername());
                    }
                    if ("update_time".equals(key)) {
                        newMap.put("update_time", formattedDateTime);
                        uptMap.put("update_time", formattedDateTime);
                    } else {
                        newMap.put("update_time", formattedDateTime);
                    }
                    if ("blzt".equals(key)) {
                        Object blztValue = uptMap.get("blzt");
//                        if ("1".equals(blztValue)) {
//                            blId = (String) uptMap.get("id");
//                        }
                        if(!"".equals(blztValue)){
                            blId = (String) uptMap.get("id");
                        }
                    }
                    if (!"id".equals(key)) {
                        if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                            if ("rybrlxdh".equals(key)) {
                                sql.append(key).append(" = '' ,");
                            }else {
                                sql.append(key).append(" = " + null + ",");
                            }
                        } else {
                            sql.append(key).append(" = '" + uptMap.get(key) + "',");
                        }
                    } else {
                        newMap.put("id", uuid);
                    }
                }
                map.putAll(newMap);
                sql.setLength(sql.length() - 1);

                if (addOrUpt == true || addOrUpt == false) {
                    if (addOrUpt == false && isAddressDis == true) {
                        sql.append(" WHERE del_flag = '0' AND " + dbOnlyName + " = '" + dbOnlyNameValue + "' ;");
                        jdbcTemplate.update(sql.toString());
                    } else if ((addOrUpt == false || addOrUpt == true) && isAddressDis == false) {
                        String[] addressId = addressIdNotNull.split(",");
                        for (int i = 0; i < addressId.length; i++) {
                            StringBuilder sqlBase = new StringBuilder(sql.toString());
                            sqlBase.append(" WHERE del_flag = '0' AND address_id = '" + addressId[i] + "' ");
                            if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                if (rysfzhBuilder.length() > 0) {
                                    rysfzhBuilder.setLength(rysfzhBuilder.length() - 1);
                                }
                                String[] rysfzhString = rysfzhBuilder.toString().split(",");
                                for (int j = 0; j < rysfzhString.length; j++) {
                                    String rysfzh = rysfzhString[j];
                                    sqlBase.append(" AND rysfzh = '" + rysfzh + "' ;");
                                    jdbcTemplate.update(sqlBase.toString());
                                }
                            } else if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                if (sfzhBuilder.length() > 0) {
                                    sfzhBuilder.setLength(sfzhBuilder.length() - 1);
                                }
                                String[] sfzhString = sfzhBuilder.toString().split(",");
                                for (int l = 0; l < sfzhString.length; l++) {
                                    String sfzh = sfzhString[l];
                                    sqlBase.append(" AND sfzh = '" + sfzh + "' ;");
                                    jdbcTemplate.update(sqlBase.toString());
                                }
                            } else {
                                sqlBase.append(" ;");
                                jdbcTemplate.update(sqlBase.toString());
                            }
                        }
                    } else {
                        sql.append(" WHERE del_flag = '0' AND id = '" + idValue + "'");
                        jdbcTemplate.update(sql.toString());
                    }
                }
                map.put("data_id_ls", idValue);
            }

            //数据大屏webSocke
            webSocket.sendMergedMessage();

            if ("1".equals(cjxtMbgl.getSfls())) {
                StringBuilder sqlLs = new StringBuilder();
                sqlLs.append("INSERT INTO ");
                sqlLs.append(cjxtMbgl.getBm() + "_ls");
                sqlLs.append(" (");
                for (String key : map.keySet()) {
                    sqlLs.append(key).append(",");
                }
                sqlLs.setLength(sqlLs.length() - 1);
                sqlLs.append(") VALUES (");
                for (int i = 0; i < map.size(); i++) {
                    sqlLs.append("?,");
                }
                sqlLs.setLength(sqlLs.length() - 1);
                sqlLs.append(")");
                jdbcTemplate.update(sqlLs.toString(), map.values().toArray());
            }
            //修改数据补录表中补录状态
            if (!"".equals(blId)) {
                CjxtDataReentry cjxtDataReentry = cjxtDataReentryService.getOne(new LambdaQueryWrapper<CjxtDataReentry>().eq(CjxtDataReentry::getBlzt,"1").eq(CjxtDataReentry::getBm, cjxtMbgl.getBm()).eq(CjxtDataReentry::getDataId, blId).last("LIMIT 1"));
                if(cjxtDataReentry!=null){
                    cjxtDataReentry.setBlzt("2");
                    cjxtDataReentryService.updateById(cjxtDataReentry);
                }
            }

            CjxtScoreRule cjxtScoreRule = cjxtScoreRuleService.getOne(new LambdaQueryWrapper<CjxtScoreRule>().eq(CjxtScoreRule::getMbCode, mbCode));
            if (cjxtScoreRule != null) {
                CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                cjxtScoreDetail.setUserId(sysUser.getId());
                cjxtScoreDetail.setUserName(sysUser.getRealname());
                cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                cjxtScoreDetail.setDataId(dataVId);
                cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                cjxtScoreDetailService.save(cjxtScoreDetail);
            }
            if (idValue == null || "".equals(idValue)) {
                return Result.ok("新增成功");
            } else  {
                return Result.ok("编辑成功");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("");
    }

    /**
     * PC模版管理 数据审核
     *
     * @param map
     * @return
     */
    @ApiOperation(value = "模板管理配置-PC数据审核", notes = "模板管理配置-PC数据审核")
    @PostMapping(value = "/sjshPcValue")
    public Result<String> sjshPcValue(@RequestBody Map<String, Object> map) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            //取出模版ID
            String mbId = (String) map.get("mb_id");
            String id = (String) map.get("id");

            if (!"".equals(mbId)) {
                CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbId);
                if (cjxtMbgl != null) {
                    String sql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = '1' WHERE id = '" + id + "' ;";
                    jdbcTemplate.update(sql);
                    if ("1".equals(cjxtMbgl.getSfls())) {
                        String sqlLs = "UPDATE " + cjxtMbgl.getBm() + "_ls" + " SET wszt = '1' WHERE data_id_ls = '" + id + "' ;";
                        jdbcTemplate.update(sqlLs);
                    }
                    return Result.ok("审核成功");
                } else {
                    return Result.error("当前模版信息有误,数据审核失败!!!");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("");
    }

    @ApiOperation(value = "模板管理配置-APP数据审核", notes = "模板管理配置-APP数据审核")
    @GetMapping(value = "/sjshAppValue")
    public Result<String> sjshAppValue(@RequestParam(name = "addressId",required = false) String addressId,
                                       @RequestParam(name = "taskId",required = false) String taskId,
                                       @RequestParam(name = "userId",required = false) String userId,
                                       @RequestParam(name = "errMsg",required = false) String errMsg,
                                       @RequestParam(name = "id",required = false) String id,
                                       @RequestParam(name = "wszt",required = false) String wszt,
                                       @RequestParam(name = "mbCode",required = false) String mbCode) {
        try {
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            Date futureDate = calendar.getTime();

//            String msg = "";
//            if("0".equals(wszt)){
//                msg = "不通过";
//            }
//            if("1".equals(wszt)){
//                msg = "通过";
//            }


            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            //派发人员、部门
            SysUser disSysUser = sysUserService.getById(userId);
            SysDepart disSysdepart = null;
            if(disSysUser.getOrgCode()!=null){
                disSysdepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,disSysUser.getOrgCode()));
            }

            //任务信息
            CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getById(taskId);

            if (!"".equals(mbCode) && mbCode!=null) {
                CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
                //接收人、部门、部门负责人
                SysUser recSysUser = null;
                SysDepart recSysDepart = null;
                CjxtBmfzr cjxtBmfzr = null;

                if(cjxtMbgl != null){
                    //数据失败原因
                    StringBuilder sqlB = new StringBuilder();
                    if("0".equals(wszt)){
                        if("".equals(errMsg) || errMsg == null){
                            sqlB.append(", err_msg = NULL ");
                        }else {
                            sqlB.append(", err_msg = '"+errMsg+"' ");
                        }

                        //发送消息--网格员
                        CjxtWarningMessage messageNex = new CjxtWarningMessage();
                        if(cjxtTaskDispatch!=null && cjxtTaskDispatch.getReceiverId()!=null){
                            recSysUser = sysUserService.getById(cjxtTaskDispatch.getReceiverId());
                            if(recSysUser.getOrgCode()!=null && !"".equals(recSysUser.getOrgCode())){
                                recSysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,recSysUser.getOrgCode()));
                                if(recSysDepart!=null){
                                    cjxtBmfzr = cjxtBmfzrService.getOne(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,recSysDepart.getId()).last("LIMIT 1"));
                                }
                            }
                            messageNex.setUserId(recSysUser.getId());
                            messageNex.setUsername(recSysUser.getUsername());
                            messageNex.setRealname(recSysUser.getRealname());
                            if(errMsg!=null && !"".equals(errMsg)){
                                messageNex.setMessage(cjxtTaskDispatch.getTaskName()+",采集数据审核不通过,任务已重新下发!!! 不通过原因："+errMsg);
                            }else {
                                messageNex.setMessage(cjxtTaskDispatch.getTaskName()+",采集数据审核不通过,任务已重新下发!!!");
                            }
                        }
                        messageNex.setStatus("1");
                        messageNex.setBm(cjxtMbgl.getBm());
                        messageNex.setMsgType("1"); //提醒消息
                        cjxtWarningMessageService.save(messageNex);

                        //WebSocket消息推送
                        if(messageNex.getUserId()!=null){
                            JSONObject json = new JSONObject();
                            json.put("msgType", "waMsg");
                            String msgNew = json.toString();
                            webSocket.sendOneMessage(messageNex.getUserId(), msgNew);
                        }
                    }


                    //地址ID审核
                    String[] addressIds = null;
                    if(addressId!=null && !"".equals(addressId)){
                        addressIds = addressId.split(",");
                        for(int i=0;i<addressIds.length;i++){
                            String addressID = addressIds[i];
                            String sql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = '" + wszt + "'" + sqlB + " WHERE address_id = '" + addressID + "' ;";
                            jdbcTemplate.update(sql);
                            if ("1".equals(cjxtMbgl.getSfls())) {
                                String sqlLs = "UPDATE " + cjxtMbgl.getBm() + "_ls" + " SET wszt = '" + wszt + "'" + sqlB + " WHERE address_id = '" + addressID + "' ;";
                                jdbcTemplate.update(sqlLs);
                            }
                        }
//                        return Result.ok("操作成功");
                    }

                    //数据ID审核
                    if(id !=null && !"".equals(id)){
                        String sql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = '" + wszt + "'" + sqlB + " WHERE id = '" + id + "' ;";
                        jdbcTemplate.update(sql);
                        if ("1".equals(cjxtMbgl.getSfls())) {
                            String sqlLs = "UPDATE " + cjxtMbgl.getBm() + "_ls" + " SET wszt = '" + wszt + "'" + sqlB + " WHERE data_id_ls = '" + id + "' ;";
                            jdbcTemplate.update(sqlLs);
                        }
//                        return Result.ok("操作成功");
                    }

                    String ids[] = null;
                    if(!"".equals(id) && id != null){
                        String addressSql = "SELECT address_id FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND id = '" + id + "'";
                        List<Map<String, Object>> addressList = jdbcTemplate.queryForList(addressSql);
                        for (Map<String, Object> row : addressList) {
                            String addressid = (String) row.get("address_id");
                            ids = addressid.split(",");
                        }
                    }else if(!"".equals(addressId) && addressId != null){
                        ids = addressId.split(",");
                    }

                    if("1".equals(wszt) && cjxtTaskDispatch!=null){
                        cjxtTaskDispatch.setWszt(wszt);
                        cjxtTaskDispatch.setErrMsg(errMsg);
                        cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
                    }

                    if("0".equals(wszt)){
                        //新任务派发
                        CjxtTask cjxtTask = new CjxtTask();
                        String uuid = UUID.randomUUID().toString().replace("-","");
                        cjxtTask.setId(uuid);
                        cjxtTask.setTaskCode(uuid);
                        //节点信息
                        cjxtTask.setPid("0");
                        cjxtTask.setHasChild("1");
                        //任务信息
                        if(cjxtTaskDispatch.getTaskName().contains("_数据不完整")){
                            cjxtTask.setTaskName(cjxtTaskDispatch.getTaskName());
                        }else {
                            cjxtTask.setTaskName(cjxtTaskDispatch.getTaskName()+"_数据不完整");
                        }
                        cjxtTask.setTaskDescription(cjxtTaskDispatch.getTaskDescription());
                        //模板信息
                        cjxtTask.setMbId(cjxtTaskDispatch.getMbId());
                        cjxtTask.setMbName(cjxtTaskDispatch.getMbName());
                        cjxtTask.setMbCode(cjxtTaskDispatch.getMbCode());
                        cjxtTask.setBm(cjxtTaskDispatch.getBm());
                        //部门数据权限==派发部门
                        cjxtTask.setOrgId(disSysdepart.getId());
                        cjxtTask.setOrgCode(disSysdepart.getOrgCode());
                        cjxtTask.setOrgName(disSysdepart.getDepartNameFull());
                        //当前派发部门
                        cjxtTask.setDispatcherOrgId(disSysdepart.getId());
                        cjxtTask.setDispatcherOrgCode(disSysdepart.getOrgCode());
                        cjxtTask.setDispatcherOrgName(disSysdepart.getDepartNameFull());
                        //当前派发人
                        cjxtTask.setDispatcherId(disSysUser.getId());
                        cjxtTask.setDispatcherName(disSysUser.getRealname());
                        //接收部门、部门负责人信息
                        cjxtTask.setReceiverOrgId(recSysDepart.getId());
                        cjxtTask.setReceiverOrgCode(recSysDepart.getOrgCode());
                        cjxtTask.setReceiverOrgName(recSysDepart.getDepartNameFull());
                        if(cjxtBmfzr!=null){
                            cjxtTask.setReceiverBmfzrId(cjxtBmfzr.getFzryId());
                            cjxtTask.setReceiverBmfzrZh(cjxtBmfzr.getFzryZh());
                            cjxtTask.setReceiverBmfzrName(cjxtBmfzr.getFzryName());
                        }
                        //接收人信息：：：接收人为空存入部门负责人信息
                        cjxtTask.setReceiverId(recSysUser.getId());
                        cjxtTask.setReceiverZh(recSysUser.getUsername());
                        cjxtTask.setReceiverName(recSysUser.getRealname());
                        cjxtTask.setDueDate(futureDate);
                        //采集情况
                        if(ids.length>0){
                            cjxtTask.setCjZs(ids.length);
                            cjxtTask.setCjSy(ids.length);
                        }else {
                            cjxtTask.setCjSy(0);
                            cjxtTask.setCjSy(0);
                        }
                        cjxtTask.setCjYwc(0);
                        cjxtTask.setCjWcqk("0%");
                        cjxtTask.setRwzt("2");
                        cjxtTask.setChzt("1");
                        cjxtTaskService.save(cjxtTask);

                        CjxtTask cjxtTaskC = new CjxtTask();
                        BeanUtils.copyProperties(cjxtTask, cjxtTaskC);
                        String uuidC = UUID.randomUUID().toString().replace("-","");
                        cjxtTaskC.setId(uuidC);
                        cjxtTaskC.setPid(cjxtTask.getId());
                        cjxtTaskC.setHasChild("0");
                        cjxtTaskService.save(cjxtTaskC);

                        for(int i=0;i<ids.length;i++) {
                            String addressid = ids[i];
                            CjxtStandardAddress standardAddress = cjxtStandardAddressService.getById(addressid);
                            CjxtTaskDispatch cjxtTaskDispatchNew = new CjxtTaskDispatch();
                            cjxtTaskDispatchNew.setTaskId(cjxtTaskC.getId());
                            cjxtTaskDispatchNew.setTaskCode(cjxtTaskC.getTaskCode());
                            cjxtTaskDispatchNew.setTaskName(cjxtTaskC.getTaskName());
                            cjxtTaskDispatchNew.setTaskDescription(cjxtTaskC.getTaskDescription());
                            cjxtTaskDispatchNew.setMbId(cjxtTaskC.getMbId());
                            cjxtTaskDispatchNew.setMbCode(cjxtTaskC.getMbCode());
                            cjxtTaskDispatchNew.setMbName(cjxtTaskC.getMbName());
                            cjxtTaskDispatchNew.setBm(cjxtTaskC.getBm());
                            cjxtTaskDispatchNew.setAddressId(standardAddress.getId());
                            cjxtTaskDispatchNew.setAddressCode(standardAddress.getAddressCodeMz());
                            cjxtTaskDispatchNew.setAddressName(standardAddress.getAddressName());
                            cjxtTaskDispatchNew.setDispatcherId(cjxtTaskC.getDispatcherId());
                            cjxtTaskDispatchNew.setDispatcherName(cjxtTaskC.getDispatcherName());
                            cjxtTaskDispatchNew.setReceiverId(cjxtTaskC.getReceiverId());
                            cjxtTaskDispatchNew.setReceiverName(cjxtTaskC.getReceiverName());
                            cjxtTaskDispatchNew.setDueDate(cjxtTaskC.getDueDate());
                            cjxtTaskDispatchNew.setRwzt("2");
                            cjxtTaskDispatchNew.setErrMsg(errMsg);
                            cjxtTaskDispatchNew.setWszt(null);
                            String sql = "SELECT id,create_time,update_time FROM " + cjxtTaskC.getBm() + " WHERE del_flag = '0' AND address_id = '" + standardAddress.getId() +"' ORDER BY create_time ASC LIMIT 1";
                            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
                            Date createTime = null;
                            Date updateTime = null;
                            if(resultList.size()>0){
                                Map<String, Object> row = resultList.get(0);
                                Object idD = row.get("id");
                                LocalDateTime create_time = (LocalDateTime) row.get("create_time");
                                LocalDateTime update_time = (LocalDateTime) row.get("update_time");
                                if(create_time != null){
                                    createTime = java.sql.Timestamp.valueOf(create_time);
                                }
                                if(update_time != null){
                                    updateTime = java.sql.Timestamp.valueOf(update_time);
                                }
                                cjxtTaskDispatchNew.setDataId(String.valueOf(idD));
                                if(updateTime!=null){
                                    cjxtTaskDispatchNew.setSchssj(updateTime);
                                }else {
                                    cjxtTaskDispatchNew.setSchssj(createTime);
                                }
                                cjxtTaskDispatchNew.setHszt("2");
                            }
                            cjxtTaskDispatchService.save(cjxtTaskDispatchNew);
                        }
                    }
                    return Result.ok("操作成功");
                }else {
                    return Result.error("当前模版信息有误,数据审核失败!!!");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("操作失败");
    }

    @ApiOperation(value = "模板管理配置-民警APP数据批量审核", notes = "模板管理配置-民警APP数据批量审核")
    @PostMapping(value = "/batchAppValue")
    public Result<String> batchAppValue(@RequestParam(name = "addressId",required = false) String addressId,
                                       @RequestParam(name = "taskId",required = false) String taskId,
                                       @RequestParam(name = "userId",required = false) String userId,
                                       @RequestParam(name = "errMsg",required = false) String errMsg,
                                       @RequestParam(name = "id",required = false) String id,
                                       @RequestParam(name = "wszt",required = false) String wszt,
                                       @RequestParam(name = "mbCode",required = false) String mbCode) {
        try {
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            Date futureDate = calendar.getTime();

//            String msg = "";
//            if("0".equals(wszt)){
//                msg = "不通过";
//            }
//            if("1".equals(wszt)){
//                msg = "通过";
//            }


            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            //派发人员、部门
            SysUser disSysUser = sysUserService.getById(userId);
            SysDepart disSysdepart = null;
            if(disSysUser.getOrgCode()!=null){
                disSysdepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,disSysUser.getOrgCode()));
            }

            //任务信息
            CjxtTaskDispatch cjxtTaskDispatch = null;
            String TASKID[] = taskId.split(",");
            if(!"".equals(taskId)){
                for(int i = 0;i< TASKID.length;i++){
                    String taskID = TASKID[i];
                    cjxtTaskDispatch = cjxtTaskDispatchService.getById(taskID);

                    if (!"".equals(mbCode) && mbCode!=null) {
                        CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
                        //接收人、部门、部门负责人
                        SysUser recSysUser = null;
                        SysDepart recSysDepart = null;
                        CjxtBmfzr cjxtBmfzr = null;

                        if(cjxtMbgl != null){
                            //数据失败原因
                            StringBuilder sqlB = new StringBuilder();
                            if("0".equals(wszt)){
                                if("".equals(errMsg) || errMsg == null){
                                    sqlB.append(", err_msg = NULL ");
                                }else {
                                    sqlB.append(", err_msg = '"+errMsg+"' ");
                                }

                                //发送消息--网格员
                                CjxtWarningMessage messageNex = new CjxtWarningMessage();
                                if(cjxtTaskDispatch!=null && cjxtTaskDispatch.getReceiverId()!=null){
                                    recSysUser = sysUserService.getById(cjxtTaskDispatch.getReceiverId());
                                    if(recSysUser.getOrgCode()!=null && !"".equals(recSysUser.getOrgCode())){
                                        recSysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,recSysUser.getOrgCode()));
                                        if(recSysDepart!=null){
                                            cjxtBmfzr = cjxtBmfzrService.getOne(new LambdaQueryWrapper<CjxtBmfzr>().eq(CjxtBmfzr::getBmid,recSysDepart.getId()).last("LIMIT 1"));
                                        }
                                    }
                                    messageNex.setUserId(recSysUser.getId());
                                    messageNex.setUsername(recSysUser.getUsername());
                                    messageNex.setRealname(recSysUser.getRealname());
                                    if(errMsg!=null && !"".equals(errMsg)){
                                        messageNex.setMessage(cjxtTaskDispatch.getTaskName()+",采集数据审核不通过,任务已重新下发!!! 不通过原因："+errMsg);
                                    }else {
                                        messageNex.setMessage(cjxtTaskDispatch.getTaskName()+",采集数据审核不通过,任务已重新下发!!!");
                                    }
                                }
                                messageNex.setStatus("1");
                                messageNex.setBm(cjxtMbgl.getBm());
                                messageNex.setMsgType("1"); //提醒消息
                                cjxtWarningMessageService.save(messageNex);

                                //WebSocket消息推送
                                if(messageNex.getUserId()!=null){
                                    JSONObject json = new JSONObject();
                                    json.put("msgType", "waMsg");
                                    String msgNew = json.toString();
                                    webSocket.sendOneMessage(messageNex.getUserId(), msgNew);
                                }
                            }


                            //地址ID审核
                            String[] addressIds = null;
                            if(addressId!=null && !"".equals(addressId)){
                                addressIds = addressId.split(",");
                                for(int j=0;j<addressIds.length;j++){
                                    String addressID = addressIds[j];
                                    String sql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = '" + wszt + "'" + sqlB + " WHERE address_id = '" + addressID + "' ;";
                                    jdbcTemplate.update(sql);
                                    if ("1".equals(cjxtMbgl.getSfls())) {
                                        String sqlLs = "UPDATE " + cjxtMbgl.getBm() + "_ls" + " SET wszt = '" + wszt + "'" + sqlB + " WHERE address_id = '" + addressID + "' ;";
                                        jdbcTemplate.update(sqlLs);
                                    }
                                }
//                        return Result.ok("操作成功");
                            }

                            //数据ID审核
                            if(id !=null && !"".equals(id)){
                                String sql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = '" + wszt + "'" + sqlB + " WHERE id = '" + id + "' ;";
                                jdbcTemplate.update(sql);
                                if ("1".equals(cjxtMbgl.getSfls())) {
                                    String sqlLs = "UPDATE " + cjxtMbgl.getBm() + "_ls" + " SET wszt = '" + wszt + "'" + sqlB + " WHERE data_id_ls = '" + id + "' ;";
                                    jdbcTemplate.update(sqlLs);
                                }
//                        return Result.ok("操作成功");
                            }

                            String ids[] = null;
                            if(!"".equals(id) && id != null){
                                String addressSql = "SELECT address_id FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND id = '" + id + "'";
                                List<Map<String, Object>> addressList = jdbcTemplate.queryForList(addressSql);
                                for (Map<String, Object> row : addressList) {
                                    String addressid = (String) row.get("address_id");
                                    ids = addressid.split(",");
                                }
                            }else if(!"".equals(addressId) && addressId != null){
                                ids = addressId.split(",");
                            }

                            if("1".equals(wszt) && cjxtTaskDispatch!=null){
                                cjxtTaskDispatch.setWszt(wszt);
                                cjxtTaskDispatch.setErrMsg(errMsg);
                                cjxtTaskDispatchService.updateById(cjxtTaskDispatch);
                            }

                            if("0".equals(wszt)){
                                //新任务派发
                                CjxtTask cjxtTask = new CjxtTask();
                                String uuid = UUID.randomUUID().toString().replace("-","");
                                cjxtTask.setId(uuid);
                                cjxtTask.setTaskCode(uuid);
                                //节点信息
                                cjxtTask.setPid("0");
                                cjxtTask.setHasChild("1");
                                //任务信息
                                if(cjxtTaskDispatch.getTaskName().contains("_数据不完整")){
                                    cjxtTask.setTaskName(cjxtTaskDispatch.getTaskName());
                                }else {
                                    cjxtTask.setTaskName(cjxtTaskDispatch.getTaskName()+"_数据不完整");
                                }
                                cjxtTask.setTaskDescription(cjxtTaskDispatch.getTaskDescription());
                                //模板信息
                                cjxtTask.setMbId(cjxtTaskDispatch.getMbId());
                                cjxtTask.setMbName(cjxtTaskDispatch.getMbName());
                                cjxtTask.setMbCode(cjxtTaskDispatch.getMbCode());
                                cjxtTask.setBm(cjxtTaskDispatch.getBm());
                                //部门数据权限==派发部门
                                cjxtTask.setOrgId(disSysdepart.getId());
                                cjxtTask.setOrgCode(disSysdepart.getOrgCode());
                                cjxtTask.setOrgName(disSysdepart.getDepartNameFull());
                                //当前派发部门
                                cjxtTask.setDispatcherOrgId(disSysdepart.getId());
                                cjxtTask.setDispatcherOrgCode(disSysdepart.getOrgCode());
                                cjxtTask.setDispatcherOrgName(disSysdepart.getDepartNameFull());
                                //当前派发人
                                cjxtTask.setDispatcherId(disSysUser.getId());
                                cjxtTask.setDispatcherName(disSysUser.getRealname());
                                //接收部门、部门负责人信息
                                cjxtTask.setReceiverOrgId(recSysDepart.getId());
                                cjxtTask.setReceiverOrgCode(recSysDepart.getOrgCode());
                                cjxtTask.setReceiverOrgName(recSysDepart.getDepartNameFull());
                                if(cjxtBmfzr!=null){
                                    cjxtTask.setReceiverBmfzrId(cjxtBmfzr.getFzryId());
                                    cjxtTask.setReceiverBmfzrZh(cjxtBmfzr.getFzryZh());
                                    cjxtTask.setReceiverBmfzrName(cjxtBmfzr.getFzryName());
                                }
                                //接收人信息：：：接收人为空存入部门负责人信息
                                cjxtTask.setReceiverId(recSysUser.getId());
                                cjxtTask.setReceiverZh(recSysUser.getUsername());
                                cjxtTask.setReceiverName(recSysUser.getRealname());
                                cjxtTask.setDueDate(futureDate);
                                //采集情况
                                if(ids.length>0){
                                    cjxtTask.setCjZs(ids.length);
                                    cjxtTask.setCjSy(ids.length);
                                }else {
                                    cjxtTask.setCjSy(0);
                                    cjxtTask.setCjSy(0);
                                }
                                cjxtTask.setCjYwc(0);
                                cjxtTask.setCjWcqk("0%");
                                cjxtTask.setRwzt("2");
                                cjxtTask.setChzt("1");
                                cjxtTaskService.save(cjxtTask);

                                CjxtTask cjxtTaskC = new CjxtTask();
                                BeanUtils.copyProperties(cjxtTask, cjxtTaskC);
                                String uuidC = UUID.randomUUID().toString().replace("-","");
                                cjxtTaskC.setId(uuidC);
                                cjxtTaskC.setPid(cjxtTask.getId());
                                cjxtTaskC.setHasChild("0");
                                cjxtTaskService.save(cjxtTaskC);

                                for(int k=0;k<ids.length;k++) {
                                    String addressid = ids[k];
                                    CjxtStandardAddress standardAddress = cjxtStandardAddressService.getById(addressid);
                                    CjxtTaskDispatch cjxtTaskDispatchNew = new CjxtTaskDispatch();
                                    cjxtTaskDispatchNew.setTaskId(cjxtTaskC.getId());
                                    cjxtTaskDispatchNew.setTaskCode(cjxtTaskC.getTaskCode());
                                    cjxtTaskDispatchNew.setTaskName(cjxtTaskC.getTaskName());
                                    cjxtTaskDispatchNew.setTaskDescription(cjxtTaskC.getTaskDescription());
                                    cjxtTaskDispatchNew.setMbId(cjxtTaskC.getMbId());
                                    cjxtTaskDispatchNew.setMbCode(cjxtTaskC.getMbCode());
                                    cjxtTaskDispatchNew.setMbName(cjxtTaskC.getMbName());
                                    cjxtTaskDispatchNew.setBm(cjxtTaskC.getBm());
                                    cjxtTaskDispatchNew.setAddressId(standardAddress.getId());
                                    cjxtTaskDispatchNew.setAddressCode(standardAddress.getAddressCodeMz());
                                    cjxtTaskDispatchNew.setAddressName(standardAddress.getAddressName());
                                    cjxtTaskDispatchNew.setDispatcherId(cjxtTaskC.getDispatcherId());
                                    cjxtTaskDispatchNew.setDispatcherName(cjxtTaskC.getDispatcherName());
                                    cjxtTaskDispatchNew.setReceiverId(cjxtTaskC.getReceiverId());
                                    cjxtTaskDispatchNew.setReceiverName(cjxtTaskC.getReceiverName());
                                    cjxtTaskDispatchNew.setDueDate(cjxtTaskC.getDueDate());
                                    cjxtTaskDispatchNew.setRwzt("2");
                                    cjxtTaskDispatchNew.setErrMsg(errMsg);
                                    cjxtTaskDispatchNew.setWszt(null);
                                    String sql = "SELECT id,create_time,update_time FROM " + cjxtTaskC.getBm() + " WHERE del_flag = '0' AND address_id = '" + standardAddress.getId() +"' ORDER BY create_time ASC LIMIT 1";
                                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
                                    Date createTime = null;
                                    Date updateTime = null;
                                    if(resultList.size()>0){
                                        Map<String, Object> row = resultList.get(0);
                                        Object idD = row.get("id");
                                        LocalDateTime create_time = (LocalDateTime) row.get("create_time");
                                        LocalDateTime update_time = (LocalDateTime) row.get("update_time");
                                        if(create_time != null){
                                            createTime = java.sql.Timestamp.valueOf(create_time);
                                        }
                                        if(update_time != null){
                                            updateTime = java.sql.Timestamp.valueOf(update_time);
                                        }
                                        cjxtTaskDispatchNew.setDataId(String.valueOf(idD));
                                        if(updateTime!=null){
                                            cjxtTaskDispatchNew.setSchssj(updateTime);
                                        }else {
                                            cjxtTaskDispatchNew.setSchssj(createTime);
                                        }
                                        cjxtTaskDispatchNew.setHszt("2");
                                    }
                                    cjxtTaskDispatchService.save(cjxtTaskDispatchNew);
                                }
                            }
                        }else {
                            return Result.error("当前模版信息有误,数据审核失败!!!");
                        }
                    }
                }
            }

            if(cjxtTaskDispatch!=null){
                List<CjxtTaskDispatch> cjxtTaskDispatchList = cjxtTaskDispatchService.list(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getTaskId,cjxtTaskDispatch.getTaskId()).isNull(CjxtTaskDispatch::getWszt));
                if(cjxtTaskDispatchList.size() == 0){
                    CjxtTask cjxtTask = cjxtTaskService.getById(cjxtTaskDispatch.getTaskId());
                    cjxtTask.setRwzt("4");
                    cjxtTaskService.updateById(cjxtTask);
                }
            }

            return Result.ok("操作成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("操作失败");
    }

    /**
     * PC模版管理 数据审核
     *
     * @param mapBatch
     * @return
     */
    @ApiOperation(value = "模板管理配置-PC数据批量审核", notes = "模板管理配置-PC数据批量审核")
    @PostMapping(value = "/sjshPcValueBatch")
    public Result<String> sjshPcValueBatch(@RequestBody List<Map<String, Object>> mapBatch) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);
            boolean mbStatus = true;
            for (Map<String, Object> map : mapBatch) {
                //取出模版ID
                String mbId = (String) map.get("mb_id");
                String id = (String) map.get("id");
                if (!"".equals(mbId)) {
                    CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbId);
                    if (cjxtMbgl != null) {
                        String sql = "UPDATE " + cjxtMbgl.getBm() + " SET wszt = '1', update_time = '" + formattedDateTime + "', update_by = '" + sysUser.getUsername() + "' WHERE id = '" + id + "' ;";
                        jdbcTemplate.update(sql);
                        if ("1".equals(cjxtMbgl.getSfls())) {
                            String sqlLs = "UPDATE " + cjxtMbgl.getBm() + "_ls" + " SET wszt = '1', update_time = '" + formattedDateTime + "', update_by = '" + sysUser.getUsername() + "' WHERE data_id_ls = '" + id + "' ;";
                            jdbcTemplate.update(sqlLs);
                        }
                    } else {
                        return Result.error("当前模版信息有误,数据审核失败!!!");
                    }
                }
            }
            if (mbStatus == true) {
                return Result.ok("审核成功");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("");
    }

    /**
     * PC模版管理 数据上报审核
     *
     * @param map
     * @return
     */
    @ApiOperation(value = "模板管理配置-数据上报审核", notes = "模板管理配置-数据上报审核")
    @PostMapping(value = "/sbshPcValue")
    public Result<String> sbshPcValue(@RequestBody Map<String, Object> map) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            //取出模版ID
            String mbId = (String) map.get("mb_id");
            String id = (String) map.get("id");
            String tableName = (String) map.get("table_name");

            String dbOnlyName = "";//唯一字段名
            String onlyNameValue = "";//唯一字段值
            String onlyTableName = "";//唯一表名称
            boolean addOrUpt = true;
            if (!"".equals(mbId)) {
                CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbId);
                if (cjxtMbgl != null) {
                    if (cjxtMbgl != null) {
                        onlyTableName = cjxtMbgl.getBm();
                        dbOnlyName = cjxtMbgl.getDbOnly();
                    }
                    //唯一字段不等于空进入
                    if (!"".equals(dbOnlyName) && dbOnlyName != null && !dbOnlyName.isEmpty()) {
                        if (map.containsKey(dbOnlyName)) {
                            onlyNameValue = (String) map.get(dbOnlyName);
//							CjxtMbglPz cjxtMbglPz = cjxtMbglPzService.getOne(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId,cjxtMbgl.getId()).eq(CjxtMbglPz::getDbFieldName,dbOnlyName));
                            String sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + dbOnlyName + " = '" + onlyNameValue + "' ;";
                            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                            if (resultList.size() > 0) {
                                StringBuilder onlyUpt = new StringBuilder();
                                Map<String, Object> uptMap = new HashMap<>();
                                uptMap = map;
                                uptMap.remove("id");
                                uptMap.remove("create_by");
                                uptMap.remove("create_time");
                                uptMap.remove("sys_org_code");
                                uptMap.remove("del_flag");
                                uptMap.remove("mb_id");
                                uptMap.remove("mb_name");
                                uptMap.remove("table_name");
                                uptMap.remove("address_id");
                                uptMap.remove("address");
                                uptMap.remove("longitude");
                                uptMap.remove("latitude");
                                uptMap.remove("blzt");
                                uptMap.remove("wszt");
                                uptMap.remove("data_id");
                                uptMap.remove("shzt");
                                onlyUpt.append("UPDATE ");
                                onlyUpt.append(cjxtMbgl.getBm());
                                onlyUpt.append(" SET ");
                                for (String key : uptMap.keySet()) {
                                    if (uptMap.containsKey("update_by")) {
                                        uptMap.put("update_by", sysUser.getUsername());
                                    } else {
                                        uptMap.put("update_by", sysUser.getUsername());
                                    }
                                    if (uptMap.containsKey("update_time")) {
                                        uptMap.put("update_time", formattedDateTime);
                                    } else {
                                        uptMap.put("update_time", formattedDateTime);
                                    }
                                    if (!"id".equals(key)) {
                                        if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                            onlyUpt.append(key).append(" = " + null + ",");
                                        } else {
                                            onlyUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                        }
                                    }
                                }
                                onlyUpt.append(" WHERE del_flag = '0' AND " + dbOnlyName + " = '" + onlyNameValue + "' ;");
                                jdbcTemplate.update(onlyUpt.toString());
                                if ("1".equals(cjxtMbgl.getSfls())) {
                                    onlyUpt.append("UPDATE ");
                                    onlyUpt.append(cjxtMbgl.getBm() + "_ls");
                                    onlyUpt.append(" SET ");
                                    for (String key : uptMap.keySet()) {
                                        if (uptMap.containsKey("update_by")) {
                                            uptMap.put("update_by", sysUser.getUsername());
                                        } else {
                                            uptMap.put("update_by", sysUser.getUsername());
                                        }
                                        if (uptMap.containsKey("update_time")) {
                                            uptMap.put("update_time", formattedDateTime);
                                        } else {
                                            uptMap.put("update_time", formattedDateTime);
                                        }
                                        if (!"id".equals(key)) {
                                            if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                                onlyUpt.append(key).append(" = " + null + ",");
                                            } else {
                                                onlyUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                            }
                                        }
                                    }
                                    onlyUpt.append(" WHERE del_flag = '0' AND " + dbOnlyName + " = '" + onlyNameValue + "' ;");
                                    jdbcTemplate.update(onlyUpt.toString());
                                }
                                addOrUpt = false;
                            }
                        }
                    }

                    //判断当前模版为从业人员模版
                    if ("RY002".equals(cjxtMbgl.getMbbh()) || "RY001".equals(cjxtMbgl.getMbbh())) {
                        if ("RY002".equals(cjxtMbgl.getMbbh())) {
                            String bm = (String) map.get("bm");
                            String sfzh = (String) map.get("sfzh");
                            String bmValue = "";
                            if (!"".equals(bm)) {
                                bmValue = bm;
                            }
                            if (!"".equals(sfzh) && !"".equals(bmValue)) {
                                String updateSql = "UPDATE cjxt_rkcj SET ryfwcsgzdw = '" + bmValue + "' WHERE del_flag = '0' AND rysfzh = '" + sfzh + "' ;";
                                jdbcTemplate.update(updateSql);
                            }
                        }
                        String addressID = (String) map.get("address_id");
                        String sqlOnly = "";
                        if ("RY001".equals(cjxtMbgl.getMbbh())) {
                            String rysfzh = (String) map.get("rysfzh");
                            sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' AND address_id = '" + addressID + "' ;";
                        }
                        if ("RY002".equals(cjxtMbgl.getMbbh())) {
                            String sfzh = (String) map.get("sfzh");
                            sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND sfzh = '" + sfzh + "' AND address_id = '" + addressID + "' ;";
                        }
                        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                        if (resultList.size() > 0) {
                            StringBuilder onlyUpt = new StringBuilder();
                            Map<String, Object> uptMap = new HashMap<>();
                            uptMap = map;
                            uptMap.remove("id");
                            uptMap.remove("create_by");
                            uptMap.remove("create_time");
                            uptMap.remove("sys_org_code");
                            uptMap.remove("del_flag");
                            uptMap.remove("mb_id");
                            uptMap.remove("mb_name");
                            uptMap.remove("table_name");
                            uptMap.remove("address_id");
                            uptMap.remove("address");
                            uptMap.remove("longitude");
                            uptMap.remove("latitude");
                            uptMap.remove("blzt");
                            uptMap.remove("wszt");
                            uptMap.remove("data_id");
                            uptMap.remove("shzt");
                            onlyUpt.append("UPDATE ");
                            onlyUpt.append(cjxtMbgl.getBm());
                            onlyUpt.append(" SET ");
                            for (String key : uptMap.keySet()) {
//                                if (uptMap.containsKey("wszt")) {
//                                    uptMap.put("wszt", "0");
//                                }
                                if (uptMap.containsKey("blzt")) {
                                    uptMap.put("blzt", "0");
                                }
                                if (uptMap.containsKey("update_by")) {
                                    uptMap.put("update_by", sysUser.getUsername());
                                } else {
                                    uptMap.put("update_by", sysUser.getUsername());
                                }
                                if (uptMap.containsKey("update_time")) {
                                    uptMap.put("update_time", formattedDateTime);
                                } else {
                                    uptMap.put("update_time", formattedDateTime);
                                }
                                if (!"id".equals(key)) {
                                    if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                        onlyUpt.append(key).append(" = " + null + ",");
                                    } else {
                                        onlyUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                    }
                                }
                            }
                            if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                String rysfzh = (String) map.get("rysfzh");
                                onlyUpt.append(" WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' AND address_id = '" + addressID + "' ;");
                            }
                            if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                String sfzh = (String) map.get("sfzh");
                                onlyUpt.append(" WHERE del_flag = '0' AND sfzh = '" + sfzh + "' AND address_id = '" + addressID + "' ;");
                            }
                            jdbcTemplate.update(onlyUpt.toString());
                            if ("1".equals(cjxtMbgl.getSfls())) {
                                onlyUpt.append("UPDATE ");
                                onlyUpt.append(cjxtMbgl.getBm() + "_ls");
                                onlyUpt.append(" SET ");
                                for (String key : uptMap.keySet()) {
//                                    if (uptMap.containsKey("wszt")) {
//                                        uptMap.put("wszt", "0");
//                                    }
                                    if (uptMap.containsKey("blzt")) {
                                        uptMap.put("blzt", "0");
                                    }
                                    if (uptMap.containsKey("update_by")) {
                                        uptMap.put("update_by", sysUser.getUsername());
                                    } else {
                                        uptMap.put("update_by", sysUser.getUsername());
                                    }
                                    if (uptMap.containsKey("update_time")) {
                                        uptMap.put("update_time", formattedDateTime);
                                    } else {
                                        uptMap.put("update_time", formattedDateTime);
                                    }
                                    if (!"id".equals(key)) {
                                        if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                            onlyUpt.append(key).append(" = " + null + ",");
                                        } else {
                                            onlyUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                        }
                                    } else {
                                        uptMap.put("id", onlyUpt);
                                    }
                                }
                                if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                    String rysfzh = (String) map.get("rysfzh");
                                    onlyUpt.append(" WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' AND address_id = '" + addressID + "' ;");
                                }
                                if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                    String sfzh = (String) map.get("sfzh");
                                    onlyUpt.append(" WHERE del_flag = '0' AND sfzh = '" + sfzh + "' AND address_id = '" + addressID + "' ;");
                                }
                                jdbcTemplate.update(onlyUpt.toString());
                            }
                            addOrUpt = false;
                        }
                    }

                    if (addOrUpt == true) {
                        StringBuilder sqlAdd = new StringBuilder();
                        sqlAdd.append("INSERT INTO ");
                        sqlAdd.append(cjxtMbgl.getBm());
                        sqlAdd.append(" (");
                        map.remove("shzt");
                        for (String key : map.keySet()) {
//                            if (map.containsKey("wszt")) {
//                                map.put("wszt", "0");
//                            }
                            if (map.containsKey("blzt")) {
                                map.put("blzt", "0");
                            }
                            sqlAdd.append(key).append(",");
                        }
                        sqlAdd.setLength(sqlAdd.length() - 1);
                        sqlAdd.append(") VALUES (");
                        for (int i = 0; i < map.size(); i++) {
                            sqlAdd.append("?,");
                        }
                        sqlAdd.setLength(sqlAdd.length() - 1);
                        sqlAdd.append(");");
                        jdbcTemplate.update(sqlAdd.toString(), map.values().toArray());
                        if ("1".equals(cjxtMbgl.getSfls())) {
                            StringBuilder sqlLs = new StringBuilder();
                            sqlLs.append("INSERT INTO ");
                            sqlLs.append(cjxtMbgl.getBm() + "_ls");
                            sqlLs.append(" (");
                            for (String key : map.keySet()) {
//                                if (map.containsKey("wszt")) {
//                                    map.put("wszt", "0");
//                                }
                                if (map.containsKey("blzt")) {
                                    map.put("blzt", "0");
                                }
                                sqlLs.append(key).append(",");
                            }
                            sqlLs.setLength(sqlLs.length() - 1);
                            sqlLs.append(") VALUES (");
                            for (int i = 0; i < map.size(); i++) {
                                sqlLs.append("?,");
                            }
                            sqlLs.setLength(sqlLs.length() - 1);
                            sqlLs.append(") ;");
                            jdbcTemplate.update(sqlLs.toString(), map.values().toArray());
                        }

                        //修改上报表状态
                        String sql = "UPDATE " + tableName + "_sb" + " SET shzt = '1', update_time = '" + formattedDateTime + "', update_by = '" + sysUser.getUsername() + "' WHERE id = '" + id + "'";
                        jdbcTemplate.update(sql);
                    }
                } else {
                    return Result.error("当前模版信息有误,数据审核失败!!!");
                }
            }
            if (addOrUpt == true) {
                return Result.ok("审核成功");
            } else {
                return Result.ok("审核成功");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("");
    }

    /**
     * PC模版管理 数据上报批量审核
     *
     * @param mapBatch
     * @return
     */
    @ApiOperation(value = "模板管理配置-数据上报批量审核", notes = "模板管理配置-数据上报批量审核")
    @PostMapping(value = "/sbshPcValueBatch")
    public Result<String> sbshPcValueBatch(@RequestBody List<Map<String, Object>> mapBatch) {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            boolean addOrUpt = true;
            for (Map<String, Object> map : mapBatch) {
                //取出模版ID
                String mbId = (String) map.get("mb_id");
                String id = (String) map.get("id");
                String tableName = (String) map.get("table_name");

                String dbOnlyName = "";//唯一字段名
                String onlyNameValue = "";//唯一字段值
                String onlyTableName = "";//唯一表名称
                addOrUpt = true;
                if (!"".equals(mbId)) {
                    CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbId);
                    if (cjxtMbgl != null) {
                        if (cjxtMbgl != null) {
                            onlyTableName = cjxtMbgl.getBm();
                            dbOnlyName = cjxtMbgl.getDbOnly();
                        }
                        //唯一字段不等于空进入
                        if (!"".equals(dbOnlyName) && dbOnlyName != null && !dbOnlyName.isEmpty()) {
                            if (map.containsKey(dbOnlyName)) {
                                onlyNameValue = (String) map.get(dbOnlyName);
                                //							CjxtMbglPz cjxtMbglPz = cjxtMbglPzService.getOne(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId,cjxtMbgl.getId()).eq(CjxtMbglPz::getDbFieldName,dbOnlyName));
                                String sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + dbOnlyName + " = '" + onlyNameValue + "' ;";
                                List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                                if (resultList.size() > 0) {
                                    StringBuilder onlyUpt = new StringBuilder();
                                    Map<String, Object> uptMap = new HashMap<>();
                                    uptMap = map;
                                    uptMap.remove("id");
                                    uptMap.remove("create_by");
                                    uptMap.remove("create_time");
                                    uptMap.remove("sys_org_code");
                                    uptMap.remove("del_flag");
                                    uptMap.remove("mb_id");
                                    uptMap.remove("mb_name");
                                    uptMap.remove("table_name");
                                    uptMap.remove("address_id");
                                    uptMap.remove("address");
                                    uptMap.remove("longitude");
                                    uptMap.remove("latitude");
                                    uptMap.remove("blzt");
                                    uptMap.remove("wszt");
                                    uptMap.remove("data_id");
                                    uptMap.remove("shzt");
//                                    uptMap.put("wszt", "0");
                                    onlyUpt.append("UPDATE ");
                                    onlyUpt.append(cjxtMbgl.getBm());
                                    onlyUpt.append(" SET ");
                                    for (String key : uptMap.keySet()) {
                                        if (uptMap.containsKey("update_by")) {
                                            uptMap.put("update_by", sysUser.getUsername());
                                        } else {
                                            uptMap.put("update_by", sysUser.getUsername());
                                        }
                                        if (uptMap.containsKey("update_time")) {
                                            uptMap.put("update_time", formattedDateTime);
                                        } else {
                                            uptMap.put("update_time", formattedDateTime);
                                        }
                                        if (!"id".equals(key)) {
                                            if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                                onlyUpt.append(key).append(" = " + null + ",");
                                            } else {
                                                onlyUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                            }
                                        }
                                    }
                                    onlyUpt.setLength(onlyUpt.length() - 1);
                                    onlyUpt.append(" WHERE del_flag = '0' AND " + dbOnlyName + " = '" + onlyNameValue + "' ;");
                                    jdbcTemplate.update(onlyUpt.toString());
                                    if ("1".equals(cjxtMbgl.getSfls())) {
                                        StringBuilder lsUpt = new StringBuilder();
                                        lsUpt.append("UPDATE ");
                                        lsUpt.append(cjxtMbgl.getBm() + "_ls");
                                        lsUpt.append(" SET ");
                                        for (String key : uptMap.keySet()) {
                                            if (uptMap.containsKey("update_by")) {
                                                uptMap.put("update_by", sysUser.getUsername());
                                            } else {
                                                uptMap.put("update_by", sysUser.getUsername());
                                            }
                                            if (uptMap.containsKey("update_time")) {
                                                uptMap.put("update_time", formattedDateTime);
                                            } else {
                                                uptMap.put("update_time", formattedDateTime);
                                            }
                                            if (!"id".equals(key)) {
                                                if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                                    lsUpt.append(key).append(" = " + null + ",");
                                                } else {
                                                    lsUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                                }
                                            }
                                        }
                                        lsUpt.setLength(lsUpt.length() - 1);
                                        lsUpt.append(" WHERE del_flag = '0' AND " + dbOnlyName + " = '" + onlyNameValue + "' ;");
                                        jdbcTemplate.update(lsUpt.toString());
                                    }
                                    addOrUpt = false;
                                }
                            }
                        }

                        //判断当前模版为从业人员模版
                        if ("RY002".equals(cjxtMbgl.getMbbh()) || "RY001".equals(cjxtMbgl.getMbbh())) {
                            if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                String bm = (String) map.get("bm");
                                String sfzh = (String) map.get("sfzh");
                                String bmValue = "";
                                if (!"".equals(bm)) {
                                    bmValue = bm;
                                }
                                if (!"".equals(sfzh) && !"".equals(bmValue)) {
                                    String updateSql = "UPDATE cjxt_rkcj SET ryfwcsgzdw = '" + bmValue + "' WHERE del_flag = '0' AND rysfzh = '" + sfzh + "' ;";
                                    jdbcTemplate.update(updateSql);
                                }
                            }
                            String addressID = (String) map.get("address_id");
                            String sqlOnly = "";
                            if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                String rysfzh = (String) map.get("rysfzh");
                                sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' AND address_id = '" + addressID + "' ;";
                            }
                            if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                String sfzh = (String) map.get("sfzh");
                                sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND sfzh = '" + sfzh + "' AND address_id = '" + addressID + "' ;";
                            }
                            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                            if (resultList.size() > 0) {
                                StringBuilder onlyUpt = new StringBuilder();
                                Map<String, Object> uptMap = new HashMap<>();
                                uptMap = map;
                                uptMap.remove("id");
                                uptMap.remove("create_by");
                                uptMap.remove("create_time");
                                uptMap.remove("sys_org_code");
                                uptMap.remove("del_flag");
                                uptMap.remove("mb_id");
                                uptMap.remove("mb_name");
                                uptMap.remove("table_name");
                                uptMap.remove("address_id");
                                uptMap.remove("address");
                                uptMap.remove("longitude");
                                uptMap.remove("latitude");
                                uptMap.remove("blzt");
                                uptMap.remove("wszt");
                                uptMap.remove("data_id");
                                uptMap.remove("shzt");
//                                uptMap.put("wszt", "0");
                                onlyUpt.append("UPDATE ");
                                onlyUpt.append(cjxtMbgl.getBm());
                                onlyUpt.append(" SET ");
                                for (String key : uptMap.keySet()) {
//                                    if (uptMap.containsKey("wszt")) {
//                                        uptMap.put("wszt", "0");
//                                    }
                                    if (uptMap.containsKey("blzt")) {
                                        uptMap.put("blzt", "0");
                                    }
                                    if (uptMap.containsKey("update_by")) {
                                        uptMap.put("update_by", sysUser.getUsername());
                                    } else {
                                        uptMap.put("update_by", sysUser.getUsername());
                                    }
                                    if (uptMap.containsKey("update_time")) {
                                        uptMap.put("update_time", formattedDateTime);
                                    } else {
                                        uptMap.put("update_time", formattedDateTime);
                                    }
                                    if (!"id".equals(key)) {
                                        if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                            onlyUpt.append(key).append(" = " + null + ",");
                                        } else {
                                            onlyUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                        }
                                    }
                                }
                                onlyUpt.setLength(onlyUpt.length() - 1);
                                if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                    String rysfzh = (String) map.get("rysfzh");
                                    onlyUpt.append(" WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' AND address_id = '" + addressID + "' ;");
                                }
                                if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                    String sfzh = (String) map.get("sfzh");
                                    onlyUpt.append(" WHERE del_flag = '0' AND sfzh = '" + sfzh + "' AND address_id = '" + addressID + "' ;");
                                }
                                jdbcTemplate.update(onlyUpt.toString());

                                if ("1".equals(cjxtMbgl.getSfls())) {
                                    StringBuilder lsUpt = new StringBuilder();
                                    lsUpt.append("UPDATE ");
                                    lsUpt.append(cjxtMbgl.getBm() + "_ls");
                                    lsUpt.append(" SET ");
                                    for (String key : uptMap.keySet()) {
//                                        if (uptMap.containsKey("wszt")) {
//                                            uptMap.put("wszt", "0");
//                                        }
                                        if (uptMap.containsKey("blzt")) {
                                            uptMap.put("blzt", "0");
                                        }
                                        if (uptMap.containsKey("update_by")) {
                                            uptMap.put("update_by", sysUser.getUsername());
                                        } else {
                                            uptMap.put("update_by", sysUser.getUsername());
                                        }
                                        if (uptMap.containsKey("update_time")) {
                                            uptMap.put("update_time", formattedDateTime);
                                        } else {
                                            uptMap.put("update_time", formattedDateTime);
                                        }
                                        if (!"id".equals(key)) {
                                            if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                                                lsUpt.append(key).append(" = " + null + ",");
                                            } else {
                                                lsUpt.append(key).append(" = '" + uptMap.get(key) + "',");
                                            }
                                        } else {
                                            uptMap.put("id", lsUpt);
                                        }
                                    }
                                    lsUpt.setLength(lsUpt.length() - 1);
                                    if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                        String rysfzh = (String) map.get("rysfzh");
                                        lsUpt.append(" WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' AND address_id = '" + addressID + "' ;");
                                    }
                                    if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                        String sfzh = (String) map.get("sfzh");
                                        lsUpt.append(" WHERE del_flag = '0' AND sfzh = '" + sfzh + "' AND address_id = '" + addressID + "' ;");
                                    }
                                    jdbcTemplate.update(lsUpt.toString());
                                }
                                addOrUpt = false;
                            }
                        }

                        if (addOrUpt == true) {
                            StringBuilder sqlAdd = new StringBuilder();
                            sqlAdd.append("INSERT INTO ");
                            sqlAdd.append(cjxtMbgl.getBm());
                            sqlAdd.append(" (");
                            map.remove("shzt");
                            for (String key : map.keySet()) {
//                                if (map.containsKey("wszt")) {
//                                    map.put("wszt", "0");
//                                }
                                if (map.containsKey("blzt")) {
                                    map.put("blzt", "0");
                                }
                                sqlAdd.append(key).append(",");
                            }
                            sqlAdd.setLength(sqlAdd.length() - 1);
                            sqlAdd.append(") VALUES (");
                            for (int i = 0; i < map.size(); i++) {
                                sqlAdd.append("?,");
                            }
                            sqlAdd.setLength(sqlAdd.length() - 1);
                            sqlAdd.append(");");
                            jdbcTemplate.update(sqlAdd.toString(), map.values().toArray());
                            if ("1".equals(cjxtMbgl.getSfls())) {
                                StringBuilder sqlLs = new StringBuilder();
                                sqlLs.append("INSERT INTO ");
                                sqlLs.append(cjxtMbgl.getBm() + "_ls");
                                sqlLs.append(" (");
                                for (String key : map.keySet()) {
//                                    if (map.containsKey("wszt")) {
//                                        map.put("wszt", "0");
//                                    }
                                    if (map.containsKey("blzt")) {
                                        map.put("blzt", "0");
                                    }
                                    sqlLs.append(key).append(",");
                                }
                                sqlLs.setLength(sqlLs.length() - 1);
                                sqlLs.append(") VALUES (");
                                for (int i = 0; i < map.size(); i++) {
                                    sqlLs.append("?,");
                                }
                                sqlLs.setLength(sqlLs.length() - 1);
                                sqlLs.append(");");
                                jdbcTemplate.update(sqlLs.toString(), map.values().toArray());
                            }
                        }

                        //修改上报表状态
                        String sql = "UPDATE " + tableName + "_sb" + " SET shzt = '1', update_time = '" + formattedDateTime + "', update_by = '" + sysUser.getUsername() + "' WHERE id = '" + id + "' ;";
                        jdbcTemplate.update(sql);
                    } else {
                        return Result.error("当前模版信息有误,数据审核失败!!!");
                    }
                }
            }
            if (addOrUpt == true) {
                return Result.ok("审核成功");
            } else {
                return Result.ok("审核成功");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("");
    }

    @ApiOperation(value = "模板管理配置-PC动态回显字段", notes = "模板管理配置-PC动态回显字段")
    @GetMapping(value = "/listPcZd")
    public Result<Map<String, Object>> listPcZd(@RequestParam(required = true, name = "id") String id, HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String[] sysUserRoleS = sysUser.getRoleCode().split(",");//获取当前登录用户角色
        Map<String, Object> result = new HashMap<>();
        String tableName = "";
        String mbglId = "";
        CjxtMbgl cjxtMbgl = cjxtMbglService.getById(id);
        if (cjxtMbgl != null) {
            tableName = cjxtMbgl.getBm();
            mbglId = cjxtMbgl.getId();
        } else {
            return Result.error("当前模板数据有误");
        }
        boolean isRole = false;
        CjxtJsmbpz jsmbpz = null;
        for (int i = 0; i < sysUserRoleS.length; i++) {
            String userRoleCode = sysUserRoleS[i];
            List<CjxtJsmbpz> cjxtJsmbpzList = cjxtJsmbpzService.list(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getRoleCode, userRoleCode));
            for (CjxtJsmbpz cjxtJsmbpz : cjxtJsmbpzList) {
                if (cjxtMbgl.getId().equals(cjxtJsmbpz.getMbId())) {
                    jsmbpz = cjxtJsmbpz;
                    isRole = true;
                    break;
                }
            }
        }
        //查询模板所有字段
        List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, mbglId).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).ne(CjxtMbglPz::getIsTitle, "1").orderByAsc(CjxtMbglPz::getOrderNum));
        if (isRole && jsmbpz != null) {
            //根据配置返回展示新增、编辑、详情
            List<CjxtJsmbpzDtl> jsmbpzDtlList = cjxtJsmbpzDtlService.selectByMainId(jsmbpz.getId());
            List<CjxtMbglPz> filteredList = new ArrayList<>();
            for (CjxtMbglPz item : list) {
                if ("1".equals(item.getIsCommon())) {
                    filteredList.add(item);
                } else if ("0".equals(item.getIsCommon())) {
                    for (CjxtJsmbpzDtl dtl : jsmbpzDtlList) {
                        if (dtl.getDbFieldName().equals(item.getDbFieldName())) {
                            filteredList.add(item);
                            break;
                        }
                    }
                }
            }
            result.put("pzList", filteredList);
        } else {
            result.put("pzList", list);
        }
//
//		if(!"".equals(tableName)){
//			String sql = "SELECT * FROM " + tableName ;
//			List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
//			result.put("dataValues", resultList);
//		}
        return Result.OK(result);
    }

    @ApiOperation(value = "模板管理配置-PC动态回显数据", notes = "模板管理配置-PC动态回显数据")
    @GetMapping(value = "/listPcDataValue")
    public Result<Map<String, Object>> listPcDataValue(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "tableName") String tableName,
            @RequestParam(required = false, name = "addressId") String addressId,
            @RequestParam(required = false, name = "dataId") String dataId,
            @RequestParam(required = false, name = "shzt") String shzt,
            @RequestParam(required = false, name = "shws") String shws,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) throws Exception {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (tableName != null) {
            String countSql = null;
            String dataSql = null;
            Integer total = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();

            //查询字段配置
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, tableName).last("LIMIT 1"));
            List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));

            StringBuilder shwsSqlAdd = new StringBuilder();
            String sjshZt = ""; // 数据审核状态
            String sjwsZt = ""; // 数据完善状态
            //数据审核 查询自主上报数据
            if (!"".equals(shws) && shws != null && "sb".equals(shws)) {
                tableName = tableName + "_sb";
                if (!"".equals(shzt) && shzt != null) {
                    if ("true".equals(shzt)) {
                        sjshZt = "1";
                        shwsSqlAdd.append(" AND t.shzt = '1' ");
                    } else if ("false".equals(shzt)) {
                        sjshZt = "0";
                        shwsSqlAdd.append(" AND t.shzt = '0' ");
                    }
                }
            } else {
                if (!"".equals(shzt) && shzt != null) {
                    if ("true".equals(shzt)) {
                        sjshZt = "1";
                        shwsSqlAdd.append(" AND t.wszt = '1' ");
                    } else if ("false".equals(shzt)) {
                        sjshZt = "0";
                        shwsSqlAdd.append(" AND t.wszt = '0' ");
                    }
                }
            }

            //根据地址ID查询
            StringBuilder addressIdBuilder = new StringBuilder();
            if(!"".equals(addressId) && addressId!=null){
                addressIdBuilder.append(" AND t.address_id = '"+addressId+"' ");
            }

            // 移除不需要的键
            params.remove("tableName");
            params.remove("addressId");
            params.remove("pageNo");
            params.remove("pageSize");
            params.remove("column");
            params.remove("order");
            params.remove("shzt");
            params.remove("shws");
            params.remove("_t");
            StringBuilder additionalQuery = new StringBuilder();
            if (!params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalQuery.append(" AND ").append(entry.getKey()).append(" LIKE '%").append(entry.getValue()).append("%'");
                }
            }
            if ("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                        + additionalQuery;
            } else if ("6".equals(sysUser.getUserSf())
                    || "7".equals(sysUser.getUserSf())
                    || "8".equals(sysUser.getUserSf())
                    || "9".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                        + additionalQuery;
            } else if ("4".equals(sysUser.getUserSf())
                    || "5".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                        + additionalQuery;
            } else if ("1".equals(sysUser.getUserSf())) {
                //获取部门信息
//				SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                        + additionalQuery;
            } else {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " + shwsSqlAdd + addressIdBuilder +
                        additionalQuery;
            }
            resultList = jdbcTemplate.queryForList(dataSql);

            // 执行查询并获取总条数
            int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            // 计算总页数
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            // 将总页数添加到结果中
            result.put("totalPages", totalPages);

            HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");

            System.out.println("输出dictText" + dictText);
            if (resultList.size() > 0) {
                for (Map<String, Object> row : resultList) {
                    for (CjxtMbglPz cjxtMbglPz : list) {
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText != null && dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getValue().equals(value)) {
                                            row.put(dbFieldName, dictModel.getText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //图片或者附件
                        if ("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                if (value != null && !"".equals(value)) {
                                    row.put(dbFieldName, minioUrl + "/" + bucketName + "/" + value);
                                }
                            }
                        }
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            Object value = row.get(dbFieldName);
                            if(!"".equals(value) && value!=null){
                                if(((String) value).contains("_sxby")){
                                    String dataV = sjjm(value.toString());
                                    row.put(dbFieldName, dataV);
                                }else {
                                    row.put(dbFieldName, value);
                                }
                            }else {
                                row.put(dbFieldName, "");
                            }
                        }
                    }
                }
            }

            result.put("current", pageNo);
            result.put("size", pageSize);
            result.put("total", totalCount);
            result.put("pages", totalPages);
            result.put("records", resultList);
        }
        return Result.OK(result);
    }

    @ApiOperation(value = "模板管理配置-PC动态单条查询数据", notes = "模板管理配置-PC动态单条查询数据")
    @GetMapping(value = "/listQueryByOne")
    public Result<Map<String, Object>> listQueryByOne(
            @RequestParam(required = false, name = "tableName") String tableName,
            @RequestParam(required = false, name = "dataId") String dataId) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (tableName != null) {
            List<Map<String, Object>> resultList = new ArrayList<>();

            //查询字段配置
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, tableName).last("LIMIT 1"));
            List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));

            StringBuilder idBuilder = new StringBuilder();
            if(dataId!=null && !"".equals(dataId)){
                idBuilder.append(" AND t.id = '"+dataId+"' ");
            }

            //数据查询
            String dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " + idBuilder;
            resultList = jdbcTemplate.queryForList(dataSql);

            //字典数据
            HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");
            System.out.println("输出dictText" + dictText);
            if (resultList.size() > 0) {
                for (Map<String, Object> row : resultList) {
                    for (CjxtMbglPz cjxtMbglPz : list) {
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText != null && dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getValue().equals(value)) {
                                            row.put(dbFieldName, dictModel.getText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //图片或者附件
                        if ("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                if (value != null && !"".equals(value)) {
                                    row.put(dbFieldName, minioUrl + "/" + bucketName + "/" + value);
                                }
                            }
                        }
                    }
                }
            }

            result.put("records", resultList);
        }
        return Result.OK(result);
    }

    @ApiOperation(value = "模板管理配置-PC动态回显数据", notes = "模板管理配置-PC动态回显数据")
    @GetMapping(value = "/listPcDataLsValue")
    public Result<Map<String, Object>> listPcDataLsValue(
            @RequestParam Map<String, String> params,
            @RequestParam(required = false, name = "tableName") String tableName,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (tableName != null) {
            String countSql = null;
            String dataSql = null;
            Integer total = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();

            String TABLENAME = tableName;
            if(tableName.endsWith("_ls")){
                TABLENAME = tableName.replaceAll("_ls", "");
            }
            //查询字段配置
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, TABLENAME).last("LIMIT 1"));
            List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));

            // 移除不需要的键
            params.remove("tableName");
            params.remove("pageNo");
            params.remove("pageSize");
            params.remove("column");
            params.remove("order");
            params.remove("_t");
            StringBuilder additionalQuery = new StringBuilder();
            if (!params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalQuery.append(" AND ").append("t."+entry.getKey()).append(" LIKE '%").append(entry.getValue()).append("%'");
                }
            }
            if ("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                        "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " +
                        "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                        + additionalQuery;
            } else if ("6".equals(sysUser.getUserSf())
                    || "7".equals(sysUser.getUserSf())
                    || "8".equals(sysUser.getUserSf())
                    || "9".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                        "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " +
                        "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                        + additionalQuery;
            } else if ("4".equals(sysUser.getUserSf())
                    || "5".equals(sysUser.getUserSf())) {
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                        "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " +
                        "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                        + additionalQuery;
            } else if ("1".equals(sysUser.getUserSf())) {
                //获取部门信息
//				SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
                dataSql = "SELECT * FROM " + tableName + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                        "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                        + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " +
                        "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                        + additionalQuery;
            } else {
                dataSql = "SELECT * FROM " + tableName + " t WHERE t.del_flag = '0' " +
                        additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                countSql = "SELECT COUNT(*) FROM " + tableName + " t WHERE t.del_flag = '0' " +
                        additionalQuery;
            }
            resultList = jdbcTemplate.queryForList(dataSql);
            if(resultList.size()==0){
                if ("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())) {
                    dataSql = "SELECT * FROM " + TABLENAME + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                            "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                            + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                    countSql = "SELECT COUNT(*) FROM " + TABLENAME + " t WHERE t.del_flag = '0' " +
                            "AND EXISTS (SELECT 1 FROM cjxt_pjwgqx d,sys_depart s WHERE d.wg_id=s.id and d.del_flag='0' AND d.pj_id = '" + sysUser.getId() + "' and t.sys_org_code LIKE CONCAT(s.org_code,'%')) "
                            + additionalQuery;
                } else if ("6".equals(sysUser.getUserSf())
                        || "7".equals(sysUser.getUserSf())
                        || "8".equals(sysUser.getUserSf())
                        || "9".equals(sysUser.getUserSf())) {
                    dataSql = "SELECT * FROM " + TABLENAME + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                            "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                            + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                    countSql = "SELECT COUNT(*) FROM " + TABLENAME + " t WHERE t.del_flag = '0' " +
                            "AND EXISTS (SELECT 1 FROM cjxt_bm_data d WHERE d.del_flag='0' AND d.org_id = '" + sysUser.getOrgId() + "' and t.sys_org_code LIKE CONCAT(d.data_org_code,'%')) "
                            + additionalQuery;
                } else if ("4".equals(sysUser.getUserSf())
                        || "5".equals(sysUser.getUserSf())) {
                    dataSql = "SELECT * FROM " + TABLENAME + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                            "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                            + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    countSql = "SELECT COUNT(*) FROM " + TABLENAME + " t WHERE t.del_flag = '0' " +
                            "AND t.sys_org_code LIKE CONCAT('" + sysUser.getOrgCode() + "','%') "
                            + additionalQuery;
                } else if ("1".equals(sysUser.getUserSf())) {
                    //获取部门信息
//				SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",sysUser.getOrgCode()).eq("del_flag","0"));
                    dataSql = "SELECT * FROM " + TABLENAME + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " +
                            "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                            + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    countSql = "SELECT COUNT(*) FROM " + TABLENAME + " t WHERE t.del_flag = '0' " +
                            "AND t.address_id in (select p.id from cjxt_standard_address p where p.address_code_mz like '" + sysUser.getOrgCode() + "%') "
                            + additionalQuery;
                } else {
                    dataSql = "SELECT * FROM " + TABLENAME + " t WHERE t.del_flag = '0' " +
                            additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    countSql = "SELECT COUNT(*) FROM " + TABLENAME + " t WHERE t.del_flag = '0' " +
                            additionalQuery;
                }
                resultList = jdbcTemplate.queryForList(dataSql);
            }
            // 执行查询并获取总条数
            int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            // 计算总页数
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            // 将总页数添加到结果中
            result.put("totalPages", totalPages);

            HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");

            System.out.println("输出dictText" + dictText);
            if (resultList.size() > 0) {
                for (Map<String, Object> row : resultList) {
                    for (CjxtMbglPz cjxtMbglPz : list) {
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getValue().equals(value)) {
                                            row.put(dbFieldName, dictModel.getText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            Object value = row.get(dbFieldName);
                            if(!"".equals(value) && value!=null){
                                if(((String) value).contains("_sxby")){
                                    String dataV = sjjm(value.toString());
                                    row.put(dbFieldName, dataV);
                                }else {
                                    row.put(dbFieldName, value);
                                }
                            }else {
                                row.put(dbFieldName, "");
                            }
                        }
                    }
                }
            }

            result.put("current", pageNo);
            result.put("size", pageSize);
            result.put("total", totalCount);
            result.put("pages", totalPages);
            result.put("records", resultList);
        }
        return Result.OK(result);
    }

    /**
     * 导出我的采集excel模板
     *
     * @param request
     * @param tableName
     */
//    @RequiresPermissions("cjxt:cjxt_mbgl:exportPcDataTemp")
    @GetMapping(value = "/exportPcDataTemp")
    public ModelAndView exportPcDataTemp(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> params, @RequestParam(required = false, name = "tableName") String tableName) throws IOException {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<ExcelExportEntity> entity = new ArrayList<ExcelExportEntity>();
        ModelAndView mv = new ModelAndView(new JeecgMapExcelView());
        String mbName = "";
        if (tableName != null) {

            //查询字段配置
            String mbTableName =tableName;
            CjxtMbgl cjxtMbgl = null;
            //查询字段配置
            if(mbTableName.endsWith("_sb")){
                mbTableName = mbTableName.replaceAll("_sb","");
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
            }else{
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
            }
            if(cjxtMbgl==null){
                mbTableName = mbTableName+"_sb";
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
            }
            mbName = cjxtMbgl.getMbname();

            List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
            //去掉无需展示的字段
            StringBuilder column = new StringBuilder();
            entity.add(new ExcelExportEntity("省(province_name)", "province_name"));
            entity.add(new ExcelExportEntity("市(city_name)", "city_name"));
            entity.add(new ExcelExportEntity("区/县(district_name)", "district_name"));
            entity.add(new ExcelExportEntity("乡镇/街道(street_name)", "street_name"));
            entity.add(new ExcelExportEntity("村名/社区(detail_mc)", "detail_mc"));
            entity.add(new ExcelExportEntity("网格(grid_name)", "grid_name"));
            entity.add(new ExcelExportEntity("网格2(grid_name2)", "grid_name2"));
            entity.add(new ExcelExportEntity("路名(detail_lm)", "detail_lm"));
            entity.add(new ExcelExportEntity("路号名(detail_lhm)", "detail_lhm"));
            entity.add(new ExcelExportEntity("补充说明(detail_address)", "detail_address"));
            entity.add(new ExcelExportEntity("居住区域类型(jzlx)", "jzlx"));
            entity.add(new ExcelExportEntity("地址名称(dzmc)", "dzmc"));
            entity.add(new ExcelExportEntity("小区(dz1_xqm)", "dz1_xqm"));
            entity.add(new ExcelExportEntity("楼栋(dz1_ld)", "dz1_ld"));
            entity.add(new ExcelExportEntity("单元(dz1_dy)", "dz1_dy"));
            entity.add(new ExcelExportEntity("室(dz1_s)", "dz1_s"));
            entity.add(new ExcelExportEntity("村名(dz2_cm)", "dz2_cm"));
            entity.add(new ExcelExportEntity("组名(dz2_zm)", "dz2_zm"));
            entity.add(new ExcelExportEntity("号名(dz2_hm)", "dz2_hm"));
            entity.add(new ExcelExportEntity("大厦名(dz3_dsm)", "dz3_dsm"));
            entity.add(new ExcelExportEntity("楼栋名(dz3_ldm)", "dz3_ldm"));
            entity.add(new ExcelExportEntity("室名(dz3_sm)", "dz3_sm"));
            entity.add(new ExcelExportEntity("排(排号室)(dz5_p)", "dz5_p"));
            entity.add(new ExcelExportEntity("号(排号室)(dz5_h)", "dz5_h"));
            entity.add(new ExcelExportEntity("室(排号室)(dz5_s)", "dz5_s"));
            entity.add(new ExcelExportEntity("室(宿舍)(dz6_s)", "dz6_s"));
            for (CjxtMbglPz cjxtMbglPz : list) {
                String dbFieldName = cjxtMbglPz.getDbFieldName();
                String dbFieldTxt = cjxtMbglPz.getDbFieldTxt();
                if (dbFieldName.indexOf("id") == -1 && dbFieldName.indexOf("create_by") == -1 && dbFieldName.indexOf("create_time") == -1
                        && dbFieldName.indexOf("update_by") == -1 && dbFieldName.indexOf("update_time") == -1 && dbFieldName.indexOf("sys_org_code") == -1
                        && dbFieldName.indexOf("del_flag") == -1 && dbFieldName.indexOf("longitude") == -1 && dbFieldName.indexOf("latitude") == -1
                        && dbFieldName.indexOf("blzt") == -1 && dbFieldName.indexOf("wszt") == -1 && dbFieldName.indexOf("address") == -1
                ) {
                    column.append("," + dbFieldName);
                    entity.add(new ExcelExportEntity(dbFieldTxt + "(" + dbFieldName + ")", dbFieldName));
                }
            }
            Map map = new HashMap();
            map.put("mb_name", mbName);
            map.put("table_name", tableName);
            resultList.add(map);
            // 在添加现有数据之前插入新行
            Map<String, Object> newRow = new HashMap<>();
            newRow.put("table_name", tableName); // 设置table_name列
            // 其余列值为空
            entity.forEach(e -> newRow.put((String) e.getKey(), "")); // 设置其他列为空
            resultList.add(newRow);

            //导出文件名称
            mv.addObject(NormalExcelConstants.FILE_NAME, mbName);
            mv.addObject(MapExcelConstants.ENTITY_LIST, entity);
            mv.addObject(NormalExcelConstants.PARAMS, new ExportParams(mbName, mbName));
            mv.addObject(NormalExcelConstants.MAP_LIST, resultList);
        }

        return mv;
    }

    /**
     * 导出我的采集excel
     *
     * @param request
     * @param tableName
     */
//    @RequiresPermissions("cjxt:cjxt_mbgl:exportPcDataValue")
    @GetMapping(value = "/exportPcDataValue")
    public ModelAndView exportPcDataValue(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> params, @RequestParam(required = false, name = "tableName") String tableName) throws IOException {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String countSql = null;
        String dataSql = null;
        Integer total = 0;
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<ExcelExportEntity> entity = new ArrayList<ExcelExportEntity>();
        ModelAndView mv = new ModelAndView(new JeecgMapExcelView());
        String mbName = "";


        boolean isRole = false;
        CjxtJsmbpz jsmbpz = null;
        List<CjxtMbglPz> list = new ArrayList<>();
        SysRole role = null;

        if (tableName != null) {

            //查询字段配置
            String mbTableName =tableName;
            CjxtMbgl cjxtMbgl = null;
            //查询字段配置
            if(mbTableName.endsWith("_sb")){
                mbTableName = mbTableName.replaceAll("_sb","");
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
            }else{
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
            }
            if(cjxtMbgl==null){
                mbTableName = mbTableName+"_sb";
                cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
            }
            mbName = cjxtMbgl.getMbname();

            String ryOrderBy = "";
            if("RY001".equals(cjxtMbgl.getMbbh())){
                ryOrderBy = ", CAST(t.yhzgx AS UNSIGNED)";
            }

            List<SysUserRole> userRoleList = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));
            if (userRoleList != null && userRoleList.size() > 0) {
                for (SysUserRole sysUserRole : userRoleList) {
                    role = sysRoleService.getById(sysUserRole.getRoleId());
                    CjxtJsmbpz cjxtJsmbpz = cjxtJsmbpzService.getOne(new LambdaQueryWrapper<CjxtJsmbpz>().eq(CjxtJsmbpz::getMbId, cjxtMbgl.getId()).eq(CjxtJsmbpz::getRoleCode, role.getRoleCode()));
                    if (cjxtJsmbpz != null) {
                        jsmbpz = cjxtJsmbpz;
                        isRole = true;
                        break;
                    }
                }
            }

            if (isRole && jsmbpz != null) {
                String sql = "select r.db_field_name from cjxt_jsmbpz_dtl r where r.mb_id = '" + cjxtMbgl.getId() + "' AND role_code = '" + role.getRoleCode() + "' union all select m.zdname from cjxt_gtzd m";
                list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().inSql(CjxtMbglPz::getDbFieldName, sql).eq(CjxtMbglPz::getIsCommon,"0").eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
            } else {
                list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getIsCommon,"0").eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
            }
//			List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId,cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh,cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
            //去掉无需展示的字段
            StringBuilder column = new StringBuilder();
            for (CjxtMbglPz cjxtMbglPz : list) {
                String dbFieldName = cjxtMbglPz.getDbFieldName();
                String dbFieldTxt = cjxtMbglPz.getDbFieldTxt();
                if (dbFieldName.indexOf("id") == -1 && dbFieldName.indexOf("create_by") == -1 && dbFieldName.indexOf("create_time") == -1
                        && dbFieldName.indexOf("update_by") == -1 && dbFieldName.indexOf("update_time") == -1 && dbFieldName.indexOf("sys_org_code") == -1
                        && dbFieldName.indexOf("del_flag") == -1 && dbFieldName.indexOf("longitude") == -1 && dbFieldName.indexOf("latitude") == -1
                        && dbFieldName.indexOf("blzt") == -1 && dbFieldName.indexOf("wszt") == -1
                ) {
                    column.append("," + "t."+dbFieldName);
                    entity.add(new ExcelExportEntity(dbFieldTxt + "(" + dbFieldName + ")", dbFieldName));
                }
            }
            String columnStr = "";
            if (column != null && column.length() > 1) {
                columnStr = column.toString().substring(1);
            }
            // 移除不需要的键
            params.remove("tableName");
            params.remove("pageNo");
            params.remove("pageSize");
            params.remove("column");
            params.remove("order");
            params.remove("_t");

            StringBuilder additionalQuery = new StringBuilder();
            String selections = request.getParameter("selections");
            //关联语句
            String innerJoinSql = " INNER JOIN cjxt_standard_address cst ON t.address_id = cst.id ";
//            String innerJoinSql = " ";
            //排序
            String orderBySql = " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address ";
//            String orderBySql = " ";
            if (oConvertUtils.isNotEmpty(selections)) {
                List<String> selectionList = Arrays.asList(selections.split(","));
                StringBuilder sb = new StringBuilder();
                for (String selection : selectionList) {
                    sb.append(",'" + selection + "'");
                }
                if (sb != null) {
                    additionalQuery.append(" and t.id in (" + sb.toString().substring(1) + ")");
                }
            }
            if ("4".equals(sysUser.getUserSf()) || "5".equals(sysUser.getUserSf()) || "6".equals(sysUser.getUserSf()) || "7".equals(sysUser.getUserSf()) || "8".equals(sysUser.getUserSf()) || "9".equals(sysUser.getUserSf())) {
                if (!params.isEmpty()) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (!"selections".equals(entry.getKey())) {
                            additionalQuery.append(" AND ").append("t."+entry.getKey()).append(" LIKE '%").append(entry.getValue()).append("%'");
                        }
                    }
                }

                List<String> orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysUser.getOrgId());
                StringBuilder orgCodeBuilder = new StringBuilder();
                for (int i = 0; i < orgCodes.size(); i++) {
                    if (i > 0) {
                        orgCodeBuilder.append(",");
                    }
                    orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                }

                String orgCode = "''";
                StringBuilder sysOrgCodeBuilder = new StringBuilder();
                if (orgCodes.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                    sysOrgCodeBuilder.append(" AND t.sys_org_code in (").append(orgCode).append(") ");
                }
                dataSql = "SELECT t.address_id," + columnStr + " FROM " + tableName + " t " + innerJoinSql + " WHERE t.del_flag = '0' " + sysOrgCodeBuilder + additionalQuery + orderBySql + ryOrderBy;
                resultList = jdbcTemplate.queryForList(dataSql);
            } else {
                if (!params.isEmpty()) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (!"selections".equals(entry.getKey())) {
                            additionalQuery.append(" AND ").append("t."+entry.getKey()).append(" LIKE '%").append(entry.getValue()).append("%'");
                        }
                    }
                }
                dataSql = "SELECT t.address_id," + columnStr + " FROM " + tableName + " t " + innerJoinSql + " WHERE t.del_flag = '0' AND t.create_by = '" + sysUser.getUsername() + "'" + additionalQuery + orderBySql + ryOrderBy;
                resultList = jdbcTemplate.queryForList(dataSql);
            }
            HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");

            System.out.println("输出dictText" + dictText);
            if (resultList.size() > 0) {
                for (Map<String, Object> row : resultList) {
                    for (CjxtMbglPz cjxtMbglPz : list) {
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (row.containsKey(dbFieldName)) {
                                Object value = row.get(dbFieldName);
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getValue().equals(value)) {
                                            row.put(dbFieldName, dictModel.getText());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //导出文件名称
            mv.addObject(NormalExcelConstants.FILE_NAME, mbName);
            mv.addObject(MapExcelConstants.ENTITY_LIST, entity);
            mv.addObject(NormalExcelConstants.PARAMS, new ExportParams(mbName, "导出人:" + sysUser.getRealname(), mbName));
            mv.addObject(NormalExcelConstants.MAP_LIST, resultList);
        }

        return mv;
    }

    /**
     * 导入我的采集excel
     *
     * @param request
     * @param response
     * @return
     */
//    @RequiresPermissions("cjxt:cjxt_mbgl:importPcDataValue")
    @RequestMapping(value = "/importPcDataValue", method = RequestMethod.POST)
    public Result<?> importPcDataValue(HttpServletRequest request, HttpServletResponse response) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(1);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<Map<String, Object>> list = ExcelImportUtil.importExcel(file.getInputStream(), Map.class, params);
                boolean importStatus = true;
                String importBm = "";
                String tableName = "";
                String mbTableName="";
                if (list.size() > 0) {
                    tableName = list.get(0).get("表名(table_name)") + "";
                    mbTableName = tableName;
                }
                CjxtMbgl cjxtMbgl = null;
                if(mbTableName.endsWith("_sb")){
                    mbTableName = mbTableName.replaceAll("_sb","");
                    cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
                }else{
                    cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
                }
                if(cjxtMbgl==null){
                    mbTableName = mbTableName+"_sb";
                    cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, mbTableName).last("LIMIT 1"));
                }
                if(cjxtMbgl==null){
                    return Result.error("当前导入数据，表名(table_name)信息无法查到模板信息，请检查!!!");
                }
                String mbId = cjxtMbgl.getId();
                List<CjxtMbglPz> mbpzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));

                HashMap<String, ArrayList<DictModel>> dictText = (HashMap<String, ArrayList<DictModel>>) redisUtil.get("dictText");
                for (Map<String, Object> map : list) {

                    for (CjxtMbglPz cjxtMbglPz : mbpzList) {
                        String dbFieldTxt = cjxtMbglPz.getDbFieldTxt();
                        String dbFieldName = cjxtMbglPz.getDbFieldName();
                        if (cjxtMbglPz.getDictField() != null && !"".equals(cjxtMbglPz.getDictField())) {
                            if (map.containsKey(dbFieldTxt + "(" + dbFieldName + ")")) {
                                Object value = map.get(dbFieldTxt + "(" + dbFieldName + ")");
                                String dictField = cjxtMbglPz.getDictField();
                                if (dictText.containsKey(dictField)) {
                                    ArrayList<DictModel> dictModels = dictText.get(dictField);
                                    for (DictModel dictModel : dictModels) {
                                        if (dictModel.getText().equals(value)) {
                                            map.put(dbFieldTxt + "(" + dbFieldName + ")", dictModel.getValue());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                for (Map<String, Object> map : list) {
                    String province_name = map.get("省(province_name)") == null ? "" : map.get("省(province_name)") + "";
                    String province_code = getAreaCode(province_name);
                    String city_name = map.get("市(city_name)") == null ? "" : map.get("市(city_name)") + "";
                    String city_code = getAreaCode(city_name);
                    String district_name = map.get("区/县(district_name)") == null ? "" : map.get("区/县(district_name)") + "";
                    String district_code = getAreaCode(district_name);
                    String street_name = map.get("乡镇/街道(street_name)") == null ? "" : map.get("乡镇/街道(street_name)") + "";
                    if("".equals(street_name)){
                        return Result.error("乡镇/街道(street_name)，不能为空!!!");
                    }
                    String detail_mc = map.get("村名/社区(detail_mc)") == null ? "" : map.get("村名/社区(detail_mc)") + "";
                    if("".equals(detail_mc)){
                        return Result.error("村名/社区(detail_mc)，不能为空!!!");
                    }
                    String grid_name = map.get("网格(grid_name)") == null ? "" : map.get("网格(grid_name)") + "";
                    if("".equals(grid_name)){
                        return Result.error("网格(grid_name)，不能为空!!!");
                    }
                    String grid_name2 = map.get("网格2(grid_name2)") == null ? "" : map.get("网格2(grid_name2)") + "";
                    if("".equals(grid_name2)){
                        return Result.error("网格2(grid_name2)，不能为空!!!");
                    }
                    String detail_lm = map.get("路名(detail_lm)") == null ? "" : map.get("路名(detail_lm)") + "";
                    String detail_lhm = map.get("路号名(detail_lhm)") != null ? doubleToIntStr(map.get("路号名(detail_lhm)") + "") : map.get("路号名(detail_lhm)") + "";
                    String detail_address = map.get("补充说明(detail_address)") == null ? "" : map.get("补充说明(detail_address)") + "";
                    String jzlx = map.get("居住区域类型(jzlx)") == null ? "" : map.get("居住区域类型(jzlx)") + "";
                    String dzmc = map.get("地址名称(dzmc)") == null ? "" : map.get("地址名称(dzmc)") + "";
                    String dz1_xqm = map.get("小区(dz1_xqm)") == null ? "" : map.get("小区(dz1_xqm)") + "";
                    String dz1_ld = map.get("楼栋(dz1_ld)") != null ? doubleToIntStr(map.get("楼栋(dz1_ld)") + "") : map.get("楼栋(dz1_ld)") + "";
                    String dz1_dy = map.get("单元(dz1_dy)") != null ? doubleToIntStr(map.get("单元(dz1_dy)") + "") : map.get("单元(dz1_dy)") + "";
                    String dz1_s = map.get("室(dz1_s)") != null ? doubleToIntStr(map.get("室(dz1_s)") + "") : map.get("室(dz1_s)") + "";
                    String dz2_cm = map.get("村名(dz2_cm)") == null ? "" : map.get("村名(dz2_cm)") + "";
                    String dz2_zm = map.get("组名(dz2_zm)") != null ? doubleToIntStr(map.get("组名(dz2_zm)") + "") : map.get("组名(dz2_zm)") + "";
                    String dz2_hm = map.get("号名(dz2_hm)") != null ? doubleToIntStr(map.get("号名(dz2_hm)") + "") : map.get("号名(dz2_hm)") + "";
                    String dz3_dsm = map.get("大厦名(dz3_dsm)") == null ? "" : map.get("大厦名(dz3_dsm)") + "";
                    String dz3_ldm = map.get("楼栋名(dz3_ldm)") != null ? doubleToIntStr(map.get("楼栋名(dz3_ldm)") + "") : map.get("楼栋名(dz3_ldm)") + "";
                    String dz3_sm = map.get("室名(dz3_sm)") != null ? doubleToIntStr(map.get("室名(dz3_sm)") + "") : map.get("室名(dz3_sm)") + "";
                    String dz5_p = map.get("排(排号室)(dz5_p)") != null ? doubleToIntStr(map.get("排(排号室)(dz5_p)") + "") : map.get("排(排号室)(dz5_p)") + "";
                    String dz5_h = map.get("号(排号室)(dz5_h)") != null ? doubleToIntStr(map.get("号(排号室)(dz5_h)") + "") : map.get("号(排号室)(dz5_h)") + "";
                    String dz5_s = map.get("室(排号室)(dz5_s)") != null ? doubleToIntStr(map.get("室(排号室)(dz5_s)") + "") : map.get("室(排号室)(dz5_s)") + "";
                    String dz6_s = map.get("室(宿舍)(dz6_s)") != null ? doubleToIntStr(map.get("室(宿舍)(dz6_s)") + "") : map.get("室(宿舍)(dz6_s)") + "";

                    //街道信息查询
                    SysDepart sysDepartStreet = getSysCodeByNameAndPid(street_name, "");
                    String street_id = "";
                    String street_code = "";
                    if(sysDepartStreet!=null){
                        street_id = sysDepartStreet.getId()!=null ? sysDepartStreet.getId() : "";
                        street_code = sysDepartStreet.getOrgCode()!=null ? sysDepartStreet.getOrgCode() : "";
                    }else {
                        return Result.error(street_name+",不存在数据库中,请核查信息是否存在数据库!!!");
                    }

                    String dz_type = "";
                    ArrayList<DictModel> dictModels = dictText.get("dzlx");
                    for (DictModel dictModel : dictModels) {
                        if (dictModel.getText().equals(jzlx)) {
                            dz_type = dictModel.getValue();
                            break;
                        }
                    }
                    String prefix = "DZBM";
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                    int random = RandomUtils.nextInt(90) + 10;
                    String address_code = prefix + format.format(new Date()) + random;
                    String xqld = "";
                    if (dzmc != null && !"".equals(dzmc) && !"null".equals(dzmc)) {
                        xqld += dzmc;
                    }
                    if (dz1_ld != null && !"".equals(dz1_ld) && !"null".equals(dz1_ld)) {
                        xqld += dz1_ld + "栋";
                    }
                    if (dz1_dy != null && !"".equals(dz1_dy) && !"null".equals(dz1_dy)) {
                        xqld += dz1_dy + "单元";
                    }
                    if (dz1_s != null && !"".equals(dz1_s) && !"null".equals(dz1_s)) {
                        xqld += dz1_s + "室";
                    }
                    if (dz2_zm != null && !"".equals(dz2_zm) && !"null".equals(dz2_zm)) {
                        xqld += dz2_zm + "组";
                    }
                    if (dz2_hm != null && !"".equals(dz2_hm) && !"null".equals(dz2_hm)) {
                        xqld += dz2_hm + "号";
                    }
                    if (dz3_ldm != null && !"".equals(dz3_ldm) && !"null".equals(dz3_ldm)) {
                        xqld += dz3_ldm + "栋";
                    }
                    if (dz3_sm != null && !"".equals(dz3_sm) && !"null".equals(dz3_sm)) {
                        xqld += dz3_sm + "室";
                    }
                    if (dz5_p != null && !"".equals(dz5_p) && !"null".equals(dz5_p)) {
                        xqld += dz5_p + "排";
                    }
                    if (dz5_h != null && !"".equals(dz5_h) && !"null".equals(dz5_h)) {
                        xqld += dz5_h + "号";
                    }
                    if (dz5_s != null && !"".equals(dz5_s) && !"null".equals(dz5_s)) {
                        xqld += dz5_s + "室";
                    }
                    if (dz6_s != null && !"".equals(dz6_s) && !"null".equals(dz6_s)) {
                        xqld += dz6_s + "室";
                    }

                    //通过街道ID查询社区信息是否存在
                    SysDepart sysDepartDetail = getSysCodeByNameAndPid(detail_mc, street_id);
                    String detail_id = "";
                    String detail_code = "";
                    if(sysDepartDetail!=null){
                        detail_id = sysDepartDetail.getId()!=null ? sysDepartDetail.getId() : "";
                        detail_code = sysDepartDetail.getOrgCode()!=null ? sysDepartDetail.getOrgCode() : "";
                    }else {
                        return Result.error(detail_mc+",不存在"+street_name+"下,请核查信息是否存在数据库!!!");
                    }

                    //通过社区ID查询网格是否存在
                    SysDepart sysDepartGrid = getSysCodeByNameAndPid(grid_name, detail_id);
                    String grid_id = "";
                    String grid_code = "";
                    if(sysDepartGrid!=null){
                        grid_id = sysDepartGrid.getId()!=null ? sysDepartGrid.getId() : "";
                        grid_code = sysDepartGrid.getOrgCode()!=null ? sysDepartGrid.getOrgCode() : "";
                    }else {
                        return Result.error(grid_name+",不存在"+detail_mc+"下,请核查信息是否存在数据库!!!");
                    }

                    //通过网格ID查询是否存在下级网格
                    SysDepart sysDepartGrid2 = getSysCodeByNameAndPid(grid_name2, grid_id);
                    String grid_id2 = "";
                    String grid_code2 = "";
                    if (sysDepartGrid2 != null) {
                        grid_id2 = sysDepartGrid2.getId()!=null ? sysDepartGrid2.getId() : "";
                        grid_code2 = sysDepartGrid2.getOrgCode()!=null ? sysDepartGrid2.getOrgCode() : "";
                    }else {
                        return Result.error(grid_name2+",不存在"+grid_name+"下,请核查信息是否存在数据库!!!");
                    }

                    String detailLhmText = "";
                    if (detail_lhm != null && !"".equals(detail_lhm) && !"null".equals(detail_lhm)) {
                        detailLhmText = detail_lhm +"号";
                    }
                    String addressName = province_name + city_name + district_name + detail_lm + detail_lhm + xqld;
                    addressName = addressName.replaceAll("null", "");
                    String address_name_mz = province_name + city_name + district_name + street_name + detail_mc  + grid_name2 + detail_lm + detailLhmText + xqld;
                    address_name_mz = address_name_mz.replaceAll("null", "");
                    String address_id = java.util.UUID.randomUUID().toString().replaceAll("-", "");
                    StringBuilder addressColumnKey = new StringBuilder();
                    StringBuilder addressColumnVal = new StringBuilder();

                    addressColumnKey.append("id").append(",create_by").append(",create_time").append(",sys_org_code");
                    addressColumnVal.append("'" + address_id + "'").append(",'" + sysUser.getUsername() + "'").append(",'" + sdf.format(new Date()) + "'")
                            .append(grid_code2 == null ? ",'" + grid_code + "'" : ",'" + grid_code2 + "'");
                    addressColumnKey.append(",province_code").append(",province_name").append(",city_code").append(",city_name").append(",district_code")
                            .append(",district_name").append(",street_code").append(",street_name").append(",ssq_code").append(",address_name").append(",detail_mc").append(",detail_lm")
                            .append(",detail_lhm").append(",dz1_xqm").append(",dz1_ld").append(",dz1_dy").append(",dz1_s").append(",dz5_p").append(",dz5_h").append(",dz5_s").append(",dz6_s")
                            .append(",dz2_cm").append(",dz2_zm").append(",dz2_hm").append(",dz3_dsm").append(",dz3_ldm").append(",dz3_sm")
                            .append(",address_id_mz").append(",address_code_mz").append(",address_name_mz").append(",address_code").append(",detail_address")
                            .append(",dz_type");

                    //导入数据人员为网格员时，新增地址ID信息为网格员部门xinxi
                    if("1".equals(sysUser.getUserSf())){
                        List<Map<String, Object>> gridUserDepartList = jdbcTemplate.queryForList("SELECT sd.* FROM sys_user_depart sud ,sys_depart sd ,sys_user su\n" +
                                "WHERE sud.dep_id = sd.id AND sud.user_id = su.id AND su.id = '"+sysUser.getId()+"' AND sd.org_category in ('9','10')");
                        if(gridUserDepartList.size()==1){
                            Map<String, Object> row = gridUserDepartList.get(0);
                            grid_id2 = (String) row.get("id");
                            grid_code2 = (String) row.get("org_code");
                        }
                        if(gridUserDepartList.size()>1){
                            boolean hasOrgCategory = false;
                            for(int i=0;i<gridUserDepartList.size();i++){
                                Map<String, Object> row = gridUserDepartList.get(i);
                                if("10".equals((String) row.get("org_category"))){
                                    grid_id2 = (String) row.get("id");
                                    grid_code2 = (String) row.get("org_code");
                                    hasOrgCategory = true;
                                    break;
                                }
                            }
                            if(hasOrgCategory == false){
                                Map<String, Object> row = gridUserDepartList.get(0);
                                grid_id2 = (String) row.get("id");
                                grid_code2 = (String) row.get("org_code");
                            }
                        }
                    }
                    addressColumnVal.append(",'" + province_code + "'").append(",'" + province_name + "'").append(",'" + city_code + "'").append(",'" + city_name + "'")
                            .append(",'" + district_code + "'").append(",'" + district_name + "'").append(",'" + street_code + "'").append(",'" + street_name + "'").append(",'" + province_code + "," + city_code + "," + district_code + "'")
                            .append(",'" + addressName + "'").append(",'" + dzmc + "'").append(",'" + detail_lm + "'").append("," + detail_lhm + "").append(",'" + dz1_xqm + "'")
                            .append("," + dz1_ld + "").append("," + dz1_dy + "").append("," + dz1_s + "").append("," + dz5_p + "").append("," + dz5_h + "")
                            .append("," + dz5_s + "").append("," + dz6_s + "").append(",'" + dz2_cm + "'").append("," + dz2_zm + "").append("," + dz2_hm + "")
                            .append(",'" + dz3_dsm + "'").append("," + dz3_ldm + "").append("," + dz3_sm + "").append(",'" + grid_id2 + "'").append(",'" + grid_code2 + "'")
                            .append(",'" + address_name_mz + "'").append(",'" + address_code + "'").append(",'" + detail_address + "'").append(",'" + dz_type + "'");
                    String addressColumnKeyStr = addressColumnKey.toString();
                    String addressColumnValStr = addressColumnVal.toString();
                    List<Map<String, Object>> countList = jdbcTemplate.queryForList("select id from cjxt_standard_address where address_name_mz='" + address_name_mz + "'");
                    if (countList.size() == 0) {
                        String addressDataSql = "INSERT INTO cjxt_standard_address(" + addressColumnKeyStr + ") VALUES(" + addressColumnValStr + ")";
                        jdbcTemplate.update(addressDataSql);
                    }
                    String id = java.util.UUID.randomUUID().toString().replaceAll("-", "");
                    StringBuilder columnKey = new StringBuilder();
                    StringBuilder columnVal = new StringBuilder();
                    columnKey.append("id").append(",create_by").append(",create_time").append(",sys_org_code").append(",mb_id").append(",address_id");
                    columnVal.append("'" + id + "'").append(",'" + sysUser.getUsername() + "'").append(",'" + sdf.format(new Date()) + "'")
                            .append(",'" + sysUser.getOrgCode() + "'").append(",'" + mbId + "'").append(",'" + address_id + "'");
                    for (CjxtMbglPz cjxtMbglPz : mbpzList) {
                        if (map.containsKey(cjxtMbglPz.getDbFieldTxt() + "(" + cjxtMbglPz.getDbFieldName() + ")")) {
                            String dbFiledTxt = cjxtMbglPz.getDbFieldTxt();
                            String dbFiledName = cjxtMbglPz.getDbFieldName();
                            Object value = map.get(dbFiledTxt + "(" + cjxtMbglPz.getDbFieldName() + ")");
                            columnKey.append("," + dbFiledName);
                            if (value != null) {
                                if (value instanceof Double) {
                                    BigDecimal bigDecimal = new BigDecimal((Double) value);
                                    columnVal.append(",'" + bigDecimal + "'");
                                } else {
                                    columnVal.append(",'" + value + "'");
                                }
                            } else {
                                columnVal.append("," + value + "");
                            }
                        }
                    }
                    if (columnKey != null && columnKey.length() > 1) {
                        String columnKeyStr = columnKey.toString();
                        String columnValStr = columnVal.toString();
                        String dataSql = "INSERT INTO " + tableName + "(" + columnKeyStr + ") VALUES(" + columnValStr + ")";
                        jdbcTemplate.update(dataSql);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.OK("导入成功！");
    }

    public String getAreaCode(String areaName) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("area_name", areaName);
        CjxtArea cjxtArea = cjxtAreaService.getOne(queryWrapper);
        String areaCode = "";
        if (cjxtArea != null) {
            areaCode = cjxtArea.getAreaCode();
        }
        return areaCode;
    }

    public String doubleToIntStr(String doubleStr) {
        if (doubleStr.length() > 0 && doubleStr.indexOf(".") > -1) {
            return doubleStr.substring(0, doubleStr.indexOf("."));
        } else {
            return doubleStr;
        }
    }

    /**
     * 读取数据
     *
     * @param row
     * @param cellIndex
     * @return
     */
    private String getRowCell(HttpServletRequest request, HSSFRow row, int cellIndex) throws Exception {
        String retStr = "";
        if (row != null) {
            HSSFCell cell = row.getCell(cellIndex);
            if (cell != null) {
                retStr = cell.getStringCellValue().trim();
            } else {
                retStr = "";
            }
        } else {
            retStr = "";
        }
        return retStr;
    }

    /**
     * 模板统计
     *
     * @param pzList
     * @return
     */
    @PostMapping("/mbNumTj")
    @ApiOperation(value = "模板管理配置-模板统计", notes = "模板管理配置-模板统计")
    public Result<Map<String, Object>> mbNumTj(@RequestParam(required = true, name = "userId") String userId,
                                               @RequestParam(required = true, name = "mbCode") String mbCode,
                                               @RequestBody(required = false) List<CjxtMbglPz> pzList) {
        Map<String, Object> result = new HashMap<>();
        if (userId != null) {
            SysUser sysUser = sysUserService.getById(userId);
            List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
            String dataSql = null;
            String countSql = null;
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<CjxtPjwgqx> pjwgqxList = new ArrayList<>(); //片警网格权限
            //查询模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
            if (cjxtMbgl != null) {
                StringBuilder orgCodeBuilder = new StringBuilder();
                StringBuilder sysDepartCode = new StringBuilder();
                StringBuilder zzsbQuery = new StringBuilder();
                List<String> orgCodes = new ArrayList<>();
                if (sysDepartsList.size() > 0) {
                    for (int j = 0; j < sysDepartsList.size(); j++) {
                        SysDepart sysDepart = sysDepartsList.get(j);
                        if (j > 0) {
                            sysDepartCode.append(",");
                        }
                        sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                        if ("4".equals(sysUser.getUserSf()) || "5".equals(sysUser.getUserSf()) || "6".equals(sysUser.getUserSf()) || "7".equals(sysUser.getUserSf()) || "8".equals(sysUser.getUserSf()) || "9".equals(sysUser.getUserSf())) {
                            orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysDepart.getId());
                            for (int i = 0; i < orgCodes.size(); i++) {
                                if (i > 0) {
                                    orgCodeBuilder.append(",");
                                }
                                orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                            }
                        }
                        if ("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())) {
                            pjwgqxList = cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId, userId));
                            for (int i = 0; i < pjwgqxList.size(); i++) {
                                CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                                if (i > 0) {
                                    orgCodeBuilder.append(",");
                                }
                                orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                            }
                        }
//                        if ("1".equals(sysUser.getUserSf())) {
//                            zzsbQuery.append(" OR sys_org_code = '").append(sysUser.getOrgCode()).append("'");
//                        }
                    }
                }

                String orgCode = "''";
                if (orgCodes.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else if (pjwgqxList.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else {
                    if (sysDepartsList.size() > 0) {
                        orgCode = sysDepartCode.toString();
                    }
                }
                StringBuilder sysOrgCodeBuilder = new StringBuilder();
                if (!"".equals(sysOrgCodeBuilder)) {
                    sysOrgCodeBuilder.append(" AND sys_org_code in (").append(orgCode).append(") ");
                }
                //动态表单参数
                StringBuilder pzListAddQuery = new StringBuilder();
                boolean isFirstQuery = true;
                boolean isK = false;
                if (pzList != null && !"".equals(pzList) && !pzList.isEmpty()) {
                    for (CjxtMbglPz cjxtMbglPz : pzList) {
                        String dataV = "";
                        if (cjxtMbglPz.getDataValue() == null || "".equals(cjxtMbglPz.getDataValue()) || cjxtMbglPz.getDataValue().isEmpty()) {
                            dataV = "";
                        } else {
                            dataV = cjxtMbglPz.getDataValue();
                        }
                        if (!"".equals(dataV)) {
                            isK = true;
                            if (isFirstQuery) {
                                pzListAddQuery.append(" AND (");
                                isFirstQuery = false;
                            } else {
                                pzListAddQuery.append(" AND ");
                            }
                            pzListAddQuery.append(cjxtMbglPz.getDbFieldName()).append(" LIKE '%").append(dataV).append("%'");
                        }
                    }
                    if (isK == true) {
                        pzListAddQuery.append(")");
                    }
                }
                if (pzList != null && pzList.size() > 0) {
                    countSql = "SELECT COUNT(*) FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' " + sysOrgCodeBuilder + pzListAddQuery + zzsbQuery;
                }

                int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
                result.put("num", totalCount);
//				resultList = jdbcTemplate.queryForList(dataSql);
//				result.put("records", resultList);
            }
        }
        return Result.OK(result);
    }

    /**
     * @param userId
     * @param mblx
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "模板管理配置-模板列表数据(mblx)", notes = "模板管理配置-模板列表数据(mblx)")
    @PostMapping(value = "/listMblxData")
    public Result<Map<String, Object>> listMblxData(
            @RequestParam(required = true, name = "userId") String userId,
            @RequestParam(required = true, name = "mblx") String mblx,
            @RequestParam(required = false, name = "addressId") String addressId,
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "searchLd") String searchLd,
            @RequestParam(required = false, name = "cjSb") String cjSb,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestBody(required = false) List<CjxtMbglPz> pzList,
            HttpServletRequest req) throws UnsupportedEncodingException {
//        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (userId != null && mblx != null) {
            SysUser sysUser = sysUserService.getById(userId);
            List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
            String dataSql = null;
            String countSql = null;
            List<Map<String, Object>> resultList = new ArrayList<>();
            //查询模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMblx, mblx).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
            if (cjxtMbgl != null) {
                StringBuilder orgCodeBuilder = new StringBuilder();
                StringBuilder sysDepartCode = new StringBuilder();
                List<String> orgCodes = new ArrayList<>();
                if (sysDepartsList.size() > 0) {
                    for (int j = 0; j < sysDepartsList.size(); j++) {
                        SysDepart sysDepart = sysDepartsList.get(j);
                        if (j > 0) {
                            sysDepartCode.append(",");
                        }
                        sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                        if ("4".equals(sysUser.getUserSf()) || "5".equals(sysUser.getUserSf()) || "6".equals(sysUser.getUserSf()) || "7".equals(sysUser.getUserSf()) || "8".equals(sysUser.getUserSf()) || "9".equals(sysUser.getUserSf())) {
                            orgCodes = jdbcTemplate.queryForList("SELECT data_org_code FROM cjxt_bm_data WHERE del_flag = '0' and org_id = ?", String.class, sysDepart.getId());
                            for (int i = 0; i < orgCodes.size(); i++) {
                                if (i > 0) {
                                    orgCodeBuilder.append(",");
                                }
                                orgCodeBuilder.append("'").append(orgCodes.get(i)).append("'");
                            }
                        }
                    }
                }
                StringBuilder additionalQuery = new StringBuilder();
                boolean isFirstCondition = true;
                if (search != null && !"".equals(search) && !search.isEmpty()) {
                    List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getIsQuery, "1"));
                    if (cjxtMbglPzList.size() > 0) {
                        for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
                            if (isFirstCondition) {
                                additionalQuery.append(" AND (");
                                isFirstCondition = false;
                            } else {
                                additionalQuery.append(" OR ");
                            }
                            additionalQuery.append(cjxtMbglPz.getDbFieldName()).append(" LIKE '%").append(search).append("%'");
                        }
                        additionalQuery.append(")");
                    }
                }
                String orgCode = "''";
                if (orgCodes.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else {
                    if (sysDepartsList.size() > 0) {
                        orgCode = sysDepartCode.toString();
                    }
                }

                //动态表单参数
                StringBuilder pzListAddQuery = new StringBuilder();
                boolean isFirstQuery = true;
                boolean isK = false;
                if (pzList != null && !"".equals(pzList) && !pzList.isEmpty()) {
                    for (CjxtMbglPz cjxtMbglPz : pzList) {
                        String dataV = "";
                        if (cjxtMbglPz.getDataValue() == null) {
                            dataV = "";
                        } else {
                            dataV = cjxtMbglPz.getDataValue();
                        }
                        if (!"".equals(dataV)) {
                            isK = true;
                            if (isFirstQuery) {
                                pzListAddQuery.append(" AND (");
                                isFirstQuery = false;
                            } else {
                                pzListAddQuery.append(" OR ");
                            }
                            pzListAddQuery.append(cjxtMbglPz.getDbFieldName()).append(" LIKE '%").append(dataV).append("%'");
                        }
                    }
                    if (isK == true) {
                        pzListAddQuery.append(")");
                    }
                }

                String bm = cjxtMbgl.getBm();
                //任务采集1 自主上报3
                if ("1".equals(cjSb)) {
                    bm = cjxtMbgl.getBm();
                } else if ("3".equals(cjSb)) {
                    bm = cjxtMbgl.getBm() + "_sb";
                }
                dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND sys_org_code in (" + orgCode + ") " + additionalQuery + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                if (pzList != null && pzList.size() > 0) {
                    dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND sys_org_code in (" + orgCode + ") " + pzListAddQuery + additionalQuery + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                }

                if (searchLd != null && !"".equals(searchLd)) {
                    searchLd = java.net.URLDecoder.decode(searchLd, "utf8");
                    searchLd = searchLd.replace(",", "%");
                    searchLd = "%" + searchLd + "%";
                }

                if (searchLd != null && !"".equals(searchLd)) {
                    dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND sys_org_code in (" + orgCode + ") " + additionalQuery + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                }
                if (searchLd != null && !"".equals(searchLd) && pzList != null) {
                    dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND sys_org_code in (" + orgCode + ") " + pzListAddQuery + additionalQuery + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                }
                if (addressId != null && !"".equals(addressId) && !addressId.isEmpty()) {
                    dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND address_id = '" + addressId + "'" + additionalQuery;
                    if (pzList != null && pzList.size() > 0) {
                        dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery;
                    }
                    if (searchLd != null && !"".equals(searchLd)) {
                        dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND address_id = '" + addressId + "'";
                    }
                    if (searchLd != null && !"".equals(searchLd) && pzList != null) {
                        dataSql = "SELECT * FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND address_id = '" + addressId + "'" + pzListAddQuery;
                    }
                }
                resultList = jdbcTemplate.queryForList(dataSql);
                for (Map<String, Object> row : resultList) {
                    String addressid = (String) row.get("address_id");
                    String address = (String) row.get("address");
                    CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressid);
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
                    row.put("address", addressName);
                }

                // 获取总条数的 SQL 查询
                countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND sys_org_code in (" + orgCode + ") " + additionalQuery;
                if (pzList != null && pzList.size() > 0) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND sys_org_code in (" + orgCode + ") " + pzListAddQuery + additionalQuery;
                }
                if (searchLd != null && !"".equals(searchLd)) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND sys_org_code in (" + orgCode + ") " + additionalQuery;
                }
                if (searchLd != null && pzList != null) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND sys_org_code in (" + orgCode + ") " + pzListAddQuery + additionalQuery;
                }
                if (addressId != null && !"".equals(addressId) && !addressId.isEmpty()) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND address_id = '" + addressId + "'" + additionalQuery;
                    if (pzList != null && pzList.size() > 0) {
                        countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery;
                    }
                    if (searchLd != null && !"".equals(searchLd)) {
                        countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND address_id = '" + addressId + "'";
                    }
                    if (searchLd != null && !"".equals(searchLd) && pzList != null) {
                        countSql = "SELECT COUNT(*) FROM " + bm + " WHERE del_flag = '0' AND address like '" + searchLd + "' AND address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery;
                    }
                }
                // 执行查询并获取总条数
                int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
                // 计算总页数
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                // 将总页数添加到结果中
                result.put("totalPages", totalPages);
            }
            result.put("records", resultList);
        }
        return Result.OK(result);
    }

    /**
     * @return
     */
    @ApiOperation(value = "模板管理配置-模板列表数据(code)", notes = "模板管理配置-模板列表数据(code)")
    @PostMapping(value = "/listCodeData")
    public Result<Map<String, Object>> listCodeData(
            @RequestParam(required = false, name = "userId") String userId,
            @RequestParam(required = false, name = "mbCode") String mbCode,
            @RequestParam(required = false, name = "addressId") String addressId,
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "searchLd") String searchLd,
            @RequestParam(required = false, name = "cjSb") String cjSb,
            @RequestParam(required = false, name = "addressTask") String addressTask,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestBody(required = false) List<CjxtMbglPz> pzList,
            HttpServletRequest req) throws UnsupportedEncodingException {
//        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Map<String, Object> result = new HashMap<>();
        if (userId != null && mbCode != null) {
            //返回列表脱敏数据
            List<CjxtMbglPz> mbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().last(" AND ((is_title = '0' AND sfjm = '1' AND mbgl_mbbh = '"+mbCode+"') OR (db_jylx IS NOT NULL AND db_jylx <> '')) AND mbgl_mbbh = '"+mbCode+"' ORDER BY order_num ASC"));

            //自主上报或网格员角色
            StringBuilder zzsbQuery = new StringBuilder();
            //网格员
            SysUser sysUser = null;
            List<SysDepart> sysDepartsList = new ArrayList<>();
            List<CjxtPjwgqx> pjwgqxList = new ArrayList<>();//片警民警网格权限
            //自主上报
            CjxtStandardAddressSbry addressSbry = null;
            if ("1".equals(cjSb) || "4".equals(cjSb)) { //4为网格员待审核自主上报列表
                sysUser = sysUserService.getById(userId);
                sysDepartsList = sysDepartService.queryUserDeparts(userId);
                if("4".equals(cjSb)){//4为网格员待审核自主上报列表
                    zzsbQuery.append(" AND t.shzt = '0'");
                }
            }
            if ("3".equals(cjSb)) {
                //自主上报
                addressSbry = cjxtStandardAddressSbryService.getById(userId);
            }
            String dataSql = null;
            String countSql = null;
            List<Map<String, Object>> resultList = new ArrayList<>();
            //查询模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
            if (cjxtMbgl != null) {
                StringBuilder orgCodeBuilder = new StringBuilder();
                StringBuilder sysDepartCode = new StringBuilder();
                List<String> orgCodes = new ArrayList<>();
                if (sysDepartsList.size() > 0) {
                    for (int j = 0; j < sysDepartsList.size(); j++) {
                        SysDepart sysDepart = sysDepartsList.get(j);
                        if (j > 0) {
                            sysDepartCode.append(",");
                        }
                        sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                    }
                }
                if (sysUser != null) {
                    if ("4".equals(sysUser.getUserSf()) || "5".equals(sysUser.getUserSf()) || "6".equals(sysUser.getUserSf()) || "7".equals(sysUser.getUserSf()) || "8".equals(sysUser.getUserSf()) || "9".equals(sysUser.getUserSf())) {
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
                    if ("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())) {
                        //片警网格查询方式修改
//						String pjwgqxSql = "SELECT wg_code FROM cjxt_pjwgqx WHERE del_flag = '0' AND pj_id = '" + userId + "'" ;
                        pjwgqxList = cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId, userId));
                        for (int i = 0; i < pjwgqxList.size(); i++) {
                            CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                            if (i > 0) {
                                orgCodeBuilder.append(",");
                            }
                            orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                        }
                    }
//                    if ("1".equals(sysUser.getUserSf())) {
//                        zzsbQuery.append(" AND t.create_by = '").append(sysUser.getUsername()).append("'");
//                    }
                }
                if (addressSbry != null && addressSbry.getPhone() != null && !"".equals(addressSbry.getPhone())) {
                    zzsbQuery.append(" AND t.create_by = '").append(addressSbry.getPhone()).append("'");
                }
                if("3".equals(cjSb)){
                    zzsbQuery.append(" AND t.address_id = '"+addressId+"'");
                }
                StringBuilder additionalQuery = new StringBuilder();
                boolean isFirstCondition = true;
                if (search != null && !"".equals(search) && !search.isEmpty()) {
                    List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getIsQuery, "1"));
                    if (cjxtMbglPzList.size() > 0) {
                        for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
                            if (isFirstCondition) {
                                additionalQuery.append(" AND (");
                                isFirstCondition = false;
                            } else {
                                additionalQuery.append(" OR ");
                            }
                            additionalQuery.append("t." + cjxtMbglPz.getDbFieldName()).append(" LIKE '%").append(search).append("%'");
                        }
                        additionalQuery.append(")");
                    }
                }
                String orgCode = "''";
                if (orgCodes.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else if (pjwgqxList.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else {
                    if (sysDepartsList.size() > 0) {
                        orgCode = sysDepartCode.toString();
                    }
                }

                //部门信息数据
                StringBuilder orgCodeQuery = new StringBuilder();
                if(("".equals(addressTask) || addressTask == null || !"1".equals(addressTask)) && sysUser!=null && !"1".equals(sysUser.getUserSf())){
                    orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
                }

                if(("".equals(addressTask) || addressTask == null || !"1".equals(addressTask)) && sysUser!=null && "1".equals(sysUser.getUserSf())){
                    orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
                }
                if(("".equals(addressTask) || addressTask == null || !"1".equals(addressTask)) && "3".equals(cjSb)){
                    orgCodeQuery.append(" AND t.address_id in (").append(addressId).append(")");
                }

                //动态表单参数
                StringBuilder pzListAddQuery = new StringBuilder();
                boolean isFirstQuery = true;
                boolean isK = false;
                if (pzList != null && !"".equals(pzList) && !pzList.isEmpty()) {
                    for (CjxtMbglPz cjxtMbglPz : pzList) {
                        String dataV = "";
                        if (cjxtMbglPz.getDataValue() == null || "".equals(cjxtMbglPz.getDataValue()) || cjxtMbglPz.getDataValue().isEmpty()) {
                            dataV = "";
                        } else {
                            dataV = cjxtMbglPz.getDataValue();
                        }
                        if (!"".equals(dataV)) {
                            isK = true;
                            if (isFirstQuery) {
                                pzListAddQuery.append(" AND (");
                                isFirstQuery = false;
                            } else {
                                pzListAddQuery.append(" AND ");
                            }
                            pzListAddQuery.append("t." + cjxtMbglPz.getDbFieldName()).append(" LIKE '%").append(dataV).append("%'");
                        }
                    }
                    if (isK == true) {
                        pzListAddQuery.append(")");
                    }
                }

                String bm = cjxtMbgl.getBm();
                //任务采集1 自主上报3
                if ("1".equals(cjSb)) {
                    bm = cjxtMbgl.getBm();
                } else if ("3".equals(cjSb) || "4".equals(cjSb)) {//4为网格员待审核自主上报列表
                    bm = cjxtMbgl.getBm() + "_sb";
                }
                StringBuilder rymbOrder = new StringBuilder();
                if (!"".equals(addressId) && addressId != null && "RY001".equals(mbCode)) {
                    rymbOrder.append(", CAST(t.yhzgx AS UNSIGNED)");

                }
                dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + zzsbQuery + orgCodeQuery + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

                if (pzList != null && pzList.size() > 0) {
                    dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + zzsbQuery + orgCodeQuery + pzListAddQuery + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                }

                if (searchLd != null && !"".equals(searchLd)) {
                    searchLd = java.net.URLDecoder.decode(searchLd, "utf8");
                    searchLd = searchLd.replace(",", "%");
                    searchLd = "%" + searchLd + "%";
                }

                if (searchLd != null && !"".equals(searchLd)) {
                    dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' " + zzsbQuery + orgCodeQuery + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                }
                if (searchLd != null && !"".equals(searchLd) && pzList != null && pzList.size() > 0) {
                    dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' " + zzsbQuery + orgCodeQuery + pzListAddQuery + additionalQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                }
                if (addressId != null && !"".equals(addressId) && !addressId.isEmpty()) {
                    dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + additionalQuery + zzsbQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    if (pzList != null && pzList.size() > 0) {
                        dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery + zzsbQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    }
                    if (searchLd != null && !"".equals(searchLd)) {
                        dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' AND t.address_id = '" + addressId + "'" + zzsbQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    }
                    if (searchLd != null && !"".equals(searchLd) && pzList != null && pzList.size() > 0) {
                        dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' AND t.address_id = '" + addressId + "'" + pzListAddQuery + zzsbQuery + " ORDER BY t.create_time DESC, cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
                    }
                }
                resultList = jdbcTemplate.queryForList(dataSql);

                for (Map<String, Object> row : resultList) {
                    row.forEach((key, value) -> {
                        if (value == null) {
                            row.put(key, "");
                        }
                    });

                    if(mbglPzList.size()>0){
                        for(CjxtMbglPz mbglPz: mbglPzList){
                            Object value = row.get(mbglPz.getDbFieldName());
                            if(!"".equals(value) && value!=null){
                                if(((String) value).contains("_sxby")){
                                    String dataV = sjjm(value.toString());
                                    row.put(mbglPz.getDbFieldName(), dataV);
                                }else {
                                    row.put(mbglPz.getDbFieldName(), value);
                                }
                            }else {
                                row.put(mbglPz.getDbFieldName(), "");
                            }

                            if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
                                //身份证
                                if("1".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String sfzh = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(sfzh) && sfzh!=null){
                                            if(sfzh.contains("_sxby")){
                                                String sfzhTm = desensitize(sjjm(sfzh));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }else {
                                                String sfzhTm = desensitize(sfzh);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String sfzh = desensitize((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                                //手机号
                                if("2".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String phone = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(phone) && phone!=null){
                                            if(phone.contains("_sxby")){
                                                String phoneTm = maskPhone(sjjm(phone));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }else {
                                                String phoneTm = maskPhone(phone);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String phone = maskPhone((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String addressid = (String) row.get("address_id");
                    String address = (String) row.get("address");
                    CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressid);
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
                    row.put("address", addressName);
                }

                // 获取总条数的 SQL 查询
                countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + zzsbQuery + orgCodeQuery + additionalQuery;
                if (pzList != null && pzList.size() > 0) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' " + zzsbQuery + orgCodeQuery + pzListAddQuery + additionalQuery;
                }
                if (searchLd != null && !"".equals(searchLd)) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' " + zzsbQuery + orgCodeQuery + additionalQuery;
                }
                if (searchLd != null && pzList != null && pzList.size() > 0) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' " + zzsbQuery + orgCodeQuery + pzListAddQuery + additionalQuery;
                }
                if (addressId != null && !"".equals(addressId) && !addressId.isEmpty()) {
                    countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + additionalQuery + zzsbQuery;
                    if (pzList != null && pzList.size() > 0) {
                        countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery + zzsbQuery;
                    }
                    if (searchLd != null && !"".equals(searchLd)) {
                        countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' AND t.address_id = '" + addressId + "'" + zzsbQuery;
                    }
                    if (searchLd != null && !"".equals(searchLd) && pzList != null && pzList.size() > 0) {
                        countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND cst.address_name like '" + searchLd + "' AND t.address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery + zzsbQuery;
                    }
                }
                // 执行查询并获取总条数
                int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
                // 计算总页数
                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                // 将总页数添加到结果中
                result.put("totalPages", totalPages);
            }
            result.put("records", resultList);
            result.put("tmzd",mbglPzList);
        }
        return Result.OK(result);
    }

    /**
     * @param mbId
     * @param dataId
     * @param req
     * @return
     */
    @ApiOperation(value = "模板管理配置-动态数据删除", notes = "模板管理配置-动态数据删除")
    @GetMapping(value = "/dataValueDelete")
    public Result<String> dataValueDelete(
            @RequestParam(required = true, name = "mbId") String mbId,
            @RequestParam(required = true, name = "dataId") String dataId,
            @RequestParam(required = false, name = "cjSb") String cjSb,
            HttpServletRequest req) {
//        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (mbId != null && dataId != null) {
            //查询模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getId, mbId).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
            if (cjxtMbgl != null) {
                String bm = cjxtMbgl.getBm();
                if ("1".equals(cjSb)) {
                    bm = cjxtMbgl.getBm();
                } else if ("3".equals(cjSb)) {
                    bm = cjxtMbgl.getBm() + "_sb";
                }
                String renameColumnSql = "UPDATE " + bm + " SET del_flag = '1' WHERE id = '" + dataId + "' ;";
                jdbcTemplate.execute(renameColumnSql);
            }
        }
        return Result.OK("删除成功");
    }

    /**
     * 网格员自主上报-批量审核
     * @param mbCode 模板Code
     * @param dataId 数据ID
     * @param userId 用户ID
     * @return
     */
    @AutoLog(value = "网格员自主上报-批量审核")
    @ApiOperation(value="网格员自主上报-批量审核", notes="网格员自主上报-批量审核")
    @PostMapping(value = "/batchReview")
    public Result<String> batchReview(@RequestParam(name="mbCode",required=false) String mbCode,
                                      @RequestParam(name="dataId",required=false) String dataId,
                                      @RequestParam(name="shzt",required=true) String shzt,
                                      @RequestParam(name="errMsg",required=false) String errMsg,
                                      @RequestParam(name="addressId",required=true) String addressId,
                                      @RequestParam(name="mbCodeS",required=false) String mbCodeS,
                                      @RequestParam(name="warMsgUserId",required=false) String warMsgUserId,
                                      @RequestParam(name="userId",required=true) String userId,
                                      @RequestParam(name="flag",required = false) String flag) {
        SysUser sysUser = sysUserService.getById(userId);
        //处理数据createBy
        String createByUser = "";

        if(mbCode!=null && !"".equals(mbCode)){
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
            //积分规则
            CjxtScoreRule cjxtScoreRule = cjxtScoreRuleService.getOne(new LambdaQueryWrapper<CjxtScoreRule>().eq(CjxtScoreRule::getMbCode, mbCode));
            if(cjxtMbgl==null){
                return Result.OK("当前模板不存在上报数据!");
            }
            StringBuilder errMsgBuilder = new StringBuilder();
            if(!"".equals(errMsg) && errMsg!=null){
                errMsgBuilder.append(" , err_msg = '"+errMsg+"'");
            }
            String DATAID[] = dataId.split(",");
            //定义所有的数据ID
            String dataVId = "";
            for(int i = 0;i< DATAID.length;i++){
                String dataID = DATAID[i];
                dataVId += "'"+dataID+"',";
                String renameColumnSql = "UPDATE " + cjxtMbgl.getBm()+"_sb" + " SET shzt = '" + shzt + "' "+errMsgBuilder+"  WHERE id = '" + dataID + "' ;";
                jdbcTemplate.execute(renameColumnSql);
            }

            List<CjxtMbglPz> list =  cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
            //全部通过
            if("1".equals(shzt) && !"".equals(dataVId)){
                dataVId = dataVId.substring(0,dataVId.length()-1);
                List<Map<String, Object>> resultZzsb = new ArrayList<>();
                List<Map<String, Object>> hasValue = new ArrayList<>();

                String addressUser = "";
                if(("RY001".equals(cjxtMbgl.getMbbh()) || "RY002".equals(cjxtMbgl.getMbbh()))){
                    addressUser = "SELECT t.* FROM " + cjxtMbgl.getBm()+"_sb" + " t  WHERE t.del_flag = '0' AND t.id in (" + dataVId + ")";
                }else {
                    addressUser = "SELECT t.* FROM " + cjxtMbgl.getBm()+"_sb" + " t  WHERE t.del_flag = '0' AND t.id = " + dataVId + "";
                }
                resultZzsb = jdbcTemplate.queryForList(addressUser);
                if(resultZzsb.size()>0) {
                    StringBuilder fields = new StringBuilder();
                    StringBuilder placeholders = new StringBuilder();
                    for (CjxtMbglPz pz : list) {
                        fields.append(pz.getDbFieldName()).append(", ");
                        placeholders.append("?, ");
                    }
                    // 去掉最后的逗号和空格
                    fields.setLength(fields.length() - 2);
                    placeholders.setLength(placeholders.length() - 2);

                    String dataIdLs = "";
                    String insertSql = "";
                    List<Object> insertValues = new ArrayList<>();
                    List<Object> insertLsValues = new ArrayList<>();
                    if("RY001".equals(cjxtMbgl.getMbbh())){
                        for (Map<String, Object> row : resultZzsb) {
                            createByUser = (String) row.get("create_by")+",";
                            String rysfzh = (String) row.get("rysfzh");
                            String hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.rysfzh = ? AND t.address_id = ?";
                            hasValue = jdbcTemplate.queryForList(hasValueSql, rysfzh, addressId);
                            if(hasValue.size()>0){
                                for (Map<String, Object> hasValueRow : hasValue) {
                                    StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
                                    List<Object> updateValues = new ArrayList<>();
                                    row.remove("shzt");
                                    for (String key : row.keySet()) {
                                        if (!"id".equals(key) && !"create_by".equals(key) && !"create_time".equals(key) && !"shzt".equals(key)) {
                                            updateSql.append(key).append(" = ?, ");
                                            updateValues.add(row.get(key));
                                        }
                                    }
                                    // 去掉最后的逗号和空格
                                    updateSql.setLength(updateSql.length() - 2);
                                    updateSql.append(" WHERE id = ?");
                                    updateValues.add(hasValueRow.get("id"));
                                    jdbcTemplate.update(updateSql.toString(), updateValues.toArray());

                                    dataIdLs = (String) hasValueRow.get("id") ;
                                }
                            } else {
                                dataIdLs = UUID.randomUUID().toString().replace("-", "");
                                insertSql = "INSERT INTO " + cjxtMbgl.getBm() + " (" + fields.toString() + ") VALUES (" + placeholders.toString() + ")";
                                for (CjxtMbglPz pz : list) {
                                    if ("id".equals(pz.getDbFieldName())) {
                                        insertValues.add(dataIdLs); // 使用生成的UUID替换id字段的值
                                    } else {
                                        insertValues.add(row.get(pz.getDbFieldName()));
                                    }
                                }
                                jdbcTemplate.update(insertSql, insertValues.toArray());
                            }

                            //插入历史
                            if("1".equals(cjxtMbgl.getSfls())){
                                insertSql = "INSERT INTO " + cjxtMbgl.getBm()+"_ls" + " (" + fields.toString() + ", data_id_ls) VALUES (" + placeholders.toString() + ", ?)";
                                for (CjxtMbglPz pz : list) {
                                    if ("id".equals(pz.getDbFieldName())) {
                                        insertLsValues.add(UUID.randomUUID().toString().replace("-", ""));
                                    } else {
                                        insertLsValues.add(row.get(pz.getDbFieldName()));
                                    }
                                }
                                insertLsValues.add(dataIdLs);
                                jdbcTemplate.update(insertSql, insertLsValues.toArray());
                            }

                            //积分明细
                            if (cjxtScoreRule != null) {
                                CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                                cjxtScoreDetail.setUserId(sysUser.getId());
                                cjxtScoreDetail.setUserName(sysUser.getRealname());
                                cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                                cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                                cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                                cjxtScoreDetail.setDataId(dataIdLs);
                                cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                                cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                                cjxtScoreDetailService.save(cjxtScoreDetail);
                            }
                        }
                    } else if("RY002".equals(cjxtMbgl.getMbbh())){
                        for (Map<String, Object> row : resultZzsb) {
                            row.remove("shzt");
                            createByUser = (String) row.get("create_by")+",";
                            String sfzh = (String) row.get("sfzh");
                            String hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.sfzh = ? AND t.address_id = ?";
                            hasValue = jdbcTemplate.queryForList(hasValueSql, sfzh, addressId);
                            if(hasValue.size()>0){
                                for (Map<String, Object> hasValueRow : hasValue) {
                                    StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
                                    List<Object> updateValues = new ArrayList<>();
                                    for (String key : row.keySet()) {
                                        if (!"id".equals(key) && !"create_by".equals(key) && !"create_time".equals(key) && !"shzt".equals(key)) {
                                            updateSql.append(key).append(" = ?, ");
                                            updateValues.add(row.get(key));
                                        }
                                    }
                                    // 去掉最后的逗号和空格
                                    updateSql.setLength(updateSql.length() - 2);
                                    updateSql.append(" WHERE id = ?");
                                    updateValues.add(hasValueRow.get("id"));
                                    jdbcTemplate.update(updateSql.toString(), updateValues.toArray());

                                    dataIdLs = (String) hasValueRow.get("id") ;
                                }
                            } else {
                                dataIdLs = UUID.randomUUID().toString().replace("-", "");
                                insertSql = "INSERT INTO " + cjxtMbgl.getBm() + " (" + fields.toString() + ") VALUES (" + placeholders.toString() + ")";
                                insertValues = new ArrayList<>();
                                for (CjxtMbglPz pz : list) {
                                    if ("id".equals(pz.getDbFieldName())) {
                                        insertValues.add(dataIdLs); // 使用生成的UUID替换id字段的值
                                    } else {
                                        insertValues.add(row.get(pz.getDbFieldName()));
                                    }
                                }
                                jdbcTemplate.update(insertSql, insertValues.toArray());
                            }

                            //插入历史
                            if("1".equals(cjxtMbgl.getSfls())){
                                insertSql = "INSERT INTO " + cjxtMbgl.getBm()+"_ls" + " (" + fields.toString() + ", data_id_ls) VALUES (" + placeholders.toString() + ", ?)";
                                for (CjxtMbglPz pz : list) {
                                    if ("id".equals(pz.getDbFieldName())) {
                                        insertLsValues.add(UUID.randomUUID().toString().replace("-", ""));
                                    } else {
                                        insertLsValues.add(row.get(pz.getDbFieldName()));
                                    }
                                }
                                insertLsValues.add(dataIdLs);
                                jdbcTemplate.update(insertSql, insertLsValues.toArray());
                            }

                            //积分明细
                            if (cjxtScoreRule != null) {
                                CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                                cjxtScoreDetail.setUserId(sysUser.getId());
                                cjxtScoreDetail.setUserName(sysUser.getRealname());
                                cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                                cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                                cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                                cjxtScoreDetail.setDataId(dataIdLs);
                                cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                                cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                                cjxtScoreDetailService.save(cjxtScoreDetail);
                            }
                        }
                    } else {
                        //其他模板
                        String dbonly = cjxtMbgl.getDbOnly();
                        for (Map<String, Object> row : resultZzsb) {
                            createByUser = (String) row.get("create_by")+",";
                            String hasValueSql = "";
                            if(!"".equals(dbonly) && dbonly!=null){
                                hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t."+dbonly+" = ? AND t.address_id = ?";
                                hasValue = jdbcTemplate.queryForList(hasValueSql, dbonly, addressId);
                            }else {
                                hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.address_id = ?";
                                hasValue = jdbcTemplate.queryForList(hasValueSql, addressId);
                            }
                            if(hasValue.size()>0){
                                for (Map<String, Object> hasValueRow : hasValue) {
                                    StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
                                    List<Object> updateValues = new ArrayList<>();
                                    row.remove("shzt");
                                    for (String key : row.keySet()) {
                                        if (!"id".equals(key) && !"create_by".equals(key) && !"create_time".equals(key) && !"shzt".equals(key)) {
                                            updateSql.append(key).append(" = ?, ");
                                            updateValues.add(row.get(key));
                                        }
                                    }
                                    // 去掉最后的逗号和空格
                                    updateSql.setLength(updateSql.length() - 2);
                                    updateSql.append(" WHERE id = ?");
                                    updateValues.add(hasValueRow.get("id"));
                                    jdbcTemplate.update(updateSql.toString(), updateValues.toArray());

                                    dataIdLs = (String) hasValueRow.get("id") ;
                                }
                            } else {
                                dataIdLs = UUID.randomUUID().toString().replace("-", "");
                                insertSql = "INSERT INTO " + cjxtMbgl.getBm() + " (" + fields.toString() + ") VALUES (" + placeholders.toString() + ")";
                                insertValues = new ArrayList<>();
                                for (CjxtMbglPz pz : list) {
                                    if ("id".equals(pz.getDbFieldName())) {
                                        insertValues.add(dataIdLs); // 使用生成的UUID替换id字段的值
                                    } else {
                                        insertValues.add(row.get(pz.getDbFieldName()));
                                    }
                                }
                                jdbcTemplate.update(insertSql, insertValues.toArray());
                            }

                            //插入历史
                            if("1".equals(cjxtMbgl.getSfls())){
                                insertSql = "INSERT INTO " + cjxtMbgl.getBm()+"_ls" + " (" + fields.toString() + ", data_id_ls) VALUES (" + placeholders.toString() + ", ?)";
                                for (CjxtMbglPz pz : list) {
                                    if ("id".equals(pz.getDbFieldName())) {
                                        insertLsValues.add(UUID.randomUUID().toString().replace("-", ""));
                                    } else {
                                        insertLsValues.add(row.get(pz.getDbFieldName()));
                                    }
                                }
                                insertLsValues.add(dataIdLs);
                                jdbcTemplate.update(insertSql, insertLsValues.toArray());
                            }

                            //积分明细
                            if (cjxtScoreRule != null) {
                                CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                                cjxtScoreDetail.setUserId(sysUser.getId());
                                cjxtScoreDetail.setUserName(sysUser.getRealname());
                                cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                                cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                                cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                                cjxtScoreDetail.setDataId(dataIdLs);
                                cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                                cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                                cjxtScoreDetailService.save(cjxtScoreDetail);
                            }
                        }
                    }
                }
            }

        } else if(!"".equals(mbCodeS) && mbCodeS!=null){
            //最外层判断全部通过
            String[] MBCODES = mbCodeS.split(",");
            String[] addressIDS = addressId.split(",");

            //处理地址ID数据
            for(int k = 0;k<addressIDS.length;k++){
                String ADDRESSID = addressIDS[k];
                for(int i=0;i<MBCODES.length;i++){
                    String mbCODE = MBCODES[i];
                    CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCODE).eq(CjxtMbgl::getIsDb,"1"));
                    //积分规则
                    CjxtScoreRule cjxtScoreRule = cjxtScoreRuleService.getOne(new LambdaQueryWrapper<CjxtScoreRule>().eq(CjxtScoreRule::getMbCode, mbCODE));
                    String renameColumnSql = "UPDATE " + cjxtMbgl.getBm()+"_sb" + " SET shzt = '" + shzt + "'  WHERE address_id = '" + ADDRESSID + "' ;";
                    if(!"".equals(flag) && flag!=null && "pc".equals(flag)){
                        renameColumnSql = "UPDATE " + cjxtMbgl.getBm()+"_sb" + " SET shzt = '" + shzt + "'  WHERE id = '" + ADDRESSID + "' ;";
                    }
                    jdbcTemplate.execute(renameColumnSql);

                    List<CjxtMbglPz> list =  cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).orderByAsc(CjxtMbglPz::getOrderNum));
                    List<Map<String, Object>> resultZzsb = new ArrayList<>();
                    List<Map<String, Object>> hasValue = new ArrayList<>();
                    String addressUser = "SELECT t.* FROM " + cjxtMbgl.getBm()+"_sb" + " t  WHERE t.del_flag = '0' AND t.address_id = '" + ADDRESSID + "'";
                    resultZzsb = jdbcTemplate.queryForList(addressUser);
                    if(resultZzsb.size()>0) {
                        StringBuilder fields = new StringBuilder();
                        StringBuilder placeholders = new StringBuilder();
                        for (CjxtMbglPz pz : list) {
                            fields.append(pz.getDbFieldName()).append(", ");
                            placeholders.append("?, ");
                        }
                        // 去掉最后的逗号和空格
                        fields.setLength(fields.length() - 2);
                        placeholders.setLength(placeholders.length() - 2);

                        String dataIdLs = "";
                        String insertSql = "";
                        List<Object> insertValues = new ArrayList<>();
                        List<Object> insertLsValues = new ArrayList<>();
                        if("RY001".equals(cjxtMbgl.getMbbh())){
                            for (Map<String, Object> row : resultZzsb) {
                                //获取数据上报人信息
                                createByUser = (String) row.get("create_by")+",";
                                String rysfzh = (String) row.get("rysfzh");
                                String hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.rysfzh = ? AND t.address_id = ?";
                                hasValue = jdbcTemplate.queryForList(hasValueSql, rysfzh, ADDRESSID);
                                if(hasValue.size()>0){
                                    for (Map<String, Object> hasValueRow : hasValue) {
                                        StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
                                        List<Object> updateValues = new ArrayList<>();
                                        row.remove("shzt");
                                        for (String key : row.keySet()) {
                                            if (!"id".equals(key) && !"create_by".equals(key) && !"create_time".equals(key) && !"shzt".equals(key)) {
                                                updateSql.append(key).append(" = ?, ");
                                                updateValues.add(row.get(key));
                                            }
                                        }
                                        // 去掉最后的逗号和空格
                                        updateSql.setLength(updateSql.length() - 2);
                                        updateSql.append(" WHERE id = ?");
                                        updateValues.add(hasValueRow.get("id"));
                                        jdbcTemplate.update(updateSql.toString(), updateValues.toArray());

                                        dataIdLs = (String) hasValueRow.get("id") ;
                                    }
                                } else {
                                    dataIdLs = UUID.randomUUID().toString().replace("-", "");
                                    insertSql = "INSERT INTO " + cjxtMbgl.getBm() + " (" + fields.toString() + ") VALUES (" + placeholders.toString() + ")";
                                    for (CjxtMbglPz pz : list) {
                                        if ("id".equals(pz.getDbFieldName())) {
                                            insertValues.add(dataIdLs); // 使用生成的UUID替换id字段的值
                                        } else {
                                            insertValues.add(row.get(pz.getDbFieldName()));
                                        }
                                    }
                                    jdbcTemplate.update(insertSql, insertValues.toArray());
                                }

                                //插入历史
                                if("1".equals(cjxtMbgl.getSfls())){
                                    insertSql = "INSERT INTO " + cjxtMbgl.getBm()+"_ls" + " (" + fields.toString() + ", data_id_ls) VALUES (" + placeholders.toString() + ", ?)";
                                    for (CjxtMbglPz pz : list) {
                                        if ("id".equals(pz.getDbFieldName())) {
                                            insertLsValues.add(UUID.randomUUID().toString().replace("-", ""));
                                        } else {
                                            insertLsValues.add(row.get(pz.getDbFieldName()));
                                        }
                                    }
                                    insertLsValues.add(dataIdLs);
                                    jdbcTemplate.update(insertSql, insertLsValues.toArray());
                                }

                                //积分明细
                                if (cjxtScoreRule != null) {
                                    CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                                    cjxtScoreDetail.setUserId(sysUser.getId());
                                    cjxtScoreDetail.setUserName(sysUser.getRealname());
                                    cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                                    cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                                    cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                                    cjxtScoreDetail.setDataId(dataIdLs);
                                    cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                                    cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                                    cjxtScoreDetailService.save(cjxtScoreDetail);
                                }
                            }
                        } else if("RY002".equals(cjxtMbgl.getMbbh())){
                            for (Map<String, Object> row : resultZzsb) {
                                //获取数据上报人信息
                                createByUser = (String) row.get("create_by")+",";
                                String sfzh = (String) row.get("sfzh");
                                String hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.sfzh = ? AND t.address_id = ?";
                                hasValue = jdbcTemplate.queryForList(hasValueSql, sfzh, ADDRESSID);
                                if(hasValue.size()>0){
                                    for (Map<String, Object> hasValueRow : hasValue) {
                                        StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
                                        List<Object> updateValues = new ArrayList<>();
                                        row.remove("shzt");
                                        for (String key : row.keySet()) {
                                            if (!"id".equals(key) && !"create_by".equals(key) && !"create_time".equals(key) && !"shzt".equals(key)) {
                                                updateSql.append(key).append(" = ?, ");
                                                updateValues.add(row.get(key));
                                            }
                                        }
                                        // 去掉最后的逗号和空格
                                        updateSql.setLength(updateSql.length() - 2);
                                        updateSql.append(" WHERE id = ?");
                                        updateValues.add(hasValueRow.get("id"));
                                        jdbcTemplate.update(updateSql.toString(), updateValues.toArray());
                                    }
                                } else {
                                    dataIdLs = UUID.randomUUID().toString().replace("-", "");
                                    insertSql = "INSERT INTO " + cjxtMbgl.getBm() + " (" + fields.toString() + ") VALUES (" + placeholders.toString() + ")";
                                    insertValues = new ArrayList<>();
                                    for (CjxtMbglPz pz : list) {
                                        if ("id".equals(pz.getDbFieldName())) {
                                            insertValues.add(dataIdLs); // 使用生成的UUID替换id字段的值
                                        } else {
                                            insertValues.add(row.get(pz.getDbFieldName()));
                                        }
                                    }
                                    jdbcTemplate.update(insertSql, insertValues.toArray());
                                }

                                //插入历史
                                if("1".equals(cjxtMbgl.getSfls())){
                                    insertSql = "INSERT INTO " + cjxtMbgl.getBm()+"_ls" + " (" + fields.toString() + ", data_id_ls) VALUES (" + placeholders.toString() + ", ?)";
                                    for (CjxtMbglPz pz : list) {
                                        if ("id".equals(pz.getDbFieldName())) {
                                            insertLsValues.add(UUID.randomUUID().toString().replace("-", ""));
                                        } else {
                                            insertLsValues.add(row.get(pz.getDbFieldName()));
                                        }
                                    }
                                    insertLsValues.add(dataIdLs);
                                    jdbcTemplate.update(insertSql, insertLsValues.toArray());
                                }

                                //积分明细
                                if (cjxtScoreRule != null) {
                                    CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                                    cjxtScoreDetail.setUserId(sysUser.getId());
                                    cjxtScoreDetail.setUserName(sysUser.getRealname());
                                    cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                                    cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                                    cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                                    cjxtScoreDetail.setDataId(dataIdLs);
                                    cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                                    cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                                    cjxtScoreDetailService.save(cjxtScoreDetail);
                                }
                            }
                        } else {
                            //其他模板
                            String dbonly = cjxtMbgl.getDbOnly();
                            for (Map<String, Object> row : resultZzsb) {
                                //获取数据上报人信息
                                createByUser = (String) row.get("create_by")+",";
                                String hasValueSql = "";
                                if(!"".equals(dbonly) && dbonly!=null){
                                    hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t."+dbonly+" = ? AND t.address_id = ?";
                                    hasValue = jdbcTemplate.queryForList(hasValueSql, dbonly, ADDRESSID);
                                }else {
                                    hasValueSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.address_id = ?";
                                    hasValue = jdbcTemplate.queryForList(hasValueSql, ADDRESSID);
                                }
                                if(hasValue.size()>0){
                                    for (Map<String, Object> hasValueRow : hasValue) {
                                        StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
                                        List<Object> updateValues = new ArrayList<>();
                                        row.remove("shzt");
                                        for (String key : row.keySet()) {
                                            if (!"id".equals(key) && !"create_by".equals(key) && !"create_time".equals(key) && !"shzt".equals(key)) {
                                                updateSql.append(key).append(" = ?, ");
                                                updateValues.add(row.get(key));
                                            }
                                        }
                                        // 去掉最后的逗号和空格
                                        updateSql.setLength(updateSql.length() - 2);
                                        updateSql.append(" WHERE id = ?");
                                        updateValues.add(hasValueRow.get("id"));
                                        jdbcTemplate.update(updateSql.toString(), updateValues.toArray());
                                    }
                                } else {
                                    dataIdLs = UUID.randomUUID().toString().replace("-", "");
                                    insertSql = "INSERT INTO " + cjxtMbgl.getBm() + " (" + fields.toString() + ") VALUES (" + placeholders.toString() + ")";
                                    insertValues = new ArrayList<>();
                                    for (CjxtMbglPz pz : list) {
                                        if ("id".equals(pz.getDbFieldName())) {
                                            insertValues.add(dataIdLs); // 使用生成的UUID替换id字段的值
                                        } else {
                                            insertValues.add(row.get(pz.getDbFieldName()));
                                        }
                                    }
                                    jdbcTemplate.update(insertSql, insertValues.toArray());
                                }

                                //插入历史
                                if("1".equals(cjxtMbgl.getSfls())){
                                    insertSql = "INSERT INTO " + cjxtMbgl.getBm()+"_ls" + " (" + fields.toString() + ", data_id_ls) VALUES (" + placeholders.toString() + ", ?)";
                                    for (CjxtMbglPz pz : list) {
                                        if ("id".equals(pz.getDbFieldName())) {
                                            insertLsValues.add(UUID.randomUUID().toString().replace("-", ""));
                                        } else {
                                            insertLsValues.add(row.get(pz.getDbFieldName()));
                                        }
                                    }
                                    insertLsValues.add(dataIdLs);
                                    jdbcTemplate.update(insertSql, insertLsValues.toArray());
                                }

                                //积分明细
                                if (cjxtScoreRule != null) {
                                    CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                                    cjxtScoreDetail.setUserId(sysUser.getId());
                                    cjxtScoreDetail.setUserName(sysUser.getRealname());
                                    cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                                    cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                                    cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                                    cjxtScoreDetail.setDataId(dataIdLs);
                                    cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                                    cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                                    cjxtScoreDetailService.save(cjxtScoreDetail);
                                }
                            }
                        }
                    }
                }


            }
        } else {
            return Result.OK("当前模板不存在上报数据!");
        }
        //发送消息提醒上报人员
        if("2".equals(shzt) || "1".equals(shzt)){
            String shztText = "通过";
            if("2".equals(shzt)){
                shztText = "不通过";
            }
            //迁出网格员接受信息
            CjxtWarningMessage messageSec = new CjxtWarningMessage();
            String[] createBYS = createByUser.split(",");
            for(int i = 0;i<createBYS.length;i++){
                String createBy = createBYS[i];
                CjxtStandardAddressSbry standardAddressSbry = cjxtStandardAddressSbryService.getOne(new LambdaQueryWrapper<CjxtStandardAddressSbry>().eq(CjxtStandardAddressSbry::getPhone,createBy));
                if(standardAddressSbry!=null){
                    messageSec.setUserId(standardAddressSbry.getId());
                    messageSec.setUsername(standardAddressSbry.getPhone());
                    messageSec.setRealname(standardAddressSbry.getUserName());
                    if("1".equals(shzt)){
                        messageSec.setMessage(sysUser.getRealname() + "网格员已审核自主上报提交信息，审核"+shztText+"。");
                    }else {
                        if(!"".equals(errMsg) && errMsg!=null){
                            messageSec.setMessage(sysUser.getRealname() + "网格员已审核自主上报提交信息，审核"+shztText+";不通过原因："+errMsg+"。");
                        }else {
                            messageSec.setMessage(sysUser.getRealname() + "网格员已审核自主上报提交信息，审核"+shztText+";网格员未说明不通过原因，网格员联系电话："+sysUser.getPhone()+"。");
                        }
                    }
                    messageSec.setStatus("1");
                    messageSec.setMsgType("1"); //提醒消息
                    cjxtWarningMessageService.save(messageSec);

                    //WebSocket消息推送
                    if(standardAddressSbry!=null && standardAddressSbry.getId()!=null){
                        JSONObject json = new JSONObject();
                        json.put("msgType", "waMsg");
                        String msg = json.toString();
                        webSocket.sendOneMessage(standardAddressSbry.getId(), msg);
                    }

                    //WebSocket消息推送处理人
                    if(sysUser!=null && sysUser.getId()!=null){
                        JSONObject json = new JSONObject();
                        json.put("msgType", "waMsg");
                        String msg = json.toString();
                        webSocket.sendOneMessage(sysUser.getId(), msg);
                    }
                }
            }
        }
        return Result.OK("操作成功!");
    }

    /**
     * 模板定义动态新增数据
     *
     * @param pzList
     * @return
     */
    @IgnoreAuth
    @PostMapping("/addOrUptPz")
    @AutoLog(value = "模板管理配置-数据新增")
    @ApiOperation(value = "模板管理配置-数据新增", notes = "模板管理配置-数据新增")
    public Result<String> addPz(
            @RequestParam(required = true, name = "userId") String userId, // 当前登录用户ID
            @RequestParam(required = false, name = "taskId") String taskId, // taskId
            @RequestBody List<CjxtMbglPz> pzList) {
//		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        try {
            SysUser sysUser = new SysUser();
            if (userId != null) {
                sysUser = sysUserService.getById(userId);
            }
            String mbCode = "";
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            String idValue = null;
            //		String taskID = null;
            boolean isAdd = false;
            //定义UUID
            String uuid = UUID.randomUUID().toString().replace("-", "");
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            CjxtMbglPz mbglPz = pzList.get(0);
            CjxtMbgl mbgl = null;
            String dbOnlyName = "";//唯一字段名
            String dbOnlyNameValue = "";//唯一字段值
            String onlyTableName = "";//唯一表名称
            String addressIdValue = "";
            if (mbglPz != null) {
                CjxtMbgl cjxtMbgl = cjxtMbglService.getById(mbglPz.getMbglId());
                if (cjxtMbgl != null) {
                    mbgl = cjxtMbgl;
                    onlyTableName = cjxtMbgl.getBm();
                    dbOnlyName = cjxtMbgl.getDbOnly();
                }
            }

            String dataVId= "";//获取数据ID
            boolean addOrUpt = true;  // 唯一字段值是否存在数据
            boolean isAddressDis = true;  // 地址是否存在数据
            //数据已存在ID
            StringBuilder idStringBuilder = new StringBuilder();
            //人员模版身份证信息
            StringBuilder rysfzhBuilder = new StringBuilder();
            StringBuilder sfzhBuilder = new StringBuilder();
            String addressIdNotNull = "";//数据已存在地址ID数据
            for (CjxtMbglPz cjxtMbglPz : pzList) {
                //唯一字段不等于空进入
                if (!"".equals(dbOnlyName) && dbOnlyName != null && !dbOnlyName.isEmpty()) {
                    if (dbOnlyName.equals(cjxtMbglPz.getDbFieldName())) {
                        dbOnlyNameValue = cjxtMbglPz.getDataValue();
                        String sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "'";
                        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                        if (resultList.size() > 0) {
                            Map<String, Object> row = resultList.get(0);
                            dataVId = (String) row.get("id");
                            addOrUpt = false;
                            //                        return Result.error(cjxtMbglPz.getDbFieldTxt()+"数据已存在,请重新输入!!!");
                        }
                    }
                }
                //任务采集地址是否已存在数据
                if ("address_id".equals(cjxtMbglPz.getDbFieldName())) {
                    addressIdValue = cjxtMbglPz.getDataValue();
                    CjxtStandardAddress standardAddress = cjxtStandardAddressService.getById(addressIdValue);
                    map.put("address", standardAddress.getAddressName());
                    String sqlOnly = "";
                    if ("2".equals(mbgl.getMblx()) && mbgl != null) {
                        if ("RY001".equals(mbgl.getMbbh())) {
                            for (CjxtMbglPz cjxtMbglPzSfzh : pzList) {
                                if ("rysfzh".equals(cjxtMbglPzSfzh.getDbFieldName())) {
                                    sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "' AND rysfzh = '" + cjxtMbglPzSfzh.getDataValue() + "'";
                                }
                            }
                        }
                        if ("RY002".equals(mbgl.getMbbh())) {
                            for (CjxtMbglPz cjxtMbglPzSfzh : pzList) {
                                if ("sfzh".equals(cjxtMbglPzSfzh.getDbFieldName())) {
                                    sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "' AND sfzh = '" + cjxtMbglPzSfzh.getDataValue() + "'";
                                }
                            }
                        }
                    } else {
                        sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "'";
                    }
                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                    if (resultList.size() > 0) {
                        for (Map<String, Object> result : resultList) {
                            dataVId = (String) result.get("id");
                            if ("2".equals(mbgl.getMblx()) && mbgl != null) {
                                if ("RY001".equals(mbgl.getMbbh())) {
                                    rysfzhBuilder.append(result.get("rysfzh")).append(",");
                                }
                                if ("RY002".equals(mbgl.getMbbh())) {
                                    sfzhBuilder.append(result.get("sfzh")).append(",");
                                }
                            }
                            idStringBuilder.append(result.get("id")).append(",");
                        }
                        addressIdNotNull += cjxtMbglPz.getDataValue();
                        isAddressDis = false;
                    }
                }

                if ("1".equals(cjxtMbglPz.getIsTitle())) {
                    continue;
                }
                if ("id".equals(cjxtMbglPz.getDbFieldName())) {
                    mbCode = cjxtMbglPz.getMbglMbbh();
                    idValue = cjxtMbglPz.getDataValue();
                    if (idValue == null || "".equals(idValue)) {
                        dataVId = uuid;
                        isAdd = true;
                        map.put(cjxtMbglPz.getDbFieldName(), uuid);
                    } else {
                        dataVId = idValue;
                        map.put(cjxtMbglPz.getDbFieldName(), idValue);
                    }
                } else {
                    //				if("data_id".equals(cjxtMbglPz.getDbFieldName())){
                    //					taskID = cjxtMbglPz.getDataValue();
                    //				}
                    if (!"err_msg".equals(cjxtMbglPz.getDbFieldName()) && ("null".equals(cjxtMbglPz.getDataValue()) || cjxtMbglPz.getDataValue() == null || "".equals(cjxtMbglPz.getDataValue()))) {
                        map.put(cjxtMbglPz.getDbFieldName(), null);
                    } else {
                        if("err_msg".equals(cjxtMbglPz.getDbFieldName())){
                            if (isAdd == true) {
                                map.put("err_msg", "数据新增");
                            }else {
                                map.put("err_msg", "数据修改");
                            }
                        }else{
                            map.put(cjxtMbglPz.getDbFieldName(), cjxtMbglPz.getDataValue());
                        }
                    }
                    if("del_flag".equals(cjxtMbglPz.getDbFieldName())){
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), "0");
                        }
                    }
                    if ("create_by".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), sysUser.getUsername());
                        }
                    }
                    if ("create_time".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), formattedDateTime);
                        }
                    }
                    if ("update_by".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == false || isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), sysUser.getUsername());
                        }
                    }
                    if ("update_time".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == false || isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), formattedDateTime);
                        }
                    }
                    if ("wszt".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true || "0".equals(cjxtMbglPz.getDataValue())) {
                            map.put(cjxtMbglPz.getDbFieldName(), null);
                        }
                    }
                    if ("blzt".equals(cjxtMbglPz.getDbFieldName())) {
                        if (isAdd == true) {
                            map.put(cjxtMbglPz.getDbFieldName(), "0");
                        }
                    }
                    if("image".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())){
                        String heardUrl = minioUrl+"/"+bucketName+"/";
                        if(!"".equals(cjxtMbglPz.getDataValue()) && cjxtMbglPz.getDataValue()!=null){
                            if(cjxtMbglPz.getDataValue().contains(heardUrl)){
                                map.put(cjxtMbglPz.getDbFieldName(), cjxtMbglPz.getDataValue().replace(heardUrl,""));
                            }
                        }
                    }
                }

                //判断当前模版为从业人员模版
                if ("RY002".equals(mbCode)) {
                    String bmValue = "";
                    if ("bm".equals(cjxtMbglPz.getDbFieldName())) {
                        bmValue = cjxtMbglPz.getDataValue();
                    }
                    if ("sfzh".equals(cjxtMbglPz.getDbFieldName()) && !"".equals(bmValue)) {
                        String updateSql = "UPDATE cjxt_rkcj SET ryfwcsgzdw = '" + bmValue + "' WHERE del_flag = '0' AND rysfzh = '" + cjxtMbglPz.getDataValue() + "' ;";
                        jdbcTemplate.update(updateSql);
                    }
                }

                //处理脱敏字段 身份证脱敏字段
                if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                    //身份证
                    if("1".equals(cjxtMbglPz.getDbJylx())){
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            String sfzh = (String) map.get(cjxtMbglPz.getDbFieldName());
                            if(!"".equals(sfzh) && sfzh!=null){
                                if(sfzh.contains("_sxby")){
                                    String dataV = sjjm(sfzh);
                                    String sfzhTm = desensitize(dataV);
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                }else {
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", sfzh);
                                }
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }else {
                            if(!"".equals(cjxtMbglPz.getDataValue()) && cjxtMbglPz.getDataValue()!=null){
                                String sfzh = desensitize(cjxtMbglPz.getDataValue());
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", sfzh);
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }
                    }
                    //手机号
                    if("2".equals(cjxtMbglPz.getDbJylx())){
                        if("1".equals(cjxtMbglPz.getSfjm())){
                            String phone = (String) map.get(cjxtMbglPz.getDbFieldName());
                            if(!"".equals(phone) && phone!=null){
                                if(phone.contains("_sxby")){
                                    String dataV = sjjm(phone);
                                    String phoneTm = maskPhone(dataV);
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                }else {
                                    map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", phone);
                                }
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }else {
                            if(!"".equals(cjxtMbglPz.getDataValue()) && cjxtMbglPz.getDataValue()!=null){
                                String phone = maskPhone(cjxtMbglPz.getDataValue());
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", phone);
                            }else {
                                map.put(cjxtMbglPz.getDbFieldName()+"_jmzd", "");
                            }
                        }
                    }
                }
            }

            //处理模板数据
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode));
            StringBuilder sql = new StringBuilder();
            if ((idValue == null || "".equals(idValue)) && addOrUpt == true && isAddressDis == true) {
                StringBuilder sqlAdd = new StringBuilder();
                sqlAdd.append("INSERT INTO ");
                sqlAdd.append(cjxtMbgl.getBm());
                sqlAdd.append(" (");
                for (String key : map.keySet()) {
                    sqlAdd.append(key).append(",");
                }
                sqlAdd.setLength(sqlAdd.length() - 1);
                sqlAdd.append(") VALUES (");
                for (int i = 0; i < map.size(); i++) {
                    sqlAdd.append("?,");
                }
                sqlAdd.setLength(sqlAdd.length() - 1);
                sqlAdd.append(")");
                jdbcTemplate.update(sqlAdd.toString(), map.values().toArray());
                //人口采集判断当前用户信息是否已存在 存在添加预警数据
                if ("RY001".equals(cjxtMbgl.getMbbh())) {
                    String addressId = (String) map.get("address_id");
                    String address = (String) map.get("address");
                    String rysfzh = (String) map.get("rysfzh");
                    String ryxmN = (String) map.get("ryxm");
                    String sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE del_flag = '0' AND rysfzh = '" + rysfzh + "' ORDER BY create_time DESC ";
                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                    if (resultList.size() > 1) {
                        Map<String, Object> secondResult = resultList.get(1);
                        String id = (String) secondResult.get("id");
                        String createBy = (String) secondResult.get("create_by");
                        String dataId = (String) secondResult.get("data_id");
                        String ryxm = (String) secondResult.get("ryxm");
                        CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getById(dataId);
                        if (dispatch != null) {
                            CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressId);
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

                            CjxtStandardAddress disAddress = cjxtStandardAddressService.getById(dispatch.getAddressId());
                            String disAddressName = "";
                            if ("1".equals(disAddress.getDzType())) {
                                //小区名
                                if (disAddress.getDz1Xqm() != null && !"".equals(disAddress.getDz1Xqm())) {
                                    disAddressName = disAddressName + disAddress.getDz1Xqm();
                                }
                                //楼栋
                                if (disAddress.getDz1Ld() != null && !"".equals(disAddress.getDz1Ld())) {
                                    disAddressName = disAddressName + disAddress.getDz1Ld() + "号楼";
                                }
                                //单元
                                if (disAddress.getDz1Dy() != null && !"".equals(disAddress.getDz1Dy())) {
                                    disAddressName = disAddressName + disAddress.getDz1Dy() + "单元";
                                }
                                //室
                                if (disAddress.getDz1S() != null && !"".equals(disAddress.getDz1S())) {
                                    disAddressName = disAddressName + disAddress.getDz1S() + "室";
                                }
                            } else if ("2".equals(disAddress.getDzType())) {
                                disAddress.setDetailMc(disAddress.getDz2Cm());
                                //村名
                                if (disAddress.getDz2Cm() != null && !"".equals(disAddress.getDz2Cm())) {
                                    disAddressName = disAddressName + disAddress.getDz2Cm();
                                }
                                //组名
                                if (disAddress.getDz2Zm() != null && !"".equals(disAddress.getDz2Zm())) {
                                    disAddressName = disAddressName + disAddress.getDz2Zm() + "组";
                                }
                                //号名
                                if (disAddress.getDz2Hm() != null && !"".equals(disAddress.getDz2Hm())) {
                                    disAddressName = disAddressName + disAddress.getDz2Hm() + "号";
                                }

                            } else if ("3".equals(disAddress.getDzType())) {
                                disAddress.setDetailMc(disAddress.getDz3Dsm());
                                //大厦名
                                if (disAddress.getDz3Dsm() != null && !"".equals(disAddress.getDz3Dsm())) {
                                    disAddressName = disAddressName + disAddress.getDz3Dsm();
                                }
                                //楼栋名
                                if (disAddress.getDz3Ldm() != null && !"".equals(disAddress.getDz3Ldm())) {
                                    disAddressName = disAddressName + disAddress.getDz3Ldm() + "栋";
                                }
                                //室名
                                if (disAddress.getDz3Sm() != null && !"".equals(disAddress.getDz3Sm())) {
                                    disAddressName = disAddressName + disAddress.getDz3Sm() + "室";
                                }
                            } else if ("4".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                            } else if ("5".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                                if (disAddress.getDz5P() != null && !"".equals(disAddress.getDz5P())) {
                                    disAddressName = disAddressName + disAddress.getDz5P() + "排";
                                }
                                if (disAddress.getDz5H() != null && !"".equals(disAddress.getDz5H())) {
                                    disAddressName = disAddressName + disAddress.getDz5H() + "号";
                                }
                                if (disAddress.getDz5S() != null && !"".equals(disAddress.getDz5S())) {
                                    disAddressName = disAddressName + disAddress.getDz5S() + "室";
                                }

                            } else if ("6".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                                if (disAddress.getDz6S() != null && !"".equals(disAddress.getDz6S())) {
                                    disAddressName = disAddressName + disAddress.getDz6S() + "室";
                                }
                            } else if ("99".equals(disAddress.getDzType())) {
                                if (disAddress.getDetailMc() != null && !"".equals(disAddress.getDetailMc())) {
                                    disAddressName = disAddressName + disAddress.getDetailMc();
                                }
                            }

                            //迁出网格员接受信息
                            CjxtWarningMessage messageSec = new CjxtWarningMessage();
                            SysUser sysUserSec = sysUserService.getUserByName(createBy);
                            messageSec.setUserId(sysUserSec.getId());
                            messageSec.setUsername(createBy);
                            messageSec.setRealname(sysUserSec.getRealname());
                            messageSec.setMessage(ryxm + "已从(" + disAddressName + ")地址迁出");
                            messageSec.setStatus("1");
                            messageSec.setDataId(id);
                            messageSec.setBm(cjxtMbgl.getBm());
                            messageSec.setMsgType("0"); //预警消息
                            cjxtWarningMessageService.save(messageSec);

                            //WebSocket消息推送
                            if(sysUserSec.getId()!=null){
                                JSONObject json = new JSONObject();
                                json.put("msgType", "waMsg");
                                String msg = json.toString();
                                webSocket.sendOneMessage(sysUserSec.getId(), msg);
                            }

                            //迁入网格员接受信息
                            CjxtWarningMessage messageNex = new CjxtWarningMessage();
                            messageNex.setUserId(sysUser.getId());
                            messageNex.setUsername(sysUser.getUsername());
                            messageNex.setRealname(sysUser.getRealname());
                            messageNex.setMessage(ryxmN + "已从原地址(" + disAddressName + ")迁入新地址(" + addressName + ")");
                            messageNex.setStatus("1");
                            messageNex.setDataId(id);
                            messageNex.setBm(cjxtMbgl.getBm());
                            messageNex.setMsgType("0"); //预警消息
                            cjxtWarningMessageService.save(messageNex);

                            //WebSocket消息推送
                            if(sysUser.getId()!=null){
                                JSONObject json = new JSONObject();
                                json.put("msgType", "waMsg");
                                String msg = json.toString();
                                webSocket.sendOneMessage(sysUser.getId(), msg);
                            }
                        }
                    }
                }
            }
            //定义补录表ID
            String blId = "";
            if (idValue!=null && !"".equals(idValue) && !idValue.isEmpty()) {
                if ("1".equals(cjxtMbgl.getSfls())) {
                    List<Map<String, Object>> lsData = jdbcTemplate.queryForList("SELECT * FROM " + cjxtMbgl.getBm() + "_ls" + " WHERE data_id_ls = '" + map.get("id") + "'");
                    if (lsData.size() == 0) {
                        // 查询cjxtMbgl.getBm()中的所有数据
                        List<Map<String, Object>> datalsInsert = jdbcTemplate.queryForList("SELECT * FROM " + cjxtMbgl.getBm() + " WHERE id = '" + map.get("id") + "'");

                        // 如果datalsInsert不为空，进行插入操作
                        if (!datalsInsert.isEmpty()) {
                            for (Map<String, Object> data : datalsInsert) {
                                // 构造插入语句
                                StringBuilder sqlInsert = new StringBuilder("INSERT INTO " + cjxtMbgl.getBm() + "_ls (id, data_id_ls");

                                // 动态拼接字段
                                StringBuilder values = new StringBuilder(" VALUES ('" + UUID.randomUUID().toString().replace("-", "") + "', '" + map.get("id") + "'");
                                for (Map.Entry<String, Object> entry : data.entrySet()) {
                                    String column = entry.getKey();
                                    Object value = entry.getValue();

                                    if (!"id".equals(column)) { // 避免插入原有的id
                                        sqlInsert.append(", ").append(column);
                                        if("".equals(value) || "null".equals(value) || value == null){
                                            values.append(", null ");
                                        }else {
                                            values.append(", '").append(value).append("'");
                                        }
                                    }
                                }
                                sqlInsert.append(")").append(values).append(")");

                                // 执行插入操作
                                jdbcTemplate.update(sqlInsert.toString());
                            }
                        }
                    }
                }

                Map<String, Object> uptMap = new HashMap<>();
                uptMap.putAll(map);
                uptMap.remove("mb_id");
                uptMap.remove("mb_name");
                uptMap.remove("table_name");
                uptMap.remove("address_id");
                uptMap.remove("address");
                uptMap.remove("longitude");
                uptMap.remove("latitude");
                if (uptMap.containsKey("blzt")) {
                    Object blztValue = uptMap.get("blzt");
                    if (!"1".equals(blztValue)) {
                        uptMap.remove("blzt");
                    }
                }
//                uptMap.remove("wszt");
                uptMap.remove("data_id");
                sql.append("UPDATE ");
                sql.append(cjxtMbgl.getBm());
                sql.append(" SET ");
                Map<String, Object> newMap = new HashMap<>();
                if (taskId != null && !"".equals(taskId)) {
                    if (uptMap.containsKey("wszt")) {
                        newMap.put("wszt", null);
                    } else {
                        newMap.put("wszt", null);
                    }
                }
                for (String key : uptMap.keySet()) {
                    if ("update_by".equals(key)) {
                        newMap.put("update_by", sysUser.getUsername());
                        uptMap.put("update_by", sysUser.getUsername());
                    } else {
                        newMap.put("update_by", sysUser.getUsername());
                    }
                    if ("update_time".equals(key)) {
                        newMap.put("update_time", formattedDateTime);
                        uptMap.put("update_time", formattedDateTime);
                    } else {
                        newMap.put("update_time", formattedDateTime);
                    }
                    if ("blzt".equals(key)) {
                        Object blztValue = uptMap.get("blzt");
//                        if ("1".equals(blztValue)) {
//                            blId = (String) uptMap.get("id");
//                        }
                        if(!"".equals(blztValue)){
                            blId = (String) uptMap.get("id");
                        }
                    }
                    if (!"id".equals(key)) {
                        if (uptMap.get(key) == null || "null".equals(uptMap.get(key)) || "".equals(uptMap.get(key))) {
                            if ("rybrlxdh".equals(key)) {
                                sql.append(key).append(" = '' ,");
                            }else {
                                sql.append(key).append(" = " + null + ",");
                            }
                        } else {
                            sql.append(key).append(" = '" + uptMap.get(key) + "',");
                        }
                    } else {
                        newMap.put("id", uuid);
                    }
                }
                map.putAll(newMap);
                sql.setLength(sql.length() - 1);

                if (addOrUpt == true || addOrUpt == false) {
                    if (addOrUpt == false && isAddressDis == true) {
                        sql.append(" WHERE del_flag = '0' AND " + dbOnlyName + " = '" + dbOnlyNameValue + "' ;");
                        jdbcTemplate.update(sql.toString());
                    } else if ((addOrUpt == false || addOrUpt == true) && isAddressDis == false) {
                        String[] addressId = addressIdNotNull.split(",");
                        for (int i = 0; i < addressId.length; i++) {
                            StringBuilder sqlBase = new StringBuilder(sql.toString());
                            sqlBase.append(" WHERE del_flag = '0' AND address_id = '" + addressId[i] + "' ");
                            if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                if (rysfzhBuilder.length() > 0) {
                                    rysfzhBuilder.setLength(rysfzhBuilder.length() - 1);
                                }
                                String[] rysfzhString = rysfzhBuilder.toString().split(",");
                                for (int j = 0; j < rysfzhString.length; j++) {
                                    String rysfzh = rysfzhString[j];
                                    sqlBase.append(" AND rysfzh = '" + rysfzh + "' ;");
                                    jdbcTemplate.update(sqlBase.toString());
                                }
                            } else if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                if (sfzhBuilder.length() > 0) {
                                    sfzhBuilder.setLength(sfzhBuilder.length() - 1);
                                }
                                String[] sfzhString = sfzhBuilder.toString().split(",");
                                for (int l = 0; l < sfzhString.length; l++) {
                                    String sfzh = sfzhString[l];
                                    sqlBase.append(" AND sfzh = '" + sfzh + "' ;");
                                    jdbcTemplate.update(sqlBase.toString());
                                }
                            } else {
                                sqlBase.append(" ;");
                                jdbcTemplate.update(sqlBase.toString());
                            }
                        }
                    } else {
                        sql.append(" WHERE del_flag = '0' AND id = '" + dataVId + "'");
                        jdbcTemplate.update(sql.toString());
                    }
                }
            }

            //数据大屏webSocke
            webSocket.sendMergedMessage();

            map.put("data_id_ls", dataVId);

            if ("1".equals(cjxtMbgl.getSfls())) {
                StringBuilder sqlLs = new StringBuilder();
                sqlLs.append("INSERT INTO ");
                sqlLs.append(cjxtMbgl.getBm() + "_ls");
                sqlLs.append(" (");
                for (String key : map.keySet()) {
                    sqlLs.append(key).append(",");
                }
                sqlLs.setLength(sqlLs.length() - 1);
                sqlLs.append(") VALUES (");
                for (int i = 0; i < map.size(); i++) {
                    sqlLs.append("?,");
                }
                sqlLs.setLength(sqlLs.length() - 1);
                sqlLs.append(")");
                jdbcTemplate.update(sqlLs.toString(), map.values().toArray());
            }
            //修改数据补录表中补录状态
            if (!"".equals(blId)) {
                CjxtDataReentry cjxtDataReentry = cjxtDataReentryService.getOne(new LambdaQueryWrapper<CjxtDataReentry>().eq(CjxtDataReentry::getBlzt,"1").eq(CjxtDataReentry::getBm, cjxtMbgl.getBm()).eq(CjxtDataReentry::getDataId, blId).last("LIMIT 1"));
                if(cjxtDataReentry!=null){
                    cjxtDataReentry.setBlzt("2");
                    cjxtDataReentryService.updateById(cjxtDataReentry);
                }
            }

            CjxtScoreRule cjxtScoreRule = cjxtScoreRuleService.getOne(new LambdaQueryWrapper<CjxtScoreRule>().eq(CjxtScoreRule::getMbCode, mbCode));
            if (cjxtScoreRule != null) {
                CjxtScoreDetail cjxtScoreDetail = new CjxtScoreDetail();
                cjxtScoreDetail.setUserId(sysUser.getId());
                cjxtScoreDetail.setUserName(sysUser.getRealname());
                cjxtScoreDetail.setRuleId(cjxtScoreRule.getId());
                cjxtScoreDetail.setRuleName(cjxtScoreRule.getRuleName());
                cjxtScoreDetail.setScore(cjxtScoreRule.getScoreValue());
                cjxtScoreDetail.setDataId(dataVId);
                cjxtScoreDetail.setMbId(cjxtScoreRule.getMbId());
                cjxtScoreDetail.setMbName(cjxtScoreRule.getMbName());
                cjxtScoreDetailService.save(cjxtScoreDetail);
            }
            log.info("输出任务ID====================================="+taskId);
            if (taskId != null && !"".equals(taskId)) {
//				String sqlOnly = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE data_id = '" + taskId + "'";
//				List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                CjxtTaskDispatch dispatch = cjxtTaskDispatchService.getById(taskId);
                if (dispatch != null && "2".equals(dispatch.getRwzt())) {
                    CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getId, taskId));
                    cjxtTaskDispatch.setId(taskId);
                    cjxtTaskDispatch.setHszt("2");
                    cjxtTaskDispatch.setSchssj(new Date());
                    cjxtTaskDispatch.setWcsj(new Date());
                    cjxtTaskDispatch.setDataId(dataVId);
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
            if (idValue == null || "".equals(idValue)) {
                return Result.ok("提交采集成功");
            } else {
                return Result.ok("提交采集成功");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.ok("");
    }

    /**
     * 自主申报
     *
     * @param pzList
     * @return
     */
    @IgnoreAuth
    @PostMapping("/sbPz")
    @ApiOperation(value = "模板管理配置-自主申报", notes = "模板管理配置-自主申报")
    public Result<String> sbPz(@RequestParam(required = true, name = "userId") String userId, // 当前登录用户ID
                               @RequestParam(required = true, name = "mbCode") String mbCode,
                               @RequestBody List<CjxtMbglPz> pzList) {
        if (!"".equals(mbCode)) {
            SysUser sysUser = null;
            CjxtStandardAddressSbry cjxtStandardAddressSbry = null;
            if (userId != null) {
                sysUser = sysUserService.getById(userId);
            }
            if (sysUser == null) {
                cjxtStandardAddressSbry = cjxtStandardAddressSbryService.getById(userId);
            }
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            String idValue = null;
            boolean isAdd = false;
            //定义UUID
            String uuid = UUID.randomUUID().toString().replace("-", "");
            // 获取当前日期时间
            LocalDateTime now = LocalDateTime.now();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将日期时间格式化为字符串
            String formattedDateTime = now.format(formatter);

            boolean addOrUpt = true;
            boolean isAddressDis = true;
            //数据已存在ID
            StringBuilder idStringBuilder = new StringBuilder();
            //人员模版身份证信息
            StringBuilder rysfzhBuilder = new StringBuilder();
            StringBuilder sfzhBuilder = new StringBuilder();
            CjxtStandardAddress standardAddress = null;

            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode));
            String dbOnlyName = "";//唯一字段名
            String dbOnlyNameValue = "";//唯一字段值
            String onlyTableName = "";//唯一表名称
            String addressIdValue = "";
            String addressIdNotNull = "";//数据已存在地址ID数据
            if (cjxtMbgl != null) {
                onlyTableName = cjxtMbgl.getBm() + "_sb";
                dbOnlyName = cjxtMbgl.getDbOnly();
            }
            if (cjxtMbgl != null && "1".equals(cjxtMbgl.getSfsb())) {
                for (CjxtMbglPz cjxtMbglPz : pzList) {
                    //唯一字段不等于空进入
                    if (!"".equals(dbOnlyName) && dbOnlyName != null && !dbOnlyName.isEmpty()) {
                        if (dbOnlyName.equals(cjxtMbglPz.getDbFieldName())) {
                            dbOnlyNameValue = cjxtMbglPz.getDataValue();
                            String sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "'";
                            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                            if (resultList.size() > 0) {
                                addOrUpt = false;
                            }
                        }
                    }
                    //任务采集地址是否已存在数据
                    if ("address_id".equals(cjxtMbglPz.getDbFieldName())) {
                        addressIdValue = cjxtMbglPz.getDataValue();
                        standardAddress = cjxtStandardAddressService.getById(addressIdValue);
                        if(standardAddress!=null){
                            map.put("address", standardAddress.getAddressName());
                        }
                        String sqlOnly = "";
                        if ("2".equals(cjxtMbgl.getMblx()) && cjxtMbgl != null) {
                            if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                for (CjxtMbglPz cjxtMbglPzSfzh : pzList) {
                                    if ("rysfzh".equals(cjxtMbglPzSfzh.getDbFieldName())) {
                                        sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "' AND rysfzh = '" + cjxtMbglPzSfzh.getDataValue() + "'";
                                    }
                                }
                            } else if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                for (CjxtMbglPz cjxtMbglPzSfzh : pzList) {
                                    if ("sfzh".equals(cjxtMbglPzSfzh.getDbFieldName())) {
                                        sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "' AND sfzh = '" + cjxtMbglPzSfzh.getDataValue() + "'";
                                    }
                                }
                            } else {
                                sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "'";
                            }
                        } else {
                            sqlOnly = "SELECT * FROM " + onlyTableName + " WHERE del_flag = '0' AND " + cjxtMbglPz.getDbFieldName() + " = '" + cjxtMbglPz.getDataValue() + "'";
                        }
                        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sqlOnly);
                        if (resultList.size() > 0) {
                            for (Map<String, Object> result : resultList) {
                                if ("2".equals(cjxtMbgl.getMblx()) && cjxtMbgl != null) {
                                    if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                        rysfzhBuilder.append(result.get("rysfzh")).append(",");
                                    }
                                    if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                        sfzhBuilder.append(result.get("sfzh")).append(",");
                                    }
                                }
                                idStringBuilder.append(result.get("id")).append(",");
                            }
                            addressIdNotNull += cjxtMbglPz.getDataValue();
                            isAddressDis = false;
                        }
                    }

                    if ("1".equals(cjxtMbglPz.getIsTitle())) {
                        continue;
                    }
                    if ("id".equals(cjxtMbglPz.getDbFieldName())) {
                        mbCode = cjxtMbglPz.getMbglMbbh();
                        idValue = cjxtMbglPz.getDataValue();
                        if (idValue == null || "".equals(idValue)) {
                            isAdd = true;
                            map.put(cjxtMbglPz.getDbFieldName(), uuid);
                        } else {
                            map.put(cjxtMbglPz.getDbFieldName(), idValue);
                        }
                    } else {
                        if (!"err_msg".equals(cjxtMbglPz.getDbFieldName()) && ("null".equals(cjxtMbglPz.getDataValue()) || cjxtMbglPz.getDataValue() == null || "".equals(cjxtMbglPz.getDataValue()))) {
                            map.put(cjxtMbglPz.getDbFieldName(), null);
                        } else {
                            if("err_msg".equals(cjxtMbglPz.getDbFieldName())){
                                if (isAdd == true) {
                                    map.put("err_msg", "数据新增");
                                }else {
                                    map.put("err_msg", "数据修改");
                                }
                            }else{
                                map.put(cjxtMbglPz.getDbFieldName(), cjxtMbglPz.getDataValue());
                            }
                        }
                        if ("create_time".equals(cjxtMbglPz.getDbFieldName())) {
                            if (isAdd == true) {
                                map.put(cjxtMbglPz.getDbFieldName(), formattedDateTime);
                            }
                        }
                        if ("update_by".equals(cjxtMbglPz.getDbFieldName())) {
                            if (isAdd == false) {
                                if (sysUser != null) {
                                    map.put(cjxtMbglPz.getDbFieldName(), sysUser.getUsername());
                                } else if (cjxtStandardAddressSbry != null) {
                                    map.put(cjxtMbglPz.getDbFieldName(), cjxtStandardAddressSbry.getUserName());
                                }
                            }
                        }
                        if ("update_time".equals(cjxtMbglPz.getDbFieldName())) {
                            if (isAdd == false) {
                                map.put(cjxtMbglPz.getDbFieldName(), formattedDateTime);
                            }
                        }
                    }
                    //判断当前模版为从业人员模版
                    if ("RY002".equals(mbCode)) {
                        String bmValue = "";
                        if ("bm".equals(cjxtMbglPz.getDbFieldName())) {
                            bmValue = cjxtMbglPz.getDataValue();
                        }
                        if ("sfzh".equals(cjxtMbglPz.getDbFieldName()) && !"".equals(bmValue)) {
                            String updateSql = "UPDATE cjxt_rkcj SET del_flag = '0' AND ryfwcsgzdw = '" + bmValue + "' WHERE rysfzh = '" + cjxtMbglPz.getDataValue() + "'  ;";
                            jdbcTemplate.update(updateSql);
                        }
                    }
                }

                StringBuilder sql = new StringBuilder();
                if ((idValue == null || "".equals(idValue)) && addOrUpt == true && isAddressDis == true) {
                    StringBuilder sqlAdd = new StringBuilder();
                    sqlAdd.append("INSERT INTO ");
                    sqlAdd.append(cjxtMbgl.getBm() + "_sb");
                    sqlAdd.append(" (");
                    for (String key : map.keySet()) {
                        sqlAdd.append(key).append(",");
                    }
                    sqlAdd.setLength(sqlAdd.length() - 1);
                    sqlAdd.append(") VALUES (");
                    for (int i = 0; i < map.size(); i++) {
                        sqlAdd.append("?,");
                    }
                    sqlAdd.setLength(sqlAdd.length() - 1);
                    sqlAdd.append(")");
                    jdbcTemplate.update(sqlAdd.toString(), map.values().toArray());
                }
                if (addOrUpt == false || isAddressDis == false) {
                    //自主上报编辑后重新审核
                    map.put("shzt", "0");

                    map.remove("mb_id");
                    map.remove("mb_name");
                    map.remove("table_name");
                    map.remove("address_id");
                    map.remove("address_id");
                    map.remove("address");
                    map.remove("longitude");
                    map.remove("latitude");
                    map.remove("blzt");
                    map.remove("wszt");
                    map.remove("data_id");
                    sql.append("UPDATE ");
                    sql.append(cjxtMbgl.getBm() + "_sb");
                    sql.append(" SET ");
                    Map<String, Object> newMap = new HashMap<>();
                    for (String key : map.keySet()) {
                        if (map.containsKey("update_by")) {
                            if (sysUser != null) {
                                newMap.put("update_by", sysUser.getUsername());
                            } else if (cjxtStandardAddressSbry != null) {
                                newMap.put("update_by", cjxtStandardAddressSbry.getUserName());
                            }
                        } else {
                            if (sysUser != null) {
                                newMap.put("update_by", sysUser.getUsername());
                            } else if (cjxtStandardAddressSbry != null) {
                                newMap.put("update_by", cjxtStandardAddressSbry.getUserName());
                            }
                            newMap.put("update_by", sysUser.getUsername());
                        }
                        if (map.containsKey("update_time")) {
                            newMap.put("update_time", formattedDateTime);
                        } else {
                            newMap.put("update_time", formattedDateTime);
                        }
                        if (!"id".equals(key)) {
                            if (map.get(key) == null || "null".equals(map.get(key)) || "".equals(map.get(key))) {
                                sql.append(key).append(" = " + null + ",");
                            } else {
                                sql.append(key).append(" = '" + map.get(key) + "',");
                            }
                        } else {
                            newMap.put("id", uuid);
                        }
                    }
                    map = newMap;
                    sql.setLength(sql.length() - 1);

                    if ((idValue != null || !"".equals(idValue))) {
                        if (addOrUpt == false && isAddressDis == true) {
                            sql.append(" WHERE " + dbOnlyName + " = '" + dbOnlyNameValue + "' ;");
                            jdbcTemplate.update(sql.toString());
                        } else if ((addOrUpt == false || addOrUpt == true) && isAddressDis == false) {
                            String[] addressId = addressIdNotNull.split(",");
                            for (int i = 0; i < addressId.length; i++) {
                                StringBuilder sqlBase = new StringBuilder(sql.toString());
                                sqlBase.append(" WHERE del_flag = '0' AND address_id = '" + addressId[i] + "' ");
                                if ("RY001".equals(cjxtMbgl.getMbbh())) {
                                    if (rysfzhBuilder.length() > 0) {
                                        rysfzhBuilder.setLength(rysfzhBuilder.length() - 1);
                                    }
                                    String[] rysfzhString = rysfzhBuilder.toString().split(",");
                                    for (int j = 0; j < rysfzhString.length; j++) {
                                        String rysfzh = rysfzhString[j];
                                        sqlBase.append(" AND rysfzh = '" + rysfzh + "' ;");
                                        jdbcTemplate.update(sqlBase.toString());
                                    }
                                } else if ("RY002".equals(cjxtMbgl.getMbbh())) {
                                    if (sfzhBuilder.length() > 0) {
                                        sfzhBuilder.setLength(sfzhBuilder.length() - 1);
                                    }
                                    String[] sfzhString = sfzhBuilder.toString().split(",");
                                    for (int l = 0; l < sfzhString.length; l++) {
                                        String sfzh = sfzhString[l];
                                        sqlBase.append(" AND sfzh = '" + sfzh + "' ;");
                                        jdbcTemplate.update(sqlBase.toString());
                                    }
                                } else {
                                    sqlBase.append(" ;");
                                    jdbcTemplate.update(sqlBase.toString());
                                }
                            }
                        }else {
                            sql.append(" WHERE del_flag = '0' AND id = '" + idValue + "'");
                            jdbcTemplate.update(sql.toString());
                        }
                        String MsgXx = "数据未修改";
                        if("RY001".equals(cjxtMbgl.getMbbh())){
                            String updateSql = "UPDATE cjxt_fwcj_sb SET shzt = '0' , err_msg = '"+MsgXx+"' WHERE address_id = '"+addressIdValue+"' AND shzt = '1' ;" ;
                            jdbcTemplate.update(updateSql);
                        }
                        if("RY002".equals(cjxtMbgl.getMbbh())){
                            String updateSql = "UPDATE cjxt_dwcj_sb SET shzt = '0' , err_msg = '"+MsgXx+"' WHERE address_id = '"+addressIdValue+"' AND shzt = '1' ;" ;
                            jdbcTemplate.update(updateSql);
                        }
                    }
                }
                if(standardAddress!=null && standardAddress.getAddressCodeMz()!=null && !"".equals(standardAddress.getAddressCodeMz())){
                    List<SysUser> userList = sysUserService.list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserSf,"1").eq(SysUser::getOrgCode,standardAddress.getAddressCodeMz()));
                    if(userList.size()>0){
                        for(SysUser user: userList){
                            //迁出网格员接受信息
                            CjxtWarningMessage messageSec = new CjxtWarningMessage();
                            messageSec.setUserId(user.getId());
                            messageSec.setUsername(user.getUsername());
                            messageSec.setRealname(user.getRealname());
                            if(cjxtStandardAddressSbry!=null&&cjxtStandardAddressSbry.getUserName()!=null&&!"".equals(cjxtStandardAddressSbry.getUserName())){
                                messageSec.setMessage(cjxtStandardAddressSbry.getUserName() + ",已自主上报数据,请及时处理!!!");
                            }else {
                                messageSec.setMessage("您有新的自主上报审核数据,请即时处理!!!");
                            }
                            messageSec.setStatus("1");
//                            messageSec.setDataId(id);
                            messageSec.setBm(cjxtMbgl.getBm());
                            messageSec.setMsgType("0"); //预警消息
                            cjxtWarningMessageService.save(messageSec);

                            //WebSocket消息推送
                            if(user.getId()!=null){
                                JSONObject json = new JSONObject();
                                json.put("msgType", "waMsg");
                                String msg = json.toString();
                                webSocket.sendOneMessage(user.getId(), msg);
                            }
                        }
                    }
                }
                //数据大屏webSocke
                webSocket.sendMergedMessage();
                if (idValue == null || "".equals(idValue)) {
                    return Result.ok("上报成功");
                } else {
                    return Result.ok("修改成功");
                }
            } else {
                return Result.error("模版信息有误!!!");
            }
        }
        return Result.error("模版信息有误!!!");
    }

    @AutoLog("模板管理配置-自主上报审核角标")
    @ApiOperation(value="模板管理配置-自主上报审核角标", notes="模板管理配置-自主上报审核角标")
    @GetMapping(value = "/zzsbNum")
    public Result<Integer> zzsbNum(@RequestParam(name="userId",required=true) String userId,
                                                      @RequestParam(name="mbCode",required=false) String mbCode,
                                                      @RequestParam(name="cjSb",required=false) String cjSb,
                                                      HttpServletRequest req) {
        Integer num = 0;
        if(userId!=null){
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getIsDb,"1").eq(CjxtMbgl::getMbbh,mbCode));
            String bm = "";
            if(cjxtMbgl!=null){
                List<CjxtPjwgqx> pjwgqxList = new ArrayList<>();//片警民警网格权限
                SysUser sysUser = sysUserService.getById(userId);
                List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
                StringBuilder orgCodeBuilder = new StringBuilder();
                StringBuilder sysDepartCode = new StringBuilder();
                List<String> orgCodes = new ArrayList<>();
                if (sysDepartsList.size() > 0) {
                    for (int j = 0; j < sysDepartsList.size(); j++) {
                        SysDepart sysDepart = sysDepartsList.get(j);
                        if (j > 0) {
                            sysDepartCode.append(",");
                        }
                        sysDepartCode.append("'").append(sysDepart.getOrgCode()).append("'");
                    }
                }
                if (sysUser != null) {
                    if ("4".equals(sysUser.getUserSf()) || "5".equals(sysUser.getUserSf()) || "6".equals(sysUser.getUserSf()) || "7".equals(sysUser.getUserSf()) || "8".equals(sysUser.getUserSf()) || "9".equals(sysUser.getUserSf())) {
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
                    if ("2".equals(sysUser.getUserSf()) || "3".equals(sysUser.getUserSf())) {
                        //片警网格查询方式修改
//						String pjwgqxSql = "SELECT wg_code FROM cjxt_pjwgqx WHERE del_flag = '0' AND pj_id = '" + userId + "'" ;
                        pjwgqxList = cjxtPjwgqxService.list(new LambdaQueryWrapper<CjxtPjwgqx>().eq(CjxtPjwgqx::getPjId, userId));
                        for (int i = 0; i < pjwgqxList.size(); i++) {
                            CjxtPjwgqx cjxtPjwgqx = pjwgqxList.get(i);
                            if (i > 0) {
                                orgCodeBuilder.append(",");
                            }
                            orgCodeBuilder.append("'").append(cjxtPjwgqx.getWgCode()).append("'");
                        }
                    }
                }
                String orgCode = "''";
                if (orgCodes.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else if (pjwgqxList.size() > 0) {
                    orgCode = orgCodeBuilder.toString();
                } else {
                    if (sysDepartsList.size() > 0) {
                        orgCode = sysDepartCode.toString();
                    }
                }

                StringBuilder orgCodeQuery = new StringBuilder();
                orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");

                bm = cjxtMbgl.getBm();
                if("1".equals(cjSb)){
                    bm = cjxtMbgl.getBm();
                } else if("3".equals(cjSb)){
                    bm = cjxtMbgl.getBm()+"_sb";
                }

                String countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND shzt = '0' " + orgCodeQuery ;
                List<Integer> fw = jdbcTemplate.queryForList(countSql, Integer.class);
                num = fw.size()<=0 ? 0 : fw.get(0);
            }
        }
        return Result.OK(num);
    }

    /**
     * 添加
     *
     * @param cjxtMbglPage
     * @return
     */
    @AutoLog(value = "模板管理-添加")
    @ApiOperation(value = "模板管理-添加", notes = "模板管理-添加")
//    @RequiresPermissions("cjxt:cjxt_mbgl:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CjxtMbglPage cjxtMbglPage) {
//        CjxtMbgl cjxtBm = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, cjxtMbglPage.getBm()));
        if (cjxtMbglPage.getBm() != null && !"".equals(cjxtMbglPage.getBm())) {
            String checkTableExistsSql = "SHOW TABLES LIKE '" + cjxtMbglPage.getBm() + "'";
            List<String> tables = jdbcTemplate.queryForList(checkTableExistsSql, String.class);
            if (tables.size() > 0) {
                return Result.error("当前表名称已存在数据库!");
            }
        }
        cjxtMbglPage.setBdfg("2");
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        CjxtMbgl cjxtMbgl = new CjxtMbgl();
        BeanUtils.copyProperties(cjxtMbglPage, cjxtMbgl);
        cjxtMbglService.saveMain(cjxtMbgl, cjxtMbglPage.getCjxtMbglPzList());
        Date cretime = cjxtMbgl.getCreateTime();
        OnlCgformHead onlCgformHead = new OnlCgformHead();
        onlCgformHead.setId(cjxtMbgl.getId());
        onlCgformHead.setTableName(cjxtMbgl.getBm());
        onlCgformHead.setTableType(Integer.valueOf(cjxtMbgl.getTableType()));
        onlCgformHead.setTableVersion("1");
        onlCgformHead.setTableTxt(cjxtMbgl.getMbname());
        onlCgformHead.setIsCheckbox("Y");
        onlCgformHead.setIsDbSynch("N");
        onlCgformHead.setIsPage("Y");
        onlCgformHead.setIsTree("N");
        onlCgformHead.setIdSequence(null);
        onlCgformHead.setIdType("UUID");
        onlCgformHead.setQueryMode("single");
        onlCgformHead.setRelationType(null);//映射关系 0一对多  1一对一
        onlCgformHead.setSubTableStr(null);//子表
        onlCgformHead.setTabOrderNum(null);//附表排序序号
        onlCgformHead.setTreeParentIdField(null);
        onlCgformHead.setTreeIdField(null);
        onlCgformHead.setTreeFieldname(null);
        onlCgformHead.setFormCategory("temp");
        onlCgformHead.setFormTemplate("2");//表单列表风格 默认2列
        onlCgformHead.setFormTemplateMobile(null);
        onlCgformHead.setScroll("1");
        onlCgformHead.setCopyType("0");
        onlCgformHead.setPhysicId(null);
        onlCgformHead.setExtConfigJson("{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":\"\",\"commentStatus\":0,\"tableFixedAction\":1,\"tableFixedActionType\":\"right\"}");
        onlCgformHead.setUpdateBy(null);
        onlCgformHead.setUpdateTime(null);
        onlCgformHead.setCreateBy(sysUser.getUsername());
        onlCgformHead.setCreateTime(cretime);
        onlCgformHead.setThemeTemplate("normal");
        onlCgformHead.setIsDesForm("N");
        onlCgformHead.setDesFormCode("");
        onlCgformHead.setLowAppId(null);
        onlCgformHeadService.save(onlCgformHead);

        OnlCgformField onlCgformField = new OnlCgformField();
        List<CjxtMbglPz> cjxtMbglPz = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getIsTitle, "0").orderByAsc(CjxtMbglPz::getOrderNum));
        if (cjxtMbglPz != null && cjxtMbglPz.size() > 0) {
            int i = 0;
            for (CjxtMbglPz cj : cjxtMbglPz) {
                i++;
                onlCgformField.setId(cj.getId());
                onlCgformField.setCgformHeadId(onlCgformHead.getId());
                onlCgformField.setDbFieldName(cj.getDbFieldName());
                onlCgformField.setDbFieldTxt(cj.getDbFieldTxt());
                onlCgformField.setDbIsKey("0");
                onlCgformField.setDbIsNull("0");//是否允许为空 0否 1是
                onlCgformField.setDbIsPersist("1");//是否需要同步数据库字段 0否 1是
                onlCgformField.setDbType(cj.getDbType());
                onlCgformField.setDbLength(cj.getDbLength());
                onlCgformField.setDbPointLength("0");
                onlCgformField.setDbDefaultVal("");
                onlCgformField.setDictField(cj.getDictField());
                onlCgformField.setDictTable("");
                onlCgformField.setDictText("");
                onlCgformField.setFieldShowType(cj.getFieldShowType());
                onlCgformField.setFieldHref("");
                onlCgformField.setFieldLength("200");
                onlCgformField.setFieldValidType("");
                onlCgformField.setFieldMustInput("0");// 0否 1是
                onlCgformField.setFieldExtendJson("");
                onlCgformField.setFieldDefaultValue("");
                if ("".equals(cj.getIsQuery()) || cj.getIsQuery() == null) {
                    onlCgformField.setIsQuery("0");//是否查询 0否 1是
                } else {
                    onlCgformField.setIsQuery(cj.getIsQuery());//是否查询 0否 1是
                }
                onlCgformField.setIsShowForm(cj.getIsShowFrom()); // 表单是否显示 0否 1是
                onlCgformField.setIsShowList(cj.getIsShowList()); // 列表是否显示 0否 1是
                onlCgformField.setIsReadOnly("0"); // 是否制度 0否 1是
                onlCgformField.setQueryMode("single"); // 查询模式
                onlCgformField.setMainTable("");
                onlCgformField.setMainField("");
                onlCgformField.setOrderNum(String.valueOf(i)); // 排序号
                onlCgformField.setConverter("");
                onlCgformField.setQueryDefVal("");
                onlCgformField.setQueryDictText("");
                onlCgformField.setQueryDictField("");
                onlCgformField.setQueryDictTable("");
                onlCgformField.setQueryShowType("text");
                onlCgformField.setQueryConfigFlag("0");
                onlCgformField.setQueryValidType(null);
                onlCgformField.setQueryMustInput(null);
                onlCgformField.setSortFlag("0");
                onlCgformField.setCreateBy(sysUser.getUsername());
                onlCgformField.setCreateTime(cretime);
                onlCgformField.setUpdateBy(null);
                onlCgformField.setUpdateTime(null);
                onlCgformFieldsService.save(onlCgformField);
            }
        }
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param cjxtMbglPage
     * @return
     */
    @AutoLog(value = "模板管理-编辑")
    @ApiOperation(value = "模板管理-编辑", notes = "模板管理-编辑")
//    @RequiresPermissions("cjxt:cjxt_mbgl:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody CjxtMbglPage cjxtMbglPage) {
        if (!cjxtMbglPage.getBmDto().equals(cjxtMbglPage.getBm())) {
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, cjxtMbglPage.getBm()));
            if (cjxtMbgl != null) {
                return Result.error("当前表名已存在模板!");
            }
        }
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        CjxtMbgl cjxtMbgl = new CjxtMbgl();
        BeanUtils.copyProperties(cjxtMbglPage, cjxtMbgl);
        CjxtMbgl cjxtMbglEntity = cjxtMbglService.getById(cjxtMbgl.getId());
        if (cjxtMbglEntity == null) {
            return Result.error("未找到对应数据");
        }
        cjxtMbglService.updateMain(cjxtMbgl, cjxtMbglPage.getCjxtMbglPzList());
        Date updateTime = cjxtMbgl.getUpdateTime();
        OnlCgformHead onlCgformHead = new OnlCgformHead();
        onlCgformHead.setId(cjxtMbgl.getId());
        onlCgformHead.setTableName(cjxtMbgl.getBm());
        onlCgformHead.setTableType(Integer.valueOf(cjxtMbgl.getTableType()));
        onlCgformHead.setTableVersion("1");
        onlCgformHead.setTableTxt(cjxtMbgl.getMbname());
        onlCgformHead.setIsCheckbox("Y");
//		onlCgformHead.setIsDbSynch("N");
        onlCgformHead.setIsPage("Y");
        onlCgformHead.setIsTree("N");
        onlCgformHead.setIdSequence(null);
        onlCgformHead.setIdType("UUID");
        onlCgformHead.setQueryMode("single");
        onlCgformHead.setRelationType(null);//映射关系 0一对多  1一对一
        onlCgformHead.setSubTableStr(null);//子表
        onlCgformHead.setTabOrderNum(null);//附表排序序号
        onlCgformHead.setTreeParentIdField(null);
        onlCgformHead.setTreeIdField(null);
        onlCgformHead.setTreeFieldname(null);
        onlCgformHead.setFormCategory("temp");
        onlCgformHead.setFormTemplate("2");//表单列表风格 默认2列
        onlCgformHead.setFormTemplateMobile(null);
        onlCgformHead.setScroll("1");
        onlCgformHead.setCopyType("0");
        onlCgformHead.setPhysicId(null);
        onlCgformHead.setExtConfigJson("{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":\"\",\"commentStatus\":0,\"tableFixedAction\":1,\"tableFixedActionType\":\"right\"}");
        onlCgformHead.setUpdateBy(sysUser.getUsername());
        onlCgformHead.setUpdateTime(updateTime);
//		onlCgformHead.setCreateBy(sysUser.getUsername());
//		onlCgformHead.setCreateTime(cretime);
        onlCgformHead.setThemeTemplate("normal");
        onlCgformHead.setIsDesForm("N");
        onlCgformHead.setDesFormCode("");
        onlCgformHead.setLowAppId(null);
        onlCgformHeadService.updateById(onlCgformHead);

        // 查询配置字段是否存在 不存在删除
        List<OnlCgformField> cgformFieldList = onlCgformFieldsService.list(new QueryWrapper<OnlCgformField>().eq("cgform_head_id", cjxtMbgl.getId()));
        if (cgformFieldList.size() > 0) {
            for (OnlCgformField field : cgformFieldList) {
                CjxtMbglPz cjxtPz = cjxtMbglPzService.getById(field.getId());
                if (cjxtPz == null) {
                    onlCgformFieldsService.removeById(field.getId());
                }
            }
        }

        //修改onl表达动态字段配置
        OnlCgformField onlCgformField = new OnlCgformField();
        List<CjxtMbglPz> cjxtMbglPz = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getIsTitle, "0").orderByAsc(CjxtMbglPz::getOrderNum));
//		List<CjxtMbglPz> cjxtMbglPz = cjxtMbglPage.getCjxtMbglPzList();
        if (cjxtMbglPz != null && cjxtMbglPz.size() > 0) {
            int i = 0;
            for (CjxtMbglPz cj : cjxtMbglPz) {
                i++;
                onlCgformField.setId(cj.getId());
                onlCgformField.setCgformHeadId(onlCgformHead.getId());
                onlCgformField.setDbFieldName(cj.getDbFieldName());
                onlCgformField.setDbFieldNameOld(cj.getDbFieldNameOld());
                onlCgformField.setDbFieldTxt(cj.getDbFieldTxt());
                if ("id".equals(cj.getDbFieldName())) {
                    onlCgformField.setDbIsKey("1");
                } else {
                    onlCgformField.setDbIsKey("0");
                }
                onlCgformField.setDbIsNull("0");//是否允许为空 0否 1是
                onlCgformField.setDbIsPersist("1");//是否需要同步数据库字段 0否 1是
                onlCgformField.setDbType(cj.getDbType());
                onlCgformField.setDbLength(cj.getDbLength());
                onlCgformField.setDbPointLength("0");
                onlCgformField.setDbDefaultVal("");
                onlCgformField.setDictField(cj.getDictField());
                onlCgformField.setDictTable("");
                onlCgformField.setDictText("");
                onlCgformField.setFieldShowType(cj.getFieldShowType());
                onlCgformField.setFieldHref("");
                onlCgformField.setFieldLength("200");
                onlCgformField.setFieldValidType("");
                onlCgformField.setFieldMustInput("0");// 0否 1是
                onlCgformField.setFieldExtendJson("");
                onlCgformField.setFieldDefaultValue("");
                if ("".equals(cj.getIsQuery()) || cj.getIsQuery() == null) {
                    onlCgformField.setIsQuery("0");//是否查询 0否 1是
                } else {
                    onlCgformField.setIsQuery(cj.getIsQuery());//是否查询 0否 1是
                }
                onlCgformField.setIsShowForm(cj.getIsShowFrom()); // 表单是否显示 0否 1是
                onlCgformField.setIsShowList(cj.getIsShowList()); // 列表是否显示 0否 1是
                onlCgformField.setIsReadOnly("0"); // 是否制度 0否 1是
                onlCgformField.setQueryMode("single"); // 查询模式
                onlCgformField.setMainTable("");
                onlCgformField.setMainField("");
                onlCgformField.setOrderNum(String.valueOf(i)); // 排序号
                onlCgformField.setConverter("");
                onlCgformField.setQueryDefVal("");
                onlCgformField.setQueryDictText("");
                onlCgformField.setQueryDictField("");
                onlCgformField.setQueryDictTable("");
                onlCgformField.setQueryShowType("text");
                onlCgformField.setQueryConfigFlag("0");
                onlCgformField.setQueryValidType(null);
                onlCgformField.setQueryMustInput(null);
                onlCgformField.setSortFlag("0");
//				onlCgformField.setCreateBy(sysUser.getUsername());
//				onlCgformField.setCreateTime(cretime);
                onlCgformField.setUpdateBy(sysUser.getUsername());
                onlCgformField.setUpdateTime(updateTime);
                boolean onlField = false;
                OnlCgformField onlFidldOne = onlCgformFieldsService.getById(cj.getId());
                if (onlFidldOne == null) {
                    onlField = true;
                }
                if (onlField) {
                    onlCgformFieldsService.save(onlCgformField);
                } else {
                    onlCgformFieldsService.updateById(onlCgformField);
                }

            }
        }
        return Result.OK("编辑成功!");
    }

    public List<String> getColumnNames(String tableName, String dbFieldName) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, tableName, dbFieldName);
            while (resultSet.next()) {
                columnNames.add(resultSet.getString("COLUMN_NAME"));
            }
        } finally {
            connection.close();
        }
        return columnNames;
    }

    /**
     * 同步数据库
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模板管理-同步数据库")
    @ApiOperation(value = "模板管理-同步数据库", notes = "模板管理-同步数据库")
    @GetMapping(value = "/dbUpdate")
    public Result<String> dbUpdate(@RequestParam(name = "id", required = true) String id) throws SQLException {
        CjxtMbgl cjxtMbgl = cjxtMbglService.getById(id);
        if (cjxtMbgl != null) {
            List<String> list = new ArrayList<>();
            String tablename = cjxtMbgl.getBm();
            list.add(tablename);
            if ("1".equals(cjxtMbgl.getSfls())) {
                list.add(tablename + "_ls"); //历史表
            }
            if ("1".equals(cjxtMbgl.getSfsb())) {
                list.add(tablename + "_sb"); //上报表
            }
            for (String table : list) {
                //查询数据库中是否存在表
                String checkTableExistsSql = "SHOW TABLES LIKE '" + table + "'";
                List<String> tables = jdbcTemplate.queryForList(checkTableExistsSql, String.class);
                if (tables.size() > 0) {
                    // 循环表字段
                    List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, id).eq(CjxtMbglPz::getIsCommon, "0").eq(CjxtMbglPz::getIsTitle, "0").orderByAsc(CjxtMbglPz::getOrderNum));
                    if (cjxtMbglPzList.size() > 0 && cjxtMbgl != null) {
                        for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
                            //判断是否存在修改字段
                            if (cjxtMbglPz.getDbFieldNameOld() != null && !"".equals(cjxtMbglPz.getDbFieldNameOld())) {
                                String dbFieldNameNew = cjxtMbglPz.getDbFieldName();
                                String dbFieldNameOld = cjxtMbglPz.getDbFieldNameOld();
                                // 旧字段信息是否存在表中
                                List<String> fileNameList = getColumnNames(table, dbFieldNameOld);
                                if (fileNameList.size() > 0) {
                                    String dbType = mapDbType(cjxtMbglPz.getDbType());
                                    for (String fileName : fileNameList) {
                                        String renameColumnSql = "ALTER TABLE " + table + " CHANGE " + fileName + " " + dbFieldNameNew + " " + dbType + "(" + cjxtMbglPz.getDbLength() + ")";
                                        jdbcTemplate.execute(renameColumnSql);
                                    }
                                }
                            }
                            //判断是否存在字段长度变更
                            if (cjxtMbglPz.getDbLengthOld() != null && !"".equals(cjxtMbglPz.getDbLengthOld())) {
                                String dbLength = cjxtMbglPz.getDbLength();
                                // 旧字段信息是否存在表中
                                List<String> fileNameList = getColumnNames(table, cjxtMbglPz.getDbFieldName());
                                if (fileNameList.size() > 0) {
                                    String dbType = mapDbType(cjxtMbglPz.getDbType());
                                    for (String fileName : fileNameList) {
                                        String renameColumnSql = "ALTER TABLE " + table + " MODIFY " + fileName + " " + dbType + "(" + dbLength + ")";
                                        jdbcTemplate.execute(renameColumnSql);
                                    }
                                }
                            }
                            //判断是否存在字段类型变更
                            if (cjxtMbglPz.getDbTypeOld() != null && !"".equals(cjxtMbglPz.getDbTypeOld())) {
                                String dbType = mapDbType(cjxtMbglPz.getDbType());
                                // 旧字段信息是否存在表中
                                List<String> fileNameList = getColumnNames(table, cjxtMbglPz.getDbFieldName());
                                if (fileNameList.size() > 0) {
                                    for (String fileName : fileNameList) {
                                        String renameColumnSql = "ALTER TABLE " + table + " MODIFY " + fileName + " " + dbType;
                                        jdbcTemplate.execute(renameColumnSql);
                                    }
                                }
                            }
                            //判断是否有删除字段
                            if ("1".equals(cjxtMbglPz.getDelFlag())) {
                                String dbFirldNameDel = cjxtMbglPz.getDbFieldName();
                                // 旧字段信息是否存在表中
                                List<String> fileNameList = getColumnNames(table, dbFirldNameDel);
                                if (fileNameList.size() > 0) {
                                    for (String fileName : fileNameList) {
                                        String renameColumnSql = "ALTER TABLE " + table + " DROP COLUMN " + fileName;
                                        jdbcTemplate.execute(renameColumnSql);
                                    }
                                }
                            }
                            //判断未删除字段是否存在数据库
                            if ("0".equals(cjxtMbglPz.getDelFlag())) {
                                String dbFieldName = cjxtMbglPz.getDbFieldName();
                                if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                                    dbFieldName = dbFieldName+"_jmzd";
                                }
                                // 查询表中是否存在该字段
                                List<String> fileNameList = getColumnNames(table, dbFieldName);
                                if (fileNameList.size() <= 0) {
                                    // 如果字段不存在，添加字段
                                    String dbType = mapDbType(cjxtMbglPz.getDbType());
                                    StringBuilder sql = new StringBuilder("ALTER TABLE " + table + " ADD COLUMN ");
                                    sql.append(dbFieldName).append(" ").append(dbType);
                                    if (cjxtMbglPz.getDbLength() != null && !"0".equals(cjxtMbglPz.getDbLength()) && cjxtMbglPz.getDbLength() != "0") {
                                        if ("Date".equals(cjxtMbglPz.getDbType()) || "Datetime".equals(cjxtMbglPz.getDbType()) || "date".equals(cjxtMbglPz.getFieldShowType()) || "datetime".equals(cjxtMbglPz.getFieldShowType())) {
                                            sql.append("(").append("0").append(")");
                                        } else if ("image".equals(cjxtMbglPz.getFieldShowType()) || "textarea".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                                            sql.append("(").append("1000").append(")");
                                        } else {
                                            sql.append("(").append(cjxtMbglPz.getDbLength()).append(")");
                                        }
                                    }
                                    if (cjxtMbglPz.getDbDefaultVal() != null && !"".equals(cjxtMbglPz.getDbDefaultVal())) {
                                        sql.append(" DEFAULT '").append(cjxtMbglPz.getDbDefaultVal()).append("'");
                                    }
                                    if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                                        sql.append(" COMMENT '").append(cjxtMbglPz.getDbFieldTxt()+"加密字段").append("'");
                                    } else {
                                        sql.append(" COMMENT '").append(cjxtMbglPz.getDbFieldTxt()).append("'");
                                    }
                                    jdbcTemplate.execute(sql.toString());
                                }
                            }
                        }
                    }
                } else {
                    List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, id).eq(CjxtMbglPz::getDelFlag, "0").eq(CjxtMbglPz::getIsTitle, "0").orderByAsc(CjxtMbglPz::getOrderNum));
                    if (cjxtMbglPzList.size() > 0 && cjxtMbgl != null) {
                        StringBuilder sql = new StringBuilder("CREATE TABLE " + table + " (");
                        String primaryKey = "";
                        for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
                            String dbType = mapDbType(cjxtMbglPz.getDbType());
                            if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                                sql.append(cjxtMbglPz.getDbFieldName()+"_jmzd VARCHAR(32) DEFAULT NULL COMMENT '"+cjxtMbglPz.getDbFieldTxt()+"加密字段"+"',");
                            }
                            sql.append(cjxtMbglPz.getDbFieldName()).append(" ").append(dbType);
                            if (cjxtMbglPz.getDbLength() != null && !"0".equals(cjxtMbglPz.getDbLength()) && cjxtMbglPz.getDbLength() != "0") {
                                if ("Date".equals(cjxtMbglPz.getDbType()) || "Datetime".equals(cjxtMbglPz.getDbType()) || "date".equals(cjxtMbglPz.getFieldShowType()) || "datetime".equals(cjxtMbglPz.getFieldShowType())) {
                                    sql.append("(").append("0").append(")");
                                } else if ("image".equals(cjxtMbglPz.getFieldShowType()) || "textarea".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                                    sql.append("(").append("1000").append(")");
                                } else {
                                    sql.append("(").append(cjxtMbglPz.getDbLength()).append(")");
                                }
                            }
                            if (cjxtMbglPz.getDbDefaultVal() != null && !"".equals(cjxtMbglPz.getDbDefaultVal())) {
                                sql.append(" DEFAULT '").append(cjxtMbglPz.getDbDefaultVal()).append("'");
                            }
                            if ("1".equals(cjxtMbglPz.getDbIsKey())) {
                                primaryKey = cjxtMbglPz.getDbFieldName();
                            }
                            sql.append(" COMMENT '").append(cjxtMbglPz.getDbFieldTxt()).append("', ");
                        }
                        if (primaryKey.isEmpty()) {
                            primaryKey = "id"; // 如果没有字段被设置为主键，就将名为"id"的字段设置为主键
                        }
                        sql.append("PRIMARY KEY (").append(primaryKey).append(")");
                        sql.append(")");
                        jdbcTemplate.execute(sql.toString());

                        //如果表名包含_ls 怎给表中新增字段dataid
                        if (table.contains("_ls")) {
                            StringBuilder sql1 = new StringBuilder("ALTER TABLE " + table + " ADD COLUMN data_id_ls VARCHAR(64) DEFAULT NULL COMMENT '主表ID'");
                            jdbcTemplate.execute(sql1.toString());
                        }
                        if (table.contains("_sb")) {
                            StringBuilder sql1 = new StringBuilder("ALTER TABLE " + table + " ADD COLUMN shzt VARCHAR(64) DEFAULT '0' COMMENT '审核状态'");
                            jdbcTemplate.execute(sql1.toString());
                        }
                    } else {
                        return Result.error("请添加字段后同步!");
                    }
                }
                //修改Online表单、模版管理表单表同步状态
                OnlCgformHead onlCgformHead = onlCgformHeadService.getById(id);
                if (onlCgformHead != null) {
                    onlCgformHead.setIsDbSynch("Y");
                    onlCgformHeadService.updateById(onlCgformHead);
                }
                cjxtMbgl.setIsDb("1");
                cjxtMbglService.updateById(cjxtMbgl);
            }
            return Result.OK("同步成功!");
        }
        return Result.error("同步失败!");
    }

    /**
     * 强制同步数据库
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模板管理-强制同步数据库")
    @ApiOperation(value = "模板管理-强制同步数据库", notes = "模板管理-强制同步数据库")
    @GetMapping(value = "/dbCreate")
    public Result<String> dbCreate(@RequestParam(name = "id", required = true) String id) {
        CjxtMbgl cjxtMbgl = cjxtMbglService.getById(id);
        if (cjxtMbgl != null) {
            List list = new ArrayList<>();
            String tablename = cjxtMbgl.getBm();
            list.add(tablename);
            if ("1".equals(cjxtMbgl.getSfls())) {
                list.add(tablename + "_ls"); //历史表
            }
            if ("1".equals(cjxtMbgl.getSfsb())) {
                list.add(tablename + "_sb"); //上报表
            }
            for (Object table : list) {
                //查询数据库中是否存在表
                String checkTableExistsSql = "SHOW TABLES LIKE '" + table + "'";
                List<String> tables = jdbcTemplate.queryForList(checkTableExistsSql, String.class);
                if (tables.size() > 0) {
                    // 存在表删除表
                    String dropTableSql = "DROP TABLE " + table;
                    jdbcTemplate.execute(dropTableSql);
                }

                List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, id).eq(CjxtMbglPz::getDelFlag, "0").eq(CjxtMbglPz::getIsTitle, "0").orderByAsc(CjxtMbglPz::getOrderNum));
                if (cjxtMbglPzList.size() > 0 && cjxtMbgl != null) {
                    StringBuilder sql = new StringBuilder("CREATE TABLE " + table + " (");
                    String primaryKey = "";
                    for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
                        String dbType = mapDbType(cjxtMbglPz.getDbType());
                        if(!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx()!=null){
                            sql.append(cjxtMbglPz.getDbFieldName()+"_jmzd VARCHAR(32) DEFAULT NULL COMMENT '"+cjxtMbglPz.getDbFieldTxt()+"加密字段"+"',");
                        }
                        sql.append(cjxtMbglPz.getDbFieldName()).append(" ").append(dbType);
                        if (cjxtMbglPz.getDbLength() != null && !"0".equals(cjxtMbglPz.getDbLength()) && cjxtMbglPz.getDbLength() != "0") {
                            if ("Date".equals(cjxtMbglPz.getDbType()) || "Datetime".equals(cjxtMbglPz.getDbType()) || "date".equals(cjxtMbglPz.getFieldShowType()) || "datetime".equals(cjxtMbglPz.getFieldShowType())) {
                                sql.append("(").append("0").append(")");
                            } else if ("image".equals(cjxtMbglPz.getFieldShowType()) || "textarea".equals(cjxtMbglPz.getFieldShowType()) || "file".equals(cjxtMbglPz.getFieldShowType())) {
                                sql.append("(").append("1000").append(")");
                            } else {
                                sql.append("(").append(cjxtMbglPz.getDbLength()).append(")");
                            }
                        }
                        if (cjxtMbglPz.getDbDefaultVal() != null && !"".equals(cjxtMbglPz.getDbDefaultVal())) {
                            sql.append(" DEFAULT '").append(cjxtMbglPz.getDbDefaultVal()).append("'");
                        }
                        if ("1".equals(cjxtMbglPz.getDbIsKey())) {
                            primaryKey = cjxtMbglPz.getDbFieldName();
                        }
                        sql.append(" COMMENT '").append(cjxtMbglPz.getDbFieldTxt()).append("', ");
                    }
                    if (primaryKey.isEmpty()) {
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
                    if (String.valueOf(table).contains("_sb")) {
                        StringBuilder sql1 = new StringBuilder("ALTER TABLE " + table + " ADD COLUMN shzt VARCHAR(64) DEFAULT '0' COMMENT '审核状态'");
                        jdbcTemplate.execute(sql1.toString());
                    }
                    OnlCgformHead onlCgformHead = onlCgformHeadService.getById(id);
                    if (onlCgformHead != null) {
                        onlCgformHead.setIsDbSynch("Y");
                        onlCgformHeadService.updateById(onlCgformHead);
                    }
                    cjxtMbgl.setIsDb("1");
                    cjxtMbglService.updateById(cjxtMbgl);
                } else {
                    return Result.error("请添加字段后同步!");
                }
            }
            return Result.OK("同步成功!");
        }
        return Result.error("数据库不存在该模版,请检查数据!");
    }

    /**
     *
     * @param mbCode 模板编号
     * @param addressId 地址ID
     * @param dataId 人员采集数据中data_id
     * @param id 数据ID
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "模板管理配置-地址人员采集详情", notes = "模板管理配置-地址人员采集详情")
    @GetMapping(value = "/addressIdUserXq")
    public Result<Map<String, Object>> addressIdRyXq(
            @RequestParam(required = false, name = "mbCode") String mbCode,
            @RequestParam(required = false, name = "addressId") String addressId,
            @RequestParam(required = false, name = "dataId") String dataId,
            @RequestParam(required = false, name = "id") String id,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> cjryXx = new HashMap<>();
        // 加密/脱敏字段返回处理
        List<CjxtMbglPz> mbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().last(" AND ((is_title = '0' AND sfjm = '1' AND mbgl_mbbh = '"+mbCode+"') OR (db_jylx IS NOT NULL AND db_jylx <> '')) AND mbgl_mbbh = '"+mbCode+"' ORDER BY order_num ASC"));

        //模板信息
        List<Map<String, Object>> resultAddressUser = new ArrayList<>();
        //地址人员列表信息
        List<Map<String, Object>> resultAddressUserList = new ArrayList<>();
        //采集人员信息
        List<Map<String, Object>> resultCjUser = new ArrayList<>();
        //查询模板信息
        CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
        CjxtMbgl mbgl = null;
        if("FW001".equals(mbCode)){
            mbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, "RY001").orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
        }
        if("DW001".equals(mbCode)){
            mbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, "RY002").orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
        }
        //RY001模板排序条件
        StringBuilder rymbOrder = new StringBuilder();
        if (!"".equals(addressId) && addressId != null && ("RY001".equals(mbCode) || "FW001".equals(mbCode))) {
            rymbOrder.append(", CAST(t.yhzgx AS UNSIGNED)");
        }

        //模板信息
        String addressUser = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.id = '" + id + "'";
        resultAddressUser = jdbcTemplate.queryForList(addressUser);
        if(resultAddressUser.size()>0){
            for (Map<String, Object> row : resultAddressUser) {
                String addressid = (String) row.get("address_id");
                CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressid);
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
                row.put("address", addressName);

                // 加密/脱敏字段返回处理
                if(mbglPzList.size()>0){
                    for(CjxtMbglPz mbglPz: mbglPzList){
                        Object value = row.get(mbglPz.getDbFieldName());
                        if(!"".equals(value) && value!=null){
                            if(value.toString().contains("_sxby")){
                                String dataV = sjjm(value.toString());
                                row.put(mbglPz.getDbFieldName(), dataV);
                            }else {
                                row.put(mbglPz.getDbFieldName(), value);
                            }

                            if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
                                //身份证
                                if("1".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String sfzh = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(sfzh) && sfzh!=null){
                                            if(sfzh.contains("_sxby")){
                                                String sfzhTm = desensitize(sjjm(sfzh));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }else {
                                                String sfzhTm = desensitize(sfzh);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String sfzh = desensitize((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                                //手机号
                                if("2".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String phone = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(phone) && phone!=null){
                                            if(phone.contains("_sxby")){
                                                String phoneTm = maskPhone(sjjm(phone));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }else {
                                                String phoneTm = maskPhone(phone);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String phone = maskPhone((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //地址人员列表信息
        String addressUserList = "";
        if(!"FW001".equals(mbCode) && !"DW001".equals(mbCode)){
            addressUserList = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "' ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
        }else {
            addressUserList = "SELECT t.* FROM " + mbgl.getBm() + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "' ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
        }
        resultAddressUserList = jdbcTemplate.queryForList(addressUserList);
        if(resultAddressUserList.size()>0){
            for (Map<String, Object> row : resultAddressUserList) {
                String addressid = (String) row.get("address_id");
                CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressid);
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
                row.put("address", addressName);

                // 加密/脱敏字段返回处理
                if(mbglPzList.size()>0){
                    for(CjxtMbglPz mbglPz: mbglPzList){

                        Object value = row.get(mbglPz.getDbFieldName());
                        if(!"".equals(value) && value!=null){
                            if(value.toString().contains("_sxby")){
                                String dataV = sjjm(value.toString());
                                row.put(mbglPz.getDbFieldName(), dataV);
                            }else {
                                row.put(mbglPz.getDbFieldName(), value);
                            }

                            if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
                                //身份证
                                if("1".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String sfzh = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(sfzh) && sfzh!=null){
                                            if(sfzh.contains("_sxby")){
                                                String sfzhTm = desensitize(sjjm(sfzh));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }else {
                                                String sfzhTm = desensitize(sfzh);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String sfzh = desensitize((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                                //手机号
                                if("2".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String phone = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(phone) && phone!=null){
                                            if(phone.contains("_sxby")){
                                                String phoneTm = maskPhone(sjjm(phone));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }else {
                                                String phoneTm = maskPhone(phone);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String phone = maskPhone((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


//        CjxtTaskDispatch taskDispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getAddressId,addressId).eq(CjxtTaskDispatch::getBm,cjxtMbgl.getBm()).orderByDesc(CjxtTaskDispatch::getCreateTime).last("LIMIT 1"));
//        result.put("wgyUser", taskDispatch);
        String cjryxx = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.id = '" + id + "'";
        resultCjUser = jdbcTemplate.queryForList(cjryxx);
        if(resultCjUser.size()>0){
            Map<String, Object> row = resultCjUser.get(0);
            if(row.size()>0){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                String updateBy = (String) row.get("update_by");

                String UTime = "";
                LocalDateTime updateTime = (LocalDateTime) row.get("update_time");
                if(updateTime!=null && !"".equals(updateTime)){
                    UTime = updateTime.format(formatter);
                }

                String CTime = "";
                LocalDateTime createTime = (LocalDateTime) row.get("create_time");
                if(createTime!=null && !"".equals(createTime)){
                    CTime = createTime.format(formatter);
                }

                String createBy = (String) row.get("create_by");
                if((!"".equals(updateBy) && updateBy!=null) && (!"".equals(updateTime) && updateTime!=null)){
                    SysUser user = sysUserService.getUserByName(updateBy);
                    if(user!=null){
                        cjryXx.put("cjry",user.getRealname());
                    }
                    cjryXx.put("cjsj",UTime);
                }else if((!"".equals(createBy) && createBy!=null) && (!"".equals(createTime) && createTime!=null)){
                    SysUser user = sysUserService.getUserByName(createBy);
                    if(user!=null){
                        cjryXx.put("cjry",user.getRealname());
                    }
                    cjryXx.put("cjsj",CTime);
                }

                // 加密/脱敏字段返回处理
                if(mbglPzList.size()>0){
                    for(CjxtMbglPz mbglPz: mbglPzList){

                        Object value = row.get(mbglPz.getDbFieldName());
                        if(!"".equals(value) && value!=null){
                            if(value.toString().contains("_sxby")){
                                String dataV = sjjm(value.toString());
                                row.put(mbglPz.getDbFieldName(), dataV);
                            }else {
                                row.put(mbglPz.getDbFieldName(), value);
                            }

                            if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
                                //身份证
                                if("1".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String sfzh = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(sfzh) && sfzh!=null){
                                            if(sfzh.contains("_sxby")){
                                                String sfzhTm = desensitize(sjjm(sfzh));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }else {
                                                String sfzhTm = desensitize(sfzh);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String sfzh = desensitize((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                                //手机号
                                if("2".equals(mbglPz.getDbJylx())){
                                    if("1".equals(mbglPz.getSfjm())){
                                        String phone = (String) row.get(mbglPz.getDbFieldName());
                                        if(!"".equals(phone) && phone!=null){
                                            if(phone.contains("_sxby")){
                                                String phoneTm = maskPhone(sjjm(phone));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }else {
                                                String phoneTm = maskPhone(phone);
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                            }
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }else {
                                        if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                            String phone = maskPhone((String) row.get(mbglPz.getDbFieldName()));
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
                                        }else {
                                            row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(cjryXx.size()>0){
                result.put("wgyUser", cjryXx);
            }else {
                result.put("wgyUser", null);
            }
        }else {
            String cjryxxAddressId = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t  WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "' LIMIT 1";
            resultCjUser = jdbcTemplate.queryForList(cjryxxAddressId);
            if(resultCjUser.size()>0){
                Map<String, Object> row = resultCjUser.get(0);
                if(row.size()>0){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    String updateBy = (String) row.get("update_by");

                    String UTime = "";
                    LocalDateTime updateTime = (LocalDateTime) row.get("update_time");
                    if(updateTime!=null && !"".equals(updateTime)){
                        UTime = updateTime.format(formatter);
                    }

                    String CTime = "";
                    LocalDateTime createTime = (LocalDateTime) row.get("create_time");
                    if(createTime!=null && !"".equals(createTime)){
                        CTime = createTime.format(formatter);
                    }

                    String createBy = (String) row.get("create_by");
                    if((!"".equals(updateBy) && updateBy!=null) && (!"".equals(updateTime) && updateTime!=null)){
                        SysUser user = sysUserService.getUserByName(updateBy);
                        if(user!=null){
                            cjryXx.put("cjry",user.getRealname());
                        }
                        cjryXx.put("cjsj",UTime);
                    }else if((!"".equals(createBy) && createBy!=null) && (!"".equals(createTime) && createTime!=null)){
                        SysUser user = sysUserService.getUserByName(createBy);
                        if(user!=null){
                            cjryXx.put("cjry",user.getRealname());
                        }
                        cjryXx.put("cjsj",CTime);
                    }

                    // 加密/脱敏字段返回处理
                    if(mbglPzList.size()>0){
                        for(CjxtMbglPz mbglPz: mbglPzList){

                            Object value = row.get(mbglPz.getDbFieldName());
                            if(!"".equals(value) && value!=null){
                                if(value.toString().contains("_sxby")){
                                    String dataV = sjjm(value.toString());
                                    row.put(mbglPz.getDbFieldName(), dataV);
                                }else {
                                    row.put(mbglPz.getDbFieldName(), value);
                                }

                                if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
                                    //身份证
                                    if("1".equals(mbglPz.getDbJylx())){
                                        if("1".equals(mbglPz.getSfjm())){
                                            String sfzh = (String) row.get(mbglPz.getDbFieldName());
                                            if(!"".equals(sfzh) && sfzh!=null){
                                                if(sfzh.contains("_sxby")){
                                                    String sfzhTm = desensitize(sjjm(sfzh));
                                                    row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                                }else {
                                                    String sfzhTm = desensitize(sfzh);
                                                    row.put(mbglPz.getDbFieldName()+"_jmzd", sfzhTm);
                                                }
                                            }else {
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                            }
                                        }else {
                                            if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                                String sfzh = desensitize((String) row.get(mbglPz.getDbFieldName()));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
                                            }else {
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                            }
                                        }
                                    }
                                    //手机号
                                    if("2".equals(mbglPz.getDbJylx())){
                                        if("1".equals(mbglPz.getSfjm())){
                                            String phone = (String) row.get(mbglPz.getDbFieldName());
                                            if(!"".equals(phone) && phone!=null){
                                                if(phone.contains("_sxby")){
                                                    String phoneTm = maskPhone(sjjm(phone));
                                                    row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                                }else {
                                                    String phoneTm = maskPhone(phone);
                                                    row.put(mbglPz.getDbFieldName()+"_jmzd", phoneTm);
                                                }
                                            }else {
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                            }
                                        }else {
                                            if(!"".equals((String) row.get(mbglPz.getDbFieldName())) && (String) row.get(mbglPz.getDbFieldName())!=null){
                                                String phone = maskPhone((String) row.get(mbglPz.getDbFieldName()));
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
                                            }else {
                                                row.put(mbglPz.getDbFieldName()+"_jmzd", "");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(cjryXx.size()>0){
                    result.put("wgyUser", cjryXx);
                }else {
                    result.put("wgyUser", null);
                }
            }else {
                result.put("wgyUser", null);
            }
        }

        String countSql = "SELECT COUNT(*) FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' AND address_id = '" + addressId + "'" ;
        // 执行查询并获取总条数
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        // 将总页数添加到结果中
        result.put("totalPages", totalPages);
        result.put("size", pageSize);
        result.put("total", totalCount);
        result.put("pages", totalPages);
        result.put("tmzd", mbglPzList);
        if(resultAddressUserList.size()>0){
            result.put("addressUserList", resultAddressUserList);
        }
        if(resultAddressUser.size()>0){
            result.put("resultAddressUser", resultAddressUser);
        }
        return Result.OK(result);
    }

    @ApiOperation(value = "模板管理配置-动态数据移除", notes = "模板管理配置-动态数据移除")
    @GetMapping(value = "/dataValueYc")
    public Result<String> dataValueYc(
            @RequestParam(required = true, name = "mbCode") String mbCode,
            @RequestParam(required = true, name = "dataId") String dataId,
            @RequestParam(required = false, name = "cjSb") String cjSb,
            HttpServletRequest req) {
//        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (mbCode != null && dataId != null) {
            //查询模版
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
            if (cjxtMbgl != null) {
                String bm = cjxtMbgl.getBm();
                if("".equals(cjSb) || cjSb == null){
                    cjSb = "1";
                }
                if ("1".equals(cjSb)) {
                    bm = cjxtMbgl.getBm();
                } else if ("3".equals(cjSb)) {
                    bm = cjxtMbgl.getBm() + "_sb";
                }
                String renameColumnSql = "UPDATE " + bm + " SET del_flag = '1' WHERE id = '" + dataId + "' ;";
                jdbcTemplate.execute(renameColumnSql);

                //数据大屏webSocke
                webSocket.sendMergedMessage();
            }
        }
        return Result.OK("删除成功");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "模板管理-通过id删除")
    @ApiOperation(value = "模板管理-通过id删除", notes = "模板管理-通过id删除")
//    @RequiresPermissions("cjxt:cjxt_mbgl:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        CjxtMbgl cjxtMbgl = cjxtMbglService.getById(id);
        //删除数据库表逻辑
//        if (cjxtMbgl != null) {
//            List list = new ArrayList<>();
//            String tablename = cjxtMbgl.getBm();
//            list.add(tablename);
//            if ("1".equals(cjxtMbgl.getSfls())) {
//                list.add(tablename + "_ls"); //历史表
//            }
//            if ("1".equals(cjxtMbgl.getSfsb())) {
//                list.add(tablename + "_sb"); //上报表
//            }
//            for (Object table : list) {
//                //查询数据库中是否存在表
//                String checkTableExistsSql = "SHOW TABLES LIKE '" + table + "'";
//                List<String> tables = jdbcTemplate.queryForList(checkTableExistsSql, String.class);
//                if (tables.size() > 0) {
//                    // 存在表删除表
//                    String dropTableSql = "DROP TABLE " + table;
//                    jdbcTemplate.execute(dropTableSql);
//                }
//            }
//        }
        cjxtMbglService.delMain(id);
        OnlCgformHead onlCgformHead = onlCgformHeadService.getById(id);
        if (onlCgformHead != null) {
            onlCgformHeadService.removeById(onlCgformHead.getId());
        }
        List<OnlCgformField> cgformFieldList = onlCgformFieldsService.list(new LambdaQueryWrapper<OnlCgformField>().eq(OnlCgformField::getCgformHeadId, id));
        if (cgformFieldList.size() > 0) {
            for (OnlCgformField cgformField : cgformFieldList) {
                onlCgformFieldsService.removeById(cgformField.getId());
            }
        }
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "模板管理-批量删除")
    @ApiOperation(value = "模板管理-批量删除", notes = "模板管理-批量删除")
//    @RequiresPermissions("cjxt:cjxt_mbgl:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.cjxtMbglService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "模板管理-通过id查询")
    @ApiOperation(value = "模板管理-通过id查询", notes = "模板管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<CjxtMbgl> queryById(@RequestParam(name = "id", required = true) String id) {
        CjxtMbgl cjxtMbgl = cjxtMbglService.getById(id);
        if (cjxtMbgl == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cjxtMbgl);
    }

    /**
     * CODE查询
     * @param code
     * @return
     */
    @ApiOperation(value = "模板管理-通过code查询", notes = "模板管理-通过code查询")
    @GetMapping(value = "/queryByCode")
    public Result<CjxtMbgl> queryByCode(@RequestParam(name = "code", required = true) String code) {
//        CjxtMbgl cjxtMbgl = cjxtMbglService.getById(id);
        CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,code).eq(CjxtMbgl::getIsDb,"1"));
        if (cjxtMbgl == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cjxtMbgl);
    }

    /**
     * 身份证信息
     * @param sfzh
     * @param mbCode
     * @return
     */
    @GetMapping(value = "/sfzhMsg")
    @ApiOperation(value = "模板管理-身份证信息", notes = "模板管理-身份证信息")
    public Result<Map<String, Object>> sfzhMsg(@RequestParam(name = "sfzh", required = false)String sfzh,
                                                  @RequestParam(name = "mbCode", required = false)String mbCode) {
        Map<String, Object> result = new HashMap<>();
        if(!"".equals(sfzh) && sfzh!=null && !"".equals(mbCode) && mbCode!=null){
            CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
            if(cjxtMbgl!=null) {
                if ("RY001".equals(mbCode)) {
                    String sql = "SELECT * FROM " + cjxtMbgl.getBm() + " WHERE rysfzh = '" + sfzh + "' ORDER BY create_time ASC LIMIT 1";
                    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
                    if (resultList.size() > 0) {
                        result.put("dataValue", resultList);
                        result.put("hasValue", "true");
                    } else {
                        Map<String, Object> resultSfzh = new HashMap<>();
                        if (sfzh.length() == 18) {
                            // 提取出生日期
                            String birthDate = sfzh.substring(6, 14);
                            String formattedBirthDate = birthDate.substring(0, 4) + "-" + birthDate.substring(4, 6) + "-" + birthDate.substring(6, 8);
                            // 提取性别
                            String genderCode = sfzh.substring(16, 17);
                            String sex = Integer.parseInt(genderCode) % 2 == 0 ? "2" : "1"; // 男传1 女传2

                            resultSfzh.put("birthDate", formattedBirthDate);
                            resultSfzh.put("sex", sex);
                        } else {
                            resultSfzh.put("birthDate", "");
                            resultSfzh.put("sex", "");
                        }
                        result.put("dataValue", resultSfzh);
                        result.put("hasValue", "false");
                    }
                }
            }
        }
        return Result.OK(result);
    }


    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "动态模板配置通过主表ID查询")
    @ApiOperation(value = "动态模板配置主表ID查询", notes = "动态模板配置-通主表ID查询")
    @GetMapping(value = "/queryCjxtMbglPzByMainId")
    public Result<List<CjxtMbglPz>> queryCjxtMbglPzListByMainId(@RequestParam(name = "id", required = true) String id) {
        List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.selectByMainId(id);
        for (CjxtMbglPz cjxtMbglPz : cjxtMbglPzList) {
            cjxtMbglPz.setDbFieldNameDto(cjxtMbglPz.getDbFieldName());
            cjxtMbglPz.setDbLengthDto(cjxtMbglPz.getDbLength());
            cjxtMbglPz.setDbTypeDto(cjxtMbglPz.getDbType());
            cjxtMbglPz.setDbJylxDto(cjxtMbglPz.getDbJylx());
        }
        return Result.OK(cjxtMbglPzList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param cjxtMbgl
     */
//    @RequiresPermissions("cjxt:cjxt_mbgl:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtMbgl cjxtMbgl) {
        // Step.1 组装查询条件查询数据
        QueryWrapper<CjxtMbgl> queryWrapper = QueryGenerator.initQueryWrapper(cjxtMbgl, request.getParameterMap());
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //配置选中数据查询条件
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        //Step.2 获取导出数据
        List<CjxtMbgl> cjxtMbglList = cjxtMbglService.list(queryWrapper);

        // Step.3 组装pageList
        List<CjxtMbglPage> pageList = new ArrayList<CjxtMbglPage>();
        for (CjxtMbgl main : cjxtMbglList) {
            CjxtMbglPage vo = new CjxtMbglPage();
            BeanUtils.copyProperties(main, vo);
            List<CjxtMbglPz> cjxtMbglPzList = cjxtMbglPzService.selectByMainId(main.getId());
            vo.setCjxtMbglPzList(cjxtMbglPzList);
            pageList.add(vo);
        }

        // Step.4 AutoPoi 导出Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "模板管理列表");
        mv.addObject(NormalExcelConstants.CLASS, CjxtMbglPage.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("模板管理数据", "导出人:" + sysUser.getRealname(), "模板管理"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    @RequestMapping(value = "/exportXlsTemplate")
    public ResponseEntity<InputStreamResource> exportTemplateXls() throws IOException {
        // 1 指定文件路径
        ClassPathResource file = new ClassPathResource("static/bigscreen/template1/模板管理示例.xls");

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

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
//    @RequiresPermissions("cjxt:cjxt_mbgl:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
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
                List<CjxtMbglPage> list = ExcelImportUtil.importExcel(file.getInputStream(), CjxtMbglPage.class, params);
                boolean importStatus = true;
                String importBm = "";
                for (CjxtMbglPage page : list) {
                    CjxtMbgl po = new CjxtMbgl();
                    BeanUtils.copyProperties(page, po);
                    CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getBm, po.getBm()));
                    if (cjxtMbgl != null) {
                        importBm = po.getBm();
                        importStatus = false;
                        continue;
                    }
                    cjxtMbglService.saveMain(po, page.getCjxtMbglPzList());
                    Date date = new Date();
                    OnlCgformHead onlCgformHead = new OnlCgformHead();
                    onlCgformHead.setId(po.getId());
                    onlCgformHead.setTableName(page.getBm());
                    if (!"".equals(page.getTableType()) && page.getTableType() != null && !page.getTableType().isEmpty()) {
                        onlCgformHead.setTableType(Integer.valueOf(page.getTableType()));
                    } else {
                        onlCgformHead.setTableType(1);
                    }
                    onlCgformHead.setTableVersion("1");
                    onlCgformHead.setTableTxt(page.getMbname());
                    onlCgformHead.setIsCheckbox("Y");
                    onlCgformHead.setIsDbSynch("N");
                    onlCgformHead.setIsPage("Y");
                    onlCgformHead.setIsTree("N");
                    onlCgformHead.setIdSequence(null);
                    onlCgformHead.setIdType("UUID");
                    onlCgformHead.setQueryMode("single");
                    onlCgformHead.setRelationType(null);//映射关系 0一对多  1一对一
                    onlCgformHead.setSubTableStr(null);//子表
                    onlCgformHead.setTabOrderNum(null);//附表排序序号
                    onlCgformHead.setTreeParentIdField(null);
                    onlCgformHead.setTreeIdField(null);
                    onlCgformHead.setTreeFieldname(null);
                    onlCgformHead.setFormCategory("temp");
                    onlCgformHead.setFormTemplate("2");//表单列表风格 默认2列
                    onlCgformHead.setFormTemplateMobile(null);
                    onlCgformHead.setScroll("1");
                    onlCgformHead.setCopyType("0");
                    onlCgformHead.setPhysicId(null);
                    onlCgformHead.setExtConfigJson("{\"reportPrintShow\":0,\"reportPrintUrl\":\"\",\"joinQuery\":0,\"modelFullscreen\":0,\"modalMinWidth\":\"\",\"commentStatus\":0,\"tableFixedAction\":1,\"tableFixedActionType\":\"right\"}");
                    onlCgformHead.setUpdateBy(null);
                    onlCgformHead.setUpdateTime(null);
                    onlCgformHead.setCreateBy("admin");
                    onlCgformHead.setCreateTime(date);
                    onlCgformHead.setThemeTemplate("normal");
                    onlCgformHead.setIsDesForm("N");
                    onlCgformHead.setDesFormCode("");
                    onlCgformHead.setLowAppId(null);
                    onlCgformHeadService.save(onlCgformHead);

                    OnlCgformField onlCgformField = new OnlCgformField();

//				  List<CjxtMbglPz> cjxtMbglPz = page.getCjxtMbglPzList();
                    List<CjxtMbglPz> cjxtMbglPz = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, po.getId()));
                    if (cjxtMbglPz != null && cjxtMbglPz.size() > 0) {
                        int i = 0;
                        for (CjxtMbglPz cj : cjxtMbglPz) {
                            if (cj.getDbFieldName() == null || "".equals(cj.getDbFieldName()) || cj.getDbFieldName().isEmpty()) {
                                continue;
                            }
                            i++;
                            onlCgformField.setId(cj.getId());
                            onlCgformField.setCgformHeadId(onlCgformHead.getId());
                            onlCgformField.setDbFieldName(cj.getDbFieldName());
                            onlCgformField.setDbFieldTxt(cj.getDbFieldTxt());
                            if ("id".equals(cj.getDbFieldName())) {
                                onlCgformField.setDbIsKey("1");
                            } else {
                                onlCgformField.setDbIsKey("0");
                            }
                            onlCgformField.setDbIsNull("0");//是否允许为空 0否 1是
                            onlCgformField.setDbIsPersist("1");//是否需要同步数据库字段 0否 1是
                            onlCgformField.setDbType(cj.getDbType());
                            onlCgformField.setDbLength(cj.getDbLength());
                            onlCgformField.setDbPointLength("0");
                            onlCgformField.setDbDefaultVal("");
                            onlCgformField.setDictField(cj.getDictField());
                            onlCgformField.setDictTable("");
                            onlCgformField.setDictText("");
                            onlCgformField.setFieldShowType(cj.getFieldShowType());
                            onlCgformField.setFieldHref("");
                            onlCgformField.setFieldLength("200");
                            onlCgformField.setFieldValidType("");
                            onlCgformField.setFieldMustInput("0");// 0否 1是
                            onlCgformField.setFieldExtendJson("");
                            onlCgformField.setFieldDefaultValue("");
                            if ("".equals(cj.getIsQuery()) || cj.getIsQuery() == null) {
                                onlCgformField.setIsQuery("0");//是否查询 0否 1是
                            } else {
                                onlCgformField.setIsQuery(cj.getIsQuery());//是否查询 0否 1是
                            }
                            onlCgformField.setIsShowForm("1"); // 表单是否显示 0否 1是
                            onlCgformField.setIsShowList("1"); // 列表是否显示 0否 1是
                            onlCgformField.setIsReadOnly("0"); // 是否制度 0否 1是
                            onlCgformField.setQueryMode("single"); // 查询模式
                            onlCgformField.setMainTable("");
                            onlCgformField.setMainField("");
                            onlCgformField.setOrderNum(String.valueOf(i)); // 排序号
                            onlCgformField.setConverter("");
                            onlCgformField.setQueryDefVal("");
                            onlCgformField.setQueryDictText("");
                            onlCgformField.setQueryDictField("");
                            onlCgformField.setQueryDictTable("");
                            onlCgformField.setQueryShowType("text");
                            onlCgformField.setQueryConfigFlag("0");
                            onlCgformField.setQueryValidType(null);
                            onlCgformField.setQueryMustInput(null);
                            onlCgformField.setSortFlag("0");
                            onlCgformField.setCreateBy("admin");
                            onlCgformField.setCreateTime(date);
                            onlCgformField.setUpdateBy(null);
                            onlCgformField.setUpdateTime(null);
                            onlCgformFieldsService.save(onlCgformField);
                        }
                    }
                }
                if (importStatus == true) {
                    return Result.OK("文件导入成功！数据行数:" + list.size());
                } else {
                    return Result.error("文件部分导入成功！表名:" + importBm + "已存在模版管理!");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("文件导入失败:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.OK("上传文件异常,文件导入失败！");
    }

    public boolean isTableExist(String tableName) {
        try {
            String sql = "SELECT 1 FROM " + tableName;
            entityManager.createNativeQuery(sql).getResultList();
            return true;
        } catch (Exception e) {
            return false;
        }
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

    public SysDepart getSysCodeByNameAndPid(String name, String pid) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.eq("depart_name", name);
        if (pid != null && !"".equals(pid)) {
            queryWrapper.eq("parent_id", pid);
        }
        SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
        return sysDepart;
    }

    /**
     * 通过bm查询
     *
     * @param bm
     * @return
     */
    //@AutoLog(value = "系统参数-通过bm查询")
    @ApiOperation(value = "系统参数-通过bm查询", notes = "系统参数-通过bm查询")
    @GetMapping(value = "/queryIdByBm")
    public Result<CjxtMbgl> queryIdByBm(@RequestParam(name = "bm", required = true) String bm) {
        QueryWrapper<CjxtMbgl> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bm", bm);
        CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(queryWrapper);

        if (cjxtMbgl == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cjxtMbgl);
    }

    /**
     * 人口轨迹-通过人员身份证号查询
     *
     * @param rysfzh
     * @return
     */
    //@AutoLog(value = "人口轨迹-通过人员身份证号查询")
    @ApiOperation(value = "人口轨迹-通过人员身份证号查询", notes = "人口轨迹-通过人员身份证号查询")
    @GetMapping(value = "/queryRklsDataByRysfz")
    public Result<List<Map<String, Object>>> queryRklsDataByRysfz(@RequestParam(name = "rysfzh", required = true) String rysfzh, @RequestParam(name = "tableName", required = true) String tableName) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        String dataSql = "SELECT t.* FROM "+tableName+" t WHERE t.del_flag = '0' AND t.rysfzh = '"+rysfzh+"' ORDER BY create_time DESC ;";
        resultList = jdbcTemplate.queryForList(dataSql);
        if(resultList.size()==0){
            dataSql = "SELECT t.* FROM cjxt_rkcj t WHERE t.del_flag = '0' AND t.rysfzh = '"+rysfzh+"' ORDER BY create_time DESC ;";
            resultList = jdbcTemplate.queryForList(dataSql);
        }
        return Result.OK(resultList);
    }

    @ApiOperation(value = "部门网格转换排序", notes = "部门网格转换排序")
    @GetMapping(value = "/departZh")
    public Result<String> departZh() {
        updateDepartOrder();
        return Result.OK("转换完成");
    }

    public void updateDepartOrder() {
        String sql = "SELECT * FROM sys_depart WHERE org_category = '9' AND depart_name like '第%网格' ORDER BY org_code";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        for (Map row : rows) {
            String departName = (String) row.get("depart_name");
            departName = departName.replace("第", "")
                    .replace("网格", "")
                    .replace("南", "")
                    .replace("北", "")
                    .replace("组网格", "")
                    .replace("上", "")
                    .replace("下", "")
                    .replace("组", "")
                    .replace("专业队", "")
                    .replace("麻生圐圙村", "");
            System.out.println("网格名称::" + departName);
//			int order = Integer.valueOf(chineseNumToArabicNumTwo(departName));
//			jdbcTemplate.update("UPDATE sys_depart SET depart_order = ? WHERE id = ?", order, row.get("id"));
        }
    }

    /**
     * ListPz接口 返回数据数据脱敏
     * @param dbJylx
     * @param value
     * @param sfjm
     * @return
     */
    public String sjtm(String dbJylx, String sfjm, String value){
        //身份证
        if("1".equals(dbJylx)){
            if("1".equals(sfjm)){
                List<CjxtXtcs> xtcsList = cjxtXtcsService.list(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"AESJMIV").or().eq(CjxtXtcs::getCsKey,"AESJMKEY"));
                String aesIV = "";
                String aesKEY = "";
                for(CjxtXtcs xtcs: xtcsList){
                    String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOP2o/qai0m6TbC+PnHXl/Qa5oq7\r\nHuDYiBKqvkX9Fj3zvz1sSup8H2d0kV7x1WxhQJ5rJyN/5HdRSfXd1jmI9o/yAhGNwLLVtFBgCQsf\r\nuqFIlwECRMhq9o6rB7z19UBS+Ybi8HHcysZJbE30miNd13p0g3ehnFSHQgKxsuxCMlXzAgMBAAEC\r\ngYBkgTMeo9BROyBRki65xlGqP8nw1Dry6sRjZn/pH4IU4kgobnxBEWRPGmeVMo3j8KSqs5dX4WT6\r\n7g3rplhymV4Kl1Sm2EeMN2w43R1bDGZX2XuAEBQlF36Lqq2l6rTmwieFC21zeNNVtqifzcYj9NU5\r\nWNjiGyS/bJExhFkOEUVo4QJBAP8p3kJrqcdEDRObXeA1xqcsCJFvfS/QZLZS9QAOtmXjXIjY2kyv\r\ngcDwZIDj2NXDe0KQ7Q5yYMbxnQwQj3IGhukCQQDktfI36FrFrTMAiC8wzJB96bCXEitSFPcbRVXJ\r\nrpoGMXqdZ+QOtVZ7xp13IeypD+tvzkKYobvi5d0wlsUgqOR7AkBTRcqvSouh6GeMGoxMe8BVFl3F\r\neOkWDaCQkAo89pA4ODcewgacODrUSJ/EIJfHS4CSfNGggVtsEbX7Ffx6bf7hAkBYLFUPfMLSBu3s\r\nvJQE6jiicl+kPlr5MB/8IYTrVicHUDgHSzA/A2YuHepDLY8BtqN73TcLhP7cgOX/f4DMEJDVAkEA\r\n0XA3Fh4DKen2N/iKiKbtI7f9gXGlb8/jjIqw4s4NNRodvOHxsE55nc7BQnjJ9sJQOn1NdlIL6sq6\r\n9qnisQX5Tg==";
                    String mw = decryptRes(xtcs.getCsVal(),privateKey);
                    if("AESJMIV".equals(xtcs.getCsKey())){
                        aesIV = mw;
                    }
                    if("AESJMKEY".equals(xtcs.getCsKey())){
                        aesKEY = mw;
                    }
                }
                if(!"".equals(aesIV) && !"".equals(aesKEY)){
                    try {
                        AesTestOne aesTestOne = new AesTestOne();
                        String sfzhTm = desensitize(value);
                        return sfzhTm;
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }
            }else {
                String sfzh = desensitize(value);
                return sfzh;
            }
        }
        //手机号
        if("2".equals(dbJylx)){
            if("1".equals(sfjm)){
                List<CjxtXtcs> xtcsList = cjxtXtcsService.list(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"AESJMIV").or().eq(CjxtXtcs::getCsKey,"AESJMKEY"));
                String aesIV = "";
                String aesKEY = "";
                for(CjxtXtcs xtcs: xtcsList){
                    String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOP2o/qai0m6TbC+PnHXl/Qa5oq7\r\nHuDYiBKqvkX9Fj3zvz1sSup8H2d0kV7x1WxhQJ5rJyN/5HdRSfXd1jmI9o/yAhGNwLLVtFBgCQsf\r\nuqFIlwECRMhq9o6rB7z19UBS+Ybi8HHcysZJbE30miNd13p0g3ehnFSHQgKxsuxCMlXzAgMBAAEC\r\ngYBkgTMeo9BROyBRki65xlGqP8nw1Dry6sRjZn/pH4IU4kgobnxBEWRPGmeVMo3j8KSqs5dX4WT6\r\n7g3rplhymV4Kl1Sm2EeMN2w43R1bDGZX2XuAEBQlF36Lqq2l6rTmwieFC21zeNNVtqifzcYj9NU5\r\nWNjiGyS/bJExhFkOEUVo4QJBAP8p3kJrqcdEDRObXeA1xqcsCJFvfS/QZLZS9QAOtmXjXIjY2kyv\r\ngcDwZIDj2NXDe0KQ7Q5yYMbxnQwQj3IGhukCQQDktfI36FrFrTMAiC8wzJB96bCXEitSFPcbRVXJ\r\nrpoGMXqdZ+QOtVZ7xp13IeypD+tvzkKYobvi5d0wlsUgqOR7AkBTRcqvSouh6GeMGoxMe8BVFl3F\r\neOkWDaCQkAo89pA4ODcewgacODrUSJ/EIJfHS4CSfNGggVtsEbX7Ffx6bf7hAkBYLFUPfMLSBu3s\r\nvJQE6jiicl+kPlr5MB/8IYTrVicHUDgHSzA/A2YuHepDLY8BtqN73TcLhP7cgOX/f4DMEJDVAkEA\r\n0XA3Fh4DKen2N/iKiKbtI7f9gXGlb8/jjIqw4s4NNRodvOHxsE55nc7BQnjJ9sJQOn1NdlIL6sq6\r\n9qnisQX5Tg==";
                    String mw = decryptRes(xtcs.getCsVal(),privateKey);
                    if("AESJMIV".equals(xtcs.getCsKey())){
                        aesIV = mw;
                    }
                    if("AESJMKEY".equals(xtcs.getCsKey())){
                        aesKEY = mw;
                    }
                }
                if(!"".equals(aesIV) && !"".equals(aesKEY)){
                    try {
                        AesTestOne aesTestOne = new AesTestOne();
                        String phoneTm = maskPhone(value);
                        return phoneTm;
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }
            }else {
                String phone = maskPhone(value);
                return phone;
            }
        }
        return "";
    }


    /**
     * 数据解密
     * @param value
     * @return
     */
    public String sjjm(String value){
        List<CjxtXtcs> cjxtList = cjxtXtcsService.list(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"AESJMIV").or().eq(CjxtXtcs::getCsKey,"AESJMKEY"));
        String aesIv = "";
        String aesKey = "";
        for(CjxtXtcs xtcs: cjxtList){
            String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOP2o/qai0m6TbC+PnHXl/Qa5oq7\r\nHuDYiBKqvkX9Fj3zvz1sSup8H2d0kV7x1WxhQJ5rJyN/5HdRSfXd1jmI9o/yAhGNwLLVtFBgCQsf\r\nuqFIlwECRMhq9o6rB7z19UBS+Ybi8HHcysZJbE30miNd13p0g3ehnFSHQgKxsuxCMlXzAgMBAAEC\r\ngYBkgTMeo9BROyBRki65xlGqP8nw1Dry6sRjZn/pH4IU4kgobnxBEWRPGmeVMo3j8KSqs5dX4WT6\r\n7g3rplhymV4Kl1Sm2EeMN2w43R1bDGZX2XuAEBQlF36Lqq2l6rTmwieFC21zeNNVtqifzcYj9NU5\r\nWNjiGyS/bJExhFkOEUVo4QJBAP8p3kJrqcdEDRObXeA1xqcsCJFvfS/QZLZS9QAOtmXjXIjY2kyv\r\ngcDwZIDj2NXDe0KQ7Q5yYMbxnQwQj3IGhukCQQDktfI36FrFrTMAiC8wzJB96bCXEitSFPcbRVXJ\r\nrpoGMXqdZ+QOtVZ7xp13IeypD+tvzkKYobvi5d0wlsUgqOR7AkBTRcqvSouh6GeMGoxMe8BVFl3F\r\neOkWDaCQkAo89pA4ODcewgacODrUSJ/EIJfHS4CSfNGggVtsEbX7Ffx6bf7hAkBYLFUPfMLSBu3s\r\nvJQE6jiicl+kPlr5MB/8IYTrVicHUDgHSzA/A2YuHepDLY8BtqN73TcLhP7cgOX/f4DMEJDVAkEA\r\n0XA3Fh4DKen2N/iKiKbtI7f9gXGlb8/jjIqw4s4NNRodvOHxsE55nc7BQnjJ9sJQOn1NdlIL6sq6\r\n9qnisQX5Tg==";
            String mw = decryptRes(xtcs.getCsVal(),privateKey);
            if("AESJMIV".equals(xtcs.getCsKey())){
                aesIv = mw;
            }
            if("AESJMKEY".equals(xtcs.getCsKey())){
                aesKey = mw;
            }
        }
        if(!"".equals(aesIv) && !"".equals(aesKey)){
            try {
                AesTestOne aesTestOne = new AesTestOne();
                return aesTestOne.decryptZdyCf(value,aesKey,aesIv);
            }catch (Exception e){
                System.out.println(e);
            }
        }
        return "";
    }

    /**
     * 数据加密
     * @param value
     * @return
     */
    public String sjjmValue(String value){
        List<CjxtXtcs> cjxtList = cjxtXtcsService.list(new LambdaQueryWrapper<CjxtXtcs>().eq(CjxtXtcs::getCsKey,"AESJMIV").or().eq(CjxtXtcs::getCsKey,"AESJMKEY"));
        String aesIv = "";
        String aesKey = "";
        for(CjxtXtcs xtcs: cjxtList){
            String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOP2o/qai0m6TbC+PnHXl/Qa5oq7\r\nHuDYiBKqvkX9Fj3zvz1sSup8H2d0kV7x1WxhQJ5rJyN/5HdRSfXd1jmI9o/yAhGNwLLVtFBgCQsf\r\nuqFIlwECRMhq9o6rB7z19UBS+Ybi8HHcysZJbE30miNd13p0g3ehnFSHQgKxsuxCMlXzAgMBAAEC\r\ngYBkgTMeo9BROyBRki65xlGqP8nw1Dry6sRjZn/pH4IU4kgobnxBEWRPGmeVMo3j8KSqs5dX4WT6\r\n7g3rplhymV4Kl1Sm2EeMN2w43R1bDGZX2XuAEBQlF36Lqq2l6rTmwieFC21zeNNVtqifzcYj9NU5\r\nWNjiGyS/bJExhFkOEUVo4QJBAP8p3kJrqcdEDRObXeA1xqcsCJFvfS/QZLZS9QAOtmXjXIjY2kyv\r\ngcDwZIDj2NXDe0KQ7Q5yYMbxnQwQj3IGhukCQQDktfI36FrFrTMAiC8wzJB96bCXEitSFPcbRVXJ\r\nrpoGMXqdZ+QOtVZ7xp13IeypD+tvzkKYobvi5d0wlsUgqOR7AkBTRcqvSouh6GeMGoxMe8BVFl3F\r\neOkWDaCQkAo89pA4ODcewgacODrUSJ/EIJfHS4CSfNGggVtsEbX7Ffx6bf7hAkBYLFUPfMLSBu3s\r\nvJQE6jiicl+kPlr5MB/8IYTrVicHUDgHSzA/A2YuHepDLY8BtqN73TcLhP7cgOX/f4DMEJDVAkEA\r\n0XA3Fh4DKen2N/iKiKbtI7f9gXGlb8/jjIqw4s4NNRodvOHxsE55nc7BQnjJ9sJQOn1NdlIL6sq6\r\n9qnisQX5Tg==";
            String mw = decryptRes(xtcs.getCsVal(),privateKey);
            if("AESJMIV".equals(xtcs.getCsKey())){
                aesIv = mw;
            }
            if("AESJMKEY".equals(xtcs.getCsKey())){
                aesKey = mw;
            }
        }
        if(!"".equals(aesIv) && !"".equals(aesKey)){
            try {
                AesTestOne aesTestOne = new AesTestOne();
                return aesTestOne.encryptZdyCf(value,aesKey,aesIv);
            }catch (Exception e){
                System.out.println(e);
            }
        }
        return "";
    }

    /**
     * 加密解密脱敏转译
     * @param mbCode
     * @param sjType
     * @return
     */
    @ApiOperation(value = "模板管理配置-加密解密脱敏转译", notes = "模板管理配置-加密解密脱敏转译")
    @GetMapping(value = "/jmjmTm")
    public Result<List<Map<String, Object>>> jmjmTm(@RequestParam(name = "mbCode", required = true) String mbCode, @RequestParam(name = "sjType", required = true) String sjType) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 查询模版
        CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode).eq(CjxtMbgl::getIsDb,"1"));
        // 查询模版字段
        List<CjxtMbglPz> mbglPzList = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getIsTitle,"0").eq(CjxtMbglPz::getMbglMbbh,mbCode).eq(CjxtMbglPz::getIsCommon,"0").ne(CjxtMbglPz::getDbType,"Date").ne(CjxtMbglPz::getDbType,"DateTime").orderByAsc(CjxtMbglPz::getOrderNum));
        // 数据Type 0 数据加密
        if("0".equals(sjType)){
            if(cjxtMbgl!=null){
                String dataSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' ORDER BY create_time DESC;";
                resultList = jdbcTemplate.queryForList(dataSql);

                for (Map<String, Object> row : resultList) {
                    List<Object> updateValues = new ArrayList<>();
                    StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");

                    boolean hasUpdate = false;

                    // 遍历字段配置
                    for (CjxtMbglPz cjxtMbglPz : mbglPzList) {
                        String fieldName = cjxtMbglPz.getDbFieldName();
                        String encryptedValue = "";
                        String encryptedTmValue = "";

                        if(!"".equals((String) row.get(fieldName)) && (String) row.get(fieldName) != null){
                            // 字段加密
                            if ("1".equals(cjxtMbglPz.getSfjm())) {
                                if(((String) row.get(fieldName)).contains("_sxby")){
                                    encryptedValue = (String) row.get(fieldName);
                                }else {
                                    encryptedValue = sjjmValue((String) row.get(fieldName));
                                }
                            }
                            if (!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx() != null) {
                                if ("1".equals(cjxtMbglPz.getDbJylx())) {
                                    if(((String) row.get(fieldName)).contains("_sxby")){
                                        encryptedTmValue = desensitize(sjjm((String) row.get(fieldName)));
                                    }else {
                                        encryptedTmValue = desensitize((String) row.get(fieldName)); // 脱敏
                                    }
                                } else if ("2".equals(cjxtMbglPz.getDbJylx())) {
                                    if(((String) row.get(fieldName)).contains("_sxby")){
                                        encryptedTmValue = maskPhone(sjjm((String) row.get(fieldName))); // 手机号脱敏
                                    }else {
                                        encryptedTmValue = maskPhone((String) row.get(fieldName)); // 手机号脱敏
                                    }
                                }
                            }
                        }
                        if (!"".equals(encryptedValue)) {
                            if (hasUpdate) {
                                updateSql.append(", ");
                            }
                            if ("1".equals(cjxtMbglPz.getSfjm())) {
                                updateSql.append(fieldName).append(" = ?");
                                updateValues.add(encryptedValue);
                                hasUpdate = true;
                            }
                            if (!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx() != null) {
                                updateSql.append(", ");
                                updateSql.append(fieldName).append("_jmzd").append(" = ?");
                                updateValues.add(encryptedTmValue);
                                hasUpdate = true;
                            }
                        }
                    }
                    if (hasUpdate) {
                        String idValue = (String) row.get("id");
                        updateSql.append(" WHERE id = ? AND del_flag = '0'");
                        updateValues.add(idValue);
                        jdbcTemplate.update(updateSql.toString(), updateValues.toArray());
                    }
                }
            }
        }
        // 数据Type 1 数据解密
        if("1".equals(sjType)){
            if(cjxtMbgl!=null){
                String dataSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' ORDER BY create_time DESC;";
                resultList = jdbcTemplate.queryForList(dataSql);

                for (Map<String, Object> row : resultList) {
                    List<Object> updateValues = new ArrayList<>();
                    StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");

                    boolean hasUpdate = false;

                    // 遍历字段配置
                    for (CjxtMbglPz cjxtMbglPz : mbglPzList) {
                        String fieldName = cjxtMbglPz.getDbFieldName();
                        String encryptedValue = "";
                        String encryptedTmValue = "";
                        // 字段解密
                        if(!"".equals((String) row.get(fieldName)) && (String) row.get(fieldName) != null){
                            if ("1".equals(cjxtMbglPz.getSfjm())) {
                                if(((String) row.get(fieldName)).contains("_sxby")){
                                    encryptedValue = sjjm((String) row.get(fieldName));
                                }else {
                                    encryptedValue = (String) row.get(fieldName);
                                }
                            }
                            if (!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx() != null) {
                                if ("1".equals(cjxtMbglPz.getDbJylx())) {
                                    if(((String) row.get(fieldName)).contains("_sxby")){
                                        encryptedTmValue = desensitize(sjjm((String) row.get(fieldName)));
                                    }else {
                                        encryptedTmValue = desensitize((String) row.get(fieldName)); // 脱敏
                                    }
                                } else if ("2".equals(cjxtMbglPz.getDbJylx())) {
                                    if(((String) row.get(fieldName)).contains("_sxby")){
                                        encryptedTmValue = maskPhone(sjjm((String) row.get(fieldName))); // 手机号脱敏
                                    }else {
                                        encryptedTmValue = maskPhone((String) row.get(fieldName)); // 手机号脱敏
                                    }
                                }
                            }
                        }
                        if (!"".equals(encryptedValue)) {
                            if (hasUpdate) {
                                updateSql.append(", ");
                            }
                            if ("1".equals(cjxtMbglPz.getSfjm())) {
                                updateSql.append(fieldName).append(" = ?");
                                updateValues.add(encryptedValue);
                                hasUpdate = true;
                            }
                            if (!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx() != null) {
                                updateSql.append(", ");
                                updateSql.append(fieldName).append("_jmzd").append(" = ?");
                                if(!"".equals(encryptedTmValue)){
                                    updateValues.add(encryptedTmValue);
                                }
                                hasUpdate = true;
                            }
                        }
                    }
                    if (hasUpdate) {
                        String idValue = (String) row.get("id");
                        updateSql.append(" WHERE id = ? AND del_flag = '0'");
                        updateValues.add(idValue);
                        jdbcTemplate.update(updateSql.toString(), updateValues.toArray());
                    }
                }
            }
        }
        // 数据Type 2 数据脱敏
//        if("2".equals(sjType)){
//            if(cjxtMbgl!=null){
//                String dataSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' ORDER BY create_time DESC;";
//                resultList = jdbcTemplate.queryForList(dataSql);
//
//                for (Map<String, Object> row : resultList) {
//                    List<Object> updateValues = new ArrayList<>();
//                    StringBuilder updateSql = new StringBuilder("UPDATE " + cjxtMbgl.getBm() + " SET ");
//
//                    boolean hasUpdate = false;
//
//                    // 遍历字段配置
//                    for (CjxtMbglPz cjxtMbglPz : mbglPzList) {
//                        String fieldName = cjxtMbglPz.getDbFieldName();
//                        String encryptedValue = "";
//                        // 字段加密
//                        if (!"".equals(cjxtMbglPz.getDbJylx()) && cjxtMbglPz.getDbJylx() != null) {
//                            if(((String) row.get(fieldName)).contains("_sxby")){
//                                encryptedValue = sjjm((String) row.get(fieldName));
//                            }else {
//                                encryptedValue = (String) row.get(fieldName);
//                            }
//                        }
//                        if ("1".equals(cjxtMbglPz.getDbJylx())) {
//                            encryptedValue = desensitize((String) row.get(fieldName)); // 脱敏
//                        } else if ("2".equals(cjxtMbglPz.getDbJylx())) {
//                            encryptedValue = maskPhone((String) row.get(fieldName)); // 手机号脱敏
//                        }
//                        if (!"".equals(encryptedValue)) {
//                            if (hasUpdate) {
//                                updateSql.append(", ");
//                            }
//                            if ("1".equals(cjxtMbglPz.getSfjm())) {
//                                updateSql.append(fieldName).append(" = ?");
//                            } else {
//                                updateSql.append(", ");
//                                updateSql.append(fieldName).append("_jmzd").append(" = ?");
//                            }
//                            updateValues.add(encryptedValue);
//                            hasUpdate = true;
//                        }
//                    }
//                    if (hasUpdate) {
//                        String idValue = (String) row.get("id");
//                        updateSql.append(" WHERE id = ? AND del_flag = '0'");
//                        updateValues.add(idValue);
//                        jdbcTemplate.update(updateSql.toString(), updateValues.toArray());
//                    }
//                }
//            }
//        }
        return Result.OK(resultList);
    }

}
