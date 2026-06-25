package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SGripStrength;
import org.apache.ibatis.annotations.Mapper;

/**
 * 握力测量记录数据访问层
 */
@Mapper
public interface SGripStrengthMapper extends BaseMapper<SGripStrength> {
}
