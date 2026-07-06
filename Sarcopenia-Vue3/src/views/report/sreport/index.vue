<template>
  <div class="report-page">
    <!-- 顶部标题栏 -->
    <div class="hero-bar">
      <div class="hero-left">
        <div class="hero-icon">
          <el-icon :size="26"><Document /></el-icon>
        </div>
        <div>
          <h1>肌少症综合评估报告</h1>
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
        title="肌少症综合评估报告"
        width="860px"
        top="1vh"
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
          <!-- 顶部标题 + 患者信息 -->
          <header class="paper-header">
            <div class="header-top">
              <span class="logo-text">Sarcopenia</span>
              <span class="report-title-main">肌少症检测报告</span>
            </div>
            <div class="patient-info-row">
              <div class="info-item"><span class="info-label">PID</span><span class="info-value">{{ report.caseNo || report.medicalCaseNo || '-' }}</span></div>
              <div class="info-item"><span class="info-label">姓名</span><span class="info-value">{{ report.patientName || report.name || '-' }}</span></div>
              <div class="info-item"><span class="info-label">性别</span><span class="info-value">{{ formatGender(report.gender) }}</span></div>
              <div class="info-item"><span class="info-label">年龄</span><span class="info-value">{{ displayAge }}</span></div>
              <div class="info-item"><span class="info-label">身高</span><span class="info-value">{{ formatHeight(report.height) }}</span></div>
              <div class="info-item"><span class="info-label">体重</span><span class="info-value">{{ formatWeight(report.weight) }}</span></div>
              <div class="info-item"><span class="info-label">测试时间</span><span class="info-value">{{ reportDate }}</span></div>
            </div>
          </header>

          <!-- 综合评价 -->
          <section class="summary-section">
            <h3 class="section-title">综合评价</h3>
            <div class="summary-bar-wrap">
              <div class="summary-bar">
                <div class="bar-segment seg-1"><span>消瘦</span></div>
                <div class="bar-segment seg-2"><span>正常</span></div>
                <div class="bar-segment seg-3"><span>肌少症可能</span></div>
                <div class="bar-segment seg-4"><span>肌少症</span></div>
                <div class="bar-segment seg-5"><span>肌少性肥胖</span></div>
                <div class="bar-segment seg-6"><span>肥胖</span></div>
                <div class="bar-indicator" :style="{ left: indicatorLeft }"></div>
              </div>
            </div>
            <div class="summary-text">
              <div class="summary-result">
                <span class="result-label" :class="diagnosisLevel">{{ diagnosisShort }}</span>
              </div>
              <div class="summary-desc">{{ diagnosisDescShort }}</div>
            </div>
          </section>

          <!-- 测试结果 -->
          <section class="test-section">
            <h3 class="section-title">测试结果</h3>
            <div class="test-content">
              <table class="test-table">
                <thead>
                  <tr>
                    <th>测量项目</th>
                    <th>测量结果</th>
                    <th>标准值</th>
                    <th>评价</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>小腿围</td>
                    <td>{{ formatWithUnit(calfMaxValue, 'cm') }}</td>
                    <td>≥34cm<br>≥33cm</td>
                    <td :class="calfLevel">{{ calfLevel === 'pending' ? '未测' : (calfLevel === 'positive' ? '偏低' : '正常') }}</td>
                  </tr>
                  <tr>
                    <td>握力检测</td>
                    <td>{{ formatWithUnit(gripMaxValue, 'kg') }}</td>
                    <td>≥28.00kg<br>≥18.00kg</td>
                    <td :class="gripLevel">{{ gripLevel === 'pending' ? '未测' : (gripLevel === 'positive' ? '偏低' : '正常') }}</td>
                  </tr>
                  <tr>
                    <td>5次起坐</td>
                    <td>{{ sitUpTimeValue }}</td>
                    <td>&lt;12.00s</td>
                    <td :class="sitUpLevel">{{ sitUpLevel === 'pending' ? '未测' : (sitUpLevel === 'positive' ? '偏慢' : '正常') }}</td>
                  </tr>
                  <tr>
                    <td>并足站立</td>
                    <td>{{ balanceFeetValue }}</td>
                    <td>10.00s(1分)</td>
                    <td rowspan="3" :class="balanceLevel">{{ balanceLevel === 'pending' ? '未测' : (balanceLevel === 'positive' ? '异常' : '正常') }}</td>
                  </tr>
                  <tr>
                    <td>半足站立</td>
                    <td>{{ balanceSemiValue }}</td>
                    <td>10.00s(1分)</td>
                  </tr>
                  <tr>
                    <td>全足站立</td>
                    <td>{{ balanceTandemValue }}</td>
                    <td>10.00s(2分)</td>
                  </tr>
                  <tr>
                    <td>6m步速</td>
                    <td>{{ paceSpeedValue }}</td>
                    <td>≥1.00m/s</td>
                    <td :class="paceLevel">{{ paceLevel === 'pending' ? '未测' : (paceLevel === 'positive' ? '偏慢' : '正常') }}</td>
                  </tr>
                  <tr>
                    <td>6m步行时间</td>
                    <td>{{ paceTimeValue }}</td>
                    <td>≤6.00s</td>
                    <td>-</td>
                  </tr>
                  <tr>
                    <td>简易体能测试(SPPB)</td>
                    <td>{{ sppbScore }}</td>
                    <td>≥10分</td>
                    <td :class="sppbLevel">{{ sppbLevel === 'pending' ? '未测' : (sppbLevel === 'positive' ? '偏低' : '正常') }}</td>
                  </tr>
                  <tr>
                    <td>四肢骨骼肌指数(ASMI)</td>
                    <td>{{ asmiValue }}</td>
                    <td>≥5.7kg/m²</td>
                    <td :class="bodyCompLevel">{{ bodyCompLevel === 'pending' ? '未测' : (bodyCompLevel === 'positive' ? '偏低' : '正常') }}</td>
                  </tr>
                </tbody>
              </table>
              <div class="radar-area">
                <div class="radar-title">肌肉综合表现雷达图</div>
                <div class="radar-chart">
                  <svg viewBox="0 0 200 200" class="radar-svg">
                    <!-- 雷达网格 -->
                    <polygon points="100,20 170,60 170,140 100,180 30,140 30,60" fill="none" stroke="#e5e7eb" stroke-width="1"/>
                    <polygon points="100,40 155,70 155,130 100,160 45,130 45,70" fill="none" stroke="#e5e7eb" stroke-width="1"/>
                    <polygon points="100,60 140,80 140,120 100,140 60,120 60,80" fill="none" stroke="#e5e7eb" stroke-width="1"/>
                    <polygon points="100,80 125,90 125,110 100,120 75,110 75,90" fill="none" stroke="#e5e7eb" stroke-width="1"/>
                    <!-- 轴线 -->
                    <line x1="100" y1="20" x2="100" y2="180" stroke="#e5e7eb" stroke-width="1"/>
                    <line x1="30" y1="60" x2="170" y2="140" stroke="#e5e7eb" stroke-width="1"/>
                    <line x1="30" y1="140" x2="170" y2="60" stroke="#e5e7eb" stroke-width="1"/>
                    <!-- 数据多边形 -->
                    <polygon :points="radarPoints" fill="rgba(99, 102, 241, 0.25)" stroke="#6366f1" stroke-width="2"/>
                    <!-- 数据点 -->
                    <circle v-for="(p, i) in radarPointArray" :key="i" :cx="p.x" :cy="p.y" r="4" fill="#6366f1"/>
                    <!-- 标签 -->
                    <text x="100" y="14" text-anchor="middle" class="radar-label">上肢肌力</text>
                    <text x="180" y="65" text-anchor="start" class="radar-label">肌肉量</text>
                    <text x="180" y="145" text-anchor="start" class="radar-label">步速</text>
                    <text x="100" y="195" text-anchor="middle" class="radar-label">平衡</text>
                    <text x="20" y="145" text-anchor="end" class="radar-label">下肢肌力</text>
                    <text x="20" y="65" text-anchor="end" class="radar-label">SARC-F</text>
                  </svg>
                </div>
                <div class="radar-legend">
                  <span class="legend-dot"></span>
                  <span>{{ reportDate }}</span>
                </div>
              </div>
            </div>
          </section>

          <!-- 人体成分分析 -->
          <section class="body-section">
            <h3 class="section-title">人体成分分析</h3>
            <div class="body-content">
              <div class="body-left">
                <div class="body-grid">
                  <div class="body-item">
                    <div class="body-label">体重 [41.8~56.5]</div>
                    <div class="body-value" :class="bodyWeightLevel">{{ bodyWeightValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ bodyWeightStatus }}</div>
                  </div>
                  <div class="body-item">
                    <div class="body-label">去脂体重 [34.1~41.6]</div>
                    <div class="body-value">{{ leanMassValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ leanMassStatus }}</div>
                  </div>
                  <div class="body-item">
                    <div class="body-label">肌肉量 [31.7~38.7]</div>
                    <div class="body-value">{{ muscleMassValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ muscleMassStatus }}</div>
                  </div>
                  <div class="body-item">
                    <div class="body-label">总体脂重 [11.5~14.1]</div>
                    <div class="body-value">{{ fatMassValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ fatMassStatus }}</div>
                  </div>
                  <div class="body-item">
                    <div class="body-label">身体总水分 [49.5~60.4]</div>
                    <div class="body-value">{{ totalWaterValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ totalWaterStatus }}</div>
                  </div>
                  <div class="body-item">
                    <div class="body-label">蛋白质 [6.7~8.2]</div>
                    <div class="body-value">{{ proteinValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ proteinStatus }}</div>
                  </div>
                  <div class="body-item">
                    <div class="body-label">无机盐 [2.3~2.8]</div>
                    <div class="body-value">{{ mineralValue }}<span class="body-unit">kg</span></div>
                    <div class="body-status">{{ mineralStatus }}</div>
                  </div>
                </div>
                <div class="body-unit-label">单位: kg</div>
              </div>
              <div class="body-right">
                <div class="body-bar-item">
                  <div class="bar-header">
                    <span>BMI</span>
                    <span class="bar-value" :class="bmiBarLevel">{{ bmiValue }}</span>
                  </div>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ width: bmiBarWidth }"></div>
                    <div class="bar-marks">
                      <span>低标准</span>
                      <span>标准</span>
                      <span>超标</span>
                      <span>超标准</span>
                    </div>
                  </div>
                </div>
                <div class="body-bar-item">
                  <div class="bar-header">
                    <span>PBF</span>
                    <span class="bar-value" :class="pbfBarLevel">{{ pbfValue }}</span>
                  </div>
                  <div class="bar-track">
                    <div class="bar-fill bar-fill-pbf" :style="{ width: pbfBarWidth }"></div>
                    <div class="bar-marks">
                      <span>低标准</span>
                      <span>标准</span>
                      <span>超标</span>
                      <span>超标准</span>
                    </div>
                  </div>
                </div>
                <div class="body-bar-item">
                  <div class="bar-header">
                    <span>腰围</span>
                    <span class="bar-value">{{ waistValue }}</span>
                  </div>
                  <div class="bar-track bar-waist">
                    <div class="bar-fill bar-fill-waist" :style="{ width: waistBarWidth }"></div>
                    <div class="bar-marks">
                      <span>标准</span>
                      <span>80</span>
                      <span>超标准</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 节段评估 + 研究项目 -->
          <section class="segment-section">
            <div class="segment-left">
              <h3 class="section-title">节段评估</h3>
              <div class="segment-body">
                <div class="segment-panel segment-muscle">
                  <div class="seg-title">节段肌肉量评估</div>
                  <div class="seg-figure">
                    <div class="seg-side seg-side-left">
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(rightArmMuscle, 'kg') }}</strong>
                        <span>{{ segmentMuscleStatus }}</span>
                        <em>右上肢</em>
                      </div>
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(rightLegMuscle, 'kg') }}</strong>
                        <span>{{ segmentMuscleStatus }}</span>
                        <em>右下肢</em>
                      </div>
                    </div>
                    <svg viewBox="0 0 120 180" class="body-svg">
                      <!-- 简化人形 -->
                      <circle cx="60" cy="20" r="12" fill="#60a5fa"/>
                      <rect x="45" y="32" width="30" height="40" rx="4" fill="#60a5fa"/>
                      <rect x="20" y="38" width="25" height="10" rx="4" fill="#60a5fa"/>
                      <rect x="75" y="38" width="25" height="10" rx="4" fill="#60a5fa"/>
                      <rect x="45" y="72" width="14" height="50" rx="3" fill="#60a5fa"/>
                      <rect x="61" y="72" width="14" height="50" rx="3" fill="#60a5fa"/>
                    </svg>
                    <div class="seg-center">
                      <strong>{{ formatWithUnit(trunkMuscle, 'kg') }}</strong>
                      <span>{{ segmentMuscleStatus }}</span>
                      <em>躯干</em>
                    </div>
                    <div class="seg-side seg-side-right">
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(leftArmMuscle, 'kg') }}</strong>
                        <span>{{ segmentMuscleStatus }}</span>
                        <em>左上肢</em>
                      </div>
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(leftLegMuscle, 'kg') }}</strong>
                        <span>{{ segmentMuscleStatus }}</span>
                        <em>左下肢</em>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="segment-panel segment-fat">
                  <div class="seg-title">节段脂肪量评估</div>
                  <div class="seg-figure">
                    <div class="seg-side seg-side-left">
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(rightArmFat, 'kg') }}</strong>
                        <span>{{ segmentFatStatus }}</span>
                        <em>右上肢</em>
                      </div>
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(rightLegFat, 'kg') }}</strong>
                        <span>{{ segmentFatStatus }}</span>
                        <em>右下肢</em>
                      </div>
                    </div>
                    <svg viewBox="0 0 120 180" class="body-svg body-fat">
                      <circle cx="60" cy="20" r="12" fill="#fbbf24"/>
                      <rect x="45" y="32" width="30" height="40" rx="4" fill="#fbbf24"/>
                      <rect x="20" y="38" width="25" height="10" rx="4" fill="#fbbf24"/>
                      <rect x="75" y="38" width="25" height="10" rx="4" fill="#fbbf24"/>
                      <rect x="45" y="72" width="14" height="50" rx="3" fill="#fbbf24"/>
                      <rect x="61" y="72" width="14" height="50" rx="3" fill="#fbbf24"/>
                    </svg>
                    <div class="seg-center">
                      <strong>{{ formatWithUnit(trunkFat, 'kg') }}</strong>
                      <span>{{ trunkFatStatus }}</span>
                      <em>躯干</em>
                    </div>
                    <div class="seg-side seg-side-right">
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(leftArmFat, 'kg') }}</strong>
                        <span>{{ segmentFatStatus }}</span>
                        <em>左上肢</em>
                      </div>
                      <div class="seg-metric">
                        <strong>{{ formatWithUnit(leftLegFat, 'kg') }}</strong>
                        <span>{{ segmentFatStatus }}</span>
                        <em>左下肢</em>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="segment-right">
              <h3 class="section-title">研究项目</h3>
              <div class="research-list">
                <div class="research-item">
                  <span class="research-label">细胞内水分</span>
                  <span class="research-value">{{ formatWithUnit(icwValue, 'L') }}</span>
                  <span class="research-status">{{ icwStatus }}</span>
                </div>
                <div class="research-item">
                  <span class="research-label">细胞外水分</span>
                  <span class="research-value">{{ formatWithUnit(ecwValue, 'L') }}</span>
                  <span class="research-status">{{ ecwStatus }}</span>
                </div>
                <div class="research-item">
                  <span class="research-label">内脏脂肪面积</span>
                  <span class="research-value">{{ formatWithUnit(vfaValue, 'cm²') }}</span>
                  <span class="research-status">{{ vfaStatus }}</span>
                </div>
                <div class="research-item">
                  <span class="research-label">腰臀比</span>
                  <span class="research-value">{{ whtrValue }}</span>
                  <span class="research-status">{{ whtrStatus }}</span>
                </div>
              </div>
            </div>
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

