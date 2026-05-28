package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.Patient;

import java.util.List;

/**
 * 患者信息 业务服务层
 */
public interface PatientService {

    /**
     * 新增患者信息
     * @param patient 患者信息
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
     * @param patient 患者信息
     * @return 影响行数
     */
    int updatePatient(Patient patient);

    /**
     * 查询患者信息列表
     * @param patient 查询条件
     * @return 患者信息集合
     */
    List<Patient> selectPatientList(Patient patient);

    /**
     * 根据ID查询患者信息
     * @param patientId 患者ID
     * @return 患者信息
     */
    Patient selectPatientById(Long patientId);
}