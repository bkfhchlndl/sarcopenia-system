package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarcopenia.common.utils.SecurityUtils;
import com.sarcopenia.web.entity.SRecord;
import com.sarcopenia.web.entity.dto.SRecordDTO;
import com.sarcopenia.web.entity.vo.SVO;
import com.sarcopenia.web.mapper.SRecordMapper;
import com.sarcopenia.web.mapper.SVOMapper;
import com.sarcopenia.web.service.PatientProjectService;
import com.sarcopenia.web.service.SRecordService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 肌少症评估记录业务实现类
 * 负责肌少症评估记录提交、查询等核心业务逻辑处理
 */
@Slf4j
@Service
public class SRecordServiceImpl extends ServiceImpl<SRecordMapper, SRecord> implements SRecordService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";
    /** 默认操作人ID（获取登录用户失败时兜底） */
    private static final Long DEFAULT_OPERATOR_ID = 0L;

    /** JSON序列化工具 */
    @Resource
    private ObjectMapper objectMapper;

    /** 肌少症量表数据访问层 */
    @Resource
    private SVOMapper svoMapper;

    /** 患者项目关联服务 */
    @Resource
    private PatientProjectService patientProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertSRecord(SRecordDTO dto) {
        // 全量加载所有选项配置，构建ID-选项映射用于快速计分
        Map<Long, SVO.OptionVO> optionMap = loadOptionMap();
        // 格式化前端提交的答案，构建存储结构并计算单题得分
        Map<Long, StoredAnswer> storedAnswers = buildStoredAnswers(dto.getAnswers(), optionMap);

        // 汇总计算本次评估总分
        int totalScore = storedAnswers.values().stream()
                .map(StoredAnswer::getScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        // 构建评估记录实体
        SRecord record = new SRecord();
        record.setPatientId(dto.getPatientId());
        record.setScaleId(dto.getScaleId());
        // 获取当前登录用户ID，获取失败使用兜底值
        try {
            record.setUserId(SecurityUtils.getUserId());
        } catch (Exception e) {
            log.warn("获取当前登录用户ID失败，使用默认兜底值，患者ID:{}", dto.getPatientId(), e);
            record.setUserId(DEFAULT_OPERATOR_ID);
        }
        record.setTotalScore(totalScore);
        record.setResult(dto.getResult());
        record.setSuggest(dto.getSuggest());
        record.setDelFlag(DEL_FLAG_NORMAL);

        // 序列化答案结构为JSON字符串存储
        try {
            record.setAnswerJson(objectMapper.writeValueAsString(storedAnswers));
        } catch (Exception e) {
            log.error("肌少症评估答案序列化失败，患者ID:{}", dto.getPatientId(), e);
            throw new RuntimeException("评估答案序列化失败", e);
        }

        // 保存评估记录
        boolean saveResult = this.save(record);
        if (!saveResult) {
            log.error("保存肌少症评估记录失败，患者ID:{}", dto.getPatientId());
            throw new RuntimeException("保存评估记录失败");
        }

        // 患者与项目关联，不存在则新增
        if (dto.getProjectId() != null) {
            patientProjectService.insertIfNotExists(dto.getPatientId(), dto.getProjectId());
        }

        return saveResult;
    }

    /**
     * 根据患者 id 查询所有肌少症量表评估记录
     * @param patientId 患者ID
     * @return 评估记录列表
     */
    @Override
    public List<SRecord> selectSRecordList(Long patientId) {
        return this.lambdaQuery()
                .eq(patientId != null, SRecord::getPatientId, patientId)
                .eq(SRecord::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(SRecord::getCreateTime)
                .orderByDesc(SRecord::getId)
                .list();
    }

    /**
     * 全量加载所有肌少症量表的选项配置，组装为ID-选项映射
     * @return 选项ID与选项对象的映射关系
     */
    private Map<Long, SVO.OptionVO> loadOptionMap() {
        List<SVO> scaleList = svoMapper.selectSVOList();
        Map<Long, SVO.OptionVO> optionMap = new LinkedHashMap<>();
        // 三层循环遍历：量表→题目→选项，收集所有选项
        for (SVO scale : safeList(scaleList)) {
            for (SVO.QuestionVO question : safeList(scale.getQuestionList())) {
                for (SVO.OptionVO option : safeList(question.getOptionList())) {
                    optionMap.put(Long.valueOf(option.getOptionId()), option);
                }
            }
        }
        return optionMap;
    }

    /**
     * 构建答案存储结构，计算单题得分
     * 兼容选择题（单选/多选）与非选择题（文本/日期等），非选择题不计入选项得分
     * @param answers 前端提交的原始答案
     * @param optionMap 选项配置映射
     * @return 格式化后的答案存储结构
     */
    private Map<Long, StoredAnswer> buildStoredAnswers(Map<Long, Object> answers, Map<Long, SVO.OptionVO> optionMap) {
        Map<Long, StoredAnswer> storedAnswers = new LinkedHashMap<>();
        for (Map.Entry<Long, Object> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            Object rawValue = entry.getValue();

            // 提取答案中的选项ID，过滤无效ID
            List<Long> optionIds = extractOptionIds(rawValue).stream()
                    .filter(optionMap::containsKey)
                    .collect(Collectors.toList());

            // 计算当前题目得分（多选累加所有选项分数）
            int score = optionIds.stream()
                    .map(optionMap::get)
                    .map(SVO.OptionVO::getScore)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            // 组装存储对象
            StoredAnswer storedAnswer = new StoredAnswer();
            storedAnswer.setOptionIds(optionIds);
            // 无有效选项ID且有原始值时，保存原始文本/日期等非选项答案
            if (optionIds.isEmpty() && rawValue != null) {
                storedAnswer.setValue(rawValue);
            }
            storedAnswer.setScore(score);
            storedAnswers.put(questionId, storedAnswer);
        }
        return storedAnswers;
    }

    /**
     * 从原始答案中提取选项ID列表
     * @param rawValue 前端提交的原始答案值
     * @return 有效选项ID列表
     */
    private List<Long> extractOptionIds(Object rawValue) {
        List<Long> ids = new ArrayList<>();
        if (rawValue == null) {
            return ids;
        }
        // 多选场景：集合类型遍历提取
        if (rawValue instanceof List<?> list) {
            for (Object item : list) {
                Long id = toLong(item);
                if (id != null) {
                    ids.add(id);
                }
            }
            return ids;
        }
        // 单选场景：单个值转换
        Long id = toLong(rawValue);
        if (id != null) {
            ids.add(id);
        }
        return ids;
    }

    /**
     * 通用对象转Long类型，兼容数字类型与数字字符串
     * @param value 原始值
     * @return 转换后的Long值，转换失败返回null
     */
    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.valueOf(text.trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * 列表空安全处理，null返回空列表
     * @param list 原始列表
     * @return 非空列表
     */
    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 评估答案存储结构
     * 用于序列化后存入数据库answer_json字段
     */
    @Data
    public static class StoredAnswer {

        /** 选中的选项ID集合 */
        private List<Long> optionIds;

        /** 非选项答案值（文本/日期/时长等） */
        private Object value;

        /** 本题最终得分 */
        private Integer score;
    }
}
