package com.cyanrocks.boilerplate.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest;
import com.aliyun.dingtalktodo_1_0.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.cyanrocks.boilerplate.dao.entity.User;
import com.cyanrocks.boilerplate.dao.mapper.UserMapper;
import com.cyanrocks.boilerplate.exception.BusinessException;
import com.cyanrocks.boilerplate.utils.http.HttpClientService;
import com.cyanrocks.boilerplate.utils.http.HttpResponseContent;
import com.cyanrocks.boilerplate.utils.http.HttpTimeoutConfig;
import com.cyanrocks.boilerplate.utils.http.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Formatter;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.List;

/**
 * @Author wjq
 * @Date 2024/10/15 15:06
 */
@Component
public class DingUtils {

    @Value("${dingding.appkey}")
    private String DingDing_APPKEY;

    @Value("${dingding.appsecret}")
    private String DingDing_APPSECRET;

    @Value("${pm.robot.id}")
    private String PM_ROBOT_ID;

    @Value("${union.id}")
    private String UNION_ID;


    private static final String REDIS_KEY = "ding:token";
    private static final String REDIS_OAUTH2_KEY = "ding:oauth2token";
    private static final String REDIS_JSAPI_TICKET = "ding:jsapiTicket";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private HttpClientService httpClientService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SmsUtils smsUtils;

