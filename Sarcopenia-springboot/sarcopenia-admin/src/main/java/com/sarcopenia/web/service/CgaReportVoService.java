package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.entity.vo.CgaReportVO;

import java.util.List;

/**
 * 评估报告 业务服务层
 */
public interface CgaReportVoService {

    /**
     * 获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
    CgaReportVO getFullReport(Long id);

    /**
     * 根据患者ID获取最新完整评估报告
     * @param patientId 患者ID
     * @return 评估报告详情
     */
    CgaReportVO getFullReportByPatient(Long patientId);

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作影响行数
     */
    int deleteReportByPatientId(Long patientId);
}
