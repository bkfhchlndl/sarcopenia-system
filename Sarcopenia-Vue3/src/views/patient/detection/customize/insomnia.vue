<template>
  <div class="daily-life-container">
    <!-- 顶部导航 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>阿森斯失眠量表(AIS)</h1>
          <p>睡眠障碍评估 · 共 {{ totalQuestions }} 项 · 总分 {{ totalQuestions * 3 }} 分</p>
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

    <!-- 题目列表 -->
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
        <div class="q-options">
          <div
              v-for="option in question.optionList"
              :key="option.id"
              class="q-option"
              :class="{
              'is-selected': answers[question.questionId]?.[0] === option.id,
              'is-max': isMaxScore(question, option.score)
            }"
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
            <div class="opt-right">
              <span class="opt-score">{{ option.score }}</span>
              <span class="opt-unit">分</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 结果展示 -->
    <section class="result-area">
      <!-- 评估结果 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠</span>
          <span>评估结果 · {{ sleepLevel.text }}</span>
        </div>
        <div class="score-display">
          <span class="score-big">{{ totalScore }}</span>
          <span class="score-unit">分</span>
        </div>
        <div class="level-display">
          <div class="level-tag" :style="{ background: sleepLevel.bg, color: sleepLevel.color }">
            {{ sleepLevel.text }}
          </div>
        </div>
        <div class="standard-section">
          <div class="standard-title">评分标准</div>
          <div class="standard-list">
            <div class="standard-item" :class="{ 'is-current': currentStandardIdx === 0 }">
              <span class="std-dot" style="background:#22c55e"></span>
              <span class="std-range">0 ~ 3 分</span>
              <span class="std-text">无睡眠障碍</span>
            </div>
            <div class="standard-item" :class="{ 'is-current': currentStandardIdx === 1 }">
              <span class="std-dot" style="background:#f97316"></span>
              <span class="std-range">4 ~ 6 分</span>
              <span class="std-text">可能有睡眠障碍</span>
            </div>
            <div class="standard-item" :class="{ 'is-current': currentStandardIdx === 2 }">
              <span class="std-dot" style="background:#ef4444"></span>
              <span class="std-range">≥ 7 分</span>
              <span class="std-text">存在睡眠障碍</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 答题详情 -->
      <div class="result-card detail-card">
        <div class="rc-head">
          <span class="rc-icon">📋</span>
          <span>答题详情</span>
        </div>
        <div v-if="answerSummary.length > 0" class="detail-list">
          <div
              v-for="(item, idx) in answerSummary"
              :key="idx"
              class="detail-row"
              :class="{ 'is-abnormal': item.isAbnormal, 'is-normal': !item.isAbnormal }"
          >
            <span class="detail-index">{{ String(idx + 1).padStart(2, '0') }}</span>
            <div class="detail-content">
              <div class="detail-q">{{ item.questionTitle }}</div>
              <div class="detail-a">{{ item.displayText }}</div>
            </div>
            <div class="detail-score">{{ item.score }}</div>
          </div>
        </div>
        <div v-else-if="totalQuestions > 0" class="no-finding">
          <div class="hint-text">完成全部题目后展示答题详情</div>
        </div>
      </div>

      <!-- 评估建议 -->
      <div class="result-card suggest-card">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ sleepSuggest }}</p>
        <div v-if="sleepLevel.text === '存在睡眠障碍'" class="suggest-sub">
          建议进一步完善睡眠相关检查，评估睡眠障碍类型，必要时到神经内科或睡眠专科就诊。
        </div>
      </div>
    </section>

    <!-- 提交按钮 -->
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

<script setup name="AisScale">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectInsomniaScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)

// 路由参数
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || '患者')
const targetScaleId = computed(() => {
  const values = [route.query.targetScaleId, route.query.projectId, route.query.scaleId]
  const val = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return val === undefined ? 32 : Number(val)
})

