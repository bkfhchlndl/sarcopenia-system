package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.CagScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;

import java.util.List;

public interface CagScaleService extends IService<CagScale> {

    // 查询全部量表（带题目+选项）
    List<ScaleQuestionOptionVO> selectScaleList();
}