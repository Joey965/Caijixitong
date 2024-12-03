package org.jeecg.modules.demo.cjxt.service.impl;

import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
import org.jeecg.modules.demo.cjxt.mapper.CjxtJsmbpzDtlMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtJsmbpzDtlService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 角色模版配置子表
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
@Service
public class CjxtJsmbpzDtlServiceImpl extends ServiceImpl<CjxtJsmbpzDtlMapper, CjxtJsmbpzDtl> implements ICjxtJsmbpzDtlService {
	
	@Autowired
	private CjxtJsmbpzDtlMapper cjxtJsmbpzDtlMapper;
	
	@Override
	public List<CjxtJsmbpzDtl> selectByMainId(String mainId) {
		return cjxtJsmbpzDtlMapper.selectByMainId(mainId);
	}
}