    public String getUseridByCode(String code) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("code", code);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        if (200 != content.getStatusCode() || 0 != contentJson.getInt("errcode")) {
            return null;
        }
        return contentJson.getJSONObject("result").getStr("userid");
    }

    /**
     * 获取钉钉用户信息
     * @param userId 钉钉用户ID
     * @return 用户信息JSON对象，用户不存在返回null，API调用失败抛出异常
     */
    public JSONObject getUserinfoByUserid(String userId) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/v2/user/get?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("userid", userId);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取用户信息网络异常: " + e.getMessage());
            throw new RuntimeException("获取钉钉用户信息网络异常: " + e.getMessage(), e);
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        int errcode = contentJson.getInt("errcode");

        // Token过期，尝试刷新后重试一次
        if (errcode == 40014 || errcode == 42001) {
            System.out.println("Token已过期，尝试刷新Token重试");
            stringRedisTemplate.delete(REDIS_KEY);
            token = this.getToken();
            url = "https://oapi.dingtalk.com/topapi/v2/user/get?access_token=" + token;
            try {
                content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                        HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
            } catch (Exception e) {
                System.out.println("重试获取用户信息网络异常: " + e.getMessage());
                throw new RuntimeException("获取钉钉用户信息网络异常: " + e.getMessage(), e);
            }
            contentJson = JSONUtil.parseObj(content.getContent());
            errcode = contentJson.getInt("errcode");
        }

        if (200 != content.getStatusCode()) {
            throw new RuntimeException("获取钉钉用户信息HTTP错误: " + content.getStatusCode());
        }

        // 用户不存在（已离职或被删除），返回null
        if (errcode == 60121) {
            System.out.println("用户不存在(已离职): userId=" + userId);
            return null;
        }

        // 其他错误码，抛出异常
        if (errcode != 0) {
            throw new RuntimeException("获取钉钉用户信息失败: errcode=" + errcode + ", errmsg=" + contentJson.getStr("errmsg"));
        }

        return contentJson.getJSONObject("result");
    }

    public JSONObject getDingParentbyuser(String userId) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/v2/department/listparentbyuser?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("userid", userId);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        if (200 != content.getStatusCode() || 0 != contentJson.getInt("errcode")) {
            return null;
        }
        return contentJson.getJSONObject("result");
    }

    public JSONObject getDingDepartment(Long deptId) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/v2/department/get?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dept_id", deptId);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        if (200 != content.getStatusCode() || 0 != contentJson.getInt("errcode")) {
            return null;
        }
        return contentJson.getJSONObject("result");
    }

    public JSONObject getDingJsapi(String nonceStr, String url) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
        if (null == token) {
            token = this.getOauth2Token();
        }
        String jsapiTicket = stringRedisTemplate.opsForValue().get(REDIS_JSAPI_TICKET);
        if (null == jsapiTicket) {
            jsapiTicket = this.getJsapiTicket(token);
        }
        JSONObject result = new JSONObject();
        long timeStamp = System.currentTimeMillis();
        result.set("timeStamp", timeStamp);
        result.set("sign", this.sign(jsapiTicket, nonceStr, timeStamp, url));
        return result;
    }

    private String sign(String jsticket, String nonceStr, long timeStamp, String url) {
        try {
            String plain = "jsapi_ticket=" + jsticket + "&noncestr=" + nonceStr + "&timestamp=" + String.valueOf(timeStamp)
                    + "&url=" + decodeUrl(url);
            MessageDigest sha1 = MessageDigest.getInstance("SHA-256");
            sha1.reset();
            sha1.update(plain.getBytes("UTF-8"));
            return byteToHex(sha1.digest());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ;
        }

        return null;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String decodeUrl(String url) throws Exception {
        URL urler = new URL(url);
        StringBuilder urlBuffer = new StringBuilder();
        urlBuffer.append(urler.getProtocol());
        urlBuffer.append(":");
        if (urler.getAuthority() != null && urler.getAuthority().length() > 0) {
            urlBuffer.append("//");
            urlBuffer.append(urler.getAuthority());
        }
        if (urler.getPath() != null) {
            urlBuffer.append(urler.getPath());
        }
        if (urler.getQuery() != null) {
            urlBuffer.append('?');
            urlBuffer.append(URLDecoder.decode(urler.getQuery(), "utf-8"));
        }
        return urlBuffer.toString();
    }

    private String getToken() {
        String url = "https://oapi.dingtalk.com/gettoken?appkey=" + DingDing_APPKEY + "&appsecret=" + DingDing_APPSECRET;
        HttpResponseContent content;
        try {
            content = httpClientService.doGet(url, null, null,
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        if (200 != content.getStatusCode() || 0 != contentJson.getInt("errcode")) {
            throw new RuntimeException(contentJson.getStr("errmsg"));
        }
        String token = contentJson.getStr("access_token");
        stringRedisTemplate.opsForValue().set(REDIS_KEY, token);
        stringRedisTemplate.expire(REDIS_KEY, Duration.ofSeconds(7200));
        return token;
    }

    private String getOauth2Token() {
        try {
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalkoauth2_1_0.Client client = new com.aliyun.dingtalkoauth2_1_0.Client(config);
            com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest getAccessTokenRequest = new com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest()
                    .setAppKey(DingDing_APPKEY)
                    .setAppSecret(DingDing_APPSECRET);
            String token = client.getAccessToken(getAccessTokenRequest).getBody().accessToken;
            stringRedisTemplate.opsForValue().set(REDIS_OAUTH2_KEY, token);
            stringRedisTemplate.expire(REDIS_OAUTH2_KEY, Duration.ofSeconds(7200));
            return token;
        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println("getOauth2Token error: " + err.code + " " + err.message);
            }

        }
        return null;
    }

    private String getJsapiTicket(String token) {
        try {
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalkoauth2_1_0.Client client = new com.aliyun.dingtalkoauth2_1_0.Client(config);
            com.aliyun.dingtalkoauth2_1_0.models.CreateJsapiTicketHeaders createJsapiTicketHeaders = new com.aliyun.dingtalkoauth2_1_0.models.CreateJsapiTicketHeaders();
            createJsapiTicketHeaders.xAcsDingtalkAccessToken = token;
            String jsapiTicket = client.createJsapiTicketWithOptions(createJsapiTicketHeaders, new RuntimeOptions()).getBody().jsapiTicket;
            stringRedisTemplate.opsForValue().set(REDIS_JSAPI_TICKET, jsapiTicket);
            stringRedisTemplate.expire(REDIS_JSAPI_TICKET, Duration.ofSeconds(7200));
            return jsapiTicket;
        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println("getJsapiTicket error: " + err.code + " " + err.message);
            }
        }
        return null;
    }

    public void sendPmMessage(Integer type, String message, Long taskId, String[] userList) {
        // 由于外包人员传过来的userId是我们的id，不是钉钉id，通过查询user表去除
        List<String> filteredUserList = new ArrayList<>();
        List<String> smsUserList = new ArrayList<>();
        for (String s : userList) {
            User user = userMapper.selectById(s);
            if (null == user) {
                filteredUserList.add(s);
            }else {
                if (message.contains("，您有一条新的任务需要处理，请尽快完成。")){
                    smsUtils.sentSmsPm(user.getMobile(),message.replace("，您有一条新的任务需要处理，请尽快完成。",""));
                }
            }
        }
        if (CollectionUtil.isEmpty(filteredUserList)){
            return;
        }
        BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest().setRobotCode(PM_ROBOT_ID)
                .setUserIds(filteredUserList);
        switch (type){
            case 1:{
                JSONObject param = new JSONObject();
                param.set("content",message);
                batchSendOTORequest.setMsgKey("sampleText").setMsgParam(param.toString());
                break;
            }
            case 2:{
                JSONObject param = new JSONObject();
                param.set("text",message);
                param.set("title","任务管理");
                param.set("singleTitle","查看详情");
                param.set("singleURL","dingtalk://dingtalkclient/action/openapp?corpid=dingfc722e531a4125b735c2f4657eb6378f&container_type=work_platform&app_id=0_应用agentid&redirect_type=jump&redirect_url=http%3A%2F%2Fpm.peidigroup.cn%2F%23%2Findex%3FdetailId%3D"+ taskId);
                batchSendOTORequest.setMsgKey("sampleActionCard").setMsgParam(param.toString());
                break;
            }
            default: throw new BusinessException(500,"错误类型");
        }
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        String token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
        if (null == token) {
            token = this.getOauth2Token();
        }

        try {
            com.aliyun.dingtalkrobot_1_0.Client client = new com.aliyun.dingtalkrobot_1_0.Client(config);
            BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
            batchSendOTOHeaders.xAcsDingtalkAccessToken = token;
            client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println(err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println(err.message);
            }

        }
    }

    /**
     * 分页获取考勤组下参与考勤人员的userId（单页）
     * 接口：/topapi/attendance/group/memberusers/list
     * @param groupId 考勤组ID
     * @param cursor 分页游标，第一页传0
     * @return result对象（含 result:userId数组、cursor、has_more），调用失败返回null
     */
    public JSONObject getAttendanceGroupUsers(Long groupId, Long cursor) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/attendance/group/memberusers/list?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("cursor", cursor != null ? cursor : 0L);
        jsonObject.set("op_user_id", "dd_dd");
        jsonObject.set("group_id", groupId);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取考勤组人员列表网络异常: " + e.getMessage());
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());

        // Token过期，尝试刷新后重试一次
        int errcode = contentJson.getInt("errcode");
        if (200 != content.getStatusCode() || errcode == 40014 || errcode == 42001) {
            System.out.println("Token已过期，尝试刷新Token重试");
            stringRedisTemplate.delete(REDIS_KEY);
            token = this.getToken();
            url = "https://oapi.dingtalk.com/topapi/attendance/group/memberusers/list?access_token=" + token;
            try {
                content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                        HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
            } catch (Exception e) {
                System.out.println("重试获取考勤组人员列表网络异常: " + e.getMessage());
                return null;
            }
            contentJson = JSONUtil.parseObj(content.getContent());
            errcode = contentJson.getInt("errcode");
        }

        if (200 != content.getStatusCode() || 0 != errcode) {
            System.out.println("获取考勤组人员列表失败: errcode=" + errcode + ", errmsg=" + contentJson.getStr("errmsg"));
            return null;
        }
        return contentJson.getJSONObject("result");
    }

    /**
     * 获取考勤组下所有参与考勤人员的userId（自动分页）
     * @param groupId 考勤组ID
     * @return 全部userId列表，调用失败返回null
     */
    public List<String> getAllAttendanceGroupUsers(Long groupId) {
        List<String> allUserIds = new ArrayList<>();
        Long cursor = 0L;
        // 防御性上限，正常一页即可取完
        for (int page = 0; page < 100; page++) {
            JSONObject result = getAttendanceGroupUsers(groupId, cursor);
            if (null == result) {
                return null;
            }
            List<String> userIds = result.getJSONArray("result").toList(String.class);
            allUserIds.addAll(userIds);
            if (!result.getBool("has_more", false)) {
                break;
            }
            cursor = result.getLong("cursor");
        }
        return allUserIds;
    }

    /**
     * 获取打卡结果（单页）
     * 接口：/attendance/list
     * @param workDateFrom 起始工作日，格式yyyy-MM-dd HH:mm:ss（与workDateTo相隔最多7天）
     * @param workDateTo 结束工作日，格式yyyy-MM-dd HH:mm:ss
     * @param userIdList 员工userId列表，最大50
     * @param offset 起始点，第一次传0
     * @param limit 条数，最大50
     * @return 完整响应体JSONObject（含recordresult数组、hasMore），调用失败返回null
     */
    public JSONObject getAttendanceRecords(String workDateFrom, String workDateTo, List<String> userIdList, Long offset, Long limit) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/attendance/list?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("workDateFrom", workDateFrom);
        jsonObject.set("workDateTo", workDateTo);
        jsonObject.set("userIdList", userIdList);
        jsonObject.set("offset", offset);
        jsonObject.set("limit", limit);
        jsonObject.set("isI18n", false);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取打卡结果网络异常: " + e.getMessage());
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());

        // Token过期，尝试刷新后重试一次
        int errcode = contentJson.getInt("errcode");
        if (200 != content.getStatusCode() || errcode == 40014 || errcode == 42001) {
            System.out.println("Token已过期，尝试刷新Token重试");
            stringRedisTemplate.delete(REDIS_KEY);
            token = this.getToken();
            url = "https://oapi.dingtalk.com/attendance/list?access_token=" + token;
            try {
                content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                        HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
            } catch (Exception e) {
                System.out.println("重试获取打卡结果网络异常: " + e.getMessage());
                return null;
            }
            contentJson = JSONUtil.parseObj(content.getContent());
            errcode = contentJson.getInt("errcode");
        }

        if (200 != content.getStatusCode() || 0 != errcode) {
            System.out.println("获取打卡结果失败: errcode=" + errcode + ", errmsg=" + contentJson.getStr("errmsg"));
            return null;
        }
        return contentJson;
    }

    /**
     * 获取请假状态（单页）
     * 接口：/topapi/attendance/getleavestatus
     * 工时看板 支线逻辑3（工时看板文档20260824.md 第9章）
     * @param userIdList 员工userId列表，逗号分隔，每次最多100个
     * @param startTimeMilli 查询开始时间（毫秒时间戳），与endTimeMilli相隔最多180天
     * @param endTimeMilli 查询结束时间（毫秒时间戳）
     * @param offset 起始点，第一次传0
     * @param size 单页条数，最大20
     * @return 完整响应体JSONObject（含result.leave_status数组、result.has_more），调用失败返回null
     */
    public JSONObject getLeaveStatus(String userIdList, Long startTimeMilli, Long endTimeMilli, Long offset, Long size) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/attendance/getleavestatus?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("userid_list", userIdList);
        jsonObject.set("start_time", startTimeMilli);
        jsonObject.set("end_time", endTimeMilli);
        jsonObject.set("offset", offset);
        jsonObject.set("size", size);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取请假状态网络异常: " + e.getMessage());
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());

        // Token过期，尝试刷新后重试一次
        int errcode = contentJson.getInt("errcode");
        if (200 != content.getStatusCode() || errcode == 40014 || errcode == 42001) {
            System.out.println("Token已过期，尝试刷新Token重试");
            stringRedisTemplate.delete(REDIS_KEY);
            token = this.getToken();
            url = "https://oapi.dingtalk.com/topapi/attendance/getleavestatus?access_token=" + token;
            try {
                content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                        HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
            } catch (Exception e) {
                System.out.println("重试获取请假状态网络异常: " + e.getMessage());
                return null;
            }
            contentJson = JSONUtil.parseObj(content.getContent());
            errcode = contentJson.getInt("errcode");
        }

        if (200 != content.getStatusCode() || 0 != errcode) {
            System.out.println("获取请假状态失败: errcode=" + errcode + ", errmsg=" + contentJson.getStr("errmsg"));
            return null;
        }
        return contentJson;
    }

    public String creatTodoTask(String userId){
        User user = userMapper.selectById(userId);
        JSONObject userInfo = getUserinfoByUserid(user.getDingId());
        String unionId = userInfo.getStr("unionid");

        String token = null;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(REDIS_OAUTH2_KEY))) {
            token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
        }
        if (null == token) {
            token = this.getOauth2Token();
        }
        CreateTodoTaskHeaders createTodoTaskHeaders = new CreateTodoTaskHeaders();
        createTodoTaskHeaders.xAcsDingtalkAccessToken = token;
        CreateTodoTaskRequest.CreateTodoTaskRequestNotifyConfigs notifyConfigs = new CreateTodoTaskRequest.CreateTodoTaskRequestNotifyConfigs()
                .setDingNotify("1");
        CreateTodoTaskRequest.CreateTodoTaskRequestContentFieldList contentFieldList0 = new CreateTodoTaskRequest.CreateTodoTaskRequestContentFieldList();
        CreateTodoTaskRequest.CreateTodoTaskRequestDetailUrl detailUrl = new CreateTodoTaskRequest.CreateTodoTaskRequestDetailUrl()
                .setAppUrl("https://eps.peidigroup.cn/#/task/index")
                .setPcUrl("https://eps.peidigroup.cn/#/task/index");
        CreateTodoTaskRequest createTodoTaskRequest = new CreateTodoTaskRequest()
                .setOperatorId(UNION_ID)//沈烨丽unionId
                .setSubject("新员工入职任务")
                .setCreatorId(UNION_ID)
                .setExecutorIds(java.util.Arrays.asList(
                        unionId
                ))
                .setParticipantIds(java.util.Arrays.asList(
                        unionId
                ))
                .setDetailUrl(detailUrl)
                .setContentFieldList(java.util.Arrays.asList(
                        contentFieldList0
                ))
                .setIsOnlyShowExecutor(true)
                .setPriority(30)
                .setNotifyConfigs(notifyConfigs);

        try {
            Config config = new Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalktodo_1_0.Client client = new com.aliyun.dingtalktodo_1_0.Client(config);
            CreateTodoTaskResponse res = client.createTodoTaskWithOptions(UNION_ID, createTodoTaskRequest, createTodoTaskHeaders, new RuntimeOptions());
            return res.getBody().getId();

        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
            }

        }
        return null;
    }

    public void deleteTodoTask(String taskId){
        String token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
        if (null == token) {
            token = this.getOauth2Token();
        }
        DeleteTodoTaskHeaders deleteTodoTaskHeaders = new DeleteTodoTaskHeaders();
        deleteTodoTaskHeaders.xAcsDingtalkAccessToken = token;
        DeleteTodoTaskRequest deleteTodoTaskRequest = new DeleteTodoTaskRequest()
                .setOperatorId(UNION_ID);
        try {
            Config config = new Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalktodo_1_0.Client client = new com.aliyun.dingtalktodo_1_0.Client(config);
            client.deleteTodoTaskWithOptions(UNION_ID, taskId, deleteTodoTaskRequest, deleteTodoTaskHeaders, new RuntimeOptions());
            System.out.println("删除待办任务成功: taskId=" + taskId);
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println("删除待办任务失败: " + err.code + " " + err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println("删除待办任务失败: " + err.code + " " + err.message);
            }

        }

    }

    /**
     * 获取部门用户列表（支持分页）
     * @param deptId 部门ID
     * @param cursor 分页游标，第一页传0
     * @param size 每页大小，最大100
     * @return 用户列表JSON对象
     */
    public JSONObject getDepartmentUsers(Long deptId, Long cursor, Integer size) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/v2/user/list?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dept_id", deptId);
        jsonObject.set("cursor", cursor != null ? cursor : 0L);
        jsonObject.set("size", size != null ? size : 100);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取部门用户列表失败: " + e.getMessage());
            return null;
        }
        System.out.println(content);
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        if (200 != content.getStatusCode() || 0 != contentJson.getInt("errcode")) {
            System.out.println("获取部门用户列表失败: " + contentJson.getStr("errmsg"));
            return null;
        }
        return contentJson.getJSONObject("result");
    }

    /**
     * 获取部门下所有用户（自动分页）
     * @param deptId 部门ID
     * @return 所有用户列表
     */


    /**
     * 创建钉钉待办任务（通用方法）
     * @param subject 任务主题
     * @param initiatorId 发起人id
     * @param executorUnionIds 执行人unionId列表
     * @param participantUnionIds 参与人unionId列表（可选）
     * @param appUrl App端详情链接
     * @param pcUrl PC端详情链接
     * @param priority 优先级：10(较低)、20(普通)、30(紧急)、40(非常紧急)
     * @param dueTime 截止时间戳（毫秒，可选）
     * @return 任务ID，创建失败返回null
     */
    public String createTodoTask(String initiatorId,String subject, List<String> executorUnionIds, List<String> participantUnionIds,
                                  String appUrl, String pcUrl, Integer priority, Long dueTime) {
        String token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
        if (null == token) {
            token = this.getOauth2Token();
        }
        if (null == token) {
            System.out.println("获取OAuth2Token失败");
            return null;
        }

        CreateTodoTaskHeaders createTodoTaskHeaders = new CreateTodoTaskHeaders();
        createTodoTaskHeaders.xAcsDingtalkAccessToken = token;

        CreateTodoTaskRequest.CreateTodoTaskRequestNotifyConfigs notifyConfigs = new CreateTodoTaskRequest.CreateTodoTaskRequestNotifyConfigs()
                .setDingNotify("1");

        CreateTodoTaskRequest.CreateTodoTaskRequestDetailUrl detailUrl = new CreateTodoTaskRequest.CreateTodoTaskRequestDetailUrl()
                .setAppUrl(appUrl)
                .setPcUrl(pcUrl);

        CreateTodoTaskRequest createTodoTaskRequest = new CreateTodoTaskRequest()
                .setOperatorId(initiatorId)
                .setSubject(subject)
                .setDescription("描述")
                .setCreatorId(initiatorId)
                .setExecutorIds(executorUnionIds)
                .setDetailUrl(detailUrl)
                .setIsOnlyShowExecutor(true)
                .setPriority(priority != null ? priority : 20)
                .setNotifyConfigs(notifyConfigs);

        if (participantUnionIds != null && !participantUnionIds.isEmpty()) {
            createTodoTaskRequest.setParticipantIds(participantUnionIds);
        }

        if (dueTime != null) {
            createTodoTaskRequest.setDueTime(dueTime);
        }

        try {
            Config config = new Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalktodo_1_0.Client client = new com.aliyun.dingtalktodo_1_0.Client(config);
            CreateTodoTaskResponse res = client.createTodoTaskWithOptions(UNION_ID, createTodoTaskRequest, createTodoTaskHeaders, new RuntimeOptions());
            System.out.println("创建待办任务成功: taskId=" + res.getBody().getId() + ", subject=" + subject);
            return res.getBody().getId();

        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println("创建待办任务失败: " + err.code + " " + err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                System.out.println("创建待办任务失败: " + err.code + " " + err.message);
            }

        }
        return null;
    }

    // ==================== 加班审批（工时看板 支线逻辑1）====================

    /**
     * 获取审批实例ID列表（新版API + OAuth2 token，自动nextToken翻页）
     * @param startTimeMilli 开始时间毫秒时间戳（按审批发起时间过滤）
     * @param endTimeMilli 结束时间毫秒时间戳（按审批发起时间过滤）
     * @param processCode 审批模板code
     * @return 审批实例ID列表（仅COMPLETED状态），调用失败返回null
     */
    public List<String> getProcessInstanceIds(Long startTimeMilli, Long endTimeMilli, String processCode) {
        try {
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalkworkflow_1_0.Client client = new com.aliyun.dingtalkworkflow_1_0.Client(config);
            List<String> allIds = new ArrayList<>();
            Long nextToken = 0L;
            // 防御性上限，防止死循环
            for (int page = 0; page < 100; page++) {
                com.aliyun.dingtalkworkflow_1_0.models.ListProcessInstanceIdsHeaders headers =
                        new com.aliyun.dingtalkworkflow_1_0.models.ListProcessInstanceIdsHeaders();
                headers.xAcsDingtalkAccessToken = getValidOauth2Token();
                com.aliyun.dingtalkworkflow_1_0.models.ListProcessInstanceIdsRequest request =
                        new com.aliyun.dingtalkworkflow_1_0.models.ListProcessInstanceIdsRequest()
                                .setStatuses(java.util.Arrays.asList("COMPLETED"))
                                .setStartTime(startTimeMilli)
                                .setEndTime(endTimeMilli)
                                .setProcessCode(processCode)
                                .setNextToken(nextToken)
                                .setMaxResults(20L);
                com.aliyun.dingtalkworkflow_1_0.models.ListProcessInstanceIdsResponseBody body =
                        client.listProcessInstanceIdsWithOptions(request, headers, new RuntimeOptions()).getBody();
                if (null == body || null == body.getResult()) {
                    break;
                }
                List<String> list = body.getResult().getList();
                if (CollectionUtil.isEmpty(list)) {
                    break;
                }
                allIds.addAll(list);
                // 无nextToken表示翻页结束
                String next = body.getResult().getNextToken();
                if (com.aliyun.teautil.Common.empty(next)) {
                    break;
                }
                try {
                    nextToken = Long.parseLong(next);
                } catch (NumberFormatException e) {
                    break;
                }
            }
            return allIds;
        } catch (Exception e) {
            System.out.println("获取审批实例ID列表异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取单个审批实例详情（新版API + OAuth2 token）
     * @param processInstanceId 审批实例ID
     * @return 审批实例详情JSON（含formComponentValues数组），调用失败返回null
     */
    public JSONObject getProcessInstanceDetail(String processInstanceId) {
        try {
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
            config.protocol = "https";
            config.regionId = "central";
            com.aliyun.dingtalkworkflow_1_0.Client client = new com.aliyun.dingtalkworkflow_1_0.Client(config);
            com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceHeaders headers =
                    new com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceHeaders();
            headers.xAcsDingtalkAccessToken = getValidOauth2Token();
            com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceRequest request =
                    new com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceRequest()
                            .setProcessInstanceId(processInstanceId);
            com.aliyun.dingtalkworkflow_1_0.models.GetProcessInstanceResponseBody body =
                    client.getProcessInstanceWithOptions(request, headers, new RuntimeOptions()).getBody();
            if (null == body || null == body.getResult()) {
                return null;
            }
            // 转成hutool JSONObject便于调用方解析
            return JSONUtil.parseObj(JSONUtil.toJsonStr(body.getResult()));
        } catch (Exception e) {
            System.out.println("获取审批实例详情异常: processInstanceId=" + processInstanceId + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取OAuth2 token（优先取Redis缓存，与todo任务同一token）
     */
    private String getValidOauth2Token() {
        String token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
        if (null == token) {
            token = this.getOauth2Token();
        }
        return token;
    }

    // ==================== 部门架构（工时看板 支线逻辑2）====================

    /** 钉钉QPS限流错误码（subcode=90002，所有应用共享约1200次/秒上限） */
    private static final int ERR_QPS_LIMIT = 88;
    /** 部门相关接口调用最小间隔（毫秒），将本应用QPS压到20以下，避免触发钉钉限流 */
    private static final long DEPT_API_INTERVAL_MS = 50;
    /** 部门相关接口QPS限流重试次数 */
    private static final int DEPT_API_MAX_RETRY = 4;

    /** QPS限流异常（可重试） */
    private static class DingQpsLimitException extends RuntimeException {
        DingQpsLimitException(String message) {
            super(message);
        }
    }

    /**
     * 部门相关接口全局限流：保证相邻两次调用间隔不小于 {@link #DEPT_API_INTERVAL_MS}
     */
    private static volatile long lastDeptApiTime = 0L;

    private static void throttleDeptApi() {
        long now = System.currentTimeMillis();
        long earliest = lastDeptApiTime + DEPT_API_INTERVAL_MS;
        if (now < earliest) {
            try {
                Thread.sleep(earliest - now);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastDeptApiTime = System.currentTimeMillis();
    }

    /**
     * 获取指定部门的直属子部门列表（不含子部门的子部门，需递归调用）
     * 接口：/topapi/v2/department/listsub
     * 内置限流（间隔{@value #DEPT_API_INTERVAL_MS}ms）与QPS限流重试（errcode=88）
     * @param parentDeptId 父部门ID，根部门传1
     * @return 子部门列表（每项含dept_id/name/parent_id），调用失败返回null，无子部门返回空列表
     */
    public List<JSONObject> getSubDepartments(Long parentDeptId) {
        for (int attempt = 1; attempt <= DEPT_API_MAX_RETRY; attempt++) {
            try {
                return doGetSubDepartments(parentDeptId);
            } catch (DingQpsLimitException e) {
                if (attempt == DEPT_API_MAX_RETRY) {
                    System.out.println("获取子部门列表失败(重试" + (attempt - 1) + "次后仍被限流): " + e.getMessage());
                    return null;
                }
                long backoff = attempt * 1000L;
                System.out.println("获取子部门列表触发钉钉QPS限流, " + backoff + "ms后重试(" + attempt + "/"
                        + (DEPT_API_MAX_RETRY - 1) + "): " + e.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    private List<JSONObject> doGetSubDepartments(Long parentDeptId) {
        throttleDeptApi();
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/v2/department/listsub?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dept_id", parentDeptId);
        jsonObject.set("language", "zh_CN");
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取子部门列表网络异常: " + e.getMessage());
            return null;
        }
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());

        // Token过期，尝试刷新后重试一次
        int errcode = contentJson.getInt("errcode");
        if (200 != content.getStatusCode() || errcode == 40014 || errcode == 42001) {
            stringRedisTemplate.delete(REDIS_KEY);
            token = this.getToken();
            url = "https://oapi.dingtalk.com/topapi/v2/department/listsub?access_token=" + token;
            try {
                content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                        HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
            } catch (Exception e) {
                System.out.println("重试获取子部门列表网络异常: " + e.getMessage());
                return null;
            }
            contentJson = JSONUtil.parseObj(content.getContent());
            errcode = contentJson.getInt("errcode");
        }

        if (200 != content.getStatusCode() || 0 != errcode) {
            if (errcode == ERR_QPS_LIMIT) {
                throw new DingQpsLimitException("errcode=88, errmsg=" + contentJson.getStr("errmsg"));
            }
            System.out.println("获取子部门列表失败: errcode=" + errcode + ", errmsg=" + contentJson.getStr("errmsg"));
            return null;
        }
        cn.hutool.json.JSONArray result = contentJson.getJSONArray("result");
        if (null == result) {
            return new ArrayList<>();
        }
        List<JSONObject> departments = new ArrayList<>();
        for (Object obj : result) {
            departments.add((JSONObject) obj);
        }
        return departments;
    }

    /**
     * 获取部门下的直属员工userid列表（不含子部门员工，需用子部门ID再查）
     * 接口：/topapi/user/listid
     * 内置限流（间隔{@value #DEPT_API_INTERVAL_MS}ms）与QPS限流重试（errcode=88）
     * @param deptId 部门ID
     * @return 该部门直属员工的userid列表，调用失败返回null
     */
    public List<String> getDeptUserIds(Long deptId) {
        for (int attempt = 1; attempt <= DEPT_API_MAX_RETRY; attempt++) {
            try {
                return doGetDeptUserIds(deptId);
            } catch (DingQpsLimitException e) {
                if (attempt == DEPT_API_MAX_RETRY) {
                    System.out.println("获取部门员工列表失败(重试" + (attempt - 1) + "次后仍被限流): " + e.getMessage());
                    return null;
                }
                long backoff = attempt * 1000L;
                System.out.println("获取部门员工列表触发钉钉QPS限流, " + backoff + "ms后重试(" + attempt + "/"
                        + (DEPT_API_MAX_RETRY - 1) + "): " + e.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    private List<String> doGetDeptUserIds(Long deptId) {
        throttleDeptApi();
        String token = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if (null == token) {
            token = this.getToken();
        }
        String url = "https://oapi.dingtalk.com/topapi/user/listid?access_token=" + token;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dept_id", deptId);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println("获取部门员工列表网络异常: " + e.getMessage());
            return null;
        }
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());

        // Token过期，尝试刷新后重试一次
        int errcode = contentJson.getInt("errcode");
        if (200 != content.getStatusCode() || errcode == 40014 || errcode == 42001) {
            stringRedisTemplate.delete(REDIS_KEY);
            token = this.getToken();
            url = "https://oapi.dingtalk.com/topapi/user/listid?access_token=" + token;
            try {
                content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                        HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
            } catch (Exception e) {
                System.out.println("重试获取部门员工列表网络异常: " + e.getMessage());
                return null;
            }
            contentJson = JSONUtil.parseObj(content.getContent());
            errcode = contentJson.getInt("errcode");
        }

        if (200 != content.getStatusCode() || 0 != errcode) {
            if (errcode == ERR_QPS_LIMIT) {
                throw new DingQpsLimitException("errcode=88, errmsg=" + contentJson.getStr("errmsg"));
            }
            System.out.println("获取部门员工列表失败: errcode=" + errcode + ", errmsg=" + contentJson.getStr("errmsg"));
            return null;
        }
        JSONObject result = contentJson.getJSONObject("result");
        if (null == result || null == result.getJSONArray("userid_list")) {
            return new ArrayList<>();
        }
        return result.getJSONArray("userid_list").toList(String.class);
    }

}
