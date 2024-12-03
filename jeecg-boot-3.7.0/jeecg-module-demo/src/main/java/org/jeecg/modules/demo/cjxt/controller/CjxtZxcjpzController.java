package org.jeecg.modules.demo.cjxt.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
import org.jeecg.modules.demo.cjxt.entity.*;
import org.jeecg.modules.demo.cjxt.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.utils.AesTestOne;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.jeecg.modules.demo.cjxt.utils.Desensitization.desensitize;
import static org.jeecg.modules.demo.cjxt.utils.Desensitization.maskPhone;
import static org.jeecg.modules.demo.cjxt.utils.RsaUtil.decryptRes;

/**
 * @Description: 专项采集配置
 * @Author: jeecg-boot
 * @Date:   2024-09-11
 * @Version: V1.0
 */
@Api(tags="专项采集配置")
@RestController
@RequestMapping("/cjxt/cjxtZxcjpz")
@Slf4j
public class CjxtZxcjpzController extends JeecgController<CjxtZxcjpz, ICjxtZxcjpzService> {
	@Autowired
	private ICjxtZxcjpzService cjxtZxcjpzService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	@Autowired
	private ICjxtMbglPzService cjxtMbglPzService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	@Autowired
	private ICjxtPjwgqxService cjxtPjwgqxService;
	@Autowired
	private ICjxtXtcsService cjxtXtcsService;
	//minio图片服务器
	@Value(value="${jeecg.minio.minio_url}")
	private String minioUrl;
	@Value(value="${jeecg.minio.bucketName}")
	private String bucketName;

	 /**
	  * 地址数据状态
	  * @param mbCode
	  * @return
	  */
	 @ApiOperation(value="专项采集配置-地址数据状态", notes="专项采集配置-地址数据状态")
	 @GetMapping(value = "/addressIdHasValue")
	 public Result<Map<String, Object>> addressIdHasValue(@RequestParam(value = "addressId", required = true) String addressId,
														  @RequestParam(value = "mbCode", required = true) String mbCode) {
		 Map<String, Object> result = new HashMap<>();
		 if(addressId!=null && !"".equals(addressId) && mbCode!=null && !"".equals(mbCode)){
			 CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
			 if(cjxtMbgl!=null){
				 String sql = "SELECT * FROM "+ cjxtMbgl.getBm() +" WHERE del_flag = '0' AND address_id = '" + addressId + "' ; ";
				 List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
				 if(resultList.size()>0){
					 result.put("status",true);
				 }else {
					 result.put("status",false);
				 }
			 }
		 }
		 return Result.OK(result);
	 }

	 /**
	  * 列表/详情列表展示字段
	  * @param mbCode
	  * @return
	  */
	 @ApiOperation(value="专项采集配置-列表/详情列表展示字段", notes="专项采集配置-列表/详情列表展示字段")
	 @GetMapping(value = "/appListZd")
	 public Result<Map<String, Object>> appListZd(@RequestParam(value = "mbCode", required = true) String mbCode,
											      @RequestParam(value = "isList", required = true) String isList) {
		 Map<String, Object> result = new HashMap<>();
		 if(isList!=null && !"".equals(isList) && mbCode!=null && !"".equals(mbCode)){
			 String fieldName = "lbxszd";
			 if("0".equals(isList)){
				 fieldName = "xqlbxszd";
			 }
			 String sql = "SELECT db_field_txt,db_field_name FROM cjxt_mbgl_pz WHERE del_flag = '0' AND mbgl_mbbh = '" + mbCode + "' AND " + fieldName + " = '1' ORDER BY order_num ASC ";
			 List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
			 result.put("data",resultList);
		 }
		 return Result.OK(result);
	 }

