<template>
  <div class="balance-container">
    <!-- 顶部导航栏 -->
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>站立平衡测试</h1>
          <p>Standing Balance Test · SPPB 躯体功能评估</p>
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
      <!-- 左侧：测试操作区 -->
      <div class="left-panel">
        <!-- 当前项目大卡片 -->
        <div class="current-test-card">
          <div class="ct-header">
            <span class="ct-badge">第 {{ currentIndex + 1 }} / 3 项</span>
            <span class="ct-title">{{ testItems[currentIndex].name }}</span>
            <span class="ct-target">目标: {{ testItems[currentIndex].target }}</span>
          </div>

          <!-- 测试主体区：左图右计时 -->
          <div class="ct-body">
            <!-- 动作示意图 -->
            <div class="illustration">
              <div class="img-crop">
                <img v-if="currentIndex === 0" src="/balance_feet_together.png" alt="双脚并立" class="foot-img" />
                <img v-else-if="currentIndex === 1" src="/balance_semi_offset.png" alt="半脚错开" class="foot-img" />
                <img v-else src="/balance_tandem.png" alt="前后脚一条线" class="foot-img" />
              </div>
            </div>

            <!-- 计时与操作区 -->
            <div class="ct-right">
              <!-- 计时大显示 -->
              <div class="timer-display">
                <div class="timer-value" :class="{ 'success': isTargetReached }">
                  <span class="tv-num">{{ formatTime(currentTime) }}</span>
                  <span class="tv-unit">秒</span>
                </div>
                <div v-if="isTargetReached" class="target-reached">
                  <el-icon><CircleCheckFilled /></el-icon>
                  已达目标时长
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <template v-if="!isRunning && !itemFinished">
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
                    完成测试
                  </el-button>
                  <el-button
                      type="danger"
                      size="large"
                      class="btn-cancel"
                      plain
                      @click="cancelTimer"
                  >
                    <el-icon><Close /></el-icon>
                    不能完成
                  </el-button>
                </template>
                <template v-if="itemFinished">
                  <el-button
                      type="primary"
                      size="large"
                      class="btn-next"
                      @click="nextItem"
                  >
                    <el-icon><Right /></el-icon>
                    {{ currentIndex < testItems.length - 1 ? '下一项' : '查看结果' }}
                  </el-button>
                  <el-button
                      size="large"
                      class="btn-retry"
                      @click="retryItem"
                  >
                    <el-icon><RefreshRight /></el-icon>
                    重测本项
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 三个项目进度卡 -->
        <div class="items-progress">
          <div
              v-for="(item, idx) in testItems"
              :key="item.key"
              class="item-card"
              :class="{
                'active': currentIndex === idx && !allFinished,
                'done': itemFinished && currentIndex === idx,
                'completed': getItemValue(item.key) != null,
                'clickable': !isRunning
              }"
              @click="switchToItem(idx)"
          >
            <div class="item-head">
              <span class="item-index">{{ String(idx + 1).padStart(2, '0') }}</span>
              <span class="item-name">{{ item.name }}</span>
              <el-tag
                  v-if="getItemValue(item.key) != null"
                  size="small"
                  :type="getItemScore(item.key) > 0 ? 'success' : 'danger'"
                  effect="light"
              >
                {{ getItemScore(item.key) }} 分
              </el-tag>
              <el-tag
                  v-else-if="currentIndex === idx && isRunning"
                  size="small"
                  type="warning"
                  effect="light"
              >
                进行中
              </el-tag>
            </div>
            <div class="item-body">
              <span class="item-value">
                {{ getItemValue(item.key) != null ? getItemValue(item.key).toFixed(2) : '--' }}
              </span>
              <span class="item-unit">秒</span>
            </div>
            <div class="item-target">
              目标: {{ item.target }}
              <span class="item-score-detail">({{ item.scoreDesc }})</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：数据面板 -->
      <div class="right-panel">
        <!-- 总分卡片 -->
        <div class="score-card" v-if="allFinished">
          <div class="score-header">
            <el-icon><Trophy /></el-icon>
            SPPB 平衡测试总分
          </div>
          <div class="score-display">
            <span class="score-num">{{ totalScore }}</span>
            <span class="score-total">/ 4 分</span>
          </div>
          <div class="score-level" :class="'level-' + scoreLevel.type">
            {{ scoreLevel.text }}
          </div>
          <div class="score-desc">{{ scoreLevel.desc }}</div>
        </div>

        <!-- 未完成时的占位 -->
        <div class="score-card placeholder" v-else>
          <div class="placeholder-icon">
            <el-icon :size="48"><Aim /></el-icon>
          </div>
          <div class="placeholder-text">完成全部 3 项后查看总分</div>
          <div class="placeholder-progress">
            已完成 {{ completedCount }} / 3 项
          </div>
        </div>

        <!-- 各项得分明细 -->
        <div class="detail-card">
          <div class="dc-title">
            <el-icon><List /></el-icon>
            各项得分明细
          </div>
          <div class="dc-list">
            <div
                v-for="(item, idx) in testItems"
                :key="item.key"
                class="dc-item"
            >
              <div class="dc-left">
                <div class="dc-name">{{ item.name }}</div>
                <div class="dc-time" v-if="getItemValue(item.key) != null">
                  {{ getItemValue(item.key).toFixed(2) }} 秒
                </div>
              </div>
              <div class="dc-right">
                <el-button
                    v-if="getItemValue(item.key) != null"
                    size="small"
                    text
                    type="primary"
                    @click.stop="remeasureItem(idx)"
                >
                  <el-icon><RefreshRight /></el-icon>
                  重测
                </el-button>
                <div class="dc-score">
                  <span class="dc-num">{{ getItemScore(item.key) }}</span>
                  <span class="dc-max">/ {{ item.maxScore }} 分</span>
                </div>
              </div>
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
              :disabled="!allFinished"
              @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            提交记录
          </el-button>
        </div>

        <!-- 评分标准 -->
        <div class="reference-card">
          <div class="ref-title">
            <el-icon><InfoFilled /></el-icon>
            SPPB 平衡测试评分标准
          </div>
          <div class="ref-list">
            <div class="ref-section">
              <div class="ref-sec-title">双脚并立 / 半脚错开</div>
              <div class="ref-row">
                <span class="ref-score">1 分</span>
                <span class="ref-cond">≥ 10 秒</span>
              </div>
              <div class="ref-row">
                <span class="ref-score">0 分</span>
                <span class="ref-cond">＜ 10 秒 / 不能完成</span>
              </div>
            </div>
            <div class="ref-section">
              <div class="ref-sec-title">前后脚一条线</div>
              <div class="ref-row">
                <span class="ref-score">2 分</span>
                <span class="ref-cond">≥ 10 秒</span>
              </div>
              <div class="ref-row">
                <span class="ref-score">1 分</span>
                <span class="ref-cond">3 ~ 9.99 秒</span>
              </div>
              <div class="ref-row">
                <span class="ref-score">0 分</span>
                <span class="ref-cond">＜ 3 秒 / 不能完成</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="BalanceStand">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Check, Close, EditPen, VideoPlay,
  RefreshRight, Right, InfoFilled, List, Trophy,
  CircleCheckFilled, Aim
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { selectBalanceStandList, insertBalanceStand } from '@/api/s.js'

