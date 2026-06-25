package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SPace;
import org.apache.ibatis.annotations.Mapper;

/**
 * 6米步速测量记录数据访问层
 */
@Mapper
public interface SPaceMapper extends BaseMapper<SPace> {
}
