package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.entity.vo.CagReportVO;
import com.sarcopenia.web.service.CagReportVoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评估报告 控制层
 */
@RestController
@RequestMapping("/cagReportVo")
public class CagReportVoController {

    /** 评估报告服务 */
    @Autowired
    private CagReportVoService cagReportVoService;

    /**
     * 获取完整评估报告
     * @param id 评估记录ID
     * @return 评估报告详情
     */
    @Anonymous
    @GetMapping("/getFullReport/{id}")
    public AjaxResult getFullReport(@PathVariable Long id) {
        CagReportVO report = cagReportVoService.getFullReport(id);
        return report != null ? AjaxResult.success(report) : AjaxResult.error("query failed");
    }

    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件
     * @return 患者列表
     */
    @Anonymous
    @GetMapping("/selectPatientReportedList")
    public AjaxResult selectPatientReportedList(Patient patient) {
        List<Patient> list = cagReportVoService.selectPatientReportedList(patient);
        return AjaxResult.success(list);
    }

    /**
     * 根据患者ID删除评估记录
     * @param patientId 患者ID
     * @return 操作结果
     */
    @Anonymous
    @DeleteMapping("/deleteReportByPatientId/{patientId}")
    public AjaxResult deleteReport(@PathVariable Long patientId) {
        return cagReportVoService.deleteReportByPatientId(patientId) > 0
                ? AjaxResult.success()
                : AjaxResult.error("delete failed");
    }
}