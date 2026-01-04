package com.cyanrocks.boilerplate.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaConverter;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.TeaPair;
import com.aliyun.teaopenapi.models.Config;
import com.cyanrocks.boilerplate.config.EmailConfig;
import com.cyanrocks.boilerplate.config.SmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wjq
 * @Date 2024/8/16 12:06
 */
@Component
public class EmailUtils {

    @Autowired
    private EmailConfig emailConfig;
    /**
     * <b>description</b> :
     * <p>使用凭据初始化账号Client</p>
     * @return Client
     *
     * @throws Exception
     */
    public com.aliyun.teaopenapi.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(emailConfig.getAccessKeyId()).setAccessKeySecret(emailConfig.getAccessKeySecret());
        // Endpoint 请参考 https://api.aliyun.com/product/Dm
        config.endpoint = "dm.aliyuncs.com";
        return new com.aliyun.teaopenapi.Client(config);
    }

    /**
     * <b>description</b> :
     * <p>API 相关</p>
     * @return OpenApi.Params
     */
    public com.aliyun.teaopenapi.models.Params createApiInfo() throws Exception {
        com.aliyun.teaopenapi.models.Params params = new com.aliyun.teaopenapi.models.Params()
                // 接口名称
                .setAction("SingleSendMail")
                // 接口版本
                .setVersion("2015-11-23")
                // 接口协议
                .setProtocol("HTTPS")
                // 接口 HTTP 方法
                .setMethod("POST")
                .setAuthType("AK")
                .setStyle("RPC")
                // 接口 PATH
                .setPathname("/")
                // 接口请求体内容格式
                .setReqBodyType("formData")
                // 接口响应体内容格式
                .setBodyType("json");
        return params;
    }

    public void sentEmailCode(String email, String code) {
        try {
            com.aliyun.teaopenapi.Client client = this.createClient();
            com.aliyun.teaopenapi.models.Params params = this.createApiInfo();
            // body params
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("AccountName", "peidigroup@email.peidigroup.cn");
            body.put("AddressType", 1);
            body.put("ReplyToAddress", false);
            body.put("ToAddress", email);
            body.put("Subject", "aa");
            body.put("HtmlBody", "bb");
            body.put("TextBody", "cc");
            body.put("Template", "{\"TemplateData\":{\"code\":\""+code+"\"},\"TemplateId\":\""+emailConfig.getTemplateCode()+"\"}");
            // runtime options
            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
            com.aliyun.teaopenapi.models.OpenApiRequest request = new com.aliyun.teaopenapi.models.OpenApiRequest()
                    .setBody(body);
            // 复制代码运行请自行打印 API 的返回值
            // 返回值实际为 Map 类型，可从 Map 中获得三类数据：响应体 body、响应头 headers、HTTP 返回的状态码 statusCode。
            client.callApi(params, request, runtime);
        }catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
        }

    }


}
