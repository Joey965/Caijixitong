package org.jeecg.modules.demo.cjxt.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.demo.cjxt.entity.CjxtScoreDetail;
import org.jeecg.modules.demo.cjxt.mapper.CjxtScoreDetailMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtScoreDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 积分明细表
 * @Author: jeecg-boot
 * @Date:   2024-06-17
 * @Version: V1.0
 */
@Service
public class CjxtScoreDetailServiceImpl extends ServiceImpl<CjxtScoreDetailMapper, CjxtScoreDetail> implements ICjxtScoreDetailService {

    @Autowired
    private CjxtScoreDetailMapper cjxtScoreDetailMapper;

    @Override
    public IPage<CjxtScoreDetail> queryListMx(Page<CjxtScoreDetail> page, CjxtScoreDetail cjxtScoreDetail) {
        return cjxtScoreDetailMapper.queryListMx(page,cjxtScoreDetail);
    }
}
