package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SBodyComposition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人体成分测量记录数据访问层
 */
@Mapper
public interface SBodyCompositionMapper extends BaseMapper<SBodyComposition> {
}
