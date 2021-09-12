import request from '@/utils/request'

const api_name = `/api/ucenter/wx`
export default {
  getLoginParam() {
    return request({
      url: `${api_name}/getLoginParam`,
      method: `get`
    })
  },
  createPayQrc(orderId) {
    return request({
      url: `/api/order/pay/auth/weixin/createPayQrc/${orderId}`,
      method: 'get'
    })
  },
  queryPayStatus(orderId){
    return request({
      url: `/api/order/pay/auth/queryPayStatus/${orderId}`,
      method: 'get'
    })
  },
}