<script setup name="SarcopeniaReport">
import { computed, onActivated, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Document, Download, Printer, Refresh, Search, View, WarningFilled } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

import { selectPatientById, selectReportedPatientList } from '@/api/patient.js'
import {
  selectSRecordList,
  selectCalfMeasureList,
  selectGripStrengthList,
  selectSitUpList,
  selectPaceList,
  selectBalanceStandList,
  selectBodyCompositionList,
  deleteSReport
} from '@/api/s.js'
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

// 各模块数据
const patientInfo = ref(null)
const sRecordList = ref([])
const calfMeasureList = ref([])
const gripStrengthList = ref([])
const sitUpList = ref([])
const paceList = ref([])
const balanceStandList = ref([])
const bodyCompositionList = ref([])

// 查询参数
const queryParams = reactive({
  name: '',
  phone: '',
  isProject: 2,
  pageNum: 1,
  pageSize: 20
})

function pickValue(source, keys, fallback = '') {
  if (!source) return fallback
  for (const key of keys) {
    const value = source[key]
    if (value !== undefined && value !== null && value !== '') return value
  }
  return fallback
}

function formatNumberValue(value, fallback = '-') {
  if (value === undefined || value === null || value === '') return fallback
  const num = Number(value)
  if (!Number.isNaN(num)) return Number.isInteger(num) ? String(num) : String(Number(num.toFixed(2)))
  return String(value)
}

function isPositiveMeasuredValue(value) {
  if (value === undefined || value === null || value === '' || value === '-') return false
  const num = Number(value)
  return !Number.isNaN(num) && num > 0
}

function formatPositiveNumberValue(value, fallback = '-') {
  return isPositiveMeasuredValue(value) ? formatNumberValue(value, fallback) : fallback
}

function rangeStatus(value, min, max) {
  if (!isPositiveMeasuredValue(value)) return '未测'
  const num = Number(value)
  if (num < min) return '偏低'
  if (num > max) return '偏高'
  return '标准'
}

function measuredStatus(value) {
  return isPositiveMeasuredValue(value) ? '标准' : '未测'
}

function researchMeasuredStatus(value) {
  return isPositiveMeasuredValue(value) ? '标准' : ''
}

function formatWithUnit(value, unit) {
  return value && value !== '-' ? `${value}${unit}` : '-'
}

function maxMeasureValue(source, keyGroups) {
  const values = keyGroups
    .map(keys => pickValue(source, keys))
    .map(Number)
    .filter(value => !Number.isNaN(value) && value > 0)
  return values.length ? Math.max(...values) : null
}

function pickBodyCompositionValue(keys, fallback = '') {
  for (const item of bodyCompositionList.value) {
    const value = pickValue(item, keys)
    if (value !== undefined && value !== null && value !== '') return value
  }
  return fallback
}

