package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 题目选项表 cag_option
 */
@Data
@TableName("cag_option")
public class CagOption extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属题目ID */
    @Excel(name = "所属题目ID", sort = 1)
    private Long questionId;

    /** 选项标识（A/B/C/D...） */
    @Excel(name = "选项标识", sort = 2)
    private String label;

    /** 选项内容 */
    @Excel(name = "选项内容", sort = 3)
    private String content;

    /** 选项分数 */
    @Excel(name = "选项分数", sort = 4)
    private Integer score;

    /** 选项排序（同一题目内顺序） */
    @Excel(name = "选项排序", sort = 5)
    private Integer sort;

    /** 删除标记 0-未删除 1-已删除 */
    @Excel(name = "删除标记", sort = 6)
    private String delFlag;

}