package com.study.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.hosp.HospitalSet;
import com.study.vo.order.SignInfoVo;

/**
 * <p>
 * 医院信息设置表 服务类
 * </p>
 *
 * @author smame210
 * @since 2021-08-27
 */
public interface IHospitalSetService extends IService<HospitalSet> {

    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
