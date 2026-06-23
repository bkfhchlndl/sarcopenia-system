<template>
  <div class="geriatric-entry">
    <section class="overview-panel">
      <div class="overview-main">
        <div class="overview-mark">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div>
          <div class="section-eyebrow">Geriatric Assessment</div>
          <h1>老年评估</h1>
          <p>围绕躯体功能、心理认知、营养用药与生活支持情况，选择适合当前患者的评估路径。</p>
        </div>
      </div>
      <div class="overview-action">
        <el-button class="back-link" plain @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上级
        </el-button>
      </div>
    </section>

    <section class="patient-strip" v-if="patientId">
      <div class="patient-avatar">
        <el-icon><User /></el-icon>
      </div>
      <div class="patient-info">
        <div class="patient-name">{{ patientName }}</div>
        <div class="patient-meta">
          <span v-if="medicalCaseNo">病历号：{{ medicalCaseNo }}</span>
          <span v-if="rowInfo?.gender">性别：{{ formatGender(rowInfo.gender) }}</span>
          <span v-if="rowInfo?.birthday">出生日期：{{ rowInfo.birthday }}</span>
        </div>
      </div>
      <span class="patient-state">待选择评估</span>
    </section>

    <section class="assessment-panel">
      <div class="panel-heading">
        <div>
          <h2>选择评估路径</h2>
          <p>综合评估用于完整筛查，自定义评估适合只补做或复查某一项。</p>
        </div>
      </div>

      <div v-if="hasAssessment" class="result-panel">
        <div class="result-card-hero">
          <div class="result-card-icon">
            <el-icon :size="22"><DocumentAdd /></el-icon>
          </div>
          <div class="result-card-body">
            <div class="result-card-title">
              已完成 {{ validRecords.length }} 项专项评估
            </div>
            <div class="result-card-sub">
              当前患者已有专项评估记录，可以直接进入综合评估页面查看结果并生成报告，也可以继续补做其他项目。
            </div>
          </div>
          <button class="result-card-btn" type="button" @click="goComprehensive">
            <span>查看结果并生成报告</span>
            <el-icon><ArrowRightBold /></el-icon>
          </button>
        </div>
      </div>

      <div class="path-list">
        <article
            v-for="opt in assessmentTypes"
            :key="opt.code"
            class="path-item"
            :style="{ '--accent-color': opt.color, '--accent-soft': opt.color + '12' }"
        >
          <div class="path-index">
            <span class="path-icon">{{ opt.icon }}</span>
          </div>

          <div class="path-content">
            <div class="path-title-row">
              <div>
                <h3>{{ opt.title }}</h3>
                <p>{{ opt.subtitle }}</p>
              </div>
              <div class="tag-row">
                <span v-for="tag in opt.tags" :key="tag" class="tag-item">{{ tag }}</span>
              </div>
            </div>
            <div class="path-desc">
              {{ opt.code === 'cga'
                ? '按老年综合评估流程完成多维度筛查，并在结束后生成综合评估结果。'
                : '从单项量表中按需选择，适合专项补充、复评或只生成已完成项目报告。' }}
            </div>
          </div>

          <button class="path-button" type="button" @click="selectOption(opt)">
            <span>开始评估</span>
            <el-icon><ArrowRightBold /></el-icon>
          </button>
        </article>
      </div>

      <div class="panel-note">
        <el-icon><InfoFilled /></el-icon>
        <span>评估结果会归档到当前患者名下，请确认患者信息无误后再开始。</span>
      </div>
    </section>

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
          当前检测评估页面没有收到患者ID，无法把评估结果保存到具体患者名下。请先新建患者，或从用户管理列表点击“进入测试”。
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
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {User, UserFilled, ArrowLeft, DataAnalysis, ArrowRightBold, InfoFilled, Close, DocumentAdd} from '@element-plus/icons-vue'
import {selectCgaRecordList} from '@/api/cga.js'

const route = useRoute()
const router = useRouter()
const patientDialogVisible = ref(false)
const cgaRecordList = ref([])
const recordLoading = ref(false)

