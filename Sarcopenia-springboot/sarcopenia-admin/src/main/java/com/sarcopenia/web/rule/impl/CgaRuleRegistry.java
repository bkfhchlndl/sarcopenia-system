package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class CgaRuleRegistry {

    /** 量表编码与规则的映射 */
    private static final Map<String, CgaRule> RULES = new LinkedHashMap<>();

    static {
        // ==================== 阴阳型量表（0分=阳性） ====================

        // 体能筛查
        RULES.put("exercise", new YinYangRule("建议进行体能状况评估 B3", null));

        // 抑郁
        RULES.put("depression", new YinYangRule("建议进行抑郁评估 WS/T802 B4", null));

        // 焦虑
        RULES.put("anxiety", new YinYangRule("建议进行焦虑评估 WS/T802 B3", null));

        // 跌倒
        RULES.put("fall", new YinYangRule("建议进行跌倒风险评估 MZT 185 A1", null));

        // 尿失禁
        RULES.put("incontinence", new YinYangRule("建议进行尿失禁评估 B11", null));

        // 便秘
        RULES.put("constipation", new YinYangRule("建议进行便秘评估 B12", null));

        // 睡眠障碍
        RULES.put("sleep", new YinYangRule("建议进行睡眠障碍评估 B13", null));

        // 慢性疼痛
        RULES.put("chronic", new YinYangRule("建议进行慢性疼痛评估 B14", null));

        // 压力性损伤
        RULES.put("pressure", new YinYangRule("建议进行压力性损伤风险评估 B16", null));

        // 吞咽障碍
        RULES.put("swallow", new YinYangRule("建议进行吞咽障碍评估 B17", null));

        // 肌少症
        RULES.put("sarcopenia", new YinYangRule("建议进行肌肉力量和躯体功能评估", null));

        // 谵妄
        RULES.put("delirium", new YinYangRule("B21", null));


        // ==================== 评分型量表（简单判断） ====================

        // 视力
        RULES.put("vision", new ScoreRule("4分",
                score -> score >= 4 ? "视力正常" : "≤3分应进一步到眼科评估"));

        // 视功能
        RULES.put("visual_function", new ScoreRule("3分",
                score -> score >= 3 ? "视功能良好" : "≤2分应进一步到眼科评估"));

        // 听力
        RULES.put("listening", new ScoreRule("4分",
                score -> score >= 4 ? "听力正常" : "≤3分应进一步到耳鼻喉科评估"));

        // 认知功能
        RULES.put("cognition", new ScoreRule("-",
                score -> score <= 2 ? "建议进行认知功能评估 WS/T484 C1" : ""));

        // 居家能力
        RULES.put("stay_at_home", new ScoreRule("14分",
                score -> score >= 14 ? "正常" : "得分越低，风险越大 B9"));

        // 营养
        RULES.put("nutrition", new ScoreRule("≤11分为营养不良风险",
                score -> score <= 11 ? "建议进行营养不良评估及诊断 B18/B19" : "正常营养状况"));


        // ==================== 评分型量表（复杂分段判断） ====================

        // 衰弱（三段判断）
        RULES.put("frailty", new ScoreRule("≥3项衰弱；<2项衰弱前期；0项无衰弱",
                score -> {
                    if (score >= 3) {
                        return "衰弱 B10";
                    } else if (score > 0) {
                        return "衰弱前期 B10";
                    } else {
                        return "无衰弱";
                    }
                }));

        // 多重用药（三段判断）
        RULES.put("medication", new ScoreRule("<5种无；5-9种轻度；≥10种重度",
                score -> {
                    if (score >= 2) {
                        return "无多重用药";
                    } else if (score == 1) {
                        return "轻度多重用药";
                    } else {
                        return "重度用药";
                    }
                }));

        // 基本日常生活
        RULES.put("daily_life", new ScoreRule("≤40重度依赖；41-60中度；61-99轻度；100无依赖",
                score -> {
                    if (score <= 40){
                        return "重度依赖";
                    } else if (score <= 60) {
                        return "中度依赖";
                    } else if (score <= 99) {
                        return "轻度依赖";
                    } else {
                        return "无依赖";
                    }
                }));
        RULES.put("sppb", new ScoreRule("最终得分为以上3项的综合",
                score -> {
                    if (score <= 6){
                        return "较差";
                    } else if (score <= 8) {
                        return "中等";
                    } else {
                        return "良好";
                    }
                }));
//        RULES.put("vision_hearing", new ScoreRule("最终得分为以上3项的综合",
//                score -> {
//                    if (score <= 6){
//                        return "较差";
//                    } else if (score <= 8) {
//                        return "中等";
//                    } else {
//                        return "良好";
//                    }
//                }));
    }
    /**
     * 根据量表编码获取规则
     */
    public static CgaRule getRule(String scaleCode) {
        return RULES.get(scaleCode);
    }

    /**
     * 判断是否存在该量表的规则
     */
    public static boolean hasRule(String scaleCode) {
        return RULES.containsKey(scaleCode);
    }
}