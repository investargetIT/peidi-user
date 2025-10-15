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

    private static final String DingDing_APPID = "";
    private static final String DingDing_APPKEY = "";
    private static final String DingDing_APPSECRET = "";
    private static final String PM_APPKEY = "";
    private static final String PM_APPSECRET = "";
    private static final String REDIS_KEY = "ding:token";
    private static final String REDIS_OAUTH2_KEY = "ding:oauth2token";
    private static final String REDIS_JSAPI_TICKET = "ding:jsapiTicket";
    private static final String PM_ROBOT_ID = "";
    private static final String UNION_ID = "";//沈烨丽unionId
//    private static final String UNION_ID = "";//王家琦unionId


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
                    .setAppKey(PM_APPKEY)
                    .setAppSecret(PM_APPSECRET);
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

    public String creatTodoTask(String userId){
        User user = userMapper.selectById(userId);
        JSONObject userInfo = getUserinfoByUserid(user.getDingId());
        String unionId = userInfo.getStr("unionid");
        String token = stringRedisTemplate.opsForValue().get(REDIS_OAUTH2_KEY);
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

    }

}
