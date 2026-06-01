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

    <el-dialog
      v-model="reportVisible"
      title="老年综合评估报告"
      width="1200px"
      top="5vh"
      :close-on-click-modal="false"
      class="report-dialog"
      append-to-body
    >
      <div class="dialog-content">
        <el-skeleton v-if="reportLoading" :rows="10" animated />
        <el-empty v-else-if="!report" description="暂无报告数据" />

        <main v-else ref="reportContentRef" class="paper">
          <!-- 加个外层容器，统一控制边距 -->
          <div class="report-inner">
            <header class="report-header">
              <div>
                <h2>老年综合评估报告</h2>
              </div>
            </header>

            <section class="identity-grid">
              <div>
                <label>病历号</label>
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
                <strong>{{ formatDate(report.updateTime) }}</strong>
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
                  <td>{{ formatScore(item) }}</td>
                  <td>{{ item.normalRange || '-' }}</td>
                  <td>{{ item.tip || '-' }}</td>
                </tr>
                </tbody>
              </table>
            </section>
          </div>
        </main>
      </div>

      <template #footer>
        <div class="dialog-footer no-print">
          <el-button @click="reportVisible = false">关闭</el-button>
          <el-button type="success" :icon="Download" :loading="exportLoading" :disabled="!report" @click="exportPDF">导出PDF</el-button>
          <el-button type="primary" :icon="Printer" :disabled="!report" @click="printReport">打印</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CgaReport">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Printer, Refresh, Search, View } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { deleteReportByPatientId, getFullCgaReportByPatient } from '@/api/cgaReport'
import { selectReportedPatientList } from '@/api/patient.js'
import { parseTime } from '@/utils/ruoyi'

/** 列表加载状态 */
const loading = ref(false)
/** 报告加载状态 */
const reportLoading = ref(false)
/** 导出加载状态 */
const exportLoading = ref(false)
/** 报告抽屉显示状态 */
const reportVisible = ref(false)
/** 已评估患者列表 */
const patientList = ref([])
/** 报告详情数据 */
const report = ref(null)
/** 报告内容引用 */
const reportContentRef = ref(null)

/** 查询条件 */
const queryParams = reactive({
  caseNo: '',
  name: '',
  phone: ''
})

/** 评估项目列表 */
const assessmentList = computed(() => report.value?.assessmentList || [])

/** 异常提示映射为简短标签 */
const problemTagMap = {
  '建议进行体能状况评估': '体能下降',
  '应进一步到眼科评估': '视力下降',
  '应进一步到耳鼻喉科评估': '听力下降',
  '建议进行认知功能评估': '认知障碍风险',
  '得分越低，风险越大': '居家环境风险',
  '衰弱': '衰弱',
  '重度用药': '多重用药',
  '建议进行营养不良评估及诊断': '营养不良风险',
  '视功能较差': '视功能下降',
  '视功能差': '视功能严重下降',
  '建议进行抑郁评估': '抑郁风险',
  '建议进行焦虑评估': '焦虑风险',
  '建议进行跌倒风险评估': '跌倒高风险',
  '建议进行尿失禁评估': '尿失禁风险',
  '建议进行便秘评估': '便秘',
  '建议进行睡眠障碍评估': '睡眠障碍',
  '建议进行慢性疼痛评估': '慢性疼痛',
  '建议进行压力性损伤风险评估': '压力性损伤风险',
  '建议进行吞咽障碍评估': '吞咽障碍风险',
  '建议进行肌少症评估': '肌少症风险',
  '建议进行营养不良评估': '营养不良风险',
  '可能有睡眠障碍': '睡眠障碍',
}

