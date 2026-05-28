<template>
  <div class="app-container report-page">
    <el-form :model="queryParams" inline class="query-form">
      <el-form-item label="用户ID">
        <el-input
            v-model="queryParams.caseNo"
            clearable
            placeholder="请输入用户ID"
            @keyup.enter="getList"
        />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入姓名"
            @keyup.enter="getList"
        />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input
            v-model="queryParams.phone"
            clearable
            placeholder="请输入手机号"
            @keyup.enter="getList"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" :loading="loading" @click="getList">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="patientList" border>
      <el-table-column type="index" label="序号" width="70" align="center" />
      <el-table-column prop="caseNo" label="用户ID" min-width="130" />
      <el-table-column prop="name" label="姓名" min-width="110" />
      <el-table-column label="性别" width="90" align="center">
        <template #default="{ row }">{{ formatGender(row.gender) }}</template>
      </el-table-column>
      <el-table-column label="出生日期" width="130" align="center">
        <template #default="{ row }">{{ formatDate(row.birthday) }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="address" label="家庭住址" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="210" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link :icon="View" @click="handleView(row)">查看报告</el-button>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除报告</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="reportVisible" size="80%" title="老年综合评估报告" class="report-drawer">
      <template #header="{ titleId, titleClass }">
        <div class="drawer-header no-print">
          <h3 :id="titleId" :class="titleClass">老年综合评估报告</h3>
          <el-button :icon="Printer" :disabled="!report" @click="printReport">打印</el-button>
        </div>
      </template>

      <el-skeleton v-if="reportLoading" :rows="10" animated />
      <el-empty v-else-if="!report" description="暂无报告数据" />

      <main v-else class="paper">
        <header class="report-header">
          <div>
            <h1>X-Age</h1>
            <span></span>
            <h2>老年综合评估报告</h2>
          </div>
        </header>

        <section class="identity-grid">
          <div>
            <label>用户ID</label>
            <strong>{{ report.caseNo || report.patientId || '-' }}</strong>
          </div>
          <div>
            <label>姓名</label>
            <strong>{{ report.patientName || '-' }}</strong>
          </div>
          <div>
            <label>性别</label>
            <strong>{{ formatGender(report.gender) }}</strong>
          </div>
          <div>
            <label>年龄</label>
            <strong>{{ report.age || '-' }}岁</strong>
          </div>
          <div>
            <label>身高</label>
            <strong>{{ formatHeight(report.height) }}</strong>
          </div>
          <div>
            <label>评估时间</label>
            <strong>{{ formatDate(report.createTime) }}</strong>
          </div>
        </section>

        <section class="overview">
          <div class="basic-info">
            <h3>基本情况</h3>
            <dl>
              <dt>病史记录</dt>
              <dd>{{ report.result || '-' }}</dd>
              <dt>用药评估</dt>
              <dd>{{ report.suggest || '-' }}</dd>
              <dt>家庭住址</dt>
              <dd>{{ report.address || '-' }}</dd>
            </dl>
          </div>

          <div class="score-card">
            <strong>{{ report.totalScore || 0 }}</strong>
            <span>/ 总分</span>
          </div>
        </section>

        <section class="problem-section">
          <h3>现存问题</h3>
          <div v-if="problemTags.length" class="tag-list">
            <span v-for="tag in problemTags" :key="tag">{{ tag }}</span>
          </div>
          <p v-else class="muted-text">暂无明显异常问题</p>
        </section>

        <section class="assessment-section">
          <h3>综合评估结果</h3>
          <table>
            <thead>
            <tr>
              <th>评估内容</th>
              <th>评估结果</th>
              <th>正常范围</th>
              <th>提示</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(item, index) in assessmentList" :key="item.scaleId">
              <td>{{ index + 1 }}. {{ item.scaleName }}</td>
              <td>{{ formatScore(item.itemScore) }}</td>
              <td>{{ item.normalRange || '-' }}</td>
              <td>{{ item.tip || '-' }}</td>
            </tr>
            </tbody>
          </table>
        </section>
      </main>
    </el-drawer>
  </div>
</template>

<script setup name="CagReport">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Printer, Refresh, Search, View } from '@element-plus/icons-vue'
import {
  deleteReportByPatientId,
  selectReportedPatientList,
  getFullCagReport
} from '@/api/cagReport'
import { parseTime } from '@/utils/ruoyi'

/** 列表加载状态 */
const loading = ref(false)
/** 报告加载状态 */
const reportLoading = ref(false)
/** 报告抽屉显示状态 */
const reportVisible = ref(false)
/** 已评估患者列表 */
const patientList = ref([])
/** 报告详情数据 */
const report = ref(null)

/** 查询条件 */
const queryParams = reactive({
  caseNo: '',
  name: '',
  phone: ''
})

/** 评估项目列表 */
const assessmentList = computed(() => report.value?.assessmentList || [])

/** 异常问题判断正则 */
const abnormalText = /正常|阴性|良好|无/

/** 异常问题标签（自动筛选） */
const problemTags = computed(() => {
  return assessmentList.value
      .filter(item => item.tip && !abnormalText.test(item.tip))
      .map(item => item.tip)
})