const patientId = computed(() => route.query.patientId || route.query.id || '')
const patientName = computed(() => route.query.patientName || route.query.name || '患者')
const medicalCaseNo = computed(() => route.query.medicalCaseNo || route.query.caseNo || '')
const rowInfo = computed(() => ({
  gender: route.query.gender,
  age: route.query.age,
  birthday: route.query.birthday
}))

const validRecords = computed(() => (cgaRecordList.value || []).filter(r => String(r?.delFlag ?? '0') === '0'))
const hasAssessment = computed(() => validRecords.value.length > 0)

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
    route: {path: '/patient/detection/cga', paramsKeep: true}
  },
  {
    code: 'custom',
    title: '自定义单项评估',
    subtitle: 'Custom Single Assessment',
    icon: '✏️',
    color: '#8b5cf6',
    tags: ['自由选择', '单项评估', '灵活配置'],
    highlight: false,
    route: {path: '/patient/detection/customize'}
  }
]

/** 校验：必须传patientId才能进入评估 */
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
  const query = {...buildPatientQuery(), ...(opt.route?.queryMerge || {})}
  router.push({path: opt.route.path, query})
}

function goBack() {
  router.push({path: '/patient/detection/index', query: buildPatientQuery()})
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

async function loadCgaRecords() {
  if (!patientId.value) return
  recordLoading.value = true
  try {
    const res = await selectCgaRecordList({ patientId: patientId.value })
    cgaRecordList.value = Array.isArray(res) ? res : (res?.data || res?.result || [])
  } catch (e) {
    cgaRecordList.value = []
  } finally {
    recordLoading.value = false
  }
}

function goComprehensive() {
  if (!requirePatient()) return
  router.push({ path: '/patient/detection/comprehensive', query: buildPatientQuery() })
}

onMounted(() => {
  loadCgaRecords()
})
</script>

<style scoped lang="scss">
.geriatric-entry {
  min-height: calc(100vh - 84px);
  padding: 24px 24px 42px;
  background: #f4f7f8;
  box-sizing: border-box;
}

.overview-panel,
.patient-strip,
.assessment-panel {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.overview-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 26px 28px;
  border-radius: 8px;
  background: #113b43;
  border: 1px solid #0d3037;
  color: #ffffff;
}

.overview-main {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.overview-mark {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.13);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 25px;
}

.section-eyebrow {
  margin-bottom: 7px;
  color: #98d7cf;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.overview-panel h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.25;
  font-weight: 800;
}

.overview-panel p {
  max-width: 680px;
  margin: 8px 0 0;
  color: #d7e5e7;
  font-size: 14px;
  line-height: 1.7;
}

.overview-action {
  flex-shrink: 0;
}

.back-link {
  height: 38px;
  padding: 0 16px;
  border-radius: 8px;
  border-color: rgba(255, 255, 255, 0.34);
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-weight: 700;

  &:hover {
    color: #113b43;
    background: #ffffff;
    border-color: #ffffff;
  }
}

.patient-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 16px;
  padding: 16px 18px;
  background: #ffffff;
  border: 1px solid #d9e5e7;
  border-radius: 8px;
  box-shadow: 0 8px 22px rgba(31, 56, 66, 0.04);
}

.patient-avatar {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 8px;
  background: #e8f3f1;
  color: #20786e;
  font-size: 20px;
}

.patient-info {
  flex: 1;
  min-width: 0;
}

.patient-name {
  color: #182f35;
  font-size: 16px;
  font-weight: 800;
}

.patient-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  margin-top: 5px;
  color: #667b82;
  font-size: 13px;
}

.patient-state {
  flex-shrink: 0;
  padding: 5px 11px;
  border-radius: 6px;
  background: #fff7e6;
  color: #a15c07;
  font-size: 12px;
  font-weight: 800;
}

.assessment-panel {
  margin-top: 18px;
  padding: 24px 26px 26px;
  background: #ffffff;
  border: 1px solid #d9e5e7;
  border-radius: 8px;
  box-shadow: 0 10px 26px rgba(31, 56, 66, 0.05);
}

.panel-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid #e6eef0;
}

.panel-heading h2 {
  margin: 0;
  color: #172f36;
  font-size: 20px;
  font-weight: 800;
}

