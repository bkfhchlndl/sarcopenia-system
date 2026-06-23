package com.sarcopenia.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarcopenia.web.entity.CgaRecord;
import com.sarcopenia.web.entity.Patient;
import com.sarcopenia.web.entity.vo.CgaReportVO;
import com.sarcopenia.web.mapper.CgaRecordMapper;
import com.sarcopenia.web.mapper.CgaReportVoMapper;
import com.sarcopenia.web.mapper.PatientMapper;
import com.sarcopenia.web.rule.impl.CgaRuleEngine;
import com.sarcopenia.web.service.CgaReportVoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评估报告业务实现类
 * 负责报告查询、生成、规则计算等核心业务处理
 */
@Slf4j
@Service
public class CgaReportVoServiceImpl implements CgaReportVoService {

    /** 逻辑删除-正常状态 */
    private static final String DEL_FLAG_NORMAL = "0";
    /** 谵妄评估量表编码 */
    private static final String DELIRIUM_ASSESSMENT_CODE = "delirium_assessment";
    /** 谵妄评估正常参考范围 */
    private static final String DELIRIUM_NORMAL_RANGE = "第1题和第2题均选“是”，且第3题或第4题至少一题选“是”";

    /** 综合评估量表编码白名单（共 21 项，对应 comprehensive.vue 中 FULL_SCALE_LIST） */
    private static final java.util.Set<String> ASSESSMENT_CODE_WHITELIST = new java.util.HashSet<>(Arrays.asList(
            "basic_adl", "instrumental_adl", "exercise_function",
            "vision_assessment", "hearing_assessment", "home_environment",
            "frail_assessment", "incontinence_screen", "constipation_screen",
            "sleep_disorder_screen", "chronic_pain_screen", "pressure_injury_risk",
            "water_swallowing_test", "nutrition_risk_screen", "sarcopenia_screen",
            "cognition_screen", "delirium_assessment", "depression_screen",
            "anxiety_screen", "fall_risk_screen", "polypharmacy_assessment"
    ));
    /** 量表编码与专项量表ID映射 */
    private static final Map<String, List<Long>> SPECIAL_SCALE_IDS = new LinkedHashMap<>();
    /** 专项量表ID与编码、名称映射 */
    private static final Map<Long, String[]> SPECIAL_SCALE_NAMES = new LinkedHashMap<>();

