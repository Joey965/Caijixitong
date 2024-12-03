package org.jeecg.modules.demo.cjxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.modules.demo.cjxt.entity.CjxtGtzd;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbgl;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import org.jeecg.modules.demo.cjxt.entity.OnlCgformHead;
import org.jeecg.modules.demo.cjxt.mapper.CjxtGtzdMapper;
import org.jeecg.modules.demo.cjxt.mapper.CjxtMbglPzMapper;
import org.jeecg.modules.demo.cjxt.mapper.CjxtMbglMapper;
import org.jeecg.modules.demo.cjxt.mapper.OnlCgformHeadsMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtMbglService;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.mapper.SysDictItemMapper;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Collection;

/**
 * @Description: 模板管理
 * @Author: jeecg-boot
 * @Date:   2024-06-03
 * @Version: V1.0
 */
@Service
public class CjxtMbglServiceImpl extends ServiceImpl<CjxtMbglMapper, CjxtMbgl> implements ICjxtMbglService {

	@Autowired
	private CjxtMbglMapper cjxtMbglMapper;
	@Autowired
	private CjxtMbglPzMapper cjxtMbglPzMapper;
	@Autowired
	private CjxtGtzdMapper cjxtGtzdMapper;
	@Autowired
	private OnlCgformHeadsMapper onlCgformHeadsMapper;
	@Autowired
	private SysDictItemMapper sysDictItemMapper;
	@Autowired
	private SysDictMapper sysDictMapper;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(CjxtMbgl cjxtMbgl, List<CjxtMbglPz> cjxtMbglPzList) {
		SysDict sysDict = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode,"jxcs"));
		String bmTitle = "";
		String MBBH = "" ;
		if(sysDict!=null){
			SysDictItem sysDictItem = sysDictItemMapper.selectOne(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId,sysDict.getId()).eq(SysDictItem::getItemValue,cjxtMbgl.getMblx()));
			if(sysDictItem!=null&&sysDictItem.getDescription()!=null&&!"".equals(sysDictItem.getDescription())&&!sysDictItem.getDescription().isEmpty()){
				bmTitle = sysDictItem.getDescription();
			}else if("1".equals(cjxtMbgl.getMblx())){
				bmTitle = "FW";
			}else if("2".equals(cjxtMbgl.getMblx())){
				bmTitle = "RY";
			}else if("3".equals(cjxtMbgl.getMblx())){
				bmTitle = "DW";
			}else if("4".equals(cjxtMbgl.getMblx())){
				bmTitle = "JXCS";
			}else if("5".equals(cjxtMbgl.getMblx())){
				bmTitle = "ZXCJ";
			}else {
				bmTitle = "数据错误模板有误";
			}
		}
		// 生成MBBH
		String serialNumber = "001";
		MBBH = bmTitle + serialNumber;

		// 检查数据库中是否存在相同的MBBH，如果存在则递增序号
		while (cjxtMbglMapper.selectOne(new LambdaQueryWrapper<CjxtMbgl>().eq(CjxtMbgl::getMbbh, MBBH)) != null) {
			int currentSerialNumber = Integer.parseInt(serialNumber);
			currentSerialNumber++;
			serialNumber = String.format("%03d", currentSerialNumber);
			MBBH = bmTitle + serialNumber;
		}
		cjxtMbgl.setMbbh(MBBH);
		cjxtMbgl.setBdfg("2");
		cjxtMbglMapper.insert(cjxtMbgl);
		int zdNum = 0;
		if(cjxtMbglPzList!=null && cjxtMbglPzList.size()>0) {
			int i = 0;
			List<CjxtGtzd> cjxtGtzdList = cjxtGtzdMapper.selectList(new LambdaQueryWrapper<CjxtGtzd>().eq(CjxtGtzd::getZdmb,"1").eq(CjxtGtzd::getYmsx,"3").orderByAsc(CjxtGtzd::getZdnum));
			if(cjxtGtzdList.size()>0){
				for(CjxtGtzd cjxtGtzd : cjxtGtzdList){
					i++;
					CjxtMbglPz cjxtMbglPz = new CjxtMbglPz();
					cjxtMbglPz.setCreateBy("admin");
					cjxtMbglPz.setCreateTime(new Date());
					cjxtMbglPz.setDbFieldName(cjxtGtzd.getZdname());
					cjxtMbglPz.setDbFieldTxt(cjxtGtzd.getZdbz());
					cjxtMbglPz.setDbLength(cjxtGtzd.getZdcd());
					cjxtMbglPz.setDbType(cjxtGtzd.getZdlx());
					cjxtMbglPz.setDbDefaultVal(cjxtGtzd.getMrz());
					cjxtMbglPz.setFieldShowType(cjxtGtzd.getKjlx());
					cjxtMbglPz.setDbIsKey(cjxtGtzd.getIsKey());
					cjxtMbglPz.setMbglId(cjxtMbgl.getId());
					cjxtMbglPz.setOrderNum(i);
					cjxtMbglPz.setIsCommon("1");
					if("address".equals(cjxtGtzd.getZdname())){
						cjxtMbglPz.setIsCommon("2");
					}
					cjxtMbglPz.setMbglMbbh(MBBH);
					cjxtMbglPz.setIsShowList("0");
					cjxtMbglPz.setIsShowFrom("0");
					cjxtMbglPzMapper.insert(cjxtMbglPz);
				}
			}
			boolean hasAddressField = false;
			for (CjxtMbglPz entity : cjxtMbglPzList) {
				if ("address".equals(entity.getDbFieldName())) {
					hasAddressField = true;
					break;
				}
			}
			if(hasAddressField==false){
				List<CjxtMbglPz> mbglPzList = cjxtMbglPzMapper.selectByMainId("1");
				if(mbglPzList!=null){
					for(CjxtMbglPz gtpz:mbglPzList){
						i++;
						//外键设置
						gtpz.setOrderNum(i);
						gtpz.setMbglId(cjxtMbgl.getId());
						if("id".equals(gtpz.getDbFieldName())){
							gtpz.setDbIsKey("1");
						}
						if(!"".equals(gtpz.getId())||!gtpz.getId().isEmpty()){
							gtpz.setId("");
						}
						gtpz.setIsShowFrom("1");
						gtpz.setIsShowList("1");
						if("file".equals(gtpz.getFieldShowType())||"1".equals(gtpz.getIsTitle())){
							gtpz.setIsShowList("0");
						}
						if("Date".equals(gtpz.getDbType()) || "Datetime".equals(gtpz.getDbType()) || "date".equals(gtpz.getFieldShowType()) || "datetime".equals(gtpz.getFieldShowType())){
							gtpz.setDbLength("0");
						} else if ("image".equals(gtpz.getFieldShowType()) || "textarea".equals(gtpz.getFieldShowType()) || "file".equals(gtpz.getFieldShowType())) {
							gtpz.setDbLength("1000");
						}
						gtpz.setIsQuery("1");
						gtpz.setMbglMbbh(MBBH);
						cjxtMbglPzMapper.insert(gtpz);
						zdNum++;
					}
				}
			}
			for(CjxtMbglPz entity:cjxtMbglPzList) {
				if(entity.getDbFieldName()==null||"".equals(entity.getDbFieldName())||entity.getDbFieldName().isEmpty()){
					continue;
				}
				i++;
				//外键设置
				entity.setOrderNum(i);
				entity.setMbglId(cjxtMbgl.getId());
				if("id".equals(entity.getDbFieldName())){
					entity.setDbIsKey("1");
				}
				if(!"".equals(entity.getId())||!entity.getId().isEmpty()){
					entity.setId("");
				}
				entity.setIsShowFrom("1");
				entity.setIsShowList("1");
				if("file".equals(entity.getFieldShowType())||"1".equals(entity.getIsTitle())){
					entity.setIsShowList("0");
				}
				if("Date".equals(entity.getDbType()) || "Datetime".equals(entity.getDbType()) || "date".equals(entity.getFieldShowType()) || "datetime".equals(entity.getFieldShowType())){
					entity.setDbLength("0");
				} else if ("image".equals(entity.getFieldShowType()) || "textarea".equals(entity.getFieldShowType()) || "file".equals(entity.getFieldShowType())) {
					entity.setDbLength("1000");
				}
				entity.setMbglMbbh(MBBH);
				cjxtMbglPzMapper.insert(entity);
				zdNum++;
			}
		}
		cjxtMbgl.setZdnum(String.valueOf(zdNum));
		cjxtMbglMapper.updateById(cjxtMbgl);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(CjxtMbgl cjxtMbgl,List<CjxtMbglPz> cjxtMbglPzList) {
		OnlCgformHead onlCgformHead = new OnlCgformHead();
		cjxtMbglMapper.updateById(cjxtMbgl);

		//1.先删除子表数据（原逻辑直接删除）
		cjxtMbglPzMapper.deleteByMainId(cjxtMbgl.getId());

		//2.子表数据重新插入
		if(cjxtMbglPzList!=null && cjxtMbglPzList.size()>0) {
			//如果字段数不相等修改同步状态
//			if(cjxtMbgl.getZdnum()!=null&&!"".equals(cjxtMbgl.getZdnum())){
//				if(!cjxtMbgl.getZdnum().equals(Integer.toString(cjxtMbglPzList.size()))&&Integer.parseInt(cjxtMbgl.getZdnum())!=cjxtMbglPzList.size()){
//					cjxtMbgl.setIsDb("0");
//					cjxtMbgl.setZdnum(String.valueOf(cjxtMbglPzList.size()));
//					cjxtMbglMapper.updateById(cjxtMbgl);
//
//					//onlCgformHead动态表单同步状态
//					onlCgformHead.setId(cjxtMbgl.getId());
//					onlCgformHead.setIsDbSynch("N");
//					onlCgformHeadsMapper.updateById(onlCgformHead);
//				}
//			}
//
//			CjxtGtzd gtzd = cjxtGtzdMapper.selectOne(new QueryWrapper<CjxtGtzd>().orderByDesc("zdnum").last("LIMIT 1"));

			int i = 0;
			List<CjxtGtzd> cjxtGtzdList = cjxtGtzdMapper.selectList(new LambdaQueryWrapper<CjxtGtzd>().eq(CjxtGtzd::getZdmb,"1").eq(CjxtGtzd::getYmsx,"3").orderByAsc(CjxtGtzd::getZdnum));
			if(cjxtGtzdList.size()>0){
				for(CjxtGtzd cjxtGtzd : cjxtGtzdList){
					i++;
					CjxtMbglPz cjxtMbglPz = new CjxtMbglPz();
					cjxtMbglPz.setCreateBy("admin");
					cjxtMbglPz.setCreateTime(new Date());
					cjxtMbglPz.setDbFieldName(cjxtGtzd.getZdname());
					cjxtMbglPz.setDbFieldTxt(cjxtGtzd.getZdbz());
					cjxtMbglPz.setDbLength(cjxtGtzd.getZdcd());
					cjxtMbglPz.setDbType(cjxtGtzd.getZdlx());
					cjxtMbglPz.setDbDefaultVal(cjxtGtzd.getMrz());
					cjxtMbglPz.setFieldShowType(cjxtGtzd.getKjlx());
					cjxtMbglPz.setDbIsKey(cjxtGtzd.getIsKey());
					cjxtMbglPz.setMbglId(cjxtMbgl.getId());
					cjxtMbglPz.setOrderNum(i);
					cjxtMbglPz.setIsCommon("1");
					if("address".equals(cjxtGtzd.getZdname())){
						cjxtMbglPz.setIsCommon("2");
					}
					cjxtMbglPz.setMbglMbbh(cjxtMbgl.getMbbh());
					cjxtMbglPz.setIsShowList("0");
					cjxtMbglPz.setIsShowFrom("0");
					cjxtMbglPzMapper.insert(cjxtMbglPz);
				}
			}

			for(CjxtMbglPz entity:cjxtMbglPzList) {
				boolean ifUpt = true;
				String updateId = "";
				CjxtMbglPz cjxtMbglPz = cjxtMbglPzMapper.selectById(entity.getId());
				i++;
				if(cjxtMbglPz!=null){
					updateId = cjxtMbglPz.getId();
					ifUpt = true;
				}else {
					ifUpt = false;
				}
				//外键设置
				entity.setMbglId(cjxtMbgl.getId());
				entity.setOrderNum(i);
				//主键设置
				if("id".equals(entity.getDbFieldName())){
					entity.setDbIsKey("1");
				}
				entity.setIsShowFrom("1");
				entity.setIsShowList("1");
				entity.setMbglMbbh(cjxtMbgl.getMbbh());
				if("file".equals(entity.getFieldShowType())){
					entity.setIsShowList("0");
				}
				//字段名称变更修改同步状态
				if(entity.getDbFieldNameDto()!=null&&!"".equals(entity.getDbFieldNameDto())&&!entity.getDbFieldNameDto().isEmpty()){
					if(!entity.getDbFieldNameDto().equals(entity.getDbFieldName())){
						entity.setDbFieldNameOld(entity.getDbFieldNameDto());
						cjxtMbgl.setIsDb("0");
						cjxtMbglMapper.updateById(cjxtMbgl);

						//onlCgformHead动态表单同步状态
						onlCgformHead.setId(cjxtMbgl.getId());
						onlCgformHead.setIsDbSynch("N");
						onlCgformHeadsMapper.updateById(onlCgformHead);
					}
				}
				//字段长度变更修改同步状态
				if(entity.getDbLengthDto()!=null && !"".equals(entity.getDbLengthDto()) && !entity.getDbLengthDto().isEmpty()){
					if(!entity.getDbLengthDto().equals(entity.getDbLength())){
						entity.setDbLengthOld(entity.getDbLengthDto());
						cjxtMbgl.setIsDb("0");
						cjxtMbglMapper.updateById(cjxtMbgl);

						//onlCgformHead动态表单同步状态
						onlCgformHead.setId(cjxtMbgl.getId());
						onlCgformHead.setIsDbSynch("N");
						onlCgformHeadsMapper.updateById(onlCgformHead);
					}
				}
				//字段类型变更修改同步状态
				if(entity.getDbTypeDto()!=null && !"".equals(entity.getDbTypeDto()) && !entity.getDbTypeDto().isEmpty()){
					if(!entity.getDbTypeDto().equals(entity.getDbType())){
						entity.setDbTypeOld(entity.getDbTypeDto());
						cjxtMbgl.setIsDb("0");
						cjxtMbglMapper.updateById(cjxtMbgl);

						//onlCgformHead动态表单同步状态
						onlCgformHead.setId(cjxtMbgl.getId());
						onlCgformHead.setIsDbSynch("N");
						onlCgformHeadsMapper.updateById(onlCgformHead);
					}
				}
				//字段校验类型
				if(entity.getDbJylxDto()!=null && !"".equals(entity.getDbJylxDto()) && !entity.getDbJylxDto().isEmpty()){
					if(!entity.getDbJylxDto().equals(entity.getDbJylx())){
						cjxtMbgl.setIsDb("0");
						cjxtMbgl.setZdnum(String.valueOf(i));
						cjxtMbglMapper.updateById(cjxtMbgl);
					}
				}
				if(ifUpt==true){
					cjxtMbglPzMapper.updateById(entity);
					//删除标识
					CjxtMbglPz pz = new CjxtMbglPz();
					pz.setId(updateId);
					pz.setDelFlag("0");
					cjxtMbglPzMapper.updateById(pz);
				}else {
					cjxtMbglPzMapper.insert(entity);
				}
			}
			if(cjxtMbgl.getZdnum()!=null && !"".equals(cjxtMbgl.getZdnum())){
				if(Integer.parseInt(cjxtMbgl.getZdnum()) != i){
					cjxtMbgl.setIsDb("0");
					cjxtMbgl.setZdnum(String.valueOf(i));
					cjxtMbglMapper.updateById(cjxtMbgl);

					//onlCgformHead动态表单同步状态
					onlCgformHead.setId(cjxtMbgl.getId());
					onlCgformHead.setIsDbSynch("N");
					onlCgformHeadsMapper.updateById(onlCgformHead);
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		cjxtMbglPzMapper.deleteByMainIdDelFlag(id);
		cjxtMbglMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			cjxtMbglPzMapper.deleteByMainId(id.toString());
			cjxtMbglMapper.deleteById(id);
		}
	}
	
}
