package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.vo.CgaReportVO;

/**
 * 评估报告业务接口
 * 负责评估报告查询、生成、删除等业务处理
 */
public interface CgaReportVoService {

    /**
     * 根据记录ID获取完整评估报告
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
     * 生成患者评估报告
     * @param patientId 患者ID
     * @return 操作影响行数
     */
    int generateReport(Long patientId);

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作影响行数
     */
    int deleteReportByPatientId(Long patientId);
}