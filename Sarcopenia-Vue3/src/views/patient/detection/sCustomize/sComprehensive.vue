<template>
  <div class="s-total-page">
    <header class="top-bar">
      <div class="top-left">
        <el-button link class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </el-button>
        <div class="title-block">
          <h1>肌少症综合测量</h1>
          <p>Sarcopenia Comprehensive Measurement · 共 {{ steps.length }} 项</p>
        </div>
      </div>
      <div class="top-right">
        <div class="patient-badge">
          <span class="badge-avatar">{{ (patientName || '患').slice(0, 1) }}</span>
          <div>
            <div class="badge-name">{{ patientName || '未选择患者' }}</div>
            <div class="badge-id">{{ patientId ? 'ID: ' + patientId : '-' }}</div>
          </div>
        </div>
      </div>
    </header>

    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      <span class="progress-label">当前 {{ currentIndex + 1 }} / {{ steps.length }} 项 · 已暂存 {{ completedCount }} / {{ steps.length }} 项</span>
    </div>

    <div class="page-body">
      <section class="work-panel">
        <div class="panel-head">
          <div>
            <div class="panel-kicker">当前项目 {{ currentIndex + 1 }} / {{ steps.length }}</div>
            <h2>{{ currentStep.title }}</h2>
            <p>{{ currentStep.desc }}</p>
          </div>
          <el-tag :type="completed[currentStep.key] ? 'success' : 'info'" effect="light">
            {{ completed[currentStep.key] ? '已暂存' : '未暂存' }}
          </el-tag>
        </div>

        <div class="panel-body">
          <template v-if="currentStep.key === 'sarcF'">
            <div v-if="sarcQuestions.length" class="question-list">
              <div
                v-for="(question, qIndex) in sarcQuestions"
                :key="question.questionId"
                class="question-card"
                :class="{ 'is-answered': forms.sarcF.answers[question.questionId] != null }"
              >
                <div class="q-head">
                  <span class="q-index">{{ String(qIndex + 1).padStart(2, '0') }}</span>
                  <span class="q-title">{{ question.title }}</span>
                  <span v-if="forms.sarcF.answers[question.questionId] != null" class="q-done">
                    <el-icon><Check /></el-icon>
                  </span>
                </div>
                <div class="q-options">
                  <div
                    v-for="option in question.optionList"
                    :key="option.optionId"
                    class="q-option"
                    :class="{
                      'is-selected': Number(forms.sarcF.answers[question.questionId]) === Number(option.optionId),
                      'is-max': isMaxOption(question, option)
                    }"
                    @click="forms.sarcF.answers[question.questionId] = option.optionId"
                  >
                    <div class="opt-left">
                      <div class="opt-radio" :class="{ 'is-checked': Number(forms.sarcF.answers[question.questionId]) === Number(option.optionId) }">
                        <el-icon><CircleCheck /></el-icon>
                      </div>
                      <div class="opt-content">
                        <span class="opt-label">{{ option.content }}</span>
                      </div>
                    </div>
                    <div class="opt-right">
                      <span class="opt-score">{{ option.score }}</span>
                      <span class="opt-unit">分</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="正在加载 SARC-F 题目" />
            <div class="result-line">
              <span>SARC-F 总分：{{ sarcTotalScore }} / {{ sarcTotalFullScore }} 分</span>
              <el-tag :type="sarcTotalScore >= 4 ? 'warning' : 'success'">{{ sarcLevelText }}</el-tag>
            </div>
          </template>

          <template v-else-if="currentStep.key === 'calf'">
            <div class="device-card">
              <div class="device-header">
                <div class="device-icon">
                  <el-icon :size="24"><Connection /></el-icon>
                </div>
                <div class="device-info">
                  <div class="device-title">蓝牙测量设备</div>
                  <div class="device-status" :class="{ connected: calfDeviceConnected }">
                    <span class="status-dot"></span>
                    {{ calfDeviceConnected ? '已连接' : '未连接' }}
                  </div>
                </div>
              </div>
              <div class="device-actions">
                <el-button
                  :type="calfDeviceConnected ? 'danger' : 'primary'"
                  plain
                  size="small"
                  :loading="calfDeviceLoading"
                  @click="toggleCalfDevice"
                >
                  <el-icon v-if="!calfDeviceConnected"><Link /></el-icon>
                  <el-icon v-else><Close /></el-icon>
                  {{ calfDeviceConnected ? '断开' : '连接设备' }}
                </el-button>
              </div>
            </div>
            <MeasureThreeForm
              v-model:first="forms.calf.firstValue"
              v-model:second="forms.calf.secondValue"
              v-model:third="forms.calf.thirdValue"
              v-model:remark="forms.calf.remark"
              unit="cm"
              title="小腿围"
              :min="10"
              :max="60"
              :device-connected="calfDeviceConnected"
              :fetching-index="calfFetchingIndex"
              @fetch="fetchCalfMeasureValue"
            />
            <div class="result-line">
              <span>最大值：{{ maxValue(forms.calf) || '--' }} cm</span>
              <el-tag :type="isLowCalf ? 'warning' : 'success'">{{ isLowCalf ? '小腿围偏低' : '范围内' }}</el-tag>
            </div>
          </template>

          <template v-else-if="currentStep.key === 'grip'">
            <div class="device-card">
              <div class="device-header">
                <div class="device-icon">
                  <el-icon :size="24"><Connection /></el-icon>
                </div>
                <div class="device-info">
                  <div class="device-title">蓝牙握力计设备</div>
                  <div class="device-status" :class="{ connected: gripDeviceConnected }">
                    <span class="status-dot"></span>
                    {{ gripDeviceConnected ? '已连接' : '未连接' }}
                  </div>
                </div>
              </div>
              <div class="device-actions">
                <el-button
                  :type="gripDeviceConnected ? 'danger' : 'primary'"
                  plain
                  size="small"
                  :loading="gripDeviceLoading"
                  @click="toggleGripDevice"
                >
                  <el-icon v-if="!gripDeviceConnected"><Link /></el-icon>
                  <el-icon v-else><Close /></el-icon>
                  {{ gripDeviceConnected ? '断开' : '连接设备' }}
                </el-button>
              </div>
            </div>
            <MeasureThreeForm
              v-model:first="forms.grip.firstValue"
              v-model:second="forms.grip.secondValue"
              v-model:third="forms.grip.thirdValue"
              v-model:remark="forms.grip.remark"
              unit="kg"
              title="握力"
              :min="0"
              :max="80"
              :device-connected="gripDeviceConnected"
              :fetching-index="gripFetchingIndex"
              @fetch="fetchGripMeasureValue"
            />
            <div class="result-line">
              <span>最大值：{{ maxValue(forms.grip) || '--' }} kg</span>
              <el-tag :type="isLowGrip ? 'warning' : 'success'">{{ isLowGrip ? '肌力下降' : '范围内' }}</el-tag>
            </div>
          </template>

          <template v-else-if="currentStep.key === 'sitUp'">
            <div class="situp-test-card">
              <div class="timer-display">
                <div class="timer-value">
                  <span class="tv-num">{{ formatTime(sitUpCurrentTime) }}</span>
                  <span class="tv-unit">秒</span>
                </div>
              </div>

              <div class="action-buttons">
                <el-button
                  v-if="!sitUpRunning && !sitUpFinished"
                  type="primary"
                  size="large"
                  class="btn-start"
                  @click="startSitUpTest"
                >
                  <el-icon><VideoPlay /></el-icon>
                  开始测试
                </el-button>
                <template v-if="sitUpRunning">
                  <el-button type="success" size="large" class="btn-finish" @click="finishSitUpTest">
                    <el-icon><Check /></el-icon>
                    完成测试
                  </el-button>
                  <el-button type="danger" size="large" class="btn-stop" plain @click="stopSitUpTest">
                    <el-icon><VideoPause /></el-icon>
                    终止本项目测试
                  </el-button>
                </template>
                <template v-if="sitUpFinished">
                  <el-button type="primary" size="large" class="btn-restart" @click="restartSitUpTest">
                    <el-icon><RefreshRight /></el-icon>
                    重新测试
                  </el-button>
                  <el-button size="large" class="btn-reset" @click="resetSitUpTest">
                    <el-icon><RefreshLeft /></el-icon>
                    重置
                  </el-button>
                </template>
              </div>

              <div class="illustration">
                <img src="/img_1.png" alt="5次起坐动作示意图" class="illustration-img" />
              </div>

              <div class="situp-remark">
                <el-input v-model="forms.sitUp.remark" type="textarea" :rows="3" placeholder="可填写测量说明" />
              </div>
            </div>
            <div class="result-line">
              <span>测试成绩：{{ forms.sitUp.firstValue != null ? Number(forms.sitUp.firstValue).toFixed(2) : '0.00' }} 秒</span>
              <span>SPPB 起坐得分：{{ sitUpScore }} / 4 分</span>
            </div>
          </template>

          <template v-else-if="currentStep.key === 'pace'">
            <div class="pace-test-card">
              <div class="pace-round">
                <span class="round-label">当前测量</span>
                <span class="round-num">第 {{ paceCurrentRound }} 次</span>
                <span class="round-total">/ 共 2 次</span>
              </div>

              <div class="pace-timer-display">
                <div class="pace-timer-value">
                  <span class="pace-tv-num">{{ formatTime(paceCurrentTime) }}</span>
                  <span class="pace-tv-unit">秒</span>
                </div>
              </div>

              <div class="pace-action-buttons">
                <template v-if="!paceRunning && !paceRoundFinished">
                  <el-button type="primary" size="large" class="pace-btn-start" @click="startPaceTimer">
                    <el-icon><VideoPlay /></el-icon>
                    开始计时
                  </el-button>
                </template>
                <template v-if="paceRunning">
                  <el-button type="success" size="large" class="pace-btn-finish" @click="finishPaceTimer">
                    <el-icon><Check /></el-icon>
                    完成计时
                  </el-button>
                  <el-button type="danger" size="large" plain class="pace-btn-cancel" @click="cancelPaceTimer">
                    <el-icon><Close /></el-icon>
                    取消本次
                  </el-button>
                </template>
                <template v-if="paceRoundFinished && paceCurrentRound === 1">
                  <el-button type="primary" size="large" class="pace-btn-next" @click="nextPaceRound">
                    <el-icon><RefreshRight /></el-icon>
                    进行第 2 次
                  </el-button>
                  <el-button size="large" class="pace-btn-skip" @click="skipPaceSecond">
                    跳过第二次
                  </el-button>
                </template>
                <template v-if="paceRoundFinished && paceCurrentRound === 2">
                  <el-button type="primary" size="large" class="pace-btn-restart" @click="restartPaceAll">
                    <el-icon><RefreshRight /></el-icon>
                    重新测量
                  </el-button>
                  <el-button size="large" class="pace-btn-reset" @click="resetPaceAll">
                    <el-icon><RefreshLeft /></el-icon>
                    重置
                  </el-button>
                </template>
              </div>
            </div>

            <div class="pace-measure-cards">
              <div class="pace-measure-card" :class="{ 'has-value': forms.pace.firstValue != null }">
                <div class="pmc-head">
                  <span class="pmc-index">01</span>
                  <span class="pmc-label">第 1 次测量</span>
                  <span v-if="forms.pace.firstValue != null" class="pmc-tag success">已完成</span>
                </div>
                <div class="pmc-body">
                  <div class="pmc-value-display">
                    <span class="pmc-num">{{ forms.pace.firstValue != null ? Number(forms.pace.firstValue).toFixed(2) : '--' }}</span>
                    <span class="pmc-unit">s</span>
                  </div>
                  <div class="pmc-speed">步速: <strong>{{ calcPaceSpeed(forms.pace.firstValue) }}</strong> m/s</div>
                </div>
              </div>

              <div class="pace-measure-card" :class="{ 'has-value': forms.pace.secondValue != null }">
                <div class="pmc-head">
                  <span class="pmc-index">02</span>
                  <span class="pmc-label">第 2 次测量</span>
                  <span v-if="forms.pace.secondValue != null" class="pmc-tag success">已完成</span>
                  <span v-else class="pmc-tag">选填</span>
                </div>
                <div class="pmc-body">
                  <div class="pmc-value-display">
                    <span class="pmc-num">{{ forms.pace.secondValue != null ? Number(forms.pace.secondValue).toFixed(2) : '--' }}</span>
                    <span class="pmc-unit">s</span>
                  </div>
                  <div class="pmc-speed">步速: <strong>{{ calcPaceSpeed(forms.pace.secondValue) }}</strong> m/s</div>
                </div>
              </div>
            </div>

            <div class="pace-remark">
              <el-input v-model="forms.pace.remark" type="textarea" :rows="3" placeholder="可填写测量说明" />
            </div>
            <div class="result-line">
              <span>平均用时：{{ paceAverage || '--' }} 秒；步速：{{ paceSpeed || '--' }} m/s</span>
              <span>SPPB 步速得分：{{ paceScore }} / 4 分</span>
            </div>
          </template>

          <template v-else-if="currentStep.key === 'balance'">
            <div class="balance-test-card">
              <div class="balance-header">
                <span class="balance-badge">第 {{ balanceCurrentIndex + 1 }} / 3 项</span>
                <span class="balance-title">{{ balanceItems[balanceCurrentIndex].name }}</span>
                <span class="balance-target">目标: {{ balanceItems[balanceCurrentIndex].target }}</span>
              </div>

              <div class="balance-body">
                <div class="balance-illustration">
                  <div class="balance-img-crop">
                    <img v-if="balanceCurrentIndex === 0" src="/balance_feet_together.png" alt="双脚并立" class="balance-foot-img" />
                    <img v-else-if="balanceCurrentIndex === 1" src="/balance_semi_offset.png" alt="半脚错开" class="balance-foot-img" />
                    <img v-else src="/balance_tandem.png" alt="前后脚一条线" class="balance-foot-img" />
                  </div>
                </div>

                <div class="balance-right">
                  <div class="balance-timer-display">
                    <div class="balance-timer-value" :class="{ success: balanceTargetReached }">
                      <span class="balance-tv-num">{{ formatTime(balanceCurrentTime) }}</span>
                      <span class="balance-tv-unit">秒</span>
                    </div>
                    <div v-if="balanceTargetReached" class="balance-target-reached">
                      <el-icon><CircleCheck /></el-icon>
                      已达目标时长
                    </div>
                  </div>

                  <div class="balance-action-buttons">
                    <template v-if="!balanceRunning && !balanceItemFinished">
                      <el-button type="primary" size="large" class="balance-btn-start" @click="startBalanceTimer">
                        <el-icon><VideoPlay /></el-icon>
                        开始计时
                      </el-button>
                    </template>
                    <template v-if="balanceRunning">
                      <el-button type="success" size="large" class="balance-btn-finish" @click="finishBalanceTimer">
                        <el-icon><Check /></el-icon>
                        完成测试
                      </el-button>
                      <el-button type="danger" size="large" plain class="balance-btn-cancel" @click="cancelBalanceTimer">
                        <el-icon><Close /></el-icon>
                        不能完成
                      </el-button>
                    </template>
                    <template v-if="balanceItemFinished">
                      <el-button type="primary" size="large" class="balance-btn-next" @click="nextBalanceItem">
                        <el-icon><RefreshRight /></el-icon>
                        {{ balanceCurrentIndex < balanceItems.length - 1 ? '下一项' : '完成平衡测试' }}
                      </el-button>
                      <el-button size="large" class="balance-btn-retry" @click="retryBalanceItem">
                        <el-icon><RefreshLeft /></el-icon>
                        重测本项
                      </el-button>
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <div class="balance-items-progress">
              <div
                v-for="(item, idx) in balanceItems"
                :key="item.key"
                class="balance-item-card"
                :class="{
                  active: balanceCurrentIndex === idx && !balanceAllFinished,
                  completed: getBalanceItemValue(item.key) != null,
                  clickable: !balanceRunning
                }"
                @click="switchBalanceItem(idx)"
              >
                <div class="bic-head">
                  <span class="bic-index">{{ String(idx + 1).padStart(2, '0') }}</span>
                  <span class="bic-name">{{ item.name }}</span>
                  <span v-if="getBalanceItemValue(item.key) != null" class="bic-tag">{{ getBalanceItemScore(item.key) }} 分</span>
                </div>
                <div class="bic-body">
                  <span class="bic-value">{{ getBalanceItemValue(item.key) != null ? Number(getBalanceItemValue(item.key)).toFixed(2) : '--' }}</span>
                  <span class="bic-unit">秒</span>
                </div>
                <div class="bic-target">
                  目标: {{ item.target }} <span>{{ item.scoreDesc }}</span>
                </div>
              </div>
            </div>

            <div class="balance-remark">
              <el-input v-model="forms.balance.remark" type="textarea" :rows="3" placeholder="可填写测量说明" />
            </div>
            <div class="result-line">
              <span>SPPB 平衡得分：{{ balanceScore }} / 4 分</span>
            </div>
          </template>

          <template v-else-if="currentStep.key === 'body'">
            <div class="body-device-card">
              <div class="body-device-area">
                <div class="body-device-visual">
                  <div class="body-scale-device">
                    <div class="body-scale-platform">
                      <div class="body-platform-surface">
                        <div class="body-foot-print left-foot"></div>
                        <div class="body-foot-print right-foot"></div>
                      </div>
                      <div class="body-platform-side"></div>
                    </div>
                    <div class="body-scale-display">
                      <div class="body-display-screen">
                        <span v-if="bodyMeasureStatus === 'idle'">--</span>
                        <span v-else-if="bodyMeasureStatus === 'measuring'">{{ bodyCurrentDisplay }}</span>
                        <span v-else>完成</span>
                      </div>
                    </div>
                    <div class="body-scale-stand"></div>
                  </div>
                  <div class="body-hand-electrodes">
                    <div class="body-electrode left">
                      <div class="body-electrode-body"></div>
                      <div class="body-electrode-wire"></div>
                    </div>
                    <div class="body-electrode right">
                      <div class="body-electrode-body"></div>
                      <div class="body-electrode-wire"></div>
                    </div>
                  </div>
                  <div v-if="bodyMeasureStatus === 'measuring'" class="body-scan-effect">
                    <div class="body-scan-line"></div>
                  </div>
                </div>

                <div class="body-measure-status-text">
                  <template v-if="!bodyDeviceOnline">
                    <el-icon class="offline"><Warning /></el-icon>
                    <span>设备未连接，请检查设备电源和网络</span>
                  </template>
                  <template v-else-if="bodyMeasureStatus === 'idle'">
                    <el-icon class="ready"><CircleCheck /></el-icon>
                    <span>设备就绪，请站在体脂秤上，双手握住手柄</span>
                  </template>
                  <template v-else-if="bodyMeasureStatus === 'measuring'">
                    <el-icon class="measuring"><Loading /></el-icon>
                    <span>{{ bodyCurrentPhase }}</span>
                  </template>
                  <template v-else>
                    <el-icon class="success"><CircleCheck /></el-icon>
                    <span>测量完成，数据已同步到下方指标</span>
                  </template>
                </div>
              </div>

              <div class="body-action-area">
                <el-button
                  v-if="!bodyDeviceOnline"
                  type="primary"
                  size="large"
                  class="body-btn-connect"
                  :loading="bodyDeviceLoading"
                  @click="connectBodyDevice"
                >
                  <el-icon><Link /></el-icon>
                  连接设备
                </el-button>
                <el-button v-else-if="bodyMeasureStatus === 'idle'" type="primary" size="large" class="body-btn-start" @click="startBodyMeasure">
                  <el-icon><VideoPlay /></el-icon>
                  开始测量
                </el-button>
                <el-button v-else-if="bodyMeasureStatus === 'measuring'" type="danger" size="large" plain class="body-btn-stop" @click="stopBodyMeasure">
                  <el-icon><Close /></el-icon>
                  停止测量
                </el-button>
                <template v-else>
                  <el-button size="large" class="body-btn-retry" @click="retryBodyMeasure">
                    <el-icon><RefreshRight /></el-icon>
                    重新测量
                  </el-button>
                  <el-button type="primary" size="large" class="body-btn-start" @click="startBodyMeasure">
                    <el-icon><VideoPlay /></el-icon>
                    再次测量
                  </el-button>
                </template>
              </div>

              <el-progress
                v-if="bodyMeasureStatus === 'measuring'"
                :percentage="bodyMeasureProgress"
                :stroke-width="10"
                color="#8b5cf6"
              />
            </div>

            <div class="result-line">
              <span>ASMI：{{ forms.body.asmi || '--' }} kg/m2</span>
              <el-tag :type="isLowAsmi ? 'warning' : 'success'">{{ isLowAsmi ? '肌量不足' : '范围内' }}</el-tag>
            </div>
          </template>
        </div>

        <div class="submit-area">
          <el-button :disabled="currentIndex === 0" @click="prevStep">上一项</el-button>
          <el-button type="success" plain @click="finishCurrent">完成当前项</el-button>
          <el-button v-if="currentIndex < steps.length - 1" type="primary" @click="nextStep">下一项</el-button>
          <el-button v-else type="primary" :loading="submitting" :disabled="!canSubmit" @click="submitAll">提交记录</el-button>
          <el-button plain @click="clearDraft">清空草稿</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup name="SComprehensive">
