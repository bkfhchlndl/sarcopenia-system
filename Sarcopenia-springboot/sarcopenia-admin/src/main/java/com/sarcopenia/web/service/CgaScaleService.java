package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.CgaScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;

import java.util.List;

/**
 * 评估量表 业务服务层
 */
public interface CgaScaleService extends IService<CgaScale> {

    /**
     * 查询全部量表信息（包含题目及选项）
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectScaleList();

    /**
     * 查询所有量表及题目选项信息(21-日常生活)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectDailyLifeScale();

    /**
     * 查询所有量表及题目选项信息(22-简易体能)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectSppbScale();

    /**
     * 查询简易体能 (24- 视听力简易评估)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectVisionHearingScale();
}
