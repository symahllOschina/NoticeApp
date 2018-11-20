package com.wanding.notice.statis.util;

import android.util.Log;

import com.wanding.notice.utils.DateTimeUtil;
import com.wanding.notice.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 统计时日期时间转换帮助类 */
public class StatisDateTime {



    /**
     * 将传过来的日期转换为时间戳（这里传过来的值为yyyy-MM-dd格式，需改为yyyyMMdd，并手动补充时分秒）
     */
    public static String getStartTimeStamp(){
        String timeStr = "000000";
        //获取前30天的日期（不包含当天的30天）
        String sysDateStr = DateTimeUtil.getDateStr(-30,"yyyyMMdd");
        Log.e("获取的日期：",sysDateStr);
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = null;
            date = simpleDateFormat.parse(sysDateStr+timeStr);
            long ts = date.getTime();
            String stampStr = String.valueOf(ts);
            Log.e("生成的时间戳",stampStr);
            return stampStr;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将传过来的日期转换为时间戳（这里传过来的值为yyyy-MM-dd格式，需改为yyyyMMdd，并手动补充时分秒）
     */
    public static String getEndTimeStamp(){
        String timeStr = "235959";
        //获取前一天的日期
        String sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
        Log.e("获取的日期：",sysDateStr);
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = null;
            date = simpleDateFormat.parse(sysDateStr+timeStr);
            long ts = date.getTime();
            String stampStr = String.valueOf(ts);
            Log.e("生成的时间戳",stampStr);
            return stampStr;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }
}
