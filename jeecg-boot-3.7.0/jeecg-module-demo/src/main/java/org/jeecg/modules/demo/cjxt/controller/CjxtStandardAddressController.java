package org.jeecg.modules.demo.cjxt.controller;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.util.Precision;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.*;
import org.jeecg.modules.demo.cjxt.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.demo.cjxt.utils.log.Dg;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysUserDepartService;
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

 /**
 * @Description: 标准地址表
 * @Author: jeecg-boot
 * @Date:   2024-06-13
 * @Version: V1.0
 */
@Api(tags="标准地址表")
@RestController
@RequestMapping("/cjxt/cjxtStandardAddress")
@Slf4j
public class CjxtStandardAddressController extends JeecgController<CjxtStandardAddress, ICjxtStandardAddressService> {
	@Autowired
	private ICjxtStandardAddressService cjxtStandardAddressService;
	@Autowired
	private ICjxtAreaService cjxtAreaService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private ICjxtStandardAddressPersonService cjxtStandardAddressPersonService;
	@Autowired
	private ICjxtMbglService cjxtMbglService;
	@Autowired
	private ICjxtMbglPzService cjxtMbglPzService;
	@Autowired
	private ISysDictItemService sysDictItemService;
	@Autowired
	private ICjxtTaskDispatchService cjxtTaskDispatchService;
	 //minio图片服务器
	 @Value(value="${jeecg.minio.minio_url}")
	 private String minioUrl;
	 @Value(value="${jeecg.minio.bucketName}")
	 private String bucketName;



	 /**
	 * 分页列表查询
	 *
	 * @param cjxtStandardAddress
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "标准地址表-分页列表查询")
	@ApiOperation(value="标准地址表-分页列表查询", notes="标准地址表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtStandardAddress>> queryPageList(CjxtStandardAddress cjxtStandardAddress,
								   @RequestParam(required = false, name="search") String search,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		//如果传入AddressCodeMz的字段为id，转为code,  树地址查询组件使用
		if(cjxtStandardAddress.getAddressCodeMz() != null && !cjxtStandardAddress.getAddressCodeMz().contains("A")){
			SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("id",cjxtStandardAddress.getAddressCodeMz()).eq("del_flag","0"));
			if(sysDepart!=null){
				cjxtStandardAddress.setAddressCodeMz(sysDepart.getOrgCode() + "*");
			}
		}
		Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		parameterMap.remove("order");
		QueryWrapper<CjxtStandardAddress> queryWrapper = QueryGenerator.initQueryWrapper(cjxtStandardAddress, parameterMap);
		Page<CjxtStandardAddress> page = new Page<CjxtStandardAddress>(pageNo, pageSize);
		queryWrapper.orderByAsc("address_code_mz");
		queryWrapper.orderByAsc("detail_lm");
		queryWrapper.orderByAsc("detail_lhm");
		queryWrapper.orderByAsc("detail_mc");
		queryWrapper.orderByAsc("dz1_ld");
		queryWrapper.orderByAsc("dz1_dy");
		queryWrapper.orderByAsc("dz1_s");
		queryWrapper.orderByAsc("dz2_zm");
		queryWrapper.orderByAsc("dz2_hm");
		queryWrapper.orderByAsc("dz3_ldm");
		queryWrapper.orderByAsc("dz3_sm");
		queryWrapper.orderByAsc("dz5_p");
		queryWrapper.orderByAsc("dz5_h");
		queryWrapper.orderByAsc("dz5_s");
		queryWrapper.orderByAsc("dz6_s");
		queryWrapper.orderByAsc("detail_address");
		if(search!=null && !search.equals("")){
			search = search.replaceAll("，",",");
			String[] sea = search.split(",");
			for (String name : sea) {
				queryWrapper.and(wrapper -> wrapper.like("address_name_mz", name));
			}
		}
		IPage<CjxtStandardAddress> pageList = cjxtStandardAddressService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	 /**
	  * 分页列表查询
	  *
	  * @param cjxtStandardAddress
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 //@AutoLog(value = "标准地址表-APP分页列表查询")
	 @ApiOperation(value="标准地址表-APP分页列表查询", notes="标准地址表-APP分页列表查询")
	 @GetMapping(value = "/listApp")
	 public Result<IPage<CjxtStandardAddress>> queryPageListApp(CjxtStandardAddress cjxtStandardAddress,
			 												 @RequestParam(required = true, name="userId") String userId, // 用户ID
															 @RequestParam(required = false, name="search") String search,
															 @RequestParam(required = false, name="searchLd") String searchLd,
															 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															 HttpServletRequest req) throws UnsupportedEncodingException {

		 SysUser sysUser = sysUserService.getById(userId);

		 if(!("2".equals(sysUser.getUserSf()) ||  "3".equals(sysUser.getUserSf()))){//片警、民警
			 cjxtStandardAddress.setAddressCodeMz(sysUser.getOrgCode() + "*");
		 }
		 Map<String, String[]> parameterMap = new HashMap<>(req.getParameterMap());
		 parameterMap.remove("order");
		 QueryWrapper<CjxtStandardAddress> queryWrapper = QueryGenerator.initQueryWrapper(cjxtStandardAddress, parameterMap);
		 Page<CjxtStandardAddress> page = new Page<CjxtStandardAddress>(pageNo, pageSize);
		 queryWrapper.orderByAsc("address_code_mz");
		 queryWrapper.orderByAsc("detail_lm");
		 queryWrapper.orderByAsc("detail_lhm");
		 queryWrapper.orderByAsc("detail_mc");
		 queryWrapper.orderByAsc("dz1_ld");
		 queryWrapper.orderByAsc("dz1_dy");
		 queryWrapper.orderByAsc("dz1_s");
		 queryWrapper.orderByAsc("dz2_zm");
		 queryWrapper.orderByAsc("dz2_hm");
		 queryWrapper.orderByAsc("dz3_ldm");
		 queryWrapper.orderByAsc("dz3_sm");
		 queryWrapper.orderByAsc("dz5_p");
		 queryWrapper.orderByAsc("dz5_h");
		 queryWrapper.orderByAsc("dz5_s");
		 queryWrapper.orderByAsc("dz6_s");
		 queryWrapper.orderByAsc("detail_address");

		 if("2".equals(sysUser.getUserSf()) ||  "3".equals(sysUser.getUserSf())){//片警、民警
			 queryWrapper.and(wrapper -> wrapper.last("address_code_mz in  (select b.wg_code from cjxt_pjwgqx b where b.pj_id='"+userId+"')"));
		 }

		 if(search!=null && !search.equals("")){
			 search = search.replaceAll("，",",");
			 String[] sea = search.split(",");
			 for (String name : sea) {
				 queryWrapper.and(wrapper -> wrapper.like("address_name_mz", name));
			 }
		 }
		 if(searchLd!=null && !"".equals(searchLd)){
//			 searchLd = java.net.URLDecoder.decode(searchLd,"utf8");
			 searchLd = searchLd.replace(",","%");
			 searchLd = "%"+searchLd+"%";
			 String finalSearchLd = searchLd;
			 queryWrapper.and(wrapper -> wrapper.like("address_name_mz", finalSearchLd));
		 }
		 IPage<CjxtStandardAddress> pageList = cjxtStandardAddressService.page(page, queryWrapper);
		 for (CjxtStandardAddress address : pageList.getRecords()) {
			 String addressName = "";
			 if("1".equals(address.getDzType())){
				 //小区名
				 if(address.getDz1Xqm()!=null && !"".equals(address.getDz1Xqm())){
					 addressName = addressName + address.getDz1Xqm();
				 }
				 //楼栋
				 if(address.getDz1Ld()!=null && !"".equals(address.getDz1Ld())){
					 addressName = addressName + address.getDz1Ld() + "号楼";
				 }
				 //单元
				 if(address.getDz1Dy()!=null && !"".equals(address.getDz1Dy())){
					 addressName = addressName + address.getDz1Dy() + "单元";
				 }
				 //室
				 if(address.getDz1S()!=null && !"".equals(address.getDz1S())){
					 addressName = addressName + address.getDz1S() + "室";
				 }
			 }else if("2".equals(address.getDzType())){
				 address.setDetailMc(address.getDz2Cm());
				 //村名
				 if(address.getDz2Cm()!=null && !"".equals(address.getDz2Cm())){
					 addressName = addressName + address.getDz2Cm();
				 }
				 //组名
				 if(address.getDz2Zm()!=null && !"".equals(address.getDz2Zm())){
					 addressName = addressName + address.getDz2Zm() + "组";
				 }
				 //号名
				 if(address.getDz2Hm()!=null && !"".equals(address.getDz2Hm())){
					 addressName = addressName + address.getDz2Hm() + "号";
				 }

			 }else if("3".equals(address.getDzType())){
				 address.setDetailMc(address.getDz3Dsm());
				 //大厦名
				 if(address.getDz3Dsm()!=null && !"".equals(address.getDz3Dsm())){
					 addressName = addressName + address.getDz3Dsm();
				 }
				 //楼栋名
				 if(address.getDz3Ldm()!=null && !"".equals(address.getDz3Ldm())){
					 addressName = addressName + address.getDz3Ldm() + "栋";
				 }
				 //室名
				 if(address.getDz3Sm()!=null && !"".equals(address.getDz3Sm())){
					 addressName = addressName + address.getDz3Sm() + "室";
				 }
			 }else if("4".equals(address.getDzType())){
				 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
					 addressName = addressName + address.getDetailMc();
				 }
			 }else if("5".equals(address.getDzType())){
				 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
					 addressName = addressName + address.getDetailMc();
				 }
				 if(address.getDz5P()!=null && !"".equals(address.getDz5P())){
					 addressName = addressName + address.getDz5P() + "排";
				 }
				 if(address.getDz5H()!=null && !"".equals(address.getDz5H())){
					 addressName = addressName + address.getDz5H() + "号";
				 }
				 if(address.getDz5S()!=null && !"".equals(address.getDz5S())){
					 addressName = addressName + address.getDz5S() + "室";
				 }

			 }else if("6".equals(address.getDzType())){
				 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
					 addressName = addressName + address.getDetailMc();
				 }
				 if(address.getDz6S()!=null && !"".equals(address.getDz6S())){
					 addressName = addressName + address.getDz6S() + "室";
				 }
			 }else if("99".equals(address.getDzType())){
				 if(address.getDetailMc()!=null && !"".equals(address.getDetailMc())){
					 addressName = addressName + address.getDetailMc();
				 }
			 }
			 address.setAddressNameMz(addressName);

			 //地址模版信息
			 SysDictItem sysDictItem = sysDictItemService.getOne(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getItemValue,address.getDzType()).inSql(SysDictItem::getDictId,"SELECT id FROM sys_dict WHERE dict_code = 'dzlx'"));
			 if(sysDictItem!=null){
				 address.setDzmbCodeDto(sysDictItem.getDescription());
			 }
		 }
		 return Result.OK(pageList);
	 }

	/**
	 *   添加
	 *
	 * @param cjxtStandardAddress
	 * @return
	 */
	@AutoLog(value = "标准地址表-添加")
	@ApiOperation(value="标准地址表-添加", notes="标准地址表-添加")
//	@RequiresPermissions("cjxt:cjxt_standard_address:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtStandardAddress cjxtStandardAddress) {
//		CjxtStandardAddress one = cjxtStandardAddressService.getOne(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressCode,cjxtStandardAddress.getAddressCode()));
//		if(one!=null){
//			return Result.error("当前地址编码已存在!");
//		}
		//地址编码自动生成
		String prefix = "DZBM";
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		int random = RandomUtils.nextInt(90) + 10;
		String value = prefix + format.format(new Date()) + random;
		cjxtStandardAddress.setAddressCode(value);


//		String ssqCode = cjxtStandardAddress.getSsqCode();
//
//		if(!"".equals(ssqCode)&&ssqCode!=null){
//			String[] codes = ssqCode.split(",");
//			//省信息
//			CjxtArea provinceArea = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaCode, codes[0]));
//			cjxtStandardAddress.setProvinceCode(codes[0]);
//			if(provinceArea!=null){
//				cjxtStandardAddress.setProvinceName(provinceArea.getAreaName());
//				addressName = provinceArea.getAreaName();
//			}
//			//市信息
//			CjxtArea cityArea = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaCode, codes[1]));
//			cjxtStandardAddress.setCityCode(codes[1]);
//			if(cityArea!=null){
//				cjxtStandardAddress.setCityName(cityArea.getAreaName());
//				addressName = addressName+cityArea.getAreaName();
//			}
//			//区街道信息
//			CjxtArea districtArea = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaCode, codes[2]));
//			cjxtStandardAddress.setDistrictCode(codes[2]);
//			if(districtArea!=null){
//				cjxtStandardAddress.setDistrictName(districtArea.getAreaName());
//				addressName = addressName+districtArea.getAreaName();
//			}
//		}


