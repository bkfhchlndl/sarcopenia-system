<template>
  <div class="s-comp-page">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <div class="top-left">
        <el-button text class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <span class="page-title">肌少症综合评估结果</span>
      </div>
      <div class="top-right">
        <el-button @click="goCustomize">
          <el-icon><EditPen /></el-icon>
          自定义项目
        </el-button>
        <el-button type="primary" :disabled="!canGenerate" @click="generateReport">
          <el-icon><Document /></el-icon>
          生成报告
        </el-button>
      </div>
    </div>

    <div class="page-content">
      <!-- 报告标题 -->
      <div class="report-header">
        <div class="report-title-area">
          <h1 class="report-title">肌少症综合评估报告</h1>
          <p class="report-subtitle">Sarcopenia Comprehensive Assessment Report</p>
        </div>
      </div>

      <!-- 患者信息卡 -->
      <div class="patient-card">
        <div class="pc-row">
          <div class="pc-item">
            <span class="pc-label">姓名</span>
            <span class="pc-value">{{ patientName || '--' }}</span>
          </div>
          <div class="pc-item">
            <span class="pc-label">性别</span>
            <span class="pc-value">{{ genderText }}</span>
          </div>
          <div class="pc-item">
            <span class="pc-label">出生日期</span>
            <span class="pc-value">{{ birthday || '--' }}</span>
          </div>
          <div class="pc-item">
            <span class="pc-label">年龄</span>
            <span class="pc-value">{{ ageText }}</span>
          </div>
          <div class="pc-item">
            <span class="pc-label">病历号</span>
            <span class="pc-value">{{ medicalCaseNo || '--' }}</span>
          </div>
          <div class="pc-item">
            <span class="pc-label">评估日期</span>
            <span class="pc-value">{{ todayStr }}</span>
          </div>
        </div>
      </div>

      <!-- 基本家庭情况 -->
      <div class="basic-info-card">
        <div class="basic-info-main">
          <div class="basic-info-title">基本家庭情况</div>
          <div class="basic-info-desc">
            {{ isBasicInformationFilled ? '已完善患者基本情况、家庭与照护信息。' : '可补充患者基本情况、家庭与照护信息，用于完善评估报告。' }}
          </div>
        </div>
        <el-button
          :type="isBasicInformationFilled ? 'success' : 'primary'"
          size="large"
          @click="goBasicInformation"
        >
          <el-icon><EditPen /></el-icon>
          {{ isBasicInformationFilled ? '已填写过' : '填写基本情况' }}
        </el-button>
      </div>

      <!-- 一、筛查结果 -->
      <div class="section-card">
        <div class="section-header">
        <span class="section-index">一</span>
          <span class="section-title">筛查结果</span>
          <span class="section-desc">SARC-F 量表 + 小腿围测量</span>
        </div>
        <div class="section-body">
          <el-table :data="screeningItems" border stripe style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="评估项目" width="160" />
            <el-table-column label="筛查结果" width="120" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.result != null" :type="row.positive ? 'danger' : 'success'" effect="light">
                  {{ row.positive ? '阳性' : '阴性' }}
                </el-tag>
                <el-tag v-else type="info" effect="plain">未评估</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="评估结果">
              <template #default="{ row }">
                <span v-if="row.result != null">{{ row.detail }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="goToItem(row)">
                  {{ row.result != null ? '查看/重测' : '前往评估' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 二、肌肉力量评估 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-index">二</span>
          <span class="section-title">肌肉力量评估</span>
          <span class="section-desc">握力测量 — 肌少症核心指标</span>
        </div>
        <div class="section-body">
          <el-table :data="strengthItems" border stripe style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="评估项目" width="160" />
            <el-table-column label="评估结果" width="120" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.result != null" :type="row.positive ? 'danger' : 'success'" effect="light">
                  {{ row.positive ? '肌力降低' : '正常' }}
                </el-tag>
                <el-tag v-else type="info" effect="plain">未评估</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="测量数据">
              <template #default="{ row }">
                <span v-if="row.result != null">{{ row.detail }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="goToItem(row)">
                  {{ row.result != null ? '查看/重测' : '前往评估' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 三、躯体功能评估 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-index">三</span>
          <span class="section-title">躯体功能评估</span>
          <span class="section-desc">步速、起坐、平衡 — SPPB 躯体功能</span>
        </div>
        <div class="section-body">
          <el-table :data="functionItems" border stripe style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="评估项目" width="160" />
            <el-table-column label="得分" width="100" align="center">
              <template #default="{ row }">
                <span v-if="row.score != null" class="score-text">
                  <strong>{{ row.score }}</strong> / {{ row.maxScore }}
                </span>
                <el-tag v-else type="info" effect="plain">未评估</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="测量数据">
              <template #default="{ row }">
                <span v-if="row.result != null">{{ row.detail }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="goToItem(row)">
                  {{ row.result != null ? '查看/重测' : '前往评估' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- SPPB 总分 -->
          <div class="sppb-summary" v-if="sppbTotal != null">
            <div class="sppb-label">SPPB 躯体功能总分</div>
            <div class="sppb-score">
              <span class="sppb-num">{{ sppbTotal }}</span>
              <span class="sppb-total">/ 12 分</span>
            </div>
            <el-tag :type="sppbLevel.type" effect="light">{{ sppbLevel.text }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 四、肌肉量评估 -->
      <div class="section-card">
        <div class="section-header">
          <span class="section-index">四</span>
          <span class="section-title">肌肉量评估</span>
          <span class="section-desc">人体成分分析 — 肌量确诊依据</span>
        </div>
        <div class="section-body">
          <el-table :data="massItems" border stripe style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="评估项目" width="160" />
            <el-table-column label="评估结果" width="120" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.result != null" :type="row.positive ? 'danger' : 'success'" effect="light">
                  {{ row.positive ? '肌量降低' : '正常' }}
                </el-tag>
                <el-tag v-else type="info" effect="plain">未评估</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="核心指标">
              <template #default="{ row }">
                <span v-if="row.result != null">{{ row.detail }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="goToItem(row)">
                  {{ row.result != null ? '查看/重测' : '前往评估' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 综合建议 -->
      <div class="suggestion-card">
        <div class="sg-title">
          <el-icon><EditPen /></el-icon>
          综合建议
        </div>
        <div class="sg-content">
          <div v-if="suggestionList.length === 0" class="sg-empty">
            完成评估后将根据结果生成个性化建议
          </div>
          <ul v-else class="sg-list">
            <li v-for="(item, idx) in suggestionList" :key="idx">
              <span class="sg-num">{{ idx + 1 }}</span>
              <span class="sg-text">{{ item }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="SComprehensive">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, EditPen, Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectPatientById } from '@/api/patient.js'
import {
  selectSRecordList, selectCalfMeasureList, selectGripStrengthList,
  selectSitUpList, selectPaceList, selectBalanceStandList,
  selectBodyCompositionList, generateSReport
} from '@/api/s.js'

const route = useRoute()
const router = useRouter()

// ==================== 患者信息 ====================
const patientInfo = ref(null)
const patientId = computed(() => route.query.patientId || route.query.id || '')
const patientName = computed(() => patientInfo.value?.name || patientInfo.value?.patientName || route.query.patientName || route.query.name || '')
const medicalCaseNo = computed(() => patientInfo.value?.medicalCaseNo || patientInfo.value?.caseNo || route.query.medicalCaseNo || route.query.caseNo || '')
const gender = computed(() => patientInfo.value?.gender ?? route.query.gender ?? '')
const birthday = computed(() => patientInfo.value?.birthday || patientInfo.value?.birthDate || route.query.birthday || '')

const genderText = computed(() => {
  if (gender.value === '1' || gender.value === 1) return '男'
  if (gender.value === '2' || gender.value === 2) return '女'
  return '--'
})

const ageText = computed(() => {
  if (!birthday.value) return '--'
  const b = new Date(birthday.value)
  const now = new Date()
  let age = now.getFullYear() - b.getFullYear()
  const m = now.getMonth() - b.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < b.getDate())) age--
  return age + ' 岁'
})

const todayStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})

const isBasicInformationFilled = computed(() => {
  const patient = patientInfo.value || {}
  const fields = [
    'phone', 'idCard', 'id_card', 'socialCardNo', 'social_card_no',
    'height', 'weight', 'nation', 'educationalLevel', 'educational_level',
    'religiousBelief', 'religious_belief', 'maritalStatus', 'marital_status',
    'familyStatus', 'family_status', 'liveSituation', 'live_situation',
    'careSituation', 'care_situation', 'payType', 'pay_type',
    'incomeSource', 'income_source'
  ]
  return fields.some(field => hasBasicValue(patient[field]))
})

// ==================== 数据状态 ====================
const sRecordList = ref([])
const calfMeasureList = ref([])
const gripStrengthList = ref([])
const sitUpList = ref([])
const paceList = ref([])
const balanceStandList = ref([])
const bodyCompositionList = ref([])

// ==================== 项目配置 ====================
const ITEM_CONFIG = {
  sarc_f: {
    name: 'SARC-F 量表',
    category: 'screening',
    path: '/patient/detection/sCustomize/sSarcF',
    maxScore: 0
  },
  calf_measure: {
    name: '小腿围测量',
    category: 'screening',
    path: '/patient/detection/sCustomize/sCalfMeasure'
  },
  grip_strength: {
    name: '握力测量',
    category: 'strength',
    path: '/patient/detection/sCustomize/gripStrength'
  },
  sit_up: {
    name: '5次起坐测试',
    category: 'function',
    path: '/patient/detection/sCustomize/situp',
    maxScore: 4
  },
  pace: {
    name: '6米步速测量',
    category: 'function',
    path: '/patient/detection/sCustomize/pace',
    maxScore: 4
  },
  balance_stand: {
    name: '站立平衡测试',
    category: 'function',
    path: '/patient/detection/sCustomize/balanceStand',
    maxScore: 4
  },
  body_composition: {
    name: '人体成分测量',
    category: 'mass',
    path: '/patient/detection/sCustomize/bodyComposition'
  }
}

// ==================== 筛查项目 ====================
const screeningItems = computed(() => {
  const items = []

  // SARC-F
  const sarcFRecord = sRecordList.value.find(isSarcFRecord) || sRecordList.value[0]
  const sarcFScore = sarcFRecord ? toNumber(sarcFRecord.totalScore) : null
  items.push({
    key: 'sarc_f',
    name: 'SARC-F 量表筛查',
    result: sarcFScore != null ? true : null,
    positive: sarcFScore != null ? sarcFScore <= 4 : null,
    detail: sarcFScore != null ? `得分 ${sarcFScore} 分（≤4分提示风险）` : null,
    score: sarcFRecord?.totalScore ?? null,
    maxScore: 10,
    ...ITEM_CONFIG.sarc_f
  })

  // 小腿围
  const calf = calfMeasureList.value[0]
  const calfValues = measureValues(calf)
  const calfMin = calfValues.length ? Math.min(...calfValues) : null
  const calfThreshold = (gender.value === '1' || gender.value === 1) ? 34 : 33
  const isLowCalf = calfMin != null ? calfMin < calfThreshold : null
  items.push({
    key: 'calf_measure',
    name: '小腿围测量',
    result: calfMin != null ? true : null,
    positive: isLowCalf,
    detail: calf && calfMin != null
      ? `测量值 ${formatMeasureValues(calfValues, 'cm')}；最小值 ${formatNumber(calfMin, 1)} cm（参考界值 ${calfThreshold} cm）`
      : null,
    score: null,
    ...ITEM_CONFIG.calf_measure
  })

  return items
})

// ==================== 肌肉力量 ====================
const strengthItems = computed(() => {
  const items = []
  const grip = gripStrengthList.value[0]
  const gripValues = measureValues(grip)
  const gripMax = gripValues.length ? Math.max(...gripValues) : null

  // 判断是否降低：男性<28kg，女性<18kg（参考值
  const gripThreshold = (gender.value === '1' || gender.value === 1) ? 28 : 18
  const isLowGrip = gripMax != null ? gripMax < gripThreshold : null

  items.push({
    key: 'grip_strength',
    name: '握力测量',
    result: gripMax != null ? true : null,
    positive: isLowGrip,
    detail: grip && gripMax != null
      ? `测量值 ${formatMeasureValues(gripValues, 'kg')}；最大值 ${formatNumber(gripMax, 1)} kg（参考界值 ${gripThreshold} kg）`
      : null,
    score: null,
    ...ITEM_CONFIG.grip_strength
  })

  return items
})

// ==================== 躯体功能 ====================
const functionItems = computed(() => {
  const items = []

  // 5次起坐
  const sitUp = sitUpList.value[0]
  const sitUpTime = toNumber(firstValue(sitUp, ['firstValue', 'first_value', 'duration']))
  let sitUpScore = toNumber(firstValue(sitUp, ['score', 'totalScore', 'total_score']))
  if (sitUpScore == null && sitUpTime != null) {
    if (sitUpTime <= 11.19) sitUpScore = 4
    else if (sitUpTime <= 13.69) sitUpScore = 3
    else if (sitUpTime <= 16.69) sitUpScore = 2
    else if (sitUpTime <= 60) sitUpScore = 1
    else sitUpScore = 0
  }
  items.push({
    key: 'sit_up',
    name: '5次起坐测试',
    result: sitUpTime != null ? true : null,
    positive: sitUpScore != null ? sitUpScore < 3 : null,
    detail: sitUp && sitUpTime != null
      ? `用时 ${formatNumber(sitUpTime, 2)} 秒`
      : null,
    score: sitUpScore,
    maxScore: 4,
    ...ITEM_CONFIG.sit_up
  })

  // 6米步速
  const pace = paceList.value[0]
  const paceTime = toNumber(firstValue(pace, ['avgValue', 'avg_value', 'averageTime', 'average_time']))
    ?? averageNumbers([
      firstValue(pace, ['firstValue', 'first_value']),
      firstValue(pace, ['secondValue', 'second_value'])
    ])
  const paceSpeed = paceTime ? 6 / paceTime : null
  let paceScore = toNumber(firstValue(pace, ['score', 'totalScore', 'total_score']))
  if (paceScore == null && paceSpeed != null) {
    if (paceSpeed >= 1.2) paceScore = 4
    else if (paceSpeed >= 1.0) paceScore = 3
    else if (paceSpeed >= 0.8) paceScore = 2
    else paceScore = 1
  }
  items.push({
    key: 'pace',
    name: '6米步速测量',
    result: paceTime != null ? true : null,
    positive: paceScore != null ? paceScore < 3 : null,
    detail: pace && paceTime != null
      ? `平均 ${formatNumber(paceTime, 2)} 秒（${formatNumber(paceSpeed, 2)} m/s）`
      : null,
    score: paceScore,
    maxScore: 4,
    ...ITEM_CONFIG.pace
  })

  // 站立平衡
  const balance = balanceStandList.value[0]
  const feetTogetherTime = toNumber(firstValue(balance, ['feetTogetherTime', 'feet_together_time']))
  const semiOffsetTime = toNumber(firstValue(balance, ['semiOffsetTime', 'semi_offset_time']))
  const tandemStandTime = toNumber(firstValue(balance, ['tandemStandTime', 'tandem_stand_time']))
  let balanceScore = toNumber(firstValue(balance, ['totalScore', 'total_score', 'score']))
  if (balanceScore == null && balance != null) {
    balanceScore = calculateBalanceScore(feetTogetherTime, semiOffsetTime, tandemStandTime)
  }
  const hasBalanceData = balance != null && [feetTogetherTime, semiOffsetTime, tandemStandTime].some(v => v != null)
  items.push({
    key: 'balance_stand',
    name: '站立平衡测试',
    result: hasBalanceData ? true : null,
    positive: balanceScore != null ? balanceScore < 3 : null,
    detail: balance != null
      ? `双脚并立 ${formatNumber(feetTogetherTime, 1)}秒 / 半脚错开 ${formatNumber(semiOffsetTime, 1)}秒 / 前后脚 ${formatNumber(tandemStandTime, 1)}秒`
      : null,
    score: balanceScore,
    maxScore: 4,
    ...ITEM_CONFIG.balance_stand
  })

  return items
})

// SPPB 总分
const sppbTotal = computed(() => {
  const scores = functionItems.value
    .filter(i => i.score != null)
    .map(i => i.score)
  if (scores.length < 3) return null
  return scores.reduce((a, b) => a + b, 0)
})

const sppbLevel = computed(() => {
  if (sppbTotal.value == null) return { text: '待评估', type: 'info' }
  if (sppbTotal.value >= 10) return { text: '躯体功能良好', type: 'success' }
  if (sppbTotal.value >= 7) return { text: '轻度功能下降', type: 'warning' }
  if (sppbTotal.value >= 4) return { text: '中度功能障碍', type: 'warning' }
  return { text: '重度功能障碍', type: 'danger' }
})

// ==================== 肌肉量 ====================
const massItems = computed(() => {
  const items = []
  const bc = bodyCompositionList.value[0]

  const isLowAsmi = bc && bc.asmi != null
    ? ((gender.value === '1' || gender.value === 1)
      ? bc.asmi < 7.0
      : bc.asmi < 5.7)
    : null

  items.push({
    key: 'body_composition',
    name: '人体成分测量 (ASMI)',
    result: bc && bc.asmi != null ? true : null,
    positive: isLowAsmi,
    detail: bc && bc.asmi != null
      ? `ASMI ${bc.asmi} kg/m²  |  骨骼肌量 ${bc.asm ?? '--'} kg`
      : null,
    score: null,
    ...ITEM_CONFIG.body_composition
  })

  return items
})

// ==================== 综合诊断 ====================
const totalScore = computed(() => sppbTotal.value ?? 0)

const diagnosisLevel = computed(() => {
  // 根据 AWGS 2019 诊断流程简化
  const hasLowStrength = strengthItems.value.some(i => i.positive)
  const hasLowMass = massItems.value.some(i => i.positive)
  const hasLowPhysical = sppbTotal.value != null && sppbTotal.value < 8

  if (hasLowStrength && hasLowMass) return 'severe'
  if (hasLowStrength || hasLowMass || hasLowPhysical) return 'mild'
  if (screeningItems.value.some(i => i.positive)) return 'risk'
  return 'normal'
})

const diagnosisText = computed(() => {
  switch (diagnosisLevel.value) {
    case 'severe': return '严重肌少症可能'
    case 'mild': return '肌少症可能'
    case 'risk': return '肌少症风险'
    default: return '肌少症低风险'
  }
})

const scoreHint = computed(() => {
  switch (diagnosisLevel.value) {
    case 'severe': return '肌力+肌量均降低，建议专科就诊'
    case 'mild': return '存在肌力或肌量下降，建议干预'
    case 'risk': return '筛查阳性，建议进一步检查'
    default: return '各项指标基本正常'
  }
})

// ==================== 综合建议 ====================
const suggestionList = computed(() => {
  const suggestions = []
  const level = diagnosisLevel.value

  if (level === 'normal') {
    suggestions.push('当前肌少症风险较低，继续保持健康的生活方式。')
    suggestions.push('建议每年定期进行肌肉力量和躯体功能评估，监测变化趋势。')
    return suggestions
  }

  // 运动建议
  if (sppbTotal.value != null && sppbTotal.value < 8) {
    suggestions.push('建议进行抗阻训练，每周2-3次，包括上下肢主要肌群。')
  }

  // 营养建议
  const bc = bodyCompositionList.value[0]
  if (bc && bc.asmi != null && ((gender.value === '1' || gender.value === 1) ? bc.asmi < 7.0 : bc.asmi < 5.7)) {
    suggestions.push('建议增加蛋白质摄入（每日1.2-1.6g/kg体重），必要时补充营养制剂。')
  }

  // 维生素D
  const grip = gripStrengthList.value[0]
  if (grip && ((gender.value === '1' || gender.value === 1) ? grip.maxValue < 28 : grip.maxValue < 18)) {
    suggestions.push('建议检测血清25-羟维生素D水平，必要时补充维生素D。')
  }

  // 平衡训练
  const balanceScore = functionItems.value.find(i => i.key === 'balance_stand')
  if (balanceScore?.score != null && balanceScore.score < 3) {
    suggestions.push('建议增加平衡训练，降低跌倒风险，如太极、单腿站立练习。')
  }

  // 就医建议
  if (level === 'severe') {
    suggestions.push('建议尽快到老年科或康复医学科就诊，进行全面评估和规范治疗。')
  }

  if (suggestions.length === 0) {
    suggestions.push('建议咨询临床医生结合临床情况进行综合判断。')
  }

  return suggestions
})

// 能否生成报告
const canGenerate = computed(() => {
  return sRecordList.value.length > 0
    || calfMeasureList.value.length > 0
    || gripStrengthList.value.length > 0
    || sitUpList.value.length > 0
    || paceList.value.length > 0
    || balanceStandList.value.length > 0
    || bodyCompositionList.value.length > 0
})

// ==================== 加载数据 ====================
async function loadAllData() {
  if (!patientId.value) return

  try {
    const [
      patientRes, sRes, calfRes, gripRes, sitUpRes, paceRes, balanceRes, bcRes
    ] = await Promise.all([
      selectPatientById({ patientId: patientId.value }).catch(() => null),
      selectSRecordList({ patientId: patientId.value }).catch(() => ({ data: [] })),
      selectCalfMeasureList({ patientId: patientId.value }).catch(() => ({ data: [] })),
      selectGripStrengthList({ patientId: patientId.value }).catch(() => ({ data: [] })),
      selectSitUpList({ patientId: patientId.value }).catch(() => ({ data: [] })),
      selectPaceList({ patientId: patientId.value }).catch(() => ({ data: [] })),
      selectBalanceStandList({ patientId: patientId.value }).catch(() => ({ data: [] })),
      selectBodyCompositionList({ patientId: patientId.value }).catch(() => ({ data: [] }))
    ])

    patientInfo.value = normalizePatient(patientRes)
    sRecordList.value = normalizeList(sRes)
    calfMeasureList.value = normalizeList(calfRes)
    gripStrengthList.value = normalizeList(gripRes)
    sitUpList.value = normalizeList(sitUpRes)
    paceList.value = normalizeList(paceRes)
    balanceStandList.value = normalizeList(balanceRes)
    bodyCompositionList.value = normalizeList(bcRes)
  } catch (e) {
    console.error('加载评估数据失败:', e)
  }
}

function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (res.data && Array.isArray(res.data)) return res.data
  if (res.rows && Array.isArray(res.rows)) return res.rows
  return []
}

function normalizePatient(res) {
  if (!res) return null
  if (res.data && typeof res.data === 'object') return res.data
  if (res.patientId !== undefined || res.id !== undefined) return res
  return null
}

function isSarcFRecord(record) {
  if (!record) return false
  const code = String(record.scaleCode || record.code || record.scale_code || '').toLowerCase()
  const scaleId = Number(record.scaleId ?? record.scale_id ?? record.projectId ?? record.project_id)
  return code === 'sarc_f' || code === 'sarc-f' || scaleId === 1
}

function firstValue(record, keys) {
  if (!record) return null
  for (const key of keys) {
    if (record[key] !== undefined && record[key] !== null && record[key] !== '') return record[key]
  }
  return null
}

function hasBasicValue(value) {
  if (Array.isArray(value)) return value.length > 0
  return value !== undefined && value !== null && String(value).trim() !== ''
}

function toNumber(value) {
  if (value === undefined || value === null || value === '') return null
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

function averageNumbers(values) {
  const nums = values.map(toNumber).filter(v => v != null && v > 0)
  if (!nums.length) return null
  return nums.reduce((sum, item) => sum + item, 0) / nums.length
}

function formatNumber(value, digits = 2) {
  const num = toNumber(value)
  if (num == null) return '--'
  return num.toFixed(digits)
}

function calculateBalanceScore(feet, semi, tandem) {
  let score = 0
  if (feet != null && feet >= 10) score += 1
  if (semi != null && semi >= 10) score += 1
  if (tandem != null) {
    if (tandem >= 10) score += 2
    else if (tandem >= 3) score += 1
  }
  return score
}

function measureValues(record) {
  if (!record) return []
  return [
    firstValue(record, ['firstValue', 'first_value', 'leftCalf', 'left_calf', 'leftValue', 'left_value']),
    firstValue(record, ['secondValue', 'second_value', 'rightCalf', 'right_calf', 'rightValue', 'right_value']),
    firstValue(record, ['thirdValue', 'third_value', 'maxValue', 'max_value'])
  ].map(toNumber).filter(v => v != null)
}

function formatMeasureValues(values, unit) {
  if (!values.length) return '--'
  return values.map((value, index) => `第${index + 1}次 ${formatNumber(value, 1)} ${unit}`).join(' / ')
}

// ==================== 跳转 ====================
function goToItem(item) {
  const config = ITEM_CONFIG[item.key]
  if (!config?.path) return
  router.push({
    path: config.path,
    query: {
      patientId: patientId.value,
      patientName: patientName.value,
      medicalCaseNo: medicalCaseNo.value,
      gender: gender.value,
      birthday: birthday.value,
      from: 'sComprehensive'
    }
  })
}

function goBack() {
  router.push({
    path: '/patient/detection/sarcopeniaSyndrome',
    query: route.query
  })
}

function goCustomize() {
  router.push({
    path: '/patient/detection/sCustomize',
    query: route.query
  })
}

function goBasicInformation() {
  router.push({
    path: '/patient/detection/customize/basicInformation',
    query: {
      ...route.query,
      patientId: patientId.value,
      patientName: patientName.value,
      medicalCaseNo: medicalCaseNo.value,
      gender: gender.value,
      birthday: birthday.value,
      from: 'sComprehensive'
    }
  })
}

async function generateReport() {
  if (!patientId.value) {
    ElMessage.warning('患者信息异常，无法生成报告')
    return
  }
  try {
    await generateSReport(patientId.value)
    ElMessage.success('报告生成成功')
    router.push({
      path: '/report/sreport'
    })
  } catch (err) {
    ElMessage.error(err?.msg || err?.message || '报告生成失败')
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadAllData()
})
</script>

<style scoped lang="scss">
.s-comp-page {
  min-height: 100vh;
  padding: 0 0 40px;
  background: #f5f7fa;
  box-sizing: border-box;
}

/* 顶部操作栏 */
.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  color: #606266;
  font-size: 14px;

  &:hover {
    color: #f59e0b;
    background: #fef3c7;
  }
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.top-right {
  display: flex;
  gap: 10px;
}

/* 内容区 */
.page-content {
  max-width: 1000px;
  margin: 24px auto;
  margin-left: auto;
  margin-right: auto;
  padding: 0 24px;
}

/* 报告标题 */
.report-header {
  text-align: center;
  margin-bottom: 20px;
}

.report-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #b45309;
}

.report-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #92400e;
  letter-spacing: 1px;
}

/* 患者信息卡 */
.patient-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  margin-bottom: 16px;
  border: 1px solid #fde68a;
  border-left: 4px solid #f59e0b;
}

.pc-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.pc-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pc-label {
  font-size: 12px;
  color: #909399;
}

.pc-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.basic-info-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 22px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffffff 72%);
  border: 1px solid #fed7aa;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(180, 83, 9, 0.06);
}

.basic-info-main {
  min-width: 0;
}

.basic-info-title {
  margin-bottom: 6px;
  font-size: 17px;
  font-weight: 700;
  color: #92400e;
}

.basic-info-desc {
  font-size: 13px;
  line-height: 1.6;
  color: #78716c;
}

/* 诊断卡 */
.diagnosis-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid;

  &.normal {
    background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    border-color: #86efac;
    .dg-icon { color: #16a34a; }
    .dg-result { color: #15803d; }
    .dg-num { color: #16a34a; }
  }

  &.risk {
    background: linear-gradient(135deg, #fefce8 0%, #fef9c3 100%);
    border-color: #fde047;
    .dg-icon { color: #ca8a04; }
    .dg-result { color: #a16207; }
    .dg-num { color: #ca8a04; }
  }

  &.mild {
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
    border-color: #fdba74;
    .dg-icon { color: #ea580c; }
    .dg-result { color: #c2410c; }
    .dg-num { color: #ea580c; }
  }

  &.severe {
    background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    border-color: #fca5a5;
    .dg-icon { color: #dc2626; }
    .dg-result { color: #b91c1c; }
    .dg-num { color: #dc2626; }
  }
}

.dg-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dg-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}

.dg-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.dg-result {
  font-size: 20px;
  font-weight: 700;
}

.dg-right {
  text-align: right;
}

.dg-score {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  gap: 4px;
  margin-bottom: 4px;
}

.dg-num {
  font-size: 32px;
  font-weight: 800;
  font-family: 'SF Mono', Menlo, monospace;
}

.dg-total {
  font-size: 14px;
  color: #6b7280;
}

.dg-desc {
  font-size: 12px;
  color: #6b7280;
}

/* 分段卡片 */
.section-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: linear-gradient(90deg, #fffbeb 0%, #fff 100%);
  border-bottom: 1px solid #fde68a;
}

.section-index {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f59e0b;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  border-radius: 6px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #92400e;
}

.section-desc {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.section-body {
  padding: 16px 20px;
}

.text-muted {
  color: #c0c4cc;
}

.score-text {
  font-size: 14px;
  color: #303133;

  strong {
    font-size: 18px;
    color: #f59e0b;
    font-weight: 700;
  }
}

/* SPPB 汇总 */
.sppb-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: 8px;
  border: 1px dashed #fbbf24;
}

.sppb-label {
  font-size: 14px;
  color: #92400e;
  font-weight: 600;
}

.sppb-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.sppb-num {
  font-size: 32px;
  font-weight: 800;
  color: #d97706;
  font-family: 'SF Mono', Menlo, monospace;
}

.sppb-total {
  font-size: 14px;
  color: #92400e;
}

/* 建议卡 */
.suggestion-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.sg-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  background: linear-gradient(90deg, #eff6ff 0%, #fff 100%);
  border-bottom: 1px solid #bfdbfe;
  font-size: 16px;
  font-weight: 700;
  color: #1e40af;

  .el-icon {
    color: #3b82f6;
  }
}

.sg-content {
  padding: 16px 20px;
}

.sg-empty {
  text-align: center;
  color: #909399;
  padding: 20px 0;
  font-size: 14px;
}

.sg-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.sg-list li {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.sg-num {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3b82f6;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  border-radius: 50%;
}

.sg-text {
  flex: 1;
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .pc-row {
    grid-template-columns: repeat(3, 1fr);
  }

  .page-content {
    margin: 16px;
    padding: 0;
  }

  .diagnosis-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .dg-right {
    text-align: center;
  }
}
</style>
