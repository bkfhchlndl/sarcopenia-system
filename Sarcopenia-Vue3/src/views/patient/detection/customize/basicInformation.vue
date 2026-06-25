<template>
  <div class="basic-info-container">
    <div class="page-shell">
      <!-- 页面头部 -->
      <header class="page-header">
        <div class="header-main">
          <el-button class="back-btn" text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回</span>
          </el-button>
          <div class="title-block">
            <span class="title-kicker">老年综合评估</span>
            <h1>基本情况评估</h1>
          </div>
        </div>

        <div class="patient-badge">
          <span class="badge-avatar">{{ (form.name || patientName || '患').slice(0, 1) }}</span>
          <div>
            <div class="badge-name">{{ form.name || patientName }}</div>
            <div class="badge-id">{{ patientId ? `#${patientId}` : '-' }}</div>
          </div>
        </div>
      </header>

      <!-- 填写进度概览 -->
      <section class="overview-panel">
        <div class="overview-item">
          <span class="overview-label">填写进度</span>
          <strong>{{ progressPercent }}%</strong>
        </div>
        <div class="overview-item">
          <span class="overview-label">已填写</span>
          <strong>{{ filledCount }} / {{ totalCount }}</strong>
        </div>
        <div
            class="overview-status"
            :style="{ color: progressLevel.color, background: progressLevel.bg, borderColor: progressLevel.color }"
        >
          {{ progressLevel.text }}
        </div>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: `${progressPercent}%` }"></div>
        </div>
      </section>

      <!-- 表单主体 -->
      <el-form :model="form" label-position="top" class="info-form">
        <!-- 身份信息模块 -->
        <section class="form-section" :class="{ 'is-done': isGroupDone(IDENTITY_FIELDS) }">
          <div class="section-head">
            <div class="section-title">
              <span class="section-index">01</span>
              <h2>身份信息</h2>
            </div>
            <span v-if="isGroupDone(IDENTITY_FIELDS)" class="section-done">
              <el-icon><Check /></el-icon>
            </span>
          </div>

          <div class="form-grid">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender" class="segmented-control">
                <el-radio-button label="1">男</el-radio-button>
                <el-radio-button label="2">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="出生日期">
              <el-date-picker
                  v-model="form.birthday"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择出生日期"
                  :disabled-date="disabledFutureDate"
                  class="full-control"
              />
            </el-form-item>
            <el-form-item label="病历号">
              <el-input v-model="form.medicalCaseNo" placeholder="请输入病历号" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" maxlength="18" placeholder="请输入身份证号" />
            </el-form-item>
            <el-form-item label="社会保障卡号">
              <el-input v-model="form.socialCardNo" placeholder="请输入社会保障卡号" />
            </el-form-item>
            <el-form-item label="身高">
              <div class="with-unit">
                <el-input-number
                    v-model="form.height"
                    :min="0"
                    :max="260"
                    :precision="1"
                    controls-position="right"
                    class="number-control"
                />
                <span>cm</span>
              </div>
            </el-form-item>
            <el-form-item label="体重">
              <div class="with-unit">
                <el-input-number
                    v-model="form.weight"
                    :min="0"
                    :max="300"
                    :precision="1"
                    controls-position="right"
                    class="number-control"
                />
                <span>kg</span>
              </div>
            </el-form-item>
          </div>
        </section>

        <!-- 人口学信息模块 -->
        <section class="form-section" :class="{ 'is-done': isGroupDone(PROFILE_FIELDS) }">
          <div class="section-head">
            <div class="section-title">
              <span class="section-index">02</span>
              <h2>人口学信息</h2>
            </div>
            <span v-if="isGroupDone(PROFILE_FIELDS)" class="section-done">
              <el-icon><Check /></el-icon>
            </span>
          </div>

          <div class="form-grid profile-grid">
            <el-form-item label="民族">
              <el-select
                  v-model="form.nation"
                  filterable
                  allow-create
                  default-first-option
                  clearable
                  placeholder="请选择或输入民族"
                  class="full-control"
              >
                <el-option v-for="item in nationOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="文化程度">
              <el-select
                  v-model="form.educationalLevel"
                  clearable
                  placeholder="请选择文化程度"
                  class="full-control"
              >
                <el-option v-for="item in educationOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="宗教信仰">
              <el-input v-model="form.religiousBelief" placeholder="无，或填写具体信仰" />
            </el-form-item>
            <el-form-item label="婚姻状况">
              <el-select
                  v-model="form.maritalStatus"
                  clearable
                  placeholder="请选择婚姻状况"
                  class="full-control"
              >
                <el-option v-for="item in maritalOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="家庭情况">
              <el-radio-group v-model="form.familyStatus" class="segmented-control">
                <el-radio-button v-for="item in familyOptions" :key="item" :label="item">
                  {{ item }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
        </section>

        <!-- 生活与照护情况模块 -->
        <section class="form-section" :class="{ 'is-done': isGroupDone(LIFE_FIELDS) }">
          <div class="section-head">
            <div class="section-title">
              <span class="section-index">03</span>
              <h2>生活与照护情况</h2>
            </div>
            <span v-if="isGroupDone(LIFE_FIELDS)" class="section-done">
              <el-icon><Check /></el-icon>
            </span>
          </div>

          <div class="choice-block">
            <div class="choice-title">居住情况</div>
            <el-checkbox-group
                v-model="liveSituationList"
                class="choice-grid compact-grid"
                @change="handleChoiceChange('liveSituation')"
            >
              <el-checkbox
                  v-for="item in liveOptions"
                  :key="item"
                  :label="getOptionValue(item)"
                  :class="{ 'has-fill': isOtherFillOption(item) }"
              >
                <span>{{ getOptionLabel(item) }}</span>
                <el-input
                    v-if="isOtherFillOption(item) && liveSituationList.includes(getOptionValue(item))"
                    v-model="liveSituationOther"
                    class="inline-other-input"
                    placeholder="请填写"
                    @click.stop
                />
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="choice-block">
            <div class="choice-title">接受照顾情况</div>
            <el-checkbox-group
                v-model="careSituationList"
                class="choice-grid"
                @change="handleChoiceChange('careSituation')"
            >
              <el-checkbox
                  v-for="item in careOptions"
                  :key="item"
                  :label="getOptionValue(item)"
                  :class="{ 'has-fill': isOtherFillOption(item) }"
              >
                <span>{{ getOptionLabel(item) }}</span>
                <el-input
                    v-if="isOtherFillOption(item) && careSituationList.includes(getOptionValue(item))"
                    v-model="careSituationOther"
                    class="inline-other-input"
                    placeholder="请填写"
                    @click.stop
                />
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </section>

        <!-- 保障与经济来源模块 -->
        <section class="form-section" :class="{ 'is-done': isGroupDone(PAYMENT_FIELDS) }">
          <div class="section-head">
            <div class="section-title">
              <span class="section-index">04</span>
              <h2>保障与经济来源</h2>
            </div>
            <span v-if="isGroupDone(PAYMENT_FIELDS)" class="section-done">
              <el-icon><Check /></el-icon>
            </span>
          </div>

          <div class="choice-block">
            <div class="choice-title">医疗费用支付方式</div>
            <el-checkbox-group
                v-model="payTypeList"
                class="choice-grid"
                @change="handleChoiceChange('payType')"
            >
              <el-checkbox
                  v-for="item in payOptions"
                  :key="item"
                  :label="getOptionValue(item)"
                  :class="{ 'has-fill': isOtherFillOption(item) }"
              >
                <span>{{ getOptionLabel(item) }}</span>
                <el-input
                    v-if="isOtherFillOption(item) && payTypeList.includes(getOptionValue(item))"
                    v-model="payTypeOther"
                    class="inline-other-input"
                    placeholder="请填写"
                    @click.stop
                />
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="choice-block">
            <div class="choice-title">经济来源</div>
            <el-checkbox-group
                v-model="incomeSourceList"
                class="choice-grid compact-grid"
                @change="handleChoiceChange('incomeSource')"
            >
              <el-checkbox
                  v-for="item in incomeOptions"
                  :key="item"
                  :label="getOptionValue(item)"
                  :class="{ 'has-fill': isOtherFillOption(item) }"
              >
                <span>{{ getOptionLabel(item) }}</span>
                <el-input
                    v-if="isOtherFillOption(item) && incomeSourceList.includes(getOptionValue(item))"
                    v-model="incomeSourceOther"
                    class="inline-other-input"
                    placeholder="请填写"
                    @click.stop
                />
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </section>
      </el-form>

      <!-- 底部提交栏 -->
      <footer class="submit-dock">
        <div class="dock-summary">
          <span class="dock-label">当前进度</span>
          <strong>{{ progressPercent }}%</strong>
          <span>{{ progressSuggest }}</span>
        </div>
        <el-button type="primary" size="large" :loading="submitting" @click="submitBasicInfo">
          <el-icon><Check /></el-icon>
          保存基本情况
        </el-button>
      </footer>
    </div>
  </div>
</template>

<script setup name="BasicInformation">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { selectPatientById, saveBasicInformation } from '@/api/patient.js'
import { selectBasicInformationScale } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
/** 身份信息字段分组 */
const IDENTITY_FIELDS = ['name', 'gender', 'birthday', 'medicalCaseNo', 'phone', 'idCard', 'socialCardNo', 'height', 'weight']
/** 人口学信息字段分组 */
const PROFILE_FIELDS = ['nation', 'educationalLevel', 'religiousBelief', 'maritalStatus', 'familyStatus']
/** 生活照护字段分组 */
const LIFE_FIELDS = ['liveSituation', 'careSituation']
/** 保障经济字段分组 */
const PAYMENT_FIELDS = ['payType', 'incomeSource']

/** 民族默认选项 */
const NATION_OPTIONS = [
  '汉族', '蒙古族', '回族', '藏族', '维吾尔族', '苗族', '彝族', '壮族', '布依族', '朝鲜族',
  '满族', '侗族', '瑶族', '白族', '土家族', '哈尼族', '哈萨克族', '傣族', '黎族', '傈僳族',
  '佤族', '畲族', '高山族', '拉祜族', '水族', '东乡族', '纳西族', '景颇族', '柯尔克孜族',
  '土族', '达斡尔族', '仫佬族', '羌族', '布朗族', '撒拉族', '毛南族', '仡佬族', '锡伯族',
  '阿昌族', '普米族', '塔吉克族', '怒族', '乌孜别克族', '俄罗斯族', '鄂温克族', '德昂族',
  '保安族', '裕固族', '京族', '塔塔尔族', '独龙族', '鄂伦春族', '赫哲族', '门巴族',
  '珞巴族', '基诺族'
]
/** 文化程度默认选项 */
const DEFAULT_EDUCATION_OPTIONS = ['文盲', '小学', '初中', '高中/技校/中专', '大学专科及以上', '不详']
/** 婚姻状况默认选项 */
const DEFAULT_MARITAL_OPTIONS = ['未婚', '已婚', '丧偶', '离婚', '未说明婚姻状况']
/** 家庭情况默认选项 */
const DEFAULT_FAMILY_OPTIONS = ['有子女', '无子女']
/** 居住情况默认选项 */
const DEFAULT_LIVE_OPTIONS = ['独居', '与配偶/伴侣居住', '与子女居住', '与父母居住', '与兄弟姐妹居住', '与其他亲属居住', '与非亲属关系的人居住', '养老机构']
/** 照护情况默认选项 */
const DEFAULT_CARE_OPTIONS = ['日常生活不需要人照顾', '日常生活需要人照顾', '配偶', '子女', '保姆', '机构护理员/工作人员', '其他']
/** 支付方式默认选项 */
const DEFAULT_PAY_OPTIONS = ['城镇职工基本医疗保险', '城镇居民基本医疗保险', '新型农村合作医疗', '医疗救助', '商业医疗保险', '公务员补助', '企业补充保险', '公费医疗及医疗照顾对象', '自费', '其他']
/** 收入来源默认选项 */
const DEFAULT_INCOME_OPTIONS = ['退休金/养老金', '子女补贴', '亲友资助', '国家普惠型补贴', '个人储蓄', '个人资产', '其他']

/** 本地存储完成状态前缀（根据来源区分） */
const STORAGE_DONE_PREFIX = computed(() => {
  if (route.query.from === 'sCustomize') {
    return 'sarcopenia_customize_done:'
  }
  return 'customize_done:'
})

/** 返回目标路径（根据来源区分） */
const backPath = computed(() => {
  if (route.query.from === 'sCustomize') {
    return '/patient/detection/sCustomize'
  }
  return '/patient/detection/customize'
})

// ==================== 响应式状态 ====================
/** 提交加载状态 */
const submitting = ref(false)
/** 量表配置数据 */
const scaleData = ref({ scaleId: null, scaleName: '', code: '', questionList: [] })

/** 表单数据 */
const form = reactive({
  id: null,
  name: '',
  medicalCaseNo: '',
  phone: '',
  gender: '',
  birthday: '',
  idCard: '',
  socialCardNo: '',
  height: null,
  weight: null,
  nation: '汉族',
  educationalLevel: '',
  religiousBelief: '无',
  maritalStatus: '',
  familyStatus: '',
  liveSituation: '',
  careSituation: '',
  payType: '',
  incomeSource: ''
})

/** 多选列表状态 */
const liveSituationList = ref([])
const careSituationList = ref([])
const payTypeList = ref([])
const incomeSourceList = ref([])

/** 其他项输入值 */
const liveSituationOther = ref('')
const careSituationOther = ref('')
const payTypeOther = ref('')
const incomeSourceOther = ref('')

/** 数据库动态选项映射 */
const dbOptionMap = reactive({
  educationalLevel: [],
  maritalStatus: [],
  familyStatus: [],
  liveSituation: [],
  careSituation: [],
  payType: [],
  incomeSource: []
})

// ==================== 计算属性 ====================
/** 患者ID（响应式获取路由参数） */
const patientId = computed(() => route.query.patientId || '')
/** 患者姓名（响应式获取路由参数） */
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

/** 民族选项 */
const nationOptions = computed(() => NATION_OPTIONS)
/** 文化程度选项（优先数据库配置） */
const educationOptions = computed(() => dbOptionMap.educationalLevel.length ? dbOptionMap.educationalLevel : DEFAULT_EDUCATION_OPTIONS)
/** 婚姻状况选项 */
const maritalOptions = computed(() => dbOptionMap.maritalStatus.length ? dbOptionMap.maritalStatus : DEFAULT_MARITAL_OPTIONS)
/** 家庭情况选项 */
const familyOptions = computed(() => dbOptionMap.familyStatus.length ? dbOptionMap.familyStatus : DEFAULT_FAMILY_OPTIONS)
/** 居住情况选项 */
const liveOptions = computed(() => dbOptionMap.liveSituation.length ? dbOptionMap.liveSituation : DEFAULT_LIVE_OPTIONS)
/** 照护情况选项 */
const careOptions = computed(() => dbOptionMap.careSituation.length ? dbOptionMap.careSituation : DEFAULT_CARE_OPTIONS)
/** 支付方式选项 */
const payOptions = computed(() => dbOptionMap.payType.length ? dbOptionMap.payType : DEFAULT_PAY_OPTIONS)
/** 收入来源选项 */
const incomeOptions = computed(() => dbOptionMap.incomeSource.length ? dbOptionMap.incomeSource : DEFAULT_INCOME_OPTIONS)

/** 总字段数 */
const totalCount = computed(() => IDENTITY_FIELDS.length + PROFILE_FIELDS.length + LIFE_FIELDS.length + PAYMENT_FIELDS.length)

/** 已填写字段数 */
const filledCount = computed(() => {
  const allFields = [...IDENTITY_FIELDS, ...PROFILE_FIELDS, ...LIFE_FIELDS, ...PAYMENT_FIELDS]
  return allFields.filter(field => hasValue(form[field])).length
})

/** 填写进度百分比 */
const progressPercent = computed(() => {
  if (!totalCount.value) return 0
  return Math.round((filledCount.value / totalCount.value) * 100)
})

/** 进度等级配置 */
const progressLevel = computed(() => {
  const percent = progressPercent.value
  if (percent === 0) return {text: '未填写', color: '#94a3b8', bg: '#f8fafc'}
  if (percent < 50) return {text: '已填写部分', color: '#f97316', bg: '#fff7ed'}
  if (percent < 90) return {text: '资料较完整', color: '#3b82f6', bg: '#eff6ff'}
  return {text: '资料完整', color: '#22c55e', bg: '#f0fdf4'}
})

/** 进度提示文案 */
const progressSuggest = computed(() => {
  if (progressPercent.value === 0) return '基本情况为可选补充信息，可根据患者实际情况填写后保存。'
  if (progressPercent.value >= 90) return '基本情况资料已较完整，可用于后续综合评估报告生成。'
  return '已填写的信息会保存到患者基本资料中，未填写项目不会影响保存。'
})

// ==================== 工具函数 ====================
/**
 * 拆分逗号/顿号分隔的字符串为数组
 * @param {string} value 原始字符串
 * @returns {string[]} 拆分后的数组
 */
function splitValue(value) {
  if (!value) return []
  return String(value).split(/[、,，]/).map(item => item.trim()).filter(Boolean)
}

/**
 * 标准化「其他」项的文本内容，去除前缀
 * @param {string} value 原始文本
 * @returns {string} 标准化后的文本
 */
function normalizeOtherText(value) {
  return String(value || '')
      .replace(/^其他[：:]/, '')
      .replace(/^其他/, '')
      .trim()
}

/**
 * 标准化选项值，统一「其他」项的取值
 * @param {string} value 原始选项值
 * @returns {string} 标准化后的值
 */
function normalizeOtherValue(value) {
  const text = String(value || '').trim()
  if (/^其他[：:]/.test(text)) return '其他'
  return text
}

/**
 * 从存储字符串恢复多选列表与「其他」输入值
 * @param {string} value 存储的字符串
 * @param {Ref} otherRef 其他输入值的响应式引用
 * @returns {string[]} 多选列表数组
 */
function restoreListValue(value, otherRef) {
  const list = splitValue(value)
  const normalizedList = []

  list.forEach(item => {
    if (/^其他[：:]/.test(item)) {
      normalizedList.push('其他')
      otherRef.value = normalizeOtherText(item)
    } else {
      normalizedList.push(normalizeOtherValue(item))
    }
  })

  return normalizedList
}

/**
 * 将多选列表与「其他」值拼接为存储字符串
 * @param {string[]} list 多选列表
 * @param {string} otherText 「其他」输入值
 * @returns {string} 拼接后的字符串
 */
function joinValue(list, otherText = '') {
  if (!Array.isArray(list)) return ''
  return list
      .map(item => {
        const value = normalizeOtherValue(item)
        if (value === '其他' && String(otherText || '').trim()) {
          return `其他：${String(otherText).trim()}`
        }
        return value
      })
      .filter(Boolean)
      .join('、')
}

/**
 * 判断选项是否为需要填写补充内容的「其他」项
 * @param {string} option 选项文本
 * @returns {boolean}
 */
function isOtherFillOption(option) {
  const text = String(option || '').trim()
  return text === '其他' || /^其他/.test(text) && text.includes('填写')
}

/**
 * 获取选项的实际存储值
 * @param {string} option 选项文本
 * @returns {string}
 */
function getOptionValue(option) {
  return isOtherFillOption(option) ? '其他' : String(option || '').trim()
}

/**
 * 获取选项的展示文本
 * @param {string} option 选项文本
 * @returns {string}
 */
function getOptionLabel(option) {
  return isOtherFillOption(option) ? '其他' : String(option || '').trim()
}

/**
 * 判断字段是否有有效值
 * @param {*} value 字段值
 * @returns {boolean}
 */
function hasValue(value) {
  return value !== undefined && value !== null && value !== '' && value !== 0
}

/**
 * 日期选择器禁用未来日期
 * @param {Date} time 日期
 * @returns {boolean}
 */
function disabledFutureDate(time) {
  return time.getTime() > Date.now()
}

/**
 * 标准化接口返回的患者数据
 * @param {*} res 接口响应
 * @returns {object|null}
 */
function normalizePatient(res) {
  if (res?.data && typeof res.data === 'object') return res.data
  if (res && typeof res === 'object') return res
  return null
}

/**
 * 标准化接口返回的列表数据
 * @param {*} res 接口响应
 * @returns {Array}
 */
function normalizeListResponse(res) {
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.rows)) return res.rows
  if (Array.isArray(res)) return res
  return []
}

