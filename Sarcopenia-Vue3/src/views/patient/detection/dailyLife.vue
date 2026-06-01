<template>
  <div class="barthel-container">
    <!-- 顶部导航 -->
    <div class="header">
      <el-button type="primary" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-title">
        <h2>{{ scaleData.scaleName }}</h2>
        <span class="subtitle">Barthel ADL 评定量表</span>
      </div>
      <div class="patient-info">
        <span class="name">{{ patientName }}</span>
      </div>
    </div>

    <!-- 评估表格 -->
    <div class="assessment-table">
      <table>
        <thead>
        <tr>
          <th class="col-seq">序号</th>
          <th class="col-item">项目</th>
          <th class="col-content">评估内容</th>
          <th class="col-score">分值</th>
          <th class="col-select">选择</th>
        </tr>
        </thead>
        <tbody>
        <template v-for="(question, qIndex) in scaleData.questionList" :key="question.questionId">
          <tr v-for="(option, oIndex) in question.optionList" :key="option.id"
              :class="{ 'first-option': oIndex === 0, 'selected': answers[question.questionId] == option.id }">
            <!-- 序号（第一行显示） -->
            <td v-if="oIndex === 0" :rowspan="question.optionList.length" class="col-seq">
              {{ qIndex + 1 }}
            </td>
            <!-- 项目（第一行显示） -->
            <td v-if="oIndex === 0" :rowspan="question.optionList.length" class="col-item">
              {{ question.title }}
            </td>
            <!-- 评估内容 -->
            <td class="col-content">{{ option.content }}</td>
            <!-- 分值 -->
            <td class="col-score">{{ option.score }} 分</td>
            <!-- 选择 -->
            <td class="col-select">
              <el-radio
                  :model-value="answers[question.questionId]?.[0]"
                  @update:model-value="(val) => { answers[question.questionId] = [val] }"
                  :label="option.id"
                  size="large"
              >
                {{ option.label }}
              </el-radio>
            </td>
          </tr>
        </template>
        </tbody>
      </table>
    </div>

    <!-- 底部结果 -->
    <div class="result-section">
      <div class="result-left">
        <div class="result-item">
          <span class="label">总分：</span>
          <span class="score">{{ totalScore }}</span>
          <span class="unit">分</span>
        </div>
        <div class="result-item">
          <span class="label">评估结果：</span>
          <span class="level" :style="{ color: dependencyLevel.color }">
            {{ dependencyLevel.text }}
          </span>
        </div>
      </div>
      <div class="result-right">
        <div class="standard">
          <div class="standard-title">评判标准：</div>
          <div class="standard-item">≤40分：重度依赖</div>
          <div class="standard-item">41~60分：中度依赖</div>
          <div class="standard-item">61~99分：轻度依赖</div>
          <div class="standard-item">100分：无依赖</div>
        </div>
      </div>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-section">
      <el-button type="primary" size="large" @click="submitAssessment">
        <el-icon><Check /></el-icon>
        提交评估
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { selectDailyLifeScale, insertCgaRecord} from '@/api/cga.js'

const route = useRoute()
const router = useRouter()

// 患者信息
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'

// 量表数据（从后端获取）
const scaleData = ref({
  scaleId: null,
  scaleName: '',
  code: '',
  questionList: []
})

// 加载量表数据
const loadScaleData = async () => {
  try {
    const res = await selectDailyLifeScale()
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 后端返回的是列表，取第一个元素
      scaleData.value = res.data[0]
    }
  } catch (error) {
    ElMessage.error('加载量表数据失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadScaleData()
})

// 答案存储（格式：{ questionId: [optionId] }）
const answers = ref({})

// 计算总分
const totalScore = computed(() => {
  let sum = 0
  for (const questionId in answers.value) {
    const optionId = answers.value[questionId]
    const question = scaleData.value.questionList.find(q => q.questionId == questionId)
    if (question) {
      const option = question.optionList.find(o => o.id == optionId)
      if (option) {
        sum += option.score
      }
    }
  }
  return sum
})

// 计算依赖程度
const dependencyLevel = computed(() => {
  const score = totalScore.value
  if (score <= 40) return { text: '重度依赖', color: '#ef4444' }
  if (score <= 60) return { text: '中度依赖', color: '#f97316' }
  if (score <= 99) return { text: '轻度依赖', color: '#3b82f6' }
  return { text: '无依赖', color: '#22c55e' }
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 提交评估
const submitAssessment = async () => {
  // 检查是否全部作答
  const answeredCount = Object.keys(answers.value).length
  const totalCount = scaleData.value.questionList?.length || 0

  if (answeredCount < totalCount) {
    ElMessage.warning(`还有 ${totalCount - answeredCount} 道题未作答`)
    return
  }

  // 构建提交数据 - 确保格式正确
  const formattedAnswers = {}
  for (const questionId in answers.value) {
    const optionId = answers.value[questionId]
    // 确保questionId是字符串，optionId是数组
    formattedAnswers[String(questionId)] = [Number(optionId)]
  }

  const dto = {
    patientId: Number(patientId),
    projectId: 2,  // 日常生活评估项目ID
    answers: formattedAnswers  // { "questionId": [optionId] }
  }

  console.log('提交数据:', JSON.stringify(dto, null, 2))

  try {
    const res = await insertCgaRecord(dto)
    if (res.code === 200) {
      ElMessage.success('评估提交成功')
      setTimeout(() => router.back(), 1500)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  }
}
</script>

<style scoped lang="scss">
.barthel-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .header-title {
    flex: 1;

    h2 {
      margin: 0;
      font-size: 18px;
      color: #333;
    }

    .subtitle {
      font-size: 14px;
      color: #666;
    }
  }

  .patient-info {
    .name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
}

.assessment-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;

  table {
    width: 100%;
    border-collapse: collapse;

    th, td {
      padding: 12px 16px;
      text-align: left;
      border: 1px solid #e5e7eb;
    }

    th {
      background: #f8fafc;
      font-weight: 600;
      color: #374151;
    }

    .col-seq {
      width: 60px;
      text-align: center;
    }

    .col-item {
      width: 25%;
    }

    .col-content {
      width: 35%;
    }

    .col-score {
      width: 80px;
      text-align: center;
    }

    .col-select {
      width: 100px;
      text-align: center;
    }

    tbody {
      tr {
        &:hover {
          background: #f8fafc;
        }

        &.selected {
          background: #eff6ff;
        }
      }

      td {
        vertical-align: top;
      }
    }
  }
}

.result-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;

  .result-left {
    .result-item {
      margin-bottom: 12px;

      .label {
        font-size: 14px;
        color: #666;
      }

      .score {
        font-size: 32px;
        font-weight: 700;
        color: #9333ea;
      }

      .unit {
        font-size: 14px;
        color: #666;
      }

      .level {
        font-size: 20px;
        font-weight: 600;
      }
    }
  }

  .result-right {
    .standard {
      background: #f8fafc;
      padding: 16px;
      border-radius: 8px;

      .standard-title {
        font-weight: 600;
        color: #374151;
        margin-bottom: 8px;
      }

      .standard-item {
        font-size: 13px;
        color: #666;
        line-height: 1.8;
      }
    }
  }
}

.submit-section {
  text-align: center;
  padding: 20px;

  .el-button {
    background: #9333ea;
    border-color: #9333ea;
    padding: 0 40px;
    height: 48px;
    font-size: 16px;

    &:hover {
      background: #7c3aed;
      border-color: #7c3aed;
    }
  }
}
</style>
