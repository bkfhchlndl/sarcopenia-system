package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.CgaRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评估记录数据访问层
 * 对应数据库表：cga_record，负责评估记录数据的增删改查操作
 */
@Mapper
public interface CgaRecordMapper extends BaseMapper<CgaRecord> {

}