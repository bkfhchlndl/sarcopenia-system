<template>
  <div class="customize-container">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <div class="top-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="title-block">
          <h2>老年综合评估</h2>
          <p>Comprehensive Geriatric Assessment</p>
        </div>
      </div>
      <div class="top-right">
        <el-button type="primary" @click="goComprehensiveReport">
          <el-icon><Files /></el-icon>
          查看综合报告
        </el-button>
      </div>
    </div>

    <!-- 患者信息条 -->
    <div class="patient-bar">
      <div class="patient-avatar">
        <el-icon><User /></el-icon>
      </div>
      <div class="patient-info">
        <div class="patient-name">
          {{ patientName }}
          <span class="tag">进行中</span>
        </div>
        <div class="patient-meta">
          病历号:{{ patientId || '-' }} · {{ todayText }} · 共 {{ totalCount }} 项评估,已完成 {{ completedCount }} 项
        </div>
      </div>
      <div class="progress-wrap">
        <el-progress
            :percentage="progressPercent"
            :stroke-width="8"
            :show-text="false"
            class="progress"
        />
        <span class="progress-text">{{ progressPercent }}%</span>
      </div>
    </div>

    <!-- 分组评估卡片 -->
    <div v-for="section in assessmentSections" :key="section.title" class="section">
      <div class="section-head">
        <span class="section-index">{{ section.index }}</span>
        <span class="section-title">{{ section.title }}</span>
        <span v-if="section.desc" class="section-sub">{{ section.desc }}</span>
        <span class="section-count">{{ section.items.length }} 项</span>
      </div>

      <div class="card-grid">
        <div
            v-for="(item, idx) in section.items"
            :key="item.code"
            class="assess-card"
            :class="{ 'is-done': item.completed }"
            @click="startAssessment(item)"
        >
          <div class="assess-index">
            {{ String(section.startIdx + idx).padStart(2, '0') }}
          </div>
          <div
              class="assess-icon"
              :style="{ color: item.color, background: item.color + '18' }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="assess-text">
            <div class="assess-title">{{ item.name }}</div>
            <div v-if="item.subTitle" class="assess-sub">{{ item.subTitle }}</div>
          </div>
          <div class="assess-status">
            <span v-if="item.completed" class="status-done">
              <el-icon><Check /></el-icon>
              已完成
            </span>
            <span v-else class="status-pending">
              开始
              <el-icon><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CustomizeAssessment">
import { computed, ref, markRaw, onMounted, onActivated } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Files, User, Check, ArrowRight,
  Document, DocumentChecked, DocumentAdd,
  Dish, DataAnalysis, View, Loading, HomeFilled, Setting,
  UserFilled, MagicStick, ChatDotRound, Monitor
} from '@element-plus/icons-vue'

import { getFullCgaReportByPatient } from '@/api/cgaReport.js'
import { selectCgaRecordList } from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
/** 本地存储完成状态前缀 */
const STORAGE_DONE_PREFIX = 'customize_done:'

