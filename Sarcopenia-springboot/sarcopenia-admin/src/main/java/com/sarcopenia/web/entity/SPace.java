package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 6米步速测量记录实体
 * 对应数据库表：s_pace
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "s_pace", excludeProperty = {"searchValue", "params"})
public class SPace extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 第一次6米步速时长(秒) */
    @TableField("`first_value`")
    private java.math.BigDecimal firstValue;

    /** 第二次6米步速时长(秒) */
    @TableField("`second_value`")
    private java.math.BigDecimal secondValue;

    /** 平均时长(秒)，最终结果 */
    @TableField("`avg_value`")
    private java.math.BigDecimal avgValue;

    /** SPPB评分(0-4分) */
    @TableField("`score`")
    private Integer score;

    /** 备注说明 */
    private String remark;

    /** 删除标记 0=未删除 1=已删除 */
    private String delFlag;

}
