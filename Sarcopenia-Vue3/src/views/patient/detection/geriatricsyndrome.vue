<template>
  <div class="full-page">
    <!-- 返回按钮 -->
    <el-button class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      返回
    </el-button>

    <!-- 评估类型选择卡片 -->
    <div class="card-wrapper">
      <!-- CGA综合评估 -->
      <div class="entry-card cga-card">
        <div class="card-icon">
          <el-icon><Document /></el-icon>
        </div>
        <h3 class="card-title">CGA筛查与评估</h3>
        <p class="card-subtitle">COMPREHENSIVE GERIATRIC ASSESSMENT</p>
        <el-button class="card-btn" @click="goCga">去评估</el-button>
      </div>

      <!-- 自定义单项评估 -->
      <div class="entry-card custom-card">
        <div class="card-icon">
          <el-icon><EditPen /></el-icon>
        </div>
        <h3 class="card-title">自定义单项评估</h3>
        <p class="card-subtitle">CUSTOM SINGLE ASSESSMENT</p>
        <el-button class="card-btn" @click="goCustom">去评估</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Document, EditPen, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 跳转到CGA综合评估页面
 * 携带当前路由的患者参数和项目ID
 */
const goCga = () => {
  router.push({
    name: 'Cga',
    query: router.currentRoute.value.query
  })
}

/**
 * 进入自定义单项评估（待实现）
 */
const goCustom = () => {
  console.log('进入自定义单项评估')
  router.push({
    name: 'Customize',
    query: router.currentRoute.value.query
  })
}
</script>

<style scoped lang="scss">
.full-page {
  min-height: calc(100vh - 84px);
  padding-bottom: 30px;
  background: linear-gradient(180deg, #f7fbff 0%, #eef4fb 100%);
  box-sizing: border-box;
}

.back-btn {
  margin: 30px 0 60px;
  font-size: 15px;
  padding: 10px 20px;
  border-radius: 8px;
  background: #fff;
  color: #29465f;
  border: 1px solid #d0d7de;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: #2f9a75;
    border-color: #2f9a75;
    background: #f0faf5;
  }
}

.card-wrapper {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 22px;
  max-width: 900px;
  margin: 0 auto;
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

.cga-card {
  background: #2f9a75;
}

.custom-card {
  background: #7964d7;
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
  .card-wrapper {
    grid-template-columns: 1fr;
  }
}
</style>
