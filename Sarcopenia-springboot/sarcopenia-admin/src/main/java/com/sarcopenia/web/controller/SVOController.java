package com.sarcopenia.web.controller;

import com.sarcopenia.common.core.domain.AjaxResult;
import com.sarcopenia.web.entity.vo.SVO;
import com.sarcopenia.web.service.SVOService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/svo")
public class SVOController {

    @Resource
    private SVOService svoService;

    /**
     * 查询所有肌少症量表列表（轻量，只查主表）
     */
    @GetMapping("/selectSVOList")
    public AjaxResult selectSVOList() {
        List<SVO> list = svoService.selectSVOList();
        return AjaxResult.success(list);
    }

    /**
     * 根据ID查询单个肌少症量表详情（带题目+选项）
     */
    @GetMapping("/selectSVOById/{id}")
    public AjaxResult selectSVOById(@PathVariable("id") Long id) {
        SVO svo = svoService.selectSVOById(id);
        return AjaxResult.success(svo);
    }
}
