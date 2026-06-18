package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 老年综合评估报告VO
 * 用于前端展示评估报告详情、打印报告场景
 */
@Data
@NoArgsConstructor
public class CgaReportVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 患者基本信息
    /** 患者ID */
    private Long patientId;

    /** 病历号 */
    private String caseNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别：1-男 2-女 */
    private String gender;

    /** 年龄，单位：岁 */
    private Integer age;

    /** 出生日期 */
    private Date birthday;

    /** 身高，单位：cm */
    private BigDecimal height;

    /** 手机号码 */
    private String phone;

    /** 身份证号码 */
    private String idCard;

    /** 家庭住址 */
    private String address;

    /** 居住情况 */
    private String liveSituation;

    /** 接受照顾情况 */
    private String careSituation;

    /** 医疗费用支付方式 */
    private String payType;

    /** 经济来源 */
    private String incomeSource;

    // 评估记录信息
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

    /** 原始答案JSON，仅后端规则计算使用 */
    @JsonIgnore
    private String answerJson;

    /** 评估创建时间 */
    private Date createTime;

    /** 评估项明细列表 */
    private List<AssessmentItemVO> assessmentList;

    /**
     * 评估项明细VO
     */
    @Data
    @NoArgsConstructor
    public static class AssessmentItemVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 量表ID */
        private Long scaleId;

        /** 量表名称 */
        private String scaleName;

        /** 量表编码，如：exercise、vision、cognition */
        private String scaleCode;

        /** 量表满分分值 */
        private Integer scaleTotalScore;

        /** 量表原始得分，数据库计算值 */
        private Integer rawScore;

        /** 量表实际得分，显示值，如"5分"/"阳性"/"阴性" */
        private String itemScore;

        /** 正常参考范围 */
        private String normalRange;

        /** 结果提示信息 */
        private String tip;

        /** 备注说明 */
        private String remark;
    }
}
