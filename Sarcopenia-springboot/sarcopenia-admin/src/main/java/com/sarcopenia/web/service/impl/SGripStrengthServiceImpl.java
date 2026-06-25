package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.SGripStrength;
import com.sarcopenia.web.mapper.SGripStrengthMapper;
import com.sarcopenia.web.service.SGripStrengthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 握力测量记录业务实现类
 */
@Slf4j
@Service
public class SGripStrengthServiceImpl extends ServiceImpl<SGripStrengthMapper, SGripStrength> implements SGripStrengthService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";

    @Override
    public List<SGripStrength> selectByPatientId(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SGripStrength::getPatientId, patientId)
                .eq(SGripStrength::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SGripStrength::getCreateTime)
                .orderByDesc(SGripStrength::getId)
                .list();
    }

    @Override
    public boolean insertRecord(SGripStrength entity) {
        if (entity == null || entity.getPatientId() == null) {
            log.warn("握力测量记录参数缺失，患者ID为空");
            return false;
        }
        if (entity.getDelFlag() == null) {
            entity.setDelFlag(DEL_FLAG_NORMAL);
        }
        return this.save(entity);
    }
}
