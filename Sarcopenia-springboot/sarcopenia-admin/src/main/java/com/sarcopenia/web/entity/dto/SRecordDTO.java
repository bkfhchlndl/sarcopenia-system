package com.sarcopenia.web.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 肌少症测评记录提交DTO
 * 前端提交测评答案时使用的数据传输对象
 */
@Data
@NoArgsConstructor
public class SRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 患者ID */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /** 项目ID，标识当前检查项目 */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 测评答案集合，key为题目ID，value为作答内容 */
    @NotNull(message = "答案不能为空")
    private Map<Long, Object> answers;

    /** 量表ID */
    private Long scaleId;

    /** 评估结果描述 */
    private String result;

    /** 评估建议内容 */
    private String suggest;
}
