package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.CgaRecord;
import com.sarcopenia.web.entity.dto.CgaRecordDTO;

import java.util.List;

/**
 * 评估记录业务接口
 * 负责评估记录相关的业务逻辑处理
 */
public interface CgaRecordService {

    /**
     * 提交量表评估并保存记录
     * @param dto 评估记录提交参数
     * @return 保存成功返回true，失败返回false
     */
    boolean insertCgaRecord(CgaRecordDTO dto);

    /**
     * 根据患者 id 查询所有量表评估记录
     * @param patientId 患者ID
     * @return 评估记录列表
     */
    List<CgaRecord> selectCgaRecordList(Long patientId);
}