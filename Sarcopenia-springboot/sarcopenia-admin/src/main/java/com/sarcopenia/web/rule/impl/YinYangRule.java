package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;

/**
 * 阴阳型评估规则实现
 * 适用于仅区分阳性/阴性结果的量表，判定规则：0分为阳性，非0分为阴性
 */
public class YinYangRule implements CgaRule {

    /** 正常参考范围描述 */
    private final String normalRange;

    /** 阳性结果提示信息 */
    private final String positiveTip;

    /** 阴性结果提示信息 */
    private final String negativeTip;

    /**
     * 构造阴阳型量表规则
     * @param normalRange 正常参考范围描述
     * @param positiveTip 阳性结果提示信息
     * @param negativeTip 阴性结果提示信息
     */
    public YinYangRule(String normalRange, String positiveTip, String negativeTip) {
        this.normalRange = normalRange;
        this.positiveTip = positiveTip;
        this.negativeTip = negativeTip;
    }

    @Override
    public String getNormalRange() {
        return normalRange != null ? normalRange : "";
    }

    @Override
    public String formatScore(int itemScore) {
        return itemScore == 0 ? "阳性" : "阴性";
    }

    @Override
    public String getTip(int itemScore) {
        if (itemScore == 0) {
            return positiveTip != null ? positiveTip : "";
        }
        return negativeTip != null ? negativeTip : "";
    }
}