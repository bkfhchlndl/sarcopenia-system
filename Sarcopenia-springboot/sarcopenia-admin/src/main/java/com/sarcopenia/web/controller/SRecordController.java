package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SRecord;
import com.sarcopenia.web.entity.dto.SRecordDTO;
import com.sarcopenia.web.service.SRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 肌少症评估记录控制层
 * 对外提供肌少症评估记录提交、查询等接口
 */
@RestController
@RequestMapping("/sRecord")
public class SRecordController {

    /** 肌少症评估记录业务服务 */
    @Resource
    private SRecordService sRecordService;

    /**
     * 提交肌少症量表评估记录
     * @param dto 评估提交参数
     * @return 操作结果
     */
    @PostMapping("/insertSRecord")
    public AjaxResult insertSRecord(@Valid @RequestBody SRecordDTO dto) {
        boolean saveResult = sRecordService.insertSRecord(dto);
        return saveResult ? AjaxResult.success("提交成功") : AjaxResult.error("提交失败");
    }

    /**
     * 查询患者的肌少症量表评估记录列表
     * @param patientId 患者ID
     * @return 评估记录列表
     */
    @GetMapping("/selectSRecordList")
    public AjaxResult selectSRecordList(@RequestParam(value = "patientId", required = true) Long patientId) {
        List<SRecord> list = sRecordService.selectSRecordList(patientId);
        return AjaxResult.success(list);
    }
}
