<template>
  <div class="nutrition-assessment-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>微营养状况评估</h1>
          <p>老年综合评估 · 共 {{ totalQuestions }} 项</p>
        </div>
      </div>
      <div class="top-right">
        <div class="patient-badge">
          <span class="badge-avatar">{{ (patientName || '患').slice(0, 1) }}</span>
          <div>
            <div class="badge-name">{{ patientName }}</div>
            <div class="badge-id">{{ patientId ? '#' + patientId : '-' }}</div>
          </div>
        </div>
      </div>
    </header>

    <!-- 进度条 -->
    <section class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">已完成 {{ answeredCount }} / {{ totalQuestions }} 项</span>
    </section>

    <!-- 第一部分：微营养筛选量表（MNA-SF） -->
    <section class="section-card" v-loading="loading">
      <div class="section-title">
        <el-icon><Food /></el-icon>
        <span>微营养筛选量表（MNA-SF）</span>
      </div>
      <div class="question-list">
        <div
            v-for="(question, qIndex) in scaleData?.questionList || []"
            :key="'main-' + question.questionId"
            class="question-card"
            :class="{ 'is-answered': isMainAnswered(question) }"
        >
          <div class="q-head">
            <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
            <span class="q-title">{{ question.title }}</span>
            <span v-if="isMainAnswered(question)" class="q-done">
              <el-icon><Check /></el-icon>
            </span>
          </div>

          <div class="q-options">
            <div
                v-for="option in question.optionList"
                :key="option.id"
                class="q-option"
                :class="{ 'is-selected': mainAnswers[question.questionId]?.[0] === option.id }"
                @click="selectMainOption(question, option, qIndex)"
            >
              <div class="opt-left">
                <div class="opt-radio" :class="{ 'is-checked': mainAnswers[question.questionId]?.[0] === option.id }">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="opt-content">
                  <span class="opt-label">{{ option.content }}</span>
                </div>
              </div>
              <div v-if="option.score !== undefined && option.score !== null" class="opt-right">
                <span class="opt-score">{{ option.score }}</span>
                <span class="opt-unit">分</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 第一部分得分 -->
      <div class="score-summary" v-if="mainQuestionCount > 0">
        <span>微营养筛选得分：</span>
        <span class="score-num">{{ mainScore }}</span>
        <span class="score-unit-sm"> / {{ maxMainScore }} 分</span>
      </div>
    </section>

    <!-- 第二部分：营养标准量表（仅主量表得分<11分时显示，支持跳过） -->
    <section class="section-card" v-if="shouldShowStdArea" v-loading="loading">
      <div class="section-title standard-title">
        <el-icon><Document /></el-icon>
        <span>营养标准量表</span>
        <span class="section-tag">补充评估</span>
        <el-button type="info" plain size="small" @click="handleSkipExtension">
          {{ skipExtension ? '恢复扩展评估' : '不参与扩展评估' }}
        </el-button>
      </div>
      <div class="question-list" v-if="!skipExtension">
        <template v-for="(question, qIndex) in standardScaleData?.questionList || []" :key="'std-' + question.questionId">
          <div
              v-if="qIndex < STD_CONTROL_QUESTION_INDEX[0] + 1 || shouldShowStdQ6Q7"
              class="question-card"
              :class="{ 'is-answered': isStandardAnswered(question) }"
          >
            <div class="q-head q-head-std">
              <span class="q-index q-index-std">{{ String(qIndex + 1).padStart(2, '0') }}</span>
              <span class="q-title">{{ question.title }}</span>
              <span v-if="isStandardAnswered(question)" class="q-done">
                <el-icon><Check /></el-icon>
              </span>
            </div>

            <div class="q-options">
              <div
                  v-for="option in question.optionList"
                  :key="option.id"
                  class="q-option"
                  :class="{ 'is-selected': standardAnswers[question.questionId]?.[0] === option.id }"
                  @click="selectStandardOption(question, option, qIndex)"
              >
                <div class="opt-left">
                  <div class="opt-radio" :class="{ 'is-checked': standardAnswers[question.questionId]?.[0] === option.id }">
                    <el-icon><CircleCheck /></el-icon>
                  </div>
                  <div class="opt-content">
                    <span class="opt-label">{{ option.content }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </section>

    <!-- 结果展示区 -->
    <section class="result-area">
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠</span>
          <span>评估结果 · {{ nutritionLevel.text }}</span>
        </div>
        <div class="score-display" v-if="!showStandardScale">
          <span class="score-big">{{ mainScore }}</span>
          <span class="score-unit">分</span>
        </div>
        <div class="score-display" v-else>
          <span class="score-big">{{ totalScore }}</span>
          <span class="score-unit">分</span>
        </div>
        <div class="score-breakdown" v-if="showStandardScale && totalQuestions > 0">
          <span class="breakdown-item">微营养筛选：{{ mainScore }} 分</span>
          <span class="breakdown-sep">+</span>
          <span class="breakdown-item">营养标准量表：{{ standardScore }} 分</span>
        </div>

        <!-- 整合结论 -->
        <div v-if="combinedResultText" class="combined-result">
          <div class="cr-block">
            <div class="cr-title">📋 评估结论</div>
            <div class="cr-text">{{ combinedResultText }}</div>
          </div>
          <div class="cr-block">
            <div class="cr-title"><span class="cr-normal">●</span> 正常项</div>
            <div class="cr-text">{{ normalItemsText || '无' }}</div>
          </div>
          <div v-if="abnormalItemsText" class="cr-block">
            <div class="cr-title"><span class="cr-abnormal">●</span> 风险 / 异常项</div>
            <div class="cr-text">{{ abnormalItemsText }}</div>
          </div>
        </div>
        <div v-else-if="totalQuestions > 0" class="no-finding">
          <div class="hint-text">完成全部题目后显示评估详情</div>
        </div>
      </div>

      <!-- 综合建议 -->
      <div class="result-card suggest-card">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ combinedSuggest }}</p>
      </div>
    </section>

    <!-- 提交按钮 -->
    <div class="submit-area">
      <el-button
          type="primary"
          size="large"
          round
          :loading="submitting"
          :disabled="!canSubmit"
          @click="submitAssessment"
      >
        <el-icon><Check /></el-icon>
        提交评估
      </el-button>
      <p v-if="!canSubmit && totalQuestions > 0" class="submit-hint">
        {{ submitHintText }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, CircleCheck, Document, Food } from '@element-plus/icons-vue'
