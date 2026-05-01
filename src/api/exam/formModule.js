import request from '@/utils/request'

// 查询表单及模块信息
export function getFormModule(formId) {
  return request({
    url: '/exam/form-module/' + formId,
    method: 'get'
  })
}

