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
          <h1>焦虑评估</h1>
          <p>GAD-7 广泛性焦虑量表 · 共 {{ totalQuestions }} 项</p>
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
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">已完成 {{ answeredCount }} / {{ totalQuestions }} 项</span>
    </div>

    <!-- ===== 3. 评估题卡列表 ===== -->
    <section class="question-list">
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
          <span v-if="answers[question.questionId] !== undefined" class="q-done">
            <el-icon><Check /></el-icon>
          </span>
        </div>

        <div class="q-options">
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
            <div v-if="option.score !== undefined && option.score !== null" class="opt-right">
              <span class="opt-score">{{ option.score }}</span>
              <span class="opt-unit">分</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 4. 结果与评判标准 ===== -->
    <section class="result-area" v-if="answeredCount === totalQuestions">
      <div class="result-card score-card">
        <div class="rc-head">
          <span class="rc-icon">📊</span>
          <span>评估总分</span>
        </div>
        <div class="rc-body">
          <span class="score-big">{{ totalScore }}</span>
          <span class="score-unit">分</span>
        </div>
        <div class="rc-footer">
          <span class="level-tag" :style="{ background: resultLevel.color, color: '#fff', borderColor: resultLevel.color }">
            {{ resultLevel.text }}
          </span>
        </div>
      </div>

      <div class="result-card standard-card">
        <div class="rc-head">
          <span class="rc-icon">📋</span>
          <span>评判标准</span>
        </div>
        <div class="standard-list">
          <div class="stan-row" :class="{ 'is-current': currentStandardIdx === 0 }">
            <span class="stan-dot" style="background:#22c55e"></span>
            <span class="stan-range">0 ~ 4 分</span>
            <span class="stan-text">正常，无明显焦虑症状</span>
          </div>
          <div class="stan-row" :class="{ 'is-current': currentStandardIdx === 1 }">
            <span class="stan-dot" style="background:#f59e0b"></span>
            <span class="stan-range">5 ~ 9 分</span>
            <span class="stan-text">轻度焦虑</span>
          </div>
          <div class="stan-row" :class="{ 'is-current': currentStandardIdx === 2 }">
            <span class="stan-dot" style="background:#f97316"></span>
            <span class="stan-range">10 ~ 14 分</span>
            <span class="stan-text">中度焦虑</span>
          </div>
          <div class="stan-row" :class="{ 'is-current': currentStandardIdx === 3 }">
            <span class="stan-dot" style="background:#ef4444"></span>
            <span class="stan-range">15 ~ 21 分</span>
            <span class="stan-text">重度焦虑</span>
          </div>
        </div>
      </div>

      <div class="result-card note-card" style="grid-column: 1 / -1;">
        <div class="note-block">
          <div class="note-title">评估结论</div>
          <div class="note-text">{{ resultLevel.text }}（{{ totalScore }} 分）</div>
        </div>
        <div class="note-block" v-if="riskItemsText">
          <div class="note-title"><span class="dot-abnormal">●</span> 风险因素</div>
          <div class="note-text">{{ riskItemsText }}</div>
        </div>
        <div class="note-block" v-if="normalItemsText">
          <div class="note-title"><span class="dot-normal">●</span> 正常项</div>
          <div class="note-text">{{ normalItemsText }}</div>
        </div>
      </div>

      <div class="result-card suggest-card" style="grid-column: 1 / -1;">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ resultSuggest }}</p>
      </div>
    </section>

    <!-- ===== 5. 提交按钮 ===== -->
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
        请完成全部 {{ totalQuestions }} 项评估后再提交
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectAnxietyScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)

const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'

const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || 42

// ============ 数据 ============
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
const answers = ref({})

// ============ 加载 ============
const loadScaleData = async () => {
  try {
    const res = await selectAnxietyScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败')
  }
}

onMounted(() => { loadScaleData() })

// ============ 选择 ============
function selectOption(questionId, optionId) {
  answers.value[questionId] = optionId
}

// ============ 进度 ============
const totalQuestions = computed(() => scaleData.value.questionList?.length || 0)
const answeredCount = computed(() => Object.keys(answers.value).length)
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ============ 分数 ============
const totalScore = computed(() => {
  let sum = 0
  for (const questionId in answers.value) {
    const optionId = answers.value[questionId]
    const question = scaleData.value.questionList.find(q => Number(q.questionId) === Number(questionId))
    if (!question) continue
    const option = question.optionList.find(o => Number(o.id) === Number(optionId))
    if (option) sum += Number(option.score || 0)
  }
  return sum
})

