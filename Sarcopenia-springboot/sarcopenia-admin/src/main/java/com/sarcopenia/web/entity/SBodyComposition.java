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
import java.time.LocalDate;

/**
 * 人体成分测量记录实体
 * 对应数据库表：s_body_composition
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "s_body_composition", excludeProperty = {"searchValue", "params"})
public class SBodyComposition extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 测量日期 */
    private LocalDate measureDate;

    /** 体重(kg) */
    @TableField("`weight`")
    private BigDecimal weight;

    /** 体重指数BMI */
    @TableField("`bmi`")
    private BigDecimal bmi;

    /** 腰围(cm) */
    @TableField("`waist`")
    private BigDecimal waist;

    /** 腰臀比 */
    @TableField("`waist_hip_ratio`")
    private BigDecimal waistHipRatio;

    /** 四肢骨骼肌量(ASM) */
    @TableField("`asm`")
    private BigDecimal asm;

    /** 四肢骨骼肌量指数(ASMI) */
    @TableField("`asmi`")
    private BigDecimal asmi;

    /** 瘦体重 */
    @TableField("`lean_body_mass`")
    private BigDecimal leanBodyMass;

    /** 总肌肉量 */
    @TableField("`total_muscle_mass`")
    private BigDecimal totalMuscleMass;

    /** 体脂肪量 */
    @TableField("`body_fat_mass`")
    private BigDecimal bodyFatMass;

    /** 体脂率 */
    @TableField("`body_fat_rate`")
    private BigDecimal bodyFatRate;

    /** 蛋白质 */
    @TableField("`protein`")
    private BigDecimal protein;

    /** 无机盐 */
    @TableField("`inorganic_salt`")
    private BigDecimal inorganicSalt;

    /** 总体水 */
    @TableField("`total_body_water`")
    private BigDecimal totalBodyWater;

    /** 右臂肌肉量 */
    @TableField("`arm_right_muscle`")
    private BigDecimal armRightMuscle;

    /** 左臂肌肉量 */
    @TableField("`arm_left_muscle`")
    private BigDecimal armLeftMuscle;

    /** 右腿肌肉量 */
    @TableField("`leg_right_muscle`")
    private BigDecimal legRightMuscle;

    /** 左腿肌肉量 */
    @TableField("`leg_left_muscle`")
    private BigDecimal legLeftMuscle;

    /** 躯干肌肉量 */
    @TableField("`trunk_muscle`")
    private BigDecimal trunkMuscle;

    /** 右臂脂肪量 */
    @TableField("`arm_right_fat`")
    private BigDecimal armRightFat;

    /** 左臂脂肪量 */
    @TableField("`arm_left_fat`")
    private BigDecimal armLeftFat;

    /** 右腿脂肪量 */
    @TableField("`leg_right_fat`")
    private BigDecimal legRightFat;

    /** 左腿脂肪量 */
    @TableField("`leg_left_fat`")
    private BigDecimal legLeftFat;

    /** 躯干脂肪量 */
    @TableField("`trunk_fat`")
    private BigDecimal trunkFat;

    /** 细胞内液 */
    @TableField("`intracellular_water`")
    private BigDecimal intracellularWater;

    /** 细胞外液 */
    @TableField("`extracellular_water`")
    private BigDecimal extracellularWater;

    /** 内脏脂肪面积 */
    @TableField("`visceral_fat_area`")
    private BigDecimal visceralFatArea;

    /** 备注说明 */
    private String remark;

    /** 删除标记 0=未删除 1=已删除 */
    private String delFlag;

}
