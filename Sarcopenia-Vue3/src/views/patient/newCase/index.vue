<template>
  <div class="app-container create-page">
    <section class="create-hero">
      <div>
        <p class="hero-kicker">New Profile</p>
        <h1>新增用户</h1>
        <p>用更清晰的分区录入用户信息，减少遗漏，也方便后续评估流程直接接入。</p>
      </div>
      <div class="hero-note">
        <span class="note-dot"></span>
        基础信息填写完成后可立即进入后续检测
      </div>
    </section>

    <section class="form-card">
      <el-form
        ref="patientFormRef"
        :model="patientForm"
        :rules="rules"
        label-width="86px"
        class="patient-form"
      >
        <div class="section-title">基础资料</div>
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="病历号" prop="caseNo">
              <el-input v-model="patientForm.caseNo" placeholder="请输入病历号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="patientForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="patientForm.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男性" value="1" />
                <el-option label="女性" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="出生年月" prop="birthday">
              <el-date-picker
                v-model="patientForm.birthday"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择出生日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="section-title">联系与体征</div>
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="patientForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="身高" prop="height">
              <el-input v-model="patientForm.height" placeholder="请输入身高(cm)" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="家庭住址" prop="address">
          <el-input
            v-model="patientForm.address"
            type="textarea"
            :rows="4"
            placeholder="请输入家庭住址"
          />
        </el-form-item>

        <div class="action-bar">
          <el-button type="primary" class="primary-btn" @click="submitForm">提交保存</el-button>
          <el-button class="ghost-btn" @click="resetForm">重置内容</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { insertPatient } from '@/api/patient'

const patientFormRef = ref(null)

const createEmptyForm = () => ({
  caseNo: '',
  name: '',
  gender: '',
  birthday: null,
  phone: '',
  height: '',
  address: ''
})

const patientForm = ref(createEmptyForm())

const rules = ref({
  caseNo: [{ required: true, message: '请输入病历号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择出生年月', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }],
  address: [{ required: true, message: '请输入家庭住址', trigger: 'blur' }]
})

function submitForm() {
  patientFormRef.value.validate((valid) => {
    if (!valid) return
    insertPatient(patientForm.value)
      .then(() => {
        ElMessage.success('新增成功')
        resetForm()
      })
      .catch(() => {
        ElMessage.error('提交失败')
      })
  })
}

function resetForm() {
  patientForm.value = createEmptyForm()
  patientFormRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.create-page {
  min-height: calc(100vh - 84px);
  background:
    radial-gradient(circle at top right, rgba(255, 214, 153, 0.16), transparent 24%),
    radial-gradient(circle at left top, rgba(143, 203, 167, 0.18), transparent 28%),
    linear-gradient(180deg, #f9fbff 0%, #eef5fb 100%);
}

.create-hero,
.form-card {
  border: 1px solid rgba(172, 194, 216, 0.72);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 20px 44px rgba(88, 117, 151, 0.12);
}

.create-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
  padding: 28px 30px;
  background:
    linear-gradient(135deg, rgba(255, 242, 213, 0.92), rgba(228, 241, 255, 0.95)),
    #fff;
}

.create-hero h1 {
  margin: 6px 0 0;
  color: #21344d;
  font-size: 32px;
}

.create-hero p {
  margin: 8px 0 0;
  color: #5c7088;
}

.hero-kicker {
  margin: 0;
  color: #7c7458;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-note {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  color: #4f6880;
  font-size: 13px;
}

.note-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7bc96f, #f2b94b);
}

.form-card {
  padding: 26px;
}

.section-title {
  margin-bottom: 16px;
  color: #233955;
  font-size: 18px;
  font-weight: 700;
}

.section-title:not(:first-child) {
  margin-top: 10px;
}

.patient-form :deep(.el-input__wrapper),
.patient-form :deep(.el-textarea__inner),
.patient-form :deep(.el-select__wrapper) {
  border-radius: 14px;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-top: 26px;
}

.primary-btn,
.ghost-btn {
  min-width: 120px;
  height: 44px;
  border-radius: 14px;
}

.primary-btn {
  box-shadow: 0 14px 24px rgba(64, 138, 255, 0.2);
}

@media (max-width: 768px) {
  .create-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-card {
    padding: 18px 14px;
  }

  .action-bar {
    flex-direction: column;
  }
}
</style>
