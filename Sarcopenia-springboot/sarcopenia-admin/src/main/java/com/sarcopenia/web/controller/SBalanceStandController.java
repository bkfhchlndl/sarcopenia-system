package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SBalanceStand;
import com.sarcopenia.web.service.SBalanceStandService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站立平衡测试控制层
 */
@RestController
@RequestMapping("/sBalanceStand")
public class SBalanceStandController {

    @Resource
    private SBalanceStandService sBalanceStandService;

    /**
     * 根据患者ID查询站立平衡测试记录列表
     * @param patientId 患者ID
     * @return 记录列表
     */
    @GetMapping("/selectByPatientId")
    public AjaxResult selectByPatientId(@RequestParam("patientId") Long patientId) {
        List<SBalanceStand> list = sBalanceStandService.selectByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增站立平衡测试记录
     * @param entity 测试记录
     * @return 操作结果
     */
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody SBalanceStand entity) {
        boolean success = sBalanceStandService.insertRecord(entity);
        return success ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }
}
