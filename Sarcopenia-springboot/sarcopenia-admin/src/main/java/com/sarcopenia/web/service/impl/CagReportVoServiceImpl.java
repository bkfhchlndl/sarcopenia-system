package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.entity.vo.CagReportVO;
import com.sarcopenia.web.mapper.CagReportVoMapper;
import com.sarcopenia.web.service.CagReportVoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评估报告 业务服务实现
 */
@Service
public class CagReportVoServiceImpl implements CagReportVoService {

    /** 评估报告Mapper */
    @Autowired
    private CagReportVoMapper reportMapper;

    /**
     * 获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
    @Override
    public CagReportVO getFullReport(Long id) {
        return reportMapper.getFullReport(id);
    }

    /**
     * 获取已评估患者列表
     * @param patient 患者查询条件
     * @return 患者信息集合
     */
    @Override
    public List<Patient> selectPatientReportedList(Patient patient) {
        return reportMapper.selectPatientReportedList(patient);
    }

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作影响行数
     */
    @Override
    public int deleteReportByPatientId(Long patientId) {
        return reportMapper.deleteReportByPatientId(patientId);
    }
}