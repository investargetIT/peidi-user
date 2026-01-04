package com.cyanrocks.boilerplate.scheduled;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.entity.UserInfo;
import com.cyanrocks.boilerplate.dao.mapper.UserInfoMapper;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.utils.DingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author wjq
 * @Date 2025/5/30 10:42
 */

@Component
public class ScheduledTasks {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private DingUtils dingUtils;


    /**
     * Cron表达式任务（每天9点）
     * 格式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void deleteUserInfo() {
        System.out.println("离职用户处理开始: " + new java.util.Date());
        List<User> userList = userMapper.selectList(Wrappers.<User>lambdaQuery().isNotNull(User::getDingId));
        userList.forEach(user -> {
            JSONObject jsonObject = dingUtils.getUserinfoByUserid(user.getDingId());
            if (null == jsonObject){
                user.setOaDelete(true);
                userMapper.updateById(user);
            }
        });
        System.out.println("离职用户处理结束: " + new java.util.Date());
    }

}