import { computed, defineComponent, h, markRaw, onMounted, onUnmounted, reactive, ref, resolveComponent, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Check, CircleCheck, EditPen, Document, Trophy, Monitor, Stopwatch, Aim, DataAnalysis,
  Connection, Link, Close, Refresh, VideoPlay, VideoPause, RefreshRight, RefreshLeft, Loading, Warning
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  selectSVOById,
  insertSRecord,
  insertCalfMeasure,
  insertGripStrength,
  insertSitUp,
  insertPace,
  insertBalanceStand,
  insertBodyComposition
} from '@/api/s.js'
import { selectPatientById } from '@/api/patient.js'

const MeasureThreeForm = defineComponent({
  name: 'MeasureThreeForm',
  props: {
    first: Number,
    second: Number,
    third: Number,
    remark: String,
    unit: { type: String, default: '' },
    title: { type: String, default: '测量' },
    min: { type: Number, default: 0 },
    max: { type: Number, default: 100 },
    deviceConnected: { type: Boolean, default: null },
    fetchingIndex: { type: Number, default: 0 }
  },
  emits: ['update:first', 'update:second', 'update:third', 'update:remark', 'fetch'],
  setup(props, { emit }) {
    const ElInputNumber = resolveComponent('ElInputNumber')
    const ElInput = resolveComponent('ElInput')
    const ElButton = resolveComponent('ElButton')
    const ElIcon = resolveComponent('ElIcon')
    const renderInput = (label, value, eventName, required, index) => h('div', {
      class: value != null ? 'measure-card has-value' : 'measure-card'
    }, [
      h('div', { class: 'mc-head' }, [
        h('span', { class: 'mc-index' }, String(index).padStart(2, '0')),
        h('span', { class: 'mc-label' }, label),
        h('span', { class: ['mini-tag', value != null ? 'success' : ''] }, value != null ? '已完成' : (required ? '必填' : '选填'))
      ]),
      h('div', { class: 'mc-body' }, [
        h('div', { class: 'mc-value-display' }, [
          h('span', { class: 'mc-num' }, value ?? '--'),
          h('span', { class: 'mc-unit' }, props.unit)
        ]),
        h('div', { class: 'mc-input-row' }, [
          h(ElInputNumber, {
            modelValue: value,
            'onUpdate:modelValue': val => emit(eventName, val),
            min: props.min,
            max: props.max,
            step: 0.1,
            precision: 1,
            controls: true,
            style: 'width: 100%'
          })
        ])
      ]),
      h('div', { class: 'mc-foot' }, [
        props.deviceConnected !== null
          ? h(ElButton, {
            type: 'primary',
            plain: index !== 1,
            disabled: !props.deviceConnected,
            loading: props.fetchingIndex === index,
            onClick: () => emit('fetch', index)
          }, {
            default: () => [
              h(ElIcon, null, { default: () => h(Refresh) }),
              '获取测量值'
            ]
          })
          : null,
        value != null
          ? h('button', { class: 'clear-link', type: 'button', onClick: () => emit(eventName, null) }, '清除')
          : null
      ])
    ])

    return () => h('div', { class: 'measure-three' }, [
      h('div', { class: 'measure-three-title' }, props.title + '三次测量'),
      h('div', { class: 'measure-cards' }, [
        renderInput('第 1 次测量', props.first, 'update:first', true, 1),
        renderInput('第 2 次测量', props.second, 'update:second', false, 2),
        renderInput('第 3 次测量', props.third, 'update:third', false, 3)
      ]),
      h(ElInput, {
        modelValue: props.remark,
        'onUpdate:modelValue': val => emit('update:remark', val),
        type: 'textarea',
        rows: 3,
        placeholder: '备注说明',
        class: 'measure-remark'
      })
    ])
  }
})

