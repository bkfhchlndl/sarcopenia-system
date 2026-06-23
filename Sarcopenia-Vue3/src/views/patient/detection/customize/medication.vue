<template>
  <div class="medication-assessment-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>多重用药评估</h1>
          <p>用药清单核查 · 共 {{ totalQuestions }} 项</p>
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
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">已完成 {{ answeredCount }} / {{ totalQuestions }} 项</span>
    </div>

    <!-- 评估题卡列表 -->
    <section class="question-list" v-loading="loading">
      <div
          v-for="(question, qIndex) in scaleData.questionList"
          :key="question.questionId"
          class="question-card"
          :class="{ 'is-answered': isQuestionAnswered(question) }"
      >
        <div class="q-head">
          <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
          <div class="q-title-wrap">
            <span class="q-title">{{ question.title }}</span>
            <span v-if="question.remark" class="q-remark-inline">{{ question.remark }}</span>
          </div>
          <span v-if="isQuestionAnswered(question)" class="q-done">
            <el-icon><Check /></el-icon>
          </span>
        </div>

        <!-- 文本题（选填） -->
        <div v-if="isTextQuestion(question)" class="q-textarea-wrap">
          <el-input
              v-model="answers[question.questionId]"
              type="textarea"
              :autosize="{ minRows: 4, maxRows: 10 }"
              placeholder="请输入描述（选填）"
              maxlength="500"
              show-word-limit
              resize="vertical"
          />
          <div class="q-textarea-tip">
            <span v-if="answers[question.questionId]" class="tip-ok">已填写</span>
            <span v-else class="tip-empty">选填 · 可留空</span>
          </div>
        </div>

        <!-- 选择题 -->
        <div v-else class="q-options">
          <div
              v-for="option in question.optionList"
              :key="option.id"
              class="q-option"
              :class="{ 'is-selected': answers[question.questionId] === option.id }"
              @click="selectOption(question.questionId, option.id)"
          >
            <div class="opt-left">
              <div class="opt-radio" :class="{ 'is-checked': answers[question.questionId] === option.id }">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="opt-content">
                <span class="opt-label">{{ option.content }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 结果与建议 -->
    <section class="result-area" v-if="answeredCount === totalQuestions">
      <div class="result-card conclusion-card full-width">
        <div class="rc-head">
          <span class="rc-icon">📋</span>
          <span>评估结论</span>
        </div>
        <div class="conclusion-text">{{ conclusionText }}</div>
      </div>

      <div class="result-card note-card full-width">
        <div class="note-block" v-if="riskItemsText">
          <div class="note-title"><span class="dot-abnormal">●</span> 风险因素</div>
          <div class="note-text">{{ riskItemsText }}</div>
        </div>
        <div class="note-block" v-if="textItemsText">
          <div class="note-title"><span class="dot-text">●</span> 附加描述</div>
          <div class="note-text">{{ textItemsText }}</div>
        </div>
        <div class="note-block" v-if="normalItemsText">
          <div class="note-title"><span class="dot-normal">●</span> 正常项</div>
          <div class="note-text">{{ normalItemsText }}</div>
        </div>
      </div>

      <div class="result-card suggest-card full-width">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ resultSuggest }}</p>
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
        还有 {{ totalQuestions - answeredCount }} 道题未作答，请完成后再提交
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectMedicationScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
const DEFAULT_SCALE_ID = 42
const QUESTION_TYPE_TEXT = 3
// 风险分级阈值（按风险项数量）
const RISK_LEVEL = {
  LOW: 0,
  MILD: 2,
  MODERATE: 4
}

// ==================== 状态变量 ====================
const loading = ref(false)
const submitting = ref(false)
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'

// 量表数据
const scaleData = ref({
  scaleId: null,
  scaleName: '',
  code: '',
  questionList: []
})
// 答案数据（reactive保证对象属性修改响应式）
const answers = reactive({})

// ==================== 工具函数 ====================
const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || DEFAULT_SCALE_ID

/**
 * 获取某题已选选项对象
 */
