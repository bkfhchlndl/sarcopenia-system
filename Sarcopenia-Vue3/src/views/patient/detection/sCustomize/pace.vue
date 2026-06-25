<template>
  <div class="pace-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>6米步速测量</h1>
          <p>6-Meter Gait Speed Test · 肌少症评估</p>
        </div>
      </div>
      <div class="top-right">
        <div class="patient-badge">
          <span class="badge-avatar">{{ (patientName || '患').slice(0, 1) }}</span>
          <div>
            <div class="badge-name">{{ patientName }}</div>
            <div class="badge-id">{{ patientId ? 'ID: ' + patientId : '-' }}</div>
          </div>
        </div>
      </div>
    </header>

    <div class="main-content">
      <!-- 左侧：测量操作区 -->
      <div class="left-panel">
        <div class="test-card">
          <!-- 当前测量次数提示 -->
          <div class="test-round">
            <span class="round-label">当前测量</span>
            <span class="round-num">第 {{ currentRound }} 次</span>
            <span class="round-total">/ 共 2 次</span>
          </div>

          <!-- 计时大显示 -->
          <div class="timer-display">
            <div class="timer-value">
              <span class="tv-num">{{ formatTime(currentTime) }}</span>
              <span class="tv-unit">秒</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <template v-if="!isRunning && !roundFinished">
              <el-button
                  type="primary"
                  size="large"
                  class="btn-start"
                  @click="startTimer"
              >
                <el-icon><VideoPlay /></el-icon>
                开始计时
              </el-button>
            </template>
            <template v-if="isRunning">
              <el-button
                  type="success"
                  size="large"
                  class="btn-finish"
                  @click="stopTimer"
              >
                <el-icon><Check /></el-icon>
                完成计时
              </el-button>
              <el-button
                  type="danger"
                  size="large"
                  class="btn-cancel"
                  plain
                  @click="cancelTimer"
              >
                <el-icon><Close /></el-icon>
                取消本次
              </el-button>
            </template>
            <template v-if="roundFinished && !allFinished">
              <el-button
                  type="primary"
                  size="large"
                  class="btn-next"
                  @click="nextRound"
              >
                <el-icon><Right /></el-icon>
                进行第 {{ currentRound + 1 }} 次
              </el-button>
              <el-button
                  size="large"
                  class="btn-skip"
                  @click="skipSecond"
              >
                <el-icon><DArrowRight /></el-icon>
                跳过，直接提交
              </el-button>
            </template>
            <template v-if="allFinished">
              <el-button
                  type="primary"
                  size="large"
                  class="btn-restart"
                  @click="restartAll"
              >
                <el-icon><RefreshRight /></el-icon>
                重新测量
              </el-button>
              <el-button
                  size="large"
                  class="btn-reset"
                  @click="resetAll"
              >
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </template>
          </div>
        </div>

        <!-- 两次测量结果卡片 -->
        <div class="measure-cards">
          <!-- 第1次 -->
          <div class="measure-card" :class="{ 'has-value': firstValue != null }">
            <div class="mc-head">
              <span class="mc-index">01</span>
              <span class="mc-label">第 1 次测量</span>
              <el-tag v-if="firstValue != null" size="small" type="success" effect="light">
                已完成
              </el-tag>
            </div>
            <div class="mc-body">
              <div class="mc-value-display">
                <span class="mc-num">{{ firstValue != null ? firstValue.toFixed(2) : '--' }}</span>
                <span class="mc-unit">s</span>
              </div>
              <div class="mc-speed">
                步速: <strong>{{ calcSpeed(firstValue) }}</strong> m/s
              </div>
            </div>
          </div>

          <!-- 第2次 -->
          <div class="measure-card" :class="{ 'has-value': secondValue != null }">
            <div class="mc-head">
              <span class="mc-index">02</span>
              <span class="mc-label">第 2 次测量</span>
              <el-tag v-if="secondValue != null" size="small" type="success" effect="light">
                已完成
              </el-tag>
              <el-tag v-else size="small" type="info" effect="plain">选填</el-tag>
            </div>
            <div class="mc-body">
              <div class="mc-value-display">
                <span class="mc-num">{{ secondValue != null ? secondValue.toFixed(2) : '--' }}</span>
                <span class="mc-unit">s</span>
              </div>
              <div class="mc-speed">
                步速: <strong>{{ calcSpeed(secondValue) }}</strong> m/s
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：数据面板 -->
      <div class="right-panel">
        <!-- 平均时长 + 步速 -->
        <div class="result-card" v-if="avgValue != null">
          <div class="result-row">
            <div class="result-item">
              <div class="result-label">平均时长</div>
              <div class="result-value">
                <span>{{ avgValue.toFixed(2) }}</span>
                <span class="result-unit">s</span>
              </div>
            </div>
            <div class="result-divider"></div>
            <div class="result-item">
              <div class="result-label">平均步速</div>
              <div class="result-value speed">
                <span>{{ calcSpeed(avgValue) }}</span>
                <span class="result-unit">m/s</span>
              </div>
            </div>
          </div>
          <div class="risk-level" :class="'risk-' + riskLevel.type">
            <el-icon><WarningFilled /></el-icon>
            {{ riskLevel.text }}
          </div>
          <div class="risk-desc">{{ riskLevel.desc }}</div>
          <!-- SPPB 评分 -->
          <div class="sppb-score">
            <div class="sppb-label">SPPB 评分</div>
            <div class="sppb-value">
              <span class="sppb-num">{{ sppbScore }}</span>
              <span class="sppb-total">/ 4 分</span>
            </div>
          </div>
        </div>

        <!-- 未测量时的占位 -->
        <div class="result-card placeholder" v-else>
          <div class="placeholder-icon">
            <el-icon :size="48"><Stopwatch /></el-icon>
          </div>
          <div class="placeholder-text">请完成测量后查看结果</div>
        </div>

        <!-- 备注 -->
        <div class="remark-card">
          <div class="rc-title">
            <el-icon><EditPen /></el-icon>
            备注说明
          </div>
          <el-input
              v-model="remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注说明（选填）"
              maxlength="500"
              show-word-limit
          />
        </div>

        <!-- 提交按钮 -->
        <div class="submit-area">
          <el-button size="default" @click="goBack">取消</el-button>
          <el-button
              type="primary"
              size="default"
              :loading="submitting"
              :disabled="firstValue == null && secondValue == null"
              @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            提交记录
          </el-button>
        </div>

        <!-- 临床参考 -->
        <div class="reference-card">
          <div class="ref-title">
            <el-icon><InfoFilled /></el-icon>
            临床参考标准
          </div>
          <div class="ref-list">
            <div class="ref-item">
              <span class="ref-dot normal"></span>
              <span class="ref-label">正常步速</span>
              <span class="ref-value">≥ 1.0 m/s</span>
            </div>
            <div class="ref-item">
              <span class="ref-dot warning"></span>
              <span class="ref-label">步速下降</span>
              <span class="ref-value">0.8 ~ 1.0 m/s</span>
            </div>
            <div class="ref-item">
              <span class="ref-dot danger"></span>
              <span class="ref-label">肌少症风险</span>
              <span class="ref-value">＜ 0.8 m/s</span>
            </div>
          </div>
          <div class="ref-tip">
            6米步速测试是评估躯体功能的重要指标，步速低于0.8m/s提示肌少症可能。取两次测量的平均值作为最终结果。
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="SPace">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Check, Clock, Close, EditPen, Document,
  VideoPlay, RefreshRight, RefreshLeft,
  WarningFilled, InfoFilled, Right, DArrowRight, Stopwatch
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectPaceList, insertPace } from '@/api/s.js'

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
const submitting = ref(false)
const loading = ref(false)
const historyList = ref([])

