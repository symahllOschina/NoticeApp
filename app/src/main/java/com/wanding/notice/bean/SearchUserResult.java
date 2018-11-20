package com.wanding.notice.bean;

import java.io.Serializable;

/**
 * 查询门店，款台返回的实体对象
 */
public class SearchUserResult implements Serializable{
    /*模糊查询门店返回：
    {
        "data": {
        "storeList": [{
            "saccount": "1000145101",
                    "id": 1971,
                    "value": "大客户会员门店"
        }]
    },
        "message": null,
            "status": 200
    }
    模糊查询款台返回：
    {
        "data": {
        "emplyeeList": [{
            "eid": 89,
                    "value": "大客户会员款台"
        }]
    },
        "message": null,
            "status": 200
    }*/

    private String saccount;
    //此id意义与UserBean 的sid意义同样，代表门店id
    private Integer id;
    //此eid意义与UserBean 的eid意义同样，代表款台id
    private Integer eid;
    private String value;

    public SearchUserResult() {
    }

    public String getSaccount() {
        return saccount;
    }

    public void setSaccount(String saccount) {
        this.saccount = saccount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
