package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.vo.CgaReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评估报告数据访问层
 * 负责评估报告相关的自定义查询与数据操作
 */
@Mapper
public interface CgaReportVoMapper {

    /**
     * 根据评估记录ID获取完整评估报告
     * @param id 评估记录ID
     * @return 完整评估报告信息
     */
    CgaReportVO getFullReport(@Param("id") Long id);

    /**
     * 根据患者ID查询最新评估记录ID
     * @param patientId 患者ID
     * @return 最新评估记录ID
     */
    Long selectLatestRecordIdByPatientId(@Param("patientId") Long patientId);

    /**
     * 根据患者ID删除相关评估记录
     * @param patientId 患者ID
     * @return 影响行数
     */
    int deleteReportByPatientId(@Param("patientId") Long patientId);
}