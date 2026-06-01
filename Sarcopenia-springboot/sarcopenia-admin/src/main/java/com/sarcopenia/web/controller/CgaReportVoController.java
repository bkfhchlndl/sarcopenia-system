package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.vo.CgaReportVO;
import com.sarcopenia.web.service.CgaReportVoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评估报告 控制层
 */
@RestController
@RequestMapping("/cgaReportVo")
public class CgaReportVoController {

    /** 评估报告服务 */
    @Autowired
    private CgaReportVoService cgaReportVoService;

    /**
     * 获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
//    @Anonymous
    @GetMapping("/getFullReport/{id}")
    public AjaxResult getFullReport(@PathVariable Long id) {
        CgaReportVO report = cgaReportVoService.getFullReport(id);
        return report != null ? AjaxResult.success(report) : AjaxResult.error("query failed");
    }

    /**
     * 根据患者ID获取最新完整评估报告
     * @param patientId 患者ID
     * @return 评估报告详情
     */
//    @Anonymous
    @GetMapping("/getFullReportByPatient/{patientId}")
    public AjaxResult getFullReportByPatient(@PathVariable Long patientId) {
        CgaReportVO report = cgaReportVoService.getFullReportByPatient(patientId);
        return report != null ? AjaxResult.success(report) : AjaxResult.error("query failed");
    }

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作结果
     */
//    @Anonymous
    @DeleteMapping("/deleteReportByPatientId/{patientId}")
    public AjaxResult deleteReport(@PathVariable Long patientId) {
        return cgaReportVoService.deleteReportByPatientId(patientId) > 0
                ? AjaxResult.success()
                : AjaxResult.error("delete failed");
    }
}
