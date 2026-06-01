package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;

public class YinYangRule implements CgaRule {

    /** 正常参考范围 */
    private static final String NORMAL_RANGE = "-";

    /** 阳性提示信息 */
    private final String positiveTip;

    /** 阴性提示信息（可为空） */
    private final String negativeTip;

    /**
     * 构造阴阳型规则
     *
     * @param positiveTip 阳性提示（如：建议进行体能状况评估 B3）
     * @param negativeTip 阴性提示（可为null）
     */
    public YinYangRule(String positiveTip, String negativeTip) {
        this.positiveTip = positiveTip;
        this.negativeTip = negativeTip;
    }
    /**
     * 获取正常范围
     */
    @Override
    public String getNormalRange() {
        return NORMAL_RANGE;
    }

    /**
     * 获得得分展示值
     * 阴阳型返回"阳性"/"阴性"，评分型返回"X分"
     */
    @Override
    public String formatScore(int itemScore) {
        return itemScore == 0 ? "阳性" : "阴性";
    }

    /**
     * 获取评估提示信息
     */
    @Override
    public String getTip(int itemScore) {
        if(itemScore == 0){
            return positiveTip != null ? positiveTip : "";
        }
        return negativeTip != null ? negativeTip : "";
    }
}