package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 量表题目选项视图对象
 * 用于前端展示量表及下属题目、选项的完整层级结构
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleQuestionOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 量表ID */
    private Long scaleId;

    /** 量表名称 */
    private String scaleName;

    /** 量表编码 */
    private String code;

    /** 量表总分 */
    private Integer totalScore;

    /** 备注信息 */
    private String remark;

    /** 删除标记：0-未删除 1-已删除 */
    private String delFlag;

    /** 量表包含的题目选项列表 */
    private List<QuestionOptionVO> questionList;
}