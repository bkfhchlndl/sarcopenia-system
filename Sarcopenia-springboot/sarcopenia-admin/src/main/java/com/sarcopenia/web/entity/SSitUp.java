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
 * 5次起坐时间测量记录实体
 * 对应数据库表：s_sit_up
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "s_sit_up", excludeProperty = {"searchValue", "params"})
public class SSitUp extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 5次起坐时长(秒) */
    @TableField("`first_value`")
    private java.math.BigDecimal firstValue;

    /** SPPB评分(0-4分) */
    @TableField("`score`")
    private Integer score;

    /** 备注说明 */
    private String remark;

    /** 完成标记 0=已完成 1=未完成 */
    @TableField("`is_complete`")
    private String isComplete;

    /** 删除标记 0=未删除 1=已删除 */
    private String delFlag;

}
