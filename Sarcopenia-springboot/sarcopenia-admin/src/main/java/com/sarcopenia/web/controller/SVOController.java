package com.sarcopenia.web.controller;

import com.sarcopenia.common.annotation.Anonymous;
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

    @Anonymous
    @GetMapping("/selectSVOList")
    public List<SVO> selectSVOList() {
        return svoService.selectSVOList();
    }
    @Anonymous
    @GetMapping("/selectSVOById/{id}")
    public SVO selectSVOById(@PathVariable("id") Long id) {
        return svoService.selectSVOById(id);
    }
}