/** 异常问题标签（自动筛选并映射为短标签） */
const problemTags = computed(() => {
  const tags = new Set()
  assessmentList.value.forEach(item => {
    if (!item.tip) return

    // 匹配映射表
    for (const [key, value] of Object.entries(problemTagMap)) {
      if (item.tip.includes(key)) {
        tags.add(value)
        return
      }
    }

    // 没匹配到的，如果包含"建议"或"风险"也加入
    if (item.tip.includes('建议') || item.tip.includes('风险') || item.tip.includes('阳性')) {
      const shortTip = item.tip.replace(/建议进行|评估|WS\/T\d+\s*[A-Z]\d?/g, '').trim()
      tags.add(shortTip || '需关注')
    }
  })
  return Array.from(tags)
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

/** 分数格式化：筛查项显示阴阳，评分项显示分数 */
function formatScore(item) {
  // 这些项目显示 阴性/阳性
  const yinYangList = [
    'exercise',
    'depression',
    'anxiety',
    'fall',
    'incontinence',
    'constipation',
    'sleep',
    'chronic',
    'pressure',
    'swallow',
    'sarcopenia',
    'delirium'
  ]

  // 如果是阴阳项目
  if (yinYangList.includes(item.scaleCode)) {
    return item.itemScore === 0 ? '阳性' : '阴性'
  }

  // 其他显示分数（后端已返回"X分"格式，直接显示）
  return item.itemScore != null ? item.itemScore : '-'
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
    const res = await getFullCgaReportByPatient(row.id)
    report.value = res.data || null
  } catch (error) {
    report.value = null
    ElMessage.error(error.message || '报告查询失败')
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

function getReportElement() {
  if (!report.value || !reportContentRef.value) {
    ElMessage.warning('报告内容未加载')
    return null
  }
  return reportContentRef.value
}

function getReportFileName() {
  const name = report.value?.patientName || report.value?.caseNo || 'report'
  return `老年综合评估报告_${name}_${formatDate(new Date())}`
}

function getPrintStyle() {
  return `
    @page {
      size: A4;
      margin: 8mm 9mm;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      background: #fff;
      color: #30343b;
      font-family: "Microsoft YaHei", "PingFang SC", "Helvetica Neue", Arial, sans-serif;
      line-height: 1.35;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
    .paper {
      width: auto;
      max-width: 192mm;
      background: #fff;
      margin: 0 auto;
      padding: 0;
    }
    .report-inner {
      width: 100%;
      margin: 0;
    }
    .report-header {
      padding-bottom: 10px;
      border-bottom: 2px solid #555b63;
      text-align: center;
    }
    .report-header h2 {
      margin: 0;
      color: #30343b;
      font-size: 22px;
      font-weight: 600;
    }
    .identity-grid {
      display: grid;
      grid-template-columns: repeat(6, minmax(0, 1fr));
      gap: 6px 14px;
      padding: 12px 0 8px;
    }
    .identity-grid label {
      display: block;
      margin-bottom: 2px;
      color: #6f7782;
      font-size: 11px;
    }
    .identity-grid strong {
      color: #3d424a;
      font-size: 13px;
    }
    .overview {
      display: grid;
      grid-template-columns: minmax(0, 1fr) 92px;
      gap: 16px;
      align-items: center;
      margin-top: 8px;
    }
    h3 {
      margin: 0 0 8px;
      color: #3b414a;
      font-size: 15px;
      font-weight: 600;
    }
    dl {
      display: grid;
      grid-template-columns: 75px minmax(0, 1fr);
      margin: 0;
    }
    dt,
    dd {
      min-height: 23px;
      margin: 0;
      padding: 4px 8px;
      border-bottom: 1px solid #edf0f3;
      font-size: 12px;
      line-height: 1.35;
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
      font-size: 34px;
      line-height: 1;
    }
    .score-card span {
      display: block;
      margin-top: 2px;
      font-size: 12px;
    }
    .problem-section {
      margin-top: 14px;
    }
    .tag-list {
      display: flex;
      flex-wrap: wrap;
      gap: 7px 9px;
    }
    .tag-list span {
      display: inline-block;
      padding: 3px 8px;
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 14px;
      color: #991b1b;
      font-size: 11px;
      font-weight: 500;
    }
    .muted-text {
      color: #78818d;
      font-size: 12px;
    }
    .assessment-section {
      margin-top: 14px;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      table-layout: fixed;
      font-size: 11px;
    }
    thead { display: table-header-group; }
    th {
      padding: 6px 8px;
      background: #edf0f3;
      color: #59616d;
      text-align: left;
      font-size: 11px;
      font-weight: 600;
    }
    td {
      padding: 5px 8px;
      border-bottom: 1px solid #e9edf1;
      color: #343a43;
      line-height: 1.28;
    }
    th:nth-child(1), td:nth-child(1) { width: 26%; }
    th:nth-child(2), td:nth-child(2) { width: 10%; }
    th:nth-child(3), td:nth-child(3) { width: 14%; }
    th:nth-child(4), td:nth-child(4) { width: 50%; }
    tr,
    .overview,
    .problem-section {
      break-inside: avoid;
      page-break-inside: avoid;
    }
  `
}

function getPdfStyle() {
  return `
    ${getPrintStyle()}
    @page { size: A4; margin: 0; }
    .paper {
      width: 210mm;
      max-width: 210mm;
      margin: 0;
      padding: 10mm 10mm 8mm;
    }
    th:nth-child(1), td:nth-child(1) { width: 28%; }
    th:nth-child(2), td:nth-child(2) { width: 10%; }
    th:nth-child(3), td:nth-child(3) { width: 18%; }
    th:nth-child(4), td:nth-child(4) { width: 44%; }
  `
}

/** 打印报告 */
function printReport() {
  const reportElement = getReportElement()
  if (!reportElement) return

  const printWindow = window.open('', '_blank', 'width=980,height=720')
  if (!printWindow) {
    ElMessage.warning('请允许打开新窗口')
    return
  }

  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8">
        <title>${getReportFileName()}</title>
        <style>${getPrintStyle()}</style>
      </head>
      <body>${reportElement.outerHTML}</body>
    </html>
  `)
  printWindow.document.close()

  printWindow.onload = () => {
    printWindow.focus()
    printWindow.print()
  }
}

/** 导出PDF（单页A4，字体清晰版） */
async function exportPDF() {
  const reportElement = getReportElement()
  if (!reportElement) return

  exportLoading.value = true
  const renderContainer = document.createElement('div')
  try {
    renderContainer.style.position = 'fixed'
    renderContainer.style.left = '-10000px'
    renderContainer.style.top = '0'
    renderContainer.style.width = '210mm'
    renderContainer.style.background = '#fff'
    renderContainer.innerHTML = `<style>${getPdfStyle()}</style>${reportElement.outerHTML}`
    document.body.appendChild(renderContainer)

    const renderElement = renderContainer.querySelector('.paper') || reportElement

    const canvas = await html2canvas(renderElement, {
      backgroundColor: '#ffffff',
      scale: 3,
      useCORS: true,
      logging: false,
      width: renderElement.scrollWidth,
      height: renderElement.scrollHeight,
      imageTimeout: 0,
      removeContainer: true
    })

    const pdf = new jsPDF('p', 'mm', 'a4')
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const pdfMarginX = 7
    const pdfMarginTop = 8
    const pdfMarginBottom = 7
    const availableWidth = pageWidth - pdfMarginX * 2
    const availableHeight = pageHeight - pdfMarginTop - pdfMarginBottom

    const scale = Math.min(availableWidth / canvas.width, availableHeight / canvas.height)
    const imgWidth = canvas.width * scale
    const imgHeight = canvas.height * scale
    const offsetX = pdfMarginX + (availableWidth - imgWidth) / 2
    const offsetY = pdfMarginTop + (availableHeight - imgHeight) / 2

    pdf.addImage(
        canvas.toDataURL('image/png'),
        'PNG',
        offsetX,
        offsetY,
        imgWidth,
        imgHeight
    )

    pdf.save(`${getReportFileName()}.pdf`)
  } catch (error) {
    ElMessage.error(error.message || 'PDF导出失败')
  } finally {
    renderContainer.remove()
    exportLoading.value = false
  }
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

.dialog-content {
  max-height: 80vh;
  overflow-y: auto;
  padding: 0 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.paper {
  width: 210mm;
  min-height: 297mm;
  background: #fff;
  margin: 0 auto;
  padding: 0;
}

.report-inner {
  width: 190mm;
  margin: 10px auto;
}

.report-header {
  padding-bottom: 12px;
  border-bottom: 3px solid #555b63;
}

.report-header h2 {
  margin: 0;
  color: #30343b;
  font-size: 20px;
}

.identity-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  padding: 16px 0 12px;
}

.identity-grid label {
  display: block;
  margin-bottom: 4px;
  color: #6f7782;
  font-size: 12px;
}

.identity-grid strong {
  color: #3d424a;
  font-size: 14px;
}

.overview {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 24px;
  align-items: center;
  margin-top: 8px;
}

.paper h3 {
  margin: 0 0 12px;
  color: #3b414a;
  font-size: 16px;
}

dl {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr);
  margin: 0;
}

dt,
dd {
  min-height: 40px;
  margin: 0;
  padding: 10px 12px;
  border-bottom: 1px solid #edf0f3;
  font-size: 13px;
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
  font-size: 60px;
  line-height: 1;
}

.score-card span {
  display: block;
  margin-top: 6px;
  font-size: 12px;
}

.problem-section {
  margin-top: 36px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-list span {
  display: inline-block;
  padding: 6px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 16px;
  color: #991b1b;
  font-size: 13px;
  font-weight: 500;
}

.assessment-section {
  margin-top: 40px;
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th {
  padding: 10px 12px;
  background: #edf0f3;
  color: #59616d;
  font-size: 13px;
  text-align: left;
}

td {
  padding: 10px 12px;
  border-bottom: 1px solid #e9edf1;
  color: #343a43;
  font-size: 14px;
  line-height: 1.4;
}

th:nth-child(1),
td:nth-child(1) {
  width: 23%;
}

th:nth-child(2),
td:nth-child(2) {
  width: 13%;
}

th:nth-child(3),
td:nth-child(3) {
  width: 20%;
}

th:nth-child(4),
td:nth-child(4) {
  width: 20%;
}

@media (max-width: 900px) {
  .paper {
    padding: 20px 16px 30px;
  }

  .report-inner {
    width: 100%;
    margin: 0;
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
    width: 210mm !important;
    height: auto !important;
    margin: 0 !important;
    padding: 0 !important;
  }
}
</style>
