package com.cyanrocks.boilerplate.scheduled;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyanrocks.boilerplate.dao.entity.AttendanceUser;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.entity.UserOa;
import com.cyanrocks.boilerplate.dao.mapper.AttendanceUserMapper;
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
    /** 标准工时考勤组ID（工时看板） */
    @Value("${attendance.group.id:209090469}")
    private Long ATTENDANCE_GROUP_ID;


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private AttendanceUserMapper attendanceUserMapper;
    @Autowired
    private com.cyanrocks.boilerplate.service.AttendanceRecordService attendanceRecordService;
    @Autowired
    private com.cyanrocks.boilerplate.service.AttendanceOvertimeService attendanceOvertimeService;
    @Autowired
    private com.cyanrocks.boilerplate.service.AttendanceDeptService attendanceDeptService;
    @Autowired
    private com.cyanrocks.boilerplate.service.AttendanceLeaveService attendanceLeaveService;
    @Autowired
    private DingUtils dingUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OaUtils oaUtils;

    /**
     * 同步考勤组参与人员userId（每天早上9点30）
     * 数据来源：钉钉标准工时考勤组，全量同步到 attendance_user 表
     * 策略：新增的插入，考勤组中已移除的删除
     */
    @Scheduled(cron = "0 30 9 * * ?", zone = "Asia/Shanghai")
    public void syncAttendanceUsers() {
        LOG.info("考勤组人员同步开始: {}", new java.util.Date());
        List<String> remoteUserIds = dingUtils.getAllAttendanceGroupUsers(ATTENDANCE_GROUP_ID);
        if (CollectionUtil.isEmpty(remoteUserIds)) {
            LOG.error("考勤组人员同步失败: 获取考勤组人员列表为空, groupId={}", ATTENDANCE_GROUP_ID);
            return;
        }
        // 转成Set便于比对
        java.util.Set<String> remoteSet = new java.util.HashSet<>(remoteUserIds);
        // 查询库中已有数据
        List<AttendanceUser> dbUsers = attendanceUserMapper.selectList(null);
        java.util.Set<String> dbSet = new java.util.HashSet<>();
        for (AttendanceUser dbUser : dbUsers) {
            dbSet.add(dbUser.getDingUserId());
        }
        LocalDateTime now = LocalDateTime.now();
        // 1. 新增：钉钉有、库里没有的
        int insertCount = 0;
        for (String dingUserId : remoteSet) {
            if (!dbSet.contains(dingUserId)) {
                AttendanceUser attendanceUser = new AttendanceUser();
                attendanceUser.setDingUserId(dingUserId);
                attendanceUser.setGroupId(ATTENDANCE_GROUP_ID);
                attendanceUser.setCreateTime(now);
                attendanceUser.setUpdateTime(now);
                attendanceUserMapper.insert(attendanceUser);
                insertCount++;
            }
        }
        // 2. 删除：库里有、钉钉没有的（已移出考勤组）
        int deleteCount = 0;
        for (AttendanceUser dbUser : dbUsers) {
            if (!remoteSet.contains(dbUser.getDingUserId())) {
                attendanceUserMapper.deleteById(dbUser.getId());
                deleteCount++;
            }
        }
        LOG.info("考勤组人员同步结束: 远程人数={}, 新增={}, 移除={}, 库中保留={}",
                remoteSet.size(), insertCount, deleteCount, remoteSet.size());
        // 3. 补充未登录系统用户的姓名（无法通过 dingUserId 关联到 user 表的用户）
        fillOtherNameForUnmatchedUsers();
    }

    /**
     * 通过 AttendanceUser.dingUserId 关联 User.ding_id，
     * 对关联不到的用户（从未登录过系统），调用钉钉「查询用户详情」接口获取姓名，
     * 保存到 AttendanceUser.otherName
     */
    private void fillOtherNameForUnmatchedUsers() {
        // 查询尚未获取到姓名的考勤人员
        List<AttendanceUser> pendingUsers = attendanceUserMapper.selectList(
                Wrappers.<AttendanceUser>lambdaQuery().isNull(AttendanceUser::getOtherName));
        if (CollectionUtil.isEmpty(pendingUsers)) {
            LOG.info("考勤人员姓名补充: 无待处理数据");
            return;
        }
        // 查询这些钉钉ID中已登录过系统（user表存在）的用户
        List<String> dingUserIds = new ArrayList<>();
        for (AttendanceUser attendanceUser : pendingUsers) {
            dingUserIds.add(attendanceUser.getDingUserId());
        }
        List<User> matchedUsers = userMapper.selectList(
                Wrappers.<User>lambdaQuery()
                        .in(User::getDingId, dingUserIds)
                        .isNotNull(User::getDingId));
        java.util.Set<String> matchedDingIds = new java.util.HashSet<>();
        for (User user : matchedUsers) {
            matchedDingIds.add(user.getDingId());
        }
        LocalDateTime now = LocalDateTime.now();
        int fillCount = 0;
        for (AttendanceUser attendanceUser : pendingUsers) {
            // 已登录过系统的用户，姓名可从 user 表关联获取，无需处理
            if (matchedDingIds.contains(attendanceUser.getDingUserId())) {
                continue;
            }
            try {
                // 调用钉钉「查询用户详情」接口获取姓名
                JSONObject userInfo = dingUtils.getUserinfoByUserid(attendanceUser.getDingUserId());
                String name = null == userInfo ? null : userInfo.getStr("name");
                if (StringUtils.isNotBlank(name)) {
                    attendanceUser.setOtherName(name);
                    attendanceUser.setUpdateTime(now);
                    attendanceUserMapper.updateById(attendanceUser);
                    fillCount++;
                } else {
                    LOG.warn("获取考勤人员姓名失败(用户不存在或姓名为空): dingUserId={}", attendanceUser.getDingUserId());
                }
            } catch (Exception e) {
                LOG.error("获取考勤人员姓名异常: dingUserId={}, 原因: {}", attendanceUser.getDingUserId(), e.getMessage());
            }
        }
        LOG.info("考勤人员姓名补充结束: 待处理={}, 已登录系统={}, 从钉钉获取姓名={}",
                pendingUsers.size(), matchedDingIds.size(), fillCount);
    }

    /**
     * 同步昨天的打卡结果（每天早上9点40）
     * 前置条件：8点的考勤组人员同步已完成（attendance_user 表有数据）
     * 幂等：按recordId upsert，重复执行安全，漏数据时可手动重跑
     */
    @Scheduled(cron = "0 40 9 * * ?", zone = "Asia/Shanghai")
    public void syncYesterdayAttendanceRecords() {
        LOG.info("昨日打卡结果同步开始: {}", new java.util.Date());
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            int count = attendanceRecordService.syncAttendanceRecords(yesterday);
            LOG.info("昨日打卡结果同步结束: 日期={}, 处理记录数={}", yesterday, count);
        } catch (Exception e) {
            LOG.error("昨日打卡结果同步失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步近7天的加班审批（每天早上9点50）
     * 按审批发起时间拉取已完成的加班申请，滚动7天窗口
     * （加班审批可能在加班日期之后几天才审批完成，只同步昨天会漏数据）
     * 幂等：按审批实例ID upsert，重复执行安全
     */
    @Scheduled(cron = "0 50 9 * * ?", zone = "Asia/Shanghai")
    public void syncOvertimeApprovals() {
        LOG.info("加班审批同步开始: {}", new java.util.Date());
        try {
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(6);
            int count = attendanceOvertimeService.syncOvertimeApprovals(startDate, endDate);
            LOG.info("加班审批同步结束: {} ~ {}, 处理记录数={}", startDate, endDate, count);
        } catch (Exception e) {
            LOG.error("加班审批同步失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步近7天的请假信息（每天早上9点55）
     * 拉取钉钉请假状态（getleavestatus），滚动7天窗口，
     * 用于每日工时汇总的请假标记（leave: true/false）
     * 幂等：按 钉钉userId+开始/结束时间+假种code upsert，重复执行安全
     */
    @Scheduled(cron = "0 55 9 * * ?", zone = "Asia/Shanghai")
    public void syncLeaveRecords() {
        LOG.info("请假信息同步开始: {}", new java.util.Date());
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);
            int count = attendanceLeaveService.syncLeaveRecords(startDate, endDate);
            LOG.info("请假信息同步结束: {} ~ {}, 处理记录数={}", startDate, endDate, count);
        } catch (Exception e) {
            LOG.error("请假信息同步失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步钉钉部门树 + 考勤人员所属部门（每天早上10点13分，错开整点高峰避免钉钉QPS限流）
     * 从根部门递归拉取部门架构，落库 attendance_dept 表，
     * 并回填 attendance_user 表的 dept_id/dept_name
     * 部门架构变动频率低，每天一次足够；幂等可重跑
     */
    @Scheduled(cron = "0 13 10 * * ?", zone = "Asia/Shanghai")
    public void syncDepartments() {
        LOG.info("部门架构同步开始: {}", new java.util.Date());
        try {
            int count = attendanceDeptService.syncDepartments();
            LOG.info("部门架构同步结束: 部门数={}", count);
        } catch (Exception e) {
            LOG.error("部门架构同步失败: {}", e.getMessage(), e);
        }
    }

    /**
     * Cron表达式任务（每天9点）
     * 格式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Shanghai")
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