// ==================== 路由实例 ====================
const route = useRoute()
const router = useRouter()

// ==================== 测试项目配置 ====================
const testItems = [
  {
    key: 'feetTogether',
    name: '双脚并立站立',
    target: '10 秒',
    maxScore: 1,
    scoreDesc: '≥10秒得1分',
    targetSeconds: 10
  },
  {
    key: 'semiOffset',
    name: '半脚错开站立',
    target: '10 秒',
    maxScore: 1,
    scoreDesc: '≥10秒得1分',
    targetSeconds: 10
  },
  {
    key: 'tandemStand',
    name: '前后脚一条线站立',
    target: '10 秒',
    maxScore: 2,
    scoreDesc: '≥10秒得2分，3~9.99秒得1分',
    targetSeconds: 10
  }
]

// ==================== 响应式状态 ====================
const submitting = ref(false)
const remark = ref('')

/** 计时相关 */
const currentTime = ref(0)
const isRunning = ref(false)
let timerInterval = null

/** 当前项目索引 */
const currentIndex = ref(0)
/** 当前项目是否完成 */
const itemFinished = ref(false)

/** 各项测量值（秒） */
const feetTogetherTime = ref(null)
const semiOffsetTime = ref(null)
const tandemStandTime = ref(null)

// ==================== 计算属性 ====================
const patientId = computed(() => route.query.patientId || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')

/** 是否全部完成 */
const allFinished = computed(() => {
  return feetTogetherTime.value != null
      && semiOffsetTime.value != null
      && tandemStandTime.value != null
})

/** 已完成项数 */
const completedCount = computed(() => {
  let count = 0
  if (feetTogetherTime.value != null) count++
  if (semiOffsetTime.value != null) count++
  if (tandemStandTime.value != null) count++
  return count
})

/** 当前项目是否已达目标时长 */
const isTargetReached = computed(() => {
  const target = testItems[currentIndex.value]?.targetSeconds || 10
  return currentTime.value >= target
})

/** 总分 */
const totalScore = computed(() => {
  return testItems.reduce((sum, item) => sum + getItemScore(item.key), 0)
})

/** 总分等级 */
const scoreLevel = computed(() => {
  const score = totalScore.value
  if (score >= 4) return { text: '平衡功能优秀', type: 'success', desc: '平衡功能良好，跌倒风险低，继续保持运动锻炼' }
  if (score >= 3) return { text: '平衡功能良好', type: 'success', desc: '平衡功能基本正常，建议加强下肢力量训练' }
  if (score >= 2) return { text: '平衡功能下降', type: 'warning', desc: '平衡功能有所下降，存在一定跌倒风险，建议进行平衡训练' }
  if (score >= 1) return { text: '平衡功能较差', type: 'danger', desc: '平衡功能明显下降，跌倒风险较高，建议就医评估' }
  return { text: '平衡功能严重下降', type: 'danger', desc: '平衡功能严重受损，跌倒风险极高，需立即就医评估' }
})

// ==================== 工具函数 ====================
function formatTime(seconds) {
  return Number(seconds).toFixed(2)
}

/**
 * 获取某项的测量值
 */
function getItemValue(key) {
  if (key === 'feetTogether') return feetTogetherTime.value
  if (key === 'semiOffset') return semiOffsetTime.value
  if (key === 'tandemStand') return tandemStandTime.value
  return null
}

/**
 * 设置某项的测量值
 */
function setItemValue(key, value) {
  if (key === 'feetTogether') feetTogetherTime.value = value
  else if (key === 'semiOffset') semiOffsetTime.value = value
  else if (key === 'tandemStand') tandemStandTime.value = value
}

/**
 * 计算某项的得分
 */
function getItemScore(key) {
  const time = getItemValue(key)
  if (time == null) return 0
  const item = testItems.find(t => t.key === key)
  if (!item) return 0

  if (key === 'feetTogether' || key === 'semiOffset') {
    return time >= 10 ? 1 : 0
  }
  if (key === 'tandemStand') {
    if (time >= 10) return 2
    if (time >= 3) return 1
    return 0
  }
  return 0
}

// ==================== 计时相关方法 ====================
function startTimer() {
  currentTime.value = 0
  isRunning.value = true
  itemFinished.value = false

  const startTime = Date.now()
  timerInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    currentTime.value = Math.round(elapsed * 100) / 100
  }, 10)
}