import { selectMicroNutritionScale, selectNutritionalStandardsScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置（统一管理，避免硬编码） ====================
const DEFAULT_SCALE_ID = 37 // 默认量表ID
// 主量表互斥题索引（第6、7题二选一，数组从0开始）
const MAIN_MUTEX_QUESTION_INDEX = [5, 6]
// 标准量表互斥题索引
const STD_MUTEX_QUESTION_INDEX = [5, 6]
// 标准量表控制第6、7题显示的题目索引（第4、5题）
const STD_CONTROL_QUESTION_INDEX = [3, 4]
// 营养等级判定阈值（占满分比例）
const NUTRITION_LEVEL_GOOD_RATIO = 0.8
const NUTRITION_LEVEL_RISK_RATIO = 0.55

// ==================== 状态变量 ====================
const loading = ref(false) // 页面加载状态
const submitting = ref(false) // 提交状态
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'
const skipExtension = ref(false) // 是否跳过扩展评估

// 量表数据
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
const standardScaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })

// 答案存储
const mainAnswers = reactive({})
const standardAnswers = reactive({})

// ==================== 工具函数 ====================
/**
 * 将参数转换为数字ID
 */
const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}

const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || DEFAULT_SCALE_ID

// ==================== 数据加载 ====================
/**
 * 加载主量表数据
 */
