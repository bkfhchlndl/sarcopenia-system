<template>
  <div class="customize-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <el-button type="primary" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-actions">
        <el-button @click="reEvaluate">
          <el-icon><Refresh /></el-icon>
          重新评估
        </el-button>
        <el-button type="primary" @click="generateSingleReport" class="primary-btn">
          <el-icon><Document /></el-icon>
          生成单项报告
        </el-button>
        <el-button type="primary" @click="generateComprehensiveReport" class="primary-btn">
          <el-icon><Files /></el-icon>
          生成综合报告
        </el-button>
      </div>
    </div>

    <!-- 患者信息 -->
    <div class="patient-info">
      <span class="patient-name">{{ patientName }}</span>
      <span class="patient-id">病历号：{{ patientId }}</span>
    </div>

    <!-- 评估模块 -->
    <div class="assessment-sections">
      <div v-for="section in assessmentSections" :key="section.title" class="section">
        <!-- 分类标题 -->
        <div class="section-title">{{ section.title }}</div>

        <!-- 评估卡片 -->
        <div class="section-items">
          <div v-for="item in section.items" :key="item.id" class="assessment-card">
            <!-- 图标（使用emoji或图片） -->
            <div class="card-icon" :style="{ backgroundColor: item.iconColor + '20' }">
              <span class="icon-emoji">{{ item.icon }}</span>
            </div>

            <!-- 内容 -->
            <div class="card-content">
              <div class="card-title">{{ item.name }}</div>
              <div class="card-sub-items">
                <div v-for="(sub, idx) in item.subItems" :key="idx" class="sub-item">
                  • {{ sub }}
                </div>
              </div>
            </div>

            <!-- 状态和操作 -->
            <div class="card-footer">
              <div v-if="item.completed" class="completed-tag">
                <el-icon><Check /></el-icon>
                已完成
              </div>
              <el-button
                  :style="{ backgroundColor: item.iconColor, borderColor: item.iconColor }"
                  @click="startAssessment(item)"
                  class="start-btn"
              >
                开始评估
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Refresh, Document, Files, Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 从路由获取患者信息
const patientId = route.query.patientId
const patientName = route.query.patientName || '患者'
const projectId = route.query.projectId || 3  // 自定义单项评估项目ID

// 评估模块配置
const assessmentSections = ref([
  {
    title: '一般情况',
    items: [
      {
        id: 1,
        name: '基本情况',
        icon: '📄',
        iconColor: '#3b82f6',
        subItems: ['基本信息采集'],
        completed: false
      },
      {
        id: 2,
        name: '疾病评估',
        icon: '❤️',
        iconColor: '#14b8a6',
        subItems: ['病史记录', 'Charlson Index 查尔森合并症指数', 'CIRS-G 老年累计疾病评估量表'],
        completed: false
      }
    ]
  },
  {
    title: '躯体功能',
    items: [
      {
        id: 3,
        name: '日常生活活动能力',
        code: 'daily_life',
        icon: '🛁',
        iconColor: '#22c55e',
        subItems: ['Barthel ADL 评定', 'IADL 评定'],
        completed: false
      },
      {
        id: 4,
        name: '运动功能',
        code: 'sppb',
        iconColor: '#f97316',
        subItems: ['筛查', 'SPPB 简易体能评估', 'TUGT 起立行走', 'Tinetti 平衡和步态量表'],
        completed: false
      },
      {
        id: 5,
        name: '视听力',
        code: 'vision_hearing',
        icon: '👂',
        iconColor: '#a855f7',
        subItems: ['视力简易评估(筛查)', '视功能简易评估(筛查)', '听力简易评估(筛查)', '耳语实验(筛查)'],
        completed: false
      }
    ]
  },
  {
    title: '精神心理',
    items: [
      {
        id: 6,
        name: '认知功能',
        icon: '🧠',
        iconColor: '#3b82f6',
        subItems: ['Mini-Cog 简易认知评估(筛查)', 'MMSE 简明精神状态检查', 'MoCA 蒙特利尔认知评估'],
        completed: false
      },
      {
        id: 7,
        name: '抑郁',
        icon: '😢',
        iconColor: '#14b8a6',
        subItems: ['筛查', 'GDS-15 简版老年抑郁量表'],
        completed: false
      },
      {
        id: 8,
        name: '焦虑',
        icon: '😰',
        iconColor: '#f97316',
        subItems: ['筛查', 'GAD-7', 'HAMA 汉密尔顿焦虑量表', 'SAS 焦虑自评量表'],
        completed: false
      }
    ]
  }
])

// 返回上一页
const goBack = () => {
  router.back()
}

// 开始评估
const startAssessment = (item) => {
  // TODO: 跳转到对应量表评估页面
  console.log('开始评估:', item.name)
  let path = '/patient/detection/cga'
  if (item.code == 'daily_life') {
    path = '/patient/detection/dailyLife'
  }else if (item.code == 'sppb'){
    path = '/patient/detection/sppb'
  }else if (item.code == 'vision_hearing'){
    path = '/patient/detection/visionHearing'
  }
  router.push({
    path,
    query: { patientId, scaleId: item.id, projectId }
  })
}

// 重新评估
const reEvaluate = () => {
  console.log('重新评估')
}

// 生成单项报告
const generateSingleReport = () => {
  console.log('生成单项报告')
}

// 生成综合报告
const generateComprehensiveReport = () => {
  console.log('生成综合报告')
}
</script>

<style scoped lang="scss">
.customize-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.back-btn {
  background: #9333ea;
  border-color: #9333ea;
  
  &:hover {
    background: #7c3aed;
    border-color: #7c3aed;
  }
}

.primary-btn {
  background: #9333ea;
  border-color: #9333ea;
  
  &:hover {
    background: #7c3aed;
    border-color: #7c3aed;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.patient-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 8px;
  
  .patient-name {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }
  
  .patient-id {
    font-size: 14px;
    color: #666;
  }
}

.assessment-sections {
  .section {
    margin-bottom: 24px;
  }
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 16px;
    padding-left: 12px;
    border-left: 4px solid #9333ea;
  }
  
  .section-items {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }
}

.assessment-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  height: 100%;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
    flex-shrink: 0;
    
    .icon-emoji {
      font-size: 24px;
    }
  }
  
  .card-content {
    margin-bottom: 16px;
    flex: 1;
  }
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
  
  .card-sub-items {
    .sub-item {
      font-size: 13px;
      color: #666;
      line-height: 1.8;
    }
  }
  
  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: auto;
    flex-shrink: 0;
  }
  
  .completed-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #22c55e;
  }
  
  .start-btn {
    flex: 1;
    color: #fff;
    
    &:hover {
      opacity: 0.9;
    }
  }
}
</style>
