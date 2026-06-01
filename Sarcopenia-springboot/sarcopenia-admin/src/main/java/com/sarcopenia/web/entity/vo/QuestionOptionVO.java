package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sarcopenia.common.core.domain.BaseEntity;

import com.sarcopenia.web.entity.CgaOption;
import lombok.Data;

import java.util.List;

/**
 * 题目、选项
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOptionVO extends BaseEntity {

    /** 题目ID */
    private Long questionId;

    /** 题目内容 */
    private String title;

    /** 题目类型 */
    private Integer type;

    /** 题目排序序号 */
    private Integer sort;

    /** 题目对应的选项列表 */
    private List<CgaOption> optionList;

}