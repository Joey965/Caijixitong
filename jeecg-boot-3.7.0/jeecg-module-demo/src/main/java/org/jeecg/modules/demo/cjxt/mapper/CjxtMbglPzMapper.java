package org.jeecg.modules.demo.cjxt.mapper;

import java.util.List;
import org.jeecg.modules.demo.cjxt.entity.CjxtMbglPz;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 动态模板配置
 * @Author: jeecg-boot
 * @Date:   2024-06-04
 * @Version: V1.0
 */
public interface CjxtMbglPzMapper extends BaseMapper<CjxtMbglPz> {

	/**
	 * 通过主表id删除子表数据
	 *
	 * @param mainId 主表id
	 * @return boolean
	 */
	public boolean deleteByMainId(@Param("mainId") String mainId);

	public boolean deleteByMainIdDelFlag(@Param("mainId") String mainId);

	/**
	 * 通过主表id查询子表数据
	 *
	 * @param mainId 主表id
	 * @return List<CjxtMbglPz>
	 */
	public List<CjxtMbglPz> selectByMainId(@Param("mainId") String mainId);

	public List<CjxtMbglPz> selectByMainIdCommm(@Param("mainId") String mainId);
}
