package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.SBodyComposition;
import com.sarcopenia.web.mapper.SBodyCompositionMapper;
import com.sarcopenia.web.service.SBodyCompositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人体成分测量业务实现类
 */
@Slf4j
@Service
public class SBodyCompositionServiceImpl extends ServiceImpl<SBodyCompositionMapper, SBodyComposition> implements SBodyCompositionService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";

    @Override
    public List<SBodyComposition> selectByPatientId(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SBodyComposition::getPatientId, patientId)
                .eq(SBodyComposition::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SBodyComposition::getCreateTime)
                .orderByDesc(SBodyComposition::getId)
                .list();
    }

    @Override
    public boolean insertRecord(SBodyComposition entity) {
        if (entity == null || entity.getPatientId() == null) {
            log.warn("人体成分测量记录参数缺失，患者ID为空");
            return false;
        }
        if (entity.getDelFlag() == null) {
            entity.setDelFlag(DEL_FLAG_NORMAL);
        }
        return this.save(entity);
    }
}