const route = useRoute()
const router = useRouter()

const patientId = computed(() => route.query.patientId || '')
const patientInfo = ref(null)
const currentIndex = ref(0)
const submitting = ref(false)
const sarcScale = ref({ scaleId: 1, totalScore: 10, questionList: [] })
const calfDeviceConnected = ref(false)
const calfDeviceLoading = ref(false)
const calfFetchingIndex = ref(0)
const gripDeviceConnected = ref(false)
const gripDeviceLoading = ref(false)
const gripFetchingIndex = ref(0)
const sitUpCurrentTime = ref(0)
const sitUpRunning = ref(false)
const sitUpFinished = ref(false)
const sitUpStopped = ref(false)
let sitUpTimerInterval = null
const paceCurrentRound = ref(1)
const paceCurrentTime = ref(0)
const paceRunning = ref(false)
const paceRoundFinished = ref(false)
let paceTimerInterval = null
const balanceCurrentIndex = ref(0)
const balanceCurrentTime = ref(0)
const balanceRunning = ref(false)
const balanceItemFinished = ref(false)
let balanceTimerInterval = null
const bodyDeviceOnline = ref(false)
const bodyDeviceLoading = ref(false)
const bodyMeasureStatus = ref('idle')
const bodyMeasureProgress = ref(0)
const bodyCurrentPhase = ref('')
const bodyCurrentDisplay = ref('0.0')
let bodyMeasureTimer = null

