package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.vo.SVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SVOMapper {
    // 1. 查询所有量表列表（轻量，只查主表）
    List<SVO> selectSVOList();

    // 2. 根据ID查询单个量表详情（带题目+选项，给答题页用）
    SVO selectSVOById(Long scaleId);
}