<template>
  <div class="home-container">
    <!-- 左上角logo -->
    <div class="logo">
<!--      <span class="logo-icon">众</span>-->
      <span class="logo-text">众享健康</span>
    </div>

    <!-- 中间主标题区域 -->
    <div class="main-content">
      <h1 class="system-title">肌少症数字化评估与干预系统</h1>
      <p class="system-subtitle">WS/T 888-2026 老年人综合健康评估体系</p>
      <div class="divider"></div>
      <p class="system-desc">
        涵盖营养评估、睡眠评估、疼痛评估、日常生活能力评估等<br>
        多维度老年健康评估量表，助力临床精准诊疗
      </p>
    </div>

    <!-- 底部版权 -->
    <div class="footer">
      <p>© 2026 众享健康 版权所有</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomePage'
}
</script>

<style lang="scss" scoped>
.home-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf5 100%);
  position: relative;
  overflow: hidden;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

/* 左上角logo */
.logo {
  position: absolute;
  top: 32px;
  left: 40px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #165DFF;

  .logo-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.2);
  }

  .logo-text {
    letter-spacing: 1px;
  }
}

/* 主内容区域 */
.main-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  width: 90%;
  max-width: 900px;

  .system-title {
    font-size: 56px;
    font-weight: 800;
    color: #1d2129;
    margin: 0 0 16px 0;
    letter-spacing: 2px;
    line-height: 1.3;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  .system-subtitle {
    font-size: 20px;
    color: #4e5969;
    margin: 0 0 32px 0;
    letter-spacing: 1px;
  }

  .divider {
    width: 120px;
    height: 4px;
    background: linear-gradient(90deg, #165DFF, #4080FF);
    margin: 0 auto 32px auto;
    border-radius: 2px;
  }

  .system-desc {
    font-size: 16px;
    color: #6b7785;
    line-height: 1.8;
    margin: 0;
  }
}

/* 底部版权 */
.footer {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 14px;
  color: #86909c;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .logo {
    top: 20px;
    left: 20px;
    font-size: 16px;

    .logo-icon {
      width: 32px;
      height: 32px;
      font-size: 18px;
    }
  }

  .main-content {
    .system-title {
      font-size: 32px;
      letter-spacing: 1px;
    }

    .system-subtitle {
      font-size: 16px;
    }

    .system-desc {
      font-size: 14px;
    }
  }
}
</style>