package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.CagScale;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.mapper.CagScaleMapper;
import com.sarcopenia.web.service.CagScaleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CagScaleServiceImpl extends ServiceImpl<CagScaleMapper, CagScale> implements CagScaleService {

    @Override
    public List<ScaleQuestionOptionVO> selectScaleList() {
        return baseMapper.selectScaleList();
    }
}