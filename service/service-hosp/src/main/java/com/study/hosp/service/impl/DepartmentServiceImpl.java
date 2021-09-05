package com.study.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.study.hosp.repository.DepartmentRepository;
import com.study.hosp.service.IDepartmentService;
import com.study.model.hosp.Department;
import com.study.vo.hosp.DepartmentQueryVo;
import com.study.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements IDepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> map) {
        Department department = JSONObject.parseObject(JSONObject.toJSONString(map), Department.class);
        Department departmentByHoscode = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        department.setUpdateTime(new Date());
        department.setIsDeleted(0);
        // 若在数据库中有对应数据，则更新，否则就添加
        if (departmentByHoscode == null) {
            // 添加
            department.setCreateTime(new Date());
        } else {
            // 修改
            department.setCreateTime(departmentByHoscode.getCreateTime());
        }
        departmentRepository.save(department);
    }

    @Override
    public Page<Department> findPageDepartment(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department, matcher);
        Pageable pageable = PageRequest.of(page-1, limit);
        Page<Department> departments = departmentRepository.findAll(example, pageable);
        return departments;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null && department.getIsDeleted() != 1){
//            departmentRepository.deleteById(department.getId());
            department.setIsDeleted(1);
            departmentRepository.save(department);
        }
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        List<DepartmentVo> result = new ArrayList<>();

        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> departments = departmentRepository.findAll(example);
        //根据大科室分组
        Map<String, List<Department>> collect = departments.stream().collect(Collectors.groupingBy(Department::getBigcode));
        for (Map.Entry<String,List<Department>> entry : collect.entrySet()) {
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(entry.getKey());
            List<Department> value = entry.getValue();
            departmentVo.setDepname(value.get(0).getBigname());
            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            value.forEach(item -> {
                DepartmentVo child = new DepartmentVo();
                child.setDepcode(item.getDepcode());
                child.setDepname(item.getDepname());
                children.add(child);
            });
            departmentVo.setChildren(children);

            result.add(departmentVo);
        }
        return result;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
