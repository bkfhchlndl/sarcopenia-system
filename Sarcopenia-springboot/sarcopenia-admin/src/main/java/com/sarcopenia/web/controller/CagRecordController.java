package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.dto.CagRecordDTO;
import com.sarcopenia.web.service.CagRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评估记录 控制层
 */
@RestController
@RequestMapping("/cagRecord")
public class CagRecordController {

    /** 评估记录服务 */
    @Autowired
    private CagRecordService cagRecordService;

    /**
     * 提交量表评估记录
     * @param dto 评估提交参数
     * @return 操作结果
     */
    @Anonymous
    @PostMapping("/insertCagRecord")
    public AjaxResult insertCagRecord(@Valid @RequestBody CagRecordDTO dto) {
        boolean b = cagRecordService.insertCagRecord(dto);
        return b ? AjaxResult.success("提交成功") : AjaxResult.error("提交失败");
    }
}