package com.wanding.notice.bean;

/**
 * 统计返回Data
 */
public class StatisData {

    private Long date;//日期
    private Double money;//金额/笔数

    public StatisData() {
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }
}
