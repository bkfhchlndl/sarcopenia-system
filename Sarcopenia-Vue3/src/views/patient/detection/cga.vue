<template>
  <div class="app-container cga-page">
    <!-- 页面头部 -->
    <section class="page-head">
      <div class="head-content">
        <p class="eyebrow">Comprehensive Geriatric Assessment</p>
        <h2>CGA筛查与评估</h2>
        <div class="patient-card">
          <div class="patient-main">
            <div class="patient-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div>
              <span class="patient-label">当前评估对象</span>
              <strong>{{ patientInfo.name || '未选择用户' }}</strong>
            </div>
          </div>
          <div class="patient-meta">
            <div class="meta-item">
              <span>性别</span>
              <strong>{{ formatGender(patientInfo.gender) }}</strong>
            </div>
            <div class="meta-item">
              <span>出生年月</span>
              <strong>{{ formatBirthdayWithAge(patientInfo.birthday) }}</strong>
            </div>
          </div>
        </div>
      </div>
      <div class="head-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" :loading="loading" @click="loadScaleData">刷新题目</el-button>
      </div>
    </section>

    <!-- 患者缺失提示 -->
    <el-alert
        v-if="!patientId"
        class="patient-alert"
        title="请从用户列表选择用户后再提交评估，否则无法保存记录。"
        type="warning"
        show-icon
        :closable="false"
    />

    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="8" animated />

    <!-- 空数据状态 -->
    <el-empty v-else-if="scaleList.length === 0" description="暂无CGA评估题目">
      <el-button type="primary" @click="loadScaleData">重新加载</el-button>
    </el-empty>

    <!-- 评估表单主体 -->
    <template v-else>
      <!-- 进度统计栏 -->
      <section class="summary-bar">
        <div>
          <span class="summary-number">{{ answeredCount }}</span>
          <span class="summary-label">已答题目</span>
        </div>
        <div>
          <span class="summary-number">{{ totalQuestionCount }}</span>
          <span class="summary-label">题目总数</span>
        </div>
        <div class="summary-progress">
          <span class="summary-number">{{ answeredPercent }}%</span>
          <span class="summary-label">完成进度</span>
          <el-progress :percentage="answeredPercent" :stroke-width="8" :show-text="false" />
        </div>
      </section>

      <!-- 量表题目表单 -->
      <el-form ref="formRef" label-position="top" class="scale-form">
        <section
            v-for="(scale, scaleIndex) in scaleList"
            :key="scale.scaleId"
            class="scale-section"
        >
          <div class="scale-head">
            <div>
              <span class="scale-index">评估模块 {{ scaleIndex + 1 }}</span>
              <h3>{{ scale.scaleName }}</h3>
            </div>
            <span class="scale-count">{{ (scale.questionList || []).length }} 题</span>
          </div>

          <div
              v-for="question in scale.questionList"
              :key="question.questionId"
              class="question-item"
          >
            <!-- 题目标题 -->
            <div class="question-title">
              <span class="question-sort">{{ question.sort }}</span>
              <p>
                <template
                    v-for="(line, idx) in formatQuestionTitleLines(question.title)"
                    :key="idx"
                >
                  <span class="question-line">{{ line }}</span>
                </template>
              </p>
            </div>

            <!-- 题目提示说明 -->
            <div v-if="question.remark" class="question-hint">
              <div class="hint-tag">提示</div>
              <div class="hint-content">
                <template
                    v-for="(line, idx) in formatQuestionTitleLines(question.remark)"
                    :key="idx"
                >
                  <div class="hint-line">{{ line }}</div>
                </template>
              </div>
            </div>

            <!-- 单选选项 -->
            <el-radio-group
                v-if="isSingle(question) && getValidOptions(question).length > 0"
                v-model="answers[question.questionId]"
                class="option-list"
            >
              <el-radio
                  v-for="option in getValidOptions(question)"
                  :key="option.id"
                  :value="option.id"
                  border
                  :class="{ 'is-picked': answers[question.questionId] === option.id }"
              >
                <span class="option-label">{{ option.label }}.</span>
                <span>{{ option.content }}</span>
              </el-radio>
            </el-radio-group>

            <!-- 多选选项（含互斥逻辑） -->
            <el-checkbox-group
                v-else-if="isMultiple(question) && getValidOptions(question).length > 0"
                v-model="answers[question.questionId]"
                class="option-list"
            >
              <el-checkbox
                  v-for="option in getValidOptions(question)"
                  :key="option.id"
                  :value="option.id"
                  border
                  :class="{ 'is-picked': Array.isArray(answers[question.questionId]) && answers[question.questionId].includes(option.id) }"
                  @change="(checked) => handleCheckboxChange(checked, question, option)"
              >
                <span class="option-label">{{ option.label }}.</span>
                <span>{{ option.content }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </section>
      </el-form>

      <!-- 底部提交栏 -->
      <section class="submit-bar">
        <div>
          <strong>完成后提交评估记录</strong>
          <p>系统会保存本次CGA评估记录，当前已完成 {{ answeredCount }}/{{ totalQuestionCount }} 题。</p>
        </div>
        <el-button
            type="success"
            size="large"
            :loading="submitting"
            :disabled="!patientId"
            @click="submitForm"
        >
          提交评估
        </el-button>
      </section>
    </template>
  </div>
</template>

<script setup name="CgaAssessment">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'

import { insertCgaRecord, selectCgaScaleList } from '@/api/cga'
import { selectPatientById } from '@/api/patient'

const route = useRoute()
const router = useRouter()

// ==================== 状态变量 ====================
/** 页面加载状态 */
const loading = ref(false)
/** 提交加载状态 */
const submitting = ref(false)
/** 量表列表（包含题目+选项） */
const scaleList = ref([])
/** 当前患者信息 */
const patientInfo = ref({
  name: '',
  gender: '',
  birthday: '',
  phone: '',
  height: '',
  address: ''
})
/** 题目答案集合：key为questionId，value为选项ID/选项ID数组 */
const answers = reactive({})

// ==================== 计算属性 ====================
/** 从路由获取患者ID */
const patientId = computed(() => {
  const value = route.query.patientId || route.query.id
  return value ? Number(value) : null
})

/** 从路由获取项目ID */
const projectId = computed(() => {
  const value = route.query.projectId
  return value ? Number(value) : 1
})

/** 所有题目平铺数组 */
const allQuestions = computed(() => scaleList.value.flatMap(scale => scale.questionList || []))

/** 需要作答的有效题目列表 */
const answerableQuestions = computed(() => allQuestions.value.filter(question => isAnswerableQuestion(question)))

/** 有效题目总数 */
const totalQuestionCount = computed(() => answerableQuestions.value.length)

/** 已作答题目数量 */
const answeredCount = computed(() => {
  return answerableQuestions.value.filter(question => {
    const value = answers[question.questionId]
    if (Array.isArray(value)) return value.length > 0
    return value !== null && value !== undefined && value !== ''
  }).length
})

/** 答题完成百分比 */
const answeredPercent = computed(() => {
  if (!totalQuestionCount.value) return 0
  return Math.round((answeredCount.value / totalQuestionCount.value) * 100)
})

// ==================== 工具函数 ====================
/**
 * 判断题目是否为单选题
 * @param {Object} question 题目对象
 * @returns {Boolean}
 */
function isSingle(question) {
  return String(question.type) === '1' && !isForcedMultiple(question)
}

/**
 * 判断题目是否为多选题
 * @param {Object} question 题目对象
 * @returns {Boolean}
 */
function isMultiple(question) {
  return String(question.type) === '2' || isForcedMultiple(question)
}

/**
 * 判断题目是否为可作答的有效题目
 * @param {Object} question 题目对象
 * @returns {Boolean}
 */
function isAnswerableQuestion(question) {
  return (isSingle(question) || isMultiple(question)) && getValidOptions(question).length > 0
}

/**
 * 判断是否为强制多选的题目（兼容历史数据类型错误的场景）
 * @param {Object} question 题目对象
 * @returns {Boolean}
 */
function isForcedMultiple(question) {
  const questionId = Number(question?.questionId ?? question?.id)
  const scaleId = Number(question?.scaleId)
  const title = String(question?.title || '')
  const hasNoneOption = getValidOptions(question).some(option => isNoneOption(option))

  // 命中任一规则即判定为多选
  return questionId === 21
      || (scaleId === 15 && Number(question?.sort) === 1)
      || (title.includes('营养状况') && hasNoneOption)
}

/**
 * 过滤有效选项（去除内容为空的无效选项）
 * @param {Object} question 题目对象
 * @returns {Array} 有效选项列表
 */
function getValidOptions(question) {
  const list = question?.optionList || []
  return list.filter(opt => {
    const content = opt?.content
    return content !== null && content !== undefined && String(content).trim() !== ''
  })
}

/**
 * 判断选项是否为"以上均无"类互斥选项
 * @param {Object} option 选项对象
 * @returns {Boolean}
 */
function isNoneOption(option) {
  if (!option?.content) return false
  const content = String(option.content).toLowerCase()
  return content.includes('以上均没有')
      || content.includes('以上都没有')
      || content.includes('以上均无')
}

/**
 * 处理多选选项变化，实现互斥逻辑
 * 选中"以上均无"时自动清空其他选项；选中其他选项时自动取消"以上均无"
 * @param {Boolean} checked 当前选项是否选中
 * @param {Object} question 题目对象
 * @param {Object} option 当前操作的选项
 */
function handleCheckboxChange(checked, question, option) {
  if (!checked) return

  const questionId = question.questionId
  const noneOption = question.optionList.find(opt => isNoneOption(opt))
  if (!noneOption) return

  const currentAnswers = answers[questionId] || []
  // 选中互斥选项：清空其他所有选项
  if (option.id === noneOption.id) {
    answers[questionId] = [noneOption.id]
    return
  }
  // 选中普通选项：移除互斥选项
  answers[questionId] = currentAnswers.filter(id => String(id) !== String(noneOption.id))
}

/**
 * 标准化量表数据：题目、选项按排序号升序排列
 * @param {Array} list 原始量表列表
 * @returns {Array} 标准化后的量表列表
 */
function normalizeScales(list) {
  return (Array.isArray(list) ? list : []).map(scale => ({
    ...scale,
    questionList: [...(scale.questionList || [])]
        .sort((a, b) => (a.sort || 0) - (b.sort || 0) || (a.questionId || 0) - (b.questionId || 0))
        .map(question => ({
          ...question,
          optionList: [...(question.optionList || [])]
              .sort((a, b) => (a.sort || 0) - (b.sort || 0) || (a.id || 0) - (b.id || 0))
        }))
  }))
}

/**
 * 初始化答案对象，按题目类型设置默认值
 */
function initAnswers() {
  // 清空原有答案
  Object.keys(answers).forEach(key => delete answers[key])

  answerableQuestions.value.forEach(question => {
    if (isSingle(question)) {
      answers[question.questionId] = null
    } else if (isMultiple(question)) {
      answers[question.questionId] = []
    } else {
      answers[question.questionId] = ''
    }
  })
}

/**
 * 格式化题目标题，按结构分行展示
 * @param {String} value 题目标题文本
 * @returns {Array} 分行后的文本数组
 */
function formatQuestionTitleLines(value) {
  const text = String(value || '')
      .replace(/\s+/g, ' ')
      .replace(/\s*(问题\s*\d+\s*[：:])/g, '\n$1')
      .trim()

  return text.split('\n').map(line => line.trim()).filter(Boolean)
}

/**
 * 格式化性别展示
 * @param {String|Number} gender 性别编码
 * @returns {String}
 */
function formatGender(gender) {
  if (gender === '1' || gender === 1) return '男性'
  if (gender === '2' || gender === 2) return '女性'
  return '-'
}

/**
 * 格式化出生日期（仅保留年月日）
 * @param {String} value 日期字符串
 * @returns {String}
 */
function formatBirthday(value) {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  return ''
}

/**
 * 根据出生日期计算年龄
 * @param {String} value 出生日期
 * @returns {Number|null}
 */
function getAge(value) {
  const birthdayText = formatBirthday(value)
  if (!birthdayText) return null

  const birthday = new Date(birthdayText)
  if (Number.isNaN(birthday.getTime())) return null

  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const monthDiff = today.getMonth() - birthday.getMonth()
  const dayDiff = today.getDate() - birthday.getDate()

  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    age--
  }
  return age >= 0 ? age : null
}

