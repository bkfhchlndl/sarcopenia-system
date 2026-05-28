package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.CagScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;

import java.util.List;

/**
 * 评估量表 业务服务层
 */
public interface CagScaleService extends IService<CagScale> {

    /**
     * 查询全部量表信息（包含题目及选项）
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectScaleList();
}