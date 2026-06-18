package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.PatientProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 患者项目关联数据访问层
 * 负责患者与检查项目关联关系的数据操作
 */
@Mapper
public interface PatientProjectMapper {

    /**
     * 查询患者与项目的关联记录数量
     * @param patientId 患者ID
     * @param projectId 项目ID
     * @return 关联记录数量
     */
    int selectCount(@Param("patientId") Long patientId, @Param("projectId") Long projectId);

    /**
     * 新增患者与项目的关联记录
     * @param pp 关联信息实体
     * @return 影响行数
     */
    int insertPatientProject(PatientProject pp);
}