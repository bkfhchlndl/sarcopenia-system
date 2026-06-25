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
 * 站立平衡测试记录实体
 * 对应数据库表：s_balance_stand
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "s_balance_stand", excludeProperty = {"searchValue", "params"})
public class SBalanceStand extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 双脚并立保持时长(秒) */
    @TableField("`feet_together_time`")
    private java.math.BigDecimal feetTogetherTime;

    /** 半脚错开站立保持时长(秒) */
    @TableField("`semi_offset_time`")
    private java.math.BigDecimal semiOffsetTime;

    /** 前后脚站立保持时长(秒) */
    @TableField("`tandem_stand_time`")
    private java.math.BigDecimal tandemStandTime;

    /** 最终总得分 */
    @TableField("`total_score`")
    private Integer totalScore;

    /** 备注说明 */
    private String remark;

    /** 删除标记 0=未删除 1=已删除 */
    private String delFlag;

}