		//设置民政地址
		//民政地址
		String addressNameMz = "陕西省";
		String addressCodeMz = cjxtStandardAddress.getAddressCodeMz();
		if(addressCodeMz!=null && !"".equals(addressCodeMz)){
			SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",addressCodeMz).eq("del_flag","0"));
			if(sysDepart!=null){
				cjxtStandardAddress.setAddressIdMz(sysDepart.getId());
				int len = addressCodeMz.length()/3;
				for(int i=1;i<len+1;i++){
					sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",addressCodeMz.substring(0, i*3)).eq("del_flag","0"));

					addressNameMz +=  sysDepart.getDepartName();

					//市
					if(i==1){
						cjxtStandardAddress.setProvinceName("陕西省");
						cjxtStandardAddress.setProvinceCode("610000");
						cjxtStandardAddress.setCityName("榆林市");
						cjxtStandardAddress.setCityCode("610800");
					}
					//区/县
					if(i==2){
						CjxtArea districtArea = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName, sysDepart.getDepartName()));
						if(districtArea != null){
							cjxtStandardAddress.setDistrictName(districtArea.getAreaName());
							cjxtStandardAddress.setDistrictCode(districtArea.getAreaCode());
							cjxtStandardAddress.setSsqCode(cjxtStandardAddress.getProvinceCode()+","+cjxtStandardAddress.getCityCode()+","+districtArea.getAreaCode());
						}
					}
					//街办
					if(i==3){
						cjxtStandardAddress.setStreetCode(sysDepart.getOrgCode());
						cjxtStandardAddress.setStreetName(sysDepart.getDepartName());
					}
				}
			}

			//标准地址
			String addressName = "";
			//省
			if(cjxtStandardAddress.getProvinceName()!=null && !"".equals(cjxtStandardAddress.getProvinceName())){
				addressName = cjxtStandardAddress.getProvinceName();
			}
			//市
			if(cjxtStandardAddress.getCityName()!=null && !"".equals(cjxtStandardAddress.getCityName())){
				addressName = addressName + cjxtStandardAddress.getCityName();
			}
			//区/县
			if(cjxtStandardAddress.getDistrictName()!=null && !"".equals(cjxtStandardAddress.getDistrictName())){
				addressName = addressName + cjxtStandardAddress.getDistrictName();
			}
			//路名
			if(cjxtStandardAddress.getDetailLm()!=null && !"".equals(cjxtStandardAddress.getDetailLm())){
				addressName = addressName + cjxtStandardAddress.getDetailLm();
				addressNameMz = addressNameMz + cjxtStandardAddress.getDetailLm();
			}
			//路号
			if(cjxtStandardAddress.getDetailLhm()!=null && !"".equals(cjxtStandardAddress.getDetailLhm())){
				addressName = addressName + cjxtStandardAddress.getDetailLhm() + "号";
				addressNameMz = addressNameMz + cjxtStandardAddress.getDetailLhm() + "号";
			}
			//地址类型（1-城市家庭住宅 2-农村家庭住宅 3-单位大厦 4-商铺 5-城中村 6-宿舍 99-其他 ）
			if("1".equals(cjxtStandardAddress.getDzType())){
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz1Xqm());
				//小区名
				if(cjxtStandardAddress.getDz1Xqm()!=null && !"".equals(cjxtStandardAddress.getDz1Xqm())){
					addressName = addressName + cjxtStandardAddress.getDz1Xqm();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Xqm();
				}
				//楼栋
				if(cjxtStandardAddress.getDz1Ld()!=null && !"".equals(cjxtStandardAddress.getDz1Ld())){
					addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Ld() + "号楼";
				}
				//单元
				if(cjxtStandardAddress.getDz1Dy()!=null && !"".equals(cjxtStandardAddress.getDz1Dy())){
					addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Dy() + "单元";
				}
				//室
				if(cjxtStandardAddress.getDz1S()!=null && !"".equals(cjxtStandardAddress.getDz1S())){
					addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1S() + "室";
				}
			}else if("2".equals(cjxtStandardAddress.getDzType())){
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
				//村名
				if(cjxtStandardAddress.getDz2Cm()!=null && !"".equals(cjxtStandardAddress.getDz2Cm())){
					addressName = addressName + cjxtStandardAddress.getDz2Cm();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Cm();
				}
				//组名
				if(cjxtStandardAddress.getDz2Zm()!=null && !"".equals(cjxtStandardAddress.getDz2Zm())){
					addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Zm() + "组";
				}
				//号名
				if(cjxtStandardAddress.getDz2Hm()!=null && !"".equals(cjxtStandardAddress.getDz2Hm())){
					addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Hm() + "号";
				}

			}else if("3".equals(cjxtStandardAddress.getDzType())){
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
				//大厦名
				if(cjxtStandardAddress.getDz3Dsm()!=null && !"".equals(cjxtStandardAddress.getDz3Dsm())){
					addressName = addressName + cjxtStandardAddress.getDz3Dsm();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Dsm();
				}
				//楼栋名
				if(cjxtStandardAddress.getDz3Ldm()!=null && !"".equals(cjxtStandardAddress.getDz3Ldm())){
					addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Ldm() + "栋";
				}
				//室名
				if(cjxtStandardAddress.getDz3Sm()!=null && !"".equals(cjxtStandardAddress.getDz3Sm())){
					addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Sm() + "室";
				}
			}else if("4".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
			}else if("5".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
				if(cjxtStandardAddress.getDz5P()!=null && !"".equals(cjxtStandardAddress.getDz5P())){
					addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz5P() + "排";
				}
				if(cjxtStandardAddress.getDz5H()!=null && !"".equals(cjxtStandardAddress.getDz5H())){
					addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz5H() + "号";
				}
				if(cjxtStandardAddress.getDz5S()!=null && !"".equals(cjxtStandardAddress.getDz5S())){
					addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz5S() + "室";
				}

			}else if("6".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
				if(cjxtStandardAddress.getDz6S()!=null && !"".equals(cjxtStandardAddress.getDz6S())){
					addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz6S() + "室";
				}
			}else if("99".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
			}
			//补充说明
			if(cjxtStandardAddress.getDetailAddress()!=null && !"".equals(cjxtStandardAddress.getDetailAddress())){
				addressName = addressName + "(" + cjxtStandardAddress.getDetailAddress() + ")";
				addressNameMz = addressNameMz + "(" + cjxtStandardAddress.getDetailAddress() + ")";
			}
			//不动产编号
			if(cjxtStandardAddress.getBdcbh()!=null && !"".equals(cjxtStandardAddress.getBdcbh())){
				addressName = addressName + "(不动产编号：" + cjxtStandardAddress.getBdcbh() + ")";
				addressNameMz = addressNameMz + "(不动产编号：" + cjxtStandardAddress.getBdcbh() + ")";
			}

			cjxtStandardAddress.setAddressName(addressName);
			cjxtStandardAddress.setAddressNameMz(addressNameMz);

			cjxtStandardAddressService.save(cjxtStandardAddress);
		}
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtStandardAddress
	 * @returnd
	 */
	@AutoLog(value = "标准地址表-编辑")
	@ApiOperation(value="标准地址表-编辑", notes="标准地址表-编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtStandardAddress cjxtStandardAddress) {
//		if(!cjxtStandardAddress.getAddressCodeDto().equals(cjxtStandardAddress.getAddressCode())){
//			CjxtStandardAddress one = cjxtStandardAddressService.getOne(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressCode,cjxtStandardAddress.getAddressCode()));
//			if(one!=null){
//				return Result.error("当前地址编码已存在!");
//			}
//		}
		//设置民政地址
		//民政地址
		String addressNameMz = "陕西省";
		String addressCodeMz = cjxtStandardAddress.getAddressCodeMz();
		if(addressCodeMz!=null && !"".equals(addressCodeMz)){
			SysDepart sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",addressCodeMz).eq("del_flag","0"));
			if(sysDepart!=null){
				cjxtStandardAddress.setAddressIdMz(sysDepart.getId());
				int len = addressCodeMz.length()/3;
				for(int i=1;i<len+1;i++){
					sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",addressCodeMz.substring(0, i*3)).eq("del_flag","0"));

					addressNameMz +=  sysDepart.getDepartName();

					//市
					if(i==1){
						cjxtStandardAddress.setProvinceName("陕西省");
						cjxtStandardAddress.setProvinceCode("610000");
						cjxtStandardAddress.setCityName("榆林市");
						cjxtStandardAddress.setCityCode("610800");
					}
					//区/县
					if(i==2){
						CjxtArea districtArea = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName, sysDepart.getDepartName()));
						if(districtArea != null){
							cjxtStandardAddress.setDistrictName(districtArea.getAreaName());
							cjxtStandardAddress.setDistrictCode(districtArea.getAreaCode());
							cjxtStandardAddress.setSsqCode(cjxtStandardAddress.getProvinceCode()+","+cjxtStandardAddress.getCityCode()+","+districtArea.getAreaCode());
						}
					}
					//街办
					if(i==3){
						cjxtStandardAddress.setStreetCode(sysDepart.getOrgCode());
						cjxtStandardAddress.setStreetName(sysDepart.getDepartName());
					}
				}
			}

			//标准地址
			String addressName = "";
			//省
			if(cjxtStandardAddress.getProvinceName()!=null && !"".equals(cjxtStandardAddress.getProvinceName())){
				addressName = cjxtStandardAddress.getProvinceName();
			}
			//市
			if(cjxtStandardAddress.getCityName()!=null && !"".equals(cjxtStandardAddress.getCityName())){
				addressName = addressName + cjxtStandardAddress.getCityName();
			}
			//区/县
			if(cjxtStandardAddress.getDistrictName()!=null && !"".equals(cjxtStandardAddress.getDistrictName())){
				addressName = addressName + cjxtStandardAddress.getDistrictName();
			}
			//路名
			if(cjxtStandardAddress.getDetailLm()!=null && !"".equals(cjxtStandardAddress.getDetailLm())){
				addressName = addressName + cjxtStandardAddress.getDetailLm();
				addressNameMz = addressNameMz + cjxtStandardAddress.getDetailLm();
			}
			//路号
			if(cjxtStandardAddress.getDetailLhm()!=null && !"".equals(cjxtStandardAddress.getDetailLhm())){
				addressName = addressName + cjxtStandardAddress.getDetailLhm() + "号";
				addressNameMz = addressNameMz + cjxtStandardAddress.getDetailLhm() + "号";
			}
			//1 城市家庭住宅	  2 农村家庭住宅	3 单位大厦	4 商铺	5 其他
			if("1".equals(cjxtStandardAddress.getDzType())){
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz1Xqm());
				//小区名
				if(cjxtStandardAddress.getDz1Xqm()!=null && !"".equals(cjxtStandardAddress.getDz1Xqm())){
					addressName = addressName + cjxtStandardAddress.getDz1Xqm();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Xqm();
				}
				//楼栋
				if(cjxtStandardAddress.getDz1Ld()!=null && !"".equals(cjxtStandardAddress.getDz1Ld())){
					addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Ld() + "号楼";
				}
				//单元
				if(cjxtStandardAddress.getDz1Dy()!=null && !"".equals(cjxtStandardAddress.getDz1Dy())){
					addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Dy() + "单元";
				}
				//室
				if(cjxtStandardAddress.getDz1S()!=null && !"".equals(cjxtStandardAddress.getDz1S())){
					addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz1S() + "室";
				}
			}else if("2".equals(cjxtStandardAddress.getDzType())){
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
				//村名
				if(cjxtStandardAddress.getDz2Cm()!=null && !"".equals(cjxtStandardAddress.getDz2Cm())){
					addressName = addressName + cjxtStandardAddress.getDz2Cm();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Cm();
				}
				//组名
				if(cjxtStandardAddress.getDz2Zm()!=null && !"".equals(cjxtStandardAddress.getDz2Zm())){
					addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Zm() + "组";
				}
				//号名
				if(cjxtStandardAddress.getDz2Hm()!=null && !"".equals(cjxtStandardAddress.getDz2Hm())){
					addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Hm() + "号";
				}

			}else if("3".equals(cjxtStandardAddress.getDzType())){
				cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
				//大厦名
				if(cjxtStandardAddress.getDz3Dsm()!=null && !"".equals(cjxtStandardAddress.getDz3Dsm())){
					addressName = addressName + cjxtStandardAddress.getDz3Dsm();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Dsm();
				}
				//楼栋名
				if(cjxtStandardAddress.getDz3Ldm()!=null && !"".equals(cjxtStandardAddress.getDz3Ldm())){
					addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
					addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
				}
				//室名
				if(cjxtStandardAddress.getDz3Sm()!=null && !"".equals(cjxtStandardAddress.getDz3Sm())){
					addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Sm() + "室";
				}
			}else if("4".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
			}else if("5".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
				if(cjxtStandardAddress.getDz5P()!=null && !"".equals(cjxtStandardAddress.getDz5P())){
					addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz5P() + "排";
				}
				if(cjxtStandardAddress.getDz5H()!=null && !"".equals(cjxtStandardAddress.getDz5H())){
					addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz5H() + "号";
				}
				if(cjxtStandardAddress.getDz5S()!=null && !"".equals(cjxtStandardAddress.getDz5S())){
					addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz5S() + "室";
				}

			}else if("6".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
				if(cjxtStandardAddress.getDz6S()!=null && !"".equals(cjxtStandardAddress.getDz6S())){
					addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
					addressNameMz = addressNameMz + cjxtStandardAddress.getDz6S() + "室";
				}
			}else if("99".equals(cjxtStandardAddress.getDzType())){
				if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					addressName = addressName + cjxtStandardAddress.getDetailMc();
					addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				}
			}
			//补充说明
			if(cjxtStandardAddress.getDetailAddress()!=null && !"".equals(cjxtStandardAddress.getDetailAddress())){
				addressName = addressName + "(" + cjxtStandardAddress.getDetailAddress() + ")";
				addressNameMz = addressNameMz + "(" + cjxtStandardAddress.getDetailAddress() + ")";
			}
			//不动产编号
			if(cjxtStandardAddress.getBdcbh()!=null && !"".equals(cjxtStandardAddress.getBdcbh())){
				addressName = addressName + "(不动产编号：" + cjxtStandardAddress.getBdcbh() + ")";
				addressNameMz = addressNameMz + "(不动产编号：" + cjxtStandardAddress.getBdcbh() + ")";
			}

			cjxtStandardAddress.setAddressName(addressName);
			cjxtStandardAddress.setAddressNameMz(addressNameMz);

		}
		cjxtStandardAddressService.updateById(cjxtStandardAddress);
		return Result.OK("编辑成功!");
	}

	 /**
	  *  编辑
	  *
	  * @param cjxtStandardAddress
	  * @returnd
	  */
	 @AutoLog(value = "标准地址表-PC编辑")
	 @ApiOperation(value="标准地址表-PC编辑", notes="标准地址表-PC编辑")
