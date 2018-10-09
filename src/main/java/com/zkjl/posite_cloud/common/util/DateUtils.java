package com.zkjl.posite_cloud.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    public static String getFormatString(Date date){
        return simpleDateFormat.format(date);
    }
    public static String getFormatStringByDay(Date date){
        return simpleDateFormat2.format(date);
    }
}
