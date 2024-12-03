package org.jeecg.modules.demo.cjxt.service;

import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpz;
import com.baomidou.mybatisplus.extension.service.IService;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 角色模版配置
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
public interface ICjxtJsmbpzService extends IService<CjxtJsmbpz> {

	/**
	 * 添加一对多
	 *
	 * @param cjxtJsmbpz
	 * @param cjxtJsmbpzDtlList
	 */
	public void saveMain(CjxtJsmbpz cjxtJsmbpz,List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList) ;
	
	/**
	 * 修改一对多
	 *
   * @param cjxtJsmbpz
   * @param cjxtJsmbpzDtlList
	 */
	public void updateMain(CjxtJsmbpz cjxtJsmbpz,List<CjxtJsmbpzDtl> cjxtJsmbpzDtlList);
	
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
