package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.SBalanceStand;
import com.sarcopenia.web.mapper.SBalanceStandMapper;
import com.sarcopenia.web.service.SBalanceStandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 站立平衡测试业务实现类
 *
 * SPPB 平衡测试评分标准：
 * - 双脚并立站立：≥10秒得1分，<10秒或未尝试得0分
 * - 半脚错开站立：≥10秒得1分，<10秒或未尝试得0分
 * - 前后脚一条线站立：≥10秒得2分，3~9.99秒得1分，<3秒或未尝试得0分
 * 总分范围：0 ~ 4 分
 */
@Slf4j
@Service
public class SBalanceStandServiceImpl extends ServiceImpl<SBalanceStandMapper, SBalanceStand> implements SBalanceStandService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";

    @Override
    public List<SBalanceStand> selectByPatientId(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SBalanceStand::getPatientId, patientId)
                .eq(SBalanceStand::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SBalanceStand::getCreateTime)
                .orderByDesc(SBalanceStand::getId)
                .list();
    }

    @Override
    public boolean insertRecord(SBalanceStand entity) {
        if (entity == null || entity.getPatientId() == null) {
            log.warn("站立平衡测试记录参数缺失，患者ID为空");
            return false;
        }
        if (entity.getDelFlag() == null) {
            entity.setDelFlag(DEL_FLAG_NORMAL);
        }
        // 自动计算总分
        entity.setTotalScore(calculateTotalScore(entity));
        return this.save(entity);
    }

    /**
     * 计算 SPPB 平衡测试总分
     */
    private Integer calculateTotalScore(SBalanceStand entity) {
        int score = 0;
        // 双脚并立：≥10秒得1分
        score += calcFeetTogetherScore(entity.getFeetTogetherTime());
        // 半脚错开：≥10秒得1分
        score += calcSemiOffsetScore(entity.getSemiOffsetTime());
        // 前后脚一条线：≥10秒得2分，3~9.99秒得1分
        score += calcTandemStandScore(entity.getTandemStandTime());
        return score;
    }

    /**
     * 双脚并立评分：≥10s = 1分，否则 0分
     */
    private int calcFeetTogetherScore(BigDecimal time) {
        if (time == null) return 0;
        return time.compareTo(new BigDecimal("10")) >= 0 ? 1 : 0;
    }

    /**
     * 半脚错开评分：≥10s = 1分，否则 0分
     */
    private int calcSemiOffsetScore(BigDecimal time) {
        if (time == null) return 0;
        return time.compareTo(new BigDecimal("10")) >= 0 ? 1 : 0;
    }

    /**
     * 前后脚一条线评分：≥10s = 2分，3~9.99s = 1分，<3s = 0分
     */
    private int calcTandemStandScore(BigDecimal time) {
        if (time == null) return 0;
        if (time.compareTo(new BigDecimal("10")) >= 0) return 2;
        if (time.compareTo(new BigDecimal("3")) >= 0) return 1;
        return 0;
    }
}