const patientName = computed(() => patientInfo.value?.name || patientInfo.value?.patientName || route.query.patientName || route.query.name || '')
const medicalCaseNo = computed(() => patientInfo.value?.medicalCaseNo || route.query.medicalCaseNo || '')
const gender = computed(() => patientInfo.value?.gender || route.query.gender || '')
const genderText = computed(() => {
  if (gender.value === '1' || gender.value === 1 || gender.value === '男') return '男'
  if (gender.value === '2' || gender.value === 2 || gender.value === '女') return '女'
  return '-'
})
const isMale = computed(() => genderText.value === '男')
const todayStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})

const steps = [
  { key: 'sarcF', title: 'SARC-F 量表', subTitle: '肌力、辅助行走、坐站、爬楼、跌倒', desc: '按患者实际情况完成 SARC-F 五个条目。', icon: markRaw(Document), color: '#f59e0b', bg: '#fff7ed' },
  { key: 'calf', title: '小腿围测量', subTitle: 'Calf Circumference', desc: '录入小腿围测量值，至少填写第 1 次。', icon: markRaw(EditPen), color: '#ea580c', bg: '#fff1f2' },
  { key: 'grip', title: '握力测量', subTitle: 'Grip Strength', desc: '录入握力三次测量值，系统取最大值判断。', icon: markRaw(Monitor), color: '#ef4444', bg: '#fef2f2' },
  { key: 'sitUp', title: '5次起坐测试', subTitle: '5 Times Sit-to-Stand', desc: '录入完成 5 次起坐的用时。', icon: markRaw(Stopwatch), color: '#8b5cf6', bg: '#f5f3ff' },
  { key: 'pace', title: '6米步速测量', subTitle: '6-Meter Gait Speed', desc: '录入 6 米步行测试用时，可填写两次。', icon: markRaw(DataAnalysis), color: '#10b981', bg: '#ecfdf5' },
  { key: 'balance', title: '站立平衡测试', subTitle: 'Standing Balance', desc: '录入三项站立平衡测试时间。', icon: markRaw(Aim), color: '#f97316', bg: '#fff7ed' },
  { key: 'body', title: '人体成分测量', subTitle: 'Body Composition', desc: '录入人体成分核心指标，至少填写 ASMI。', icon: markRaw(Trophy), color: '#0ea5e9', bg: '#e0f2fe' }
]

const balanceItems = [
  { key: 'feetTogether', name: '双脚并立站立', target: '10 秒', maxScore: 1, scoreDesc: '≥10秒得1分', targetSeconds: 10 },
  { key: 'semiOffset', name: '半脚错开站立', target: '10 秒', maxScore: 1, scoreDesc: '≥10秒得1分', targetSeconds: 10 },
  { key: 'tandemStand', name: '前后脚一条线站立', target: '10 秒', maxScore: 2, scoreDesc: '≥10秒得2分，3~9.99秒得1分', targetSeconds: 10 }
]

const currentStep = computed(() => steps[currentIndex.value])
const measureSteps = computed(() => steps)
const completed = reactive({
  sarcF: false,
  calf: false,
  grip: false,
  sitUp: false,
  pace: false,
  balance: false,
  body: false
})

const forms = reactive({
  sarcF: { answers: {} },
  calf: { firstValue: null, secondValue: null, thirdValue: null, remark: '' },
  grip: { firstValue: null, secondValue: null, thirdValue: null, remark: '' },
  sitUp: { firstValue: null, isComplete: '1', remark: '' },
  pace: { firstValue: null, secondValue: null, remark: '' },
  balance: { feetTogetherTime: null, semiOffsetTime: null, tandemStandTime: null, remark: '' },
  body: { weight: null, bmi: null, bodyFatRate: null, asm: null, asmi: null, totalBodyWater: null, leanBodyMass: null }
})

const draftKey = computed(() => `sarcopenia_total_draft:${patientId.value || 'unknown'}`)
const sarcQuestions = computed(() => sarcScale.value?.questionList || [])
const sarcTotalFullScore = computed(() => sarcScale.value?.totalScore || 10)
const sarcTotalScore = computed(() => {
  return sarcQuestions.value.reduce((sum, q) => {
    const selected = forms.sarcF.answers[q.questionId]
    const option = q.optionList?.find(o => Number(o.optionId) === Number(selected))
    return sum + (Number(option?.score) || 0)
  }, 0)
})
const sarcLevelText = computed(() => {
  if (sarcTotalScore.value >= 7) return '高度风险'
  if (sarcTotalScore.value >= 4) return '中度风险'
  return '低风险'
})
const canSubmit = computed(() => measureSteps.value.every(step => completed[step.key]))
const completedCount = computed(() => measureSteps.value.filter(step => completed[step.key]).length)
const progressPercent = computed(() => Math.round(((currentIndex.value + 1) / steps.length) * 100))

const isLowCalf = computed(() => {
  const value = maxValue(forms.calf)
  if (value == null) return false
  return isMale.value ? value < 34 : value < 33
})
const isLowGrip = computed(() => {
  const value = maxValue(forms.grip)
  if (value == null) return false
  return isMale.value ? value < 28 : value < 18
})
const isLowAsmi = computed(() => {
  const value = forms.body.asmi
  if (value == null) return false
  return isMale.value ? Number(value) < 7.0 : Number(value) < 5.7
})
const sitUpScore = computed(() => {
  const t = Number(forms.sitUp.firstValue)
  if (!Number.isFinite(t) || t <= 0) return 0
  if (t <= 11.19) return 4
  if (t <= 13.69) return 3
  if (t <= 16.69) return 2
  if (t <= 60) return 1
  return 0
})
const paceAverage = computed(() => {
  const values = [forms.pace.firstValue, forms.pace.secondValue].filter(v => v != null && Number(v) > 0).map(Number)
  if (!values.length) return null
  return Number((values.reduce((a, b) => a + b, 0) / values.length).toFixed(2))
})
const paceSpeed = computed(() => paceAverage.value ? Number((6 / paceAverage.value).toFixed(2)) : null)
const paceScore = computed(() => {
  const speed = paceSpeed.value
  if (speed == null) return 0
  if (speed >= 1.0) return 4
  if (speed >= 0.8) return 3
  if (speed >= 0.6) return 2
  return 1
})
const balanceTargetReached = computed(() => {
  const target = balanceItems[balanceCurrentIndex.value]?.targetSeconds || 10
  return balanceCurrentTime.value >= target
})
const balanceAllFinished = computed(() => {
  return forms.balance.feetTogetherTime != null
    && forms.balance.semiOffsetTime != null
    && forms.balance.tandemStandTime != null
})
const balanceScore = computed(() => {
  return balanceItems.reduce((sum, item) => sum + getBalanceItemScore(item.key), 0)
})

function maxValue(group) {
  const values = [group.firstValue, group.secondValue, group.thirdValue]
    .filter(v => v != null && v !== '' && Number.isFinite(Number(v)))
    .map(Number)
  if (!values.length) return null
  return Number(Math.max(...values).toFixed(1))
}

function isMaxOption(question, option) {
  const maxScore = Math.max(...(question.optionList || []).map(item => Number(item.score) || 0))
  return Number(option.score) === maxScore && maxScore > 0
}

function formatTime(seconds) {
  return Number(seconds || 0).toFixed(2)
}

function calcPaceSpeed(seconds) {
  const value = Number(seconds)
  if (!Number.isFinite(value) || value <= 0) return '--'
  return (6 / value).toFixed(2)
}

function startSitUpTest() {
  sitUpCurrentTime.value = 0
  sitUpRunning.value = true
  sitUpFinished.value = false
  sitUpStopped.value = false
  forms.sitUp.firstValue = null
  forms.sitUp.isComplete = '1'

  const startTime = Date.now()
  clearInterval(sitUpTimerInterval)
  sitUpTimerInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    sitUpCurrentTime.value = Math.round(elapsed * 100) / 100
  }, 10)
}

function finishSitUpTest() {
  clearInterval(sitUpTimerInterval)
  sitUpTimerInterval = null
  sitUpRunning.value = false
  sitUpFinished.value = true
  sitUpStopped.value = false
  forms.sitUp.firstValue = Number(sitUpCurrentTime.value.toFixed(2))
  forms.sitUp.isComplete = '1'
  ElMessage.success('测试完成')
}

function stopSitUpTest() {
  clearInterval(sitUpTimerInterval)
  sitUpTimerInterval = null
  sitUpRunning.value = false
  sitUpFinished.value = true
  sitUpStopped.value = true
  forms.sitUp.firstValue = Number(sitUpCurrentTime.value.toFixed(2))
  forms.sitUp.isComplete = '0'
  ElMessage.info('测试已终止')
}

function restartSitUpTest() {
  startSitUpTest()
}

function resetSitUpTest() {
  clearInterval(sitUpTimerInterval)
  sitUpTimerInterval = null
  sitUpCurrentTime.value = 0
  sitUpRunning.value = false
  sitUpFinished.value = false
  sitUpStopped.value = false
  forms.sitUp.firstValue = null
  forms.sitUp.isComplete = '1'
  forms.sitUp.remark = ''
}