/** 计时相关 */
const currentTime = ref(0)
const isRunning = ref(false)
let timerInterval = null

/** 两次测量值 */
const firstValue = ref(null)
const secondValue = ref(null)

/** 当前测量轮次 1 或 2 */
const currentRound = ref(1)
/** 当前轮是否完成 */
const roundFinished = ref(false)

/** 备注 */
const remark = ref('')

// ==================== 计算属性 - 路由参数 ====================
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

/** 是否全部完成（至少第一次完成了） */
const allFinished = computed(() => {
  // 第一次完成了，并且第二次要么完成了要么跳过了
  return firstValue.value != null && roundFinished.value && currentRound.value >= 2
})

/** 平均时长 */
const avgValue = computed(() => {
  const values = []
  if (firstValue.value != null) values.push(firstValue.value)
  if (secondValue.value != null) values.push(secondValue.value)
  if (values.length === 0) return null
  const avg = values.reduce((a, b) => a + b, 0) / values.length
  return Math.round(avg * 100) / 100
})

/**
 * 风险等级（基于步速 m/s）
 * 正常 ≥1.0，下降 0.8~1.0，风险 <0.8
 */
const riskLevel = computed(() => {
  if (avgValue.value == null || avgValue.value <= 0) {
    return { text: '待评估', type: 'info', desc: '请完成测量后查看步速评估' }
  }
  const speed = 6 / avgValue.value
  if (speed < 0.8) {
    return {
      text: '步速低下',
      type: 'danger',
      desc: '步速低于正常范围，提示躯体功能下降，建议进一步评估肌力和肌量'
    }
  }
  if (speed < 1.0) {
    return {
      text: '步速下降',
      type: 'warning',
      desc: '步速处于临界范围，建议加强运动锻炼，定期监测步态和平衡功能'
    }
  }
  return {
    text: '步速正常',
    type: 'success',
    desc: '步速在正常范围内，躯体功能良好，继续保持健康生活方式'
  }
})