/**
 * 从题目配置中提取选项内容列表
 * @param {object} question 题目对象
 * @returns {string[]}
 */
function getOptionContentList(question) {
  return (question?.optionList || [])
      .map(option => option.content || option.label)
      .filter(Boolean)
}

/**
 * 根据字段匹配对应的量表题目
 * @param {string} field 字段名
 * @returns {object|undefined} 匹配到的题目
 */
function matchQuestionByField(field) {
  const keywordMap = {
    educationalLevel: ['文化程度', '受教育', '教育程度'],
    maritalStatus: ['婚姻'],
    familyStatus: ['家庭情况', '家庭状况', '子女'],
    liveSituation: ['居住'],
    careSituation: ['照顾', '照护', '照料'],
    payType: ['支付', '医保', '医疗费用'],
    incomeSource: ['经济来源', '收入来源']
  }
  const keywords = keywordMap[field] || []
  return scaleData.value.questionList.find(item => {
    const title = String(item.title || '')
    return keywords.some(keyword => title.includes(keyword))
  })
}

// ==================== 业务方法 ====================
/**
 * 将所有多选列表同步到表单对应字段
 */
function syncListFieldsToForm() {
  form.liveSituation = joinValue(liveSituationList.value, liveSituationOther.value)
  form.careSituation = joinValue(careSituationList.value, careSituationOther.value)
  form.payType = joinValue(payTypeList.value, payTypeOther.value)
  form.incomeSource = joinValue(incomeSourceList.value, incomeSourceOther.value)
}

