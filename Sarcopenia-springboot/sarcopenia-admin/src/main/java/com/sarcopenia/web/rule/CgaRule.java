package com.sarcopenia.web.rule;

public interface CgaRule {

    /**
     * 获取正常范围
     */
    String getNormalRange();

    /**
     * 获得得分展示值
     * 阴阳型返回"阳性"/"阴性"，评分型返回"X分"
     */
    String formatScore(int itemScore);

    /**
     * 获取评估提示信息
     */
    String getTip(int itemScore);
}
