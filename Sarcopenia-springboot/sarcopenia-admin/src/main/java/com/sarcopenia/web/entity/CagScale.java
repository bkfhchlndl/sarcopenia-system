package com.sarcopenia.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sarcopenia.common.annotation.Excel;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;
import java.util.Date;

/**
 * 量表信息表 cag_scale
 */
@Data
@TableName("cag_scale")
public class CagScale extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 量表名称 */
    @Excel(name = "量表名称", sort = 1)
    private String scaleName;

    /** 量表编码（英文标识） */
    @Excel(name = "量表编码", sort = 2)
    private String code;

    /** 量表总分 */
    @Excel(name = "量表总分", sort = 3)
    private Integer totalScore;

    /** 备注说明（评估规则） */
    @Excel(name = "备注说明", sort = 4)
    private String remark;

    /** 创建时间 */
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 5)
    private Date createTime;

    /** 删除标记 0-未删除 1-已删除 */
    @Excel(name = "删除标记", sort = 6)
    private String delFlag;

}