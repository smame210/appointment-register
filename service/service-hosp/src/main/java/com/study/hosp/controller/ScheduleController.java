package com.study.hosp.controller;

import com.study.common.result.Result;
import com.study.hosp.service.IScheduleService;
import com.study.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "排班管理")
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Autowired
    private IScheduleService iScheduleService;

    @ApiOperation(value = "查询排班规则数据")
    @RequestMapping(value = "getScheduleRule/{page}/{limit}/{hoscode}/{depcode}", method = RequestMethod.GET)
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode) {
        Map<String,Object> map = iScheduleService.getRuleSchedule(page,limit,hoscode,depcode);
        return Result.ok(map);
    }

    @ApiOperation(value = "查询排班详细信息")
    @RequestMapping(value = "getScheduleDetail/{hoscode}/{depcode}/{workDate}", method = RequestMethod.GET)
    public Result getScheduleDetail( @PathVariable String hoscode,
                                     @PathVariable String depcode,
                                     @PathVariable String workDate) {
        List<Schedule> list = iScheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return Result.ok(list);
    }

}
