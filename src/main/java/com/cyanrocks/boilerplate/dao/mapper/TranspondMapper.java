package com.cyanrocks.boilerplate.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


@Mapper
public interface TranspondMapper extends BaseMapper {

    @Select("${sql}")
    List<Map<String, Object>> sql(@Param("sql") String sql);
}
