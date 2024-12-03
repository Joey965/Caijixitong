package org.jeecg.modules.demo.cjxt.service;

import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbgl;
import com.baomidou.mybatisplus.extension.service.IService;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 模板管理
 * @Author: jeecg-boot
 * @Date:   2024-06-03
 * @Version: V1.0
 */
public interface ICjxtMbglService extends IService<CjxtMbgl> {

	/**
	 * 添加一对多
	 *
	 * @param cjxtMbgl
	 * @param cjxtMbglPzList
	 */
	public void saveMain(CjxtMbgl cjxtMbgl,List<CjxtMbglPz> cjxtMbglPzList) ;
	
	/**
	 * 修改一对多
	 *
   * @param cjxtMbgl
   * @param cjxtMbglPzList
	 */
	public void updateMain(CjxtMbgl cjxtMbgl,List<CjxtMbglPz> cjxtMbglPzList);
	
	/**
	 * 删除一对多
	 *
	 * @param id
	 */
	public void delMain (String id);
	
	/**
	 * 批量删除一对多
	 *
	 * @param idList
	 */
	public void delBatchMain (Collection<? extends Serializable> idList);
	
}
