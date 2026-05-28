<template>
  <div class="app-container cag-page">
    <section class="page-head">
      <div>
        <p class="eyebrow">Comprehensive Geriatric Assessment</p>
        <h2>CAG筛查与评估</h2>
        <div class="patient-card">
          <div class="patient-main">
            <span class="patient-label">当前用户</span>
            <strong>{{ patientInfo.name || '未选择用户' }}</strong>
          </div>
          <div class="patient-meta">
            <span>性别：{{ formatGender(patientInfo.gender) }}</span>
            <span>出生年月：{{ formatBirthdayWithAge(patientInfo.birthday) }}</span>
            <span>手机号：{{ maskPhone(patientInfo.phone) }}</span>
            <span>身高：{{ patientInfo.height ? `${patientInfo.height} cm` : '-' }}</span>
            <span>住址：{{ patientInfo.address || '-' }}</span>
          </div>
        </div>
      </div>
      <div class="head-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" :loading="loading" @click="loadScaleData">刷新题目</el-button>
      </div>
    </section>

    <el-alert
        v-if="!patientId"
        class="patient-alert"
        title="请从用户列表选择用户后再提交评估，否则后端无法保存记录。"
        type="warning"
        show-icon
        :closable="false"
    />

    <el-skeleton v-if="loading" :rows="8" animated />

    <el-empty v-else-if="scaleList.length === 0" description="暂无CAG评估题目">
      <el-button type="primary" @click="loadScaleData">重新加载</el-button>
    </el-empty>

    <template v-else>
      <section class="summary-bar">
        <div>
          <span class="summary-number">{{ answeredCount }}</span>
          <span class="summary-label">已答题目</span>
        </div>
        <div>
          <span class="summary-number">{{ totalQuestionCount }}</span>
          <span class="summary-label">题目总数</span>
        </div>
      </section>

      <el-form ref="formRef" label-position="top" class="scale-form">
        <section v-for="scale in scaleList" :key="scale.scaleId" class="scale-section">
          <div class="scale-head">
            <div>
              <h3>{{ scale.scaleName }}</h3>
              <p v-if="scale.remark" class="scale-remark">{{ scale.remark }}</p>
            </div>
          </div>

          <div
              v-for="question in scale.questionList"
              :key="question.questionId"
              class="question-item"
          >
            <div class="question-title">
              <span>{{ question.sort }}</span>
              <p>{{ question.title }}</p>
            </div>

            <el-radio-group
                v-if="isSingle(question)"
                v-model="answers[question.questionId]"
                class="option-list"
            >
              <el-radio
                  v-for="option in question.optionList"
                  :key="option.id"
                  :value="option.id"
                  border
              >
                <span class="option-label">{{ option.label }}.</span>
                <span>{{ option.content }}</span>
              </el-radio>
            </el-radio-group>

            <el-checkbox-group
                v-else
                v-model="answers[question.questionId]"
                class="option-list"
            >
              <el-checkbox
                  v-for="option in question.optionList"
                  :key="option.id"
                  :value="option.id"
                  border
              >
                <span class="option-label">{{ option.label }}.</span>
                <span>{{ option.content }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </section>
      </el-form>

      <section class="submit-bar">
        <div>
          <strong>完成后提交评估记录</strong>
          <p>系统会保存本次评估记录。</p>
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

<script setup name="CagAssessment">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { insertCagRecord, selectCagScaleList } from '@/api/cag'
import { selectPatientById } from '@/api/patient'

const route = useRoute()
const router = useRouter()

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
/** 题目答案集合 key: questionId, value: 选项ID/选项ID数组 */
const answers = reactive({})

/** 从路由获取患者ID */
const patientId = computed(() => {
  const value = route.query.patientId
  return value ? Number(value) : null
})

/** 所有题目平铺数组 */
const allQuestions = computed(() => scaleList.value.flatMap(scale => scale.questionList || []))
/** 总题目数 */
const totalQuestionCount = computed(() => allQuestions.value.length)
/** 已答题目数 */
const answeredCount = computed(() => {
  return allQuestions.value.filter(question => {
    const value = answers[question.questionId]
    return Array.isArray(value) ? value.length > 0 : value !== null && value !== undefined && value !== ''
  }).length
})

/**
 * 判断是否为单选题
 * @param {Object} question 题目对象
 * @returns {Boolean}
 */
function isSingle(question) {
  return String(question.type) === '1'
}

/**
 * 标准化量表数据：题目/选项排序
 * @param {Array} list 原始量表列表
 * @returns {Array} 标准化后的数据
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

/** 初始化答案对象 */
function initAnswers() {
  Object.keys(answers).forEach(key => delete answers[key])
  allQuestions.value.forEach(question => {
    answers[question.questionId] = isSingle(question) ? null : []
  })
}

/** 从路由参数初始化患者信息 */
function initPatientFromQuery() {
  patientInfo.value = {
    name: route.query.name || '',
    gender: route.query.gender || '',
    birthday: route.query.birthday || '',
    phone: route.query.phone || '',
    height: route.query.height || '',
    address: route.query.address || ''
  }
}

/** 加载患者详情 */
async function loadPatientData() {
  initPatientFromQuery()
  if (!patientId.value) return

  try {
    const response = await selectPatientById({ patientId: patientId.value })
    if (response && typeof response === 'object') {
      patientInfo.value = {
        ...patientInfo.value,
        ...response
      }
    }
  } catch {
    // 查询详情失败时保留从上一页带来的基础用户信息
  }
}

/** 加载量表题目数据 */
async function loadScaleData() {
  loading.value = true
  try {
    const response = await selectCagScaleList()
    scaleList.value = normalizeScales(response.data || response)
    initAnswers()
  } finally {
    loading.value = false
  }
}

/**
 * 构建提交给后端的答案结构
 * @returns {Object} 格式化后的答案
 */
function buildOptionAnswers() {
  return allQuestions.value.reduce((result, question) => {
    const value = answers[question.questionId]
    result[question.questionId] = Array.isArray(value) ? value : [value]
    return result
  }, {})
}

/** 校验所有题目是否已作答 */
function validateAnswers() {
  const missing = allQuestions.value.filter(question => {
    const value = answers[question.questionId]
    return Array.isArray(value) ? value.length === 0 : value === null || value === undefined || value === ''
  })

  if (missing.length > 0) {
    ElMessage.warning(`还有 ${missing.length} 道题未作答`)
    return false
  }
  return true
}

/** 提交评估表单 */
async function submitForm() {
  if (!patientId.value) {
    ElMessage.warning('缺少患者ID，请从用户列表进入测试')
    return
  }
  if (!validateAnswers()) return

  submitting.value = true
  try {
    await insertCagRecord({
      patientId: patientId.value,
      answers: buildOptionAnswers()
    })
    ElMessage.success('CAG评估已提交')
  } finally {
    submitting.value = false
  }
}

/** 返回上一页 */
function goBack() {
  router.back()
}

/** 格式化性别 */
function formatGender(gender) {
  if (gender === '1' || gender === 1) return '男性'
  if (gender === '2' || gender === 2) return '女性'
  return '-'
}

/** 格式化出生日期 */
function formatBirthday(value) {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  return ''
}

/** 计算年龄 */
function getAge(value) {
  const birthdayText = formatBirthday(value)
  if (!birthdayText) return null
  const birthday = new Date(birthdayText)
  if (Number.isNaN(birthday.getTime())) return null
  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const monthDiff = today.getMonth() - birthday.getMonth()
  const dayDiff = today.getDate() - birthday.getDate()
  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) age -= 1
  return age
}