const getSelectedOption = (qIndex) => {
  const q = scaleData.value.questionList?.[qIndex]
  if (!q) return null
  if (isTextQuestion(q)) {
    const text = answers[q.questionId]
    return text ? { content: String(text).trim() } : null
  }
  const selectedId = answers[q.questionId]
  if (selectedId === undefined || selectedId === null) return null
  return q.optionList?.find(o => Number(o.id) === Number(selectedId)) || null
}

/**
 * 判断选项是否为正常/无风险
 * 优先按分值判断：score <= 0 为正常；无分值则兜底文本匹配
 */
const isOptionNormal = (option) => {
  if (!option) return false
  if (option.score !== undefined && option.score !== null) {
    return Number(option.score) <= 0
  }
  // 无分值时兜底文本判断
  const text = String(option.content || '').trim()
  const normalKeywords = ['无', '没有', '否', '正常', '不', '未', '独立']
  const hitNormal = normalKeywords.some(k => text.includes(k))
  const hitRisk = text.includes('是') && !text.includes('无')
  return hitNormal && !hitRisk
}

// ==================== 题目类型判断 ====================
function isTextQuestion(question) {
  // type === 3 表示文本题，与其他量表组件统一规范
  if (question.type === QUESTION_TYPE_TEXT) return true
  if (!question.optionList || question.optionList.length === 0) return true
  const hasValidOption = question.optionList.some(opt => opt && opt.content && String(opt.content).trim())
  return !hasValidOption
}

// 文本题为选填，不计入必填完成统计
function isQuestionAnswered(question) {
  if (isTextQuestion(question)) {
    const v = answers[question.questionId]
    return v !== undefined && v !== null && String(v).trim().length > 0
  }
  return answers[question.questionId] !== undefined && answers[question.questionId] !== null
}

// ==================== 数据加载 ====================
const loadScaleData = async () => {
  try {
    loading.value = true
    const res = await selectMedicationScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

/**
 * 重置答案数据
 */
const resetAnswers = () => {
  Object.keys(answers).forEach(key => delete answers[key])
}

// 监听路由参数变化，切换患者/量表时自动重置并重载
watch(
    () => [route.query.patientId, route.query.scaleId],
    () => {
      resetAnswers()
      loadScaleData()
    },
    { immediate: true }
)

// ==================== 选项选择 ====================
const selectOption = (questionId, optionId) => {
  answers[questionId] = optionId
}

// ==================== 进度计算 ====================
const totalQuestions = computed(() => {
  // 仅统计选择题数量（文本题为选填，不计入总数）
  const list = scaleData.value.questionList || []
  return list.filter(q => !isTextQuestion(q)).length
})
const answeredCount = computed(() => {
  let count = 0
  for (const q of (scaleData.value.questionList || [])) {
    if (isTextQuestion(q)) continue
    if (answers[q.questionId] !== undefined && answers[q.questionId] !== null) count++
  }
  return count
})
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ==================== 风险项 / 正常项 / 附加描述 ====================
const riskItems = computed(() => {
  if (answeredCount.value < totalQuestions.value) return []
  const list = []
  scaleData.value.questionList.forEach((q, index) => {
    if (isTextQuestion(q)) return
    const option = getSelectedOption(index)
    if (!option) return
    if (!isOptionNormal(option)) {
      list.push(`${index + 1}. ${q.title}：${option.content}`)
    }
  })
  return list
})
const riskItemsText = computed(() => riskItems.value.join('；'))

const normalItems = computed(() => {
  if (answeredCount.value < totalQuestions.value) return []
  const list = []
  scaleData.value.questionList.forEach((q, index) => {
    if (isTextQuestion(q)) return
    const option = getSelectedOption(index)
    if (!option) return
    if (isOptionNormal(option)) {
      list.push(`${index + 1}. ${q.title}：${option.content}`)
    }
  })
  return list
})
const normalItemsText = computed(() => normalItems.value.length ? normalItems.value.join('；') : '无')

const textItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const list = []
  scaleData.value.questionList.forEach((q, index) => {
    if (!isTextQuestion(q)) return
    const option = getSelectedOption(index)
    if (option && option.content) {
      list.push(`${index + 1}. ${q.title}：${option.content}`)
    }
  })
  return list.join('；')
})

