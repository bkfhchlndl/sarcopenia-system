import request from '@/utils/request'

/**
 * 肌少症综合评估 - 报告相关API接口
 */

/**
 * 根据患者ID获取最新肌少症综合报告（组装各模块最新数据）
 * @param {Number|String} patientId 患者ID
 */
export function getFullSarcopeniaReportByPatient(patientId) {
  return request({
    url: `/sReport/getFullReportByPatient/${patientId}`,
    method: 'get'
  })
}

/**
 * 查询有肌少症评估记录的患者列表
 * @param {Object} params 查询参数
 */
export function selectSarcopeniaReportedPatientList(params) {
  return request({
    url: '/patient/selectSarcopeniaReportedList',
    method: 'get',
    params
  })
}

/**
 * 根据患者ID删除肌少症报告记录
 * @param {Number|String} patientId 患者ID
 */
export function deleteSarcopeniaReportByPatientId(patientId) {
  return request({
    url: `/sRecord/deleteByPatientId/${patientId}`,
    method: 'delete'
  })
}
