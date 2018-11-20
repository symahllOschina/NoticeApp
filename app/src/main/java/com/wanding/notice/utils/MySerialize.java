package com.wanding.notice.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.wanding.notice.bean.UserBean;

/**
 * 本地序列化保存对象或者集合
 * 使用方法可见:meethodTest()
 */
public class MySerialize {
	
	public static String serialize(Object obj) throws IOException {
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		objectOutputStream.writeObject(obj);
		String serStr = byteArrayOutputStream.toString("ISO-8859-1");
		serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
		objectOutputStream.close();
		byteArrayOutputStream.close();
		return serStr;
	}
	/**
	 * 反序列化对象
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deSerialization(String str) throws IOException,
			ClassNotFoundException {
		
		String redStr = java.net.URLDecoder.decode(str, "UTF-8");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				redStr.getBytes("ISO-8859-1"));
		ObjectInputStream objectInputStream = new ObjectInputStream(
				byteArrayInputStream);
		Object obj = (Object) objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		
		return obj;
	}
	public static String getObject(String key,Context content) {
		SharedPreferences sp = content.getSharedPreferences(key, 0);
		return sp.getString(key, null);
	}
	public static void saveObject(String key,Context content,String str) {
		SharedPreferences sp = content.getSharedPreferences(key, 0);
		Editor edit = sp.edit();
		edit.putString(key, str);
		edit.commit();
	}


	/**
	 * 调用示例
	 */
	private void meethod(Context context){
		/**  保存对象  */
		Object obj = new Object();
		//保存
		try {
			MySerialize.saveObject("myName",context.getApplicationContext(),MySerialize.serialize(obj));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//取出对象
		try {
			obj=(Object) MySerialize.deSerialization(MySerialize.getObject("myName", context));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**  集合跳转传值保存  */
		List<String> strList = new ArrayList<String>();
		//跳转传值保存
        try {
            Intent in=new Intent();
//				in.setClass(getActivity(), OrderConfirmActivity.class);
            String shopListStr = MySerialize.serialize(strList);
            in.putExtra("strList", shopListStr);
            context.startActivity(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //目标界面取值
        try {
            Intent in=new Intent();
            String shopListStr=in.getExtras().getString("shopList");
            strList=(List<String>) MySerialize.deSerialization(shopListStr);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //集合保存
        List<String> list=new ArrayList<String>();
        //保存
        try {
            String shopListStr = MySerialize.serialize(list);
            MySerialize.saveObject("myName",context.getApplicationContext(),shopListStr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //取值
        try {
            list = (List<String>) MySerialize.deSerialization(MySerialize.getObject("myName", context));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
