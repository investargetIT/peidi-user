package com.cyanrocks.boilerplate.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cyanrocks.boilerplate.dao.entity.UserOa;
import com.cyanrocks.boilerplate.dao.mapper.UserOaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author wjq
 * @Date 2025/8/19 10:55
 */
@Component
public class OaUtils {

    @Autowired
    private UserOaMapper userOaMapper;

    @Value("${oa.secret}")
    private String SECRET;

    @Value("${oa.spk}")
    private String SPK;

    /**
     * ecology系统发放的授权许可证(appid)
     */
    @Value("${oa.appid}")
    private String APPID;

    /**
     * 模拟缓存服务
     */
    private static final Map<String,String> SYSTEM_CACHE = new HashMap <>();

    /**
     * 第一步：
     *
     * 调用ecology注册接口,根据appid进行注册,将返回服务端公钥和Secret信息
     */
    public Map<String,Object> regist(String address){

        //获取当前系统RSA加密的公钥
        RSA rsa = new RSA();
        String publicKey = rsa.getPublicKeyBase64();
        String privateKey = rsa.getPrivateKeyBase64();

        // 客户端RSA私钥
        SYSTEM_CACHE.put("LOCAL_PRIVATE_KEY",privateKey);
        // 客户端RSA公钥
        SYSTEM_CACHE.put("LOCAL_PUBLIC_KEY",publicKey);

        //调用ECOLOGY系统接口进行注册
        String data = HttpRequest.post(address + "/api/ec/dev/auth/regist")
                .header("appid",APPID)
                .header("cpk",publicKey)
                .timeout(2000)
                .execute().body();

        // 打印ECOLOGY响应信息
        System.out.println("testRegist()："+data);
        Map<String,Object> datas = JSONUtil.parseObj(data);

        //ECOLOGY返回的系统公钥
        SYSTEM_CACHE.put("SERVER_PUBLIC_KEY",StrUtil.nullToEmpty((String)datas.get("spk")));
        //ECOLOGY返回的系统密钥
        SYSTEM_CACHE.put("SERVER_SECRET",StrUtil.nullToEmpty((String)datas.get("secrit")));
        return datas;
    }



    /**
     * 第二步：
     *
     * 通过第一步中注册系统返回信息进行获取token信息
     */
    public Map<String,Object> getoken(String address){
//         从系统缓存或者数据库中获取ECOLOGY系统公钥和Secret信息
        String secret = SYSTEM_CACHE.get("SERVER_SECRET");
        String spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");
//         如果为空,说明还未进行注册,调用注册接口进行注册认证与数据更新
        if (Objects.isNull(secret)||Objects.isNull(spk)){
            regist(address);
            // 重新获取最新ECOLOGY系统公钥和Secret信息
            secret = SYSTEM_CACHE.get("SERVER_SECRET");
            spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");
        }

        // 公钥加密,所以RSA对象私钥为null
        RSA rsa = new RSA(null,SPK);
        //对秘钥进行加密传输，防止篡改数据
        String encryptSecret = rsa.encryptBase64(SECRET,CharsetUtil.CHARSET_UTF_8,KeyType.PublicKey);

        //调用ECOLOGY系统接口进行注册
        String data = HttpRequest.post(address+ "/api/ec/dev/auth/applytoken")
                .header("appid",APPID)
                .header("secret",encryptSecret)
                .header("time","3600")
                .execute().body();

        System.out.println("testGetoken()："+data);
        Map<String,Object> datas = JSONUtil.parseObj(data);

        //ECOLOGY返回的token
        SYSTEM_CACHE.put("SERVER_TOKEN",StrUtil.nullToEmpty((String)datas.get("token")));

        return datas;
    }

