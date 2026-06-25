<template>
  <div class="sarc-f-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>SARC-F 肌少症筛查</h1>
          <p>Strength · Assistance · Rise · Climb · Falls · 共 {{ totalQuestions }} 项</p>
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

    <!-- 评估题目列表 -->
    <section class="question-list">
      <div
          v-for="(question, qIndex) in scaleData?.questionList || []"
          :key="question.questionId"
          class="question-card"
          :class="{ 'is-answered': answers[question.questionId]?.length > 0 }"
      >
        <div class="q-head">
          <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
          <span class="q-title">{{ question.title }}</span>
          <span v-if="answers[question.questionId]?.length > 0" class="q-done">
            <el-icon><Check /></el-icon>
          </span>
        </div>
        <div class="q-options">
          <div
              v-for="option in question.optionList"
              :key="option.optionId"
              class="q-option"
              :class="{
              'is-selected': answers[question.questionId]?.includes(option.optionId),
              'is-max': isMaxScore(question.questionId, option.score)
            }"
              @click="selectOption(question.questionId, option)"
          >
            <div class="opt-left">
              <div class="opt-radio" :class="{ 'is-checked': answers[question.questionId]?.includes(option.optionId) }">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="opt-content">
                <span class="opt-label">{{ option.label }}. {{ option.content }}</span>
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
      <!-- 总分卡片 -->
      <div class="result-card score-card">
        <div class="rc-head">
          <span class="rc-icon">💪</span>
          <span>SARC-F 评估 · 得分</span>
        </div>
        <div class="rc-body">
          <span class="score-big">{{ totalScore }}</span>
          <span class="score-unit">/{{ scaleData?.totalScore || 10 }}分</span>
        </div>
        <div class="rc-footer">
          <span
              class="level-tag"
              :style="{
              background: sarcLevel.bg,
              color: sarcLevel.color,
              borderColor: sarcLevel.color
            }"
          >
            {{ sarcLevel.text }}
          </span>
        </div>
      </div>

      <!-- 检出问题卡片 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠️</span>
          <span>功能受损维度 · 共 {{ abnormalAnswers.length }} 项</span>
        </div>
        <div v-if="abnormalAnswers.length === 0" class="no-finding">
          <el-icon class="ok-icon"><Check /></el-icon>
          <div class="ok-text">未检出明显肌少症风险，躯体功能状态良好。</div>
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
                <span class="finding-label">评估结果：</span>
                <span class="finding-value">{{ item.option.content }}</span>
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
        <p class="suggest-text">{{ sarcSuggest }}</p>
        <div v-if="sarcLevel.text !== '低风险'" class="suggest-sub">
          建议进一步进行肌肉力量、躯体功能和肌肉量评估，明确诊断并制定个体化运动与营养干预方案。
        </div>
      </div>
    </section>

    <!-- 提交按钮区 -->
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

<script setup name="SSarcFAssessment">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectSVOById, insertSRecord } from '@/api/s.js'

// ==================== 常量配置 ====================
/** 本地存储完成状态前缀（肌少症自定义评估） */
const STORAGE_PREFIX = 'sarcopenia_customize_done:'
/** 默认量表ID - SARC-F */
const DEFAULT_SCALE_ID = 1

/** 未完成评估默认状态 */
const PENDING_LEVEL = {
  text: '待评估',
  color: '#94a3b8',
  bg: '#f8fafc',
  suggest: '请完成全部评估题目以生成建议。'
}

/**
 * SARC-F 肌少症风险等级配置
 * 标准：0-3分低风险，4-6分中度风险，≥7分高度风险
 */
const SARC_LEVEL_CONFIG = [
  {
    minScore: 7,
    text: '高度风险',
    color: '#dc2626',
    bg: '#fef2f2',
    suggest: 'SARC-F评分提示高度肌少症风险，建议尽快进行肌肉力量、躯体功能和肌量的专业评估，明确诊断后制定抗阻运动、营养补充等综合干预方案。'
  },
  {
    minScore: 4,
    text: '中度风险',
    color: '#f97316',
    bg: '#fff7ed',
    suggest: 'SARC-F评分提示中度肌少症风险，存在部分躯体功能下降。建议加强抗阻运动训练，增加蛋白质摄入，定期监测肌力和躯体功能变化。'
  },
  {
    minScore: 0,
    text: '低风险',
    color: '#16a34a',
    bg: '#f0fdf4',
    suggest: 'SARC-F评分提示肌少症低风险，躯体功能状态良好。建议保持规律运动和均衡饮食，定期进行健康体检和功能监测。'
  }
]

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
/** 量表完整数据 */
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
/** 用户答案集合：key为题目ID，value为选中的选项ID数组（单选题固定长度为1） */
const answers = ref({})
/** 提交加载状态，防止重复提交 */
const submitting = ref(false)

// ==================== 计算属性 - 路由参数 ====================
/** 患者ID（响应式获取，保证路由参数变化时同步更新） */
const patientId = computed(() => route.query.patientId || '')
/** 患者姓名 */
const patientName = computed(() => route.query.patientName || route.query.name || '患者')
/** 目标量表ID（优先取路由参数，兜底默认值） */
const targetScaleId = computed(() => {
  return toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || DEFAULT_SCALE_ID
})

