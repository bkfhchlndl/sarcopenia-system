package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.SSitUp;
import com.sarcopenia.web.service.SSitUpService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 5次起坐时间测量记录控制层
 */
@RestController
@RequestMapping("/sSitUp")
public class SSitUpController {

    @Resource
    private SSitUpService sSitUpService;

    /**
     * 根据患者ID查询5次起坐测量记录列表
     * @param patientId 患者ID
     * @return 测量记录列表
     */
    @GetMapping("/selectByPatientId")
    public AjaxResult selectByPatientId(@RequestParam("patientId") Long patientId) {
        List<SSitUp> list = sSitUpService.selectByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增5次起坐测量记录
     * @param entity 测量记录
     * @return 操作结果
     */
    @PostMapping("/insert")
    public AjaxResult insert(@RequestBody SSitUp entity) {
        boolean success = sSitUpService.insertRecord(entity);
        return success ? AjaxResult.success("保存成功") : AjaxResult.error("保存失败");
    }
}