// ==================== 计算属性 ====================
// 报告日期
const reportDate = computed(() => {
  const times = [
    report.value?.createTime,
    report.value?.updateTime,
    sRecordList.value[0]?.createTime,
    gripStrengthList.value[0]?.createTime,
    sitUpList.value[0]?.createTime,
    paceList.value[0]?.createTime,
    balanceStandList.value[0]?.createTime,
    bodyCompositionList.value[0]?.createTime
  ].filter(Boolean)
  if (times.length === 0) return '-'
  return formatDate(times.sort((a, b) => new Date(b) - new Date(a))[0])
})

// 展示年龄
const displayAge = computed(() => {
  const birthday = report.value?.birthday || patientInfo.value?.birthday
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

// BMI
const displayBmi = computed(() => {
  const h = report.value?.height || patientInfo.value?.height
  const w = report.value?.weight || patientInfo.value?.weight
  if (!h || !w) return '-'
  const bmi = (w / ((h / 100) ** 2)).toFixed(1)
  return `${bmi} kg/m²`
})

// SARC-F 评分
const sarcfScore = computed(() => {
  const latest = sRecordList.value[0]
  if (!latest) return '未评估'
  const score = latest.totalScore ?? latest.score ?? 0
  return `${score}分`
})

const sarcfLevel = computed(() => {
  const latest = sRecordList.value[0]
  if (!latest) return 'pending'
  const score = latest.totalScore ?? latest.score ?? 0
  return score >= 4 ? 'positive' : 'negative'
})

// 小腿围
const calfDisplay = computed(() => {
  const latest = calfMeasureList.value[0]
  if (!latest) return '未测量'
  return formatWithUnit(calfMaxValue.value, 'cm')
})

const calfFirstValue = computed(() => formatNumberValue(pickValue(calfMeasureList.value[0], ['firstValue', 'leftCalf', 'left', 'value'])))
const calfSecondValue = computed(() => formatNumberValue(pickValue(calfMeasureList.value[0], ['secondValue', 'rightCalf', 'right', 'value'])))
const calfThirdValue = computed(() => formatNumberValue(pickValue(calfMeasureList.value[0], ['thirdValue'])))
const calfMaxValue = computed(() => {
  const max = maxMeasureValue(calfMeasureList.value[0], [
    ['firstValue', 'leftCalf', 'left', 'value'],
    ['secondValue', 'rightCalf', 'right'],
    ['thirdValue']
  ])
  return max === null ? '-' : formatNumberValue(max)
})

const calfLevel = computed(() => {
  const latest = calfMeasureList.value[0]
  if (!latest) return 'pending'
  const gender = String(report.value?.gender || patientInfo.value?.gender || '1')
  const maxCalf = Number(calfMaxValue.value)
  if (Number.isNaN(maxCalf) || maxCalf <= 0) return 'pending'
  // 男性 <34cm 女性 <33cm 为低
  const threshold = gender === '2' ? 33 : 34
  return maxCalf < threshold ? 'positive' : 'negative'
})

// 筛查结论
const screenConclusion = computed(() => {
  const hasSarcf = sRecordList.value.length > 0
  const hasCalf = calfMeasureList.value.length > 0
  if (!hasSarcf && !hasCalf) return '未完成筛查'
  const sarcfPos = sarcfLevel.value === 'positive'
  const calfPos = calfLevel.value === 'positive'
  if (sarcfPos || calfPos) return '筛查阳性 · 建议进一步评估'
  return '筛查阴性 · 暂不支持肌少症诊断'
})

const screenLevel = computed(() => {
  const hasSarcf = sRecordList.value.length > 0
  const hasCalf = calfMeasureList.value.length > 0
  if (!hasSarcf && !hasCalf) return 'pending'
  return sarcfLevel.value === 'positive' || calfLevel.value === 'positive' ? 'positive' : 'negative'
})

// 握力
const gripDisplay = computed(() => {
  const latest = gripStrengthList.value[0]
  if (!latest) return '未测量'
  return formatWithUnit(gripMaxValue.value, 'kg')
})

const gripFirstValue = computed(() => formatNumberValue(pickValue(gripStrengthList.value[0], ['firstValue', 'leftGrip', 'left', 'value'])))
const gripSecondValue = computed(() => formatNumberValue(pickValue(gripStrengthList.value[0], ['secondValue', 'rightGrip', 'right', 'value'])))
const gripThirdValue = computed(() => formatNumberValue(pickValue(gripStrengthList.value[0], ['thirdValue'])))
const gripMaxValue = computed(() => {
  const max = maxMeasureValue(gripStrengthList.value[0], [
    ['firstValue', 'leftGrip', 'left', 'value'],
    ['secondValue', 'rightGrip', 'right'],
    ['thirdValue']
  ])
  return max === null ? '-' : formatNumberValue(max)
})

const gripLevel = computed(() => {
  const latest = gripStrengthList.value[0]
  if (!latest) return 'pending'
  const result = latest.result || latest.assessmentResult || ''
  if (result) {
    if (result.includes('降低') || result.includes('异常') || result.includes('阳性')) return 'positive'
    if (result.includes('正常') || result.includes('阴性')) return 'negative'
  }
  const gender = String(report.value?.gender || patientInfo.value?.gender || '1')
  const maxGrip = Number(gripMaxValue.value)
  if (Number.isNaN(maxGrip) || maxGrip <= 0) return 'pending'
  const threshold = gender === '2' ? 22 : 28
  return maxGrip < threshold ? 'positive' : 'negative'
})

const gripTip = computed(() => {
  const latest = gripStrengthList.value[0]
  if (!latest) return '尚未进行握力测量'
  if (latest.tip || latest.suggest) return latest.tip || latest.suggest
  return gripLevel.value === 'positive'
    ? '握力低于正常参考值，提示肌肉力量下降，建议加强抗阻训练并进一步评估。'
    : '握力在正常范围内，建议保持现有运动习惯。'
})

// 5次起坐
const sitUpDisplay = computed(() => {
  const latest = sitUpList.value[0]
  if (!latest) return '未测量'
  const time = pickValue(latest, ['firstValue', 'completionTime', 'time', 'value'])
  return `${time}秒`
})

const sitUpTimeValue = computed(() => {
  const latest = sitUpList.value[0]
  if (!latest) return '-'
  return `${formatNumberValue(pickValue(latest, ['firstValue', 'completionTime', 'time', 'value']))}s`
})

const sitUpLevel = computed(() => {
  const latest = sitUpList.value[0]
  if (!latest) return 'pending'
  const result = latest.result || latest.assessmentResult || ''
  if (result) {
    if (result.includes('异常') || result.includes('降低') || result.includes('延长')) return 'positive'
    if (result.includes('正常')) return 'negative'
  }
  const time = Number(pickValue(latest, ['firstValue', 'completionTime', 'time', 'value']))
  if (Number.isNaN(time) || time <= 0) return 'pending'
  return time >= 12 ? 'positive' : 'negative'
})

const sitUpTip = computed(() => {
  const latest = sitUpList.value[0]
  if (!latest) return '尚未进行5次起坐测试'
  if (latest.tip || latest.suggest) return latest.tip || latest.suggest
  return sitUpLevel.value === 'positive'
    ? '5次起坐时间延长，提示下肢肌力及躯体功能下降，建议加强下肢力量训练。'
    : '5次起坐测试在正常范围内，下肢功能良好。'
})

// 步速
const paceDisplay = computed(() => {
  const latest = paceList.value[0]
  if (!latest) return '未测量'
  return paceSpeedValue.value
})

const paceTimeValue = computed(() => {
  const latest = paceList.value[0]
  if (!latest) return '-'
  const value = pickValue(latest, ['avgValue', 'firstValue', 'time', 'completionTime'])
  return `${formatNumberValue(value)}s`
})

const paceSpeedValue = computed(() => {
  const latest = paceList.value[0]
  if (!latest) return '-'
  const speed = pickValue(latest, ['gaitSpeed', 'speed'])
  if (speed !== '') return `${formatNumberValue(speed)}m/s`
  const time = Number(pickValue(latest, ['avgValue', 'firstValue', 'time', 'completionTime']))
  if (!Number.isNaN(time) && time > 0) return `${formatNumberValue(6 / time)}m/s`
  return '-'
})

const paceLevel = computed(() => {
  const latest = paceList.value[0]
  if (!latest) return 'pending'
  const result = latest.result || latest.assessmentResult || ''
  if (result) {
    if (result.includes('降低') || result.includes('缓慢') || result.includes('异常')) return 'positive'
    if (result.includes('正常')) return 'negative'
  }
  let speed = Number(pickValue(latest, ['gaitSpeed', 'speed']))
  if (Number.isNaN(speed) || speed <= 0) {
    const time = Number(pickValue(latest, ['avgValue', 'firstValue', 'time', 'completionTime']))
    speed = !Number.isNaN(time) && time > 0 ? 6 / time : 0
  }
  if (!speed) return 'pending'
  return speed < 1.0 ? 'positive' : 'negative'
})

const paceTip = computed(() => {
  const latest = paceList.value[0]
  if (!latest) return '尚未进行步速测量'
  if (latest.tip || latest.suggest) return latest.tip || latest.suggest
  return paceLevel.value === 'positive'
    ? '步速减慢，提示躯体功能下降，为肌少症重要表现之一，建议进一步评估及干预。'
    : '步速在正常范围内，躯体功能良好。'
})

// 平衡
const balanceDisplay = computed(() => {
  const latest = balanceStandList.value[0]
  if (!latest) return '未测量'
  return latest.result || latest.assessmentResult || '已完成'
})

const balanceFeetValue = computed(() => {
  const latest = balanceStandList.value[0]
  if (!latest) return '-'
  return `${formatNumberValue(pickValue(latest, ['feetTogetherTime', 'feetTogether', 'firstValue']))}s`
})

const balanceSemiValue = computed(() => {
  const latest = balanceStandList.value[0]
  if (!latest) return '-'
  return `${formatNumberValue(pickValue(latest, ['semiOffsetTime', 'semiOffset', 'secondValue']))}s`
})

const balanceTandemValue = computed(() => {
  const latest = balanceStandList.value[0]
  if (!latest) return '-'
  return `${formatNumberValue(pickValue(latest, ['tandemStandTime', 'tandemStand', 'thirdValue']))}s`
})

const balanceLevel = computed(() => {
  const latest = balanceStandList.value[0]
  if (!latest) return 'pending'
  const result = latest.result || latest.assessmentResult || ''
  if (result.includes('异常') || result.includes('障碍') || result.includes('不能')) return 'positive'
  if (result.includes('正常') || result.includes('良好')) return 'negative'
  const score = Number(pickValue(latest, ['totalScore', 'score']))
  if (!Number.isNaN(score) && score > 0) return score < 4 ? 'positive' : 'negative'
  return 'negative'
})

const balanceTip = computed(() => {
  const latest = balanceStandList.value[0]
  if (!latest) return '尚未进行站立平衡测试'
  if (latest.tip || latest.suggest) return latest.tip || latest.suggest
  return balanceLevel.value === 'positive'
    ? '平衡功能异常，跌倒风险增高，建议进行平衡功能训练及防跌倒干预。'
    : '平衡功能良好，建议保持。'
})

// 人体成分
const bodyCompDisplay = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '未测量'
  const smm = pickValue(latest, ['totalMuscleMass', 'skeletalMuscleMass', 'muscleMass', 'appendicularMuscle'])
  const asm = pickValue(latest, ['asmi', 'appendicularSkeletalMuscleIndex', 'asmIndex'])
  if (asm) return `ASM指数: ${asm} kg/m²`
  if (smm) return `骨骼肌量: ${smm}kg`
  return '已测量'
})

