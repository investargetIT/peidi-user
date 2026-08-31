package com.cyanrocks.boilerplate.utils;

import com.cyanrocks.boilerplate.vo.response.PageResult;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.LongSupplier;

/**
 * 分页工具类：统一处理页码/页大小校验、offset计算、PageResult组装
 *
 * 用法示例：
 * <pre>
 * PageUtils.of(pageNum, pageSize,
 *         () -> mapper.countXxx(param),                       // 总条数
 *         (offset, size) -> mapper.selectXxx(param, offset, size)) // 分页数据
 * </pre>
 */
public final class PageUtils {

    private PageUtils() {
    }

    /** 默认页码 */
    private static final int DEFAULT_PAGE_NUM = 1;
    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 10;
    /** 每页最大条数，防止pageSize过大拖垮查询 */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * 通用分页查询
     * @param pageNum 页码（从1开始，非法值自动回退为1）
     * @param pageSize 每页条数（非法值自动回退为10，最大1000）
     * @param countSupplier 总条数查询
     * @param dataSupplier 分页数据查询，入参为(offset, size)
     */
    public static <T> PageResult<T> of(int pageNum, int pageSize,
                                       LongSupplier countSupplier,
                                       BiFunction<Integer, Integer, List<T>> dataSupplier) {
        if (pageNum < 1) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        long total = countSupplier.getAsLong();
        int offset = (pageNum - 1) * pageSize;
        List<T> list = dataSupplier.apply(offset, pageSize);
        return new PageResult<>(total, pageNum, pageSize, list);
    }
}
