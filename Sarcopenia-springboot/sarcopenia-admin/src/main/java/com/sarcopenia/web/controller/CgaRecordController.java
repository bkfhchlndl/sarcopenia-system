package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.CgaRecord;
import com.sarcopenia.web.entity.dto.CgaRecordDTO;
import com.sarcopenia.web.service.CgaRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评估记录控制层
 * 对外提供评估记录提交、查询等接口
 */
@RestController
@RequestMapping("/cgaRecord")
public class CgaRecordController {

    /** 评估记录业务服务 */
    @Resource
    private CgaRecordService cgaRecordService;

    /**
     * 提交量表评估记录
     * @param dto 评估提交参数
     * @return 操作结果
     */
    @PostMapping("/insertCgaRecord")
    public AjaxResult insertCgaRecord(@Valid @RequestBody CgaRecordDTO dto) {
        boolean saveResult = cgaRecordService.insertCgaRecord(dto);
        return saveResult ? AjaxResult.success("提交成功") : AjaxResult.error("提交失败");
    }

    /**
     * 查询患者的量表评估记录列表
     * @param patientId 患者ID
     * @return 评估记录列表
     */
    @GetMapping("/selectCgaRecordList")
    public AjaxResult selectCgaRecordList(@RequestParam(value = "patientId", required = true) Long patientId) {
        List<CgaRecord> list = cgaRecordService.selectCgaRecordList(patientId);
        return AjaxResult.success(list);
    }
}