<template>
  <div class="report-page">
    <!-- 顶部标题栏 -->
    <div class="hero-bar">
      <div class="hero-left">
        <div class="hero-icon">
          <el-icon :size="26"><Document /></el-icon>
        </div>
        <div>
          <h1>老年综合评估报告</h1>
          <p>共 <b>{{ patientList.length }}</b> 份报告 · 支持查看、打印和 PDF 导出</p>
        </div>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="filter-card">
      <el-form :model="queryParams" inline label-width="68px" class="filter-form">
        <el-form-item label="姓名">
          <el-input
              v-model="queryParams.name"
              placeholder="请输入姓名"
              clearable
              style="width: 160px"
              @keyup.enter="getList"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
              v-model="queryParams.phone"
              placeholder="请输入手机号"
              clearable
              style="width: 180px"
              @keyup.enter="getList"
          />
        </el-form-item>
        <el-form-item>
          <el-button plain @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            <span>重置</span>
          </el-button>
          <el-button type="primary" :loading="loading" @click="getList">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 报告列表表格 -->
    <div class="table-card">
      <div v-loading="loading" class="table-shell">
        <el-table
            :data="patientList"
            stripe
            border
            row-key="id"
            :header-cell-style="headerStyle"
            :cell-style="cellStyle"
            class="report-list-table"
        >
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="姓名" min-width="110" align="center">
            <template #default="{ row }">
              <div class="name-cell">{{ row.name || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="性别" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="String(row.gender) === '2' ? 'danger' : 'primary'" effect="light" size="small">
                {{ formatGender(row.gender) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="出生日期" min-width="145" align="center">
            <template #default="{ row }">{{ formatDate(row.birthday) }}</template>
          </el-table-column>
          <el-table-column label="身份证" min-width="190" align="center" show-overflow-tooltip>
            <template #default="{ row }">{{ row.idCard || '-' }}</template>
          </el-table-column>
          <el-table-column label="手机号" min-width="135" align="center">
            <template #default="{ row }">{{ row.phone || '-' }}</template>
          </el-table-column>
          <el-table-column label="评估时间" min-width="170" align="center">
            <template #default="{ row }">{{ formatDateTime(row.updateTime || row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" align="center" min-width="210">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                <span>查看报告</span>
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                <span>删除报告</span>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && patientList.length === 0" description="暂无报告数据" />
      </div>
    </div>

    <!-- 报告详情弹窗 -->
    <el-dialog
        v-model="reportVisible"
        title="老年综合评估报告"
        width="1120px"
        top="3vh"
        :close-on-click-modal="false"
        class="report-dialog"
        append-to-body
        destroy-on-close
    >
      <div class="dialog-content">
        <el-skeleton v-if="reportLoading" :rows="10" animated />
        <el-empty v-else-if="!report" description="暂无报告数据" />

        <!-- A4 报告内容 -->
        <main v-else ref="reportContentRef" class="report-paper">
          <header class="paper-header">
            <div class="hospital-line">
              <span>老年综合评估中心</span>
              <span>{{ reportDate }}</span>
            </div>
            <h1>老年综合评估报告单</h1>
            <p>Comprehensive Geriatric Assessment Report</p>
          </header>

          <section class="identity-strip">
            <div class="identity-cell">
              <span>姓名</span>
              <strong>{{ report.patientName || report.name || '-' }}</strong>
            </div>
            <div class="identity-cell">
              <span>性别</span>
              <strong>{{ formatGender(report.gender) }}</strong>
            </div>
            <div class="identity-cell">
              <span>年龄</span>
              <strong>{{ displayAge }}</strong>
            </div>
            <div class="identity-cell">
              <span>身高</span>
              <strong>{{ formatHeight(report.height) }}</strong>
            </div>
            <div class="identity-cell">
              <span>体重</span>
              <strong>{{ formatWeight(report.weight) }}</strong>
            </div>
            <div class="identity-cell wide">
              <span>评估时间</span>
              <strong>{{ reportDate }}</strong>
            </div>
          </section>

          <section class="report-section">
            <h2>基本情况</h2>
            <dl class="clinical-list">
              <div>
                <dt>生活情况</dt>
                <dd>{{ report.liveSituation || '暂无生活情况记录' }}</dd>
              </div>
              <div>
                <dt>照顾情况</dt>
                <dd>{{ report.careSituation || '暂无照顾情况记录' }}</dd>
              </div>
              <div>
                <dt>医疗支付</dt>
                <dd>{{ report.payType || '暂无医疗费用支付方式记录' }}</dd>
              </div>
              <div>
                <dt>经济来源</dt>
                <dd>{{ report.incomeSource || '暂无经济来源记录' }}</dd>
              </div>
            </dl>
          </section>

          <section class="report-section">
            <h2>现存问题</h2>
            <div v-if="problemTags.length" class="problem-tags">
              <span v-for="tag in problemTags" :key="tag" class="problem-tag">
                <i>!</i>{{ tag }}
              </span>
            </div>
            <div v-else class="empty-problem">暂无明显异常问题</div>
          </section>

          <section class="report-section">
            <h2>综合评估结果</h2>
            <table class="assessment-table">
              <thead>
              <tr>
                <th class="col-name">评估内容</th>
                <th class="col-result">评估结果</th>
                <th class="col-normal">正常范围</th>
                <th class="col-tip">提示</th>
              </tr>
              </thead>
              <tbody>
              <tr
                  v-for="(item, index) in assessmentRows"
                  :key="item.scaleCode || index"
                  :class="'row-' + getResultType(item)"
              >
                <td class="item-name">{{ index + 1 }}. {{ item._displayName }}</td>
                <td>
                  <span class="result-pill" :class="getResultType(item)">{{ formatResult(item) }}</span>
                </td>
                <td>{{ formatNormalRange(item) }}</td>
                <td>{{ formatTip(item) }}</td>
              </tr>
              </tbody>
            </table>
          </section>

          <section class="report-section">
            <h2>综合建议</h2>
            <p class="conclusion-text">{{ reportConclusion }}</p>
          </section>

          <section class="signature-section">
            <div><span>评估人员签名：</span><b></b></div>
            <div><span>审核医师签名：</span><b></b></div>
            <div><span>评估日期：</span><b>{{ reportDate }}</b></div>
          </section>
        </main>
      </div>

      <template #footer>
        <div class="dialog-footer no-print">
          <el-button @click="reportVisible = false">关闭</el-button>
          <el-button type="success" :icon="Download" :loading="exportLoading" :disabled="!report" @click="exportPDF">
            导出PDF
          </el-button>
          <el-button type="primary" :icon="Printer" :disabled="!report" @click="printReport">打印</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CgaReport">
import { computed, onActivated, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Document, Download, Printer, Refresh, Search, View } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

import { deleteReportByPatientId, getFullCgaReportByPatient } from '@/api/cgaReport'
import { selectReportedPatientList } from '@/api/patient.js'
import { parseTime } from '@/utils/ruoyi'

const route = useRoute()

// ==================== 状态变量 ====================
const loading = ref(false)
const reportLoading = ref(false)
const exportLoading = ref(false)
const reportVisible = ref(false)
const reportContentRef = ref(null)
const lastOpenedPatientId = ref('')

const patientList = ref([])
const report = ref(null)

// 查询参数
const queryParams = reactive({
  name: '',
  phone: ''
})

// ==================== 常量映射 ====================
// 量表编码-名称映射
const scaleNameMap = {
  basic_adl: '基本日常生活活动能力',
  instrumental_adl: '工具性日常生活活动能力',
  exercise_function: '运动功能',
  vision_assessment: '视力视觉功能',
  hearing_assessment: '听力',
  home_environment: '居家环境',
  frail_assessment: '衰弱',
  incontinence_screen: '尿失禁',
  constipation_screen: '便秘',
  chronic_pain_screen: '慢性疼痛',
  pressure_injury_risk: '压力性损伤风险',
  water_swallowing_test: '吞咽功能',
  nutrition_risk_screen: '营养风险',
  fall_risk_screen: '跌倒风险',
  polypharmacy_assessment: '多重用药',
  sarcopenia_screen: '微营养',
  cognition_screen: '认知功能',
  sleep_disorder_screen: '睡眠障碍',
  delirium_assessment: '谵妄',
  depression_screen: '抑郁',
  anxiety_screen: '焦虑'
}

// 量表默认正常范围映射
const defaultNormalRangeMap = {
  basic_adl: '无依赖',
  instrumental_adl: '无依赖',
  exercise_function: '运动功能正常',
  vision_assessment: '筛查阴性',
  hearing_assessment: '筛查阴性',
  home_environment: '无明显风险',
  frail_assessment: '无衰弱',
  incontinence_screen: '阴性',
  constipation_screen: '阴性',
  chronic_pain_screen: '阴性',
  pressure_injury_risk: '无风险',
  water_swallowing_test: '阴性',
  nutrition_risk_screen: '无营养风险',
  fall_risk_screen: '无跌倒风险',
  polypharmacy_assessment: '无多重用药',
  sarcopenia_screen: '阴性',
  cognition_screen: '阴性',
  sleep_disorder_screen: '阴性',
  delirium_assessment: '阴性',
  depression_screen: '阴性',
  anxiety_screen: '阴性'
}

// ==================== 计算属性 ====================
// 报告日期
const reportDate = computed(() => {
  return formatDate(report.value?.createTime || report.value?.updateTime)
})

// 展示年龄
const displayAge = computed(() => {
  // 优先取接口返回的年龄
  if (report.value?.age || report.value?.age === 0) {
    return `${report.value.age}岁`
  }
  // 根据出生日期计算
  const birthday = report.value?.birthday
  if (!birthday) return '-'
  const birth = new Date(birthday)
  if (Number.isNaN(birth.getTime())) return '-'

  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return `${Math.max(age, 0)}岁`
})

// 评估项列表（过滤未作答项）
const assessmentRows = computed(() => {
  return (report.value?.assessmentList || [])
      .map(buildItem)
      .filter(item => item._answered)
})

// 已完成评估项数
const completedCount = computed(() => assessmentRows.value.length)

// 异常项数
const abnormalCount = computed(() => {
  return assessmentRows.value.filter(item => getResultType(item) === 'positive').length
})

// 问题标签列表
const problemTags = computed(() => {
  return assessmentRows.value
      .filter(item => getResultType(item) === 'positive')
      .map(item => normalizeProblemName(item))
})

// 综合结论文案
const reportConclusion = computed(() => {
  const done = completedCount.value
  const abn = abnormalCount.value

  if (done === 0) {
    return '患者本次尚未完成评估项目，请先完成评估后再查看报告。'
  }
  if (abn === 0) {
    return `患者本次共完成${done}项评估，暂未发现明显异常问题，建议保持现有活动能力和健康管理方式，定期随访复评。`
  }
  return `患者本次共完成${done}项评估，发现${abn}项现存问题。建议结合评估结果完善专项干预、康复训练和随访管理，由医护人员制定个体化处理方案。`
})

// ==================== 工具函数 ====================
/**
 * 构建评估项展示数据
 */
function buildItem(raw) {
  const code = raw?.scaleCode || ''
  return {
    ...raw,
    scaleCode: code,
    _displayName: scaleNameMap[code] || raw?.scaleName || '未命名项目',
    _answered: true,
    normalRange: raw?.normalRange || defaultNormalRangeMap[code] || '-',
    itemScore: raw?.itemScore || raw?.result || '',
    tip: raw?.tip || raw?.suggest || ''
  }
}

/**
 * 表格头部样式
 */
function headerStyle() {
  return {
    background: '#f1f5f9',
    color: '#334155',
    fontWeight: 700,
    height: '48px'
  }
}

/**
 * 表格单元格样式
 */
function cellStyle() {
  return {
    color: '#334155',
    height: '52px'
  }
}

/**
 * 格式化性别展示
 */
function formatGender(gender) {
  if (String(gender) === '1') return '男'
  if (String(gender) === '2') return '女'
  return '-'
}

/**
 * 格式化日期（年月日）
 */
function formatDate(value) {
  if (!value) return '-'
  return parseTime(value, '{y}-{m}-{d}') || '-'
}

/**
 * 格式化日期时间（年月日时分）
 */
function formatDateTime(value) {
  if (!value) return '-'
  return parseTime(value, '{y}-{m}-{d} {h}:{i}') || '-'
}

/**
 * 格式化身高展示
 */
function formatHeight(value) {
  return value || value === 0 ? `${value}cm` : '-'
}

/**
 * 格式化体重展示
 */
function formatWeight(value) {
  return value || value === 0 ? `${value}kg` : '-'
}

/**
 * 判断文本是否包含阴性关键词
 */
function hasNegativeWord(text) {
  const negativeWords = ['阴性', '正常', '无依赖', '无异常', '无衰弱', '无风险', '良好', '无多重用药', '无需进行专项评估']
  return negativeWords.some(word => text.includes(word))
}

/**
 * 判断文本是否包含阳性关键词
 */
function hasPositiveWord(text) {
  const positiveWords = ['阳性', '依赖', '异常', '障碍', '下降', '风险', '衰弱', '疼痛', '便秘', '失禁', '抑郁', '焦虑', '营养不良', '轻度多重用药', '重度多重用药', '应进行', '应进一步', '建议进行']
  return positiveWords.some(word => text.includes(word))
}

/**
 * 获取评估结果类型（阳性/阴性/待完成）
 */
function getResultType(item) {
  if (!item._answered) return 'pending'

  const text = `${item.itemScore || ''} ${item.tip || ''} ${item.remark || ''}`
  // 先判断阴性，排除正常表述
  if (hasNegativeWord(text) && !hasPositiveWord(text.replace(/无依赖|无异常|无风险|无衰弱|无多重用药/g, ''))) {
    return 'negative'
  }
  // 再判断阳性
  if (hasPositiveWord(text)) return 'positive'

  // 原始得分兜底判断
  if (item.rawScore !== null && item.rawScore !== undefined && !Number.isNaN(Number(item.rawScore))) {
    return Number(item.rawScore) === 0 ? 'positive' : 'negative'
  }

  return 'negative'
}

/**
 * 格式化评估结果展示
 */
function formatResult(item) {
  if (!item._answered) return '未完成'

  const result = String(item.itemScore || '').trim()
  // 多重用药特殊处理
  if (item.scaleCode === 'polypharmacy_assessment' && /^\d+分$/.test(result)) {
    return formatPolypharmacyResult(item)
  }
  if (result) return result

  // 无得分时根据类型返回
  const type = getResultType(item)
  if (type === 'positive') return '阳性'
  if (type === 'negative') return '阴性'
  return '未完成'
}

/**
 * 格式化多重用药结果
 */
function formatPolypharmacyResult(item) {
  const text = `${item.itemScore || ''} ${item.tip || ''}`
  const matched = text.match(/无多重用药|轻度多重用药|重度多重用药|重度用药/)

  if (matched) {
    return matched[0] === '重度用药' ? '重度多重用药' : matched[0]
  }

  const score = Number(item.rawScore)
  if (Number.isNaN(score)) return String(item.itemScore || '').trim()
  if (score >= 2) return '无多重用药'
  if (score === 1) return '轻度多重用药'
  return '重度多重用药'
}

/**
 * 格式化正常范围展示
 */
function formatNormalRange(item) {
  const normalRange = String(item.normalRange || '').trim()
  if (normalRange && !['已完成评估', '已完成专项评估'].includes(normalRange)) {
    return normalRange
  }
  return defaultNormalRangeMap[item.scaleCode] || '-'
}

/**
 * 格式化评估提示文案
 */
function formatTip(item) {
  const tip = String(item.tip || '').trim()
  if (tip) return tip

  const result = formatResult(item)
  if (getResultType(item) === 'negative') {
    if (result && !['阴性', '正常'].includes(result)) {
      return `${item._displayName}${result}，建议保持现有状态，定期随访复评。`
    }
    return '未见明显异常，建议定期随访复评。'
  }
  return '提示存在相关风险，建议结合专项评估结果进一步处理。'
}

/**
 * 标准化问题名称展示
 */
function normalizeProblemName(item) {
  const result = formatResult(item)
  const name = item._displayName.replace(/评估|筛查/g, '')
  if (result && !['阳性', '阴性', '正常', '未完成'].includes(result)) {
    return result
  }
  return name
}

/**
 * 生成报告PDF文件名
 */
function getReportFileName() {
  const name = report.value?.patientName || report.value?.name || 'report'
  return `老年综合评估报告-${name}-${formatDate(new Date())}.pdf`
}

/**
 * 获取报告DOM元素（带校验）
 */
function getReportElement() {
  if (!report.value || !reportContentRef.value) {
    ElMessage.warning('报告内容未加载')
    return null
  }
  return reportContentRef.value
}

/**
 * 报告打印样式字符串
 */
function reportPaperCss() {
  return `
    .report-paper { width: 210mm; min-height: 297mm; margin: 0 auto; padding: 18mm; background: #fff; color: #1f2937; font-family: "Microsoft YaHei", Arial, sans-serif; }
    .paper-header { border-bottom: 2px solid #0f6b85; padding-bottom: 14px; text-align: center; }
    .hospital-line { display: flex; justify-content: space-between; color: #607080; font-size: 12px; }
    .paper-header h1 { margin: 16px 0 4px; color: #0f2f44; font-size: 28px; letter-spacing: 2px; }
    .paper-header p { margin: 0; color: #6b7280; font-size: 12px; }
    .identity-strip { display: grid; grid-template-columns: repeat(6, 1fr); margin-top: 16px; border: 1px solid #cbd5df; }
    .identity-cell { min-height: 42px; padding: 7px 10px; border-right: 1px solid #cbd5df; background: #f8fbfd; }
    .identity-cell.wide { grid-column: span 2; }
    .identity-cell span { display: block; color: #7b8794; font-size: 11px; }
    .identity-cell strong { display: block; margin-top: 3px; color: #20242a; font-size: 13px; }
    .report-section { margin-top: 16px; padding: 14px 18px; border: 1px solid #d8e3ea; }
    .report-section h2 { margin: 0 0 12px; padding-left: 10px; border-left: 4px solid #0f7895; color: #0f2f44; font-size: 17px; }
    .clinical-list { margin: 0; }
    .clinical-list div { display: grid; grid-template-columns: 110px 1fr; padding: 11px 0; border-bottom: 1px solid #edf2f6; }
    .clinical-list div:last-child { border-bottom: 0; }
    .clinical-list dt { color: #8793a1; }
    .clinical-list dd { margin: 0; color: #303740; }
    .problem-tags { display: flex; flex-wrap: wrap; gap: 8px; }
    .problem-tag { display: inline-flex; align-items: center; gap: 6px; padding: 5px 10px; border: 1px solid #facaca; background: #fff5f5; color: #bf3b3b; font-size: 12px; }
    .problem-tag i { display: inline-flex; align-items: center; justify-content: center; width: 16px; height: 16px; border-radius: 50%; background: #d94a4a; color: #fff; font-style: normal; font-weight: 700; }
    .empty-problem { color: #6f7a88; font-size: 13px; }
    .assessment-table { width: 100%; border-collapse: collapse; table-layout: fixed; font-size: 12px; }
    .assessment-table th { padding: 9px 10px; border: 1px solid #aebfcb; background: #e8f1f4; color: #102a43; text-align: left; }
    .assessment-table td { padding: 8px 10px; border: 1px solid #cfd9e1; line-height: 1.65; vertical-align: top; word-break: break-word; }
    .assessment-table .col-name { width: 28%; }
    .assessment-table .col-result { width: 18%; }
    .assessment-table .col-normal { width: 24%; }
    .assessment-table .col-tip { width: 30%; }
    .assessment-table .item-name { font-weight: 700; color: #20242a; }
    .assessment-table tr.row-positive td { background: #fffafa; }
    .assessment-table tr.row-positive .item-name { border-left: 3px solid #d84a4a; }
    .assessment-table tr.row-negative .item-name { border-left: 3px solid #2f855a; }
    .result-pill { display: inline-flex; padding: 3px 8px; border: 1px solid #d7dde5; background: #f7f9fb; }
    .result-pill.positive { border-color: #f1c7c7; background: #fff1f1; color: #a12b2b; }
    .result-pill.negative { border-color: #b9ddc4; background: #f1fbf4; color: #24653a; }
    .conclusion-text { margin: 0; line-height: 1.9; text-indent: 2em; }
    .signature-section { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 22px; padding-top: 14px; border-top: 1px solid #d7dde5; }
    .signature-section div { display: flex; gap: 8px; align-items: center; }
    .signature-section b { flex: 1; min-height: 24px; border-bottom: 1px solid #9aa5b1; }
    @media print { body { margin: 0; background: #fff; } .report-paper { width: auto; min-height: auto; padding: 12mm; } }
  `
}

// ==================== 业务方法 ====================
/**
 * 获取报告列表
 */
async function getList() {
  loading.value = true
  try {
    const res = await selectReportedPatientList(queryParams)
    patientList.value = res.data || []
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询条件
 */
function resetQuery() {
  queryParams.name = ''
  queryParams.phone = ''
  getList()
}

/**
 * 查看报告详情
 */
async function handleView(row) {
  reportLoading.value = true
  reportVisible.value = true
  report.value = null

  try {
    const patientId = row.patientId || row.id
    const res = await getFullCgaReportByPatient(patientId)
    report.value = res.data || null
  } catch (error) {
    report.value = null
    ElMessage.error(error.message || '报告查询失败')
  } finally {
    reportLoading.value = false
  }
}

/**
 * 根据路由参数自动打开报告
 */
async function openReportFromQuery() {
  if (String(route.query.openReport || '') !== '1') return
  const patientId = route.query.patientId ? String(route.query.patientId) : ''
  if (!patientId) return
  // 重复打开拦截
  if (lastOpenedPatientId.value === patientId && reportVisible.value) return

  lastOpenedPatientId.value = patientId
  await handleView({ id: patientId })
}

/**
 * 删除患者报告
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
        `确定删除 ${row.name || '该患者'} 的报告吗？`,
        '删除提示',
        { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteReportByPatientId(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch {
    // 用户取消操作，不做处理
  }
}

/**
 * 导出报告为PDF
 */
async function exportPDF() {
  const el = getReportElement()
  if (!el) return

  exportLoading.value = true
  try {
    const canvas = await html2canvas(el, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff'
    })

    const pdf = new jsPDF('p', 'mm', 'a4')
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = pageWidth - 20
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    let heightLeft = imgHeight
    let position = 10
    const imgData = canvas.toDataURL('image/jpeg', 0.95)

    pdf.addImage(imgData, 'JPEG', 10, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    // 超长内容分页
    while (heightLeft > 0) {
      position = heightLeft - imgHeight + 10
      pdf.addPage()
      pdf.addImage(imgData, 'JPEG', 10, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    pdf.save(getReportFileName())
  } finally {
    exportLoading.value = false
  }
}

/**
 * 打印报告
 */
function printReport() {
  const el = getReportElement()
  if (!el) return

  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    ElMessage.error('浏览器阻止了打印窗口，请允许弹窗后重试')
    return
  }

  printWindow.document.write(`
    <html>
      <head>
        <title>老年综合评估报告</title>
        <style>${reportPaperCss()}</style>
      </head>
      <body>${el.outerHTML}</body>
    </html>
  `)
  printWindow.document.close()
  printWindow.focus()

  // 等待样式加载后打印
  setTimeout(() => {
    printWindow.print()
    printWindow.close()
  }, 300)
}

// ==================== 生命周期 & 监听 ====================
onMounted(async () => {
  await getList()
  await openReportFromQuery()
})

onActivated(async () => {
  await openReportFromQuery()
})

watch(
    () => route.query.openReport,
    async () => {
      await openReportFromQuery()
    }
)
</script>

<style scoped lang="scss">
.report-page {
  min-height: calc(100vh - 84px);
  padding: 24px 28px 40px;
  background:
      radial-gradient(1200px 500px at 10% -20%, rgba(59, 130, 246, 0.08), transparent 60%),
      radial-gradient(1000px 400px at 110% 20%, rgba(99, 102, 241, 0.08), transparent 60%),
      linear-gradient(180deg, #f7faff 0%, #eef3fb 100%);
}

/* ========== 顶部标题栏 ========== */
.hero-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px;
  margin-bottom: 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f7ff 100%);
  border: 1px solid rgba(99, 102, 241, 0.12);
  box-shadow: 0 10px 28px rgba(30, 41, 59, 0.06);
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 18px;

  h1 {
    margin: 0;
    font-size: 22px;
    color: #1e293b;
    font-weight: 700;
  }

  p {
    margin: 4px 0 0;
    font-size: 13px;
    color: #64748b;

    b {
      color: #3b82f6;
      font-weight: 600;
      margin: 0 2px;
    }
  }
}

.hero-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%);
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.28);
}

/* ========== 筛选区 ========== */
.filter-card {
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px;
  padding: 18px 22px 4px;
  margin-bottom: 20px;
  box-shadow: 0 8px 24px rgba(30, 41, 59, 0.04);
}

.filter-form :deep(.el-input__wrapper),
.filter-form :deep(.el-select__wrapper),
.filter-form :deep(.el-date-editor) {
  border-radius: 10px;
}

/* ========== 表格区 ========== */
.table-card {
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px;
  padding: 6px 6px 4px;
  box-shadow: 0 8px 24px rgba(30, 41, 59, 0.04);
  overflow: hidden;
}

.table-shell {
  padding: 14px 14px 8px;
}

.name-cell {
  display: inline-flex;
  align-items: center;
  color: #1e293b;
  font-weight: 600;
}

/* ========== 弹窗 ========== */
.dialog-content {
  max-height: 72vh;
  overflow: auto;
  background: #eef3f8;
  padding: 18px 0;
}

:deep(.report-dialog .el-dialog__body) {
  padding: 0;
}

:deep(.report-list-table .el-button) {
  font-weight: 600;
}

/* ========== A4 报告样式 ========== */
:deep(.report-paper) {
  width: 210mm;
  min-height: 297mm;
  margin: 0 auto;
  padding: 18mm;
  background: #fff;
  color: #1f2937;
  font-family: "Microsoft YaHei", Arial, sans-serif;
}

:deep(.paper-header) {
  border-bottom: 2px solid #0f6b85;
  padding-bottom: 14px;
  text-align: center;
}

:deep(.hospital-line) {
  display: flex;
  justify-content: space-between;
  color: #607080;
  font-size: 12px;
}

:deep(.paper-header h1) {
  margin: 16px 0 4px;
  color: #0f2f44;
  font-size: 28px;
  letter-spacing: 2px;
}

:deep(.paper-header p) {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

:deep(.identity-strip) {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  margin-top: 16px;
  border: 1px solid #cbd5df;
}

:deep(.identity-cell) {
  min-height: 42px;
  padding: 7px 10px;
  border-right: 1px solid #cbd5df;
  background: #f8fbfd;
}

:deep(.identity-cell.wide) {
  grid-column: span 2;
}

:deep(.identity-cell span) {
  display: block;
  color: #7b8794;
  font-size: 11px;
}

:deep(.identity-cell strong) {
  display: block;
  margin-top: 3px;
  color: #20242a;
  font-size: 13px;
}

:deep(.report-section) {
  margin-top: 16px;
  padding: 14px 18px;
  border: 1px solid #d8e3ea;
}

:deep(.report-section h2) {
  margin: 0 0 12px;
  padding-left: 10px;
  border-left: 4px solid #0f7895;
  color: #0f2f44;
  font-size: 17px;
}

:deep(.clinical-list) {
  margin: 0;
}

:deep(.clinical-list div) {
  display: grid;
  grid-template-columns: 110px 1fr;
  padding: 11px 0;
  border-bottom: 1px solid #edf2f6;
}

:deep(.clinical-list div:last-child) {
  border-bottom: 0;
}

:deep(.clinical-list dt) {
  color: #8793a1;
}

:deep(.clinical-list dd) {
  margin: 0;
  color: #303740;
}

:deep(.problem-tags) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.problem-tag) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  border: 1px solid #facaca;
  background: #fff5f5;
  color: #bf3b3b;
  font-size: 12px;
}

:deep(.problem-tag i) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #d94a4a;
  color: #fff;
  font-style: normal;
  font-weight: 700;
}

:deep(.empty-problem) {
  color: #6f7a88;
  font-size: 13px;
}

:deep(.assessment-table) {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 12px;
}

:deep(.assessment-table th) {
  padding: 9px 10px;
  border: 1px solid #aebfcb;
  background: #e8f1f4;
  color: #102a43;
  text-align: left;
}

:deep(.assessment-table td) {
  padding: 8px 10px;
  border: 1px solid #cfd9e1;
  line-height: 1.65;
  vertical-align: top;
  word-break: break-word;
}

:deep(.assessment-table .col-name) {
  width: 28%;
}

:deep(.assessment-table .col-result) {
  width: 18%;
}

:deep(.assessment-table .col-normal) {
  width: 24%;
}

:deep(.assessment-table .col-tip) {
  width: 30%;
}

:deep(.assessment-table .item-name) {
  font-weight: 700;
  color: #20242a;
}

:deep(.assessment-table tr.row-positive td) {
  background: #fffafa;
}

:deep(.assessment-table tr.row-positive .item-name) {
  border-left: 3px solid #d84a4a;
}

:deep(.assessment-table tr.row-negative .item-name) {
  border-left: 3px solid #2f855a;
}

:deep(.result-pill) {
  display: inline-flex;
  padding: 3px 8px;
  border: 1px solid #d7dde5;
  background: #f7f9fb;
}

:deep(.result-pill.positive) {
  border-color: #f1c7c7;
  background: #fff1f1;
  color: #a12b2b;
}

:deep(.result-pill.negative) {
  border-color: #b9ddc4;
  background: #f1fbf4;
  color: #24653a;
}

:deep(.conclusion-text) {
  margin: 0;
  line-height: 1.9;
  text-indent: 2em;
}

:deep(.signature-section) {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-top: 22px;
  padding-top: 14px;
  border-top: 1px solid #d7dde5;
}

:deep(.signature-section div) {
  display: flex;
  gap: 8px;
  align-items: center;
}

:deep(.signature-section b) {
  flex: 1;
  min-height: 24px;
  border-bottom: 1px solid #9aa5b1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ========== 响应式适配 ========== */
@media (max-width: 768px) {
  .report-page {
    padding: 16px 12px 32px;
  }

  .hero-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 18px;
  }

  .form-section {
    padding: 18px 14px 2px;
  }

  .table-card {
    padding: 20px 16px;
  }
}
</style>