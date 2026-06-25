package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.SPace;

import java.util.List;

/**
 * 6米步速测量记录业务接口
 */
public interface SPaceService extends IService<SPace> {

    /**
     * 根据患者ID查询6米步速测量记录列表（按创建时间倒序）
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    List<SPace> selectByPatientId(Long patientId);

    /**
     * 新增6米步速测量记录（自动计算平均值）
     * @param entity 测量记录
     * @return 是否成功
     */
    boolean insertRecord(SPace entity);
}
