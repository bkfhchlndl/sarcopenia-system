package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 肌少症评估记录实体
 * 对应数据库表：s_record，存储患者肌少症评估的结果数据
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "s_record", excludeProperty = {"searchValue", "params", "remark"})
public class SRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID，关联患者表主键 */
    private Long patientId;

    /** 操作人ID，关联系统用户表主键 */
    private Long userId;

    /** 评估量表ID，关联肌少症量表表主键 */
    private Long scaleId;

    /** 评估总分，所有题目得分汇总 */
    private Integer totalScore;

    /** 评估结果描述 */
    private String result;

    /** 医生建议内容 */
    private String suggest;

    /** 评估答案JSON串，存储用户答题明细 */
    private String answerJson;

    /** 删除标记：0-未删除 1-已删除 */
    private String delFlag;

}