    static {
        SPECIAL_SCALE_IDS.put("basic_adl", Arrays.asList(22L));
        SPECIAL_SCALE_IDS.put("instrumental_adl", Arrays.asList(23L));
        SPECIAL_SCALE_IDS.put("exercise_function", Arrays.asList(24L));
        SPECIAL_SCALE_IDS.put("vision_assessment", Arrays.asList(25L));
        SPECIAL_SCALE_IDS.put("hearing_assessment", Arrays.asList(26L));
        SPECIAL_SCALE_IDS.put("home_environment", Arrays.asList(27L));
        SPECIAL_SCALE_IDS.put("cognition_screen", Arrays.asList(28L));
        SPECIAL_SCALE_IDS.put("frail_assessment", Arrays.asList(29L));
        SPECIAL_SCALE_IDS.put("incontinence_screen", Arrays.asList(30L));
        SPECIAL_SCALE_IDS.put("constipation_screen", Arrays.asList(31L));
        SPECIAL_SCALE_IDS.put("sleep_disorder_screen", Arrays.asList(32L));
        SPECIAL_SCALE_IDS.put("chronic_pain_screen", Arrays.asList(33L));
        SPECIAL_SCALE_IDS.put("pressure_injury_risk", Arrays.asList(34L));
        SPECIAL_SCALE_IDS.put("water_swallowing_test", Arrays.asList(35L));
        SPECIAL_SCALE_IDS.put("nutrition_risk_screen", Arrays.asList(36L));
        SPECIAL_SCALE_IDS.put("sarcopenia_screen", Arrays.asList(37L));
        SPECIAL_SCALE_IDS.put("delirium_assessment", Arrays.asList(39L));
        SPECIAL_SCALE_IDS.put("depression_screen", Arrays.asList(40L));
        SPECIAL_SCALE_IDS.put("fall_risk_screen", Arrays.asList(41L));
        SPECIAL_SCALE_IDS.put("polypharmacy_assessment", Arrays.asList(42L));
        SPECIAL_SCALE_IDS.put("anxiety_screen", Arrays.asList(43L));

        SPECIAL_SCALE_NAMES.put(1L, new String[]{"basic_adl", "基本日常生活"});
        SPECIAL_SCALE_NAMES.put(2L, new String[]{"instrumental_adl", "工具性日常生活"});
        SPECIAL_SCALE_NAMES.put(3L, new String[]{"exercise_function", "运动功能"});
        SPECIAL_SCALE_NAMES.put(4L, new String[]{"vision_assessment", "视力视觉功能"});
        SPECIAL_SCALE_NAMES.put(5L, new String[]{"hearing_assessment", "听力"});
        SPECIAL_SCALE_NAMES.put(6L, new String[]{"home_environment", "居家环境"});
        SPECIAL_SCALE_NAMES.put(7L, new String[]{"frail_assessment", "衰弱"});
        SPECIAL_SCALE_NAMES.put(8L, new String[]{"incontinence_screen", "尿失禁"});
        SPECIAL_SCALE_NAMES.put(9L, new String[]{"constipation_screen", "便秘"});
        SPECIAL_SCALE_NAMES.put(10L, new String[]{"chronic_pain_screen", "慢性疼痛"});
        SPECIAL_SCALE_NAMES.put(11L, new String[]{"pressure_injury_risk", "压力性损伤风险"});
        SPECIAL_SCALE_NAMES.put(12L, new String[]{"water_swallowing_test", "吞咽功能"});
        SPECIAL_SCALE_NAMES.put(13L, new String[]{"nutrition_risk_screen", "营养风险"});
        SPECIAL_SCALE_NAMES.put(14L, new String[]{"fall_risk_screen", "跌倒风险"});
        SPECIAL_SCALE_NAMES.put(15L, new String[]{"polypharmacy_assessment", "多重用药"});
        SPECIAL_SCALE_NAMES.put(16L, new String[]{"sarcopenia_screen", "微营养"});
        SPECIAL_SCALE_NAMES.put(17L, new String[]{"cognition_screen", "认知功能"});
        SPECIAL_SCALE_NAMES.put(18L, new String[]{"sleep_disorder_screen", "睡眠障碍"});
        SPECIAL_SCALE_NAMES.put(19L, new String[]{"delirium_assessment", "谵妄"});
        SPECIAL_SCALE_NAMES.put(20L, new String[]{"depression_screen", "抑郁"});
        SPECIAL_SCALE_NAMES.put(21L, new String[]{"anxiety_screen", "焦虑"});

        SPECIAL_SCALE_NAMES.put(22L, new String[]{"basic_adl", "基本日常生活活动能力评估"});
        SPECIAL_SCALE_NAMES.put(23L, new String[]{"instrumental_adl", "工具性日常生活活动能力评估"});
        SPECIAL_SCALE_NAMES.put(24L, new String[]{"exercise_function", "简易体能状况评估"});
        SPECIAL_SCALE_NAMES.put(25L, new String[]{"vision_assessment", "视力视觉功能评估"});
        SPECIAL_SCALE_NAMES.put(26L, new String[]{"hearing_assessment", "听力评估"});
        SPECIAL_SCALE_NAMES.put(27L, new String[]{"home_environment", "居家环境评估"});
        SPECIAL_SCALE_NAMES.put(28L, new String[]{"cognition_screen", "认知功能评估"});
        SPECIAL_SCALE_NAMES.put(29L, new String[]{"frail_assessment", "衰弱评估"});
        SPECIAL_SCALE_NAMES.put(30L, new String[]{"incontinence_screen", "尿失禁评估"});
        SPECIAL_SCALE_NAMES.put(31L, new String[]{"constipation_screen", "便秘评估"});
        SPECIAL_SCALE_NAMES.put(32L, new String[]{"sleep_disorder_screen", "睡眠障碍评估"});
        SPECIAL_SCALE_NAMES.put(33L, new String[]{"chronic_pain_screen", "慢性疼痛评估"});
        SPECIAL_SCALE_NAMES.put(34L, new String[]{"pressure_injury_risk", "压力性损伤风险评估"});
        SPECIAL_SCALE_NAMES.put(35L, new String[]{"water_swallowing_test", "吞咽功能评估"});
        SPECIAL_SCALE_NAMES.put(36L, new String[]{"nutrition_risk_screen", "营养风险评估"});
        SPECIAL_SCALE_NAMES.put(37L, new String[]{"sarcopenia_screen", "微营养评估"});
        SPECIAL_SCALE_NAMES.put(39L, new String[]{"delirium_assessment", "谵妄评估"});
        SPECIAL_SCALE_NAMES.put(40L, new String[]{"depression_screen", "抑郁评估"});
        SPECIAL_SCALE_NAMES.put(41L, new String[]{"fall_risk_screen", "跌倒风险评估"});
        SPECIAL_SCALE_NAMES.put(42L, new String[]{"polypharmacy_assessment", "多重用药评估"});
        SPECIAL_SCALE_NAMES.put(43L, new String[]{"anxiety_screen", "焦虑评估"});
    }

