package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;
import org.springframework.stereotype.Component;

/**
 * CGA评估规则引擎
 * 统一调度各量表的评估规则实现，提供参考范围查询、得分格式化、评估提示等核心能力
 */
@Component
public class CgaRuleEngine {

    /**
     * 获取指定量表的正常参考范围描述
     * @param scaleCode 量表编码
     * @return 正常参考范围文本，无对应规则时返回"-"
     */
    public String getNormalRange(String scaleCode) {
        CgaRule rule = CgaRuleRegistry.getRule(scaleCode);
        if (rule == null) {
            return "-";
        }
        return rule.getNormalRange();
    }

    /**
     * 格式化量表原始得分为展示文本
     * 阴阳型量表返回阳性/阴性，评分型量表返回带单位的分数字符串
     * @param scaleCode 量表编码
     * @param itemScore 原始得分
     * @return 格式化后的得分展示文本，无对应规则时直接返回"X分"
     */
    public String formatScore(String scaleCode, int itemScore) {
        CgaRule rule = CgaRuleRegistry.getRule(scaleCode);
        if (rule == null) {
            return itemScore + "分";
        }
        return rule.formatScore(itemScore);
    }

    /**
     * 根据得分获取对应评估提示信息
     * @param scaleCode 量表编码
     * @param itemScore 原始得分
     * @return 评估提示文本，无对应规则时返回空字符串
     */
    public String getTip(String scaleCode, int itemScore) {
        CgaRule rule = CgaRuleRegistry.getRule(scaleCode);
        if (rule == null) {
            return "";
        }
        return rule.getTip(itemScore);
    }

    /**
     * 判断是否存在指定量表的评估规则
     * @param scaleCode 量表编码
     * @return 存在返回true，不存在返回false
     */
    public boolean hasRule(String scaleCode) {
        return CgaRuleRegistry.hasRule(scaleCode);
    }
}