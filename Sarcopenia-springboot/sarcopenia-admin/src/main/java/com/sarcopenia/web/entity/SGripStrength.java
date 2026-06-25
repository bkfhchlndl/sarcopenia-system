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
 * 握力测量记录实体
 * 对应数据库表：s_grip_strength
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "s_grip_strength", excludeProperty = {"searchValue", "params", "remark"})
public class SGripStrength extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 第一次握力测量值(kg) */
    @TableField("`first_value`")
    private java.math.BigDecimal firstValue;

    /** 第二次握力测量值(kg) */
    @TableField("`second_value`")
    private java.math.BigDecimal secondValue;

    /** 第三次握力测量值(kg) */
    @TableField("`third_value`")
    private java.math.BigDecimal thirdValue;

    /** 备注说明 */
    private String remark;

    /** 删除标记 0=未删除 1=已删除 */
    private String delFlag;

}
