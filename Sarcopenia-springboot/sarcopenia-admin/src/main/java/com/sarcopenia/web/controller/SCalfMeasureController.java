package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SCalfMeasure;
import com.sarcopenia.web.service.SCalfMeasureService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小腿围测量记录控制层
 */
@RestController
@RequestMapping("/sCalfMeasure")
public class SCalfMeasureController {

    @Resource
    private SCalfMeasureService sCalfMeasureService;

    /**
     * 根据患者ID查询小腿围测量记录列表
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    @GetMapping("/selectByPatientId")
    public AjaxResult selectByPatientId(@RequestParam("patientId") Long patientId) {
        List<SCalfMeasure> list = sCalfMeasureService.selectByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增小腿围测量记录
     * @param entity 测量记录
     * @return 操作结果
     */
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody SCalfMeasure entity) {
        boolean success = sCalfMeasureService.insertRecord(entity);
        return success ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }
}
