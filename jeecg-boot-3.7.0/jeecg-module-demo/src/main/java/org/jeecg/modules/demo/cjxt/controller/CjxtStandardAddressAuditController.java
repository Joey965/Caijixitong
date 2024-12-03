package org.jeecg.modules.demo.cjxt.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtArea;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddress;
import org.jeecg.modules.demo.cjxt.entity.CjxtStandardAddressAudit;
import org.jeecg.modules.demo.cjxt.service.ICjxtAreaService;
import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressAuditService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.service.ICjxtStandardAddressService;
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
import org.springframework.beans.BeanUtils;
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
 * @Description: 地址审核
 * @Author: jeecg-boot
 * @Date:   2024-08-07
 * @Version: V1.0
 */
@Api(tags="地址审核")
@RestController
@RequestMapping("/cjxt/cjxtStandardAddressAudit")
@Slf4j
public class CjxtStandardAddressAuditController extends JeecgController<CjxtStandardAddressAudit, ICjxtStandardAddressAuditService> {
	@Autowired
	private ICjxtStandardAddressAuditService cjxtStandardAddressAuditService;
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	@Autowired
	private ICjxtAreaService cjxtAreaService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISysUserService sysUserService;
	 @Autowired
	 private JdbcTemplate jdbcTemplate;


	/**
	 * 分页列表查询
	 *
	 * @param cjxtStandardAddressAudit
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "地址审核-分页列表查询")
	@ApiOperation(value="地址审核-分页列表查询", notes="地址审核-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtStandardAddressAudit>> queryPageList(CjxtStandardAddressAudit cjxtStandardAddressAudit,
								   @RequestParam(name = "tjzt",required = false) String tjzt,
								   @RequestParam(name = "search",required = false) String search,
								   @RequestParam(name = "flag",required = false) String flag,
								   @RequestParam(name = "username",required = false) String username,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if(cjxtStandardAddressAudit.getShzt()==null && ("".equals(flag) || flag==null)){
			cjxtStandardAddressAudit.setShzt("1");
		}
		QueryWrapper<CjxtStandardAddressAudit> queryWrapper = QueryGenerator.initQueryWrapper(cjxtStandardAddressAudit, req.getParameterMap());
		if(!"".equals(search) && search!=null){
			queryWrapper.like("address_name",search);
		}
		if("APP".equals(flag)){
			if("1".equals(tjzt)){
				queryWrapper.eq("tjzt","1");
			}
			if("2".equals(tjzt)){
				queryWrapper.eq("tjzt","2");
			}
			queryWrapper.eq("create_by",username);
		}else {
			queryWrapper.eq("tjzt","2");
			queryWrapper.eq("shr_org_code",sysUser.getOrgCode());
		}
		Page<CjxtStandardAddressAudit> page = new Page<CjxtStandardAddressAudit>(pageNo, pageSize);
		IPage<CjxtStandardAddressAudit> pageList = cjxtStandardAddressAuditService.page(page, queryWrapper);
		for(CjxtStandardAddressAudit cjxtStandardAddress: pageList.getRecords()){
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
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param cjxtStandardAddressAudit
	 * @return
	 */
	@AutoLog(value = "地址审核-添加")
	@ApiOperation(value="地址审核-添加", notes="地址审核-添加")
//	@RequiresPermissions("cjxt:cjxt_standard_address_audit:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtStandardAddressAudit cjxtStandardAddressAudit) {
		//地址编码自动生成
		String prefix = "DZBM";
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		int random = RandomUtils.nextInt(90) + 10;
		String value = prefix + format.format(new Date()) + random;
		cjxtStandardAddressAudit.setAddressCode(value);

