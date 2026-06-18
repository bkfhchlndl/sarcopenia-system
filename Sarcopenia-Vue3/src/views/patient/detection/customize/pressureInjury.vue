<template>
  <div class="daily-life-container">
    <!-- ===== 1. 顶部导航栏 ===== -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>压力性损伤评估</h1>
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

    <!-- ===== 2. 进度条 ===== -->
    <section class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">已完成 {{ answeredCount }} / {{ totalQuestions }} 项</span>
    </section>

    <!-- ===== 3. 题目卡 ===== -->
    <section class="question-list">
      <div
          v-for="(question, qIndex) in scaleData?.questionList || []"
          :key="question.questionId"
          class="question-card"
          :class="{ 'is-answered': isQuestionAnswered(question) }"
      >
        <div class="q-head">
          <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
          <span class="q-title">{{ question.title }}</span>
          <span v-if="isQuestionAnswered(question)" class="q-done">
            <el-icon><Check /></el-icon>
          </span>
        </div>

        <!-- 选择题 -->
        <div v-if="question.optionList && question.optionList.length > 0" class="q-options">
          <div
              v-for="option in question.optionList"
              :key="option.id"
              class="q-option"
              :class="{ 'is-selected': answers[question.questionId]?.[0] === option.id }"
              @click="selectOption(question, option)"
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

    <!-- ===== 4. 结果展示区 ===== -->
    <section class="result-area">
      <!-- 评估详情 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠</span>
          <span>评估结果 · {{ riskLevel.text }}</span>
        </div>
        <div class="score-display">
          <span class="score-big">{{ totalScore }}</span>
          <span class="score-unit">分</span>
        </div>

        <div v-if="answerSummary.length > 0" class="abnormal-section">
          <div class="abnormal-title">评估详情</div>
          <div v-for="(item, idx) in answerSummary" :key="idx" class="abnormal-row"
               :class="{ 'is-abnormal': item.isAbnormal, 'is-normal': !item.isAbnormal }"
          >
            <div class="abnormal-content">
              <div class="abnormal-q">{{ item.questionTitle }}</div>
              <div class="abnormal-a">{{ item.displayText }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="totalQuestions > 0" class="no-finding">
          <div class="hint-text">完成全部题目后展示评估详情</div>
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

    <!-- ===== 5. 提交按钮 ===== -->
    <div class="submit-area">
      <el-button
          type="primary"
          size="large"
          round
          :loading="submitting"
          :disabled="answeredCount < totalQuestions || !scaleData?.questionList?.length"
          @click="submitAssessment"
      >
        <el-icon><Check /></el-icon>
        提交评估
      </el-button>
      <p v-if="answeredCount < totalQuestions" class="submit-hint">
        请完成全部 {{ totalQuestions }} 项评估后再提交
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectPressureInjuryScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'
const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || 34

const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })

