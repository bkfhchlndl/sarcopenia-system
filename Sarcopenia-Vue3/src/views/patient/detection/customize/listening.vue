<template>
  <div class="daily-life-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>听力与耳语试验评估</h1>
          <p>听力筛查 · 共 {{ totalQuestions }} 项</p>
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

    <!-- 听力评估题卡列表 -->
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
                <span class="opt-label">{{ option.content || option.label }}</span>
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

    <!-- 结果展示区 -->
    <section class="result-area">
      <!-- 听力简易评估得分卡片 -->
      <div class="result-card score-card">
        <div class="rc-head">
          <span class="rc-icon">👂</span>
          <span>听力简易评估 · 得分</span>
        </div>
        <div class="rc-body">
          <span class="score-big">{{ hearingScore }}</span>
          <span class="score-unit">/4分</span>
        </div>
        <div class="rc-footer">
          <span
              class="level-tag"
              :style="{
              background: hearingLevel.bg,
              color: hearingLevel.color,
              borderColor: hearingLevel.color
            }"
          >
            {{ hearingLevel.text }}
          </span>
        </div>
      </div>

      <!-- 耳语试验得分卡片 -->
      <div class="result-card score-card">
        <div class="rc-head">
          <span class="rc-icon">🔊</span>
          <span>耳语试验 · 得分</span>
        </div>
        <div class="rc-body">
          <span class="score-big">{{ whisperTestScore }}</span>
          <span class="score-unit">/4分</span>
        </div>
        <div class="rc-footer">
          <span
              class="level-tag"
              :style="{
              background: whisperTestLevel.bg,
              color: whisperTestLevel.color,
              borderColor: whisperTestLevel.color
            }"
          >
            {{ whisperTestLevel.text }}
          </span>
        </div>
      </div>

      <!-- 检出问题卡片 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠</span>
          <span>检出问题 · 共 {{ abnormalAnswers.length }} 项</span>
        </div>
        <div v-if="abnormalAnswers.length === 0" class="no-finding">
          <el-icon class="ok-icon"><Check /></el-icon>
          <div class="ok-text">未检出异常项目，建议保持良好用耳习惯，定期进行听力检查。</div>
        </div>
        <div v-else class="finding-list">
          <div
              v-for="(item, idx) in abnormalAnswers"
              :key="item.question.questionId"
              class="finding-row"
          >
            <div class="finding-index">{{ idx + 1 }}</div>
            <div class="finding-content">
              <div class="finding-title">{{ item.question.title }}</div>
              <div class="finding-option">
                <span class="finding-label">你的选择：</span>
                <span class="finding-value">{{ item.option.content || item.option.label }}</span>
                <span class="finding-score">（{{ item.option.score }} 分）</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 综合建议卡片 -->
      <div class="result-card suggest-card combined-suggest">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ hearingSuggest }} {{ whisperTestSuggest }}</p>
        <div v-if="abnormalAnswers.length > 0" class="suggest-sub">
          如以上异常项目持续存在，建议尽快耳鼻喉科专科评估，完善纯音测听检查，排查老年性聋、中耳炎、耵聍栓塞等病因。
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
          :disabled="answeredCount < totalQuestions"
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

<script setup name="HearingAssessment">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectListeningScale, insertCgaRecord } from '@/api/cga.js'

// ==================== 常量配置 ====================
/** 本地存储完成状态前缀 */
const STORAGE_PREFIX = 'customize_done:'
/** 默认量表ID */
const DEFAULT_SCALE_ID = 26

/** 听力简易评估等级配置（对应国标B.6） */
const HEARING_LEVEL_CONFIG = [
  {
    maxScore: 0,
    text: '完全失聪',
    color: '#ef4444',
    bg: '#fef2f2',
    suggest: '完全失聪，建议尽快耳鼻喉科专科检查，评估人工耳蜗植入等康复方案。'
  },
  {
    maxScore: 2,
    text: '听力障碍',
    color: '#f97316',
    bg: '#fff7ed',
    suggest: '听力障碍较明显，建议尽快耳鼻喉科专科评估，排查老年性聋、中耳炎、听神经瘤等病因，必要时佩戴助听器。'
  },
  {
    maxScore: 3,
    text: '听力下降',
    color: '#3b82f6',
    bg: '#eff6ff',
    suggest: '存在轻度听力下降，应考虑是否有耵聍栓塞、早期老年性聋或中耳炎等问题，建议耳鼻喉科就诊完善纯音测听检查。'
  },
  {
    maxScore: 4,
    text: '听力正常',
    color: '#22c55e',
    bg: '#f0fdf4',
    suggest: '听力正常，建议保持良好用耳习惯，避免长时间暴露于噪音环境，定期进行听力检查。'
  }
]

