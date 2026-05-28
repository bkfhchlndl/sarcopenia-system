package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarcopenia.common.utils.SecurityUtils;
import com.sarcopenia.web.entity.CagOption;
import com.sarcopenia.web.entity.CagRecord;
import com.sarcopenia.web.entity.dto.CagRecordDTO;
import com.sarcopenia.web.entity.vo.QuestionOptionVO;
import com.sarcopenia.web.entity.vo.ScaleQuestionOptionVO;
import com.sarcopenia.web.mapper.CagRecordMapper;
import com.sarcopenia.web.mapper.CagScaleMapper;
import com.sarcopenia.web.mapper.PatientMapper;
import com.sarcopenia.web.service.CagRecordService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 评估记录 业务服务实现
 */
@Service
public class CagRecordServiceImpl extends ServiceImpl<CagRecordMapper, CagRecord> implements CagRecordService {

    /** JSON序列化工具 */
    @Resource
    private ObjectMapper objectMapper;

    /** 患者Mapper */
    @Autowired
    private PatientMapper patientMapper;

    /** 量表Mapper */
    @Autowired
    private CagScaleMapper cagScaleMapper;

    /**
     * 提交量表评估并保存记录
     * @param dto 评估提交参数
     * @return 操作结果
     */
    @Override
    @Transactional
    public boolean insertCagRecord(CagRecordDTO dto) {
        Map<Long, CagOption> optionMap = loadOptionMap();
        Map<Long, StoredAnswer> storedAnswers = buildStoredAnswers(dto.getAnswers(), optionMap);
        int totalScore = storedAnswers.values().stream()
                .map(StoredAnswer::getScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        CagRecord record = new CagRecord();
        record.setPatientId(dto.getPatientId());
        try {
            record.setUserId(SecurityUtils.getUserId());
        } catch (Exception e) {
            record.setUserId(0L);
        }
        record.setTotalScore(totalScore);
        record.setDelFlag("0");

        // 保存原始选项，同时保留当次计算出的题目分数快照
        try {
            record.setAnswerJson(objectMapper.writeValueAsString(storedAnswers));
        } catch (Exception e) {
            throw new RuntimeException("保存失败");
        }

        boolean save = this.save(record);

        if (!save) {
            throw new RuntimeException("保存失败");
        } else {
            patientMapper.updatePatientIsReport(dto.getPatientId());
        }
        return save;
    }

    /**
     * 根据ID查询量表评估记录
     * @param id 评估记录ID
     * @return 评估记录信息
     */
    @Override
    public CagRecord selectCagRecordById(Long id) {
        return this.getById(id);
    }

    /**
     * 加载所有选项并组装为Map结构
     * @return 选项ID-选项对象映射
     */
    private Map<Long, CagOption> loadOptionMap() {
        List<ScaleQuestionOptionVO> scaleList = cagScaleMapper.selectScaleList();
        Map<Long, CagOption> optionMap = new LinkedHashMap<>();
        for (ScaleQuestionOptionVO scale : safeList(scaleList)) {
            for (QuestionOptionVO question : safeList(scale.getQuestionList())) {
                for (CagOption option : safeList(question.getOptionList())) {
                    optionMap.put(option.getId(), option);
                }
            }
        }
        return optionMap;
    }

    /**
     * 构建前端答案的存储结构
     * @param answers 前端提交答案
     * @param optionMap 选项Map
     * @return 格式化后的答案结构
     */
    private Map<Long, StoredAnswer> buildStoredAnswers(Map<Long, List<Long>> answers, Map<Long, CagOption> optionMap) {
        Map<Long, StoredAnswer> storedAnswers = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Long>> entry : answers.entrySet()) {
            List<Long> optionIds = safeList(entry.getValue()).stream()
                    .filter(optionMap::containsKey)
                    .collect(Collectors.toList());
            int score = optionIds.stream()
                    .map(optionMap::get)
                    .map(CagOption::getScore)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            StoredAnswer storedAnswer = new StoredAnswer();
            storedAnswer.setOptionIds(optionIds);
            storedAnswer.setScore(score);
            storedAnswers.put(entry.getKey(), storedAnswer);
        }
        return storedAnswers;
    }

    /**
     * 安全获取List，避免空指针
     */
    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 存储答案静态内部类
     */
    @Data
    public static class StoredAnswer {
        /** 选项ID集合 */
        private List<Long> optionIds;
        /** 题目得分 */
        private Integer score;
    }
}