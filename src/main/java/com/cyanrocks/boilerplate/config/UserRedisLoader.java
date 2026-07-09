package com.cyanrocks.boilerplate.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动时加载用户到Redis
 * @Author wjq
 */
@Component
public class UserRedisLoader implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UserRedisLoader.class);

    private static final String USER_REDIS_KEY = "peidi:user:dingids";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("开始加载用户到Redis...");
        loadUsersToRedis();
        LOG.info("用户加载到Redis完成");
    }

    /**
     * 加载所有未在OA删除的用户到Redis
     */
    public void loadUsersToRedis() {
        // 清除旧数据
        stringRedisTemplate.delete(USER_REDIS_KEY);

        // 查询所有未在OA删除的用户
        List<User> userList = userMapper.selectList(
            Wrappers.<User>lambdaQuery()
                .isNotNull(User::getDingId)
                .eq(User::getOaDelete, false)
                .or()
                .isNull(User::getOaDelete)
        );

        // 存储到Redis Set中
        for (User user : userList) {
            if (null != user.getDingId()) {
                stringRedisTemplate.opsForSet().add(USER_REDIS_KEY, user.getDingId());
            }
        }

        LOG.info("已加载 {} 个用户到Redis", userList.size());
    }
}