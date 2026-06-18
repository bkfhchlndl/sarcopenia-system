import request from '@/utils/request'

/**
 * CGA老年综合评估 - 量表相关API接口
 */

/**
 * 查询综合评估量表列表（1-21号，含题目及选项）
 */
export function selectCgaScaleList() {
  return request({
    url: '/cgaScale/selectScaleList',
    method: 'get'
  })
}

/**
 * 查询基本日常生活活动能力评估量表（含题目及选项）
 */
export function selectDailyLifeScale() {
  return request({
    url: '/cgaScale/selectDailyLifeScale',
    method: 'get'
  })
}

/**
 * 查询工具性日常生活活动能力评估量表（含题目及选项）
 */
export function selectInstrumentalityLifeScale() {
  return request({
    url: '/cgaScale/selectInstrumentalityLifeScale',
    method: 'get'
  })
}

/**
 * 查询简易体能状况量表（SPPB，含题目及选项）
 */
export function selectSppbScale() {
  return request({
    url: '/cgaScale/selectSppbScale',
    method: 'get'
  })
}

/**
 * 查询视力视觉功能评估量表（含题目及选项）
 */
export function selectVisualAcuityScale() {
  return request({
    url: '/cgaScale/selectVisualAcuityScale',
    method: 'get'
  })
}

/** 兼容别名：视听力评估（指向视力视觉量表） */
export const selectVisionHearingScale = selectVisualAcuityScale

/**
 * 查询听力评估量表（含题目及选项）
 */
export function selectListeningScale() {
  return request({
    url: '/cgaScale/selectListeningScale',
    method: 'get'
  })
}

/**
 * 查询居家环境评估量表（含题目及选项）
 */
export function selectHomeEnvironmentScale() {
  return request({
    url: '/cgaScale/selectHomeEnvironmentScale',
    method: 'get'
  })
}

/**
 * 查询认知功能评估量表（含题目及选项）
 */
export function selectCognitionScale() {
  return request({
    url: '/cgaScale/selectCognitionScale',
    method: 'get'
  })
}

/** 兼容别名：认知评估（指向认知功能量表） */
export const selectCognitiveAssessmentScale = selectCognitionScale

/**
 * 查询衰弱评估量表（含题目及选项）
 */
export function selectFrailScale() {
  return request({
    url: '/cgaScale/selectFrailScale',
    method: 'get'
  })
}

/**
 * 查询尿失禁评估量表（含题目及选项）
 */
export function selectUrinaryIncontinenceScale() {
  return request({
    url: '/cgaScale/selectUrinaryIncontinenceScale',
    method: 'get'
  })
}

/**
 * 查询便秘评估量表（含题目及选项）
 */
export function selectConstipationScale() {
  return request({
    url: '/cgaScale/selectConstipationScale',
    method: 'get'
  })
}

/**
 * 查询睡眠障碍评估量表（含题目及选项）
 */
export function selectInsomniaScale() {
  return request({
    url: '/cgaScale/selectInsomniaScale',
    method: 'get'
  })
}

/**
 * 查询慢性疼痛评估量表（含题目及选项）
 */
export function selectChronicPainScale() {
  return request({
    url: '/cgaScale/selectChronicPainScale',
    method: 'get'
  })
}

/**
 * 查询压力性损伤风险评估量表（含题目及选项）
 */
export function selectPressureInjuryScale() {
  return request({
    url: '/cgaScale/selectPressureInjuryScale',
    method: 'get'
  })
}

/**
 * 查询吞咽功能评估量表（含题目及选项）
 */
export function selectSwallowScale() {
  return request({
    url: '/cgaScale/selectSwallowScale',
    method: 'get'
  })
}

/**
 * 查询营养风险筛查量表（含题目及选项）
 */
export function selectNutritionScale() {
  return request({
    url: '/cgaScale/selectNutritionScale',
    method: 'get'
  })
}

/**
 * 查询微营养评估量表（含题目及选项）
 */
export function selectMicroNutritionScale() {
  return request({
    url: '/cgaScale/selectMicroNutritionScale',
    method: 'get'
  })
}

/**
 * 查询营养标准评估量表（含题目及选项）
 */
export function selectNutritionalStandardsScale() {
  return request({
    url: '/cgaScale/selectNutritionalStandardsScale',
    method: 'get'
  })
}

/**
 * 查询谵妄评估量表（含题目及选项）
 */
export function selectDeliriumScale() {
  return request({
    url: '/cgaScale/selectDeliriumScale',
    method: 'get'
  })
}

/**
 * 查询抑郁评估量表（含题目及选项）
 */
export function selectDepressionScale() {
  return request({
    url: '/cgaScale/selectDepressionScale',
    method: 'get'
  })
}

/**
 * 查询跌倒风险筛查量表（含题目及选项）
 */
export function selectFallScale() {
  return request({
    url: '/cgaScale/selectFallScale',
    method: 'get'
  })
}

/**
 * 查询多重用药评估量表（含题目及选项）
 */
export function selectMedicationScale() {
  return request({
    url: '/cgaScale/selectMedicationScale',
    method: 'get'
  })
}

/**
 * 查询焦虑筛查量表（含题目及选项）
 */
export function selectAnxietyScale() {
  return request({
    url: '/cgaScale/selectAnxietyScale',
    method: 'get'
  })
}

/**
 * 查询基本信息采集量表（含题目及选项）
 */
export function selectBasicInformationScale() {
  return request({
    url: '/cgaScale/selectBasicInformationScale',
    method: 'get'
  })
}

/**
 * 提交量表评估记录
 * @param {Object} data 评估提交参数
 */
export function insertCgaRecord(data) {
  return request({
    url: '/cgaRecord/insertCgaRecord',
    method: 'post',
    data
  })
}

/**
 * 条件查询评估记录列表
 * @param {Object} params 查询参数
 */
export function selectCgaRecordList(params) {
  return request({
    url: '/cgaRecord/selectCgaRecordList',
    method: 'get',
    params
  })
}