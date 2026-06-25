package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.SCalfMeasure;
import com.sarcopenia.web.mapper.SCalfMeasureMapper;
import com.sarcopenia.web.service.SCalfMeasureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小腿围测量记录业务实现类
 */
@Slf4j
@Service
public class SCalfMeasureServiceImpl extends ServiceImpl<SCalfMeasureMapper, SCalfMeasure> implements SCalfMeasureService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";

    @Override
    public List<SCalfMeasure> selectByPatientId(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SCalfMeasure::getPatientId, patientId)
                .eq(SCalfMeasure::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SCalfMeasure::getCreateTime)
                .orderByDesc(SCalfMeasure::getId)
                .list();
    }

    @Override
    public boolean insertRecord(SCalfMeasure entity) {
        if (entity == null || entity.getPatientId() == null) {
            log.warn("小腿围测量记录参数缺失，患者ID为空");
            return false;
        }
        if (entity.getDelFlag() == null) {
            entity.setDelFlag(DEL_FLAG_NORMAL);
        }
        return this.save(entity);
    }
}
