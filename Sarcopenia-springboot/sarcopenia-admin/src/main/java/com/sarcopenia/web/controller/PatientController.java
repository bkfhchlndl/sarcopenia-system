package com.sarcopenia.web.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.service.PatientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者信息控制层
 * 对外提供患者基础信息的增删改查与评估相关接口
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    /** 患者信息业务服务 */
    @Resource
    private PatientService patientService;

    /**
     * 新增患者信息
     * @param patient 患者信息实体
     * @return 操作结果
     */
    @PostMapping("/insertPatient")
    public AjaxResult insertPatient(@RequestBody Patient patient) {
        return AjaxResult.success(patientService.insertPatient(patient));
    }

    /**
     * 根据ID删除患者信息（逻辑删除）
     * @param id 患者ID
     * @return 操作结果
     */
    @PutMapping("/deletePatientById")
    public AjaxResult deletePatientById(@RequestParam Long id) {
        return AjaxResult.success(patientService.deletePatientById(id));
    }

    /**
     * 修改患者信息
     * @param patient 患者信息实体
     * @return 操作结果
     */
    @PutMapping("/updatePatient")
    public AjaxResult updatePatient(@RequestBody Patient patient) {
        return AjaxResult.success(patientService.updatePatient(patient));
    }

    /**
     * 保存老年综合评估基本情况
     * @param patient 患者基本信息实体
     * @return 操作结果
     */
    @PutMapping("/saveBasicInformation")
    public AjaxResult saveBasicInformation(@RequestBody Patient patient) {
        return AjaxResult.success(patientService.saveBasicInformation(patient));
    }

    /**
     * 条件查询患者信息列表
     * @param patient 查询条件实体
     * @return 患者信息列表
     */
    @GetMapping("/selectPatientList")
    public AjaxResult selectPatientList(Patient patient,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Patient> list = patientService.selectPatientList(patient);
        PageInfo<Patient> pageInfo = new PageInfo<>(list);
        return AjaxResult.success(pageInfo);
    }

    /**
     * 根据ID查询患者详情
     * @param patientId 患者ID
     * @return 患者信息详情
     */
    @GetMapping("/selectPatientById")
    public AjaxResult selectPatientById(@RequestParam Long patientId) {
        return AjaxResult.success(patientService.selectPatientById(patientId));
    }

    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件实体
     * @return 已评估患者信息列表
     */
    @GetMapping("/selectPatientReportedList")
    public AjaxResult selectPatientReportedList(Patient patient,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageHelper.startPage(pageNum, pageSize);
        List<Patient> list = patientService.selectPatientReportedList(patient);
        PageInfo<Patient> pageInfo = new PageInfo<>(list);
        return AjaxResult.success(pageInfo);
    }
}