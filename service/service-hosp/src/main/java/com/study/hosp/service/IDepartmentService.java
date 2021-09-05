package com.study.hosp.service;

import com.study.model.hosp.Department;
import com.study.vo.hosp.DepartmentQueryVo;
import com.study.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IDepartmentService {
    void save(Map<String, Object> map);

    Page<Department> findPageDepartment(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> findDeptTree(String hoscode);

    Department getDepartment(String hoscode, String depcode);
}
