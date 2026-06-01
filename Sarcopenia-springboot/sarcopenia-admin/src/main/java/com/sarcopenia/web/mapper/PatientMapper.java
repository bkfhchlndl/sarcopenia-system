package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 患者信息 数据访问层
 */
@Mapper
public interface PatientMapper {

    /**
     * 新增患者信息
     * @param patient 患者信息
     * @return 影响行数
     */
    int insertPatient(Patient patient);

    /**
     * 根据ID删除患者信息
     * @param id 患者ID
     * @return 影响行数
     */
    int deletePatientById(Long id);

    /**
     * 修改患者信息
     * @param patient 患者信息
     * @return 影响行数
     */
    int updatePatient(Patient patient);

    /**
     * 查询患者信息列表
     * @param patient 查询条件
     * @return 患者信息集合
     */
    List<Patient> selectPatientList(Patient patient);

    /**
     * 根据患者ID查询患者信息
     * @param patientId 患者ID
     * @return 患者信息
     */
    Patient selectPatientById(Long patientId);

    /**
     * 更新患者状态为已检查
     * @param id 患者ID
     * @return 影响行数
     */
    int updatePatientIsReport(Long id);

    /**
     * 更新患者状态为已报告
     * @param id 患者ID
     * @return 影响行数
     */
    int updatePatientIsReports(Long id);
    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件
     * @return 已评估患者集合
     */
    List<Patient> selectPatientReportedList(Patient patient);
}