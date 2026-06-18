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
 * 测评量表题目实体
 * 对应数据库表：cga_question，存储量表题目基础配置
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("cga_question")
public class CgaQuestion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属量表ID，关联量表表主键 */
    private Long scaleId;

    /** 题目内容 */
    private String title;

    /** 题目类型：1-单选 2-多选 */
    private String type;

    /** 题目排序序号，同量表内数值越小展示越靠前 */
    private Integer sort;

    /** 删除标记：0-未删除 1-已删除 */
    private String delFlag;

}