package com.sarcopenia.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sarcopenia.web.entity.SCalfMeasure;

import java.util.List;

/**
 * 小腿围测量记录业务接口
 */
public interface SCalfMeasureService extends IService<SCalfMeasure> {

    /**
     * 根据患者ID查询小腿围测量记录列表（按创建时间倒序）
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    List<SCalfMeasure> selectByPatientId(Long patientId);

    /**
     * 新增小腿围测量记录
     * @param entity 测量记录
     * @return 是否成功
     */
    boolean insertRecord(SCalfMeasure entity);
}
