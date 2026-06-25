package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 肌少症评估记录数据访问层
 */
@Mapper
public interface SRecordMapper extends BaseMapper<SRecord> {
}
