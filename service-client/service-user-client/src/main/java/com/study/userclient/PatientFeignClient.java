package com.study.userclient;

import com.study.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("service-user")
@Service
public interface PatientFeignClient {
    @RequestMapping(value = "/api/user/patient/inner/get/{id}", method = RequestMethod.GET)
    Patient getPatient(@PathVariable("id") Long id);
}
