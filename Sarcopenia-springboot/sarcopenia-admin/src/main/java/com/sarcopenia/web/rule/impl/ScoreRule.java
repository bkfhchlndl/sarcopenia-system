package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;

import java.util.function.Function;

/**
 * 评分型评估规则实现
 * 适用于按分数分段输出结果的量表，支持自定义得分对应的提示逻辑
 */
public class ScoreRule implements CgaRule {

    /** 正常参考范围描述 */
    private final String normalRange;

    /** 得分转提示信息的自定义处理函数 */
    private final Function<Integer, String> tipFunction;

    /**
     * 构造评分型量表规则
     * @param normalRange 正常参考范围描述
     * @param tipFunction 得分转提示信息的处理函数
     */
    public ScoreRule(String normalRange, Function<Integer, String> tipFunction) {
        this.normalRange = normalRange;
        this.tipFunction = tipFunction;
    }

    @Override
    public String getNormalRange() {
        return normalRange;
    }

    @Override
    public String formatScore(int itemScore) {
        return itemScore + "分";
    }

    @Override
    public String getTip(int itemScore) {
        String tip = tipFunction.apply(itemScore);
        return tip != null ? tip : "";
    }
}