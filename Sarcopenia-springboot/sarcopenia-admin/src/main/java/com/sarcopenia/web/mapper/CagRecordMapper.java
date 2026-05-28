package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.CagRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评估记录 数据访问层
 *
 */
@Mapper
public interface CagRecordMapper extends BaseMapper<CagRecord> {

}