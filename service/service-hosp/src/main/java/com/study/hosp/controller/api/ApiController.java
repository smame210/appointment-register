package com.study.hosp.controller.api;

import com.study.common.exception.BizException;
import com.study.common.helper.HttpRequestHelper;
import com.study.common.result.Result;
import com.study.common.result.ResultCodeEnum;
import com.study.common.utils.MD5;
import com.study.hosp.service.IDepartmentService;
import com.study.hosp.service.IHospitalService;
import com.study.hosp.service.IHospitalSetService;
import com.study.hosp.service.IScheduleService;
import com.study.model.hosp.Department;
import com.study.model.hosp.Hospital;
import com.study.model.hosp.Schedule;
import com.study.vo.hosp.DepartmentQueryVo;
import com.study.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private IHospitalService iHospitalService;

    @Autowired
    private IDepartmentService iDepartmentService;

    @Autowired
    private IScheduleService iScheduleService;

    @Autowired
    private IHospitalSetService iHospitalSetService;

    @ApiOperation(value = "上传医院")
    @RequestMapping(value = "saveHospital", method = RequestMethod.POST)
    public Result saveHospital(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String)map.get("sign");
        //2 根据传递过来医院编码，查询数据库，查询签名
        String hoscode = (String)map.get("hoscode");
        String signKey = iHospitalSetService.getSignKey(hoscode);
        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new BizException(ResultCodeEnum.SIGN_ERROR);
        }

        String logo = (String)map.get("logoData");
        logo = logo.replaceAll(" ", "+");
        map.put("logoData", logo);

        iHospitalService.save(map);
        return Result.ok();
    }

    @ApiOperation(value = "获取医院信息")
    @RequestMapping(value = "hospital/show", method = RequestMethod.POST)
    public Result hospitalShow(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String)map.get("sign");
        //2 根据传递过来医院编码，查询数据库，查询签名
        String hoscode = (String)map.get("hoscode");
        String signKey = iHospitalSetService.getSignKey(hoscode);
        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new BizException(ResultCodeEnum.SIGN_ERROR);
        }

        Hospital hospital = iHospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation(value = "上传科室")
    @RequestMapping(value = "saveDepartment", method = RequestMethod.POST)
    public Result saveDepartment(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String)map.get("sign");
        //2 根据传递过来医院编码，查询数据库，查询签名
        String hoscode = (String)map.get("hoscode");
        String signKey = iHospitalSetService.getSignKey(hoscode);
        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new BizException(ResultCodeEnum.SIGN_ERROR);
        }

        iDepartmentService.save(map);
        return Result.ok();
    }

    @ApiOperation(value = "查询科室")
    @RequestMapping(value = "department/list", method = RequestMethod.POST)
    public Result departmentShow(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String)map.get("hoscode");
        Integer page = StringUtils.isEmpty(map.get("page")) ? 1: Integer.parseInt((String)map.get("page"));
        Integer limit = StringUtils.isEmpty(map.get("limit")) ? 1: Integer.parseInt((String)map.get("limit"));
        //TODO 签名校验

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        //调用service方法
        Page<Department> pageModel = iDepartmentService.findPageDepartment(page,limit,departmentQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除科室")
    @RequestMapping(value = "department/remove", method = RequestMethod.POST)
    public Result departmentRemove(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        //TODO 签名校验

        //调用service方法
        iDepartmentService.remove(hoscode, depcode);
        return Result.ok();
    }

    @ApiOperation(value = "上传排班")
    @RequestMapping(value = "saveSchedule", method = RequestMethod.POST)
    public Result saveSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String)map.get("sign");
        //2 根据传递过来医院编码，查询数据库，查询签名
        String hoscode = (String)map.get("hoscode");
        String signKey = iHospitalSetService.getSignKey(hoscode);
        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        //4 判断签名是否一致
        if(!hospSign.equals(signKeyMd5)) {
            throw new BizException(ResultCodeEnum.SIGN_ERROR);
        }

        iScheduleService.save(map);
        return Result.ok();
    }

    @ApiOperation(value = "查询排班")
    @RequestMapping(value = "schedule/list", method = RequestMethod.POST)
    public Result scheduleShow(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String)map.get("hoscode");
        Integer page = StringUtils.isEmpty(map.get("page")) ? 1: Integer.parseInt((String)map.get("page"));
        Integer limit = StringUtils.isEmpty(map.get("limit")) ? 1: Integer.parseInt((String)map.get("limit"));
        //TODO 签名校验

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        //调用service方法
        Page<Schedule> pageModel = iScheduleService.findPageDepartment(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除排班")
    @RequestMapping(value = "schedule/remove", method = RequestMethod.POST)
    public Result scheduleRemove(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String)map.get("hoscode");
        String hosScheduleId = (String)map.get("hosScheduleId");
        //TODO 签名校验

        //调用service方法
        iScheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }
}