/**
 * SPPB 6米步行评分
 * 0分: 不能完成
 * 1分: >13.05s
 * 2分: 9.32~13.05s
 * 3分: 7.23~9.32s
 * 4分: <7.23s
 */
const sppbScore = computed(() => {
  if (avgValue.value == null || avgValue.value <= 0) return 0
  const val = Number(avgValue.value)
  if (val > 13.05) return 1
  if (val >= 9.32) return 2
  if (val >= 7.23) return 3
  return 4
})

// ==================== 工具函数 ====================
/**
 * 格式化时间显示（保留两位小数）
 */
function formatTime(seconds) {
  return Number(seconds).toFixed(2)
}

/**
 * 计算步速（6米距离）
 */
function calcSpeed(seconds) {
  if (seconds == null || seconds <= 0) return '--'
  return (6 / Number(seconds)).toFixed(2)
}

/**
 * 格式化日期时间
 */
function formatDateTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return String(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ==================== 计时相关方法 ====================
/**
 * 开始计时
 */
function startTimer() {
  currentTime.value = 0
  isRunning.value = true
  roundFinished.value = false

  const startTime = Date.now()
  timerInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    currentTime.value = Math.round(elapsed * 100) / 100
  }, 10)
}

/**
 * 停止计时（完成当前轮）
 */
function stopTimer() {
  clearInterval(timerInterval)
  timerInterval = null
  isRunning.value = false
  roundFinished.value = true

  if (currentRound.value === 1) {
    firstValue.value = currentTime.value
  } else {
    secondValue.value = currentTime.value
  }
  ElMessage.success(`第 ${currentRound.value} 次测量完成`)
}

/**
 * 取消本次计时
 */
function cancelTimer() {
  clearInterval(timerInterval)
  timerInterval = null
  isRunning.value = false
  currentTime.value = 0
  roundFinished.value = false
  ElMessage.info('已取消本次计时')
}

/**
 * 下一轮
 */
function nextRound() {
  currentRound.value++
  currentTime.value = 0
  roundFinished.value = false
}

/**
 * 跳过第二次，直接提交
 */
function skipSecond() {
  secondValue.value = null
  currentRound.value = 2
  roundFinished.value = true
  ElMessage.info('已跳过第二次测量')
}

/**
 * 重新全部测量
 */
function restartAll() {
  firstValue.value = null
  secondValue.value = null
  currentRound.value = 1
  currentTime.value = 0
  roundFinished.value = false
  isRunning.value = false
  remark.value = ''
}

/**
 * 重置
 */
function resetAll() {
  firstValue.value = null
  secondValue.value = null
  currentRound.value = 1
  currentTime.value = 0
  roundFinished.value = false
  isRunning.value = false
  remark.value = ''
}

// ==================== 业务方法 ====================
/**
 * 加载历史测量记录
 */
