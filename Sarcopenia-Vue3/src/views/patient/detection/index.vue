<template>
  <div class="detection-entry">
    <!-- 顶部患者信息条 -->
    <div class="patient-bar" v-if="patientId">
      <div class="patient-bar-avatar">
        <el-icon><User /></el-icon>
      </div>
      <div class="patient-bar-info">
        <div class="patient-bar-name">{{ patientName }}
          <span class="patient-tag">待评估</span>
        </div>
        <div class="patient-bar-meta">
          <template v-if="medicalCaseNo">{{ medicalCaseNo }}</template>
          <template v-if="rowInfo?.gender"> · {{ formatGender(rowInfo.gender) }}</template>
          <template v-if="rowInfo?.birthday"> · {{ rowInfo.birthday }}</template>
        </div>
      </div>
      <div class="patient-actions">
        <el-button class="back-link" plain @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
    </div>

    <!-- 核心弹窗式卡片 -->
    <div class="dialog-card">
      <div class="dialog-header">
        <div class="header-main">
          <div class="header-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="header-text">
            <h2>选择评估类型</h2>
            <p>请根据该患者的临床需求，选择合适的评估方案</p>
          </div>
        </div>
      </div>

      <!-- 两大方案入口 -->
      <div class="option-grid">
        <div
            v-for="(opt, idx) in assessmentTypes"
            :key="opt.code"
            class="option-card"
            :class="{ highlight: opt.highlight }"
            :style="{ '--accent-color': opt.color, '--accent-soft': opt.color + '14' }"
        >
          <!-- 左上色条 -->
          <div class="accent-bar" :style="{ background: opt.color }"></div>

          <!-- 图标方块 -->
          <div class="icon-box" :style="{ background: opt.color + '14', color: opt.color }">
            <span class="icon-emoji">{{ opt.icon }}</span>
          </div>

          <!-- 标题副标题 -->
          <div class="card-title">{{ opt.title }}</div>
          <div class="card-sub">{{ opt.subtitle }}</div>

          <!-- 项目标签 -->
          <div class="tag-row">
            <span v-for="tag in opt.tags" :key="tag" class="tag-item">{{ tag }}</span>
          </div>

          <!-- 分隔 -->
          <div class="divider"></div>

          <!-- 底部信息 -->
          <div class="card-footer">
            <button class="footer-btn" type="button" @click="selectOption(opt)">
              <span>开始评估</span>
              <el-icon><ArrowRightBold /></el-icon>
            </button>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="bottom-hint">
        <el-icon><InfoFilled /></el-icon>
        <span>评估数据严格遵守医疗数据保密规范，仅用于临床辅助决策。</span>
      </div>
    </div>

    <el-dialog
      v-model="patientDialogVisible"
      width="520px"
      class="patient-guide-dialog"
      append-to-body
      :show-close="false"
      :close-on-click-modal="false"
    >
      <div class="guide-dialog">
        <button class="guide-close" type="button" @click="patientDialogVisible = false">
          <el-icon><Close /></el-icon>
        </button>
        <div class="guide-top">
          <div class="guide-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div>
            <div class="guide-eyebrow">缺少患者信息</div>
            <h3>请先选择或新建患者</h3>
          </div>
        </div>
        <p class="guide-desc">
          当前检测评估页面没有收到患者 ID，无法把评估结果保存到具体患者名下。请先新建患者，或从用户管理列表点击“进入测试”。
        </p>
        <div class="guide-options">
          <button class="guide-option primary" type="button" @click="goCreatePatient">
            <span class="guide-option-icon">+</span>
            <span>
              <strong>新建用户</strong>
              <small>录入基础信息后再开始评估</small>
            </span>
            <el-icon><ArrowRightBold /></el-icon>
          </button>
          <button class="guide-option" type="button" @click="goPatientList">
            <span class="guide-option-icon list">☰</span>
            <span>
              <strong>用户管理</strong>
              <small>从已有患者列表进入测试</small>
            </span>
            <el-icon><ArrowRightBold /></el-icon>
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, UserFilled, ArrowLeft, DataAnalysis, ArrowRightBold, InfoFilled, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const patientDialogVisible = ref(false)

