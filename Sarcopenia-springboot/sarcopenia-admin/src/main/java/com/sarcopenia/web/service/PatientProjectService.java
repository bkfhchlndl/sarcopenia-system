package com.sarcopenia.web.service;

/**
 * 患者项目关联业务接口
 * 处理患者与检查项目关联关系的业务逻辑
 */
public interface PatientProjectService {

    /**
     * 不存在则新增患者项目关联记录，已存在直接跳过
     * @param patientId 患者ID
     * @param projectId 项目ID
     * @return 新增成功返回true，已存在/失败返回false
     */
    boolean insertIfNotExists(Long patientId, Long projectId);
}