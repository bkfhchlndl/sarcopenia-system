package com.sarcopenia.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sarcopenia.web.entity.SBalanceStand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站立平衡测试记录数据访问层
 */
@Mapper
public interface SBalanceStandMapper extends BaseMapper<SBalanceStand> {
}
