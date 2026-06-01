package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 【实体】评估记录信息
 * 对应数据库表：cga_record（评估记录表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cga_record", excludeProperty = "params")
public class CgaRecord extends BaseEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    @Excel(name = "患者ID", sort = 1)
    private Long patientId;

    /** 操作人用户ID */
    @Excel(name = "用户ID", sort = 2)
    private Long userId;

    /** 评估量表ID */
    @Excel(name = "量表ID", sort = 3)
    private Long scaleId;

    /** 评估总分 */
    @Excel(name = "总分", sort = 4)
    private Integer totalScore;

    /** 评估结果描述 */
    @Excel(name = "评估结果", sort = 5)
    private String result;

    /** 医生建议 */
    @Excel(name = "建议", sort = 6)
    private String suggest;

    /** 评估答案JSON字符串 */
    @Excel(name = "答案JSON", sort = 7)
    private String answerJson;

    /** 评估创建时间 */
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 8)
    private Date createTime;

    /** 删除标记（0-未删除 1-已删除） */
    @Excel(name = "删除标记", sort = 9)
    private String delFlag;
}