watch(() => route.fullPath, () => {
  if (!scaleData.value) {
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}, { immediate: true })

const loadScaleData = async () => {
  try {
    const res = await selectPressureInjuryScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败')
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}

onMounted(() => { loadScaleData() })

// 选择题答案
const answers = ref({})

function selectOption(question, option) {
  answers.value[question.questionId] = [option.id]
}

function isQuestionAnswered(question) {
  const selected = answers.value[question.questionId] || []
  return selected.length > 0
}

// 判断选项是否表示正常/无风险（得分低表示状况好）
function isOptionNormal(option) {
  const text = String(option.content || '').toLowerCase()
  const label = String(option.label || '').toLowerCase()
  const combined = `${label} ${text}`
  const normalKeywords = ['正常', '无', '完全', '好', '正常营养', '没有', '健康', '良好', '活动自如', '完全能', '完全清醒', '可感知']
  const match = normalKeywords.some(k => combined.includes(k))
  // 如果有关键词匹配，或者分数较低（<=1），视为正常
  const score = Number(option.score || 0)
  return match || score <= 1
}

const totalQuestions = computed(() => scaleData.value.questionList?.length || 0)

const answeredCount = computed(() => {
  let count = 0
  for (const q of scaleData.value.questionList || []) {
    if (isQuestionAnswered(q)) count++
  }
  return count
})

const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// 总分
const totalScore = computed(() => {
  let sum = 0
  for (const q of scaleData.value.questionList || []) {
    const selectedIds = answers.value[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (option) sum += Number(option.score || 0)
  }
  return sum
})

// 答题摘要
const answerSummary = computed(() => {
  const result = []
  for (const q of scaleData.value.questionList || []) {
    const selectedIds = answers.value[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (!option) continue
    // 分数越高表示风险越大（压力性损伤量表通常分数越低越好/越高越好取决于量表设计）
    // 这里假设分数越高越异常
    const score = Number(option.score || 0)
    const maxScoreInQuestion = Math.max(...q.optionList.map(o => Number(o.score || 0)))
    const isAbnormal = score >= maxScoreInQuestion * 0.5 && score > 1
    result.push({
      questionTitle: q.title,
      displayText: option.content,
      isAbnormal,
      score
    })
  }
  return result
})

// 风险等级（基于总分，常见的Braden量表 19-23正常 15-18轻度 13-14中度 10-12高度 ≤9非常高）
// 这里做一个通用判断：总分越高风险越低（Braden型），或者反过来——根据已作答的异常项数量判断
const riskLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8' }
  }
  const abnormal = answerSummary.value.filter(a => a.isAbnormal).length
  const total = answerSummary.value.length
  if (abnormal === 0) return { text: '无压力性损伤风险', color: '#22c55e' }
  if (abnormal <= Math.ceil(total * 0.3)) return { text: '轻度风险', color: '#eab308' }
  if (abnormal <= Math.ceil(total * 0.6)) return { text: '中度风险', color: '#f97316' }
  return { text: '高度风险', color: '#ef4444' }
})

// 综合建议
const combinedSuggest = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return '请完成全部题目以生成评估建议。'
  }
  const level = riskLevel.value.text
  if (level === '无压力性损伤风险') {
    return '目前压力性损伤相关评估未见明显异常。建议保持良好的活动习惯，注意皮肤护理和营养状况，定期翻身和活动，保持床垫干燥清洁。'
  }
  if (level === '轻度风险') {
    return '存在轻度压力性损伤风险。建议加强皮肤观察，每2小时翻身一次，使用减压床垫，注意营养摄入和水分补充，保持皮肤清洁干燥。'
  }
  if (level === '中度风险') {
    return '存在中度压力性损伤风险。建议立即采取干预措施：使用减压设备（气垫床、减压垫），增加翻身频率（每1-2小时），加强营养支持，进行皮肤评估和专业护理。'
  }
  // 高度风险
  return '存在高度压力性损伤风险，强烈建议立即进行专业皮肤评估，使用高级减压设备（交替式气垫床），增加翻身频率，加强营养和水分管理，必要时到专科就诊制定详细护理方案。'
})

// 提交到数据库的 result 文本（精简版）
const resultText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  return `${riskLevel.value.text}。${combinedSuggest.value.split('。')[0]}。`
})

