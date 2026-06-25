package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SCalfMeasure;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小腿围测量记录数据访问层
 */
@Mapper
public interface SCalfMeasureMapper extends BaseMapper<SCalfMeasure> {
}
