package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.CagScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CagScaleMapper extends BaseMapper<CagScale> {
    // 查询所有量表信息
    List<ScaleQuestionOptionVO> selectScaleList();
}
