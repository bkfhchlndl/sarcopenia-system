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
 * 评估量表实体
 * 对应数据库表：cga_scale，存储CGA评估量表基础配置
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("cga_scale")
public class CgaScale extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 量表名称 */
    private String scaleName;

    /** 量表编码，全局唯一标识 */
    private String code;

    /** 量表满分值 */
    private Integer totalScore;

    /** 删除标记：0-未删除 1-已删除 */
    private String delFlag;

}