/** 耳语试验等级配置（对应国标B.7） */
const WHISPER_TEST_LEVEL_CONFIG = [
  {
    maxScore: 0,
    text: '双耳异常',
    color: '#ef4444',
    bg: '#fef2f2',
    suggest: '耳语试验双耳阳性，提示双侧听力障碍，建议尽快耳鼻喉科专科评估。'
  },
  {
    maxScore: 2,
    text: '单耳异常',
    color: '#f97316',
    bg: '#fff7ed',
    suggest: '耳语试验单耳阳性，提示单侧听力障碍，建议进一步纯音测听检查。'
  },
  {
    maxScore: 4,
    text: '双耳正常',
    color: '#22c55e',
    bg: '#f0fdf4',
    suggest: '耳语试验双耳正常，无明显异常。'
  }
]

/** 未完成评估默认状态 */
const PENDING_LEVEL = {
  text: '待评估',
  color: '#94a3b8',
  bg: '#f8fafc',
  suggest: '请完成全部评估题目以生成建议。'
}

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
/** 量表完整数据 */
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
/** 用户答案集合：key为题目ID，value为选项ID数组 */
const answers = ref({})
/** 提交加载状态，防止重复提交 */
const submitting = ref(false)

// ==================== 计算属性 - 路由参数 ====================
/** 患者ID（响应式获取，保证路由参数变化时同步更新） */
const patientId = computed(() => route.query.patientId || '')
/** 患者姓名 */
const patientName = computed(() => route.query.patientName || '患者')
/** 目标量表ID（优先取路由参数，兜底默认值） */
const targetScaleId = computed(() => {
  return toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || DEFAULT_SCALE_ID
})

// ==================== 计算属性 - 进度统计 ====================
/** 题目总数 */
const totalQuestions = computed(() => scaleData.value.questionList?.length || 0)
/** 已作答题目数量 */
const answeredCount = computed(() => Object.keys(answers.value).length)
/** 完成进度百分比 */
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ==================== 计算属性 - 得分计算 ====================
/**
 * 听力简易评估得分
 * 取 sort=1 的第1题得分，满分4分
 */
const hearingScore = computed(() => {
  const hearingQuestion = scaleData.value.questionList.find(q => q.sort === 1)
  if (!hearingQuestion) return 0
  const optionId = answers.value[hearingQuestion.questionId]
  if (!optionId) return 0
  const option = hearingQuestion.optionList.find(o => o.id == optionId)
  return Number(option?.score) || 0
})

/**
 * 耳语试验得分
 * 取 sort≥2 的第2-3题得分总和，每耳2分，满分4分
 */
const whisperTestScore = computed(() => {
  let sum = 0
  scaleData.value.questionList.forEach(q => {
    if (q.sort >= 2) {
      const optionId = answers.value[q.questionId]
      if (optionId) {
        const option = q.optionList.find(o => o.id == optionId)
        sum += Number(option?.score) || 0
      }
    }
  })
  return sum
})

// ==================== 计算属性 - 等级与建议 ====================
/** 听力简易评估等级 */
const hearingLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value) return PENDING_LEVEL
  // 倒序匹配，找到第一个得分满足条件的等级
  for (let i = HEARING_LEVEL_CONFIG.length - 1; i >= 0; i--) {
    if (hearingScore.value >= HEARING_LEVEL_CONFIG[i].maxScore) {
      return HEARING_LEVEL_CONFIG[i]
    }
  }
  return HEARING_LEVEL_CONFIG[0]
})

/** 耳语试验等级 */
const whisperTestLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value) return PENDING_LEVEL
  for (let i = WHISPER_TEST_LEVEL_CONFIG.length - 1; i >= 0; i--) {
    if (whisperTestScore.value >= WHISPER_TEST_LEVEL_CONFIG[i].maxScore) {
      return WHISPER_TEST_LEVEL_CONFIG[i]
    }
  }
  return WHISPER_TEST_LEVEL_CONFIG[0]
})

/** 听力简易评估建议 */
const hearingSuggest = computed(() => hearingLevel.value.suggest)
/** 耳语试验建议 */
const whisperTestSuggest = computed(() => whisperTestLevel.value.suggest)

// ==================== 计算属性 - 异常项与提交数据 ====================
/** 检出异常项：所选选项得分低于该题最高分的项目 */
const abnormalAnswers = computed(() => {
  const list = []
  for (const q of scaleData.value.questionList || []) {
    const optionId = answers.value[q.questionId]
    if (!optionId) continue
    const option = q.optionList.find(o => o.id == optionId)
    if (!option) continue
    const maxScore = getMaxScoreOf(q)
    if (Number(option.score) < maxScore) {
      list.push({ question: q, option })
    }
  }
  return list
})

/** 综合评估结果文本 */
const combinedResult = computed(() =>
    `听力简易评估：${hearingLevel.value.text}；耳语试验：${whisperTestLevel.value.text}；检出异常项目 ${abnormalAnswers.value.length} 项`
)

/** 综合评估建议文本 */
const combinedSuggest = computed(() => {
  return `${hearingSuggest.value} ${whisperTestSuggest.value}`
})

// ==================== 工具函数 ====================
/**
 * 从多个候选值中提取有效的数字ID
 * @param  {...any} values 候选值
 * @returns {number|null} 有效的数字ID，无效则返回null
 */
function toNumberId(...values) {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}

/**
 * 获取单题最高分
 * @param {object} question 题目对象
 * @returns {number} 该题最高分
 */
