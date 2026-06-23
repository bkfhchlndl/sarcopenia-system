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
          <h1>国际尿失禁咨询委员会问卷（简表）</h1>
          <p>尿失禁筛查 · 共 {{ totalQuestions }} 项</p>
        </div>
      </div>
      <div class="top-right">
        <div class="patient-badge">
          <span class="badge-avatar">{{ (patientName || '患').slice(0, 1) }}</span>
          <div>
            <div class="badge-name">{{ patientName }}</div>
            <div class="badge-id">{{ patientId ? '#' + patientId : '' }}</div>
          </div>
        </div>
      </div>
    </header>

    <!-- 进度条 -->
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">已完成 {{ answeredCount }} / {{ totalQuestions }} 项</span>
    </div>

    <!-- 题目卡片列表 -->
    <section class="question-list">
      <div
          v-for="(question, qIndex) in scaleData?.questionList || []"
          :key="question.questionId"
          class="question-card"
          :class="{
          'is-answered': answers[question.questionId]?.length > 0,
          'is-horizontal': question.sort === 3
        }"
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
              :key="option.id"
              class="q-option"
              :class="{
              'is-selected': answers[question.questionId]?.includes(option.id)
            }"
              @click="toggleOption(question, option)"
          >
            <div class="opt-left">
              <div
                  :class="[
                  question.type === 1 ? 'opt-radio' : 'opt-checkbox',
                  { 'is-checked': answers[question.questionId]?.includes(option.id) }
                ]"
              >
                <el-icon><component :is="question.type === 1 ? CircleCheck : Check" /></el-icon>
              </div>
              <div class="opt-content">
                <span v-if="question.sort === 3" class="opt-label">{{ q3DisplayScore(question, option.score) }}</span>
                <span v-else class="opt-label">{{ option.label }}. {{ option.content }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 结果展示区 -->
    <section class="result-area">
      <!-- 漏尿类型卡片 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠️</span>
          <span>漏尿类型 · 共 {{ selectedTypes.length }} 项</span>
        </div>
        <div v-if="selectedTypes.length === 0" class="no-finding">
          <el-icon class="ok-icon"><Check /></el-icon>
          <div class="ok-text">未勾选任何漏尿情况</div>
        </div>
        <div v-else class="finding-list">
          <div v-for="(item, idx) in selectedTypes" :key="item.id" class="finding-row">
            <div class="finding-index">{{ idx + 1 }}</div>
            <div class="finding-content">
              <div class="finding-title">{{ item.content }}</div>
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
        <p class="suggest-text">{{ incontinenceSuggest }}</p>
        <div v-if="incontinenceLevel.text !== '无尿失禁'" class="suggest-sub">
          建议到泌尿外科或妇科就诊，进行尿流动力学等详细检查，明确尿失禁类型并制定针对性治疗方案。
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

<script setup name="UrinaryIncontinenceAssessment">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectUrinaryIncontinenceScale, insertCgaRecord } from '@/api/cga.js'

// ==================== 常量配置 ====================
/** 本地存储完成状态前缀 */
const STORAGE_PREFIX = 'customize_done:'
/** 默认量表ID */
const DEFAULT_SCALE_ID = 30

/** 未完成评估默认状态 */
const PENDING_LEVEL = {
  text: '待评估',
  color: '#94a3b8',
  bg: '#f8fafc',
  suggest: '请完成全部评估题目以生成建议。'
}

/**
 * 尿失禁严重程度等级配置
 * 评分规则：得分越高代表尿失禁程度越重
 */
const INCONTINENCE_LEVEL_CONFIG = [
  {
    maxScore: 0,
    text: '无尿失禁',
    color: '#22c55e',
    bg: '#f0fdf4',
    suggest: '泌尿系统功能良好。建议保持良好的排尿习惯，避免憋尿，定期进行泌尿系统检查。'
  },
  {
    maxScore: 5,
    text: '轻度尿失禁',
    color: '#3b82f6',
    bg: '#eff6ff',
    suggest: '对日常生活影响较小。建议进行盆底肌训练（凯格尔运动），控制体重，避免增加腹压的动作。'
  },
  {
    maxScore: 12,
    text: '中度尿失禁',
    color: '#f97316',
    bg: '#fff7ed',
    suggest: '已对日常生活产生一定影响。建议在医生指导下进行盆底肌康复治疗，必要时使用药物干预。'
  },
  {
    maxScore: Infinity,
    text: '重度尿失禁',
    color: '#ef4444',
    bg: '#fef2f2',
    suggest: '严重影响日常生活和社交活动。建议尽快到专科就诊，评估手术治疗的可行性。'
  }
]

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
/** 量表完整数据 */
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })
/** 用户答案集合：key为题目ID，value为选中的选项ID数组 */
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
const answeredCount = computed(() => {
  return Object.values(answers.value).filter(arr => arr.length > 0).length
})
/** 完成进度百分比 */
const progressPercent = computed(() => {
  const total = totalQuestions.value || 1
  return Math.round((answeredCount.value / total) * 100)
})

// ==================== 计算属性 - 得分计算 ====================
/**
 * 评估总分
 * 特殊规则：第4题为漏尿类型题，不计入总分；第3题为倒序计分
 */
