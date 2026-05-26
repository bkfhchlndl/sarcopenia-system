package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.mapper.PatientMapper;
import com.sarcopenia.web.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;
// 新增病人信息
    @Override
    public int insertPatient(Patient patient) {
        return patientMapper.insertPatient(patient);
    }
// 删除病人信息
    @Override
    public int deletePatientById(Long id) {
        return patientMapper.deletePatientById(id);
    }
// 修改病人信息
    @Override
    public int updatePatient(Patient patient) {
        return patientMapper.updatePatient(patient);
    }
// 查询全部病人信息
    @Override
    public List<Patient> selectPatientList(Patient patient) {
        return patientMapper.selectPatientList(patient);
    }
// 根据ID查询病人信息
    @Override
    public Patient selectPatientById(Long patientId) {
        return patientMapper.selectPatientById(patientId);
    }
}