/**
 * 格式化出生日期+年龄组合展示
 * @param {String} value 出生日期
 * @returns {String}
 */
function formatBirthdayWithAge(value) {
  const birthday = formatBirthday(value)
  if (!birthday) return '-'
  const age = getAge(value)
  return age === null ? birthday : `${birthday}（${age}岁）`
}

/**
 * 手机号脱敏展示
 * @param {String} phone 手机号
 * @returns {String}
 */
function maskPhone(phone) {
  if (!phone) return '-'
  return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

/**
 * 标准化患者接口返回数据，兼容不同字段命名
 * @param {Object} response 接口返回
 * @returns {Object|null} 标准化的患者信息
 */
function normalizePatient(response) {
  const patient = response?.data && typeof response.data === 'object'
      ? response.data
      : response

  if (!patient || typeof patient !== 'object') return null

  return {
    name: patient.name || patient.patientName || patientInfo.value.name || '',
    gender: patient.gender || patient.sex || patientInfo.value.gender || '',
    birthday: patient.birthday || patient.birth_date || patient.birth || patientInfo.value.birthday || '',
    phone: patient.phone || patient.mobile || patient.tel || patientInfo.value.phone || '',
    height: patient.height ?? patientInfo.value.height ?? '',
    address: patient.address || patientInfo.value.address || ''
  }
}

// ==================== 业务方法 ====================
/**
 * 从路由参数初始化患者基础信息
 */
function initPatientFromQuery() {
  patientInfo.value = {
    name: route.query.patientName || route.query.name || '',
    gender: route.query.gender || route.query.sex || '',
    birthday: route.query.birthday || route.query.birth_date || route.query.birth || '',
    phone: route.query.phone || route.query.mobile || route.query.tel || '',
    height: route.query.height || '',
    address: route.query.address || ''
  }
}

/**
 * 加载患者详情数据
 */
async function loadPatientData() {
  initPatientFromQuery()
  if (!patientId.value) return

  try {
    const response = await selectPatientById({ patientId: patientId.value })
    const patient = normalizePatient(response)
    if (patient) {
      patientInfo.value = { ...patientInfo.value, ...patient }
    }
  } catch (error) {
    // 查询失败时保留路由携带的患者信息，不中断流程
    console.warn('患者信息查询失败，已保留路由参数信息', error)
  }
}

/**
 * 加载量表题目数据
 */
async function loadScaleData() {
  loading.value = true
  try {
    const response = await selectCgaScaleList()
    scaleList.value = normalizeScales(response.data || response)
    initAnswers()
  } finally {
    loading.value = false
  }
}

/**
 * 构建提交给后端的答案结构
 * @returns {Object} 格式化后的答案对象
 */
function buildOptionAnswers() {
  return answerableQuestions.value.reduce((result, question) => {
    const value = answers[question.questionId]
    result[question.questionId] = Array.isArray(value) ? value : [value]
    return result
  }, {})
}

/**
 * 校验所有必答题是否已完成
 * @returns {Boolean}
 */
function validateAnswers() {
  const missingList = answerableQuestions.value.filter(question => {
    const value = answers[question.questionId]
    if (Array.isArray(value)) return value.length === 0
    return value === null || value === undefined || value === ''
  })

  if (missingList.length > 0) {
    ElMessage.warning(`还有 ${missingList.length} 道题未作答`)
    return false
  }
  return true
}

/**
 * 提交评估表单
 */
async function submitForm() {
  if (!patientId.value) {
    ElMessage.warning('缺少患者ID，请从用户列表进入测试')
    return
  }
  if (!validateAnswers()) return

  submitting.value = true
  const payload = {
    patientId: patientId.value,
    projectId: projectId.value,
    scaleId: 0,
    answers: buildOptionAnswers()
  }

  try {
    console.log('【CGA评估提交】请求参数：', payload)
    const res = await insertCgaRecord(payload)
    console.log('【CGA评估提交】接口返回：', res)

    ElMessage.success('CGA评估已提交')
    // 延迟跳转，保证提示可见
    setTimeout(() => {
      router.push({
        path: '/patient/detection/comprehensive',
        query: {
          patientId: patientId.value,
          patientName: patientInfo.value.name,
          projectId: projectId.value
        }
      })
    }, 1000)
  } catch (error) {
    console.error('【CGA评估提交】失败：', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 返回上一页
 */
function goBack() {
  router.push({
    path: '/patient/detection/geriatricsyndrome',
    query: {
      patientId: patientId.value,
      patientName: patientInfo.value.name,
      projectId: projectId.value
    }
  })
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadPatientData()
  loadScaleData()
})
</script>

<style scoped lang="scss">
.cga-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background:
      radial-gradient(circle at 12% 0%, rgba(36, 151, 123, 0.12), transparent 26%),
      radial-gradient(circle at 88% 18%, rgba(64, 145, 210, 0.1), transparent 24%),
      linear-gradient(180deg, #f6fbfa 0%, #eef6f7 100%);
}

// ===== 页面头部 =====
.page-head {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px 26px;
  margin-bottom: 18px;
  border: 1px solid rgba(201, 220, 225, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(38, 77, 91, 0.08);
  overflow: hidden;

  &::before {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    content: "";
    background: linear-gradient(90deg, #24a47d 0%, #4b9ed9 55%, transparent 100%);
  }
}

.head-content {
  min-width: 0;
  flex: 1;
}

.eyebrow {
  margin: 0 0 6px;
  color: #4f8790;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

.page-head h2 {
  margin: 0;
  color: #1f3447;
  font-size: 26px;
}

.patient-card {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  max-width: 100%;
  margin-top: 16px;
  padding: 12px;
  border: 1px solid #dcebef;
  border-radius: 8px;
  background: linear-gradient(135deg, #fbfefd 0%, #f4fbfa 100%);
}

.patient-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding-right: 16px;
  border-right: 1px solid #dcebef;
}

.patient-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  color: #1f7a62;
  background: #e3f4ed;
  font-size: 20px;
}

.patient-label {
  display: block;
  margin-bottom: 4px;
  color: #6d8195;
  font-size: 13px;
}

.patient-main strong {
  color: #21364a;
  font-size: 20px;
}

.patient-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.meta-item {
  min-width: 0;
  padding: 9px 14px;
  border: 1px solid #e3eef1;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 6px 14px rgba(62, 101, 117, 0.06);

  &:nth-child(1) { min-width: 112px; }
  &:nth-child(2) { min-width: 188px; }

  span {
    display: block;
    margin-bottom: 4px;
    color: #7c8fa2;
    font-size: 12px;
  }

  strong {
    display: block;
    min-width: 0;
    color: #506d83;
    font-size: 14px;
    font-weight: 700;
    line-height: 1.35;
    overflow-wrap: anywhere;
  }
}

.head-actions {
  display: flex;
  gap: 10px;
  align-self: center;
  flex: 0 0 auto;
}

.patient-alert {
  margin-bottom: 16px;
}

// ===== 进度统计栏 =====
.summary-bar {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;

  > div {
    min-height: 96px;
    padding: 18px 22px;
    border: 1px solid rgba(204, 223, 228, 0.9);
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 10px 22px rgba(38, 77, 91, 0.06);
  }
}

.summary-number {
  display: block;
  color: #145c55;
  font-size: 28px;
  font-weight: 700;
}

.summary-label {
  color: #708195;
  font-size: 13px;
}

.summary-progress {
  :deep(.el-progress) {
    margin-top: 10px;
  }

  :deep(.el-progress-bar__outer) {
    background-color: #e5edf0;
  }

  :deep(.el-progress-bar__inner) {
    background: linear-gradient(90deg, #22a079 0%, #3d9bd7 100%);
  }
}

// ===== 量表模块 =====
.scale-section {
  position: relative;
  padding: 0;
  margin-bottom: 18px;
  border: 1px solid rgba(201, 220, 225, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(38, 77, 91, 0.08);
  overflow: hidden;

  &::before {
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 76px;
    content: "";
    background: linear-gradient(180deg, #24a47d 0%, #4f9ed9 100%);
  }
}

.scale-head {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 28px 16px;
  margin-bottom: 0;
  border-bottom: 1px solid #e6eff2;
  background: linear-gradient(90deg, #f4fbf8 0%, #ffffff 62%);

  > div {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px 12px;
    min-width: 0;
  }

  h3 {
    margin: 0;
    color: #21364a;
    font-size: 20px;
  }
}

.scale-index {
  display: inline-block;
  margin: 0;
  padding: 5px 10px;
  border-radius: 999px;
  background: #e4f4ee;
  color: #2f8a76;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}

.scale-count {
  flex: 0 0 auto;
  padding: 7px 13px;
  border-radius: 999px;
  color: #2b6f63;
  background: #e7f4ef;
  font-size: 13px;
  font-weight: 700;
}

// ===== 题目样式 =====
.question-item {
  padding: 20px 28px 24px;
  margin-bottom: 0;
  border: 0;
  border-radius: 0;
  background: #ffffff;

  &:last-child {
    margin-bottom: 0;
  }
}

.question-title {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
  color: #26394c;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.7;
}

.question-sort {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 28px;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #dff3eb 0%, #edf7fb 100%);
  color: #1f7a62;
  box-shadow: inset 0 0 0 1px rgba(47, 154, 117, 0.16);
}

.question-title p {
  margin: 0;
  min-width: 0;
  white-space: normal;
}

.question-line {
  display: block;
  width: auto;
  height: auto;
  line-height: 1.65;

  + .question-line {
    margin-top: 4px;
  }
}

// 题目提示框
.question-hint {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin: 10px 0 18px 40px;
  padding: 14px 18px;
  border: 2px solid #ff9a2e;
  border-radius: 10px;
  background: #fff7e6;
  box-shadow: 0 4px 12px rgba(255, 154, 46, 0.15);
  position: relative;
}

.hint-tag {
  flex-shrink: 0;
  padding: 4px 14px;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff9a2e 0%, #ff7d00 100%);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 2px;
  box-shadow: 0 2px 6px rgba(255, 125, 0, 0.3);
}

.hint-content {
  flex: 1;
  color: #b34700;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.8;
}

.hint-line {
  font-size: 15px;
  font-weight: 600;

  + .hint-line {
    margin-top: 4px;
  }
}

// ===== 选项列表 =====
.option-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding-left: 40px;
}

.option-list {
  :deep(.el-radio),
  :deep(.el-checkbox) {
    height: auto;
    min-height: 48px;
    margin: 0;
    padding: 12px 16px;
    white-space: normal;
    align-items: flex-start;
    border-radius: 8px;
    border-color: #dce8ed;
    background: #ffffff;
    transition: all 0.18s ease;

    &:hover {
      border-color: #7dbfa7;
      background: #f6fbf9;
      box-shadow: 0 8px 18px rgba(47, 154, 117, 0.1);
    }

    &.is-picked {
      border-color: #2f9a75;
      background: #eef9f4;
      box-shadow: inset 0 0 0 1px rgba(47, 154, 117, 0.18), 0 10px 20px rgba(47, 154, 117, 0.1);
    }
  }

  :deep(.el-radio__input.is-checked .el-radio__inner),
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    border-color: #2f9a75;
    background: #2f9a75;
  }

  :deep(.el-radio__inner),
  :deep(.el-checkbox__inner) {
    width: 16px;
    height: 16px;
    border-radius: 4px;
    border-color: #d8e6ec;
    background: #ffffff;
  }

  :deep(.el-radio__inner::after) {
    top: 2px;
    left: 5px;
    width: 4px;
    height: 8px;
    border: 2px solid #ffffff;
    border-top: 0;
    border-left: 0;
    border-radius: 0;
    background: transparent;
    transform: rotate(45deg) scale(0);
  }

  :deep(.el-radio__input.is-checked .el-radio__inner::after) {
    transform: rotate(45deg) scale(1);
  }

  :deep(.el-radio__input),
  :deep(.el-checkbox__input) {
    padding-top: 3px;
  }

  :deep(.el-radio__label),
  :deep(.el-checkbox__label) {
    display: inline-flex;
    align-items: flex-start;
    gap: 6px;
    min-width: 0;
    line-height: 1.55;
  }
}

.option-label {
  color: #4f7588;
  font-weight: 700;
}

// ===== 底部提交栏 =====
.submit-bar {
  position: sticky;
  bottom: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 20px;
  border: 1px solid rgba(190, 218, 205, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
  box-shadow: 0 -12px 30px rgba(41, 76, 93, 0.1);

  strong {
    color: #1f3447;
  }

  p {
    margin: 6px 0 0;
    color: #75869a;
  }
}

// ===== 响应式适配 =====
@media (max-width: 900px) {
  .page-head,
  .scale-head,
  .submit-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .patient-card {
    flex-direction: column;
    align-items: stretch;
  }

  .patient-main {
    padding-right: 0;
    padding-bottom: 12px;
    border-right: 0;
    border-bottom: 1px solid #dcebef;
  }

  .patient-meta {
    flex-direction: column;
    align-items: stretch;
  }

  .meta-item:nth-child(1),
  .meta-item:nth-child(2) {
    min-width: 0;
  }

  .summary-bar,
  .option-list {
    grid-template-columns: 1fr;
  }

  .summary-bar > div {
    min-height: auto;
  }

  .question-item {
    padding: 14px;
  }

  .option-list {
    padding-left: 0;
  }
}
</style>