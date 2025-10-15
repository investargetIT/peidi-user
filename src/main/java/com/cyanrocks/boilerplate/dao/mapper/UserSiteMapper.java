package com.cyanrocks.boilerplate.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyanrocks.boilerplate.dao.entity.UserSite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserSiteMapper extends BaseMapper<UserSite> {

    @Select("select * from user_site where active = true")
    List<UserSite> selectAll();

}