function stopTimer() {
  clearInterval(timerInterval)
  timerInterval = null
  isRunning.value = false
  itemFinished.value = true

  const key = testItems[currentIndex.value].key
  setItemValue(key, currentTime.value)
  ElMessage.success(`${testItems[currentIndex.value].name}完成`)
}

function cancelTimer() {
  clearInterval(timerInterval)
  timerInterval = null
  isRunning.value = false
  itemFinished.value = true

  const key = testItems[currentIndex.value].key
  // 不能完成记 0 秒
  setItemValue(key, 0)
  ElMessage.info(`已标记为不能完成`)
}

function nextItem() {
  if (currentIndex.value < testItems.length - 1) {
    currentIndex.value++
    currentTime.value = 0
    itemFinished.value = false
  }
}

function retryItem() {
  currentTime.value = 0
  itemFinished.value = false
  isRunning.value = false
  const key = testItems[currentIndex.value].key
  setItemValue(key, null)
}

/** 从得分明细重测某一项 */
function remeasureItem(idx) {
  if (isRunning.value) return
  const key = testItems[idx].key
  setItemValue(key, null)
  currentIndex.value = idx
  currentTime.value = 0
  itemFinished.value = false
  isRunning.value = false
}

/** 切换到某一项（点击项目卡片时） */
function switchToItem(idx) {
  if (isRunning.value) return
  // 如果当前项正在进行中但未完成，不允许切换（避免丢失计时）
  if (currentIndex.value === idx) return
  
  currentIndex.value = idx
  // 如果切换到的项已经有值，显示完成状态
  const key = testItems[idx].key
  if (getItemValue(key) != null) {
    itemFinished.value = true
    currentTime.value = getItemValue(key)
  } else {
    itemFinished.value = false
    currentTime.value = 0
  }
  isRunning.value = false
}

