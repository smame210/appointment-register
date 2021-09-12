package com.study.common.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

public class TimeUtil {

    /**
     *
     * 将 date(年月日)+timeString(时分秒) 日期格式转换为 DateTime(yyyy-MM-dd HH:mm)
     */
    public static DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }
}
