package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者信息 控制层
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    /** 患者信息服务 */
    @Autowired
    private PatientService patientService;

    /**
     * 新增患者信息
     * @param patient 患者信息
     * @return 操作结果
     */
    @PostMapping("/insertPatient")
    public AjaxResult insertPatient(@RequestBody Patient patient) {
        return AjaxResult.success(patientService.insertPatient(patient));
    }

    /**
     * 根据ID删除患者信息
     * @param id 患者ID
     * @return 操作结果
     */
    @PutMapping("/deletePatientById")
    public AjaxResult deletePatientById(Long id) {
        return AjaxResult.success(patientService.deletePatientById(id));
    }

    /**
     * 修改患者信息
     * @param patient 患者信息
     * @return 操作结果
     */
    @PutMapping("/updatePatient")
    public AjaxResult updatePatient(@RequestBody Patient patient) {
        return AjaxResult.success(patientService.updatePatient(patient));
    }

    /**
     * 查询患者信息列表
     * @param patient 查询条件
     * @return 患者列表
     */
    @GetMapping("/selectPatientList")
    public AjaxResult selectPatientList(Patient patient) {
        return AjaxResult.success(patientService.selectPatientList(patient));
    }

    /**
     * 根据ID查询患者信息
     * @param patientId 患者ID
     * @return 患者信息
     */
    @GetMapping("/selectPatientById")
    public AjaxResult selectPatientById(Long patientId) {
        return AjaxResult.success(patientService.selectPatientById(patientId));
    }

    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件
     * @return 患者列表
     */
    @Anonymous
    @GetMapping("/selectPatientReportedList")
    public AjaxResult selectPatientReportedList(Patient patient) {
        List<Patient> list = patientService.selectPatientReportedList(patient);
        return AjaxResult.success(list);
    }
}
