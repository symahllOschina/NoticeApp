package com.wanding.notice.utils;


/**
 * 服务地址管理类
 *
 * 
 */
public class NitConfig {
	
	/**  打包前必看：
	 * 1，替换正式域名前缀(包括更新版本地址前缀)
	 */
	public static final boolean isFormal = false;//true:正式环境,false:测试环境


	//测试服务器地址前缀
	public static final String basePath =  "https://dev.weupay.com/pay";
	public static final String queryBasePath =  "https://dev.weupay.com/admin";
	public static final String historyBasePath =  "http://test.weupay.com:8080/download";


	//正式服务器地址前缀
	public static final String basePath1 = "https://weixin.weupay.com/pay";
	public static final String queryBasePath1 = "https://weixin.weupay.com/admin";
	public static final String historyBasePath1 =  "http://download.weupay.com/download";





	/**
	 * 查历史：参数如查当日
	 * 测试：http://test.weupay.com:8080/download/api/app/200/1/queryOrderHistory
	 * 正式：http://download.weupay.com/download/api/app/200/1/queryOrderHistory
	 */
	public static final String queryOrderHistoryListUrl = historyBasePath+"/api/app/200/1/queryOrderHistory";

	/**
	 * 查历史详情
	 * 测试：http://test.weupay.com:8080/download/api/app/200/1/queryOrderDetail
	 * 正式：http://download.weupay.com/download/api/app/200/1/queryOrderDetail
	 */
	public static final String getOrderHistoryDetailsUrl = historyBasePath+"/api/app/200/1/queryOrderDetail";



	/**
	*
	 * 	入参：account，password
	 * 测试登录商户账号：1000145，门店账号：1000145101  款台账号：100014510111密码123456
	 * 返回状态：state:200登录成功，300登录失败
	 */
	public static final String doLoginUrl = basePath+"/api/app/200/1/loginApp";

	/**
	 * 查询别名
	 */
	public static final String queryAliasStatusUrl = basePath+"/api/app/200/1/queryClientId";

	/**
	*
	 * 	入参：mid
	 */
	public static final String getBusInfoUrl = basePath+"/api/app/200/1/queryMerDetail";

	/**
	 * 门店模糊查询:/api/app/200/1/queryStoreByName
	 * 入参：mid,sname(门店名称)
	 */
	public static final String searchStoreUrl = basePath+"/api/app/200/1/queryStoreByName";

	/**
	 * 款台模糊查询：api/app/200/1/queryEmpByName
	 * 入参：storeId，ename
	 */
	public static final String searchTerminalUrl = basePath+"/api/app/200/1/queryEmpByName";

	/**
	 * 当日订单查询：
	 * 入参：
	 * roleId(角色登陆的主键id)，
	 * role（角色），
	 * startTime（开始时间），
	 * endTime（结束时间），
	 * 门店登陆查询传"mid",
	 * 款台登陆查询传（mid,sid）
	 * pageNum（第几页）
	 * numPerPage,一页显示条数（一页显示条数，默认10条）
	 * 支付方式 payWay（“ALI”：支付宝）（“WX”：微信）（“BEST”：翼支付）（“CREDIT”：贷记卡）（“DEBIT”：借记卡）
	 * 支付状态 status:传1代表成功，2代表失败，3代表有退款
	 */
	public static final String queryOrderDayListUrl = basePath+"/api/app/200/1/queryOrder";



	/**
	 * 订单详情：
	 *  入参：
	 * orderId（订单号）
	 */
	public static final String getOrderDetailsUrl = basePath+"/api/app/200/1/queryOrderDetail";

	/**
	 * 退款获取验证码
	 * 入参：orderId，sid， mid
	 */
	public static final String getVerCodeUrl = queryBasePath + "/api/app/200/sendVerCodeT";

	/**
	 * 退款：api/app/200/1/refund
	 *  入参：
	 * orderId（订单号），
	 * amount（退款金额），
	 * desc（备注），
	 * passWord（登陆密码）
	 * role，
	 * roleId
	 */
	public static final String refundRequestUrl = queryBasePath+"/api/app/200/1/refund";

	/**
	 * 商户三十天内日交易查询：
	 * 入参：
	 * roleId(角色登陆的主键id)，
	 * role（角色），
	 * startTime，
	 * endTime，
	 * type（传1代表      金额，传2代表查笔数）
	 */
	public static final String getStatisDataUrl = basePath+"/api/app/200/1/merDataSum";



	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
}
