import request from '@/utils/request'

export default {
  getHospSetList(current, limit, searchObj){
    return request({
      url: `/admin/hosp/hospitalSet/findPageHospSet/${current}/${limit}`,
      method: 'post',
      data: searchObj
    })
  },
  deleteHospSet(id){
    return request({
      url: `/admin/hosp/hospitalSet/${id}`,
      method: 'delete'
    })
  },
  batchRemoveHospSet(idList) {
    return request({
      url: `/admin/hosp/hospitalSet/batchRemove`,
      method: 'delete',
      data: idList
    })
  },
  lockHospSet(id, status) {
    return request({
      url: `/admin/hosp/hospitalSet/lockHospSet/${id}/${status}`,
      method: 'put'
    })
  },
  saveHospSet(form) {
    return request({
      url: `/admin/hosp/hospitalSet/saveHospSet`,
      method: 'post',
      data: form
    })
  },
  getHospSet(id) {
    return request({
      url: `/admin/hosp/hospitalSet/getHospSet/${id}`,
      method: 'get'
    })
  },
  updateHospSet(form) {
    return request({
      url: `/admin/hosp/hospitalSet/updateHospSet`,
      method: 'put',
      data: form
    })
  },
}
