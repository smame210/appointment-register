package com.study.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.cmnclient.DictFeignClient;
import com.study.enums.cmn.DictEnum;
import com.study.model.cmn.Dict;
import com.study.model.user.Patient;
import com.study.user.mapper.PatientMapper;
import com.study.user.service.IPatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author smame210
 * @since 2021-09-08
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAllUserId(Long userId) {
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Patient> patients = baseMapper.selectList(wrapper);
        patients.forEach(this::setPatient);
        return patients;
    }

    @Override
    public Patient getPatientById(Long id) {
        Patient patient = baseMapper.selectById(id);
        if(patient == null){
            return null;
        }
        return setPatient(patient);
    }

    /**
     * 重新封装patient
     */
    private Patient setPatient(Patient patient){
        //就诊人证件类型
        String certificatesType = patient.getCertificatesType();
        Dict dict = new Dict();
        if(!StringUtils.isEmpty(certificatesType)) {
            dict = dictFeignClient.findDict(DictEnum.CERTIFICATES_TYPE.getDictCode(), Long.valueOf(certificatesType));
            patient.getParam().put("certificatesTypeString", dict.getName());
        }
        //联系人证件类型
        String contactsCertificatesType = patient.getContactsCertificatesType();
        if(!StringUtils.isEmpty(contactsCertificatesType)) {
            dict = dictFeignClient.findDict(DictEnum.CERTIFICATES_TYPE.getDictCode(), Long.valueOf(contactsCertificatesType));
            patient.getParam().put("contactsCertificatesTypeString", dict.getName());
        }

        StringBuilder address = new StringBuilder();
        //省
        String provinceCode = patient.getProvinceCode();
        if(!StringUtils.isEmpty(provinceCode)) {
            dict = dictFeignClient.findDict(DictEnum.PROVINCE.getDictCode(), Long.valueOf(provinceCode));
            patient.getParam().put("provinceString", dict.getName());
            address.append(dict.getName());
        }
        //市
        String cityCode = patient.getCityCode();
        if(!StringUtils.isEmpty(cityCode)) {
           dict = dictFeignClient.findDict(Long.valueOf(cityCode));
           patient.getParam().put("cityString", dict.getName());
            address.append(dict.getName());
        }
        //区
        String districtCode = patient.getDistrictCode();
        if(!StringUtils.isEmpty(districtCode)){
            dict = dictFeignClient.findDict(Long.valueOf(districtCode));
            patient.getParam().put("districtString", dict.getName());
            address.append(dict.getName());
        }
        if (!StringUtils.isEmpty(patient.getAddress())){
            address.append(patient.getAddress());
        }
        patient.getParam().put("fullAddress", address.toString());
        return patient;
    }
}
