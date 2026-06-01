package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;
import org.springframework.stereotype.Component;

@Component
public class CgaRuleEngine {

    /**
     * 获取正常参考范围
     */
    public String getNormalRange(String scaleCode) {
        CgaRule rule = CgaRuleRegistry.getRule(scaleCode);
        if (rule == null) {
            return "-";
        }
        return rule.getNormalRange();
    }

    /**
     * 获取得分展示值
     */
    public String formatScore(String scaleCode, int itemScore) {
        CgaRule rule = CgaRuleRegistry.getRule(scaleCode);
        if (rule == null) {
            return itemScore + "分";
        }
        return rule.formatScore(itemScore);
    }
    /**
     * 获取评估提示信息
     */
    public String getTip(String scaleCode, int itemScore) {
        CgaRule rule = CgaRuleRegistry.getRule(scaleCode);
        if (rule == null) {
            return "";
        }
        return rule.getTip(itemScore);
    }
    /**
     * 判断是否存在该量表的规则
     */
    public boolean hasRule(String scaleCode) {
        return CgaRuleRegistry.hasRule(scaleCode);
    }
}