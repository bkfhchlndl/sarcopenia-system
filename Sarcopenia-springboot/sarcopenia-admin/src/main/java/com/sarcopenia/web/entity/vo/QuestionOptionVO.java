package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.sarcopenia.web.entity.CgaOption;

/**
 * 题目选项视图对象
 * 用于前端展示测评题目及对应选项组合
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

    /** 题目内容 */
    private String title;

    /** 题目类型 */
    private Integer type;

    /** 题目排序序号 */
    private Integer sort;

    /** 基本情况落库字段标识 */
    private String fieldKey;

    /** 题目对应选项列表 */
    private List<CgaOption> optionList;

}
