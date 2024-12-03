package org.jeecg.modules.demo.cjxt.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.demo.cjxt.entity.CjxtScoreDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 积分明细表
 * @Author: jeecg-boot
 * @Date:   2024-06-17
 * @Version: V1.0
 */
public interface CjxtScoreDetailMapper extends BaseMapper<CjxtScoreDetail> {
    IPage<CjxtScoreDetail> queryListMx(Page<CjxtScoreDetail> page, @Param("list") CjxtScoreDetail list);

}
