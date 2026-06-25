package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SGripStrength;
import com.sarcopenia.web.service.SGripStrengthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 握力测量记录控制层
 */
@RestController
@RequestMapping("/sGripStrength")
public class SGripStrengthController {

    @Resource
    private SGripStrengthService sGripStrengthService;

    /**
     * 根据患者ID查询握力测量记录列表
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    @GetMapping("/selectByPatientId")
    public AjaxResult selectByPatientId(@RequestParam("patientId") Long patientId) {
        List<SGripStrength> list = sGripStrengthService.selectByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增握力测量记录
     * @param entity 测量记录
     * @return 操作结果
     */
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody SGripStrength entity) {
        boolean success = sGripStrengthService.insertRecord(entity);
        return success ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }
}
