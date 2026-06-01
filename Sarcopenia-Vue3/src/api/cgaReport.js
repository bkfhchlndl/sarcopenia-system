import request from '@/utils/request'

/**
 * 根据评估记录ID获取完整报告
 * @param {Number} id 评估记录ID
 */
export function getFullCgaReport(id) {
  return request({
    url: `/cgaReportVo/getFullReport/${id}`,
    method: 'get'
  })
}

/**
 * 根据患者ID获取完整报告
 * @param {Number} patientId 患者ID
 */
export function getFullCgaReportByPatient(patientId) {
  return request({
    url: `/cgaReportVo/getFullReportByPatient/${patientId}`,
    method: 'get'
  })
}

/**
 * 根据患者ID删除评估报告
 * @param {Number} patientId 患者ID
 */
export function deleteReportByPatientId(patientId) {
  return request({
    url: `/cgaReportVo/deleteReportByPatientId/${patientId}`,
    method: 'delete'
  })
}
