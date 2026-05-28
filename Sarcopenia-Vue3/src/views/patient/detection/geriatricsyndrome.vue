<template>
  <div class="full-page">
    <!-- 返回按钮 -->
    <div class="back-btn" @click="goBack">
      <span>&lt;</span> 返回
    </div>

    <!-- 评估类型选择卡片 -->
    <div class="card-wrapper">
      <!-- CGA综合评估 -->
      <div class="card card-dark">
        <div class="title">CGA筛查与评估</div>
        <el-button class="eval-btn" @click="goCga">去评估</el-button>
      </div>

      <!-- 自定义单项评估 -->
      <div class="card card-light">
        <div class="title">自定义单项评估</div>
        <el-button class="eval-btn btn-border" @click="goCustom">去评估</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 跳转到CGA综合评估页面
 * 携带当前路由的患者参数
 */
const goCga = () => {
  router.push({
    name: 'Cag',
    query: router.currentRoute.value.query
  })
}

/**
 * 进入自定义单项评估（待实现）
 */
const goCustom = () => {
  console.log('进入自定义单项评估')
}
</script>

<style scoped>
/* 全屏页面容器 */
.full-page {
  width: 100vw;
  height: 100vh;
  background-color: #ffffff;
  padding: 30px;
  box-sizing: border-box;
}

/* 返回按钮样式 */
.back-btn {
  width: fit-content;
  background-color: #ffe799;
  font-size: 18px;
  padding: 12px 30px;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  margin-bottom: 80px;
}

/* 卡片布局容器 */
.card-wrapper {
  display: flex;
  justify-content: center;
  gap: 50px;
}

/* 公共卡片样式 */
.card {
  width: 480px;
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

/* 深色卡片（CGA评估） */
.card-dark {
  background-color: #8dd460;
}

/* 浅色卡片（自定义评估） */
.card-light {
  background-color: #c9e9b6;
  border: 1px solid #8dd460;
}

/* 卡片标题 */
.title {
  font-size: 28px;
  margin-bottom: 45px;
}

/* 评估按钮 */
.eval-btn {
  background-color: #ffe799;
  border: none;
  padding: 12px 45px;
  font-size: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

/* 边框按钮样式 */
.btn-border {
  border: 1px solid #333 !important;
  box-shadow: none;
}
</style>