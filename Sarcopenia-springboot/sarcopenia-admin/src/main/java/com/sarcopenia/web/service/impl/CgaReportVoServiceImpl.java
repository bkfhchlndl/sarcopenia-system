package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.vo.CgaReportVO;
import com.sarcopenia.web.mapper.CgaReportVoMapper;
import com.sarcopenia.web.rule.impl.CgaRuleEngine;
import com.sarcopenia.web.service.CgaReportVoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估报告 业务服务实现
 */
@Service
public class CgaReportVoServiceImpl implements CgaReportVoService {

    /** 评估报告Mapper */
    @Autowired
    private CgaReportVoMapper reportMapper;

    /** 量表规则引擎 */
    @Autowired
    private CgaRuleEngine ruleEngine;

    /**
     * 获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
    @Override
    public CgaReportVO getFullReport(Long id) {
        CgaReportVO report = reportMapper.getFullReport(id);
        if (report == null) {
            return null;
        }
        // 使用规则引擎计算评估项的展示值
        processAssessmentList(report);
        return report;
    }

    /**
     * 根据患者ID获取最新完整评估报告
     * @param patientId 患者ID
     * @return 评估报告详情
     */
    @Override
    public CgaReportVO getFullReportByPatient(Long patientId) {
        Long recordId = reportMapper.selectLatestRecordIdByPatientId(patientId);
        return recordId == null ? null : getFullReport(recordId);
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

    /**
     * 处理评估项列表，使用规则引擎计算展示值
     * @param report 评估报告
     */
    private void processAssessmentList(CgaReportVO report) {
        List<CgaReportVO.AssessmentItemVO> assessmentList = report.getAssessmentList();
        if (assessmentList == null || assessmentList.isEmpty()) {
            return;
        }

        List<CgaReportVO.AssessmentItemVO> processedList = assessmentList.stream()
                .filter(item -> item.getScaleId() != null)
                .map(this::processAssessmentItem)
                .collect(Collectors.toList());

        report.setAssessmentList(processedList);
    }

    /**
     * 处理单个评估项，使用规则引擎计算展示值
     * @param item 原始评估项
     * @return 处理后的评估项
     */
    private CgaReportVO.AssessmentItemVO processAssessmentItem(CgaReportVO.AssessmentItemVO item) {
        String scaleCode = item.getScaleCode();
        Integer rawScore = item.getRawScore();

        if (scaleCode == null || rawScore == null) {
            return item;
        }

        //使用规则引擎计算展示值
        item.setNormalRange(ruleEngine.getNormalRange(scaleCode));
        item.setItemScore(ruleEngine.formatScore(scaleCode, rawScore));
        item.setTip(ruleEngine.getTip(scaleCode, rawScore));

        return item;
    }
}
