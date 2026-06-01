package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.dto.CgaRecordDTO;
import com.sarcopenia.web.service.CgaRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评估记录
 */
@RestController
@RequestMapping("/cgaRecord")
public class CgaRecordController {

    /** 评估记录服务 */
    @Autowired
    private CgaRecordService cgaRecordService;

    /**
     * 提交量表评估记录
     * @param dto 评估提交参数
     * @return 操作结果
     */
//    @Anonymous
    @PostMapping("/insertCgaRecord")
    public AjaxResult insertCgaRecord(@Valid @RequestBody CgaRecordDTO dto) {
        boolean b = cgaRecordService.insertCgaRecord(dto);
        return b ? AjaxResult.success("提交成功") : AjaxResult.error("提交失败");
    }
}
