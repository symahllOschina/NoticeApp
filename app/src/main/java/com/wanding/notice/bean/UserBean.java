package com.wanding.notice.bean;

import java.io.Serializable;

/**
 * 登录用户实体
 */
public class UserBean implements Serializable{

    /*{"data":{"name":"大客户会员款台","sid":"1971","eid":"89","mid":"200","role": "store","roleId":"89","account":"100014510111"},"message":"","status":200}

    "name": "大客户会员款台",
		"sid": "1971",
		"eid": "89",
		"mid": "200",
		"role": "store",
		"roleId": "89",
		"account": "100014510111"*/


    private String name;//商户名称
    private String sid;//门店id
    private String eid;//款台id
    private String mid;//商户id
    private String role;//角色：("shop","商户"),("employee","员工"),("store","门店"),
    private String roleId;//角色ID
    private String account;//登录用户名



    public UserBean() {
		super();
		// TODO Auto-generated constructor stub
	}

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.name = roleId;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.name = account;
	}

}
