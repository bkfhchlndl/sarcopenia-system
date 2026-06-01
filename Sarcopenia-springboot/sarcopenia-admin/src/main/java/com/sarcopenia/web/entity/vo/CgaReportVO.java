package com.sarcopenia.web.entity.vo;

import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 【VO】老年综合评估报告
 * 前端展示评估报告详情、打印报告使用
 */
@Data
public class CgaReportVO extends BaseEntity {

    // ==================== 患者基本信息 ====================

    /** 患者ID */
    private Long patientId;

    /** 病历号 */
    private String caseNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别（1=男 2=女） */
    private String gender;

    /** 年龄（岁） */
    private Integer age;

    /** 出生日期 */
    private Date birthday;

    /** 身高（单位：cm） */
    private BigDecimal height;

    /** 手机号码 */
    private String phone;

    /** 身份证号码 */
    private String idCard;

    /** 家庭住址 */
    private String address;

    // ==================== 评估记录信息 ====================

    /** 评估记录ID */
    private Long recordId;

    /** 操作医生ID */
    private Long userId;

    /** 评估总分 */
    private Integer totalScore;

    /** 评估结果描述 */
    private String result;

    /** 医生建议与指导方案 */
    private String suggest;

    /** 评估创建时间 */
    private Date createTime;

    /** 评估项明细列表 */
    private List<AssessmentItemVO> assessmentList;

    // ==================== 内部类：评估项明细 ====================

    /**
     * 【内部VO】评估项明细信息
     */
    @Data
    public static class AssessmentItemVO {

        /** 量表ID */
        private Long scaleId;

        /** 量表名称 */
        private String scaleName;

        /** 量表编码（如：exercise、vision、cognition） */
        private String scaleCode;

        /** 量表满分分值 */
        private Integer scaleTotalScore;

        /** 该量表原始得分（数据库计算值） */
        private Integer rawScore;

        /**
         * 该量表实际得分（显示值）
         * <p>评分型：显示"5分"</p>
         * <p>阴阳型：显示"阳性"/"阴性"</p>
         */
        private String itemScore;

        /** 正常参考范围 */
        private String normalRange;

        /** 结果提示信息（如：建议进行认知功能评估） */
        private String tip;

        /** 备注说明 */
        private String remark;
    }

}