const patientId = computed(() => route.query.patientId || route.query.id || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')
const medicalCaseNo = computed(() => route.query.medicalCaseNo || route.query.caseNo || '')
const rowInfo = computed(() => ({
  gender: route.query.gender,
  age: route.query.age,
  birthday: route.query.birthday
}))

function formatGender(g) {
  if (g === '1' || g === 1) return '男'
  if (g === '2' || g === 2) return '女'
  return ''
}

// 两大评估方案
const assessmentTypes = [
  {
    code: 'cga',
    title: '老年综合评估',
    subtitle: 'Comprehensive Geriatric Assessment',
    icon: '🧬',
    color: '#3b82f6',
    tags: ['CGA标准', '多维度', '全人评估'],
    highlight: false,
    route: { path: '/patient/detection/geriatricsyndrome', paramsKeep: true }
  },
  {
    code: 'sarcopenia',
    title: '肌少症诊断',
    subtitle: 'Sarcopenia Diagnosis',
    icon: '💪',
    color: '#f97316',
    tags: ['肌量筛查', '握力检测', '步速评估', 'AWGS标准'],
    highlight: false,
    route: { path: '/patient/detection/sarcopeniaSyndrome', queryMerge: { mode: 'sarcopenia' } }
  }
]

/** 校验：必须传 patientId 才能进入评估 */
function requirePatient() {
  if (!patientId.value) {
    patientDialogVisible.value = true
    return false
  }
  return true
}

function goCreatePatient() {
  patientDialogVisible.value = false
  router.push('/newCase')
}

function goPatientList() {
  patientDialogVisible.value = false
  router.push('/caseList')
}

function selectOption(opt) {
  if (!requirePatient()) return
  const query = { ...buildPatientQuery(), ...(opt.route?.queryMerge || {}) }
  router.push({ path: opt.route.path, query })
}

function goBack() {
  router.push('/caseList')
}

function buildPatientQuery() {
  return {
    patientId: patientId.value,
    patientName: patientName.value,
    name: patientName.value,
    medicalCaseNo: medicalCaseNo.value,
    gender: rowInfo.value.gender,
    birthday: rowInfo.value.birthday,
    projectId: route.query.projectId || 1
  }
}
</script>

<style scoped lang="scss">
/* ====== 页面容器 ====== */
.detection-entry {
  min-height: calc(100vh - 84px);
  padding: 24px 20px 40px;
  background: #f5f8fb;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* ====== 顶部患者信息条 ====== */
.patient-bar {
  width: 100%;
  max-width: 1080px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 24px;
  border: 1px solid #dbe7ef;
  box-shadow: 0 6px 18px rgba(39, 73, 93, 0.04);
}

.patient-bar-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #edf7f4;
  color: #267d68;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.patient-bar-info {
  flex: 1;
}

.patient-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.assess-link,
.back-link {
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  font-weight: 700;
}

.assess-link {
  background: #267d68;
  border-color: #267d68;

  &:hover {
    background: #1f6b59;
    border-color: #1f6b59;
  }
}

.back-link {
  color: #475569;
  background: #f8fafc;
  border-color: #dbe7ef;

  &:hover {
    color: #267d68;
    background: #edf7f4;
    border-color: #9ccfc0;
  }
}

.patient-bar-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.patient-tag {
  font-size: 11px;
  font-weight: 600;
  color: #267d68;
  background: #edf7f4;
  padding: 2px 8px;
  border-radius: 999px;
  margin-left: 8px;
}

.patient-bar-meta {
  font-size: 12.5px;
  color: #64748b;
  margin-top: 3px;
}

/* ====== 核心弹窗式卡片 ====== */
.dialog-card {
  width: 100%;
  max-width: 1080px;
  background: #fff;
  border-radius: 8px;
  padding: 28px 32px 26px;
  box-shadow: 0 10px 28px rgba(39, 73, 93, 0.06);
  border: 1px solid #dbe7ef;
}

/* ====== Header ====== */
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 22px;
  margin-bottom: 22px;
  border-bottom: 1px solid #e6eef3;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  background: #edf7f4;
  color: #267d68;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.header-text h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}
.header-text p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.header-stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 10px 18px;
  background: #f1f5f9;
  border-radius: 12px;
}
.stat-num { font-size: 22px; font-weight: 700; color: #3b82f6; }
.stat-label { font-size: 12px; color: #64748b; }

/* ====== 方案卡网格 ====== */
.option-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 26px;
}

/* ====== 单张方案卡 ====== */
.option-card {
  position: relative;
  overflow: hidden;
  padding: 24px 26px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #dbe7ef;
  cursor: default;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;

  &:hover {
    box-shadow: 0 8px 20px rgba(39, 73, 93, 0.08);
    border-color: #9ccfc0;
  }

  &.highlight {
    border-color: #9ccfc0;
    background: #fbfefd;
  }
}

.accent-bar {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #267d68 !important;
  opacity: 1;
}

.icon-box {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  background: #edf7f4 !important;
  color: #267d68 !important;
}

.icon-emoji {
  font-size: 20px;
  line-height: 1;
}

.card-title {
  font-size: 19px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.card-sub {
  font-size: 11.5px;
  letter-spacing: 0;
  color: #94a3b8;
  margin-bottom: 14px;
  text-transform: uppercase;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 18px;
}

.tag-item {
  font-size: 12px;
  padding: 4px 10px;
  background: #f7fafb;
  color: #475569;
  border-radius: 6px;
  border: 1px solid #dbe7ef;
}

.option-card.highlight .tag-item:last-child {
  background: #edf7f4;
  color: #267d68;
  border-color: #c8e6dc;
}

.divider {
  height: 1px;
  background: #f1f5f9;
  margin: 0 -4px 14px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.footer-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 118px;
  height: 40px;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  color: #267d68;
  padding: 0 18px;
  border-radius: 8px;
  background: #edf7f4;
  border: 1px solid #c8e6dc;
  cursor: pointer;
  transition: all 0.18s ease;

  .el-icon {
    font-size: 16px;
  }

  &:hover {
    background: #267d68;
    border-color: #267d68;
    color: #fff;
  }
}

.option-card:nth-child(2) .footer-btn {
  color: #267d68;
  background: #edf7f4;

  &:hover {
    background: #267d68;
    border-color: #267d68;
    color: #fff;
  }
}

/* ====== 底部提示 ====== */
.bottom-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8fafb;
  border-radius: 8px;
  font-size: 12.5px;
  color: #64748b;

  .el-icon {
    color: #94a3b8;
    flex-shrink: 0;
  }
}

/* ====== 响应式 ====== */
@media (max-width: 900px) {
  .detection-entry { padding: 16px 12px 30px; }

  .dialog-card { padding: 20px 18px; }

  .option-grid { grid-template-columns: 1fr; }

  .header-stat { display: none; }

  .patient-bar {
    align-items: flex-start;
  }

  .patient-actions {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 480px) {
  .card-footer {
    align-items: stretch;
  }

  .footer-btn {
    width: 100%;
  }

  .patient-bar-meta .sep,
  .patient-bar-meta span:nth-child(n+3) {
    display: none;
  }
}
</style>

<style>
.patient-guide-dialog {
  --el-dialog-border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dbe7ef;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
}

.patient-guide-dialog .el-dialog__header,
.patient-guide-dialog .el-dialog__body {
  padding: 0;
}

.guide-dialog {
  position: relative;
  padding: 30px;
  background: #ffffff;
}

.guide-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  background: #f7fafb;
  color: #64748b;
  cursor: pointer;
  transition: all 0.18s ease;
}

.guide-close:hover {
  background: #e2e8f0;
  color: #0f172a;
}

.guide-top {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-right: 32px;
}

.guide-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #edf7f4;
  color: #267d68;
  font-size: 22px;
}

