package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.CgaScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.mapper.CgaScaleMapper;
import com.sarcopenia.web.service.CgaScaleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估量表 业务服务实现
 */
@Service
public class CgaScaleServiceImpl extends ServiceImpl<CgaScaleMapper, CgaScale> implements CgaScaleService {

    /**
     * 查询全部量表信息（包含题目及选项）
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectScaleList() {
        return baseMapper.selectScaleList();
    }

    /**
     * 查询所有量表及题目选项信息(21-日常生活)
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectDailyLifeScale() {
        return baseMapper.selectDailyLifeScale();
    }

    /**
     * 查询所有量表及题目选项信息(22-简易体能)
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectSppbScale() {
        return baseMapper.selectSppbScale();
    }

    /**
     * 查询简易体能 (24- 视听力简易评估)
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectVisionHearingScale() {
        return baseMapper.selectVisionHearingScale();
    }
}
