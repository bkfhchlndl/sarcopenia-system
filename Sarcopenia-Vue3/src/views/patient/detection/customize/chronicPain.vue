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
          <h1>慢性疼痛评估</h1>
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
      <!-- 第一题结果：疼痛强度 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠</span>
          <span>疼痛强度评估</span>
        </div>
        <div v-if="firstQuestionResult" class="abnormal-section">
          <div class="abnormal-row" :class="{ 'is-abnormal': firstQuestionResult.isAbnormal, 'is-normal': !firstQuestionResult.isAbnormal }">
            <div class="abnormal-content">
              <div class="abnormal-q">{{ firstQuestionResult.questionTitle }}</div>
              <div class="abnormal-a">{{ firstQuestionResult.displayText }}</div>
            </div>
          </div>
        </div>
        <div v-else class="no-finding">
          <div class="hint-text">完成第一题评估后展示结果</div>
        </div>
      </div>

      <!-- 第二~五题结果：疼痛情况汇总 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">📋</span>
          <span>疼痛情况汇总</span>
        </div>
        <div v-if="otherQuestionsResults.length > 0" class="abnormal-section">
          <div v-for="(item, idx) in otherQuestionsResults" :key="idx" class="abnormal-row"
               :class="{ 'is-abnormal': item.isAbnormal, 'is-normal': !item.isAbnormal }">
            <div class="abnormal-content">
              <div class="abnormal-q">{{ item.questionTitle }}</div>
              <div class="abnormal-a">{{ item.displayText }}</div>
            </div>
          </div>
        </div>
        <div v-else class="no-finding">
          <div class="hint-text">完成全部题目后展示疼痛情况详情</div>
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
import { selectChronicPainScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'
const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || 33

const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })

watch(() => route.fullPath, () => {
  if (!scaleData.value) {
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}, { immediate: true })

const loadScaleData = async () => {
  try {
    const res = await selectChronicPainScale()
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

// 判断选项是否表示正常（无痛 / 不影响）
function isOptionNormal(option) {
  const text = String(option.content || '').toLowerCase()
  const label = String(option.label || '').toLowerCase()
  const combined = `${label} ${text}`
  const normalKeywords = ['无', '正常', '从不', '没有', '否', '不是', '不影响', '无疼痛', '无痛', '0分', '0 分']
  return normalKeywords.some(k => combined.includes(k))
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

// 根据选项内容生成摘要
function buildAnswerSummary() {
  const result = []
  const questions = scaleData.value.questionList || []
  for (let i = 0; i < questions.length; i++) {
    const q = questions[i]
    const selectedIds = answers.value[q.questionId] || []
    if (selectedIds.length === 0) continue
    const option = q.optionList?.find(o => o.id === selectedIds[0])
    if (!option) continue
    const isAbnormal = !isOptionNormal(option)
    result.push({
      index: i,
      questionId: q.questionId,
      questionTitle: q.title,
      displayText: option.content,
      isAbnormal,
      score: Number(option.score || 0)
    })
  }
  return result
}

// 第一题结果
const firstQuestionResult = computed(() => {
  const all = buildAnswerSummary()
  return all.find(item => item.index === 0) || null
})

// 其他题目结果
const otherQuestionsResults = computed(() => {
  const all = buildAnswerSummary()
  return all.filter(item => item.index > 0)
})

// 综合建议
const combinedSuggest = computed(() => {
  const all = buildAnswerSummary()
  if (all.length === 0) return '请完成评估以生成建议。'

  const first = firstQuestionResult.value
  const others = otherQuestionsResults.value

  // 拼接结论：第一题结论 + 其他题目总结
  const parts = []

  if (first) {
    parts.push(`疼痛强度：${first.displayText}。`)
  }

  if (others.length > 0) {
    const abnormal = others.filter(o => o.isAbnormal).map(o => o.displayText)
    if (abnormal.length > 0) {
      parts.push(`疼痛情况：${abnormal.join('；')}。`)
    } else {
      parts.push('疼痛情况：其他方面未见明显异常。')
    }
  }

  // 建议部分
  const hasSeverePain = first && first.isAbnormal && first.score >= 5
  const hasModeratePain = first && first.isAbnormal
  const hasAbnormalOther = others.some(o => o.isAbnormal)

  if (hasSeverePain) {
    parts.push('建议进一步进行疼痛评估，必要时到疼痛科或相关专科就诊，进行针对性的疼痛管理与治疗。')
  } else if (hasModeratePain) {
    parts.push('建议关注疼痛情况，适当调整生活方式，如症状持续或加重，建议到专科就诊。')
  } else if (hasAbnormalOther) {
    parts.push('疼痛相关因素存在异常，建议关注并适时评估。')
  } else {
    parts.push('目前疼痛相关评估未见明显异常，建议保持健康生活方式，定期关注身体状况。')
  }

  return parts.join('')
})

// 生成提交的 result 文本（精简版，避免数据库字段过长）
const resultText = computed(() => {
  const first = firstQuestionResult.value
  if (!first) return ''
  const hasOther = otherQuestionsResults.value.length > 0
  const abnormalOther = otherQuestionsResults.value.filter(o => o.isAbnormal).length
  let text = `疼痛强度：${first.displayText}${first.score ? `（${first.score}分）` : ''}`
  if (hasOther) {
    if (abnormalOther > 0) {
      text += `；疼痛相关存在${abnormalOther}项异常`
    } else {
      text += '；其他方面未见明显异常'
    }
  }
  return text
})

const goBack = () => { router.back() }

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:chronicPain`, '1')
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

/* 答题详情 */
.abnormal-section { padding: 12px 20px 16px; }

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
