package com.cyanrocks.boilerplate.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyanrocks.boilerplate.dao.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Delete("delete from inventory_config")
    void deleteAll();
}
