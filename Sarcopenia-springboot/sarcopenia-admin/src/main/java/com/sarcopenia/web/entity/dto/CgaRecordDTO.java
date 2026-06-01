package com.sarcopenia.web.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 【DTO】测评记录提交请求参数
 * 前端提交测评答案时使用的数据传输对象
 */
@Data
public class CgaRecordDTO {

    /** 病人ID */
    @NotNull(message = "病人ID不能为空")
    private Long patientId;

    /** 项目ID - 标识当前检查项目 */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /** 测评答案集合 */
    @NotNull(message = "答案不能为空")
    private Map<Long, List<Long>> answers;

}