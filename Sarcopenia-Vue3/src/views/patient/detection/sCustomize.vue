<template>
  <div class="s-customize-container">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <div class="top-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="title-block">
          <h2>肌少症评估</h2>
          <p>Sarcopenia Assessment</p>
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
            {{ String(section.startIdx + idx + 1).padStart(2, '0') }}
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

<script setup name="SCustomizeAssessment">
import { computed, ref, markRaw, onMounted, onActivated } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Files, User, Check, ArrowRight,
  Document, DocumentChecked, Dish, DataAnalysis,
  View, HomeFilled, Setting,
  UserFilled, MagicStick, ChatDotRound, Monitor, DataLine, Aim, DataBoard
} from '@element-plus/icons-vue'

import { selectSRecordList, selectCalfMeasureList, selectGripStrengthList, selectSitUpList, selectPaceList, selectBalanceStandList, selectBodyCompositionList } from '@/api/s.js'

const route = useRoute()
const router = useRouter()

// ==================== 常量配置 ====================
/** 本地存储完成状态前缀 */
const STORAGE_DONE_PREFIX = 'sarcopenia_customize_done:'

/** 评估模块基础配置（静态配置与状态分离，便于维护） */
const ASSESSMENT_SECTION_CONFIG = [
  {
    index: '一',
    title: '基本情况',
    desc: '',
    startIdx: 0,
    items: [
      {
        name: '基本信息采集',
        color: '#1e40af',
        icon: markRaw(Document),
        code: 'basic_information',
        path: '/patient/detection/customize/basicInformation',
        projectId: 44,
        doneCodes: ['basic_information']
      }
    ]
  },
  {
    index: '二',
    title: '筛查与评估',
    desc: '肌少症筛查与核心诊断',
    startIdx: 0,
    items: [
      {
        name: 'SARC-F 量表',
        subTitle: '肌力、辅助行走、坐站、爬楼、跌倒',
        color: '#f59e0b',
        icon: markRaw(DataAnalysis),
        code: 'sarc_f',
        doneCodes: ['sarc_f', 'sarcopenia_screen'],
        path: '/patient/detection/sCustomize/sSarcF',
        projectId: 1
      },
      {
        name: '小腿围测量',
        subTitle: 'Calf Circumference',
        color: '#ea580c',
        icon: markRaw(UserFilled),
        code: 'calf_measure',
        doneCodes: ['calf_measure'],
        path: '/patient/detection/sCustomize/sCalfMeasure',
        projectId: 0,
        isCustom: true
      },
      {
        name: '握力测量',
        subTitle: 'Grip Strength · 肌力评估',
        color: '#dc2626',
        icon: markRaw(Monitor),
        code: 'grip_strength',
        doneCodes: ['grip_strength'],
        path: '/patient/detection/sCustomize/gripStrength',
        projectId: 0,
        isCustom: true
      },
      {
        name: '5次起坐测试',
        subTitle: '5 Times Sit-to-Stand · 下肢肌力',
        color: '#8b5cf6',
        icon: markRaw(MagicStick),
        code: 'sit_up',
        doneCodes: ['sit_up'],
        path: '/patient/detection/sCustomize/situp',
        projectId: 0,
        isCustom: true
      },
      {
        name: '6米步速测量',
        subTitle: '6-Meter Gait Speed · 躯体功能',
        color: '#10b981',
        icon: markRaw(DataLine),
        code: 'pace',
        doneCodes: ['pace'],
        path: '/patient/detection/sCustomize/pace',
        projectId: 0,
        isCustom: true
      },
      {
        name: '站立平衡测试',
        subTitle: 'Standing Balance · SPPB平衡功能',
        color: '#f97316',
        icon: markRaw(Aim),
        code: 'balance_stand',
        doneCodes: ['balance_stand'],
        path: '/patient/detection/sCustomize/balanceStand',
        projectId: 0,
        isCustom: true
      },
      {
        name: '人体成分测量',
        subTitle: 'Body Composition · 肌肉量体脂分析',
        color: '#0ea5e9',
        icon: markRaw(DataBoard),
        code: 'body_composition',
        doneCodes: ['body_composition'],
        path: '/patient/detection/sCustomize/bodyComposition',
        projectId: 0,
        isCustom: true
      },
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
/** 患者ID（响应式获取路由参数） */
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
/** 当天日期字符串 */
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
 */
function getStorageDoneKey(patientId, code) {
  return `${STORAGE_DONE_PREFIX}${patientId}:${code}`
}

/**
 * 标记评估项为完成/未完成（本地存储）
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
 */
function isItemDoneByLocal(item) {
  const codes = item.doneCodes || [item.code]
  return codes.some(code => isCustomizeDone(code))
}

/**
 * 根据接口返回的记录判断评估项是否完成
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
  let calfMeasureList = []
  let gripStrengthList = []
  let sitUpList = []
  let paceList = []
  let balanceStandList = []
  let bodyCompositionList = []
  try {
    const res = await selectSRecordList({ patientId: patientId.value })
    recordList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载肌少症评估记录失败', error)
  }
  try {
    const res = await selectCalfMeasureList({ patientId: patientId.value })
    calfMeasureList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载小腿围测量记录失败', error)
  }
  try {
    const res = await selectGripStrengthList({ patientId: patientId.value })
    gripStrengthList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载握力测量记录失败', error)
  }
  try {
    const res = await selectSitUpList({ patientId: patientId.value })
    sitUpList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载5次起坐测量记录失败', error)
  }
  try {
    const res = await selectPaceList({ patientId: patientId.value })
    paceList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载6米步速测量记录失败', error)
  }
  try {
    const res = await selectBalanceStandList({ patientId: patientId.value })
    balanceStandList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载站立平衡测试记录失败', error)
  }
  try {
    const res = await selectBodyCompositionList({ patientId: patientId.value })
    bodyCompositionList = normalizeListResponse(res)
  } catch (error) {
    console.error('加载人体成分测量记录失败', error)
  }

  // 更新所有评估项的完成状态
  assessmentSections.value.forEach(section => {
    section.items.forEach(item => {
      let done = isItemDoneByLocal(item) || isItemDoneByRecord(item, recordList)
      // 小腿围走独立表
      if (item.code === 'calf_measure' && calfMeasureList.length > 0) {
        done = true
      }
      // 握力走独立表
      if (item.code === 'grip_strength' && gripStrengthList.length > 0) {
        done = true
      }
      // 5次起坐走独立表
      if (item.code === 'sit_up' && sitUpList.length > 0) {
        done = true
      }
      // 6米步速走独立表
      if (item.code === 'pace' && paceList.length > 0) {
        done = true
      }
      // 站立平衡走独立表
      if (item.code === 'balance_stand' && balanceStandList.length > 0) {
        done = true
      }
      // 人体成分测量走独立表
      if (item.code === 'body_composition' && bodyCompositionList.length > 0) {
        done = true
      }
      item.completed = done
    })
  })
}

/**
 * 跳转至对应评估项页面
 */
function startAssessment(item) {
  if (!item.path) {
    // 暂未实现的页面
    return
  }
  router.push({
    path: item.path,
    query: {
      patientId: patientId.value,
      patientName: patientName.value,
      scaleId: item.code,
      scaleCode: item.code,
      projectId: item.projectId || 1,
      from: 'sCustomize'
    }
  })
}

/** 返回上一页 */
function goBack() {
  router.push({
    path: '/patient/detection/sarcopeniaSyndrome',
    query: {
      patientId: patientId.value,
      patientName: patientName.value
    }
  })
}

/** 跳转至综合报告页面 */
function goComprehensiveReport() {
  router.push({
    path: '/patient/detection/sComprehensive',
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
.s-customize-container {
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
  border-left: 3px solid #f59e0b;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .patient-avatar {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: #fff7ed;
    color: #f59e0b;
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
      color: #c2410c;
      background: #fed7aa;
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
      color: #f59e0b;
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
    color: #92400e;
    background: #fef3c7;
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
    border-color: #f59e0b;
    box-shadow: 0 8px 24px rgba(245, 158, 11, 0.15);
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
    color: #f59e0b;
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
    color: #f59e0b;
  }
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .s-customize-container {
    padding: 16px 14px 30px;
  }

  .card-grid {
    grid-template-columns: 1fr;
  }

  .patient-bar {
    grid-template-columns: 1fr;
    gap: 10px;

    .progress-wrap {
      min-width: 0;
    }
  }

  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;

    .top-right {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