// ==================== 业务方法 ====================
async function handleSubmit() {
  if (!allFinished.value) {
    ElMessage.warning('请完成全部 3 项测试')
    return
  }

  submitting.value = true
  try {
    const submitData = {
      patientId: Number(patientId.value),
      feetTogetherTime: feetTogetherTime.value,
      semiOffsetTime: semiOffsetTime.value,
      tandemStandTime: tandemStandTime.value,
      remark: remark.value || null
    }

    const res = await insertBalanceStand(submitData)
    if (res && res.code === 200) {
      ElMessage.success('测试记录提交成功')
      setTimeout(() => {
        backToCustomize()
      }, 1200)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交站立平衡测试失败：', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function goBack() {
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
      localStorage.setItem(`sarcopenia_customize_done:${patientId.value}:balance_stand`, '1')
    }
  } catch (e) {
    console.warn('本地存储写入失败：', e)
  }
  let backPath = '/patient/detection/customize'
  if (route.query.from === 'sCustomize') {
    backPath = '/patient/detection/sCustomize'
  } else if (route.query.from === 'sComprehensive') {
    backPath = '/patient/detection/sComprehensive'
  }
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
  // 进入页面时重置
  feetTogetherTime.value = null
  semiOffsetTime.value = null
  tandemStandTime.value = null
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
})
</script>

<style scoped lang="scss">
.balance-container {
  min-height: 100vh;
  padding: 20px 28px 40px;
  background: #fff7ed;
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
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.06);
  margin-bottom: 20px;
  border-left: 4px solid #f97316;
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
    color: #f97316;
    background: #ffedd5;
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
  background: #ffedd5;
  border-radius: 8px;
  border: 1px solid #fed7aa;
}

.badge-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #f97316, #ea580c);
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

/* 主内容区 */
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

/* 当前测试卡片 */
.current-test-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.06);
  border: 1px solid #fed7aa;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.ct-header {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 测试主体：左图右计时按钮 */
.ct-body {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 10px 0;
}

/* 动作示意图 */
.illustration {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.img-crop {
  width: 100%;
  max-width: 260px;
  overflow: hidden;
  border-radius: 12px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
}

.foot-img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
}

/* 右侧计时与操作区 */
.ct-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.ct-badge {
  padding: 4px 12px;
  background: #ffedd5;
  color: #ea580c;
  font-size: 12px;
  font-weight: 700;
  border-radius: 12px;
}

.ct-title {
  font-size: 22px;
  font-weight: 800;
  color: #1f2937;
}

.ct-target {
  font-size: 13px;
  color: #909399;
  background: #fff7ed;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #fed7aa;
}

/* 计时大显示 */
.timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.timer-value {
  background: #fff7ed;
  border: 2px dashed #fdba74;
  border-radius: 12px;
  padding: 24px 60px;
  text-align: center;
  min-width: 280px;
  transition: all 0.3s ease;

  &.success {
    border-color: #86efac;
    background: #f0fdf4;
  }
}

.tv-num {
  font-size: 64px;
  font-weight: 800;
  color: #f97316;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  line-height: 1;
  display: block;
  transition: color 0.3s ease;
}

.timer-value.success .tv-num {
  color: #22c55e;
}

.tv-unit {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
  font-weight: 500;
}

.target-reached {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #22c55e;
  font-size: 14px;
  font-weight: 600;
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
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(249, 115, 22, 0.4);
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

.btn-cancel {
  min-width: 140px;
  height: 48px;
  border-radius: 24px;
  font-weight: 600;
}

.btn-next {
  min-width: 180px;
  height: 48px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(249, 115, 22, 0.4);
  }
}

.btn-retry {
  min-width: 140px;
  height: 48px;
  border-radius: 24px;
  font-weight: 600;
}

