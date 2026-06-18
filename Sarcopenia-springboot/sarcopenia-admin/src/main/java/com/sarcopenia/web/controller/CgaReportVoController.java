package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.vo.CgaReportVO;
import com.sarcopenia.web.service.CgaReportVoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评估报告控制层
 * 对外提供报告查询、生成、删除等接口
 */
@RestController
@RequestMapping("/cgaReportVo")
public class CgaReportVoController {

    /** 评估报告业务服务 */
    @Resource
    private CgaReportVoService cgaReportVoService;

    /**
     * 根据记录ID获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
    @GetMapping("/getFullReport/{id}")
    public AjaxResult getFullReport(@PathVariable Long id) {
        CgaReportVO report = cgaReportVoService.getFullReport(id);
        return AjaxResult.success(report);
    }

    /**
     * 根据患者ID获取最新完整评估报告
     * @param patientId 患者ID
     * @return 评估报告详情
     */
    @GetMapping("/getFullReportByPatient/{patientId}")
    public AjaxResult getFullReportByPatient(@PathVariable Long patientId) {
        CgaReportVO report = cgaReportVoService.getFullReportByPatient(patientId);
        return AjaxResult.success(report);
    }

    /**
     * 生成患者评估报告
     * @param patientId 患者ID
     * @return 操作结果
     */
    @PostMapping("/generateReport/{patientId}")
    public AjaxResult generateReport(@PathVariable Long patientId) {
        return cgaReportVoService.generateReport(patientId) > 0
                ? AjaxResult.success()
                : AjaxResult.error("生成报告失败");
    }

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteReportByPatientId/{patientId}")
    public AjaxResult deleteReport(@PathVariable Long patientId) {
        return cgaReportVoService.deleteReportByPatientId(patientId) > 0
                ? AjaxResult.success()
                : AjaxResult.error("删除失败");
    }
}