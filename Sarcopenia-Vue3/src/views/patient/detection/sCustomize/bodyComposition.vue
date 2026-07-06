<template>
  <div class="body-comp-page">
    <!-- 顶部导航 -->
    <header class="page-header">
      <div class="header-left">
        <el-button text class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="header-title">
          <h1>人体成分测量</h1>
          <p>Body Composition Analysis</p>
        </div>
      </div>
      <!-- 设备状态 -->
      <div class="device-status" :class="{ online: deviceOnline }">
        <span class="status-dot"></span>
        <span class="status-text">{{ deviceOnline ? '设备已连接' : '设备未连接' }}</span>
      </div>
    </header>

    <div class="page-body">
      <!-- 左侧主区域 -->
      <div class="main-panel">
        <!-- 测量卡片 -->
        <div class="measure-card">
          <!-- 设备示意图区 -->
          <div class="device-area">
            <div class="device-visual">
              <!-- 设备主体 -->
              <div class="scale-device">
                <div class="scale-platform">
                  <div class="platform-surface">
                    <div class="foot-print left-foot"></div>
                    <div class="foot-print right-foot"></div>
                  </div>
                  <div class="platform-side"></div>
                </div>
                <div class="scale-display">
                  <div class="display-screen">
                    <span v-if="measureStatus === 'idle'">--</span>
                    <span v-else-if="measureStatus === 'measuring'">{{ currentDisplay }}</span>
                    <span v-else>✓ 完成</span>
                  </div>
                </div>
                <div class="scale-stand"></div>
              </div>
              <!-- 手柄 -->
              <div class="hand-electrodes">
                <div class="electrode left">
                  <div class="electrode-body"></div>
                  <div class="electrode-wire"></div>
                </div>
                <div class="electrode right">
                  <div class="electrode-body"></div>
                  <div class="electrode-wire"></div>
                </div>
              </div>
              <!-- 测量中光效 -->
              <div v-if="measureStatus === 'measuring'" class="scan-effect">
                <div class="scan-line"></div>
              </div>
            </div>

            <!-- 状态文字 -->
            <div class="measure-status-text">
              <template v-if="!deviceOnline">
                <el-icon class="offline"><Warning /></el-icon>
                <span>设备未连接，请检查设备电源和网络</span>
              </template>
              <template v-else-if="measureStatus === 'idle'">
                <el-icon class="ready"><CircleCheckFilled /></el-icon>
                <span>设备就绪，请站在体脂秤上，双手握住手柄</span>
              </template>
              <template v-else-if="measureStatus === 'measuring'">
                <el-icon class="measuring"><Loading /></el-icon>
                <span>{{ currentPhase }}</span>
              </template>
              <template v-else>
                <el-icon class="success"><CircleCheckFilled /></el-icon>
                <span>测量完成，数据已同步</span>
              </template>
            </div>
          </div>

          <!-- 操作区 -->
          <div class="action-area">
            <template v-if="!deviceOnline">
              <el-button type="primary" size="large" class="btn-connect" @click="connectDevice">
                <el-icon><Link /></el-icon>
                连接设备
              </el-button>
            </template>
            <template v-else-if="measureStatus === 'idle'">
              <el-button type="primary" size="large" class="btn-start" @click="startMeasure">
                <el-icon><VideoPlay /></el-icon>
                开始测量
              </el-button>
            </template>
            <template v-else-if="measureStatus === 'measuring'">
              <el-button type="danger" size="large" plain class="btn-stop" @click="stopMeasure">
                <el-icon><Close /></el-icon>
                停止测量
              </el-button>
            </template>
            <template v-else>
              <el-button size="large" class="btn-retry" @click="retryMeasure">
                <el-icon><RefreshRight /></el-icon>
                重新测量
              </el-button>
              <el-button type="primary" size="large" class="btn-save" @click="handleSubmit">
                <el-icon><Check /></el-icon>
                保存记录
              </el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- 右侧结果面板 -->
      <div class="side-panel">
        <!-- 结果卡片 -->
        <div class="result-card" v-if="measureStatus === 'completed' && resultData">
          <div class="result-header">
            <span class="result-badge">测量结果</span>
            <span class="result-date">{{ formatDate(new Date()) }}</span>
          </div>
          
          <!-- 核心指标 ASMI -->
          <div class="asmi-highlight">
            <div class="asmi-label">四肢骨骼肌指数 (ASMI)</div>
            <div class="asmi-value">
              <span class="asmi-num">{{ resultData.asmi ?? '--' }}</span>
              <span class="asmi-unit">kg/m²</span>
            </div>
            <div class="asmi-tag" :class="asmiLevel.class">
              <el-icon><TrendCharts /></el-icon>
              {{ asmiLevel.text }}
            </div>
          </div>

          <!-- 指标网格 -->
          <div class="metrics-grid">
            <div class="metric-item">
              <div class="metric-icon blue"><el-icon><DataBoard /></el-icon></div>
              <div class="metric-info">
                <div class="metric-name">体重</div>
                <div class="metric-val">{{ resultData.weight ?? '--' }} <em>kg</em></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-icon green"><el-icon><DataLine /></el-icon></div>
              <div class="metric-info">
                <div class="metric-name">BMI</div>
                <div class="metric-val">{{ resultData.bmi ?? '--' }} <em>kg/m²</em></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-icon orange"><el-icon><Document /></el-icon></div>
              <div class="metric-info">
                <div class="metric-name">体脂率</div>
                <div class="metric-val">{{ resultData.bodyFatRate ?? '--' }} <em>%</em></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-icon purple"><el-icon><Muscle /></el-icon></div>
              <div class="metric-info">
                <div class="metric-name">骨骼肌量</div>
                <div class="metric-val">{{ resultData.asm ?? '--' }} <em>kg</em></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-icon cyan"><el-icon><Dish /></el-icon></div>
              <div class="metric-info">
                <div class="metric-name">身体总水分</div>
                <div class="metric-val">{{ resultData.totalBodyWater ?? '--' }} <em>L</em></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-icon pink"><el-icon><Aim /></el-icon></div>
              <div class="metric-info">
                <div class="metric-name">去脂体重</div>
                <div class="metric-val">{{ resultData.leanBodyMass ?? '--' }} <em>kg</em></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 测量中进度 -->
        <div class="progress-card" v-else-if="measureStatus === 'measuring'">
          <div class="progress-ring-wrap">
            <el-progress
              type="circle"
              :percentage="measureProgress"
              :width="160"
              :stroke-width="10"
              :color="progressColor"
            />
          </div>
          <div class="progress-phase">{{ currentPhase }}</div>
          <div class="progress-tip">请保持站立不动，不要说话</div>
          <div class="progress-steps">
            <div class="step" :class="{ active: measureProgress >= 15, done: measureProgress >= 30 }">
              <span class="step-dot"></span>
              <span class="step-text">体重测量</span>
            </div>
            <div class="step-line" :class="{ active: measureProgress >= 30 }"></div>
            <div class="step" :class="{ active: measureProgress >= 30, done: measureProgress >= 60 }">
              <span class="step-dot"></span>
              <span class="step-text">阻抗分析</span>
            </div>
            <div class="step-line" :class="{ active: measureProgress >= 60 }"></div>
            <div class="step" :class="{ active: measureProgress >= 60, done: measureProgress >= 100 }">
              <span class="step-dot"></span>
              <span class="step-text">生成报告</span>
            </div>
          </div>
        </div>

        <!-- 未开始占位 -->
        <div class="placeholder-card" v-else>
          <div class="ph-icon">
            <el-icon :size="64"><Monitor /></el-icon>
          </div>
          <div class="ph-title">{{ deviceOnline ? '设备已就绪' : '等待设备连接' }}</div>
          <div class="ph-desc">
            {{ deviceOnline ? '点击"开始测量"获取人体成分数据' : '请确保设备已开机并连接网络' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="BodyComposition">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Check, Close, VideoPlay, RefreshRight,
  Loading, Warning, CircleCheckFilled, Link,
  Monitor, Clock, DataBoard, DataLine,
  Dish, Document, Aim, TrendCharts
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectBodyCompositionList, insertBodyComposition } from '@/api/s.js'

// 用 SVG 画一个肌肉图标
const Muscle = {
  name: 'MuscleIcon',
  template: `<svg viewBox="0 0 24 24" fill="currentColor" width="1em" height="1em">
    <path d="M12 2C8.5 2 6 4.5 6 8c0 2 1 4 3 5l-1 7h8l-1-7c2-1 3-3 3-5 0-3.5-2.5-6-6-6z"/>
  </svg>`
}

const route = useRoute()
const router = useRouter()

// ==================== 状态 ====================
const deviceOnline = ref(false)
const measureStatus = ref('idle') // idle / measuring / completed
const measureProgress = ref(0)
const currentPhase = ref('')
const currentDisplay = ref('0.0')
const submitting = ref(false)
const historyList = ref([])
const resultData = ref(null)

let statusTimer = null
let measureTimer = null

// ==================== 计算属性 ====================
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

const asmiLevel = computed(() => {
  const v = resultData.value?.asmi
  if (v == null) return { text: '待测量', class: '' }
  if (v >= 7.0) return { text: '肌肉量充足', class: 'good' }
  if (v >= 5.7) return { text: '肌肉量偏低', class: 'warn' }
  return { text: '肌肉量不足', class: 'danger' }
})

const progressColor = computed(() => {
  if (measureProgress.value < 50) return '#8b5cf6'
  if (measureProgress.value < 90) return '#6366f1'
  return '#22c55e'
})

// ==================== 工具 ====================
function formatDate(date) {
  if (!date) return '--'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ==================== 设备连接 ====================
function connectDevice() {
  // 模拟连接设备
  ElMessage.info('正在连接设备...')
  setTimeout(() => {
    deviceOnline.value = true
    measureStatus.value = 'idle'
    ElMessage.success('设备连接成功')
  }, 1500)
}

// 轮询设备状态
function pollDeviceStatus() {
  // 实际项目中调用后端接口查询设备状态
  // 这里用模拟数据
}

// ==================== 测量流程 ====================
const phases = [
  { progress: 15, phase: '正在测量体重...', display: '称重中' },
  { progress: 35, phase: '正在检测生物阻抗...', display: '阻抗分析' },
  { progress: 55, phase: '正在计算肌肉量...', display: '分析中' },
  { progress: 75, phase: '正在计算脂肪量...', display: '分析中' },
  { progress: 90, phase: '正在生成报告...', display: '处理中' },
  { progress: 100, phase: '测量完成', display: '完成' }
]

function startMeasure() {
  if (!deviceOnline.value) {
    ElMessage.warning('请先连接设备')
    return
  }
  measureStatus.value = 'measuring'
  measureProgress.value = 0
  let idx = 0

  measureTimer = setInterval(() => {
    if (idx < phases.length) {
      const p = phases[idx]
      measureProgress.value = p.progress
      currentPhase.value = p.phase
      if (idx <= 1) {
        currentDisplay.value = (Math.random() * 20 + 55).toFixed(1)
      } else {
        currentDisplay.value = '...'
      }
      idx++
    } else {
      finishMeasure()
    }
  }, 2500)
}

function stopMeasure() {
  if (measureTimer) {
    clearInterval(measureTimer)
    measureTimer = null
  }
  measureStatus.value = 'idle'
  measureProgress.value = 0
  ElMessage.info('测量已停止')
}

function finishMeasure() {
  if (measureTimer) {
    clearInterval(measureTimer)
    measureTimer = null
  }
  measureStatus.value = 'completed'
  measureProgress.value = 100
  
  // 模拟测量结果
  resultData.value = {
    weight: Number((Math.random() * 15 + 55).toFixed(1)),
    bmi: Number((Math.random() * 5 + 21).toFixed(1)),
    bodyFatRate: Number((Math.random() * 12 + 22).toFixed(1)),
    bodyFatMass: Number((Math.random() * 8 + 15).toFixed(1)),
    asm: Number((Math.random() * 5 + 18).toFixed(2)),
    asmi: Number((Math.random() * 2.5 + 5.8).toFixed(2)),
    totalMuscleMass: Number((Math.random() * 8 + 30).toFixed(1)),
    totalBodyWater: Number((Math.random() * 6 + 32).toFixed(1)),
    leanBodyMass: Number((Math.random() * 8 + 40).toFixed(1)),
    protein: Number((Math.random() * 2 + 7).toFixed(1)),
    inorganicSalt: Number((Math.random() * 0.8 + 2.2).toFixed(1)),
    waist: Number((Math.random() * 14 + 74).toFixed(1)),
    waistHipRatio: Number((Math.random() * 0.18 + 0.82).toFixed(2)),
    armRightMuscle: Number((Math.random() * 0.7 + 1.9).toFixed(2)),
    armLeftMuscle: Number((Math.random() * 0.7 + 1.85).toFixed(2)),
    trunkMuscle: Number((Math.random() * 3 + 17).toFixed(2)),
    legRightMuscle: Number((Math.random() * 1.8 + 5.4).toFixed(2)),
    legLeftMuscle: Number((Math.random() * 1.8 + 5.4).toFixed(2)),
    armRightFat: Number((Math.random() * 0.7 + 1.0).toFixed(2)),
    armLeftFat: Number((Math.random() * 0.7 + 1.0).toFixed(2)),
    trunkFat: Number((Math.random() * 3 + 8.5).toFixed(2)),
    legRightFat: Number((Math.random() * 1.1 + 2.2).toFixed(2)),
    legLeftFat: Number((Math.random() * 1.1 + 2.2).toFixed(2)),
    intracellularWater: Number((Math.random() * 4 + 17).toFixed(1)),
    extracellularWater: Number((Math.random() * 2.5 + 10).toFixed(1)),
    visceralFatArea: Number((Math.random() * 35 + 70).toFixed(1))
  }
  
  ElMessage.success('测量完成')
}

function retryMeasure() {
  resultData.value = null
  measureStatus.value = 'idle'
  measureProgress.value = 0
}

// ==================== 保存 ====================
async function handleSubmit() {
  if (!resultData.value) {
    ElMessage.warning('没有测量数据')
    return
  }
  submitting.value = true
  try {
    const today = new Date()
    const dateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    
    const data = {
      patientId: Number(patientId.value),
      measureDate: dateStr,
      ...resultData.value
    }

    const res = await insertBodyComposition(data)
    if (res && res.code === 200) {
      ElMessage.success('保存成功')
      setTimeout(() => backToCustomize(), 1000)
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 历史记录 ====================
async function loadHistory() {
  if (!patientId.value) return
  try {
    const res = await selectBodyCompositionList({ patientId: patientId.value })
    if (res && res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

// ==================== 路由 ====================
function goBack() {
  if (measureStatus.value === 'measuring') {
    ElMessage.warning('测量中，请先停止')
    return
  }
  let backPath = '/patient/detection/customize'
  if (route.query.from === 'sCustomize') {
    backPath = '/patient/detection/sCustomize'
  } else if (route.query.from === 'sComprehensive') {
    backPath = '/patient/detection/sComprehensive'
  }
  router.push({ path: backPath, query: route.query })
}

function backToCustomize() {
  try {
    if (patientId.value) {
      localStorage.setItem(`sarcopenia_customize_done:${patientId.value}:body_composition`, '1')
    }
  } catch (e) {}
  let backPath = '/patient/detection/customize'
  if (route.query.from === 'sCustomize') {
    backPath = '/patient/detection/sCustomize'
  } else if (route.query.from === 'sComprehensive') {
    backPath = '/patient/detection/sComprehensive'
  }
  router.push({
    path: backPath,
    query: { patientId: patientId.value, patientName: patientName.value }
  })
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadHistory()
  // 实际项目中开启轮询设备状态
  // statusTimer = setInterval(pollDeviceStatus, 5000)
})

onUnmounted(() => {
  if (measureTimer) clearInterval(measureTimer)
  if (statusTimer) clearInterval(statusTimer)
})
</script>

<style scoped lang="scss">
.body-comp-page {
  min-height: 100vh;
  padding: 20px 32px 40px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 50%, #e0e7ff 100%);
  box-sizing: border-box;
}

/* ========== 顶部导航 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.1);
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #6b7280;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;

  &:hover {
    color: #7c3aed;
    background: #f3e8ff;
  }
}

.header-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-title p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

/* 设备状态 */
.device-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f3f4f6;
  border-radius: 20px;
  font-size: 13px;
  color: #6b7280;
  transition: all 0.3s;

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #9ca3af;
  }

  &.online {
    background: #ecfdf5;
    color: #059669;

    .status-dot {
      background: #10b981;
      box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2);
      animation: dotPulse 2s ease-in-out infinite;
    }
  }
}

@keyframes dotPulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2); }
  50% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0.1); }
}