/** 格式化生日+年龄展示 */
function formatBirthdayWithAge(value) {
  const birthday = formatBirthday(value)
  if (!birthday) return '-'
  const age = getAge(value)
  return age === null ? birthday : `${birthday}（${age}岁）`
}

/** 手机号脱敏 */
function maskPhone(phone) {
  if (!phone) return '-'
  return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

/** 页面挂载时初始化 */
onMounted(() => {
  loadPatientData()
  loadScaleData()
})
</script>

<style scoped lang="scss">
.cag-page {
  min-height: calc(100vh - 84px);
  background: linear-gradient(180deg, #f7fbff 0%, #eef5f8 100%);
}

.page-head,
.summary-bar,
.scale-section,
.submit-bar {
  border: 1px solid rgba(174, 198, 216, 0.72);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 32px rgba(73, 98, 126, 0.1);
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px 24px;
  margin-bottom: 16px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #5b7f8d;
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
  margin-top: 14px;
  padding: 14px 16px;
  border: 1px solid #dfeaf2;
  border-radius: 8px;
  background: #fbfdff;
}

.patient-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.patient-main strong {
  color: #21364a;
  font-size: 18px;
}

.patient-label {
  color: #6d8195;
  font-size: 13px;
}

.patient-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 16px;
  color: #63768a;
  font-size: 14px;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.patient-alert {
  margin-bottom: 16px;
}

.summary-bar {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 16px;
  overflow: hidden;
}

.summary-bar > div {
  padding: 18px 22px;
  border-right: 1px solid #e3ebf2;
}

.summary-bar > div:last-child {
  border-right: 0;
}

.summary-number {
  display: block;
  color: #21635d;
  font-size: 28px;
  font-weight: 700;
}

.summary-label {
  color: #708195;
  font-size: 13px;
}

.scale-section {
  padding: 22px 24px;
  margin-bottom: 16px;
}

.scale-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  margin-bottom: 18px;
  border-bottom: 1px solid #e3ebf2;
}

.scale-head h3 {
  margin: 0;
  color: #21364a;
  font-size: 20px;
}

.scale-remark {
  white-space: pre-line;
  margin: 8px 0 0;
  color: #6d7d8f;
  line-height: 1.7;
}

.question-item {
  padding: 16px 0;
  border-bottom: 1px dashed #dde7ef;
}

.question-item:last-child {
  border-bottom: 0;
}

.question-title {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 12px;
  color: #26394c;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.7;
}

.question-title span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 28px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e7f3eb;
  color: #317454;
}

.question-title p {
  margin: 0;
  white-space: pre-line;
}

.option-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.option-list :deep(.el-radio),
.option-list :deep(.el-checkbox) {
  height: auto;
  min-height: 44px;
  margin: 0;
  padding: 10px 12px;
  white-space: normal;
  align-items: flex-start;
  border-radius: 8px;
  background: #fbfdff;
}

.option-list :deep(.el-radio__label),
.option-list :deep(.el-checkbox__label) {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
  line-height: 1.55;
}

.option-label {
  color: #6f8396;
  font-weight: 700;
}

.submit-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 20px;
}

.submit-bar strong {
  color: #1f3447;
}

.submit-bar p {
  margin: 6px 0 0;
  color: #75869a;
}

@media (max-width: 900px) {
  .page-head,
  .scale-head,
  .submit-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .summary-bar,
  .option-list,
  .patient-meta {
    grid-template-columns: 1fr;
  }

  .summary-bar > div {
    border-right: 0;
    border-bottom: 1px solid #e3ebf2;
  }

  .summary-bar > div:last-child {
    border-bottom: 0;
  }
}
</style>