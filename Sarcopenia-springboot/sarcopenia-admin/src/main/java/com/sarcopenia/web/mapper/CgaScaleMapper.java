package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.CgaScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评估量表 数据访问层
 */
@Mapper
public interface CgaScaleMapper extends BaseMapper<CgaScale> {

    /**
     * 查询所有量表及题目选项信息(1-20)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectScaleList();

    /**
     * 查询所有量表及题目选项信息(21-日常生活)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectDailyLifeScale();

    /**
     * 查询简易体能 (22- 简易体能状况量表)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectSppbScale();

    /**
     * 查询简易体能 (24- 视听力简易评估)
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectVisionHearingScale();
}
