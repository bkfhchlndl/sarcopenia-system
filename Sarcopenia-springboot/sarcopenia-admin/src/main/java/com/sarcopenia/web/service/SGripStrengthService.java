package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.SGripStrength;

import java.util.List;

/**
 * 握力测量记录业务接口
 */
public interface SGripStrengthService extends IService<SGripStrength> {

    /**
     * 根据患者ID查询握力测量记录列表（按创建时间倒序）
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    List<SGripStrength> selectByPatientId(Long patientId);

    /**
     * 新增握力测量记录
     * @param entity 测量记录
     * @return 是否成功
     */
    boolean insertRecord(SGripStrength entity);
}