    /** 评估报告数据访问层 */
    @Resource
    private CgaReportVoMapper reportMapper;

    /** 患者数据访问层 */
    @Resource
    private PatientMapper patientMapper;

    /** 评估记录数据访问层 */
    @Resource
    private CgaRecordMapper cgaRecordMapper;

    /** 量表规则引擎 */
    @Resource
    private CgaRuleEngine ruleEngine;

    /** JSON序列化工具 */
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public CgaReportVO getFullReport(Long id) {
        CgaReportVO report = reportMapper.getFullReport(id);
        if (report == null) {
            return null;
        }
        processAssessmentList(report);
        return report;
    }

    @Override
    public CgaReportVO getFullReportByPatient(Long patientId) {
        Long recordId = reportMapper.selectLatestRecordIdByPatientId(patientId);
        return recordId == null ? buildRecordOnlyReport(patientId) : getFullReport(recordId);
    }

    @Override
    public int generateReport(Long patientId) {
        if (patientId == null) {
            return 0;
        }
        Long recordId = reportMapper.selectLatestRecordIdByPatientId(patientId);
        if (recordId == null && selectSpecialRecords(patientId).isEmpty()) {
            return 0;
        }
        return patientMapper.updatePatientIsReport(patientId);
    }

    /**
     * 构建无主评估记录时的患者专项报告
     */
    private CgaReportVO buildRecordOnlyReport(Long patientId) {
        List<CgaRecord> records = selectSpecialRecords(patientId);
        if (patientId == null || records.isEmpty()) {
            return null;
        }
        Patient patient = patientMapper.selectPatientById(patientId);
        if (patient == null) {
            return null;
        }

        CgaRecord latest = records.get(0);
        CgaReportVO report = new CgaReportVO();
        report.setPatientId(patient.getId());
        report.setCaseNo(patient.getMedicalCaseNo());
        report.setPatientName(patient.getName());
        report.setGender(patient.getGender());
        report.setHeight(patient.getHeight());
        report.setBirthday(patient.getBirthday());
        report.setPhone(patient.getPhone());
        report.setIdCard(patient.getIdCard());
        report.setLiveSituation(patient.getLiveSituation());
        report.setCareSituation(patient.getCareSituation());
        report.setPayType(patient.getPayType());
        report.setIncomeSource(patient.getIncomeSource());
        report.setRecordId(latest.getId());
        report.setUserId(latest.getUserId());
        report.setTotalScore(latest.getTotalScore());
        report.setResult(latest.getResult());
        report.setSuggest(latest.getSuggest());
        report.setCreateTime(latest.getCreateTime());
        report.setAssessmentList(records.stream()
                .map(this::buildRecordOnlyAssessmentItem)
                .filter(item -> item.getScaleCode() != null
                        && ASSESSMENT_CODE_WHITELIST.contains(item.getScaleCode()))
                .collect(Collectors.toList()));
        return report;
    }

