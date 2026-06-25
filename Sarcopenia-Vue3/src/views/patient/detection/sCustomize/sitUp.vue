<template>
  <div class="situp-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>5次起坐测试</h1>
          <p>5 Times Sit-to-Stand Test · 肌少症评估</p>
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
      <!-- 左侧：测试区域 -->
      <div class="left-panel">
        <div class="test-card">
          <!-- 计时大显示 -->
          <div class="timer-display">
            <div class="timer-value">
              <span class="tv-num">{{ formatTime(currentTime) }}</span>
              <span class="tv-unit">秒</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button
                v-if="!isRunning && !isFinished"
                type="primary"
                size="large"
                class="btn-start"
                @click="startTest"
            >
              <el-icon><VideoPlay /></el-icon>
              开始测试
            </el-button>
            <template v-if="isRunning">
              <el-button
                  type="success"
                  size="large"
                  class="btn-finish"
                  @click="finishTest"
              >
                <el-icon><Check /></el-icon>
                完成测试
              </el-button>
              <el-button
                  type="danger"
                  size="large"
                  class="btn-stop"
                  plain
                  @click="stopTest"
              >
                <el-icon><VideoPause /></el-icon>
                终止本项目测试
              </el-button>
            </template>
            <template v-if="isFinished">
              <el-button
                  type="primary"
                  size="large"
                  class="btn-restart"
                  @click="restartTest"
              >
                <el-icon><RefreshRight /></el-icon>
                重新测试
              </el-button>
              <el-button
                  size="large"
                  class="btn-reset"
                  @click="resetTest"
              >
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </template>
          </div>

          <!-- 动作示意图 -->
          <div class="illustration">
            <img src="/img_1.png" alt="5次起坐动作示意图" class="illustration-img" />
          </div>
        </div>
      </div>

      <!-- 右侧：数据面板 -->
      <div class="right-panel">
        <!-- 计时时间 -->
        <div class="info-card">
          <div class="info-icon timer-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="info-label">计时时间</div>
          <div class="info-value">
            <span>{{ formatTime(currentTime) }}</span>
            <span class="info-unit">s</span>
          </div>
        </div>

        <!-- 测试成绩 -->
        <div class="result-card">
          <div class="result-title">
            <span class="title-bar"></span>
            测试成绩
          </div>
          <div class="result-value">
            <span>{{ testResult !== null ? testResult.toFixed(2) : '0.00' }}</span>
            <span class="result-unit">s</span>
          </div>
          <div v-if="testResult !== null" class="result-status" :class="'status-' + resultLevel.type">
            {{ resultLevel.text }}
          </div>
          <!-- SPPB 评分 -->
          <div class="sppb-score">
            <div class="sppb-label">SPPB 评分</div>
            <div class="sppb-value">
              <span class="sppb-num">{{ sppbScore }}</span>
              <span class="sppb-total">/ 4 分</span>
            </div>
          </div>
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
              :disabled="testResult === null"
              @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            提交记录
          </el-button>
        </div>

        <!-- 历史记录 -->
        <div class="history-card">
          <div class="history-header">
            <div class="hh-title">
              <el-icon><Document /></el-icon>
              历史记录
            </div>
            <el-tag size="small" type="info" effect="plain">共 {{ historyList.length }} 条</el-tag>
          </div>

          <el-empty v-if="!loading && historyList.length === 0" description="暂无测量记录" :image-size="60" />

          <div v-else class="history-list">
            <div
                v-for="(record, idx) in historyList"
                :key="record.id"
                class="history-item"
            >
              <div class="hi-head">
                <span class="hi-index">第 {{ historyList.length - idx }} 次</span>
                <span class="hi-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatDateTime(record.createTime) }}
                </span>
              </div>
              <div class="hi-body">
                <div class="hi-value">
                  <span class="hv-num">{{ record.firstValue != null ? Number(record.firstValue).toFixed(2) : '--' }}</span>
                  <span class="hv-unit">s</span>
                </div>
                <el-tag
                    v-if="record.isComplete === '0'"
                    size="small"
                    type="success"
                    effect="light"
                >
                  已完成
                </el-tag>
                <el-tag
                    v-else
                    size="small"
                    type="warning"
                    effect="light"
                >
                  未完成
                </el-tag>
              </div>
              <div v-if="record.remark" class="hi-remark">
                <span>备注：</span>{{ record.remark }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="SitUp">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Check, Clock, EditPen, Document,
  VideoPlay, VideoPause, RefreshRight, RefreshLeft,
  Timer, Histogram
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectSitUpList, insertSitUp } from '@/api/s.js'

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
const submitting = ref(false)
const loading = ref(false)
const historyList = ref([])

