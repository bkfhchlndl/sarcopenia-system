package com.sarcopenia.web.rule.impl;

import com.sarcopenia.web.rule.CgaRule;

import java.util.function.Function;

public class ScoreRule implements CgaRule {

    /** 正常参考范围描述 */
    private final String normalRange;

    /** 提示信息生成函数 */
    private final Function<Integer, String> tipFunction;

    public ScoreRule(String normalRange, Function<Integer, String> tipFunction) {
        this.normalRange = normalRange;
        this.tipFunction = tipFunction;
    }
    /**
     * 获取正常范围
     */
    @Override
    public String getNormalRange() {
        return normalRange;
    }

    /**
     * 获得得分展示值
     * 阴阳型返回"阳性"/"阴性"，评分型返回"X分"
     */
    @Override
    public String formatScore(int itemScore) {
        return itemScore + "分";
    }

    /**
     * 获取评估提示信息
     */
    @Override
    public String getTip(int itemScore) {
        String apply = tipFunction.apply(itemScore);
        if(apply == null){
            return "";
        }
        return apply != null ? apply : "";
    }
}