.panel-heading p {
  margin: 6px 0 0;
  color: #6b7f86;
  font-size: 13px;
}

.result-panel {
  margin-top: 18px;
}

.result-card-hero {
  display: grid;
  grid-template-columns: 48px 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  background: linear-gradient(135deg, #1f6b55 0%, #113b43 100%);
  border: 1px solid #0d3037;
  border-radius: 8px;
  color: #ffffff;
}

.result-card-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
}

.result-card-title {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 4px;
}

.result-card-sub {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.6;
}

.result-card-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: #ffffff;
  color: #113b43;
  font-weight: 600;
  font-size: 13px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.18s ease, transform 0.15s ease;

  &:hover {
    background: #eef5f4;
    transform: translateY(-1px);
  }

  .el-icon {
    font-size: 14px;
  }
}

.path-list {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.path-item {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  align-items: center;
  gap: 18px;
  min-height: 128px;
  padding: 20px;
  border: 1px solid #dbe7ea;
  border-left: 5px solid var(--accent-color);
  border-radius: 8px;
  background: #fbfcfc;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;

  &:hover {
    background: #ffffff;
    border-color: #bdd5da;
    border-left-color: var(--accent-color);
    box-shadow: 0 8px 20px rgba(31, 56, 66, 0.06);
  }
}

.path-index {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--accent-soft);
  color: var(--accent-color);
}

.path-icon {
  font-size: 24px;
  line-height: 1;
}

.path-content {
  min-width: 0;
}

.path-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.path-title-row h3 {
  margin: 0;
  color: #102a31;
  font-size: 18px;
  font-weight: 800;
}

.path-title-row p {
  margin: 5px 0 0;
  color: #8a9aa0;
  font-size: 12px;
  letter-spacing: 0;
}

.path-desc {
  margin-top: 12px;
  color: #4f646b;
  font-size: 13.5px;
  line-height: 1.75;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
  max-width: 360px;
}

.tag-item {
  padding: 4px 9px;
  border-radius: 6px;
  background: #eef5f6;
  color: #536970;
  font-size: 12px;
  font-weight: 700;
}

.path-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 122px;
  height: 40px;
  padding: 0 18px;
  border-radius: 8px;
  border: 1px solid var(--accent-color);
  background: var(--accent-color);
  color: #ffffff;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  transition: opacity 0.18s ease, transform 0.18s ease;

  &:hover {
    opacity: 0.88;
  }
}

.panel-note {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f3f8f8;
  color: #637980;
  font-size: 12.5px;

  .el-icon {
    color: #20786e;
    flex-shrink: 0;
  }
}

@media (max-width: 900px) {
  .geriatric-entry {
    padding: 16px 12px 30px;
  }

  .overview-panel,
  .patient-strip,
  .assessment-panel {
    max-width: none;
  }

  .overview-panel {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px 18px;
  }

  .overview-action,
  .back-link {
    width: 100%;
  }

  .path-item {
    grid-template-columns: 46px 1fr;
    align-items: flex-start;
  }

  .result-card-hero {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 18px;
  }

  .result-card-icon {
    width: 44px;
    height: 44px;
  }

  .result-card-btn {
    width: 100%;
    justify-content: center;
  }

  .path-button {
    grid-column: 2;
    width: fit-content;
  }

  .path-title-row {
    display: block;
  }

  .tag-row {
    justify-content: flex-start;
    margin-top: 10px;
    max-width: none;
  }

  .patient-strip {
    align-items: flex-start;
  }

  .patient-state {
    display: none;
  }
}

@media (max-width: 520px) {
  .overview-main {
    align-items: flex-start;
  }

  .overview-mark {
    width: 46px;
    height: 46px;
  }

  .overview-panel h1 {
    font-size: 22px;
  }

  .assessment-panel {
    padding: 20px 16px;
  }

  .path-item {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 18px 16px;
  }

  .path-button {
    grid-column: auto;
    width: 100%;
  }

  .patient-strip {
    flex-wrap: wrap;
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

.v-modal {
  background: rgba(15, 23, 42, 0.34);
}
</style>
