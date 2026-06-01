package com.sarcopenia.web.service;

import com.sarcopenia.web.entity.CgaRecord;
import com.sarcopenia.web.entity.dto.CgaRecordDTO;

/**
 * 评估记录 业务服务层
 */
public interface CgaRecordService {

    /**
     * 提交量表评估并保存记录
     * @param dto 评估记录提交参数
     * @return 操作结果
     */
    boolean insertCgaRecord(CgaRecordDTO dto);

}
