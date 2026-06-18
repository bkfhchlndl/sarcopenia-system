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
          <h1>谵妄评估</h1>
          <p>CAM 意识模糊评估量表 · 共 {{ scaleData.questionList?.length || 0 }} 项</p>
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
      <span class="progress-label">已完成 {{ answeredCount }} / {{ scaleData.questionList?.length || 0 }} 项</span>
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
              :class="{
                'is-selected': answers[question.questionId]?.[0] === option.id,
                'is-max': isMaxScore(question.questionId, option.score)
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

    <!-- ===== 4. 结果与评判标准 ===== -->
    <section class="result-area">


      <div class="result-card note-card">
        <div class="note-block">
          <div class="note-title">评估结论</div>
          <div class="note-text">{{ resultLevel.text }}（{{ totalScore }} 分）</div>
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
      <p v-if="!canSubmit && (scaleData.questionList?.length || 0) > 0" class="submit-hint">
        请完成全部 {{ scaleData.questionList?.length || 0 }} 项评估后再提交
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectDeliriumScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)

const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'

const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || 29

// ============ 数据 ============
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
const answers = ref({})

// ============ 加载 ============
const loadScaleData = async () => {
  try {
    const res = await selectDeliriumScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败')
  }
}

onMounted(() => { loadScaleData() })

// ============ 选择 ============
function selectOption(questionId, option) {
  answers.value[questionId] = [option.id]
}

function isMaxScore(questionId, score) {
  const q = scaleData.value.questionList.find(qq => qq.questionId === questionId)
  if (!q) return false
  const max = Math.max(...q.optionList.map(o => Number(o.score || 0)))
  return Number(score || 0) >= max
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
    const [optionId] = answers.value[questionId]
    const question = scaleData.value.questionList.find(q => Number(q.questionId) === Number(questionId))
    if (!question) continue
    const option = question.optionList.find(o => Number(o.id) === Number(optionId))
    if (option) sum += Number(option.score || 0)
  }
  return sum
})

// ============ 结论 ============
// 判断某题已选答案是否为"是"（即存在风险）
function isAnswerYes(qIndex) {
  const q = scaleData.value.questionList?.[qIndex]
  if (!q) return false
  const selectedIds = answers.value[q.questionId] || []
  if (selectedIds.length === 0) return false
  const option = q.optionList?.find(o => o.id === selectedIds[0])
  if (!option) return false
  const text = String(option.content || '').trim()
  if (text === '无' || text === '没有' || text.includes('正常')) return false
  return text.includes('是') || Number(option.score || 0) > 0
}

// 各题对应的描述标签
const itemLabels = ['意识状态急性改变', '注意力障碍', '思维紊乱', '意识水平改变']

const resultLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8', bg: '#f8fafc' }
  }
  const q1Yes = isAnswerYes(0)
  const q2Yes = isAnswerYes(1)
  const q3Yes = isAnswerYes(2)
  const q4Yes = isAnswerYes(3)

  // 1. CAM 完整满足 → 明确谵妄
  if (q1Yes && q2Yes && (q3Yes || q4Yes)) {
    return { text: '明确谵妄', color: '#ef4444', bg: '#fef2f2' }
  }
  // 2. Q1+Q2 是，但 Q3/Q4 都无 → 可疑谵妄
  if (q1Yes && q2Yes) {
    return { text: '可疑谵妄', color: '#f97316', bg: '#fff7ed' }
  }
  // 3. 存在部分阳性项 → 给出具体提示
  const yesItems = []
  if (q1Yes) yesItems.push(itemLabels[0])
  if (q2Yes) yesItems.push(itemLabels[1])
  if (q3Yes) yesItems.push(itemLabels[2])
  if (q4Yes) yesItems.push(itemLabels[3])
  if (yesItems.length > 0) {
    return { text: '存在' + yesItems.join('、'), color: '#f59e0b', bg: '#fffbeb' }
  }
  // 4. 全部为"无" → 无谵妄
  return { text: '无谵妄', color: '#22c55e', bg: '#f0fdf4' }
})

const currentStandardIdx = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) return -1
  const t = resultLevel.value.text
  if (t === '无谵妄') return 0
  if (t.includes('存在') || t === '可疑谵妄') return 1
  return 2
})

// 判断选项是否正常（基于关键词）
function isOptionNormal(optionContent) {
  const text = String(optionContent || '').toLowerCase()
  const normalKeywords = ['无', '没有', '正常', '良好', '可以', '能', '会']
  const hasNormal = normalKeywords.some(k => text.includes(k))
  if (hasNormal) return true
  const abnormalKeywords = ['有', '是', '经常', '有时', '偶尔', '存在', '困难', '下降', '减少', '较差']
  return !abnormalKeywords.some(k => text.includes(k))
}

// 正常项文本
const normalItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const items = []
  for (let i = 0; i < (scaleData.value.questionList || []).length; i++) {
    const q = scaleData.value.questionList[i]
    const selectedIds = answers.value[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (!option) continue
    if (isOptionNormal(option.content)) items.push(`${i + 1}. ${option.content}`)
  }
  return items.length ? items.join('；') : '无'
})