// ===================== 格式化工具 =====================
/** 性别格式化 */
function formatGender(gender) {
  if (gender === '1' || gender === 1) return '男'
  if (gender === '2' || gender === 2) return '女'
  return '-'
}

/** 日期格式化 */
function formatDate(value) {
  if (!value) return '-'
  if (typeof value === 'string') return value.slice(0, 10)
  return parseTime(value, '{y}-{m}-{d}') || '-'
}

/** 身高格式化 */
function formatHeight(value) {
  return value || value === 0 ? `${value}cm` : '-'
}

/** 分数格式化 */
function formatScore(value) {
  return value || value === 0 ? `${value}分` : '-'
}

// ===================== 列表查询 =====================
/** 获取已评估患者列表 */
async function getList() {
  loading.value = true
  try {
    const res = await selectReportedPatientList(queryParams)
    patientList.value = res.data || []
  } finally {
    loading.value = false
  }
}

/** 重置查询条件 */
function resetQuery() {
  queryParams.caseNo = ''
  queryParams.name = ''
  queryParams.phone = ''
  getList()
}

// ===================== 报告操作 =====================
/** 查看报告详情 */
async function handleView(row) {
  reportLoading.value = true
  reportVisible.value = true
  report.value = null
  try {
    const res = await getFullCagReport(row.id)
    report.value = res.data || null
  } finally {
    reportLoading.value = false
  }
}

/** 删除报告 */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除 ${row.name || '该患者'} 的报告吗？`, '提示', {
    type: 'warning'
  })
  await deleteReportByPatientId(row.id)
  ElMessage.success('删除成功')
  getList()
}

/** 打印报告 */
function printReport() {
  window.print()
}

/** 页面初始化 */
onMounted(getList)
</script>

<style scoped lang="scss">
.report-page {
  min-height: calc(100vh - 84px);
}

.query-form {
  margin-bottom: 12px;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.drawer-header h3 {
  margin: 0;
}

.paper {
  width: min(1120px, 100%);
  min-height: 1480px;
  margin: 0 auto;
  padding: 48px 56px 64px;
  background: #fff;
  color: #30343b;
}

.report-header {
  padding-bottom: 18px;
  border-bottom: 3px solid #555b63;
}

.report-header > div {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.report-header h1 {
  margin: 0;
  color: #5b6068;
  font-size: 34px;
  font-weight: 800;
}

.report-header span {
  width: 2px;
  height: 22px;
  background: #6b7078;
}

.report-header h2 {
  margin: 0;
  color: #30343b;
  font-size: 22px;
}

.identity-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 22px;
  padding: 22px 0 18px;
}

.identity-grid label {
  display: block;
  margin-bottom: 6px;
  color: #6f7782;
  font-size: 13px;
}

.identity-grid strong {
  color: #3d424a;
  font-size: 15px;
}

.overview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 32px;
  align-items: center;
  margin-top: 8px;
}

.paper h3 {
  margin: 0 0 16px;
  color: #3b414a;
  font-size: 18px;
}

dl {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  margin: 0;
}

dt,
dd {
  min-height: 48px;
  margin: 0;
  padding: 14px 16px;
  border-bottom: 1px solid #edf0f3;
  font-size: 14px;
}

dt {
  background: #f4f5f7;
  color: #59616d;
  font-weight: 700;
}

dd {
  color: #333941;
}

.score-card {
  text-align: center;
  color: #747b84;
}

.score-card strong {
  display: block;
  color: #505762;
  font-size: 72px;
  line-height: 1;
}

.score-card span {
  display: block;
  margin-top: 8px;
  font-size: 13px;
}

.problem-section {
  margin-top: 48px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 26px;
}

.tag-list span {
  color: #6c3333;
  font-size: 15px;
}

.tag-list span::before {
  content: "*";
  margin-right: 6px;
  color: #b84d59;
}

.muted-text {
  color: #78818d;
}

.assessment-section {
  margin-top: 56px;
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th {
  padding: 14px 16px;
  background: #edf0f3;
  color: #59616d;
  font-size: 14px;
  text-align: left;
}

td {
  padding: 15px 16px;
  border-bottom: 1px solid #e9edf1;
  color: #343a43;
  font-size: 15px;
  line-height: 1.5;
}

th:nth-child(1),
td:nth-child(1) {
  width: 36%;
}

th:nth-child(2),
td:nth-child(2),
th:nth-child(3),
td:nth-child(3) {
  width: 16%;
  text-align: center;
}

@media (max-width: 900px) {
  .paper {
    padding: 28px 20px 40px;
  }

  .identity-grid,
  .overview {
    grid-template-columns: 1fr 1fr;
  }

  .score-card {
    grid-column: 1 / -1;
  }

  table {
    min-width: 820px;
  }

  .assessment-section {
    overflow-x: auto;
  }
}

@media print {
  .no-print,
  :global(.el-overlay),
  :global(.navbar),
  :global(.tags-view-container),
  :global(.sidebar-container) {
    display: none !important;
  }

  .paper {
    width: 100%;
    min-height: auto;
    padding: 0;
  }
}
</style>