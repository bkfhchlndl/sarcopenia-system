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
 * 测评题目选项实体
 * 对应数据库表：cga_option，存储题目选项配置及得分规则
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("cga_option")
public class CgaOption extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属题目ID，关联cga_question表主键 */
    private Long questionId;

    /** 选项标识，如A/B/C/D，同题目内唯一 */
    private String label;

    /** 选项内容描述 */
    private String content;

    /** 选项对应得分，评分计算核心字段 */
    private Integer score;

    /** 选项排序序号，数值越小展示越靠前 */
    private Integer sort;

    /** 删除标记：0-未删除 1-已删除 */
    private String delFlag;

}