package com.cyanrocks.boilerplate.scheduled;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.entity.UserInfo;
import com.cyanrocks.boilerplate.dao.entity.UserOa;
import com.cyanrocks.boilerplate.dao.mapper.UserInfoMapper;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.utils.DingUtils;
import com.cyanrocks.boilerplate.utils.OaUtils;
import com.cyanrocks.boilerplate.vo.dto.DingDepartmentDTO;
import com.cyanrocks.boilerplate.vo.dto.DingUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author wjq
 * @Date 2025/5/30 10:42
 */

@Component
public class ScheduledTasks {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final String USER_REDIS_KEY = "peidi:user:dingids";
    @Value("${union.send.kpi.id}")
    private String UNION_SEND_KPI_ID;
    @Value("${dingding.corpid}")
    private String DING_CORPID;
    @Value("${dingding.appkey}")
    private String DING_APPKEY;
    @Value("${pm.app.url}")
    private String PM_APP_URL;
    @Value("${oa.address}")
    private String oaUtilUrl;


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private DingUtils dingUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OaUtils oaUtils;

    /**
     * Cron表达式任务（每天9点）
     * 格式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void deleteUserInfo() {
        LOG.info("离职用户处理开始: {}", new java.util.Date());
        List<User> userList = userMapper.selectList(Wrappers.<User>lambdaQuery().isNotNull(User::getDingId));
        //数据同步来自dingding
        for (User user : userList) {
            try {
                JSONObject jsonObject = dingUtils.getUserinfoByUserid(user.getDingId());
                if (null == jsonObject) {
                    // 用户不存在（已离职），设置oaDelete=true
                    user.setOaDelete(true);
                    userMapper.updateById(user);
                    // 从Redis中移除离职用户
                    stringRedisTemplate.opsForSet().remove(USER_REDIS_KEY, user.getDingId());
                    LOG.info("用户已离职: userId={}, dingId={}", user.getId(), user.getDingId());
                }
                if (user.getOaDelete() && jsonObject != null) {
                    //debug : 实际用户没有在dd中删除,因为网络等其他原因被删除的时候在此debug
                }
            } catch (Exception e) {
                // API调用失败（网络异常、Token过期等），不设置oaDelete，记录日志
                LOG.error("检查用户离职状态失败, userId={}, dingId={}: {}", user.getId(), user.getDingId(), e.getMessage());
            }
        }
        //数据同步来自温州总部系统
        oaUtils.getoken(oaUtilUrl);
        //一业一百条一共100*100=10000条数据
        for (int i = 0; i < 101; i++) {
            //获取到状态是4.解聘 5.离职 6.退休 7.无效
            List<UserOa> oaUserInfo = oaUtils.getOaUserInfo(i + 1);
            for (UserOa userOa : oaUserInfo) {
                String lastname = userOa.getLastname();
                List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery().eq(User::getUsername, lastname));

                // 1. 如果数组不为空且数组长度等于1，且oa_delete!=1的情况下修改用户状态为离职
                if (CollectionUtil.isNotEmpty(users) && users.size() == 1) {
                    User user = users.get(0);
                    if (user.getOaDelete() == null || !user.getOaDelete()) {
                        user.setOaDelete(true);
                        userMapper.updateById(user);
                        // 从Redis中移除离职用户
                        stringRedisTemplate.opsForSet().remove(USER_REDIS_KEY, user.getDingId());
                        LOG.info("OA离职用户处理成功: userId={}, username={}, dingId={}", user.getId(), user.getUsername(), user.getDingId());
                    }
                }
                // 2. 如果数组长度大于等于2的情况下，循环数组，增加电话为搜索条件继续查询，直到查询到唯一用户时才修改离职状态
                else if (users != null && users.size() >= 2) {
                    String oaMobile = userOa.getMobile();
                    if (StringUtils.isNotBlank(oaMobile)) {
                        // 使用姓名和电话双重条件查询唯一用户
                        List<User> filteredUsers = userMapper.selectList(
                            Wrappers.<User>lambdaQuery()
                                .eq(User::getUsername, lastname)
                                .eq(User::getMobile, oaMobile)
                        );
                        if (filteredUsers.size() == 1) {
                            User user = filteredUsers.get(0);
                            if (user.getOaDelete() == null || !user.getOaDelete()) {
                                user.setOaDelete(true);
                                userMapper.updateById(user);
                                // 从Redis中移除离职用户
                                stringRedisTemplate.opsForSet().remove(USER_REDIS_KEY, user.getDingId());
                                LOG.info("OA离职用户处理成功(通过电话匹配): userId={}, username={}, mobile={}, dingId={}",
                                    user.getId(), user.getUsername(), user.getMobile(), user.getDingId());
                            }
                        } else {
                            LOG.warn("OA离职用户处理失败: 姓名为{}且电话为{}的用户不唯一或不存在, 查询到{}条记录",
                                lastname, oaMobile, filteredUsers.size());
                        }
                    } else {
                        LOG.warn("OA离职用户处理失败: 姓名为{}存在多个用户({}个), 但OA电话为空, 无法唯一确定",
                            lastname, users.size());
                    }
                }
            }

        }

        LOG.info("离职用户处理结束: {}", new java.util.Date());
    }

    /**
     * 检测当天创建但在Redis中不存在的用户（每10分钟执行一次）
     * 输出不存在的用户钉钉ID
     */
//    @Scheduled(cron = "0 */10 * * * ?")
//    @Scheduled(fixedRate = 10000)
    public void checkNewUsersNotInRedis() {
        LOG.info("检测新用户开始: {}", new java.util.Date());

        // 查询没有发送的用户
        List<User> todayUsers = userMapper.selectList(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getIsSend, 0)
        );
        for (User user : todayUsers) {
            //待通知人的ddId
            JSONObject userInfo = dingUtils.getUserinfoByUserid(user.getDingId());
            String unionId = userInfo.getStr("unionid");
            List<String> list = Arrays.asList(
                    unionId
            );
            JSONObject userinfoByUserid = dingUtils.getUserinfoByUserid(user.getDingId());
            DingUserDTO dataUser = userinfoByUserid.toBean(DingUserDTO.class);
            Long deptId = dataUser.getDept_order_list().get(0).getDept_id();
            DingDepartmentDTO dataDepart = dingUtils.getDingDepartment(deptId).toBean(DingDepartmentDTO.class);
            System.out.println();

            String appUrl = PM_APP_URL;
            String encodedUrl;
            try {
                encodedUrl = URLEncoder.encode(PM_APP_URL, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                encodedUrl = PM_APP_URL;
            }
            String pcUrl = "dingtalk://dingtalkclient/action/openapp?corpid=" + DING_CORPID
                    + "&container_type=work_platform&app_id=0_" + DING_APPKEY
                    + "&redirect_type=jump&redirect_url=" + encodedUrl;
            String taskId = dingUtils.createTodoTask(UNION_SEND_KPI_ID, "新人入职:" + dataUser.getName() + " 部门:" + dataDepart.getName() + ",此用户是否需要配置绩效目标", list, null, appUrl, pcUrl, 10, System.currentTimeMillis() + 5 * 60 * 1000);
            if (StringUtils.isNotBlank(taskId)) {
                user.setIsSend(1);
                userMapper.updateById(user);
                LOG.info("通知绩效配置成功: {},任务id: {}", new java.util.Date(), taskId);
            } else {
                LOG.info("通知绩效配置失败: {}, 失败原因: {}", new java.util.Date(), "taskId为空");
            }

        }
        LOG.info("检测新用户结束: {}, 检测用户数: {}", new java.util.Date(), todayUsers.size());
    }


}