//	@RequiresPermissions("cjxt:cjxt_standard_address:edit")
	 @RequestMapping(value = "/editPc", method = {RequestMethod.PUT,RequestMethod.POST})
	 public Result<String> editPc(@RequestBody CjxtStandardAddress cjxtStandardAddress) {
//		if(!cjxtStandardAddress.getAddressCodeDto().equals(cjxtStandardAddress.getAddressCode())){
//			CjxtStandardAddress one = cjxtStandardAddressService.getOne(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getAddressCode,cjxtStandardAddress.getAddressCode()));
//			if(one!=null){
//				return Result.error("当前地址编码已存在!");
//			}
//		}
		 //设置民政地址
		 //民政地址
		 String addressNameMz = "陕西省";
		 String addressCodeMz = cjxtStandardAddress.getAddressCodeMz();
		 if(addressCodeMz!=null && !"".equals(addressCodeMz)){
			 SysDepart sysDepart = null;
			 if(!cjxtStandardAddress.getAddressDepartnameMzDto().equals(cjxtStandardAddress.getAddressDepartnameMz())){
				 sysDepart = sysDepartService.getById(cjxtStandardAddress.getAddressDepartnameMz());
			 }else {
				 sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",addressCodeMz).eq("del_flag","0"));
			 }
			 if(sysDepart!=null){
				 cjxtStandardAddress.setAddressIdMz(sysDepart.getId());
				 if(!cjxtStandardAddress.getAddressDepartnameMzDto().equals(cjxtStandardAddress.getAddressDepartnameMz())){
					 addressCodeMz = sysDepart.getOrgCode();
				 }
				 int len = addressCodeMz.length()/3;
				 for(int i=1;i<len+1;i++){
					 sysDepart = sysDepartService.getOne(new QueryWrapper<SysDepart>().eq("org_code",addressCodeMz.substring(0, i*3)).eq("del_flag","0"));
					 addressNameMz +=  sysDepart.getDepartName();
					 //市
					 if(i==1){
						 cjxtStandardAddress.setProvinceName("陕西省");
						 cjxtStandardAddress.setProvinceCode("610000");
						 cjxtStandardAddress.setCityName("榆林市");
						 cjxtStandardAddress.setCityCode("610800");
					 }
					 //区/县
					 if(i==2){
						 CjxtArea districtArea = cjxtAreaService.getOne(new LambdaQueryWrapper<CjxtArea>().eq(CjxtArea::getAreaName, sysDepart.getDepartName()));
						 if(districtArea != null){
							 cjxtStandardAddress.setDistrictName(districtArea.getAreaName());
							 cjxtStandardAddress.setDistrictCode(districtArea.getAreaCode());
							 cjxtStandardAddress.setSsqCode(cjxtStandardAddress.getProvinceCode()+","+cjxtStandardAddress.getCityCode()+","+districtArea.getAreaCode());
						 }
					 }
					 //街办
					 if(i==3){
						 cjxtStandardAddress.setStreetCode(sysDepart.getOrgCode());
						 cjxtStandardAddress.setStreetName(sysDepart.getDepartName());
					 }
				 }
			 }

			 //标准地址
			 String addressName = "";
			 //省
			 if(cjxtStandardAddress.getProvinceName()!=null && !"".equals(cjxtStandardAddress.getProvinceName())){
				 addressName = cjxtStandardAddress.getProvinceName();
			 }
			 //市
			 if(cjxtStandardAddress.getCityName()!=null && !"".equals(cjxtStandardAddress.getCityName())){
				 addressName = addressName + cjxtStandardAddress.getCityName();
			 }
			 //区/县
			 if(cjxtStandardAddress.getDistrictName()!=null && !"".equals(cjxtStandardAddress.getDistrictName())){
				 addressName = addressName + cjxtStandardAddress.getDistrictName();
			 }
			 //路名
			 if(cjxtStandardAddress.getDetailLm()!=null && !"".equals(cjxtStandardAddress.getDetailLm())){
				 addressName = addressName + cjxtStandardAddress.getDetailLm();
				 addressNameMz = addressNameMz + cjxtStandardAddress.getDetailLm();
			 }
			 //路号
			 if(cjxtStandardAddress.getDetailLhm()!=null && !"".equals(cjxtStandardAddress.getDetailLhm())){
				 addressName = addressName + cjxtStandardAddress.getDetailLhm() + "号";
				 addressNameMz = addressNameMz + cjxtStandardAddress.getDetailLhm() + "号";
			 }
			 //1 城市家庭住宅	  2 农村家庭住宅	3 单位大厦	4 商铺	5 其他
			 if("1".equals(cjxtStandardAddress.getDzType())){
				 cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz1Xqm());
				 //小区名
				 if(cjxtStandardAddress.getDz1Xqm()!=null && !"".equals(cjxtStandardAddress.getDz1Xqm())){
					 addressName = addressName + cjxtStandardAddress.getDz1Xqm();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Xqm();
				 }
				 //楼栋
				 if(cjxtStandardAddress.getDz1Ld()!=null && !"".equals(cjxtStandardAddress.getDz1Ld())){
					 addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Ld() + "号楼";
				 }
				 //单元
				 if(cjxtStandardAddress.getDz1Dy()!=null && !"".equals(cjxtStandardAddress.getDz1Dy())){
					 addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz1Dy() + "单元";
				 }
				 //室
				 if(cjxtStandardAddress.getDz1S()!=null && !"".equals(cjxtStandardAddress.getDz1S())){
					 addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz1S() + "室";
				 }
			 }else if("2".equals(cjxtStandardAddress.getDzType())){
				 cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
				 //村名
				 if(cjxtStandardAddress.getDz2Cm()!=null && !"".equals(cjxtStandardAddress.getDz2Cm())){
					 addressName = addressName + cjxtStandardAddress.getDz2Cm();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Cm();
				 }
				 //组名
				 if(cjxtStandardAddress.getDz2Zm()!=null && !"".equals(cjxtStandardAddress.getDz2Zm())){
					 addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Zm() + "组";
				 }
				 //号名
				 if(cjxtStandardAddress.getDz2Hm()!=null && !"".equals(cjxtStandardAddress.getDz2Hm())){
					 addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz2Hm() + "号";
				 }

			 }else if("3".equals(cjxtStandardAddress.getDzType())){
				 cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
				 //大厦名
				 if(cjxtStandardAddress.getDz3Dsm()!=null && !"".equals(cjxtStandardAddress.getDz3Dsm())){
					 addressName = addressName + cjxtStandardAddress.getDz3Dsm();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Dsm();
				 }
				 //楼栋名
				 if(cjxtStandardAddress.getDz3Ldm()!=null && !"".equals(cjxtStandardAddress.getDz3Ldm())){
					 addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
					 addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
				 }
				 //室名
				 if(cjxtStandardAddress.getDz3Sm()!=null && !"".equals(cjxtStandardAddress.getDz3Sm())){
					 addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz3Sm() + "室";
				 }
			 }else if("4".equals(cjxtStandardAddress.getDzType())){
				 if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				 }
			 }else if("5".equals(cjxtStandardAddress.getDzType())){
				 if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				 }
				 if(cjxtStandardAddress.getDz5P()!=null && !"".equals(cjxtStandardAddress.getDz5P())){
					 addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz5P() + "排";
				 }
				 if(cjxtStandardAddress.getDz5H()!=null && !"".equals(cjxtStandardAddress.getDz5H())){
					 addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz5H() + "号";
				 }
				 if(cjxtStandardAddress.getDz5S()!=null && !"".equals(cjxtStandardAddress.getDz5S())){
					 addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz5S() + "室";
				 }

			 }else if("6".equals(cjxtStandardAddress.getDzType())){
				 if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				 }
				 if(cjxtStandardAddress.getDz6S()!=null && !"".equals(cjxtStandardAddress.getDz6S())){
					 addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDz6S() + "室";
				 }
			 }else if("99".equals(cjxtStandardAddress.getDzType())){
				 if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
					 addressName = addressName + cjxtStandardAddress.getDetailMc();
					 addressNameMz = addressNameMz + cjxtStandardAddress.getDetailMc();
				 }
			 }
			 //补充说明
			 if(cjxtStandardAddress.getDetailAddress()!=null && !"".equals(cjxtStandardAddress.getDetailAddress())){
				 addressName = addressName + "(" + cjxtStandardAddress.getDetailAddress() + ")";
				 addressNameMz = addressNameMz + "(" + cjxtStandardAddress.getDetailAddress() + ")";
			 }
			 //不动产编号
			 if(cjxtStandardAddress.getBdcbh()!=null && !"".equals(cjxtStandardAddress.getBdcbh())){
				 addressName = addressName + "(不动产编号：" + cjxtStandardAddress.getBdcbh() + ")";
				 addressNameMz = addressNameMz + "(不动产编号：" + cjxtStandardAddress.getBdcbh() + ")";
			 }
			 cjxtStandardAddress.setAddressName(addressName);
			 cjxtStandardAddress.setAddressNameMz(addressNameMz);
			 cjxtStandardAddress.setAddressDepartnameMz(sysDepart.getDepartNameFull());
			 cjxtStandardAddress.setAddressCodeMz(sysDepart.getOrgCode());
		 }
		 cjxtStandardAddressService.updateById(cjxtStandardAddress);
		 return Result.OK("编辑成功!");
	 }

	 /**
	  *  detailLm
	  *  let col = ['dz1Ld','dz1Dy','dz1S','dz2Zm','dz2Hm','dz3Ldm','dz3Sm','dz5P','dz5H','dz5S','dz6S'];
	  * @param detailLm
	  */
	 @AutoLog(value = "标准地址表-APP地址列表")
	 @ApiOperation(value="标准地址表-APP地址列表", notes="标准地址表-APP地址列表")
	 @GetMapping(value = "/addressAppList")
	 public Result<Map<String, Object>> addressAppList(@RequestParam(name="userId",required=true) String userId,
													   @RequestParam(name="detailLm",required=false) String detailLm,
													   @RequestParam(name="detailLhm",required=false) String detailLhm,
													   @RequestParam(name="detailMc",required=false) String detailMc,
													   @RequestParam(name="dz1Ld",required=false) String dz1Ld,
													   @RequestParam(name="dz1Dy",required=false) String dz1Dy,
													   @RequestParam(name="dz1S",required=false) String dz1S,
													   @RequestParam(name="dz2Zm",required=false) String dz2Zm,
													   @RequestParam(name="dz2Hm",required=false) String dz2Hm,
													   @RequestParam(name="dz3Ldm",required=false) String dz3Ldm,
													   @RequestParam(name="dz3Sm",required=false) String dz3Sm,
													   @RequestParam(name="dz5P",required=false) String dz5P,
													   @RequestParam(name="dz5H",required=false) String dz5H,
													   @RequestParam(name="dz5S",required=false) String dz5S,
													   @RequestParam(name="dz6S",required=false) String dz6S
	 ) {
		 Map<String, Object> result = new HashMap<>();
		 SysUser sysUser = sysUserService.getById(userId);
		 String zxSql = "";
//		 String sql = "select a.detail_Lm detailLm,a.detail_Lhm detailLhm,a.detail_Mc detailMc," +
//				 "a.dz1_Ld dz1Ld,a.dz1_Dy dz1Dy,a.dz1_S dz1S,a.dz2_Zm dz2Zm,a.dz2_Hm dz2Hm," +
//				 "a.dz3_Ldm dz3Ldm,a.dz3_Sm dz3Sm,a.dz5_P dz5P,a.dz5_H dz5H,a.dz5_S dz5S,a.dz6_S dz6S" +
//				 " from cjxt_standard_address a " +
//				 "where a.address_code_mz like '"+sysUser.getOrgCode()+"%' ";
		 String sql = "from cjxt_standard_address a where a.del_flag='0' and a.address_code_mz like '"+sysUser.getOrgCode()+"%' ";
		 if("2".equals(sysUser.getUserSf()) ||  "3".equals(sysUser.getUserSf())){//片警、民警
			 sql = "from cjxt_standard_address a where a.del_flag='0' and exists (select 1 from cjxt_pjwgqx b where b.pj_id='"+userId+"' and a.address_code_mz like concat(b.wg_code, '%')) ";
		 }
		 if(StringUtils.isNotEmpty(detailLm))   sql += "and a.detail_Lm = '" + detailLm + "' ";
		 if(StringUtils.isNotEmpty(detailLhm))  sql += "and a.detail_Lhm = '" + detailLhm + "' ";
		 if(StringUtils.isNotEmpty(detailMc))   sql += "and a.detail_Mc = '" + detailMc + "' ";
		 if(StringUtils.isNotEmpty(dz1Ld))      sql += "and a.dz1_Ld = '" + dz1Ld + "' ";
		 if(StringUtils.isNotEmpty(dz1Dy))      sql += "and a.dz1_Dy = '" + dz1Dy + "' ";
		 if(StringUtils.isNotEmpty(dz2Zm))      sql += "and a.dz2_Zm = '" + dz2Zm + "' ";
		 if(StringUtils.isNotEmpty(dz3Ldm))     sql += "and a.dz3_Ldm = '" + dz3Ldm + "' ";
		 if(StringUtils.isNotEmpty(dz5P))       sql += "and a.dz5_P = '" + dz5P + "' ";
		 if(StringUtils.isNotEmpty(dz5H))       sql += "and a.dz5_H = '" + dz5H + "' ";
		 //只有传了userid,其他都是空值,初始化路名和名称

		 if( StringUtils.isEmpty(detailLm)
				 && StringUtils.isEmpty(detailLhm)
				 && StringUtils.isEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 //路名
			 zxSql = "select distinct a.detail_Lm detailLm " + sql + " order by a.detail_Lm";
			 List<Map<String, Object>> objList = jdbcTemplate.queryForList(zxSql);
			 //名称
			 zxSql = "select distinct a.detail_Mc detailMc " + sql + " order by a.detail_Mc";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("detailLm",objList);
			 result.put("detailMc",objList1);
		 }else if(StringUtils.isNotEmpty(detailLm)  //路名不为空， 初始化 路号 和 名称
				 && StringUtils.isEmpty(detailLhm)
				 && StringUtils.isEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 //路号
			 zxSql = "select distinct a.detail_Lhm detailLhm " + sql + " order by a.detail_Lhm";
			 List<Map<String, Object>> objList = jdbcTemplate.queryForList(zxSql);
			 //名称
			 zxSql = "select distinct a.detail_Mc detailMc " + sql + " order by a.detail_Mc";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("detailLhm",objList);
			 result.put("detailMc",objList1);
		 }else if(StringUtils.isNotEmpty(detailLm)  //路名 路号 不为空， 初始化 名称
				 && StringUtils.isNotEmpty(detailLhm)
				 && StringUtils.isEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 //名称
			 zxSql = "select distinct a.detail_Mc detailMc " + sql + " order by a.detail_Mc";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("detailMc",objList1);
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称不为空， 初始化 具体地址，先取出 地址类型 判断，再定
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 //路号
			 zxSql = "select a.dz_type " + sql + " limit 1";
			 List<Map<String, Object>> objList = jdbcTemplate.queryForList(zxSql);
			 if(objList!=null && objList.size()>0) {
				 String dzType = objList.get(0).get("dz_type").toString();
				 //1城市家庭住宅 2农村家庭住宅 3单位大厦 4商铺 5城中村 6宿舍 99其他
				 if ("1".equals(dzType)) {
					 zxSql = "select distinct a.dz1_Ld dz1Ld " + sql + " order by a.dz1_Ld";
					 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
					 result.put("dz1Ld", objList1);
				 } else if ("2".equals(dzType)) {
					 zxSql = "select distinct a.dz2_Zm dz2Zm " + sql + " order by a.dz2_Zm";
					 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
					 result.put("dz2Zm", objList1);
				 } else if ("3".equals(dzType)) {
					 zxSql = "select distinct a.dz3_Ldm dz3Ldm " + sql + " order by a.dz3_Ldm";
					 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
					 result.put("dz3Ldm", objList1);
				 } else if ("4".equals(dzType)) {

				 } else if ("5".equals(dzType)) {
					 zxSql = "select distinct a.dz5_P dz5P " + sql + " order by a.dz5_P";
					 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
					 result.put("dz5P", objList1);
				 } else if ("6".equals(dzType)) {
					 zxSql = "select distinct a.dz6_S dz6S " + sql + " order by a.dz6_S";
					 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
					 result.put("dz6S", objList1);
				 } else if ("99".equals(dzType)) {

				 }
			 }
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称 dz1Ld不为空， 初始化 dz1Dy
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isNotEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 zxSql = "select distinct a.dz1_Dy dz1Dy " + sql + " order by a.dz1_Dy";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("dz1Dy",objList1);
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称 dz1Ld dz1Dy不为空， 初始化 dz1S
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isNotEmpty(dz1Ld)
				 && StringUtils.isNotEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 zxSql = "select distinct a.dz1_s dz1S " + sql + " order by a.dz1_s";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("dz1S",objList1);
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称 dz2Zm不为空， 初始化 dz2Hm
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isNotEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 zxSql = "select distinct a.dz2_Hm dz2Hm " + sql + " order by a.dz2_Hm";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("dz2Hm",objList1);
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称 dz3Ldm不为空， 初始化 dz3Sm
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isNotEmpty(dz3Ldm)
				 && StringUtils.isEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 zxSql = "select distinct a.dz3_Sm dz3Sm " + sql + " order by a.dz3_Sm";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("dz3Sm",objList1);
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称 dz5P不为空， 初始化 dz5H
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isNotEmpty(dz5P)
				 && StringUtils.isEmpty(dz5H)
		 ){
			 zxSql = "select distinct a.dz5_H dz5H " + sql + " order by a.dz5_H";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("dz5H",objList1);
		 }else if(/*StringUtils.isNotEmpty(detailLm)  //路名 路号 名称 dz5P dz5H不为空， 初始化 dz5S
				 && StringUtils.isNotEmpty(detailLhm)
				 && */StringUtils.isNotEmpty(detailMc)
				 && StringUtils.isEmpty(dz1Ld)
				 && StringUtils.isEmpty(dz1Dy)
				 && StringUtils.isEmpty(dz2Zm)
				 && StringUtils.isEmpty(dz3Ldm)
				 && StringUtils.isNotEmpty(dz5P)
				 && StringUtils.isNotEmpty(dz5H)
		 ){
			 zxSql = "select distinct a.dz5_S dz5S " + sql + " order by a.dz5_S";
			 List<Map<String, Object>> objList1 = jdbcTemplate.queryForList(zxSql);
			 result.put("dz5S",objList1);
		 }
		 return Result.ok(result);
	 }

	 /**
	  *  userId
	  * @param userId
	  */
	 @AutoLog(value = "标准地址表-APP用户地址列表")
	 @ApiOperation(value="标准地址表-APP用户地址列表", notes="标准地址表-APP用户地址列表")
	 @GetMapping(value = "/userAddress")
	 public Result<Map<String, Object>> userAddress(@RequestParam(name="userId",required=true) String userId,
													@RequestParam(name="mbId",required=false) String mbId,
													@RequestParam(name="mbCode",required=false) String mbCode) {
		 Map<String, Object> result = new HashMap<>();
		 List<CjxtStandardAddressPerson> personList = cjxtStandardAddressPersonService.list(new LambdaQueryWrapper<CjxtStandardAddressPerson>().eq(CjxtStandardAddressPerson::getUserId, userId));
		 List<CjxtStandardAddress> addressListLm = new ArrayList<>();
         List<CjxtStandardAddress> addressListLhm = new ArrayList<>();
         List<CjxtStandardAddress> addressListMc = new ArrayList<>();

		 //取出唯一值
		 Set<String> processedDetailLm = new HashSet<>();
		 List<CjxtStandardAddress> filteredAddressListLm = new ArrayList<>();
		 Set<Integer> processedDetailLhm = new HashSet<>();
		 List<CjxtStandardAddress> filteredAddressListLhm = new ArrayList<>();
		 Set<String> processedDetailMc = new HashSet<>();
		 List<CjxtStandardAddress> filteredAddressListMc = new ArrayList<>();

		 if (personList != null) {
			 for (CjxtStandardAddressPerson person : personList) {
				 List<CjxtStandardAddress> addressesLm = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getId, person.getAddressId()));
//				 List<CjxtStandardAddress> addressLhm = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getId, person.getAddressId()));
				 List<CjxtStandardAddress> addressesMc = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getId, person.getAddressId()));
                 addressListLm.addAll(addressesLm);
