package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.Patient;

import java.util.List;

/**
 * 患者信息业务接口
 * 负责患者基础信息的增删改查与评估相关业务处理
 */
public interface PatientService {

    /**
     * 新增患者信息
     * @param patient 患者信息实体
     * @return 影响行数
     */
    int insertPatient(Patient patient);

    /**
     * 根据ID删除患者信息
     * @param id 患者ID
     * @return 影响行数
     */
    int deletePatientById(Long id);

    /**
     * 修改患者信息
     * @param patient 患者信息实体
     * @return 影响行数
     */
    int updatePatient(Patient patient);

    /**
     * 保存老年综合评估基本情况信息
     * @param patient 患者基本情况实体
     * @return 影响行数
     */
    int saveBasicInformation(Patient patient);

    /**
     * 条件查询患者信息列表
     * @param patient 查询条件实体
     * @return 患者信息集合
     */
    List<Patient> selectPatientList(Patient patient);

    /**
     * 根据患者ID查询患者详情
     * @param patientId 患者ID
     * @return 患者信息实体
     */
    Patient selectPatientById(Long patientId);

    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件实体
     * @return 已评估患者信息集合
     */
    List<Patient> selectPatientReportedList(Patient patient);
}