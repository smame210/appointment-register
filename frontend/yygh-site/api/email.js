import request from '@/utils/request'

const api_name = 'api/email'

export default {
  sendCode(email) {
    return request({
      url: `${api_name}/send/${email}`,
      method: 'get'
    })
  }
}
