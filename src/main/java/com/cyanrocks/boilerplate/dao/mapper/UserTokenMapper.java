package com.cyanrocks.boilerplate.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.entity.UserToken;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {

}