async function loadHistoryList() {
  if (!patientId.value) return
  loading.value = true
  try {
    const res = await selectPaceList({ patientId: patientId.value })
    if (res && res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (error) {
    console.error('加载6米步速测量记录失败：', error)
  } finally {
    loading.value = false
  }
}

/**
 * 提交测量记录
 */
async function handleSubmit() {
  if (firstValue.value == null && secondValue.value == null) {
    ElMessage.warning('请至少完成 1 次测量')
    return
  }

  submitting.value = true
  try {
    const submitData = {
      patientId: Number(patientId.value),
      firstValue: firstValue.value,
      secondValue: secondValue.value,
      remark: remark.value || null
    }

    const res = await insertPace(submitData)
    if (res && res.code === 200) {
      ElMessage.success('测量记录提交成功')
      // 刷新列表
      await loadHistoryList()
      // 重置
      resetAll()
      // 返回
      setTimeout(() => {
        backToCustomize()
      }, 1200)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交6米步速测量失败：', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 返回上一页
 */
function goBack() {
  const backPath = route.query.from === 'sCustomize'
    ? '/patient/detection/sCustomize'
    : '/patient/detection/customize'
  router.push({ path: backPath, query: route.query })
}

/**
 * 返回自定义评估页，并写入本地完成标记
 */
function backToCustomize() {
  try {
    if (patientId.value) {
      localStorage.setItem(`sarcopenia_customize_done:${patientId.value}:pace`, '1')
    }
  } catch (e) {
    console.warn('本地存储写入失败：', e)
  }
  const backPath = route.query.from === 'sCustomize'
    ? '/patient/detection/sCustomize'
    : '/patient/detection/customize'
  router.push({
    path: backPath,
    query: {
      patientId: patientId.value,
      patientName: patientName.value
    }
  })
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadHistoryList()
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
})
</script>

<style scoped lang="scss">
.pace-container {
  min-height: 100vh;
  padding: 20px 28px 40px;
  background: #ecfdf5;
  box-sizing: border-box;
  max-width: 1400px;
  margin: 0 auto;
}

/* 顶部导航 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.06);
  margin-bottom: 20px;
  border-left: 4px solid #10b981;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: #606266;
  padding: 6px 12px;
  border-radius: 6px;

  &:hover {
    color: #10b981;
    background: #d1fae5;
  }
}

.title-block h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
}

.title-block p {
  margin: 3px 0 0;
  font-size: 12px;
  color: #909399;
  letter-spacing: 0.5px;
}

.patient-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: #d1fae5;
  border-radius: 8px;
  border: 1px solid #a7f3d0;
}

.badge-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}

.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.badge-id {
  font-size: 11.5px;
  color: #909399;
  margin-top: 2px;
}

/* 主内容区：左右两栏 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  align-items: start;
}

/* 左侧面板 */
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 测试卡片 */
.test-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 32px 28px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.06);
  border: 1px solid #d1fae5;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

/* 当前轮次提示 */
.test-round {
  display: flex;
  align-items: baseline;
  gap: 6px;

  .round-label {
    font-size: 14px;
    color: #6b7280;
  }

  .round-num {
    font-size: 24px;
    font-weight: 800;
    color: #10b981;
  }

  .round-total {
    font-size: 14px;
    color: #9ca3af;
  }
}

/* 计时大显示 */
.timer-display {
  display: flex;
  justify-content: center;
}

.timer-value {
  background: #ecfdf5;
  border: 2px dashed #6ee7b7;
  border-radius: 12px;
  padding: 24px 60px;
  text-align: center;
  min-width: 280px;
}

.tv-num {
  font-size: 64px;
  font-weight: 800;
  color: #10b981;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  line-height: 1;
  display: block;
}

.tv-unit {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
  font-weight: 500;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-start {
  min-width: 180px;
  height: 48px;
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
  }
}

.btn-finish {
  min-width: 160px;
  height: 48px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  }
}

.btn-cancel {
  min-width: 140px;
  height: 48px;
  border-radius: 24px;
  font-weight: 600;
}

.btn-next {
  min-width: 180px;
  height: 48px;
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
  }
}

.btn-skip {
  min-width: 160px;
  height: 48px;
  border-radius: 24px;
  font-weight: 600;
}

.btn-restart {
  min-width: 140px;
  height: 44px;
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  font-weight: 600;
  border-radius: 22px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25);
}

.btn-reset {
  min-width: 120px;
  height: 44px;
  border-radius: 22px;
}

/* 测量卡片组 */
.measure-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.measure-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.06);
  border: 2px solid transparent;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 14px;

  &:hover {
    box-shadow: 0 4px 16px rgba(16, 185, 129, 0.12);
  }

  &.has-value {
    border-color: #6ee7b7;
    background: linear-gradient(180deg, #ecfdf5 0%, #fff 100%);
  }
}

.mc-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mc-index {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #d1fae5;
  color: #10b981;
  font-size: 14px;
  font-weight: 700;
  font-family: 'SF Mono', Menlo, monospace;
}

.mc-label {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.mc-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mc-value-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  padding: 16px 0;
  background: #f0fdf4;
  border-radius: 10px;
  border: 1px dashed #6ee7b7;
}

.mc-num {
  font-size: 40px;
  font-weight: 800;
  color: #10b981;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  line-height: 1;
  min-width: 80px;
  text-align: center;
}

.measure-card:not(.has-value) .mc-num {
  color: #c0c4cc;
}

