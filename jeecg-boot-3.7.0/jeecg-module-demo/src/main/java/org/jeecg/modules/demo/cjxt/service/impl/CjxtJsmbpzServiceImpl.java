package org.jeecg.modules.demo.cjxt.service.impl;

import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpz;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
import org.jeecg.modules.demo.cjxt.mapper.CjxtJsmbpzDtlMapper;
import org.jeecg.modules.demo.cjxt.mapper.CjxtJsmbpzMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtJsmbpzService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Collection;

/**
 * @Description: 角色模版配置
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
@Service
public class CjxtJsmbpzServiceImpl extends ServiceImpl<CjxtJsmbpzMapper, CjxtJsmbpz> implements ICjxtJsmbpzService {

	@Autowired
	private CjxtJsmbpzMapper cjxtJsmbpzMapper;
	@Autowired
	private CjxtJsmbpzDtlMapper cjxtJsmbpzDtlMapper;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(CjxtJsmbpz cjxtJsmbpz, List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList) {
		cjxtJsmbpzMapper.insert(cjxtJsmbpz);
		if(cjxtJsmbpzDtlList!=null && cjxtJsmbpzDtlList.size()>0) {
			for(CjxtJsmbpzDtl entity:cjxtJsmbpzDtlList) {
				//外键设置
				entity.setId(null);
				entity.setJsmbpzId(cjxtJsmbpz.getId());
				entity.setRoleCode(cjxtJsmbpz.getRoleCode());
				entity.setRoleName(cjxtJsmbpz.getRoleName());
				entity.setMbId(cjxtJsmbpz.getMbId());
				entity.setMbbh(cjxtJsmbpz.getMbbh());
				entity.setMbname(cjxtJsmbpz.getMbname());
				if("true".equals(entity.getDbShow())){
					cjxtJsmbpzDtlMapper.insert(entity);
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(CjxtJsmbpz cjxtJsmbpz,List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList) {
		cjxtJsmbpzMapper.updateById(cjxtJsmbpz);
		
		//1.先删除子表数据
		cjxtJsmbpzDtlMapper.deleteByMainId(cjxtJsmbpz.getId());
		
		//2.子表数据重新插入
		if(cjxtJsmbpzDtlList!=null && cjxtJsmbpzDtlList.size()>0) {
			for(CjxtJsmbpzDtl entity:cjxtJsmbpzDtlList) {
				//外键设置
				entity.setId(null);
				entity.setJsmbpzId(cjxtJsmbpz.getId());
				entity.setRoleCode(cjxtJsmbpz.getRoleCode());
				entity.setRoleName(cjxtJsmbpz.getRoleName());
				entity.setMbId(cjxtJsmbpz.getMbId());
				entity.setMbbh(cjxtJsmbpz.getMbbh());
				entity.setMbname(cjxtJsmbpz.getMbname());
				if("true".equals(entity.getDbShow())){
					cjxtJsmbpzDtlMapper.insert(entity);
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		cjxtJsmbpzDtlMapper.deleteByMainId(id);
		cjxtJsmbpzMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			cjxtJsmbpzDtlMapper.deleteByMainId(id.toString());
			cjxtJsmbpzMapper.deleteById(id);
		}
	}
	
}