const bodyCompLevel = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return 'pending'
  const result = latest.result || latest.assessmentResult || ''
  if (result) {
    if (result.includes('降低') || result.includes('减少') || result.includes('不足')) return 'positive'
    if (result.includes('正常')) return 'negative'
  }
  const asm = Number(pickValue(latest, ['asmi', 'appendicularSkeletalMuscleIndex', 'asmIndex']))
  if (asm > 0) {
    const gender = String(report.value?.gender || patientInfo.value?.gender || '1')
    const threshold = gender === '2' ? 5.7 : 7.0
    return asm < threshold ? 'positive' : 'negative'
  }
  return 'negative'
})

const bodyCompTip = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '尚未进行人体成分分析'
  if (latest.tip || latest.suggest) return latest.tip || latest.suggest
  return bodyCompLevel.value === 'positive'
    ? '骨骼肌量低于正常参考值，支持肌少症肌肉量减少诊断，建议营养干预结合抗阻训练。'
    : '骨骼肌量在正常范围内。'
})

// 综合诊断
const diagnosisTitle = computed(() => {
  const levels = [gripLevel.value, sitUpLevel.value, paceLevel.value, balanceLevel.value, bodyCompLevel.value]
  const positives = levels.filter(l => l === 'positive').length
  const measured = levels.filter(l => l !== 'pending').length
  if (measured === 0) return '未完成评估'
  if (bodyCompLevel.value === 'positive' && (gripLevel.value === 'positive' || sitUpLevel.value === 'positive' || paceLevel.value === 'positive')) {
    return '确诊肌少症'
  }
  if (screenLevel.value === 'positive' && gripLevel.value === 'positive' && bodyCompLevel.value === 'positive') {
    return '确诊肌少症'
  }
  if (screenLevel.value === 'positive' && positives >= 2) {
    return '高度怀疑肌少症'
  }
  if (screenLevel.value === 'positive') {
    return '筛查阳性 · 需进一步检查'
  }
  if (positives >= 1) {
    return '部分指标异常 · 建议随访'
  }
  return '未见明显异常'
})

const diagnosisLevel = computed(() => {
  const title = diagnosisTitle.value
  if (title.includes('确诊')) return 'positive'
  if (title.includes('高度怀疑') || title.includes('阳性') || title.includes('异常')) return 'warning'
  return 'negative'
})

const diagnosisDesc = computed(() => {
  const title = diagnosisTitle.value
  if (title === '未完成评估') return '患者尚未完成肌少症相关评估项目，请完成评估后查看综合诊断结果。'
  if (title === '确诊肌少症') return '根据AWGS 2019诊断标准，结合肌肉量减少及肌肉力量/躯体功能下降，可确诊为肌少症。建议制定综合干预方案。'
  if (title === '高度怀疑肌少症') return '多项指标提示肌少症可能，建议完善人体成分分析等检查以明确诊断，并尽早开始干预。'
  if (title.includes('筛查阳性')) return 'SARC-F评分或小腿围筛查提示阳性，建议进一步行握力、步速及人体成分等检查以明确诊断。'
  if (title.includes('部分指标')) return '部分评估指标存在异常，但尚未达到肌少症诊断标准，建议定期随访，关注肌肉健康。'
  return '各项评估指标均在正常范围内，暂不支持肌少症诊断。建议保持健康生活方式，定期复查。'
})

// 综合建议
const reportConclusion = computed(() => {
  const level = diagnosisLevel.value
  const items = []
  if (gripLevel.value === 'positive') items.push('握力下降')
  if (sitUpLevel.value === 'positive') items.push('下肢肌力下降')
  if (paceLevel.value === 'positive') items.push('步速减慢')
  if (balanceLevel.value === 'positive') items.push('平衡功能异常')
  if (bodyCompLevel.value === 'positive') items.push('肌肉量减少')

  if (level === 'positive') {
    return `患者本次肌少症综合评估提示${items.length}项异常（${items.join('、')}），结合肌肉量及肌肉功能评估结果，${diagnosisTitle.value}。建议：1. 营养干预：保证充足蛋白质及维生素D摄入；2. 运动干预：以抗阻训练为主，结合有氧运动和平衡训练；3. 临床干预：排查继发性肌少症病因，必要时转诊专科；4. 定期随访，监测肌肉量及功能变化。`
  }
  if (level === 'warning') {
    return `患者本次评估发现${items.length}项异常（${items.join('、')}），${diagnosisTitle.value}。建议完善相关检查明确诊断，同时注意加强营养和运动，预防肌少症进展。3-6个月后复查评估。`
  }
  return '患者本次肌少症综合评估各项指标基本正常，暂不支持肌少症诊断。建议保持规律运动和均衡膳食，定期进行肌少症筛查，关注肌肉健康。'
})

// ==================== 新报告样式计算属性 ====================

// 综合评价简短标题
const diagnosisShort = computed(() => {
  if (diagnosisLevel.value === 'positive') return '肌少症'
  if (diagnosisLevel.value === 'warning') return '肌少症可能'
  return '正常'
})

// 综合评价简短描述
const diagnosisDescShort = computed(() => {
  if (diagnosisLevel.value === 'positive') return '肌肉量、肌力和躯体功能均见异常，建议进一步检查并干预。'
  if (diagnosisLevel.value === 'warning') return '部分指标异常，建议进一步评估，关注肌肉健康。'
  return '肌肉量、肌力和躯体功能未见异常，请继续保持。'
})

// 刻度条指示器位置（6段，0-100%）
const indicatorLeft = computed(() => {
  const level = diagnosisLevel.value
  if (level === 'positive') return '70%'
  if (level === 'warning') return '50%'
  return '25%'
})

// ASMI值
const asmiValue = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '-'
  const asm = pickValue(latest, ['asmi', 'appendicularSkeletalMuscleIndex', 'asmIndex'])
  return asm ? asm + ' kg/m²' : '-'
})

const sppbScore = computed(() => {
  const sit = Number(pickValue(sitUpList.value[0], ['score']))
  const pace = Number(pickValue(paceList.value[0], ['score']))
  const balance = Number(pickValue(balanceStandList.value[0], ['totalScore', 'score']))
  const scores = [sit, pace, balance].filter(v => !Number.isNaN(v))
  if (scores.length === 0) return '-'
  return `${scores.reduce((sum, item) => sum + item, 0)}分`
})

const sppbLevel = computed(() => {
  const text = sppbScore.value
  if (text === '-') return 'pending'
  const score = Number(String(text).replace('分', ''))
  return score < 10 ? 'positive' : 'negative'
})

// BMI 值（用于人体成分条形图）
const bmiValue = computed(() => {
  const bodyBmi = pickValue(bodyCompositionList.value[0], ['bmi'])
  if (bodyBmi !== '') return formatNumberValue(bodyBmi)
  const h = report.value?.height || patientInfo.value?.height
  const w = bodyWeightValue.value !== '-' ? bodyWeightValue.value : (report.value?.weight || patientInfo.value?.weight)
  if (!h || !w) return '-'
  return (w / ((h / 100) ** 2)).toFixed(1)
})

const bmiBarLevel = computed(() => {
  const bmi = Number(bmiValue.value)
  if (isNaN(bmi)) return ''
  if (bmi < 18.5) return 'low'
  if (bmi < 24) return 'normal'
  if (bmi < 28) return 'high'
  return 'over'
})

const bmiBarWidth = computed(() => {
  const bmi = Number(bmiValue.value)
  if (isNaN(bmi)) return '50%'
  // 范围 15-35，映射到 0-100%
  const pct = Math.max(0, Math.min(100, ((bmi - 15) / 20) * 100))
  return pct + '%'
})

