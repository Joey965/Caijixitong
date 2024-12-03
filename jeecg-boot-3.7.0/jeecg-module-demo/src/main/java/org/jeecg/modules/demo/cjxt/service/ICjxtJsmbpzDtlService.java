package org.jeecg.modules.demo.cjxt.service;

import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 角色模版配置子表
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
public interface ICjxtJsmbpzDtlService extends IService<CjxtJsmbpzDtl> {

	/**
	 * 通过主表id查询子表数据
	 *
	 * @param mainId 主表id
	 * @return List<CjxtJsmbpzDtl>
	 */
	public List<CjxtJsmbpzDtl> selectByMainId(String mainId);
}
