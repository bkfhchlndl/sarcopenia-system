<template>
  <div class="grip-strength-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>握力测量</h1>
          <p>Grip Strength Measurement · 肌少症评估</p>
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
        <!-- 设备连接状态 -->
        <div class="device-card">
          <div class="device-header">
            <div class="device-icon">
              <el-icon :size="24"><Connection /></el-icon>
            </div>
            <div class="device-info">
              <div class="device-title">蓝牙握力计设备</div>
              <div class="device-status" :class="{ connected: deviceConnected }">
                <span class="status-dot"></span>
                {{ deviceConnected ? '已连接' : '未连接' }}
              </div>
            </div>
          </div>
          <div class="device-actions">
            <el-button
                :type="deviceConnected ? 'danger' : 'primary'"
                plain
                size="small"
                :loading="deviceLoading"
                @click="toggleDevice"
            >
              <el-icon v-if="!deviceConnected"><Link /></el-icon>
              <el-icon v-else><Close /></el-icon>
              {{ deviceConnected ? '断开' : '连接设备' }}
            </el-button>
          </div>
        </div>

        <!-- 测量卡片组 -->
        <div class="measure-cards">
          <!-- 第1次测量 -->
          <div class="measure-card" :class="{ 'has-value': formData.firstValue != null }">
            <div class="mc-head">
              <span class="mc-index">01</span>
              <span class="mc-label">第 1 次测量</span>
              <el-tag v-if="formData.firstValue != null" size="small" type="success" effect="light">
                已完成
              </el-tag>
            </div>
            <div class="mc-body">
              <div class="mc-value-display">
                <span class="mc-num">{{ formData.firstValue ?? '--' }}</span>
                <span class="mc-unit">kg</span>
              </div>
              <div class="mc-input-row">
                <el-input-number
                    v-model="formData.firstValue"
                    :min="1"
                    :max="100"
                    :step="0.1"
                    :precision="1"
                    placeholder="手动输入"
                    controls
                    style="width: 100%"
                />
              </div>
            </div>
            <div class="mc-foot">
              <el-button
                  type="primary"
                  :disabled="!deviceConnected"
                  :loading="fetchingIndex === 1"
                  @click="fetchMeasureValue(1)"
              >
                <el-icon><Refresh /></el-icon>
                获取测量值
              </el-button>
              <el-button
                  v-if="formData.firstValue != null"
                  link
                  type="danger"
                  @click="formData.firstValue = null"
              >
                清除
              </el-button>
            </div>
          </div>

          <!-- 第2次测量 -->
          <div class="measure-card" :class="{ 'has-value': formData.secondValue != null }">
            <div class="mc-head">
              <span class="mc-index">02</span>
              <span class="mc-label">第 2 次测量</span>
              <el-tag v-if="formData.secondValue != null" size="small" type="success" effect="light">
                已完成
              </el-tag>
              <el-tag v-else size="small" type="info" effect="plain">选填</el-tag>
            </div>
            <div class="mc-body">
              <div class="mc-value-display">
                <span class="mc-num">{{ formData.secondValue ?? '--' }}</span>
                <span class="mc-unit">kg</span>
              </div>
              <div class="mc-input-row">
                <el-input-number
                    v-model="formData.secondValue"
                    :min="1"
                    :max="100"
                    :step="0.1"
                    :precision="1"
                    placeholder="手动输入（选填）"
                    controls
                    style="width: 100%"
                    :value-on-clear="null"
                />
              </div>
            </div>
            <div class="mc-foot">
              <el-button
                  type="primary"
                  plain
                  :disabled="!deviceConnected"
                  :loading="fetchingIndex === 2"
                  @click="fetchMeasureValue(2)"
              >
                <el-icon><Refresh /></el-icon>
                获取测量值
              </el-button>
              <el-button
                  v-if="formData.secondValue != null"
                  link
                  type="danger"
                  @click="formData.secondValue = null"
              >
                清除
              </el-button>
            </div>
          </div>

          <!-- 第3次测量 -->
          <div class="measure-card" :class="{ 'has-value': formData.thirdValue != null }">
            <div class="mc-head">
              <span class="mc-index">03</span>
              <span class="mc-label">第 3 次测量</span>
              <el-tag v-if="formData.thirdValue != null" size="small" type="success" effect="light">
                已完成
              </el-tag>
              <el-tag v-else size="small" type="info" effect="plain">选填</el-tag>
            </div>
            <div class="mc-body">
              <div class="mc-value-display">
                <span class="mc-num">{{ formData.thirdValue ?? '--' }}</span>
                <span class="mc-unit">kg</span>
              </div>
              <div class="mc-input-row">
                <el-input-number
                    v-model="formData.thirdValue"
                    :min="1"
                    :max="100"
                    :step="0.1"
                    :precision="1"
                    placeholder="手动输入（选填）"
                    controls
                    style="width: 100%"
                    :value-on-clear="null"
                />
              </div>
            </div>
            <div class="mc-foot">
              <el-button
                  type="primary"
                  plain
                  :disabled="!deviceConnected"
                  :loading="fetchingIndex === 3"
                  @click="fetchMeasureValue(3)"
              >
                <el-icon><Refresh /></el-icon>
                获取测量值
              </el-button>
              <el-button
                  v-if="formData.thirdValue != null"
                  link
                  type="danger"
                  @click="formData.thirdValue = null"
              >
                清除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 最大值 + 风险等级 -->
        <div class="result-card" v-if="maxValue !== null">
          <div class="result-left">
            <div class="result-label">最大握力值</div>
            <div class="result-value">
              <span class="rv-num">{{ maxValue }}</span>
              <span class="rv-unit">kg</span>
            </div>
          </div>
          <div class="result-divider"></div>
          <div class="result-right">
            <div class="result-label">肌力评估</div>
            <div class="risk-level" :class="'risk-' + riskLevel.type">
              <el-icon><WarningFilled /></el-icon>
              {{ riskLevel.text }}
            </div>
            <div class="risk-desc">{{ riskLevel.desc }}</div>
          </div>
        </div>

        <!-- 备注 -->
        <div class="remark-card">
          <div class="rc-title">
            <el-icon><EditPen /></el-icon>
            备注说明
          </div>
          <el-input
              v-model="formData.remark"
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
              :disabled="formData.firstValue == null"
              @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            提交记录
          </el-button>
        </div>
      </div>

      <!-- 右侧：历史记录 -->
      <div class="right-panel">
        <div class="history-card">
          <div class="history-header">
            <div class="hh-title">
              <el-icon><Document /></el-icon>
              历史测量记录
            </div>
            <el-tag size="small" type="info" effect="plain">共 {{ measureList.length }} 条</el-tag>
          </div>

          <el-empty v-if="!loading && measureList.length === 0" description="暂无测量记录" />

          <div v-else class="history-list">
            <div
                v-for="(record, idx) in measureList"
                :key="record.id"
                class="history-item"
            >
              <div class="hi-head">
                <span class="hi-index">第 {{ measureList.length - idx }} 次</span>
                <span class="hi-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatDateTime(record.createTime) }}
                </span>
              </div>
              <div class="hi-values">
                <div class="hv-item" :class="{ active: record.firstValue != null }">
                  <span class="hv-label">第1次</span>
                  <span class="hv-num">{{ record.firstValue ?? '--' }}</span>
                  <span class="hv-unit">kg</span>
                </div>
                <div class="hv-item" :class="{ active: record.secondValue != null }">
                  <span class="hv-label">第2次</span>
                  <span class="hv-num">{{ record.secondValue ?? '--' }}</span>
                  <span class="hv-unit">kg</span>
                </div>
                <div class="hv-item" :class="{ active: record.thirdValue != null }">
                  <span class="hv-label">第3次</span>
                  <span class="hv-num">{{ record.thirdValue ?? '--' }}</span>
                  <span class="hv-unit">kg</span>
                </div>
                <div class="hv-item max">
                  <span class="hv-label">最大</span>
                  <span class="hv-num">{{ calcMax(record) }}</span>
                  <span class="hv-unit">kg</span>
                </div>
              </div>
              <div v-if="record.remark" class="hi-remark">
                <span>备注：</span>{{ record.remark }}
              </div>
            </div>
          </div>
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
              <span class="ref-label">正常肌力</span>
              <span class="ref-value">男 ≥ 28kg / 女 ≥ 18kg</span>
            </div>
            <div class="ref-item">
              <span class="ref-dot warning"></span>
              <span class="ref-label">肌力下降</span>
              <span class="ref-value">男 22~28kg / 女 14~18kg</span>
            </div>
            <div class="ref-item">
              <span class="ref-dot danger"></span>
              <span class="ref-label">肌少症风险</span>
              <span class="ref-value">男 ＜ 22kg / 女 ＜ 14kg</span>
            </div>
          </div>
          <div class="ref-tip">
            握力是评估肌肉力量的核心指标，是肌少症诊断的重要依据。取三次测量的最大值作为评估依据。
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="GripStrength">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Check, Clock, Connection, Link, Close, Refresh,
  EditPen, Document, WarningFilled, InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectGripStrengthList, insertGripStrength } from '@/api/s.js'

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 响应式状态 ====================
const submitting = ref(false)
const loading = ref(false)
const measureList = ref([])

