package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.CgaScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评估量表数据访问层
 * 负责量表基础信息及题目选项的查询操作
 */
@Mapper
public interface CgaScaleMapper extends BaseMapper<CgaScale> {

    /**
     * 查询量表及题目选项信息(1-21)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectScaleList();

    /**
     * 查询全部量表及题目选项信息
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectAllScaleList();

    /**
     * 查询日常生活能力量表及题目选项信息(22)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectDailyLifeScale();

    /**
     * 查询工具性日常生活能力量表及题目选项信息(23)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectInstrumentalityLifeScale();

    /**
     * 查询简易体能状况量表(SPPB)及题目选项信息(24)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectSppbScale();

    /**
     * 查询视力视觉功能量表及题目选项信息(25)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectVisualAcuityScale();

    /**
     * 查询听力评估量表及题目选项信息(26)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectListeningScale();

    /**
     * 查询居家环境评估量表及题目选项信息(27)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectHomeEnvironmentScale();

    /**
     * 查询认知功能评估量表及题目选项信息(28)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectCognitionScale();

    /**
     * 查询衰弱评估量表及题目选项信息(29)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectFrailScale();

    /**
     * 查询尿失禁评估量表及题目选项信息(30)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectUrinaryIncontinenceScale();

    /**
     * 查询便秘评估量表及题目选项信息(31)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectConstipationScale();

    /**
     * 查询睡眠障碍评估量表及题目选项信息(32)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectInsomniaScale();

    /**
     * 查询慢性疼痛评估量表及题目选项信息(33)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectChronicPainScale();

    /**
     * 查询压力性损伤风险评估量表及题目选项信息(34)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectPressureInjuryScale();

    /**
     * 查询吞咽功能评估量表及题目选项信息(35)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectSwallowScale();

    /**
     * 查询营养风险筛查量表及题目选项信息(36)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectNutritionScale();

    /**
     * 查询微营养评估量表及题目选项信息(37)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectMicroNutritionScale();

    /**
     * 查询营养标准评估量表及题目选项信息(38)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectNutritionalStandardsScale();

    /**
     * 查询谵妄评估量表及题目选项信息(39)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectDeliriumScale();

    /**
     * 查询抑郁评估量表及题目选项信息(40)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectDepressionScale();

    /**
     * 查询跌倒风险筛查量表及题目选项信息(41)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectFallScale();

    /**
     * 查询多重用药评估量表及题目选项信息(42)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectMedicationScale();

    /**
     * 查询焦虑筛查量表及题目选项信息(43)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectAnxietyScale();

    /**
     * 查询基本信息采集量表及题目选项信息(44)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectBasicInformationScale();

    /**
     * 查询SARC-F肌少症筛查量表及题目选项信息
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectSarcFScale();
}