/**
 * 多选选项变化处理
 * @param {string} field 字段名
 */
function handleChoiceChange(field) {
  const mapping = {
    liveSituation: {list: liveSituationList, other: liveSituationOther},
    careSituation: {list: careSituationList, other: careSituationOther},
    payType: {list: payTypeList, other: payTypeOther},
    incomeSource: {list: incomeSourceList, other: incomeSourceOther}
  }

  const target = mapping[field]
  if (target && !target.list.value.includes('其他')) {
    target.other.value = ''
  }
}

/**
 * 判断一组字段是否全部填写完成
 * @param {string[]} fields 字段名数组
 * @returns {boolean}
 */
function isGroupDone(fields) {
  return fields.every(field => hasValue(form[field]))
}

/**
 * 应用数据库动态选项配置
 */
function applyDbOptions() {
  Object.keys(dbOptionMap).forEach(field => {
    const question = matchQuestionByField(field)
    const options = getOptionContentList(question)
    if (options.length) {
      dbOptionMap[field] = options
    }
  })
}

/**
 * 加载量表配置数据
 */
async function loadScaleData() {
  try {
    const list = normalizeListResponse(await selectBasicInformationScale())
    if (list.length) {
      scaleData.value = list[0]
      applyDbOptions()
    }
  } catch (error) {
    console.error('加载基本情况题目选项失败', error)
  }
}