/** 设备连接状态 */
const deviceConnected = ref(false)
const deviceLoading = ref(false)
/** 当前正在获取的测量序号（1/2/3），0 表示不在获取中 */
const fetchingIndex = ref(0)

/** 表单数据 */
const formData = ref({
  firstValue: null,
  secondValue: null,
  thirdValue: null,
  remark: ''
})

// ==================== 计算属性 - 路由参数 ====================
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

// ==================== 计算属性 - 最大值与风险等级 ====================
/** 计算当前表单中的最大值（握力取最大值） */
const maxValue = computed(() => {
  const values = []
  if (formData.value.firstValue != null) values.push(Number(formData.value.firstValue))
  if (formData.value.secondValue != null) values.push(Number(formData.value.secondValue))
  if (formData.value.thirdValue != null) values.push(Number(formData.value.thirdValue))
  if (values.length === 0) return null
  return Math.max(...values).toFixed(1)
})

/**
 * 肌少症风险等级（基于握力）
 * 男性：正常≥28kg，下降22-28kg，风险<22kg
 * 女性：正常≥18kg，下降14-18kg，风险<14kg
 * 注：此处默认按男性标准显示，实际应根据患者性别判断
 */
const riskLevel = computed(() => {
  if (maxValue.value == null) {
    return { text: '待评估', type: 'info', desc: '请完成测量后查看肌力评估' }
  }
  const val = Number(maxValue.value)
  // 默认按男性标准，可根据患者性别调整
  if (val < 22) {
    return {
      text: '肌力低下',
      type: 'danger',
      desc: '握力低于正常范围，提示肌肉力量不足，建议进一步进行肌量和躯体功能评估'
    }
  }
  if (val < 28) {
    return {
      text: '肌力下降',
      type: 'warning',
      desc: '握力处于临界范围，建议加强抗阻运动和营养摄入，定期监测肌力变化'
    }
  }
  return {
    text: '肌力正常',
    type: 'success',
    desc: '握力在正常范围内，肌肉力量良好，继续保持健康生活方式'
  }
})

