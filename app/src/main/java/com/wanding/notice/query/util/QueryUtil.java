package com.wanding.notice.query.util;

import com.wanding.notice.bean.SearchUserResult;
import com.wanding.notice.bean.UserBean;

/**
 * 查询交易参数帮助类
 */
public class QueryUtil {

    /**  条件内容  */
    public static final String[] payTypeArray = {"全部","微信支付","支付宝支付","翼支付","贷记卡","借记卡","银联二维码"};

    public static final String[] payStateArray = {"全部","收款成功","退款成功"};
    /**
     * 支付方式类型转换
     * payWay（“ALI”：支付宝）（“WX”：微信）（“BEST”：翼支付）（“CREDIT”：贷记卡）（“DEBIT”：借记卡）（“UNIONPAY” ：银联二维码）
     */
    public static String getPayTypeStr(String str){
        if(str.equals(payTypeArray[0])){
            return "";
        }else if(str.equals(payTypeArray[1])){
            return "WX";
        }else if(str.equals(payTypeArray[2])){
            return "ALI";
        }else if(str.equals(payTypeArray[3])){
            return "BEST";
        }else if(str.equals(payTypeArray[4])){
            return "CREDIT";
        }else if(str.equals(payTypeArray[5])){
            return "DEBIT";
        }else if(str.equals(payTypeArray[6])){
            return "UNIONPAY";
        }
        return "";
    }

    public static String getPayTypeName(String str){
        if(str.equals("WX")){
            return "微信";
        }else if(str.equals("ALI")){
            return "支付宝";
        }else if(str.equals("BEST")){
            return "翼支付";
        }else if(str.equals("CREDIT")){
            return "贷记卡";
        }else if(str.equals("DEBIT")){
            return "借记卡";
        }else if(str.equals("UNIONPAY")){
            return "银联二维码";
        }else if(str.equals("BANK")){
            return "银行卡";
        }
        return "";

    }

    /**
     * 支付状态类型转换
     * 支付状态 status:传1代表成功，2代表失败，3代表有退款
     * 支付状态 orderType:0.收款成功 1.退款成功2.全部
     */
    public static String getPayStateStr(String str){
        if(str.equals(payStateArray[0])){
            return "2";
        }else if(str.equals(payStateArray[1])){
            return "0";
        }else if(str.equals(payStateArray[2])){
            return "1";
        }
        return "";
    }


}
