package org.jeecg.modules.demo.cjxt.service;

import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 动态模板配置
 * @Author: jeecg-boot
 * @Date:   2024-06-03
 * @Version: V1.0
 */
public interface ICjxtMbglPzService extends IService<CjxtMbglPz> {

	/**
	 * 通过主表id查询子表数据
	 *
	 * @param mainId 主表id
	 * @return List<CjxtMbglPz>
	 */
	public List<CjxtMbglPz> selectByMainId(String mainId);

	public List<CjxtMbglPz> selectByMainIdCommm(String mainId);
}
