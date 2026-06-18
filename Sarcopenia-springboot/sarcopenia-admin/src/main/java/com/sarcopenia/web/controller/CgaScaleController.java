package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.service.CgaScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评估量表控制层
 * 对外提供量表信息、题目选项的查询接口
 */
@RestController
@RequestMapping("/cgaScale")
public class CgaScaleController {

    /** 评估量表服务 */
    @Autowired
    private CgaScaleService cgaScaleService;

    /**
     * 查询综合评估量表列表（1-21号）
     * @return 量表题目选项集合
     */
    @GetMapping("/selectScaleList")
    public AjaxResult listAll() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectScaleList();
        return AjaxResult.success(list);
    }

    /**
     * 查询日常生活能力量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectDailyLifeScale")
    public AjaxResult selectDailyLifeScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectDailyLifeScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询工具性日常生活能力量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectInstrumentalityLifeScale")
    public AjaxResult selectInstrumentalityLifeScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectInstrumentalityLifeScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询简易体能状况量表（SPPB）及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectSppbScale")
    public AjaxResult selectSppbScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectSppbScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询视力视觉功能量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectVisualAcuityScale")
    public AjaxResult selectVisualAcuityScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectVisualAcuityScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询听力评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectListeningScale")
    public AjaxResult selectListeningScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectListeningScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询居家环境评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectHomeEnvironmentScale")
    public AjaxResult selectHomeEnvironmentScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectHomeEnvironmentScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询认知功能评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectCognitionScale")
    public AjaxResult selectCognitionScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectCognitionScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询衰弱评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectFrailScale")
    public AjaxResult selectFrailScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectFrailScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询尿失禁评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectUrinaryIncontinenceScale")
    public AjaxResult selectUrinaryIncontinenceScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectUrinaryIncontinenceScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询便秘评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectConstipationScale")
    public AjaxResult selectConstipationScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectConstipationScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询失眠评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectInsomniaScale")
    public AjaxResult selectInsomniaScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectInsomniaScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询慢性疼痛评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectChronicPainScale")
    public AjaxResult selectChronicPainScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectChronicPainScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询压力性损伤风险评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectPressureInjuryScale")
    public AjaxResult selectPressureInjuryScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectPressureInjuryScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询吞咽功能评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectSwallowScale")
    public AjaxResult selectSwallowScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectSwallowScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询营养风险筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectNutritionScale")
    public AjaxResult selectNutritionScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectNutritionScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询微营养评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectMicroNutritionScale")
    public AjaxResult selectMicroNutritionScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectMicroNutritionScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询营养标准评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectNutritionalStandardsScale")
    public AjaxResult selectNutritionalStandardsScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectNutritionalStandardsScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询谵妄评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectDeliriumScale")
    public AjaxResult selectDeliriumScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectDeliriumScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询抑郁评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectDepressionScale")
    public AjaxResult selectDepressionScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectDepressionScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询跌倒风险筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectFallScale")
    public AjaxResult selectFallScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectFallScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询多重用药评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectMedicationScale")
    public AjaxResult selectMedicationScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectMedicationScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询焦虑筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectAnxietyScale")
    public AjaxResult selectAnxietyScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectAnxietyScale();
        return AjaxResult.success(list);
    }

    /**
     * 查询基本信息采集量表及题目选项信息
     * @return 量表题目选项集合
     */
    @GetMapping("/selectBasicInformationScale")
    public AjaxResult selectBasicInformationScale() {
        List<ScaleQuestionOptionVO> list = cgaScaleService.selectBasicInformationScale();
        return AjaxResult.success(list);
    }
}