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

        <!-- 选择题（单选 / 多选）—— 持续时间题用下面的专用UI -->
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

        <!-- 填空题（type === 3 或有 inputType） -->
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

        <!-- 日期题（type === 4 或 标题含 日期/发病时间/上次排便） -->
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

        <!-- 年月下拉框（持续时间型） + "不知道"按钮，两者互斥 -->
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
          建议增加膳食纤维摄入，每日饮水1500-2000ml，规律排便习惯，必要时到消化内科就诊进一步检查。
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
import { ArrowLeft, Check, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectConstipationScale, insertCgaRecord } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'
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

// 选择题答案：{ questionId: [optionId, ...] }
const answers = ref({})
// 填空题答案：{ questionId: 'text' }
const fillAnswers = reactive({})
// 日期题答案：{ questionId: { date: 'YYYY-MM-DD' } }
const dateAnswers = reactive({})
// 持续时间（年月）答案：{ questionId: { years: null, months: null } }
const durationAnswers = reactive({})

// 禁用未来日期
const disabledFutureDate = (time) => {
  return time.getTime() > Date.now()
}

// 题目类型判断
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
  // 只有真正需要"具体某一天"的题才是日期题
  // 如：上次排便时间、最近一次排便日期
  // 排除"持续时间/多长时间/发病时间/病程"（这些用年月下拉框）
  if (title.includes('持续时间') || title.includes('多长时间')
      || title.includes('发病时间') || title.includes('病程')) {
    return false
  }
  return title.includes('日期') && !title.includes('发病')
    || title.includes('上次排便') || title.includes('最近排便')
}
function isDateQuestion(q) {
  return isDateQuestionRaw(q)
}

// 持续时间题（年月下拉框）——只要标题问时间长短就是
function isDurationQuestion(q) {
  if (q.type === 5) return true
  const title = String(q.title || '')
  // 包含明确问时间长短的关键词
  if (title.includes('持续时间') || title.includes('多长时间')
      || title.includes('发病时间') || title.includes('病程')
      || title.includes('多久') || title.includes('几年')
      || title.includes('几个月') || title.includes('到现在')
      || title.includes('多长') || title.includes('时间是')
      || title.includes('有几年') || title.includes('有几个月')) {
    return true
  }
  // type=4 作为备选
  if (q.type === 4) return true
  return false
}

// 切换选项（单选/多选）
function toggleOption(question, option) {
  if (!answers.value[question.questionId]) {
    answers.value[question.questionId] = []
  }
  const qType = question.type || 1
  // 单选：直接替换
  if (qType === 1) {
    answers.value[question.questionId] = [option.id]
  } else {
    // 多选："不清楚/不知道"和其他选项互斥
    const isClickingUnknown = isOptionUnknown(option)
    if (isClickingUnknown) {
      // 点的是"不知道"类选项 → 只保留它自己（如果已选中则取消）
      const currentIdx = answers.value[question.questionId].indexOf(option.id)
      if (currentIdx === -1) {
        answers.value[question.questionId] = [option.id]
      } else {
        answers.value[question.questionId] = []
      }
    } else {
      // 点的是正常选项 → 先清除所有"不知道"类选项，再正常切换当前选项
      // 清除"不知道"类选项
      answers.value[question.questionId] = answers.value[question.questionId].filter(id => {
        const opt = (question.optionList || []).find(o => o.id === id)
        return opt ? !isOptionUnknown(opt) : true
      })
      // 正常切换当前选项
      const idx = answers.value[question.questionId].indexOf(option.id)
      if (idx === -1) {
        answers.value[question.questionId].push(option.id)
      } else {
        answers.value[question.questionId].splice(idx, 1)
      }
    }
  }
}

// 切换持续时间题的"不知道"状态（和年月互斥）
function toggleDurationUnknown(question) {
  const d = durationAnswers[question.questionId]
  if (!d) {
    durationAnswers[question.questionId] = reactive({ years: null, months: null, unknown: true })
    return
  }
  // 如果已经是unknown，取消
  if (d.unknown) {
    d.unknown = false
    return
  }
  // 勾选"不知道"时清空年月
  d.unknown = true
  d.years = null
  d.months = null
}

