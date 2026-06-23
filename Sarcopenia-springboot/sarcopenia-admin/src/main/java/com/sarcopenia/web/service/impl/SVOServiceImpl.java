package com.sarcopenia.web.service.impl;

import com.sarcopenia.web.entity.vo.SVO;
import com.sarcopenia.web.mapper.SVOMapper;
import com.sarcopenia.web.service.SVOService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SVOServiceImpl implements SVOService {

    @Resource
    private SVOMapper svoMapper;

    @Override
    public SVO selectSVOById(Long scaleId) {
        return svoMapper.selectSVOById(scaleId);
    }

    @Override
    public List<SVO> selectSVOList() {
        return svoMapper.selectSVOList();
    }
}