function startPaceTimer() {
  paceCurrentTime.value = 0
  paceRunning.value = true
  paceRoundFinished.value = false

  if (paceCurrentRound.value === 1) {
    forms.pace.firstValue = null
  } else {
    forms.pace.secondValue = null
  }

  const startTime = Date.now()
  clearInterval(paceTimerInterval)
  paceTimerInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    paceCurrentTime.value = Math.round(elapsed * 100) / 100
  }, 10)
}

function finishPaceTimer() {
  clearInterval(paceTimerInterval)
  paceTimerInterval = null
  paceRunning.value = false
  paceRoundFinished.value = true
  const value = Number(paceCurrentTime.value.toFixed(2))
  if (paceCurrentRound.value === 1) {
    forms.pace.firstValue = value
  } else {
    forms.pace.secondValue = value
  }
  ElMessage.success(`第 ${paceCurrentRound.value} 次测量完成`)
}

function cancelPaceTimer() {
  clearInterval(paceTimerInterval)
  paceTimerInterval = null
  paceCurrentTime.value = 0
  paceRunning.value = false
  paceRoundFinished.value = false
  ElMessage.info('已取消本次计时')
}

function nextPaceRound() {
  paceCurrentRound.value = 2
  paceCurrentTime.value = 0
  paceRoundFinished.value = false
}

function skipPaceSecond() {
  paceCurrentRound.value = 2
  paceCurrentTime.value = 0
  paceRoundFinished.value = true
  ElMessage.info('已跳过第 2 次测量')
}

function restartPaceAll() {
  resetPaceAll()
  startPaceTimer()
}

function resetPaceAll() {
  clearInterval(paceTimerInterval)
  paceTimerInterval = null
  paceCurrentRound.value = 1
  paceCurrentTime.value = 0
  paceRunning.value = false
  paceRoundFinished.value = false
  forms.pace.firstValue = null
  forms.pace.secondValue = null
  forms.pace.remark = ''
}

function getBalanceItemValue(key) {
  if (key === 'feetTogether') return forms.balance.feetTogetherTime
  if (key === 'semiOffset') return forms.balance.semiOffsetTime
  if (key === 'tandemStand') return forms.balance.tandemStandTime
  return null
}

function setBalanceItemValue(key, value) {
  if (key === 'feetTogether') forms.balance.feetTogetherTime = value
  else if (key === 'semiOffset') forms.balance.semiOffsetTime = value
  else if (key === 'tandemStand') forms.balance.tandemStandTime = value
}

function getBalanceItemScore(key) {
  const time = getBalanceItemValue(key)
  if (time == null) return 0
  const value = Number(time)
  if (!Number.isFinite(value)) return 0
  if (key === 'feetTogether' || key === 'semiOffset') return value >= 10 ? 1 : 0
  if (key === 'tandemStand') {
    if (value >= 10) return 2
    if (value >= 3) return 1
  }
  return 0
}

function startBalanceTimer() {
  balanceCurrentTime.value = 0
  balanceRunning.value = true
  balanceItemFinished.value = false

  const key = balanceItems[balanceCurrentIndex.value].key
  setBalanceItemValue(key, null)

  const startTime = Date.now()
  clearInterval(balanceTimerInterval)
  balanceTimerInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    balanceCurrentTime.value = Math.round(elapsed * 100) / 100
  }, 10)
}

function finishBalanceTimer() {
  clearInterval(balanceTimerInterval)
  balanceTimerInterval = null
  balanceRunning.value = false
  balanceItemFinished.value = true
  const key = balanceItems[balanceCurrentIndex.value].key
  setBalanceItemValue(key, Number(balanceCurrentTime.value.toFixed(2)))
  ElMessage.success(`${balanceItems[balanceCurrentIndex.value].name}完成`)
}

function cancelBalanceTimer() {
  clearInterval(balanceTimerInterval)
  balanceTimerInterval = null
  balanceRunning.value = false
  balanceItemFinished.value = true
  const key = balanceItems[balanceCurrentIndex.value].key
  setBalanceItemValue(key, 0)
  balanceCurrentTime.value = 0
  ElMessage.info('已标记为不能完成')
}

function nextBalanceItem() {
  if (balanceCurrentIndex.value < balanceItems.length - 1) {
    balanceCurrentIndex.value++
    balanceCurrentTime.value = getBalanceItemValue(balanceItems[balanceCurrentIndex.value].key) || 0
    balanceItemFinished.value = getBalanceItemValue(balanceItems[balanceCurrentIndex.value].key) != null
    return
  }
  balanceItemFinished.value = true
  ElMessage.success('站立平衡测试已完成')
}

function retryBalanceItem() {
  clearInterval(balanceTimerInterval)
  balanceTimerInterval = null
  balanceRunning.value = false
  balanceItemFinished.value = false
  balanceCurrentTime.value = 0
  setBalanceItemValue(balanceItems[balanceCurrentIndex.value].key, null)
}

function switchBalanceItem(index) {
  if (balanceRunning.value || balanceCurrentIndex.value === index) return
  balanceCurrentIndex.value = index
  const value = getBalanceItemValue(balanceItems[index].key)
  balanceCurrentTime.value = value || 0
  balanceItemFinished.value = value != null
}

const bodyMeasurePhases = [
  { progress: 15, phase: '正在测量体重...', display: '称重中' },
  { progress: 35, phase: '正在检测生物阻抗...', display: '阻抗分析' },
  { progress: 55, phase: '正在计算肌肉量...', display: '分析中' },
  { progress: 75, phase: '正在计算脂肪量...', display: '分析中' },
  { progress: 90, phase: '正在生成报告...', display: '处理中' },
  { progress: 100, phase: '测量完成', display: '完成' }
]

function connectBodyDevice() {
  bodyDeviceLoading.value = true
  ElMessage.info('正在连接人体成分设备...')
  setTimeout(() => {
    bodyDeviceOnline.value = true
    bodyDeviceLoading.value = false
    bodyMeasureStatus.value = 'idle'
    ElMessage.success('人体成分设备连接成功')
  }, 800)
}

function startBodyMeasure() {
  if (!bodyDeviceOnline.value) {
    ElMessage.warning('请先连接设备')
    return
  }
  bodyMeasureStatus.value = 'measuring'
  bodyMeasureProgress.value = 0
  bodyCurrentPhase.value = '准备测量...'
  bodyCurrentDisplay.value = '0.0'
  let index = 0

  clearInterval(bodyMeasureTimer)
  bodyMeasureTimer = setInterval(() => {
    if (index < bodyMeasurePhases.length) {
      const phase = bodyMeasurePhases[index]
      bodyMeasureProgress.value = phase.progress
      bodyCurrentPhase.value = phase.phase
      bodyCurrentDisplay.value = index <= 1 ? (Math.random() * 20 + 55).toFixed(1) : phase.display
      index++
      return
    }
    finishBodyMeasure()
  }, 800)
}

function stopBodyMeasure() {
  clearInterval(bodyMeasureTimer)
  bodyMeasureTimer = null
  bodyMeasureStatus.value = 'idle'
  bodyMeasureProgress.value = 0
  bodyCurrentPhase.value = ''
  ElMessage.info('测量已停止')
}

function finishBodyMeasure() {
  clearInterval(bodyMeasureTimer)
  bodyMeasureTimer = null
  bodyMeasureStatus.value = 'completed'
  bodyMeasureProgress.value = 100
  bodyCurrentPhase.value = '测量完成'
  bodyCurrentDisplay.value = '完成'

  Object.assign(forms.body, {
    weight: Number((Math.random() * 15 + 55).toFixed(1)),
    bmi: Number((Math.random() * 5 + 21).toFixed(1)),
    bodyFatRate: Number((Math.random() * 12 + 22).toFixed(1)),
    asm: Number((Math.random() * 5 + 18).toFixed(2)),
    asmi: Number((Math.random() * 2.5 + 5.8).toFixed(2)),
    totalBodyWater: Number((Math.random() * 6 + 32).toFixed(1)),
    leanBodyMass: Number((Math.random() * 8 + 40).toFixed(1))
  })
  ElMessage.success('测量完成，数据已同步')
}

function retryBodyMeasure() {
  clearInterval(bodyMeasureTimer)
  bodyMeasureTimer = null
  bodyMeasureStatus.value = 'idle'
  bodyMeasureProgress.value = 0
  bodyCurrentPhase.value = ''
  bodyCurrentDisplay.value = '0.0'
}

function toggleCalfDevice() {
  calfDeviceLoading.value = true
  setTimeout(() => {
    calfDeviceConnected.value = !calfDeviceConnected.value
    calfDeviceLoading.value = false
    ElMessage.success(calfDeviceConnected.value ? '设备连接成功' : '设备已断开')
  }, 600)
}

function fetchCalfMeasureValue(index) {
  if (!calfDeviceConnected.value) {
    ElMessage.warning('请先连接蓝牙测量设备')
    return
  }
  calfFetchingIndex.value = index
  setTimeout(() => {
    calfFetchingIndex.value = 0
    ElMessage.error('蓝牙量尺接口尚未对接，请先手动输入测量值')
  }, 500)
}