/**
 * 加载患者已有基本信息
 */
async function loadPatientInfo() {
  if (!patientId.value) return
  try {
    const patient = normalizePatient(await selectPatientById({patientId: Number(patientId.value)}))
    if (!patient) return

    form.id = patient.id || Number(patientId.value)
    form.name = patient.name || ''
    form.medicalCaseNo = patient.medicalCaseNo || ''
    form.phone = patient.phone || ''
    form.gender = patient.gender || ''
    form.birthday = patient.birthday || ''
    form.idCard = patient.idCard || ''
    form.socialCardNo = patient.socialCardNo || ''
    form.height = patient.height === undefined || patient.height === null ? null : Number(patient.height)
    form.weight = patient.weight === undefined || patient.weight === null ? null : Number(patient.weight)
    form.nation = patient.nation || '汉族'
    form.educationalLevel = patient.educationalLevel || ''
    form.religiousBelief = patient.religiousBelief || '无'
    form.maritalStatus = patient.maritalStatus || ''
    form.familyStatus = patient.familyStatus || ''
    form.liveSituation = patient.liveSituation || ''
    form.careSituation = patient.careSituation || ''
    form.payType = patient.payType || ''
    form.incomeSource = patient.incomeSource || ''

    // 恢复多选列表状态
    liveSituationList.value = restoreListValue(form.liveSituation, liveSituationOther)
    careSituationList.value = restoreListValue(form.careSituation, careSituationOther)
    payTypeList.value = restoreListValue(form.payType, payTypeOther)
    incomeSourceList.value = restoreListValue(form.incomeSource, incomeSourceOther)
  } catch (error) {
    console.error('加载患者基本情况失败', error)
    ElMessage.error('加载患者基本情况失败')
  }
}