// 量表数据
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
watch(() => route.fullPath, () => {
  if (!scaleData.value) scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
}, { immediate: true })

const loadScaleData = async () => {
  try {
    const res = await selectInsomniaScale()
    if (res.code === 200 && res.data?.length) scaleData.value = res.data[0]
  } catch (error) {
    ElMessage.error('加载量表数据失败')
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}
onMounted(() => loadScaleData())

// 答案与交互
const answers = ref({})
const selectOption = (question, option) => answers.value[question.questionId] = [option.id]
const isQuestionAnswered = question => (answers.value[question.questionId] || []).length > 0
const isMaxScore = (question, score) => score >= Math.max(...question.optionList.map(o => o.score))

// 进度统计
const totalQuestions = computed(() => scaleData.value.questionList?.length || 0)
const answeredCount = computed(() => {
  return scaleData.value.questionList.filter(q => isQuestionAnswered(q)).length
})
const progressPercent = computed(() => {
  return Math.round((answeredCount.value / (totalQuestions.value || 1)) * 100)
})

// 得分与等级
const totalScore = computed(() => {
  return scaleData.value.questionList.reduce((sum, q) => {
    const opt = q.optionList?.find(o => o.id === answers.value[q.questionId]?.[0])
    return sum + Number(opt?.score || 0)
  }, 0)
})

const LEVEL_CONFIG = [
  { max: 3, text: '无睡眠障碍', color: '#22c55e', bg: '#f0fdf4' },
  { max: 6, text: '可能有睡眠障碍', color: '#f97316', bg: '#fff7ed' },
  { max: Infinity, text: '存在睡眠障碍', color: '#ef4444', bg: '#fef2f2' }
]

const sleepLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || !totalQuestions.value) {
    return { text: '待评估', color: '#94a3b8', bg: '#f8fafc' }
  }
  return LEVEL_CONFIG.find(item => totalScore.value <= item.max)
})

const currentStandardIdx = computed(() => {
  if (answeredCount.value < totalQuestions.value) return -1
  return LEVEL_CONFIG.findIndex(item => totalScore.value <= item.max)
})

// 答题摘要
const answerSummary = computed(() => {
  return scaleData.value.questionList.map(q => {
    const opt = q.optionList?.find(o => o.id === answers.value[q.questionId]?.[0])
    if (!opt) return null
    return {
      questionTitle: q.title,
      displayText: opt.content,
      isAbnormal: Number(opt.score) >= 2,
      score: opt.score
    }
  }).filter(Boolean)
})

// 建议文案
const SUGGEST_MAP = {
  '待评估': '请完成全部题目以生成评估建议。',
  '无睡眠障碍': '建议保持规律作息，避免熬夜，睡前避免使用电子产品，维持良好的睡眠习惯。',
  '可能有睡眠障碍': '建议调整作息时间，改善睡眠环境，避免睡前饮用咖啡、浓茶等刺激性饮品，适当增加日间运动。',
  '存在睡眠障碍': '建议进一步完善睡眠相关检查，评估睡眠障碍类型，并给予针对性的干预和治疗。'
}
const sleepSuggest = computed(() => SUGGEST_MAP[sleepLevel.value.text])

