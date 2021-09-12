package com.study.hosp.controller.api;

import com.study.common.result.Result;
import com.study.hosp.service.IDepartmentService;
import com.study.hosp.service.IHospitalService;
import com.study.hosp.service.IHospitalSetService;
import com.study.hosp.service.IScheduleService;
import com.study.model.hosp.Hospital;
import com.study.model.hosp.Schedule;
import com.study.vo.hosp.DepartmentVo;
import com.study.vo.hosp.HospitalQueryVo;
import com.study.vo.hosp.ScheduleOrderVo;
import com.study.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import java.util.List;
import java.util.Map;

@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    private IHospitalService iHospitalService;

    @Autowired
    private IDepartmentService iDepartmentService;

    @Autowired
    private IScheduleService iScheduleService;

    @Autowired
    private IHospitalSetService iHospitalSetService;

    @ApiOperation(value = "获取分页列表")
    @RequestMapping(value = "findPage/{page}/{limit}", method = RequestMethod.POST)
    public Result findPage(@PathVariable Integer page,
                           @PathVariable Integer limit,
                           @RequestBody(required = false) HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = iHospitalService.findPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }


    @ApiOperation(value = "根据医院名称获取医院列表")
    @RequestMapping(value = "findByHosname/{hosname}", method = RequestMethod.GET)
    public Result findByHosname(@PathVariable String hosname){
        List<Hospital> hospitalList = iHospitalService.findByHosname(hosname);
        return Result.ok(hospitalList);
    }

    @ApiOperation(value = "获取科室列表")
    @RequestMapping(value = "department/{hoscode}", method = RequestMethod.GET)
    public Result getDepList(@PathVariable String hoscode){
        List<DepartmentVo> deptTree = iDepartmentService.findDeptTree(hoscode);
        return Result.ok(deptTree);
    }

    @ApiOperation(value = "医院预约挂号详情")
    @RequestMapping(value = "booking/{hoscode}", method = RequestMethod.GET)
    public Result getBooking(@PathVariable String hoscode){
        Hospital hospital = iHospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(@PathVariable Integer page,
                                     @PathVariable Integer limit,
                                     @PathVariable String hoscode,
                                     @PathVariable String depcode) {
        Map<String, Object> map = iScheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,
                                   @PathVariable String depcode,
                                   @PathVariable String workDate) {
        List<Schedule> detailSchedule = iScheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return Result.ok(detailSchedule);
    }

    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId){
        Schedule schedule = iScheduleService.getScheduleById(scheduleId);
        return Result.ok(schedule);
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return iScheduleService.getScheduleOrderVo(scheduleId);
    }

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode) {
        return iHospitalSetService.getSignInfoVo(hoscode);
    }

}
