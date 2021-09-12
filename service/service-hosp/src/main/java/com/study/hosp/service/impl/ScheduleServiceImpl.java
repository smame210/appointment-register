package com.study.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.study.common.exception.BizException;
import com.study.common.result.ResultCodeEnum;
import com.study.common.utils.TimeUtil;
import com.study.hosp.repository.ScheduleRepository;
import com.study.hosp.service.IDepartmentService;
import com.study.hosp.service.IHospitalService;
import com.study.hosp.service.IScheduleService;
import com.study.model.hosp.BookingRule;
import com.study.model.hosp.Department;
import com.study.model.hosp.Hospital;
import com.study.model.hosp.Schedule;
import com.study.vo.hosp.BookingScheduleRuleVo;
import com.study.vo.hosp.ScheduleOrderVo;
import com.study.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements IScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IHospitalService iHospitalService;

    @Autowired
    private IDepartmentService iDepartmentService;

    @Override
    public void save(Map<String, Object> map) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(map), Schedule.class);
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());

        schedule.setUpdateTime(new Date());
        if(scheduleExist == null){
            schedule.setCreateTime(new Date());
            schedule.setIsDeleted(0);
        }else{
            schedule.setCreateTime(scheduleExist.getCreateTime());
            schedule.setIsDeleted(schedule.getIsDeleted());
        }
        scheduleRepository.save(schedule);
    }

    @Override
    public Page<Schedule> findPageDepartment(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo) {
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Schedule> example = Example.of(schedule, matcher);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Schedule> schedules = scheduleRepository.findAll(example, pageable);
        return schedules;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(schedule != null && schedule.getIsDeleted() != 1){
//            schedule.setIsDeleted(1);
//            scheduleRepository.save(schedule);
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation aggregation1 = Aggregation.newAggregation(
                //条件查询
                Aggregation.match(criteria),
                //按日期分组
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        //统计每组记录数，已预约数，剩余预约数
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //按日期排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //分页
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos = mongoTemplate.aggregate(aggregation1, Schedule.class, BookingScheduleRuleVo.class);

        //总记录数
        Aggregation aggregation2 = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        //把日期对应星期获取
        bookingScheduleRuleVos.forEach(bookingScheduleRuleVo -> {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        });

        //设置最终数据，进行返回
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVos.getMappedResults());
        result.put("total",total);

        //获取医院名称
        String hosName = iHospitalService.getByHoscode(hoscode).getHosname();
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);

        return result;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //根据参数查询mongodb
        List<Schedule> scheduleList =
                scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        scheduleList.forEach(this::setSchedule);
        return scheduleList;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //根据hoscode查找hospital，进而获取预约规则
        Hospital hospital = iHospitalService.getByHoscode(hoscode);
        if (hospital==null){
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //根据预约规则获取排班表及时间列表，并封装到分页中
        IPage<Date> datePage = this.getListDate(page, limit, bookingRule);
        List<Date> dateList = datePage.getRecords();
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation aggregation1 = Aggregation.newAggregation(
                //条件查询
                Aggregation.match(criteria),
                //按日期分组
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        //统计每组记录数，已预约数，剩余预约数
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //按日期排序
                Aggregation.sort(Sort.Direction.DESC, "workDate")
        );
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos = mongoTemplate.aggregate(aggregation1, Schedule.class, BookingScheduleRuleVo.class);

        //获取科室剩余预约数
        List<BookingScheduleRuleVo> scheduleRuleVoList = bookingScheduleRuleVos.getMappedResults();
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleRuleVoList)) {
            scheduleVoMap = scheduleRuleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //获取每一天的排班医生及可预约数
        for(int i=0, len=dateList.size(); i<dateList.size(); i++){
            Date date = dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (bookingScheduleRuleVo == null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == len-1 && page == datePage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = TimeUtil.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //可预约日期规则数据
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", datePage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", iHospitalService.getByHoscode(hoscode).getHosname());
        //科室
        Department department = iDepartmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    @Override
    public Schedule getScheduleById(String scheduleId) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(scheduleId);
        if(scheduleOptional.isPresent()){
            Schedule schedule = scheduleOptional.get();
            this.setSchedule(schedule);
            return schedule;
        }else{
            return new Schedule();
        }
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(scheduleId);
        if (!scheduleOptional.isPresent()){
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }

        //获取预约规则信息
        Schedule schedule = scheduleOptional.get();
        Hospital hospital = iHospitalService.getByHoscode(schedule.getHoscode());
        if(hospital == null){
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule == null){
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(iDepartmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        Integer quitDay = bookingRule.getQuitDay();
        DateTime quitDateTime = TimeUtil.getDateTime(new DateTime(schedule.getWorkDate()).plus(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitDateTime.toDate());

        //预约开始时间
        DateTime startDateTime = TimeUtil.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startDateTime.toDate());

        //预约截止时间
        DateTime endDateTime = TimeUtil.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endDateTime.toDate());

        //当天停止挂号时间
        DateTime stopDateTime = TimeUtil.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopDateTime.toDate());

        return scheduleOrderVo;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    /**
     *
     * 根据预约规则建立预约时间集合，并用分页封装起来
     */
    private IPage<Date> getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //当天放号时间 yyyy-MM-dd HH:mm
        DateTime releaseTime = TimeUtil.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        Integer cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期加1
        if(releaseTime.isBeforeNow()){
            cycle += 1;
        }
        //展示全部可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i=0; i<cycle; i++){
            //封装当前预约日期
            String today = new DateTime().plusDays(i).toString("yyyy-MM-dd");
            dateList.add(new DateTime(today).toDate());
        }

        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        int start = (page-1)*limit;
        int total = dateList.size();
        IPage<Date> datePage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, total);
        try {
            datePage.setRecords(dateList.subList(start, start+limit));
        } catch (IndexOutOfBoundsException e) {
            datePage.setRecords(dateList.subList(start, total));
        }

        return datePage;
    }

    /**
     *
     * 封装排班详情其他值 医院名称、科室名称、日期对应星期
     * @param: [item]
     * @return: void
     */
    private void setSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",iHospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname",iDepartmentService.getDepartment(schedule.getHoscode(),schedule.getDepcode()).getDepname());
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