/* ========== 主体布局 ========== */
.page-body {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
  max-width: 1280px;
  margin: 0 auto;
}

/* ========== 左侧主区域 ========== */
.main-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.measure-card {
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 30px rgba(124, 58, 237, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.1);
}

/* 设备区域 */
.device-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.device-visual {
  position: relative;
  width: 260px;
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: flex-end;
}

/* 体脂秤设备 */
.scale-device {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.scale-platform {
  position: relative;
  z-index: 2;
}

.platform-surface {
  width: 200px;
  height: 18px;
  background: linear-gradient(180deg, #a78bfa 0%, #8b5cf6 100%);
  border-radius: 10px 10px 4px 4px;
  position: relative;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.4);
}

.platform-side {
  width: 190px;
  height: 10px;
  background: linear-gradient(180deg, #7c3aed 0%, #6d28d9 100%);
  margin: 0 auto;
  border-radius: 0 0 8px 8px;
}

.foot-print {
  position: absolute;
  top: 3px;
  width: 30px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50% 50% 40% 40%;

  &.left-foot {
    left: 50px;
    transform: rotate(-10deg);
  }
  &.right-foot {
    right: 50px;
    transform: rotate(10deg);
  }
}

.scale-display {
  position: absolute;
  top: -90px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
}

.display-screen {
  width: 110px;
  height: 56px;
  background: linear-gradient(180deg, #1e1b4b 0%, #312e81 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a5b4fc;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  box-shadow: 0 4px 15px rgba(49, 46, 129, 0.5), inset 0 1px 0 rgba(255,255,255,0.1);

  &::before {
    content: '';
    position: absolute;
    bottom: -8px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-top: 8px solid #312e81;
  }
}

.scale-stand {
  position: absolute;
  top: -160px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 150px;
  background: linear-gradient(180deg, #c4b5fd, #a78bfa);
  border-radius: 3px;
  z-index: 1;
}

/* 手柄电极 */
.hand-electrodes {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 -20px;
}

.electrode {
  position: relative;
  width: 28px;
  height: 85px;

  &.left { transform: rotate(-8deg); margin-left: 10px; }
  &.right { transform: rotate(8deg); margin-right: 10px; }
}

.electrode-body {
  width: 28px;
  height: 75px;
  background: linear-gradient(180deg, #a78bfa 0%, #7c3aed 100%);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
  position: relative;

  &::before, &::after {
    content: '';
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    width: 16px;
    height: 5px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 3px;
  }
  &::before { top: 18px; }
  &::after { bottom: 18px; }
}

.electrode-wire {
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  width: 3px;
  height: 30px;
  background: linear-gradient(180deg, #7c3aed, #c4b5fd);
  border-radius: 2px;
}

/* 扫描光效 */
.scan-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 280px;
  height: 280px;
  pointer-events: none;
  overflow: hidden;
  border-radius: 50%;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #8b5cf6, transparent);
  box-shadow: 0 0 15px #8b5cf6;
  animation: scanMove 2s ease-in-out infinite;
}

@keyframes scanMove {
  0% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* 状态文字 */
.measure-status-text {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: #f5f3ff;
  border-radius: 24px;
  font-size: 14px;
  color: #4b5563;

  .el-icon {
    font-size: 18px;
    &.offline { color: #f59e0b; }
    &.ready { color: #8b5cf6; }
    &.measuring {
      color: #6366f1;
      animation: spin 1s linear infinite;
    }
    &.success { color: #10b981; }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 操作区 */
.action-area {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-connect,
.btn-start,
.btn-save {
  min-width: 180px;
  height: 52px;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  border-radius: 26px;
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);

  &:hover {
    box-shadow: 0 8px 28px rgba(139, 92, 246, 0.5);
    transform: translateY(-2px);
  }
}

.btn-stop {
  min-width: 160px;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 26px;
}

.btn-retry {
  min-width: 140px;
  height: 52px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 26px;
}

/* ========== 右侧面板 ========== */
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 20px;
}

/* 结果卡片 */
.result-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.08);
  border: 1px solid rgba(139, 92, 246, 0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 12px;
}

.result-date {
  font-size: 12px;
  color: #9ca3af;
}

/* ASMI 高亮 */
.asmi-highlight {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.asmi-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.asmi-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 10px;
}

.asmi-num {
  font-size: 42px;
  font-weight: 800;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'SF Mono', Menlo, monospace;
}

.asmi-unit {
  font-size: 14px;
  color: #6b7280;
}

.asmi-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;

  &.good {
    background: #dcfce7;
    color: #15803d;
  }
  &.warn {
    background: #fef3c7;
    color: #b45309;
  }
  &.danger {
    background: #fee2e2;
    color: #b91c1c;
  }
}

/* 指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
}

.metric-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  flex-shrink: 0;

  &.blue { background: linear-gradient(135deg, #60a5fa, #3b82f6); }
  &.green { background: linear-gradient(135deg, #34d399, #10b981); }
  &.orange { background: linear-gradient(135deg, #fb923c, #f97316); }
  &.purple { background: linear-gradient(135deg, #a78bfa, #8b5cf6); }
  &.cyan { background: linear-gradient(135deg, #22d3ee, #06b6d4); }
  &.pink { background: linear-gradient(135deg, #f472b6, #ec4899); }
}

.metric-name {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 2px;
}

.metric-val {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  font-family: 'SF Mono', Menlo, monospace;

  em {
    font-style: normal;
    font-weight: 400;
    color: #9ca3af;
    font-size: 11px;
  }
}

/* 进度卡片 */
.progress-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.08);
  border: 1px solid rgba(139, 92, 246, 0.1);
}

.progress-ring-wrap {
  margin-bottom: 16px;
}

.progress-phase {
  font-size: 15px;
  font-weight: 600;
  color: #4f46e5;
  margin-bottom: 6px;
}

.progress-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 20px;
}

.progress-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;

  .step-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #e5e7eb;
    border: 2px solid #fff;
    box-shadow: 0 0 0 2px #e5e7eb;
  }

  .step-text {
    font-size: 11px;
    color: #9ca3af;
  }

  &.active .step-dot {
    background: #8b5cf6;
    box-shadow: 0 0 0 2px #8b5cf6;
  }
  &.active .step-text {
    color: #7c3aed;
    font-weight: 600;
  }

  &.done .step-dot {
    background: #10b981;
    box-shadow: 0 0 0 2px #10b981;
  }
  &.done .step-text {
    color: #059669;
  }
}

.step-line {
  width: 30px;
  height: 2px;
  background: #e5e7eb;
  margin-bottom: 22px;
  border-radius: 1px;

  &.active {
    background: #8b5cf6;
  }
}

/* 占位卡片 */
.placeholder-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 24px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.08);
  border: 1px solid rgba(139, 92, 246, 0.1);
}

.ph-icon {
  color: #c4b5fd;
  margin-bottom: 16px;
}

.ph-title {
  font-size: 17px;
  font-weight: 700;
  color: #4b5563;
  margin-bottom: 8px;
}

.ph-desc {
  font-size: 13px;
  color: #9ca3af;
  line-height: 1.6;
}

/* 历史记录 */
.history-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.06);
}

.hc-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.hc-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
  color: #374151;

  .el-icon { color: #8b5cf6; }
}

.hc-count {
  font-size: 12px;
  color: #9ca3af;
}

.hc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hc-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 8px;
  border-left: 3px solid #8b5cf6;
}

.hc-date {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.hc-data {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.hc-badge {
  font-size: 12px;
  font-weight: 700;
  color: #7c3aed;
  font-family: 'SF Mono', Menlo, monospace;
}

.hc-meta {
  font-size: 11px;
  color: #9ca3af;
}

/* 响应式 */
@media (max-width: 960px) {
  .page-body {
    grid-template-columns: 1fr;
  }
  .side-panel { position: static; }
}

@media (max-width: 600px) {
  .body-comp-page { padding: 12px; }
  .measure-card { padding: 20px; }
  .metrics-grid { grid-template-columns: 1fr; }
  .device-visual { transform: scale(0.85); }
}
</style>