// PBF（体脂率）
const pbfValue = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '-'
  const pbf = pickValue(latest, ['bodyFatRate', 'bodyFatPercentage', 'pbf', 'fatRate'])
  return pbf ? pbf + '%' : '-'
})

const pbfBarLevel = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return ''
  const result = latest.result || ''
  if (result.includes('偏高') || result.includes('超标')) return 'high'
  if (result.includes('偏低')) return 'low'
  return 'normal'
})

const pbfBarWidth = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '50%'
  const pbf = Number(pickValue(latest, ['bodyFatRate', 'bodyFatPercentage', 'pbf', 'fatRate'], 0))
  if (!pbf) return '50%'
  // 范围 10-40，映射到 0-100%
  const pct = Math.max(0, Math.min(100, ((pbf - 10) / 30) * 100))
  return pct + '%'
})

// 腰围
const waistValue = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '-'
  const waist = pickValue(latest, ['waist', 'waistCircumference'])
  return waist ? waist + 'cm' : '-'
})

const waistBarWidth = computed(() => {
  const latest = bodyCompositionList.value[0]
  if (!latest) return '50%'
  const waist = Number(pickValue(latest, ['waist', 'waistCircumference'], 0))
  if (!waist) return '50%'
  // 范围 60-100，映射到 0-100%
  const pct = Math.max(0, Math.min(100, ((waist - 60) / 40) * 100))
  return pct + '%'
})

const bodyWeightValue = computed(() => {
  const l = latestBodyComp.value
  return formatNumberValue(pickValue(l, ['weight'], report.value?.weight || patientInfo.value?.weight || '-'))
})

// 体重状态
const bodyWeightLevel = computed(() => {
  const w = Number(bodyWeightValue.value)
  if (!w) return ''
  if (w < 41.8) return 'low'
  if (w > 56.5) return 'high'
  return 'normal'
})

const bodyWeightStatus = computed(() => {
  if (!isPositiveMeasuredValue(bodyWeightValue.value)) return '未测'
  if (bodyWeightLevel.value === 'low') return '偏低'
  if (bodyWeightLevel.value === 'high') return '偏高'
  return '标准'
})

// 人体成分各项（从人体成分分析数据中提取，没有则显示占位）
const latestBodyComp = computed(() => bodyCompositionList.value[0] || null)

const leanMassValue = computed(() => {
  const l = latestBodyComp.value
  if (!l) return '-'
  return formatPositiveNumberValue(pickValue(l, ['leanBodyMass', 'leanMass', 'fatFreeMass']))
})
const leanMassStatus = computed(() => rangeStatus(leanMassValue.value, 34.1, 41.6))

const muscleMassValue = computed(() => {
  const l = latestBodyComp.value
  if (!l) return '-'
  return formatPositiveNumberValue(pickValue(l, ['totalMuscleMass', 'skeletalMuscleMass', 'muscleMass']))
})
const muscleMassStatus = computed(() => rangeStatus(muscleMassValue.value, 31.7, 38.7))

const fatMassValue = computed(() => {
  const l = latestBodyComp.value
  if (!l) return '-'
  return formatPositiveNumberValue(pickValue(l, ['bodyFatMass', 'fatMass']))
})

const fatMassStatus = computed(() => {
  const l = latestBodyComp.value
  if (!l || !isPositiveMeasuredValue(fatMassValue.value)) return '未测'
  const result = l.result || ''
  if (result.includes('偏高') || result.includes('超标')) return '超标'
  if (result.includes('偏低')) return '偏低'
  return rangeStatus(fatMassValue.value, 11.5, 14.1)
})

const totalWaterValue = computed(() => {
  const l = latestBodyComp.value
  if (!l) return '-'
  return formatPositiveNumberValue(pickValue(l, ['totalBodyWater', 'waterMass']))
})
const totalWaterStatus = computed(() => rangeStatus(totalWaterValue.value, 49.5, 60.4))

const proteinValue = computed(() => {
  const l = latestBodyComp.value
  if (!l) return '-'
  return formatPositiveNumberValue(pickValue(l, ['protein', 'proteinMass']))
})
const proteinStatus = computed(() => rangeStatus(proteinValue.value, 6.7, 8.2))

const mineralValue = computed(() => {
  const l = latestBodyComp.value
  if (!l) return '-'
  return formatPositiveNumberValue(pickValue(l, ['inorganicSalt', 'mineralMass', 'mineral']))
})
const mineralStatus = computed(() => rangeStatus(mineralValue.value, 2.3, 2.8))

// 雷达图数据 - 6个维度：上肢肌力、肌肉量、步速、平衡、下肢肌力、SARC-F
const radarScores = computed(() => {
  // 每项 0-100 分
  const grip = gripLevel.value === 'pending' ? 50 : (gripLevel.value === 'positive' ? 30 : 90)
  const muscle = bodyCompLevel.value === 'pending' ? 50 : (bodyCompLevel.value === 'positive' ? 25 : 85)
  const pace = paceLevel.value === 'pending' ? 50 : (paceLevel.value === 'positive' ? 30 : 90)
  const balance = balanceLevel.value === 'pending' ? 50 : (balanceLevel.value === 'positive' ? 35 : 85)
  const leg = sitUpLevel.value === 'pending' ? 50 : (sitUpLevel.value === 'positive' ? 30 : 88)
  const sarcf = sarcfLevel.value === 'pending' ? 50 : (sarcfLevel.value === 'positive' ? 25 : 90)
  return [grip, muscle, pace, balance, leg, sarcf]
})

// 雷达图多边形顶点坐标（正六边形，中心100,100，最大半径80）
const radarPoints = computed(() => {
  const scores = radarScores.value
  const cx = 100, cy = 100, maxR = 80
  const angles = [-90, -30, 30, 90, 150, 210] // 6个方向
  return scores.map((s, i) => {
    const r = (s / 100) * maxR
    const angle = angles[i] * Math.PI / 180
    const x = cx + r * Math.cos(angle)
    const y = cy + r * Math.sin(angle)
    return `${x.toFixed(1)},${y.toFixed(1)}`
  }).join(' ')
})

const radarPointArray = computed(() => {
  const scores = radarScores.value
  const cx = 100, cy = 100, maxR = 80
  const angles = [-90, -30, 30, 90, 150, 210]
  return scores.map((s, i) => {
    const r = (s / 100) * maxR
    const angle = angles[i] * Math.PI / 180
    return {
      x: (cx + r * Math.cos(angle)).toFixed(1),
      y: (cy + r * Math.sin(angle)).toFixed(1)
    }
  })
})

// 节段肌肉量（模拟数据，有真实数据则用真实数据）
const rightArmMuscle = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['armRightMuscle', 'rightArmMuscle', 'rightArmSkeletalMuscle', 'rightUpperLimbMuscle', 'arm_right_muscle']))
})
const leftArmMuscle = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['armLeftMuscle', 'leftArmMuscle', 'leftArmSkeletalMuscle', 'leftUpperLimbMuscle', 'arm_left_muscle']))
})
const trunkMuscle = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['trunkMuscle', 'torsoMuscle', 'trunk_muscle']))
})
const rightLegMuscle = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['legRightMuscle', 'rightLegMuscle', 'rightLowerLimbMuscle', 'leg_right_muscle']))
})
const leftLegMuscle = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['legLeftMuscle', 'leftLegMuscle', 'leftLowerLimbMuscle', 'leg_left_muscle']))
})

const hasSegmentMuscleData = computed(() => {
  return [rightArmMuscle.value, leftArmMuscle.value, trunkMuscle.value, rightLegMuscle.value, leftLegMuscle.value]
    .some(value => value && value !== '-')
})

const segmentMuscleStatus = computed(() => hasSegmentMuscleData.value ? '标准' : '未测')

// 节段脂肪量
const rightArmFat = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['armRightFat', 'rightArmFat', 'rightUpperLimbFat', 'arm_right_fat']))
})
const leftArmFat = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['armLeftFat', 'leftArmFat', 'leftUpperLimbFat', 'arm_left_fat']))
})
const trunkFat = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['trunkFat', 'torsoFat', 'trunk_fat']))
})
const trunkFatStatus = computed(() => {
  const l = latestBodyComp.value
  if (!l || !isPositiveMeasuredValue(trunkFat.value)) return '未测'
  const result = l.result || ''
  if (result.includes('偏高') || result.includes('超标')) return '超标'
  return '标准'
})
const rightLegFat = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['legRightFat', 'rightLegFat', 'rightLowerLimbFat', 'leg_right_fat']))
})
const leftLegFat = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['legLeftFat', 'leftLegFat', 'leftLowerLimbFat', 'leg_left_fat']))
})

const hasSegmentFatData = computed(() => {
  return [rightArmFat.value, leftArmFat.value, trunkFat.value, rightLegFat.value, leftLegFat.value]
    .some(value => value && value !== '-')
})

const segmentFatStatus = computed(() => hasSegmentFatData.value ? '标准' : '未测')

// 研究项目
const icwValue = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['intracellularWater', 'icw']))
})
const icwStatus = computed(() => researchMeasuredStatus(icwValue.value))
const ecwValue = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['extracellularWater', 'ecw']))
})
const ecwStatus = computed(() => researchMeasuredStatus(ecwValue.value))
const vfaValue = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['visceralFatArea', 'vfa']))
})
const vfaStatus = computed(() => {
  if (!isPositiveMeasuredValue(vfaValue.value)) return ''
  const vfa = Number(vfaValue.value)
  if (vfa > 100) return '偏高'
  return '标准'
})
const whtrValue = computed(() => {
  return formatPositiveNumberValue(pickBodyCompositionValue(['waistHipRatio', 'whtr']))
})
const whtrStatus = computed(() => researchMeasuredStatus(whtrValue.value))

