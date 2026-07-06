package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SRecord;
import com.sarcopenia.web.entity.dto.SRecordDTO;
import com.sarcopenia.web.service.SRecordService;
import com.sarcopenia.web.mapper.PatientMapper;
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

    /** 患者数据访问层 */
    @Resource
    private PatientMapper patientMapper;

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

    /**
     * 生成肌少症综合评估报告（更新患者状态为已检查，项目ID=2）
     * @param patientId 患者ID
     * @return 操作结果
     */
    @PostMapping("/generateReport/{patientId}")
    public AjaxResult generateReport(@PathVariable Long patientId) {
        if (patientId == null) {
            return AjaxResult.error("患者ID不能为空");
        }
        int rows = patientMapper.updatePatientIsReport(patientId, 2);
        return rows > 0 ? AjaxResult.success("报告生成成功") : AjaxResult.error("报告生成失败");
    }

    /**
     * 删除肌少症综合评估报告（更新患者状态为已报告归档，is_report=2）
     * @param patientId 患者ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteReport/{patientId}")
    public AjaxResult deleteReport(@PathVariable Long patientId) {
        if (patientId == null) {
            return AjaxResult.error("患者ID不能为空");
        }
        int rows = patientMapper.updatePatientIsReports(patientId, 2);
        return rows > 0 ? AjaxResult.success("删除成功") : AjaxResult.error("删除失败");
    }
}
