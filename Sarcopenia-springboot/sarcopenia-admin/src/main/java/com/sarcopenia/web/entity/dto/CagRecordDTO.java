package com.sarcopenia.web.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 【DTO】测评记录提交请求参数
 * <p>前端提交测评答案时使用的数据传输对象</p>
 */
@Data
public class CagRecordDTO {

    /** 病人ID（必填） */
    @NotNull(message = "病人ID不能为空")
    private Long patientId;

    /**
     * 测评答案集合（必填）
     */
    @NotNull(message = "答案不能为空")
    private Map<Long, List<Long>> answers;
}