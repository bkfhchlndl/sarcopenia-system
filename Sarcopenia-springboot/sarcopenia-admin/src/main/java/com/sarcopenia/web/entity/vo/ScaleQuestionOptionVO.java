package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sarcopenia.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleQuestionOptionVO extends BaseEntity {
    // 题目类型id
    private Long scaleId;
    // 量表名称
    private String scaleName;
    // 编码
    private String code;
    // 总分
    private Integer totalScore;
    // 备注
    private String remark;
    // 是否删除
    private String delFlag;
    // 该量表下的所有题目（每个题目带选项）
    private List<QuestionOptionVO> questionList;
}