<template>
  <div class="page-container">
    <div class="card-box">
      <div class="card purple-card">
        <div class="card-icon">
          <el-icon><User /></el-icon>
        </div>
        <h3 class="card-title">肌少症诊断</h3>
        <p class="card-subtitle">SARCOPENIA DIAGNOSIS</p>
        <el-button class="card-btn" @click="goSarcopenia">进入诊断</el-button>
      </div>

      <div class="card purple-card">
        <div class="card-icon">
          <el-icon><Document /></el-icon>
        </div>
        <h3 class="card-title">老年综合评估</h3>
        <p class="card-subtitle">COMPREHENSIVE GERIATRIC ASSESSMENT</p>
        <el-button class="card-btn" @click="goGeriatric">进入评估</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {useRouter} from 'vue-router'
import {User, Document} from '@element-plus/icons-vue'

const router = useRouter()

// 这里修好了！！！
const goSarcopenia = () => {
  console.log("跳肌少症诊断页面")
  // 以后你写新页面再开路径
}

const goGeriatric = () => {
  router.push('/patient/detection/geriatricsyndrome')
}
</script>

<style scoped>
.page-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
}

.card-box {
  display: flex;
  gap: 80px;
}

.card {
  width: 420px;
  height: 560px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #fff;
  white-space: nowrap;
}

.purple-card {
  background-color: #9333ea;
}

.card-icon {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 35px;
}

.card-icon .el-icon {
  font-size: 52px;
  color: #fff;
}

.card-title {
  font-size: 30px;
  font-weight: 500;
  margin: 0 0 12px;
}

.card-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 70px;
  letter-spacing: 0.5px;
}

.card-btn {
  background: #fff;
  color: #9333ea;
  border: none;
  border-radius: 30px;
  padding: 12px 40px;
  font-size: 16px;
  cursor: pointer;
}

.card-btn:hover {
  background: #f3e8ff;
}
</style>