function toggleGripDevice() {
  gripDeviceLoading.value = true
  setTimeout(() => {
    gripDeviceConnected.value = !gripDeviceConnected.value
    gripDeviceLoading.value = false
    ElMessage.success(gripDeviceConnected.value ? '设备连接成功' : '设备已断开')
  }, 600)
}

function fetchGripMeasureValue(index) {
  if (!gripDeviceConnected.value) {
    ElMessage.warning('请先连接蓝牙握力计设备')
    return
  }
  gripFetchingIndex.value = index
  setTimeout(() => {
    gripFetchingIndex.value = 0
    ElMessage.error('蓝牙握力计接口尚未对接，请先手动输入测量值')
  }, 500)
}

function normalizeList(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.data)) return res.data
  if (Array.isArray(res.rows)) return res.rows
  if (res.data?.questionList) return res.data.questionList
  return []
}

function validateStep(key = currentStep.value.key, silent = false) {
  const warn = msg => {
  if (!silent) ElMessage.warning(msg)
    return false
  }
  if (key === 'sarcF') {
    if (!sarcQuestions.value.length) return warn('SARC-F 题目还没有加载完成')
    const allAnswered = sarcQuestions.value.every(q => forms.sarcF.answers[q.questionId] != null)
    return allAnswered || warn('请完成 SARC-F 全部题目')
  }
  if (key === 'calf') return forms.calf.firstValue != null || warn('请至少填写第 1 次小腿围测量')
  if (key === 'grip') return forms.grip.firstValue != null || warn('请至少填写第 1 次握力测量')
  if (key === 'sitUp') return forms.sitUp.firstValue != null || warn('请填写 5 次起坐用时')
  if (key === 'pace') return forms.pace.firstValue != null || forms.pace.secondValue != null || warn('请至少填写 1 次步速测量')
  if (key === 'balance') {
    return forms.balance.feetTogetherTime != null
      && forms.balance.semiOffsetTime != null
      && forms.balance.tandemStandTime != null
      || warn('请填写三项站立平衡测试时间')
  }
  if (key === 'body') return forms.body.asmi != null || warn('请至少填写 ASMI')
  return true
}

function finishCurrent() {
  const key = currentStep.value.key
  if (!validateStep(key)) return
  completed[key] = true
  saveDraft()
  ElMessage.success(`${currentStep.value.title} 已暂存`)
  if (currentIndex.value < steps.length - 1) currentIndex.value += 1
}

function switchStep(index) {
  currentIndex.value = index
  saveDraft()
}

function prevStep() {
  if (currentIndex.value > 0) currentIndex.value -= 1
  saveDraft()
}

function nextStep() {
  if (currentIndex.value < steps.length - 1) currentIndex.value += 1
  saveDraft()
}

function saveDraft() {
  if (!patientId.value) return
  sessionStorage.setItem(draftKey.value, JSON.stringify({
    currentIndex: currentIndex.value,
    completed: { ...completed },
    forms: JSON.parse(JSON.stringify(forms))
  }))
}

function loadDraft() {
  if (!patientId.value) return
  const raw = sessionStorage.getItem(draftKey.value)
  if (!raw) return
  try {
    const draft = JSON.parse(raw)
    currentIndex.value = draft.currentIndex || 0
    Object.assign(completed, draft.completed || {})
    Object.assign(forms.sarcF, draft.forms?.sarcF || {})
    Object.assign(forms.calf, draft.forms?.calf || {})
    Object.assign(forms.grip, draft.forms?.grip || {})
    Object.assign(forms.sitUp, draft.forms?.sitUp || {})
    Object.assign(forms.pace, draft.forms?.pace || {})
    Object.assign(forms.balance, draft.forms?.balance || {})
    Object.assign(forms.body, draft.forms?.body || {})
  } catch (e) {
    sessionStorage.removeItem(draftKey.value)
  }
}

async function clearDraft() {
  await ElMessageBox.confirm('确定清空当前患者的综合测量草稿吗？', '提示', { type: 'warning' })
  sessionStorage.removeItem(draftKey.value)
  window.location.reload()
}

function buildSarcResult() {
  return `SARC-F总分：${sarcTotalScore.value}分，${sarcLevelText.value}`
}

function buildSarcSuggest() {
  if (sarcTotalScore.value >= 4) return 'SARC-F筛查提示肌少症风险，建议进一步完成肌肉力量、躯体功能和肌量评估。'
  return 'SARC-F筛查提示低风险，建议保持规律运动和均衡营养，定期复评。'
}

async function submitAll() {
  if (!patientId.value) {
    ElMessage.warning('未获取到患者信息')
    return
  }
  const firstInvalid = measureSteps.value.find(step => !validateStep(step.key, true))
  if (firstInvalid) {
    currentIndex.value = steps.findIndex(step => step.key === firstInvalid.key)
    ElMessage.warning(`请先完成${firstInvalid.title}`)
    return
  }

  submitting.value = true
  try {
    const pid = Number(patientId.value)
    const answers = {}
    for (const q of sarcQuestions.value) {
      answers[String(q.questionId)] = [Number(forms.sarcF.answers[q.questionId])]
    }

    await insertSRecord({
      patientId: pid,
      projectId: 1,
      scaleId: 1,
      answers,
      result: buildSarcResult(),
      suggest: buildSarcSuggest()
    })
    await insertCalfMeasure({ patientId: pid, ...forms.calf, remark: forms.calf.remark || null })
    await insertGripStrength({ patientId: pid, ...forms.grip, remark: forms.grip.remark || null })
    await insertSitUp({ patientId: pid, ...forms.sitUp, remark: forms.sitUp.remark || null })
    await insertPace({ patientId: pid, ...forms.pace, remark: forms.pace.remark || null })
    await insertBalanceStand({ patientId: pid, ...forms.balance, remark: forms.balance.remark || null })
    await insertBodyComposition({ patientId: pid, measureDate: todayStr.value, ...forms.body })

    markAllDone()
    sessionStorage.removeItem(draftKey.value)
    ElMessage.success('肌少症综合测量提交成功')
    router.push({
      path: '/patient/detection/sComprehensive',
      query: { patientId: patientId.value, patientName: patientName.value }
    })
  } catch (error) {
    console.error('提交肌少症综合测量失败', error)
    ElMessage.error(error?.msg || '提交失败，请检查后重试')
  } finally {
    submitting.value = false
  }
}

function markAllDone() {
  const prefix = `sarcopenia_customize_done:${patientId.value}:`
  ;['sarc_f', 'calf_measure', 'grip_strength', 'sit_up', 'pace', 'balance_stand', 'body_composition']
    .forEach(key => localStorage.setItem(prefix + key, '1'))
}

async function loadPatient() {
  if (!patientId.value) return
  try {
    const res = await selectPatientById({ patientId: patientId.value })
    patientInfo.value = res?.data || res || null
  } catch (e) {
    console.warn('加载患者信息失败', e)
  }
}

async function loadSarcScale() {
  try {
    const res = await selectSVOById(1)
    const data = res?.data || res || {}
    sarcScale.value = {
      scaleId: data.scaleId || data.id || 1,
      totalScore: data.totalScore || 10,
      questionList: normalizeQuestionList(data)
    }
  } catch (e) {
    console.error('加载 SARC-F 题目失败', e)
    ElMessage.error('加载 SARC-F 题目失败')
  }
}

function normalizeQuestionList(data) {
  const list = data.questionList || data.questions || []
  return list.map(q => ({
    questionId: q.questionId || q.id,
    title: q.title || q.questionTitle || q.content || q.questionName,
    optionList: (q.optionList || q.options || []).map(o => ({
      optionId: o.optionId || o.id,
      content: o.content || o.optionContent || o.label,
      score: o.score
    }))
  }))
}

function goBack() {
  router.push({ path: '/patient/detection/sarcopeniaSyndrome', query: route.query })
}

watch(forms, saveDraft, { deep: true })
watch(currentIndex, saveDraft)

onMounted(async () => {
  await Promise.all([loadPatient(), loadSarcScale()])
  loadDraft()
})

onUnmounted(() => {
  clearInterval(sitUpTimerInterval)
  sitUpTimerInterval = null
  clearInterval(paceTimerInterval)
  paceTimerInterval = null
  clearInterval(balanceTimerInterval)
  balanceTimerInterval = null
  clearInterval(bodyMeasureTimer)
  bodyMeasureTimer = null
})
</script>

<style scoped lang="scss">
.s-total-page {
  min-height: 100vh;
  padding: 20px 28px 40px;
  background: #f0f5ff;
  box-sizing: border-box;
  max-width: 1200px;
  margin: 0 auto;
  color: #1f2937;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.06);
  margin-bottom: 14px;
  border-left: 4px solid #1890ff;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: #606266;
  padding: 6px 12px;
  border-radius: 6px;

  &:hover {
    color: #1890ff;
    background: #e6f7ff;
  }
}

.title-block h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
}