// ==================== 工具函数 ====================
function headerStyle() {
  return {
    background: '#f1f5f9',
    color: '#334155',
    fontWeight: 700,
    height: '48px'
  }
}

function cellStyle() {
  return {
    color: '#334155',
    height: '52px'
  }
}

function formatGender(gender) {
  if (String(gender) === '1') return '男'
  if (String(gender) === '2') return '女'
  return '-'
}

function formatDate(value) {
  if (!value) return '-'
  return parseTime(value, '{y}-{m}-{d}') || '-'
}

function formatDateTime(value) {
  if (!value) return '-'
  return parseTime(value, '{y}-{m}-{d} {h}:{i}') || '-'
}

function formatHeight(value) {
  return value || value === 0 ? `${value}cm` : '-'
}

function formatWeight(value) {
  return value || value === 0 ? `${value}kg` : '-'
}

function getReportFileName() {
  const name = report.value?.patientName || report.value?.name || 'report'
  return `肌少症综合评估报告-${name}-${formatDate(new Date())}.pdf`
}

function getReportElement() {
  if (!report.value || !reportContentRef.value) {
    ElMessage.warning('报告内容未加载')
    return null
  }
  return reportContentRef.value
}

// 打印样式
function escapePrintHtml(value) {
  return String(value ?? '-')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
}

function getRepeatHeaderHtml() {
  const r = report.value || {}
  const infoItems = [
    ['PID', r.caseNo || r.medicalCaseNo || '-'],
    ['姓名', r.patientName || r.name || '-'],
    ['性别', formatGender(r.gender)],
    ['年龄', displayAge.value],
    ['身高', formatHeight(r.height)],
    ['体重', formatWeight(r.weight)],
    ['测试时间', reportDate.value]
  ]
  const infoHtml = infoItems.map(([label, value]) => `
    <div class="info-item">
      <span class="info-label">${escapePrintHtml(label)}</span>
      <span class="info-value">${escapePrintHtml(value || '-')}</span>
    </div>
  `).join('')
  return `
    <div class="print-repeat-header">
      <header class="paper-header">
        <div class="header-top">
          <span class="logo-text">Sarcopenia</span>
          <span class="report-title-main">肌少症检测报告</span>
        </div>
        <div class="patient-info-row">${infoHtml}</div>
      </header>
    </div>
  `
}

function reportPaperCss() {
  return `
    @page { size: A4; margin: 0; }
    html, body { margin: 0; padding: 0; background: #fff; -webkit-print-color-adjust: exact; print-color-adjust: exact; color-adjust: exact; }
    *, *::before, *::after { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; color-adjust: exact !important; }
    svg, svg * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; color-adjust: exact !important; }

    /* ===== A4 纸张基础 ===== */
    .report-paper { width: 210mm; min-height: 297mm; margin: 0 auto; padding: 10mm 12mm; background: #fff; color: #1f2937; font-size: 10.5px; line-height: 1.35; font-family: "Microsoft YaHei", Arial, sans-serif; box-sizing: border-box; overflow: hidden; -webkit-print-color-adjust: exact; print-color-adjust: exact; color-adjust: exact; }

    /* ===== 顶部标题栏 ===== */
    .paper-header { border-bottom: 2px solid #334155; padding-bottom: 7px; margin-bottom: 9px; }
    .header-top { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
    .logo-text { font-size: 18px; font-weight: 800; color: #334155; letter-spacing: 0; }
    .report-title-main { font-size: 17px; font-weight: 700; color: #1e293b; }
    .patient-info-row { display: grid; grid-template-columns: repeat(7, 1fr); gap: 0; background: #f8fafc; border: 1px solid #e2e8f0; }
    .info-item { display: flex; flex-direction: column; gap: 1px; padding: 4px 5px; border-right: 1px solid #e2e8f0; font-size: 9px; }
    .info-item:last-child { border-right: 0; }
    .info-label { color: #64748b; font-weight: 600; }
    .info-value { color: #1e293b; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    /* ===== 区块标题 ===== */
    .section-title { margin: 0 0 6px; padding: 2px 0 2px 6px; font-size: 12px; font-weight: 700; color: #1e293b; border-left: 3px solid #6366f1; }

    /* ===== 综合评价 ===== */
    .summary-section { display: grid; grid-template-columns: 1fr 0.9fr; gap: 14px; align-items: end; margin-bottom: 10px; }
    .summary-section .section-title { grid-column: 1 / -1; margin-bottom: 0; }
    .summary-bar-wrap { margin-bottom: 0; padding-top: 8px; }
    .summary-bar { position: relative; display: grid; grid-template-columns: repeat(6, 1fr); width: 86%; height: 34px; border-radius: 4px; overflow: visible; }
    .bar-segment { position: relative; display: flex; align-items: flex-end; justify-content: center; height: 14px; padding-top: 18px; font-size: 8px; color: #475569; font-weight: 500; white-space: nowrap; }
    .bar-segment span { position: absolute; top: 17px; left: 50%; transform: translateX(-50%); }
    .bar-segment.seg-1 { background: #fbbf24; }
    .bar-segment.seg-2 { background: #34d399; }
    .bar-segment.seg-3 { background: #fcd34d; color: #92400e; }
    .bar-segment.seg-4 { background: #f97316; }
    .bar-segment.seg-5 { background: #ef4444; }
    .bar-segment.seg-6 { background: #dc2626; }
    .bar-indicator { position: absolute; top: -8px; transform: translateX(-50%); width: 0; height: 0; border-left: 5px solid transparent; border-right: 5px solid transparent; border-top: 7px solid #ef4444; }
    .summary-text { display: block; padding: 4px 8px; background: transparent; border: 0; border-radius: 0; }
    .summary-result { flex-shrink: 0; }
    .result-label { font-size: 12px; font-weight: 700; }
    .result-label.positive { color: #dc2626; }
    .result-label.warning { color: #f97316; }
    .result-label.negative { color: #059669; }
    .summary-desc { margin-top: 2px; font-size: 9px; color: #475569; line-height: 1.45; }

    /* ===== 测试结果 ===== */
    .test-section { margin-bottom: 10px; }
    .test-content { display: grid; grid-template-columns: 1.28fr 0.92fr; gap: 14px; align-items: stretch; }
    .test-table { width: 100%; border-collapse: collapse; font-size: 9.5px; }
    .test-table th { background: #f8fafc; color: #334155; font-weight: 700; padding: 4px 5px; border: 1px solid #cbd5e1; text-align: center; }
    .test-table td { padding: 3px 5px; border: 1px solid #e2e8f0; text-align: center; color: #334155; line-height: 1.3; }
    .test-table td.positive { color: #dc2626; font-weight: 600; }
    .test-table td.negative { color: #059669; font-weight: 600; }
    .test-table td.pending { color: #94a3b8; }
    .test-table tr:nth-child(even) td { background: #f8fafc; }

    /* ===== 雷达图 ===== */
    .radar-area { display: flex; flex-direction: column; align-items: center; justify-content: center; background: #fafafa; border: 1px solid #e5e7eb; border-radius: 4px; padding: 4px; }
    .radar-title { font-size: 10px; font-weight: 700; color: #374151; margin-bottom: 3px; }
    .radar-chart { width: 150px; height: 150px; }
    .radar-svg { width: 100%; height: 100%; }
    .radar-label { font-size: 8px; fill: #6b7280; font-family: "Microsoft YaHei", sans-serif; }
    .radar-legend { display: flex; align-items: center; gap: 6px; font-size: 10px; color: #6b7280; margin-top: 4px; }
    .legend-dot { width: 24px; height: 8px; background: rgba(99, 102, 241, 0.5); border: 1px solid #6366f1; border-radius: 2px; }

    /* ===== 人体成分分析 ===== */
    .body-section { margin-bottom: 14px; }
    .body-content { display: grid; grid-template-columns: 1.08fr 0.92fr; gap: 14px; }
    .body-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px; }
    .body-item { display: flex; flex-direction: column; gap: 1px; padding: 5px 6px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; min-height: 37px; }
    .body-label { font-size: 8.5px; color: #64748b; }
    .body-value { font-size: 11px; font-weight: 700; color: #1e293b; }
    .body-value.low { color: #f59e0b; }
    .body-value.high { color: #ef4444; }
    .body-value.normal { color: #059669; }
    .body-unit { font-size: 10px; font-weight: 400; color: #64748b; margin-left: 2px; }
    .body-status { font-size: 10px; color: #64748b; }
    .body-unit-label { font-size: 10px; color: #94a3b8; margin-top: 4px; text-align: right; }
    .body-right { display: flex; flex-direction: column; gap: 10px; padding: 9px 10px; background: #fafafa; border: 1px solid #e5e7eb; border-radius: 5px; }
    .body-bar-item { display: flex; flex-direction: column; gap: 3px; }
    .bar-header { display: flex; justify-content: space-between; align-items: center; font-size: 11px; font-weight: 600; color: #374151; }
    .bar-value { font-size: 13px; font-weight: 700; color: #1e293b; }
    .bar-value.low { color: #f59e0b; }
    .bar-value.normal { color: #059669; }
    .bar-value.high { color: #f97316; }
    .bar-value.over { color: #dc2626; }
    .bar-track { position: relative; height: 8px; margin-bottom: 14px; background: linear-gradient(to right, #60a5fa 0%, #60a5fa 25%, #34d399 25%, #34d399 50%, #fbbf24 50%, #fbbf24 75%, #f97316 75%, #f97316 100%); border-radius: 4px; overflow: visible; }
    .bar-waist { background: linear-gradient(to right, #34d399 0%, #34d399 50%, #f97316 50%, #f97316 100%); }
    .bar-fill { position: absolute; top: 50%; left: 0; transform: translateY(-50%); height: 12px; border-right: 2px solid #1e293b; background: transparent; pointer-events: none; }
    .bar-marks { position: absolute; top: 10px; left: 0; right: 0; display: flex; justify-content: space-between; font-size: 8px; line-height: 1; color: #94a3b8; }

    /* ===== 节段评估 + 研究项目 ===== */
    .segment-section { display: grid; grid-template-columns: 1.38fr 0.78fr; gap: 18px; margin-top: 14px; margin-bottom: 0; align-items: stretch; }
    .segment-left, .segment-right { display: flex; flex-direction: column; min-width: 0; }
    .segment-body { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; flex: 1; }
    .segment-panel { min-height: 164px; padding: 8px 10px; background: #fff; border: 1px solid #e5e7eb; border-radius: 5px; }
    .seg-title { font-size: 9px; font-weight: 700; color: #374151; text-align: left; margin-bottom: 8px; }
    .seg-figure { position: relative; display: grid; grid-template-columns: 0.92fr 82px 0.92fr; align-items: center; min-height: 136px; }
    .body-svg { grid-column: 2; justify-self: center; width: 78px; height: 116px; opacity: 0.74; }
    .seg-side { display: flex; flex-direction: column; justify-content: space-between; min-height: 122px; font-size: 8.5px; color: #334155; }
    .seg-side-left { grid-column: 1; align-items: flex-start; text-align: left; }
    .seg-side-right { grid-column: 3; align-items: flex-end; text-align: right; }
    .seg-metric { display: flex; flex-direction: column; line-height: 1.25; }
    .seg-metric strong, .seg-center strong { font-size: 10px; color: #1e293b; font-weight: 700; }
    .seg-metric span, .seg-center span { color: #64748b; }
    .seg-metric em, .seg-center em { color: #334155; font-style: normal; margin-top: 2px; }
    .seg-center { position: absolute; left: 50%; top: 52%; transform: translate(-50%, -50%); display: flex; flex-direction: column; align-items: center; line-height: 1.25; font-size: 8.5px; text-align: center; color: #334155; }
    .segment-right { flex: 1; }
    .research-list { flex: 1; min-height: 164px; display: flex; flex-direction: column; justify-content: space-around; gap: 8px; border: 1px solid #e5e7eb; border-radius: 5px; padding: 12px 14px; background: #fff; }
    .research-item { display: grid; grid-template-columns: 1fr auto auto; align-items: center; gap: 10px; font-size: 9px; }
    .research-item .research-label { flex: 1; color: #64748b; }
    .research-item .research-value { font-weight: 600; color: #1e293b; }
    .research-item .research-status { font-size: 10px; color: #059669; }

    /* ===== 综合建议 ===== */
    .advice-section { margin-bottom: 0; }
    .advice-text { margin: 0; font-size: 9px; line-height: 1.45; color: #475569; text-indent: 2em; padding: 5px 8px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 5px; }

    @media print {
      html, body, .report-paper { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; color-adjust: exact !important; }
      .report-paper { width: 210mm; min-height: 297mm; box-shadow: none; page-break-after: avoid; }
      .bar-track { background-image: linear-gradient(to right, #60a5fa 0%, #60a5fa 25%, #34d399 25%, #34d399 50%, #fbbf24 50%, #fbbf24 75%, #f97316 75%, #f97316 100%) !important; }
      .bar-waist { background-image: linear-gradient(to right, #34d399 0%, #34d399 50%, #f97316 50%, #f97316 100%) !important; }
    }
  `
}

