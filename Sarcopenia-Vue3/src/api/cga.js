import request from '@/utils/request'

/**
 * 查询全部量表信息（1 - 20包含题目及选项）
 */
export function selectCgaScaleList() {
  return request({
    url: '/cgaScale/selectScaleList',
    method: 'get'
  })
}
/**
 * 查询全部量表信息（21包含题目及选项）
 */
export function selectDailyLifeScale() {
  return request({
    url: '/cgaScale/selectDailyLifeScale',
    method: 'get'
  })
}
/**
 * 查询全部量表信息（22包含题目及选项）
 */
export function selectSppbScale() {
  return request({
    url: '/cgaScale/selectSppbScale',
    method: 'get'
  })
}
/**
 * 查询全部量表信息（24包含题目及选项）
 */
export function selectVisionHearingScale() {
  return request({
    url: '/cgaScale/selectVisionHearingScale',
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
 * 根据条件查询评估记录列表
 * @param {Object} params 查询参数
 */
export function selectCgaRecordList(params) {
  return request({
    url: '/cgaRecord/selectCgaRecordList',
    method: 'get',
    params
  })
}