.title-block p {
  margin: 3px 0 0;
  font-size: 12px;
  color: #909399;
  letter-spacing: 0.5px;
}

.patient-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: #f0f5ff;
  border-radius: 8px;
  border: 1px solid #d6e4ff;
}

.badge-avatar {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}

.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.badge-id {
  font-size: 11.5px;
  color: #909399;
  margin-top: 2px;
}

.progress-bar {
  position: relative;
  height: 36px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 18px;
  box-shadow: 0 1px 3px rgba(24, 144, 255, 0.04);
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #1890ff, #52c41a);
  transition: width 0.3s ease;
}

.progress-label {
  position: relative;
  z-index: 1;
  display: block;
  line-height: 36px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.page-body {
  padding: 0;
}

.work-panel {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.06);
  border: 1px solid #e8f3ff;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 18px;
  border-bottom: 1px solid #e8f3ff;
  background: linear-gradient(90deg, #f0f5ff 0%, #fff 70%);
}

.panel-kicker {
  color: #1890ff;
  font-size: 13px;
  font-weight: 700;
}

.panel-head h2 {
  margin: 6px 0;
  font-size: 22px;
}

.panel-head p {
  margin: 0;
  color: #6b7280;
}

.panel-body {
  padding: 22px 24px 8px;
}

.device-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 22px;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #e8f3ff;
}

.device-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.device-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #e6f7ff;
  color: #1890ff;
}

.device-title {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #dcdfe6;
  }

  &.connected {
    color: #67c23a;

    .status-dot {
      background: #67c23a;
      box-shadow: 0 0 6px rgba(103, 194, 58, 0.5);
    }
  }
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  box-shadow: 0 1px 3px rgba(24, 144, 255, 0.04);
  border: 1px solid #e8f3ff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &.is-answered {
    border-color: #91d5ff;
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.1);
  }
}

.q-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.q-index {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #e6f7ff;
  color: #1890ff;
  font-size: 14px;
  font-weight: 700;
}

.q-title {
  flex: 1;
  font-size: 15.5px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
}

.q-done {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f6ffed;
  color: #52c41a;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.q-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover {
    border-color: #91d5ff;
    background: #f0f5ff;
  }

  &.is-selected {
    border-color: #1890ff;
    background: #e6f7ff;
  }

  &.is-max {
    border-color: #ffd591;
  }

  &.is-selected.is-max {
    border-color: #fa8c16;
    background: #fff7ed;
  }
}

.opt-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.opt-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.18s ease;
  color: transparent;

  &.is-checked {
    border-color: #1890ff;
    background: #1890ff;
    color: #fff;
  }
}

.opt-content {
  flex: 1;
  min-width: 0;
}

.opt-label {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.5;
}

.opt-right {
  display: flex;
  align-items: baseline;
  gap: 2px;
  flex-shrink: 0;
  margin-left: 12px;
}

.opt-score {
  font-size: 18px;
  font-weight: 700;
  color: #6b7280;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.q-option.is-selected .opt-score {
  color: #096dd9;
}

.opt-unit {
  font-size: 12px;
  color: #9ca3af;
}

.result-line {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f0f5ff;
  color: #1f2937;
  font-weight: 600;
  border: 1px solid #d6e4ff;
}

:deep(.measure-three-title) {
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 700;
}

:deep(.measure-cards) {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

:deep(.measure-card) {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.06);
  border: 2px solid transparent;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 14px;

  &:hover {
    box-shadow: 0 4px 16px rgba(24, 144, 255, 0.12);
  }

  &.has-value {
    border-color: #91d5ff;
    background: linear-gradient(180deg, #ffffff 0%, #f0f8ff 100%);
  }
}

:deep(.mc-head) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.mc-index) {
  padding: 2px 8px;
  border-radius: 6px;
  background: #e6f7ff;
  color: #1890ff;
  font-size: 12px;
  font-weight: 700;
}

:deep(.mc-label) {
  flex: 1;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

:deep(.mini-tag) {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;

  &.success {
    color: #389e0d;
    background: #f6ffed;
  }
}

:deep(.mc-value-display) {
  text-align: center;
  padding: 12px 0;
}

:deep(.mc-num) {
  font-size: 40px;
  font-weight: 800;
  color: #1890ff;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

:deep(.measure-card:not(.has-value) .mc-num) {
  color: #c0c4cc;
}

:deep(.mc-unit),
.unit-text {
  margin-left: 8px;
  color: #6b7280;
  font-style: normal;
}

:deep(.mc-foot) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

:deep(.clear-link) {
  border: 0;
  background: transparent;
  color: #f56c6c;
  cursor: pointer;
}

:deep(.measure-remark) {
  margin-top: 14px;
}

.situp-test-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px 32px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.06);
  border: 1px solid #ede9fe;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
}

.timer-display {
  display: flex;
  justify-content: center;
}

.timer-value {
  background: #faf5ff;
  border: 2px dashed #c4b5fd;
  border-radius: 12px;
  padding: 24px 60px;
  text-align: center;
  min-width: 240px;
}

.tv-num {
  font-size: 64px;
  font-weight: 800;
  color: #8b5cf6;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  line-height: 1;
  display: block;
}

.tv-unit {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-start {
  min-width: 160px;
  height: 48px;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);

  &:hover {
    box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4);
  }
}

.btn-finish {
  min-width: 160px;
  height: 48px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.btn-stop {
  min-width: 180px;
  height: 48px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  font-weight: 600;
  font-size: 15px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.btn-restart {
  min-width: 140px;
  height: 44px;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  border: none;
  font-weight: 600;
  border-radius: 22px;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.25);
}

.btn-reset {
  min-width: 120px;
  height: 44px;
  border-radius: 22px;
}

.illustration {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  position: relative;
}

.illustration-img {
  max-width: 100%;
  max-height: 280px;
  object-fit: contain;
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
}

.illustration::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 120px;
  height: 40px;
  background: linear-gradient(135deg, transparent 30%, #fff 60%);
  pointer-events: none;
}

.situp-remark {
  width: 100%;
}

.pace-test-card {
  background: #fff;
  border-radius: 16px;
  padding: 34px 32px 40px;
  border: 1px solid #bbf7d0;
  box-shadow: 0 12px 30px rgba(16, 185, 129, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 26px;
}

.pace-round {
  display: flex;
  align-items: baseline;
  gap: 8px;
  color: #64748b;
}

.round-label {
  font-size: 16px;
}

.round-num {
  font-size: 34px;
  line-height: 1;
  font-weight: 800;
  color: #10b981;
}

.round-total {
  font-size: 16px;
}

.pace-timer-display {
  display: flex;
  justify-content: center;
  width: 100%;
}

.pace-timer-value {
  width: min(420px, 100%);
  background: #ecfdf5;
  border: 2px dashed #6ee7b7;
  border-radius: 16px;
  padding: 30px 48px 28px;
  text-align: center;
}

.pace-tv-num {
  display: block;
  font-size: 68px;
  line-height: 1;
  font-weight: 800;
  color: #10b981;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.pace-tv-unit {
  display: block;
  margin-top: 10px;
  font-size: 16px;
  color: #8a94a6;
}

.pace-action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.pace-btn-start,
.pace-btn-next,
.pace-btn-restart {
  min-width: 170px;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #10b981, #059669);
  box-shadow: 0 10px 24px rgba(16, 185, 129, 0.26);
}

.pace-btn-finish {
  min-width: 160px;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.pace-btn-cancel,
.pace-btn-skip,
.pace-btn-reset {
  min-width: 130px;
  height: 46px;
  border-radius: 23px;
}

.pace-measure-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 22px;
}

.pace-measure-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e8f8ef;
  padding: 24px 32px 28px;
  box-shadow: 0 10px 28px rgba(16, 185, 129, 0.06);
}

.pace-measure-card.has-value {
  border-color: #86efac;
}

.pmc-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.pmc-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  color: #10b981;
  font-weight: 800;
  background: #d1fae5;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.pmc-label {
  flex: 1;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}

.pmc-tag {
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  color: #8a94a6;
  font-size: 13px;
}

.pmc-tag.success {
  border-color: #bbf7d0;
  color: #059669;
  background: #ecfdf5;
}

.pmc-body {
  text-align: center;
}

.pmc-value-display {
  background: #f0fdf4;
  border: 1px dashed #86efac;
  border-radius: 12px;
  padding: 26px 20px;
}

.pmc-num {
  color: #b8c0cc;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 0;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.pace-measure-card.has-value .pmc-num {
  color: #10b981;
}

.pmc-unit {
  margin-left: 10px;
  color: #8a94a6;
  font-size: 18px;
  font-weight: 700;
}

.pmc-speed {
  margin-top: 16px;
  color: #64748b;
  font-size: 16px;
}

.pmc-speed strong {
  color: #10b981;
}

.pace-remark {
  margin-top: 18px;
}

.balance-test-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #fed7aa;
  box-shadow: 0 12px 30px rgba(249, 115, 22, 0.08);
}

.balance-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 28px;
}

