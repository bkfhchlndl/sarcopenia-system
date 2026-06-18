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
 * 项目患者关联实体
 * 对应数据库表：patient_project，患者与检查项目多对多关联
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("patient_project")
public class PatientProject extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 项目ID */
    private Long projectId;

    /** 删除标记：0-正常 1-已删除 */
    private String delFlag;

}