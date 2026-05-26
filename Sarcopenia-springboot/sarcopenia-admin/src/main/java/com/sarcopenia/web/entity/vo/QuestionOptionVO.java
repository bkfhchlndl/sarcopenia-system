package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sarcopenia.common.core.domain.BaseEntity;
import com.sarcopenia.web.entity.CagOption;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOptionVO extends BaseEntity {
    // 题目id
    private Long questionId;
    // 题目内容
    private String title;
    // 题目类型
    private String type;
    // 题目排序
    private Integer sort;
    // 该题目下的选项
    private List<CagOption> optionList;
}