<template>
  <div class="comprehensive-container">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="action-right">
        <el-button plain @click="goCustomAssessment">
          <el-icon><Setting /></el-icon>
          自定义项目
        </el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerateReport">
          <el-icon><DocumentChecked /></el-icon>
          生成报告
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <div class="loading-text">正在加载评估数据...</div>
    </div>

    <!-- 空数据状态 -->
    <div v-else-if="!reportData" class="empty-box">
      <el-empty description="暂无评估数据，请先完成评估">
        <el-button type="primary" @click="goBack">返回评估</el-button>
      </el-empty>
    </div>

    <!-- 报告内容 -->
    <div v-else class="report-wrapper">
      <!-- 报告标题 -->
      <div class="report-header">
        <h1 class="report-title">老年综合评估结果</h1>
        <p class="report-subtitle">Comprehensive Geriatric Assessment Report</p>
      </div>

      <!-- 患者基本信息 -->
      <div class="patient-info-card">
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">姓　　名：</span>
            <span class="info-value">{{ patientName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性　　别：</span>
            <span class="info-value">{{ reportData.gender === '1' ? '男' : reportData.gender === '2' ? '女' : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">出生日期：</span>
            <span class="info-value">
              {{ formatBirthday(reportData.birthday) || '-' }}
              <span v-if="calcAge(reportData.birthday) !== null">
                ({{ calcAge(reportData.birthday) }}岁)
              </span>
            </span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">病历号：</span>
            <span class="info-value">{{ reportData.caseNo || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ reportData.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">评估日期：</span>
            <span class="info-value">{{ formatDate(reportData.createTime) }}</span>
          </div>
        </div>
      </div>

      <section class="basic-history-section">
        <div class="module-title">
          <span>一、基本家庭情况</span>
          <el-button
              class="basic-info-btn"
              :type="isBasicInformationFilled ? 'success' : 'primary'"
              :plain="!isBasicInformationFilled"
              size="large"
              @click="goBasicInformationAssessment"
          >
            {{ isBasicInformationFilled ? '已填写过' : '去填写基本情况' }}
          </el-button>
        </div>
      </section>

      <!-- 评估结果表格 -->
      <div class="result-table-wrapper">
        <table class="result-table">
          <thead>
          <tr>
            <th class="col-index">序号</th>
            <th class="col-item">评估项目</th>
            <th class="col-screen">筛查结果</th>
            <th class="col-result">评估结果</th>
            <th class="col-action">操作</th>
          </tr>
          </thead>
          <tbody>
          <!-- 躯体功能 -->
          <tr v-if="physicalItems.length > 0" class="category-row">
            <td colspan="5" class="category-cell">二、躯体功能</td>
          </tr>
          <tr v-for="(item, idx) in physicalItems" :key="item.scaleCode" class="data-row">
            <td class="col-index">{{ String(idx + 1).padStart(2, '0') }}</td>
            <td class="col-item">{{ getItemName(item) }}</td>
            <td class="col-screen">
              <el-tag v-if="getResultType(item) === 'positive'" type="danger" effect="dark" size="large">阳 性</el-tag>
              <el-tag v-else-if="getResultType(item) === 'negative'" type="success" effect="dark" size="large">阴 性</el-tag>
              <el-tag v-else type="info" effect="light" size="large">未完成</el-tag>
            </td>
            <td class="col-result">
              <template v-if="getResultType(item) === 'positive'">
                <div class="result-detail" :class="hasSpecialAssessment(item) ? 'special-text' : 'positive-text'">
                  {{ getResultDisplayText(item) }}
                </div>
              </template>
              <template v-else-if="getResultType(item) === 'negative'">
                <template v-if="hasSpecialAssessment(item)">
                  <div class="result-detail special-text">{{ getResultDisplayText(item) }}</div>
                </template>
                <span v-else class="result-normal">无需进一步评估</span>
              </template>
              <template v-else>
                <span class="result-pending">请完成该项目筛查</span>
              </template>
            </td>
            <td class="col-action">
              <el-button v-if="getResultType(item) === 'positive' && requiresSpecialAssessment(item)" type="primary" link size="small" :disabled="hasSpecialAssessment(item)" @click="navigateTo(item)" :icon="ArrowRight">
                {{ getSpecialActionText(item) }}
              </el-button>
              <span v-else-if="getResultType(item) === 'positive'" class="muted">无需进行专项评估</span>
              <el-button v-else-if="getResultType(item) === 'pending'" type="success" plain size="small" @click="navigateTo(item)" :icon="DocumentAdd">
                前往答题
              </el-button>
              <span v-else class="muted">-</span>
            </td>
          </tr>

          <!-- 精神心理 -->
          <tr v-if="mentalItems.length > 0" class="category-row">
            <td colspan="5" class="category-cell">三、精神心理</td>
          </tr>
          <tr v-for="(item, idx) in mentalItems" :key="item.scaleCode" class="data-row">
            <td class="col-index">{{ String(physicalItems.length + idx + 1).padStart(2, '0') }}</td>
            <td class="col-item">{{ getItemName(item) }}</td>
            <td class="col-screen">
              <el-tag v-if="getResultType(item) === 'positive'" type="danger" effect="dark" size="large">阳 性</el-tag>
              <el-tag v-else-if="getResultType(item) === 'negative'" type="success" effect="dark" size="large">阴 性</el-tag>
              <el-tag v-else type="info" effect="light" size="large">未完成</el-tag>
            </td>
            <td class="col-result">
              <template v-if="getResultType(item) === 'positive'">
                <div class="result-detail" :class="hasSpecialAssessment(item) ? 'special-text' : 'positive-text'">
                  {{ getResultDisplayText(item) }}
                </div>
              </template>
              <template v-else-if="getResultType(item) === 'negative'">
                <template v-if="hasSpecialAssessment(item)">
                  <div class="result-detail special-text">{{ getResultDisplayText(item) }}</div>
                </template>
                <span v-else class="result-normal">无需进一步评估</span>
              </template>
              <template v-else>
                <span class="result-pending">请完成该项目筛查</span>
              </template>
            </td>
            <td class="col-action">
              <el-button v-if="getResultType(item) === 'positive' && requiresSpecialAssessment(item)" type="primary" link size="small" :disabled="hasSpecialAssessment(item)" @click="navigateTo(item)" :icon="ArrowRight">
                {{ getSpecialActionText(item) }}
              </el-button>
              <span v-else-if="getResultType(item) === 'positive'" class="muted">无需进行专项评估</span>
              <el-button v-else-if="getResultType(item) === 'pending'" type="success" plain size="small" @click="navigateTo(item)" :icon="DocumentAdd">
                前往答题
              </el-button>
              <span v-else class="muted">-</span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 综合诊断意见 -->
      <div class="conclusion-card">
        <div class="conclusion-title">综合诊断意见</div>
        <div class="conclusion-content">
          <p v-if="positiveItems.length > 0">
            经老年综合评估筛查，患者以下项目筛查结果为阳性，建议进行相应专项评估：
          </p>
          <ul v-if="positiveItems.length > 0" class="conclusion-list">
            <li v-for="(item, idx) in positiveItems" :key="item.scaleCode">
              <strong>{{ idx + 1 }}. {{ getItemName(item) }}：</strong>{{ getSuggestion(item) }}
              <el-button v-if="requiresSpecialAssessment(item)" type="danger" link :disabled="hasSpecialAssessment(item)" @click="navigateTo(item)" size="small" style="margin-left: 8px;">
                [{{ getSpecialActionText(item) }}]
              </el-button>
            </li>
          </ul>
          <p v-else>
            本次老年综合评估各项目筛查结果均为阴性，整体状态良好，建议定期随访观察。
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="ComprehensiveReport">
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, DocumentChecked, Loading, ArrowRight, DocumentAdd, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { generateCgaReport, getFullCgaReportByPatient } from '@/api/cgaReport.js'
import { selectCgaRecordList } from '@/api/cga.js'
import { selectPatientById } from '@/api/patient.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
/** 量表编码映射：名称/分类/阳性建议/跳转路径 */
const SCALE_MAP = {
  // 躯体功能
  'basic_adl': { category: 'physical', name: '基本日常生活', detail: '基本日常生活活动能力筛查阳性', suggestion: '进行基本日常生活活动能力评估（Barthel指数）', path: '/patient/detection/customize/dailyLife' },
  'instrumental_adl': { category: 'physical', name: '工具性日常生活', detail: '工具性日常生活活动能力筛查阳性', suggestion: '进行工具性日常生活活动能力评估（Lawton量表）', path: '/patient/detection/customize/instrumentalityLife' },
  'exercise_function': { category: 'physical', name: '运动功能', detail: '运动功能筛查阳性', suggestion: '进行运动功能评估（SPPB简易体能状况量表）', path: '/patient/detection/customize/sppb' },
  'vision_assessment': { category: 'physical', name: '视力视觉功能', detail: '视力或视觉功能筛查阳性', suggestion: '进行视力与视觉功能综合评估', path: '/patient/detection/customize/visualAcuity' },
  'hearing_assessment': { category: 'physical', name: '听力', detail: '听力筛查阳性', suggestion: '进行听力与耳语试验综合评估', path: '/patient/detection/customize/listening' },
  'home_environment': { category: 'physical', name: '居家环境', detail: '居家环境筛查提示风险', suggestion: '进行居家环境安全评估与干预', path: '/patient/detection/customize/homeEnvironment' },
  'frail_assessment': { category: 'physical', name: '衰弱', detail: 'FRAIL衰弱评估', suggestion: '进行FRAIL衰弱评估量表评估', path: '/patient/detection/customize/frail' },
  'incontinence_screen': { category: 'physical', name: '尿失禁', detail: '尿失禁筛查阳性', suggestion: '进行尿失禁评估（国际尿失禁咨询委员会问卷表简表）', path: '/patient/detection/customize/urinaryIncontinence' },
  'constipation_screen': { category: 'physical', name: '便秘', detail: '便秘筛查阳性', suggestion: '进行老年人便秘症状评估', path: '/patient/detection/customize/constipation' },
  'chronic_pain_screen': { category: 'physical', name: '慢性疼痛', detail: '慢性疼痛筛查阳性', suggestion: '进行慢性疼痛综合评估（NRS数字评定量表+CPOT行为评估量表）', path: '/patient/detection/customize/chronicPain' },
  'pressure_injury_risk': { category: 'physical', name: '压力性损伤风险', detail: '压力性损伤风险筛查阳性', suggestion: '进行压力性损伤风险评估（Braden量表）', path: '/patient/detection/customize/pressureInjury' },
  'water_swallowing_test': { category: 'physical', name: '吞咽功能', detail: '吞咽障碍筛查阳性', suggestion: '进行吞咽障碍评估（洼田饮水试验）', path: '/patient/detection/customize/swallow' },
  'nutrition_risk_screen': { category: 'physical', name: '营养风险', detail: '营养风险筛查阳性', suggestion: '进行营养风险终筛（NRS-2002）及微营养评定（MNA-SF）', path: '/patient/detection/customize/nutrition' },
  'fall_risk_screen': { category: 'physical', name: '跌倒风险', detail: '跌倒风险筛查阳性', suggestion: '进行跌倒风险评估并制定预防措施', path: '/patient/detection/customize/fall' },
  'polypharmacy_assessment': { category: 'physical', name: '多重用药', detail: '存在多重用药风险', suggestion: '进行Beers不适当用药筛查，必要时药师会诊调整用药', path: '/patient/detection/customize/medication' },
  'sarcopenia_screen': { category: 'physical', name: '微营养', detail: '微营养筛查阳性', suggestion: '进行微营养状况评估（MNA-SF简版微营养评定量表）', path: '/patient/detection/customize/microNutrition' },

  // 精神心理
  'cognition_screen': { category: 'mental', name: '认知功能', detail: '认知功能障碍筛查阳性', suggestion: '进行认知功能评估（简易智力评估量表）', path: '/patient/detection/customize/cognition' },
  'depression_screen': { category: 'mental', name: '抑郁', detail: '抑郁筛查阳性', suggestion: '进行老年抑郁评估（GDS-15简版）', path: '/patient/detection/customize/depression' },
  'anxiety_screen': { category: 'mental', name: '焦虑', detail: '焦虑筛查阳性', suggestion: '进行焦虑障碍评估（GAD-7广泛性焦虑障碍量表）', path: '/patient/detection/customize/anxiety' },
  'sleep_disorder_screen': { category: 'mental', name: '睡眠障碍', detail: '睡眠障碍筛查阳性', suggestion: '进行睡眠障碍评估（阿森斯失眠量表AIS）', path: '/patient/detection/customize/insomnia' },
  'delirium_assessment': { category: 'mental', name: '谵妄', detail: '谵妄评估阳性', suggestion: '进行谵妄（意识模糊）评估', path: '/patient/detection/customize/delirium' },
}

/** 专项评估量表ID映射：用于判断患者是否已完成专项评估 */
const SPECIAL_SCALE_MAP = {
  basic_adl: [22],
  instrumental_adl: [23],
  exercise_function: [24],
  vision_assessment: [25],
  hearing_assessment: [26],
  home_environment: [27],
  cognition_screen: [28],
  frail_assessment: [29],
  incontinence_screen: [30],
  constipation_screen: [31],
  sleep_disorder_screen: [32],
  chronic_pain_screen: [33],
  pressure_injury_risk: [34],
  water_swallowing_test: [35],
  nutrition_risk_screen: [36],
  micro_nutrition: [37],
  delirium_assessment: [39],
  depression_screen: [40],
  fall_risk_screen: [41],
  polypharmacy_assessment: [42],
  anxiety_screen: [43],
  sarcopenia_screen: [37]
}

/** 全量量表排序配置（按业务要求顺序展示） */
const FULL_SCALE_LIST = [
  // 躯体功能（16项）
  { code: 'basic_adl', order: 1, group: '躯体功能' },
  { code: 'instrumental_adl', order: 2, group: '躯体功能' },
  { code: 'exercise_function', order: 3, group: '躯体功能' },
  { code: 'vision_assessment', order: 4, group: '躯体功能' },
  { code: 'hearing_assessment', order: 5, group: '躯体功能' },
  { code: 'home_environment', order: 6, group: '躯体功能' },
  { code: 'frail_assessment', order: 7, group: '躯体功能' },
  { code: 'incontinence_screen', order: 8, group: '躯体功能' },
  { code: 'constipation_screen', order: 9, group: '躯体功能' },
  { code: 'chronic_pain_screen', order: 10, group: '躯体功能' },
  { code: 'pressure_injury_risk', order: 11, group: '躯体功能' },
  { code: 'water_swallowing_test', order: 12, group: '躯体功能' },
  { code: 'nutrition_risk_screen', order: 13, group: '躯体功能' },
  { code: 'fall_risk_screen', order: 14, group: '躯体功能' },
  { code: 'polypharmacy_assessment', order: 15, group: '躯体功能' },
  { code: 'sarcopenia_screen', order: 16, group: '躯体功能' },
  // 精神心理（5项）
  { code: 'cognition_screen', order: 17, group: '精神心理' },
  { code: 'sleep_disorder_screen', order: 18, group: '精神心理' },
  { code: 'delirium_assessment', order: 19, group: '精神心理' },
  { code: 'depression_screen', order: 20, group: '精神心理' },
  { code: 'anxiety_screen', order: 21, group: '精神心理' }
]

/** 直接展示结果、无需跳转专项评估的量表编码 */
const DIRECT_RESULT_CODES = ['frail_assessment', 'cognition_screen', 'delirium_assessment']

// ==================== 响应式状态 ====================
/** 页面加载状态 */
const loading = ref(true)
/** 报告生成中状态 */
const generating = ref(false)
/** 报告完整数据 */
const reportData = ref(null)
/** 患者评估记录列表 */
const cgaRecordList = ref([])
/** 患者基本详情信息 */
const patientBasicInfo = ref(null)

// ==================== 计算属性 ====================
/** 患者ID（优先取报告数据，兜底路由参数） */
const patientId = computed(() => {
  const r = reportData.value
  return r?.patientId || r?.id || r?.pId || route.query.patientId
})

/** 患者姓名（优先取报告数据，兜底路由参数） */
const patientName = computed(() => {
  const r = reportData.value
  return r?.patientName || r?.name || r?.patientName2 || route.query.patientName || route.query.name || '患者'
})

/** 已完成答题的项目列表（自定义报告用，当前暂未启用） */
const answeredItems = computed(() => {
  const all = [...physicalItems.value, ...mentalItems.value]
  return all.filter(item => item._answered !== false)
})

/** 基本信息是否已填写 */
const isBasicInformationFilled = computed(() => {
  const patient = patientBasicInfo.value || {}
  const fields = [
    'phone', 'idCard', 'socialCardNo', 'height', 'weight',
    'nation', 'educationalLevel', 'religiousBelief', 'maritalStatus',
    'familyStatus', 'liveSituation', 'careSituation', 'payType', 'incomeSource'
  ]
  return fields.some(field => hasBasicValue(patient[field]))
})

/** 躯体功能分类下的所有项目 */
const physicalItems = computed(() => buildFullItems('physical'))
/** 精神心理分类下的所有项目 */
const mentalItems = computed(() => buildFullItems('mental'))

/** 阳性项目列表 */
const positiveItems = computed(() => {
  const all = [...physicalItems.value, ...mentalItems.value]
  return all.filter(item => getResultType(item) === 'positive')
})

/** 未完成专项评估的阳性项目（当前暂未启用） */
const missingSpecialItems = computed(() => {
  return positiveItems.value.filter(item => requiresSpecialAssessment(item) && !hasSpecialAssessment(item))
})

/** 阳性项目数量 */
const positiveCount = computed(() => positiveItems.value.length)
/** 阴性项目数量 */
const negativeCount = computed(() => {
  const all = [...physicalItems.value, ...mentalItems.value]
  return all.filter(item => getResultType(item) === 'negative').length
})
/** 未完成项目数量 */
const pendingCount = computed(() => {
  const all = [...physicalItems.value, ...mentalItems.value]
  return all.filter(item => getResultType(item) === 'pending').length
})

/** 有效的评估记录（已过滤删除标记） */
const validCgaRecords = computed(() => {
  return (cgaRecordList.value || []).filter(record => String(record?.delFlag ?? '0') === '0')
})

/** 已完成的专项评估记录 */
const completedAssessmentRecords = computed(() => {
  return validCgaRecords.value.filter(record => Number(record?.scaleId) !== 0)
})

// ==================== 工具函数 ====================
/**
 * 判断值是否为非空有效内容
 * @param {*} value 待判断的值
 * @returns {Boolean}
 */
function hasBasicValue(value) {
  return value !== undefined && value !== null && String(value).trim() !== ''
}

/**
 * 标准化接口返回的列表数据
 * @param {*} response 接口返回
 * @returns {Array}
 */
function normalizeListResponse(response) {
  if (Array.isArray(response)) return response
  if (Array.isArray(response?.data)) return response.data
  if (Array.isArray(response?.rows)) return response.rows
  return []
}

/**
 * 标准化患者接口返回数据
 * @param {*} response 接口返回
 * @returns {Object|null}
 */
function normalizePatientResponse(response) {
  if (response?.data && typeof response.data === 'object') return response.data
  if (response && typeof response === 'object') return response
  return null
}

/**
 * 格式化日期为 YYYY-MM-DD
 * @param {String|Date} date 日期
 * @returns {String}
 */
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/**
 * 根据出生日期计算年龄
 * @param {String} birthday 出生日期
 * @returns {Number|null}
 */
function calcAge(birthday) {
  if (!birthday) return null
  const b = new Date(birthday)
  if (Number.isNaN(b.getTime())) return null
  const today = new Date()
  let age = today.getFullYear() - b.getFullYear()
  const monthDiff = today.getMonth() - b.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < b.getDate())) age -= 1
  return age >= 0 ? age : null
}

/**
 * 格式化出生日期（去除时间部分）
 * @param {String} dateStr 日期字符串
 * @returns {String|null}
 */
function formatBirthday(dateStr) {
  if (!dateStr) return null
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return dateStr
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/**
 * 拼接结果文本，自动过滤空值
 * @param  {...String} parts 文本片段
 * @returns {String}
 */
function joinResultText(...parts) {
  return parts
      .map(part => String(part || '').trim())
      .filter(Boolean)
      .join('，')
}

/**
 * 转义HTML，避免弹窗内容被异常文本破坏
 * @param {String} text 原始文本
 * @returns {String}
 */
function escapeHtml(text) {
  return String(text || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
}

/**
 * 展示未完成专项评估提示
 * @param {Array} items 未完成专项项目
 */
async function showMissingSpecialAlert(items) {
  const listHtml = items
      .map(item => `<li>${escapeHtml(getItemName(item))}</li>`)
      .join('')
  try {
    await ElMessageBox.alert(
        `<div style="padding: 8px 0 0;">
           <div style="background: linear-gradient(135deg, #fff7ed 0%, #fffbeb 100%); padding: 18px 20px; border-radius: 12px; border: 1px solid #fed7aa;">
             <div style="display: flex; align-items: flex-start; gap: 14px;">
               <div style="flex-shrink: 0; width: 44px; height: 44px; border-radius: 50%; background: #fff; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(217,119,6,0.15);">
                 <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M12 8v4"></path><path d="M12 16h.01"></path></svg>
               </div>
               <div style="flex: 1;">
                 <div style="font-size: 16px; font-weight: 700; color: #92400e; margin-bottom: 6px;">还有阳性项目未完成专项评估</div>
                 <div style="font-size: 14px; color: #475569; line-height: 1.7;">请先完成以下专项评估后，再生成综合报告：</div>
                 <ul style="margin: 10px 0 0; padding-left: 20px; color: #78350f; font-size: 14px; line-height: 1.8;">${listHtml}</ul>
               </div>
             </div>
           </div>
         </div>`,
        '请先完成专项评估',
        {
          confirmButtonText: '我知道了',
          dangerouslyUseHTMLString: true,
          showClose: true,
          customClass: 'report-confirm-v2',
          type: 'warning'
        }
    )
  } catch (error) {
    // 关闭弹窗时不报错
  }
}

/**
 * 获取量表对应的专项评估ID列表
 * @param {Object} item 评估项
 * @returns {Array}
 */
function getSpecialScaleIds(item) {
  return SPECIAL_SCALE_MAP[item?.scaleCode] || []
}

/**
 * 获取目标专项评估的主ID
 * @param {Object} item 评估项
 * @returns {Number|null}
 */
function getTargetSpecialScaleId(item) {
  return getSpecialScaleIds(item)[0] || item?.scaleId || null
}

/**
 * 拼接记录的完整文本，用于判断阴阳属性
 * @param {Object} record 评估记录
 * @returns {String}
 */
function recordText(record) {
  return `${record?.result || ''} ${record?.suggest || ''} ${record?.scaleName || ''} ${record?.remark || ''}`
}

/**
 * 判断记录是否为对应评估项的专项评估记录
 * @param {Object} item 评估项
 * @param {Object} record 评估记录
 * @returns {Boolean}
 */
function matchesSpecialRecord(item, record) {
  return getSpecialScaleIds(item).includes(Number(record?.scaleId))
}

/**
 * 获取对应评估项的专项评估记录
 * @param {Object} item 评估项
 * @returns {Object|null}
 */
function getSpecialRecord(item) {
  return validCgaRecords.value.find(record => matchesSpecialRecord(item, record)) || null
}

/**
 * 判断是否已完成对应专项评估
 * @param {Object} item 评估项
 * @returns {Boolean}
 */
function hasSpecialAssessment(item) {
  return !!getSpecialRecord(item)
}

/**
 * 获取专项评估操作按钮文案
 * @param {Object} item 评估项
 * @returns {String}
 */
function getSpecialActionText(item) {
  return hasSpecialAssessment(item) ? '已经进行专项评估' : '前往专项评估'
}

/**
 * 获取专项评估结果展示文本
 * @param {Object} item 评估项
 * @returns {String}
 */
function getSpecialResultText(item) {
  const record = getSpecialRecord(item)
  if (!record) return getAssessmentDetail(item)
  const score = record.totalScore !== null && record.totalScore !== undefined ? `${record.totalScore}分` : ''
  if (record.result && score) return `专项评估结果：${record.result}`
  if (record.result) return `专项评估结果：${record.result}`
  if (score) return `专项评估已完成，得分：${score}`
  return '专项评估已完成'
}

/**
 * 获取专项评估建议文本
 * @param {Object} item 评估项
 * @returns {String}
 */
function getSpecialSuggestText(item) {
  const record = getSpecialRecord(item)
  return record?.suggest || '建议保持现有状态，定期随访复评。'
}

/**
 * 根据专项评估结果判断阴阳属性
 * @param {Object} item 评估项
 * @returns {'positive'|'negative'|null}
 */
function getSpecialResultType(item) {
  const record = getSpecialRecord(item)
  if (!record) return null
  const text = recordText(record)

  // 阴性关键词
  const negativeKeywords = [
    '无依赖', '正常', '良好', '认知正常', '无异常', '无明显风险',
    '未发现明显风险因素', '低风险', '风险低', '风险较低'
  ]
  if (negativeKeywords.some(key => text.includes(key))) return 'negative'

  // 阳性关键词
  const positiveKeywords = ['依赖', '异常', '障碍', '较差', '抑郁', '焦虑', '风险', '建议专科']
  if (positiveKeywords.some(key => text.includes(key))) return 'positive'

  // 兜底：有结果或有得分视为阳性
  return record.result || (record.totalScore !== null && record.totalScore !== undefined) ? 'positive' : null
}

/**
 * 判断评估项是否需要跳转专项评估
 * @param {Object} item 评估项
 * @returns {Boolean}
 */
function requiresSpecialAssessment(item) {
  // 未完成的项目不显示专项评估入口，显示前往答题
  if (item?._answered === false) return false
  return !DIRECT_RESULT_CODES.includes(item?.scaleCode)
}

/**
 * 判断项目是否已完成筛查
 * @param {Object} item 评估项
 * @returns {Boolean}
 */
function isAnswered(item) {
  return item?._answered !== false
}

/**
 * 获取评估项目展示名称
 * @param {Object} item 评估项
 * @returns {String}
 */
function getItemName(item) {
  const code = item.scaleCode
  if (SCALE_MAP[code]) return SCALE_MAP[code].name
  return item.scaleName || item.scaleCode || '未命名项目'
}

/**
 * 获取评估详情描述文本
 * @param {Object} item 评估项
 * @returns {String}
 */
function getAssessmentDetail(item) {
  const code = item.scaleCode
  const tip = String(item.tip || '').trim()

  // 衰弱量表特殊处理
  if (code === 'frail_assessment' && tip && tip !== '无衰弱') {
    return `FRAIL 量表提示${tip}`
  }

  // 从tip中提取结果部分，去掉建议
  if (tip && tip.includes('，建议')) {
    return tip.split('，建议')[0]
  }
  if (tip && tip.includes('，应')) {
    return tip.split('，应')[0]
  }

  if (SCALE_MAP[code]) return SCALE_MAP[code].detail
  return '筛查异常'
}

/**
 * 获取结果展示文本（优先取专项结果，其次接口tip，最后兜底默认描述）
 * @param {Object} item 评估项
 * @returns {String}
 */
function getResultDisplayText(item) {
  const specialRecord = getSpecialRecord(item)
  if (specialRecord) {
    return joinResultText(specialRecord.result, specialRecord.suggest) || '专项评估已完成'
  }

  const tip = String(item.tip || '').trim()
  if (tip) return tip

  return getAssessmentDetail(item)
}

/**
 * 获取评估建议文本
 * @param {Object} item 评估项
 * @returns {String}
 */
function getSuggestion(item) {
  const specialRecord = getSpecialRecord(item)
  if (specialRecord?.suggest) return specialRecord.suggest

  const tip = String(item.tip || '').trim()
  if (tip.includes('建议')) {
    return tip.replace(/^.*?建议/, '').replace(/^[:：，,]/, '').trim()
  }
  if (tip.includes('，应')) {
    return `应${tip.split('，应').slice(1).join('，应')}`.trim()
  }

  const code = item.scaleCode
  if (SCALE_MAP[code]) return SCALE_MAP[code].suggestion
  return '请进行该项目专项评估'
}

/**
 * 构建全量评估项目列表（包含未完成项，按指定顺序排序）
 * @param {String} category 分类：physical/mental
 * @returns {Array}
 */
function buildFullItems(category) {
  const list = reportData.value?.assessmentList || []
  return FULL_SCALE_LIST
      .filter(entry => SCALE_MAP[entry.code]?.category === category)
      .map(entry => {
        const code = entry.code
        const matched = list.find(a => a.scaleCode === code)

        // 有筛查数据
        if (matched) {
          return { ...matched, _answered: true, _order: entry.order, scaleCode: code }
        }

        // 无筛查数据但有专项评估数据
        const specialRecord = getSpecialRecord({ scaleCode: code })
        if (specialRecord) {
          return {
            scaleCode: code,
            scaleName: SCALE_MAP[code]?.name,
            _answered: true,
            _order: entry.order,
            rawScore: specialRecord.totalScore,
            itemScore: specialRecord.result || '',
            tip: specialRecord.suggest || ''
          }
        }

        // 完全未完成
        return { scaleCode: code, _answered: false, _order: entry.order, rawScore: null, itemScore: '', tip: '' }
      })
      .sort((a, b) => a._order - b._order)
}

/**
 * 判断评估项的结果类型
 * @param {Object} item 评估项
 * @returns {'positive'|'negative'|'pending'}
 */
function getResultType(item) {
  // 未完成的筛查项
  if (item?._answered === false) return 'pending'

  // 优先按专项评估结果判断
  const specialType = getSpecialResultType(item)
  if (specialType) return specialType

  // 按后端返回的itemScore判断
  if (item.itemScore === '阳性') return 'positive'
  if (item.itemScore === '阴性') return 'negative'

  // 按tip文本关键词判断
  const tip = String(item.tip || '')
  if (tip.includes('阳性') || tip.includes('衰弱') || tip.includes('风险')) {
    if (tip.includes('阴性') || tip.includes('无衰弱')) return 'negative'
    return 'positive'
  }

  // 阴阳型量表：按rawScore判断（0=阳性，1=阴性）
  const code = item.scaleCode
  const yinYangCodes = [
    'basic_adl', 'instrumental_adl', 'exercise_function', 'vision_assessment', 'hearing_assessment',
    'home_environment', 'incontinence_screen', 'constipation_screen', 'sleep_disorder_screen',
    'chronic_pain_screen', 'pressure_injury_risk', 'water_swallowing_test', 'nutrition_risk_screen',
    'depression_screen', 'anxiety_screen', 'fall_risk_screen', 'polypharmacy_assessment',
    'sarcopenia_screen',
    'exercise', 'depression', 'anxiety', 'fall', 'incontinence', 'constipation', 'sleep',
    'chronic', 'pressure', 'swallow', 'sarcopenia', 'delirium'
  ]
  if (yinYangCodes.includes(code)) {
    if (item.rawScore === null || item.rawScore === undefined) return 'pending'
    return Number(item.rawScore) === 0 ? 'positive' : 'negative'
  }

  // 评分型量表：有建议视为阳性
  if (item.tip && item.tip.includes('建议')) return 'positive'
  if (item.rawScore !== null && item.rawScore !== undefined) return 'negative'

  return 'pending'
}

// ==================== 业务方法 ====================
/**
 * 加载患者评估记录列表
 * @param {String|Number} pid 患者ID
 */
async function loadCgaRecordList(pid) {
  if (!pid) {
    cgaRecordList.value = []
    return
  }
  try {
    const res = await selectCgaRecordList({ patientId: pid })
    cgaRecordList.value = normalizeListResponse(res)
  } catch (error) {
    cgaRecordList.value = []
    console.error('加载专项评估记录失败', error)
  }
}

/**
 * 加载患者基本详情信息
 * @param {String|Number} pid 患者ID
 */
async function loadPatientBasicInfo(pid) {
  if (!pid) {
    patientBasicInfo.value = null
    return
  }
  try {
    patientBasicInfo.value = normalizePatientResponse(await selectPatientById({ patientId: pid }))
  } catch (error) {
    patientBasicInfo.value = null
    console.error('加载患者基本情况失败', error)
  }
}

/**
 * 加载完整报告数据
 */
async function loadReportData() {
  const pid = route.query.patientId
  if (!pid) {
    loading.value = false
    ElMessage.warning('未获取到患者信息')
    return
  }

  loading.value = true
  try {
    // 并行加载记录和患者信息，提升速度
    await Promise.all([
      loadCgaRecordList(pid),
      loadPatientBasicInfo(pid)
    ])

    const res = await getFullCgaReportByPatient(pid)
    console.log('【综合评估报告】原始返回:', res)

    // 兼容不同接口返回结构
    let realData = null
    if (res && res.data !== undefined && res.data !== null && typeof res.data === 'object') {
      realData = res.data
    } else if (res && typeof res === 'object' && res.patientId !== undefined) {
      realData = res
    }

    console.log('【综合评估报告】解析后的报告数据:', realData)
    reportData.value = realData || null
  } catch (error) {
    reportData.value = null
    ElMessage.error('加载评估报告失败')
    console.error('加载评估报告失败', error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转至对应评估页面
 * @param {Object} item 评估项
 */
function navigateTo(item) {
  const pid = patientId.value
  const pname = patientName.value
  if (!pid) {
    ElMessage.warning('未获取到患者信息，无法跳转')
    return
  }

  const code = item.scaleCode
  const config = SCALE_MAP[code]
  const path = config?.path || '/patient/detection/customize'
  const targetScaleId = getTargetSpecialScaleId(item)

  router.push({
    path,
    query: {
      patientId: pid,
      patientName: pname,
      targetScaleId,
      projectId: targetScaleId,
      scaleCode: code,
      focusCode: code
    }
  })
}

/**
 * 返回上一页
 */
function goBack() {
  router.push({ path: '/patient/detection/customize', query: route.query })
}

/**
 * 跳转至自定义评估首页
 */
function goCustomAssessment() {
  const pid = patientId.value
  const pname = patientName.value
  if (!pid) {
    ElMessage.warning('未获取到患者信息，无法跳转')
    return
  }
  router.push({
    path: '/patient/detection/customize',
    query: {
      patientId: pid,
      patientName: pname,
      scaleId: 'custom_assess',
      projectId: 99
    }
  })
}

/**
 * 跳转至基本信息填写页
 */
function goBasicInformationAssessment() {
  const pid = patientId.value
  const pname = patientName.value
  if (!pid) {
    ElMessage.warning('未获取到患者信息，无法跳转')
    return
  }
  router.push({
    path: '/patient/detection/customize/basicInformation',
    query: {
      patientId: pid,
      patientName: pname
    }
  })
}

/**
 * 生成并打开报告
 */
async function handleGenerateReport() {
  const pid = patientId.value
  if (!pid) {
    ElMessage.warning('未获取到患者信息，无法生成报告')
    return
  }

  // 刷新记录列表，确保状态最新
  await loadCgaRecordList(pid)

  // 没有任何评估结果时弹出提示
  if (answeredItems.value.length === 0 && completedAssessmentRecords.value.length === 0) {
    try {
      await ElMessageBox.alert(
          `<div style="padding: 8px 0 0;">
           <div style="background: linear-gradient(135deg, #fff7ed 0%, #fffbeb 100%); padding: 18px 20px; border-radius: 12px; border: 1px solid #fed7aa;">
             <div style="display: flex; align-items: flex-start; gap: 14px;">
               <div style="flex-shrink: 0; width: 44px; height: 44px; border-radius: 50%; background: #fff; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(217,119,6,0.15);">
                 <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M12 8v4"></path><path d="M12 16h.01"></path></svg>
               </div>
               <div style="flex: 1;">
                 <div style="font-size: 16px; font-weight: 700; color: #92400e; margin-bottom: 6px;">暂无可生成的评估结果</div>
                 <div style="font-size: 14px; color: #475569; line-height: 1.7;">请至少完成一项自定义检测或专项评估后，再生成报告。</div>
               </div>
             </div>
           </div>
         </div>`,
          '请先完成评估',
          {
            confirmButtonText: '我知道了',
            dangerouslyUseHTMLString: true,
            showClose: true,
            customClass: 'report-confirm-v2',
            type: 'warning'
          }
      )
    } catch (error) {
      // 关闭弹窗时不报错
    }
    return
  }

  // CGA总筛查存在阳性项目时，必须先完成对应专项评估
  const missingItems = missingSpecialItems.value
  if (missingItems.length > 0) {
    await showMissingSpecialAlert(missingItems)
    return
  }

  generating.value = true
  try {
    await generateCgaReport(pid)
    ElMessage.success('报告生成成功')

    // 跳转至报告列表页
    router.push({
      path: '/report/cgareport'
    })
  } catch (error) {
    ElMessage.error('生成报告失败，请重试')
    console.error('生成报告失败', error)
  } finally {
    generating.value = false
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadReportData()
})

onActivated(() => {
  loadReportData()
})
</script>

<style scoped lang="scss">
.comprehensive-container {
  min-height: 100vh;
  padding: 24px;
  background: #eef3f8;
  box-sizing: border-box;
}

/* ===== 顶部操作栏 ===== */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
  margin-bottom: 20px;
  padding: 14px 24px;
  background: #fff;
  border: 1px solid #dfe7f0;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(26, 58, 92, 0.08);

  .action-right {
    display: flex;
    gap: 8px;
  }
}

@media (max-width: 640px) {
  .action-bar {
    flex-wrap: wrap;
    gap: 10px;
    padding: 12px 14px;
  }
}

/* ===== 加载/空状态 ===== */
.loading-box,
.empty-box {
  background: #fff;
  border-radius: 8px;
  padding: 60px 20px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.loading-text {
  margin-top: 12px;
  color: #666;
}

/* ===== 报告主体容器 ===== */
.report-wrapper {
  max-width: 1120px;
  margin: 0 auto;
  padding: 56px 64px;
  background: #fff;
  border: 1px solid #dfe7f0;
  border-radius: 8px;
  box-shadow: 0 18px 45px rgba(26, 58, 92, 0.12);
}

/* ===== 报告标题 ===== */
.report-header {
  padding: 26px 30px 22px;
  margin-bottom: 20px;
  text-align: center;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;

  .report-title {
    margin: 0 0 6px;
    font-size: 28px;
    font-weight: 700;
    color: #111;
    letter-spacing: 4px;
  }

  .report-subtitle {
    margin: 0;
    font-size: 12px;
    color: #666;
    letter-spacing: 2px;
  }
}

/* ===== 患者信息卡片 ===== */
.patient-info-card {
  margin-bottom: 20px;
  padding: 20px 26px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;

  .info-row {
    display: flex;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .info-item {
    flex: 1;
    display: flex;
    align-items: center;
    font-size: 14px;
  }

  .info-label {
    min-width: 80px;
    color: #555;
    font-weight: 500;
  }

  .info-value {
    padding: 0 8px 2px 4px;
    min-width: 120px;
    color: #111;
    font-weight: 600;
  }
}

/* ===== 汇总面板（暂未启用） ===== */
.summary-panel {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 28px;

  .summary-item {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 160px;
    justify-content: center;
    padding: 16px 22px;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 10px;

    &.positive {
      border-color: #fca5a5;
      .summary-num { color: #dc2626; }
    }

    &.negative {
      border-color: #86efac;
      .summary-num { color: #16a34a; }
    }

    &.pending {
      border-color: #d1d5db;
      .summary-num { color: #6b7280; }
    }
  }

  .summary-num {
    font-size: 32px;
    font-weight: 700;
    line-height: 1;
  }

  .summary-text {
    font-size: 14px;
    color: #555;
  }
}

/* ===== 基本情况模块 ===== */
.basic-history-section {
  margin-bottom: 28px;

  .module-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 14px;
    padding: 12px 18px;
    background: #bfdbfe;
    color: #1e3a8a;
    font-size: 18px;
    font-weight: 700;
    border-radius: 8px;
  }

  .basic-info-btn {
    min-width: 150px;
    height: 42px;
    padding: 0 22px;
    font-size: 15px;
    font-weight: 700;
    border-radius: 8px;
  }
}

/* ===== 结果表格 ===== */
.result-table-wrapper {
  margin-bottom: 30px;
  overflow-x: auto;
  overflow-y: hidden;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

.result-table {
  width: 100%;
  min-width: 920px;
  border-collapse: collapse;
  font-size: 14px;

  thead th {
    padding: 16px 12px;
    background: #dbeafe !important;
    color: #1e40af !important;
    font-weight: 600;
    letter-spacing: 2px;
    text-align: center;
    border: 1px solid #bfdbfe !important;
  }

  .category-row .category-cell {
    padding: 12px 16px;
    background: #bfdbfe !important;
    color: #1e3a8a !important;
    font-weight: 700;
    font-size: 15px;
    text-align: left;
    letter-spacing: 2px;
    border: 1px solid #bfdbfe !important;
  }

  .data-row {
    background: #fff;
    transition: background 0.15s;

    &:hover {
      background: #f8fafc;
    }

    td {
      padding: 14px 12px;
      border: 1px solid #e5e7eb;
      vertical-align: middle;
    }
  }

  .col-index {
    width: 60px;
    text-align: center;
    font-weight: 500;
  }

  .col-item {
    width: 180px;
    font-weight: 500;
  }

  .col-screen {
    width: 100px;
    text-align: center;
  }

  .col-result {
    line-height: 1.8;

    .result-detail {
      margin-bottom: 4px;
      font-size: 14px;
      font-weight: 500;

      &.positive-text { color: #c53030; }
      &.special-text { color: #2563eb; }
    }

    .result-normal {
      color: #16a34a;
      font-weight: 500;
    }

    .result-pending {
      color: #9ca3af;
      font-style: italic;
    }
  }

  .col-action {
    width: 130px;
    text-align: center;
    white-space: nowrap;

    .muted {
      color: #9ca3af;
      font-size: 13px;
    }
  }
}

/* ===== 综合诊断意见 ===== */
.conclusion-card {
  padding: 24px 26px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-left: 4px solid #d97706;
  border-radius: 8px;

  .conclusion-title {
    margin-bottom: 16px;
    padding-bottom: 12px;
    font-size: 16px;
    font-weight: 700;
    color: #1a3a5c;
    letter-spacing: 3px;
    border-bottom: 1px dashed #fde68a;
  }

  .conclusion-content {
    font-size: 14px;
    color: #2d3748;
    line-height: 2;

    p {
      margin: 0 0 12px;
      text-indent: 2em;
    }

    .conclusion-list {
      margin: 0 0 12px 2em;
      padding-left: 0;
      list-style: none;

      li {
        margin-bottom: 6px;
        padding-left: 8px;
        border-left: 2px solid #fde68a;
        line-height: 2;

        strong {
          color: #c53030;
        }
      }
    }
  }
}

/* ===== 响应式适配 ===== */
@media (max-width: 900px) {
  .report-wrapper {
    padding: 30px 20px;
  }

  .patient-info-card .info-row {
    flex-direction: column;
    gap: 12px;
  }

  .summary-panel {
    flex-direction: column;
    gap: 12px;
  }

  .result-table {
    font-size: 12px;

    .col-item { width: 100px; }
    .col-screen { width: 70px; }
  }
}

/* ===== 打印样式 ===== */
@media print {
  .comprehensive-container {
    padding: 0;
    background: #fff;
  }

  .action-bar {
    display: none !important;
  }

  .report-wrapper {
    max-width: 100%;
    padding: 20px 30px;
    box-shadow: none;
  }

  .data-row:hover {
    background: #fff !important;
  }
}
</style>

<!-- 全局弹窗样式 -->
<style lang="scss">
.report-confirm-v2 {
  width: 480px !important;
  overflow: hidden;
  border-radius: 16px !important;
  border: 1px solid #e2e8f0;
  box-shadow: 0 25px 80px rgba(15, 23, 42, 0.18) !important;

  .el-message-box__header {
    padding: 18px 24px 10px;
    background: transparent;
    border-bottom: none;

    .el-message-box__title {
      font-size: 15px;
      font-weight: 600;
      color: #334155;
      letter-spacing: 2px;
    }

    .el-message-box__close {
      color: #94a3b8 !important;
      &:hover { color: #475569 !important; }
    }
  }

  .el-message-box__content {
    padding: 6px 24px 20px;
  }

  .el-message-box__message {
    margin: 0 !important;
    padding: 0 !important;
  }

  .el-message-box__container {
    margin-top: 0 !important;
  }

  .el-message-box__btns {
    padding: 4px 24px 22px;
    display: flex;
    justify-content: center;
    gap: 12px;
  }
}
</style>

