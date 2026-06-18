<template>
  <div class="newcase-page">
    <!-- 顶部栏 -->
    <div class="hero-bar">
      <div class="hero-left">
        <div class="hero-icon">
          <el-icon :size="26"><User /></el-icon>
        </div>
        <div>
          <h1>新增患者</h1>
          <p>录入患者基本信息，用于后续的综合评估与报告生成</p>
        </div>
      </div>
      <div class="hero-glow"></div>
    </div>

    <!-- 表单卡 -->
    <div class="form-card">
      <div class="card-head">
        <div class="head-title">
          <div class="dot"></div>
          <h3>患者基础资料</h3>
          <span class="sub">Basic Information</span>
        </div>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="110px"
          label-position="right"
          class="main-form"
          @keyup.enter="submitForm"
      >
        <div class="form-section required-section">
          <div class="section-title">
            <span class="section-mark"></span>
            <strong>必填信息</strong>
          </div>
          <el-row :gutter="28">
            <el-col :xs="24" :md="12" :xl="6">
              <el-form-item label="病历号" prop="medicalCaseNo">
                <el-input
                    v-model="form.medicalCaseNo"
                    placeholder="请输入病历号"
                    maxlength="30"
                    clearable
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="6">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入姓名" maxlength="20" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="6">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender" class="gender-group">
                  <el-radio-button value="1">男</el-radio-button>
                  <el-radio-button value="2">女</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="6">
              <el-form-item label="出生日期" prop="birthday">
                <el-date-picker
                    v-model="form.birthday"
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

        <div class="form-section optional-section">
          <div class="section-title muted">
            <span class="section-mark"></span>
            <strong>补充信息</strong>
            <em>可稍后完善</em>
          </div>
          <el-row :gutter="28">
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="选填" maxlength="11" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" placeholder="选填" maxlength="18" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="社保卡号">
                <el-input v-model="form.socialCardNo" placeholder="选填" maxlength="30" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="身高(cm)">
                <el-input-number
                    v-model="form.height"
                    :min="30"
                    :max="250"
                    :step="0.1"
                    :precision="1"
                    :controls="false"
                    placeholder="选填"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="体重(kg)">
                <el-input-number
                    v-model="form.weight"
                    :min="2"
                    :max="300"
                    :step="0.1"
                    :precision="1"
                    :controls="false"
                    placeholder="选填"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>

      <!-- 底部操作 -->
      <div class="action-row">
        <el-button size="large" round plain @click="resetForm">
          <el-icon><RefreshLeft /></el-icon>
          <span>重置内容</span>
        </el-button>
        <el-button type="primary" size="large" round :loading="loading" @click="submitForm">
          <el-icon><Check /></el-icon>
          <span>保存患者信息</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Check, RefreshLeft } from '@element-plus/icons-vue'
import { insertPatient } from '@/api/patient'

const router = useRouter()

// ==================== 状态变量 ====================
const formRef = ref(null)
const loading = ref(false)

// 初始表单数据（抽离复用，重置时直接使用）
const INIT_FORM = {
  medicalCaseNo: '',
  name: '',
  gender: '',
  birthday: '',
  phone: '',
  idCard: '',
  socialCardNo: '',
  height: null,
  weight: null
}

const form = reactive({ ...INIT_FORM })

// ==================== 表单校验规则 ====================
// 手机号校验（选填：有值才校验格式）
function validateOptionalPhone(rule, value, callback) {
  if (!value || /^1[3-9]\d{9}$/.test(value)) {
    callback()
  } else {
    callback(new Error('请输入正确的手机号'))
  }
}

// 身份证号校验（选填：有值才校验格式）
function validateOptionalIdCard(rule, value, callback) {
  if (!value || /^(\d{15}|\d{17}[\dXx])$/.test(value)) {
    callback()
  } else {
    callback(new Error('身份证号格式不正确'))
  }
}