/** 评估模块基础配置（静态配置与状态分离，便于维护） */
const ASSESSMENT_SECTION_CONFIG = [
  {
    index: '一',
    title: '基本情况',
    desc: '',
    startIdx: 0,
    items: [
      {
        name: '家庭情况与疾病史',
        color: '#1e40af',
        icon: markRaw(Document),
        code: 'disease_history',
        path: '/patient/detection/customize/basicInformation',
        projectId: 99,
        doneCodes: ['disease_history']
      }
    ]
  },
  {
    index: '二',
    title: '躯体功能',
    desc: '日常活动与躯体健康状况',
    startIdx: 0,
    items: [
      { name: '基本日常生活', subTitle: 'ADL 巴氏指数评定', color: '#22c55e', icon: markRaw(Dish), code: 'daily_life', doneCodes: ['daily_life', 'basic_adl'], path: '/patient/detection/customize/dailyLife', projectId: 22 },
      { name: '工具性日常生活', subTitle: 'IADL 工具性日常活动', color: '#10b981', icon: markRaw(Files), code: 'instrumental_life', doneCodes: ['instrumental_life', 'instrumental_adl'], path: '/patient/detection/customize/instrumentalityLife', projectId: 23 },
      { name: '运动功能', subTitle: 'SPPB 简易体能 / 步速 / 平衡', color: '#f97316', icon: markRaw(DataAnalysis), code: 'sppb', doneCodes: ['sppb', 'exercise_function'], path: '/patient/detection/customize/sppb', projectId: 24 },
      { name: '视力视觉功能', subTitle: '视力简易评估 / 视觉功能', color: '#8b5cf6', icon: markRaw(View), code: 'visual_acuity', doneCodes: ['visual_acuity'], path: '/patient/detection/customize/visualAcuity', projectId: 25 },
      { name: '听力', subTitle: '听力简易评估 / 耳语试验', color: '#6366f1', icon: markRaw(Loading), code: 'listening', doneCodes: ['listening', 'hearing_assessment'], path: '/patient/detection/customize/listening', projectId: 26 },
      { name: '居家环境', subTitle: '居家环境安全评估', color: '#0891b2', icon: markRaw(HomeFilled), code: 'home_environment', doneCodes: ['home_environment'], path: '/patient/detection/customize/homeEnvironment', projectId: 27 },
      { name: '衰弱', subTitle: 'FRAIL 衰弱量表', color: '#ca8a04', icon: markRaw(Setting), code: 'frail', doneCodes: ['frail', 'frail_scale'], path: '/patient/detection/customize/frail', projectId: 29 },
      { name: '尿失禁', subTitle: 'ICI-Q-SF 尿失禁问卷', color: '#ec4899', icon: markRaw(UserFilled), code: 'urinary_incontinence', doneCodes: ['urinary_incontinence'], path: '/patient/detection/customize/urinaryIncontinence', projectId: 30 },
      { name: '便秘', subTitle: '便秘症状评估', color: '#a855f7', icon: markRaw(View), code: 'constipation_screen', doneCodes: ['constipation_screen'], path: '/patient/detection/customize/constipation', projectId: 31 },
      { name: '慢性疼痛', subTitle: '疼痛数字评分 NRS', color: '#ef4444', icon: markRaw(Document), code: 'chronic_pain', doneCodes: ['chronic_pain', 'chronicPain'], path: '/patient/detection/customize/chronicPain', projectId: 33 },
      { name: '压力性损伤风险', subTitle: 'Braden 压疮风险评估', color: '#d97706', icon: markRaw(DocumentChecked), code: 'pressure_injury', doneCodes: ['pressure_injury', 'pressureInjury'], path: '/patient/detection/customize/pressureInjury', projectId: 34 },
      { name: '吞咽功能', subTitle: '洼田饮水试验', color: '#0d9488', icon: markRaw(Document), code: 'swallow', doneCodes: ['swallow'], path: '/patient/detection/customize/swallow', projectId: 35 },
      { name: '营养风险', subTitle: 'MNA-SF 微型营养评估', color: '#84cc16', icon: markRaw(Dish), code: 'nutrition', doneCodes: ['nutrition'], path: '/patient/detection/customize/nutrition', projectId: 36 },
      { name: '跌倒风险', subTitle: 'Morse 跌倒风险评估', color: '#eab308', icon: markRaw(Document), code: 'fall', doneCodes: ['fall'], path: '/patient/detection/customize/fall', projectId: 41 },
      { name: '多重用药', subTitle: '用药清单核查', color: '#14b8a6', icon: markRaw(Document), code: 'medication', doneCodes: ['medication'], path: '/patient/detection/customize/medication', projectId: 42 },
      { name: '微营养', subTitle: '微营养评估', color: '#f43f5e', icon: markRaw(DocumentAdd), code: 'micro_nutrition', doneCodes: ['micro_nutrition', 'microNutrition'], path: '/patient/detection/customize/microNutrition', projectId: 37 }
    ]
  },
  {
    index: '三',
    title: '精神心理',
    desc: '认知、情绪与心理状态',
    startIdx: 16,
    items: [
      { name: '认知', subTitle: '简易智力评估', color: '#0891b2', icon: markRaw(MagicStick), code: 'cognition', doneCodes: ['cognition', 'cognitive_assessment'], path: '/patient/detection/customize/cognition', projectId: 28 },
      { name: '睡眠障碍', subTitle: '阿森斯失眠量表 AIS', color: '#0ea5e9', icon: markRaw(Monitor), code: 'insomnia', doneCodes: ['insomnia', 'ais'], path: '/patient/detection/customize/insomnia', projectId: 32 },
      { name: '谵妄', subTitle: 'CAM 意识模糊评估', color: '#a855f7', icon: markRaw(Setting), code: 'delirium', doneCodes: ['delirium'], path: '/patient/detection/customize/delirium', projectId: 39 },
      { name: '抑郁', subTitle: 'GDS-15 老年抑郁量表', color: '#06b6d4', icon: markRaw(ChatDotRound), code: 'depression', doneCodes: ['depression'], path: '/patient/detection/customize/depression', projectId: 40 },
      { name: '焦虑', subTitle: 'GAD-7 广泛性焦虑量表', color: '#f59e0b', icon: markRaw(ChatDotRound), code: 'anxiety', doneCodes: ['anxiety'], path: '/patient/detection/customize/anxiety', projectId: 42 }
    ]
  }
]

