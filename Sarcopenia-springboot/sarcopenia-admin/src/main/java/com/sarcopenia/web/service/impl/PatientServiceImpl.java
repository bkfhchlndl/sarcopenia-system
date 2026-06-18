package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.mapper.PatientMapper;
import com.sarcopenia.web.service.PatientService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者信息业务实现类
 * 负责患者基础信息的增删改查与基本信息数据校验处理
 */
@Service
public class PatientServiceImpl implements PatientService {

    /** 患者信息数据访问层 */
    @Resource
    private PatientMapper patientMapper;

    @Override
    public int insertPatient(Patient patient) {
        return patientMapper.insertPatient(patient);
    }

    @Override
    public int deletePatientById(Long id) {
        return patientMapper.deletePatientById(id);
    }

    @Override
    public int updatePatient(Patient patient) {
        return patientMapper.updatePatient(patient);
    }

    /**
     * 保存老年综合评估基本情况
     * @param patient 患者基本信息
     * @return 影响行数
     */
    @Override
    public int saveBasicInformation(Patient patient) {
        // 先对文本字段做长度归一化处理，再写入数据库
        normalizeBasicInformation(patient);
        return patientMapper.saveBasicInformation(patient);
    }

    /**
     * 患者基本信息文本字段长度归一化
     * 对超长文本做截断处理，避免超出数据库字段长度导致SQL报错
     * @param patient 患者信息实体
     */
    private void normalizeBasicInformation(Patient patient) {
        if (patient == null) {
            return;
        }
        patient.setLiveSituation(limit(patient.getLiveSituation(), 255));
        patient.setCareSituation(limit(patient.getCareSituation(), 255));
        patient.setPayType(limit(patient.getPayType(), 255));
        patient.setIncomeSource(limit(patient.getIncomeSource(), 255));
    }

    /**
     * 字符串长度截断工具方法
     * 字符串长度未超过上限则原样返回，超过则截取前maxLength个字符
     * @param value 原始字符串
     * @param maxLength 最大允许长度
     * @return 处理后的字符串
     */
    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    @Override
    public List<Patient> selectPatientList(Patient patient) {
        return patientMapper.selectPatientList(patient);
    }

    @Override
    public Patient selectPatientById(Long patientId) {
        return patientMapper.selectPatientById(patientId);
    }

    @Override
    public List<Patient> selectPatientReportedList(Patient patient) {
        return patientMapper.selectPatientReportedList(patient);
    }
}