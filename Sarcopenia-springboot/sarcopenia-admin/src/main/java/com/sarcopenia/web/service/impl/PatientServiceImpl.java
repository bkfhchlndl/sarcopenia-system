package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.mapper.PatientMapper;
import com.sarcopenia.web.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者信息 业务服务实现
 */
@Service
public class PatientServiceImpl implements PatientService {

    /** 患者信息Mapper */
    @Autowired
    private PatientMapper patientMapper;

    /**
     * 新增患者信息
     * @param patient 患者信息
     * @return 影响行数
     */
    @Override
    public int insertPatient(Patient patient) {
        return patientMapper.insertPatient(patient);
    }

    /**
     * 根据ID删除患者信息
     * @param id 患者ID
     * @return 影响行数
     */
    @Override
    public int deletePatientById(Long id) {
        return patientMapper.deletePatientById(id);
    }

    /**
     * 修改患者信息
     * @param patient 患者信息
     * @return 影响行数
     */
    @Override
    public int updatePatient(Patient patient) {
        return patientMapper.updatePatient(patient);
    }

    /**
     * 查询患者信息列表
     * @param patient 查询条件
     * @return 患者信息集合
     */
    @Override
    public List<Patient> selectPatientList(Patient patient) {
        return patientMapper.selectPatientList(patient);
    }

    /**
     * 根据ID查询患者信息
     * @param patientId 患者ID
     * @return 患者信息
     */
    @Override
    public Patient selectPatientById(Long patientId) {
        return patientMapper.selectPatientById(patientId);
    }

    /**
     * 获取已评估患者列表
     * @param patient 患者查询条件
     * @return 患者信息集合
     */
    @Override
    public List<Patient> selectPatientReportedList(Patient patient) {
        return patientMapper.selectPatientReportedList(patient);
    }
}