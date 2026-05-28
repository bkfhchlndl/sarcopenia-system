package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.service.CagScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评估量表 控制层
 */
@RestController
@RequestMapping("/cagScale")
public class CagScaleController {

    /** 评估量表服务 */
    @Autowired
    private CagScaleService cagScaleService;

    /**
     * 查询全部量表信息（包含题目及选项）
     * @return 量表题目选项集合
     */
    @Anonymous
    @GetMapping("/selectScaleList")
    public AjaxResult listAll() {
        List<ScaleQuestionOptionVO> list = cagScaleService.selectScaleList();
        return AjaxResult.success(list);
    }

}