.mc-unit {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.mc-speed {
  text-align: center;
  font-size: 13px;
  color: #6b7280;

  strong {
    color: #10b981;
    font-weight: 700;
    font-size: 15px;
  }
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 20px;
}

/* 结果卡片 */
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.06);
  border: 1px solid #d1fae5;

  &.placeholder {
    text-align: center;
    padding: 40px 20px;
    color: #9ca3af;
  }
}

.placeholder-icon {
  color: #d1d5db;
  margin-bottom: 12px;
}

.placeholder-text {
  font-size: 14px;
  color: #9ca3af;
}

.result-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.result-item {
  flex: 1;
  text-align: center;
}

.result-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.result-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;

  span {
    font-size: 32px;
    font-weight: 800;
    color: #1f2937;
    font-family: 'SF Mono', Menlo, monospace;
  }

  .result-unit {
    font-size: 14px;
    color: #909399;
    font-weight: 500;
  }

  &.speed span {
    color: #10b981;
  }
}

.result-divider {
  width: 1px;
  height: 60px;
  background: #e5e7eb;
}

.risk-level {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  &.risk-success { color: #67c23a; }
  &.risk-warning { color: #e6a23c; }
  &.risk-danger { color: #f56c6c; }
  &.risk-info { color: #909399; }
}

.risk-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  text-align: center;
}

.sppb-score {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #e5e7eb;
  text-align: center;
}

.sppb-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.sppb-value {
  .sppb-num {
    font-size: 32px;
    font-weight: 800;
    color: #10b981;
    font-family: 'SF Mono', Menlo, monospace;
  }
  .sppb-total {
    font-size: 13px;
    color: #909399;
    margin-left: 2px;
  }
}

/* 备注卡片 */
.remark-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.04);
}

.rc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

/* 提交区域 */
.submit-area {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 4px 2px;
}

.submit-area .el-button--primary {
  min-width: 120px;
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.25);

  &:hover {
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.35);
  }
}

/* 历史记录卡片 */
.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.04);
  max-height: 340px;
  display: flex;
  flex-direction: column;
  border: 1px solid #d1fae5;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.hh-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 2px;
  }
}

.history-item {
  padding: 12px 14px;
  background: #ecfdf5;
  border-radius: 10px;
  border: 1px solid #d1fae5;
  transition: all 0.2s ease;

  &:hover {
    border-color: #6ee7b7;
    background: #d1fae5;
  }
}

.hi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.hi-index {
  font-size: 13px;
  font-weight: 700;
  color: #10b981;
}

.hi-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.hi-values {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 6px;
}

.hv-item {
  text-align: center;
  padding: 6px 4px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  opacity: 0.5;

  &.active {
    opacity: 1;
    border-color: #a7f3d0;
  }

  &.avg {
    opacity: 1;
    background: #d1fae5;
    border-color: #6ee7b7;
  }
}

.hv-label {
  display: block;
  font-size: 10px;
  color: #909399;
  margin-bottom: 2px;
}

.hv-num {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  font-family: 'SF Mono', Menlo, monospace;
}

.hv-item.avg .hv-num {
  color: #10b981;
}

.hv-unit {
  font-size: 9px;
  color: #909399;
  margin-left: 1px;
}

.hi-speed {
  font-size: 12px;
  color: #4b5563;
  text-align: center;
  padding: 4px 0;
  border-top: 1px dashed #d1fae5;

  strong {
    color: #10b981;
    font-weight: 700;
  }
}

.hi-remark {
  font-size: 12px;
  color: #606266;
  padding-top: 6px;
  border-top: 1px dashed #d1fae5;
  margin-top: 4px;

  span {
    color: #909399;
  }
}

/* 参考标准卡片 */
.reference-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.04);
  border: 1px solid #d1fae5;
}

.ref-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 14px;
}

.ref-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.ref-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ref-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;

  &.normal { background: #67c23a; }
  &.warning { background: #e6a23c; }
  &.danger { background: #f56c6c; }
}

.ref-label {
  flex: 1;
  font-size: 13px;
  color: #606266;
}

.ref-value {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  font-family: 'SF Mono', Menlo, monospace;
}

.ref-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .right-panel {
    position: static;
  }

  .history-card {
    max-height: none;
  }
}

@media (max-width: 768px) {
  .pace-container {
    padding: 14px;
  }

  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 16px;
  }

  .measure-cards {
    grid-template-columns: 1fr;
  }

  .timer-value {
    padding: 20px 40px;
    min-width: 220px;
  }

  .tv-num {
    font-size: 48px;
  }
}
</style>
