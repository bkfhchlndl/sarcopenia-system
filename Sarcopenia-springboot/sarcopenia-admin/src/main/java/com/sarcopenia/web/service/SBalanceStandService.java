package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.SBalanceStand;

import java.util.List;

/**
 * 站立平衡测试业务接口
 */
public interface SBalanceStandService extends IService<SBalanceStand> {

    /**
     * 根据患者ID查询站立平衡测试记录列表（按创建时间倒序）
     * @param patientId 患者ID
     * @return 记录列表
     */
    List<SBalanceStand> selectByPatientId(Long patientId);

    /**
     * 新增站立平衡测试记录（自动计算总分）
     * @param entity 测试记录
     * @return 是否成功
     */
    boolean insertRecord(SBalanceStand entity);
}