const loadMainScale = async () => {
  try {
    loading.value = true
    const res = await selectMicroNutritionScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载微营养量表数据失败')
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  } finally {
    loading.value = false
  }
}

/**
 * 加载标准量表数据
 */
const loadStandardScale = async () => {
  try {
    const res = await selectNutritionalStandardsScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      standardScaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载营养标准量表数据失败')
    standardScaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}

// 监听路由参数变化，自动重载数据并清空答案
watch(
    () => route.query,
    () => {
      // 清空已有答案
      Object.keys(mainAnswers).forEach(k => delete mainAnswers[k])
      Object.keys(standardAnswers).forEach(k => delete standardAnswers[k])
      skipExtension.value = false
      // 重新加载数据
      loadMainScale()
      loadStandardScale()
    },
    { immediate: true }
)

// ==================== 选项选择逻辑 ====================
/**
 * 主量表选项选择
 */
function selectMainOption(question, option, qIndex) {
  // 互斥题处理：选其中一题，清空另一题
  if (MAIN_MUTEX_QUESTION_INDEX.includes(qIndex)) {
    const otherIndex = MAIN_MUTEX_QUESTION_INDEX.find(i => i !== qIndex)
    const otherQuestion = scaleData.value.questionList?.[otherIndex]
    if (otherQuestion) delete mainAnswers[otherQuestion.questionId]
  }
  mainAnswers[question.questionId] = [option.id]
}

/**
 * 标准量表选项选择
 */
function selectStandardOption(question, option, qIndex) {
  // 互斥题处理
  if (STD_MUTEX_QUESTION_INDEX.includes(qIndex)) {
    const otherIndex = STD_MUTEX_QUESTION_INDEX.find(i => i !== qIndex)
    const otherQuestion = standardScaleData.value.questionList?.[otherIndex]
    if (otherQuestion) delete standardAnswers[otherQuestion.questionId]
  }

  // 控制题选择“无”时，清空后续条件题
  if (STD_CONTROL_QUESTION_INDEX.includes(qIndex)) {
    const isNone = isOptionNone(option)
    if (isNone) {
      STD_MUTEX_QUESTION_INDEX.forEach(index => {
        const q = standardScaleData.value.questionList?.[index]
        if (q) delete standardAnswers[q.questionId]
      })
    }
  }

  standardAnswers[question.questionId] = [option.id]
}

/**
 * 判断主量表题目是否已答
 */
function isMainAnswered(question) {
  const selected = mainAnswers[question.questionId] || []
  return selected.length > 0
}

/**
 * 判断标准量表题目是否已答
 */
function isStandardAnswered(question) {
  const selected = standardAnswers[question.questionId] || []
  return selected.length > 0
}

/**
 * 判断选项是否为“无”（优先字段判断，兼容文本匹配）
 */
function isOptionNone(option) {
  if (!option) return false
  if (option.isNone !== undefined) return Boolean(option.isNone)
  const text = String(option.content || '').trim()
  return ['无', '没有', '无异常', '完全没有'].includes(text)
}

/**
 * 判断标准量表某题选中项是否为“无”
 */
function isStdAnswerNone(qIndex) {
  const q = standardScaleData.value.questionList?.[qIndex]
  if (!q) return false
  const selectedIds = standardAnswers[q.questionId] || []
  if (selectedIds.length === 0) return false
  const option = q.optionList?.find(o => o.id === selectedIds[0])
  return isOptionNone(option)
}

// ==================== 题目数量与进度计算 ====================
/**
 * 主量表总题数（互斥题按1题算）
 */
const mainQuestionCount = computed(() => {
  const total = scaleData.value.questionList?.length || 0
  if (total <= MAIN_MUTEX_QUESTION_INDEX.length) return total
  // 互斥题二选一，总数减1
  return total - 1
})

/**
 * 标准量表总题数
 */
const standardQuestionCount = computed(() => {
  const total = standardScaleData.value.questionList?.length || 0
  const baseCount = Math.min(STD_CONTROL_QUESTION_INDEX.length + 1, total)
  // 条件题显示时，互斥题按1题算
  if (shouldShowStdQ6Q7.value && total > STD_MUTEX_QUESTION_INDEX[1]) {
    return baseCount + 1
  }
  return baseCount
})

/**
 * 主量表已答题数
 */
const mainAnsweredCount = computed(() => {
  let count = 0
  // 前5题正常计数
  for (let i = 0; i < Math.min(MAIN_MUTEX_QUESTION_INDEX[0], (scaleData.value.questionList || []).length); i++) {
    if (isMainAnswered(scaleData.value.questionList[i])) count++
  }
  // 互斥题只要答了1题就算1
  const hasMutexAnswer = MAIN_MUTEX_QUESTION_INDEX.some(index => {
    const q = scaleData.value.questionList?.[index]
    return q && isMainAnswered(q)
  })
  if (hasMutexAnswer) count++
  return count
})

/**
 * 标准量表已答题数
 */
const standardAnsweredCount = computed(() => {
  let count = 0
  // 控制题正常计数
  for (let i = 0; i < Math.min(STD_CONTROL_QUESTION_INDEX[0] + 1, (standardScaleData.value.questionList || []).length); i++) {
    if (isStandardAnswered(standardScaleData.value.questionList[i])) count++
  }
  // 条件题显示时，互斥题答了1题就算1
  if (shouldShowStdQ6Q7.value) {
    const hasMutexAnswer = STD_MUTEX_QUESTION_INDEX.some(index => {
      const q = standardScaleData.value.questionList?.[index]
      return q && isStandardAnswered(q)
    })
    if (hasMutexAnswer) count++
  }
  return count
})

/**
 * 总题数
 */
const totalQuestions = computed(() => {
  if (showStandardScale.value) {
    return mainQuestionCount.value + standardQuestionCount.value
  }
  return mainQuestionCount.value
})

/**
 * 已答题总数
 */
const answeredCount = computed(() => {
  if (showStandardScale.value) {
    return mainAnsweredCount.value + standardAnsweredCount.value
  }
  return mainAnsweredCount.value
})

/**
 * 进度百分比
 */
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ==================== 分数计算 ====================
/**
 * 主量表得分
 */
const mainScore = computed(() => {
  let sum = 0
  for (const q of scaleData.value.questionList || []) {
    const selectedIds = mainAnswers[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (option) sum += Number(option.score || 0)
  }
  return sum
})

/**
 * 主量表满分（动态计算）
 */
const maxMainScore = computed(() => {
  let totalMax = 0
  const questionList = scaleData.value.questionList || []
  for (const q of questionList) {
    const optScores = q.optionList.map(o => Number(o.score || 0))
    totalMax += Math.max(...optScores)
  }
  // 互斥题按最高分计算，减去较低的满分
  const q1 = questionList[MAIN_MUTEX_QUESTION_INDEX[0]]
  const q2 = questionList[MAIN_MUTEX_QUESTION_INDEX[1]]
  if (q1 && q2) {
    const max1 = Math.max(...q1.optionList.map(o => Number(o.score || 0)))
    const max2 = Math.max(...q2.optionList.map(o => Number(o.score || 0)))
    totalMax -= Math.min(max1, max2)
  }
  return totalMax
})

/**
 * 标准量表得分
 */
const standardScore = computed(() => {
  let sum = 0
  for (const q of standardScaleData.value.questionList || []) {
    const selectedIds = standardAnswers[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (option) sum += Number(option.score || 0)
  }
  return sum
})

/**
 * 标准量表满分
 */
const maxStandardScore = computed(() => {
  let max = 0
  for (const q of standardScaleData.value.questionList || []) {
    const maxOpt = Math.max(...q.optionList.map(o => Number(o.score || 0)))
    max += maxOpt
  }
  return max
})

/**
 * 总分
 */
const totalScore = computed(() => {
  return mainScore.value + standardScore.value
})

// ==================== 显示控制 ====================
/**
 * 是否显示标准量表区域（主量表答完且得分<11分）
 */
const shouldShowStdArea = computed(() => {
  if (mainQuestionCount.value === 0) return false
  if (mainAnsweredCount.value < mainQuestionCount.value) return false
  return mainScore.value < 11
})

/**
 * 是否显示标准量表的题目（未跳过 + 区域显示）
 */
const showStandardScale = computed(() => {
  if (skipExtension.value) return false
  return shouldShowStdArea.value
})

/**
 * 是否显示标准量表第6、7题（控制题都已答，且都不是“无”）
 */
const shouldShowStdQ6Q7 = computed(() => {
  const q4 = standardScaleData.value.questionList?.[STD_CONTROL_QUESTION_INDEX[0]]
  const q5 = standardScaleData.value.questionList?.[STD_CONTROL_QUESTION_INDEX[1]]
  if (!q4 || !q5) return false
  if (!isStandardAnswered(q4) || !isStandardAnswered(q5)) return false
  return !isStdAnswerNone(STD_CONTROL_QUESTION_INDEX[0]) && !isStdAnswerNone(STD_CONTROL_QUESTION_INDEX[1])
})

// ==================== 选项正常/异常判定 ====================
/**
 * 判断选项是否为正常项（优化逻辑：否定+异常=正常，否定+正常=异常）
 */
function isOptionNormal(optionContent) {
  const text = String(optionContent || '').trim().toLowerCase()

  // 1. 否定词 + 异常词 = 正常（如：无下降、没有困难、不存在不足）
  const negativeAbnormalRegex = /(无|没|不|没有|不存在)(.*?)(下降|减少|差|困难|不足|低|障碍|丢失|衰退)/
  if (negativeAbnormalRegex.test(text)) return true

  // 2. 否定词 + 正常词 = 异常（如：不好、不佳、不足、不能）
  const negativeNormalRegex = /(不|没|无)(.*?)(好|佳|足|正常|充足|充分|愿意|喜欢|能|可以)/
  if (negativeNormalRegex.test(text)) return false

  // 3. 明确正常关键词
  const normalKeywords = [
    '正常', '良好', '充足', '充分', '可以', '能', '会', '愿意', '喜欢',
    '没问题', '从未', '从不', '无变化', '无压力', '独立', '每周3次及以上',
    '每天', '充足', '丰富', '均衡', '正常范围', '大于', '高于', '≧', '≥'
  ]
  if (normalKeywords.some(k => text.includes(k))) return true

  // 4. 明确异常关键词
  const abnormalKeywords = [
    '下降', '减少', '差', '困难', '有', '偶尔', '经常', '有时', '很少',
    '不足', '低', '无法', '需要', '不愿意', '较少', '轻', '中度', '严重',
    '丧失', '依赖', '明显', '每周1-2次', '每月', '几乎不', '忘记', '问题',
    '低于', '小于', '<', '≤', '消瘦', '乏力', '食欲差', '减退'
  ]
  if (abnormalKeywords.some(k => text.includes(k))) return false

  // 5. 未匹配到默认正常
  return true
}

// ==================== 答题摘要 ====================
/**
 * 主量表答题摘要
 */
const mainAnswerSummary = computed(() => {
  const result = []
  for (let i = 0; i < (scaleData.value.questionList || []).length; i++) {
    const q = scaleData.value.questionList[i]
    const selectedIds = mainAnswers[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (!option) continue
    result.push({
      questionTitle: `${i + 1}. ${q.title}`,
      displayText: option.content,
      isNormal: isOptionNormal(option.content),
      index: i + 1
    })
  }
  return result
})

/**
 * 标准量表答题摘要
 */
const standardAnswerSummary = computed(() => {
  if (!showStandardScale.value) return []
  const result = []
  for (let i = 0; i < (standardScaleData.value.questionList || []).length; i++) {
    const q = standardScaleData.value.questionList[i]
    const selectedIds = standardAnswers[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (!option) continue
    result.push({
      questionTitle: `补充${i + 1}. ${q.title}`,
      displayText: option.content,
      isNormal: isOptionNormal(option.content),
      index: `补${i + 1}`
    })
  }
  return result
})

// ==================== 营养等级与结论 ====================
/**
 * 营养等级判定
 */
const nutritionLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8' }
  }

  // 仅主量表：使用标准MNA-SF阈值
  if (!showStandardScale.value) {
    const score = mainScore.value
    if (score >= 12) return { text: '营养状况良好', color: '#22c55e' }
    if (score >= 8) return { text: '存在营养不良风险', color: '#f97316' }
    return { text: '营养不良', color: '#ef4444' }
  }

  // 完整版：按得分比例判定
  const totalMax = maxMainScore + maxStandardScore.value
  const ratio = totalMax > 0 ? totalScore.value / totalMax : 0
  if (ratio >= NUTRITION_LEVEL_GOOD_RATIO) return { text: '营养状况良好', color: '#22c55e' }
  if (ratio >= NUTRITION_LEVEL_RISK_RATIO) return { text: '存在营养不良风险', color: '#f97316' }
  return { text: '营养不良', color: '#ef4444' }
})

/**
 * 整合结论文本
 */
const combinedResultText = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) return ''
  const parts = nutritionLevel.value.text
  return parts
})

