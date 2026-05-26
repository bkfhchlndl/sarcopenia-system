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

@RestController
@RequestMapping("/cagScale")
public class CagScaleController {

    @Autowired
    private CagScaleService cagScaleService;

    /**
     * 查询全部量表 + 题目 + 选项
     */
    @Anonymous
    @GetMapping("/selectScaleList")
    public AjaxResult listAll() {
        List<ScaleQuestionOptionVO> list = cagScaleService.selectScaleList();
        return AjaxResult.success(list);
    }
}