// ==================== 业务方法 ====================
async function getList() {
  loading.value = true
  try {
    // 查询有肌少症评估报告的患者
    const res = await selectReportedPatientList(queryParams)
    const pageData = res.data || {}
    patientList.value = res.rows || pageData.rows || pageData.records || pageData.list || (Array.isArray(pageData) ? pageData : [])
    total.value = Number(res.total ?? pageData.total ?? patientList.value.length ?? 0)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.pageNum = 1
  getList()
}

function getTableIndex(index) {
  return (queryParams.pageNum - 1) * queryParams.pageSize + index + 1
}

async function handleView(row) {
  reportLoading.value = true
  reportVisible.value = true
  report.value = null
  sRecordList.value = []
  calfMeasureList.value = []
  gripStrengthList.value = []
  sitUpList.value = []
  paceList.value = []
  balanceStandList.value = []
  bodyCompositionList.value = []

  try {
    const patientId = row.patientId || row.id

    const [
      patientRes, sRes, calfRes, gripRes, sitUpRes, paceRes, balanceRes, bcRes
    ] = await Promise.all([
      selectPatientById({ patientId }).catch(() => null),
      selectSRecordList({ patientId }).catch(() => ({ data: [] })),
      selectCalfMeasureList({ patientId }).catch(() => ({ data: [] })),
      selectGripStrengthList({ patientId }).catch(() => ({ data: [] })),
      selectSitUpList({ patientId }).catch(() => ({ data: [] })),
      selectPaceList({ patientId }).catch(() => ({ data: [] })),
      selectBalanceStandList({ patientId }).catch(() => ({ data: [] })),
      selectBodyCompositionList({ patientId }).catch(() => ({ data: [] }))
    ])

    patientInfo.value = patientRes?.data || patientRes || null
    report.value = patientInfo.value
    sRecordList.value = normalizeList(sRes)
    calfMeasureList.value = normalizeList(calfRes)
    gripStrengthList.value = normalizeList(gripRes)
    sitUpList.value = normalizeList(sitUpRes)
    paceList.value = normalizeList(paceRes)
    balanceStandList.value = normalizeList(balanceRes)
    bodyCompositionList.value = normalizeList(bcRes)
  } catch (error) {
    report.value = null
    ElMessage.error(error.message || '报告查询失败')
  } finally {
    reportLoading.value = false
  }
}

function normalizeList(res) {
  if (!res) return []
  const data = res.data ?? res.rows ?? res.records ?? res.list ?? res
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.rows)) return data.rows
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list
  if (Array.isArray(data?.data)) return data.data
  if (data?.latestData && typeof data.latestData === 'object') return [data.latestData]
  return data && typeof data === 'object' && data.id ? [data] : []
}

