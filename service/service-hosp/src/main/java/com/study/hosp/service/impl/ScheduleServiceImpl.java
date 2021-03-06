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
                //????????????
                Aggregation.match(criteria),
                //???????????????
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        //??????????????????????????????????????????????????????
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //???????????????
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //??????
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos = mongoTemplate.aggregate(aggregation1, Schedule.class, BookingScheduleRuleVo.class);

        //????????????
        Aggregation aggregation2 = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(aggregation2, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        //???????????????????????????
        bookingScheduleRuleVos.forEach(bookingScheduleRuleVo -> {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        });

        //?????????????????????????????????
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVos.getMappedResults());
        result.put("total",total);

        //??????????????????
        String hosName = iHospitalService.getByHoscode(hoscode).getHosname();
        //??????????????????
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);

        return result;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //??????????????????mongodb
        List<Schedule> scheduleList =
                scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        scheduleList.forEach(this::setSchedule);
        return scheduleList;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //??????hoscode??????hospital???????????????????????????
        Hospital hospital = iHospitalService.getByHoscode(hoscode);
        if (hospital==null){
            throw new BizException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //????????????????????????????????????????????????????????????????????????
        IPage<Date> datePage = this.getListDate(page, limit, bookingRule);
        List<Date> dateList = datePage.getRecords();
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation aggregation1 = Aggregation.newAggregation(
                //????????????
                Aggregation.match(criteria),
                //???????????????
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        //??????????????????????????????????????????????????????
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //???????????????
                Aggregation.sort(Sort.Direction.DESC, "workDate")
        );
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos = mongoTemplate.aggregate(aggregation1, Schedule.class, BookingScheduleRuleVo.class);

        //???????????????????????????
        List<BookingScheduleRuleVo> scheduleRuleVoList = bookingScheduleRuleVos.getMappedResults();
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleRuleVoList)) {
            scheduleVoMap = scheduleRuleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        //???????????????????????????
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //?????????????????????????????????????????????
        for(int i=0, len=dateList.size(); i<dateList.size(); i++){
            Date date = dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (bookingScheduleRuleVo == null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //??????????????????
                bookingScheduleRuleVo.setDocCount(0);
                //?????????????????????  -1????????????
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //?????????????????????????????????
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            //?????????????????????????????????????????????   ?????? 0????????? 1??????????????? -1????????????????????????
            if(i == len-1 && page == datePage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //??????????????????????????????????????? ????????????
            if(i == 0 && page == 1) {
                DateTime stopTime = TimeUtil.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //????????????
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //???????????????????????????
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", datePage.getTotal());
        //??????????????????
        Map<String, String> baseMap = new HashMap<>();
        //????????????
        baseMap.put("hosname", iHospitalService.getByHoscode(hoscode).getHosname());
        //??????
        Department department = iDepartmentService.getDepartment(hoscode, depcode);
        //???????????????
        baseMap.put("bigname", department.getBigname());
        //????????????
        baseMap.put("depname", department.getDepname());
        //???
        baseMap.put("workDateString", new DateTime().toString("yyyy???MM???"));
        //????????????
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //????????????
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

        //????????????????????????
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

        //?????????????????????????????????????????????-1????????????0???
        Integer quitDay = bookingRule.getQuitDay();
        DateTime quitDateTime = TimeUtil.getDateTime(new DateTime(schedule.getWorkDate()).plus(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitDateTime.toDate());

        //??????????????????
        DateTime startDateTime = TimeUtil.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startDateTime.toDate());

        //??????????????????
        DateTime endDateTime = TimeUtil.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endDateTime.toDate());

        //????????????????????????
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
     * ?????????????????????????????????????????????????????????????????????
     */
    private IPage<Date> getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //?????????????????? yyyy-MM-dd HH:mm
        DateTime releaseTime = TimeUtil.getDateTime(new Date(), bookingRule.getReleaseTime());
        //????????????
        Integer cycle = bookingRule.getCycle();
        //???????????????????????????????????????????????????1
        if(releaseTime.isBeforeNow()){
            cycle += 1;
        }
        //???????????????????????????????????????????????????????????????????????????
        List<Date> dateList = new ArrayList<>();
        for (int i=0; i<cycle; i++){
            //????????????????????????
            String today = new DateTime().plusDays(i).toString("yyyy-MM-dd");
            dateList.add(new DateTime(today).toDate());
        }

        //?????????????????????????????????????????????????????????????????????7????????????????????????????????????
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
     * ??????????????????????????? ????????????????????????????????????????????????
     * @param: [item]
     * @return: void
     */
    private void setSchedule(Schedule schedule) {
        //??????????????????
        schedule.getParam().put("hosname",iHospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        //??????????????????
        schedule.getParam().put("depname",iDepartmentService.getDepartment(schedule.getHoscode(),schedule.getDepcode()).getDepname());
        //????????????????????????
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    /**
     * ??????????????????????????????
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "??????";
            default:
                break;
        }
        return dayOfWeek;
    }

}
