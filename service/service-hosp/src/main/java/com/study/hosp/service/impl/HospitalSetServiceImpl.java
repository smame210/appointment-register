package com.study.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.exception.BizException;
import com.study.common.result.ResultCodeEnum;
import com.study.hosp.mapper.HospitalSetMapper;
import com.study.hosp.service.IHospitalSetService;
import com.study.model.hosp.Hospital;
import com.study.model.hosp.HospitalSet;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院信息设置表 服务实现类
 * </p>
 *
 * @author smame210
 * @since 2021-08-27
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements IHospitalSetService {

    @Override
    public String getSignKey(String hoscode) {
        HospitalSet hospitalSet = this.getByHoscode(hoscode);
        if(null == hospitalSet) {
            throw new BizException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        if(hospitalSet.getStatus() == 0) {
            throw new BizException(ResultCodeEnum.HOSPITAL_LOCK);
        }

        return hospitalSet.getSignKey();
    }

    private HospitalSet getByHoscode(String hoscode){
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet;
    }
}
