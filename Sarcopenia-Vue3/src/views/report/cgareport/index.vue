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
          <p>共 <b>{{ total }}</b> 份报告 · 支持查看、打印和 PDF 导出</p>
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
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
              v-model="queryParams.phone"
              placeholder="请输入手机号"
              clearable
              style="width: 180px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button plain @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            <span>重置</span>
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleQuery">
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
          <el-table-column type="index" label="序号" width="70" align="center" :index="getTableIndex" />
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

        <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
        />
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
              <span>病历号</span>
              <strong>{{ report.caseNo || report.medicalCaseNo || '-' }}</strong>
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
            <div class="identity-cell">
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
            <div class="assessment-list">
              <div class="assessment-list-head">
                <span>评估内容</span>
                <span>评估结果</span>
                <!-- <span>正常范围</span> -->
                <span>提示</span>
              </div>
              <div
                  v-for="(item, index) in assessmentRows"
                  :key="item.scaleCode || index"
                  class="assessment-list-row"
                  :class="'row-' + getResultType(item)"
              >
                <div class="item-name">{{ index + 1 }}. {{ item._displayName }}</div>
                <div>
                  <span class="result-pill" :class="getResultType(item)">{{ formatResult(item) }}</span>
                </div>
                <!-- <div>{{ formatNormalRange(item) }}</div> -->
                <div class="item-tip">{{ formatTip(item) }}</div>
              </div>
            </div>
          </section>

          <section class="report-section">
            <h2>综合建议</h2>
            <p class="conclusion-text">{{ reportConclusion }}</p>
          </section>

<!--          <section class="signature-section">-->
<!--            <div><span>评估人员签名：</span><b></b></div>-->
<!--            <div><span>审核医师签名：</span><b></b></div>-->
<!--            <div><span>评估日期：</span><b>{{ reportDate }}</b></div>-->
<!--          </section>-->
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
const total = ref(0)

// 查询参数
const queryParams = reactive({
  name: '',
  phone: '',
  isProject: 1,
  pageNum: 1,
  pageSize: 20
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
  const rows = (report.value?.assessmentList || [])
      .map(buildItem)
      .filter(item => item._answered)
  return dedupeAssessmentRows(rows)
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
      .filter(Boolean)
      .filter((tag, index, arr) => arr.indexOf(tag) === index)
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
    _answered: !!scaleNameMap[code],
    normalRange: raw?.normalRange || defaultNormalRangeMap[code] || '-',
    itemScore: raw?.itemScore || raw?.result || '',
    tip: raw?.tip || raw?.suggest || ''
  }
}

/**
 * 同一个量表编码只保留一条，避免筛查项和专项项重复出现在报告中。
 * 优先保留已完成专项覆盖后的记录，其次保留信息更完整的记录。
 */
function dedupeAssessmentRows(rows) {
  const rowMap = new Map()
  rows.forEach(row => {
    const key = row.scaleCode || row.scaleId
    if (!key) return
    const old = rowMap.get(key)
    if (!old || getRowPriority(row) > getRowPriority(old)) {
      rowMap.set(key, row)
    }
  })
  return Array.from(rowMap.values())
}

function getRowPriority(item) {
  let priority = 0
  const text = `${item.remark || ''} ${item.tip || ''} ${item.itemScore || ''}`
  if (text.includes('专项评估已完成')) priority += 20
  if (String(item.tip || '').trim()) priority += 4
  if (String(item.itemScore || '').trim()) priority += 3
  if (item.rawScore !== null && item.rawScore !== undefined) priority += 1
  return priority
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
  const negativeWords = [
    '阴性', '正常', '无依赖', '无异常', '无衰弱', '无风险', '无明显风险',
    '良好', '无多重用药', '无需进行专项评估', '风险低', '低风险',
    '风险较低', '未发现明显风险因素', '未发现明显异常', '用药情况相对平稳',
    '无抑郁症状', '无焦虑症状', '无睡眠障碍'
  ]
  return negativeWords.some(word => text.includes(word))
}

/**
 * 判断文本是否包含阳性关键词
 */
function hasPositiveWord(text) {
  const cleaned = text
      .replace(/无明显风险因素|未发现明显风险因素|未发现明显异常|无风险|低风险|风险低|风险较低|无多重用药|用药情况相对平稳/g, '')
      .replace(/无抑郁症状|无焦虑症状|无睡眠障碍|无营养不良风险/g, '')
  const positiveWords = [
    '阳性', '依赖', '异常', '障碍', '下降', '衰弱', '疼痛', '便秘', '失禁',
    '抑郁', '焦虑', '营养不良', '轻度多重用药', '重度多重用药',
    '中度风险', '高度风险', '高风险', '风险较高',
    '应进行', '应进一步', '建议进行'
  ]
  return positiveWords.some(word => cleaned.includes(word))
}

