<template>
  <div class="delirium-assessment-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>谵妄评估</h1>
          <p>CAM 意识模糊评估量表 · 共 {{ totalQuestions }} 项</p>
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
          :class="{ 'is-answered': answers[question.questionId] !== undefined }"
      >
        <div class="q-head">
          <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
          <div class="q-title-wrap">
            <span class="q-title">{{ question.title }}</span>
            <span v-if="question.remark" class="q-remark-inline">{{ question.remark }}</span>
          </div>
          <span v-if="answers[question.questionId]" class="q-done">
            <el-icon><Check /></el-icon>
          </span>
        </div>
        <div class="q-options">
          <div
              v-for="option in question.optionList"
              :key="option.id"
              class="q-option"
              :class="{
              'is-selected': answers[question.questionId]?.[0] === option.id,
              'is-max': questionMaxScores[question.questionId] === Number(option.score || 0)
            }"
              @click="selectOption(question.questionId, option)"
          >
            <div class="opt-left">
              <div class="opt-radio" :class="{ 'is-checked': answers[question.questionId]?.[0] === option.id }">
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
    </section>

    <!-- 结果与评判标准 -->
    <section class="result-area">
      <div class="result-card note-card" :style="{ backgroundColor: resultLevel.bg }">
        <div class="note-block">
          <div class="note-title">评估结论</div>
          <div class="note-text result-text" :style="{ color: resultLevel.color }">
            {{ resultLevel.text }}（{{ totalScore }} 分）
          </div>
        </div>
        <div class="note-block" v-if="answeredCount >= totalQuestions && totalQuestions > 0">
          <div class="note-title">判定依据（CAM）</div>
          <div class="note-text">第 1、2 题均为"是"，且第 3、4 题中至少有一项为"是"，方可诊断谵妄。</div>
        </div>
        <div class="note-block" v-if="normalItemsText">
          <div class="note-title"><span class="dot-normal">●</span> 正常项</div>
          <div class="note-text">{{ normalItemsText }}</div>
        </div>
        <div class="note-block" v-if="abnormalItemsText">
          <div class="note-title"><span class="dot-abnormal">●</span> 风险 / 异常项</div>
          <div class="note-text">{{ abnormalItemsText }}</div>
        </div>
        <div class="note-block note-placeholder" v-if="answeredCount < totalQuestions">
          <div class="hint-text">完成全部题目后显示评估详情</div>
        </div>
      </div>

      <div class="result-card suggest-card">
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
import { selectDeliriumScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
// 默认量表ID
const DEFAULT_SCALE_ID = 29
// CAM诊断核心题目索引（对应数组下标）
const CAM_QUESTION = {
  Q1: 0, // 意识状态急性改变
  Q2: 1, // 注意力障碍
  Q3: 2, // 思维紊乱
  Q4: 3  // 意识水平改变
}
// 题目对应症状标签
const SYMPTOM_LABELS = ['意识状态急性改变', '注意力障碍', '思维紊乱', '意识水平改变']

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
// 答案数据
const answers = reactive({})
// 每题最高分缓存
const questionMaxScores = reactive({})

// ==================== 工具函数 ====================
const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || DEFAULT_SCALE_ID

// ==================== 数据加载 ====================
const loadScaleData = async () => {
  try {
    loading.value = true
    const res = await selectDeliriumScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
      // 预处理每题最高分，避免渲染时重复计算
      scaleData.value.questionList.forEach(q => {
        const maxScore = Math.max(...q.optionList.map(opt => Number(opt.score || 0)))
        questionMaxScores[q.questionId] = maxScore
      })
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 重置答案
const resetAnswers = () => {
  Object.keys(answers).forEach(key => delete answers[key])
  Object.keys(questionMaxScores).forEach(key => delete questionMaxScores[key])
}

// 监听路由参数变化，切换患者/量表时重置数据
watch(
    () => [route.query.patientId, route.query.scaleId],
    () => {
      resetAnswers()
      loadScaleData()
    },
    { immediate: true }
)

// ==================== 选项选择 ====================
const selectOption = (questionId, option) => {
  answers[questionId] = [option.id]
}

// ==================== 进度计算 ====================
const totalQuestions = computed(() => scaleData.value.questionList?.length || 0)
const answeredCount = computed(() => Object.keys(answers).length)
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ==================== 分数计算 ====================
const totalScore = computed(() => {
  let sum = 0
  const questionList = scaleData.value.questionList || []
  for (const q of questionList) {
    const selectedIds = answers[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList.find(opt => Number(opt.id) === Number(selectedIds[0]))
    if (option) sum += Number(option.score || 0)
  }
  return sum
})

// ==================== 判定逻辑 ====================
/**
 * 判断某题是否为阳性（选中了得分>0的选项）
 * @param {number} qIndex 题目索引
 */
const isAnswerPositive = (qIndex) => {
  const question = scaleData.value.questionList?.[qIndex]
  if (!question) return false
  const selectedIds = answers[question.questionId] || []
  if (selectedIds.length === 0) return false
  const option = question.optionList.find(opt => opt.id === selectedIds[0])
  if (!option) return false
  // 优先以分数判断：得分>0即为阳性
  const score = Number(option.score || 0)
  if (score > 0) return true
  if (score < 0) return false
  // 分数为0时兜底文本判断
  const text = String(option.content).trim()
  return text.includes('是') && !text.includes('无') && !text.includes('没有')
}

/**
 * 判断选项是否为正常项
 */
const isOptionNormal = (option) => {
  const score = Number(option.score || 0)
  // 分数<=0为正常
  if (score <= 0) return true
  if (score > 0) return false
  // 无分数时兜底文本判断
  const text = String(option.content || '').toLowerCase()
  const normalKeywords = ['无', '没有', '正常', '良好', '否']
  return normalKeywords.some(k => text.includes(k))
}

// 评估结果等级
const resultLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8', bg: '#f8fafc' }
  }

  const q1Positive = isAnswerPositive(CAM_QUESTION.Q1)
  const q2Positive = isAnswerPositive(CAM_QUESTION.Q2)
  const q3Positive = isAnswerPositive(CAM_QUESTION.Q3)
  const q4Positive = isAnswerPositive(CAM_QUESTION.Q4)

  // CAM标准：1+2阳性，且3/4至少一个阳性 → 明确谵妄
  if (q1Positive && q2Positive && (q3Positive || q4Positive)) {
    return { text: '明确谵妄', color: '#ef4444', bg: '#fef2f2' }
  }
  // 1+2阳性，但3/4均阴性 → 可疑谵妄
  if (q1Positive && q2Positive) {
    return { text: '可疑谵妄', color: '#f97316', bg: '#fff7ed' }
  }

  // 存在部分阳性项，列出具体症状
  const positiveItems = []
  if (q1Positive) positiveItems.push(SYMPTOM_LABELS[0])
  if (q2Positive) positiveItems.push(SYMPTOM_LABELS[1])
  if (q3Positive) positiveItems.push(SYMPTOM_LABELS[2])
  if (q4Positive) positiveItems.push(SYMPTOM_LABELS[3])

  if (positiveItems.length > 0) {
    return { text: '存在' + positiveItems.join('、'), color: '#f59e0b', bg: '#fffbeb' }
  }

  // 全部阴性 → 无谵妄
  return { text: '无谵妄', color: '#22c55e', bg: '#f0fdf4' }
})

// 正常项文本
const normalItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const items = []
  scaleData.value.questionList.forEach((q, index) => {
    const selectedIds = answers[q.questionId] || []
    if (selectedIds.length === 0) return
    const option = q.optionList.find(opt => opt.id === selectedIds[0])
    if (!option) return
    if (isOptionNormal(option)) items.push(`${index + 1}. ${option.content}`)
  })
  return items.length ? items.join('；') : '无'
})

