import request from '@/utils/request'

/**
 * 患者信息管理 - 相关API接口
 */

/**
 * 新增患者基础信息
 * @param {Object} data 患者信息对象
 */
export function insertPatient(data) {
  return request({
    url: '/patient/insertPatient',
    method: 'post',
    data
  })
}

/**
 * 根据ID删除患者信息（逻辑删除）
 * @param {Object} params 请求参数（含患者ID）
 */
export function deletePatientById(params) {
  return request({
    url: '/patient/deletePatientById',
    method: 'put',
    params
  })
}

/**
 * 修改患者基础信息
 * @param {Object} data 患者信息对象
 */
export function updatePatient(data) {
  return request({
    url: '/patient/updatePatient',
    method: 'put',
    data
  })
}

/**
 * 保存老年综合评估基本情况信息
 * @param {Object} data 患者基本情况信息
 */
export function saveBasicInformation(data) {
  return request({
    url: '/patient/saveBasicInformation',
    method: 'put',
    data
  })
}

/**
 * 条件查询患者信息列表
 * @param {Object} params 查询筛选条件
 */
export function selectPatientList(params) {
  return request({
    url: '/patient/selectPatientList',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询患者详情信息
 * @param {Object} params 请求参数（含患者ID）
 */
export function selectPatientById(params) {
  return request({
    url: '/patient/selectPatientById',
    method: 'get',
    params
  })
}

/**
 * 查询已完成评估的患者列表
 * @param {Object} params 查询筛选条件
 */
export function selectReportedPatientList(params) {
  return request({
    url: '/patient/selectPatientReportedList',
    method: 'get',
    params
  })
}