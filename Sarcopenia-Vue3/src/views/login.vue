<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-layer">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- 左侧品牌区（大屏显示） -->
    <div class="hero-left">
      <div class="hero-logo">
        <span class="logo-icon">🏥</span>
        <span class="logo-name">众享健康</span>
      </div>
      <h1 class="hero-title">
        肌少症数字化<br />评估与干预系统
      </h1>
      <p class="hero-sub">专业 · 精准 · 高效，为老年健康保驾护航</p>

      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-dot"></div>
          <div>
            <div class="feature-name">综合评估</div>
            <div class="feature-desc">覆盖躯体、认知、营养等多维度</div>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-dot"></div>
          <div>
            <div class="feature-name">智能报告</div>
            <div class="feature-desc">一键生成标准化评估报告</div>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-dot"></div>
          <div>
            <div class="feature-name">数据可视化</div>
            <div class="feature-desc">直观展现患者健康数据趋势</div>
          </div>
        </div>
      </div>

      <div class="hero-footer">© {{ nowYear }} 众享健康  保留所有权利</div>
    </div>

    <!-- 右侧登录卡片 -->
    <div class="login-card">
      <div class="card-head">
        <h2 class="card-title">欢迎回来</h2>
        <p class="card-sub">请登录您的账号以继续</p>
      </div>

      <el-form
        ref="loginRef"
        :model="loginForm"
        :rules="loginRules"
        class="form-area"
        autocomplete="off"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            size="large"
            placeholder="请输入账号"
            auto-complete="username"
            class="login-input"
          >
            <template #prefix>
              <el-icon class="prefix-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="passwordType"
            size="large"
            placeholder="请输入密码"
            auto-complete="current-password"
            class="login-input"
            show-password
          >
            <template #prefix>
              <el-icon class="prefix-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <template v-if="captchaEnabled">
          <el-form-item prop="code">
            <div class="captcha-wrap">
              <el-input
                v-model="loginForm.code"
                size="large"
                placeholder="验证码"
                maxlength="4"
                auto-complete="off"
                class="login-input"
              >
                <template #prefix>
                  <el-icon class="prefix-icon"><Key /></el-icon>
                </template>
              </el-input>
              <img :src="codeUrl" class="captcha" @click="getCode" title="点击刷新" alt="验证码" />
            </div>
          </el-form-item>
        </template>

        <div class="remember-row">
          <el-checkbox v-model="loginForm.rememberMe" size="large">记住密码</el-checkbox>
          <span class="forgot-btn" @click="forgotPassword">忘记密码？</span>
        </div>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="login-btn"
          @click.prevent="handleLogin"
        >
          <template v-if="!loading">
            <el-icon><Right /></el-icon>
            <span>立即登录</span>
          </template>
          <span v-else>登录中...</span>
        </el-button>

        <div v-if="register" class="register-row">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock, Key, Right } from '@element-plus/icons-vue'
import { getCodeImg } from '@/api/login'
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const nowYear = new Date().getFullYear()
const loginRef = ref(null)
const passwordType = ref('password')
const codeUrl = ref('')
const loading = ref(false)
const captchaEnabled = ref(true)
const register = ref(false)

const loginForm = ref({
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
  code: '',
  uuid: ''
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
  code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
}

function handleLogin() {
  loginRef.value.validate(valid => {
    if (!valid) return
    loading.value = true
    if (loginForm.value.rememberMe) {
      Cookies.set('username', loginForm.value.username, { expires: 30 })
      Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
      Cookies.set('rememberMe', loginForm.value.rememberMe, { expires: 30 })
    } else {
      Cookies.remove('username')
      Cookies.remove('password')
      Cookies.remove('rememberMe')
    }
    userStore.login(loginForm.value).then(() => {
      const query = route.query
      const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') acc[cur] = query[cur]
        return acc
      }, {})
      router.push({ path: query.redirect || '/', query: otherQueryParams })
    }).catch(() => {
      loading.value = false
      if (captchaEnabled.value) getCode()
    })
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
    code: '',
    uuid: loginForm.value.uuid
  }
}

function forgotPassword() {
  alert('请联系管理员重置密码')
}

onMounted(() => {
  getCode()
  getCookie()
})
</script>

