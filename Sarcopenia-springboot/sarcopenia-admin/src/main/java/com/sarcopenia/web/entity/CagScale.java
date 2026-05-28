package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import java.util.Date;

/**
 * 【实体】评估量表信息
 * 对应数据库表：cag_scale（量表信息表）
 */
@Data
@TableName("cag_scale")
public class CagScale extends BaseEntity {

    /** 序列化版本ID */
    private static final long serialVersionUID = 1L;

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 量表名称 */
    @Excel(name = "量表名称", sort = 1)
    private String scaleName;

    /** 量表编码 */
    @Excel(name = "量表编码", sort = 2)
    private String code;

    /** 量表满分值 */
    @Excel(name = "量表总分", sort = 3)
    private Integer totalScore;

    /** 备注信息 */
    @Excel(name = "备注说明", sort = 4)
    private String remark;

    /** 创建时间 */
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 5)
    private Date createTime;

    /** 删除标记（0-未删除 1-已删除） */
    @Excel(name = "删除标记", sort = 6)
    private String delFlag;

}