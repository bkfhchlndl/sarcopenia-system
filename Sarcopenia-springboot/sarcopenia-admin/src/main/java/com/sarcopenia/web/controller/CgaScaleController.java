package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.service.CgaScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评估量表 控制层
 */
@RestController
@RequestMapping("/cgaScale")
public class CgaScaleController {

    /** 评估量表服务 */
    @Autowired
    private CgaScaleService cgaScaleService;

    /**
     * 查询全部量表信息（包含题目及选项）
     * @return 量表题目选项集合
     */
//    @Anonymous
    @GetMapping("/selectScaleList")
    public AjaxResult listAll() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectScaleList();
        return AjaxResult.success(list);
    }

    /**
     * 查询所有量表及题目选项信息(21-日常生活)
     * @return 量表题目选项集合
     */
//    @Anonymous
    @GetMapping("/selectDailyLifeScale")
    public AjaxResult selectDailyLifeScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectDailyLifeScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询所有量表及题目选项信息(22-简易体能)
     * @return 量表题目选项集合
     */
//    @Anonymous
    @GetMapping("/selectSppbScale")
    public AjaxResult selectSppbScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectSppbScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询简易体能 (24- 视听力简易评估)
     * @return 量表题目选项集合
     */
    @Anonymous
    @GetMapping("/selectVisionHearingScale")
    public AjaxResult selectVisionHearingScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectVisionHearingScale();
        return AjaxResult.success(list);
    }
}