/**
 * 正常项文本
 */
const normalItemsText = computed(() => {
  const all = [...mainAnswerSummary.value, ...standardAnswerSummary.value]
  return all.filter(a => a.isNormal).map(a => `${a.index}. ${a.displayText}`).join('；')
})

/**
 * 异常项文本
 */
const abnormalItemsText = computed(() => {
  const all = [...mainAnswerSummary.value, ...standardAnswerSummary.value]
  return all.filter(a => !a.isNormal).map(a => `${a.index}. ${a.displayText}`).join('；')
})

/**
 * 综合建议
 */
const combinedSuggest = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return '请完成全部题目以生成评估建议。'
  }
  const level = nutritionLevel.value.text
  if (level === '营养状况良好') {
    return '建议保持规律的三餐饮食，保证优质蛋白质（鸡蛋、牛奶、鱼、豆制品等）、维生素和膳食纤维的均衡摄入，适量户外活动，定期监测体重和食欲变化。'
  }
  if (level === '存在营养不良风险') {
    return '建议加强膳食指导：（1）增加优质蛋白摄入，每日保证鸡蛋、牛奶或酸奶；（2）每天至少三餐并适当加餐（上午、下午、睡前）；（3）鼓励户外活动和适量运动；（4）必要时到营养科就诊，补充口服营养制剂（ONS）。'
  }
  return '强烈建议：（1）尽快到营养科或老年科就诊，进行专业营养评估（如血清白蛋白、前白蛋白、血红蛋白等指标检测）；（2）制定个性化营养干预方案，包括饮食指导、口服营养补充或必要时肠内营养支持；（3）排查导致营养不良的基础疾病并进行针对性处理。'
})