		//网格员新增数据
		SysDepart sysDepart = null;
		//民政地址
		String addressNameMz = "陕西省";
		String ssqCode = "";
		String streetCode = "";//街道编码
		int len = cjxtStandardAddressAudit.getTjrOrgCode().length()/3;
		for(int i=1;i<len+1;i++){
			if(i==3){
				streetCode = cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3);
			}
			sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3)).eq("del_flag","0"));
			addressNameMz +=  sysDepart.getDepartName();
		}
		//省
		CjxtArea cjxtAreaProv = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getProvinceName()).last(" LIMIT 1"));
		if(cjxtAreaProv!=null){
			ssqCode += cjxtAreaProv.getAreaCode() + ",";
			cjxtStandardAddressAudit.setProvinceCode(cjxtAreaProv.getAreaCode());
		}
		//市
		CjxtArea cjxtAreaCity = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getCityName()).last(" LIMIT 1"));
		if(cjxtAreaCity!=null){
			ssqCode +=  cjxtAreaCity.getAreaCode() + ",";
			cjxtStandardAddressAudit.setCityCode(cjxtAreaCity.getAreaCode());
		}
		//区
		CjxtArea cjxtAreaDist = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getDistrictName()).last(" LIMIT 1"));
		if(cjxtAreaDist!=null){
			ssqCode +=  cjxtAreaDist.getAreaCode();
			cjxtStandardAddressAudit.setDistrictCode(cjxtAreaDist.getAreaCode());
		}

		//省市区编码
		if(!"".equals(ssqCode)){
			cjxtStandardAddressAudit.setSsqCode(ssqCode);
		}
		//街道
		if(!"".equals(streetCode)){
			SysDepart streetDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,streetCode).last("LIMIT 1"));
			if(streetDepart!=null){
				cjxtStandardAddressAudit.setStreetCode(streetDepart.getOrgCode());
				cjxtStandardAddressAudit.setStreetName(streetDepart.getDepartName());
			}
		}
		//标准地址
		String addressName = "";
		//省
		if(cjxtStandardAddressAudit.getProvinceName()!=null && !"".equals(cjxtStandardAddressAudit.getProvinceName())){
			addressName = cjxtStandardAddressAudit.getProvinceName();
		}
		//市
		if(cjxtStandardAddressAudit.getCityName()!=null && !"".equals(cjxtStandardAddressAudit.getCityName())){
			addressName = addressName + cjxtStandardAddressAudit.getCityName();
		}
		//区/县
		if(cjxtStandardAddressAudit.getDistrictName()!=null && !"".equals(cjxtStandardAddressAudit.getDistrictName())){
			addressName = addressName + cjxtStandardAddressAudit.getDistrictName();
		}
		//路名
		if(cjxtStandardAddressAudit.getDetailLm()!=null && !"".equals(cjxtStandardAddressAudit.getDetailLm())){
			addressName = addressName + cjxtStandardAddressAudit.getDetailLm();
			addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailLm();
		}
		//路号
		if(cjxtStandardAddressAudit.getDetailLhm()!=null && !"".equals(cjxtStandardAddressAudit.getDetailLhm())){
			addressName = addressName + cjxtStandardAddressAudit.getDetailLhm() + "号";
			addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailLhm() + "号";
		}
		//地址类型（1-城市家庭住宅 2-农村家庭住宅 3-单位大厦 4-商铺 5-城中村 6-宿舍 99-其他 ）
		if("1".equals(cjxtStandardAddressAudit.getDzType())){
			cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz1Xqm());
			//小区名
			if(cjxtStandardAddressAudit.getDz1Xqm()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Xqm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1Xqm();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Xqm();
			}
			//楼栋
			if(cjxtStandardAddressAudit.getDz1Ld()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Ld())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1Ld() + "号楼";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Ld() + "号楼";
			}
			//单元
			if(cjxtStandardAddressAudit.getDz1Dy()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Dy())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1Dy() + "单元";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Dy() + "单元";
			}
			//室
			if(cjxtStandardAddressAudit.getDz1S()!=null && !"".equals(cjxtStandardAddressAudit.getDz1S())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1S() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1S() + "室";
			}
		}else if("2".equals(cjxtStandardAddressAudit.getDzType())){
			cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz2Cm());
			//村名
			if(cjxtStandardAddressAudit.getDz2Cm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Cm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz2Cm();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Cm();
			}
			//组名
			if(cjxtStandardAddressAudit.getDz2Zm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Zm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz2Zm() + "组";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Zm() + "组";
			}
			//号名
			if(cjxtStandardAddressAudit.getDz2Hm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Hm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz2Hm() + "号";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Hm() + "号";
			}

		}else if("3".equals(cjxtStandardAddressAudit.getDzType())){
			cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz3Dsm());
			//大厦名
			if(cjxtStandardAddressAudit.getDz3Dsm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Dsm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz3Dsm();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Dsm();
			}
			//楼栋名
			if(cjxtStandardAddressAudit.getDz3Ldm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Ldm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz3Ldm() + "栋";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Ldm() + "栋";
			}
			//室名
			if(cjxtStandardAddressAudit.getDz3Sm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Sm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz3Sm() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Sm() + "室";
			}
		}else if("4".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
		}else if("5".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
			if(cjxtStandardAddressAudit.getDz5P()!=null && !"".equals(cjxtStandardAddressAudit.getDz5P())){
				addressName = addressName + cjxtStandardAddressAudit.getDz5P() + "排";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5P() + "排";
			}
			if(cjxtStandardAddressAudit.getDz5H()!=null && !"".equals(cjxtStandardAddressAudit.getDz5H())){
				addressName = addressName + cjxtStandardAddressAudit.getDz5H() + "号";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5H() + "号";
			}
			if(cjxtStandardAddressAudit.getDz5S()!=null && !"".equals(cjxtStandardAddressAudit.getDz5S())){
				addressName = addressName + cjxtStandardAddressAudit.getDz5S() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5S() + "室";
			}

		}else if("6".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
			if(cjxtStandardAddressAudit.getDz6S()!=null && !"".equals(cjxtStandardAddressAudit.getDz6S())){
				addressName = addressName + cjxtStandardAddressAudit.getDz6S() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz6S() + "室";
			}
		}else if("99".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
		}
		//补充说明
		if(cjxtStandardAddressAudit.getDetailAddress()!=null && !"".equals(cjxtStandardAddressAudit.getDetailAddress())){
			addressName = addressName + "(" + cjxtStandardAddressAudit.getDetailAddress() + ")";
			addressNameMz = addressNameMz + "(" + cjxtStandardAddressAudit.getDetailAddress() + ")";
		}
		//不动产编号
		if(cjxtStandardAddressAudit.getBdcbh()!=null && !"".equals(cjxtStandardAddressAudit.getBdcbh())){
			addressName = addressName + "(不动产编号：" + cjxtStandardAddressAudit.getBdcbh() + ")";
			addressNameMz = addressNameMz + "(不动产编号：" + cjxtStandardAddressAudit.getBdcbh() + ")";
		}

		if(!"".equals(addressName)){
			CjxtStandardAddressAudit auditOne = cjxtStandardAddressAuditService.getOne(new LambdaQueryWrapper<CjxtStandardAddressAudit>()
					.eq(CjxtStandardAddressAudit::getAddressName,addressName).last("LIMIT 1"));
			if(auditOne!=null){
				return Result.error("当前提交地址信息已存在!!!!");
			}
		}

		CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getOne(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDzType,cjxtStandardAddressAudit.getDzType()).eq(CjxtStandardAddress::getAddressName,addressName).last("LIMIT 1"));
		if(cjxtStandardAddress!=null){
			return Result.error("当前提交地址信息已存在!!!!");
		}

		cjxtStandardAddressAudit.setAddressName(addressName);
		cjxtStandardAddressAudit.setAddressNameMz(addressNameMz);

		//提交人信息
		SysUser tjrSysUser = sysUserService.getById(cjxtStandardAddressAudit.getTjrId());
		if(tjrSysUser!=null){
			cjxtStandardAddressAudit.setTjrId(tjrSysUser.getId());
			cjxtStandardAddressAudit.setTjrName(tjrSysUser.getRealname());
			cjxtStandardAddressAudit.setTjrZh(tjrSysUser.getUsername());
		}

		SysDepart tjrDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtStandardAddressAudit.getTjrOrgCode()));
		if(tjrDepart!=null){
			cjxtStandardAddressAudit.setTjrOrgId(tjrDepart.getId());
			cjxtStandardAddressAudit.setTjrOrgCode(tjrDepart.getOrgCode());
			cjxtStandardAddressAudit.setTjrOrgName(tjrDepart.getDepartNameFull());
			if("1".equals(cjxtStandardAddressAudit.getUserSf())){
				cjxtStandardAddressAudit.setAddressIdMz(tjrDepart.getId());
				cjxtStandardAddressAudit.setAddressCodeMz(tjrDepart.getOrgCode());
				cjxtStandardAddressAudit.setAddressDepartnameMz(tjrDepart.getDepartNameFull());
			}
		}
		if("2".equals(cjxtStandardAddressAudit.getUserSf()) || "3".equals(cjxtStandardAddressAudit.getUserSf()) || "7".equals(cjxtStandardAddressAudit.getUserSf())){
			// 民警 辅警  派出所工作人员
			cjxtStandardAddressAudit.setShrId(cjxtStandardAddressAudit.getTjrId());
			cjxtStandardAddressAudit.setShrName(cjxtStandardAddressAudit.getTjrName());
			cjxtStandardAddressAudit.setShrOrgId(tjrDepart.getId());
			cjxtStandardAddressAudit.setShrOrgCode(tjrDepart.getOrgCode());
			cjxtStandardAddressAudit.setShrOrgName(tjrDepart.getDepartName());
		}else {
			String orgCode = tjrDepart.getOrgCode();
			if("9".equals(sysDepart.getOrgCategory())){
				int lastIndex = orgCode.lastIndexOf('A');
				if (lastIndex != -1) {
					orgCode = orgCode.substring(0, lastIndex);
				}
			}
			if("10".equals(sysDepart.getOrgCategory())){
				int lastIndex = orgCode.lastIndexOf('A');
				int secondLastIndex = orgCode.lastIndexOf('A', lastIndex - 1);
				if (secondLastIndex != -1) {
					orgCode = orgCode.substring(0, secondLastIndex);
				}
			}
			//提交人社区所属派出所信息
			boolean pcsMsgStatus = false;
			List<Map<String, Object>> resultList = new ArrayList<>();
			String dataSql = "SELECT bm.org_id, bm.org_code, bm.org_name " +
					"FROM cjxt_bm_data bm " +
					"INNER JOIN sys_depart sd ON bm.org_id = sd.id " +
					"WHERE bm.del_flag = '0' " +
					"AND sd.org_category IN ('4', '5') " +
					"AND bm.data_org_code LIKE '"+ orgCode +"%' LIMIT 1 ";
			resultList = jdbcTemplate.queryForList(dataSql);

			if (!resultList.isEmpty()) {
				Map<String, Object> row = resultList.get(0);
				cjxtStandardAddressAudit.setShrOrgId((String) row.get("org_id"));
				cjxtStandardAddressAudit.setShrOrgCode((String) row.get("org_code"));
				cjxtStandardAddressAudit.setShrOrgName((String) row.get("org_name"));
			}else {
				return Result.error("当前提交地址,不存在派出所信息!!!!");
			}
		}
		cjxtStandardAddressAudit.setShzt("1");
		cjxtStandardAddressAuditService.save(cjxtStandardAddressAudit);
		if("1".equals(cjxtStandardAddressAudit.getTjzt())){
			return Result.OK("保存成功！");
		}
		if("2".equals(cjxtStandardAddressAudit.getTjzt())){
			return Result.OK("提交成功！");
		}
		return Result.OK("添加成功！");
	}
	
	/**
	 *  APP编辑
	 *
	 * @param cjxtStandardAddressAudit
	 * @return
	 */
	@AutoLog(value = "地址审核-APP编辑")
	@ApiOperation(value="地址审核-APP编辑", notes="地址审核-APP编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address_audit:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtStandardAddressAudit cjxtStandardAddressAudit) {
		//网格员新增数据
		SysDepart sysDepart = null;
		//民政地址
		String addressNameMz = "陕西省";
		String ssqCode = "";
		String streetCode = "";//街道编码
		int len = cjxtStandardAddressAudit.getTjrOrgCode().length()/3;
		for(int i=1;i<len+1;i++){
			if(i==3){
				streetCode = cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3);
			}
			sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3)).eq("del_flag","0"));
			addressNameMz +=  sysDepart.getDepartName();
		}
		//省
		CjxtArea cjxtAreaProv = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getProvinceName()).last(" LIMIT 1"));
		if(cjxtAreaProv!=null){
			ssqCode += cjxtAreaProv.getAreaCode() + ",";
			cjxtStandardAddressAudit.setProvinceCode(cjxtAreaProv.getAreaCode());
		}
		//市
		CjxtArea cjxtAreaCity = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getCityName()).last(" LIMIT 1"));
		if(cjxtAreaCity!=null){
			ssqCode +=  cjxtAreaCity.getAreaCode() + ",";
			cjxtStandardAddressAudit.setCityCode(cjxtAreaCity.getAreaCode());
		}
		//区
		CjxtArea cjxtAreaDist = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getDistrictName()).last(" LIMIT 1"));
		if(cjxtAreaDist!=null){
			ssqCode +=  cjxtAreaDist.getAreaCode();
			cjxtStandardAddressAudit.setDistrictCode(cjxtAreaDist.getAreaCode());
		}

		//省市区编码
		if(!"".equals(ssqCode)){
			cjxtStandardAddressAudit.setSsqCode(ssqCode);
		}
		//街道
		if(!"".equals(streetCode)){
			SysDepart streetDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,streetCode).last("LIMIT 1"));
			if(streetDepart!=null){
				cjxtStandardAddressAudit.setStreetCode(streetDepart.getOrgCode());
				cjxtStandardAddressAudit.setStreetName(streetDepart.getDepartName());
			}
		}
		//标准地址
		String addressName = "";
		//省
		if(cjxtStandardAddressAudit.getProvinceName()!=null && !"".equals(cjxtStandardAddressAudit.getProvinceName())){
			addressName = cjxtStandardAddressAudit.getProvinceName();
		}
		//市
		if(cjxtStandardAddressAudit.getCityName()!=null && !"".equals(cjxtStandardAddressAudit.getCityName())){
			addressName = addressName + cjxtStandardAddressAudit.getCityName();
		}
		//区/县
		if(cjxtStandardAddressAudit.getDistrictName()!=null && !"".equals(cjxtStandardAddressAudit.getDistrictName())){
			addressName = addressName + cjxtStandardAddressAudit.getDistrictName();
		}
		//路名
		if(cjxtStandardAddressAudit.getDetailLm()!=null && !"".equals(cjxtStandardAddressAudit.getDetailLm())){
			addressName = addressName + cjxtStandardAddressAudit.getDetailLm();
			addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailLm();
		}
		//路号
		if(cjxtStandardAddressAudit.getDetailLhm()!=null && !"".equals(cjxtStandardAddressAudit.getDetailLhm())){
			addressName = addressName + cjxtStandardAddressAudit.getDetailLhm() + "号";
			addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailLhm() + "号";
		}
		//地址类型（1-城市家庭住宅 2-农村家庭住宅 3-单位大厦 4-商铺 5-城中村 6-宿舍 99-其他 ）
		if("1".equals(cjxtStandardAddressAudit.getDzType())){
			cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz1Xqm());
			//小区名
			if(cjxtStandardAddressAudit.getDz1Xqm()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Xqm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1Xqm();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Xqm();
			}
			//楼栋
			if(cjxtStandardAddressAudit.getDz1Ld()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Ld())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1Ld() + "号楼";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Ld() + "号楼";
			}
			//单元
			if(cjxtStandardAddressAudit.getDz1Dy()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Dy())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1Dy() + "单元";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Dy() + "单元";
			}
			//室
			if(cjxtStandardAddressAudit.getDz1S()!=null && !"".equals(cjxtStandardAddressAudit.getDz1S())){
				addressName = addressName + cjxtStandardAddressAudit.getDz1S() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1S() + "室";
			}
		}else if("2".equals(cjxtStandardAddressAudit.getDzType())){
			cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz2Cm());
			//村名
			if(cjxtStandardAddressAudit.getDz2Cm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Cm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz2Cm();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Cm();
			}
			//组名
			if(cjxtStandardAddressAudit.getDz2Zm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Zm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz2Zm() + "组";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Zm() + "组";
			}
			//号名
			if(cjxtStandardAddressAudit.getDz2Hm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Hm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz2Hm() + "号";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Hm() + "号";
			}

		}else if("3".equals(cjxtStandardAddressAudit.getDzType())){
			cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz3Dsm());
			//大厦名
			if(cjxtStandardAddressAudit.getDz3Dsm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Dsm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz3Dsm();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Dsm();
			}
			//楼栋名
			if(cjxtStandardAddressAudit.getDz3Ldm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Ldm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz3Ldm() + "栋";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Ldm() + "栋";
			}
			//室名
			if(cjxtStandardAddressAudit.getDz3Sm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Sm())){
				addressName = addressName + cjxtStandardAddressAudit.getDz3Sm() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Sm() + "室";
			}
		}else if("4".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
		}else if("5".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
			if(cjxtStandardAddressAudit.getDz5P()!=null && !"".equals(cjxtStandardAddressAudit.getDz5P())){
				addressName = addressName + cjxtStandardAddressAudit.getDz5P() + "排";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5P() + "排";
			}
			if(cjxtStandardAddressAudit.getDz5H()!=null && !"".equals(cjxtStandardAddressAudit.getDz5H())){
				addressName = addressName + cjxtStandardAddressAudit.getDz5H() + "号";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5H() + "号";
			}
			if(cjxtStandardAddressAudit.getDz5S()!=null && !"".equals(cjxtStandardAddressAudit.getDz5S())){
				addressName = addressName + cjxtStandardAddressAudit.getDz5S() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5S() + "室";
			}

		}else if("6".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
			if(cjxtStandardAddressAudit.getDz6S()!=null && !"".equals(cjxtStandardAddressAudit.getDz6S())){
				addressName = addressName + cjxtStandardAddressAudit.getDz6S() + "室";
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz6S() + "室";
			}
		}else if("99".equals(cjxtStandardAddressAudit.getDzType())){
			if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			}
		}
		//补充说明
		if(cjxtStandardAddressAudit.getDetailAddress()!=null && !"".equals(cjxtStandardAddressAudit.getDetailAddress())){
			addressName = addressName + "(" + cjxtStandardAddressAudit.getDetailAddress() + ")";
			addressNameMz = addressNameMz + "(" + cjxtStandardAddressAudit.getDetailAddress() + ")";
		}
		//不动产编号
		if(cjxtStandardAddressAudit.getBdcbh()!=null && !"".equals(cjxtStandardAddressAudit.getBdcbh())){
			addressName = addressName + "(不动产编号：" + cjxtStandardAddressAudit.getBdcbh() + ")";
			addressNameMz = addressNameMz + "(不动产编号：" + cjxtStandardAddressAudit.getBdcbh() + ")";
		}
		if(!cjxtStandardAddressAudit.getAddressNameDto().equals(cjxtStandardAddressAudit.getAddressName())){
			CjxtStandardAddressAudit auditOne = cjxtStandardAddressAuditService.getOne(new LambdaQueryWrapper<CjxtStandardAddressAudit>()
					.eq(CjxtStandardAddressAudit::getAddressName,addressName).last("LIMIT 1"));
			if(auditOne!=null){
				return Result.error("当前提交地址信息已存在!!!!");
			}
		}

		CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getOne(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDzType,cjxtStandardAddressAudit.getDzType()).eq(CjxtStandardAddress::getAddressName,addressName).last("LIMIT 1"));
		if(cjxtStandardAddress!=null){
			return Result.error("当前提交地址信息已存在!!!!");
		}

		cjxtStandardAddressAudit.setAddressName(addressName);
		cjxtStandardAddressAudit.setAddressNameMz(addressNameMz);

		//提交人信息