// 判断某题是否已作答
function isQuestionAnswered(question) {
  if (isDateQuestion(question)) {
    const d = dateAnswers[question.questionId]
    if (!d) return false
    return d.date && d.date !== ''
  }
  if (isDurationQuestion(question)) {
    const d = durationAnswers[question.questionId]
    if (!d) return false
    // 选了年月或"不知道"都算作答
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

// 判断选项是否表示"正常/无异常"
function isOptionNormal(option) {
  const text = String(option.content || '').toLowerCase()
  const label = String(option.label || '').toLowerCase()
  const combined = `${label} ${text}`
  const normalKeywords = ['无', '正常', '从不', '没有', '否', '不是', '不影响', '不费力', '畅通', '通畅', '>3', '3次以上', '3次及以上', '3～6', '4-6', '4～6', '每周3次以上', '正常范围']
  return normalKeywords.some(k => combined.includes(k))
}

// 判断选项是否是"不知道/不清楚"——中性状态，既不算正常也不算异常
function isOptionUnknown(option) {
  const text = String(option.content || '').toLowerCase()
  const label = String(option.label || '').toLowerCase()
  const combined = `${label} ${text}`
  return combined.includes('不知道') || combined.includes('不清楚') || combined.includes('不确定')
    || combined.includes('记不清') || combined.includes('不记得')
}

// 日期格式化
function formatDate(dateStr) {
  if (!dateStr) return ''
  // dateStr 可能是 YYYY-MM-DD 或 YYYY-MM
  const parts = dateStr.split('-')
  if (parts.length >= 2) {
    if (parts.length === 2) return `${parts[0]}年${parts[1]}月`
    return `${parts[0]}年${parts[1]}月${parts[2]}日`
  }
  return dateStr
}

// 计算两个日期之间的时长（用于便秘持续时间）
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

// 计算距上次排便的天数
function calcDaysAgo(dateStr) {
  if (!dateStr) return ''
  const target = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - target.getTime()
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days === 2) return '前天'
  if (days <= 30) return `${days}天前`
  if (days <= 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年${Math.floor((days % 365) / 30)}个月前`
}

// 判断日期题是否是"开始时间"类型（用于生成持续时间描述）
function isStartDateQuestion(q) {
  const title = String(q.title || '')
  return title.includes('开始') || title.includes('出现') || title.includes('多久') || title.includes('持续')
}

// 判断日期题是否是"上次排便"类型
function isLastBmQuestion(q) {
  const title = String(q.title || '')
  return title.includes('上次排便') || title.includes('最近排便') || title.includes('最近一次')
}

// 清理题目标题前缀（如"1. ""2. "、"是否有以下症状（可多选）"）并去掉末尾冒号/空格
function cleanTitle(title) {
  if (!title) return ''
  let t = String(title).replace(/^\s*\d+[.、\s]+/, '')
  t = t.trim()
  // 把"是否有以下症状（可多选）"类型的题干改成简洁描述
  if (t.includes('是否有以下症状') || t.includes('以下症状')) return '存在症状'
  if (t.includes('有症状时的粪便性状')) return '粪便性状'
  if (t.includes('有症状时的') && t.includes('粪便')) return '粪便性状'
  // 去掉"（可多选）""（单选）"括号提示
  t = t.replace(/[（(].*?[）)]/g, '').trim()
  // 去掉末尾冒号、顿号、空格
  t = t.replace(/[：:、\s]+$/g, '').trim()
  return t
}

// 生成单项结果文本
function formatAnswerItem(title, answerText, isAbnormal) {
  const clean = cleanTitle(title)
  // 对于"存在症状"类型，直接显示症状内容
  if (clean === '存在症状') return answerText
  return `${clean}：${answerText}`
}

// 答题摘要：逐条列出用户的选择（日期题打 isDate 标记，"不知道"作为中性状态）
const answerSummary = computed(() => {
  const result = []
  for (const q of scaleData.value.questionList || []) {
    // 选择题
    if (isChoiceQuestion(q)) {
      const selectedIds = answers.value[q.questionId] || []
      if (selectedIds.length === 0) continue
      const selectedOptions = q.optionList.filter(o => selectedIds.includes(o.id))
      // 区分：正常选项、不知道选项、异常选项
      const unknownOptions = selectedOptions.filter(o => isOptionUnknown(o))
      const normalOptions = selectedOptions.filter(o => isOptionNormal(o))
      const abnormalOptions = selectedOptions.filter(o => !isOptionNormal(o) && !isOptionUnknown(o))
      const isAllUnknown = unknownOptions.length === selectedOptions.length
      const isAbnormal = abnormalOptions.length > 0 && !isAllUnknown
      const answerText = selectedOptions.map(o => `${o.content}`).join('、')
      result.push({
        questionTitle: q.title,
        answerText: answerText,
        displayText: isAllUnknown ? `${cleanTitle(q.title)}：不清楚` : formatAnswerItem(q.title, answerText, isAbnormal),
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
      if (!d) continue
      if (d.unknown) continue
      if (!d.date) continue
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
        displayText: formatAnswerItem(q.title, text, true),
        isAbnormal: true,
        isDate: true
      })
    }
    // 持续时间题（年月）——时间本身不算异常，只做信息描述
    else if (isDurationQuestion(q)) {
      const d = durationAnswers[q.questionId]
      if (!d) continue
      // 选了"不知道"
      if (d.unknown) {
        result.push({
          questionTitle: q.title,
          answerText: '不清楚',
          displayText: `${cleanTitle(q.title)}：不清楚`,
          isAbnormal: false,
          isUnknown: true,
          isDate: true
        })
        continue
      }
      const y = (d.years !== null && d.years !== undefined) ? d.years : 0
      const m = (d.months !== null && d.months !== undefined) ? d.months : 0
      if (y === 0 && m === 0) continue
      let text = ''
      if (y > 0 && m > 0) text = `${y}年${m}个月`
      else if (y > 0) text = `${y}年`
      else text = `${m}个月`
      result.push({
        questionTitle: q.title,
        answerText: text,
        displayText: formatAnswerItem(q.title, text, false),
        isAbnormal: false,
        isDate: true
      })
    }
  }
  return result
})

// 是否存在日期题或持续时间题
const hasDateQuestion = computed(() => {
  return (scaleData.value.questionList || []).some(q => isDateQuestion(q) || isDurationQuestion(q))
})

// 判断某个选项是否是"不知道"类选项
function isUnknownOption(option) {
  const content = String(option.content || '').toLowerCase()
  return content.includes('不知道') || content.includes('不清楚')
    || content.includes('不确定') || content.includes('记不清')
}

// 第一题（选择题）是否选了"不知道"
// 注意：即使第一题选了不知道，持续时间题仍然可以正常作答
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

// 保留向后兼容：是否有日期题被标记为未知（当前未使用日期题的"不知道"）
const hasUnknownDate = computed(() => {
  return false
})
// 保留向后兼容：第一题选了不知道
const isFirstDateUnknown = isFirstQuestionUnknown

// 便秘等级：只基于真实症状选择（排除"不知道"和时间类题目）
const constipationLevel = computed(() => {
  if (answeredCount.value < totalQuestions.value || totalQuestions.value === 0) {
    return { text: '待评估', color: '#94a3b8', bg: '#f8fafc' }
  }
  // 只统计非日期/非"不知道"的题目（即真实症状选择）
  const meaningfulItems = answerSummary.value.filter(item => !item.isDate && !item.isUnknown)
  const abnormalCount = meaningfulItems.filter(item => item.isAbnormal).length
  // 如果所有有内容的题目都是"不知道"——信息不足
  if (meaningfulItems.length === 0) {
    return { text: '信息不足，无法评估', color: '#f59e0b', bg: '#fffbeb' }
  }
  if (abnormalCount === 0) {
    return { text: '未见明显便秘征象', color: '#22c55e', bg: '#f0fdf4' }
  }
  return { text: '存在便秘症状', color: '#ef4444', bg: '#fef2f2' }
})

// 时间描述（用于结论拼接）：日期题 + 持续时间题
const timeDescription = computed(() => {
  const parts = []
  for (const q of scaleData.value.questionList || []) {
    if (isDateQuestion(q)) {
      const d = dateAnswers[q.questionId]
      if (!d) continue
      if (!d.date) continue
      if (isStartDateQuestion(q)) {
        parts.push(`${cleanTitle(q.title)}：${formatDate(d.date)}（已持续约${calcDuration(d.date)}）`)
      } else if (isLastBmQuestion(q)) {
        parts.push(`${cleanTitle(q.title)}：${formatDate(d.date)}（${calcDaysAgo(d.date)}）`)
      } else {
        parts.push(`${cleanTitle(q.title)}：${formatDate(d.date)}`)
      }
    } else if (isDurationQuestion(q)) {
      const d = durationAnswers[q.questionId]
      if (!d) continue
      // "不知道"也加入时间描述
      if (d.unknown) {
        parts.push(`${cleanTitle(q.title)}：不清楚`)
        continue
      }
      const y = (d.years !== null && d.years !== undefined) ? d.years : 0
      const m = (d.months !== null && d.months !== undefined) ? d.months : 0
      if (y === 0 && m === 0) continue
      let text = ''
      if (y > 0 && m > 0) text = `${y}年${m}个月`
      else if (y > 0) text = `${y}年`
      else text = `${m}个月`
      parts.push(`${cleanTitle(q.title)}：${text}`)
    }
  }
  return parts
})

const constipationSuggest = computed(() => {
  const t = constipationLevel.value.text
  if (t === '未见明显便秘征象') {
    return '未见明显便秘征象，肠道功能良好。建议保持均衡饮食、足量饮水、规律运动，维持良好排便习惯。'
  }
  if (t === '信息不足，无法评估') {
    // 全部都是"不知道"——时间描述里列出来，症状部分说"无法明确评估"
    if (timeDescription.value.length > 0) {
      return `${timeDescription.value.join('，')}，患者无法明确描述具体症状表现，建议必要时到消化内科进一步就诊评估。`
    }
    return '患者无法明确描述具体症状表现，信息不足，无法评估。建议必要时到消化内科进一步就诊检查。'
  }
  if (t === '存在便秘症状') {
    // "主要表现为" 中剔除日期题、剔除"不知道"的题目
    const abnormalItems = answerSummary.value.filter(item => item.isAbnormal && !item.isDate && !item.isUnknown)
    const details = abnormalItems.map(item => item.displayText).join('；')
    if (timeDescription.value.length > 0) {
      if (!details) {
        return `${timeDescription.value.join('，')}，并无明显症状表现。建议增加膳食纤维（蔬菜、水果、粗粮）摄入，每日饮水1500-2000ml，规律排便，避免久坐。`
      }
      return `${timeDescription.value.join('，')}，主要表现为：${details}。建议增加膳食纤维（蔬菜、水果、粗粮）摄入，每日饮水1500-2000ml，规律排便，避免久坐。`
    }
    if (!details) {
      return `存在便秘症状，并无明显症状表现。建议增加膳食纤维（蔬菜、水果、粗粮）摄入，每日饮水1500-2000ml，规律排便，避免久坐。`
    }
    return `存在便秘症状，主要表现为：${details}。建议增加膳食纤维（蔬菜、水果、粗粮）摄入，每日饮水1500-2000ml，规律排便，避免久坐。`
  }
  return '请完成全部评估题目以生成建议。'
})

// 详细结果（传到数据库的 result 字段）
const combinedResult = computed(() => {
  if (totalQuestions.value === 0) return ''
  if (answeredCount.value < totalQuestions.value) return '便秘评估：待评估'

  // 逐条摘要（排除日期题，日期通过 timeDescription 单独展示）
  const symptomItems = answerSummary.value.filter(item => !item.isDate)
  const summaryParts = symptomItems.map(item => {
    if (item.isUnknown) {
      return `${item.displayText}【不清楚】`
    }
    const mark = item.isAbnormal ? '【异常】' : '【正常】'
    return `${item.displayText}${mark}`
  })

  let timePart = ''
  if (timeDescription.value.length > 0) {
    timePart = `时间情况：${timeDescription.value.join('；')}。`
  }

  return `便秘评估：${constipationLevel.value.text}。${timePart}${summaryParts.length > 0 ? '症状表现：' + summaryParts.join('；') + '。' : ''}`
})

const combinedSuggest = computed(() => {
  const base = constipationSuggest.value
  if (constipationLevel.value.text === '存在便秘症状') {
    return `${base} 建议必要时到消化内科专科就诊，完善相关检查明确诊断。`
  }
  if (constipationLevel.value.text === '信息不足，无法评估') {
    return `${base} 建议到消化内科专科就诊，完善相关检查以明确是否存在便秘问题。`
  }
  return base
})

const goBack = () => { router.back() }

const backToComprehensive = () => {
  try {
    if (patientId) {
      localStorage.setItem(`customize_done:${patientId}:constipation_screen`, '1')
    }
  } catch (e) {}
  router.push({
    path: '/patient/detection/comprehensive',
    query: { patientId, patientName }
  })
}

const buildDurationAnswer = (question) => {
  const d = durationAnswers[question.questionId]
  if (!d) return null
  if (d.unknown) {
    return { type: 'duration', unknown: true, text: '不知道' }
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

const submitAssessment = async () => {
  if (submitting.value || answeredCount.value < totalQuestions.value) return
  submitting.value = true

  // ========== 组装答案：选择题存选项ID数组，填空/日期/持续时间原样保存 ==========
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

/* 填空题样式 */
.q-fill {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

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

.date-row {
  margin-top: 4px;
}

.duration-row {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dur-select {
  width: 120px;
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

.submit-hint {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}
</style>
