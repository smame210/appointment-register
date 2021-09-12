package com.study.hosp.service;

import com.study.model.hosp.Department;
import com.study.model.hosp.Schedule;
import com.study.vo.hosp.ScheduleOrderVo;
import com.study.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IScheduleService {
    void save(Map<String, Object> map);

    Page<Schedule> findPageDepartment(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleById(String scheduleId);

    //根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //修改排班
    void update(Schedule schedule);
}
