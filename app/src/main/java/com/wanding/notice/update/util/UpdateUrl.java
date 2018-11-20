package com.wanding.notice.update.util;

/**
 * 管理服务器数据地址
 * url：应用版本升级详情（包括版本号，新版本详情等信息）
 */
public class UpdateUrl {

    /**
     * 测试环境
     */
    public static String testUrl = "http://test.weupay.com:8081/download/downloadVersion";

    /**
     * 正式环境
     */
    public static String url = "http://download.weupay.com/download/downloadVersion";
}