const goBack = () => { router.back() }

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:pressureInjury`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/comprehensive',
    query: { patientId, patientName }
  })
}

const submitAssessment = async () => {
  if (submitting.value || answeredCount.value < totalQuestions.value) return
  submitting.value = true

  const formattedAnswers = {}
  for (const q of scaleData.value.questionList || []) {
    const selectedIds = answers.value[q.questionId] || []
    formattedAnswers[String(q.questionId)] = selectedIds.map(id => Number(id))
  }

  const dto = {
    patientId: Number(patientId),
    projectId: targetScaleId,
    scaleId: targetScaleId,
    answers: formattedAnswers,
    result: resultText.value,
    suggest: combinedSuggest.value
  }

  try {
    const res = await insertCgaRecord(dto)
    if (res && (res.code === 200 || res.code === '200')) {
      ElMessage.success('评估提交成功')
      setTimeout(() => { backToComprehensive() }, 1500)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.daily-life-container {
  min-height: 100vh;
  padding: 24px 28px 48px;
  background: #faf5ff;
  box-sizing: border-box;
  max-width: 1100px;
  margin: 0 auto;
}

/* ===== 1. 顶部导航 ===== */
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

.top-left { display: flex; align-items: center; gap: 16px; }

.back-btn {
  font-size: 14px;
  color: #64748b;
  padding: 6px 12px;
  border-radius: 8px;
  &:hover { color: #a855f7; background: #f3e8ff; }
}

.title-block h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  letter-spacing: 1px;
}
.title-block p { margin: 4px 0 0; font-size: 12.5px; color: #94a3b8; }

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
  width: 36px; height: 36px;
  border-radius: 10px;
  background: #a855f7;
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700;
}
.badge-name { font-size: 14px; font-weight: 600; color: #1e293b; }
.badge-id { font-size: 11.5px; color: #94a3b8; margin-top: 2px; }

/* ===== 2. 进度条 ===== */
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
  left: 0; top: 0;
  height: 100%;
  background: linear-gradient(90deg, #a855f7, #c084fc);
  border-radius: 10px;
  transition: width 0.4s ease;
}

.progress-label {
  position: absolute;
  left: 20px; top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  z-index: 1;
}

/* ===== 3. 题目卡 ===== */
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
  &.is-answered { border-color: #d8b4fe; }
}

.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #faf5ff;
  border-bottom: 1px solid #f3e8ff;
}

.q-index {
  width: 30px; height: 30px;
  border-radius: 8px;
  background: #e9d5ff;
  color: #6b21a8;
  font-size: 12.5px;
  font-weight: 800;
  font-family: Consolas, monospace;
  display: flex; align-items: center; justify-content: center;
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
  width: 28px; height: 28px;
  border-radius: 50%;
  background: #22c55e;
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
}

/* 选项 */
.q-options {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
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

  &:hover { background: #f3e8ff; border-color: #e9d5ff; }
  &.is-selected { background: #f3e8ff; border-color: #a855f7; box-shadow: 0 2px 8px rgba(168,85,247,0.12); }
}

.opt-left { display: flex; align-items: center; gap: 12px; flex: 1; }

.opt-radio {
  width: 22px; height: 22px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;
  transition: all 0.15s;
  &.is-checked { background: #a855f7; border-color: #a855f7; }
}

.opt-content { flex: 1; }
.opt-label { font-size: 14px; color: #334155; line-height: 1.5; }

.opt-right {
  display: flex; align-items: baseline; gap: 2px; flex-shrink: 0; margin-left: 16px;
}
.opt-score {
  font-size: 20px; font-weight: 700; color: #64748b; font-family: Consolas, monospace; line-height: 1;
}
.opt-unit { font-size: 11px; color: #94a3b8; }

.is-selected .opt-score { color: #7c3aed; }
.is-selected .opt-unit { color: #a855f7; }

/* ===== 4. 结果区 ===== */
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
  width: 28px; height: 28px;
  border-radius: 8px;
  background: #e9d5ff;
  color: #a855f7;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
}

/* 分数卡片 */
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

/* 答题详情 */
.abnormal-section { padding: 12px 20px 20px; }

.abnormal-title {
  font-size: 13px;
  font-weight: 600;
  color: #a855f7;
  margin-bottom: 10px;
  padding-top: 6px;
  border-top: 1px dashed #e9d5ff;
}

.abnormal-row {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  &:last-child { border-bottom: none; }
}

.abnormal-content { flex: 1; }
.abnormal-q {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}
.abnormal-a {
  font-size: 12.5px;
  color: #64748b;
  line-height: 1.6;
}
.abnormal-row.is-abnormal .abnormal-a { color: #ef4444; }
.abnormal-row.is-normal .abnormal-a { color: #059669; }

.no-finding {
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.hint-text { font-size: 13px; color: #94a3b8; }

/* 建议卡片 */
.suggest-card .suggest-text {
  padding: 16px 20px;
  margin: 0;
  font-size: 14px;
  color: #334155;
  line-height: 1.7;
}

/* ===== 5. 提交按钮 ===== */
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

.submit-hint { margin: 0; font-size: 12px; color: #94a3b8; }
</style>
