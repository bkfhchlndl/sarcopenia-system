<template>
  <div class="app-container detection-page">

    <!-- 功能入口卡片区域 -->
    <section class="card-box">
      <!-- 肌少症诊断入口 -->
      <div class="entry-card diagnosis-card">
        <div class="card-icon">
          <el-icon><User /></el-icon>
        </div>
        <h3 class="card-title">肌少症诊断</h3>
        <p class="card-subtitle">SARCOPENIA DIAGNOSIS</p>
        <el-button class="card-btn" @click="goSarcopenia">进入诊断</el-button>
      </div>

      <!-- 老年综合评估入口 -->
      <div class="entry-card cga-card">
        <div class="card-icon">
          <el-icon><Document /></el-icon>
        </div>
        <h3 class="card-title">老年综合评估</h3>
        <p class="card-subtitle">COMPREHENSIVE GERIATRIC ASSESSMENT</p>
        <el-button class="card-btn" @click="goGeriatric">进入评估</el-button>
      </div>
    </section>

    <!-- 添加用户弹窗 -->
    <el-dialog v-model="open" title="添加用户" width="720px" append-to-body>
      <el-form ref="patientRef" :model="form" :rules="rules" label-width="86px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="病历号" prop="caseNo">
              <el-input v-model="form.caseNo" placeholder="请输入病历号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男性" value="1" />
                <el-option label="女性" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生年月" prop="birthday">
              <el-date-picker
                  v-model="form.birthday"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择出生日期"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身高" prop="height">
              <el-input v-model="form.height" placeholder="请输入身高(cm)" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入家庭住址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DetectionIndex">
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {Document, User} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {parseTime} from '@/utils/ruoyi'
import {insertPatient, selectPatientList} from '@/api/patient'

const router = useRouter()

/** 页面加载状态 */
const loading = ref(false)
/** 提交加载状态 */
const submitLoading = ref(false)
/** 弹窗显示状态 */
const open = ref(false)
/** 表单引用 */
const patientRef = ref()
/** 患者列表 */
const patientList = ref([])

/**
 * 创建空表单对象
 * @returns 初始化的患者表单数据
 */
const createEmptyForm = () => ({
  caseNo: '',
  name: '',
  gender: '',
  birthday: '',
  phone: '',
  height: '',
  idCard: '',
  address: ''
})

/** 表单数据 */
const form = ref(createEmptyForm())

/** 表单校验规则 */
const rules = {
  caseNo: [{required: true, message: '请输入病历号', trigger: 'blur'}],
  name: [{required: true, message: '请输入姓名', trigger: 'blur'}],
  gender: [{required: true, message: '请选择性别', trigger: 'change'}],
  birthday: [{required: true, message: '请选择出生年月', trigger: 'change'}],
  phone: [
    {required: true, message: '请输入手机号', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur'}
  ],
  height: [{required: true, message: '请输入身高', trigger: 'blur'}]
}

/**
 * 获取患者列表
 */
async function getList() {
  loading.value = true
  try {
    const response = await selectPatientList({})
    patientList.value = Array.isArray(response) ? response : []
  } finally {
    loading.value = false
  }
}

/**
 * 提交新增用户表单
 */
async function submitForm() {
  const valid = await patientRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await insertPatient({...form.value})
    ElMessage.success('用户添加成功')
    open.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

/**
 * 构建患者路由参数
 * @param {Object} row 患者信息
 * @returns 格式化后的参数
 */
function buildPatientQuery(row) {
  return {
    patientId: row.id,
    name: row.name,
    gender: row.gender,
    birthday: formatBirthday(row.birthday),
    phone: row.phone,
    height: row.height,
    address: row.address
  }
}

/**
 * 进入肌少症诊断页面（暂未实现）
 */
function goSarcopenia() {
  ElMessage.info('肌少症诊断页面还未接入')
}

/**
 * 进入老年综合评估页面
 */
function goGeriatric() {
  router.push({
    name: 'GeriatricSyndrome',
    query: router.currentRoute.value.query
  })
}

/**
 * 格式化出生日期
 * @param {String|Date} value 日期值
 * @returns 格式化后的日期字符串
 */
function formatBirthday(value) {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  return parseTime(value, '{y}-{m}-{d}') || ''
}

/**
 * 根据出生日期计算年龄
 * @param {String|Date} value 出生日期
 * @returns 年龄
 */
function getAge(value) {
  if (!value) return null
  const birthday = new Date(formatBirthday(value))
  if (Number.isNaN(birthday.getTime())) return null
  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const monthDiff = today.getMonth() - birthday.getMonth()
  const dayDiff = today.getDate() - birthday.getDate()
  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) age -= 1
  return age
}

/** 页面初始化加载患者列表 */
getList()
</script>

<style scoped lang="scss">
.detection-page {
  min-height: calc(100vh - 84px);
  padding-bottom: 30px;
  background: linear-gradient(180deg, #f7fbff 0%, #eef4fb 100%);
}

.patient-panel {
  padding: 20px;
  margin-bottom: 22px;
  border: 1px solid rgba(171, 196, 221, 0.74);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 16px 34px rgba(86, 118, 148, 0.11);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.panel-head h2 {
  margin: 0;
  color: #1f3447;
  font-size: 24px;
}

.panel-head p {
  margin: 6px 0 0;
  color: #718397;
}

:deep(.patient-table) {
  --el-table-header-bg-color: #e7f3eb;
  --el-table-row-hover-bg-color: #f4f9ff;
  border-radius: 8px;
  overflow: hidden;
}

.card-box {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 22px;
}

.entry-card {
  min-height: 260px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 34px;
  color: #fff;
}

.diagnosis-card {
  background: #7964d7;
}

.cga-card {
  background: #2f9a75;
}

.card-icon {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 22px;
}

.card-icon .el-icon {
  font-size: 36px;
  color: #fff;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px;
}

.card-subtitle {
  font-size: 13px;
  opacity: 0.9;
  margin: 0 0 28px;
}

.card-btn {
  background: #fff;
  color: #29465f;
  border: none;
  border-radius: 6px;
  padding: 10px 28px;
}

@media (max-width: 820px) {
  .panel-head {
    align-items: stretch;
    flex-direction: column;
  }

  .card-box {
    grid-template-columns: 1fr;
  }
}
</style>