    /**
     * 构建单条专项记录的评估项
     */
    private CgaReportVO.AssessmentItemVO buildRecordOnlyAssessmentItem(CgaRecord record) {
        CgaReportVO.AssessmentItemVO item = new CgaReportVO.AssessmentItemVO();
        Long scaleId = record.getScaleId();
        String[] scaleInfo = SPECIAL_SCALE_NAMES.getOrDefault(scaleId, new String[]{"custom_" + scaleId, "自定义评估"});
        item.setScaleId(scaleId);
        item.setScaleCode(scaleInfo[0]);
        item.setScaleName(scaleInfo[1]);
        item.setRawScore(record.getTotalScore());
        String code = scaleInfo[0];
        Integer score = record.getTotalScore();
        item.setItemScore(StringUtils.defaultIfBlank(record.getResult(),
                score == null ? "已完成" : ruleEngine.formatScore(code, score)));
        item.setTip(StringUtils.defaultString(record.getSuggest()));
        item.setNormalRange(ruleEngine.getNormalRange(code));
        item.setRemark("来自自定义/专项评估记录");
        return item;
    }

    @Override
    public int deleteReportByPatientId(Long patientId) {
        int recordRows = reportMapper.deleteReportByPatientId(patientId);
        int patientRows = patientMapper.updatePatientIsReports(patientId);
        return recordRows + patientRows;
    }

    /**
     * 批量处理评估项，规则引擎计算展示值
     */
    private void processAssessmentList(CgaReportVO report) {
        List<CgaReportVO.AssessmentItemVO> assessmentList = report.getAssessmentList();
        if (assessmentList == null || assessmentList.isEmpty()) {
            return;
        }

        List<CgaRecord> specialRecords = selectSpecialRecords(report.getPatientId());

        List<CgaReportVO.AssessmentItemVO> processedList = assessmentList.stream()
                .filter(item -> item.getScaleId() != null)
                .filter(item -> item.getScaleCode() != null
                        && ASSESSMENT_CODE_WHITELIST.contains(item.getScaleCode()))
                .map(item -> processAssessmentItem(item, report.getAnswerJson(), specialRecords))
                .collect(Collectors.toList());

        report.setAssessmentList(processedList);
    }

    /**
     * 处理单个评估项的展示值计算
     */
    private CgaReportVO.AssessmentItemVO processAssessmentItem(CgaReportVO.AssessmentItemVO item, String answerJson, List<CgaRecord> specialRecords) {
        String scaleCode = item.getScaleCode();
        Integer rawScore = item.getRawScore();

        if (scaleCode == null || rawScore == null) {
            return item;
        }
        if (!ruleEngine.hasRule(scaleCode)) {
            return item;
        }

        // 通用规则引擎计算
        item.setNormalRange(ruleEngine.getNormalRange(scaleCode));
        item.setItemScore(ruleEngine.formatScore(scaleCode, rawScore));
        item.setTip(ruleEngine.getTip(scaleCode, rawScore));
        // 谵妄专项规则覆盖
        applyDeliriumRule(item, answerJson);
        // 专项评估结果覆盖
        applySpecialRecordResult(item, specialRecords);

        return item;
    }