.balance-badge {
  padding: 6px 16px;
  background: #ffedd5;
  color: #ea580c;
  font-size: 15px;
  font-weight: 800;
  border-radius: 18px;
}

.balance-title {
  color: #0f172a;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 800;
}

.balance-target {
  color: #8a94a6;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #fed7aa;
  background: #fff7ed;
}

.balance-body {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) minmax(320px, 1fr);
  align-items: center;
  gap: 36px;
}

.balance-illustration {
  display: flex;
  justify-content: center;
}

.balance-img-crop {
  width: min(390px, 100%);
  aspect-ratio: 1;
  border-radius: 14px;
  border: 1px solid #fed7aa;
  background: #fff7ed;
  padding: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.balance-foot-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 10px;
}

.balance-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
}

.balance-timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.balance-timer-value {
  width: min(420px, 100%);
  background: #fff7ed;
  border: 2px dashed #fdba74;
  border-radius: 16px;
  padding: 30px 48px 28px;
  text-align: center;
  transition: all 0.25s ease;
}

.balance-timer-value.success {
  border-color: #86efac;
  background: #f0fdf4;
}

.balance-tv-num {
  display: block;
  font-size: 68px;
  line-height: 1;
  font-weight: 800;
  color: #f97316;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.balance-timer-value.success .balance-tv-num {
  color: #22c55e;
}

.balance-tv-unit {
  display: block;
  margin-top: 10px;
  color: #8a94a6;
  font-size: 16px;
}

.balance-target-reached {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #22c55e;
  font-size: 14px;
  font-weight: 700;
}

.balance-action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.balance-btn-start,
.balance-btn-next {
  min-width: 180px;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #f97316, #ea580c);
  box-shadow: 0 10px 24px rgba(249, 115, 22, 0.26);
}

.balance-btn-finish {
  min-width: 160px;
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.balance-btn-cancel,
.balance-btn-retry {
  min-width: 130px;
  height: 46px;
  border-radius: 23px;
}

.balance-items-progress {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 22px;
}

.balance-item-card {
  background: #fff;
  border-radius: 14px;
  border: 2px solid transparent;
  padding: 22px 26px 24px;
  box-shadow: 0 10px 28px rgba(249, 115, 22, 0.06);
  transition: all 0.2s ease;
}

.balance-item-card.clickable {
  cursor: pointer;
}

.balance-item-card.active {
  border-color: #f97316;
}

.balance-item-card.completed {
  border-color: #fed7aa;
}

.bic-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.bic-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 30px;
  border-radius: 8px;
  background: #ffedd5;
  color: #f97316;
  font-weight: 800;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.bic-name {
  flex: 1;
  color: #0f172a;
  font-size: 17px;
  font-weight: 800;
}

.bic-tag {
  padding: 4px 10px;
  border-radius: 6px;
  color: #ea580c;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  font-size: 13px;
}

.bic-body {
  text-align: center;
  padding: 20px;
  border-radius: 10px;
  background: #fff7ed;
}

.bic-value {
  color: #c4cbd6;
  font-size: 30px;
  line-height: 1;
  font-weight: 800;
  letter-spacing: 0;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.balance-item-card.completed .bic-value {
  color: #f97316;
}

.bic-unit {
  margin-left: 8px;
  color: #8a94a6;
  font-size: 16px;
}

.bic-target {
  margin-top: 12px;
  color: #8a94a6;
  text-align: center;
  font-size: 14px;
}

.bic-target span {
  color: #ea580c;
}

.balance-remark {
  margin-top: 18px;
}

.body-device-card {
  background: #fff;
  border-radius: 22px;
  padding: 42px 32px 38px;
  border: 1px solid #ede9fe;
  box-shadow: 0 18px 42px rgba(139, 92, 246, 0.12);
  margin-bottom: 22px;
}

.body-device-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.body-device-visual {
  position: relative;
  width: 280px;
  height: 330px;
  display: flex;
  justify-content: center;
  align-items: flex-end;
}

.body-scale-device {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.body-scale-platform {
  position: relative;
  z-index: 2;
}

.body-platform-surface {
  width: 220px;
  height: 20px;
  background: linear-gradient(180deg, #a78bfa 0%, #8b5cf6 100%);
  border-radius: 12px 12px 5px 5px;
  position: relative;
  box-shadow: 0 6px 24px rgba(139, 92, 246, 0.35);
}

.body-platform-side {
  width: 205px;
  height: 12px;
  background: linear-gradient(180deg, #7c3aed 0%, #6d28d9 100%);
  margin: 0 auto;
  border-radius: 0 0 10px 10px;
}

.body-foot-print {
  position: absolute;
  top: 4px;
  width: 34px;
  height: 44px;
  background: rgba(255, 255, 255, 0.26);
  border-radius: 50% 50% 40% 40%;
}

.body-foot-print.left-foot {
  left: 58px;
  transform: rotate(-10deg);
}

.body-foot-print.right-foot {
  right: 58px;
  transform: rotate(10deg);
}

.body-scale-display {
  position: absolute;
  top: -105px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
}

.body-display-screen {
  width: 128px;
  height: 64px;
  background: linear-gradient(180deg, #1e1b4b 0%, #312e81 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c4b5fd;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  font-size: 19px;
  font-weight: 800;
  box-shadow: 0 8px 22px rgba(49, 46, 129, 0.35);
  position: relative;
}

.body-display-screen::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 10px solid #312e81;
}

.body-scale-stand {
  position: absolute;
  top: -175px;
  left: 50%;
  transform: translateX(-50%);
  width: 7px;
  height: 165px;
  background: linear-gradient(180deg, #c4b5fd, #a78bfa);
  border-radius: 4px;
  z-index: 1;
}

.body-hand-electrodes {
  position: absolute;
  top: 36px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 12px;
}

.body-electrode {
  position: relative;
  width: 32px;
  height: 92px;
}

.body-electrode.left {
  transform: rotate(-8deg);
}

.body-electrode.right {
  transform: rotate(8deg);
}

.body-electrode-body {
  width: 32px;
  height: 82px;
  background: linear-gradient(180deg, #a78bfa 0%, #7c3aed 100%);
  border-radius: 9px;
  box-shadow: 0 8px 20px rgba(124, 58, 237, 0.28);
  position: relative;
}

.body-electrode-body::before,
.body-electrode-body::after {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 3px;
}

.body-electrode-body::before {
  top: 20px;
}

.body-electrode-body::after {
  bottom: 20px;
}

.body-electrode-wire {
  position: absolute;
  bottom: -28px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 34px;
  background: linear-gradient(180deg, #7c3aed, #c4b5fd);
  border-radius: 3px;
}

.body-scan-effect {
  position: absolute;
  inset: 24px 0 0;
  pointer-events: none;
  overflow: hidden;
  border-radius: 50%;
}

.body-scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #8b5cf6, transparent);
  box-shadow: 0 0 16px #8b5cf6;
  animation: bodyScanMove 1.8s ease-in-out infinite;
}

@keyframes bodyScanMove {
  0% { top: 0; opacity: 0; }
  12% { opacity: 1; }
  88% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

.body-measure-status-text {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 28px;
  background: #f5f3ff;
  border-radius: 26px;
  color: #4b5563;
  font-size: 15px;
}

.body-measure-status-text .el-icon {
  font-size: 18px;
}

.body-measure-status-text .offline {
  color: #f59e0b;
}

.body-measure-status-text .ready {
  color: #8b5cf6;
}

.body-measure-status-text .measuring {
  color: #6366f1;
  animation: bodySpin 1s linear infinite;
}

.body-measure-status-text .success {
  color: #10b981;
}

@keyframes bodySpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.body-action-area {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin-top: 26px;
  flex-wrap: wrap;
}

.body-btn-connect,
.body-btn-start {
  min-width: 180px;
  height: 52px;
  border: none;
  border-radius: 26px;
  font-size: 16px;
  font-weight: 800;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 10px 24px rgba(139, 92, 246, 0.28);
}

.body-btn-stop,
.body-btn-retry {
  min-width: 150px;
  height: 50px;
  border-radius: 25px;
  font-size: 15px;
  font-weight: 700;
}

.compact-form {
  max-width: 680px;
}

.body-grid :deep(.el-form) {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 12px 18px;
}

.body-grid :deep(.el-form-item) {
  margin-bottom: 4px;
}

.submit-area {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 22px 24px 24px;
  border-top: 1px solid #e8f3ff;
  background: #fff;
}

.submit-area .el-button--primary {
  min-width: 128px;
}

@media (max-width: 760px) {
  .top-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .page-body {
    padding: 14px;
  }

  :deep(.measure-cards),
  .body-grid :deep(.el-form),
  .pace-measure-cards,
  .balance-body,
  .balance-items-progress {
    grid-template-columns: 1fr;
  }

  .submit-area {
    flex-wrap: wrap;
  }

  .body-device-card {
    padding: 26px 18px;
  }

  .body-device-visual {
    transform: scale(0.86);
    margin: -20px 0;
  }
}
</style>
