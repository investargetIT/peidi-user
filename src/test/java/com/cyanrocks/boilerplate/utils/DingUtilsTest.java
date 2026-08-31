//package com.cyanrocks.boilerplate.utils;
//
//import cn.hutool.core.collection.CollectionUtil;
//import cn.hutool.json.JSONObject;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.cyanrocks.boilerplate.dao.entity.User;
//import com.cyanrocks.boilerplate.dao.entity.UserOa;
//import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
//import com.cyanrocks.boilerplate.scheduled.ScheduledTasks;
//import com.cyanrocks.boilerplate.utils.http.HttpClientService;
//import com.cyanrocks.boilerplate.utils.http.HttpResponseContent;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//
//import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static redis.clients.jedis.Protocol.Keyword.LOG;
//
///**
// * DingUtils 测试类
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class DingUtilsTest {
//    private static final Logger LOG = LoggerFactory.getLogger(DingUtilsTest.class);
//    @Mock
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Mock
//    private ValueOperations<String, String> valueOperations;
//
//    @Mock
//    private HttpClientService httpClientService;
//    @Autowired
//    private UserMapper userMapper;
//
//    @InjectMocks
//    private DingUtils dingUtils;
//    @Autowired
//    private ScheduledTasks tasks;
//    @Autowired
//    private OaUtils oaUtils;
//
//    private static final String REDIS_KEY = "ding:token";
//    private static final String TEST_TOKEN = "test_access_token_123";
//    private static final String TEST_USER_ID = "user123";
//
//    @Test
//    public void updateUser() {
////        oaUtils.getoken("https://oa.peidibrand.com:4433");
////        oaUtils.restfulTest(100);
//
//
//
//        oaUtils.getoken("https://oa.peidibrand.com:4433");
//        //一业一百条一共100*100=10000条数据
//        for (int i = 0; i < 101; i++) {
//            //获取到状态是4.解聘 5.离职 6.退休 7.无效
//            List<UserOa> oaUserInfo = oaUtils.getOaUserInfo(i + 1);
//            for (UserOa userOa : oaUserInfo) {
//                String lastname = userOa.getLastname();
//                List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery().eq(User::getUsername, lastname));
//
//                // 1. 如果数组不为空且数组长度等于1，且oa_delete!=1的情况下修改用户状态为离职
//                if (CollectionUtil.isNotEmpty(users) && users.size() == 1) {
//                    User user = users.get(0);
//                    if (StringUtils.isNotBlank(user.getDingId())) {
//                        //我们自己组织的人不从温州oa同步
//                        LOG.info("我们自己组织的人不从温州oa同步");
//                        continue;
//                    }
//                    if (user.getOaDelete() == null || !user.getOaDelete()) {
//                        user.setOaDelete(true);
////                        userMapper.updateById(user);
//                        // 从Redis中移除离职用户
////                        stringRedisTemplate.opsForSet().remove(USER_REDIS_KEY, user.getDingId());
//                        LOG.info("OA离职用户处理成功: userId={}, username={}, dingId={},status{}", user.getId(), user.getUsername(), user.getDingId(),userOa.getStatus());
//                    }
//                }
//                // 2. 如果数组长度大于等于2的情况下，循环数组，增加电话为搜索条件继续查询，直到查询到唯一用户时才修改离职状态
//                else if (users != null && users.size() >= 2) {
//                    String oaMobile = userOa.getMobile();
//                    if (StringUtils.isNotBlank(oaMobile)) {
//                        // 使用姓名和电话双重条件查询唯一用户
//                        List<User> filteredUsers = userMapper.selectList(
//                                Wrappers.<User>lambdaQuery()
//                                        .eq(User::getUsername, lastname)
//                                        .eq(User::getMobile, oaMobile)
//                        );
//                        if (filteredUsers.size() == 1) {
//                            User user = filteredUsers.get(0);
//                            if (user.getOaDelete() == null || !user.getOaDelete()) {
//                                user.setOaDelete(true);
////                                userMapper.updateById(user);
//                                // 从Redis中移除离职用户
////                                stringRedisTemplate.opsForSet().remove(USER_REDIS_KEY, user.getDingId());
//                                LOG.info("OA离职用户处理成功(通过电话匹配): userId={}, username={}, mobile={}, dingId={}",
//                                        user.getId(), user.getUsername(), user.getMobile(), user.getDingId());
//                            }
//                        } else {
//                            LOG.warn("OA离职用户处理失败: 姓名为{}且电话为{}的用户不唯一或不存在, 查询到{}条记录",
//                                    lastname, oaMobile, filteredUsers.size());
//                        }
//                    } else {
//                        LOG.warn("OA离职用户处理失败: 姓名为{}存在多个用户({}个), 但OA电话为空, 无法唯一确定",
//                                lastname, users.size());
//                    }
//                }
//            }
//
//        }
//        System.out.println();
//    }
//
//    @Before
//    public void setUp() {
//        // Mock Redis操作
//        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(valueOperations.get(REDIS_KEY)).thenReturn(TEST_TOKEN);
//    }
//
//    /**
//     * 测试正常获取用户信息
//     */
//    @Test
//    public void testGetUserinfoByUserid_Success() throws Exception {
//        // 模拟成功响应
//        String successResponse = "{\"errcode\":0,\"errmsg\":\"ok\",\"result\":{\"userid\":\"user123\",\"name\":\"张三\"}}";
//        HttpResponseContent mockResponse = new HttpResponseContent();
//        mockResponse.setStatusCode(200);
//        mockResponse.setContent(successResponse);
//
//        when(httpClientService.doPost(anyString(), any(), anyString(), any())).thenReturn(mockResponse);
//
//        // 执行测试
//        JSONObject result = dingUtils.getUserinfoByUserid(TEST_USER_ID);
//
//        // 验证结果
//        assertNotNull("用户信息不应为null", result);
//        assertEquals("用户ID应该匹配", "user123", result.getStr("userid"));
//        assertEquals("用户名应该匹配", "张三", result.getStr("name"));
//    }
//
//    /**
//     * 测试用户不存在（已离职）- errcode 60121
//     */
//    @Test
//    public void testGetUserinfoByUserid_UserNotFound() throws Exception {
//        // 模拟用户不存在响应
//        String notFoundResponse = "{\"errcode\":60121,\"errmsg\":\"用户不存在\"}";
//        HttpResponseContent mockResponse = new HttpResponseContent();
//        mockResponse.setStatusCode(200);
//        mockResponse.setContent(notFoundResponse);
//
//        when(httpClientService.doPost(anyString(), any(), anyString(), any())).thenReturn(mockResponse);
//
//        // 执行测试
//        JSONObject result = dingUtils.getUserinfoByUserid(TEST_USER_ID);
//
//        // 验证结果 - 用户不存在应返回null
//        assertNull("用户不存在时应返回null", result);
//    }
//
//    /**
//     * 测试Token过期后重试成功 - errcode 42001
//     */
//    @Test
//    public void testGetUserinfoByUserid_TokenExpiredRetrySuccess() throws Exception {
//        // 第一次调用返回Token过期
//        String expiredResponse = "{\"errcode\":42001,\"errmsg\":\"access_token过期\"}";
//        HttpResponseContent expiredMockResponse = new HttpResponseContent();
//        expiredMockResponse.setStatusCode(200);
//        expiredMockResponse.setContent(expiredResponse);
//
//        // 第二次调用返回成功
//        String successResponse = "{\"errcode\":0,\"errmsg\":\"ok\",\"result\":{\"userid\":\"user123\",\"name\":\"李四\"}}";
//        HttpResponseContent successMockResponse = new HttpResponseContent();
//        successMockResponse.setStatusCode(200);
//        successMockResponse.setContent(successResponse);
//
//        when(httpClientService.doPost(anyString(), any(), anyString(), any()))
//                .thenReturn(expiredMockResponse)
//                .thenReturn(successMockResponse);
//
//        // 执行测试
//        JSONObject result = dingUtils.getUserinfoByUserid(TEST_USER_ID);
//
//        // 验证结果 - Token过期后重试成功
//        assertNotNull("重试后应返回用户信息", result);
//        assertEquals("用户名应该匹配", "李四", result.getStr("name"));
//
//        // 验证调用了两次
//        verify(httpClientService, times(2)).doPost(anyString(), any(), anyString(), any());
//        // 验证删除了过期的Token
//        verify(stringRedisTemplate).delete(REDIS_KEY);
//    }
//
//    /**
//     * 测试Token过期（errcode 40014）
//     */
//    @Test
//    public void testGetUserinfoByUserid_TokenInvalid() throws Exception {
//        // 模拟Token无效响应
//        String invalidResponse = "{\"errcode\":40014,\"errmsg\":\"不合法的access_token\"}";
//        HttpResponseContent mockResponse = new HttpResponseContent();
//        mockResponse.setStatusCode(200);
//        mockResponse.setContent(invalidResponse);
//
//        // 重试后也返回Token无效
//        when(httpClientService.doPost(anyString(), any(), anyString(), any())).thenReturn(mockResponse);
//
//        // 执行测试并验证抛出异常
//        try {
//            dingUtils.getUserinfoByUserid(TEST_USER_ID);
//            fail("应该抛出RuntimeException");
//        } catch (RuntimeException e) {
//            assertTrue("异常信息应包含errcode", e.getMessage().contains("errcode=40014"));
//        }
//    }
//
//    /**
//     * 测试网络异常
//     */
//    @Test
//    public void testGetUserinfoByUserid_NetworkException() throws Exception {
//        // 模拟网络异常
//        when(httpClientService.doPost(anyString(), any(), anyString(), any()))
//                .thenThrow(new RuntimeException("网络连接超时"));
//
//        // 执行测试并验证抛出异常
//        try {
//            dingUtils.getUserinfoByUserid(TEST_USER_ID);
//            fail("应该抛出RuntimeException");
//        } catch (RuntimeException e) {
//            assertTrue("异常信息应包含网络异常", e.getMessage().contains("网络异常"));
//        }
//    }
//
//    /**
//     * 测试其他API错误
//     */
//    @Test
//    public void testGetUserinfoByUserid_OtherApiError() throws Exception {
//        // 模拟其他错误响应
//        String errorResponse = "{\"errcode\":50000,\"errmsg\":\"系统繁忙\"}";
//        HttpResponseContent mockResponse = new HttpResponseContent();
//        mockResponse.setStatusCode(200);
//        mockResponse.setContent(errorResponse);
//
//        when(httpClientService.doPost(anyString(), any(), anyString(), any())).thenReturn(mockResponse);
//
//        // 执行测试并验证抛出异常
//        try {
//            dingUtils.getUserinfoByUserid(TEST_USER_ID);
//            fail("应该抛出RuntimeException");
//        } catch (RuntimeException e) {
//            assertTrue("异常信息应包含errcode", e.getMessage().contains("errcode=50000"));
//        }
//    }
//
//    /**
//     * 测试HTTP状态码非200
//     */
//    @Test
//    public void testGetUserinfoByUserid_HttpError() throws Exception {
//        // 模拟HTTP错误
//        HttpResponseContent mockResponse = new HttpResponseContent();
//        mockResponse.setStatusCode(500);
//        mockResponse.setContent("{\"errcode\":0}");
//
//        when(httpClientService.doPost(anyString(), any(), anyString(), any())).thenReturn(mockResponse);
//
//        // 执行测试并验证抛出异常
//        try {
//            dingUtils.getUserinfoByUserid(TEST_USER_ID);
//            fail("应该抛出RuntimeException");
//        } catch (RuntimeException e) {
//            assertTrue("异常信息应包含HTTP错误", e.getMessage().contains("HTTP错误"));
//        }
//    }
//}