function getStoredResultType(item) {
  const result = String(item.itemScore || item.result || '').trim()
  if (!result) return ''

  if (hasNegativeWord(result) && !hasPositiveWord(result)) {
    return 'negative'
  }
  if (result.includes('阴性')) {
    return 'negative'
  }
  if (result.includes('阳性') || hasPositiveWord(result)) {
    return 'positive'
  }
  return ''
}

/**
 * 获取评估结果类型（阳性/阴性/待完成）
 */
function getResultType(item) {
  if (!item._answered) return 'pending'

  if (item.scaleCode === 'frail_assessment') {
    const score = getRuleScore(item)
    if (!Number.isNaN(score)) return score === 5 ? 'negative' : 'positive'
  }
  if (item.scaleCode === 'cognition_screen') {
    const score = getRuleScore(item)
    if (!Number.isNaN(score)) return score <= 2 ? 'positive' : 'negative'
  }

  const storedType = getStoredResultType(item)
  if (storedType) return storedType

  const text = `${item.tip || ''} ${item.remark || ''}`
  // 先判断阴性，排除正常表述
  if (hasNegativeWord(text) && !hasPositiveWord(text)) {
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

  const ruleResult = formatRuleBasedResult(item)
  if (ruleResult) return ruleResult

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

function getRuleScore(item) {
  if (item.rawScore !== null && item.rawScore !== undefined && !Number.isNaN(Number(item.rawScore))) {
    return Number(item.rawScore)
  }
  const text = `${item.itemScore || ''} ${item.result || ''} ${item.tip || ''}`
  const matched = text.match(/(-?\d+)\s*分?/)
  return matched ? Number(matched[1]) : NaN
}

function formatRuleBasedResult(item) {
  const score = getRuleScore(item)
  if (Number.isNaN(score)) return ''

  if (item.scaleCode === 'frail_assessment') {
    if (score === 5) return '无衰弱'
    if (score >= 3) return '衰弱前期'
    return '判定为衰弱'
  }

  if (item.scaleCode === 'cognition_screen') {
    return score <= 2 ? '认知功能障碍筛查阳性' : '阴性'
  }

  return ''
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
 * 标准化问题名称展示（项目名 + 结果描述）
 */
function normalizeProblemName(item) {
  const name = item._displayName.replace(/(评估|筛查|量表|评分表)$/, '')
  const result = cleanProblemText(formatResult(item))
  if (item.scaleCode === 'constipation_screen' && result.includes('存在便秘症状')) {
    return '存在便秘症状'
  }
  if (item.scaleCode === 'sleep_disorder_screen' && result.includes('睡眠障碍')) {
    return result.includes('可能') ? '可能有睡眠障碍' : '存在睡眠障碍'
  }
  if (item.scaleCode === 'water_swallowing_test' && result.includes('吞咽困难')) {
    return result.match(/(轻度吞咽困难|中度吞咽困难|重度吞咽困难)/)?.[1] || '存在吞咽困难'
  }
  if (item.scaleCode === 'pressure_injury_risk') {
    const riskLevel = result.match(/(极高风险|高度风险|中度风险|低度风险|低风险|无风险)/)?.[1]
    return riskLevel ? `压力性损伤风险：${riskLevel}` : '压力性损伤风险'
  }
  // 如果结果是纯关键词，直接用项目名+关键词组合
  if (['阳性', '阴性', '正常', '未完成'].includes(result)) {
    return name
  }
  // 避免重复：结果文本已经包含项目名则不再拼接
  if (result && name && result.includes(name)) {
    return result
  }
  return result ? `${name}：${result}` : name
}

function cleanProblemText(text) {
  return String(text || '')
      .replace(/[{}]/g, '')
      .replace(/[；;，,]?\s*检出异常项目\s*\d+\s*项/g, '')
      .replace(/[；;，,]?\s*检出跌倒风险\s*\d+\s*项/g, '')
      .replace(/[；;，,]?\s*疼痛相关存在\s*\d+\s*项异常/g, '')
      .replace(/\s+/g, ' ')
      .replace(/；\s*；/g, '；')
      .replace(/[；;，,]\s*$/g, '')
      .trim()
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
function escapePrintHtml(value) {
  return String(value ?? '-')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
}

function getRepeatHeaderHtml() {
  const currentReport = report.value || {}
  const headerCells = [
    ['姓名', currentReport.patientName || currentReport.name || '-'],
    ['病历号', currentReport.caseNo || currentReport.medicalCaseNo || '-'],
    ['性别', formatGender(currentReport.gender)],
    ['年龄', displayAge.value],
    ['身高', formatHeight(currentReport.height)],
    ['体重', formatWeight(currentReport.weight)],
    ['评估时间', reportDate.value]
  ]

  const cellHtml = headerCells.map(([label, value]) => `
    <div class="identity-cell">
      <span>${escapePrintHtml(label)}</span>
      <strong>${escapePrintHtml(value || '-')}</strong>
    </div>
  `).join('')

  return `
    <div class="print-repeat-header">
      <div class="print-repeat-title">
        <h1>老年综合评估报告单</h1>
        <p>Comprehensive Geriatric Assessment Report</p>
      </div>
      <section class="identity-strip">${cellHtml}</section>
    </div>
  `
}

function reportPaperCss() {
  return `
    .report-paper { width: 210mm; min-height: 297mm; margin: 0 auto; padding: 18mm; background: #fff; color: #1f2937; font-size: 13px; font-family: "Microsoft YaHei", Arial, sans-serif; }
    .paper-header { border-bottom: 2px solid #0f6b85; padding-bottom: 14px; text-align: center; }
    .hospital-line { display: flex; justify-content: space-between; color: #607080; font-size: 12px; }
    .paper-header h1 { margin: 16px 0 4px; color: #0f2f44; font-size: 28px; letter-spacing: 2px; }
    .paper-header p { margin: 0; color: #6b7280; font-size: 12px; }
    .identity-strip { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); margin-top: 16px; border: 1px solid #cbd5df; border-right: 0; border-bottom: 0; }
    .identity-cell { min-height: 38px; padding: 7px 8px; border-right: 1px solid #cbd5df; border-bottom: 1px solid #cbd5df; background: #f8fbfd; min-width: 0; }
    .identity-cell span { display: block; color: #7b8794; font-size: 11px; }
    .identity-cell strong { display: block; margin-top: 3px; color: #20242a; font-size: 12px; font-weight: 400; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .report-section { margin-top: 16px; padding: 14px 18px; border: 1px solid #d8e3ea; }
    .report-section h2 { margin: 0 0 12px; padding-left: 10px; border-left: 4px solid #0f7895; color: #0f2f44; font-size: 17px; }
    .clinical-list { margin: 0; }
    .clinical-list div { display: grid; grid-template-columns: 110px 1fr; padding: 11px 0; border-bottom: 1px solid #edf2f6; }
    .clinical-list div:last-child { border-bottom: 0; }
    .clinical-list dt { color: #8793a1; }
    .clinical-list dd { margin: 0; color: #303740; }
    .problem-tags { display: flex; flex-wrap: wrap; gap: 8px; }
    .problem-tag { display: inline-flex; align-items: center; gap: 6px; padding: 4px 8px; border: 1px solid #cfd9e1; background: #f8fafc; color: #334155; font-size: 11px; }
    .problem-tag i { display: inline-flex; align-items: center; justify-content: center; width: 14px; height: 14px; border-radius: 50%; background: #64748b; color: #fff; font-style: normal; font-weight: 400; }
    .empty-problem { color: #6f7a88; font-size: 13px; }
    .assessment-list { font-size: 11px; }
    .assessment-list-head,
    .assessment-list-row { display: grid; grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr) minmax(0, 2.2fr); column-gap: 12px; align-items: start; }
    .assessment-list-head > *,
    .assessment-list-row > * { min-width: 0; }
    .assessment-list-head { padding: 7px 0 9px; border-bottom: 2px solid #b8c8d3; color: #102a43; font-weight: 400; }
    .assessment-list-row { padding: 10px 0; border-bottom: 1px solid #dbe4ec; line-height: 1.65; word-break: break-word; overflow-wrap: anywhere; }
    .assessment-list-row:last-child { border-bottom: 0; }
    .assessment-list-row .item-name { font-weight: 400; color: #20242a; }
    .assessment-list-row.row-positive .item-name { color: #20242a; }
    .assessment-list-row.row-negative .item-name { color: #20242a; }
    .result-pill { display: inline; padding: 0; border: 0; background: transparent; color: #1f2937; font-weight: 400; }
    .result-pill.positive,
    .result-pill.negative { color: #1f2937; }
    .conclusion-text { margin: 0; line-height: 1.9; text-indent: 2em; }
    .signature-section { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 22px; padding-top: 14px; border-top: 1px solid #d7dde5; }
    .signature-section div { display: flex; gap: 8px; align-items: center; }
    .signature-section b { flex: 1; min-height: 24px; border-bottom: 1px solid #9aa5b1; }
    .print-repeat-header { display: none; }
    .print-repeat-title { border-bottom: 2px solid #0f6b85; padding-bottom: 8px; text-align: center; }
    .print-repeat-title h1 { margin: 0 0 4px; color: #0f2f44; font-size: 20px; letter-spacing: 1px; }
    .print-repeat-title p { margin: 0; color: #6b7280; font-size: 10px; }
    @media print {
      @page { size: A4; margin: 12mm; }
      body { margin: 0; background: #fff; }
      .print-report-table { width: 100%; border-collapse: collapse; }
      .print-report-table thead { display: table-header-group; }
      .print-report-table tbody { display: table-row-group; }
      .print-report-table td { padding: 0; border: 0; vertical-align: top; }
      .print-repeat-header { display: block; position: static; margin-bottom: 10mm; background: #fff; }
      .print-repeat-header .identity-strip { margin-top: 8px; }
      .report-paper { width: auto; min-height: auto; padding: 0 !important; }
      .report-paper > .paper-header,
      .report-paper > .identity-strip { display: none; }
      .assessment-list-row,
      .signature-section { break-inside: avoid; page-break-inside: avoid; }
    }
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
    const pageData = res.data || {}
    patientList.value = res.rows || pageData.rows || pageData.records || pageData.list || (Array.isArray(pageData) ? pageData : [])
    total.value = Number(res.total ?? pageData.total ?? patientList.value.length ?? 0)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索报告列表
 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/**
 * 重置查询条件
 */
function resetQuery() {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.pageNum = 1
  getList()
}

/**
 * 表格序号按分页连续显示
 */
function getTableIndex(index) {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
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
      <body>
        <table class="print-report-table">
          <thead>
            <tr><td>${getRepeatHeaderHtml()}</td></tr>
          </thead>
          <tbody>
            <tr><td>${el.outerHTML}</td></tr>
          </tbody>
        </table>
      </body>
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
  font-size: 13px;
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
  grid-template-columns: repeat(7, minmax(0, 1fr));
  margin-top: 16px;
  border: 1px solid #cbd5df;
  border-right: 0;
  border-bottom: 0;
}

:deep(.identity-cell) {
  min-height: 38px;
  padding: 7px 8px;
  border-right: 1px solid #cbd5df;
  border-bottom: 1px solid #cbd5df;
  background: #f8fbfd;
  min-width: 0;
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
  font-size: 12px;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  padding: 4px 8px;
  border: 1px solid #cfd9e1;
  background: #f8fafc;
  color: #334155;
  font-size: 11px;
}

:deep(.problem-tag i) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #64748b;
  color: #fff;
  font-style: normal;
  font-weight: 400;
}

:deep(.empty-problem) {
  color: #6f7a88;
  font-size: 13px;
}

:deep(.assessment-list) {
  font-size: 12px;
}

:deep(.assessment-list-head),
:deep(.assessment-list-row) {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr) minmax(0, 2.2fr);
  column-gap: 12px;
  align-items: start;
}

:deep(.assessment-list-head > *),
:deep(.assessment-list-row > *) {
  min-width: 0;
}

:deep(.assessment-list-head) {
  padding: 8px 0 10px;
  border-bottom: 2px solid #b8c8d3;
  color: #102a43;
  font-weight: 400;
}

:deep(.assessment-list-row) {
  padding: 12px 0;
  border-bottom: 1px solid #dbe4ec;
  line-height: 1.65;
  word-break: break-word;
  overflow-wrap: anywhere;
}

:deep(.assessment-list-row:last-child) {
  border-bottom: 0;
}

:deep(.assessment-list-row .item-name) {
  font-weight: 400;
  color: #20242a;
}

:deep(.result-pill) {
  display: inline;
  padding: 0;
  border: 0;
  background: transparent;
  color: #1f2937;
  font-weight: 400;
}

:deep(.result-pill.positive) {
  color: #1f2937;
}

:deep(.result-pill.negative) {
  color: #1f2937;
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
