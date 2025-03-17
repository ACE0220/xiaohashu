package cn.ace.xiaohashu.auth.controller;

import cn.ace.framework.biz.operationlog.aspect.ApiOperationLog;
import cn.ace.framework.common.response.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    @ApiOperationLog(description = "测试接口1")
    public Response<String> test() {
        return Response.success("Hello auth");
    }

    @PostMapping("/test2")
    @ApiOperationLog(description = "测试接口2")
    public Response<User> test(@RequestBody User user) {
        return Response.success(user);
    }

    @GetMapping("/test3")
    @ApiOperationLog(description = "测试接口3")
    public Response<User> test3(@RequestBody @Validated User user) {
        return Response.success(user);
    }
}
