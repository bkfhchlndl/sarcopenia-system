import request from '@/utils/request'

export function insertPatient(data) {
  return request({
    url: '/patient/insertPatient',
    method: 'post',
    data
  })
}

export function deletePatientById(params) {
  return request({
    url: '/patient/deletePatientById',
    method: 'put',
    params
  })
}

export function updatePatient(data) {
  return request({
    url: '/patient/updatePatient',
    method: 'put',
    data
  })
}

export function selectPatientList(params) {
  return request({
    url: '/patient/selectPatientList',
    method: 'get',
    params
  })
}

export function selectPatientById(params) {
  return request({
    url: '/patient/selectPatientById',
    method: 'get',
    params
  })
}