// ==================== 工具函数 ====================
/**
 * 计算单条记录的最大值
 */
function calcMax(record) {
  const values = []
  if (record.firstValue != null) values.push(Number(record.firstValue))
  if (record.secondValue != null) values.push(Number(record.secondValue))
  if (record.thirdValue != null) values.push(Number(record.thirdValue))
  if (values.length === 0) return '-'
  return Math.max(...values).toFixed(1)
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

// ==================== 蓝牙设备操作（C# 对接，此处为占位） ====================
/**
 * 连接/断开蓝牙设备
 * TODO: 对接 C# 蓝牙设备接口
 */
function toggleDevice() {
  deviceLoading.value = true
  // 模拟连接过程
  setTimeout(() => {
    deviceConnected.value = !deviceConnected.value
    deviceLoading.value = false
    ElMessage.success(deviceConnected.value ? '设备连接成功' : '设备已断开')
  }, 1000)
}

/**
 * 从蓝牙设备获取测量值
 * @param {number} index 第几次测量（1/2/3）
 * TODO: 对接 C# 蓝牙设备接口，获取真实测量数据
 *       成功时赋值给对应的 formData 字段；失败时调用 ElMessage.error 提示
 */
function fetchMeasureValue(index) {
  if (!deviceConnected.value) {
    ElMessage.warning('请先连接蓝牙设备')
    return
  }
  fetchingIndex.value = index
  // TODO: 替换为真实蓝牙设备接口调用，以下为占位（直接报错提示未对接）
  setTimeout(() => {
    fetchingIndex.value = 0
    ElMessage.error('蓝牙设备接口尚未对接，请联系管理员配置设备')
  }, 500)
}

// ==================== 业务方法 ====================
/**
 * 加载历史测量记录
 */
async function loadMeasureList() {
  if (!patientId.value) return
  loading.value = true
  try {
    const res = await selectGripStrengthList({ patientId: patientId.value })
    if (res && res.code === 200) {
      measureList.value = res.data || []
    }
  } catch (error) {
    console.error('加载握力测量记录失败：', error)
  } finally {
    loading.value = false
  }
}

/**
 * 提交测量记录
 */
async function handleSubmit() {
  if (formData.value.firstValue == null) {
    ElMessage.warning('请至少完成第 1 次测量')
    return
  }

  submitting.value = true
  try {
    const submitData = {
      patientId: Number(patientId.value),
      firstValue: formData.value.firstValue,
      secondValue: formData.value.secondValue,
      thirdValue: formData.value.thirdValue,
      remark: formData.value.remark || null
    }

    const res = await insertGripStrength(submitData)
    if (res && res.code === 200) {
      ElMessage.success('测量记录提交成功')
      // 重置表单
      formData.value = { firstValue: null, secondValue: null, thirdValue: null, remark: '' }
      // 刷新列表
      await loadMeasureList()
      // 返回
      setTimeout(() => {
        backToCustomize()
      }, 1200)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交握力测量失败：', error)
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
      localStorage.setItem(`sarcopenia_customize_done:${patientId.value}:grip_strength`, '1')
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
  loadMeasureList()
})
</script>

<style scoped lang="scss">
.grip-strength-container {
  min-height: 100vh;
  padding: 20px 28px 40px;
  background: #f0f5ff;
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
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
  margin-bottom: 20px;
  border-left: 4px solid #dc2626;
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
    color: #dc2626;
    background: #fef2f2;
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
  background: #fef2f2;
  border-radius: 8px;
  border: 1px solid #fecaca;
}

.badge-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #dc2626, #991b1b);
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

/* 设备卡片 */
.device-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 22px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #fee2e2;
}

.device-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.device-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #fee2e2;
  color: #dc2626;
}

