package com.sarcopenia.web.mapper;

import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.entity.vo.CagReportVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评估报告 数据访问层
 */
@Mapper
public interface CagReportVoMapper {

    /**
     * 根据评估记录ID获取完整评估报告
     * @param id 评估记录ID
     * @return 完整评估报告信息
     */
    CagReportVO getFullReport(Long id);

    /**
     * 查询已完成评估的患者列表
     * @param patient 查询条件
     * @return 已评估患者集合
     */
    List<Patient> selectPatientReportedList(Patient patient);

    /**
     * 根据患者ID删除相关评估记录
     * @param patientId 患者ID
     * @return 影响行数
     */
    int deleteReportByPatientId(Long patientId);
}