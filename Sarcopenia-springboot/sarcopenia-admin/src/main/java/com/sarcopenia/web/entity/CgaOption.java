package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【实体】测评题目选项信息
 * 对应数据库表：cga_option（题目选项表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cga_option")
public class CgaOption extends BaseEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属题目ID（关联cga_question表主键） */
    @Excel(name = "所属题目ID", sort = 1)
    private Long questionId;

    /** 选项标识（A/B/C/D/E...） */
    @Excel(name = "选项标识", sort = 2)
    private String label;

    /** 选项内容描述 */
    @Excel(name = "选项内容", sort = 3)
    private String content;

    /** 该选项对应的得分 */
    @Excel(name = "选项分数", sort = 4)
    private Integer score;

    /** 同一题目内的选项排序序号 */
    @Excel(name = "选项排序", sort = 5)
    private Integer sort;

    /** 删除标记（0-未删除 1-已删除） */
    @Excel(name = "删除标记", sort = 6)
    private String delFlag;

}
