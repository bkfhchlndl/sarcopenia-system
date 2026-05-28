import request from '@/utils/request'

/**
 * 查询全部量表信息（包含题目及选项）
 */
export function selectCagScaleList() {
  return request({
    url: '/cagScale/selectScaleList',
    method: 'get'
  })
}

/**
 * 提交量表评估记录
 * @param {Object} data 评估提交参数
 */
export function insertCagRecord(data) {
  return request({
    url: '/cagRecord/insertCagRecord',
    method: 'post',
    data
  })
}

/**
 * 根据条件查询评估记录列表
 * @param {Object} params 查询参数
 */
export function selectCagRecordList(params) {
  return request({
    url: '/cagRecord/selectCagRecordList',
    method: 'get',
    params
  })
}