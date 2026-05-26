package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.Patient;

import java.util.List;

public interface PatientService {
    //新增病人信息
    int insertPatient(Patient patient);
    //删除病人信息
    int deletePatientById(Long id);
    //修改病人信息
    int updatePatient(Patient patient);
    //查询全部病人信息
    List<Patient> selectPatientList(Patient patient);
    // 根据ID查询病人信息
    Patient selectPatientById(Long patientId);
}
