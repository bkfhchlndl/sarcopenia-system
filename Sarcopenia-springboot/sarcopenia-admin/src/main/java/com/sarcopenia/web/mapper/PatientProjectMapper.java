package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.PatientProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目病人关联 数据访问层
 */
@Mapper
public interface PatientProjectMapper {

    /**
     * 查询项目病人关联数量
     * @param patientId 病人ID
     * @param projectId 项目ID
     * @return 数量
     */
    int selectCount(Long patientId, Long projectId);

    /**
     * 新增项目病人关联
     * @param pp 关联信息
     * @return 影响行数
     */
    int insertPatientProject(PatientProject pp);
}
