<template>
  <div class="daily-life-container">
    <!-- ===== 顶部导航栏 ===== -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>简易体能状况量表评估</h1>
          <p>SPPB · 平衡测试 · 步速测试 · 椅子起立测试 · 共 {{ scaleData.questionList?.length || 0 }} 项</p>
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

    <!-- ===== 进度条 ===== -->
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">已完成 {{ answeredCount }} / {{ scaleData.questionList?.length || 0 }} 项</span>
    </div>

    <!-- ===== 评估题卡列表 ===== -->
    <section class="question-list">
      <div
          v-for="(question, qIndex) in scaleData.questionList"
          :key="question.questionId"
          class="question-card"
          :class="{ 'is-answered': answers[question.questionId] !== undefined }"
      >
        <div class="q-head">
          <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
          <span class="q-title">{{ question.title }}</span>
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
            <div class="opt-right">
              <span class="opt-score">{{ option.score }}</span>
              <span class="opt-unit">分</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 结果与评判标准 ===== -->
    <section class="result-area">
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
          <span
              class="level-tag"
              :style="{
              background: dependencyLevel.bg,
              color: dependencyLevel.color,
              borderColor: dependencyLevel.color
            }"
          >
            {{ dependencyLevel.text }}
          </span>
        </div>
      </div>

      <div class="result-card standard-card">
        <div class="rc-head">
          <span class="rc-icon">📋</span>
          <span>评判标准</span>
        </div>
        <div class="standard-list">
          <div
              v-for="(level, index) in DEPENDENCY_LEVELS"
              :key="index"
              class="stan-row"
              :class="{ 'is-current': currentStandardIdx === index }"
          >
            <span class="stan-dot" :style="{ background: level.color }"></span>
            <span class="stan-range">
              {{ index === 0 ? '0 ~ 6 分' : index === 1 ? '7 ~ 9 分' : '10 ~ 12 分' }}
            </span>
            <span class="stan-text">{{ level.text }}</span>
          </div>
        </div>
      </div>

      <div class="result-card suggest-card">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ dependencySuggest }}</p>
      </div>
    </section>

    <!-- ===== 提交按钮 ===== -->
    <div class="submit-area">
      <el-button
          type="primary"
          size="large"
          round
          :disabled="answeredCount < (scaleData.questionList?.length || 0)"
          @click="submitAssessment"
      >
        <el-icon><Check /></el-icon>
        提交评估
      </el-button>
      <p v-if="answeredCount < (scaleData.questionList?.length || 0)" class="submit-hint">
        请完成全部 {{ scaleData.questionList?.length || 0 }} 项评估后再提交
      </p>
    </div>
  </div>
</template>

<script setup name="SppbAssessment">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectSppbScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
const STORAGE_PREFIX = 'customize_done:'
const DEFAULT_SCALE_ID = 22

const DEPENDENCY_LEVELS = [
  {
    maxScore: 6,
    text: '功能较差',
    color: '#ef4444',
    bg: '#fef2f2',
    suggest: '简易体能状况量表得分提示下肢功能明显减退，平衡与肌力下降，跌倒风险较高，建议转诊康复评估，制定个体化下肢肌力训练、平衡训练与防跌倒方案。'
  },
  {
    maxScore: 9,
    text: '功能中等',
    color: '#f97316',
    bg: '#fff7ed',
    suggest: '简易体能状况量表得分提示躯体功能轻度下降，建议加强下肢肌力训练与平衡练习，结合日常步行、起坐练习等功能性活动，3-6 个月复查。'
  },
  {
    maxScore: 12,
    text: '功能良好',
    color: '#22c55e',
    bg: '#f0fdf4',
    suggest: '简易体能状况量表得分提示下肢功能、平衡与步速良好，建议维持现有运动习惯，坚持规律运动与日常活动，定期随访复查。'
  }
]

// ==================== 路由参数 ====================
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || '患者')
const targetScaleId = computed(() => {
  return toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || DEFAULT_SCALE_ID
})

// ==================== 响应式状态 ====================
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
const answers = ref({})

// ==================== 计算属性 ====================
const answeredCount = computed(() => Object.keys(answers.value).length)

const progressPercent = computed(() => {
  const total = scaleData.value.questionList?.length || 1
  return Math.round((answeredCount.value / total) * 100)
})

const totalScore = computed(() => {
  let sum = 0
  const questionList = scaleData.value.questionList || []
  for (const questionId in answers.value) {
    const [optionId] = answers.value[questionId]
    const question = questionList.find(q => q.questionId == questionId)
    if (!question) continue
    const option = question.optionList.find(o => o.id == optionId)
    if (option) sum += option.score
  }
  return sum
})

