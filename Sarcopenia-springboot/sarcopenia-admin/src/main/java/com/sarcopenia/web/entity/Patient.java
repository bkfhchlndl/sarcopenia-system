package com.sarcopenia.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 【实体】患者信息
 * 对应数据库表：patient（病人信息表）
 */
@Data
public class Patient extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 病历号 */
    @Excel(name = "病历号")
    private String caseNo;

    /** 患者姓名 */
    @Excel(name = "姓名")
    private String name;

    /** 性别（1=男 2=女） */
    @Excel(name = "性别", readConverterExp = "1=男,2=女")
    private String gender;

    /** 身高（单位：cm） */
    @Excel(name = "身高(cm)")
    private BigDecimal height;

    /** 出生日期 */
    @Excel(name = "出生年月日", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 手机号码 */
    @Excel(name = "手机号")
    private String phone;

    /** 身份证号码 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 家庭住址 */
    @Excel(name = "家庭住址")
    private String address;

    /** 创建开始时间（查询条件） */
    private String beginCreateTime;

    /** 创建结束时间（查询条件） */
    private String endCreateTime;

    /** 删除标志（0=正常 1=已删除） */
    private Integer isDel;

    /** 报告状态（0=未报告 1=已检查 2=已报告） */
    private Integer isReport;
}