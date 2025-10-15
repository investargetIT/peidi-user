package com.cyanrocks.boilerplate.controller;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cyanrocks.boilerplate.dao.mapper.TranspondMapper;
import com.cyanrocks.boilerplate.exception.BusinessException;
import com.cyanrocks.boilerplate.utils.OssUtils;
import com.cyanrocks.boilerplate.utils.http.HttpClientService;
import com.cyanrocks.boilerplate.utils.http.HttpResponseContent;
import com.cyanrocks.boilerplate.utils.http.HttpTimeoutConfig;
import com.cyanrocks.boilerplate.utils.http.HttpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @Author wjq
 * @Date 2024/12/10 16:53
 */
@RestController
@RequestMapping("/transpond")
@Api(tags = {"转发接口"})
public class TranspondController {

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private TranspondMapper transpondMapper;

    @Autowired
    private OssUtils ossUtils;

    @GetMapping("/newbi-session")
    @ApiOperation(value = "newbi-session")
    public String getNewbiSession(@RequestParam(value="username") String username,
                                  @RequestParam(value="password") String password){
        String url = "https://newbi.peidigroup.cn/api/session";
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("username", username);
        jsonObject.set("password", password);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println(e.getMessage() + "username:" + username + " password:" + password);
            return "";
        }
        JSONObject contentJson = JSONUtil.parseObj(content.getContent());
        System.out.println(contentJson);
        return contentJson.getStr("id");
    }

    @PostMapping("/newbi-user")
    @ApiOperation(value = "newbi-user")
    public void sendNewbiUser(@RequestParam(value="firstName") String firstName,
                                  @RequestParam(value="lastName") String lastName,
                               @RequestParam(value="email") String email,
                               @RequestParam(value="password") String password){
        String url = "https://newbi.peidigroup.cn/api/user";
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("first_name", firstName);
        jsonObject.set("last_name", lastName);
        jsonObject.set("email", email);
        jsonObject.set("password", password);
        HttpResponseContent content;
        try {
            content = httpClientService.doPost(url, null, JSONUtil.toJsonStr(jsonObject),
                    HttpUtils.initHttpClientContext(null, new HttpTimeoutConfig(300000)));
        } catch (Exception e) {
            System.out.println(e.getMessage() + "firstName:" + firstName + " lastName:" + lastName + " email:" + email + " password:" + password);
            throw new BusinessException(500, "转发失败");
        }
        System.out.println(content.getContent());
    }

    @GetMapping("/query-sql")
    @ApiOperation(value = "query-sql")
    public List<Map<String, Object>> querySql(@RequestParam(value="sql") String sql) throws UnsupportedEncodingException {
        if (!sql.startsWith("select") && !sql.startsWith("SELECT")){
            throw new BusinessException(500,"只允许执行查询语句");
        }
        sql = URLDecoder.decode(sql, "UTF-8").replace("\\n"," ");
        return transpondMapper.sql(sql);
    }

    @GetMapping("/download-oss")
    @ApiOperation(value = "下载pdf文件")
    public ResponseEntity<Object> downloadExcel(@RequestParam(value="objectName") String objectName,
                                                HttpServletRequest request, HttpServletResponse response) {
        byte[] fileContent = ossUtils.downloadFromOss(objectName);

        if (fileContent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        try {
            String[] objectNames = objectName.split("/");
            String fileName = objectNames[objectNames.length - 1];

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replace("+", "%20");
            String contentDisposition = String.format(
                    "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                    encodedFileName, encodedFileName
            );
            headers.add("Content-Disposition", contentDisposition);
        } catch (UnsupportedEncodingException e) {
            // 更建议使用日志记录而非直接打印堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件编码处理失败".getBytes(StandardCharsets.UTF_8));
        }
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
}