	 @AutoLog(value = "专项采集配置-APP列表显示")
	 @ApiOperation(value = "专项采集配置-APP列表显示", notes = "专项采集配置-APP列表显示")
	 @PostMapping(value = "/frontjyListCp")
	 public Result<Map<String, Object>> frontjyListCp(
			 @RequestParam(required = false, name = "userId") String userId,
			 @RequestParam(required = false, name = "mbCode") String mbCode,
			 @RequestParam(required = false, name = "addressId") String addressId,
			 @RequestParam(required = false, name = "search") String search,
			 @RequestParam(required = false, name = "searchLd") String searchLd,
			 @RequestParam(required = false, name = "addressTask") String addressTask,
			 @RequestParam(required = false, name = "isList") String isList,
			 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			 @RequestBody(required = false) List<CjxtMbglPz> pzList,
			 HttpServletRequest req) throws UnsupportedEncodingException {
//        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		 Map<String, Object> result = new HashMap<>();
		 List<Map<String, Object>> records = new ArrayList<>();
		 if (userId != null && mbCode != null) {
			 SysUser sysUser = sysUserService.getById(userId);
			 List<SysDepart> sysDepartsList = sysDepartService.queryUserDeparts(userId);
			 List<CjxtPjwgqx> pjwgqxList = new ArrayList<>();//片警民警网格权限
			 String dataSql = null;
			 String countSql = null;
			 List<Map<String, Object>> resultList = new ArrayList<>();//模版数据
			 List<Map<String, Object>> zdList = new ArrayList<>();//列表展示字段
			 List<Map<String, Object>> dictTextList = new ArrayList<>();//列表展示字典
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
				 if("".equals(addressTask) || addressTask == null || !"1".equals(addressTask) || !"".equals(orgCode)){
					 orgCodeQuery.append(" AND t.sys_org_code in (").append(orgCode).append(")");
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
								 pzListAddQuery.append(" OR ");
							 }
							 pzListAddQuery.append("t." + cjxtMbglPz.getDbFieldName()).append(" LIKE '%").append(dataV).append("%'");
						 }
					 }
					 if (isK == true) {
						 pzListAddQuery.append(")");
					 }
				 }

				 String bm = cjxtMbgl.getBm();
				 StringBuilder rymbOrder = new StringBuilder();
				 if (!"".equals(addressId) && addressId != null && "RY001".equals(mbCode)) {
					 rymbOrder.append(", CAST(t.yhzgx AS UNSIGNED)");
				 }
				 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' "  + orgCodeQuery + additionalQuery + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;

				 if (pzList != null && pzList.size() > 0) {
					 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' "  + orgCodeQuery + pzListAddQuery + additionalQuery + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
				 }

				 if (searchLd != null && !"".equals(searchLd)) {
					 searchLd = java.net.URLDecoder.decode(searchLd, "utf8");
					 searchLd = searchLd.replace(",", "%");
					 searchLd = "%" + searchLd + "%";
				 }

				 if (searchLd != null && !"".equals(searchLd)) {
					 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' "  + orgCodeQuery + additionalQuery + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
				 }
				 if (searchLd != null && !"".equals(searchLd) && pzList != null && pzList.size() > 0) {
					 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' "  + orgCodeQuery + pzListAddQuery + additionalQuery + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
				 }
				 if (addressId != null && !"".equals(addressId) && !addressId.isEmpty()) {
					 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + additionalQuery  + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address  " + rymbOrder + " ASC ";
					 if (pzList != null && pzList.size() > 0) {
						 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery  + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC ";
					 }
					 if (searchLd != null && !"".equals(searchLd)) {
						 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' AND t.address_id = '" + addressId + "'"  + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC ";
					 }
					 if (searchLd != null && !"".equals(searchLd) && pzList != null && pzList.size() > 0) {
						 dataSql = "SELECT t.* FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' AND t.address_id = '" + addressId + "'" + pzListAddQuery  + " ORDER BY cst.address_code_mz, cst.detail_lm, cst.detail_lhm, cst.detail_mc, cst.dz1_ld, cst.dz1_dy, cst.dz1_s, cst.dz2_zm, cst.dz2_hm, cst.dz3_ldm, cst.dz3_sm, cst.dz5_p, cst.dz5_h, cst.dz5_s, cst.dz6_s, cst.detail_address " + rymbOrder + " ASC ";
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
					 if(isList!=null && !"".equals(isList) && mbCode!=null && !"".equals(mbCode)){
						 String fieldName = "lbxszd";
						 if("0".equals(isList)){
							 fieldName = "xqlbxszd";
						 }
						 String sql = "SELECT db_field_txt,db_field_name,field_show_type,dict_field,db_jylx,sfjm FROM cjxt_mbgl_pz WHERE del_flag = '0' AND mbgl_mbbh = '" + mbCode + "' AND " + fieldName + " = '1' ORDER BY order_num ASC ";
						 zdList = jdbcTemplate.queryForList(sql);
					 }

					 // 加密/脱敏字段返回处理
					 List<CjxtMbglPz> mbglPzs = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().last(" AND ((is_title = '0' AND sfjm = '1' AND mbgl_mbbh = '"+mbCode+"') OR (db_jylx IS NOT NULL AND db_jylx <> '')) AND mbgl_mbbh = '"+mbCode+"' ORDER BY order_num ASC"));
					 if(mbglPzs.size()>0){
						 for(CjxtMbglPz mbglPz: mbglPzs){
							 Object value = row.get(mbglPz.getDbFieldName());
							 if(!"".equals(value) && value!=null){
								 if(value.toString().contains("_sxby")){
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
										 String sfzh = mbglPz.getDataValue();
										 if(!"".equals(sfzh) && sfzh!=null){
											 if(value.toString().contains("_sxby")){
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
										 if(!"".equals(mbglPz.getDataValue()) && mbglPz.getDataValue()!=null){
											 String sfzh = desensitize(mbglPz.getDataValue());
											 row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
										 }else {
											 row.put(mbglPz.getDbFieldName()+"_jmzd", "");
										 }
									 }
								 }
								 //手机号
								 if("2".equals(mbglPz.getDbJylx())){
									 if("1".equals(mbglPz.getSfjm())){
										 String phone = mbglPz.getDataValue();
										 if(!"".equals(phone) && phone!=null){
											 if(value.toString().contains("_sxby")){
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
										 if(!"".equals(mbglPz.getDataValue()) && mbglPz.getDataValue()!=null){
											 String phone = maskPhone(mbglPz.getDataValue());
											 row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
										 }else {
											 row.put(mbglPz.getDbFieldName()+"_jmzd", "");
										 }
									 }
								 }
							 }
						 }
					 }

					 Map<String, Object> resultMap = new LinkedHashMap<>();
					 resultMap.put("title", "地址");
					 resultMap.put("titleV", addressName);
					 if(zdList.size()>0){
						 int index = 1;
						 for (Map<String, Object> zd : zdList) {
							 //字段名称
							 String fieldName = (String) zd.get("db_field_name");
							 //字段中文名称
							 String fieldTxt = (String) zd.get("db_field_txt");
							 //控件类型
							 String fieldShowType = (String) zd.get("field_show_type");
							 //字典值
							 String dictField = (String) zd.get("dict_field");
							 //是否加密
							 String sfjm = (String) zd.get("sfjm");
							 //校验类型
							 String dbJylx = (String) zd.get("db_jylx");
							 //根据字段获取数据
							 Object fieldValue = row.get(fieldName);
							 if(fieldValue==null || "null".equals(fieldValue) || "undefined".equals(fieldValue) || "".equals(fieldValue)){
								 fieldValue = "";
							 }

							 // 数据解密脱敏
							 if(!"".equals(fieldValue) && fieldValue!=null){
								 if(fieldValue.toString().contains("_sxby")){
									 String dataV = sjjm(fieldValue.toString());
									 row.put(fieldName, dataV);
									 fieldValue = dataV;
								 }else {
									 row.put(fieldName, fieldValue);
								 }
							 }else {
								 row.put(fieldName, "");
							 }

							 if(!"".equals(dbJylx) && dbJylx!=null){
								 //身份证
								 if("1".equals(dbJylx)){
									 if("1".equals(sfjm)){
										 String sfzh = (String) fieldValue;
										 if(!"".equals(sfzh) && sfzh!=null){
											 if(fieldValue.toString().contains("_sxby")){
												 String sfzhTm = desensitize(sjjm(sfzh));
												 row.put(fieldName+"_jmzd", sfzhTm);
												 fieldValue = sfzhTm;
											 }else {
												 String sfzhTm = desensitize(sfzh);
												 row.put(fieldName+"_jmzd", sfzhTm);
												 fieldValue = sfzhTm;
											 }
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }else {
										 if(!"".equals(fieldValue) && fieldValue!=null){
											 String sfzh = desensitize(fieldValue.toString());
											 row.put(fieldName+"_jmzd", sfzh);
											 fieldValue = sfzh;
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }
								 }
								 //手机号
								 if("2".equals(dbJylx)){
									 if("1".equals(sfjm)){
										 String phone = (String) fieldValue;
										 if(!"".equals(phone) && phone!=null){
											 if(fieldValue.toString().contains("_sxby")){
												 String phoneTm = maskPhone(sjjm(phone));
												 row.put(fieldName+"_jmzd", phoneTm);
												 fieldValue = phoneTm;
											 }else {
												 String phoneTm = maskPhone(phone);
												 row.put(fieldName+"_jmzd", phoneTm);
												 fieldValue = phoneTm;
											 }
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }else {
										 if(!"".equals(fieldValue) && fieldValue!=null){
											 String phone = maskPhone(fieldValue.toString());
											 row.put(fieldName+"_jmzd", phone);
											 fieldValue = phone;
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }
								 }
							 }

							 resultMap.put("title" + index, fieldTxt);
							 if("list".equals(fieldShowType) && !"".equals(dictField) && dictField!=null && !"".equals(fieldValue) && fieldValue != null){
								 String dictSql = "SELECT sdi.item_text FROM sys_dict sd\n" +
										 "INNER JOIN sys_dict_item sdi\n" +
										 "ON sd.id = sdi.dict_id\n" +
										 "WHERE sd.dict_code = '"+dictField+"' AND sdi.item_value = '"+fieldValue+"'";
								 dictTextList = jdbcTemplate.queryForList(dictSql);
								 if(dictTextList.size()>0){
									 Map<String,Object> dictRow = dictTextList.get(0);
									 resultMap.put("title" + index + "V", dictRow.get("item_text"));
								 }else {
									 resultMap.put("title" + index + "V", fieldValue);
								 }
							 } else if (("datetime".equals(fieldShowType) || "date".equals(fieldShowType)) && !"".equals(fieldValue) && fieldValue!=null) {
								 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
								 if("date".equals(fieldShowType)){
									 formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
								 }
								 LocalDateTime dateTime = (LocalDateTime) row.get(fieldName);
								 String date = dateTime.format(formatter);
								 resultMap.put(fieldTxt, fieldTxt + "：" + date);
							 } else {
								 resultMap.put("title" + index + "V", fieldValue);
							 }
							 index++;
						 }
					 }

					 Map<String, Object> dataMap = new HashMap<>(row);
					 dataMap.put("XXXXXXXXXXXX", resultMap);
					 records.add(dataMap);
				 }

				 // 获取总条数的 SQL 查询
				 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' "  + orgCodeQuery + additionalQuery;
				 if (pzList != null && pzList.size() > 0) {
					 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' "  + orgCodeQuery + pzListAddQuery + additionalQuery;
				 }
				 if (searchLd != null && !"".equals(searchLd)) {
					 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' "  + orgCodeQuery + additionalQuery;
				 }
				 if (searchLd != null && pzList != null && pzList.size() > 0) {
					 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' "  + orgCodeQuery + pzListAddQuery + additionalQuery;
				 }
				 if (addressId != null && !"".equals(addressId) && !addressId.isEmpty()) {
					 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + additionalQuery ;
					 if (pzList != null && pzList.size() > 0) {
						 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery ;
					 }
					 if (searchLd != null && !"".equals(searchLd)) {
						 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' AND t.address_id = '" + addressId + "'" ;
					 }
					 if (searchLd != null && !"".equals(searchLd) && pzList != null && pzList.size() > 0) {
						 countSql = "SELECT COUNT(*) FROM " + bm + " t INNER JOIN cjxt_standard_address cst ON cst.id = t.address_id WHERE t.del_flag = '0' AND t.address like '" + searchLd + "' AND t.address_id = '" + addressId + "'" + pzListAddQuery + additionalQuery ;
					 }
				 }
				 // 执行查询并获取总条数
				 int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
				 // 计算总页数
				 int totalPages = (int) Math.ceil((double) totalCount / pageSize);
				 // 将总页数添加到结果中
				 result.put("totalPages", totalPages);
			 }
			 result.put("records", records);
		 }
		 return Result.OK(result);
	 }

	 @AutoLog(value = "专项采集配置-APP详情列表展示")
	 @ApiOperation(value = "专项采集配置-APP详情列表展示", notes = "专项采集配置-APP详情列表展示")
	 @PostMapping(value = "/frontjyXqCp")
	 public Result<Map<String, Object>> xq(
			 @RequestParam(required = false, name = "id") String id,
			 @RequestParam(required = false, name = "mbCode") String mbCode,
			 @RequestParam(required = false, name = "isList") String isList,
			 HttpServletRequest req) throws UnsupportedEncodingException {
		 Map<String, Object> result = new HashMap<>();
		 Map<String, Object> resultMap = new LinkedHashMap<>();
		 List<Map<String, Object>> records = new ArrayList<>();
		 if (id != null && mbCode != null) {
			 String dataSql = null;
			 List<Map<String, Object>> resultList = new ArrayList<>();//模版数据
			 List<Map<String, Object>> zdList = new ArrayList<>();//列表展示字段
			 List<Map<String, Object>> dictTextList = new ArrayList<>();//列表展示字典数据
			 //查询模版
			 CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
			 if (cjxtMbgl != null) {
				 dataSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' AND id = '" + id + "'";
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

					 // 加密/脱敏字段返回处理
					 List<CjxtMbglPz> mbglPzs = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().last(" AND ((is_title = '0' AND sfjm = '1' AND mbgl_mbbh = '"+mbCode+"') OR (db_jylx IS NOT NULL AND db_jylx <> '')) AND mbgl_mbbh = '"+mbCode+"' ORDER BY order_num ASC"));
					 if(mbglPzs.size()>0){
						 for(CjxtMbglPz mbglPz: mbglPzs){

							 Object value = row.get(mbglPz.getDbFieldName());
							 if(!"".equals(value) && value!=null){
								 if(value.toString().contains("_sxby")){
									 String dataV = sjjm(value.toString());
									 row.put(mbglPz.getDbFieldName(), dataV);
								 }else {
									 row.put(mbglPz.getDbFieldName(), value);
								 }
							 }

							 if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
								 //身份证
								 if("1".equals(mbglPz.getDbJylx())){
									 if("1".equals(mbglPz.getSfjm())){
										 String sfzh = mbglPz.getDataValue();
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
										 if(!"".equals(mbglPz.getDataValue()) && mbglPz.getDataValue()!=null){
											 String sfzh = desensitize(mbglPz.getDataValue());
											 row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
										 }else {
											 row.put(mbglPz.getDbFieldName()+"_jmzd", "");
										 }
									 }
								 }
								 //手机号
								 if("2".equals(mbglPz.getDbJylx())){
									 if("1".equals(mbglPz.getSfjm())){
										 String phone = mbglPz.getDataValue();
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
										 if(!"".equals(mbglPz.getDataValue()) && mbglPz.getDataValue()!=null){
											 String phone = maskPhone(mbglPz.getDataValue());
											 row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
										 }else {
											 row.put(mbglPz.getDbFieldName()+"_jmzd", "");
										 }
									 }
								 }
							 }
						 }
					 }

					 row.put("address", addressName);
					 if(isList!=null && !"".equals(isList) && mbCode!=null && !"".equals(mbCode)){
						 String fieldName = "lbxszd";
						 if("0".equals(isList)){
							 fieldName = "xqlbxszd";
						 }
						 String sql = "SELECT db_field_txt,db_field_name,field_show_type,dict_field,db_jylx,sfjm FROM cjxt_mbgl_pz WHERE del_flag = '0' AND mbgl_mbbh = '" + mbCode + "' AND " + fieldName + " = '1' ORDER BY order_num ASC ";
						 zdList = jdbcTemplate.queryForList(sql);
					 }
					 if(zdList.size()>0){
						 for (Map<String, Object> zd : zdList) {
							 //字段名称
							 String fieldName = (String) zd.get("db_field_name");
							 //字段中文名称
							 String fieldTxt = (String) zd.get("db_field_txt");
							 //控件类型
							 String fieldShowType = (String) zd.get("field_show_type");
							 //字典值
							 String dictField = (String) zd.get("dict_field");
							 //是否加密
							 String sfjm = (String) zd.get("sfjm");
							 //校验类型
							 String dbJylx = (String) zd.get("db_jylx");
							 //根据字段获取数据
							 Object fieldValue = row.get(fieldName);
							 if(fieldValue==null || "null".equals(fieldValue) || "undefined".equals(fieldValue) || "".equals(fieldValue)){
								 fieldValue = "";
							 }

							 // 数据解密脱敏
							 if(!"".equals(fieldValue) && fieldValue!=null){
								 if(fieldValue.toString().contains("_sxby")){
									 String dataV = sjjm(fieldValue.toString());
									 row.put(fieldName, dataV);
									 fieldValue = dataV;
								 }else {
									 row.put(fieldName, fieldValue);
								 }
							 }else {
								 row.put(fieldName, "");
							 }

							 if(!"".equals(dbJylx) && dbJylx!=null){
								 //身份证
								 if("1".equals(dbJylx)){
									 if("1".equals(sfjm)){
										 String sfzh = (String) fieldValue;
										 if(!"".equals(sfzh) && sfzh!=null){
											 if(fieldValue.toString().contains("_sxby")){
												 String sfzhTm = desensitize(sjjm(sfzh));
												 row.put(fieldName+"_jmzd", sfzhTm);
												 fieldValue = sfzhTm;
											 }else {
												 String sfzhTm = desensitize(sfzh);
												 row.put(fieldName+"_jmzd", sfzhTm);
												 fieldValue = sfzhTm;
											 }
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }else {
										 if(!"".equals(fieldValue) && fieldValue!=null){
											 String sfzh = desensitize(fieldValue.toString());
											 row.put(fieldName+"_jmzd", sfzh);
											 fieldValue = sfzh;
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }
								 }
								 //手机号
								 if("2".equals(dbJylx)){
									 if("1".equals(sfjm)){
										 String phone = (String) fieldValue;
										 if(!"".equals(phone) && phone!=null){
											 if(fieldValue.toString().contains("_sxby")){
												 String phoneTm = maskPhone(sjjm(phone));
												 row.put(fieldName+"_jmzd", phoneTm);
												 fieldValue = phoneTm;
											 }else {
												 String phoneTm = maskPhone(phone);
												 row.put(fieldName+"_jmzd", phoneTm);
												 fieldValue = phoneTm;
											 }
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }else {
										 if(!"".equals(fieldValue) && fieldValue!=null){
											 String phone = maskPhone(fieldValue.toString());
											 row.put(fieldName+"_jmzd", phone);
											 fieldValue = phone;
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }
								 }
							 }

							 if("list".equals(fieldShowType) && !"".equals(dictField) && dictField!=null && !"".equals(fieldValue) && fieldValue!=null){
								 String dictSql = "SELECT sdi.item_text FROM sys_dict sd\n" +
										 "INNER JOIN sys_dict_item sdi\n" +
										 "ON sd.id = sdi.dict_id\n" +
										 "WHERE sd.dict_code = '"+dictField+"' AND sdi.item_value = '"+fieldValue+"'";
								 dictTextList = jdbcTemplate.queryForList(dictSql);
								 if(dictTextList.size()>0){
									 Map<String,Object> dictRow = dictTextList.get(0);
									 resultMap.put(fieldTxt, fieldTxt + "：" + dictRow.get("item_text"));
								 }else {
									 resultMap.put(fieldTxt, fieldTxt + "：" + fieldValue);
								 }
							 } else if (("datetime".equals(fieldShowType) || "date".equals(fieldShowType)) && !"".equals(fieldValue) && fieldValue!=null) {
								 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
								 if("date".equals(fieldShowType)){
									 formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
								 }
								 LocalDateTime dateTime = (LocalDateTime) row.get(fieldName);
								 String date = dateTime.format(formatter);
								 resultMap.put(fieldTxt, fieldTxt + "：" + date);
							 } else {
								 resultMap.put(fieldTxt, fieldTxt + "：" + fieldValue);
							 }
						 }
					 }
					 List<Object> listData = new ArrayList<>(resultMap.values());
					 Map<String, Object> dataMap = new HashMap<>();
					 dataMap.put("data", row);
					 dataMap.put("listData", listData);
					 records.add(dataMap);
				 }
			 }
			 result.put("records", records);
		 }
		 return Result.OK(result);
	 }

	 @AutoLog(value = "专项采集配置-APP地址ID查询详情列表")
	 @ApiOperation(value = "专项采集配置-APP地址ID查询详情列表", notes = "专项采集配置-APP地址ID查询详情列表")
	 @PostMapping(value = "/frontjyAddressIdXq")
	 public Result<Map<String, Object>> frontjyAddressIdXq(
			 @RequestParam(required = false, name = "addressId") String addressId,
			 @RequestParam(required = false, name = "mbCode") String mbCode,
			 @RequestParam(required = false, name = "isList") String isList,
			 HttpServletRequest req) throws UnsupportedEncodingException {
		 Map<String, Object> result = new HashMap<>();
		 Map<String, Object> resultMap = new LinkedHashMap<>();
		 List<Map<String, Object>> records = new ArrayList<>();
		 Map<String, Object> cjryXx = new HashMap<>();
		 if (addressId != null && mbCode != null) {
			 String dataSql = null;
			 List<Map<String, Object>> resultList = new ArrayList<>();//模版数据
			 List<Map<String, Object>> zdList = new ArrayList<>();//列表展示字段
			 List<Map<String, Object>> dictTextList = new ArrayList<>();//列表展示字典数据
			 //查询模版
			 CjxtMbgl cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, mbCode).orderByDesc(CjxtMbgl::getCreateTime).last("LIMIT 1"));
			 if (cjxtMbgl != null) {
				 dataSql = "SELECT t.* FROM " + cjxtMbgl.getBm() + " t WHERE t.del_flag = '0' AND address_id = '" + addressId + "'";
				 resultList = jdbcTemplate.queryForList(dataSql);
				 if(resultList.size()>0){
					 Map<String, Object> row = resultList.get(0);
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
					 }
					 if(cjryXx.size()>0){
						 result.put("wgyUser", cjryXx);
					 }else {
						 result.put("wgyUser", null);
					 }
				 }else {
					 result.put("wgyUser", null);
				 }
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

					 // 加密/脱敏字段返回处理
					 List<CjxtMbglPz> mbglPzs = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().last(" AND ((is_title = '0' AND sfjm = '1' AND mbgl_mbbh = '"+mbCode+"') OR (db_jylx IS NOT NULL AND db_jylx <> '')) AND mbgl_mbbh = '"+mbCode+"' ORDER BY order_num ASC"));
					 if(mbglPzs.size()>0){
						 for(CjxtMbglPz mbglPz: mbglPzs){

							 Object value = row.get(mbglPz.getDbFieldName());
							 if(!"".equals(value) && value!=null){
								 if(value.toString().contains("_sxby")){
									 String dataV = sjjm(value.toString());
									 row.put(mbglPz.getDbFieldName(), dataV);
								 }else {
									 row.put(mbglPz.getDbFieldName(), value);
								 }
							 }

							 if(!"".equals(mbglPz.getDbJylx()) && mbglPz.getDbJylx()!=null){
								 //身份证
								 if("1".equals(mbglPz.getDbJylx())){
									 if("1".equals(mbglPz.getSfjm())){
										 String sfzh = mbglPz.getDataValue();
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
										 if(!"".equals(mbglPz.getDataValue()) && mbglPz.getDataValue()!=null){
											 String sfzh = desensitize(mbglPz.getDataValue());
											 row.put(mbglPz.getDbFieldName()+"_jmzd", sfzh);
										 }else {
											 row.put(mbglPz.getDbFieldName()+"_jmzd", "");
										 }
									 }
								 }
								 //手机号
								 if("2".equals(mbglPz.getDbJylx())){
									 if("1".equals(mbglPz.getSfjm())){
										 String phone = mbglPz.getDataValue();
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
										 if(!"".equals(mbglPz.getDataValue()) && mbglPz.getDataValue()!=null){
											 String phone = maskPhone(mbglPz.getDataValue());
											 row.put(mbglPz.getDbFieldName()+"_jmzd", phone);
										 }else {
											 row.put(mbglPz.getDbFieldName()+"_jmzd", "");
										 }
									 }
								 }
							 }
						 }
					 }

					 row.put("address", addressName);
					 if(isList!=null && !"".equals(isList) && mbCode!=null && !"".equals(mbCode)){
						 String fieldName = "lbxszd";
						 if("0".equals(isList)){
							 fieldName = "xqlbxszd";
						 }
						 String sql = "SELECT db_field_txt,db_field_name,field_show_type,dict_field,db_jylx,sfjm FROM cjxt_mbgl_pz WHERE del_flag = '0' AND mbgl_mbbh = '" + mbCode + "' AND " + fieldName + " = '1' ORDER BY order_num ASC ";
						 zdList = jdbcTemplate.queryForList(sql);
					 }
					 if(zdList.size()>0){
						 for (Map<String, Object> zd : zdList) {
							 //字段名称
							 String fieldName = (String) zd.get("db_field_name");
							 //字段中文名称
							 String fieldTxt = (String) zd.get("db_field_txt");
							 //控件类型
							 String fieldShowType = (String) zd.get("field_show_type");
							 //字典值
							 String dictField = (String) zd.get("dict_field");
							 //是否加密
							 String sfjm = (String) zd.get("sfjm");
							 //校验类型
							 String dbJylx = (String) zd.get("db_jylx");
							 Object fieldValue = row.get(fieldName);
							 if(fieldValue==null || "null".equals(fieldValue) || "undefined".equals(fieldValue) || "".equals(fieldValue)){
								 fieldValue = "";
							 }

							 // 数据解密脱敏
							 if(!"".equals(fieldValue) && fieldValue!=null){
								 if(fieldValue.toString().contains("_sxby")){
									 String dataV = sjjm(fieldValue.toString());
									 row.put(fieldName, dataV);
									 fieldValue = dataV;
								 }else {
									 row.put(fieldName, fieldValue);
								 }
							 }else {
								 row.put(fieldName, "");
							 }

							 if(!"".equals(dbJylx) && dbJylx!=null){
								 //身份证
								 if("1".equals(dbJylx)){
									 if("1".equals(sfjm)){
										 String sfzh = (String) fieldValue;
										 if(!"".equals(sfzh) && sfzh!=null){
											 if(fieldValue.toString().contains("_sxby")){
												 String sfzhTm = desensitize(sjjm(sfzh));
												 row.put(fieldName+"_jmzd", sfzhTm);
												 fieldValue = sfzhTm;
											 }else {
												 String sfzhTm = desensitize(sfzh);
												 row.put(fieldName+"_jmzd", sfzhTm);
												 fieldValue = sfzhTm;
											 }
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }else {
										 if(!"".equals(fieldValue) && fieldValue!=null){
											 String sfzh = desensitize(fieldValue.toString());
											 row.put(fieldName+"_jmzd", sfzh);
											 fieldValue = sfzh;
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }
								 }
								 //手机号
								 if("2".equals(dbJylx)){
									 if("1".equals(sfjm)){
										 String phone = (String) fieldValue;
										 if(!"".equals(phone) && phone!=null){
											 if(fieldValue.toString().contains("_sxby")){
												 String phoneTm = maskPhone(sjjm(phone));
												 row.put(fieldName+"_jmzd", phoneTm);
												 fieldValue = phoneTm;
											 }else {
												 String phoneTm = maskPhone(phone);
												 row.put(fieldName+"_jmzd", phoneTm);
												 fieldValue = phoneTm;
											 }
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }else {
										 if(!"".equals(fieldValue) && fieldValue!=null){
											 String phone = maskPhone(fieldValue.toString());
											 row.put(fieldName+"_jmzd", phone);
											 fieldValue = phone;
										 }else {
											 row.put(fieldName+"_jmzd", "");
										 }
									 }
								 }
							 }

							 if("list".equals(fieldShowType) && !"".equals(dictField) && dictField!=null && !"".equals(fieldValue) && fieldValue!=null){
								 String dictSql = "SELECT sdi.item_text FROM sys_dict sd\n" +
										 "INNER JOIN sys_dict_item sdi\n" +
										 "ON sd.id = sdi.dict_id\n" +
										 "WHERE sd.dict_code = '"+dictField+"' AND sdi.item_value = '"+fieldValue+"'";
								 dictTextList = jdbcTemplate.queryForList(dictSql);
								 if(dictTextList.size()>0){
									 Map<String,Object> dictRow = dictTextList.get(0);
									 resultMap.put(fieldTxt, fieldTxt + "：" + dictRow.get("item_text"));
								 }else {
									 resultMap.put(fieldTxt, fieldTxt + "：" + fieldValue);
								 }
							 } else if (("datetime".equals(fieldShowType) || "date".equals(fieldShowType)) && !"".equals(fieldValue) && fieldValue!=null) {
								 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
								 if("date".equals(fieldShowType)){
									 formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
								 }
								 LocalDateTime dateTime = (LocalDateTime) row.get(fieldName);
								 String date = dateTime.format(formatter);
								 resultMap.put(fieldTxt, fieldTxt + "：" + date);
							 } else {
								 resultMap.put(fieldTxt, fieldTxt + "：" + fieldValue);
							 }
						 }
					 }
					 List<Object> listData = new ArrayList<>(resultMap.values());
					 Map<String, Object> dataMap = new HashMap<>();
					 dataMap.put("data", row);
					 dataMap.put("listData", listData);
					 records.add(dataMap);
				 }
			 }
			 result.put("records", records);
		 }
		 return Result.OK(result);
	 }

	/**
	 * 分页列表查询
	 *
	 * @param cjxtZxcjpz
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "专项采集配置-分页列表查询")
	@ApiOperation(value="专项采集配置-分页列表查询", notes="专项采集配置-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtZxcjpz>> queryPageList(CjxtZxcjpz cjxtZxcjpz,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtZxcjpz> queryWrapper = QueryGenerator.initQueryWrapper(cjxtZxcjpz, parameterMap);
		queryWrapper.orderByAsc("mb_sort");
		Page<CjxtZxcjpz> page = new Page<CjxtZxcjpz>(pageNo, pageSize);
		IPage<CjxtZxcjpz> pageList = cjxtZxcjpzService.page(page, queryWrapper);
		pageList.getRecords().forEach(cjxtZxcjpzItem ->{
			if(cjxtZxcjpzItem.getIconPath()!=null && !cjxtZxcjpzItem.getIconPath().isEmpty()){
				cjxtZxcjpzItem.setIconPath(minioUrl+"/"+bucketName+"/"+cjxtZxcjpzItem.getIconPath());
			}
		});
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtZxcjpz
	 * @return
	 */
	@AutoLog(value = "专项采集配置-添加")
	@ApiOperation(value="专项采集配置-添加", notes="专项采集配置-添加")
//	@RequiresPermissions("cjxt:cjxt_zxcjpz:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtZxcjpz cjxtZxcjpz) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtZxcjpz.getMbid()!=null && !"".equals(cjxtZxcjpz.getMbid())){
			CjxtZxcjpz zxcjpz = cjxtZxcjpzService.getOne(new LambdaQueryWrapper<CjxtZxcjpz>().eq(CjxtZxcjpz::getMbid,cjxtZxcjpz.getMbid()));
			if(zxcjpz!=null){
				return Result.error("模版信息已存在！");
			}
		}
		if(cjxtZxcjpz.getIconPath()!=null && !"".equals(cjxtZxcjpz.getIconPath()) && cjxtZxcjpz.getIconPath().contains(heardUrl)){
			cjxtZxcjpz.setIconPath(cjxtZxcjpz.getIconPath().replace(heardUrl,""));
		}
		cjxtZxcjpzService.save(cjxtZxcjpz);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param cjxtZxcjpz
	 * @return
	 */
	@AutoLog(value = "专项采集配置-编辑")
	@ApiOperation(value="专项采集配置-编辑", notes="专项采集配置-编辑")
//	@RequiresPermissions("cjxt:cjxt_zxcjpz:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtZxcjpz cjxtZxcjpz) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(!cjxtZxcjpz.getMbidDto().equals(cjxtZxcjpz.getMbid())){
			if(cjxtZxcjpz.getMbid()!=null && !"".equals(cjxtZxcjpz.getMbid())){
				CjxtZxcjpz zxcjpz = cjxtZxcjpzService.getOne(new LambdaQueryWrapper<CjxtZxcjpz>().eq(CjxtZxcjpz::getMbid,cjxtZxcjpz.getMbid()));
				if(zxcjpz!=null){
					return Result.error("模版信息已存在！");
				}
			}
		}
		if(cjxtZxcjpz.getIconPath()!=null && !"".equals(cjxtZxcjpz.getIconPath()) && cjxtZxcjpz.getIconPath().contains(heardUrl)){
			cjxtZxcjpz.setIconPath(cjxtZxcjpz.getIconPath().replace(heardUrl,""));
		}
		cjxtZxcjpzService.updateById(cjxtZxcjpz);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "专项采集配置-通过id删除")
	@ApiOperation(value="专项采集配置-通过id删除", notes="专项采集配置-通过id删除")
	@RequiresPermissions("cjxt:cjxt_zxcjpz:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtZxcjpzService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "专项采集配置-批量删除")
	@ApiOperation(value="专项采集配置-批量删除", notes="专项采集配置-批量删除")
//	@RequiresPermissions("cjxt:cjxt_zxcjpz:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtZxcjpzService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "专项采集配置-通过id查询")
	@ApiOperation(value="专项采集配置-通过id查询", notes="专项采集配置-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtZxcjpz> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtZxcjpz cjxtZxcjpz = cjxtZxcjpzService.getById(id);
		if(cjxtZxcjpz==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtZxcjpz);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtZxcjpz
    */
//    @RequiresPermissions("cjxt:cjxt_zxcjpz:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtZxcjpz cjxtZxcjpz) {
        return super.exportXls(request, cjxtZxcjpz, CjxtZxcjpz.class, "专项采集配置");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_zxcjpz:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtZxcjpz.class);
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
}
