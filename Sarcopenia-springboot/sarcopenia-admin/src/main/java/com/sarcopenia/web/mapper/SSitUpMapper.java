package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SSitUp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 5次起坐时间测量记录数据访问层
 */
@Mapper
public interface SSitUpMapper extends BaseMapper<SSitUp> {
}