// ==================== 响应式状态 ====================
/** 评估模块列表（带完成状态） */
const assessmentSections = ref(
    ASSESSMENT_SECTION_CONFIG.map(section => ({
      ...section,
      items: section.items.map(item => ({ ...item, completed: false }))
    }))
)

// ==================== 计算属性 ====================
/** 患者ID（响应式获取路由参数，修复原代码静态取值丢失响应性的问题） */
const patientId = computed(() => route.query.patientId || '')

/** 患者姓名 */
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

/** 评估项总数 */
const totalCount = computed(() => {
  return assessmentSections.value.reduce((sum, section) => sum + section.items.length, 0)
})

/** 已完成评估项数 */
const completedCount = computed(() => {
  return assessmentSections.value.reduce((sum, section) => {
    return sum + section.items.filter(item => item.completed).length
  }, 0)
})

/** 完成进度百分比 */
const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((completedCount.value / totalCount.value) * 100)
})

// ==================== 常量值 ====================
/** 当天日期字符串（页面初始化时生成，当天内无需更新） */
const todayText = (() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})()

// ==================== 工具函数 ====================
/**
 * 生成本地存储完成状态的key
 * @param {string|number} patientId 患者ID
 * @param {string} code 评估项编码
 * @returns {string}
 */
function getStorageDoneKey(patientId, code) {
  return `${STORAGE_DONE_PREFIX}${patientId}:${code}`
}

/**
 * 标记评估项为完成/未完成（本地存储）
 * @param {string} code 评估项编码
 * @param {boolean} value 是否完成
 */
function markCustomizeDone(code, value = true) {
  if (!patientId.value || !code) return
  try {
    const key = getStorageDoneKey(patientId.value, code)
    if (value) {
      localStorage.setItem(key, '1')
    } else {
      localStorage.removeItem(key)
    }
  } catch (error) {
    console.warn('本地存储写入失败', error)
  }
}

/**
 * 从本地存储判断评估项是否完成
 * @param {string} code 评估项编码
 * @returns {boolean}
 */
function isCustomizeDone(code) {
  if (!patientId.value || !code) return false
  try {
    return localStorage.getItem(getStorageDoneKey(patientId.value, code)) === '1'
  } catch (error) {
    return false
  }
}

/**
 * 根据本地存储判断评估项是否完成（兼容多编码映射）
 * @param {object} item 评估项配置
 * @returns {boolean}
 */
function isItemDoneByLocal(item) {
  const codes = item.doneCodes || [item.code]
  return codes.some(code => isCustomizeDone(code))
}

/**
 * 根据接口返回的记录判断评估项是否完成
 * @param {object} item 评估项配置
 * @param {Array} recordList 评估记录列表
 * @returns {boolean}
 */
function isItemDoneByRecord(item, recordList) {
  const targetProjectId = Number(item.projectId)
  if (!targetProjectId) return false

  return recordList.some(record => {
    const recordScaleId = Number(record?.scaleId || record?.projectId)
    const delFlag = String(record?.delFlag ?? '0')
    return delFlag === '0' && recordScaleId === targetProjectId
  })
}

/**
 * 标准化接口返回的列表数据
 * @param {*} response 接口返回值
 * @returns {Array}
 */
function normalizeListResponse(response) {
  if (Array.isArray(response?.data)) return response.data
  if (Array.isArray(response?.rows)) return response.rows
  if (Array.isArray(response)) return response
  return []
}

// ==================== 业务方法 ====================
/**
 * 刷新所有评估项的完成状态
 * 优先级：接口记录 > 本地存储
 */
async function refreshCompletionStatus() {
  if (!patientId.value) return

  let recordList = []
  try {
    const res = await selectCgaRecordList({ patientId: patientId.value })
    recordList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载评估记录失败', error)
  }

  // 更新所有评估项的完成状态
  assessmentSections.value.forEach(section => {
    section.items.forEach(item => {
      item.completed = isItemDoneByLocal(item) || isItemDoneByRecord(item, recordList)
    })
  })

  // 兼容基本情况：若综合报告中有病史内容也视为完成
  try {
    const reportRes = await getFullCgaReportByPatient(patientId.value)
    const report = reportRes?.data || {}
    const hasHistoryContent = Boolean(
        (report.result && String(report.result).trim()) ||
        (report.suggest && String(report.suggest).trim())
    )

    const basicSection = assessmentSections.value[0]
    if (basicSection?.items?.[0]) {
      basicSection.items[0].completed = basicSection.items[0].completed || hasHistoryContent
    }
  } catch (error) {
    console.error('加载综合报告失败', error)
  }
}