// ==================== 评估结论 ====================
const conclusionText = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) return '待评估'
  const riskCount = riskItems.value.length
  if (riskCount <= RISK_LEVEL.LOW) {
    return '本次多重用药评估未发现明显风险因素，用药情况相对平稳。'
  }
  if (riskCount <= RISK_LEVEL.MILD) {
    return `本次多重用药评估共发现 ${riskCount} 项用药相关问题，存在轻度多重用药风险。`
  }
  if (riskCount <= RISK_LEVEL.MODERATE) {
    return `本次多重用药评估共发现 ${riskCount} 项用药相关问题，存在中度多重用药风险。`
  }
  return `本次多重用药评估共发现 ${riskCount} 项用药相关问题，存在高度多重用药风险，建议药物重整。`
})

// ==================== 评估建议 ====================
const resultSuggest = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return '请完成全部题目以生成评估建议。'
  }
  const riskCount = riskItems.value.length
  if (riskCount <= RISK_LEVEL.LOW) {
    return '多重用药评估提示用药情况平稳，建议继续按医嘱规律服药，定期复诊评估用药方案，避免自行增减药物。'
  }
  if (riskCount <= RISK_LEVEL.MILD) {
    return '多重用药评估提示存在轻度多重用药风险，建议携带完整用药清单至药师或专科医生处评估，关注药物相互作用与不良反应，定期复查。'
  }
  if (riskCount <= RISK_LEVEL.MODERATE) {
    return '多重用药评估提示存在中度多重用药风险，建议尽快至临床药师或专科医生处进行用药重整，评估重复用药、药物相互作用及潜在不良反应，优化用药方案。'
  }
  return '多重用药评估提示高度多重用药风险，建议立即进行药物综合评估与重整：完善用药清单（包括处方药物、非处方药物、保健品与中药），排查重复用药、药物相互作用、禁忌症与不良反应风险，由药师或专科医生制定优化方案，定期随访评估。'
})

