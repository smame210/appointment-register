import request from '@/utils/request'

const api_name = `/api/order/info`
export default {
  submitOrder(scheduleId, patientId) {
    return request({
      url: `${api_name}/auth/submitOrder/${scheduleId}/${patientId}`,
      method: 'post'
    })
  },
  getOrderInfo(id) {
    return request({
      url: `${api_name}/auth/getOrder/${id}`,
      method: 'get'
    })
  },
  //订单列表
  getPageList(page, limit, searchObj) {
    return request({
      url: `${api_name}/auth/${page}/${limit}`,
      method: `post`,
      data: searchObj
    })
  },
  //订单状态
  getStatusList() {
    return request({
      url: `${api_name}/auth/getStatusList`,
      method: 'get'
    })
  },
  cancelOrder(orderId) {
    return request({
      url: `${api_name}/auth/cancelOrder/${orderId}`,
      method: 'get'
    })
  },
}