/* 项目进度卡 */
.items-progress {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.04);
  border: 2px solid transparent;
  transition: all 0.25s ease;

  &.clickable {
    cursor: pointer;
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.12);
      border-color: #fdba74;
    }
  }

  &.active {
    border-color: #f97316;
    background: linear-gradient(180deg, #fff7ed 0%, #fff 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
  }

  &.completed {
    border-color: #86efac;
    background: linear-gradient(180deg, #f0fdf4 0%, #fff 100%);
  }
}

.item-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.item-index {
  font-size: 13px;
  font-weight: 700;
  color: #f97316;
  font-family: 'SF Mono', Menlo, monospace;
  background: #ffedd5;
  padding: 2px 6px;
  border-radius: 4px;
}

.item-card.completed .item-index {
  color: #16a34a;
  background: #dcfce7;
}

.item-name {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.item-body {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
  padding: 10px 0;
  background: #fff7ed;
  border-radius: 8px;
  justify-content: center;
}

.item-card.completed .item-body {
  background: #dcfce7;
}

.item-value {
  font-size: 28px;
  font-weight: 800;
  color: #f97316;
  font-family: 'SF Mono', Menlo, monospace;
}

.item-card.completed .item-value {
  color: #16a34a;
}

.item-unit {
  font-size: 12px;
  color: #909399;
}

.item-target {
  font-size: 11px;
  color: #909399;
  text-align: center;
}

.item-score-detail {
  color: #d97706;
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 20px;
}

/* 总分卡片 */
.score-card {
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.06);
  border: 1px solid #fed7aa;

  &.placeholder {
    text-align: center;
    padding: 32px 20px;
  }
}

.score-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
}

.score-display {
  text-align: center;
  margin-bottom: 12px;
}

.score-num {
  font-size: 56px;
  font-weight: 800;
  color: #f97316;
  font-family: 'SF Mono', Menlo, monospace;
  line-height: 1;
}

.score-total {
  font-size: 16px;
  color: #909399;
  margin-left: 4px;
}

.score-level {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  padding: 8px 0;
  margin-bottom: 8px;
  border-top: 1px solid #f0f0f0;
  padding-top: 14px;

  &.level-success { color: #67c23a; }
  &.level-warning { color: #e6a23c; }
  &.level-danger { color: #f56c6c; }
}

.score-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  text-align: center;
}

.placeholder-icon {
  color: #fed7aa;
  margin-bottom: 12px;
}

.placeholder-text {
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.placeholder-progress {
  font-size: 12px;
  color: #d97706;
  font-weight: 600;
}

/* 明细卡片 */
.detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.04);
}

.dc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 14px;
}

.dc-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dc-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #fff7ed;
  border-radius: 8px;
  gap: 10px;
}

.dc-left {
  flex: 1;
  min-width: 0;
}

.dc-name {
  font-size: 13px;
  color: #4b5563;
  font-weight: 500;
  margin-bottom: 2px;
}

.dc-time {
  font-size: 12px;
  color: #909399;
  font-family: 'SF Mono', Menlo, monospace;
}

.dc-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.dc-score {
  text-align: right;
  .dc-num {
    font-size: 20px;
    font-weight: 800;
    color: #f97316;
    font-family: 'SF Mono', Menlo, monospace;
  }
  .dc-max {
    font-size: 12px;
    color: #909399;
    margin-left: 2px;
  }
}

/* 备注卡片 */
.remark-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.04);
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
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.25);

  &:hover {
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.35);
  }
}

/* 评分标准 */
.reference-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.04);
  border: 1px solid #fed7aa;
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
  gap: 14px;
}

.ref-section {
  padding-bottom: 12px;
  border-bottom: 1px dashed #fed7aa;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.ref-sec-title {
  font-size: 13px;
  font-weight: 600;
  color: #ea580c;
  margin-bottom: 8px;
}

.ref-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  font-size: 12px;
}

.ref-score {
  width: 36px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffedd5;
  color: #ea580c;
  font-weight: 700;
  border-radius: 4px;
  font-size: 11px;
}

.ref-cond {
  flex: 1;
  color: #606266;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  .right-panel { position: static; }
}

@media (max-width: 768px) {
  .balance-container { padding: 14px; }
  .top-bar { flex-direction: column; align-items: flex-start; gap: 12px; padding: 14px 16px; }
  .items-progress { grid-template-columns: 1fr; }
  .timer-value { padding: 20px 40px; min-width: 220px; }
  .tv-num { font-size: 48px; }
}
</style>
