package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SPace;
import com.sarcopenia.web.service.SPaceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 6米步速测量记录控制层
 */
@RestController
@RequestMapping("/sPace")
public class SPaceController {

    @Resource
    private SPaceService sPaceService;

    /**
     * 根据患者ID查询6米步速测量记录列表
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    @GetMapping("/selectByPatientId")
    public AjaxResult selectByPatientId(@RequestParam("patientId") Long patientId) {
        List<SPace> list = sPaceService.selectByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增6米步速测量记录
     * @param entity 测量记录
     * @return 操作结果
     */
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody SPace entity) {
        boolean success = sPaceService.insertRecord(entity);
        return success ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }
}
