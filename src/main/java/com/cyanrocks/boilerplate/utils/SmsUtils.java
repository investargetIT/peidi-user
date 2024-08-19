package com.cyanrocks.boilerplate.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.cyanrocks.boilerplate.config.SmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;

/**
 * @Author wjq
 * @Date 2024/8/16 12:06
 */
@Component
public class SmsUtils {

    private static final String SIGN_NAME = "Peidi验证码";
    private static final String TEMPLATE_CODE = "SMS_465348269";

    @Autowired
    private SmsConfig smsConfig;

    public void sentSmsCode(String phoneNumber, String content) {
        //初始化账号Client
        Config config = new Config().setAccessKeyId(smsConfig.getAccessKeyId()).setAccessKeySecret(smsConfig.getAccessKeySecret());
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = smsConfig.getEndpoint();

        try {
            JSONObject param = new JSONObject();
            param.put("code", content);
            Client client = new Client(config);
            SendSmsRequest sendSmsRequest = new SendSmsRequest().setPhoneNumbers(phoneNumber).setSignName(SIGN_NAME)
                    .setTemplateCode(TEMPLATE_CODE).setTemplateParam(JSONUtil.toJsonStr(param));
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse response = client.sendSmsWithOptions(sendSmsRequest, new com.aliyun.teautil.models.RuntimeOptions());
            System.out.println(response);
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
        }
    }

}
