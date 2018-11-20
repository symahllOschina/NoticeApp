package com.wanding.notice.query.util;

import android.util.Log;

import com.wanding.notice.utils.DateTimeUtil;
import com.wanding.notice.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 查询时日期时间转换帮助类 */
public class QueryDateTime {




    /**
     * 将传过来的时间转换为时间戳（这里传过来的值为HH:mm格式，需手动补充秒）
     * APP每次打开hhmmStr默认值为"",需手动补充时分秒
     */
    public static String getStartTimeStamp(String hhmmStr){
        String ssStr = "";
        //获取系统日期
        String sysDateStr = DateTimeUtil.getFormatSystemTime("yyyyMMdd");
        Log.e("获取的日期：",sysDateStr);
        if(Utils.isEmpty(hhmmStr)){
            ssStr = "000000";
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                date = simpleDateFormat.parse(sysDateStr+hhmmStr+ssStr);
                long ts = date.getTime();
                String stampStr = String.valueOf(ts);
                Log.e("生成的时间戳",stampStr);
                return stampStr;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            //判断当前时间是日期还是时间
            if(hhmmStr.contains(":")){
                ssStr = "00";
                String hhStr = hhmmStr.split(":")[0];
                String mmStr = hhmmStr.split(":")[1];
                hhmmStr = hhStr + mmStr;

                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = null;
                    date = simpleDateFormat.parse(sysDateStr+hhmmStr+ssStr);
                    long ts = date.getTime();
                    String stampStr = String.valueOf(ts);
                    Log.e("生成的时间戳",stampStr);
                    return stampStr;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                ssStr = "000000";
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = null;
                    date = simpleDateFormat.parse(sysDateStr+ssStr);
                    long ts = date.getTime();
                    String stampStr = String.valueOf(ts);
                    Log.e("生成的时间戳",stampStr);
                    return stampStr;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }



        }

        return "";
    }


    /**
     * 将传过来的时间转换为时间戳（这里传过来的值为HH:mm格式，需手动补充秒）
     * APP每次打开hhmmStr默认值为"",需手动补充时分秒
     */
    public static String getEndTimeStamp(String hhmmStr){
        String ssStr = "";
        //获取系统日期
        String sysDateStr = DateTimeUtil.getFormatSystemTime("yyyyMMdd");
        Log.e("获取的日期：",sysDateStr);
        if(Utils.isEmpty(hhmmStr)){
            ssStr = "235959";
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                date = simpleDateFormat.parse(sysDateStr+hhmmStr+ssStr);
                long ts = date.getTime();
                String stampStr = String.valueOf(ts);
                Log.e("生成的时间戳",stampStr);
                return stampStr;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            //判断当前时间是日期还是时间
            if(hhmmStr.contains(":")){
                ssStr = "59";
                String hhStr = hhmmStr.split(":")[0];
                String mmStr = hhmmStr.split(":")[1];
                hhmmStr = hhStr + mmStr;
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = null;
                    date = simpleDateFormat.parse(sysDateStr+hhmmStr+ssStr);
                    long ts = date.getTime();
                    String stampStr = String.valueOf(ts);
                    Log.e("生成的时间戳",stampStr);
                    return stampStr;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                ssStr = "235959";
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = null;
                    date = simpleDateFormat.parse(sysDateStr+ssStr);
                    long ts = date.getTime();
                    String stampStr = String.valueOf(ts);
                    Log.e("生成的时间戳",stampStr);
                    return stampStr;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    /**
     * 将传过来的日期转换为时间戳（这里传过来的值为yyyy-MM-dd格式，需改为yyyyMMdd，并手动补充时分秒）
     */
    public static String getStartTimeStampTo(String dateStr){
        String timeStr = "000000";
        String sysDateStr = "";
        if(Utils.isEmpty(dateStr)){
            //获取系统日期
//            sysDateStr = DateTimeUtil.getFormatSystemTime("yyyyMMdd");
            //获取前一天的日期
            sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
            Log.e("获取的日期：",sysDateStr);
        }else{
            if(dateStr.contains("-")){
                String syaDate = DateTimeUtil.getFormatSystemTime("yyyyMMdd");
                if(dateStr.equals(syaDate)){
                    //获取前一天的日期
                    sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
                    Log.e("获取的日期：",dateStr);
                }else{
                    Log.e("获取的日期：",dateStr);
                    String year = dateStr.split("-")[0];
                    String month = dateStr.split("-")[1];
                    String day = dateStr.split("-")[2];
                    sysDateStr = year+month+day;
                }
            }else{
                sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
            }
            Log.e("获取的日期：",sysDateStr);

        }

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
    public static String getEndTimeStampTo(String dateStr){
        String timeStr = "235959";
        String sysDateStr = "";
        if(Utils.isEmpty(dateStr)){
            //获取系统日期
//            sysDateStr = DateTimeUtil.getFormatSystemTime("yyyyMMdd");
            //获取前一天的日期
            sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
            Log.e("获取的日期：",sysDateStr);
        }else{

            if(dateStr.contains("-")){
                String syaDate = DateTimeUtil.getFormatSystemTime("yyyyMMdd");
                if(dateStr.equals(syaDate)){
                    //获取前一天的日期
                    sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
                    Log.e("获取的日期：",dateStr);
                }else{
                    Log.e("获取的日期：",dateStr);
                    String year = dateStr.split("-")[0];
                    String month = dateStr.split("-")[1];
                    String day = dateStr.split("-")[2];
                    sysDateStr = year+month+day;
                }
            }else{
                sysDateStr = DateTimeUtil.getDateStr(-1,"yyyyMMdd");
            }



            Log.e("获取的日期：",sysDateStr);

        }


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