// ==================== 计算属性 - 进度统计 ====================
/** 题目总数 */
const totalQuestions = computed(() => scaleData.value.questionList?.length || 0)
/** 已作答题目数量 */
const answeredCount = computed(() => {
  return Object.values(answers.value).filter(arr => arr.length > 0).length
})
/** 完成进度百分比 */
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ==================== 计算属性 - 得分计算 ====================
/** 评估总分，累加所有已答题目选项得分 */
const totalScore = computed(() => {
  let sum = 0
  for (const question of scaleData.value.questionList || []) {
    const selectedIds = answers.value[question.questionId]
    if (!selectedIds?.length) continue
    const option = question.optionList.find(o => o.optionId === selectedIds[0])
    sum += Number(option?.score) || 0
  }
  return sum
})

// ==================== 计算属性 - 等级判定 ====================
/** 当前肌少症风险等级 */
const sarcLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value) return PENDING_LEVEL
  const score = totalScore.value
  for (const level of SARC_LEVEL_CONFIG) {
    if (score >= level.minScore) return level
  }
  return SARC_LEVEL_CONFIG[SARC_LEVEL_CONFIG.length - 1]
})

// ==================== 计算属性 - 异常项与建议 ====================
/**
 * 检出异常项列表
 * 判定逻辑：选项得分 >= 2 分视为存在功能受损
 */
const abnormalAnswers = computed(() => {
  const list = []
  for (const question of scaleData.value.questionList || []) {
    const selectedIds = answers.value[question.questionId]
    if (!selectedIds?.length) continue
    const option = question.optionList.find(o => o.optionId === selectedIds[0])
    if (!option) continue
    if (Number(option.score) >= 2) {
      list.push({ question, option })
    }
  }
  return list
})

/** 评估主建议 */
const sarcSuggest = computed(() => sarcLevel.value.suggest)

// ==================== 计算属性 - 提交数据拼接 ====================
/** 综合评估结果文本，存入后端记录 */
const combinedResult = computed(() => {
  return `SARC-F总分：${totalScore.value}分，${sarcLevel.value.text}`
})

/** 综合评估建议文本，包含异常项明细 */
const combinedSuggest = computed(() => {
  const extra = abnormalAnswers.value.map((item, idx) => {
    return `${idx + 1}. ${item.question.title}：${item.option.content}（${item.option.score}分）`
  }).join('；')
  const scoreText = `SARC-F量表得分${totalScore.value}/${scaleData.value.totalScore || 10}分，${sarcLevel.value.text}。`
  return `${scoreText}${sarcSuggest.value}${extra ? ' 功能受损维度：' + extra : ''}`
})

// ==================== 工具函数 ====================
/**
 * 从多个候选值中提取有效的数字ID
 */
function toNumberId(...values) {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}

/**
 * 判断选项是否为对应题目的最高分
 */
function isMaxScore(questionId, score) {
  const question = scaleData.value.questionList.find(q => q.questionId === questionId)
  if (!question) return false
  const maxScore = Math.max(...question.optionList.map(o => Number(o.score) || 0))
  return score >= maxScore
}

// ==================== 业务方法 ====================
/**
 * 加载量表题目数据
 */