// ==================== 导航 ====================
const goBack = () => {
  router.push({ path: '/patient/detection/customize', query: route.query })
}

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:medication`, '1')
    }
  } catch (e) {
    console.warn('本地存储写入失败', e)
  }
  router.push({
    path: '/patient/detection/customize',
    query: { patientId, patientName }
  })
}

// ==================== 提交 ====================
const canSubmit = computed(() => {
  return totalQuestions.value > 0
      && answeredCount.value >= totalQuestions.value
      && patientId
      && !submitting.value
})

const submitAssessment = async () => {
  if (!canSubmit.value) {
    const remain = totalQuestions.value - answeredCount.value
    if (remain > 0) {
      ElMessage.warning(`还有 ${remain} 道题未作答，请完成后再提交`)
    }
    if (!patientId) {
      ElMessage.error('患者信息异常，请重新进入评估页面')
    }
    return
  }

  submitting.value = true
  try {
    // 格式化答案：仅选择题提交，文本题跳过（避免后端Long类型解析报错）
    const formattedAnswers = {}
    for (const q of (scaleData.value.questionList || [])) {
      const qid = String(q.questionId)
      const raw = answers[q.questionId]
      if (isTextQuestion(q)) continue
      if (raw !== undefined && raw !== null) {
        formattedAnswers[qid] = [Number(raw)]
      }
    }

    const dto = {
      patientId: Number(patientId),
      projectId: targetScaleId,
      scaleId: targetScaleId,
      answers: formattedAnswers,
      result: conclusionText.value,
      suggest: resultSuggest.value
    }

    const res = await insertCgaRecord(dto)
    if (res && (res.code === 200 || res.code === '200')) {
      ElMessage.success('评估提交成功')
      setTimeout(() => {
        backToComprehensive()
      }, 1500)
    } else {
      ElMessage.error(res.msg || '提交失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error('提交失败，请检查网络后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.medication-assessment-container {
  min-height: 100vh;
  padding: 24px 28px 48px;
  background: #f4f6fb;
  box-sizing: border-box;
  max-width: 1100px;
  margin: 0 auto;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
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
    color: #14b8a6;
    background: #ccfbf1;
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
  background: #14b8a6;
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

.progress-bar {
  height: 46px;
  background: #fff;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #14b8a6, #0d9488);
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

.question-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 24px;
}

.question-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: border-color 0.2s;

  &.is-answered {
    border-color: #5eead4;
  }
}

.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.q-title-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.q-index {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #e2e8f0;
  color: #475569;
  font-size: 12.5px;
  font-weight: 800;
  font-family: Consolas, monospace;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.question-card.is-answered .q-index {
  background: #14b8a6;
  color: #fff;
}

.q-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.q-remark-inline {
  font-size: 12.5px;
  color: #64748b;
  font-weight: 400;
  line-height: 1.6;
}

.q-done {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #22c55e;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
}

.q-options {
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.q-textarea-wrap {
  padding: 16px 20px;
  background: #f8fafc;
}

.q-textarea-wrap .el-textarea__inner {
  min-height: 110px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.7;
  color: #1f2937;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-family: inherit;
  transition: border-color .15s, box-shadow .15s;
  box-sizing: border-box;
  resize: vertical;
}

.q-textarea-wrap .el-textarea__inner::placeholder {
  color: #94a3b8;
}

.q-textarea-wrap .el-textarea__inner:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.15);
}

.q-textarea-tip {
  margin-top: 10px;
  font-size: 12.5px;
  display: flex;
  align-items: center;
  gap: 6px;

  .tip-ok {
    color: #0f766e;
    font-weight: 600;
  }
  .tip-empty {
    color: #94a3b8;
  }
}

.q-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
  background: #fafbfc;

  &:hover {
    background: #ccfbf1;
    border-color: #5eead4;
  }
  &.is-selected {
    background: #ccfbf1;
    border-color: #14b8a6;
    box-shadow: 0 2px 8px rgba(20,184,166,0.14);
  }
}

.opt-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.opt-radio {
  font-size: 20px;
  color: #cbd5e1;
  flex-shrink: 0;
  transition: color 0.15s;
  &.is-checked {
    color: #14b8a6;
  }
}

.opt-label {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}
.is-selected .opt-label {
  color: #115e59;
  font-weight: 600;
}

.result-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 28px;
}

.result-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  border: 1px solid #e5e7eb;
}

.full-width {
  grid-column: 1 / -1;
}

.rc-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 14px;
}
.rc-icon {
  font-size: 20px;
}

.conclusion-card {
  border-left: 4px solid #14b8a6;
}

.conclusion-text {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.9;
  padding: 4px 8px;
}

.note-block {
  padding: 8px 12px;
  margin-bottom: 6px;
  background: #f8fafc;
  border-radius: 8px;
  &:last-child {
    margin-bottom: 0;
  }
}
.note-title {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot-normal {
  color: #22c55e;
  font-size: 10px;
}
.dot-abnormal {
  color: #ef4444;
  font-size: 10px;
}
.dot-text {
  color: #14b8a6;
  font-size: 10px;
}
.note-text {
  font-size: 13.5px;
  color: #1f2937;
  line-height: 1.7;
}

.suggest-card {
  border-left: 4px solid #14b8a6;
}
.suggest-text {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.9;
}

.submit-area {
  text-align: center;
  padding: 8px 0;
}

.submit-area .el-button--primary {
  background: linear-gradient(135deg, #14b8a6, #0d9488);
  border: none;
  padding: 14px 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 14px rgba(13,148,136,0.3);
  &:hover {
    box-shadow: 0 6px 20px rgba(13,148,136,0.4);
    transform: translateY(-1px);
  }
  &:disabled {
    background: #cbd5e1;
    box-shadow: none;
    cursor: not-allowed;
  }
}

.submit-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: #94a3b8;
}

@media (max-width: 800px) {
  .medication-assessment-container {
    padding: 14px;
  }
  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .result-area {
    grid-template-columns: 1fr;
  }
}
</style>

