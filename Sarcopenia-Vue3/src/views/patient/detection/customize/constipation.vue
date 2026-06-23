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
          <h1>便秘症状评估</h1>
          <p>便秘筛查量表 · 共 {{ totalQuestions }} 项</p>
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

        <!-- 选择题（单选 / 多选）—— 持续时间题用专用UI -->
        <div v-if="isChoiceQuestion(question) && !isDurationQuestion(question)" class="q-options">
          <div
              v-for="option in question.optionList"
              :key="option.id"
              class="q-option"
              :class="{ 'is-selected': answers[question.questionId]?.includes(option.id) }"
              @click="toggleOption(question, option)"
          >
            <div class="opt-left">
              <div
                  :class="[
                  (question.type === 1 || !question.type) ? 'opt-radio' : 'opt-checkbox',
                  { 'is-checked': answers[question.questionId]?.includes(option.id) }
                ]"
              >
                <el-icon><Component :is="(question.type === 1 || !question.type) ? CircleCheck : Check" /></el-icon>
              </div>
              <div class="opt-content">
                <span class="opt-label">{{ option.label }}. {{ option.content }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 填空题 -->
        <div v-if="isFillQuestion(question)" class="q-fill">
          <div class="fill-row">
            <span class="fill-label">{{ question.inputLabel || '请输入：' }}</span>
            <el-input
                v-model="fillAnswers[question.questionId]"
                :placeholder="question.inputPlaceholder || '请输入'"
                :maxlength="question.inputMaxlength || 20"
                class="fill-input"
                clearable
            />
            <span v-if="question.inputUnit" class="fill-unit">{{ question.inputUnit }}</span>
          </div>
        </div>

        <!-- 日期题 -->
        <div v-if="isDateQuestion(question)" class="q-fill">
          <div class="fill-row date-row">
            <span class="fill-label">{{ question.dateLabel || '选择日期：' }}</span>
            <el-date-picker
                v-model="dateAnswers[question.questionId].date"
                :type="question.dateType || 'month'"
                :placeholder="question.datePlaceholder || '选择年月'"
                :disabled-date="disabledFutureDate"
                :clearable="true"
                :format="question.dateType === 'date' ? 'YYYY 年 MM 月 DD 日' : 'YYYY 年 MM 月'"
                :value-format="question.dateType === 'date' ? 'YYYY-MM-DD' : 'YYYY-MM'"
                class="date-input"
                :editable="false"
            />
          </div>
        </div>

        <!-- 持续时间题（年月下拉 + 不知道互斥） -->
        <div v-if="isDurationQuestion(question)" class="q-fill">
          <div class="fill-row duration-row">
            <span class="fill-label">持续时间：</span>
            <el-select
                v-model="durationAnswers[question.questionId].years"
                placeholder="年"
                class="dur-select"
                clearable
                :disabled="durationAnswers[question.questionId]?.unknown"
                @change="durationAnswers[question.questionId].unknown = false"
            >
              <el-option v-for="y in 51" :key="y" :label="String(y - 1) + ' 年'" :value="y - 1" />
            </el-select>
            <el-select
                v-model="durationAnswers[question.questionId].months"
                placeholder="月"
                class="dur-select"
                clearable
                :disabled="durationAnswers[question.questionId]?.unknown"
                @change="durationAnswers[question.questionId].unknown = false"
            >
              <el-option v-for="m in 12" :key="m" :label="String(m - 1) + ' 个月'" :value="m - 1" />
            </el-select>
            <div
                class="unknown-option"
                :class="{ 'is-selected': durationAnswers[question.questionId]?.unknown }"
                @click="toggleDurationUnknown(question)"
            >
              不知道
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 4. 结果展示区 ===== -->
    <section class="result-area">
      <!-- 评估结果卡片 -->
      <div class="result-card findings-card">
        <div class="rc-head">
          <span class="rc-icon">⚠</span>
          <span>评估结果 · {{ constipationLevel.text }}</span>
        </div>
        <div class="level-display">
          <div class="level-tag" :style="{ background: constipationLevel.bg, color: constipationLevel.color }">
            {{ constipationLevel.text }}
          </div>
        </div>
        <!-- 检出项展示 -->
        <div v-if="answerSummary.length > 0" class="abnormal-section">
          <div class="abnormal-title">评估详情 · 共 {{ answerSummary.length }} 项</div>
          <div v-for="(item, idx) in answerSummary" :key="idx" class="abnormal-row"
               :class="{ 'is-abnormal': item.isAbnormal, 'is-normal': !item.isAbnormal }">
            <div class="abnormal-content">
              <div class="abnormal-a">{{ item.displayText }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="totalQuestions > 0" class="no-finding">
          <div class="hint-text">完成全部题目后展示评估详情</div>
        </div>
      </div>

      <!-- 综合建议卡片 -->
      <div class="result-card suggest-card">
        <div class="rc-head">
          <span class="rc-icon">💡</span>
          <span>评估建议</span>
        </div>
        <p class="suggest-text">{{ constipationSuggest }}</p>
        <div v-if="constipationLevel.text === '存在便秘症状'" class="suggest-sub">
          若症状持续无改善或进行性加重，建议及时前往消化内科专科就诊，完善相关检查明确诊断。
        </div>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { selectConstipationScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'

// 数字ID格式化
const toNumberId = (...values) => {
  const value = values.find(v => v !== undefined && v !== null && v !== '' && Number.isFinite(Number(v)))
  return value === undefined ? null : Number(value)
}
const targetScaleId = toNumberId(route.query.targetScaleId, route.query.projectId, route.query.scaleId) || 31

const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })

watch(() => route.fullPath, () => {
  if (!scaleData.value) {
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}, { immediate: true })

// 加载量表数据
const loadScaleData = async () => {
  try {
    const res = await selectConstipationScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      scaleData.value = res.data[0]
      // 初始化日期题和持续时间题答案
      for (const q of scaleData.value.questionList || []) {
        if (isDateQuestionRaw(q)) {
          if (!dateAnswers[q.questionId]) {
            dateAnswers[q.questionId] = reactive({ date: '' })
          }
        }
        if (isDurationQuestion(q)) {
          if (!durationAnswers[q.questionId]) {
            durationAnswers[q.questionId] = reactive({ years: null, months: null, unknown: false })
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败')
    scaleData.value = { scaleId: null, scaleName: '', code: '', questionList: [] }
  }
}

onMounted(() => { loadScaleData() })

// ==================== 答案存储 ====================
const answers = ref({}) // 选择题
const fillAnswers = reactive({}) // 填空题
const dateAnswers = reactive({}) // 日期题
const durationAnswers = reactive({}) // 持续时间题

// 禁用未来日期
const disabledFutureDate = (time) => {
  return time.getTime() > Date.now()
}

// ==================== 题目类型判断 ====================
function isChoiceQuestion(q) {
  if (isDateQuestionRaw(q)) return false
  if (isDurationQuestion(q)) return false
  return q.type === 1 || q.type === 2 || (!q.type && !q.inputType)
}

function isFillQuestion(q) {
  if (isDateQuestionRaw(q)) return false
  if (isDurationQuestion(q)) return false
  return q.type === 3 || q.inputType
}

function isDateQuestionRaw(q) {
  if (q.type === 5) return false
  const title = String(q.title || '')
  // 持续时间类排除，仅真实日期题生效
  if (title.includes('持续时间') || title.includes('多长时间')
      || title.includes('发病时间') || title.includes('病程')
      || title.includes('多久') || title.includes('到现在')) {
    return false
  }
  return title.includes('日期') || title.includes('上次排便') || title.includes('最近一次排便')
}

function isDateQuestion(q) {
  return isDateQuestionRaw(q)
}

// 持续时间题判断
function isDurationQuestion(q) {
  if (q.type === 5) return true
  const title = String(q.title || '')
  return title.includes('持续时间') || title.includes('多长时间')
      || title.includes('发病时间') || title.includes('病程')
      || title.includes('多久') || title.includes('到现在')
      || title.includes('有几年') || title.includes('有几个月')
      || q.type === 4
}

// ==================== 选项交互逻辑 ====================
// 切换选择题选项
function toggleOption(question, option) {
  if (!answers.value[question.questionId]) {
    answers.value[question.questionId] = []
  }
  const qType = question.type || 1
  // 单选直接替换
  if (qType === 1) {
    answers.value[question.questionId] = [option.id]
    return
  }

  // 多选：不知道和普通选项互斥
  const isClickUnknown = isOptionUnknown(option)
  if (isClickUnknown) {
    const currentIdx = answers.value[question.questionId].indexOf(option.id)
    answers.value[question.questionId] = currentIdx === -1 ? [option.id] : []
  } else {
    // 先清除所有不知道类选项，再切换当前选项
    answers.value[question.questionId] = answers.value[question.questionId].filter(id => {
      const opt = (question.optionList || []).find(o => o.id === id)
      return opt ? !isOptionUnknown(opt) : true
    })
    const idx = answers.value[question.questionId].indexOf(option.id)
    if (idx === -1) {
      answers.value[question.questionId].push(option.id)
    } else {
      answers.value[question.questionId].splice(idx, 1)
    }
  }
}

// 切换持续时间题的不知道状态
function toggleDurationUnknown(question) {
  const d = durationAnswers[question.questionId]
  if (!d) {
    durationAnswers[question.questionId] = reactive({ years: null, months: null, unknown: true })
    return
  }
  if (d.unknown) {
    d.unknown = false
  } else {
    d.unknown = true
    d.years = null
    d.months = null
  }
}

// 判断选项是否为正常/无异常
function isOptionNormal(option) {
  const text = String(option.content || '').toLowerCase()
  const label = String(option.label || '').toLowerCase()
  const combined = `${label} ${text}`
  const normalKeywords = ['无', '正常', '从不', '没有', '否', '不是', '不影响', '不费力', '畅通', '通畅', '>3', '3次以上', '3次及以上', '每周3次以上', '正常范围']
  return normalKeywords.some(k => combined.includes(k))
}

// 判断选项是否为不知道/不清楚
function isOptionUnknown(option) {
  const text = String(option.content || '').toLowerCase()
  const label = String(option.label || '').toLowerCase()
  const combined = `${label} ${text}`
  return combined.includes('不知道') || combined.includes('不清楚') || combined.includes('不确定')
      || combined.includes('记不清') || combined.includes('不记得')
}

// ==================== 作答状态统计 ====================
function isQuestionAnswered(question) {
  if (isDateQuestion(question)) {
    const d = dateAnswers[question.questionId]
    return !!(d && d.date)
  }
  if (isDurationQuestion(question)) {
    const d = durationAnswers[question.questionId]
    if (!d) return false
    if (d.unknown) return true
    return (d.years !== null && d.years !== undefined) || (d.months !== null && d.months !== undefined)
  }
  if (isFillQuestion(question)) {
    const val = fillAnswers[question.questionId]
    return val !== undefined && val !== null && val !== ''
  }
  const selected = answers.value[question.questionId] || []
  return selected.length > 0
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

// ==================== 工具方法 ====================
// 日期格式化
function formatDate(dateStr) {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  if (parts.length >= 2) {
    if (parts.length === 2) return `${parts[0]}年${parts[1]}月`
    return `${parts[0]}年${parts[1]}月${parts[2]}日`
  }
  return dateStr
}

// 计算病程持续时长
function calcDuration(startStr) {
  if (!startStr) return ''
  const start = new Date(startStr)
  const now = new Date()
  let years = now.getFullYear() - start.getFullYear()
  let months = now.getMonth() - start.getMonth()
  let days = now.getDate() - start.getDate()
  if (days < 0) {
    months -= 1
    const prevMonth = new Date(now.getFullYear(), now.getMonth(), 0)
    days += prevMonth.getDate()
  }
  if (months < 0) {
    years -= 1
    months += 12
  }
  const parts = []
  if (years > 0) parts.push(`${years}年`)
  if (months > 0) parts.push(`${months}个月`)
  if (years === 0 && months === 0 && days >= 0) {
    if (days === 0) return '今天'
    parts.push(`${days}天`)
  }
  return parts.join('') || '今天'
}

// 计算距上次排便天数
function calcDaysAgo(dateStr) {
  if (!dateStr) return ''
  const target = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - target.getTime()
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  if (days === 0) return '今日'
  if (days === 1) return '昨日'
  if (days <= 30) return `${days}天前`
  if (days <= 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年${Math.floor((days % 365) / 30)}个月前`
}

// 判断日期题类型
function isStartDateQuestion(q) {
  const title = String(q.title || '')
  return title.includes('开始') || title.includes('出现') || title.includes('多久') || title.includes('持续')
}
function isLastBmQuestion(q) {
  const title = String(q.title || '')
  return title.includes('上次排便') || title.includes('最近排便') || title.includes('最近一次')
}

/**
 * 清洗题目标题，输出简洁专业表述
 */
function cleanTitle(title) {
  if (!title) return ''
  let t = String(title).replace(/^\s*\d+[.、\s]+/, '').trim()

  // 统一语义映射
  if (t.includes('是否有以下症状') || t.includes('症状表现') || t.includes('存在症状')) return '症状表现'
  if (t.includes('粪便性状') || t.includes('大便性状')) return '粪便性状'
  if (t.includes('开始到现在持续') || t.includes('总病程') || t.includes('出现至今') || t.includes('症状至今')) return '症状出现至今'
  if (t.includes('有多长时间是有症状') || t.includes('症状持续时长') || t.includes('实际有症状')) return '有症状时长'
  if (t.includes('上次排便') || t.includes('最近一次排便') || t.includes('末次排便')) return '末次排便时间'

  // 移除括号提示、末尾标点
  t = t.replace(/[（(].*?[）)]/g, '').trim()
  t = t.replace(/[：:、\s]+$/g, '').trim()
  return t
}

// 格式化单条答案文本
function formatAnswerItem(title, answerText, isAbnormal) {
  const clean = cleanTitle(title)
  if (clean === '症状表现') return answerText
  return `${clean}：${answerText}`
}

// ==================== 结果汇总计算 ====================
/**
 * 答题摘要列表
 */
const answerSummary = computed(() => {
  const result = []
  for (const q of scaleData.value.questionList || []) {
    // 选择题
    if (isChoiceQuestion(q)) {
      const selectedIds = answers.value[q.questionId] || []
      if (selectedIds.length === 0) continue
      const selectedOptions = q.optionList.filter(o => selectedIds.includes(o.id))

      const unknownOptions = selectedOptions.filter(o => isOptionUnknown(o))
      const abnormalOptions = selectedOptions.filter(o => !isOptionNormal(o) && !isOptionUnknown(o))
      const isAllUnknown = unknownOptions.length === selectedOptions.length
      const isAbnormal = abnormalOptions.length > 0 && !isAllUnknown

      const answerText = selectedOptions.map(o => o.content).join('、')
      result.push({
        questionTitle: q.title,
        answerText: answerText,
        displayText: isAllUnknown ? `${cleanTitle(q.title)}：患者无法明确表述` : formatAnswerItem(q.title, answerText, isAbnormal),
        isAbnormal,
        isUnknown: isAllUnknown,
        isDate: false
      })
    }
    // 填空题
    else if (isFillQuestion(q)) {
      const val = fillAnswers[q.questionId]
      if (!val) continue
      const answerText = `${val}${q.inputUnit || ''}`
      result.push({
        questionTitle: q.title,
        answerText: answerText,
        displayText: formatAnswerItem(q.title, answerText, true),
        isAbnormal: true,
        isDate: false
      })
    }
    // 日期题
    else if (isDateQuestion(q)) {
      const d = dateAnswers[q.questionId]
      if (!d?.date) continue
      let text = ''
      if (isStartDateQuestion(q)) {
        text = `${formatDate(d.date)}（已持续约${calcDuration(d.date)}）`
      } else if (isLastBmQuestion(q)) {
        text = `${formatDate(d.date)}（${calcDaysAgo(d.date)}）`
      } else {
        text = formatDate(d.date)
      }
      result.push({
        questionTitle: q.title,
        answerText: text,
        displayText: formatAnswerItem(q.title, text, false),
        isAbnormal: false,
        isDate: true
      })
    }
    // 持续时间题
    else if (isDurationQuestion(q)) {
      const d = durationAnswers[q.questionId]
      if (!d) continue
      if (d.unknown) {
        result.push({
          questionTitle: q.title,
          answerText: '患者无法明确表述',
          displayText: `${cleanTitle(q.title)}：患者无法明确表述`,
          isAbnormal: false,
          isUnknown: true,
          isDate: true
        })
        continue
      }
      const y = (d.years !== null && d.years !== undefined) ? d.years : 0
      const m = (d.months !== null && d.months !== undefined) ? d.months : 0
      if (y === 0 && m === 0) continue

      const parts = []
      if (y > 0) parts.push(`${y}年`)
      if (m > 0) parts.push(`${m}个月`)
      result.push({
        questionTitle: q.title,
        answerText: parts.join(''),
        displayText: formatAnswerItem(q.title, parts.join(''), false),
        isAbnormal: false,
        isDate: true
      })
    }
  }
  return result
})

const hasDateQuestion = computed(() => {
  return (scaleData.value.questionList || []).some(q => isDateQuestion(q) || isDurationQuestion(q))
})

// 首题是否选了不知道
const isFirstQuestionUnknown = computed(() => {
  const questions = scaleData.value.questionList || []
  for (const q of questions) {
    if (isChoiceQuestion(q)) {
      const selectedIds = answers.value[q.questionId] || []
      if (selectedIds.length === 0) return false
      const selectedOptions = (q.optionList || []).filter(o => selectedIds.includes(o.id))
      return selectedOptions.some(o => isUnknownOption(o))
    }
  }
  return false
})
const hasUnknownDate = computed(() => false)
const isFirstDateUnknown = isFirstQuestionUnknown

/**
 * 便秘等级判定
 */
const constipationLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8', bg: '#f8fafc' }
  }
  // 仅统计真实症状项（排除时间、不知道）
  const meaningfulItems = answerSummary.value.filter(item => !item.isDate && !item.isUnknown)
  const abnormalCount = meaningfulItems.filter(item => item.isAbnormal).length

  if (meaningfulItems.length === 0) {
    return { text: '信息不足，无法评估', color: '#f59e0b', bg: '#fffbeb' }
  }
  if (abnormalCount === 0) {
    return { text: '未见明显便秘征象', color: '#22c55e', bg: '#f0fdf4' }
  }
  return { text: '存在便秘症状', color: '#ef4444', bg: '#fef2f2' }
})

/**
 * 时间病程描述（智能拼接，时长一致时自动简化）
 */
const timeDescription = computed(() => {
  const timeMap = {}
  for (const q of scaleData.value.questionList || []) {
    const titleKey = cleanTitle(q.title)
    if (isDateQuestion(q)) {
      const d = dateAnswers[q.questionId]
      if (!d?.date) continue
      if (isStartDateQuestion(q)) {
        timeMap[titleKey] = `${formatDate(d.date)}（已持续约${calcDuration(d.date)}）`
      } else if (isLastBmQuestion(q)) {
        timeMap[titleKey] = `${formatDate(d.date)}（${calcDaysAgo(d.date)}）`
      } else {
        timeMap[titleKey] = formatDate(d.date)
      }
    } else if (isDurationQuestion(q)) {
      const d = durationAnswers[q.questionId]
      if (!d) continue
      if (d.unknown) {
        timeMap[titleKey] = '患者无法明确表述'
        continue
      }
      const y = (d.years !== null && d.years !== undefined) ? d.years : 0
      const m = (d.months !== null && d.months !== undefined) ? d.months : 0
      if (y === 0 && m === 0) continue
      const parts = []
      if (y > 0) parts.push(`${y}年`)
      if (m > 0) parts.push(`${m}个月`)
      timeMap[titleKey] = parts.join('')
    }
  }

  const parts = []
  const totalDuration = timeMap['症状出现至今']
  const symptomDuration = timeMap['有症状时长']

  // 两个时长都存在且一致时，简化表述
  if (totalDuration && symptomDuration) {
    if (totalDuration === symptomDuration) {
      parts.push(`症状出现至今约${totalDuration}，期间症状持续存在`)
    } else {
      parts.push(`症状出现至今约${totalDuration}，其中存在明显症状的时长约${symptomDuration}`)
    }
  } else if (totalDuration) {
    parts.push(`症状出现至今约${totalDuration}`)
  } else if (symptomDuration) {
    parts.push(`症状持续时长约${symptomDuration}`)
  }

  if (timeMap['末次排便时间']) {
    parts.push(`末次排便为${timeMap['末次排便时间']}`)
  }

  // 其他时间项兜底
  Object.entries(timeMap).forEach(([key, val]) => {
    if (!['症状出现至今', '有症状时长', '末次排便时间'].includes(key)) {
      parts.push(`${key}：${val}`)
    }
  })
  return parts
})

/**
 * 综合建议文案（修正多余逗号，表述更通顺专业）
 */
const constipationSuggest = computed(() => {
  const level = constipationLevel.value.text

  if (level === '未见明显便秘征象') {
    return '肠道功能未见明显异常。建议继续保持均衡饮食，每日足量饮水，坚持规律运动，维持良好的排便习惯。'
  }

  if (level === '信息不足，无法评估') {
    return '因有效信息不足，暂无法完成准确评估。建议密切观察排便情况，若症状持续或加重，及时前往消化内科专科就诊检查。'
  }

  if (level === '存在便秘症状') {
    const abnormalCount = answerSummary.value.filter(item => item.isAbnormal && !item.isDate && !item.isUnknown).length
    let base = '日常建议增加膳食纤维摄入，多吃蔬菜水果与粗粮，每日保证1500-2000ml饮水，养成规律排便习惯，避免久坐不动。'

    // 有异常项时自然衔接，不加多余标点
    if (abnormalCount > 0) {
      base = `针对上述症状，${base}`
    }
    return base
  }

  return '请完成全部评估题目后生成专属建议。'
})

/**
 * 便秘症状详情，存入建议，不放入评估结果
 */
const constipationDetailText = computed(() => {
  const symptomItems = answerSummary.value.filter(item => !item.isDate)
  if (symptomItems.length === 0) return ''

  const summaryParts = symptomItems.map(item => {
    if (item.isUnknown) {
      return `${item.displayText}不清楚`
    }
    return item.displayText
  })
  return `症状表现：${summaryParts.join('；')}。`
})

/**
 * 存入数据库的结果文本：只保留结论和病程
 */
const combinedResult = computed(() => {
  if (totalQuestions.value === 0) return ''
  if (answeredCount.value < totalQuestions.value) return '便秘评估：待评估'

  let timePart = ''
  if (timeDescription.value.length > 0) {
    timePart = `${timeDescription.value.join('；')}。`
  }

  return `${constipationLevel.value.text}。${timePart}`
      .replace(/[。；;，,]+$/, '')
})

/**
 * 存入数据库的完整建议文本
 */
const combinedSuggest = computed(() => {
  const baseSuggest = constipationSuggest.value
  if (constipationLevel.value.text === '存在便秘症状') {
    return `${constipationDetailText.value}${baseSuggest}若症状持续无改善或进行性加重，建议及时前往消化内科专科就诊，完善相关检查明确诊断。`
  }
  if (constipationLevel.value.text === '信息不足，无法评估') {
    return `${baseSuggest}建议前往消化内科专科就诊，完善相关检查以明确是否存在便秘及相关问题。`
  }
  return baseSuggest
})

// ==================== 交互方法 ====================
const goBack = () => { router.push({ path: '/patient/detection/customize', query: route.query }) }

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:constipation_screen`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/customize',
    query: { patientId, patientName }
  })
}

// 构建持续时间答案对象
const buildDurationAnswer = (question) => {
  const d = durationAnswers[question.questionId]
  if (!d) return null
  if (d.unknown) {
    return { type: 'duration', unknown: true, text: '患者无法明确表述' }
  }
  const hasYears = d.years !== null && d.years !== undefined && d.years !== ''
  const hasMonths = d.months !== null && d.months !== undefined && d.months !== ''
  if (!hasYears && !hasMonths) return null

  const years = hasYears ? Number(d.years) : 0
  const months = hasMonths ? Number(d.months) : 0
  const parts = []
  if (years > 0) parts.push(`${years}年`)
  if (months > 0) parts.push(`${months}个月`)
  return {
    type: 'duration',
    years,
    months,
    unknown: false,
    text: parts.join('') || '0个月'
  }
}

// 提交评估
const submitAssessment = async () => {
  if (submitting.value || answeredCount.value < totalQuestions.value) return
  submitting.value = true

  // 格式化所有答案
  const formattedAnswers = {}
  for (const q of scaleData.value.questionList || []) {
    if (isDateQuestion(q)) {
      const d = dateAnswers[q.questionId]
      if (d?.date) {
        formattedAnswers[String(q.questionId)] = {
          type: 'date',
          value: d.date,
          text: formatDate(d.date)
        }
      }
      continue
    }
    if (isDurationQuestion(q)) {
      const durationAnswer = buildDurationAnswer(q)
      if (durationAnswer) {
        formattedAnswers[String(q.questionId)] = durationAnswer
      }
      continue
    }
    if (isFillQuestion(q)) {
      const val = fillAnswers[q.questionId]
      if (val !== undefined && val !== null && String(val).trim() !== '') {
        formattedAnswers[String(q.questionId)] = String(val).trim()
      }
      continue
    }
    const selectedIds = answers.value[q.questionId] || []
    if (selectedIds.length > 0) {
      formattedAnswers[String(q.questionId)] = selectedIds.map(id => Number(id))
    }
  }

  const dto = {
    patientId: Number(patientId),
    projectId: targetScaleId,
    scaleId: targetScaleId,
    answers: formattedAnswers,
    result: combinedResult.value,
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
  min-height: calc(100vh - 84px);
  padding: 24px 28px 48px;
  background: #faf5ff;
  box-sizing: border-box;
  max-width: 1100px;
  margin: 0 auto;
}

/* 顶部导航栏 */
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

/* 选项样式 */
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
  &:hover { background: #f3e8ff; border-color: #e9d5ff; }
  &.is-selected { background: #f3e8ff; border-color: #a855f7; box-shadow: 0 2px 8px rgba(168,85,247,0.12); }
}
.opt-left { display: flex; align-items: center; gap: 12px; flex: 1; }
.opt-radio, .opt-checkbox {
  width: 22px; height: 22px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;
  transition: all 0.15s;
  &.is-checked {
    background: #a855f7;
    border-color: #a855f7;
  }
}
.opt-checkbox { border-radius: 6px; }
.opt-content { flex: 1; }
.opt-label {
  font-size: 14px;
  color: #334155;
  line-height: 1.5;
}

/* 填空/日期/持续时间 通用样式 */
.q-fill {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.fill-row {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fafbfc;
  padding: 12px 16px;
  border-radius: 10px;
  &.is-disabled {
    opacity: 0.5;
    pointer-events: none;
  }
}
.fill-label {
  font-size: 14px;
  color: #334155;
  flex-shrink: 0;
}
.fill-input, .date-input {
  flex: 1;
  max-width: 320px;
}
.fill-unit {
  font-size: 14px;
  color: #64748b;
  flex-shrink: 0;
}
.date-row { margin-top: 4px; }

/* 持续时间专用 */
.duration-row {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dur-select { width: 120px; }

/* 不知道按钮 */
.unknown-option {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  background: #fff;
  border: 1.5px solid #e2e8f0;
  border-radius: 20px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
  margin-left: 4px;
  flex-shrink: 0;
  &:hover {
    background: #faf5ff;
    border-color: #c4b5fd;
    color: #7c3aed;
  }
  &.is-selected {
    background: linear-gradient(135deg, #a855f7 0%, #7c3aed 100%);
    border-color: #7c3aed;
    color: #fff;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(168, 85, 247, 0.3);
  }
}

.skip-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f0f9ff;
  border-radius: 8px;
  color: #0284c7;
  font-size: 13px;
  margin-top: 4px;
}
.q-fill.is-disabled {
  opacity: 0.65;
  pointer-events: none;
}

/* 结果区域 */
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

.level-display {
  padding: 20px;
  display: flex;
  justify-content: center;
}
.level-tag {
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
}

/* 异常条目 */
.abnormal-section {
  padding: 0 20px 20px;
}
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
  &.is-abnormal .abnormal-q { color: #b91c1c; }
  &.is-normal .abnormal-q { color: #047857; }
}
.abnormal-index {
  width: 26px; height: 26px;
  border-radius: 6px;
  background: #e9d5ff;
  color: #6b21a8;
  font-size: 11.5px; font-weight: 700;
  font-family: Consolas, monospace;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.abnormal-row.is-abnormal .abnormal-index {
  background: #fef3c7;
  color: #b45309;
}
.abnormal-content { flex: 1; }
.abnormal-q {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}
.abnormal-a {
  font-size: 12.5px;
  color: #64748b;
  line-height: 1.5;
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
.ok-icon {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: #f0fdf4;
  color: #22c55e;
  font-size: 24px;
  display: flex; align-items: center; justify-content: center;
}
.ok-text { font-size: 14px; font-weight: 600; color: #22c55e; }
.hint-text { font-size: 13px; color: #94a3b8; }

/* 建议卡片 */
.suggest-card .suggest-text {
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
.submit-hint {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

/* 响应式适配 */
@media (max-width: 900px) {
  .daily-life-container {
    padding: 16px 12px 32px;
  }
  .hero-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 18px;
  }
  .patient-dialog :deep(.el-dialog__body) {
    padding: 0 16px 20px;
  }
}
</style>