    /**
     * 第三步：
     *
     * 调用ecology系统的rest接口，请求头部带上token和用户标识认证信息
     *
     * @param address ecology系统地址
     * @param api rest api 接口地址(该测试代码仅支持GET请求)
     * @param jsonParams 请求参数json串
     *
     * 注意：ECOLOGY系统所有POST接口调用请求头请设置 "Content-Type","application/x-www-form-urlencoded; charset=utf-8"
     */
    public String restful(String address, String api, String jsonParams){

        //ECOLOGY返回的token
        String token= SYSTEM_CACHE.get("SERVER_TOKEN");
        if (StrUtil.isEmpty(token)){
            token = (String) getoken(address).get("token");
        }

        //封装请求头参数
        RSA rsa = new RSA(null,SPK);
        //对用户信息进行加密传输,暂仅支持传输OA用户ID
        String encryptUserid = rsa.encryptBase64("1",CharsetUtil.CHARSET_UTF_8,KeyType.PublicKey);

        //调用ECOLOGY系统接口
        String data = HttpRequest.get(address + api)
                .header("appid",APPID)
                .header("token",token)
                .header("userid",encryptUserid)
                .body(jsonParams)
                .execute().body();
        if (data.contains("token不存在或者超时")){
            token = (String) getoken(address).get("token");
            //封装请求头参数
            rsa = new RSA(null,SPK);
            //对用户信息进行加密传输,暂仅支持传输OA用户ID
            encryptUserid = rsa.encryptBase64("1",CharsetUtil.CHARSET_UTF_8,KeyType.PublicKey);

            //调用ECOLOGY系统接口
            data = HttpRequest.get(address + api)
                    .header("appid",APPID)
                    .header("token",token)
                    .header("userid",encryptUserid)
                    .body(jsonParams)
                    .execute().body();
        }
        return data;
    }

    public List<UserOa> getOaUserInfo(Integer page) {
            JSONObject jsonObject = new JSONObject();
            JSONObject params = new JSONObject();
            List<UserOa> userOas= new ArrayList<>();
            params.set("pagesize",100);
            params.set("curpage",page);
            jsonObject.set("params",params);
            //调用ECOLOGY系统接口
            String data = HttpRequest.post("https://oa.peidibrand.com:4433/api/hrm/resful/getHrmUserInfoWithPage")
                    .header("appid",APPID)
                    .header("token",SYSTEM_CACHE.get("SERVER_TOKEN"))
                    .header("userid","ZTyPa3GTGqfOQnrdt1Ci0kcjTh+J6g+5oFmWEP/E8BKO2ZyYIWqBSBMfEqHzCoHdkri4jmg/7K+N2TMgkQwlX/Itj6lHYfwH+uvGTF5tZg/BZfRgAZXLNPDw+kYDcjINy5GyGLDWGLZn4XAk7hGQvR9ZWDv/2eIKABK5ZP3UpljWYy+T8j19TsNbVBMuVW1Mq0s3HFhKp/NZkAMBxmYHSXuuC/hQIy5Mml2UJGmz3t54spgyUPgAkoxpP9KdWK52HJ3KqLEbmnHHzZ7Fm1UZFIeXWYeledRsSJs50LjzhQxe1F0KJNoDi5aannb/5mGiwSIH1pJDBAtQGmrmgNXkDA==")
                    .body(JSONUtil.toJsonStr(jsonObject))
                    .execute().body();
            JSONArray dataList = JSONUtil.parseObj(data).getJSONObject("data").getJSONArray("dataList");
            if (CollectionUtil.isNotEmpty(dataList)){
                for (int i=0;i<dataList.size();i++){
                    JSONObject object = dataList.getJSONObject(i);
                    String status = object.getStr("status");
                    //todo 只有status=4.解聘 5.离职 6.退休 7.无效 才处理
                    if (!Arrays.asList("4", "5", "6", "7").contains(status)) continue;
                    UserOa userOa = new UserOa();
                    if (object.getStr("lastname").equals("王琳")) {
                        System.out.println();
                    }
                    userOa.setCompanystartdate(object.getStr("companystartdate"));
                    userOa.setTempresidentnumber(object.getStr("tempresidentnumber"));
                    userOa.setCreatedate(object.getStr("createdate"));
                    userOa.setLanguage(object.getStr("language"));
                    userOa.setWorkstartdate(object.getStr("workstartdate"));
                    userOa.setSubcompanyid1(object.getStr("subcompanyid1"));
                    userOa.setSubcompanyname(object.getStr("subcompanyname"));
                    userOa.setJoblevel(object.getStr("joblevel"));
                    userOa.setStartdate(object.getStr("startdate"));
                    userOa.setPassword(object.getStr("password"));
                    userOa.setSubcompanycode(object.getStr("subcompanycode"));
                    userOa.setJobactivitydesc(object.getStr("jobactivitydesc"));
                    userOa.setBememberdate(object.getStr("bememberdate"));
                    userOa.setModified(object.getStr("modified"));
                    userOa.setOaId(object.getStr("id"));
                    userOa.setMobilecall(object.getStr("mobilecall"));
                    userOa.setNativeplace(object.getStr("nativeplace"));
                    userOa.setCertificatenum(object.getStr("certificatenum"));
                    userOa.setHeight(object.getStr("height"));
                    userOa.setLoginid(object.getStr("loginid"));
                    userOa.setCreated(object.getStr("created"));
                    userOa.setDegree(object.getStr("degree"));
                    userOa.setBepartydate(object.getStr("bepartydate"));
                    userOa.setWeight(object.getStr("weight"));
                    userOa.setTelephone(object.getStr("telephone"));
                    userOa.setResidentplace(object.getStr("residentplace"));
                    userOa.setLastname(object.getStr("lastname"));
                    userOa.setHealthinfo(object.getStr("healthinfo"));
                    userOa.setEnddate(object.getStr("enddate"));
                    userOa.setMaritalstatus(object.getStr("maritalstatus"));
                    userOa.setDepartmentname(object.getStr("departmentname"));
                    userOa.setFolk(object.getStr("folk"));
                    userOa.setStatus(status);
                    userOa.setBirthday(object.getStr("birthday"));
                    userOa.setAccounttype(object.getStr("accounttype"));
                    userOa.setJobcall(object.getStr("jobcall"));
                    userOa.setManagerid(object.getStr("managerid"));
                    userOa.setAssistantid(object.getStr("assistantid"));
                    userOa.setDepartmentcode(object.getStr("departmentcode"));
                    userOa.setBelongto(object.getStr("belongto"));
                    userOa.setEmail(object.getStr("email"));
                    userOa.setSeclevel(object.getStr("seclevel"));
                    userOa.setPolicy(object.getStr("policy"));
                    userOa.setJobtitle(object.getStr("jobtitle"));
                    userOa.setWorkcode(object.getStr("workcode"));
                    userOa.setSex(object.getStr("sex"));
                    userOa.setDepartmentid(object.getStr("departmentid"));
                    userOa.setHomeaddress(object.getStr("homeaddress"));
                    userOa.setMobile(object.getStr("mobile"));
                    userOa.setLastmoddate(object.getStr("lastmoddate"));
                    userOa.setEducationlevel(object.getStr("educationlevel"));
                    userOa.setIslabouunion(object.getStr("islabouunion"));
                    userOa.setLocationid(object.getStr("locationid"));
                    userOa.setRegresidentplace(object.getStr("regresidentplace"));
                    userOa.setDsporder(object.getStr("dsporder"));
                    userOas.add(userOa);
                }

        }
        return userOas;
    }

