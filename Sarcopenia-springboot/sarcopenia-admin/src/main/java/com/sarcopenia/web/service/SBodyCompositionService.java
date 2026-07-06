package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.SBodyComposition;

import java.util.List;

/**
 * 人体成分测量业务接口
 */
public interface SBodyCompositionService extends IService<SBodyComposition> {

    /**
     * 根据患者ID查询人体成分测量记录列表（按创建时间倒序）
     * @param patientId 患者ID
     * @return 记录列表
     */
    List<SBodyComposition> selectByPatientId(Long patientId);

    /**
     * 新增人体成分测量记录
     * @param entity 测量记录
     * @return 是否成功
     */
    boolean insertRecord(SBodyComposition entity);
}
