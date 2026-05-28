<template>
  <div class="app-container patient-page">
    <section class="filter-card">
      <div class="section-head">
        <h2>用户管理</h2>
        <el-button type="success" @click="handleAdd">新增用户</el-button>
      </div>

      <el-form ref="queryRef" :model="queryParams" inline label-width="76px">
        <el-form-item prop="caseNo">
          <el-input
              v-model="queryParams.caseNo"
              placeholder="请输入病历号"
              clearable
              style="width: 220px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="name">
          <el-input
              v-model="queryParams.name"
              placeholder="请输入姓名"
              clearable
              style="width: 220px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
              v-model="queryParams.phone"
              placeholder="请输入手机号"
              clearable
              style="width: 220px"
              @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <div class="date-range-inline">
            <el-date-picker
                v-model="queryParams.beginCreateTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="开始日期"
                style="width: 160px"
            />
            <span class="date-range-separator">-</span>
            <el-date-picker
                v-model="queryParams.endCreateTime"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="结束日期"
                style="width: 160px"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" plain @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-card">
      <el-table v-loading="loading" :data="pagedPatientList" row-key="id" class="patient-table">
        <el-table-column label="用户ID" prop="caseNo" min-width="110" />
        <el-table-column label="姓名" prop="name" min-width="110" />
        <el-table-column label="性别" min-width="88">
          <template #default="{ row }">
            {{ formatGender(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column label="出生年月" min-width="190">
          <template #default="{ row }">
            {{ formatBirthdayWithAge(row.birthday) }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" min-width="138">
          <template #default="{ row }">
            {{ maskPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column label="注册日期" min-width="180">
          <template #default="{ row }">
            {{ parseTime(row.createTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <el-button link class="action-enter" @click="handleTest(row)">进入测试</el-button>
            <el-button link class="action-edit" @click="handleEdit(row)">编辑信息</el-button>
            <el-button link class="action-delete" @click="handleDelete(row)">删除用户</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && patientList.length === 0" description="暂无用户数据" />

      <div v-if="patientList.length > 0" class="pager-wrap">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[5, 10, 20, 50]"
            :total="patientList.length"
        />
      </div>
    </section>

    <el-dialog v-model="open" :title="dialogTitle" width="760px" class="patient-dialog" append-to-body>
      <div class="dialog-shell">
        <div class="dialog-banner">
          <div>
            <p class="dialog-kicker">Profile Editor</p>
            <h3>{{ dialogTitle }}</h3>
          </div>
          <span class="dialog-badge">{{ form.id ? 'Update' : 'Create' }}</span>
        </div>

        <el-form ref="patientRef" :model="form" :rules="rules" label-width="86px" class="dialog-form">
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
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PatientCaseList">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { deletePatientById, insertPatient, selectPatientList, updatePatient } from '@/api/patient'

const router = useRouter()

/** 查询表单引用 */
const queryRef = ref()
/** 患者表单引用 */
const patientRef = ref()
/** 加载状态 */
const loading = ref(false)
/** 提交加载状态 */
const submitLoading = ref(false)
/** 弹窗显示状态 */
const open = ref(false)
/** 弹窗标题 */
const dialogTitle = ref('编辑信息')
/** 患者列表 */
const patientList = ref([])
/** 当前页码 */
const pageNum = ref(1)
/** 每页条数 */
const pageSize = ref(10)

/** 查询参数 */
const queryParams = reactive({
  caseNo: '',
  name: '',
  phone: '',
  beginCreateTime: '',
  endCreateTime: ''
})

/** 创建空表单对象 */
const createEmptyForm = () => ({
  id: undefined,
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
  caseNo: [{ required: true, message: '请输入病历号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择出生年月', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }]
}

/** 分页患者列表（计算属性） */
const pagedPatientList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  return patientList.value.slice(start, end)
})

/** 构建查询参数 */
function buildQueryParams() {
  return { ...queryParams }
}

/** 获取患者列表 */
async function getList() {
  loading.value = true
  try {
    const response = await selectPatientList(buildQueryParams())
    patientList.value = Array.isArray(response) ? response : []
    pageNum.value = 1
  } finally {
    loading.value = false
  }
}

/** 查询按钮 */
function handleQuery() {
  getList()
}

/** 重置查询 */
function resetQuery() {
  queryRef.value?.resetFields()
  getList()
}

/** 重置表单 */
function resetForm() {
  form.value = createEmptyForm()
  patientRef.value?.resetFields()
}

/** 新增用户 */
function handleAdd() {
  resetForm()
  dialogTitle.value = '新增用户'
  open.value = true
}

/** 编辑用户 */
function handleEdit(row) {
  resetForm()
  dialogTitle.value = '编辑信息'
  form.value = {
    id: row.id,
    caseNo: row.caseNo || '',
    name: row.name || '',
    gender: row.gender || '',
    birthday: formatBirthday(row.birthday),
    phone: row.phone || '',
    height: row.height || '',
    idCard: row.idCard || '',
    address: row.address || ''
  }
  open.value = true
}

/** 提交表单（新增/修改） */
async function submitForm() {
  const valid = await patientRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = { ...form.value }
    if (payload.id) {
      await updatePatient(payload)
      ElMessage.success('用户信息已更新')
    } else {
      await insertPatient(payload)
      ElMessage.success('用户新增成功')
    }
    open.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

/** 删除用户 */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户“${row.name}”吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deletePatientById({ id: row.id })
  ElMessage.success('删除成功')
  getList()
}

/** 进入测试页面 */
function handleTest(row) {
  router.push({
    name: 'DetectionIndex',
    query: {
      patientId: row.id,
      name: row.name
    }
  })
}

/** 格式化性别 */
function formatGender(gender) {
  if (gender === '1' || gender === 1) return '男性'
  if (gender === '2' || gender === 2) return '女性'
  return '-'
}

/** 格式化出生日期 */
function formatBirthday(value) {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  return parseTime(value, '{y}-{m}-{d}') || ''
}

/** 计算年龄 */
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

/** 格式化生日+年龄 */
function formatBirthdayWithAge(value) {
  const birthday = formatBirthday(value)
  if (!birthday) return '-'
  const age = getAge(value)
  return age === null ? birthday : `${birthday}（${age}岁）`
}

/** 手机号脱敏 */
function maskPhone(phone) {
  if (!phone) return '-'
  return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

/** 初始化加载列表 */
getList()
</script>

<style scoped lang="scss">
.patient-page {
  min-height: calc(100vh - 84px);
  padding-bottom: 24px;
  background:
      radial-gradient(circle at top left, rgba(125, 201, 154, 0.16), transparent 24%),
      radial-gradient(circle at top right, rgba(106, 169, 255, 0.14), transparent 28%),
      linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%);
}

.filter-card,
.table-card {
  border: 1px solid rgba(171, 196, 221, 0.7);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 40px rgba(90, 118, 155, 0.12);
}

.filter-card {
  padding: 20px 22px 2px;
  margin-bottom: 18px;
}

.table-card {
  padding: 18px 18px 14px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.section-head h2 {
  margin: 0;
  color: #1f3552;
  font-size: 22px;
}

.date-range-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-range-separator {
  color: #7d8ea4;
  font-weight: 700;
}

:deep(.patient-table) {
  --el-table-header-bg-color: #d9efc5;
  --el-table-row-hover-bg-color: #f4f9ff;
  border-radius: 16px;
  overflow: hidden;
}

:deep(.patient-table th.el-table__cell) {
  color: #1d2f47;
  font-size: 15px;
  font-weight: 700;
}

:deep(.patient-table td.el-table__cell) {
  padding-top: 18px;
  padding-bottom: 18px;
  font-size: 15px;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.action-enter {
  color: #84b92b;
}

.action-edit {
  color: #7445d8;
}

.action-delete {
  color: #ff4848;
}

.dialog-shell {
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff 0%, #f1f6fc 100%);
  padding: 16px;
}

.dialog-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #dff4dc 0%, #edf5ff 100%);
}

.dialog-banner h3 {
  margin: 4px 0 0;
  color: #203551;
  font-size: 22px;
}

.dialog-kicker {
  margin: 0;
  color: #6a7f8b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.dialog-badge {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: #3b6688;
  font-size: 12px;
  font-weight: 700;
}

.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-textarea__inner),
.dialog-form :deep(.el-select__wrapper) {
  border-radius: 12px;
}

@media (max-width: 768px) {
  .filter-card,
  .table-card {
    padding-left: 14px;
    padding-right: 14px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .date-range-inline {
    width: 100%;
  }

  .pager-wrap {
    justify-content: center;
  }
}
</style>