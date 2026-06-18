package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CGA量表规则注册器
 * 统一管理所有量表的评估规则，提供规则查询、存在性校验能力
 */
public class CgaRuleRegistry {

    /** 量表编码与对应规则实例的映射容器 */
    private static final Map<String, CgaRule> RULES = new LinkedHashMap<>();

    static {
        // ==================== 阴阳型量表规则 ====================
        // 构造参数说明：(正常范围描述, 阳性提示文案, 阴性提示文案)

        // 基本日常生活活动能力筛查
        RULES.put("basic_adl", new YinYangRule(
                "无依赖",
                "基本日常生活活动能力筛查阳性，应进行基本日常生活活动能力评估",
                "无依赖"
        ));

        // 工具性日常生活活动能力筛查
        RULES.put("instrumental_adl", new YinYangRule(
                "全部条目可独立完成；分数越低工具性日常活动能力越差",
                "工具性日常生活活动能力筛查阳性，应进行工具性日常生活活动能力评估",
                "工具性日常生活活动能力正常"
        ));

        // 运动功能筛查
        RULES.put("exercise_function", new YinYangRule(
                "10-12分=正常；7-9分=轻度下降；4-6分=中度下降；0-3分=重度下降",
                "简易体能筛查阳性，应进行简易体能状况评估",
                "简易体能状况评估正常"
        ));

        // 视力视觉简易评估
        RULES.put("vision_assessment", new YinYangRule(
                "视力筛查>3分且视觉功能筛查>2分=阴性",
                "视力筛查≤3分或视觉功能筛查≤2分，应进一步进行视力视觉评估",
                "视力视觉筛查阴性"
        ));

        // 听力简易评估
        RULES.put("hearing_assessment", new YinYangRule(
                "听力筛查>3分且耳语试验能听清=阴性",
                "听力简易评估≤3分或耳语试验阳性者，应进一步进行听力评估",
                "听力筛查阴性"
        ));

        // 居家环境筛查
        RULES.put("home_environment", new YinYangRule(
                " - ",
                "居家环境筛查提示风险，应进行居家环境评估",
                "居家环境无明显风险"
        ));

        // 尿失禁筛查
        RULES.put("incontinence_screen", new YinYangRule(
                "阴性",
                "尿失禁筛查阳性，应进行尿失禁评估",
                "尿失禁筛查阴性"
        ));

        // 便秘筛查
        RULES.put("constipation_screen", new YinYangRule(
                "阴性",
                "便秘筛查阳性，应进行便秘评估",
                "便秘筛查阴性"
        ));

        // 睡眠障碍筛查
        RULES.put("sleep_disorder_screen", new YinYangRule(
                "无睡眠障碍症状=阴性",
                "睡眠障碍筛查阳性，应进行睡眠障碍评估",
                "睡眠障碍筛查阴性"
        ));

        // 慢性疼痛筛查
        RULES.put("chronic_pain_screen", new YinYangRule(
                "无慢性疼痛症状=阴性",
                "慢性疼痛筛查阳性，应进行慢性疼痛评估",
                "慢性疼痛筛查阴性"
        ));

        // 压力性损伤风险筛查
        RULES.put("pressure_injury_risk", new YinYangRule(
                "Braden评分≥19分=无风险；15-18分=轻度；13-14分=中度；10-12分=高度；≤9分=极高危",
                "压力性损伤风险筛查阳性，应进行压力性损伤风险评估",
                "无压力性损伤风险"
        ));

        // 洼田饮水试验
        RULES.put("water_swallowing_test", new YinYangRule(
                "洼田饮水试验1级=阴性；2级及以上提示不同程度吞咽障碍",
                "吞咽障碍筛查阳性，应进行吞咽障碍评估",
                "吞咽功能正常"
        ));

        // 营养风险筛查
        RULES.put("nutrition_risk_screen", new YinYangRule(
                "NRS-2002评分<3分=无营养风险；≥3分提示存在营养风险",
                "营养风险或营养不良风险筛查阳性，应进行营养不良的评估及诊断",
                "无营养风险"
        ));

        // 抑郁筛查
        RULES.put("depression_screen", new YinYangRule(
                "无抑郁症状=阴性",
                "抑郁筛查阳性，应进行抑郁评估",
                "抑郁筛查阴性"
        ));

        // 焦虑筛查
        RULES.put("anxiety_screen", new YinYangRule(
                "无焦虑症状=阴性",
                "焦虑筛查阳性，应进行焦虑评估",
                "焦虑筛查阴性"
        ));

        // 跌倒风险筛查
        RULES.put("fall_risk_screen", new YinYangRule(
                "无跌倒风险因素=阴性；选'是'项越多跌倒风险越高",
                "跌倒风险筛查阳性，应进行跌倒风险评估",
                "无跌倒风险"
        ));

        // 肌少症筛查
        RULES.put("sarcopenia_screen", new YinYangRule(
                "筛查阴性=无肌少症风险；筛查阳性需进一步进行肌肉力量、躯体功能评估",
                "肌少症筛查阳性，宜进行肌肉力量和躯体功能评估；阳性者进一步进行肌少症评估及诊断",
                "肌少症筛查阴性"
        ));


        // ==================== 评分型量表规则（简单二分判断） ====================

        // 认知功能筛查
        RULES.put("cognition_screen", new ScoreRule(
                "0-2分阳性；3-5分阴性",
                score -> score <= 2 ? "认知功能障碍筛查阳性" : "阴性"
        ));

        // 谵妄评估（报告服务层会按条目做精确判断，此处为通用兜底规则）
        RULES.put("delirium_assessment", new ScoreRule(
                "第1题和第2题均选'是'，且第3题或第4题至少一题选'是'",
                score -> score >= 3 ? "正常" : "谵妄评估阳性"
        ));


        // ==================== 评分型量表规则（多分段复杂判断） ====================

        // 衰弱评估（FRAIL量表：分数越高健康状态越好）
        RULES.put("frail_assessment", new ScoreRule(
                "FRAIL量表5分=无衰弱；3-4分=衰弱前期；0-2分=衰弱",
                score -> {
                    if (score == 5) {
                        return "无衰弱";
                    } else if (score >= 3) {
                        return "衰弱前期";
                    } else {
                        return "衰弱";
                    }
                }
        ));

        // 多重用药评估
        RULES.put("polypharmacy_assessment", new ScoreRule(
                "用药<5种=无多重用药；5-9种=轻度多重用药；≥10种=重度多重用药",
                score -> score >= 2 ? "无多重用药" : "重度多重用药"
        ));
    }

    /**
     * 根据量表编码获取对应的评估规则
     * @param scaleCode 量表编码
     * @return 匹配的规则实例，无匹配时返回null
     */
    public static CgaRule getRule(String scaleCode) {
        return RULES.get(scaleCode);
    }

    /**
     * 判断是否存在指定量表的评估规则
     * @param scaleCode 量表编码
     * @return 存在返回true，不存在返回false
     */
    public static boolean hasRule(String scaleCode) {
        return RULES.containsKey(scaleCode);
    }
}