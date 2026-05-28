package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.entity.vo.CagReportVO;

import java.util.List;

/**
 * 评估报告 业务服务层
 */
public interface CagReportVoService {

    /**
     * 获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
    CagReportVO getFullReport(Long id);

    /**
     * 获取已评估患者列表
     * @param patient 患者查询条件
     * @return 患者信息集合
     */
    List<Patient> selectPatientReportedList(Patient patient);

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作影响行数
     */
    int deleteReportByPatientId(Long patientId);
}