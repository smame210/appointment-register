package com.study.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.study.cmnclient.DictFeignClient;
import com.study.enums.cmn.DictEnum;
import com.study.hosp.repository.HospitalRepository;
import com.study.hosp.service.IHospitalService;
import com.study.model.cmn.Dict;
import com.study.model.hosp.Hospital;
import com.study.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class HospitalServiceImpl implements IHospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> map) {
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(map), Hospital.class);
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());

        hospital.setUpdateTime(new Date());
        hospital.setIsDeleted(0);
        // 若在数据库中有对应数据，则更新，否则就添加
        if (hospitalByHoscode == null) {
            // 添加
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
        } else {
            // 修改
            hospital.setStatus(hospitalByHoscode.getStatus());
            hospital.setCreateTime(hospitalByHoscode.getCreateTime());
        }
        hospitalRepository.save(hospital);
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital!=null && hospital.getIsDeleted()!=1){
            return hospital;
        }
        return null;
    }

    @Override
    public Page<Hospital> findPage(Integer current, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        hospital.setIsDeleted(0);

        ExampleMatcher matcher = ExampleMatcher.matching()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                        .withIgnoreCase(true);
        Example<Hospital> example = Example.of(hospital, matcher);
        Pageable pageable = PageRequest.of(current-1, limit);
        Page<Hospital> page = hospitalRepository.findAll(example, pageable);
        return setPage(page);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        Optional<Hospital> hospitalOptional = hospitalRepository.findById(id);
        if (hospitalOptional.isPresent()){
            Hospital hospital = hospitalOptional.get();
            hospital.setStatus(status);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getHospById(String id) {
        Hospital hospital = new Hospital();
        Optional<Hospital> hospitalOptional = hospitalRepository.findById(id);
        if (hospitalOptional.isPresent()){
            hospital = hospitalOptional.get();
            fillParams(hospital);
        }
        return hospital;
    }

    /**
     *
     * 对findPage的结果重新封装，即添加医院类型，省市区代码在数据字典中的中文名称
     * 结果保存在Map<String,Object> param中
     */
    private Page<Hospital> setPage(Page<Hospital> page){
        page.getContent().forEach(this::fillParams);
        return page;
    }

    private void fillParams(Hospital item) {
        Map<String, Object> param = item.getParam();
        Dict dict = dictFeignClient.findDict(DictEnum.HOSTYPE.getDictCode(), Long.valueOf(item.getHostype()));
        param.put("hostypeName", dict.getName());

        StringBuilder fullAddress = new StringBuilder();
        dict = dictFeignClient.findDict(DictEnum.PROVINCE.getDictCode(), Long.valueOf(item.getProvinceCode()));
        fullAddress.append(dict.getName());
        dict = dictFeignClient.findDict(Long.valueOf(item.getCityCode()));
        fullAddress.append(dict.getName());
        dict = dictFeignClient.findDict(Long.valueOf(item.getDistrictCode()));
        fullAddress.append(dict.getName());
        fullAddress.append(item.getAddress());
        param.put("fullAddress", fullAddress.toString());

        item.setParam(param);
    }
}