/** 计时相关 */
const currentTime = ref(0)       // 当前计时（秒，两位小数）
const isRunning = ref(false)     // 是否正在计时
const isFinished = ref(false)    // 是否已结束测试（完成或终止都算）
const isStopped = ref(false)     // 是否是被终止的
const testResult = ref(null)     // 测试结果（完成时的时间）
let timerInterval = null

/** 备注 */
const remark = ref('')

// ==================== 计算属性 - 路由参数 ====================
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

/** 历史次数 */
const historyCount = computed(() => historyList.value.length)

/**
 * SPPB 5次起坐评分
 * 0分: 不能完成或>60s
 * 1分: 16.70~60s
 * 2分: 13.70~16.69s
 * 3分: 11.20~13.69s
 * 4分: <11.19s
 */
const sppbScore = computed(() => {
  if (testResult.value == null) return 0
  if (isStopped.value) return 0
  const val = Number(testResult.value)
  if (val > 60) return 0
  if (val >= 16.70) return 1
  if (val >= 13.70) return 2
  if (val >= 11.20) return 3
  return 4
})

/**
 * 结果等级评估
 * 5次起坐测试：<12秒 正常；12~15秒 临界；>15秒 肌力下降风险
 */
const resultLevel = computed(() => {
  if (testResult.value == null) {
    return { text: '待测试', type: 'info' }
  }
  const val = Number(testResult.value)
  if (val <= 12) {
    return { text: '正常', type: 'success' }
  }
  if (val <= 15) {
    return { text: '临界范围', type: 'warning' }
  }
  return { text: '肌力下降风险', type: 'danger' }
})

// ==================== 工具函数 ====================
/**
 * 格式化时间显示（保留两位小数）
 */
function formatTime(seconds) {
  return Number(seconds).toFixed(2)
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

// ==================== 测试相关方法 ====================
/**
 * 开始测试
 */
function startTest() {
  currentTime.value = 0
  isRunning.value = true
  isFinished.value = false
  isStopped.value = false
  testResult.value = null

  const startTime = Date.now()
  timerInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    currentTime.value = Math.round(elapsed * 100) / 100
  }, 10)
}

/**
 * 完成测试（正常完成）
 */
function finishTest() {
  clearInterval(timerInterval)
  timerInterval = null
  isRunning.value = false
  isFinished.value = true
  isStopped.value = false
  testResult.value = currentTime.value
  ElMessage.success('测试完成')
}

/**
 * 终止测试（结束计时，记录为未完成）
 */
function stopTest() {
  clearInterval(timerInterval)
  timerInterval = null
  isRunning.value = false
  isFinished.value = true
  isStopped.value = true
  testResult.value = currentTime.value
  ElMessage.info('测试已终止')
}

/**
 * 重新测试
 */
function restartTest() {
  startTest()
}

/**
 * 重置测试
 */
function resetTest() {
  clearInterval(timerInterval)
  timerInterval = null
  currentTime.value = 0
  isRunning.value = false
  isFinished.value = false
  isStopped.value = false
  testResult.value = null
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
    const res = await selectSitUpList({ patientId: patientId.value })
    if (res && res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (error) {
    console.error('加载5次起坐测量记录失败：', error)
  } finally {
    loading.value = false
  }
}

/**
 * 提交测量记录
 */
async function handleSubmit() {
  if (testResult.value == null) {
    ElMessage.warning('请先完成测试')
    return
  }

  submitting.value = true
  try {
    const submitData = {
      patientId: Number(patientId.value),
      firstValue: testResult.value,
      remark: remark.value || null,
      isComplete: isStopped.value ? '1' : '0'
    }

    const res = await insertSitUp(submitData)
    if (res && res.code === 200) {
      ElMessage.success('测量记录提交成功')
      // 刷新列表
      await loadHistoryList()
      // 重置
      resetTest()
      // 返回
      setTimeout(() => {
        backToCustomize()
      }, 1200)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交5次起坐测量失败：', error)
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
      localStorage.setItem(`sarcopenia_customize_done:${patientId.value}:sit_up`, '1')
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
.situp-container {
  min-height: 100vh;
  padding: 20px 28px 40px;
  background: #f5f0ff;
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
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.06);
  margin-bottom: 20px;
  border-left: 4px solid #8b5cf6;
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
    color: #8b5cf6;
    background: #f3e8ff;
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
  background: #f3e8ff;
  border-radius: 8px;
  border: 1px solid #ddd6fe;
}

.badge-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
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
  padding: 40px 32px 32px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.06);
  border: 1px solid #ede9fe;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
}

/* 计时大显示 */
.timer-display {
  display: flex;
  justify-content: center;
}

.timer-value {
  background: #faf5ff;
  border: 2px dashed #c4b5fd;
  border-radius: 12px;
  padding: 24px 60px;
  text-align: center;
  min-width: 240px;
}

.tv-num {
  font-size: 64px;
  font-weight: 800;
  color: #8b5cf6;
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
  min-width: 160px;
  height: 48px;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4);
  }
}