    public void restfulTest(Integer page){
        for (int curpage = 1;curpage <=page ;curpage++){
            JSONObject jsonObject = new JSONObject();
            JSONObject params = new JSONObject();
            params.set("pagesize",100);
            params.set("curpage",curpage);
            jsonObject.set("params",params);

            //调用ECOLOGY系统接口
            String data = HttpRequest.post("https://oa.peidibrand.com:4433/api/hrm/resful/getHrmUserInfoWithPage")
                    .header("appid",APPID)
                    .header("token","6faed885-fc46-4e45-94bd-ee40a3d83922")
                    .header("userid","ZTyPa3GTGqfOQnrdt1Ci0kcjTh+J6g+5oFmWEP/E8BKO2ZyYIWqBSBMfEqHzCoHdkri4jmg/7K+N2TMgkQwlX/Itj6lHYfwH+uvGTF5tZg/BZfRgAZXLNPDw+kYDcjINy5GyGLDWGLZn4XAk7hGQvR9ZWDv/2eIKABK5ZP3UpljWYy+T8j19TsNbVBMuVW1Mq0s3HFhKp/NZkAMBxmYHSXuuC/hQIy5Mml2UJGmz3t54spgyUPgAkoxpP9KdWK52HJ3KqLEbmnHHzZ7Fm1UZFIeXWYeledRsSJs50LjzhQxe1F0KJNoDi5aannb/5mGiwSIH1pJDBAtQGmrmgNXkDA==")
                    .body(JSONUtil.toJsonStr(jsonObject))
                    .execute().body();
            System.out.println("testRestful()："+data);
            JSONArray dataList = JSONUtil.parseObj(data).getJSONObject("data").getJSONArray("dataList");
            if (CollectionUtil.isNotEmpty(dataList)){
                for (int i=0;i<dataList.size();i++){
                    UserOa userOa = new UserOa();
                    JSONObject object = dataList.getJSONObject(i);
                    userOa.setCompanystartdate(object.getStr("companystartdate"));
                    userOa.setTempresidentnumber(object.getStr("tempresidentnumber"));
                    userOa.setCreatedate(object.getStr("createdate"));
                    userOa.setLanguage(object.getStr("language"));
                    userOa.setWorkstartdate(object.getStr("workstartdate"));
                    userOa.setSubcompanyid1(object.getStr("subcompanyid1"));
                    userOa.setSubcompanyname(object.getStr("subcompanyname"));
                    userOa.setJoblevel(object.getStr("joblevel"));
                    userOa.setStartdate(object.getStr("startdate"));
                    userOa.setPassword(object.getStr("password"));
                    userOa.setSubcompanycode(object.getStr("subcompanycode"));
                    userOa.setJobactivitydesc(object.getStr("jobactivitydesc"));
                    userOa.setBememberdate(object.getStr("bememberdate"));
                    userOa.setModified(object.getStr("modified"));
                    userOa.setOaId(object.getStr("id"));
                    userOa.setMobilecall(object.getStr("mobilecall"));
                    userOa.setNativeplace(object.getStr("nativeplace"));
                    userOa.setCertificatenum(object.getStr("certificatenum"));
                    userOa.setHeight(object.getStr("height"));
                    userOa.setLoginid(object.getStr("loginid"));
                    userOa.setCreated(object.getStr("created"));
                    userOa.setDegree(object.getStr("degree"));
                    userOa.setBepartydate(object.getStr("bepartydate"));
                    userOa.setWeight(object.getStr("weight"));
                    userOa.setTelephone(object.getStr("telephone"));
                    userOa.setResidentplace(object.getStr("residentplace"));
                    userOa.setLastname(object.getStr("lastname"));
                    userOa.setHealthinfo(object.getStr("healthinfo"));
                    userOa.setEnddate(object.getStr("enddate"));
                    userOa.setMaritalstatus(object.getStr("maritalstatus"));
                    userOa.setDepartmentname(object.getStr("departmentname"));
                    userOa.setFolk(object.getStr("folk"));
                    userOa.setStatus(object.getStr("status"));
                    userOa.setBirthday(object.getStr("birthday"));
                    userOa.setAccounttype(object.getStr("accounttype"));
                    userOa.setJobcall(object.getStr("jobcall"));
                    userOa.setManagerid(object.getStr("managerid"));
                    userOa.setAssistantid(object.getStr("assistantid"));
                    userOa.setDepartmentcode(object.getStr("departmentcode"));
                    userOa.setBelongto(object.getStr("belongto"));
                    userOa.setEmail(object.getStr("email"));
                    userOa.setSeclevel(object.getStr("seclevel"));
                    userOa.setPolicy(object.getStr("policy"));
                    userOa.setJobtitle(object.getStr("jobtitle"));
                    userOa.setWorkcode(object.getStr("workcode"));
                    userOa.setSex(object.getStr("sex"));
                    userOa.setDepartmentid(object.getStr("departmentid"));
                    userOa.setHomeaddress(object.getStr("homeaddress"));
                    userOa.setMobile(object.getStr("mobile"));
                    userOa.setLastmoddate(object.getStr("lastmoddate"));
                    userOa.setEducationlevel(object.getStr("educationlevel"));
                    userOa.setIslabouunion(object.getStr("islabouunion"));
                    userOa.setLocationid(object.getStr("locationid"));
                    userOa.setRegresidentplace(object.getStr("regresidentplace"));
                    userOa.setDsporder(object.getStr("dsporder"));
                    userOaMapper.insert(userOa);
                }
            }
        }


    }

}
