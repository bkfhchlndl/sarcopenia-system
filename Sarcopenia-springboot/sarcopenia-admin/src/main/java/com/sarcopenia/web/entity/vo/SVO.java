package com.sarcopenia.web.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SVO implements Serializable {

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

    /** 题目列表（一对多：一个量表多个题目） */
    private List<QuestionVO> questionList;

    /**
     * 内部类：题目VO
     */
    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 题目ID */
        private Long questionId;

        /** 题干 */
        private String title;

        /** 题型 1=单选 2=多选 3=判断 4=填空 */
        private String type;

        /** 排序 */
        private Integer sort;

        /** 题目备注 */
        private String remark;

        /** 选项列表（一对多：一个题目多个选项） */
        private List<OptionVO> optionList;
    }

    /**
     * 内部类：选项VO
     */
    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** 选项ID */
        private Integer optionId;

        /** 选项标识 A/B/C/D */
        private String label;

        /** 选项内容 */
        private String content;

        /** 选项得分 */
        private Integer score;

        /** 排序 */
        private Integer sort;
    }
}