package com.sarcopenia.web.entity.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 【VO】老年综合评估报告视图对象
 * <p>前端展示评估报告详情、打印报告使用</p>
 */
@Data
public class CagReportVO {

    /** 患者ID */
    private Long patientId;

    /** 病历号 */
    private String caseNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别 */
    private String gender;

    /** 身高（单位：cm） */
    private BigDecimal height;

    /** 出生日期 */
    private Date birthday;

    /** 年龄 */
    private Integer age;

    /** 手机号码 */
    private String phone;

    /** 身份证号码 */
    private String idCard;

    /** 家庭住址 */
    private String address;

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

    /**
     * 【内部VO】评估项明细信息
     */
    @Data
    public static class AssessmentItemVO {
        /** 量表ID */
        private Long scaleId;

        /** 量表名称 */
        private String scaleName;

        /** 量表编码 */
        private String scaleCode;

        /** 量表满分分值 */
        private Integer scaleTotalScore;

        /** 该量表实际得分 */
        private Integer itemScore;

        /** 正常参考范围 */
        private String normalRange;

        /** 结果提示信息 */
        private String tip;

        /** 备注说明 */
        private String remark;
    }
}