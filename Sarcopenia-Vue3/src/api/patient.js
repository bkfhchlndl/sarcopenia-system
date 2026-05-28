import request from '@/utils/request'

/**
 * 新增患者信息
 * @param {Object} data 患者信息
 */
export function insertPatient(data) {
  return request({
    url: '/patient/insertPatient',
    method: 'post',
    data
  })
}

/**
 * 根据ID删除患者信息
 * @param {Object} params 请求参数
 */
export function deletePatientById(params) {
  return request({
    url: '/patient/deletePatientById',
    method: 'put',
    params
  })
}

/**
 * 修改患者信息
 * @param {Object} data 患者信息
 */
export function updatePatient(data) {
  return request({
    url: '/patient/updatePatient',
    method: 'put',
    data
  })
}

/**
 * 查询患者信息列表
 * @param {Object} params 查询条件
 */
export function selectPatientList(params) {
  return request({
    url: '/patient/selectPatientList',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询患者信息
 * @param {Object} params 请求参数
 */
export function selectPatientById(params) {
  return request({
    url: '/patient/selectPatientById',
    method: 'get',
    params
  })
}