<style scoped lang="scss">
.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 680px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 50%, #f5f0ff 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

/* ===== 背景装饰 ===== */
.bg-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
  animation: float 18s ease-in-out infinite;
}

.blob-1 {
  width: 480px;
  height: 480px;
  background: radial-gradient(circle, #a5b4fc 0%, transparent 70%);
  top: -180px;
  right: -120px;
  animation-delay: 0s;
}

.blob-2 {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, #93c5fd 0%, transparent 70%);
  bottom: -180px;
  left: -120px;
  animation-delay: -6s;
}

.blob-3 {
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, #c4b5fd 0%, transparent 70%);
  top: 50%;
  left: 45%;
  transform: translate(-50%, -50%);
  animation-delay: -12s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.04) 1px, transparent 1px);
  background-size: 40px 40px;
}

/* ===== 左侧品牌区 ===== */
.hero-left {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 520px;
  padding: 40px 60px;
  margin-right: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  animation: slideInLeft 0.8s ease-out;
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-30px); }
  to { opacity: 1; transform: translateX(0); }
}

.hero-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
}

.logo-icon {
  font-size: 28px;
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
}

.logo-name {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 1px;
}

.hero-title {
  font-size: 44px;
  font-weight: 800;
  line-height: 1.25;
  margin: 0 0 20px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3730a3 50%, #5b21b6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 48px;
  line-height: 1.6;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: transform 0.2s ease;

  &:hover {
    transform: translateX(4px);
  }
}

.feature-dot {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  border-radius: 10px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: 12px;
    border-radius: 4px;
    background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  }
}

.feature-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
}

.feature-desc {
  font-size: 12.5px;
  color: #64748b;
}

.hero-footer {
  margin-top: 48px;
  font-size: 12px;
  color: #94a3b8;
}

/* ===== 登录卡片 ===== */
.login-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 48px 44px 40px;
  box-shadow:
    0 20px 60px rgba(59, 130, 246, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  animation: slideInRight 0.8s ease-out;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.card-head {
  margin-bottom: 32px;
}

.card-title {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 8px;
}

.card-sub {
  font-size: 13.5px;
  color: #64748b;
  margin: 0;
}

/* ===== 表单 ===== */
.form-area {
  margin-bottom: 8px;
}

.login-input {
  :deep(.el-input__wrapper) {
    height: 48px;
    border-radius: 12px;
    background: #f8fafc;
    padding: 0 16px;
    box-shadow: none;
    border: 1.5px solid transparent;
    transition: all 0.2s ease;
  }

  :deep(.el-input__wrapper:hover) {
    border-color: #c7d2fe;
    background: #fff;
  }

  :deep(.el-input__wrapper.is-focus) {
    border-color: #3b82f6;
    background: #fff;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.12);
  }

  :deep(.el-input__inner) {
    font-size: 14.5px;
    color: #1e293b;
  }
}

.prefix-icon {
  color: #94a3b8;
  font-size: 18px;
}

/* 验证码 */
.captcha-wrap {
  display: flex;
  gap: 12px;
  width: 100%;
  align-items: center;
}

.captcha-wrap .login-input {
  flex: 1;
}

.captcha {
  width: 118px;
  height: 48px;
  border-radius: 12px;
  cursor: pointer;
  object-fit: cover;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    border-color: #3b82f6;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }
}

/* 记住 & 忘记 */
.remember-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 24px;
  font-size: 13px;

  :deep(.el-checkbox__label) {
    color: #64748b;
  }

  :deep(.el-checkbox__inner) {
    border-radius: 4px;
  }
}

.forgot-btn {
  color: #3b82f6;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;

  &:hover {
    color: #1d4ed8;
    text-decoration: underline;
  }
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: none;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.25s ease;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

/* 注册 */
.register-row {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #94a3b8;

  a {
    color: #3b82f6;
    margin-left: 4px;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .hero-left {
    display: none;
  }

  .login-page {
    background: linear-gradient(135deg, #e0e7ff 0%, #f0f4ff 100%);
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 36px 24px 28px;
    border-radius: 16px;
  }

  .card-title {
    font-size: 22px;
  }

  .captcha {
    width: 100px;
  }
}
</style>
