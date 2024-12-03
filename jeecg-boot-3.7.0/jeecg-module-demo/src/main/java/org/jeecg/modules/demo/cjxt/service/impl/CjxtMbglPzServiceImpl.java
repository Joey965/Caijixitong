package org.jeecg.modules.demo.cjxt.service.impl;

import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import org.jeecg.modules.demo.cjxt.mapper.CjxtMbglPzMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtMbglPzService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 动态模板配置
 * @Author: jeecg-boot
 * @Date:   2024-06-03
 * @Version: V1.0
 */
@Service
public class CjxtMbglPzServiceImpl extends ServiceImpl<CjxtMbglPzMapper, CjxtMbglPz> implements ICjxtMbglPzService {
	
	@Autowired
	private CjxtMbglPzMapper cjxtMbglPzMapper;
	
	@Override
	public List<CjxtMbglPz> selectByMainId(String mainId) {
		return cjxtMbglPzMapper.selectByMainId(mainId);
	}

	@Override
	public List<CjxtMbglPz> selectByMainIdCommm(String mainId) {
		return cjxtMbglPzMapper.selectByMainIdCommm(mainId);
	}
}