async function openReportFromQuery() {
  if (String(route.query.openReport || '') !== '1') return
  const patientId = route.query.patientId ? String(route.query.patientId) : ''
  if (!patientId) return
  if (lastOpenedPatientId.value === patientId && reportVisible.value) return
  lastOpenedPatientId.value = patientId
  await handleView({ id: patientId })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
        `确定删除 ${row.name || '该患者'} 的肌少症报告吗？`,
        '删除提示',
        { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch {
    // 用户取消操作，不做处理
    return
  }
  try {
    const patientId = row.patientId || row.id
    const res = await deleteSReport(patientId)
    if (res && res.code === 200) {
      ElMessage.success('删除成功')
      getList()
    } else {
      ElMessage.error(res?.msg || '删除失败')
    }
  } catch (err) {
    ElMessage.error(err?.msg || err?.message || '删除失败，请稍后重试')
  }
}

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
        <title>肌少症综合评估报告</title>
        <style>${reportPaperCss()}</style>
      </head>
      <body>
        ${el.outerHTML}
      </body>
    </html>
  `)
  printWindow.document.close()
  printWindow.focus()

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
      radial-gradient(1200px 500px at 10% -20%, rgba(249, 115, 22, 0.08), transparent 60%),
      radial-gradient(1000px 400px at 110% 20%, rgba(234, 88, 12, 0.08), transparent 60%),
      linear-gradient(180deg, #fffaf5 0%, #fef3e8 100%);
}

:deep(.report-paper),
:deep(.report-paper *),
:deep(.report-paper svg),
:deep(.report-paper svg *) {
  -webkit-print-color-adjust: exact !important;
  print-color-adjust: exact !important;
  color-adjust: exact !important;
}

/* ========== 顶部标题栏 ========== */
.hero-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px;
  margin-bottom: 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #fff7ed 100%);
  border: 1px solid rgba(249, 115, 22, 0.15);
  box-shadow: 0 10px 28px rgba(30, 41, 59, 0.06);
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 18px;

  h1 {
    margin: 0;
    font-size: 22px;
    color: #7c2d12;
    font-weight: 700;
  }

  p {
    margin: 4px 0 0;
    font-size: 13px;
    color: #9a3412;

    b {
      color: #c2410c;
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
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  box-shadow: 0 10px 24px rgba(249, 115, 22, 0.28);
}

/* ========== 筛选区 ========== */
.filter-card {
  background: #fff;
  border: 1px solid rgba(249, 115, 22, 0.12);
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
  border: 1px solid rgba(249, 115, 22, 0.1);
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
  color: #7c2d12;
  font-weight: 600;
}

/* ========== 弹窗 ========== */
.dialog-content {
  max-height: 82vh;
  overflow: auto;
  background: #f3f4f6;
  padding: 10px 0;
}

:deep(.report-dialog) {
  .el-dialog {
    max-width: calc(100vw - 32px);
    border-radius: 8px;
    overflow: hidden;
  }

  .el-dialog__header {
    padding: 12px 18px;
    margin: 0;
    border-bottom: 1px solid #e5e7eb;
  }

  .el-dialog__footer {
    padding: 10px 18px;
    border-top: 1px solid #e5e7eb;
  }
}

:deep(.report-dialog .el-dialog__body) {
  padding: 0;
}

:deep(.report-list-table .el-button) {
  font-weight: 600;
}

/* ========== 专业检测报告样式 ========== */
:deep(.report-paper) {
  width: 210mm;
  min-height: 297mm;
  margin: 0 auto;
  padding: 10mm 12mm;
  background: #fff;
  color: #1f2937;
  font-size: 10.5px;
  line-height: 1.35;
  font-family: "Microsoft YaHei", Arial, sans-serif;
  box-sizing: border-box;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.12);
}

/* 顶部标题栏 */
:deep(.paper-header) {
  border-bottom: 2px solid #334155;
  padding-bottom: 7px;
  margin-bottom: 9px;
}

:deep(.header-top) {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

:deep(.logo-text) {
  font-size: 18px;
  font-weight: 800;
  color: #334155;
  letter-spacing: 0;
}

:deep(.report-title-main) {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
}

:deep(.patient-info-row) {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

:deep(.info-item) {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 4px 5px;
  border-right: 1px solid #e2e8f0;
  font-size: 9px;

  &:last-child {
    border-right: 0;
  }
}

:deep(.info-label) {
  color: #64748b;
  font-weight: 600;
}

:deep(.info-value) {
  color: #1e293b;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 区块标题 */
:deep(.section-title) {
  margin: 0 0 6px;
  padding: 2px 0 2px 6px;
  font-size: 12px;
  font-weight: 700;
  color: #1e293b;
  border-left: 3px solid #6366f1;
}

/* 综合评价 */
:deep(.summary-section) {
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 14px;
  align-items: end;
  margin-bottom: 10px;
}

:deep(.summary-section .section-title) {
  grid-column: 1 / -1;
  margin-bottom: 0;
}

:deep(.summary-bar-wrap) {
  margin-bottom: 0;
  padding-top: 8px;
}

:deep(.summary-bar) {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  width: 86%;
  height: 34px;
  border-radius: 4px;
  overflow: visible;
}

:deep(.bar-segment) {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 14px;
  padding-top: 16px;
  font-size: 8px;
  color: #475569;
  font-weight: 500;
  white-space: nowrap;

  span {
    position: absolute;
    top: 17px;
    left: 50%;
    transform: translateX(-50%);
  }

  &.seg-1 { background: #fbbf24; }
  &.seg-2 { background: #34d399; }
  &.seg-3 { background: #fcd34d; color: #92400e; }
  &.seg-4 { background: #f97316; }
  &.seg-5 { background: #ef4444; }
  &.seg-6 { background: #dc2626; }
}

:deep(.bar-indicator) {
  position: absolute;
  top: -8px;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 7px solid #ef4444;
  transition: left 0.3s;
}

:deep(.summary-text) {
  display: block;
  padding: 4px 8px;
  background: transparent;
  border: 0;
  border-radius: 0;
}

:deep(.summary-result) {
  flex-shrink: 0;
}

:deep(.result-label) {
  font-size: 12px;
  font-weight: 700;

  &.positive { color: #dc2626; }
  &.warning { color: #f97316; }
  &.negative { color: #059669; }
}

:deep(.summary-desc) {
  flex: 1;
  margin-top: 2px;
  font-size: 9px;
  color: #475569;
  line-height: 1.45;
}

/* 测试结果 */
:deep(.test-section) {
  margin-bottom: 10px;
}

:deep(.test-content) {
  display: grid;
  grid-template-columns: 1.28fr 0.92fr;
  gap: 14px;
  align-items: stretch;
}

:deep(.test-table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 9.5px;
}

:deep(.test-table th) {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
  padding: 4px 5px;
  border: 1px solid #cbd5e1;
  text-align: center;
}

:deep(.test-table td) {
  padding: 3px 5px;
  border: 1px solid #e2e8f0;
  text-align: center;
  color: #334155;
  line-height: 1.3;

  &.positive { color: #dc2626; font-weight: 600; }
  &.negative { color: #059669; font-weight: 600; }
  &.pending { color: #94a3b8; }
}

:deep(.test-table tr:nth-child(even) td) {
  background: #f8fafc;
}

/* 雷达图 */
:deep(.radar-area) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 4px;
}

:deep(.radar-title) {
  font-size: 10px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 3px;
}

:deep(.radar-chart) {
  width: 150px;
  height: 150px;
}

:deep(.radar-svg) {
  width: 100%;
  height: 100%;
}

:deep(.radar-label) {
  font-size: 8px;
  fill: #6b7280;
  font-family: "Microsoft YaHei", sans-serif;
}

:deep(.radar-legend) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  color: #6b7280;
  margin-top: 4px;
}

:deep(.legend-dot) {
  width: 24px;
  height: 8px;
  background: rgba(99, 102, 241, 0.5);
  border: 1px solid #6366f1;
  border-radius: 2px;
}

/* 人体成分分析 */
:deep(.body-section) {
  margin-bottom: 14px;
}

:deep(.body-content) {
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  gap: 14px;
}

:deep(.body-left) {
  min-width: 0;
}

:deep(.body-grid) {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
}

:deep(.body-item) {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-height: 37px;
  padding: 5px 6px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
}

:deep(.body-label) {
  font-size: 8.5px;
  color: #64748b;
}

:deep(.body-value) {
  font-size: 11px;
  font-weight: 700;
  color: #1e293b;

  &.low { color: #f59e0b; }
  &.high { color: #ef4444; }
  &.normal { color: #059669; }
}

:deep(.body-unit) {
  font-size: 10px;
  font-weight: 400;
  color: #64748b;
  margin-left: 2px;
}

:deep(.body-status) {
  font-size: 10px;
  color: #64748b;
}

:deep(.body-unit-label) {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 4px;
  text-align: right;
}

:deep(.body-right) {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 9px 10px;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 5px;
}

:deep(.body-bar-item) {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

:deep(.bar-header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  color: #374151;
}

:deep(.bar-value) {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;

  &.low { color: #f59e0b; }
  &.normal { color: #059669; }
  &.high { color: #f97316; }
  &.over { color: #dc2626; }
}

:deep(.bar-track) {
  position: relative;
  height: 8px;
  margin-bottom: 14px;
  background: linear-gradient(to right,
    #60a5fa 0%, #60a5fa 25%,
    #34d399 25%, #34d399 50%,
    #fbbf24 50%, #fbbf24 75%,
    #f97316 75%, #f97316 100%);
  border-radius: 4px;
  overflow: visible;
}

:deep(.bar-waist) {
  background: linear-gradient(to right,
    #34d399 0%, #34d399 50%,
    #f97316 50%, #f97316 100%);
}

:deep(.bar-fill) {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  height: 12px;
  border-right: 2px solid #1e293b;
  background: transparent;
  pointer-events: none;
}

:deep(.bar-marks) {
  position: absolute;
  top: 10px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  font-size: 8px;
  line-height: 1;
  color: #94a3b8;
}

/* 节段评估 + 研究项目 */
:deep(.segment-section) {
  display: grid;
  grid-template-columns: 1.38fr 0.78fr;
  gap: 18px;
  margin-top: 14px;
  margin-bottom: 0;
  align-items: stretch;
}

:deep(.segment-left),
:deep(.segment-right) {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

:deep(.segment-left) {
  min-width: 0;
}

:deep(.segment-body) {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  flex: 1;
}

:deep(.segment-panel) {
  min-height: 164px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 5px;
}

:deep(.seg-title) {
  font-size: 9px;
  font-weight: 700;
  color: #374151;
  text-align: left;
  margin-bottom: 8px;
}

:deep(.seg-figure) {
  position: relative;
  display: grid;
  grid-template-columns: 0.92fr 82px 0.92fr;
  align-items: center;
  min-height: 136px;
}

:deep(.body-svg) {
  grid-column: 2;
  justify-self: center;
  width: 78px;
  height: 116px;
  opacity: 0.74;
}

:deep(.seg-side) {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 122px;
  font-size: 8.5px;
  color: #334155;
}

:deep(.seg-side-left) {
  grid-column: 1;
  align-items: flex-start;
  text-align: left;
}

:deep(.seg-side-right) {
  grid-column: 3;
  align-items: flex-end;
  text-align: right;
}

:deep(.seg-metric) {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

:deep(.seg-metric strong),
:deep(.seg-center strong) {
  font-size: 10px;
  color: #1e293b;
  font-weight: 700;
}

:deep(.seg-metric span),
:deep(.seg-center span) {
  color: #64748b;
}

:deep(.seg-metric em),
:deep(.seg-center em) {
  color: #334155;
  font-style: normal;
  margin-top: 2px;
}

:deep(.seg-center) {
  position: absolute;
  left: 50%;
  top: 52%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.25;
  font-size: 8.5px;
  text-align: center;
  color: #334155;
}

:deep(.segment-right) {
  min-width: 0;
}

:deep(.research-list) {
  flex: 1;
  min-height: 164px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  gap: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 5px;
  padding: 12px 14px;
  background: #fff;
}

:deep(.research-item) {
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  gap: 10px;
  font-size: 9px;

  .research-label {
    flex: 1;
    color: #64748b;
  }
  .research-value {
    font-weight: 600;
    color: #1e293b;
  }
  .research-status {
    font-size: 10px;
    color: #059669;

    &:not(.标准) { color: #f97316; }
  }
}

/* 综合建议 */
:deep(.advice-section) {
  margin-bottom: 0;
}

:deep(.advice-text) {
  margin: 0;
  font-size: 9px;
  line-height: 1.45;
  color: #475569;
  text-indent: 2em;
  padding: 5px 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

  .table-card {
    padding: 20px 16px; 
  }
}
</style>
