package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 患者信息实体
 * 对应数据库表：patient，存储患者基础档案信息
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("patient")
public class Patient extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名 */
    private String name;

    /** 病历号 */
    private String medicalCaseNo;

    /** 手机号 */
    private String phone;

    /** 性别：1-男 2-女 */
    private String gender;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 身份证号 */
    private String idCard;

    /** 社会保障卡号 */
    private String socialCardNo;

    /** 身高(cm) */
    private BigDecimal height;

    /** 体重(kg) */
    private BigDecimal weight;

    /** 民族 */
    private String nation;

    /** 文化程度 */
    private String educationalLevel;

    /** 宗教信仰 */
    private String religiousBelief;

    /** 婚姻状况 */
    private String maritalStatus;

    /** 家庭状况 */
    private String familyStatus;

    /** 居住情况 */
    private String liveSituation;

    /** 接受照顾情况 */
    private String careSituation;

    /** 医疗费用支付方式 */
    private String payType;

    /** 经济来源 */
    private String incomeSource;

    /** 逻辑删除标记：0-正常 1-删除 */
    private String isDel;

    /** 报告状态：0-未报告 1-已检查 2-已报告 */
    private Integer isReport;

    /** 创建时间起始值，查询入参 */
    @TableField(exist = false)
    private String beginCreateTime;

    /** 创建时间结束值，查询入参 */
    @TableField(exist = false)
    private String endCreateTime;
}