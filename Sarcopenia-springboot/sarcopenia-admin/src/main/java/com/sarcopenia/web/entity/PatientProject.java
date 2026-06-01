package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 【实体】项目病人关联信息
 * 对应数据库表：patient_project（项目病人表）
 *
 * 多对多关联表：一个病人可以做多个检查项目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("patient_project")
public class PatientProject extends BaseEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 病人ID */
    private Long patientId;

    /** 项目ID */
    private Long projectId;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 删除标志（0=正常 1=已删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 更新者 */
    private String updateBy;
}