function getMaxScoreOf(question) {
  if (!question || !question.optionList) return 0
  return Math.max(...question.optionList.map(o => Number(o.score) || 0))
}

/**
 * 判断选项是否为该题最高分
 * @param {number|string} questionId 题目ID
 * @param {number} score 选项得分
 * @returns {boolean}
 */
function isMaxScore(questionId, score) {
  const question = scaleData.value.questionList.find(q => q.questionId === questionId)
  if (!question) return false
  const maxScore = getMaxScoreOf(question)
  return score >= maxScore
}

// ==================== 业务方法 ====================
/**
 * 加载量表题目数据
 */
async function loadScaleData() {
  try {
    const res = await selectListeningScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    console.error('加载听力评估量表失败：', error)
    ElMessage.error('加载量表数据失败')
  }
}

/**
 * 选中题目选项
 * @param {number|string} questionId 题目ID
 * @param {object} option 选项对象
 */
function selectOption(questionId, option) {
  answers.value[questionId] = [option.id]
}

/**
 * 返回上一页
 */
function goBack() {
  router.push({ path: '/patient/detection/customize', query: route.query })
}

/**
 * 返回综合评估首页，并写入本地完成标记
 */
function backToComprehensive() {
  try {
    if (patientId.value) {
      localStorage.setItem(`${STORAGE_PREFIX}${patientId.value}:hearing_assessment`, '1')
    }
  } catch (e) {
    console.warn('本地存储写入失败：', e)
  }
  router.push({
    path: '/patient/detection/customize',
    query: {
      patientId: patientId.value,
      patientName: patientName.value
    }
  })
}

/**
 * 提交评估结果
 */
async function submitAssessment() {
  if (submitting.value) return
  if (answeredCount.value < totalQuestions.value) {
    ElMessage.warning(`还有 ${totalQuestions.value - answeredCount.value} 道题未作答`)
    return
  }

  submitting.value = true
  try {
    // 格式化答案为后端要求结构
    const formattedAnswers = {}
    for (const questionId in answers.value) {
      const optionId = answers.value[questionId]
      formattedAnswers[String(questionId)] = [Number(optionId)]
    }

    const submitDto = {
      patientId: Number(patientId.value),
      projectId: targetScaleId.value,
      scaleId: targetScaleId.value,
      answers: formattedAnswers,
      result: combinedResult.value,
      suggest: combinedSuggest.value
    }

    const res = await insertCgaRecord(submitDto)
    if (res && (res.code === 200 || res.code === '200')) {
      ElMessage.success('评估提交成功')
      setTimeout(() => {
        backToComprehensive()
      }, 1500)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交评估失败：', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
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
  background: #f0f7ff;
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
    color: #8b5cf6;
    background: #ede9fe;
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
  background: #8b5cf6;
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
  background: linear-gradient(90deg, #8b5cf6, #6366f1);
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

  &.is-answered {
    border-color: #a78bfa;
  }
}

.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fafafa;
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
  background: #8b5cf6;
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
    background: #ede9fe;
    border-color: #c4b5fd;
  }

  &.is-selected {
    background: #ede9fe;
    border-color: #8b5cf6;
    box-shadow: 0 2px 8px rgba(139, 92, 246, 0.12);
  }

  &.is-max .opt-score {
    color: #16a34a;
  }
}

.opt-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.opt-radio {
  font-size: 20px;
  color: #cbd5e1;
  flex-shrink: 0;
  transition: color 0.15s;
  margin-top: 1px;

  &.is-checked {
    color: #8b5cf6;
  }
}

.opt-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.opt-label {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
  font-weight: 500;
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

/* 结果区 */
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
  color: #7c3aed;
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
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 检出问题卡片 */
.findings-card {
  border-left: 4px solid #f97316;
}

.no-finding {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 4px;
  color: #16a34a;
}

.ok-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0fdf4;
  color: #16a34a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.ok-text {
  font-size: 14px;
  line-height: 1.6;
}

.finding-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finding-row {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 10px;
}

.finding-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f97316;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.finding-content {
  flex: 1;
}

.finding-title {
  font-size: 14px;
  font-weight: 600;
  color: #9a3412;
  margin-bottom: 4px;
  line-height: 1.5;
}

.finding-option {
  font-size: 13px;
  color: #78350f;
  line-height: 1.6;
}

.finding-label {
  color: #a16207;
}

.finding-value {
  font-weight: 600;
}

.finding-score {
  color: #c2410c;
  font-weight: 700;
  font-family: Consolas, monospace;
}

/* 建议卡片 */
.combined-suggest {
  grid-column: 1 / -1;
  border-left: 4px solid #8b5cf6;
}

.suggest-text {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.9;
}

.suggest-sub {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e5e7eb;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}

/* 提交区 */
.submit-area {
  text-align: center;
  padding: 8px 0;
}

.submit-area .el-button--primary {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  border: none;
  padding: 14px 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 14px rgba(139, 92, 246, 0.3);

  &:hover {
    box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
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

/* 响应式适配 */
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
}
</style>
