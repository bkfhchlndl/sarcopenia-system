import request from '@/utils/request'

/**
 * 查询所有肌少症量表列表（轻量，只查主表）
 */
export function selectSVOList() {
  return request({
    url: '/svo/selectSVOList',
    method: 'get'
  })
}

/**
 * 根据ID查询单个肌少症量表详情（带题目+选项）
 * @param {number|string} id 量表ID
 */
export function selectSVOById(id) {
  return request({
    url: `/svo/selectSVOById/${id}`,
    method: 'get'
  })
}

/**
 * 提交肌少症量表评估记录
 * @param {Object} data 评估提交参数
 */
export function insertSRecord(data) {
  return request({
    url: '/sRecord/insertSRecord',
    method: 'post',
    data
  })
}

/**
 * 查询患者的肌少症评估记录列表
 * @param {Object} params 查询参数
 */
export function selectSRecordList(params) {
  return request({
    url: '/sRecord/selectSRecordList',
    method: 'get',
    params
  })
}

/**
 * 查询患者小腿围测量记录列表
 * @param {Object} params 查询参数
 */
export function selectCalfMeasureList(params) {
  return request({
    url: '/sCalfMeasure/selectByPatientId',
    method: 'get',
    params
  })
}

/**
 * 新增小腿围测量记录
 * @param {Object} data 测量数据
 */
export function insertCalfMeasure(data) {
  return request({
    url: '/sCalfMeasure/insert',
    method: 'post',
    data
  })
}

/**
 * 查询患者握力测量记录列表
 * @param {Object} params 查询参数
 */
export function selectGripStrengthList(params) {
  return request({
    url: '/sGripStrength/selectByPatientId',
    method: 'get',
    params
  })
}

/**
 * 新增握力测量记录
 * @param {Object} data 测量数据
 */
export function insertGripStrength(data) {
  return request({
    url: '/sGripStrength/insert',
    method: 'post',
    data
  })
}

/**
 * 查询患者5次起坐测量记录列表
 * @param {Object} params 查询参数
 */
export function selectSitUpList(params) {
  return request({
    url: '/sSitUp/selectByPatientId',
    method: 'get',
    params
  })
}

/**
 * 新增5次起坐测量记录
 * @param {Object} data 测量数据
 */
export function insertSitUp(data) {
  return request({
    url: '/sSitUp/insert',
    method: 'post',
    data
  })
}

/**
 * 查询患者6米步速测量记录列表
 * @param {Object} params 查询参数
 */
export function selectPaceList(params) {
  return request({
    url: '/sPace/selectByPatientId',
    method: 'get',
    params
  })
}

/**
 * 新增6米步速测量记录
 * @param {Object} data 测量数据
 */
export function insertPace(data) {
  return request({
    url: '/sPace/insert',
    method: 'post',
    data
  })
}

/**
 * 查询患者站立平衡测试记录列表
 * @param {Object} params 查询参数
 */
export function selectBalanceStandList(params) {
  return request({
    url: '/sBalanceStand/selectByPatientId',
    method: 'get',
    params
  })
}

/**
 * 新增站立平衡测试记录
 * @param {Object} data 测试数据
 */
export function insertBalanceStand(data) {
  return request({
    url: '/sBalanceStand/insert',
    method: 'post',
    data
  })
}

/**
 * 查询患者人体成分测量记录列表
 * @param {Object} params 查询参数
 */
export function selectBodyCompositionList(params) {
  return request({
    url: '/sBodyComposition/selectByPatientId',
    method: 'get',
    params
  })
}

/**
 * 新增人体成分测量记录
 * @param {Object} data 测量数据
 */
export function insertBodyComposition(data) {
  return request({
    url: '/sBodyComposition/insert',
    method: 'post',
    data
  })
}

/**
 * 生成肌少症综合评估报告
 * @param {Number|String} patientId 患者ID
 */
export function generateSReport(patientId) {
  return request({
    url: `/sRecord/generateReport/${patientId}`,
    method: 'post'
  })
}

/**
 * 删除肌少症综合评估报告
 * @param {Number|String} patientId 患者ID
 */
export function deleteSReport(patientId) {
  return request({
    url: `/sRecord/deleteReport/${patientId}`,
    method: 'delete'
  })
}