//                 addressListLhm.addAll(addressLhm);
                 addressListMc.addAll(addressesMc);
			 }
		 }
		 //路名
		 for (CjxtStandardAddress address : addressListLm) {
			 String detailLm = address.getDetailLm();
			 if (!processedDetailLm.contains(detailLm)) {
				 filteredAddressListLm.add(address);
				 processedDetailLm.add(detailLm);
			 }
		 }
		 //路号名
//		 for (CjxtStandardAddress address : addressListLhm) {
//			 Integer detailLhm = address.getDetailLhm();
//			 if (!processedDetailLhm.contains(detailLhm)) {
//				 filteredAddressListLhm.add(address);
//				 processedDetailLhm.add(detailLhm);
//			 }
//		 }
		 //名称
		 for (CjxtStandardAddress address : addressListMc) {
			 String detailMc = address.getDetailMc();
			 if (!processedDetailMc.contains(detailMc)) {
				 filteredAddressListMc.add(address);
				 processedDetailMc.add(detailMc);
			 }
		 }

		 CjxtMbgl cjxtMbgl = new CjxtMbgl();
		 if(mbId!=null&&!"".equals(mbId)&&!mbId.isEmpty()){
			 //模版
			 cjxtMbgl = cjxtMbglService.getById(mbId);
		 }else if(mbCode!=null&&!"".equals(mbCode)&&!mbCode.isEmpty()){
			 cjxtMbgl = cjxtMbglService.getOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh,mbCode));
		 }
		 result.put("detailLm",filteredAddressListLm);
		 result.put("detailLhm",filteredAddressListLhm);
		 result.put("detailMc",filteredAddressListMc);
		 if(cjxtMbgl!=null) {
			 List<CjxtMbglPz> list = cjxtMbglPzService.list(new LambdaQueryWrapper<CjxtMbglPz>().eq(CjxtMbglPz::getMbglId, cjxtMbgl.getId()).eq(CjxtMbglPz::getMbglMbbh, cjxtMbgl.getMbbh()).eq(CjxtMbglPz::getIsQuery,"1").orderByAsc(CjxtMbglPz::getOrderNum));
			 result.put("mbglQuery",list);
		 }
		 return Result.ok(result);
	 }

	 /**
	  *  detailLm
	  * @param detailLm
	  */
	 @AutoLog(value = "标准地址表-APP地址列表")
	 @ApiOperation(value="标准地址表-APP地址列表", notes="标准地址表-APP地址列表")
	 @GetMapping(value = "/addressLmList")
	 public Result<Map<String, Object>> addressLmList(@RequestParam(name="detailLm",required=true) String detailLm) {
		 Map<String, Object> result = new HashMap<>();
		 List<CjxtStandardAddress> addressListLhm = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDetailLm,detailLm));
		 List<CjxtStandardAddress> addressListMc = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDetailLm,detailLm));

		 //取出唯一值
		 Set<Integer> processedDetailLhm = new HashSet<>();
		 List<CjxtStandardAddress> filteredAddressListLhm = new ArrayList<>();
		 Set<String> processedDetailMc = new HashSet<>();
		 List<CjxtStandardAddress> filteredAddressListMc = new ArrayList<>();
		 if(addressListLhm!=null){
			 //路号名
			 for (CjxtStandardAddress address : addressListLhm) {
				 Integer detailLhm = address.getDetailLhm();
				 if (!processedDetailLhm.contains(detailLhm)) {
					 filteredAddressListLhm.add(address);
					 processedDetailLhm.add(detailLhm);
				 }
			 }
			 Collections.sort(filteredAddressListLhm, Comparator.comparing(CjxtStandardAddress::getDetailLhm));
		 }
		 if(addressListMc!=null){
			 //名称
			 for (CjxtStandardAddress address : addressListMc) {
				 String detailMc = address.getDetailMc();
				 if (!processedDetailMc.contains(detailMc)) {
					 filteredAddressListMc.add(address);
					 processedDetailMc.add(detailMc);
				 }
			 }
			 Collections.sort(filteredAddressListMc, Comparator.comparing(CjxtStandardAddress::getDetailMc));
		 }
		 result.put("detailLhm",filteredAddressListLhm);
		 result.put("detailMc",filteredAddressListMc);
		 return Result.ok(result);
	 }

	 /**
	  *  detailMc
	  * @param detailMc
	  */
	 @AutoLog(value = "标准地址表-APP名称列表")
	 @ApiOperation(value="标准地址表-APP名称列表", notes="标准地址表-APP名称列表")
	 @GetMapping(value = "/addressMcList")
	 public Result<Map<String, Object>> addressMcList(@RequestParam(name="detailMc",required=true) String detailMc) {
		 Map<String, Object> result = new HashMap<>();
		 //名称查询对应内容取第一条
		 List<CjxtStandardAddress> addressMc = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDetailMc,detailMc));
		 CjxtStandardAddress cjxtStandardAddress = new CjxtStandardAddress();
		 if(addressMc!=null){
			 cjxtStandardAddress = addressMc.get(0);
		 }
		 if(cjxtStandardAddress!=null){
			 List<CjxtStandardAddress> list = cjxtStandardAddressService.list(new LambdaQueryWrapper<CjxtStandardAddress>().eq(CjxtStandardAddress::getDetailMc,cjxtStandardAddress.getDetailMc()));
			 if("1".equals(cjxtStandardAddress.getDzType())){
				 //取出唯一值
				 Set<String> processedDetailDz1Ld = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz1Ld = new ArrayList<>();
				 Set<String> processedDetailDz1Dy = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz1Dy = new ArrayList<>();
				 Set<String> processedDetailDz1S = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz1S = new ArrayList<>();

				 if(list!=null){
					 for (CjxtStandardAddress address : list) {
						 //楼栋
						 CjxtStandardAddress addressDz1Ld = new CjxtStandardAddress();
						 addressDz1Ld.setId(address.getId());
						 addressDz1Ld.setDz1Ld(address.getDz1Ld());
						 String detailDz1Ld = address.getDz1Ld();
						 if (!processedDetailDz1Ld.contains(detailDz1Ld)) {
							 addressDz1Ld.setMcText("楼栋");
							 addressDz1Ld.setMcKey("dz1Ld");
							 filteredAddressListDz1Ld.add(addressDz1Ld);
							 processedDetailDz1Ld.add(detailDz1Ld);
						 }
						 //单元
						 CjxtStandardAddress addressDz1Dy = new CjxtStandardAddress();
						 addressDz1Dy.setId(address.getId());
						 addressDz1Dy.setDz1Dy(address.getDz1Dy());
						 String detailDz1Dy = address.getDz1Dy();
						 if (!processedDetailDz1Dy.contains(detailDz1Dy)) {
							 addressDz1Dy.setMcText("单元");
							 addressDz1Dy.setMcKey("dz1Dy");
							 filteredAddressListDz1Dy.add(addressDz1Dy);
							 processedDetailDz1Dy.add(detailDz1Dy);
						 }
						 //室
						 CjxtStandardAddress addressDz1S = new CjxtStandardAddress();
						 addressDz1S.setId(address.getId());
						 addressDz1S.setDz1S(address.getDz1S());
						 String detailDz1S = address.getDz1S();
						 if (!processedDetailDz1S.contains(detailDz1S)) {
							 addressDz1S.setMcText("室");
							 addressDz1S.setMcKey("dz1S");
							 filteredAddressListDz1S.add(addressDz1S);
							 processedDetailDz1S.add(detailDz1S);
						 }
					 }
					 Collections.sort(filteredAddressListDz1Ld, Comparator.comparing(address -> Integer.parseInt(address.getDz1Ld())));
					 Collections.sort(filteredAddressListDz1Dy, Comparator.comparing(address -> Integer.parseInt(address.getDz1Dy())));
					 Collections.sort(filteredAddressListDz1S, Comparator.comparing(address -> Integer.parseInt(address.getDz1S())));
				 }
//				 result.put("dz1LdText","楼栋");
//				 result.put("dz1DyText","单元");
//				 result.put("dz1SText","室");
				 result.put("dz1Ld",processedDetailDz1Ld);
				 result.put("dz1Dy",filteredAddressListDz1Dy);
				 result.put("dz1S",filteredAddressListDz1S);
			 }
			 if("2".equals(cjxtStandardAddress.getDzType())){
				 //取出唯一值
				 Set<String> processedDetailDz2Zm = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz2Zm = new ArrayList<>();
				 Set<String> processedDetailDz2Hm = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz2Hm = new ArrayList<>();
				 if(list!=null){
					 for (CjxtStandardAddress address : list) {
						 //组名
						 CjxtStandardAddress addressDz2Zm = new CjxtStandardAddress();
						 addressDz2Zm.setId(address.getId());
						 addressDz2Zm.setDz2Zm(address.getDz2Zm());
						 String detailDz2Zm = address.getDz2Zm();
						 if (!processedDetailDz2Zm.contains(detailDz2Zm)) {
							 addressDz2Zm.setMcText("村名");
							 addressDz2Zm.setMcKey("dz2Zm");
							 filteredAddressListDz2Zm.add(addressDz2Zm);
							 processedDetailDz2Zm.add(detailDz2Zm);
						 }
						 //号名
						 CjxtStandardAddress addressDz2Hm = new CjxtStandardAddress();
						 addressDz2Hm.setId(address.getId());
						 addressDz2Hm.setDz2Hm(address.getDz2Hm());
						 String detailDz2Hm = address.getDz2Hm();
						 if (!processedDetailDz2Hm.contains(detailDz2Hm)) {
							 addressDz2Hm.setMcText("号名");
							 addressDz2Hm.setMcKey("dz2Hm");
							 filteredAddressListDz2Hm.add(addressDz2Hm);
							 processedDetailDz2Hm.add(detailDz2Hm);
						 }
					 }
					 Collections.sort(filteredAddressListDz2Zm, Comparator.comparing(CjxtStandardAddress::getDz2Zm));
					 Collections.sort(filteredAddressListDz2Hm, Comparator.comparing(address -> Integer.parseInt(address.getDz2Hm())));
				 }
//				 result.put("dz2ZmText","组名");
//				 result.put("dz2HmText","号名");
				 result.put("dz2Zm",filteredAddressListDz2Zm);
				 result.put("dz2Hm",filteredAddressListDz2Hm);
			 }
			 if("3".equals(cjxtStandardAddress.getDzType())){
				 //取出唯一值
				 Set<String> processedDetailDz3Ldm = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz3Ldm = new ArrayList<>();
				 Set<String> processedDetailDz3Sm = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz3Sm = new ArrayList<>();
				 if(list!=null){
					 for (CjxtStandardAddress address : list) {
						 //楼栋名
						 CjxtStandardAddress addressDz3Ldm = new CjxtStandardAddress();
						 addressDz3Ldm.setId(address.getId());
						 addressDz3Ldm.setDz3Ldm(address.getDz3Ldm());
						 String detailDz3Ldm = address.getDz3Ldm();
						 if (!processedDetailDz3Ldm.contains(detailDz3Ldm)) {
							 address.setMcText("楼栋名");
							 address.setMcKey("dz3Ldm");
							 filteredAddressListDz3Ldm.add(address);
							 processedDetailDz3Ldm.add(detailDz3Ldm);
						 }
						 //室名
						 CjxtStandardAddress addressDz3Sm = new CjxtStandardAddress();
						 addressDz3Sm.setId(address.getId());
						 addressDz3Sm.setDz3Sm(address.getDz3Sm());
						 String detailDz3Sm = address.getDz3Sm();
						 if (!processedDetailDz3Sm.contains(detailDz3Sm)) {
							 addressDz3Sm.setMcText("室名");
							 addressDz3Sm.setMcKey("dz3Sm");
							 filteredAddressListDz3Sm.add(addressDz3Sm);
							 processedDetailDz3Sm.add(detailDz3Sm);
						 }
					 }
					 Collections.sort(filteredAddressListDz3Ldm, Comparator.comparing(CjxtStandardAddress::getDz3Ldm));
					 Collections.sort(filteredAddressListDz3Sm, Comparator.comparing(address -> Integer.parseInt(address.getDz3Sm())));
				 }
//				 result.put("dz3LdmText","楼栋名");
//				 result.put("dz3SmText","室名");
				 result.put("dz3Ldm",filteredAddressListDz3Ldm);
				 result.put("dz3Sm",filteredAddressListDz3Sm);
			 }
			 if("4".equals(cjxtStandardAddress.getDzType())){

			 }
			 if("5".equals(cjxtStandardAddress.getDzType())){
				 //取出唯一值
				 Set<String> processedDetailDz5P = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz5P = new ArrayList<>();
				 Set<String> processedDetailDz5H = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz5H = new ArrayList<>();
				 Set<String> processedDetailDz5S = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz5S = new ArrayList<>();

				 if(list!=null){
					 for (CjxtStandardAddress address : list) {
						 //排(排号室)
						 CjxtStandardAddress addressDz5P = new CjxtStandardAddress();
						 addressDz5P.setId(address.getId());
						 addressDz5P.setDz5P(address.getDz5P());
						 String detailDz5P = address.getDz5P();
						 if (!processedDetailDz5P.contains(detailDz5P)) {
							 addressDz5P.setMcText("排(排号室)");
							 addressDz5P.setMcKey("dz5P");
							 filteredAddressListDz5P.add(addressDz5P);
							 processedDetailDz5P.add(detailDz5P);
						 }

						 //号(排号室)
						 CjxtStandardAddress addressDz5H = new CjxtStandardAddress();
						 addressDz5H.setId(address.getId());
						 addressDz5H.setDz5H(address.getDz5H());
						 String detailDz5H = address.getDz5H();
						 if (!processedDetailDz5H.contains(detailDz5H)) {
							 addressDz5H.setMcText("号(排号室)");
							 addressDz5H.setMcKey("dz5H");
							 filteredAddressListDz5H.add(addressDz5H);
							 processedDetailDz5H.add(detailDz5H);
						 }

						 //室(宿舍)
						 CjxtStandardAddress addressDz5S = new CjxtStandardAddress();
						 addressDz5S.setId(address.getId());
						 addressDz5S.setDz5S(address.getDz5S());
						 String detailDz5S = address.getDz5S();
						 if (!processedDetailDz5S.contains(detailDz5S)) {
							 addressDz5S.setMcText("室(排号室)");
							 addressDz5S.setMcKey("dz5S");
							 filteredAddressListDz5S.add(addressDz5S);
							 processedDetailDz5S.add(detailDz5S);
						 }
					 }
					 Collections.sort(filteredAddressListDz5P, Comparator.comparing(address -> Integer.parseInt(address.getDz5P())));
					 Collections.sort(filteredAddressListDz5H, Comparator.comparing(address -> Integer.parseInt(address.getDz5H())));
					 Collections.sort(filteredAddressListDz5S, Comparator.comparing(address -> Integer.parseInt(address.getDz5S())));
//					 Collections.sort(filteredAddressListDz5P, Comparator.comparing(CjxtStandardAddress::getDz5P));
//					 Collections.sort(filteredAddressListDz5H, Comparator.comparing(CjxtStandardAddress::getDz5H));
//					 Collections.sort(filteredAddressListDz5S, Comparator.comparing(CjxtStandardAddress::getDz5S));
				 }
//				 result.put("dz5PText","排(排号室)");
//				 result.put("dz5HText","号(排号室)");
//				 result.put("dz5SText","室(排号室)");
				 result.put("dz5P",filteredAddressListDz5P);
				 result.put("dz5H",filteredAddressListDz5H);
				 result.put("dz5S",filteredAddressListDz5S);
			 }
			 if("6".equals(cjxtStandardAddress.getDzType())){
				 //取出唯一值
				 Set<String> processedDetailDz6S = new HashSet<>();
				 List<CjxtStandardAddress> filteredAddressListDz6S = new ArrayList<>();
				 if(list!=null){
					 for (CjxtStandardAddress address : list) {
						 //室(排号室)
						 String detailDz6S = address.getDz6S();
						 if (!processedDetailDz6S.contains(detailDz6S)) {
							 address.setMcText("室(宿舍)");
							 address.setMcKey("dz6S");
							 filteredAddressListDz6S.add(address);
							 processedDetailDz6S.add(detailDz6S);
						 }
					 }
					 Collections.sort(filteredAddressListDz6S, Comparator.comparing(address -> Integer.parseInt(address.getDz6S())));
//					 Collections.sort(filteredAddressListDz6S, Comparator.comparing(CjxtStandardAddress::getDz6S));

				 }
//				 result.put("dz6SText","室(排号室)");
				 result.put("dz6S",filteredAddressListDz6S);
			 }
			 if("99".equals(cjxtStandardAddress.getDzType())){

			 }
		 }
		 return Result.ok(result);
	 }

	 /**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "标准地址表-通过id删除")
	@ApiOperation(value="标准地址表-通过id删除", notes="标准地址表-通过id删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtStandardAddressService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "标准地址表-批量删除")
	@ApiOperation(value="标准地址表-批量删除", notes="标准地址表-批量删除")
//	@RequiresPermissions("cjxt:cjxt_standard_address:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtStandardAddressService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "标准地址表-通过id查询")
	@ApiOperation(value="标准地址表-通过id查询", notes="标准地址表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtStandardAddress> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(id);
		String addressName = "";
		if("1".equals(cjxtStandardAddress.getDzType())){
			//小区名
			if(cjxtStandardAddress.getDz1Xqm()!=null && !"".equals(cjxtStandardAddress.getDz1Xqm())){
				addressName = addressName + cjxtStandardAddress.getDz1Xqm();
			}
			//楼栋
			if(cjxtStandardAddress.getDz1Ld()!=null && !"".equals(cjxtStandardAddress.getDz1Ld())){
				addressName = addressName + cjxtStandardAddress.getDz1Ld() + "号楼";
			}
			//单元
			if(cjxtStandardAddress.getDz1Dy()!=null && !"".equals(cjxtStandardAddress.getDz1Dy())){
				addressName = addressName + cjxtStandardAddress.getDz1Dy() + "单元";
			}
			//室
			if(cjxtStandardAddress.getDz1S()!=null && !"".equals(cjxtStandardAddress.getDz1S())){
				addressName = addressName + cjxtStandardAddress.getDz1S() + "室";
			}
		}else if("2".equals(cjxtStandardAddress.getDzType())){
			cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz2Cm());
			//村名
			if(cjxtStandardAddress.getDz2Cm()!=null && !"".equals(cjxtStandardAddress.getDz2Cm())){
				addressName = addressName + cjxtStandardAddress.getDz2Cm();
			}
			//组名
			if(cjxtStandardAddress.getDz2Zm()!=null && !"".equals(cjxtStandardAddress.getDz2Zm())){
				addressName = addressName + cjxtStandardAddress.getDz2Zm() + "组";
			}
			//号名
			if(cjxtStandardAddress.getDz2Hm()!=null && !"".equals(cjxtStandardAddress.getDz2Hm())){
				addressName = addressName + cjxtStandardAddress.getDz2Hm() + "号";
			}

		}else if("3".equals(cjxtStandardAddress.getDzType())){
			cjxtStandardAddress.setDetailMc(cjxtStandardAddress.getDz3Dsm());
			//大厦名
			if(cjxtStandardAddress.getDz3Dsm()!=null && !"".equals(cjxtStandardAddress.getDz3Dsm())){
				addressName = addressName + cjxtStandardAddress.getDz3Dsm();
			}
			//楼栋名
			if(cjxtStandardAddress.getDz3Ldm()!=null && !"".equals(cjxtStandardAddress.getDz3Ldm())){
				addressName = addressName + cjxtStandardAddress.getDz3Ldm() + "栋";
			}
			//室名
			if(cjxtStandardAddress.getDz3Sm()!=null && !"".equals(cjxtStandardAddress.getDz3Sm())){
				addressName = addressName + cjxtStandardAddress.getDz3Sm() + "室";
			}
		}else if("4".equals(cjxtStandardAddress.getDzType())){
			if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
				addressName = addressName + cjxtStandardAddress.getDetailMc();
			}
		}else if("5".equals(cjxtStandardAddress.getDzType())){
			if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
				addressName = addressName + cjxtStandardAddress.getDetailMc();
			}
			if(cjxtStandardAddress.getDz5P()!=null && !"".equals(cjxtStandardAddress.getDz5P())){
				addressName = addressName + cjxtStandardAddress.getDz5P() + "排";
			}
			if(cjxtStandardAddress.getDz5H()!=null && !"".equals(cjxtStandardAddress.getDz5H())){
				addressName = addressName + cjxtStandardAddress.getDz5H() + "号";
			}
			if(cjxtStandardAddress.getDz5S()!=null && !"".equals(cjxtStandardAddress.getDz5S())){
				addressName = addressName + cjxtStandardAddress.getDz5S() + "室";
			}

		}else if("6".equals(cjxtStandardAddress.getDzType())){
			if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
				addressName = addressName + cjxtStandardAddress.getDetailMc();
			}
			if(cjxtStandardAddress.getDz6S()!=null && !"".equals(cjxtStandardAddress.getDz6S())){
				addressName = addressName + cjxtStandardAddress.getDz6S() + "室";
			}
		}else if("99".equals(cjxtStandardAddress.getDzType())){
			if(cjxtStandardAddress.getDetailMc()!=null && !"".equals(cjxtStandardAddress.getDetailMc())){
				addressName = addressName + cjxtStandardAddress.getDetailMc();
			}
		}
		cjxtStandardAddress.setAddressName(addressName);
		if(cjxtStandardAddress==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtStandardAddress);
	}

	 /**
	  * 地址派出所信息
	  *
	  * @param id
	  * @return
	  */
	 //@AutoLog(value = "标准地址表-通过id查询")
	 @ApiOperation(value="标准地址表-通过地址id查询", notes="标准地址表-通过地址id查询")
	 @GetMapping(value = "/queryByPoc")
	 public Result<Map<String, Object>> mbIsDb(
			 @RequestParam(required = false, name="id") String id, // 地址Id
			 @RequestParam(required = false, name="userId") String userId, // 用户Id
			 HttpServletRequest req) {
		 Map<String, Object> result = new HashMap<>();
		 //地址部门信息
		 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(id);

		 SysDepart addressDepart = sysDepartService.getById(cjxtStandardAddress.getAddressIdMz());
		 String orgCode = "";
		 if("9".equals(addressDepart.getOrgCategory())){
			 int lastIndex = addressDepart.getOrgCode().lastIndexOf('A');
			 if (lastIndex != -1) {
				 orgCode = addressDepart.getOrgCode().substring(0, lastIndex);
			 }
		 }
		 if("10".equals(addressDepart.getOrgCategory())){
			 int lastIndex = addressDepart.getOrgCode().lastIndexOf('A');
			 int secondLastIndex = addressDepart.getOrgCode().lastIndexOf('A', lastIndex - 1);
			 if (secondLastIndex != -1) {
				 orgCode = addressDepart.getOrgCode().substring(0, secondLastIndex);
			 }
		 }
		 //派出所信息
		 List<Map<String, Object>> resultList = new ArrayList<>();
		 String dataSql = "SELECT bm.org_id, bm.org_name " +
				 "FROM cjxt_bm_data bm " +
				 "INNER JOIN sys_depart sd ON bm.org_id = sd.id " +
				 "WHERE bm.del_flag = '0' " +
				 "AND sd.org_category IN ('4', '5') " +
				 "AND bm.data_org_code LIKE '"+ orgCode +"%' LIMIT 1 ";
		 resultList = jdbcTemplate.queryForList(dataSql);
		 String orgId = "";
		 String orgName = "";
		 SysUser user = null;
		 if (!resultList.isEmpty()) {
			 Map<String, Object> row = resultList.get(0);
			 orgId = (String) row.get("org_id");
			 orgName = (String) row.get("org_name");
			 String sql = "SELECT user_id FROM sys_user_depart WHERE dep_id = '"+orgId+"' LIMIT 1";
			 List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
			 if (!results.isEmpty()) {
				 Map<String, Object> userIdRow = results.get(0);
				 for(int i=0;i<results.size();i++){
					 user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId,(String) userIdRow.get("user_id")));
					 if(user!=null){
						 break;
					 }
				 }
			 }
		 }
		 if(user!=null && !"".equals(orgName)){
			 result.put("pocName",orgName);
			 result.put("pocUser",user.getRealname());
			 result.put("pocUserPhone",user.getPhone());
		 }else {
			 result.put("pocName",orgName);
			 result.put("pocUser","");
			 result.put("pocUserPhone","");
		 }
		 return Result.OK(result);
	 }

	 /**
	  * 地址ID任务查询
	  * @param addressId 地址ID
	  * @param req
	  * @return
	  */
	 @ApiOperation(value="标准地址表-地址ID任务查询", notes="标准地址表-地址ID任务查询")
	 @GetMapping(value = "/addressIdTask")
	 public Result<Map<String, Object>> addressIdTask(
			 @RequestParam(required = false, name="addressId") String addressId,
			 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
			 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
			 HttpServletRequest req) {
		 Map<String, Object> result = new HashMap<>();
		 CjxtTaskDispatch cjxtTaskDispatch = cjxtTaskDispatchService.getOne(new LambdaQueryWrapper<CjxtTaskDispatch>().eq(CjxtTaskDispatch::getRwzt,"2").eq(CjxtTaskDispatch::getAddressId,addressId).orderByDesc(CjxtTaskDispatch::getCreateTime).last("LIMIT 1"));
		 CjxtMbgl cjxtMbgl = null;
		 if(cjxtTaskDispatch!=null){
			 result.put("mbCode",cjxtTaskDispatch.getMbCode());
			 result.put("task",cjxtTaskDispatch);
			 result.put("Status","TASK_VALUE");
		 } else {
			 String sql = "";
			 List<Map<String, Object>> sqlList = new ArrayList<>();
			 CjxtMbgl cMbgl = null;
			 List<CjxtMbgl> mbglList = cjxtMbglService.list();

			 for(CjxtMbgl mbgl: mbglList){
				sql = "SElECT * FROM " + mbgl.getBm() + " t WHERE t.del_flag = '0' AND t.address_id = '" + addressId + "'";
				sqlList = jdbcTemplate.queryForList(sql);
				if(sqlList.size()>0){
					cMbgl = mbgl;
					break;
				}
			 }

			 if(cMbgl!=null && sqlList.size()>0){
				 result.put("mbCode",cMbgl.getMbbh());
				 result.put("Status","DATA_VALUE");
			 }else {
				 CjxtStandardAddress cjxtStandardAddress = cjxtStandardAddressService.getById(addressId);
				 //地址模版信息
				 SysDictItem sysDictItem = sysDictItemService.getOne(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getItemValue,cjxtStandardAddress.getDzType()).inSql(SysDictItem::getDictId,"SELECT id FROM sys_dict WHERE dict_code = 'dzlx'"));
				 if(sysDictItem!=null){
					 cjxtStandardAddress.setDzmbCodeDto(sysDictItem.getDescription());
				 }
				 result.put("standardAddress",cjxtStandardAddress);
				 result.put("Status","ADD_VALUE");
			 }
		 }
		 return Result.OK(result);
	 }

	 @ApiOperation(value="标准地址表-地址ID二维码查询", notes="标准地址表-地址ID二维码查询")
	 @GetMapping(value = "/addressIdQrCode")
	 public Result<Map<String, Object>> addressIdQrCode(
			 @RequestParam(required = false, name="addressId") String addressId,
			 HttpServletRequest req) {
		Map<String, Object> result = new HashMap<>();
		CjxtStandardAddress standardAddress = cjxtStandardAddressService.getById(addressId);
		if(standardAddress!=null){
			if(standardAddress.getCyryQrcode()!=null && !"".equals(standardAddress.getCyryQrcode())){
				result.put("cyryQrcode",minioUrl+"/"+bucketName+"/"+standardAddress.getCyryQrcode());
			}
			if(standardAddress.getAddressMpQrcode()!=null && !"".equals(standardAddress.getAddressMpQrcode())){
				result.put("addressMpQrcode",minioUrl+"/"+bucketName+"/"+standardAddress.getAddressMpQrcode());
			}
			if(standardAddress.getTaskAddressQrcode()!=null && !"".equals(standardAddress.getTaskAddressQrcode())){
				result.put("taskAddressQrcode",minioUrl+"/"+bucketName+"/"+standardAddress.getTaskAddressQrcode());
			}
		}
		return Result.OK(result);
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtStandardAddress
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtStandardAddress cjxtStandardAddress) {
        return super.exportXls(request, cjxtStandardAddress, CjxtStandardAddress.class, "标准地址表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
//    @RequiresPermissions("cjxt:cjxt_standard_address:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtStandardAddress.class);
    }

}