const currentStandardIdx = computed(() => {
  if (!answeredCount.value) return -1
  const score = totalScore.value
  for (let i = 0; i < DEPENDENCY_LEVELS.length; i++) {
    if (score <= DEPENDENCY_LEVELS[i].maxScore) return i
  }
  return DEPENDENCY_LEVELS.length - 1
})

const dependencyLevel = computed(() => {
  const idx = currentStandardIdx.value
  return idx >= 0
      ? DEPENDENCY_LEVELS[idx]
      : { text: '待评估', color: '#94a3b8', bg: '#f8fafc' }
})

const dependencySuggest = computed(() => {
  const idx = currentStandardIdx.value
  return idx >= 0
      ? DEPENDENCY_LEVELS[idx].suggest
      : '请完成全部评估项目以生成建议。'
})

// ==================== 工具函数 ====================
function toNumberId(...values) {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}

function isMaxScore(questionId, score) {
  const question = scaleData.value.questionList.find(q => q.questionId === questionId)
  if (!question) return false
  const max = Math.max(...question.optionList.map(o => o.score))
  return score >= max
}

// ==================== 业务方法 ====================
async function loadScaleData() {
  try {
    const res = await selectSppbScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败')
  }
}

function selectOption(questionId, option) {
  answers.value[questionId] = [option.id]
}

function goBack() {
  router.back()
}

function backToComprehensive() {
  try {
    if (patientId.value) {
      localStorage.setItem(`${STORAGE_PREFIX}${patientId.value}:exercise_function`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/comprehensive',
    query: {
      patientId: patientId.value,
      patientName: patientName.value
    }
  })
}

async function submitAssessment() {
  const totalCount = scaleData.value.questionList?.length || 0
  if (answeredCount.value < totalCount) {
    ElMessage.warning(`还有 ${totalCount - answeredCount.value} 道题未作答`)
    return
  }

  const formattedAnswers = {}
  for (const questionId in answers.value) {
    const [optionId] = answers.value[questionId]
    formattedAnswers[String(questionId)] = [Number(optionId)]
  }

  const dto = {
    patientId: Number(patientId.value),
    projectId: targetScaleId.value,
    scaleId: targetScaleId.value,
    answers: formattedAnswers,
    result: dependencyLevel.value.text,
    suggest: dependencySuggest.value
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
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadScaleData()
})
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

/* ===== 顶部导航 ===== */
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
  background: #3b82f6;
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

/* ===== 进度条 ===== */
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
  background: linear-gradient(90deg, #3b82f6, #2563eb);
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
  mix-blend-mode: difference;
}

/* ===== 题目卡片 ===== */
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
    border-color: #93c5fd;
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
  background: #3b82f6;
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

/* 选项 */
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
    background: #eff6ff;
    border-color: #bfdbfe;
  }

  &.is-selected {
    background: #eff6ff;
    border-color: #3b82f6;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.10);
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
    color: #3b82f6;
  }
}

.opt-label {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}

.is-selected .opt-label {
  color: #1e40af;
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
  color: #1d4ed8;
}

/* ===== 结果区 ===== */
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

/* 分数卡片 */
.score-card {
  text-align: center;
}

.score-big {
  font-size: 56px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
  font-family: Consolas, monospace;
}

.score-unit {
  font-size: 18px;
  color: #64748b;
  margin-left: 4px;
}

.rc-footer {
  margin-top: 12px;
}

.level-tag {
  display: inline-block;
  padding: 6px 20px;
  border-radius: 999px;
  border: 1.5px solid;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* 标准卡片 */
.standard-list {
  margin-top: -4px;
}

.stan-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  font-size: 13px;
  color: #64748b;
  border-radius: 6px;
  transition: all 0.2s;

  &.is-current {
    background: #f8fafc;
    padding: 6px 10px;
    margin: 0 -10px;
    color: #1e293b;
    font-weight: 600;
  }
}

.stan-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.stan-range {
  width: 90px;
  font-weight: 700;
  font-family: Consolas, monospace;
  color: #334155;
}

.stan-text {
  color: #475569;
}

.is-current .stan-text {
  color: #111827;
}

/* 建议卡片 — 跨两列 */
.suggest-card {
  grid-column: 1 / -1;
  border-left: 4px solid #3b82f6;
}

.suggest-text {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.9;
}

/* ===== 提交 ===== */
.submit-area {
  text-align: center;
  padding: 8px 0;
}

.submit-area .el-button--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  padding: 14px 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.3);

  &:hover {
    box-shadow: 0 6px 20px rgba(37, 99, 235, 0.4);
    transform: translateY(-1px);
  }

  &:disabled {
    background: #cbd5e1;
    box-shadow: none;
  }
}

.submit-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: #94a3b8;
}

/* ===== 响应式 ===== */
@media (max-width: 800px) {
  .daily-life-container {
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

  .suggest-card {
    grid-column: 1;
  }
}
</style>