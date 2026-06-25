package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.SSitUp;
import com.sarcopenia.web.mapper.SSitUpMapper;
import com.sarcopenia.web.service.SSitUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 5次起坐时间测量记录业务实现类
 *
 * SPPB 5次起坐评分标准：
 * - 0分：不能完成 或 >60秒
 * - 1分：16.70秒 ~ 60秒
 * - 2分：13.70秒 ~ 16.69秒
 * - 3分：11.20秒 ~ 13.69秒
 * - 4分：< 11.19秒
 */
@Slf4j
@Service
public class SSitUpServiceImpl extends ServiceImpl<SSitUpMapper, SSitUp> implements SSitUpService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";
    /** 完成标记-已完成 */
    private static final String COMPLETE_YES = "0";
    /** 完成标记-未完成 */
    private static final String COMPLETE_NO = "1";

    @Override
    public List<SSitUp> selectByPatientId(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SSitUp::getPatientId, patientId)
                .eq(SSitUp::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SSitUp::getCreateTime)
                .orderByDesc(SSitUp::getId)
                .list();
    }

    @Override
    public boolean insertRecord(SSitUp entity) {
        if (entity == null || entity.getPatientId() == null) {
            log.warn("5次起坐测量记录参数缺失，患者ID为空");
            return false;
        }
        if (entity.getDelFlag() == null) {
            entity.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (entity.getIsComplete() == null) {
            entity.setIsComplete(COMPLETE_YES);
        }
        // 自动计算 SPPB 评分
        entity.setScore(calculateScore(entity));
        return this.save(entity);
    }

    /**
     * 计算 SPPB 5次起坐评分
     */
    private Integer calculateScore(SSitUp entity) {
        // 未完成的记 0 分
        if (COMPLETE_NO.equals(entity.getIsComplete())) {
            return 0;
        }
        BigDecimal time = entity.getFirstValue();
        if (time == null) return 0;

        // >60秒 → 0分
        if (time.compareTo(new BigDecimal("60")) > 0) return 0;
        // 16.70~60秒 → 1分
        if (time.compareTo(new BigDecimal("16.70")) >= 0) return 1;
        // 13.70~16.69秒 → 2分
        if (time.compareTo(new BigDecimal("13.70")) >= 0) return 2;
        // 11.20~13.69秒 → 3分
        if (time.compareTo(new BigDecimal("11.20")) >= 0) return 3;
        // <11.19秒 → 4分
        return 4;
    }
}