/**
 * 跳转至对应评估项页面
 * @param {object} item 评估项配置
 */
function startAssessment(item) {
  router.push({
    path: item.path || '/patient/detection/cga',
    query: {
      patientId: patientId.value,
      patientName: patientName.value,
      scaleId: item.code,
      scaleCode: item.code,
      projectId: item.projectId || 1
    }
  })
}

/** 返回上一页 */
function goBack() {
  router.push({
    path: '/patient/detection/geriatricsyndrome',
    query: {
      patientId: patientId.value,
      patientName: patientName.value
    }
  })
}

/** 跳转至综合报告页面 */
function goComprehensiveReport() {
  router.push({
    path: '/patient/detection/comprehensive',
    query: {
      patientId: patientId.value,
      patientName: patientName.value
    }
  })
}

// ==================== 生命周期 ====================
onMounted(() => {
  refreshCompletionStatus()
})

onActivated(() => {
  refreshCompletionStatus()
})
</script>

<style scoped lang="scss">
.customize-container {
  min-height: 100vh;
  padding: 20px 24px 40px;
  background: #f5f7fb;
  box-sizing: border-box;
}

/* ===== 顶部操作栏 ===== */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  margin-bottom: 18px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .top-left {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .top-right {
    display: flex;
    gap: 10px;
  }

  .title-block {
    h2 {
      margin: 0;
      font-size: 17px;
      font-weight: 600;
      color: #1f2937;
    }

    p {
      margin: 2px 0 0;
      font-size: 11.5px;
      color: #9ca3af;
      letter-spacing: 1px;
    }
  }
}

/* ===== 患者信息条 ===== */
.patient-bar {
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  margin-bottom: 22px;
  background: #fff;
  border-radius: 12px;
  border-left: 3px solid #3b82f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .patient-avatar {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: #eff6ff;
    color: #3b82f6;
    font-size: 20px;
  }

  .patient-name {
    margin-bottom: 4px;
    font-size: 15px;
    font-weight: 600;
    color: #111827;

    .tag {
      margin-left: 6px;
      padding: 2px 8px;
      font-size: 11px;
      font-weight: 600;
      color: #2563eb;
      background: #dbeafe;
      border-radius: 999px;
    }
  }

  .patient-meta {
    font-size: 12.5px;
    color: #6b7280;
  }

  .progress-wrap {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 200px;

    .progress {
      flex: 1;
    }

    .progress-text {
      min-width: 40px;
      text-align: right;
      font-size: 13px;
      font-weight: 600;
      color: #3b82f6;
    }
  }
}

/* ===== 分组模块 ===== */
.section {
  margin-bottom: 28px;

  .section-head {
    display: flex;
    align-items: baseline;
    gap: 12px;
    padding: 0 2px;
    margin-bottom: 14px;
  }

  .section-index {
    width: 32px;
    height: 32px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    font-weight: 700;
    color: #1e40af;
    background: #dbeafe;
    border-radius: 8px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 700;
    color: #111827;
  }

  .section-sub {
    font-size: 12.5px;
    color: #9ca3af;
  }

  .section-count {
    margin-left: auto;
    padding: 4px 12px;
    font-size: 11.5px;
    font-weight: 600;
    color: #475569;
    background: #e2e8f0;
    border-radius: 999px;
  }
}

/* ===== 卡片网格 ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

/* ===== 评估卡片 ===== */
.assess-card {
  display: grid;
  grid-template-columns: 28px 48px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 20px 18px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #3b82f6;
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
  }

  &.is-done {
    border-color: #22c55e;
    background: linear-gradient(180deg, #f0fdf4 0%, #fff 60%);
  }

  .assess-index {
    text-align: center;
    font-size: 13px;
    font-weight: 700;
    color: #94a3b8;
    font-family: 'SF Mono', Menlo, Consolas, monospace;
  }

  &:hover .assess-index {
    color: #3b82f6;
  }

  .assess-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    font-size: 22px;
  }

  .assess-title {
    margin-bottom: 3px;
    font-size: 14.5px;
    font-weight: 600;
    color: #111827;
  }

  .assess-sub {
    font-size: 11.5px;
    color: #6b7280;
    line-height: 1.4;
  }

  .assess-status {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12.5px;
    font-weight: 600;
  }

  .status-done {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: #16a34a;
  }

  .status-pending {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: #3b82f6;
  }
}

/* ===== 响应式适配 ===== */
@media (max-width: 900px) {
  .customize-container {
    padding: 14px 12px 30px;
  }

  .top-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .patient-bar {
    grid-template-columns: 44px 1fr;

    .progress-wrap {
      grid-column: 1 / -1;
    }
  }

  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>