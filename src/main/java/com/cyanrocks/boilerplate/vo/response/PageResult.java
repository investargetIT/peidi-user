package com.cyanrocks.boilerplate.vo.response;

import lombok.Data;

import java.util.List;

/**
 * 通用分页返回对象
 */
@Data
public class PageResult<T> {

    /** 总条数 */
    private long total;

    /** 当前页码（从1开始） */
    private int pageNum;

    /** 每页条数 */
    private int pageSize;

    /** 数据列表 */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long total, int pageNum, int pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }
}
