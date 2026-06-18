package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.CgaScale;
import com.sarcopenia.web.entity.vo.QuestionOptionVO;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.mapper.CgaScaleMapper;
import com.sarcopenia.web.service.CgaScaleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估量表业务实现类
 * 负责量表信息、题目选项的查询业务实现
 */
@Service
public class CgaScaleServiceImpl extends ServiceImpl<CgaScaleMapper, CgaScale> implements CgaScaleService {

    /**
     * 查询综合评估量表列表（包含题目及选项）
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectScaleList() {
        return baseMapper.selectScaleList();
    }

    /**
     * 查询日常生活能力量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectDailyLifeScale() {
        return baseMapper.selectDailyLifeScale();
    }

    /**
     * 查询工具性日常生活能力量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectInstrumentalityLifeScale() {
        return baseMapper.selectInstrumentalityLifeScale();
    }

    /**
     * 查询简易体能状况量表（SPPB）及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectSppbScale() {
        return baseMapper.selectSppbScale();
    }

    /**
     * 查询视力视觉功能量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectVisualAcuityScale() {
        return baseMapper.selectVisualAcuityScale();
    }

    /**
     * 查询听力评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectListeningScale() {
        return baseMapper.selectListeningScale();
    }

    /**
     * 查询居家环境评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectHomeEnvironmentScale() {
        return baseMapper.selectHomeEnvironmentScale();
    }

    /**
     * 查询认知功能评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectCognitionScale() {
        return baseMapper.selectCognitionScale();
    }

    /**
     * 查询衰弱评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectFrailScale() {
        return baseMapper.selectFrailScale();
    }

    /**
     * 查询尿失禁评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectUrinaryIncontinenceScale() {
        return baseMapper.selectUrinaryIncontinenceScale();
    }

    /**
     * 查询便秘评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectConstipationScale() {
        return baseMapper.selectConstipationScale();
    }

    /**
     * 查询失眠评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectInsomniaScale() {
        return baseMapper.selectInsomniaScale();
    }

    /**
     * 查询慢性疼痛评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectChronicPainScale() {
        return baseMapper.selectChronicPainScale();
    }

    /**
     * 查询压力性损伤风险评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectPressureInjuryScale() {
        return baseMapper.selectPressureInjuryScale();
    }

    /**
     * 查询吞咽功能评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectSwallowScale() {
        return baseMapper.selectSwallowScale();
    }

    /**
     * 查询营养风险筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectNutritionScale() {
        return baseMapper.selectNutritionScale();
    }

    /**
     * 查询微营养评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectMicroNutritionScale() {
        return baseMapper.selectMicroNutritionScale();
    }

    /**
     * 查询营养标准评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectNutritionalStandardsScale() {
        return baseMapper.selectNutritionalStandardsScale();
    }

    /**
     * 查询谵妄评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectDeliriumScale() {
        return baseMapper.selectDeliriumScale();
    }

    /**
     * 查询抑郁评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectDepressionScale() {
        return baseMapper.selectDepressionScale();
    }

    /**
     * 查询跌倒风险筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectFallScale() {
        return baseMapper.selectFallScale();
    }

    /**
     * 查询多重用药评估量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectMedicationScale() {
        return baseMapper.selectMedicationScale();
    }

    /**
     * 查询焦虑筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectAnxietyScale() {
        return baseMapper.selectAnxietyScale();
    }

    /**
     * 查询基本信息采集量表及题目选项信息
     * @return 量表题目选项集合
     */
    @Override
    public List<ScaleQuestionOptionVO> selectBasicInformationScale() {
        List<ScaleQuestionOptionVO> list = baseMapper.selectBasicInformationScale();
        fillBasicInformationFieldKeys(list);
        return list;
    }

    private void fillBasicInformationFieldKeys(List<ScaleQuestionOptionVO> list) {
        if (list == null) {
            return;
        }
        for (ScaleQuestionOptionVO scale : list) {
            if (scale == null || scale.getQuestionList() == null) {
                continue;
            }
            for (QuestionOptionVO question : scale.getQuestionList()) {
                if (question == null) {
                    continue;
                }
                question.setFieldKey(resolveBasicInformationFieldKey(question));
            }
        }
    }

    private String resolveBasicInformationFieldKey(QuestionOptionVO question) {
        Integer sort = question.getSort();
        if (sort == null) {
            return null;
        }
        return switch (sort) {
            case 2 -> "educationalLevel";
            case 4 -> "maritalStatus";
            case 5 -> "familyStatus";
            case 6 -> "liveSituation";
            case 7 -> "careSituation";
            case 8 -> "payType";
            case 9 -> "incomeSource";
            default -> null;
        };
    }
}
