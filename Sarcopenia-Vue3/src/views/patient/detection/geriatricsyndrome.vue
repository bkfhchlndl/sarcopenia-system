<template>
  <div class="full-page">
    <div class="back-btn" @click="goBack">
      <span>&lt;</span> 返回
    </div>

    <div class="card-wrapper">
      <div class="card card-dark">
        <div class="title">CGA筛查与评估</div>
        <el-button class="eval-btn" @click="goCga">去评估</el-button>
      </div>

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

const goBack = () => {
  router.back()
}

const goCga = () => {
  console.log('进入CGA筛查与评估')
}

const goCustom = () => {
  console.log('进入自定义单项评估')
}
</script>

<style scoped>
.full-page {
  width: 100vw;
  height: 100vh;
  background-color: #ffffff;
  padding: 30px;
  box-sizing: border-box;
}

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

.card-wrapper {
  display: flex;
  justify-content: center;
  gap: 50px;
}

.card {
  width: 480px;
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.card-dark {
  background-color: #8dd460;
}

.card-light {
  background-color: #c9e9b6;
  border: 1px solid #8dd460;
}

.title {
  font-size: 28px;
  margin-bottom: 45px;
}

.eval-btn {
  background-color: #ffe799;
  border: none;
  padding: 12px 45px;
  font-size: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.btn-border {
  border: 1px solid #333 !important;
  box-shadow: none;
}
</style>