.guide-eyebrow {
  width: fit-content;
  margin-bottom: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  background: #edf7f4;
  color: #267d68;
  font-size: 12px;
  font-weight: 800;
}

.guide-dialog h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.guide-desc {
  margin: 20px 0 18px;
  color: #475569;
  font-size: 14px;
  line-height: 1.8;
}

.guide-options {
  display: grid;
  gap: 12px;
}

.guide-option {
  width: 100%;
  min-height: 72px;
  padding: 14px 16px;
  display: grid;
  grid-template-columns: 40px 1fr 20px;
  align-items: center;
  gap: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  color: #1e293b;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.guide-option:hover {
  border-color: #9ccfc0;
  background: #fbfefd;
}

.guide-option.primary {
  border-color: #9ccfc0;
  background: #fbfefd;
}

.guide-option-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #edf7f4;
  color: #267d68;
  font-size: 22px;
  font-weight: 700;
}

.guide-option-icon.list {
  background: #edf7f4;
  font-size: 18px;
}

.guide-option strong,
.guide-option small {
  display: block;
}

.guide-option strong {
  margin-bottom: 4px;
  color: #0f172a;
  font-size: 15px;
}

.guide-option small {
  color: #64748b;
  font-size: 12.5px;
}

.guide-option .el-icon {
  justify-self: end;
  color: #94a3b8;
}

.v-modal { background: rgba(15, 23, 42, 0.34); }
</style>