.btn-finish {
  min-width: 160px;
  height: 48px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(34, 197, 94, 0.4);
  }
}

.btn-stop {
  min-width: 180px;
  height: 48px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
  }
}

.btn-restart {
  min-width: 140px;
  height: 44px;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  border: none;
  font-weight: 600;
  border-radius: 22px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.25);
}

.btn-reset {
  min-width: 120px;
  height: 44px;
  border-radius: 22px;
}

/* 动作示意图 */
.illustration {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  position: relative;
}

.illustration-img {
  max-width: 100%;
  max-height: 280px;
  object-fit: contain;
  /* 提高渲染清晰度 */
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
}

/* 右下角水印遮罩 */
.illustration::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 120px;
  height: 40px;
  background: linear-gradient(135deg, transparent 30%, #fff 60%);
  pointer-events: none;
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 20px;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.04);
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f3e8ff;
}

.info-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 20px;
  flex-shrink: 0;

  &.timer-icon {
    background: #fef3c7;
    color: #f59e0b;
  }

  &.count-icon {
    background: #cffafe;
    color: #06b6d4;
  }
}

.info-label {
  flex: 1;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.info-value {
  text-align: right;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  font-family: 'SF Mono', Menlo, monospace;

  .info-unit {
    font-size: 13px;
    font-weight: 500;
    color: #909399;
    margin-left: 2px;
  }
}

/* 测试成绩卡片 */
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.04);
  border: 1px solid #f3e8ff;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;

  .title-bar {
    width: 3px;
    height: 16px;
    background: #f59e0b;
    border-radius: 2px;
  }
}

.result-value {
  text-align: center;
  padding: 20px 0;
  background: #faf5ff;
  border-radius: 10px;
  margin-bottom: 12px;

  span {
    font-size: 48px;
    font-weight: 800;
    color: #9ca3af;
    font-family: 'SF Mono', Menlo, monospace;
  }

  .result-unit {
    font-size: 16px;
    color: #909399;
    margin-left: 4px;
  }
}

.result-card:has(.status-success) .result-value span {
  color: #67c23a;
}

.result-card:has(.status-warning) .result-value span {
  color: #e6a23c;
}

.result-card:has(.status-danger) .result-value span {
  color: #f56c6c;
}

.result-status {
  text-align: center;
  font-size: 15px;
  font-weight: 600;

  &.status-success { color: #67c23a; }
  &.status-warning { color: #e6a23c; }
  &.status-danger { color: #f56c6c; }
  &.status-info { color: #909399; }
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
    color: #8b5cf6;
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
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.04);
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
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  border: none;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.25);

  &:hover {
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.35);
  }
}

/* 历史记录卡片 */
.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.04);
  max-height: 360px;
  display: flex;
  flex-direction: column;
  border: 1px solid #f3e8ff;
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
  background: #faf5ff;
  border-radius: 10px;
  border: 1px solid #ede9fe;
  transition: all 0.2s ease;

  &:hover {
    border-color: #c4b5fd;
    background: #f3e8ff;
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
  color: #8b5cf6;
}

.hi-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.hi-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.hi-value {
  .hv-num {
    font-size: 20px;
    font-weight: 700;
    color: #1f2937;
    font-family: 'SF Mono', Menlo, monospace;
  }

  .hv-unit {
    font-size: 12px;
    color: #909399;
    margin-left: 2px;
  }
}

.hi-remark {
  font-size: 12px;
  color: #606266;
  padding-top: 8px;
  border-top: 1px dashed #ede9fe;
  margin-top: 4px;

  span {
    color: #909399;
  }
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
  .situp-container {
    padding: 14px;
  }

  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 16px;
  }

  .test-card {
    padding: 24px 16px 20px;
  }

  .timer-value {
    padding: 20px 40px;
    min-width: 180px;
  }

  .tv-num {
    font-size: 48px;
  }
}
</style>
