package com.study.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.user.Patient;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author smame210
 * @since 2021-09-08
 */
public interface IPatientService extends IService<Patient> {

    List<Patient> findAllUserId(Long userId);

    Patient getPatientById(Long id);
}