/**
 * 表单提交前校验
 * @returns {boolean}
 */
function validateForm() {
  if (!patientId.value) {
    ElMessage.warning('未获取到患者信息')
    return false
  }
  return true
}

/**
 * 构建提交的患者数据
 * @returns {object}
 */
function buildPatientPayload() {
  return {
    id: Number(patientId.value),
    name: form.name || null,
    medicalCaseNo: form.medicalCaseNo || null,
    phone: form.phone || null,
    gender: form.gender || null,
    birthday: form.birthday || null,
    idCard: form.idCard || null,
    socialCardNo: form.socialCardNo || null,
    height: form.height,
    weight: form.weight,
    nation: form.nation || null,
    educationalLevel: form.educationalLevel || null,
    religiousBelief: form.religiousBelief || null,
    maritalStatus: form.maritalStatus || null,
    familyStatus: form.familyStatus || null,
    liveSituation: form.liveSituation || null,
    careSituation: form.careSituation || null,
    payType: form.payType || null,
    incomeSource: form.incomeSource || null
  }
}

/**
 * 提交保存基本信息
 */
async function submitBasicInfo() {
  if (submitting.value || !validateForm()) return
  submitting.value = true

  try {
    const payload = buildPatientPayload()
    await saveBasicInformation(payload)

    // 标记本地完成状态
    try {
      const prefix = STORAGE_DONE_PREFIX.value
      localStorage.setItem(`${prefix}${patientId.value}:disease_history`, '1')
      localStorage.setItem(`${prefix}${patientId.value}:basic_information`, '1')
    } catch (e) {
    }

    ElMessage.success('基本情况保存成功')
    setTimeout(() => {
      router.push({
        path: backPath.value,
        query: {
          patientId: patientId.value,
          patientName: form.name || patientName.value
        }
      })
    }, 700)
  } catch (error) {
    console.error('保存基本情况失败', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 返回上一页
 */
function goBack() {
  router.push({ path: backPath.value, query: route.query })
}

// ==================== 副作用监听 ====================
/** 监听多选列表与其他输入变化，自动同步到表单字段 */
watch(
    [
      liveSituationList, liveSituationOther,
      careSituationList, careSituationOther,
      payTypeList, payTypeOther,
      incomeSourceList, incomeSourceOther
    ],
    () => {
      syncListFieldsToForm()
    },
    {deep: true}
)

// ==================== 生命周期 ====================
onMounted(() => {
  loadScaleData()
  loadPatientInfo()
})
</script>

<style scoped lang="scss">
.basic-info-container {
  min-height: 100vh;
  padding: 22px 24px 48px;
  background: #eef3f8;
  box-sizing: border-box;

  .page-shell {
    max-width: 1160px;
    margin: 0 auto;
    padding-bottom: 8px;
  }
}

/* ===== 页面头部 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 20px 22px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #dbe7f3;
  box-shadow: 0 10px 28px rgba(30, 64, 105, 0.08);
  margin-bottom: 14px;

  .header-main {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-btn {
    height: 34px;
    padding: 0 10px;
    font-size: 14px;
    color: #475569;

    &:hover {
      color: #1d4ed8;
      background: #eff6ff;
    }
  }

  .title-block {
    .title-kicker {
      display: inline-block;
      margin-bottom: 4px;
      color: #64748b;
      font-size: 12px;
      font-weight: 700;
      letter-spacing: 1px;
    }

    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: #0f172a;
      letter-spacing: 2px;
    }
  }

  .patient-badge {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 176px;
    padding: 10px 16px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;

    .badge-avatar {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: #2563eb;
      color: #fff;
      font-size: 17px;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }

    .badge-name {
      font-size: 15px;
      font-weight: 700;
      color: #0f172a;
    }

    .badge-id {
      margin-top: 2px;
      font-size: 12px;
      color: #64748b;
    }
  }
}

/* ===== 概览进度面板 ===== */
.overview-panel {
  display: grid;
  grid-template-columns: 170px 170px minmax(130px, auto) 1fr;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #dbe7f3;
  box-shadow: 0 6px 18px rgba(30, 64, 105, 0.05);
  margin-bottom: 16px;

  .overview-item {
    display: flex;
    align-items: baseline;
    gap: 8px;
    padding-right: 14px;
    border-right: 1px solid #e2e8f0;

    strong {
      color: #0f172a;
      font-size: 24px;
      font-family: Consolas, monospace;
      line-height: 1;
    }
  }

  .overview-label {
    color: #64748b;
    font-size: 13px;
    font-weight: 700;
  }

  .overview-status {
    justify-self: start;
    border: 1px solid;
    border-radius: 999px;
    padding: 7px 18px;
    font-size: 14px;
    font-weight: 700;
  }

  .progress-track {
    height: 10px;
    border-radius: 999px;
    overflow: hidden;
    background: #e2e8f0;

    .progress-fill {
      height: 100%;
      border-radius: 999px;
      background: #2563eb;
      transition: width 0.35s ease;
    }
  }
}

/* ===== 表单通用样式 ===== */
.info-form {
  display: flex;
  flex-direction: column;
  gap: 16px;

  :deep(.el-form-item__label) {
    padding-bottom: 7px;
    color: #334155;
    font-size: 13px;
    font-weight: 700;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper),
  :deep(.el-input-number .el-input__wrapper) {
    min-height: 42px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px #dbe7f3 inset;
    background: #fbfdff;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    box-shadow: 0 0 0 1px #2563eb inset;
  }
}

/* ===== 表单分区 ===== */
.form-section {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #dbe7f3;
  box-shadow: 0 8px 24px rgba(30, 64, 105, 0.06);
  overflow: hidden;

  &.is-done {
    border-color: #93c5fd;
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
    height: 62px;
    padding: 0 20px;
    background: #f8fafc;
    border-bottom: 1px solid #e5edf6;

    .section-title {
      display: flex;
      align-items: center;
      gap: 12px;

      h2 {
        margin: 0;
        color: #0f172a;
        font-size: 17px;
        font-weight: 700;
        letter-spacing: 1px;
      }
    }

    .section-index {
      width: 34px;
      height: 34px;
      border-radius: 8px;
      background: #e2e8f0;
      color: #475569;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-family: Consolas, monospace;
      font-size: 13px;
      font-weight: 800;
    }

    &.is-done .section-index {
      background: #2563eb;
      color: #fff;
    }

    .section-done {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: #22c55e;
      color: #fff;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 15px;
      flex-shrink: 0;
    }
  }
}

/* ===== 表单网格布局 ===== */
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px 22px;
  padding: 22px;
}

.profile-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.full-control,
.number-control {
  width: 100%;
}

/* ===== 带单位的数字输入 ===== */
.with-unit {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 38px;
  align-items: center;
  gap: 8px;

  span {
    color: #64748b;
    font-size: 13px;
    font-weight: 700;
  }
}

/* ===== 分段单选按钮 ===== */
.segmented-control {
  display: flex;
  width: 100%;

  :deep(.el-radio-button) {
    flex: 1;
    min-width: 0;
  }

  :deep(.el-radio-button__inner) {
    width: 100%;
    height: 42px;
    padding: 0 10px;
    border-radius: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    white-space: normal;
    line-height: 1.25;
  }
}

/* ===== 多选选项块 ===== */
.choice-block {
  padding: 22px;

  & + .choice-block {
    padding-top: 0;
  }

  .choice-title {
    margin-bottom: 12px;
    color: #334155;
    font-size: 14px;
    font-weight: 800;
  }
}

.choice-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
}

.compact-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.choice-grid :deep(.el-checkbox) {
  width: 100%;
  box-sizing: border-box;
  margin-right: 0;
  padding: 11px 14px;
  min-height: 44px;
  height: auto;
  border-radius: 8px;
  border: 1px solid #dbe7f3;
  background: #fbfdff;
  transition: all 0.15s ease;
  white-space: normal;
  line-height: 1.5;
  align-items: flex-start;

  &:hover {
    border-color: #93c5fd;
    background: #eff6ff;
  }

  &.is-checked {
    border-color: #2563eb;
    background: #eff6ff;
    box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.18);
  }

  .el-checkbox__label {
    flex: 1;
    color: #1e293b;
    font-size: 13.5px;
    line-height: 1.45;
    min-width: 0;
    word-break: break-word;
  }

  &.is-checked .el-checkbox__label {
    color: #1d4ed8;
    font-weight: 700;
  }

  &.has-fill {
    padding-top: 9px;
    padding-bottom: 9px;

    .el-checkbox__label {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }
}

.inline-other-input {
  flex: 1;
  min-width: 120px;

  :deep(.el-input__wrapper) {
    min-height: 30px;
    border-radius: 6px;
    background: #fff;
    box-shadow: 0 0 0 1px #cbd5e1 inset;
  }

  :deep(.el-input__inner) {
    height: 30px;
    font-size: 13px;
  }
}

.choice-grid :deep(.el-checkbox.has-fill.is-checked .inline-other-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px #60a5fa inset;
}

/* ===== 底部提交栏 ===== */
.submit-dock {
  position: sticky;
  bottom: 16px;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-top: 18px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #dbe7f3;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(30, 64, 105, 0.12);
  backdrop-filter: blur(8px);

  .dock-summary {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
    color: #475569;
    font-size: 13px;

    strong {
      color: #1d4ed8;
      font-size: 24px;
      font-family: Consolas, monospace;
    }

    span:last-child {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .dock-label {
    color: #64748b;
    font-weight: 700;
  }

  :deep(.el-button--primary) {
    min-width: 168px;
    height: 46px;
    border: none;
    border-radius: 8px;
    background: #2563eb;
    box-shadow: 0 8px 18px rgba(37, 99, 235, 0.22);
    font-size: 16px;
    font-weight: 700;
    letter-spacing: 1px;

    &:hover {
      background: #1d4ed8;
      transform: translateY(-1px);
    }
  }
}

/* ===== 响应式适配 ===== */
@media (max-width: 1100px) {
  .overview-panel,
  .form-grid,
  .profile-grid,
  .compact-grid {
    grid-template-columns: 1fr;
  }

  .choice-grid {
    grid-template-columns: 1fr;
  }

  .overview-item {
    border-right: 0;
    padding-right: 0;
  }
}

@media (max-width: 768px) {
  .basic-info-container {
    padding: 12px;
  }

  .page-header,
  .submit-dock {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .patient-badge {
    min-width: 0;
  }

  .form-grid,
  .choice-block {
    padding: 16px;
  }

  .dock-summary span:last-child {
    white-space: normal;
  }
}
</style>
