package com.sarcopenia.web.controller;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;
//  新增病人信息
    @PostMapping("/insertPatient")
    public int insertPatient(@RequestBody Patient patient) {
        return patientService.insertPatient(patient);
    }
//  删除病人信息
    @PutMapping("/deletePatientById")
    public int deletePatientById(Long id) {
        return patientService.deletePatientById(id);
    }
//  修改病人信息
    @PutMapping("/updatePatient")
    public int updatePatient(@RequestBody Patient patient) {
        return patientService.updatePatient(patient);
    }
//  查询全部病人信息
    @GetMapping("/selectPatientList")
    public List<Patient> selectPatientList(Patient patient) {
        return patientService.selectPatientList(patient);
    }
//  根据ID查询病人信息
    @GetMapping("/selectPatientById")
    public Patient selectPatientById(Long patientId) {
        return patientService.selectPatientById(patientId);
    }
}