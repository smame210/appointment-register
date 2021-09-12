package com.study.hosp.service;

import com.study.model.hosp.Hospital;
import com.study.vo.hosp.HospitalQueryVo;
import com.study.vo.order.SignInfoVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IHospitalService {
    void save(Map<String, Object> map);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> findPage(Integer current, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Hospital getHospById(String id);

    List<Hospital> findByHosname(String hosname);
}
