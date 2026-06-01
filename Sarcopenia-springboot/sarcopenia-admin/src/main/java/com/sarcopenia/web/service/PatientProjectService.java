package com.sarcopenia.web.service;

/**
 * 项目病人关联 业务服务层
 */
public interface PatientProjectService {

    /**
     * 插入项目病人关联（如果不存在则插入，已存在则跳过）
     * @param patientId 病人ID
     * @param projectId 项目ID
     * @return 操作结果
     */
    boolean insertIfNotExists(Long patientId, Long projectId);
}
