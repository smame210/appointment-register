import request from '@/utils/request'

export default {
  getHospList(page, limit, searchObj) {
    return request({
      url: `/admin/hosp/hospital/findPageHosp/${page}/${limit}`,
      method: 'post',
      data: searchObj
    })
  },
  getProvinces(dictCode) {
    return request({
      url: `/admin/cmn/dict/findChildByDC/${dictCode}`,
      method: 'get'
    })
  },
  getCity(parentId) {
    return request({
      url: `/admin/cmn/dict/findChildData/${parentId}`,
      method: 'get'
    })
  },
  updateStatus(id, status) {
    return request({
      url: `/admin/hosp/hospital/updateStatus/${id}/${status}`,
      method: 'put'
    })
  },
  //查看医院详情
  getHospById(id) {
    return request ({
      url: `/admin/hosp/hospital/show/${id}`,
      method: 'get'
    })
  },
  getDeptByHoscode(hoscode) {
    return request ({
      url: `/admin/hosp/department/getDeptList/${hoscode}`,
      method: 'get'
    })
  },
  getScheduleRule(page, limit, hoscode, depcode) {
    return request({
      url: `/admin/hosp/schedule/getScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
      method: 'get'
    })
  },
  //查询排班详情
  getScheduleDetail(hoscode,depcode,workDate) {
    return request ({
      url: `/admin/hosp/schedule/getScheduleDetail/${hoscode}/${depcode}/${workDate}`,
      method: 'get'
    })
  }

}
