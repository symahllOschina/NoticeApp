package com.wanding.notice.bean;

import java.util.List;

/**
 * 统计返回List的Data
 */
public class StatisListData {

    private String returnST;//
    private String returnET;
    private Double sumAmt;//总金额
    private Integer sumTotal;//
    private Integer countRow;
    private Integer numPerPage;
    private Integer totalCount;
    private Integer pageNum;

    private List<StatisData> merDataSumList;

    public StatisListData() {

    }

    public String getReturnST() {
        return returnST;
    }

    public void setReturnST(String returnST) {
        this.returnST = returnST;
    }

    public String getReturnET() {
        return returnET;
    }

    public void setReturnET(String returnET) {
        this.returnET = returnET;
    }

    public Double getSumAmt() {
        return sumAmt;
    }

    public void setSumAmt(Double sumAmt) {
        this.sumAmt = sumAmt;
    }

    public Integer getSumTotal() {
        return sumTotal;
    }

    public void setSumTotal(Integer sumTotal) {
        this.sumTotal = sumTotal;
    }

    public Integer getCountRow() {
        return countRow;
    }

    public void setCountRow(Integer countRow) {
        this.countRow = countRow;
    }

    public Integer getNumPerPage() {
        return numPerPage;
    }

    public void setNumPerPage(Integer numPerPage) {
        this.numPerPage = numPerPage;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public List<StatisData> getMerDataSumList() {
        return merDataSumList;
    }

    public void setMerDataSumList(List<StatisData> merDataSumList) {
        this.merDataSumList = merDataSumList;
    }
}