// 异常项文本
const abnormalItemsText = computed(() => {
  if (answeredCount.value < totalQuestions.value) return ''
  const items = []
  for (let i = 0; i < (scaleData.value.questionList || []).length; i++) {
    const q = scaleData.value.questionList[i]
    const selectedIds = answers.value[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (!option) continue
    if (!isOptionNormal(option.content)) items.push(`${i + 1}. ${option.content}`)
  }
  return items.join('；')
})

const resultSuggest = computed(() => {
  const text = resultLevel.value.text
  if (text === '待评估') return '请完成全部题目以生成评估建议。'
  if (text === '无谵妄') return '当前未发现谵妄相关症状，意识状态良好。建议继续观察，保持良好作息环境，避免使用可能影响意识状态的药物，定期随访观察。'
  if (text === '可疑谵妄') return '存在谵妄可疑迹象，建议进一步完善认知功能评估，排查诱发因素（如感染、电解质紊乱、药物副作用、酒精或药物戒断等），并密切观察意识状态变化。'
  if (text === '明确谵妄') return '存在明确谵妄表现，建议尽快联系神经内科或老年科进一步评估，完善基础检查（血常规、电解质、肝肾功能、甲状腺功能、影像学等），排查并处理可逆性病因，给予适当的环境干预与对症治疗。'
  // 存在部分阳性项 → 给出针对性建议
  return '评估提示' + text + '，尚不满足谵妄的完整诊断标准。建议结合临床实际情况，关注相关症状的发展变化，必要时进一步评估以明确原因，避免影响日常生活与安全。'
})

// ============ 导航 ============
const goBack = () => { router.back() }

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:delirium`, '1')
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
    const [optionId] = answers.value[questionId]
    formattedAnswers[String(questionId)] = [Number(optionId)]
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
  &:hover { color: #3b82f6; background: #eff6ff; }
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
  background: #a855f7;
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
  background: linear-gradient(90deg, #a855f7, #7c3aed);
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

  &.is-answered { border-color: #c4b5fd; }
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
  width: 30px; height: 30px;
  border-radius: 8px;
  background: #e2e8f0;
  color: #475569;
  font-size: 12.5px; font-weight: 800;
  font-family: Consolas, monospace;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.question-card.is-answered .q-index { background: #a855f7; color: #fff; }

.q-title {
  flex: 1;
  font-size: 15px; font-weight: 600;
  color: #1e293b;
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

  &:hover { background: #f5f3ff; border-color: #c4b5fd; }
  &.is-selected { background: #f5f3ff; border-color: #a855f7; box-shadow: 0 2px 8px rgba(168,85,247,0.12); }
  &.is-max .opt-score { color: #16a34a; }
}

.opt-left { display: flex; align-items: center; gap: 12px; }

.opt-radio {
  font-size: 20px; color: #cbd5e1; flex-shrink: 0;
  transition: color 0.15s;
  &.is-checked { color: #a855f7; }
}

.opt-label { font-size: 14px; color: #374151; line-height: 1.5; }
.is-selected .opt-label { color: #5b21b6; font-weight: 600; }

.opt-right {
  display: flex; align-items: baseline;
  gap: 2px; flex-shrink: 0; margin-left: 16px;
}

.opt-score {
  font-size: 22px; font-weight: 800;
  color: #64748b; font-family: Consolas, monospace; line-height: 1;
}
.opt-unit { font-size: 11px; color: #94a3b8; }
.is-selected .opt-score { color: #7c3aed; }

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
  font-size: 13.5px; font-weight: 600; color: #64748b; margin-bottom: 14px;
}
.rc-icon { font-size: 18px; }

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
.stan-range { width: 90px; font-weight: 700; font-family: Consolas, monospace; color: #334155; }
.stan-text { color: #475569; }
.is-current .stan-text { color: #111827; }

.suggest-card { grid-column: 1 / -1; border-left: 4px solid #a855f7; }
.suggest-text { margin: 0; font-size: 14px; color: #374151; line-height: 1.9; }

.note-card { grid-column: 1 / -1; }
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
.note-placeholder {
  text-align: center;
  padding: 16px 12px;
  background: transparent;
}
.hint-text {
  color: #94a3b8;
  font-size: 13px;
}

.submit-area { text-align: center; padding: 8px 0; }

.submit-area .el-button--primary {
  background: linear-gradient(135deg, #a855f7, #7c3aed);
  border: none;
  padding: 14px 48px;
  font-size: 16px; font-weight: 600; letter-spacing: 1px;
  box-shadow: 0 4px 14px rgba(124,58,237,0.3);
  &:hover { box-shadow: 0 6px 20px rgba(124,58,237,0.4); transform: translateY(-1px); }
  &:disabled { background: #cbd5e1; box-shadow: none; }
}

.submit-hint { margin: 12px 0 0; font-size: 13px; color: #94a3b8; }

@media (max-width: 800px) {
  .daily-life-container { padding: 14px; }
  .top-bar { flex-direction: column; align-items: flex-start; gap: 12px; }
  .result-area { grid-template-columns: 1fr; }
  .suggest-card { grid-column: 1; }
}
</style>