//		SysUser tjrSysUser = sysUserService.getById(cjxtStandardAddressAudit.getTjrId());
//		if(tjrSysUser!=null){
//			cjxtStandardAddressAudit.setTjrId(tjrSysUser.getId());
//			cjxtStandardAddressAudit.setTjrName(tjrSysUser.getRealname());
//			cjxtStandardAddressAudit.setTjrZh(tjrSysUser.getUsername());
//		}

		SysDepart tjrDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,cjxtStandardAddressAudit.getTjrOrgCode()));
		if(tjrDepart!=null){
			cjxtStandardAddressAudit.setTjrOrgId(tjrDepart.getId());
			cjxtStandardAddressAudit.setTjrOrgCode(tjrDepart.getOrgCode());
			cjxtStandardAddressAudit.setTjrOrgName(tjrDepart.getDepartNameFull());
			if("1".equals(cjxtStandardAddressAudit.getUserSf())){
				cjxtStandardAddressAudit.setAddressIdMz(tjrDepart.getId());
				cjxtStandardAddressAudit.setAddressCodeMz(tjrDepart.getOrgCode());
				cjxtStandardAddressAudit.setAddressDepartnameMz(tjrDepart.getDepartNameFull());
			}
		}
		if("2".equals(cjxtStandardAddressAudit.getUserSf()) || "3".equals(cjxtStandardAddressAudit.getUserSf()) || "7".equals(cjxtStandardAddressAudit.getUserSf())){
			// 民警 辅警  派出所工作人员
			cjxtStandardAddressAudit.setShrId(cjxtStandardAddressAudit.getTjrId());
			cjxtStandardAddressAudit.setShrName(cjxtStandardAddressAudit.getTjrName());
			cjxtStandardAddressAudit.setShrOrgId(tjrDepart.getId());
			cjxtStandardAddressAudit.setShrOrgCode(tjrDepart.getOrgCode());
			cjxtStandardAddressAudit.setShrOrgName(tjrDepart.getDepartName());
		}else {
			String orgCode = tjrDepart.getOrgCode();
			if("9".equals(sysDepart.getOrgCategory())){
				int lastIndex = orgCode.lastIndexOf('A');
				if (lastIndex != -1) {
					orgCode = orgCode.substring(0, lastIndex);
				}
			}
			if("10".equals(sysDepart.getOrgCategory())){
				int lastIndex = orgCode.lastIndexOf('A');
				int secondLastIndex = orgCode.lastIndexOf('A', lastIndex - 1);
				if (secondLastIndex != -1) {
					orgCode = orgCode.substring(0, secondLastIndex);
				}
			}
			//提交人社区所属派出所信息
			boolean pcsMsgStatus = false;
			List<Map<String, Object>> resultList = new ArrayList<>();
			String dataSql = "SELECT bm.org_id, bm.org_code, bm.org_name " +
					"FROM cjxt_bm_data bm " +
					"INNER JOIN sys_depart sd ON bm.org_id = sd.id " +
					"WHERE bm.del_flag = '0' " +
					"AND sd.org_category IN ('4', '5') " +
					"AND bm.data_org_code LIKE '"+ orgCode +"%' LIMIT 1 ";
			resultList = jdbcTemplate.queryForList(dataSql);

			if (!resultList.isEmpty()) {
				Map<String, Object> row = resultList.get(0);
				cjxtStandardAddressAudit.setShrOrgId((String) row.get("org_id"));
				cjxtStandardAddressAudit.setShrOrgCode((String) row.get("org_code"));
				cjxtStandardAddressAudit.setShrOrgName((String) row.get("org_name"));
			}else {
				return Result.error("当前提交地址,不存在派出所信息!!!!");
			}

		}
		cjxtStandardAddressAudit.setShzt("1");

		cjxtStandardAddressAuditService.updateById(cjxtStandardAddressAudit);
		if("1".equals(cjxtStandardAddressAudit.getTjzt())){
			return Result.OK("编辑成功！");
		}
		if("2".equals(cjxtStandardAddressAudit.getTjzt())){
			return Result.OK("提交成功！");
		}
		return Result.OK("编辑成功!");
	}

	 /**
	  *  PC编辑
	  *
	  * @param cjxtStandardAddressAudit
	  * @return
	  */
	 @AutoLog(value = "地址审核-PC编辑")
	 @ApiOperation(value="地址审核-PC编辑", notes="地址审核-PC编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address_audit:edit")
	 @RequestMapping(value = "/editPc", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> editPc(@RequestBody CjxtStandardAddressAudit cjxtStandardAddressAudit) {
		 //网格员新增数据
		 SysDepart sysDepart = null;
		 //民政地址
		 String addressNameMz = "陕西省";
		 String ssqCode = "";
		 String streetCode = "";//街道编码
		 if(!cjxtStandardAddressAudit.getAddressDepartnameMzDto().equals(cjxtStandardAddressAudit.getAddressDepartnameMz())){

			 String addressDepartnameMz = cjxtStandardAddressAudit.getAddressDepartnameMz();
			 if(addressDepartnameMz!=null && !"".equals(addressDepartnameMz)){
				 SysDepart depart = sysDepartService.getById(addressDepartnameMz);
				 cjxtStandardAddressAudit.setAddressIdMz(depart.getId());
				 cjxtStandardAddressAudit.setAddressCodeMz(depart.getOrgCode());
				 cjxtStandardAddressAudit.setAddressDepartnameMz(depart.getDepartNameFull());
				 int len = depart.getOrgCode().length()/3;
				 for(int i=1;i<len+1;i++){
					 if(i==3){
						 streetCode = cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3);
					 }
					 sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",depart.getOrgCode().substring(0, i*3)).eq("del_flag","0"));
					 addressNameMz +=  sysDepart.getDepartName();
				 }
			 }
		 }else {
			 int len = cjxtStandardAddressAudit.getTjrOrgCode().length()/3;
			 for(int i=1;i<len+1;i++){
				 if(i==3){
					 streetCode = cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3);
				 }
				 sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",cjxtStandardAddressAudit.getTjrOrgCode().substring(0, i*3)).eq("del_flag","0"));
				 addressNameMz +=  sysDepart.getDepartName();
			 }
		 }
		 //省
		 CjxtArea cjxtAreaProv = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getProvinceName()).last(" LIMIT 1"));
		 if(cjxtAreaProv!=null){
			 ssqCode += cjxtAreaProv.getAreaCode() + ",";
			 cjxtStandardAddressAudit.setProvinceCode(cjxtAreaProv.getAreaCode());
		 }
		 //市
		 CjxtArea cjxtAreaCity = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getCityName()).last(" LIMIT 1"));
		 if(cjxtAreaCity!=null){
			 ssqCode +=  cjxtAreaCity.getAreaCode() + ",";
			 cjxtStandardAddressAudit.setCityCode(cjxtAreaCity.getAreaCode());
		 }
		 //区
		 CjxtArea cjxtAreaDist = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName,cjxtStandardAddressAudit.getDistrictName()).last(" LIMIT 1"));
		 if(cjxtAreaDist!=null){
			 ssqCode +=  cjxtAreaDist.getAreaCode();
			 cjxtStandardAddressAudit.setDistrictCode(cjxtAreaDist.getAreaCode());
		 }

		 //省市区编码
		 if(!"".equals(ssqCode)){
			 cjxtStandardAddressAudit.setSsqCode(ssqCode);
		 }
		 //街道
		 if(!"".equals(streetCode)){
			 SysDepart streetDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode,streetCode).last("LIMIT 1"));
			 if(streetDepart!=null){
				 cjxtStandardAddressAudit.setStreetCode(streetDepart.getOrgCode());
				 cjxtStandardAddressAudit.setStreetName(streetDepart.getDepartName());
			 }
		 }
		 //标准地址
		 String addressName = "";
		 //省
		 if(cjxtStandardAddressAudit.getProvinceName()!=null && !"".equals(cjxtStandardAddressAudit.getProvinceName())){
			 addressName = cjxtStandardAddressAudit.getProvinceName();
		 }
		 //市
		 if(cjxtStandardAddressAudit.getCityName()!=null && !"".equals(cjxtStandardAddressAudit.getCityName())){
			 addressName = addressName + cjxtStandardAddressAudit.getCityName();
		 }
		 //区/县
		 if(cjxtStandardAddressAudit.getDistrictName()!=null && !"".equals(cjxtStandardAddressAudit.getDistrictName())){
			 addressName = addressName + cjxtStandardAddressAudit.getDistrictName();
		 }
		 //路名
		 if(cjxtStandardAddressAudit.getDetailLm()!=null && !"".equals(cjxtStandardAddressAudit.getDetailLm())){
			 addressName = addressName + cjxtStandardAddressAudit.getDetailLm();
			 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailLm();
		 }
		 //路号
		 if(cjxtStandardAddressAudit.getDetailLhm()!=null && !"".equals(cjxtStandardAddressAudit.getDetailLhm())){
			 addressName = addressName + cjxtStandardAddressAudit.getDetailLhm() + "号";
			 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailLhm() + "号";
		 }
		 //地址类型（1-城市家庭住宅 2-农村家庭住宅 3-单位大厦 4-商铺 5-城中村 6-宿舍 99-其他 ）
		 if("1".equals(cjxtStandardAddressAudit.getDzType())){
			 cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz1Xqm());
			 //小区名
			 if(cjxtStandardAddressAudit.getDz1Xqm()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Xqm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz1Xqm();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Xqm();
			 }
			 //楼栋
			 if(cjxtStandardAddressAudit.getDz1Ld()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Ld())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz1Ld() + "号楼";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Ld() + "号楼";
			 }
			 //单元
			 if(cjxtStandardAddressAudit.getDz1Dy()!=null && !"".equals(cjxtStandardAddressAudit.getDz1Dy())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz1Dy() + "单元";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1Dy() + "单元";
			 }
			 //室
			 if(cjxtStandardAddressAudit.getDz1S()!=null && !"".equals(cjxtStandardAddressAudit.getDz1S())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz1S() + "室";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz1S() + "室";
			 }
		 }else if("2".equals(cjxtStandardAddressAudit.getDzType())){
			 cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz2Cm());
			 //村名
			 if(cjxtStandardAddressAudit.getDz2Cm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Cm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz2Cm();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Cm();
			 }
			 //组名
			 if(cjxtStandardAddressAudit.getDz2Zm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Zm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz2Zm() + "组";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Zm() + "组";
			 }
			 //号名
			 if(cjxtStandardAddressAudit.getDz2Hm()!=null && !"".equals(cjxtStandardAddressAudit.getDz2Hm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz2Hm() + "号";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz2Hm() + "号";
			 }

		 }else if("3".equals(cjxtStandardAddressAudit.getDzType())){
			 cjxtStandardAddressAudit.setDetailMc(cjxtStandardAddressAudit.getDz3Dsm());
			 //大厦名
			 if(cjxtStandardAddressAudit.getDz3Dsm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Dsm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz3Dsm();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Dsm();
			 }
			 //楼栋名
			 if(cjxtStandardAddressAudit.getDz3Ldm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Ldm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz3Ldm() + "栋";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Ldm() + "栋";
			 }
			 //室名
			 if(cjxtStandardAddressAudit.getDz3Sm()!=null && !"".equals(cjxtStandardAddressAudit.getDz3Sm())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz3Sm() + "室";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz3Sm() + "室";
			 }
		 }else if("4".equals(cjxtStandardAddressAudit.getDzType())){
			 if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				 addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			 }
		 }else if("5".equals(cjxtStandardAddressAudit.getDzType())){
			 if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				 addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			 }
			 if(cjxtStandardAddressAudit.getDz5P()!=null && !"".equals(cjxtStandardAddressAudit.getDz5P())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz5P() + "排";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5P() + "排";
			 }
			 if(cjxtStandardAddressAudit.getDz5H()!=null && !"".equals(cjxtStandardAddressAudit.getDz5H())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz5H() + "号";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5H() + "号";
			 }
			 if(cjxtStandardAddressAudit.getDz5S()!=null && !"".equals(cjxtStandardAddressAudit.getDz5S())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz5S() + "室";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz5S() + "室";
			 }

		 }else if("6".equals(cjxtStandardAddressAudit.getDzType())){
			 if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				 addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			 }
			 if(cjxtStandardAddressAudit.getDz6S()!=null && !"".equals(cjxtStandardAddressAudit.getDz6S())){
				 addressName = addressName + cjxtStandardAddressAudit.getDz6S() + "室";
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDz6S() + "室";
			 }
		 }else if("99".equals(cjxtStandardAddressAudit.getDzType())){
			 if(cjxtStandardAddressAudit.getDetailMc()!=null && !"".equals(cjxtStandardAddressAudit.getDetailMc())){
				 addressName = addressName + cjxtStandardAddressAudit.getDetailMc();
				 addressNameMz = addressNameMz + cjxtStandardAddressAudit.getDetailMc();
			 }
		 }
		 //补充说明
		 if(cjxtStandardAddressAudit.getDetailAddress()!=null && !"".equals(cjxtStandardAddressAudit.getDetailAddress())){
			 addressName = addressName + "(" + cjxtStandardAddressAudit.getDetailAddress() + ")";
			 addressNameMz = addressNameMz + "(" + cjxtStandardAddressAudit.getDetailAddress() + ")";
		 }
		 //不动产编号
		 if(cjxtStandardAddressAudit.getBdcbh()!=null && !"".equals(cjxtStandardAddressAudit.getBdcbh())){
			 addressName = addressName + "(不动产编号：" + cjxtStandardAddressAudit.getBdcbh() + ")";
			 addressNameMz = addressNameMz + "(不动产编号：" + cjxtStandardAddressAudit.getBdcbh() + ")";
		 }

		 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getOne(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDzType,cjxtStandardAddressAudit.getDzType()).eq(CjxtStandardAddress::getAddressName,addressName).last("LIMIT 1"));
		 if(cjxtStandardAddress!=null){
			 return Result.error("当前提交地址信息已存在!!!!");
		 }

		 cjxtStandardAddressAudit.setAddressName(addressName);
		 cjxtStandardAddressAudit.setAddressNameMz(addressNameMz);

		 cjxtStandardAddressAudit.setShzt("1");
		 cjxtStandardAddressAuditService.updateById(cjxtStandardAddressAudit);
		 return Result.OK("编辑成功!");
	 }

	 /**
	  *  审核
	  *
	  * @param cjxtStandardAddressAudit
	  * @return
	  */
	 @AutoLog(value = "地址审核-审核")
	 @ApiOperation(value="地址审核-审核", notes="地址审核-审核")
