package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 患者信息数据访问层
 * 负责患者基础信息的增删改查与评估状态更新操作
 */
@Mapper
public interface PatientMapper {

    /**
     * 新增患者信息
     * @param patient 患者信息实体
     * @return 影响行数
     */
    int insertPatient(Patient patient);

    /**
     * 根据ID删除患者信息
     * @param id 患者ID
     * @return 影响行数
     */
    int deletePatientById(@Param("id") Long id);

    /**
     * 修改患者信息
     * @param patient 患者信息实体
     * @return 影响行数
     */
    int updatePatient(Patient patient);

    /**
     * 保存老年综合评估基本情况信息
     * @param patient 患者基本情况实体
     * @return 影响行数
     */
    int saveBasicInformation(Patient patient);

    /**
     * 条件查询患者信息列表
     * @param patient 查询条件实体
     * @return 患者信息集合
     */
    List<Patient> selectPatientList(Patient patient);

    /**
     * 根据患者ID查询患者详情
     * @param patientId 患者ID
     * @return 患者信息实体
     */
    Patient selectPatientById(@Param("patientId") Long patientId);

    /**
     * 更新患者评估状态为已检查（带项目ID）
     * @param id 患者ID
     * @param isProject 项目ID：1-老年综合 2-肌少症
     * @return 影响行数
     */
    int updatePatientIsReport(@Param("id") Long id, @Param("isProject") Integer isProject);

    /**
     * 更新患者评估状态为已报告（带项目ID）
     * @param id 患者ID
     * @param isProject 项目ID：1-老年综合 2-肌少症
     * @return 影响行数
     */
    int updatePatientIsReports(@Param("id") Long id, @Param("isProject") Integer isProject);

    /**
     * 重置患者评估报告状态为未报告
     * @param id 患者ID
     * @return 影响行数
     */
    int resetPatientIsReport(@Param("id") Long id);

    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件实体
     * @return 已评估患者信息集合
     */
    List<Patient> selectPatientReportedList(Patient patient);
}
