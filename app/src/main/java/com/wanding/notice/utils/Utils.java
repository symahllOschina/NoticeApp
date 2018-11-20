package com.wanding.notice.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 常用工具类方法总结
 * dip2px：根据手机分辨率把dp转换成px(像素)
 * px2dip：根据手机分辨率把px转换成dp
 * isEmpty ：判断String是否为null,"",是返回为true
 * isNotEmpty：判断String是否为null,"",不是返回为true
 * isMobileNO：验证手机格式是否正确
 * isZipNO ：判断邮编是否合法，中国邮政编码为6位数字，第一位不为0
 * isEmail：判断邮箱格式是否正确
 * isFastClick ： 防止按钮被连续点击
 * getDisplayWidth：获取屏幕的宽度px像素值
 * getDisplayHeight：获取屏幕的高度px像素值
 * isUrl：判断是否为网址格式（仅判断str内容是否匹配“http://或https://”）
 * getVersionName：获取当前程序的版本号
 */
public class Utils {
	public static final String TEST_IMAGE_URL = "http://115.28.147.19/images/product/20150130/641394c5f5bf40b69a083bea59d4a7f8/e78f811c7695401da691d71678b2a367_800.jpg";

	/**
	 * 根据手机分辨率把dp转换成px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机分辨率把px转换成dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 判断String是否为null,"",是返回为true
	 */
	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0 || s.trim().equals("") || s.trim().equals("null");
	}

	/**
	 * 判断String是否为null,"",不是返回为true
	 */
	public static boolean isNotEmpty(String s) {
		return s != null && s.length() != 0 && !s.trim().equals("") && !s.trim().equals("null");
	}
	
	/** 
	 * 验证手机格式 
	 * 故先要整清楚现在已经开放了多少个号码段，国家号码段分配如下：
	 *	移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
	 *	联通：130、131、132、152、155、156、185、186
	 *	电信：133、153、180、189、（1349卫通）
	 */  
	
	public static boolean isMobileNO(String mobiles){
		//注意此种方式仅支持以上号段，如177电信号也会表示不匹配
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");  
		//这里代表电话号码必须以14、13、15、18、17开头的11位数，可以根据自己的需求进行修改
		Pattern p1 = Pattern.compile("^((14[0-9])|(13[0-9])|(15[0-9])|(18[0-9])|(17[0-9]))\\d{8}$");
		//
		Pattern p2 = Pattern.compile("^(13[0-9]|14[57]|15[0-35-9]|17[6-8]|18[0-9])[0-9]{8}$");
		Matcher m = p1.matcher(mobiles);  
		return m.matches();
	}
	
	/**
	 *  判断邮编是否合法
	 *  中国邮政编码为6位数字，第一位不为0
	 */
	public static boolean isZipNO(String zipString){
	      String str = "^[1-9][0-9]{5}$";
	      return Pattern.compile(str).matcher(zipString).matches();
	}
	
	/**
	 * 判断邮箱格式是否正确
	 */
	 public static boolean isEmail(String email){
		 if (null==email || "".equals(email)) return false;	
		 //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配  
		 Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配  
		 Matcher m = p.matcher(email);  
		 return m.matches();  
	 }
	 
	 /**
	  *  防止按钮被连续点击
	  *  这里设置的是连续3秒内不能重复点击该按钮
	  */
	private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();   
        if ( time - lastClickTime < 3000) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }

    
    /**  
     * 获取屏幕宽高  
     * 获取的宽高都为屏幕的像素px
     * */
    //获取屏幕的宽度
    public static int getDisplayWidth(Activity activity){
    	int width = 0;
    	Display display= activity.getWindow().getWindowManager().getDefaultDisplay();  
    	DisplayMetrics dm=new DisplayMetrics();  
    	display.getMetrics(dm);  
    	width=dm.widthPixels;  
    	return width;
    }
    //获取屏幕的高度
    public static int getDisplayHeight(Activity activity){
    	int height = 0;
    	Display display= activity.getWindow().getWindowManager().getDefaultDisplay();  
    	DisplayMetrics dm=new DisplayMetrics();  
    	display.getMetrics(dm);  
    	height=dm.heightPixels;  
    	return height;
    }
    
    /**
     * 判断是否为网址格式（仅判断str内容是否匹配“http://或https://”）
     */
    public static boolean isUrl(String str){  
        // 转换为小写  
        str = str.toLowerCase();  
        String[] regex = { "http://", "https://" };  
        boolean isUrl = false;  
        for (int i = 0; i < regex.length; i++) {  
            isUrl = isUrl || (str.contains(regex[i])) && str.indexOf(regex[i]) == 0;  
        }  
        return isUrl;  
    }

	/**
	 * 获取当前程序的版本号
	 */
	public static String getVersionName(Context context) throws Exception {
		//获取packagemanager的实例
		PackageManager packageManager = context.getPackageManager();
		//getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
		return packInfo.versionName;
	}
}