// 导航
const goBack = () => router.push({ path: '/patient/detection/customize', query: route.query })
const backToComprehensive = () => {
  try {
    if (patientId.value) {
      localStorage.setItem(`customize_done:${patientId.value}:ais`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/customize',
    query: { patientId: patientId.value, patientName: patientName.value }
  })
}

// 提交
const submitAssessment = async () => {
  if (submitting.value || answeredCount.value < totalQuestions.value) return
  submitting.value = true

  const formattedAnswers = {}
  scaleData.value.questionList.forEach(q => {
    const ids = answers.value[q.questionId] || []
    if (ids.length) formattedAnswers[String(q.questionId)] = ids.map(id => Number(id))
  })

  const dto = {
    patientId: Number(patientId.value),
    projectId: targetScaleId.value,
    scaleId: targetScaleId.value,
    answers: formattedAnswers,
    result: `${sleepLevel.value.text}`,
    suggest: sleepSuggest.value
  }

  try {
    const res = await insertCgaRecord(dto)
    if (res?.code === 200 || res?.code === '200') {
      ElMessage.success('评估提交成功')
      setTimeout(() => backToComprehensive(), 1500)
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

/* 顶部导航 */
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

/* 进度条 */
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

/* 题目卡片 */
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
.question-card.is-answered .q-index { background: #a855f7; color: #fff; }
.q-title { flex: 1; font-size: 15px; font-weight: 600; color: #1e293b; }
.q-done {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: #22c55e;
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
}

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
  display: flex; align-items: baseline; gap: 2px;
  flex-shrink: 0; margin-left: 16px;
}
.opt-score {
  font-size: 20px; font-weight: 700;
  color: #64748b;
  font-family: Consolas, monospace;
  line-height: 1;
}
.opt-unit { font-size: 11px; color: #94a3b8; }
.is-selected .opt-score { color: #7c3aed; }
.is-selected .opt-unit { color: #a855f7; }

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
  width: 28px; height: 28px;
  border-radius: 8px;
  background: #e9d5ff;
  color: #a855f7;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px;
}

/* 总分展示 */
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
.level-display {
  padding: 8px 20px 20px;
  display: flex;
  justify-content: center;
}
.level-tag {
  padding: 8px 24px;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 2px;
}

/* 评分标准 */
.standard-section {
  padding: 16px 20px 20px;
  border-top: 1px dashed #e9d5ff;
}
.standard-title {
  font-size: 13px;
  font-weight: 700;
  color: #a855f7;
  margin-bottom: 10px;
}
.standard-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.standard-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  transition: all 0.2s;
  &.is-current { background: #faf5ff; font-weight: 600; }
}
.std-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.std-range {
  font-family: Consolas, monospace;
  color: #334155;
  min-width: 80px;
}
.std-text { color: #475569; }

/* 答题详情 */
.detail-list { padding: 4px 20px 20px; }
.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  &:last-child { border-bottom: none; }
}
.detail-index {
  width: 28px; height: 28px;
  border-radius: 6px;
  background: #e9d5ff;
  color: #6b21a8;
  font-size: 11.5px; font-weight: 700;
  font-family: Consolas, monospace;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.detail-row.is-abnormal .detail-index { background: #fef3c7; color: #b45309; }
.detail-row.is-normal .detail-index { background: #dcfce7; color: #166534; }
.detail-content { flex: 1; }
.detail-q { font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 3px; }
.detail-a { font-size: 12.5px; color: #64748b; line-height: 1.5; }
.detail-row.is-abnormal .detail-a { color: #ef4444; }
.detail-row.is-normal .detail-a { color: #059669; }
.detail-score {
  font-size: 18px; font-weight: 800;
  color: #7c3aed;
  font-family: Consolas, monospace;
  flex-shrink: 0;
  min-width: 30px;
  text-align: right;
}
.detail-row.is-abnormal .detail-score { color: #b45309; }
.detail-row.is-normal .detail-score { color: #166534; }

.no-finding {
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.hint-text { font-size: 13px; color: #94a3b8; }

/* 建议卡片 */
.suggest-text {
  padding: 16px 20px;
  margin: 0;
  font-size: 14px;
  color: #334155;
  line-height: 1.7;
}
.suggest-sub {
  padding: 0 20px 16px;
  font-size: 12.5px;
  color: #f97316;
  line-height: 1.6;
  border-top: 1px dashed #fed7aa;
  padding-top: 12px;
  margin-top: 4px;
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
.submit-hint { margin: 0; font-size: 12px; color: #94a3b8; }

/* 响应式 */
@media (max-width: 800px) {
  .daily-life-container { padding: 14px; }
  .top-bar { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