async function loadScaleData() {
  try {
    const res = await selectSVOById(targetScaleId.value)
    // 后端返回 AjaxResult 包装，数据在 data 中
    if (res && res.code === 200 && res.data && res.data.scaleId) {
      scaleData.value = res.data
    } else if (res && res.scaleId) {
      // 兼容直接返回对象的情况
      scaleData.value = res
    }
  } catch (error) {
    console.error('加载SARC-F量表失败：', error)
    ElMessage.error('加载量表数据失败')
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}

/**
 * 选中题目选项（单选题，直接覆盖）
 */
function selectOption(questionId, option) {
  answers.value[questionId] = [option.optionId]
}

/**
 * 返回上一页
 */
function goBack() {
  const backPath = route.query.from === 'sCustomize'
    ? '/patient/detection/sCustomize'
    : '/patient/detection/customize'
  router.push({ path: backPath, query: route.query })
}

/**
 * 返回自定义评估页，并写入本地完成标记
 */
function backToCustomize() {
  try {
    if (patientId.value) {
      localStorage.setItem(`${STORAGE_PREFIX}${patientId.value}:sarc_f`, '1')
    }
  } catch (e) {
    console.warn('本地存储写入失败：', e)
  }
  const backPath = route.query.from === 'sCustomize'
    ? '/patient/detection/sCustomize'
    : '/patient/detection/customize'
  router.push({
    path: backPath,
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
    ElMessage.warning('请完成全部评估题目后再提交')
    return
  }

  submitting.value = true
  try {
    // 格式化答案为后端要求结构
    const formattedAnswers = {}
    for (const questionId in answers.value) {
      formattedAnswers[String(questionId)] = answers.value[questionId].map(id => Number(id))
    }

    const submitDto = {
      patientId: Number(patientId.value),
      projectId: targetScaleId.value,
      scaleId: targetScaleId.value,
      answers: formattedAnswers,
      result: combinedResult.value,
      suggest: combinedSuggest.value
    }

    const res = await insertSRecord(submitDto)
    if (res && (res.code === 200 || res.code === '200')) {
      ElMessage.success('评估提交成功')
      setTimeout(() => {
        backToCustomize()
      }, 1500)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交SARC-F评估失败：', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// ==================== 生命周期与监听 ====================
watch(() => route.fullPath, () => {
  if (!scaleData.value) {
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}, { immediate: true })

onMounted(() => {
  loadScaleData()
})
</script>

<style scoped lang="scss">
.sarc-f-container {
  min-height: 100vh;
  padding: 24px 28px 48px;
  background: #fff8f0;
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
    color: #ea580c;
    background: #fff7ed;
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
  font-size: 12px;
  color: #9ca3af;
  letter-spacing: 0.5px;
}

.patient-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background: #fff7ed;
  border-radius: 10px;
}

.badge-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}

.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.badge-id {
  font-size: 11.5px;
  color: #9ca3af;
  margin-top: 2px;
}

/* 进度条 */
.progress-bar {
  position: relative;
  height: 36px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #f97316, #f59e0b);
  transition: width 0.3s ease;
}

.progress-label {
  position: relative;
  z-index: 1;
  display: block;
  line-height: 36px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

/* 题目列表 */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 24px;
}

.question-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &.is-answered {
    border-color: #fdba74;
    box-shadow: 0 2px 10px rgba(249, 115, 22, 0.08);
  }
}

.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.q-index {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #fff7ed;
  color: #ea580c;
  font-size: 14px;
  font-weight: 700;
}

.q-title {
  flex: 1;
  font-size: 15.5px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
}

.q-done {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #dcfce7;
  color: #16a34a;
}

/* 选项列表 */
.q-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.q-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover {
    border-color: #fdba74;
    background: #fff7ed;
  }

  &.is-selected {
    border-color: #f97316;
    background: #fff7ed;
  }

  &.is-max {
    border-color: #fca5a5;
  }

  &.is-selected.is-max {
    border-color: #dc2626;
    background: #fef2f2;
  }
}

.opt-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.opt-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.18s ease;
  color: transparent;

  &.is-checked {
    border-color: #f97316;
    background: #f97316;
    color: #fff;
  }
}

.opt-content {
  flex: 1;
  min-width: 0;
}

.opt-label {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.5;
}

.opt-right {
  display: flex;
  align-items: baseline;
  gap: 2px;
  flex-shrink: 0;
  margin-left: 12px;
}

.opt-score {
  font-size: 18px;
  font-weight: 700;
  color: #6b7280;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.q-option.is-selected .opt-score {
  color: #ea580c;
}

.opt-unit {
  font-size: 12px;
  color: #9ca3af;
}

/* 结果区域 */
.result-area {
  display: grid;
  gap: 14px;
  margin-bottom: 24px;
}

.result-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.rc-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.rc-icon {
  font-size: 18px;
}

/* 得分卡片 */
.score-card .rc-body {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 14px;
}

.score-big {
  font-size: 48px;
  font-weight: 800;
  color: #ea580c;
  line-height: 1;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.score-unit {
  font-size: 16px;
  color: #9ca3af;
  font-weight: 600;
}

.score-card .rc-footer {
  padding-top: 14px;
  border-top: 1px solid #f3f4f6;
}

.level-tag {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 999px;
  border: 1px solid;
  font-size: 13px;
  font-weight: 700;
}

/* 检出问题卡片 */
.no-finding {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 0;
  color: #6b7280;
}

.ok-icon {
  font-size: 36px;
  color: #22c55e;
}

.ok-text {
  font-size: 14px;
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
  border-radius: 10px;
}

.finding-index {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: #fed7aa;
  color: #c2410c;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.finding-content {
  flex: 1;
  min-width: 0;
}

.finding-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.finding-option {
  font-size: 13px;
  color: #6b7280;
}

.finding-label {
  color: #9ca3af;
}

.finding-value {
  color: #ea580c;
  font-weight: 500;
}

/* 建议卡片 */
.suggest-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
}

.suggest-sub {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.7;
}

/* 提交区域 */
.submit-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 10px 0 20px;
}

.submit-area .el-button {
  min-width: 220px;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.3);

  &:hover {
    box-shadow: 0 6px 20px rgba(249, 115, 22, 0.4);
  }
}

.submit-hint {
  margin: 0;
  font-size: 12.5px;
  color: #9ca3af;
}

/* 响应式 */
@media (max-width: 768px) {
  .sarc-f-container {
    padding: 16px 14px 40px;
  }

  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .title-block h1 {
    font-size: 18px;
  }

  .question-card {
    padding: 16px;
  }

  .q-title {
    font-size: 14.5px;
  }

  .score-big {
    font-size: 40px;
  }

  .submit-area .el-button {
    min-width: 100%;
  }
}
</style>