.device-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 3px;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #dcdfe6;
  }

  &.connected {
    color: #67c23a;
    .status-dot {
      background: #67c23a;
      box-shadow: 0 0 6px rgba(103, 194, 58, 0.5);
    }
  }
}

/* 测量卡片组 */
.measure-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.measure-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
  border: 2px solid transparent;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 14px;

  &:hover {
    box-shadow: 0 4px 16px rgba(220, 38, 38, 0.12);
  }

  &.has-value {
    border-color: #fca5a5;
    background: linear-gradient(180deg, #fef2f2 0%, #fff 100%);
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
  background: #fef2f2;
  color: #dc2626;
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

/* 测量值大显示 */
.mc-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mc-value-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  padding: 20px 0;
  background: #fff5f5;
  border-radius: 10px;
  border: 1px dashed #fca5a5;
}

.mc-num {
  font-size: 48px;
  font-weight: 800;
  color: #dc2626;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  line-height: 1;
  min-width: 100px;
  text-align: center;
}

.measure-card:not(.has-value) .mc-num {
  color: #c0c4cc;
}

.mc-unit {
  font-size: 16px;
  color: #909399;
  font-weight: 500;
}

.mc-input-row {
  width: 100%;

  :deep(.el-input-number) {
    width: 100% !important;
  }
}

.mc-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

/* 结果卡片 */
.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px 28px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
  display: flex;
  align-items: center;
  gap: 24px;
  border: 1px solid #fee2e2;
}

.result-left {
  text-align: center;
  min-width: 160px;
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
}

.rv-num {
  font-size: 40px;
  font-weight: 800;
  color: #dc2626;
  font-family: 'SF Mono', Menlo, monospace;
}

.rv-unit {
  font-size: 16px;
  color: #909399;
}

.result-divider {
  width: 1px;
  height: 60px;
  background: #fee2e2;
}

.result-right {
  flex: 1;
}

.risk-level {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 18px;
  font-weight: 700;
  margin: 4px 0 8px;

  &.risk-success { color: #67c23a; }
  &.risk-warning { color: #e6a23c; }
  &.risk-danger { color: #f56c6c; }
  &.risk-info { color: #909399; }
}

.risk-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

/* 备注卡片 */
.remark-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 22px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
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
  min-width: 140px;
  background: linear-gradient(135deg, #dc2626, #991b1b);
  border: none;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.25);

  &:hover {
    box-shadow: 0 4px 12px rgba(220, 38, 38, 0.35);
  }
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 20px;
}

/* 历史记录卡片 */
.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
  max-height: 480px;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.hh-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  padding: 14px 16px;
  background: #fff5f5;
  border-radius: 10px;
  border: 1px solid #fee2e2;
  transition: all 0.2s ease;

  &:hover {
    border-color: #fca5a5;
    background: #fef2f2;
  }
}

.hi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.hi-index {
  font-size: 13px;
  font-weight: 700;
  color: #dc2626;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.hv-item {
  text-align: center;
  padding: 8px 6px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  opacity: 0.5;

  &.active {
    opacity: 1;
    border-color: #fecaca;
  }

  &.max {
    opacity: 1;
    background: #fee2e2;
    border-color: #fca5a5;
  }
}

.hv-label {
  display: block;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.hv-num {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  font-family: 'SF Mono', Menlo, monospace;
}

.hv-item.max .hv-num {
  color: #dc2626;
}

.hv-unit {
  font-size: 10px;
  color: #909399;
  margin-left: 2px;
}

.hi-remark {
  font-size: 12px;
  color: #606266;
  padding-top: 8px;
  border-top: 1px dashed #fee2e2;
  margin-top: 4px;

  span {
    color: #909399;
  }
}

/* 参考标准卡片 */
.reference-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.06);
  border: 1px solid #fee2e2;
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
  .grip-strength-container {
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

  .result-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .result-divider {
    width: 80%;
    height: 1px;
  }
}
</style>