// ==================== 提交与导航 ====================
/**
 * 返回上一页
 */
const goBack = () => {
  router.push({ path: '/patient/detection/customize', query: route.query })
}

/**
 * 返回综合评估页
 */
const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:microNutrition`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/customize',
    query: { patientId, patientName }
  })
}

/**
 * 提交评估
 */
const submitAssessment = async () => {
  if (submitting.value || !canSubmit.value) return
  if (!patientId) {
    ElMessage.error('患者信息异常，请重新进入')
    return
  }

  submitting.value = true
  const formattedAnswers = {}

  // 组装主量表答案
  for (const q of scaleData.value.questionList || []) {
    const selectedIds = mainAnswers[q.questionId] || []
    formattedAnswers[String(q.questionId)] = selectedIds.map(id => Number(id))
  }

  // 组装标准量表答案（未跳过且显示时）
  if (showStandardScale.value) {
    for (const q of standardScaleData.value.questionList || []) {
      const selectedIds = standardAnswers[q.questionId] || []
      formattedAnswers[String(q.questionId)] = selectedIds.map(id => Number(id))
    }
  }

  const dto = {
    patientId: Number(patientId),
    projectId: targetScaleId,
    scaleId: targetScaleId,
    answers: formattedAnswers,
    result: combinedResultText.value,
    suggest: combinedSuggest.value
  }

  try {
    const res = await insertCgaRecord(dto)
    if (res && (res.code === 200 || res.code === '200')) {
      ElMessage.success('评估提交成功')
      setTimeout(() => {
        backToComprehensive()
      }, 1500)
    } else {
      ElMessage.error(res.msg || '提交失败，请重试')
    }
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// ==================== 提交按钮状态 ====================
const canSubmit = computed(() => {
  if (!patientId) return false
  if (mainAnsweredCount.value < mainQuestionCount.value) return false
  if (!skipExtension.value && showStandardScale.value && standardAnsweredCount.value < standardQuestionCount.value) return false
  return mainQuestionCount.value > 0
})

const submitHintText = computed(() => {
  if (!patientId) return '患者信息异常，请重新进入'
  if (mainAnsweredCount.value < mainQuestionCount.value) {
    return `微营养量表还有 ${mainQuestionCount.value - mainAnsweredCount.value} 题未作答`
  }
  if (!skipExtension.value && showStandardScale.value && standardAnsweredCount.value < standardQuestionCount.value) {
    return `营养标准量表还有 ${standardQuestionCount.value - standardAnsweredCount.value} 题未作答`
  }
  return '请完成全部题目后再提交'
})
</script>

<style scoped lang="scss">
.nutrition-assessment-container {
  min-height: 100vh;
  padding: 24px 28px 48px;
  background: #faf5ff;
  box-sizing: border-box;
  max-width: 1100px;
  margin: 0 auto;
}

/* 顶部导航 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 14px;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: #64748b;
  padding: 6px 12px;
  border-radius: 8px;
  &:hover {
    color: #a855f7;
    background: #f3e8ff;
  }
}

.title-block h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  letter-spacing: 1px;
}
.title-block p {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #94a3b8;
}

.patient-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.badge-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #a855f7;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
}
.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.badge-id {
  font-size: 11.5px;
  color: #94a3b8;
  margin-top: 2px;
}

/* 进度条 */
.progress-bar {
  height: 46px;
  background: #fff;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #a855f7, #c084fc);
  border-radius: 10px;
  transition: width 0.4s ease;
}

.progress-label {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  z-index: 1;
}

/* 分区卡片 */
.section-card {
  background: #fff;
  border-radius: 14px;
  padding: 8px 20px 20px;
  margin-bottom: 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: #581c87;
  border-bottom: 1px solid #f3e8ff;
  margin-bottom: 14px;

  .el-icon {
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background: #e9d5ff;
    color: #a855f7;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
  }
}

.standard-title {
  color: #b45309;
  border-bottom-color: #fde68a;

  .el-icon {
    background: #fde68a;
    color: #d97706;
  }
}

.section-tag {
  margin-left: auto;
  font-size: 12px;
  font-weight: 500;
  color: #b45309;
  background: #fef9c3;
  padding: 3px 10px;
  border-radius: 6px;
}

/* 分数汇总 */
.score-summary {
  margin-top: 16px;
  padding: 12px 16px;
  background: #faf5ff;
  border-radius: 10px;
  font-size: 14px;
  color: #581c87;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.score-num {
  font-size: 22px;
  font-weight: 800;
  color: #7c3aed;
  font-family: Consolas, monospace;
}

.score-unit-sm {
  font-size: 13px;
  color: #a855f7;
}

/* 题目卡 */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: border-color 0.2s;
  &.is-answered {
    border-color: #d8b4fe;
  }
}

.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: #faf5ff;
  border-bottom: 1px solid #f3e8ff;
}

.q-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #e9d5ff;
  color: #6b21a8;
  font-size: 12px;
  font-weight: 800;
  font-family: Consolas, monospace;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.q-index-std {
  background: #fde68a;
  color: #92400e;
}

.question-card.is-answered .q-index {
  background: #a855f7;
  color: #fff;
}

.q-title {
  flex: 1;
  font-size: 14.5px;
  font-weight: 600;
  color: #1e293b;
}

.q-done {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #22c55e;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

/* 选项 */
.q-options {
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.q-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
  background: #fafbfc;

  &:hover {
    background: #f3e8ff;
    border-color: #e9d5ff;
  }
  &.is-selected {
    background: #f3e8ff;
    border-color: #a855f7;
    box-shadow: 0 2px 8px rgba(168, 85, 247, 0.12);
  }
}

.opt-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.opt-radio {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  color: #fff;
  font-size: 12px;
  flex-shrink: 0;
  transition: all 0.15s;
  &.is-checked {
    background: #a855f7;
    border-color: #a855f7;
  }
}

.opt-content {
  flex: 1;
}
.opt-label {
  font-size: 13.5px;
  color: #334155;
  line-height: 1.5;
}

.opt-right {
  display: flex;
  align-items: baseline;
  gap: 2px;
  flex-shrink: 0;
  margin-left: 16px;
}
.opt-score {
  font-size: 18px;
  font-weight: 700;
  color: #64748b;
  font-family: Consolas, monospace;
  line-height: 1;
}
.opt-unit {
  font-size: 11px;
  color: #94a3b8;
}

.is-selected .opt-score {
  color: #7c3aed;
}
.is-selected .opt-unit {
  color: #a855f7;
}

/* 结果区 */
.result-area {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 24px;
}

.result-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.rc-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: #faf5ff;
  border-bottom: 1px solid #f3e8ff;
  font-size: 15px;
  font-weight: 700;
  color: #581c87;
}

.rc-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #e9d5ff;
  color: #a855f7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
}

.score-display {
  padding: 24px 20px 8px;
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 6px;
}
.score-big {
  font-size: 56px;
  font-weight: 800;
  color: #7c3aed;
  line-height: 1;
  font-family: Consolas, monospace;
}
.score-unit {
  font-size: 18px;
  color: #a855f7;
  font-weight: 600;
}

.score-breakdown {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 8px 20px 16px;
  flex-wrap: wrap;
}

.breakdown-item {
  font-size: 13px;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 10px;
  border-radius: 6px;
}

.breakdown-sep {
  font-size: 14px;
  color: #94a3b8;
  font-weight: 700;
}

/* 整合结论 */
.combined-result {
  padding: 12px 20px 20px;
}

.cr-block {
  padding: 12px 14px;
  margin-bottom: 10px;
  border-radius: 10px;
  background: #faf5ff;
  border-left: 3px solid #a855f7;

  &:last-child {
    margin-bottom: 0;
  }
}

.cr-title {
  font-size: 13.5px;
  font-weight: 700;
  color: #581c87;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.cr-normal {
  color: #22c55e;
  font-size: 10px;
}
.cr-abnormal {
  color: #ef4444;
  font-size: 10px;
}

.cr-text {
  font-size: 13px;
  color: #475569;
  line-height: 1.7;
}

.no-finding {
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.hint-text {
  font-size: 13px;
  color: #94a3b8;
}

.suggest-card .suggest-text {
  padding: 16px 20px;
  margin: 0;
  font-size: 14px;
  color: #334155;
  line-height: 1.7;
}

/* 提交按钮 */
.submit-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
}

.submit-area .el-button {
  background: linear-gradient(90deg, #a855f7, #c084fc);
  border: none;
  min-width: 200px;
  padding: 20px 40px;
  font-size: 16px;
  font-weight: 600;
}

.submit-hint {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}
</style>
