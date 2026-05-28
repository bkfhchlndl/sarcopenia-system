package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.CagOption;
import com.sarcopenia.web.entity.CagRecord;
import com.sarcopenia.web.entity.dto.CagRecordDTO;

/**
 * 评估记录 业务服务层
 */
public interface CagRecordService {

    /**
     * 提交量表评估并保存记录
     * @param dto 评估记录提交参数
     * @return 操作结果
     */
    boolean insertCagRecord(CagRecordDTO dto);

    /**
     * 根据ID查询量表评估记录
     * @param id 评估记录ID
     * @return 评估记录信息
     */
    CagRecord selectCagRecordById(Long id);
}