// ============ 工具：取某题已选选项内容 & 分数 ============
function getSelectedContent(qIndex) {
  const q = scaleData.value.questionList?.[qIndex]
  if (!q) return ''
  const selectedId = answers.value[q.questionId]
  if (selectedId === undefined || selectedId === null) return ''
  const option = q.optionList?.find(o => Number(o.id) === Number(selectedId))
  return option ? String(option.content || '').trim() : ''
}

function getSelectedScore(qIndex) {
  const q = scaleData.value.questionList?.[qIndex]
  if (!q) return 0
  const selectedId = answers.value[q.questionId]
  if (selectedId === undefined || selectedId === null) return 0
  const option = q.optionList?.find(o => Number(o.id) === Number(selectedId))
  return Number(option?.score || 0)
}

// 正常项 = 单题得分 0 的项目
const normalItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const list = []
  for (let i = 0; i < (scaleData.value.questionList || []).length; i++) {
    const q = scaleData.value.questionList[i]
    const score = getSelectedScore(i)
    const content = getSelectedContent(i)
    if (score === 0 && content) list.push(`${i + 1}. ${content}`)
  }
  return list.length ? list.join('；') : '无'
})

// 风险项 = 单题得分 ≥ 1 的项目
const riskItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const list = []
  for (let i = 0; i < (scaleData.value.questionList || []).length; i++) {
    const q = scaleData.value.questionList[i]
    const score = getSelectedScore(i)
    const content = getSelectedContent(i)
    if (score >= 1 && content) list.push(`${i + 1}. ${q.title}：${content}（${score} 分）`)
  }
  return list.join('；')
})

// ============ 评估结论 ============
const resultLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8' }
  }
  const score = totalScore.value
  if (score <= 4) return { text: '正常，无明显焦虑症状', color: '#22c55e' }
  if (score <= 9) return { text: '轻度焦虑', color: '#f59e0b' }
  if (score <= 14) return { text: '中度焦虑', color: '#f97316' }
  return { text: '重度焦虑', color: '#ef4444' }
})

const currentStandardIdx = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) return -1
  const score = totalScore.value
  if (score <= 4) return 0
  if (score <= 9) return 1
  if (score <= 14) return 2
  return 3
})

// ============ 评估建议 ============
const resultSuggest = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) return '请完成全部题目以生成评估建议。'
  const score = totalScore.value
  if (score <= 4) {
    return 'GAD-7 评分提示情绪状态良好，无明显焦虑症状。建议保持规律作息、适度运动、合理饮食与良好社会支持，定期随访观察。'
  }
  if (score <= 9) {
    return 'GAD-7 评分提示轻度焦虑症状，建议加强心理调节与压力管理，保持规律作息与社交活动，必要时寻求心理咨询或短期随访评估。'
  }
  if (score <= 14) {
    return 'GAD-7 评分提示中度焦虑，建议至精神心理专科进一步评估，考虑结合心理治疗（如认知行为治疗）与必要的药物干预，同时关注躯体健康与生活方式调整。'
  }
  return 'GAD-7 评分提示重度焦虑，建议尽快至精神心理专科就诊，完善进一步评估与诊断，制定个体化治疗方案（心理治疗、药物治疗或二者联合），家属应加强陪伴与关注，避免病情加重。'
})