    /**
     * 查询患者所有专项评估记录
     */
    private List<CgaRecord> selectSpecialRecords(Long patientId) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        return cgaRecordMapper.selectList(new LambdaQueryWrapper<CgaRecord>()
                .eq(CgaRecord::getPatientId, patientId)
                .eq(CgaRecord::getDelFlag, DEL_FLAG_NORMAL)
                .ne(CgaRecord::getScaleId, 0)
                .orderByDesc(CgaRecord::getCreateTime)
                .orderByDesc(CgaRecord::getId))
                .stream()
                .filter(record -> record.getScaleId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(CgaRecord::getScaleId, record -> record, (latest, ignored) -> latest, LinkedHashMap::new),
                        map -> map.values().stream().collect(Collectors.toList())
                ));
    }

    /**
     * 专项评估结果覆盖：已完成专项则展示专项结果
     */
    private void applySpecialRecordResult(CgaReportVO.AssessmentItemVO item, List<CgaRecord> specialRecords) {
        CgaRecord record = findSpecialRecord(item.getScaleCode(), specialRecords);
        if (record == null) {
            return;
        }
        if (StringUtils.isNotBlank(record.getResult())) {
            item.setItemScore(record.getResult());
            item.setTip(record.getSuggest());
        } else if (record.getTotalScore() != null) {
            item.setItemScore(record.getTotalScore() + "分");
        }
        item.setNormalRange(ruleEngine.getNormalRange(item.getScaleCode()));
        item.setRemark("专项评估已完成");
    }

    /**
     * 匹配对应量表的专项评估记录
     */
    private CgaRecord findSpecialRecord(String scaleCode, List<CgaRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }
        for (CgaRecord record : records) {
            if (matchesSpecialRecord(scaleCode, record)) {
                return record;
            }
        }
        return null;
    }

    /**
     * 判断记录是否匹配当前量表编码
     */
    private boolean matchesSpecialRecord(String scaleCode, CgaRecord record) {
        if (record == null || record.getScaleId() == null || scaleCode == null) {
            return false;
        }
        return hasMappedScaleId(scaleCode, record);
    }

    /**
     * 校验量表编码与记录ID是否存在映射关系
     */
    private boolean hasMappedScaleId(String scaleCode, CgaRecord record) {
        List<Long> scaleIds = SPECIAL_SCALE_IDS.get(scaleCode);
        return scaleIds != null && scaleIds.contains(record.getScaleId());
    }

    /**
     * 谵妄专项规则：按题目组合判断阴阳，不使用总分
     */
    private void applyDeliriumRule(CgaReportVO.AssessmentItemVO item, String answerJson) {
        if (!DELIRIUM_ASSESSMENT_CODE.equals(item.getScaleCode()) || answerJson == null || answerJson.trim().isEmpty()) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(answerJson);
            Integer item1Score = getQuestionScore(root, "22");
            Integer item2Score = getQuestionScore(root, "23");
            Integer item3Score = getQuestionScore(root, "24");
            Integer item4Score = getQuestionScore(root, "25");
            if (item1Score == null || item2Score == null || item3Score == null || item4Score == null) {
                return;
            }

            // 0分代表选“是”，前两题全是+后两题至少一个是=阳性
            boolean item1Positive = item1Score == 0;
            boolean item2Positive = item2Score == 0;
            boolean item3Positive = item3Score == 0;
            boolean item4Positive = item4Score == 0;
            boolean deliriumPositive = item1Positive && item2Positive && (item3Positive || item4Positive);

            item.setNormalRange(DELIRIUM_NORMAL_RANGE);
            item.setItemScore(deliriumPositive ? "阳性" : "阴性");
            item.setTip(deliriumPositive ? "谵妄评估结果为阳性" : "");
        } catch (Exception e) {
            log.warn("谵妄规则计算异常，保留通用规则结果，量表编码：{}", item.getScaleCode(), e);
        }
    }

    /**
     * 从答案JSON中提取指定题目的得分
     */
    private Integer getQuestionScore(JsonNode root, String questionId) {
        JsonNode node = root.get(questionId);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            JsonNode score = node.get("score");
            return score == null || !score.canConvertToInt() ? null : score.asInt();
        }
        return node.canConvertToInt() ? node.asInt() : null;
    }
}