//	@RequiresPermissions("cjxt:cjxt_standard_address_audit:edit")
	 @RequestMapping(value = "/editAudit", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> editAudit(@RequestBody CjxtStandardAddressAudit cjxtStandardAddressAudit) {
         CjxtStandardAddressAudit addressAudit = new CjxtStandardAddressAudit();
         addressAudit.setId(cjxtStandardAddressAudit.getId());
         addressAudit.setShzt(cjxtStandardAddressAudit.getShzt());
		 cjxtStandardAddressAuditService.updateById(addressAudit);
		 if("2".equals(cjxtStandardAddressAudit.getShzt())){
			 CjxtStandardAddress cjxtStandardAddress = new CjxtStandardAddress();
			 BeanUtils.copyProperties(cjxtStandardAddressAudit, cjxtStandardAddress);
			 cjxtStandardAddressService.save(cjxtStandardAddress);
		 }
		 return Result.OK("审核成功!");
	 }
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "地址审核-通过id删除")
	@ApiOperation(value="地址审核-通过id删除", notes="地址审核-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_audit:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtStandardAddressAuditService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "地址审核-批量删除")
	@ApiOperation(value="地址审核-批量删除", notes="地址审核-批量删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address_audit:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtStandardAddressAuditService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "地址审核-通过id查询")
	@ApiOperation(value="地址审核-通过id查询", notes="地址审核-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtStandardAddressAudit> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtStandardAddressAudit cjxtStandardAddressAudit = cjxtStandardAddressAuditService.getById(id);
		if(cjxtStandardAddressAudit==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtStandardAddressAudit);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtStandardAddressAudit
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_audit:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtStandardAddressAudit cjxtStandardAddressAudit) {
        return super.exportXls(request, cjxtStandardAddressAudit, CjxtStandardAddressAudit.class, "地址审核");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address_audit:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtStandardAddressAudit.class);
    }

}
