package org.jeecg.modules.demo.cjxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.demo.cjxt.entity.CjxtScoreDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 积分明细表
 * @Author: jeecg-boot
 * @Date:   2024-06-17
 * @Version: V1.0
 */
public interface ICjxtScoreDetailService extends IService<CjxtScoreDetail> {

    IPage<CjxtScoreDetail> queryListMx(Page<CjxtScoreDetail> page, CjxtScoreDetail cjxtScoreDetail);
}
