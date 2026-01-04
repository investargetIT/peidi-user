package com.cyanrocks.boilerplate.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cyanrocks.boilerplate.utils.DingUtils;
import com.cyanrocks.boilerplate.utils.OaUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wjq
 * @Date 2024/10/15 15:21
 */
@RestController
@RequestMapping("/oa")
@Api(tags = {"oa接口"})
public class OaController {

    @Autowired
    private OaUtils oaUtils;

    @GetMapping("/test")
    @ApiOperation(value = "测试")
    public String test(@RequestParam(value="curpage") Integer curpage) {
//        oaUtils.restfulTest(page);
        JSONObject jsonObject = new JSONObject();
        JSONObject params = new JSONObject();
        params.set("pagesize",100);
        params.set("curpage",curpage);
        jsonObject.set("params",params);
        return oaUtils.restful("https://oa.peidibrand.com:4433","/api/hrm/resful/getHrmUserInfoWithPage", JSONUtil.toJsonStr(jsonObject));
    }


}
