package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PatientMapper {
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
