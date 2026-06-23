<template>
  <div class="caselist-page">
    <!-- 顶部标题栏 -->
    <div class="hero-bar">
      <div class="hero-left">
        <div class="hero-icon">
          <el-icon :size="26"><UserFilled /></el-icon>
        </div>
        <div>
          <h1>患者管理</h1>
          <p>共 <b>{{ total }}</b> 位患者 · 最近更新 {{ latestTime }}</p>
        </div>
      </div>
      <div class="hero-right">
        <el-button type="primary" round @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增患者</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索过滤区 -->
    <div class="filter-card">
      <el-form :model="queryParams" inline label-width="68px" class="filter-form">
        <el-form-item label="病历号">
          <el-input
              v-model="queryParams.medicalCaseNo"
              placeholder="请输入病历号"
              clearable
              style="width: 180px"
              @keyup.enter="getList"
          />
        </el-form-item>
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
        <el-form-item label="性别">
          <el-select
              v-model="queryParams.gender"
              placeholder="全部"
              clearable
              style="width: 110px"
          >
            <el-option label="男" value="1" />
            <el-option label="女" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="添加日期">
          <el-date-picker
              v-model="queryParams.beginCreateTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
              style="width: 140px"
          />
          <span style="margin: 0 6px; color: #94a3b8">~</span>
          <el-date-picker
              v-model="queryParams.endCreateTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              style="width: 140px"
          />
        </el-form-item>
        <el-form-item>
          <el-button plain @click="resetQuery">
            <el-icon><RefreshLeft /></el-icon>
            <span>重置</span>
          </el-button>
          <el-button type="primary" @click="getList">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格区 -->
    <div class="table-card">
      <div v-loading="loading" class="table-shell">
        <el-table
            :data="patientList"
            stripe
            border
            row-key="id"
            :header-cell-style="headerStyle"
            :cell-style="cellStyle"
            class="patient-table"
        >
          <el-table-column label="病历号" prop="medicalCaseNo" min-width="120" align="center">
            <template #default="{ row }">
              <span class="case-badge">{{ row.medicalCaseNo || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="姓名" min-width="110" align="center">
            <template #default="{ row }">
              <div class="name-cell">{{ row.name || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="性别" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.gender === '2' ? 'danger' : 'primary'" effect="light" size="small">
                {{ row.gender === '2' ? '女' : '男' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="出生日期" min-width="160" align="center">
            <template #default="{ row }">
              <span>{{ row.birthday || '-' }}</span>
              <span v-if="calcAge(row.birthday) !== null" style="margin-left: 4px; color: #64748b">
                ({{ calcAge(row.birthday) }}岁)
              </span>
            </template>
          </el-table-column>
          <el-table-column label="手机号" min-width="140" align="center">
            <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
          </el-table-column>
          <el-table-column label="身高(cm)" width="100" align="center">
            <template #default="{ row }">{{ row.height || '-' }}</template>
          </el-table-column>
          <el-table-column label="体重(kg)" width="100" align="center">
            <template #default="{ row }">{{ row.weight || '-' }}</template>
          </el-table-column>
          <el-table-column label="添加时间" min-width="170" align="center">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" align="center" min-width="240">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleTest(row)">
                <el-icon><Notebook /></el-icon>
                <span>进入评估</span>
              </el-button>
              <el-button link type="warning" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && patientList.length === 0" description="暂无患者数据" />

        <div v-if="patientList.length > 0" class="pager-wrap">
          <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[5, 10, 20, 50]"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="getList"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogMode === 'create' ? '新增患者' : '编辑患者信息'"
        width="880px"
        top="30vh"
        :close-on-click-modal="false"
        class="patient-dialog"
        append-to-body
        destroy-on-close
    >
      <div class="dialog-inner">
        <div class="dialog-head">
          <div class="dialog-ico" :class="dialogMode">
            <el-icon :size="22"><User /></el-icon>
          </div>
          <div>
            <h4>{{ dialogMode === 'create' ? '新增患者' : '编辑患者信息' }}</h4>
            <p>带 <span class="req-mark">*</span> 为必填项，其他可留空</p>
          </div>
        </div>

        <el-form
            ref="dialogFormRef"
            :model="dialogForm"
            :rules="rules"
            label-width="110px"
            label-position="right"
            class="dialog-form"
        >
          <div class="form-section">
            <div class="section-title req-section">
              <span class="section-dot"></span>
              <span>必填信息</span>
            </div>
            <el-row :gutter="24">
              <el-col :xs="24" :md="12">
                <el-form-item label="病历号" prop="medicalCaseNo">
                  <el-input v-model="dialogForm.medicalCaseNo" placeholder="请输入病历号" maxlength="30" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="dialogForm.name" placeholder="请输入姓名" maxlength="20" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="dialogForm.gender">
                    <el-radio-button value="1">男</el-radio-button>
                    <el-radio-button value="2">女</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="出生日期" prop="birthday">
                  <el-date-picker
                      v-model="dialogForm.birthday"
                      type="date"
                      value-format="YYYY-MM-DD"
                      placeholder="请选择出生日期"
                      :disabled-date="disabledFuture"
                      style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="form-section">
            <div class="section-title opt-section">
              <span class="section-dot opt"></span>
              <span>选填信息</span>
            </div>
            <el-row :gutter="24">
              <el-col :xs="24" :md="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="dialogForm.phone" placeholder="选填" maxlength="11" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="dialogForm.idCard" placeholder="选填" maxlength="18" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="社保卡号">
                  <el-input v-model="dialogForm.socialCardNo" placeholder="选填" maxlength="30" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="身高(cm)">
                  <el-input
                      v-model="dialogForm.height"
                      type="number"
                      placeholder="选填"
                      style="width: 100%"
                      maxlength="6"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="体重(kg)">
                  <el-input
                      v-model="dialogForm.weight"
                      type="number"
                      placeholder="选填"
                      style="width: 100%"
                      maxlength="6"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" round :loading="submitLoading" @click="submitDialog">
            {{ dialogMode === 'create' ? '新增' : '保存修改' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
        v-model="delDialogVisible"
        width="440px"
        top="50%"
        class="patient-dialog delete-dialog"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <div class="dialog-inner">
        <div class="dialog-head">
          <div class="dialog-ico delete">
            <el-icon :size="22"><Warning /></el-icon>
          </div>
          <div>
            <h4>删除提示</h4>
            <p>确认删除患者「{{ delRow?.name }}」吗？删除后数据可在回收站恢复</p>
          </div>
        </div>
        <div class="delete-tip">
          <el-icon :size="14"><InfoFilled /></el-icon>
          <span>此操作会从当前评估结果一起删除，请确认后再操作。</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="delDialogVisible = false">取消</el-button>
          <el-button type="danger" round :loading="delLoading" @click="confirmDelete">确定删除</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, UserFilled, Plus, Notebook, Edit, Delete, Search, RefreshLeft, Warning, InfoFilled } from '@element-plus/icons-vue'
import { selectPatientList, insertPatient, updatePatient, deletePatientById } from '@/api/patient'

const router = useRouter()

// 状态变量
const loading = ref(false)
const submitLoading = ref(false)
const patientList = ref([])
const total = ref(0) // 总数据条数
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogFormRef = ref(null)
const delDialogVisible = ref(false)
const delRow = ref(null)
const delLoading = ref(false)

// 查询参数
const queryParams = reactive({
  medicalCaseNo: '',
  name: '',
  phone: '',
  gender: '',
  beginCreateTime: '',
  endCreateTime: ''
})

// 表单数据
const dialogForm = reactive({
  id: null,
  medicalCaseNo: '',
  name: '',
  gender: '',
  birthday: '',
  phone: '',
  idCard: '',
  socialCardNo: '',
  height: null,
  weight: null
})

// 表单校验规则
const rules = {
  medicalCaseNo: [{ required: true, message: '请输入病历号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  idCard: [{ pattern: /^(\d{15}|\d{17}[\dXx])?$/, message: '身份证号格式不正确', trigger: 'blur' }]
}

// ==================== 计算属性 ====================
// 最新更新时间（基于当前页数据，列表按创建时间倒序时第一页为全局最新）
const latestTime = computed(() => {
  if (!patientList.value.length) return '-'
  const timeList = patientList.value
      .map(item => item.createTime ? new Date(item.createTime).getTime() : 0)
      .filter(time => time > 0)
  if (!timeList.length) return '-'
  return formatTime(new Date(Math.max(...timeList)))
})

// ==================== 工具方法 ====================
// 禁用未来日期
function disabledFuture(time) {
  return time.getTime() > Date.now()
}

// 根据出生日期计算年龄
function calcAge(birthday) {
  if (!birthday) return null
  const birthDate = new Date(birthday)
  if (Number.isNaN(birthDate.getTime())) return null

  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}

// 手机号脱敏
function maskPhone(phone) {
  if (!phone) return '-'
  return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

// 时间格式化
function formatTime(time) {
  if (!time) return '-'
  if (typeof time === 'string') return time
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return '-'

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 重置表单
function resetForm() {
  Object.assign(dialogForm, {
    id: null,
    medicalCaseNo: '',
    name: '',
    gender: '',
    birthday: '',
    phone: '',
    idCard: '',
    socialCardNo: '',
    height: null,
    weight: null
  })
  dialogFormRef.value?.clearValidate()
}

// ==================== 业务方法 ====================
// 获取患者列表（后端分页）
async function getList() {
  loading.value = true
  try {
    // 过滤空值参数 + 拼接分页参数
    const params = {}
    Object.entries(queryParams).forEach(([key, value]) => {
      if (value !== '' && value != null) {
        params[key] = value
      }
    })
    params.pageNum = pageNum.value
    params.pageSize = pageSize.value

    const res = await selectPatientList(params)
    // 从 PageInfo 中取出当前页数据和总条数
    patientList.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 重置查询条件
function resetQuery() {
  Object.assign(queryParams, {
    medicalCaseNo: '',
    name: '',
    phone: '',
    gender: '',
    beginCreateTime: '',
    endCreateTime: ''
  })
  pageNum.value = 1
  getList()
}

// 每页条数变化时重置到第一页
function handleSizeChange() {
  pageNum.value = 1
  getList()
}

// 打开新增弹窗
function handleAdd() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
function handleEdit(row) {
  dialogMode.value = 'edit'
  Object.assign(dialogForm, {
    id: row.id,
    medicalCaseNo: row.medicalCaseNo || '',
    name: row.name || '',
    gender: String(row.gender || ''),
    birthday: row.birthday || '',
    phone: row.phone || '',
    idCard: row.idCard || '',
    socialCardNo: row.socialCardNo || '',
    height: row.height ?? null,
    weight: row.weight ?? null
  })
  dialogVisible.value = true
}

// 删除患者
function handleDelete(row) {
  delRow.value = row
  delDialogVisible.value = true
}

async function confirmDelete() {
  delLoading.value = true
  try {
    await deletePatientById({ id: delRow.value.id })
    ElMessage.success('删除成功')
    delDialogVisible.value = false
    getList()
  } catch (e) {
    // 接口异常
  } finally {
    delLoading.value = false
  }
}

// 进入评估页面
function handleTest(row) {
  router.push({
    path: '/patient/detection/index',
    query: {
      patientId: row.id,
      name: row.name,
      medicalCaseNo: row.medicalCaseNo,
      gender: row.gender,
      birthday: row.birthday,
      phone: row.phone,
      height: row.height,
      weight: row.weight
    }
  })
}

// 提交表单
async function submitDialog() {
  const valid = await dialogFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = { ...dialogForm }
    if (dialogMode.value === 'edit' && dialogForm.id) {
      await updatePatient(payload)
      ElMessage.success('修改成功')
    } else {
      await insertPatient(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.caselist-page {
  min-height: calc(100vh - 84px);
  padding: 24px 28px 40px;
  background:
      radial-gradient(1200px 500px at 10% -20%, rgba(59, 130, 246, 0.08), transparent 60%),
      radial-gradient(1000px 400px at 110% 20%, rgba(99, 102, 241, 0.08), transparent 60%),
      linear-gradient(180deg, #f7faff 0%, #eef3fb 100%);
}

/* 顶部标题栏 */
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

.hero-left h1 {
  margin: 0;
  font-size: 22px;
  color: #1e293b;
  font-weight: 700;
}

.hero-left p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.hero-left p b {
  color: #3b82f6;
  font-weight: 600;
  margin: 0 2px;
}

/* 搜索过滤区 */
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

/* 数据表格区 */
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

.case-badge {
  display: inline-block;
  padding: 3px 10px;
  background: #eef2ff;
  color: #4f46e5;
  border-radius: 6px;
  font-family: monospace;
  font-size: 13px;
  font-weight: 600;
}

.name-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #1e293b;
  font-weight: 500;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 14px 8px 8px;
}

/* 弹窗样式 */
.patient-dialog :deep(.el-dialog) {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.15) !important;
}

.patient-dialog :deep(.el-dialog__body) {
  padding: 0 28px 22px;
}

.patient-dialog :deep(.el-dialog__header) {
  padding: 18px 28px 16px;
  margin-right: 0;
  background: linear-gradient(135deg, #f8fafc 0%, #eef3fb 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.08);
}

.patient-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.dialog-inner {
  padding-top: 18px;
}

// 删除弹窗垂直居中
.delete-dialog:deep(.el-dialog) {
  margin-top: 0 !important;
  transform: translateY(-50%) !important;
}

.delete-dialog:deep(.el-dialog__wrapper) {
  display: flex;
  align-items: center;
  justify-content: center;
}
.delete-dialog:deep(.el-dialog) {
  top: auto !important;
  margin: 0 !important;
}

.dialog-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  margin-bottom: 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #eef4ff 0%, #f5f3ff 100%);
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.dialog-ico {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, #22c55e, #10b981);
  box-shadow: 0 6px 14px rgba(34, 197, 94, 0.25);
}

.dialog-ico.edit {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  box-shadow: 0 6px 14px rgba(59, 130, 246, 0.25);
}

.dialog-ico.delete {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  box-shadow: 0 6px 14px rgba(239, 68, 68, 0.25);
}

.delete-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: 12px;
  color: #92400e;
  font-size: 12.5px;
  line-height: 1.6;
}

.delete-dialog :deep(.dialog-head) {
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%);
  border-color: rgba(245, 158, 11, 0.2);
}

.dialog-head h4 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
  font-weight: 700;
}

.dialog-head p {
  margin: 3px 0 0;
  font-size: 12px;
  color: #64748b;
}

.req-mark {
  color: #ef4444;
  font-weight: 700;
  margin: 0 1px;
}

/* 表单分组 */
.dialog-form {
  padding-top: 0;
}

.form-section {
  margin-bottom: 6px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0 12px;
  margin: 4px 0 8px;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  letter-spacing: 0.3px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.section-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ef4444, #f97316);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
}

.section-dot.opt {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.dialog-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-select__wrapper),
.dialog-form :deep(.el-date-editor) {
  border-radius: 10px;
  transition: all 0.18s ease;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.35) inset;
}

.dialog-form :deep(.el-input__wrapper:hover),
.dialog-form :deep(.el-select__wrapper:hover),
.dialog-form :deep(.el-date-editor:hover) {
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.55) inset;
}

.dialog-form :deep(.el-input__wrapper.is-focus),
.dialog-form :deep(.el-select__wrapper.is-focus),
.dialog-form :deep(.el-date-editor.is-focus) {
  box-shadow: 0 0 0 1px #3b82f6 inset, 0 4px 12px rgba(59, 130, 246, 0.15) !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
}

/* 响应式适配 */
@media (max-width: 900px) {
  .caselist-page {
    padding: 16px 12px 32px;
  }

  .hero-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 18px;
  }

  .patient-dialog :deep(.el-dialog__body) {
    padding: 0 16px 20px;
  }
}
</style>
