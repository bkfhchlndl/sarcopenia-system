package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sarcopenia.web.entity.SPace;
import com.sarcopenia.web.mapper.SPaceMapper;
import com.sarcopenia.web.service.SPaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 6米步速测量记录业务实现类
 *
 * SPPB 6米步行评分标准（基于平均时长）：
 * - 0分：不能完成
 * - 1分：> 13.05秒
 * - 2分：9.32秒 ~ 13.05秒
 * - 3分：7.23秒 ~ 9.32秒
 * - 4分：< 7.23秒
 */
@Slf4j
@Service
public class SPaceServiceImpl extends ServiceImpl<SPaceMapper, SPace> implements SPaceService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";

    @Override
    public List<SPace> selectByPatientId(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SPace::getPatientId, patientId)
                .eq(SPace::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SPace::getCreateTime)
                .orderByDesc(SPace::getId)
                .list();
    }

    @Override
    public boolean insertRecord(SPace entity) {
        if (entity == null || entity.getPatientId() == null) {
            log.warn("6米步速测量记录参数缺失，患者ID为空");
            return false;
        }
        if (entity.getDelFlag() == null) {
            entity.setDelFlag(DEL_FLAG_NORMAL);
        }
        // 自动计算平均值
        calculateAvgValue(entity);
        // 自动计算 SPPB 评分
        entity.setScore(calculateScore(entity));
        return this.save(entity);
    }

    /**
     * 计算平均时长
     * 有两次测量取平均，只有一次则取第一次
     */
    private void calculateAvgValue(SPace entity) {
        BigDecimal first = entity.getFirstValue();
        BigDecimal second = entity.getSecondValue();
        if (first != null && second != null) {
            BigDecimal avg = first.add(second).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            entity.setAvgValue(avg);
        } else if (first != null) {
            entity.setAvgValue(first);
        } else if (second != null) {
            entity.setAvgValue(second);
        } else {
            entity.setAvgValue(null);
        }
    }

    /**
     * 计算 SPPB 6米步行评分
     */
    private Integer calculateScore(SPace entity) {
        BigDecimal avg = entity.getAvgValue();
        if (avg == null || avg.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // > 13.05秒 → 1分
        if (avg.compareTo(new BigDecimal("13.05")) > 0) return 1;
        // 9.32~13.05秒 → 2分
        if (avg.compareTo(new BigDecimal("9.32")) >= 0) return 2;
        // 7.23~9.32秒 → 3分
        if (avg.compareTo(new BigDecimal("7.23")) >= 0) return 3;
        // < 7.23秒 → 4分
        return 4;
    }
}
