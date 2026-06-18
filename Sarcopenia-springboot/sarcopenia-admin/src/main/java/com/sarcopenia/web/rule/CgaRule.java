package com.sarcopenia.web.rule;

/**
 * CGA评估规则统一接口
 * 各量表评估规则的抽象约定，负责得分格式化、正常范围与提示信息输出
 */
public interface CgaRule {

    /**
     * 获取量表评估的正常参考范围
     * @return 正常范围描述文本
     */
    String getNormalRange();

    /**
     * 格式化评估得分展示值
     * 阴阳判定型返回"阳性"/"阴性"，数值评分型返回"X分"
     * @param itemScore 原始得分
     * @return 格式化后的得分展示文本
     */
    String formatScore(int itemScore);

    /**
     * 根据得分获取对应评估提示信息
     * @param itemScore 原始得分
     * @return 评估提示文本
     */
    String getTip(int itemScore);
}