// ============ 导航 ============
const goBack = () => { router.back() }

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:anxiety`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/comprehensive',
    query: { patientId, patientName }
  })
}

// ============ 提交 ============
const canSubmit = computed(() => {
  return totalQuestions.value > 0 && answeredCount.value >= totalQuestions.value
})

const submitAssessment = async () => {
  if (submitting.value) return
  if (!canSubmit.value) {
    ElMessage.warning(`还有 ${totalQuestions.value - answeredCount.value} 道题未作答`)
    return
  }

  const formattedAnswers = {}
  for (const questionId in answers.value) {
    formattedAnswers[String(questionId)] = [Number(answers.value[questionId])]
  }

  const dto = {
    patientId: Number(patientId),
    projectId: targetScaleId,
    scaleId: targetScaleId,
    answers: formattedAnswers,
    result: resultLevel.value.text,
    suggest: resultSuggest.value
  }

  submitting.value = true
  try {
    const res = await insertCgaRecord(dto)
    if (res && (res.code === 200 || res.code === '200')) {
      ElMessage.success('评估提交成功')
      setTimeout(() => { backToComprehensive() }, 1500)
    } else {
      ElMessage.error(res.msg || '提交失败')
      submitting.value = false
    }
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.daily-life-container {
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

.top-left { display: flex; align-items: center; gap: 16px; }

.back-btn {
  font-size: 14px;
  color: #64748b;
  padding: 6px 12px;
  border-radius: 8px;
  &:hover { color: #f59e0b; background: #fef3c7; }
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
  width: 36px; height: 36px;
  border-radius: 10px;
  background: #f59e0b;
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700;
}
.badge-name { font-size: 14px; font-weight: 600; color: #1e293b; }
.badge-id { font-size: 11.5px; color: #94a3b8; margin-top: 2px; }

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
  background: linear-gradient(90deg, #f59e0b, #d97706);
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

  &.is-answered { border-color: #fcd34d; }
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
  width: 30px; height: 30px;
  border-radius: 8px;
  background: #e2e8f0;
  color: #475569;
  font-size: 12.5px; font-weight: 800;
  font-family: Consolas, monospace;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.question-card.is-answered .q-index { background: #f59e0b; color: #fff; }

.q-title {
  font-size: 15px; font-weight: 600;
  color: #1e293b;
}

.q-remark-inline {
  font-size: 12.5px;
  color: #64748b;
  font-weight: 400;
  line-height: 1.6;
}

.q-done {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: #22c55e; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
}

.q-options {
  padding: 10px 12px;
  display: flex; flex-direction: column; gap: 6px;
}

.q-option {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
  background: #fafbfc;

  &:hover { background: #fef3c7; border-color: #fcd34d; }
  &.is-selected { background: #fef3c7; border-color: #f59e0b; box-shadow: 0 2px 8px rgba(245,158,11,0.18); }
}

.opt-left { display: flex; align-items: center; gap: 12px; }

.opt-radio {
  font-size: 20px; color: #cbd5e1; flex-shrink: 0;
  transition: color 0.15s;
  &.is-checked { color: #f59e0b; }
}

.opt-label { font-size: 14px; color: #374151; line-height: 1.5; }
.is-selected .opt-label { color: #78350f; font-weight: 600; }

.opt-right {
  display: flex; align-items: baseline;
  gap: 2px; flex-shrink: 0; margin-left: 16px;
}

.opt-score {
  font-size: 22px; font-weight: 800;
  color: #64748b; font-family: Consolas, monospace; line-height: 1;
}
.opt-unit { font-size: 11px; color: #94a3b8; }
.is-selected .opt-score { color: #b45309; }

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

.rc-head {
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 700; color: #1e293b; margin-bottom: 14px;
}
.rc-icon { font-size: 20px; }

.score-card { text-align: center; }

.score-big {
  font-size: 56px; font-weight: 800;
  color: #1e293b; line-height: 1;
  font-family: Consolas, monospace;
}
.score-unit { font-size: 18px; color: #64748b; margin-left: 4px; }
.rc-footer { margin-top: 12px; }

.level-tag {
  display: inline-block;
  padding: 6px 20px;
  border-radius: 999px;
  border: 1.5px solid;
  font-size: 15px; font-weight: 700; letter-spacing: 1px;
}

.standard-list { margin-top: -4px; }

.stan-row {
  display: flex; align-items: center; gap: 10px;
  padding: 6px 0;
  font-size: 13px; color: #64748b;
  border-radius: 6px; transition: all 0.2s;

  &.is-current {
    background: #f8fafc; padding: 6px 10px;
    margin: 0 -10px; color: #1e293b; font-weight: 600;
  }
}

.stan-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.stan-range { width: 110px; font-weight: 700; font-family: Consolas, monospace; color: #334155; }
.stan-text { color: #475569; }
.is-current .stan-text { color: #111827; }

.note-card { }
.note-block {
  padding: 8px 12px;
  margin-bottom: 6px;
  background: #f8fafc;
  border-radius: 8px;
  &:last-child { margin-bottom: 0; }
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
.dot-normal { color: #22c55e; font-size: 10px; }
.dot-abnormal { color: #ef4444; font-size: 10px; }
.note-text {
  font-size: 13.5px;
  color: #1f2937;
  line-height: 1.7;
}

.suggest-card {
  border-left: 4px solid #f59e0b;
}
.suggest-text { margin: 0; font-size: 14px; color: #374151; line-height: 1.9; }

.submit-area { text-align: center; padding: 8px 0; }

.submit-area .el-button--primary {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none;
  padding: 14px 48px;
  font-size: 16px; font-weight: 600; letter-spacing: 1px;
  box-shadow: 0 4px 14px rgba(217,119,6,0.3);
  &:hover { box-shadow: 0 6px 20px rgba(217,119,6,0.4); transform: translateY(-1px); }
  &:disabled { background: #cbd5e1; box-shadow: none; }
}

.submit-hint { margin: 12px 0 0; font-size: 13px; color: #94a3b8; }

@media (max-width: 800px) {
  .daily-life-container { padding: 14px; }
  .top-bar { flex-direction: column; align-items: flex-start; gap: 12px; }
  .result-area { grid-template-columns: 1fr; }
}
</style>
