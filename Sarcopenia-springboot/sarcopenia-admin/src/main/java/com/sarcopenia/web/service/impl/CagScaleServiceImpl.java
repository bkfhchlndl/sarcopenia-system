package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.CagScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.mapper.CagScaleMapper;
import com.sarcopenia.web.service.CagScaleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估量表 业务服务实现
 */
@Service
public class CagScaleServiceImpl extends ServiceImpl<CagScaleMapper, CagScale> implements CagScaleService {

    /**
     * 查询全部量表信息（包含题目及选项）
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectScaleList() {
        return baseMapper.selectScaleList();
    }
}