const rules = {
  medicalCaseNo: [{ required: true, message: '请输入病历号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  phone: [{ validator: validateOptionalPhone, trigger: 'blur' }],
  idCard: [{ validator: validateOptionalIdCard, trigger: 'blur' }]
}

// ==================== 工具方法 ====================
// 禁用未来日期
function disabledFuture(time) {
  return time.getTime() > Date.now()
}

// ==================== 业务方法 ====================
// 提交表单
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const payload = { ...form }
    const res = await insertPatient(payload)
    // 兼容两种返回格式：{code,msg,data} 或直接返回数据
    // const newId = res?.data?.id || res?.id || null
    ElMessage.success('患者信息已保存')
    // 保存成功后跳转至患者列表页
    router.push('/caseList')
  } finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  Object.assign(form, INIT_FORM)
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.newcase-page {
  min-height: calc(100vh - 84px);
  padding: 24px 28px 40px;
  background:
      radial-gradient(900px 420px at 8% -12%, rgba(37, 99, 235, 0.12), transparent 62%),
      radial-gradient(760px 360px at 105% 14%, rgba(20, 184, 166, 0.12), transparent 60%),
      linear-gradient(180deg, #f7faff 0%, #eef3fb 100%);
}

/* ========== 顶部栏 ========== */
.hero-bar {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  margin-bottom: 26px;
  border-radius: 20px;
  background:
      linear-gradient(135deg, rgba(255, 255, 255, 0.96) 0%, rgba(248, 250, 252, 0.92) 100%);
  border: 1px solid rgba(191, 219, 254, 0.72);
  box-shadow: 0 18px 44px rgba(30, 41, 59, 0.08);
}

.hero-left {
  position: relative;
  z-index: 1;
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
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb 0%, #14b8a6 100%);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.28);
}

.hero-left h1 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
  font-weight: 800;
}

.hero-left p {
  margin: 4px 0 0;
  font-size: 13.5px;
  color: #64748b;
}

.hero-glow {
  position: absolute;
  right: -80px;
  bottom: -140px;
  width: 340px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(20, 184, 166, 0.18), transparent 66%);
}

/* ========== 表单卡 ========== */
.form-card {
  background: #fff;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 20px;
  padding: 30px 34px 26px;
  box-shadow: 0 18px 44px rgba(30, 41, 59, 0.07);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px dashed rgba(148, 163, 184, 0.25);
}

.head-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-head .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #14b8a6);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}

.card-head h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
  font-weight: 800;
}

.card-head .sub {
  font-size: 12px;
  color: #94a3b8;
  letter-spacing: 0.1em;
}

/* ========== 表单主体 ========== */
.main-form {
  padding-top: 4px;
}

.form-section {
  padding: 20px 22px 4px;
  border-radius: 16px;
}

.required-section {
  margin-bottom: 18px;
  background: linear-gradient(135deg, #f8fbff, #ffffff);
  border: 1px solid #dbeafe;
}

.optional-section {
  background: #fbfdff;
  border: 1px solid #eef2f7;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 18px;
  color: #0f172a;
  font-size: 14px;
}

.section-title.muted {
  color: #334155;
}

.section-title em {
  font-style: normal;
  color: #94a3b8;
  font-size: 12px;
}

.section-mark {
  width: 7px;
  height: 18px;
  border-radius: 999px;
  background: linear-gradient(180deg, #2563eb, #14b8a6);
}

.main-form :deep(.el-form-item__label) {
  font-size: 14px;
  color: #334155;
  font-weight: 700;
}

.main-form :deep(.el-input__wrapper),
.main-form :deep(.el-textarea__inner),
.main-form :deep(.el-select__wrapper),
.main-form :deep(.el-date-editor) {
  min-height: 40px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 0 0 1px rgba(203, 213, 225, 0.78) inset;
  transition: all 0.18s ease;
}

.main-form :deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.main-form :deep(.el-input__wrapper:hover),
.main-form :deep(.el-select__wrapper:hover),
.main-form :deep(.el-date-editor:hover) {
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.55) inset;
}

.main-form :deep(.el-input__wrapper.is-focus),
.main-form :deep(.el-select__wrapper.is-focus),
.main-form :deep(.el-date-editor.is-focus) {
  box-shadow: 0 0 0 1px #3b82f6 inset, 0 4px 12px rgba(59, 130, 246, 0.15);
}

.main-form :deep(.el-form-item.is-required:not(.is-no-asterisk) .el-form-item__label::before) {
  color: #ef4444;
}

.gender-group :deep(.el-radio-button__inner) {
  min-width: 64px;
  padding: 10px 24px;
  font-weight: 700;
}

.gender-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 12px 0 0 12px;
}

.gender-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 12px 12px 0;
}

/* ========== 底部操作 ========== */
.action-row {
  margin-top: 24px;
  padding-top: 22px;
  border-top: 1px dashed rgba(148, 163, 184, 0.25);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.action-row :deep(.el-button) {
  height: 48px;
  min-width: 148px;
  font-weight: 800;
}

.action-row :deep(.el-button--primary) {
  background: linear-gradient(135deg, #2563eb, #3ba3ff);
  border: 0;
  box-shadow: 0 12px 22px rgba(37, 99, 235, 0.22);
}

@media (max-width: 768px) {
  .newcase-page {
    padding: 16px 12px 32px;
  }

  .hero-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 18px;
  }

  .card-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-section {
    padding: 18px 14px 2px;
  }

  .form-card {
    padding: 20px 16px;
  }

  .action-row {
    flex-direction: column-reverse;
  }

  .action-row .el-button {
    width: 100%;
  }
}
</style>