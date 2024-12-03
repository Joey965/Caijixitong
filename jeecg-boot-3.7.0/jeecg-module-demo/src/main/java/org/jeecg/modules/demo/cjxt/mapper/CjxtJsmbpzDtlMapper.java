package org.jeecg.modules.demo.cjxt.mapper;

import java.util.List;
import org.jeecg.modules.demo.cjxt.entity.CjxtJsmbpzDtl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 角色模版配置子表
 * @Author: jeecg-boot
 * @Date:   2024-07-24
 * @Version: V1.0
 */
public interface CjxtJsmbpzDtlMapper extends BaseMapper<CjxtJsmbpzDtl> {

	/**
	 * 通过主表id删除子表数据
	 *
	 * @param mainId 主表id
	 * @return boolean
	 */
	public boolean deleteByMainId(@Param("mainId") String mainId);

  /**
   * 通过主表id查询子表数据
   *
   * @param mainId 主表id
   * @return List<CjxtJsmbpzDtl>
   */
	public List<CjxtJsmbpzDtl> selectByMainId(@Param("mainId") String mainId);
}
