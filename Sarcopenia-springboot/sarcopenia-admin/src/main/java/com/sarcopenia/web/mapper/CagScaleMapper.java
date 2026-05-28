package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.CagScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评估量表 数据访问层
 */
@Mapper
public interface CagScaleMapper extends BaseMapper<CagScale> {

    /**
     * 查询所有量表及题目选项信息
     * @return 量表题目选项集合
     */
    List<ScaleQuestionOptionVO> selectScaleList();
}