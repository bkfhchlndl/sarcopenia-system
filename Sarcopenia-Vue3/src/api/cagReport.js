import request from '@/utils/request'

/**
 * 根据评估记录ID获取完整报告
 * @param {Number} id 评估记录ID
 */
export function getFullCagReport(id) {
  return request({
    url: `/cagReportVo/getFullReport/${id}`,
    method: 'get'
  })
}

/**
 * 查询已完成评估的患者列表
 * @param {Object} params 查询条件
 */
export function selectReportedPatientList(params) {
  return request({
    url: '/cagReportVo/selectPatientReportedList',
    method: 'get',
    params
  })
}

/**
 * 根据患者ID获取完整报告
 * @param {Number} patientId 患者ID
 */
export function getFullCagReportByPatient(patientId) {
  return request({
    url: `/cagReportVo/getFullReportByPatient/${patientId}`,
    method: 'get'
  })
}

/**
 * 根据患者ID删除评估报告
 * @param {Number} patientId 患者ID
 */
export function deleteReportByPatientId(patientId) {
  return request({
    url: `/cagReportVo/deleteReportByPatientId/${patientId}`,
    method: 'delete'
  })
}