// 异常项文本
const abnormalItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const items = []
  scaleData.value.questionList.forEach((q, index) => {
    const selectedIds = answers[q.questionId] || []
    if (selectedIds.length === 0) return
    const option = q.optionList.find(opt => opt.id === selectedIds[0])
    if (!option) return
    if (!isOptionNormal(option)) items.push(`${index + 1}. ${option.content}`)
  })
  return items.join('；')
})

// 评估建议
const resultSuggest = computed(() => {
  const levelText = resultLevel.value.text
  if (levelText === '待评估') return '请完成全部题目以生成评估建议。'
  if (levelText === '无谵妄') return '当前未发现谵妄相关症状，意识状态良好。建议继续观察，保持规律作息和稳定环境，避免使用可能影响意识状态的药物，定期随访。'
  if (levelText === '可疑谵妄') return '存在谵妄可疑迹象，建议进一步完善认知功能评估，排查诱发因素（如感染、电解质紊乱、药物副作用、酒精或药物戒断等），密切观察意识状态变化，必要时请神经内科会诊。'
  if (levelText === '明确谵妄') return '存在明确谵妄表现，建议尽快联系神经内科或老年科进一步评估，完善基础检查（血常规、电解质、肝肾功能、甲状腺功能、影像学等），排查并处理可逆性病因，给予环境干预与对症支持治疗，做好安全防护防止意外。'
  // 部分阳性症状
  return `评估提示${levelText}，尚不满足谵妄完整诊断标准。建议结合临床实际情况，关注相关症状发展变化，排查可能的诱发因素，必要时进一步评估明确原因。`
})

