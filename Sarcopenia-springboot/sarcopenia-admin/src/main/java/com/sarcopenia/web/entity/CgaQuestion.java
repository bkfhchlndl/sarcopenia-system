package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 【实体】测评量表题目信息
 * 对应数据库表：cga_question（量表题目表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cga_question")
public class CgaQuestion extends BaseEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属量表ID */
    @Excel(name = "所属量表ID", sort = 1)
    private Long scaleId;

    /** 题目内容 */
    @Excel(name = "题目内容", sort = 2)
    private String title;

    /** 题目类型（1-单选 2-多选） */
    @Excel(name = "题目类型", sort = 3)
    private String type;

    /** 同一量表内题目排序序号 */
    @Excel(name = "题目排序", sort = 4)
    private Integer sort;

    /** 备注信息 */
    @Excel(name = "备注说明", sort = 5)
    private String remark;

    /** 创建时间 */
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 6)
    private Date createTime;

    /** 删除标记（0-未删除 1-已删除） */
    @Excel(name = "删除标记", sort = 7)
    private String delFlag;

}