const totalScore = computed(() => {
  let sum = 0
  for (const question of scaleData.value.questionList || []) {
    // 第4题是类型多选题，不计入总分
    if (question.sort === 4) continue

    const selectedIds = answers.value[question.questionId] || []
    selectedIds.forEach(id => {
      const option = question.optionList.find(item => item.id === id)
      if (!option) return
      const rawScore = Number(option.score) || 0

      // 第3题倒序计分：原始分越高，实际得分越低
      if (question.sort === 3) {
        const scoreList = question.optionList.map(item => Number(item.score) || 0)
        const maxScore = Math.max(...scoreList)
        const minScore = Math.min(...scoreList)
        sum += (maxScore + minScore - rawScore)
      } else {
        sum += rawScore
      }
    })
  }
  return sum
})

// ==================== 计算属性 - 等级判定 ====================
/** 当前尿失禁严重程度等级 */
const incontinenceLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value) return PENDING_LEVEL

  const score = totalScore.value
  // 倒序匹配等级，从最轻到最重
  for (let i = INCONTINENCE_LEVEL_CONFIG.length - 1; i >= 0; i--) {
    if (score <= INCONTINENCE_LEVEL_CONFIG[i].maxScore) {
      return INCONTINENCE_LEVEL_CONFIG[i]
    }
  }
  return INCONTINENCE_LEVEL_CONFIG.at(-1)
})

// ==================== 计算属性 - 漏尿类型 ====================
/** 选中的漏尿类型（第4题作答结果） */
const selectedTypes = computed(() => {
  const typeQuestion = scaleData.value.questionList.find(q => q.sort === 4)
  if (!typeQuestion) return []
  const selectedIds = answers.value[typeQuestion.questionId] || []
  return typeQuestion.optionList.filter(option => selectedIds.includes(option.id))
})

// ==================== 计算属性 - 评估建议 ====================
/** 主评估建议 */
const incontinenceSuggest = computed(() => incontinenceLevel.value.suggest)

// ==================== 计算属性 - 提交数据 ====================
/** 综合评估结果文本，存入后端记录 */
const combinedResult = computed(() => {
  return `${incontinenceLevel.value.text}`
})

/** 综合评估建议文本 */
const combinedSuggest = computed(() => {
  const extraTip = incontinenceLevel.value.text !== '无尿失禁' ? ' 建议完善专科检查明确诊断。' : ''
  return `${incontinenceSuggest.value}${extraTip}`
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
 * 第三题显示分数转换
 * 原始分为倒序，转换为正序数字展示
 * @param {object} question 题目对象
 * @param {number|string} rawScore 原始分数
 * @returns {number} 转换后的显示分数
 */
function q3DisplayScore(question, rawScore) {
  if (question.sort !== 3) return Number(rawScore) || 0
  const scoreList = question.optionList.map(item => Number(item.score) || 0)
  const maxScore = Math.max(...scoreList)
  const minScore = Math.min(...scoreList)
  return maxScore + minScore - (Number(rawScore) || 0)
}

// ==================== 业务方法 ====================
/**
 * 加载量表题目数据
 */
async function loadScaleData() {
  try {
    const res = await selectUrinaryIncontinenceScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
    }
  } catch (error) {
    console.error('加载尿失禁量表失败：', error)
    ElMessage.error('加载量表数据失败')
    // 接口异常时重置为默认空值，避免渲染报错
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}

/**
 * 切换选项选中状态
 * 特殊规则：第4题A选项与其他选项互斥
 * @param {object} question 题目对象
 * @param {object} option 选项对象
 */
function toggleOption(question, option) {
  if (!answers.value[question.questionId]) {
    answers.value[question.questionId] = []
  }
  const selectedList = answers.value[question.questionId]

  // 单选题直接覆盖
  if (question.type === 1) {
    answers.value[question.questionId] = [option.id]
    return
  }

  const index = selectedList.indexOf(option.id)
  // 取消选中
  if (index !== -1) {
    selectedList.splice(index, 1)
    return
  }

  // 选中操作：第4题A选项与其他选项互斥
  if (question.sort === 4) {
    const isOptionA = String(option.label).toUpperCase() === 'A'
    if (isOptionA) {
      // 选A则清空其他已选项
      answers.value[question.questionId] = [option.id]
    } else {
      // 选其他则移除A选项
      const optionA = question.optionList.find(item => String(item.label).toUpperCase() === 'A')
      const filtered = selectedList.filter(id => !optionA || id !== optionA.id)
      filtered.push(option.id)
      answers.value[question.questionId] = filtered
    }
  } else {
    selectedList.push(option.id)
  }
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
      localStorage.setItem(`${STORAGE_PREFIX}${patientId.value}:urinary_incontinence`, '1')
    }
  } catch (error) {
    console.warn('本地存储写入失败：', error)
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
    console.error('提交尿失禁评估失败：', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// ==================== 生命周期与监听 ====================
// 修复KeepAlive缓存导致scaleData变为undefined的问题，路由变化时重置默认值
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

/* 第三题横向排列样式 */
.question-card.is-horizontal .q-options {
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-around;
}

.question-card.is-horizontal .q-option {
  flex: 1;
  min-width: 60px;
  max-width: 80px;
  justify-content: center;
}

.question-card.is-horizontal .opt-left {
  justify-content: center;
}

.question-card.is-horizontal .opt-content span {
  font-size: 16px;
  font-weight: bold;
  color: #7c3aed;
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
}

.opt-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.opt-radio,
.opt-checkbox {
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