// ==================== 导航与提交 ====================
const goBack = () => {
  router.push({ path: '/patient/detection/customize', query: route.query })
}

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:delirium`, '1')
    }
  } catch (e) {
    console.warn('本地存储写入失败', e)
  }
  router.push({
    path: '/patient/detection/customize',
    query: { patientId, patientName }
  })
}

// 提交按钮可用性
const canSubmit = computed(() => {
  return totalQuestions.value > 0
      && answeredCount.value >= totalQuestions.value
      && patientId
      && !submitting.value
})

// 提交评估
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
    // 格式化答案
    const formattedAnswers = {}
    Object.keys(answers).forEach(questionId => {
      formattedAnswers[questionId] = answers[questionId].map(id => Number(id))
    })

    const dto = {
      patientId: Number(patientId),
      projectId: targetScaleId,
      scaleId: targetScaleId,
      answers: formattedAnswers,
      result: resultLevel.value.text,
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
.delirium-assessment-container {
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
    color: #3b82f6;
    background: #eff6ff;
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
  background: linear-gradient(90deg, #a855f7, #7c3aed);
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
    border-color: #c4b5fd;
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

.q-remark-inline {
  font-size: 12.5px;
  color: #64748b;
  font-weight: 400;
  line-height: 1.6;
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
  background: #a855f7;
  color: #fff;
}

.q-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
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
    background: #f5f3ff;
    border-color: #c4b5fd;
  }
  &.is-selected {
    background: #f5f3ff;
    border-color: #a855f7;
    box-shadow: 0 2px 8px rgba(168,85,247,0.12);
  }
  &.is-max .opt-score {
    color: #16a34a;
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
    color: #a855f7;
  }
}

.opt-label {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}
.is-selected .opt-label {
  color: #5b21b6;
  font-weight: 600;
}

.opt-right {
  display: flex;
  align-items: baseline;
  gap: 2px;
  flex-shrink: 0;
  margin-left: 16px;
}

.opt-score {
  font-size: 22px;
  font-weight: 800;
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

.result-area {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-bottom: 28px;
}

.result-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  border: 1px solid #e5e7eb;
}

.rc-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 14px;
}
.rc-icon {
  font-size: 18px;
}

.suggest-card {
  border-left: 4px solid #a855f7;
}
.suggest-text {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.9;
}

.note-block {
  padding: 8px 12px;
  margin-bottom: 6px;
  background: rgba(255,255,255,0.6);
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
.result-text {
  font-size: 16px;
  font-weight: 700;
}
.dot-normal {
  color: #22c55e;
  font-size: 10px;
}
.dot-abnormal {
  color: #ef4444;
  font-size: 10px;
}
.note-text {
  font-size: 13.5px;
  color: #1f2937;
  line-height: 1.7;
}
.note-placeholder {
  text-align: center;
  padding: 16px 12px;
  background: transparent;
}
.hint-text {
  color: #94a3b8;
  font-size: 13px;
}

.submit-area {
  text-align: center;
  padding: 8px 0;
}

.submit-area .el-button--primary {
  background: linear-gradient(135deg, #a855f7, #7c3aed);
  border: none;
  padding: 14px 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 14px rgba(124,58,237,0.3);
  &:hover {
    box-shadow: 0 6px 20px rgba(124,58,237,0.4);
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
  .delirium-assessment-container {
    padding: 14px;
  }
  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
